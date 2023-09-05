package com.redmoon.oa.kernel;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.sys.DebugUtil;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.*;

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

    public static void setStarted(boolean started) {
        SchedulerManager.started = started;
    }

    static boolean started = false;

    public static SchedulerManager getInstance() {
        if (schedulerMgr==null) {
            schedulerMgr = new SchedulerManager();
            schedulerMgr.init();
        }
        return schedulerMgr;
    }

    public void init() {
        // 如果全局配置中不允许调度，则不初始化调度，以便于集群时，只允许某些机器调度
        if (!Global.getInstance().isSchedule()) {
            return;
        }

        LogUtil.getLog(getClass()).info("----------Init Scheduler------------------");

        sched = (Scheduler)SpringUtil.getBean("CwsScheduler");

        // 开始调度
        start();

        sheduleJobByDb();

        // 流程节点到期提醒时间间隔
        Config cfg = new Config();
        int flowActionExpireRemindInterval = StrUtil.toInt(cfg.get("flowActionExpireRemindInterval"), 0); // 分钟
        if (flowActionExpireRemindInterval>0) {
            String cron = "0 0/" + flowActionExpireRemindInterval + " * * * ?";
            scheduleJob("flowActionExpireRemind",
                        "com.redmoon.oa.job.WorkflowExpireRemindJob", cron, "");
        }

        // 每5分钟调度一次，短信定时发送(原来是每1分钟调度一次，资源消耗较大 fgf20150306)
        /*String messagePlan = "0 0/5 * * * ?";
        scheduleJob("SendMessageJob",
                    "com.redmoon.oa.message.SendMessageJob", messagePlan, ""); */

        // add by lichao 20150804 将已经处理过的流程动作对应在oa_message中的消息记录置为已读，每隔30分钟执行一次。
        String setMessageRead = "0 0/30 0 * * ?"; 
        scheduleJob("SetOaMessageRead","com.redmoon.oa.job.SetOaMessageRead", setMessageRead, "");

        // 每10分钟调度一次，流程延时节点启动
        String cronActionDelayed = "0 0/10 * * * ?";
        scheduleJob("WorkflowDelayedDeliverJob",
            "com.redmoon.oa.flow.WorkflowDelayedDeliverJob", cronActionDelayed, "");

        // 流程节点到期自动转交
        int flowActionExpireAutoDeliverInterval = StrUtil.toInt(cfg.get("flowActionExpireAutoDeliverInterval"), 0); // 分钟
        if (flowActionExpireAutoDeliverInterval > 0) {
            String cron = "0 0/" + flowActionExpireAutoDeliverInterval + " * * * ?";
            scheduleJob("flowActionExpireAutoDeliver", "com.redmoon.oa.flow.WorkflowAutoDeliverJob", cron, "");
        }

        // 短信发送调度初始化
        if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
            com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
            int smsSendInterval = StrUtil.toInt(smscfg.getIsUsedProperty("sendInterval"), 2000)/1000; // 2秒
            int receiveInterval = StrUtil.toInt(smscfg.getIsUsedProperty("receiveInterval"), 2000)/1000; // 2秒
            String cron = "0/" + smsSendInterval + " * * * * ?";
            scheduleJob("Job_SMS_Send",
                        "com.redmoon.oa.sms.SendJob", cron, "");
            LogUtil.getLog(getClass()).info("Init sms scheduler send corn=" + cron);
            cron = "0/" + receiveInterval + " * * * * ?";
            scheduleJob("Job_SMS_Receive",
                        "com.redmoon.oa.sms.ReceiveJob", cron, "");
            LogUtil.getLog(getClass()).info("Init sms scheduler receive corn=" + cron);

            // 短信配额提醒
            cron = "0 0/60 * * * ?";
            scheduleJob("sms_boundary_remind",
                        "com.redmoon.oa.job.SMSBoundaryRemindJob", cron, "");
            LogUtil.getLog(getClass()).info("Init sms scheduler boundary remind corn=" + cron);
        }
        
        // 每年1月1号00：00初始化日历
        // 每一年调度一次
        String initCalendar = "0 0 0 1 1 ?";
        scheduleJob("InitOACalendar",
                    "com.redmoon.oa.job.InitOACalendar", initCalendar, "");
    	
        // 默认每10分钟调度一次提醒
        int formRemindInterval = StrUtil.toInt(cfg.get("formRemindInterval"), 10); // 分钟
        String cronFormRemindDelayed = "0 0/" + formRemindInterval + " * * * ?";
        scheduleJob("FormRemindJob",
            "com.redmoon.oa.job.FormRemindJob", cronFormRemindDelayed, "");
        LogUtil.getLog(getClass()).info("Init form remind scheduler corn=" + cronFormRemindDelayed);

        if (cfg.getBooleanProperty("isBirthdayRemindUsed")) {
            String birthdayCron = "0 0 9 * * ?";  //每天9点执行
            scheduleJob("BirthdayJob", "com.redmoon.oa.job.BirthdayJob", birthdayCron, "");
        }

        // 每10分钟
        String planRemindJob = "0 0/10 * * * ?";
        scheduleJob("PlanRemindJob", "com.redmoon.oa.job.PlanRemindJob", planRemindJob, "");

        String clearUserMsgCron = "0 0 2 * * ?";  //每天2点执行
        scheduleJob("ClearUserMessageJob", "com.redmoon.oa.job.ClearUserMessageJob", clearUserMsgCron, "");

        // 每3分钟刷新一次超时在位用户
        String refreshOnlineUserJobCron = "0 0/3 * * * ?";
        scheduleJob("RefreshOnlineUserJob", "com.redmoon.oa.job.RefreshOnlineUserJob", refreshOnlineUserJobCron, "");

        String clearTempCron = "0 0 4 * * ?";  // 每天凌晨4点执行
        scheduleJob("ClearTempJob", "com.redmoon.oa.job.ClearTempJob", clearTempCron, "");
    }

    public void sheduleJobByDb() {
        JobUnitDb jud = new JobUnitDb();
        Vector<JobUnitDb> v = jud.list();
        for (JobUnitDb jobUnitDb : v) {
            jud = jobUnitDb;
            String className = jud.getString("job_class");
            scheduleJob(String.valueOf(jud.getInt("id")), className, jud.getString("cron"), jud.getString("data_map"));
        }
    }

    public void scheduleJob(String jobId, String jobClass, String cron,
                            String dataMap) {
        if (isStarted()) {
            try {
                Class cls = Class.forName(jobClass);
                JobDetail jobDetail = JobBuilder.newJob(cls).withIdentity(jobId, Scheduler.DEFAULT_GROUP).build();
                // 调度发起流程
                if ("com.redmoon.oa.job.WorkflowJob".equals(jobClass)) {
                    jobDetail.getJobDataMap().put("flowCode", dataMap);
                } else if ("com.redmoon.oa.job.BeanShellScriptJob".equals(jobClass)) {
                    jobDetail.getJobDataMap().put("script", dataMap);
                } else if ("com.redmoon.oa.job.SynThirdPartyDataJob".equals(jobClass)) {
                    jobDetail.getJobDataMap().put("data", dataMap + "|" + jobId);
                }

                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
                CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobId, Scheduler.DEFAULT_GROUP)
                        .withSchedule(scheduleBuilder).build();

                sched.scheduleJob(jobDetail, trigger); // 添加到调度管理器中
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("scheduleJob:" + e.getMessage());
            }
        }
    }

    public void delJob(int jobId) {
        try {
            JobKey jobKey = new JobKey(String.valueOf(jobId), Scheduler.DEFAULT_GROUP);
            sched.deleteJob(jobKey);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("deleteJob:" + e.getMessage());
        }
    }

    /**
     * 只是重新调度，并不能更换调度的内容，所以修改JobUnitDb时，只能先删除，后添加
     * @param jobId
     * @param cron
     */
    public void rescheduleJob(int jobId, String cron) {
        try {
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(String.valueOf(jobId), Scheduler.DEFAULT_GROUP)
                    .withSchedule(scheduleBuilder).build();

            TriggerKey triggerKey = new TriggerKey(String.valueOf(jobId), Scheduler.DEFAULT_GROUP);
            sched.rescheduleJob(triggerKey, trigger);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("rescheduleJob:" + StrUtil.trace(e));
        }
    }

    public void start() {
        try {
            sched.start();
            started = true;
            LogUtil.getLog(getClass()).info("----------Start Scheduler-----------------");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("start:" + StrUtil.trace(e));
        }
    }

    public void standby() {
        // 挂起期间错过的触发，将在恢复时重新执行，通过属性misfireThreshold可以控制，包括：resumeJobGroup
        if (sched != null) {
            try {
                sched.standby();
            } catch (SchedulerException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        else {
            DebugUtil.e(getClass(), "standby", "sched is null");
        }
    }

    /**
     * 关闭调度
     */
    public void shutdown() {
        try {
            if (sched != null) {
                // 清除所有的任务，因为shutdown未能清除，也可以考虑把此段代码加入在init之前
                GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(Scheduler.DEFAULT_GROUP);
                Set<JobKey> jobkeySet = sched.getJobKeys(matcher);
                List<JobKey> jobkeyList = new ArrayList<>();
                jobkeyList.addAll(jobkeySet);
                sched.deleteJobs(jobkeyList);

                // shutdown(true)表示等待所有正在执行的job执行完毕后才停止调度器
                // shutdown方法调用后，就不可以再调用start方法了，因为shutdown方法会销毁Scheduler创建的所有资源（线程、数据库连接等）
                sched.shutdown(true);
                started = false;
                sched = null;
                LogUtil.getLog(getClass()).info("----------Shutdown Scheduler------------------");
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("shutdown:" + e.getMessage());
        }
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

    public boolean isStandby() {
        try {
            return sched.isInStandbyMode();
        } catch (SchedulerException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return false;
    }

    public boolean isShutdown() throws SchedulerException {
        return sched.isShutdown();
    }
}
