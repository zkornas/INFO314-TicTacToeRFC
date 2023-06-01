import java.net.*;
import java.io.*;

public class SocketClient {
    public static void main(String... args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket s = new Socket(hostname, port)) {
            s.setSoTimeout(15000);

            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();

            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;

            do {
                System.out.print("Enter a command (or 'exit' to quit): ");
                userInput = userInputReader.readLine();

                PrintWriter writer = new PrintWriter(out, true);
                writer.println(userInput + "\n");

                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                System.out.println("Server response: " + response);
            } while (!userInput.equals("exit"));

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
