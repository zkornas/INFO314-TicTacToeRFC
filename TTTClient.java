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
	public String clientId;
	public boolean inSession;
	public String currentGameId;

	public TTTClient(String hostname, int port) throws IOException {
		scanner = new Scanner(System.in);
		this.hostname = hostname;
		this.port = port;
		try {
			establishConnection();
		} catch (IOException ex) {
			System.out.println("Input/Output error. Restart the client");
			System.exit(1);
		}
	}
    
    // Method to establish connection
    private void establishConnection() throws IOException {
		tcpSocket = new Socket(hostname, port);
		in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
		out = new PrintWriter(tcpSocket.getOutputStream(), true);
		System.out.println("Established TCP connection to " + hostname + " on port " + port);
    }
    
    // Method to send messages
    public void sendMessage(String message) {
		try {
			System.out.println("sending message: " + message);
			out.println(message);
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
			default: 
				System.out.println("Unrecognized server message: " + receive);
        }
    }

    public void handleBord(String[] parts) {
        if (parts.length == 7) {
            System.out.println("Games ends, " + parts[parts.length - 1] + " wins!");
			String[] board = parts[parts.length - 2].split("\\|");
            System.out.println("_____________");
			System.out.println("| " + board[1] + " | " + board[2] + " | " + board[3] + " |");
			System.out.println("–––––––––––––");
			System.out.println("| " + board[4] + " | " + board[5] + " | " + board[6] + " |");
			System.out.println("–––––––––––––");
			System.out.println("| " + board[7] + " | " + board[8] + " | " + board[9] + " |");
			System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾");
			currentGameId = "";
        } else if (parts.length == 6){
            System.out.println("In game, waiting for " + parts[parts.length - 2] + " to make the next move");			
			String[] board = parts[parts.length - 1].split("\\|");
            System.out.println("_____________");
			System.out.println("| " + board[1] + " | " + board[2] + " | " + board[3] + " |");
			System.out.println("–––––––––––––");
			System.out.println("| " + board[4] + " | " + board[5] + " | " + board[6] + " |");
			System.out.println("–––––––––––––");
			System.out.println("| " + board[7] + " | " + board[8] + " | " + board[9] + " |");
			System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾");
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
        if (parts.length == 4) {
            System.out.println("The winner of the game is " + parts[2]);
        } else {
            System.out.println("No winner, stalemate ");
        }
		currentGameId = "";
		
    }

    public void handleJond (String[] parts) {
        // spl("joined successfully ")
        System.out.println(parts[1] + " has successfully joined the game with id " + parts[2]);
		currentGameId = parts[2];
    }

    public void handleSess (String[] parts) {
        // spl("session + num + created");
        System.out.println("You have created an unique session with id " + parts[2] + " over protocal " + parts[1]);
    }

    public void handleYRMV(String[] parts) {
        // System.out.println("Client " + parts[parts.length-1] + " has successfully made a move to game " + parts[parts.length - 2]);
		// Edited parts[1] to parts[2]
		if (parts[2].equals(clientId)) {
			System.out.println("It is now your turn to make a move.");
		} else {
			System.out.println("it is now " + parts[2] + "'s turn to move.");
		}
    }

    public void handleError(String[] parts) {
        System.out.println(parts[1]);
    }

	public void sendHELO() {
		System.out.println("What would you like your client identifier to be? ");
		String identifier = scanner.nextLine();
		clientId = identifier;
		String message = "HELO 1 " + identifier + "\r\n";
		sendMessage(message);
		System.out.println("Sent HELO message to server");
		System.out.println();
		inSession = true;
	}
	
	public void startGame() throws IOException, InterruptedException {
		Thread serverListenerThread = new Thread(() -> {
			try {
				while(true) {
					String message;
					while ((message = in.readLine()) != null) {
						System.out.println("received message: " + message);
						processCommandFromServer(message);
					}	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		serverListenerThread.start();
		
		sendHELO();
		while (inSession) {
			Thread.sleep(2000);
			System.out.println("Available Commands:");
			System.out.println("  CREA				(create a game)");
			System.out.println("  JOIN <game-id>		(join a game)");
			System.out.println("  LIST <CURR/ALL>		(list current games/list all games)");
			System.out.println("  STAT <game-id>		(display status of a game)");
			System.out.println("  MOVE <move>			((ingame) place a token on square <move>)");
			System.out.println("  GDBY				(end session)");

			String command = scanner.nextLine();
			String[] commandParts = command.split(" ");
			if (commandParts[0].equals("CREA")) {
				command += " " + clientId;
			} else if (commandParts[0].equals("MOVE")) {
				command = commandParts[0] + " " + currentGameId + " " + commandParts[1];
			} else if (commandParts[0].equals("QUIT")) {
				command = command + " " + currentGameId;
			} else if (commandParts[0].equals("GDBY")) {
				inSession = false;
			}
			sendMessage(command + "\r\n");
		}

		System.out.println("Ending session...");
		tcpSocket.close();
		System.exit(1);
	}
    
    public static void main(String[] args) {
        // Check the command-line arguments
		String hostname;
		int port;
		if (args.length == 3) {
			hostname = args[1];
			port = Integer.parseInt(args[2]);
		} else {
			hostname = "127.0.0.1";
			port = 3116;
		}

        try {
            // Create the client
            TTTClient client = new TTTClient(hostname, port);
            // Start the interactive loop
			client.startGame(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
