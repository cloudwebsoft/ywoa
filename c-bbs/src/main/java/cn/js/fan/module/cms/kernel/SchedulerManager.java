package cn.js.fan.module.cms.kernel;

import org.quartz.SchedulerFactory;
import org.quartz.Scheduler;
import org.quartz.JobDetail;
import org.quartz.CronTrigger;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import java.util.Vector;
import java.util.Iterator;
import org.quartz.SchedulerException;
import org.quartz.JobDataMap;
import cn.js.fan.module.cms.Config;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SchedulerManager {
    static SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
    static Scheduler sched = null;
    static SchedulerManager schedulerMgr = null;

    public static SchedulerManager getInstance() {
        if (schedulerMgr==null) {
            schedulerMgr = new SchedulerManager();
            schedulerMgr.init();
        }
        return schedulerMgr;
    }

    public void init() {
        try {
            sched = schedFact.getScheduler(); // 获取调度管理器
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("init1:" + e.getMessage());
        }
        int i = 0;
        JobUnitDb jud = new JobUnitDb();
        Vector v = jud.list();
        Iterator ir = v.iterator();
        System.out.println("----------Init Scheduler------------------");
        while (ir.hasNext()) {
            jud = (JobUnitDb)ir.next();
            // String jobName = jud.getString("job_name");
            String className = jud.getString("job_class");
            try {
                Class cls = Class.forName(jud.getString("job_class"));
                JobDetail jobDetail = new JobDetail(jud.getString("id"),
                        Scheduler.DEFAULT_GROUP,
                        cls); // 创建工作
                if (className.equals("com.redmoon.forum.job.FullTextSearchJob")) {
                    jobDetail.getJobDataMap().put("fullTextSearchTime", jud.getString("data_map"));
                } else if (className.equals("cn.js.fan.module.cms.job.RobotJob")) {
                    jobDetail.getJobDataMap().put("id", jud.getString("data_map"));
                } else if (className.equals("com.redmoon.forum.job.RobotJob")) {
                    jobDetail.getJobDataMap().put("id", jud.getString("data_map"));
                }
                // LogUtil.getLog(getClass()).info("flowCode=" + jud.getString("data_map") + " cron=" + jud.getString("cron"));
                CronTrigger trigger = new CronTrigger(jud.getString("id"), Scheduler.DEFAULT_GROUP,
                        jud.getString("cron")); // 创建触发器

                sched.scheduleJob(jobDetail, trigger); // 添加到调度管理器中
                System.out.println("Schedule " + jud.getString("job_name") + " cron=" + jud.getString("cron"));
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("init:" + StrUtil.trace(e));
            }
            i++;
        }

        start(); // 开始调度

        Config cfg = new Config();
        int homeCreateHtmlInterval = cfg.getIntProperty("cms.homeCreateHtmlInterval"); // 分钟
        if (homeCreateHtmlInterval>0) {
            String cron = "0 0/" + homeCreateHtmlInterval + " * * * ?";
            scheduleJob("homeHtml",
                        "cn.js.fan.module.cms.job.HomeCreateHtmJob", cron, "");
        }
    }

    public void scheduleJob(String jobId, String jobClass, String cron,
                            String data_map) {
        if (isStarted()) {
            System.out.println("Schedule " + jobClass + " cron=" + cron);
            try {
                Class cls = Class.forName(jobClass);
                // System.out.println(getClass() + " cls=" + cls);
                JobDetail jobDetail = new JobDetail(jobId,
                        Scheduler.DEFAULT_GROUP,
                        cls); // 创建工作

                CronTrigger trigger = new CronTrigger(jobId,
                        Scheduler.DEFAULT_GROUP,
                        cron); // 创建触发器

                sched.scheduleJob(jobDetail, trigger); // 添加到调度管理器中
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("scheduleJob:" + e.getMessage());
            }
        }
    }

    public void delJob(String jobId) {
        try {
            sched.deleteJob(jobId, Scheduler.DEFAULT_GROUP);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("deleteJob:" + e.getMessage());
        }
    }

    public void rescheduleJob(String jobId, String cron) {
        try {
            // System.out.println("Schedule " + jobId + " cron=" + cron);
            JobDetail jd = sched.getJobDetail(jobId, Scheduler.DEFAULT_GROUP);
            JobDataMap jdm = jd.getJobDataMap();
            CronTrigger trigger = new CronTrigger("" + jobId, Scheduler.DEFAULT_GROUP,
                        cron); // 创建触发器
            trigger.setJobName("" + jobId);
            trigger.setJobGroup(Scheduler.DEFAULT_GROUP);
            trigger.setJobDataMap(jdm);
            sched.rescheduleJob(jobId, Scheduler.DEFAULT_GROUP, trigger);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("rescheduleJob:" + StrUtil.trace(e));
        }
    }

    public void start() {
        try {
            sched.start();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("start:" + StrUtil.trace(e));
        }
    }

    public void pause() {
        System.out.println("----------Pause Scheduler------------------");
        try {
            sched.pauseAll(); // pause();
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("pause:" + e.getMessage());
        }
    }

/*    public boolean isPaused() {
        boolean re = false;
        try {
            re = sched.isPaused();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("isPaused:" + e.getMessage());
        }
        return re;
    }*/

    public void shutdown() {
        System.out.println("----------Shutdown Scheduler------------------");
        try {
            sched.shutdown();
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("shutdown:" + e.getMessage());
        }
        schedulerMgr = null;
    }

    public boolean isStarted() {
        boolean re = false;
        try {
            re = sched.isStarted();
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("isStarted:" + e.getMessage());
        }
        return re;
    }

    public boolean isShutdown() throws SchedulerException {
        return sched.isShutdown();
    }

}
