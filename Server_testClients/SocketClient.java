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

                out.write(userInput.getBytes());

                StringBuilder response = new StringBuilder();
                InputStreamReader reader = new InputStreamReader(in, "ASCII");
                for (int c = reader.read(); c != -1; c = reader.read()) {
                    response.append((char) c);
                }

                System.out.println("Server response: " + response);
            } while (!userInput.equals("exit"));

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
