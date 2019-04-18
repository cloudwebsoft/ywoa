package cn.js.fan.kernel;

import cn.js.fan.util.ErrMsgException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BaseSchedulerUnit implements ISchedulerUnit {
    int count = -1; // count=-1表示无限次
    public long lastTime = 0;
    public long interval = 5000; // 每隔5秒运行一次

    int TYPE_INTERVAL = 0;
    int type = TYPE_INTERVAL;

    public String name = "Base Scheduler Unit";

    public BaseSchedulerUnit() {
    }

    public String getName() {
        return name;
    }

    /**
     * OnTimer
     *
     * @param currentTime long
     * @todo Implement this cn.js.fan.kernal.ISchedulerUnit method
     */
    public void OnTimer(long currentTime) {
        if (lastTime+interval>currentTime) {

        }
        lastTime = currentTime;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    public synchronized void action() {

    }

    public void registSelf() {
        // System.out.println(this);
        Scheduler.getInstance().UnitsOperate(this, true);
    }

}
