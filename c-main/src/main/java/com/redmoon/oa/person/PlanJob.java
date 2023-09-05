package com.redmoon.oa.person;

/**
 * 周期性日程安排提醒
 */
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.*;
import java.util.*;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import com.redmoon.oa.person.*;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.sms.SMSFactory;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MobileAfterAdvice;
import org.springframework.scheduling.quartz.QuartzJobBean;

//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class PlanJob extends QuartzJobBean {
    public PlanJob() {
        try {
            jbInit();
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
    }

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        Date d = new Date();
        String currentTime = DateUtil.format(d, "HH:mm");
        Calendar c = Calendar.getInstance();
        String year = c.get(Calendar.YEAR) + "";
        String month = (c.get(Calendar.MONTH) + 1) + "";
        String day = c.get(Calendar.DATE) + "";
        String weekDate = (c.get(Calendar.DAY_OF_WEEK) - 1) + "";
        if ("0".equals(weekDate)) {
            weekDate = "7";
        }
        // PlanPeriodicityDb
        String temp = month + "-" + day;
        String sql = "select * from user_plan_periodicity where begin_date <= ? and end_date>=? and remind_count=0 and ((remind_type=1 and remind_time=" +
                     StrUtil.sqlstr(currentTime) + ")" +
                     " or (remind_type=2 and remind_date=" +
                     StrUtil.sqlstr(weekDate) + " and remind_time=" +
                     StrUtil.sqlstr(currentTime) + ")" +
                     " or (remind_type=3 and remind_date=" + StrUtil.sqlstr(day) +
                     " and remind_time=" + StrUtil.sqlstr(currentTime) + ")" +
                     " or (remind_type=4 and remind_date=" +
                     StrUtil.sqlstr(temp) + " and remind_time=" +
                     StrUtil.sqlstr(currentTime) + "))";
        Vector v = new Vector();
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[] {d, d});
            // LogUtil.getLog(getClass()).info("ri.size()=" + ri.size());
            
            while (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                PlanPeriodicityDb ppd = new PlanPeriodicityDb();
                ppd = ppd.getPlanPeriodicityDb(rr.getInt("id"));
                v.addElement(ppd);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }

        // 是否发送短信
        boolean isToMobile = SMSFactory.isUseSMS();
        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();

        Iterator ir = v.iterator();
        MessageDb md = new MessageDb();
        while (ir.hasNext()) {
            PlanPeriodicityDb ppd = (PlanPeriodicityDb) ir.next();
            try {

                if (!isToMobile) {
                    md.sendSysMsg(ppd.getString("user_name"),
                                  ppd.getString("title"),
                                  ppd.getString("content"));
                } else {
                    if (imsg != null) {
                        imsg.sendSysMsg(ppd.getString("user_name"),
                                        ppd.getString("title"),
                                        ppd.getString("content"));
                    }
                }
            } catch (ErrMsgException ex) {
                LogUtil.getLog(getClass()).error(ex.getMessage());
            }
            try {
                ppd.set("remind_count", ppd.getInt("remind_count") + 1);
                ppd.save();
            } catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
        }
    }

    private void jbInit() throws Exception {
    }
}
