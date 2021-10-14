# Contains all actions that the Python component can execute on behalf of the Java component.
# author: Gilles Lijnzaad
import pylink
import client
import time
from matplotlib import pyplot as plt
import pandas as pd


# global variables, will be changed by input_handler
display_x = 0
display_y = 0
edf_file_name = ""
trial_number = 0
trial_id = ""

raw_data = []
plot_dat_short = []
plot_dat_long = []
plot_dat_upper = []
plot_dat_lower = []

connected = False

while not connected:
    try:
        pylink.EyeLink(trackeraddress=None)
        connected = True
    except RuntimeError:
        print("Connection to eye tracker failed, trying again...")

client.start_receiving_thread()


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
	tracker = pylink.getEYELINK()
	if not tracker.isConnected():
		report_error("ABORT EXPERIMENT")
                                            
	tracker.setOfflineMode()
	recording_error = tracker.startRecording(1, 1, 1, 1)
	print("started recording")
	if recording_error:                             # 0 if successful, error code otherwise
		report_error("TRIAL ERROR")

	pylink.beginRealTimeMode(100)                   # tells Windows to give priority to this

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
	tracker = pylink.getEYELINK()

	pylink.endRealTimeMode()
	pylink.pumpDelay(100)
	tracker.stopRecording()
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

	# Save data
	dataDict = {'raw':raw_data,
				'long':plot_dat_long,
				'short':plot_dat_short,
				'lowerCI':plot_dat_lower,
				'upperCI':plot_dat_upper}
	
	pdFrame = pd.DataFrame(data=dataDict)
	pdFrame.to_csv(f"./outputPandas_{trial_number}.csv",index=False,header=True)

	# Plot data
	plt.plot(range(len(raw_data)),raw_data,color="black")
	plt.plot(range(len(plot_dat_long)),plot_dat_long,color="blue")
	plt.plot(range(len(plot_dat_upper)),plot_dat_upper,color="blue",linestyle='dashed')
	plt.plot(range(len(plot_dat_lower)),plot_dat_lower,color="blue",linestyle='dashed')
	plt.plot(range(len(plot_dat_short)),plot_dat_short,color="red")
	plt.title("Long term trend vs. short term change")
	plt.xlabel("Samples")
	plt.ylabel("Pupil size")
	plt.legend(["Raw data","Long-term trend","Upper decision boundary",
				"Lower decision boundary","Short-term trend"],loc="upper right")
	plt.show()

def query(target):
	# This method can be called from the client on behalf of the
	# server - if the Java server sends a query/ message to the
	# client!
	# It queries the eye-tracker and then tells the client to
	# send an answer to the Java server again!
	# The Java server then will update a public float with
	# the latest pupil size

	# see the pylink.Eyelink documentation Jelmer linked on
	# Nestor for details on the methods below.
	tracker = pylink.getEYELINK()
	sample = tracker.getNewestSample()

	# We can select which eye we want to record in the eye-tracking
	# software so we can just pick it directly here.
	try:
		eye = sample.getLeftEye()

		if target == "PUPIL_SIZE":
			response = eye.getPupilSize()
		else:
			report_error("Invalid target")

	except AttributeError:
		response = 0
		#print("WARNING: Empty sample")

	# Inform client to send back Pupil size to Java server.
	client.send("PUPIL_SIZE " + str(response))


def save_for_plot(message):
	if message.startswith("LONG "):
		value = message[len("LONG "):]
		plot_dat_long.append(float(value))
	elif message.startswith("SHORT "):
		value = message[len("SHORT "):]
		plot_dat_short.append(float(value))
	elif message.startswith("UPPER "):
		value = message[len("UPPER "):]
		plot_dat_upper.append(float(value))
	elif message.startswith("LOWER "):
		value = message[len("LOWER "):]
		plot_dat_lower.append(float(value))
	elif message.startswith("RAW "):
		value = message[len("RAW "):]
		raw_data.append(float(value))

def report_error(error_message):
	client.send(error_message)
