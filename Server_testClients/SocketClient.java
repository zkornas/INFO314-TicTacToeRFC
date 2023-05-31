import java.net.*;
import java.io.*;

public class SocketClient {
    public static void main(String...args) {
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        try (Socket s = new Socket(hostname, port)) {
            s.setSoTimeout(15000);
            OutputStream out = s.getOutputStream();
            if (args.length > 2) {
                String cmd = args[2];
                out.write(cmd.getBytes());
            }

            InputStream in = s.getInputStream();
            StringBuilder time = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in, "ASCII");
            for (int c = reader.read(); c != -1; c = reader.read()) {
                time.append((char) c);
            }
            System.out.println(time);

        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
