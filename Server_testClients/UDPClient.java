import java.net.*;
import java.io.*;

public class UDPClient {
    public static void main(String[] args) {
        try {
            DatagramSocket s = new DatagramSocket();
            InetAddress ad = InetAddress.getByName("localhost");
            String str1 = "Testing";
            //String str2 = "Testing 2";
            byte[] b1 = str1.getBytes();
            //byte[] b2 = str2.getBytes();
            DatagramPacket p1 = new DatagramPacket(b1, b1.length, ad, 3116);
            //DatagramPacket p2 = new DatagramPacket(b2, b2.length, ad, 3116);
            s.send(p1);
            System.out.println("Sent String 1");
            //s.send(p2);
            byte[] by = new byte[256];
            DatagramPacket pack = new DatagramPacket(by, by.length);
            s.receive(pack);
            String received = new String(pack.getData(), 0, pack.getLength());
            System.out.println(received);

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
