import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Receives multicast datagrams.
 */
public class BeaconReceiver implements Runnable {
    private int bufferSize_ = 256;
    private int port_ = 10101;
    private String addNum_ = "239.42.42.42";
    private InetAddress address_;
    private LinkedBlockingQueue<HandlerMessage> send;
    private Pattern extractor = Pattern.compile("\\[(.*?)\\]");

    BeaconReceiver(LinkedBlockingQueue<HandlerMessage> send) {
        this.send = send;
        this.start();
    }

    @Override
    public void run() {

        byte[] buffer = new byte[bufferSize_];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        MulticastSocket socket;

        try {

            address_ = InetAddress.getByName(addNum_);
            socket = new MulticastSocket(port_);
            socket.joinGroup(address_);

            while (true) {
                socket.receive(packet);
                String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
//                System.out.println("\\-- Received packet: " + received);


                Matcher m = extractor.matcher(received);
                List<String> data = new ArrayList<>();

                while (m.find()) {
                    data.add(m.group(1));
                }

                String[] aData = new String[data.size()];
                aData = data.toArray(aData);

//                System.out.println("\\-- Received packet: " + data.toString());
                if (data.size() >= 3) {

                    try {
                        switch (data.get(2)) {

                            case "text":
                                send.put(new HandlerMessage("RC", aData));
                                break;

                            case "online":
                                MessageCheckerCommon.temp_addUser(data.get(1));
                                send.put(new HandlerMessage("SC", aData));
                                break;
                            case "offline":
                                MessageCheckerCommon.temp_removeUser(data.get(1));
                                send.put(new HandlerMessage("SC", aData));
                                break;

                            case "unavailable":
                                //TODO: Implement A7 requirement
                                break;

                            default:
                                System.out.println("\\-- Bad packet received: " + new HandlerMessage("", aData).join());
                                break;

                        } // switch

                    } catch (InterruptedException ex) {
                        break;
                    } // try/catch

                } // if (data.length >= 3)
            } // while (true)

            socket.close();
        } catch (IOException ex) {

            System.out.println("/-- Receiver error.");
            System.out.println(ex.getMessage());

        } // try/catch

    } // run()

    public void start() {
        Thread t = new Thread(this, "beaconreceiver");
        t.start();
    }
}
