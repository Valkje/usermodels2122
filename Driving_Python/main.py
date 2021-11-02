# Contains all actions that the Python component can execute on behalf of the Java component.
# author: Gilles Lijnzaad
"""
Originally, Gilles split the code below across 3 files: client, server, inputhandler.
We did notice that this would lead to some ghost threads being spawned due to
recursive imports. Thus, to limit the number of possible errors that could happen
we moved everything into one file.

Most of the functions were left un-changed, but we did add some of our own
methods to allow for online processing of the data available through the link.
"""
import pylink
import time
from matplotlib import pyplot as plt
import pandas as pd
import socket
import threading
import MovingAverage
import MovingRMSE


# # GLOBAL VARIABLES ORIGINAL MAIN
display_x = 0
display_y = 0
edf_file_name = ""
trial_number = 0
trial_id = ""

# GLOBAL VARIABLES ORIGINAL CLIENT
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
HOST = "localhost"
PORT = 9000
TRACKER_LOCK = threading.Lock()
TRACKER_RECORDING = False
STOP_THREADS = False

# Pupil data tracking
PREVIOUS_SAMPLE = None
raw_data_C = []
plot_dat_short_C = []
plot_dat_long_C = []
plot_dat_UPPER_C = []
plot_dat_LOWER_C = []


shortTermTrend = MovingAverage.MovingAverage(600)
longTermTrend = MovingAverage.MovingAverage(15000)
longTermRMSE = MovingRMSE.MovingRMSE()


print("Launched SOCKET")
sock.connect((HOST, PORT))
print("SOCKET connected")

connected = False

while not connected:
    try:
        pylink.EyeLink(trackeraddress=None)
        connected = True
    except RuntimeError:
        print("Connection to eye tracker failed, trying again...")


def prepare_experiment():
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	tracker.openDataFile(edf_file_name)

	pylink.flushGetkeyQueue()                               # cleanup
	tracker.setOfflineMode()                                # idle mode

	tracker.sendCommand("screen_pixel_coords =  0 0 %d %d" % (display_x, display_y))
	tracker.sendMessage("DISPLAY_COORDS  0 0 %d %d" % (display_x, display_y))

	# Assumption for next lines: eye-tracker is version 6.10
	tracker.sendCommand("select_parser_configuration 0")
	tracker.sendCommand("file_event_filter = LEFT,RIGHT,FIXATION,SACCADE,BLINK,MESSAGE,BUTTON")
	tracker.sendCommand("file_sample_data  = LEFT,RIGHT,GAZE,AREA,GAZERES,STATUS,HTARGET")

	# Select what data is available over the link (for online data accessing)
	link_event_flags = 'LEFT,RIGHT,FIXATION,SACCADE,BLINK,BUTTON,FIXUPDATE,INPUT'
	link_sample_flags = 'LEFT,RIGHT,GAZE,GAZERES,AREA,HTARGET,STATUS,INPUT'
	tracker.sendCommand("link_event_filter = %s" % link_event_flags)
	tracker.sendCommand("link_sample_data = %s" % link_sample_flags)

	tracker.sendCommand("button_function 5 'accept_target_fixation'")

	tracker.sendCommand("pupil_size_diameter = YES")

	# This gave us problems so we do this manually.
	# do_calibration()


def do_calibration():
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	pylink.openGraphics()
	pylink.setCalibrationColors((0, 0, 0), (255, 255, 255))
	pylink.setCalibrationSounds("", "", "")
	tracker.doTrackerSetup()
	pylink.closeGraphics()


def prepare_trial():
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	record_message = "record_status_message 'Trial %d: %s'" % (trial_number, trial_id)
	tracker.sendCommand(record_message)
	tracker.sendMessage("TRIALID " + trial_id)
	print(record_message)

def drift_correction():
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	pylink.openGraphics()
	pylink.setDriftCorrectSounds("", "off", "off")
	drift_result = tracker.doDriftCorrect(int(display_x/2), int(display_y/2), 1, 1)
	pylink.closeGraphics()
	if drift_result == 27:
		drift_correction()


def start_recording():
	global TRACKER_RECORDING
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")
                                            
	tracker.setOfflineMode()
	recording_error = tracker.startRecording(1, 1, 1, 1)
	print("started recording")
	if recording_error:                             # 0 if successful, error code otherwise
		report_error("TRIAL ERROR")

	pylink.beginRealTimeMode(100)                   # tells Windows to give priority to this
	TRACKER_RECORDING = True
	if not tracker.waitForBlockStart(1000, 1, 0):
		report_error("TRIAL ERROR")


