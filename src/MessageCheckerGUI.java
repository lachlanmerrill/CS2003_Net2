/*
 * CS2003 coursework Net2 demo
 * Saleem Bhatti, Oct 2018
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;

/*
 * For convenience, a list of relevant documentation for AWT widgest used.
 * Frame https://docs.oracle.com/javase/8/docs/api/java/awt/Frame.html
 * WindowListener https://docs.oracle.com/javase/8/docs/api/java/awt/event/WindowListener.html
 * ActionListener https://docs.oracle.com/javase/8/docs/api/java/awt/event/ActionListener.html
 * FlowLayout https://docs.oracle.com/javase/8/docs/api/java/awt/FlowLayout.html
 * Label https://docs.oracle.com/javase/8/docs/api/java/awt/Label.html
 * List https://docs.oracle.com/javase/8/docs/api/java/awt/List.html
 * Panel https://docs.oracle.com/javase/8/docs/api/java/awt/Panel.html
 * TextField https://docs.oracle.com/javase/8/docs/api/java/awt/TextField.html
 * TextArea https://docs.oracle.com/javase/8/docs/api/java/awt/TextArea.html
 */

public class MessageCheckerGUI
        extends     Frame
        implements  ActionListener,
        WindowListener
{
    // Label for the GUI
    private String name;

    // For outgoing and incoming messages.
    private Messages messages;

    // Where new users will be listed.
    private Users users;

    // Where general information and notifications are displayed.
    private Notifications notifications;

    ThreadHandler handler;

    public MessageCheckerGUI(String name, TextArea n, java.awt.List u, ThreadHandler handler, Messages messages)
    {
        super(name + " : notifications and users"); // call the Frame constructor

        this.handler = handler;
        this.messages = messages;
    /*
     * The AWT code below lays out the widgets as follows.

     +------------------ Frame ---------------------+
     |                                              |
     |  +-------- Panel (Notifications) ---------+  |
     |  | +-- Label --+ +------ TextArea ------+ |  |
     |  | |           | |                      | |  |
     |  | +-----------+ +----------------------+ |  |
     |  +----------------------------------------+  |
     |                                              |
     |  +------------ Panel (Users) -------------+  |
     |  | +-- Label --+ +-------- List --------+ |  |
     |  | |           | |                      | |  |
     |  | +-----------+ +----------------------+ |  |
     |  +----------------------------------------+  |
     |                                              |
     +----------------------------------------------+

     * The Frame and Panel objects are not visible -- they
     * form part of the GUI construction.
     *
     */

        /*
         * Simple GUI layout - FlowLayout.
         * GridBagLayout would be better, giving more precise control
         * over layout, but would require a lot more code.
         */
        setLayout(new FlowLayout());
        setBounds(0, 0, 800, 400); // Size of Frame

        Panel p; // tmp variable

        p = new Panel();
        p.add(new Label("Notifications"));

        p.add(n);
        add(p); // to this Frame

        p = new Panel();
        p.add(new Label("Users"));

        u.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                userSelected(actionEvent.getActionCommand());
            }
        });

        p.add(u);
        add(p);  // to this Frame

        addWindowListener(this);

    } // MessageCheckerGUI()


    @Override
    public void windowClosing(WindowEvent we) {
        System.out.println("Message Checker Closed. Terminating.");
        Window w = we.getWindow();
        w.dispose();
        handler.end();
        System.exit(0);
    }

    /*
     * These are required for WindowListener, but we are
     * not interested in them, so they are empty methods.
     */
    @Override
    public void windowClosed(WindowEvent we) { }
    @Override
    public void windowActivated(WindowEvent we) { }
    @Override
    public void windowDeactivated(WindowEvent we) { }
    @Override
    public void windowIconified(WindowEvent we) { }
    @Override
    public void windowDeiconified(WindowEvent we) { }
    @Override
    public void windowOpened(WindowEvent we) { }

    /*
     * ActionListener method - required.
     */
    @Override
    public void actionPerformed(ActionEvent ae) { } // empty

    public void userSelected(String user) {
        messages.setInputText(user + ":");
    }

} // class MessageCheckerGUI
