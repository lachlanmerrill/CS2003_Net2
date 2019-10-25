import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MessageSender {
    private static int soTimout_ = 1000;

    public static void sendMessage(String user, String message, Notifications notifications) throws IOException {
        if (MessageCheckerCommon.users_list().contains(user)) {
            String[] user_details = MessageCheckerCommon.get_user(user);

            if (user_details.length != 2) {
                notifications.notify("tx: Bad user data. Purging.");
                MessageCheckerCommon.purgeUser(user);
                return;
            }

            Socket socket = startClient(user_details[0], Integer.parseInt(user_details[1]));

            if (socket == null) {
                notifications.notify("Message failed to send to - " + user + ": Connection refused");
                return;
            }

            OutputStream ts = socket.getOutputStream();

            byte[] buffer = message.getBytes();

            ts.write(buffer);

            System.out.println("--* Message sent.");

            socket.close();

        } else {
            notifications.notify("tx: User " + user + " not found.");
        }
    }

    static Socket startClient(String hostname, int portnumber) {
        Socket connection = null;

        try {
            InetAddress address;

            address = InetAddress.getByName(hostname);
            //String address = hostname;

            System.out.println(hostname + ":" + portnumber);

            connection = new Socket(address, portnumber); // server
            connection.setSoTimeout(soTimout_);

            System.out.println("--* Connecting to " + connection.toString());
        }

        catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
            return null;
        }

        return connection;
    }
}
