import java.io.*;
import java.net.*;
import java.util.*;

public class tttserver {

// Open TCP Socket and wait for connection
// Open UDP socket and wait for connection

    public static void main(String[] args) throws Exception {
        ServerSocket servSockT = new ServerSocket(3116);
        DatagramSocket servSockU = new DatagramSocket(3116);
        Thread t1 = new Thread(new MyRunnableTCP(servSockT));
        Thread t2 = new Thread(new MyRunnableUDP(servSockU));

        t2.start();
        t1.start();

        t1.join();
        t2.join();

        servSockU.close();
        servSockT.close();
    }

    public static class MyRunnableTCP implements Runnable {
        private ServerSocket soc;
        
        public MyRunnableTCP(ServerSocket s) {
            this.soc = s;
        }
    
        public void run() {
            try {
                System.out.println("Listening for TCP connection on port " + 3116);
                Socket sock = soc.accept();
                System.out.println("Connection Successful!");
    
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String receivedData;
                StringBuilder inputData = new StringBuilder();
    
                while ((receivedData = in.readLine()) != null) {
                    inputData.append(receivedData);
                }
    
                // Save or process the received data as needed
                String savedData = inputData.toString();
                System.out.println("Received data: " + savedData);
    
                in.close();
                sock.close();
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    

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

