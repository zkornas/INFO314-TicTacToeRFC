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
	public boolean inSession;

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
				System.out.println("sending message: " + message);
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
        String[] parts = receive.split(" ");
        switch(parts[0]) {
            case "BORD":
                handleBord(parts);
				break;
            case "GAMS":
                handleGams(parts);
				break;
            case "TERM":
                handleTERM(parts);
				break;
            case "JOND":
                handleJond(parts);
				break;
            case "SESS":
                handleSess(parts);
				break;
            case "YRMV":
                handleYRMV(parts);
				break;
            case "ERROR:":
                handleError(parts);
				break;
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
        if (parts.length == 3) {
            System.out.println("The winner of the game is " + parts[parts.length - 2]);
        } else {
            System.out.println("No winner, stalemate ");
        }
		inGame = false;
    }

    public void handleJond (String[] parts) {
        // spl("joined successfully ")
        System.out.println(parts[1] + " has successfully joined the game with id " + parts[2]);
		inGame = true;
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
		inSession = true;
	}
	
	public void startGame() throws IOException, InterruptedException {
		Thread serverListenerThread = new Thread(() -> {
			try {
				while(true) {
					if (protocol.equals("TCP")) {
						String message;
						while ((message = in.readLine()) != null) {
							System.out.println("receieve message: " + message);
							processCommandFromServer(message);
						}
					} else {
						byte[] buffer = new byte[1024];
						DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
						while (true) {
							udpSocket.receive(dp);
							String message = new String(dp.getData(), 0, dp.getLength(), "UTF-8");
							processCommandFromServer(message);
						}
					}	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		serverListenerThread.start();
		
		sendHELO();
		while (inSession) {
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
					inGame = true;
				}
				sendMessage(command + "\r\n");
				if (commandParts[0].equals("GDBY")) {
					inSession = false;
				}
				Thread.sleep(3000);
			}
			while (inGame) {
				System.out.println("Available Commands:");
				System.out.println("  MOVE");
				System.out.println("  QUIT");
				System.out.println("  GDBY");
				String command = scanner.nextLine();
				String[] commandParts = command.split(" ");
				sendMessage(command + "\r\n");
				if (commandParts[0].equals("GDBY")) {
					inGame = false;
					inSession = false;
				}
			}
			Thread.sleep(3000);
		}
		if (protocol.equals("TCP")) tcpSocket.close();
		else udpSocket.close();
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