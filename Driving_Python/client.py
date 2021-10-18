# Establishes a connection between the Python component (client) and the Java component (server)
# author: Gilles Lijnzaad

import socket
import threading
import MovingAverage
import MovingRMSE
from matplotlib import pyplot as plt
import pandas as pd

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
HOST = "localhost"
PORT = 9000
TRACKER_LOCK = threading.Lock()
TRACKER_RECORDING = False
STOP_THREADS = False

# Pupil data tracking
raw_data_C = []
plot_dat_short_C = []
plot_dat_long_C = []
plot_dat_RMSE_C = []

shortTermTrend = MovingAverage.MovingAverage(250)
longTermTrend = MovingAverage.MovingAverage(1500)
longTermRMSE = MovingRMSE.MovingRMSE(1500)

print("Launched client")
sock.connect((HOST, PORT))
print("Connected to server")

def queryTracker():
	"""
	Query the eye-tracker.
	"""
	global TRACKER_LOCK
	global STOP_THREADS
	while not STOP_THREADS:
		if TRACKER_RECORDING:
			from main import query

			TRACKER_LOCK.acquire()
			newestSample = query("PUPIL_SIZE")
			TRACKER_LOCK.release()
			
			# Skip invalid samples
			if newestSample == -1:
				continue

			# Handle all remaining samples
			raw_data_C.append(newestSample)
			if newestSample != 0:
				shortTermTrend.update(newestSample)
				longTermTrend.update(newestSample)
				longTermRMSE.update(longTermTrend.getCurrentValue(),newestSample)
			
			plot_dat_long_C.append(longTermTrend.getCurrentValue())
			plot_dat_short_C.append(shortTermTrend.getCurrentValue())
			plot_dat_RMSE_C.append(longTermRMSE.getCurrentValue())

def receive():
	"""
	Handle Java messages.
	"""
	global TRACKER_LOCK
	global STOP_THREADS
	while not STOP_THREADS:
		message = sock.recv(1024).decode()
		TRACKER_LOCK.acquire()
		for line in message.splitlines():
			from input_handler import handle_input
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
	print(len(plot_dat_RMSE_C))
	"""
	dataDict = {'raw':raw_data_C,
				'long':plot_dat_long_C,
				'short':plot_dat_short_C,
				'RMSE':plot_dat_RMSE_C}
	
	pdFrame = pd.DataFrame(data=dataDict)
	pdFrame.to_csv(f"./outputPandas_{trial_number}.csv",index=False,header=True)
	"""
	# Plot data
	
	plt.plot(range(len(raw_data_C)),raw_data_C,color="black")
	plt.plot(range(len(longTermTrend.history)),longTermTrend.history,color="blue")
	plt.plot(range(len(shortTermTrend.history)),shortTermTrend.history,color="red")
	plt.title("Long term trend vs. short term change")
	plt.xlabel("Samples")
	plt.ylabel("Pupil size")
	plt.legend(["Raw data","Long-term trend","Upper decision boundary",
				"Lower decision boundary","Short-term trend"],loc="upper right")
	plt.show()

def start_receiving_thread():
	t = threading.Thread(target=receive)
	t.start()
	t2 = threading.Thread(target=queryTracker)
	t2.start()

def send(message):
	# We use this function to send back information to the
	# Java part. For example the last pupil size we get from the eye tracker!
	message += "\n"
	sock.sendall(message.encode())
