package com.redmoon.oa.sale;

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
import com.redmoon.oa.crm.CRMConfig;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.person.UserDb;

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
public class SalesStockAlertJob implements Job {
    public SalesStockAlertJob() {
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

        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_product_info");
        FormDAO fdaoProduct = new FormDAO();

        SalesStockProductDb spd = new SalesStockProductDb();
        RoleDb rd = new RoleDb();

        FormDAO fdao = new FormDAO();
        String formCode = "sales_stock_alert";
        String sql = "select id from form_table_sales_stock_alert";
        MessageDb md = new MessageDb();
        Iterator ir = null;
        try {
            ir = fdao.list(formCode, sql).iterator();
            while (ir.hasNext()) {
                fdao = (FormDAO) ir.next();
                long productId = StrUtil.toLong(fdao.getFieldValue("product"), -1);
                int num = StrUtil.toInt(fdao.getFieldValue("num"), -1); // 预警值

                // 从库存中取得当前该产品的数量
                int curNum = spd.getNumOfProduct(productId);
                if (curNum <= num) {
                    fdaoProduct = fdaoProduct.getFormDAO(productId, fd);
                    // 报警
                    String t = StrUtil.format(crmcfg.getProperty(
                            "stockAlertRemindTitle"),
                                              new
                                              Object[] {fdao.getFieldValue(
                            "product_name")});
                    String c = StrUtil.format(crmcfg.getProperty(
                            "stockAlertRemindContent"),
                                              new
                                              Object[] {fdao.getFieldValue(
                            "product_name"), new Integer(curNum), new Integer(num)
                    });

                    // 取得报警发送角色
                    String roleCode = fdao.getFieldValue("role");
                    rd = rd.getRoleDb(roleCode);
                    Iterator iruser = rd.getAllUserOfRole().iterator();
                    UserDb user;
                    while (iruser.hasNext()) {
                        user = (UserDb)iruser.next();
                        try {
                            if (!isToMobile)
                                md.sendSysMsg(user.getName(), t, c);
                            else {
                                if (imsg != null)
                                    imsg.sendSysMsg(user.getName(), t, c);
                            }
                        } catch (ErrMsgException e) {
                            LogUtil.getLog(getClass()).error("execute2:" +
                                    e.getMessage());
                        }
                    }
                }
            }
        } catch (ErrMsgException ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
    }
}
