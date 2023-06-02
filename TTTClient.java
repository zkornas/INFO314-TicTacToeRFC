import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TTTClient {
    private Socket tcpSocket = null;
    private DatagramSocket udpSocket = null;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
	private String hostname;
	private int port;

	public TTTClient(String hostname, int port) {
		scanner = new Scanner(System.in);
		this.hostname = hostname;
		this.port = port;
	}
    
    // Method to establish connection
    public void establishConnection(String type) throws IOException {
        // Choose the connection type
        if (type.equalsIgnoreCase("TCP")) {
            tcpSocket = new Socket(hostname, port);
            in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            out = new PrintWriter(tcpSocket.getOutputStream(), true);
        } else if (type.equalsIgnoreCase("UDP")) {
            udpSocket = new DatagramSocket();
            // in = new BufferedReader(new InputStreamReader(udpSocket.getInputStream()));
            // out = new PrintWriter(udpSocket.getOutputStream(), true);
        } else {
            throw new IllegalArgumentException("Invalid connection type. Only TCP or UDP is allowed.");
        }
    }
    
    // Method to send messages
    public void sendMessage(String message) {
        out.println(message);
    }
    
    // Method to receive messages
    public String receiveMessage() throws IOException {
        return in.readLine();
    }
    
    // Method to process commands
    public void processCommand(String command) {
        // Split the command into parts
        String[] parts = command.split(" ");

        switch(parts[0]) {
            case "BORD":
                // Handle BORD command
                break;
            case "CREA":
                // Handle CREA command
                break;
            // Continue with other commands...
        }
    }
    
    public static void main(String[] args) {
        // Check the command-line arguments for protocol type
		String protocol;
		String hostname;
		int port;
		if (args.length == 3) {
			protocol = args[0];
			hostname = args[1];
			port = Integer.parseInt(args[2]);
		} else {
			hostname = "localhost";
			port = 3316;
			protocol = "TCP";
		}

        try {
            // Create the client
            TTTClient client = new TTTClient(hostname, port);
            // Connect to the server
            client.establishConnection(protocol);

            // Start the interactive loop

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}