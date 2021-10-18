# Handles input from Java component
# author: Gilles Lijnzaad

import main

# example of messages:
# info/ TRIAL_NUMBER 14
# do/ DO CALIBRATION


def handle_input(input_string):
	# This function gets called once the client receives
	# a message from the Java part! We implement a check here
	# that tells the main python interface to query the
	# eyetracker for the latest pupil size!
	#print(input_string)
	if input_string.startswith("info/ "):
		information = input_string[len("info/ "):]
		set_info(information)
	elif input_string.startswith("do/ "):
		command = input_string[len("do/ "):]
		perform_action(command)
	elif input_string.startswith("send/ "):
		message = input_string[len("send/ "):]
		main.send_tracker(message)
	elif input_string.startswith("query/ "):
		message = input_string[len("query/ "):]
		main.query(message)
	elif input_string.startswith("plot/ "):
		message = input_string[len("plot/ "):]
		main.save_for_plot(message)
	elif input_string.startswith("fetch/"):
		main.fetch_raw_avg_rmse()
	else:
		print("ERROR: Invalid message")


def set_info(information):
	if information.startswith("EDF "):
		main.edf_file_name = information[len("EDF "):]
	elif information.startswith("DIM_X "):
		dimx_string = information[len("DIM_X "):]
		main.display_x = int(dimx_string)
	elif information.startswith("DIM_Y "):
		dimy_string = information[len("DIM_Y "):]
		main.display_y = int(dimy_string)
	elif information.startswith("TRIAL_NUMBER "):
		number_string = information[len("TRIAL_NUMBER "):]
		main.trial_number = int(number_string)
	elif information.startswith("TRIAL_ID "):
		main.trial_id = information[len("TRIAL_ID "):]
	elif information.startswith("START TIME "):
		time_string = information[len("START TIME "):]
		main.send_SYNCTIME(int(time_string))
	else:
		print("ERROR: Invalid info")


command_to_action = {
	"PREPARE EXPERIMENT" : main.prepare_experiment,
	"PREPARE TRIAL" : main.prepare_trial,
	"DRIFT CORRECT" : main.drift_correction,
	"START RECORDING" : main.start_recording,
	"END TRIAL" : main.end_trial,
	"END EXPERIMENT" : main.end_experiment
}


def perform_action(command):
	command_to_action[command]()
