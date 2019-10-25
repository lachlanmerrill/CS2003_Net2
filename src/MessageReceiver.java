import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReceiver implements Runnable {
    private static int port_ = 51190;
    private static int bufferSize_ = 256;
    private static int sleep_ = 500;
    private LinkedBlockingQueue<HandlerMessage> send;
    private Thread t;

    MessageReceiver(LinkedBlockingQueue<HandlerMessage> send) {
        this.send = send;
        this.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port_);


            while (true) {
                try {
                    InputStream rx;
                    Socket connection;
                    // Blocking - wait until we get a connection request.
                    System.out.println("*-- Listening for incoming messages.");
                    connection = server.accept();
                    rx = connection.getInputStream();

                    byte[] buffer = new byte[bufferSize_];
                    int b = 0;

                    while (b < 1) {
                        buffer = new byte[bufferSize_];
                        b = rx.read(buffer);
                    }

                    String message = new String(buffer);

                    System.out.println("*-- Received message: " + message);

                    Pattern extractor = Pattern.compile("\\[(.*?)\\]");
                    Matcher m = extractor.matcher(message);
                    List<String> data = new ArrayList<>();

                    while (m.find()) {
                        data.add(m.group(1));
                    }

                    if (data.size() == 4) {
                        HandlerMessage handlerMessage = new HandlerMessage("RC", data.toArray(new String[0]));

                        send.put(handlerMessage);
                        Thread.sleep(sleep_);
                    }

                    rx.close();
                    connection.close();

                } catch (InterruptedException ex) {
                    System.out.println("*-- TCP Listener thread exiting.");
                    break;
                }
            }

            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        t = new Thread(this, "listener");
        t.start();
    }

    public void terminate() {
        t.interrupt();
    }
}
