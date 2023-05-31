import java.net.*;
import java.io.*;

public class SocketClient {
    public static void main(String... args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        String req = "";
        for (int i = 2; i < args.length; i++) {
            req += args[i] + " ";
        }

        try (Socket s = new Socket(hostname, port)) {
            s.setSoTimeout(15000);

            OutputStream out = s.getOutputStream();
            out.write(req.getBytes());

            InputStream in = s.getInputStream();
            StringBuilder response = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in, "ASCII");
            for (int c = reader.read(); c != -1; c = reader.read()) {
                response.append((char) c);
            }

            System.out.println("Server response: " + response);

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
