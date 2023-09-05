package cn.js.fan.cache.jcs;

/**
 * Simple timer that keeps the currentTime variable of Cache accurate to one
 * second of the real clock time.
 */
public class CacheTimer extends Thread {
    private static long updateInterval;
    public static long currentTime;
    RMCache rmCache = RMCache.getInstance();

    public static CacheTimer cTimer = null;

    /**
     * Creates a new CacheTimer object. The currentTime of Cache will be
     * updated at the specified update interval.
     *
     * @param updateInterval the interval in milleseconds that updates should
     *    be done.
     */
    public CacheTimer(long updateInterval) {
        CacheTimer.updateInterval = updateInterval;
        //Make the timer be a daemon thread so that it won't keep the VM from
        //shutting down if there are no other threads.
        this.setDaemon(true);
        this.setName("cn.js.fan.cache.jcs.CacheTimer");
        //Start the timer thread.
        start();
    }

    /**
     * 单态模式
     */
    public static synchronized void initInstance() {
        if (cTimer==null) {
            cTimer = new CacheTimer(60000);
        }
    }

    @Override
    public void run() {
        //Run the timer indefinetly.
        while (true) {
            currentTime = System.currentTimeMillis();
            rmCache.timer();

            try {
                sleep(updateInterval);
            }
            catch (InterruptedException ie) { }
        }
    }

    public void refresh() {

    }


}
