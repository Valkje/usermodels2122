package networking;

import java.io.IOException;

/**
 * The main class to start up the server for the Java component
 *
 * @author Gilles Lijnzaad
 */
public class ServerMain {
    public static Server server;
    public static Participant participant;
    public static int participantNumber;

    public static void main(String[] args) {
        try {
            server = new Server();
        } catch (IOException e) {
            System.err.println("Something went wrong when creating a server.");
            e.printStackTrace();
        }

        participantNumber = 38;
        participant = new Participant(participantNumber);
    }

}