def send_SYNCTIME(start_time):
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	current_time = int(time.time() * 1000)
	sync_time = current_time - start_time
	tracker.sendMessage(str(sync_time) + " SYNCTIME")


def send_tracker(message):
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")

	tracker.sendMessage(message)


def end_trial():
	global TRACKER_RECORDING
	tracker = pylink.getEYELINK()

	pylink.endRealTimeMode()
	pylink.pumpDelay(100)
	tracker.stopRecording()
	TRACKER_RECORDING = False
	while tracker.getkey():
		pass


def end_experiment():
	tracker = pylink.getEYELINK()

	if tracker is not None:
		tracker.setOfflineMode()
		pylink.msecDelay(500)

	tracker.closeDataFile()
	tracker.receiveDataFile(edf_file_name, edf_file_name)
	tracker.close()
	close_threads(trial_number=trial_number)
	

def query(target):
	global PREVIOUS_SAMPLE
	"""
	This method is used to get the latest sample from the Eye-tracker.
	We use this one to update the averages and RMSEs for the AAS.
	Java then queries the Python parts to get the latest average values and
	RMSEs. Initially we calculated those in Java as well, however the Java
	parts are slowed down because they have to wait for ACT-R. Thus
	calculations now happen all in Python while Java only executes the
	actual AAS decision.

	see the pylink.Eyelink documentation Jelmer linked on
	Nestor and the pygaze sample method for details on the methods below.
	"""
	
	tracker = pylink.getEYELINK()
	sample = tracker.getNewestSample()

	# Handle the attribute error we kept catching.
	# In pygaze this is the only check when sampling.
	# For us that resulted in duplicated samples. So
	# we also check the time in the next check :)
	# See: https://github.com/esdalmaijer/PyGaze/blob/master/pygaze/_eyetracker/libeyelink.py
	if sample is None:
		return -1

	# Now that we know there is a sample, we should check whether
	# it contains new information (getTime() in the docs)
	# See: https://github.com/ericandersonr/Landmark_LCIRT_Codebase/blob/master/LCIRT_EyelinkSync_LSL.py
	if PREVIOUS_SAMPLE is not None:
		if PREVIOUS_SAMPLE.getTime() == sample.getTime():
			return -1
	
	# We got a sample with new information!
	PREVIOUS_SAMPLE = sample

	# We can select which eye we want to record in the eye-tracking
	# software so we can just pick it directly here.
	response = 0
	eye = sample.getLeftEye()

	if target == "PUPIL_SIZE":
		response = eye.getPupilSize()
	else:
		report_error("Invalid target")

	# Inform client to send back Pupil size to Java server. [Deprecated]
	# client.send("PUPIL_SIZE " + str(response))
	return response

def fetch_raw_avg_rmse():
	"""
	Sends the latest values back to Java!
	"""
	SV = shortTermTrend.getCurrentValue()
	LV = longTermTrend.getCurrentValue()
	RMSE = longTermRMSE.getCurrentValue()

	send(f"MODEL_VAL SV {SV}")
	send(f"MODEL_VAL LV {LV}")
	send(f"MODEL_VAL RMSE {RMSE}")


def report_error(error_message):
	send(error_message)


########################################OLD INPUT HANDLER########################################################

def handle_input(input_string):
	# This function gets called once the client receives
	# a message from the Java part! We implement a check here
	# that tells the main python interface to query the
	# eyetracker for the latest pupil size!
	if input_string.startswith("info/ "):
		information = input_string[len("info/ "):]
		set_info(information)
	elif input_string.startswith("do/ "):
		command = input_string[len("do/ "):]
		perform_action(command)
	elif input_string.startswith("send/ "):
		message = input_string[len("send/ "):]
		send_tracker(message)
	elif input_string.startswith("query/ "):
		message = input_string[len("query/ "):]
		query(message)
	elif input_string.startswith("fetch/"):
		fetch_raw_avg_rmse()
	else:
		print("ERROR: Invalid message")


def set_info(information):
	global edf_file_name
	global display_x
	global display_y
	global trial_number
	global trial_id

	if information.startswith("EDF "):
		edf_file_name = information[len("EDF "):]
	elif information.startswith("DIM_X "):
		dimx_string = information[len("DIM_X "):]
		display_x = int(dimx_string)
	elif information.startswith("DIM_Y "):
		dimy_string = information[len("DIM_Y "):]
		display_y = int(dimy_string)
	elif information.startswith("TRIAL_NUMBER "):
		number_string = information[len("TRIAL_NUMBER "):]
		trial_number = int(number_string)
	elif information.startswith("TRIAL_ID "):
		trial_id = information[len("TRIAL_ID "):]
	elif information.startswith("START TIME "):
		time_string = information[len("START TIME "):]
		send_SYNCTIME(int(time_string))
	else:
		print("ERROR: Invalid info")


