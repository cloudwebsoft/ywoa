package com.redmoon.oa.kernel;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.crm.CRMConfig;
import com.redmoon.oa.pointsys.PointSystemConfig;
import com.redmoon.oa.sale.ContractConfig;

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

    public static boolean isStarted = true;

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
                if (className.equals("com.redmoon.oa.job.WorkflowJob"))
                    jobDetail.getJobDataMap().put("flowCode", jud.getString("data_map"));
                else if (className.equals("com.redmoon.oa.job.WorkplanJob"))
                    jobDetail.getJobDataMap().put("id", jud.getString("data_map"));
                else if (className.equals("com.redmoon.oa.job.BeanShellScriptJob")) {
                	// 判断许可证是否支持脚本调度
                	//if (true || com.redmoon.oa.kernel.License.getInstance().canUseModule(com.redmoon.oa.kernel.License.MODULE_ACTION_EVENT_SCRIPT)) {
                		jobDetail.getJobDataMap().put("script", jud.getString("data_map"));
                	//}
                }
                else if (className.equals("com.redmoon.oa.job.SynThirdPartyDataJob")) {
                	// 判断许可证是否支持第三方数据同步
                	//if (true || com.redmoon.oa.kernel.License.getInstance().canUseModule(com.redmoon.oa.kernel.License.MODULE_ACTION_EVENT_SCRIPT)) {
                		jobDetail.getJobDataMap().put("data", jud.getString("data_map") + "|" + jud.getInt("id"));
                	//}                	
                }
                
                // LogUtil.getLog(getClass()).info("flowCode=" + jud.getString("data_map") + " cron=" + jud.getString("cron"));
                CronTrigger trigger = new CronTrigger(jud.getString("id"), Scheduler.DEFAULT_GROUP,
                        jud.getString("cron")); // 创建触发器

                sched.scheduleJob(jobDetail, trigger); // 添加到调度管理器中
                // System.out.println("Schedule " + jud.getString("job_name"));
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("init: className=" + className + " cron=" + jud.getString("cron") + " " + StrUtil.trace(e));
            }
            i++;
        }

        start(); // 开始调度

        // 流程节点到期提醒时间间隔
        Config cfg = new Config();
        int flowActionExpireRemindInterval = StrUtil.toInt(cfg.get("flowActionExpireRemindInterval"), 0); // 分钟
        if (flowActionExpireRemindInterval>0) {
            String cron = "0 0/" + flowActionExpireRemindInterval + " * * * ?";
            scheduleJob("flowActionExpireRemind",
                        "com.redmoon.oa.flow.WorkflowJob", cron, "");
        }

        // 工作计划周报提醒，每周一、周二上午九点
        boolean workplan_annex_week_remind = cfg.getBooleanProperty("workplan_annex_week_remind");
        if (workplan_annex_week_remind) {
            // Day-of-Week(每周)：可以用数字1-7表示（1 ＝ 星期日）或用字符串“SUN, MON, TUE, WED, THU, FRI and SAT”表示
            String cron = "0 0 9 ? * MON-TUE";

            scheduleJob("WorkPlanAnnexWeekRemindJob",
                        "com.redmoon.oa.workplan.WorkPlanAnnexWeekRemindJob", cron, "");

            System.out.println("Init WorkPlanAnnexWeekRemindJob scheduler corn=" + cron);
        }

        // 工作计划月报提醒，每月1、2、3号上午九点
        boolean workplan_annex_month_remind = cfg.getBooleanProperty("workplan_annex_month_remind");
        if (workplan_annex_month_remind) {
            String cron = "0 0 9 1,2,3 * ?";
            scheduleJob("WorkPlanAnnexMonthRemindJob",
                        "com.redmoon.oa.workplan.WorkPlanAnnexMonthRemindJob", cron, "");

            System.out.println("Init WorkPlanAnnexMonthRemindJob scheduler corn=" + cron);
        }
        
        // 积分统计
		PointSystemConfig pscfg = PointSystemConfig.getInstance();
		if (pscfg.getBooleanProperty("isUse")) {
			int d = pscfg.getIntProperty("calcuDay");
			//int d = 0;
			if (d > 0) {
	            String cron = "0 0 12 " + d + " * ?";
	            scheduleJob("PointSystemJob",
	                        "com.redmoon.oa.pointsys.PointSystemJob", cron, "");		
	            
	            System.out.println("Init PointSystemJob scheduler corn=" + cron);
			}
		}
		// 删除ZIP文件
		String fileDeleteCron = "59 59 23 * * ?";
		scheduleJob("DocFileJob",
                    "com.redmoon.oa.fileark.DocFileJob", fileDeleteCron, "");
		
		/*
		 * fgf 20180417 同步不完善，废作掉，调度中心同步优化后已可替代
		// 考勤记录
		String kqPlan = "0 55 9 * * ?";
        scheduleJob("SignSyncUtil",
                    "com.redmoon.oa.hr.SignSyncUtil", kqPlan, "");
        */
        
        // 0 0/30 9-17 * * ?   朝九晚五工作时间内每半小时
        // 每分钟调度一次，日程周期性安排提醒
        String cronPlan = "0 0/1 * * * ?";
        scheduleJob("PlanJob",
                    "com.redmoon.oa.person.PlanJob", cronPlan, "");
        
        // 每5分钟调度一次，短消息定时发送(原来是每1分钟调度一次，资源消耗较大 fgf20150306)
        String messagePlan = "0 0/5 * * * ?";
        scheduleJob("SendMessageJob",
                    "com.redmoon.oa.message.SendMessageJob", messagePlan, ""); 
        
        // 每1分钟调度一次，邮件定时发送
        String emailPlan = "0 0/1 * * * ?";
        scheduleJob("SendEmailJob",
        		"com.redmoon.oa.emailpop3.SendEmailJob", emailPlan, "");
        
        //add by lichao 20150525 证书合法性验证, 20点-24点之间的某一个时间点执行一次
        int max=60;
        int min=1;
        int s1 = new Random().nextInt(max - min) + min; //秒
        int s2 = new Random().nextInt(max - min) + min; //分
        int s3 = new Random().nextInt(4) + 20;	        //时
        
        String liscense = s1 +" " + s2 + " " +s3 + " * * ?";        
        System.out.println("LicenseManagerJob:" + liscense);
        scheduleJob("LiscenseManager","com.redmoon.oa.job.LicenseManagerJob", liscense, ""); 
        
        //add by lichao 20150804 将已经处理过的流程动作对应在oa_message中的消息记录置为已读，每隔30分钟执行一次。
        String setMessageRead = "0 0/30 0 * * ?"; 
        scheduleJob("SetOaMessageRead","com.redmoon.oa.job.SetOaMessageRead", setMessageRead, "");

        // 每分钟调度一次，流程延时节点启动
        String cronActionDelayed = "0 0/1 * * * ?";
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
            System.out.println("Init sms scheduler send corn=" + cron);
            cron = "0/" + receiveInterval + " * * * * ?";
            scheduleJob("Job_SMS_Receive",
                        "com.redmoon.oa.sms.ReceiveJob", cron, "");
            System.out.println("Init sms scheduler receive corn=" + cron);

            // 短信配额提醒
            cron = "0 0/60 * * * ?";
            scheduleJob("sms_boundary_remind",
                        "com.redmoon.oa.sms.SMSBoundaryRemindJob", cron, "");
            System.out.println("Init sms scheduler boundary remind corn=" + cron);
        }
        
        // 每年1月1号00：00初始化日历
        // 每一年调度一次
        String initCalendar = "0 0 0 1 1 ?";
        scheduleJob("InitOACalendar",
                    "com.redmoon.oa.job.InitOACalendar", initCalendar, "");

        // 合同到期提醒
        ContractConfig ctfg = new ContractConfig();
        int contractExpireRemindInterval = StrUtil.toInt(ctfg.get("contractExpireRemindInterval"), 0); // 分钟
        if (contractExpireRemindInterval>0) {
            // String cron = "0 0/" + contractExpireRemindInterval + " * * * ?";
            // String cron = "0 0 9 ? * MON"; // 每周一的九点
        	String cron = "0 0 9 0/" + contractExpireRemindInterval + " * ?"; // 每隔几天的9点提醒一次
            scheduleJob("contractExpireRemind",
                        "com.redmoon.oa.sale.ContractJob", cron, "");

            System.out.println("Init contract scheduler corn=" + cron);
        }

        CRMConfig crmcfg = CRMConfig.getInstance();
        /*
        // 回访提醒，已改为通过日程安排提醒
        int visitRemindInterval = StrUtil.toInt(crmcfg.getProperty("visitRemindInterval"), 0); // 分钟
        if (visitRemindInterval>0) {
            String cron = "0 0/" + visitRemindInterval + " * * * ?";
            scheduleJob("visitRemind",
                        "com.redmoon.oa.crm.VisitRemindJob", cron, "");

            System.out.println("Init sales visit scheduler corn=" + cron);
        }
        */

        // 如果不是政务版
        if (!License.getInstance().isGov()) {
	        // 商机行动回落提醒
	        int actionRemindInterval = StrUtil.toInt(crmcfg.getProperty("actionRemindInterval"), 5); // 分钟
	        if (actionRemindInterval>0) {
	            String cron = "0 0/" + actionRemindInterval + " * * * ?";
	            scheduleJob("actionRemind",
	                        "com.redmoon.oa.crm.ActionRemindJob", cron, "");
	
	            System.out.println("Init sales action scheduler corn=" + cron);
	        }
	        // 应收帐款提醒
	        int payPlanRemindBeforeDay = StrUtil.toInt(crmcfg.getProperty("payPlanRemindBeforeDay"), 0);
	        if (payPlanRemindBeforeDay>0) {
	            int payPlanRemindTime = crmcfg.getIntProperty("payPlanRemindTime");
	            String cron = "0 0 " + payPlanRemindTime + " * * ?"; // 每天早上10点
	            scheduleJob("payPlanRemind",
	                        "com.redmoon.oa.sales.PayPlanRemindJob", cron, "");
	            System.out.println("Init sales pay plan scheduler corn=" + cron);
	        }
	        // 库存预警提醒
	        int stockAlertRemindTime = StrUtil.toInt(crmcfg.getProperty("stockAlertRemindTime"), 0);
	        if (stockAlertRemindTime>=0) {
	            String cron = "0 0 " + stockAlertRemindTime + " * * ?"; // 每天早上10点
	            scheduleJob("stockAlertRemind",
	                        "com.redmoon.oa.sales.SalesStockAlertJob", cron, "");
	            System.out.println("Init sales stock alert scheduler corn=" + cron);
	        }
        }
        
        // 云OA空间统计
    	com.redmoon.oa.android.CloudConfig ccfg = com.redmoon.oa.android.CloudConfig.getInstance();
    	// 如果空间使用存在限制
    	if (ccfg.getIntProperty("diskSpace")!=-1) {
            String cron = "0 0 1 * * ?"; // 每天早上1点
            scheduleJob("stockAlertRemind",
                        "com.redmoon.oa.job.OAStatJob", cron, "");
            System.out.println("Init cloud oa stat scheduler corn=" + cron);
    	}
    	
        // 默认每10分钟调度一次提醒
        int formRemindInterval = StrUtil.toInt(cfg.get("formRemindInterval"), 10); // 分钟
    	
        String cronFormRemindDelayed = "0 0/" + formRemindInterval + " * * * ?";
        scheduleJob("FormRemindJob",
            "com.redmoon.oa.job.FormRemindJob", cronFormRemindDelayed, "");
        System.out.println("Init form remind scheduler corn=" + cronFormRemindDelayed);
        // 触发更新oa job 每天早上1点，四小时内随机发生
        String cron = "0 0 1 * * ?"; 
        scheduleJob("Upgrade Service",
                    "com.redmoon.oa.job.UpgradeJob", cron, "");
        // 触发更新oa job 每天晚上20点，四小时内随机发生
        String senCron = "0 0 20 * * ?"; 
        scheduleJob("Upgrade Service",
                    "com.redmoon.oa.job.UpgradeJob", senCron, "");
        System.out.println("Init upgrade service corn=" + cron);     
        //生成logo_show.JS文件，然后判断index.js中是否有调用该js的对应代码，若无则添加
        String createTime = "0 01 * * * ?"; 
        scheduleJob("CreateJsJob",
                    "com.redmoon.oa.job.CreateJsJob", createTime, "");
        System.out.println("Init CreateJsJob createTime=" + createTime);   
        //超级狗校验，每2分钟执行一次
