package com.redmoon.oa;

import cn.js.fan.kernel.BaseSchedulerUnit;
import java.util.Calendar;
import cn.js.fan.util.DateUtil;
import com.redmoon.oa.person.UserDb;
import java.util.*;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.message.IMessage;
import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class OASchedulerBirthday extends BaseSchedulerUnit {
    String isBirthdayRemindUsed = "";
    String birthdayRemindContent = "";
    String isBirthdayRemindDate = "";       
    String birthdayRemindTitle = "生日祝贺";
    String persons= "";
    String birthdayRemindMonth = "";
    String birthdayRemindDay = "";
    String nowMonth = "" + DateUtil.format(new java.util.Date(), "MM");
    String nowDate = "" + DateUtil.format(new java.util.Date(), "dd");

    public OASchedulerBirthday() {
        lastTime = System.currentTimeMillis();
        interval = 2400000; // 每隔40分钟本调度执行一次
        name = "OA Scheduler Birthday";
        
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        isBirthdayRemindUsed = cfg.get("isBirthdayRemindUsed");
        birthdayRemindContent = cfg.get("birthdayRemindContent");
        isBirthdayRemindDate = cfg.get("isBirthdayRemindDate"); 
		birthdayRemindMonth = isBirthdayRemindDate.substring(0,2);
    	birthdayRemindDay = isBirthdayRemindDate.substring(3,5);       
    }

    /**
     * OnTimer
     *
     * @param currentTime long
     * @todo Implement this cn.js.fan.kernal.ISchedulerUnit method
     */
    public void OnTimer(long curTime) {
        if (curTime - lastTime >= interval) {
            action();
            lastTime = curTime;

        }
    }

    public synchronized void action() {
        Calendar nowTime = Calendar.getInstance();
        int nowHour = nowTime.get(Calendar.HOUR_OF_DAY);
        if (nowHour >= 0 && nowHour <24){
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();                	
            isBirthdayRemindDate = cfg.get("isBirthdayRemindDate");
            birthdayRemindMonth = isBirthdayRemindDate.substring(0,2);
            birthdayRemindDay = isBirthdayRemindDate.substring(3,5);
            nowMonth = DateUtil.format(new java.util.Date(), "MM");
            nowDate = DateUtil.format(new java.util.Date(), "dd");

            // 当天与上次发送不在同一月同一天时，才发送短信，以保证每天只发送一次
            if (birthdayRemindMonth.equals(nowMonth)){
            	if (!birthdayRemindDay.equals(nowDate)){
            		 sendBirthdaySms();
            	}
            }else{
            	sendBirthdaySms();
            }
            /*if (!birthdayRemindMonth.equals(nowMonth) && !birthdayRemindDay.equals(nowDate)){
                sendBirthdaySms();
            }*/
        }
    }

    public void sendBirthdaySms() {
        // @task
        UserDb userdb = new UserDb();
        String sql_birthday =
                "select distinct name from users where month(birthday) = " +
                StrUtil.sqlstr(nowMonth) + " and dayofmonth(birthday) = " +
                StrUtil.sqlstr(nowDate) + " order by name";
        // @task
        if (Global.db.equals(Global.DB_SQLSERVER)) {
            sql_birthday =
                    "select distinct name from users where DATEPART(Month, birthday) = " +
                    StrUtil.sqlstr(nowMonth) + " and DATEPART(Day, birthday) = " +
                    StrUtil.sqlstr(nowDate) + " order by name";
        } else if (Global.db.equals(Global.DB_ORACLE)) {
            sql_birthday =
                    "select distinct name from users where to_char(birthday,'mm')=" +
                    StrUtil.sqlstr(nowMonth) + " and to_char(birthday,'dd') = " +
                    StrUtil.sqlstr(nowDate) + " order by name";
        }else if (Global.db.equals(Global.DB_POSTGRESQL)) {
            sql_birthday =
                "select distinct name from users where to_char(birthday,'mm')=" +
                StrUtil.sqlstr(nowMonth) + " and to_char(birthday,'dd') = " +
                StrUtil.sqlstr(nowDate) + " order by name";
    }

        Iterator ir = userdb.list(sql_birthday).iterator();
        int nCur = 0;
        persons= "";
        while (ir.hasNext()) {
            UserDb user = (UserDb) ir.next();
            nCur++;
            if (nCur > 1)
                persons += ",";
            persons += user.getName();
        }
        if (!persons.trim().equals(""))
            sendMsg(birthdayRemindTitle, birthdayRemindContent,
                    StrUtil.split(persons, ","));
        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();        
        cfg.put("isBirthdayRemindDate",nowMonth+"."+nowDate);
    }

    public void sendMsg(String smsTitle, String smsContent,
                        String[] users) {
        String t = smsTitle;
        IMessage imsg = null;
        String smsContentFormat = smsContent;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        //proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();
        int len = users.length;
        try {
            for (int i = 0; i < len; i++) {
                imsg.sendSysMsg(users[i], t, smsContentFormat);
            }
        } catch (ErrMsgException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("sendBirthdayRemindMsg:" + StrUtil.trace(e));
        }
    }




}
