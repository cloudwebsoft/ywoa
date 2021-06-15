package com.redmoon.oa.crm;

import org.quartz.Job;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import java.util.Vector;
import java.util.Iterator;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.cloudwebsoft.framework.aop.base.Advisor;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import java.sql.SQLException;
import com.redmoon.oa.message.IMessage;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.message.MessageDb;

/**
 * <p>Title: 下一次行动提醒，已改为通过日程安排提醒</p>
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
public class VisitRemindJob implements Job {
    public VisitRemindJob() {
    }

    public Vector listRemind() {
        CRMConfig crmcfg = CRMConfig.getInstance();
        // 提醒刷新间隔
        int visitRemindInterval = crmcfg.getIntProperty("visitRemindInterval");

        String sql =
                "select id,next_visit_time,remind_before from form_table_day_lxr where next_visit_time>=? and next_visit_time<? and remind_before<>0";

        // 取7天以内需要提醒的记录
        java.util.Date now = new java.util.Date();
        java.util.Date d1 = DateUtil.addDate(now, 7);

        int n = 1;

        // 提醒提前量的间隔步长最小为15分钟，提醒提前量为0、15、30、45、60...、480，如果visitRemindInterval调度时间间隔超出15分钟，会导致遗漏记录
        // 所以应使visitRemindInterval<15
        // 调度时，当前时间 > next_visit_time - remind_before时，应发出提醒
        // 为避免被提醒多次，如：提前量为480，即8小时的，visitRemindInterval为15分钟
        // 设最多提醒不超过n次，则 当前时间 > (next_visit_time - remind_before) + visitRemindInterval * n以后，不再发出提醒

        FormDb fd = new FormDb("day_lxr");
        FormDAO fdao = new FormDAO(fd);
        Vector v = new Vector();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {now, d1});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                java.util.Date nextVisitTime = rr.getDate(2);
                int remindBefore = rr.getInt(3);
                java.util.Date d = DateUtil.addMinuteDate(nextVisitTime, -remindBefore);
                if (DateUtil.compare(now, d) == 1 &&
                    DateUtil.compare(now, DateUtil.addMinuteDate(d, visitRemindInterval * n)) == 2) {
                    v.addElement(fdao.getFormDAO(rr.getInt(1), fd));
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listRemind:" +
                                             StrUtil.trace(e));
        }
        return v;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        CRMConfig crmcfg = CRMConfig.getInstance();

        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();

        // 是否发送短信
        boolean isToMobile = SMSFactory.isUseSMS();

        FormDb fdLinkman = new FormDb();
        fdLinkman = fdLinkman.getFormDb("sales_linkman");
        FormDAO fdaoLinkman = new FormDAO();

        FormDb fdCustomer = new FormDb();
        fdCustomer = fdCustomer.getFormDb("sales_customer");
        FormDAO fdaoCustomer = new FormDAO();

        Vector v = listRemind();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FormDAO fdao = (FormDAO) ir.next();

            fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toInt(fdao.getFieldValue("lxr")), fdLinkman);
            String linkmanName = fdaoLinkman.getFieldValue("linkmanName");

            fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(fdaoLinkman.getFieldValue("customer")), fdCustomer);
            String customer = fdaoCustomer.getFieldValue("customer");

            // 发送信息
            MessageDb md = new MessageDb();
            String t = StrUtil.format(crmcfg.getProperty("visitRemindTitle"),
                                      new
                                      Object[] {fdao.getFieldValue("next_visit_time"), customer, linkmanName});
            String c = StrUtil.format(crmcfg.getProperty("visitRemindContent"),
                                      new
                                      Object[] {fdao.getFieldValue("next_visit_time"), customer, linkmanName
                                      });

            try {
                if (!isToMobile)
                    md.sendSysMsg(fdao.getCreator(), t, c);
                else {
                    if (imsg != null)
                        imsg.sendSysMsg(fdao.getCreator(), t, c);
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
            }

        }
    }
}
