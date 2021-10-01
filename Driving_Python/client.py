# Establishes a connection between the Python component (client) and the Java component (server)
# author: Gilles Lijnzaad

import socket
import threading

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
HOST = "localhost"
PORT = 9000
print("Launched client")
sock.connect((HOST, PORT))
print("Connected to server")


def receive():
	while True:
		message = sock.recv(1024).decode()
		for line in message.splitlines():
			from input_handler import handle_input
			handle_input(line)

def start_receiving_thread():
	t = threading.Thread(target=receive)
	t.start()

def send(message):
	# We use this function to send back information to the
	# Java part. For example the last pupil size we get from the eye tracker!
	message += "\n"
	sock.sendall(message.encode())

send("hello")