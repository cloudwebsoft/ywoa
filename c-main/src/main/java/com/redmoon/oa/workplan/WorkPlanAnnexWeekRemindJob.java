package com.redmoon.oa.workplan;

import org.quartz.Job;
import org.quartz.JobExecutionException;
import org.quartz.JobExecutionContext;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.person.UserDb;
import java.util.Calendar;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.person.UserMgr;
import java.util.Iterator;
import cn.js.fan.db.SQLFilter;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkPlanAnnexWeekRemindJob implements Job {
    public WorkPlanAnnexWeekRemindJob() {
    }

    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        Calendar cal = Calendar.getInstance();
	int w = cal.get(Calendar.WEEK_OF_YEAR);
        int year = cal.get(Calendar.YEAR);

        // 如果当前为本年度的第一周，则取去年的最后一周
        if (w==1) {
            year = year-1;
            cal.set(year, 12, 1);
            w = cal.get(Calendar.WEEK_OF_YEAR);
        }
        else
            w = w - 1;

        java.util.Date d = new java.util.Date();
        String dtstr = DateUtil.format(d, "yyyy-MM-dd");

        String sql = "select id from work_plan where progress<100 and beginDate<=" + SQLFilter.getDateStr(dtstr, "yyyy-MM-dd") + " and endDate>=" + SQLFilter.getDateStr(dtstr, "yyyy-MM-dd");

        // LogUtil.getLog(getClass()).info("sql=" + sql);
        // LogUtil.getLog(getClass()).info("sql2=" + sql);

        WorkPlanDb wpd = new WorkPlanDb();
        WorkPlanAnnexDb wpad = new WorkPlanAnnexDb();
        Iterator ir = wpd.list(sql).iterator();

        boolean isToMobile = SMSFactory.isUseSMS();
        IMessage imsg = null;
        // String title = "系统提醒您，请及时填写工作计划：$title月报";
        // String content = "您上月的月报尚未填写，请及时填写！该计划的内容如下：$content";

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String title = cfg.get("workplan_annex_week_remind_title");
        String content = cfg.get("workplan_annex_week_remind_content");
        UserMgr um = new UserMgr();
        if (isToMobile) {
            ProxyFactory proxyFactory = new ProxyFactory(
                    "com.redmoon.oa.message.MessageDb");
            imsg = (IMessage) proxyFactory.getProxy();
        }
        IMsgUtil imu = SMSFactory.getMsgUtil();

        while (ir.hasNext()) {
            wpd = (WorkPlanDb)ir.next();

            // 检查上周周报是否已填写
            WorkPlanAnnexDb wpa = wpad.getWorkPlanAnnexDb(wpd.getId(), year, WorkPlanAnnexDb.TYPE_WEEK, w);
            if (wpa!=null)
                continue;

            String t = title.replaceFirst("\\$title", wpd.getTitle());
            String c = content.replaceFirst("\\$content", wpd.getContent());
            String[] principals = wpd.getPrincipals();
            if (isToMobile) {
                int len = principals.length;
                for (int i = 0; i < len; i++) {
                    try {
                        UserDb ud = um.getUserDb(principals[i]);
                        imsg.sendSysMsg(principals[i], t, c);
                        imu.send(ud, t, MessageDb.SENDER_SYSTEM);
                    } catch (ErrMsgException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else {
                // 发送信息
                MessageDb md = new MessageDb();
                int len = principals.length;
                String action = "action=" + MessageDb.ACTION_WORKPLAN + "|id=" + wpd.getId();
                for (int i = 0; i < len; i++) {
                    try {
                        md.sendSysMsg(principals[i], t, c, action);
                    } catch (ErrMsgException ex1) {
                        ex1.printStackTrace();
                    }
                }
               }
        }

    }
}
