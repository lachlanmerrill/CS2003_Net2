import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handles non-blocking communication between gui, beacon, messages and users threads.
 */
public class ThreadHandler implements Runnable {
    // Allows GUI and beacon thread to add items to a queue, then handles them in this thread using a blocking queue.

    // Queue to receive from all other threads.
    private LinkedBlockingQueue<HandlerMessage> queue = new LinkedBlockingQueue<>();

    // Initialize threads and the beacon.
    private Thread t_;
    private Thread tUsers;
    private Thread tMessages;
    private Beacon beacon;
    private BeaconReceiver receiver;
    private MessageReceiver messageReceiver;

    // Initialize other classes
    private Users users;
    private MessageSender sender;
    private Messages messages;
    private Notifications notifications;
    private MessageCheckerGUI gui;
    NonBroadcastingChecker checker;

    // Initialize other useful data
    private String id;
    private String fdqn;

    // Initialize beacon communication queue.
    private ConcurrentLinkedQueue<String> beaconQ = new ConcurrentLinkedQueue<>();

    // Initialize users communication queue.
    private ConcurrentLinkedQueue<String> usersQ = new ConcurrentLinkedQueue<>();

    // Initialize messages queue.
    private ConcurrentLinkedQueue<String> messagesQ = new ConcurrentLinkedQueue<>();

    ThreadHandler(String id) {
        // Initialize various values, moved out from MessageCheckerGUI for sharing.
        this.id = id;
        TextArea n = new TextArea("", 10, 80, TextArea.SCROLLBARS_VERTICAL_ONLY);
        java.awt.List u = new List(8, false);
        {
            try {
                fdqn = InetAddress.getLocalHost().toString().split("/")[0];
            } catch (UnknownHostException e) { // This should not happen.
                e.printStackTrace();
                this.terminate();
                return;
            }
        }



        // Initialize the notifications class
        notifications = new Notifications(n);

        // Initialize and run the users thread.
        users = new Users(u, notifications, usersQ);
        tUsers = new Thread(users);
        tUsers.start();

        // Initialize and run the messages thread.
        messages = new Messages(id, notifications, queue, messagesQ, this);
        messages.setVisible(true);
        tMessages = new Thread(messages); // let it look after itself
        tMessages.start();

        // Initialize the MessageCheckerGUI
        gui = new MessageCheckerGUI(id, n, u, this, messages);
        gui.setVisible(true);

        // Fire up the beacon
        beacon = new Beacon(fdqn, id, queue, beaconQ);

        receiver = new BeaconReceiver(queue);

        messageReceiver = new MessageReceiver(queue);

        checker = new NonBroadcastingChecker();

        // Finally start handling inter-thread communication.
        this.start();
    }

    /**
     * Wait for an item to appear in the queue, then process it.
     */
    public void run() {
        while(true) {
            try {
                HandlerMessage queueItem = queue.take();
                String[] data = queueItem.data;

                // 0           1         2     3         4
                // [timestamp][username][type][text|fqdn]<port <-> 3==fqdn>



                switch (queueItem.type) {
                    case "RC": // Received message
                        messageReceived(data[1], data[3]);
                        break;

                    case "MS": // Send message
                        messageSent(data[1], data[3]);
                        break;

                    case "SC": // Discovered user
                        if (data.length > 4) {
                            if (!MessageCheckerCommon.isBlacklisted(data[1])) {
                                try {
                                    userStatusChange(data[1], data[3], Integer.parseInt(data[4]), data[2].equalsIgnoreCase("online"));
                                } catch (NumberFormatException exception) {
                                    MessageCheckerCommon.block(data[1]);
                                }
                            }
                        }
                        break;

                    case "TE": // Terminate request
                        System.out.println("^-- Received: " + queueItem);
                        end();
                        break;
                }

            } catch (InterruptedException e) {
                // Politely asked to stop, so we tidy up the mess we made and quietly exit.
                if (queue.peek() != null) {
                    //TODO Handle remaining messages.
                }
                System.out.println("^-- Handler thread exiting.");
                break;
            }
        }
    }

    public void messageReceived(String user, String message) {
        MessageCheckerCommon.tx_message(String.format("%s:%s", user, message));
    }

    public void messageSent(String user, String message) {
        try {
            String messageHeader = String.format("[%s][%s][text]", MessageCheckerCommon.timestamp(), id);
            message = String.format("%s[%s]", messageHeader, message);
            MessageSender.sendMessage(user, message, notifications);
        } catch (IOException ex) {
            notifications.notify("tx: Message sending failed.");
            System.out.println("tx: Message sending failed:");
            System.out.println(ex.getMessage());
        }
    }

    public void userStatusChange(String user, String fqdn, int port, boolean online) {
        MessageCheckerCommon.userStatusChange(user, fqdn, port, online);
    }

    public void end() {
        tUsers.interrupt();
        tMessages.interrupt();
        beacon.terminate();
        messageReceiver.terminate();
        this.terminate();
    }

    public void start() {
        t_ = new Thread(this, "threadhandler");
        t_.start();
    }

    public void terminate() {
        t_.interrupt();
    }
}
