import java.io.*;
import java.net.*;
import java.util.*;

public class tttserver {

    public static int PORT = 3116;
    public static Boolean LISTEN = true;
    public static HashMap <Integer, String> sessionsAndClients = new HashMap<>();
    public static HashMap <String, Socket> clientSocketsTCP = new HashMap<>();
    public static int sessionID = 1;
    public static final int version = 1;
    public static HashMap <Integer, String[]> games = new HashMap<>();
    public static int gameID = 1;

// Open TCP Socket and wait for connection

    public static void main(String[] args) throws Exception {
        ServerSocket servSockT = new ServerSocket(PORT);
        Socket socket = null;

        while(LISTEN = true){
            Thread t1 = new Thread(new MyRunnableTCP(servSockT));
            t1.start();
        }

        System.in.read();

        servSockT.close();
    }

    public static class MyRunnableTCP implements Runnable {
        private ServerSocket soc;
        
        public MyRunnableTCP(ServerSocket s) {
            this.soc = s;
        }
    
        public void run() {
            try {

                Socket sock = soc.accept();
                System.out.println("Listening for TCP connection on port " + 3116);
                System.out.println("Connection Successful!");

                while (true) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                    StringBuilder inputData = new StringBuilder();

                    //String line;

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
                        if (Integer.parseInt(message[1]) != 1){
                            System.out.println(message[1]);
                            out.println("Error: Invalid version");
                        }
                        System.out.println("Invoking handleClient");
                        startSession(message, sock, out);
                    } else if (message[0].equals("CREA")){
                        System.out.println("Invoking createGame");
                        createGame(message, sock, out);
                    } else if (message[0].equals("LIST")){
                        System.out.println("Invokign listGames");
                        listGames(message, sock, out);
                    } else if (message[0].equals("MOVE")){
                        System.out.println("Invoking makeMove");
                        makeMove(message, sock, out);
                    } else if (message[0].equals("JOIN")){
                        System.out.println("Invoking join");
                        join(message, sock, out);
                    } else if (message[0].equals("STAT")){
                        System.out.println("Invoking gameStat");
                        gameStat(message, sock, out);
                    } else if (message[0].equals("QUIT")){
                        System.out.println("Invoking quitGame");
                        quitGame(message, sock, out);
                    } else if (message[0].equals("GDBY")){
                        System.out.println("Invoking goodbye");
                        goodbye(message, sock, out);
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
    }
        

    public static void gameStat(String[] message, Socket sock, PrintWriter out){
        String gameID = message[1];
        String response = "";

        if(games.get(Integer.parseInt(message[1]))[1] == null){
            response = "BORD " + gameID + " " + games.get(Integer.parseInt(message[1]))[0] + " ";
        } else {
            response = "BORD " + gameID + " " + games.get(Integer.parseInt(message[1]))[0] + " " + 
            games.get(Integer.parseInt(message[1]))[1] + " " + games.get(Integer.parseInt(message[1]))[2];
        }
        try {
            out.println(response);
            System.out.println("Sent " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

        // Handles response to client
    public static void startSession(String[] message, Socket sock, PrintWriter out) {
        String clientID = message[2];
        String acknowledgment = "SESS " + version + " " + clientID;
        //String value = 
        sessionsAndClients.put(sessionID, clientID);
        sessionID++;

        clientSocketsTCP.put(clientID, sock);

        try {
            out.println(acknowledgment);
            System.out.println("Sent " + acknowledgment);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createGame(String[] message, Socket sock, PrintWriter out){
        String clientID = message[1];
        String[] gameElements = {clientID, null, "|*|*|*|*|*|*|*|*|*|", clientID};
        games.put(gameID, gameElements);
        String response = "JOND " + clientID + " " + gameID;
        gameID++;

        try {
            out.println(response);
            System.out.println("Sent " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void listGames(String[] message, Socket sock, PrintWriter out){
        String gameList = "GAMS ";
        if (message.length == 1) {
            Set<Integer> keys = games.keySet();
            for(Integer key: keys){
                gameList = gameList + (key + " ");
            }
        } else {
            if (message[1].equals("CURR")){
                Set<Integer> keys = games.keySet();
                for(Integer key: keys){
                    gameList = gameList + (key + " ");
                }
            } else if (message[1].equals("ALL")){
                for (Map.Entry<Integer, String[]> entry : games.entrySet()){
                    String[] gameState = entry.getValue();
                    if (gameState[2].contains("*")){
                        gameList = gameList + (entry.getKey() + " ");
                    }
                }
            } else {
                gameList = "Error: Please choose CURR or ALL";
            }
        }


        try {
            out.println(gameList);
            System.out.println("Sent " + gameList);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void makeMove(String[] message, Socket sock, PrintWriter out){
        // Assuming structure: MOVE <game ID> <position> <client ID>
        String clientID = "";
        for(Map.Entry<String, Socket> entry : clientSocketsTCP.entrySet()){
            if (Objects.equals(entry.getValue(), sock)){
                clientID = entry.getKey();
            }
        }

        String[] moveElements = {"MOVE", message[1], message[2], clientID};
        String response = "";
        Boolean wasSuccess = false;
        String opp = "";
        String playerX = "";
        String playerO = "";
        char playerIcon = 'X';

        if(!games.containsKey(Integer.parseInt(message[1]))){
            System.out.println(moveElements[1]);
            response = "Error: Game not found.";
        // Checks if the client's ID matches the IDs of the players in the game map
        } else if(!moveElements[3].equals((games.get(Integer.parseInt(message[1])))[0]) && !moveElements[3].equals((games.get(Integer.parseInt(message[1])))[1])){
            response = "Error: You are not a player of this game.";
        } else {
            String board = (games.get(Integer.parseInt(message[1])))[2];

            // Checks if player is X or O based on position in map value
            if (moveElements[3].equals(games.get(Integer.parseInt(message[1]))[1])){
                playerIcon = 'O';
                playerO = moveElements[3];
                playerX = games.get(Integer.parseInt(message[1]))[0];
                opp = playerX;
            } else {
                playerX = moveElements[3];
                playerO = games.get(Integer.parseInt(message[1]))[1];
                opp = playerO;
            }

            int index = 0;

            // Checks if message was sent using coordinate pair for position
            if(moveElements[2].contains(",")){
                String[] coordinates = moveElements[2].split(",");

                int row = Integer.parseInt(coordinates[0]);
                int column = Integer.parseInt(coordinates[1]);

                index = 2 * (((3 * row) - 2) + (column - 1)) - 1;

                System.out.println(index);
                
            } else {
                index = Integer.parseInt(moveElements[2]) * 2 - 1;
            }
            // checks if space on board is available for move.
            if((games.get(Integer.parseInt(message[1]))[2]).charAt(index) != '*') {
                System.out.println(games.get(Integer.parseInt(message[1]))[2]);
                System.out.println(games.get(Integer.parseInt(message[1]))[2].charAt(index));
                response = "Error: Not a valid move.";

            } else if(!moveElements[3].equals((games.get(Integer.parseInt(message[1])))[3])){
                response = "Error: Not your turn.";
            } else {
                // Updates the game status in the game map's value
                StringBuilder boardBuilder = new StringBuilder(board);
                boardBuilder.setCharAt(index, playerIcon);
                (games.get(Integer.parseInt(message[1])))[2] = boardBuilder.toString();
                (games.get(Integer.parseInt(message[1])))[3] = opp;
                // Constructs response
                
                if(playerIcon == 'X'){
                    response = "BORD " + moveElements[1] + " " + playerX + " " + 
                    playerO + " " + playerO + " " + games.get(Integer.parseInt(message[1]))[2]; 
                } else {
                    response = "BORD " + moveElements[1] + " " + playerX + " " + 
                    playerO + " " + playerX + " " + games.get(Integer.parseInt(message[1]))[2]; 
                }
                
                wasSuccess = true;

                //checkWins(games.get(Integer.parseInt(message[1]))[2], playerIcon);
            }
        }
        String end = "";
        Socket oppSockTCP = clientSocketsTCP.get(opp);

        try {
            if(checkWins(games.get(Integer.parseInt(message[1]))[2], playerIcon)){
                response+= " " + moveElements[3];
                end = "TERM " +  gameID + " " + moveElements[3] + " KTHXBYE";
                System.out.println(end);
            } else if(!(games.get(Integer.parseInt(message[1]))[2]).contains("*")){
                end = "TERM " +  gameID + " " + " KTHXBYE";
                System.out.println(end);
            }
            PrintWriter oppOut = new PrintWriter(oppSockTCP.getOutputStream(), true);
            oppOut.println(response);


            out.println(response);
            System.out.println("Sent " + response);

            if (!end.isEmpty()) {
                oppOut.println(end);
                out.println(end);
                System.out.println("Sent: " + end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(checkWins(games.get(Integer.parseInt(message[1]))[2], playerIcon));
        if(wasSuccess && !checkWins(games.get(Integer.parseInt(message[1]))[2], playerIcon)){
            if(playerIcon == 'X'){
                response = "YRMV " + message[1] + " " + playerO;
            } else {
                response = "YRMV " + message[1] + " " + playerX;
            }
            try{
                PrintWriter oppOut = new PrintWriter(oppSockTCP.getOutputStream(), true);
                oppOut.println(response);
            } catch (Exception e){
                e.printStackTrace();
            }
            try {
                out.println(response);
                System.out.println("Sent " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean checkWins(String board, char playerIcon){
        board = board.replace("|", "");

        String[] firstRow = board.substring(0,3).split("");
        String[] secondRow = board.substring(3,6).split("");
        String[] thirdRow = board.substring(6).split("");

        String[] firstColumn = new String[]{
            Character.toString(board.charAt(0)), 
            Character.toString(board.charAt(3)), 
            Character.toString(board.charAt(6))
        };
        String[] secondColumn = new String[]{
            Character.toString(board.charAt(1)), 
            Character.toString(board.charAt(4)), 
            Character.toString(board.charAt(7))
        };
        String[] thirdColumn = new String[]{
            Character.toString(board.charAt(2)), 
            Character.toString(board.charAt(5)), 
            Character.toString(board.charAt(8))
        };

        String[] diagonal = new String[]{
            Character.toString(board.charAt(0)), 
            Character.toString(board.charAt(4)), 
            Character.toString(board.charAt(8))
        };
        String[] antiDiagonal = new String[]{
            Character.toString(board.charAt(2)), 
            Character.toString(board.charAt(4)), 
            Character.toString(board.charAt(6))
        };

        String[][] boardArray = {
            firstRow,
            secondRow,
            thirdRow,
            firstColumn,
            secondColumn,
            thirdColumn,
            diagonal,
            antiDiagonal
        };

        String[] xWin = new String[]{"X", "X", "X"};
        String[] oWin = new String[]{"O", "O", "O"};

        for(int i = 0; i < 8; i++){
            // System.out.println("Curr Board: " + Arrays.toString(boardArray[i]));
            if(Arrays.equals(boardArray[i], xWin)){
                return true;
            } else if(Arrays.equals(boardArray[i], oWin)){
                return true;
            }
        }
        return false;
    }

    public static void quitGame(String[] message, Socket sock, PrintWriter out) {
        String clientID = "";
        for(Map.Entry<String, Socket> entry : clientSocketsTCP.entrySet()){
            if (Objects.equals(entry.getValue(), sock)){
                clientID = entry.getKey();
            }
        }

        System.out.println(message[2]);
        int gameID = Integer.parseInt(message[2]);
        String[] elements = games.get(gameID);
        String winner = elements[0];
        String opp = elements[1];
        if (elements[0].equals(clientID)) {
            winner = elements[1];
            opp = elements[0];
        }
        String response = "TERM " +  gameID + " " + winner + " KTHXBYE";

        try{
            Socket oppSock = clientSocketsTCP.get(opp);
            PrintWriter oppOut = new PrintWriter(oppSock.getOutputStream(), true);
            oppOut.println(response);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    

    public static void goodbye(String[] message, Socket sock, PrintWriter out) {
        String clientID = "";
        for(Map.Entry<String, Socket> entry : clientSocketsTCP.entrySet()){
            if (Objects.equals(entry.getValue(), sock)){
                clientID = entry.getKey();
            }
        }

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
        String clientID = "";
        for(Map.Entry<String, Socket> entry : clientSocketsTCP.entrySet()){
            if (Objects.equals(entry.getValue(), sock)){
                clientID = entry.getKey();
            }
        }

        int gameID = Integer.parseInt(message[1]);
        String[] state = games.get(gameID);
        state[1] = clientID; 
        games.put(gameID, state);
        String opp = state[0];
        
        String response = "JOND " + clientID + " " + gameID;

        try {
            out.println(response);
            System.out.println("Sent " + response);
            response = "YRMV " + gameID + " " + opp;
            PrintWriter oppOut = new PrintWriter((clientSocketsTCP.get(opp)).getOutputStream(), true);
            oppOut.println(response);
            try {
                out.println(response);
                System.out.println("Sent " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