/*        String checkTime = "0 0/2 * * * ?";
        scheduleJob("SuperCheck",
                    "com.redmoon.oa.job.SuperCheckJob", checkTime, "");
        System.out.println("Init SuperCheck checkTime=" + checkTime);  */
        /**
        //oem工作调度    0 0/5 * * * ?
        String oemCron = "0 0 3 * * ?";  //每天凌晨3点执行
        //String oemCron = "0 0/2 * * * ?";
        scheduleJob("OEMCheckJob",
                    "com.redmoon.oa.job.ClassJarFilesCheckJob", oemCron, ""); 
		*/
        String delLogCron = "0 0 3 * * ?";  //每天凌晨3点执行
        scheduleJob("DelLog", "com.redmoon.oa.job.DelLogJob", delLogCron, ""); 
        
        // 在user_role_user.jsp及user_op.jsp中已经作了处理，所以不需要再调度
        // String setUserDuty = "0 0/20 0 * * ?"; // 每隔20分钟一次
        // scheduleJob("SetUserDuty","com.redmoon.oa.job.SetUserDutyJob", setUserDuty, "");        
    }

    public void scheduleJob(String jobId, String jobClass, String cron,
                            String data_map) {
        if (isStarted()) {
            try {
                Class cls = Class.forName(jobClass);
                JobDetail jobDetail = new JobDetail(jobId,
                		Scheduler.DEFAULT_GROUP,
                        cls); // 创建工作
                if (jobClass.equals("com.redmoon.oa.job.WorkflowJob"))
                    jobDetail.getJobDataMap().put("flowCode", data_map);
                else if (jobClass.equals("com.redmoon.oa.job.WorkplanJob"))
                    jobDetail.getJobDataMap().put("id", data_map);
                CronTrigger trigger = new CronTrigger("" + jobId,
                		Scheduler.DEFAULT_GROUP,
                        cron); // 创建触发器

                sched.scheduleJob(jobDetail, trigger); // 添加到调度管理器中
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("scheduleJob:" + e.getMessage());
            }
        }
    }

    public void delJob(int jobId) {
        try {
            sched.deleteJob("" + jobId, Scheduler.DEFAULT_GROUP);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("deleteJob:" + e.getMessage());
        }
    }

    public void rescheduleJob(int jobId, String cron) {
        try {
            JobDetail jd = sched.getJobDetail("" + jobId, Scheduler.DEFAULT_GROUP);
            CronTrigger trigger = new CronTrigger("" + jobId, Scheduler.DEFAULT_GROUP,
                        cron); // 创建触发器
            trigger.setJobName("" + jobId);
            trigger.setJobGroup(Scheduler.DEFAULT_GROUP);
            JobDataMap jdm = jd.getJobDataMap();
            trigger.setJobDataMap(jdm);
            sched.rescheduleJob("" + jobId, Scheduler.DEFAULT_GROUP, trigger);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("rescheduleJob:" + StrUtil.trace(e));
        }
    }

    /**
     * 当shutdown后，Quartz是不允许直接start的，需重新初始化sheduler，启动scheduler
     */
    public void startWhenIsShutdown() {
        schedulerMgr = null;
        sched = null;
        getInstance();
    }

    public void start() {
        try {
            sched.start();
            isStarted = true;
            System.out.println("----------Start Scheduler------------------");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.getLog(getClass()).error("start:" + StrUtil.trace(e));
        }
    }

    public void shutdown() {
        try {
            sched.shutdown();
            isStarted = false;
            sched = null;
            System.out.println("----------Shutdown Scheduler------------------");
        }
        catch (Exception e) {
            e.printStackTrace();
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

    public boolean isShutdown() throws SchedulerException {
        return sched.isShutdown();
    }

}
