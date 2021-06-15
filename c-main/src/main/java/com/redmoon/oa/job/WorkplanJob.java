package com.redmoon.oa.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;

import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.workplan.WorkPlanDb;
import com.redmoon.oa.message.IMessage;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.message.MessageDb;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class WorkplanJob implements Job {
    public WorkplanJob() {
    }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     * @todo Implement this org.quartz.Job method
     */
    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String strId = data.getString("id");
        int id = Integer.parseInt(strId);

        WorkPlanDb wd = new WorkPlanDb();
        wd = wd.getWorkPlanDb(id);

        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();

        String[] aryusers = wd.getUsers();
        int len = aryusers.length;
        for (int i=0; i<len; i++) {
            // 发送消息通知
            boolean isToMobile = SMSFactory.isUseSMS();
            // 发送信息
            MessageDb md = new MessageDb();
            String t = "系统提醒：" + wd.getTitle();
            String c = wd.getContent();
            try {
                if (!isToMobile)
                    md.sendSysMsg(aryusers[i], t, c);
                else {
                    if (imsg != null)
                        imsg.sendSysMsg(aryusers[i], t, c);
                }
            }
            catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
            }
        }
    }
}
