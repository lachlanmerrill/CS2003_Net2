/*
 * CS2003 coursework Net2 demo
 * Saleem Bhatti, Oct 2018
 *
 * This "back-end" is purely for demonstrating to the class the GUI that
 * will be used as the starting point, the kind of behaviour the application
 * is to have overall. This class will need to be taken out completely from
 * the code. Taking this class out and replacing it with one that instead
 * uses network communication might be a good starting point.
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageCheckerCommon {

    private static HashMap<String, String[]> users_map = new HashMap<>();
    private static ArrayList<String> users_temp = new ArrayList<>();
    private static ArrayList<String> messages = new ArrayList<>();
    private static ArrayList<String> blacklist = new ArrayList<>();

    public static String timestamp() {
        final SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS");
        return s.format(new Date());
    }

    /**
     * Add a received message to the messages list.
    * @param s the received message.
     */
    public static void tx_message(String s) {
        messages.add(s);
    } // tx_message()

    /**
     * Gets the list of received messages, then clear the list.
     * @return the list of received messages
     */
    public static ArrayList<String> rx_messages() {
            return messages;
    }

    public static ArrayList<String> users_list() {
      return new ArrayList<>(users_map.keySet());
    }

    public static String[] get_user(String user) {
        return users_map.get(user);
    }

    public static void temp_addUser(String user) {
        users_temp.add(user);
    }
    public static void temp_removeUser(String user) {
        users_temp.remove(user);
    }

    public static void temp_clearUsers() {
        users_temp.clear();
    }

    public static void block(String user) {
        blacklist.add(user);
    }

    public static boolean isBlacklisted(String user) {
        return blacklist.contains(user);
    }

    public static void temp_compareUsers() {
        List<String> inactive = new ArrayList<>();
        for (String user : users_map.keySet()) {
            if (!users_temp.contains(user)) {
                inactive.add(user);
            }
        }
        //System.out.println(users_map.keySet().toString());
        users_map.keySet().removeAll(inactive);
        //System.out.println(users_map.keySet().toString());
        temp_clearUsers();
    }

    public static void userStatusChange(String user, String fqdn, int port, boolean online) {
        if (online) {
            if (!users_map.containsKey(user)) {
                users_map.put(user, new String[]{fqdn, String.valueOf(port)});
            }
            if (!users_temp.contains(user)) {
                temp_addUser(user);
            }
        } else {
            users_map.remove(user);
            if (users_temp.contains(user)) {
                temp_removeUser(user);
            }
        }
    }

    public static void purgeUser(String user) {
        users_map.remove(user);
    }

} // class MessageCheckerCommon
