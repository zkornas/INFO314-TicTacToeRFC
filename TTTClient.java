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
        } else if (type.equalsIgnoreCase("UDP")) {
            udpSocket = new DatagramSocket();
			protocol = "UDP";
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
    
    public void processCommandFromServer(String receive) {
        String[] parts = command.split(" ");
        switch(parts[0]) {
            case "BORD":
                handleBord(parts);
            case "GAMS":
                handleGams(parts);
            case "TERM":
                handleTerm(parts);
            case "JOND":
                handleJond(parts);
            case "SESS":
                handleSess(parts);
            case "YRMV":
                handleYRMV(parts);
            case "ERROR:":
                handleError(parts);
        }
    }

    public void handleBord(String[] parts) {
        if (parts.length == 7) {
            System.out.println("Games ends, " + parts[parts.length - 1] + " wins!");
            System.out.println(parts[parts.length - 2]);
        } else if (parts.length == 6){
            System.out.println("In game, waiting for " + parts[parts.length - 2] + " to make the next move");
            System.out.println(parts[parts.length - 1]);
        } else if (parts.length == 3) {
            System.out.println("The game " + parts[1] + "haven't been started since there's only one player with id " + parts[2]);
        }
    }

    public void handleGams (String[] parts) {
        // this.gamesLookingForPeople = parts (could be a arraylist, just update every time)
        // spl
        StringBuilder sb = new StringBuilder();
        for (String game : parts) {
            if (game.equals("GAMS")) {
                continue;
            } 
            sb.append(game + " ");
        }
        System.out.println("Games you can join: " + sb.toString());
    }

    public void handleTERM(String[] parts) {
        if (parts.length() == 3) {
            System.out.println("The winner of the game is " + parts[parts.size() - 2]);
        } else {
            System.out.println("No winner, stalemate ");
        }
    }

    public void handleJond (String[] parts) {
        // spl("joined successfully ")
        System.out.println(parts[1] + " has successfully joined the game with id " + parts[2]);
    }

    public void handleSess (String[] parts) {
        // spl("session + num + created");
        System.out.println("You have created an unique session with id " + parts[2] + " over protocal " + parts[1]);
    }

    public void handleYRMV(String[] parts) {
        System.out.println("Client " + parts[parts.length-1] + " has successfully made a move to game " + parts[parts.length - 2]);
    }

    public void handleError(String[] parts) {
        System.out.println(parts[1]);
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
				System.out.println("  JOIN <game-id>");
				System.out.println("  CREA");
				System.out.println("  LIST <CURR/ALL>");
				System.out.println("  STAT <game-id>");
				System.out.println("  GDBY");
		
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
