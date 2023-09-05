package com.redmoon.oa.job;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.system.OaSysVerMgr;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class BirthdayJob extends QuartzJobBean {

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        action();
    }

    public synchronized void action() {
        String birthdayRemindMonth = "", birthdayRemindDay="";
        OaSysVerMgr oaSysVerMgr = new OaSysVerMgr();
        oaSysVerMgr = oaSysVerMgr.getOaSysVer();
        String isBirthdayRemindDate = oaSysVerMgr.getBirthdayRemindDate();
        if (!"".equals(isBirthdayRemindDate)) {
            birthdayRemindMonth = isBirthdayRemindDate.substring(0, 2);
            birthdayRemindDay = isBirthdayRemindDate.substring(3, 5);
        }

        String nowMonth = DateUtil.format(new Date(), "MM");
        String nowDate = DateUtil.format(new Date(), "dd");

        // 当天与上次发送不在同一月同一天时，才发送短信，以保证每天只发送一次
        if (birthdayRemindMonth.equals(nowMonth)) {
            if (!birthdayRemindDay.equals(nowDate)) {
                sendBirthdaySms(nowMonth, nowDate);
            }
        } else {
            sendBirthdaySms(nowMonth, nowDate);
        }
    }

    public void sendBirthdaySms(String nowMonth, String nowDate) {
        // @task
        UserDb userdb = new UserDb();
        String sql;
        // @task
        switch (Global.db) {
            case Global.DB_SQLSERVER:
                sql =
                        "select distinct name from users where DATEPART(Month, birthday) = " +
                                StrUtil.sqlstr(nowMonth) + " and DATEPART(Day, birthday) = " +
                                StrUtil.sqlstr(nowDate) + " order by name";
                break;
            case Global.DB_ORACLE:
                sql =
                        "select distinct name from users where to_char(birthday,'mm')=" +
                                StrUtil.sqlstr(nowMonth) + " and to_char(birthday,'dd') = " +
                                StrUtil.sqlstr(nowDate) + " order by name";
                break;
            case Global.DB_POSTGRESQL:
                sql =
                        "select distinct name from users where to_char(birthday,'mm')=" +
                                StrUtil.sqlstr(nowMonth) + " and to_char(birthday,'dd') = " +
                                StrUtil.sqlstr(nowDate) + " order by name";
                break;
            default:
                sql =
                        "select distinct name from users where month(birthday) = " +
                                StrUtil.sqlstr(nowMonth) + " and dayofmonth(birthday) = " +
                                StrUtil.sqlstr(nowDate) + " order by name";
        }

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
        String birthdayRemindTitle = cfg.get("birthdayRemindTitle");
        String birthdayRemindContent = cfg.get("birthdayRemindContent");

        Iterator ir = userdb.list(sql).iterator();
        int nCur = 0;
        String persons = "";
        while (ir.hasNext()) {
            UserDb user = (UserDb) ir.next();
            nCur++;
            if (nCur > 1) {
                persons += ",";
            }
            persons += user.getName();
        }
        if (!"".equals(persons.trim())) {
            sendMsg(birthdayRemindTitle, birthdayRemindContent, StrUtil.split(persons, ","));
        }
        OaSysVerMgr oaSysVerMgr = new OaSysVerMgr();
        oaSysVerMgr.updateBirthdayRemindDate(nowMonth + "." + nowDate);
    }

    public void sendMsg(String smsTitle, String smsContent,
                        String[] users) {
        IMessage imsg;
        ProxyFactory proxyFactory = new ProxyFactory("com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        //proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();
        int len = users.length;
        try {
            for (String user : users) {
                imsg.sendSysMsg(user, smsTitle, smsContent);
            }
        } catch (ErrMsgException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("sendBirthdayRemindMsg:" + StrUtil.trace(e));
        }
    }
}
