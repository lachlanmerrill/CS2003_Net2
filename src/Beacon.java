import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * UDP Multicast beacon indicating whether this client is online or not.
 */
public class Beacon  implements Runnable {
    private static int port_ = 10101;
    private static int myPort_ = 51190;
    private static int bufferSize_ = 2048;
    private static String addNum_ = "239.42.42.42";
    private static InetAddress address_;
    private static byte[] buffer_ = new byte[bufferSize_];
    private static int sleep_ = 5000;
    private static int soTimeout_ = 10;
    private String onlineMessage;
    private String offlineMessage;
    private LinkedBlockingQueue<HandlerMessage> send;
    private ConcurrentLinkedQueue receive;
    Thread t_;

    Beacon(String fdqn, String userid, LinkedBlockingQueue<HandlerMessage> send, ConcurrentLinkedQueue receive) {
        this.onlineMessage = String.format("[%s][online][%s][%d]", userid, fdqn, myPort_);
        this.offlineMessage = String.format("[%s][offline][%s][%d]", userid, fdqn, myPort_);
        this.send = send;
        this.receive = receive;
        this.start();
    }


    /**
     * Light the beacon!
     * @throws IOException If something goes horribly wrong.
     */
    public void ignite() throws IOException {
        System.out.println("/-- Preparing beacon socket and packet...");
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(soTimeout_);
        address_ = InetAddress.getByName(addNum_);


        System.out.println("/-- Beacon ready.");

        while (true) {
//            System.out.println("/-- Broadcasting...");
            String broadcastMessage = String.format("[%s]%s", MessageCheckerCommon.timestamp(), this.onlineMessage);
            buffer_ = broadcastMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer_, buffer_.length, address_, port_);
            socket.send(packet);

            try {
                Object commPacket = send.remove();
                if (commPacket.toString().equals("end")) {
                    System.out.println("/-- Terminating.");
                    break;
                }
            } catch (NoSuchElementException e) {
                // Non-blocking, so continue as normal.
            }

            try {
                Thread.sleep(sleep_);
            } catch (InterruptedException e) {
                offline(socket);
                break;
            }
        }

        // Clean up and notify that we're stopping.
        socket.close();
        System.out.println("/-- Socket closed.");
    }

    public void offline(DatagramSocket socket) throws IOException {
        System.out.println("/-- Broadcasting offline.");
        String broadcastMessage = String.format("[%s]%s", MessageCheckerCommon.timestamp(), this.offlineMessage);
        buffer_ = broadcastMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer_, buffer_.length, address_, port_);
        socket.send(packet);
    }

    public void run() {
        try {
            ignite();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("/-- Beacon offline.");
    }

    public void start() {
        System.out.println("/-- Initializing beacon.");
        t_ = new Thread(this, "beacon");
        t_.start();
    }

    public void terminate() {
        t_.interrupt();
    }
}
