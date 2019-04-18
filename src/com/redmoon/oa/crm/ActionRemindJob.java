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
import com.redmoon.oa.sale.ActionSetupDb;
import com.redmoon.oa.sale.SalesConstant;

/**
 * <p>Title: 回落提醒</p>
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
public class ActionRemindJob implements Job {
	
	int actionRemindInterval = 5;
	
    public ActionRemindJob() {
        CRMConfig crmcfg = CRMConfig.getInstance();
        actionRemindInterval = StrUtil.toInt(crmcfg.getProperty("actionRemindInterval"), 5); // 分钟
    }

    /**
     * 行动提醒
     * @return Vector
     */
    public Vector listRemind() {
        // LogUtil.getLog(getClass()).info("listRemind");

        Vector v = new Vector();
        FormDb fd = new FormDb("sales_customer");
        FormDAO fdao = new FormDAO(fd);
        // 取出客户，除去已回落的客户
        String sql = "select id,customer_type,unit_code from form_table_sales_customer where kind<>'" + SalesConstant.KIND_NEGLECT + "'";
        ResultIterator ri = null;
        ActionSetupDb asd2 = new ActionSetupDb();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ri = jt.executeQuery(sql);
            java.util.Date now = new java.util.Date();
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                long customerId = rr.getLong(1);
                int customerType = StrUtil.toInt(rr.getString(2), 0);
                String unitCode = StrUtil.getNullStr(rr.getString(3));

                sql = "select id from form_table_sales_linkman where customer='" + customerId + "'";
                String linkManIds = "";
                ResultIterator ri3 = jt.executeQuery(sql);
                while (ri3.hasNext()) {
                    ResultRecord rr3 = (ResultRecord)ri3.next();
                    if (linkManIds.equals(""))
                        linkManIds = "'" + rr3.getLong(1) + "'";
                    else
                        linkManIds += ", '" + rr3.getLong(1) + "'";
                }

                if (linkManIds.equals(""))
                    continue;

                // 得到客户的所有联系人中最后一次行动的时间
                // sql = "select max(visit_date) from form_table_day_lxr where cws_id=" + rr.getInt(1) + " and is_visited='是' and lxr in (" + linkManIds + ")";
                sql = "select max(visit_date) from form_table_day_lxr where is_visited='是' and lxr in (" + linkManIds + ")";

                // System.out.println(getClass() + " sql=" + sql);

                ResultIterator ri2 = jt.executeQuery(sql);
                if (ri2.hasNext()) {
                    ResultRecord rr2 = (ResultRecord)ri2.next();
                    java.util.Date lastVisitDate = rr2.getDate(1);
                    // 如果没有回访记录，则lastVisitDate为null
                    if (lastVisitDate==null)
                        continue;

                    // 检查时间是否超出
                    ActionSetupDb asd = (ActionSetupDb)asd2.getActionSetupDb(customerType, unitCode);
                    if (asd!=null) {
                        int d1 = asd.getInt("remind_days");
                        int d2 = asd.getInt("expire_days");

                        // System.out.println(getClass() + " d2=" + d2 + " lastVisitDate=" + DateUtil.format(lastVisitDate, "yyyy-MM-dd") + " DateUtil.datediffHour(now, lastVisitDate)=" + DateUtil.datediffHour(now, lastVisitDate) );

                        // 大于回落时间，则回落
                        if (d2 >0 && DateUtil.datediffHour(now, lastVisitDate) > d2 * 24) {
                            fdao = fdao.getFormDAO(customerId, fd);
                            fdao.setFieldValue("kind", "" + SalesConstant.KIND_NEGLECT);
                            try {
                                fdao.save();
                            } catch (ErrMsgException e) {
                                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                            }
                            v.addElement(fdao);
                            continue ;
                        }

                        // 当前时间与最后一次行动时间的间隔大于提醒时间
                        if (d1>0 && DateUtil.datediffMinute(now, lastVisitDate)>=d1*24*60) {
                            // 超出刷新间隔范围，则不提醒，表达式的右侧加上0.5，是表示超出刷新间隔半分钟内都可以提醒，这样做是为了避免运行时的延迟导致不提醒
                        	// 而最小刷新间隔为1分钟，所以用0.5不会导致多次提醒的问题
                        	if (DateUtil.datediffMinute(now, lastVisitDate)<=d1*24*60 + actionRemindInterval + 0.5) {
                        		v.addElement(fdao.getFormDAO(customerId, fd));
                        	}
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error("sql=" + sql +  " " + StrUtil.trace(ex));
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

        ActionSetupDb asd2 = new ActionSetupDb();

        Vector v = listRemind();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FormDAO fdao = (FormDAO) ir.next();

            String customer = fdao.getFieldValue("customer");

            // 发送信息
            MessageDb md = new MessageDb();
            String t, c;

            // 如果是需回落的客户
            if (fdao.getFieldValue("kind").equals("" + SalesConstant.KIND_NEGLECT)) {
                t = StrUtil.format(crmcfg.getProperty("neglectTitle"),
                                   new
                                   Object[] {customer});
                c = StrUtil.format(crmcfg.getProperty("neglectContent"),
                                   new
                                   Object[] {customer});
            }
            else { // 否则提醒行动
                t = StrUtil.format(crmcfg.getProperty("actionRemindTitle"),
                                   new
                                   Object[] {customer});

                ActionSetupDb asd = (ActionSetupDb) asd2.getActionSetupDb(StrUtil.toInt(fdao.getFieldValue(
                        "customer_type"), 0), fdao.getUnitCode());

                c = StrUtil.format(crmcfg.getProperty("actionRemindContent"),
                                   new
                                   Object[] {new Integer(asd.getInt("remind_days")), customer
                });
            }

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
