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
        if (input.startsWith("POS_X ")) {
            String posXString = input.substring("POS_X ".length());
            posX = Integer.parseInt(posXString);
        }
        if (input.startsWith("POS_Y ")) {
            String posYString = input.substring("POS_Y ".length());
            posY = Integer.parseInt(posYString);
        }
    }

    public void send(String message) {
        
        try {
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            out.println(message);
        } catch (IOException e){
            System.err.println("Something went wrong with sending output to the client.");
            e.printStackTrace();
        }
        
        System.out.println(message);
    }

}
