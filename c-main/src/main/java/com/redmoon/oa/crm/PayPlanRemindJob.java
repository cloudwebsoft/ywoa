package com.redmoon.oa.crm;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.redmoon.oa.flow.FormDb;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.IMessage;
import org.quartz.JobExecutionContext;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import cn.js.fan.util.ErrMsgException;
import org.quartz.Job;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import cn.js.fan.db.ResultRecord;
import com.redmoon.oa.visual.FormDAO;
import org.quartz.JobExecutionException;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.aop.base.Advisor;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import java.util.Vector;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.message.MobileAfterAdvice;

/**
 * <p>Title: 应收帐款提醒</p>
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
public class PayPlanRemindJob implements Job {
    public PayPlanRemindJob() {
    }


    public Vector listRemind() {
        CRMConfig crmcfg = CRMConfig.getInstance();
        // 提醒刷新间隔
        int payPlanRemindBeforeDay = crmcfg.getIntProperty("payPlanRemindBeforeDay");

        String sql =
                "select id,jhhkrq from form_table_sales_ord_pay_plan where jhhkrq>=? and jhhkrq<? and is_remind=1";

        // 取7天以内需要提醒的记录
        java.util.Date now = new java.util.Date();
        java.util.Date d1 = DateUtil.addDate(now, payPlanRemindBeforeDay);

        FormDb fd = new FormDb("sales_ord_pay_plan");
        FormDAO fdao = new FormDAO(fd);
        Vector v = new Vector();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {now, d1});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                v.addElement(fdao.getFormDAO(rr.getInt(1), fd));
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

        FormDb fdOrder = new FormDb();
        fdOrder = fdOrder.getFormDb("sales_order");
        FormDAO fdaoOrder = new FormDAO();

        FormDb fdCustomer = new FormDb();
        fdCustomer = fdCustomer.getFormDb("sales_customer");
        FormDAO fdaoCustomer = new FormDAO();

        Vector v = listRemind();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FormDAO fdao = (FormDAO) ir.next();

            fdaoOrder = fdaoOrder.getFormDAO(StrUtil.toInt(fdao.getCwsId()), fdOrder);

            fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(fdaoOrder.getCwsId()), fdCustomer);
            String customer = fdaoCustomer.getFieldValue("customer");

            // 发送信息
            MessageDb md = new MessageDb();
            String t = StrUtil.format(crmcfg.getProperty("payPlanRemindTitle"),
                                      new
                                      Object[] {customer});
            String c = StrUtil.format(crmcfg.getProperty("payPlanRemindContent"),
                                      new
                                      Object[] {customer, fdao.getFieldValue("jhhkrq")
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
