public class NonBroadcastingChecker implements Runnable {
    private Thread t;
    private int sleepTime_ = 10000;

    NonBroadcastingChecker() {
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(sleepTime_);
//                System.out.println(">-- Purging inactive users.");
                MessageCheckerCommon.temp_compareUsers();
            } catch (InterruptedException e) {
                System.out.println(">-- Inactive Checker exiting.");
                break;
            }
        }
    }

    public void start() {
        t = new Thread(this, "nonbroadcastingchecker");
        t.start();
    }
}
