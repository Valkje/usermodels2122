package networking;

import java.io.*;
import java.net.*;

/**
 * Server for the Java component. Allows the Java component to communicate with the Python component
 *
 * @author Gilles Lijnzaad
 */
public class Server {
    private final int PORT = 9000;
    private boolean running = false;
    private ServerSocket server;
    private Socket client;
    public int posX;
    public int posY;
    public double lastPupilSample;
    // The three parameters below are accessed by the adaptive automation system
    public double SV;
    public double LV;
    public double RMSE;

    public Server() throws IOException {
        server = new ServerSocket(PORT);
        System.out.println("Launched server");
        running = true;
        connectToClient();
    }
    
    private void connectToClient() {
        try {
            client = server.accept();
            System.out.println("Accepted client");
        } catch (IOException e) {
            System.err.println("Something went wrong in accepting the client.");
            e.printStackTrace();
        }

        Thread t = new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String input = in.readLine();       // stalls if there is no input
                while (running) {
                    handleInput(input);
                    input = in.readLine();
                }
            } catch (IOException e) {
                System.err.println("Something went wrong in receiving input from the client.");
                e.printStackTrace();
            }
        });
        t.start();
    }

    private void handleInput(String input) {
        /**
         * Here we should listen to messages that come back from Python.
         */
//        System.out.println("Input from python");
//        System.out.println(input);
        if (input.startsWith("POS_X ")) {
            String posXString = input.substring("POS_X ".length());
            posX = Integer.parseInt(posXString);
        }
        if (input.startsWith("POS_Y ")) {
            String posYString = input.substring("POS_Y ".length());
            posY = Integer.parseInt(posYString);
        } if (input.startsWith("PUPIL_SIZE")) {
            String pupilString = input.substring("PUPIL_SIZE ".length());
            lastPupilSample = Double.parseDouble(pupilString);
        } if (input.startsWith("MODEL_VAL ")) {
            String modelString = input.substring("MODEL_VAL ".length());
            if (modelString.startsWith("SV ")) {
                String valueString = modelString.substring("SV ".length());
                SV = Double.parseDouble(valueString);
            } else if (modelString.startsWith("LV ")) {
                String valueString = modelString.substring("LV ".length());
                LV = Double.parseDouble(valueString);
            } else if (modelString.startsWith("RMSE ")) {
                String valueString = modelString.substring("RMSE ".length());
                RMSE = Double.parseDouble(valueString);
            }
        }
    }

    public void send(String message) {
        /**
         * Use this to send a message to the client from anywhere in the Java code!
         * For example: query/ PUPIL_SIZE to get the pupil size from the Eye-tracker.
         */
        try {
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(message);
        } catch (IOException e){
            System.err.println("Something went wrong with sending output to the client.");
            e.printStackTrace();
        }
        
//        System.out.println(message);
    }

}
