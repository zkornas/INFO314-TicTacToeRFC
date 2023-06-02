import java.net.*;
import java.io.*;

public class SocketClient {
    public static void main(String...args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket s = new Socket(hostname, port)) {
            s.setSoTimeout(15000);

            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();

            // The streams don't like this BufferedReader... not sure why, maybe the streams are getting mixed up.
            // After this BufferedReader line is run, the server gets sent an empty message for no reason,
            // you can try running out.write(("testing").getBytes); before and after line 20 to see it happen
            // I tried using Scanner console and it gives same result, I think System.in is the issue.. 

            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
            
            String userInput;

            PrintWriter writer = new PrintWriter(out, true);

            do {
                System.out.print("Enter a command (or 'exit' to quit): ");
                userInput = userInputReader.readLine();

                writer.println(userInput + "\n");

                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;

                // Changed while loop to (ready()) condition
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
