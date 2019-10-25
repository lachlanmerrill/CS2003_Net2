/*
* CS2003 coursework Net2 demo
* Saleem Bhatti, Oct 2018
*/

public class Net2 {
    private static ThreadHandler threadHandler;

    /**
     * Initializes a ThreadHandler to set up the application and online the beacon.
     * @param args Optional id for the user.
     */
    public static void main(String[] args) {
        String id;

        if (args.length == 1) { id = args[0]; }
        else { id = System.getProperty("user.name"); }

        System.out.println("Using id: " + id);

        threadHandler = new ThreadHandler(id);
    }

    public void finalize() {
        threadHandler.end();
    }
}
