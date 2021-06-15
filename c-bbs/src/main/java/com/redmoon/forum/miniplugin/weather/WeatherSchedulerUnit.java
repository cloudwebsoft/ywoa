package com.redmoon.forum.miniplugin.weather;

import cn.js.fan.kernel.BaseSchedulerUnit;
import org.apache.log4j.Logger;
import cn.js.fan.util.StrUtil;

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
public class WeatherSchedulerUnit extends BaseSchedulerUnit {
    static Logger logger = Logger.getLogger(WeatherSchedulerUnit.class.getName());

    public WeatherSchedulerUnit() {
        lastTime = System.currentTimeMillis();
        interval = 60000*60*1; // 每隔1个小时刷新一次
        name = "天气调度器";
    }

    /**
     * OnTimer
     *
     * @param currentTime long
     * @todo Implement this cn.js.fan.kernal.ISchedulerUnit method
     */
    @Override
    public void OnTimer(long curTime) {
        // logger.info("curTime=" + curTime);
        try {
            if (curTime - lastTime >= interval) {
                action();
                lastTime = curTime;
            }
        }
        catch (Throwable e) {
            // 防止运行有异常，导致线程退出
            logger.error("OnTimer:" + StrUtil.trace(e));
        }
    }

    @Override
    public synchronized void action() {
        try {
            WeatherUtil wu = new WeatherUtil();
            String content = wu.getWeather();
            String fullcontent = wu.getWeatherFull();
            wu.createIncFile(content, fullcontent);
        } catch (Throwable e) {
            logger.error("action:" + e.getMessage());
        }
    }

}