command_to_action = {
	"PREPARE EXPERIMENT" : prepare_experiment,
	"PREPARE TRIAL" : prepare_trial,
	"DRIFT CORRECT" : drift_correction,
	"START RECORDING" : start_recording,
	"END TRIAL" : end_trial,
	"END EXPERIMENT" : end_experiment
}


def perform_action(command):
	command_to_action[command]()


############################################# OLD CLIENT PARTS#########################################################
def queryTracker():
	"""
	This function is called in one of the threads spawned in the beginning
	and repeatedly queries the eye-tracker (if currently no messages are written
	to it as requested from JAVA).

	Also updates the moving averages and cumulative RMSE and collects values for
	plotting.
	"""
	global TRACKER_LOCK
	global STOP_THREADS
	while not STOP_THREADS:
		if TRACKER_RECORDING:
			TRACKER_LOCK.acquire()
			newestSample = query("PUPIL_SIZE")
			

			# Skip invalid samples
			if newestSample == -1:
				TRACKER_LOCK.release()
				continue

			# Handle all remaining samples
			raw_data_C.append(newestSample)
			if newestSample != 0:
				shortTermTrend.update(newestSample)
				longTermTrend.update(newestSample)
				longTermRMSE.update(longTermTrend.getCurrentValue(),shortTermTrend.getCurrentValue())

			plot_dat_long_C.append(longTermTrend.getCurrentValue())
			plot_dat_short_C.append(shortTermTrend.getCurrentValue())
			plot_dat_LOWER_C.append(longTermTrend.getCurrentValue() - (1.15 * longTermRMSE.getCurrentValue()))
			plot_dat_UPPER_C.append(longTermTrend.getCurrentValue() + (1.15 * longTermRMSE.getCurrentValue()))
			TRACKER_LOCK.release()
	
def receive():
	"""
	Original function by Gilles that delegates messages from Java,
	updated to aknowledge and set the lock on the tracker.
	"""
	global TRACKER_LOCK
	global STOP_THREADS
	while not STOP_THREADS:
		message = sock.recv(1024).decode()
		TRACKER_LOCK.acquire()
		for line in message.splitlines():
			handle_input(line)
		TRACKER_LOCK.release()

def close_threads(trial_number):
	"""
	Close threads, save and plot data.
	"""
	global STOP_THREADS
	STOP_THREADS = True
	# Save data
	print(len(raw_data_C))
	print(len(plot_dat_long_C))
	print(len(plot_dat_short_C))
	print(len(plot_dat_LOWER_C))
	print(len(plot_dat_UPPER_C))
	
	dataDict = {'raw':raw_data_C,
				'long':plot_dat_long_C,
				'short':plot_dat_short_C,
				'UPPER':plot_dat_UPPER_C,
				'LOWER':plot_dat_LOWER_C}
	
	pdFrame = pd.DataFrame(data=dataDict)
	pdFrame.to_csv(f"./outputPandas_{trial_number}.csv",index=False,header=True)
	
	# Plot data
	plt.plot(range(len(raw_data_C)),raw_data_C,color="black")
	plt.plot(range(len(plot_dat_long_C)),plot_dat_long_C,color="blue")
	plt.plot(range(len(plot_dat_short_C)),plot_dat_short_C,color="red")
	plt.plot(range(len(plot_dat_LOWER_C)),plot_dat_LOWER_C,color="blue",linestyle='dashed')
	plt.plot(range(len(plot_dat_UPPER_C)),plot_dat_UPPER_C,color="blue",linestyle='dashed')

	plt.title("Long term trend vs. short term change")
	plt.xlabel("Samples")
	plt.ylabel("Pupil size")
	plt.legend(["Raw data","Long-term trend","Short-term trend"],loc="upper right")
	plt.show()

def start_receiving_thread():
	t = threading.Thread(target=receive)
	t.start()
	t2 = threading.Thread(target=queryTracker)
	t2.start()

def send(message):
	"""
	We use this function by Gilles to send back information to the
	Java part. For example the current moving average values and the
	RMSE values.
	"""
	message += "\n"
	sock.sendall(message.encode())


# Start listening to JAVA and prepare querying eye-tracker.
start_receiving_thread()