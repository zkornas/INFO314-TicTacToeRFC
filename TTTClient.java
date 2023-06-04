import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TTTClient {
    private Socket tcpSocket = null;
    private DatagramSocket udpSocket = null;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
	public String hostname;
	public int port;
	public String protocol;
	public String clientIdentifier;
	public boolean inGame;

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
			protocol = "TCP";
			System.out.println("Established TCP connection to " + hostname + " on port " + port);
        } else if (type.equalsIgnoreCase("UDP")) {
            udpSocket = new DatagramSocket();
			protocol = "UDP";
			System.out.println("UDP Socket created");
            // in = new BufferedReader(new InputStreamReader(udpSocket.getInputStream()));
            // out = new PrintWriter(udpSocket.getOutputStream(), true);
        } else {
            throw new IllegalArgumentException("Invalid connection type.");
        }
    }
    
    // Method to send messages
    public void sendMessage(String message) {
		try {
			if (protocol.equals("TCP")) {
				out.println(message);

			} else {
				byte[] messageBytes = message.getBytes();
				InetAddress server = InetAddress.getByName(hostname);
				DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server, port);
				udpSocket.send(packet);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
            // Continue with other commands...
        }
    }

	public void sendHELO() {
		System.out.println("What would you like your client identifier to be? ");
		String identifier = scanner.nextLine();
		clientIdentifier = identifier;
		String message = "HELO 1 " + identifier + "\r\n";
		sendMessage(message);
		System.out.println("Sent HELO message to server");
	}

	public void startGame() throws IOException {
		sendHELO();
		while (true) {
			while (!inGame) {
				System.out.println("Available Commands:");
				System.out.println();
				System.out.println("  CREA				(create a game)");
				System.out.println("  JOIN <game-id>		(join a game)");
				System.out.println("  LIST <CURR/ALL>		(list current games/list all games)");
				System.out.println("  STAT <game-id>		(display status of a game)");
				System.out.println("  GDBY				(end session)");
		
				String command = scanner.nextLine();
				String[] commandParts = command.split(" ");
				if (commandParts[0].equals("CREA")) {
					command += " " + clientIdentifier;
				}
				sendMessage(command + "\r\n");
				String serverMessage = receiveMessage();
				processCommand(serverMessage);
			}
			if (inGame) {
				System.out.println("Available Commands:");
				System.out.println("  MOVE");
				System.out.println("  QUIT");
				System.out.println("  GDBY");
				String command = scanner.nextLine();
			}
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
			hostname = "127.0.0.1";
			port = 3116;
			protocol = "TCP";
		}

        try {
            // Create the client
            TTTClient client = new TTTClient(hostname, port);
            // Connect to the server
            client.establishConnection(protocol);
		
            // Start the interactive loop
			client.startGame(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}