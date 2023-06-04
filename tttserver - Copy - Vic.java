import java.io.*;
import java.net.*;
import java.util.*;

public class tttserver {

    public static int PORT = 3116;
    public static Boolean LISTEN = true;
    public static HashMap <Integer, String> sessionsAndClients = new HashMap<Integer, String>();
    public static HashMap <String, Socket> clientSockets = new HashMap<>();
    public static int sessionID = 0;
    public static final int version = 1;
    public static HashMap <Integer, String[]> games = new HashMap<>();
    public static int gameID = 0;

// Open TCP Socket and wait for connection
// Open UDP socket and wait for connection

    public static void main(String[] args) throws Exception {
        ServerSocket servSockT = new ServerSocket(PORT);
        DatagramSocket servSockU = new DatagramSocket(PORT);

        Socket socket = null;

        while((socket = servSockT.accept()) != null) {

        Thread t1 = new Thread(new MyRunnableTCP(socket));
        //t1.setDaemon(true);
        //Thread t2 = new Thread(new MyRunnableUDP(servSockU));
        //t2.setDaemon(true);

        t1.start();
        //t2.start();

        //t1.join();
        //t2.join();

        }


        servSockU.close();
        servSockT.close();
    }

    public static class MyRunnableTCP implements Runnable {
        private Socket sock;
        
        public MyRunnableTCP(Socket s) {
            this.sock = s;
        }
    
        public void run() {
            try {

                System.out.println("Listening for TCP connection on port " + 3116);
                //Socket sock = soc.accept();
                System.out.println("Connection Successful!");

                while (true) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                    StringBuilder inputData = new StringBuilder();

                    String line;

                    // There is a problem here, while the connection stays open
                    // with the client, this loops forever as the input is never
                    // null, until it times out.

                    // While loop should work correct

                    while (in.ready()) {
                        inputData.append((char) in.read());
                    }

                    // Save or process the received data as needed
                    String savedData = inputData.toString().trim();

                    if (!savedData.trim().isEmpty()) {

                    System.out.println("Received data: " + savedData);

                    // Creates output object with Socket
                    PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

                    // Split savedData and grab first word
                    String[] message = savedData.split(" ");

                    if (message[0].equals("HELO")) {
                        System.out.println("Invoking handleClient");
                        startSession(message, sock, out);
                        // out.close();
                    } else if (!message[1].equals(version)) {
                        out.println("Error: Invalid version.");
                        // out.close();
                    } else if (message[1].equals("CREA")){
                        createGame(message, sock, out);
                    } else if (message[1].equals("LIST")){
                        listGames(message, sock, out);
                    }
                    /////////////////////
                    else if (message[1].equals("QUIT")){
                        quitGame(message, sock, out);
                    }
                    
                    // Terminate the loop if "exit" is received
                    if (savedData.equals("exit")) {
                        LISTEN = false;
                        break;
                    }
                }
            }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        // Handles response to client
        public static void startSession(String[] message, Socket sock, PrintWriter out) {
            String clientID = message[2];
            String acknowledgment = "SESS " + version + sessionID;
            sessionsAndClients.put(sessionID, clientID);
            sessionID++;

            try {
                out.println(acknowledgment);
                System.out.println("Sent " + acknowledgment);
                // out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public static void createGame(String[] message, Socket sock, PrintWriter out){
            String clientID = message[2];
            String[] gameElements = {clientID, null, "|*|*|*|*|*|*|*|*|*|"};
            games.put(gameID, gameElements);
            gameID++;
            String response = "JOND " + clientID + gameID;

            try {
                out.println(response);
                System.out.println("Sent " + response);
                // out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        
        /////////////////////

        public static void quitGame(String[] message, Socket sock, PrintWriter out) {
            String clientID = message[1]; // FIX!! CLIENT WONT SEND ID IN MESSAGE
            String gameID = message[2];
            String[] elements = games.get(gameID);
            String winner = elements[0];
            if (elements[0].equals(clientID)) {
                winner = elements[1];
            }
            String response = "TERM " +  gameID + winner + "KTHXBYE";

            try {
                out.println(response);
                System.out.println("Sent " + response);
                // out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public static void goodbye(String[] message, Socket sock, PrintWriter out) {
            String clientID = message[1]; // FIX!! CLIENT WONT SEND ID IN MESSAGE
            List<Integer> quit = new ArrayList<>();
            for (int i : games.keySet()) {
                String[] elements = games.get(i);
                if (Arrays.stream(elements).anyMatch(clientID::equals)) {
                    quit.add(i);
                }
            }

            for (int j : quit) {
                String[] mess = {"QUIT", clientID, Integer.toString(j)};
                quitGame(mess, sock, out);
            }
        }

        public static void join(String[] message, Socket sock, PrintWriter out) {
            String clientID = message[1]; // FIX!! CLIENT WONT SEND ID IN MESSAGE
            String gameID = message[2];
            String[] state = games.get(gameID);
            state[2] = clientID; 
            
            String response = "JOND " + clientID + gameID;

            try {
                out.println(response);
                System.out.println("Sent " + response);

                // SEND YRMV HERE TO CLIENT 1

                // out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    // GDBY, JOIN, LIST, MOVE, QUIT, STAT

    // Zach:
        // LIST
        // MOVE
        // STAT

    // Vic:
        // GDBY
        // QUIT
        // JOIN

    public static class MyRunnableUDP implements Runnable {
        private DatagramSocket soc;
        public MyRunnableUDP (DatagramSocket s) {
            this.soc = s;
        }

        public void run() {
            try {
                System.out.println("Listening for UDP connection on port " + 3116);
                byte[] b = new byte[256];
                DatagramPacket pack = new DatagramPacket(b, b.length);
                soc.receive(pack);
                InetAddress address = pack.getAddress();
                int port = pack.getPort();
                String message = new String(pack.getData(), 0, pack.getLength());
                byte[] response = message.getBytes(); // message will be the response
                DatagramPacket newP = new DatagramPacket(response, response.length, address, port);
                soc.send(newP);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    }
}

   // Send Acknowledgment with "SESS <version> <sess ID>""
    // sessCount = 1;
    // sessCount++;
    // private static void handleClient(String[] message) {
    //     String clientID = message[2];
    
    //     try {
    //         String acknowledgment = "ACKN " + clientID;
    
    //         //Socket clientSocket = new Socket("localhost", PORT);
    
    //         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    
    //         out.println(acknowledgment);
    
    //         out.close();
    //         clientSocket.close();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

// Client will connect with hello message "HELO <version number> <client ID>"
// ex: "HELO 1 zach"
    // Can probably take in command line arguments for the identifier/username

// Server responds with receipt acknowledging the client "SESS <version number> <session ID>"
    // We could implement some sort of counter so each time a session is created it will use that "count" value and add 1 for the next 1

// Client has two options for playing
    // Create a new game:
        // "CREA <client ID>" to create a new game
        // Server will respond to client with "JOND <client ID> <game ID>"
    // Find a game:
        // "LIST CURR" Server will send a list of all games currently open to join
        // "LIST ALL" Server will send a list of all games running on the server
            // Server responds with "GAMS <List of games>
            // We could store games in like a hashmap, where the key is if it is currently open or not and the value is the game ID
            // Client will then pick a game and send "JOIN <game ID>" and server will respond with "JOND <client ID> <game ID>"

// Server needs to decide who goes first in game (can use math.rand)
// 
