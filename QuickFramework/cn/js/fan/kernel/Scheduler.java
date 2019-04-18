package cn.js.fan.kernel;

import java.util.Vector;
import java.util.Iterator;

public class Scheduler extends Thread {
    private static long updateInterval = 5000;
    public static long currentTime;

    private Vector units = new Vector();
    public static Scheduler scheduler = null;

    public static final int DO_WORK = 1;
    public static final int DO_PAUSE = 2;
    public static final int DO_EXIT = 0;

    public static int action = DO_WORK;

    //Statically start a timing thread with 1 minute of accuracy.
    //static {
    //    new Scheduler(updateInterval);
    //}

    public Scheduler(long updateInterval) {
        this.updateInterval = updateInterval;
        //Make the timer be a daemon thread so that it won't keep the VM from
        //shutting down if there are no other threads.
        this.setDaemon(true);
        this.setName("cn.js.fan.kernel.Scheduler");
        //Start the timer thread.
        start();
    }

    public int getAction() {
        return action;
    }

    /**
     * 单态模式
     * @param updateInterval long
     */
    public static synchronized void initInstance(long updateInterval) {
        if (scheduler==null)
            scheduler = new Scheduler(updateInterval);
    }

    public void run() {
        // Run the timer indefinetly.
        while (action!=DO_EXIT) {
            if (action==DO_WORK) {
                try {
                    currentTime = System.currentTimeMillis();
                    // logger.info("Scheduler currentTime:" + this + " " + currentTime);
                    Iterator ir = units.iterator();
                    while (ir.hasNext()) {
                        ISchedulerUnit isu = (ISchedulerUnit) ir.next();
                        isu.OnTimer(currentTime);
                    }
                }
                catch (Throwable t) {
                    System.out.println("run:" + t.getMessage());
                    t.printStackTrace();
                }
            }
            try {
                sleep(updateInterval);
            }
            catch (InterruptedException ie) { }
        }
        System.out.println(getName() + " exit. ");
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public synchronized void UnitsOperate(ISchedulerUnit isu, boolean AddTrueDelFalse) {
        if (AddTrueDelFalse) {
            /*
            此处算法不正确，会无此境地addElement，因为contains判断不正确,因此在Config的initScheduler中先作清除
            if (!units.contains(isu)) {
                units.addElement(isu);
                logger.info("UnitsOperate: addElement " + isu);
            }
            else {
                logger.info("UnitsOperate: Units has contained " + isu);
            }
            */
           units.addElement(isu);
        }
        else {
            units.remove(isu);
        }
    }

    public static synchronized Scheduler getInstance() {
        if (scheduler==null)
            initInstance(updateInterval);
        return scheduler;
    }

    public Vector getUnits() {
        return units;
    }

    public void ClearUnits() {
        units.clear();
    }

    public void doExit() {
        getInstance().action = DO_EXIT;
        scheduler = null;
    }

    public void doPause() {
        getInstance().action = DO_PAUSE;
    }

    public void doResume() {
        getInstance().action = DO_WORK;
    }

}
