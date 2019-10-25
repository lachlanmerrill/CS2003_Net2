/*
 * CS2003 coursework Net2 demo
 * Saleem Bhatti, Oct 2018
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Users implements Runnable {

    private java.awt.List users; // java.util.List also exists!
    private Notifications notifications;

    private ConcurrentLinkedQueue receive;

    private final static int sleepTime = 1000; // ms, 5s between checks

    Users(java.awt.List u, Notifications n, ConcurrentLinkedQueue receive) {
        users = u;
        notifications = n;
        this.receive = receive;
    }

    /*
     * Runnable method - required.
     * Control plane messages - discovery of other users.
     */
    @Override
    public void run()
    {
        while (true) { // forever

            // Check the list of users
            ArrayList<String> checklist = MessageCheckerCommon.users_list();

            /*
             ** If any of the currently listed users are no longer on the checklist,
             ** they have now gone offline.
             */
            for (int u = 0; u < users.getItemCount(); ++u) {
                String s_u = users.getItem(u);
                boolean found = false;

                for (int c = 0; c < checklist.size(); ++c) {
                    String s_c = checklist.get(c);
                    if (s_u.equals(s_c)) {
                        found = true;
                        checklist.remove(c); // finished checking this one
                        break;
                    }
                }

                if (!found) { // user has gone offline
                    notifications.notify(s_u + " - offline.");
                    users.remove(u);
                }
            } // for (u < users.size())

            /*
             ** If the checklist contains users not on the list of current users,
             ** they must have just come online.
             */
            for (int c = 0; c < checklist.size(); ++c) {
                String s_c = checklist.get(c);
                notifications.notify(s_c + " - online.");
                users.add(s_c);
            }
            checklist.clear();

            try { Thread.sleep(sleepTime); } // do not need to check constantly
            catch (InterruptedException e) {
                System.out.println("$-- Users thread exiting.");
                break;
            }

        } // while(true)

    } // run()

} // class Users
