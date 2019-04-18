package com.redmoon.oa.sale;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.message.*;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.*;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.visual.FormDAO;
import org.quartz.*;

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
public class ContractJob implements Job {
    public ContractJob() {
        super();
    }

    public Vector listWillExpire() {
        ContractConfig cfg = new ContractConfig();
        // 开始日期在从今天起往前多少天内的合同快到期时发出提醒
        int contractExpireDay = StrUtil.toInt(cfg.get(
                "contractExpireDay"), 365 * 3); // 3年
        // 在合同到期前多少天开始提醒
        int contractExpireRemindBeforeDay = StrUtil.toInt(cfg.get(
                "contractExpireRemindBeforeDay"), 60); // 60天

        // 已经到期的不再提醒
        String sql =
                "select id from form_table_sales_contract where enddate<=? and enddate>? and begindate>=?";
        java.util.Date d1 = DateUtil.addDate(new java.util.Date(),
                contractExpireRemindBeforeDay);
        java.util.Date d2 = DateUtil.addDate(new java.util.Date(),
                                             -contractExpireDay);

        FormDb fd = new FormDb("sales_contract");
        FormDAO fdao = new FormDAO(fd);
        Vector v = new Vector();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {d1,  new java.util.Date(), d2});
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                v.addElement(fdao.getFormDAO(rr.getInt(1), fd));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("listWillExpire:" +
                                             StrUtil.trace(e));
        }
        return v;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        // 根据快要到期的合同，发送提醒
        ContractConfig cfg = new ContractConfig();

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

        Vector v = listWillExpire();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            FormDAO fdao = (FormDAO) ir.next();
            // 发送信息
            MessageDb md = new MessageDb();
            String t = StrUtil.format(cfg.get("contractExpireRemindTitle"),
                                      new
                                      Object[] {fdao.getFieldValue("contact_name")});
            String c = StrUtil.format(cfg.get("contractExpireRemindContent"),
                                      new
                                      Object[] {fdao.getFieldValue("contact_name"),
                                      fdao.getFieldValue("enddate")});

            try {
                Iterator ir2 = com.redmoon.oa.pvg.Privilege.getUsersHavePriv("sales.contract", fdao.getUnitCode()).
                               iterator();
                while (ir2.hasNext()) {
                    UserDb user = (UserDb) ir2.next();
                    if (!isToMobile)
                        md.sendSysMsg(user.getName(), t, c);
                    else {
                        if (imsg != null)
                            imsg.sendSysMsg(user.getName(), t, c);
                    }
                }
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("execute2:" + e.getMessage());
            }

        }
    }
}
