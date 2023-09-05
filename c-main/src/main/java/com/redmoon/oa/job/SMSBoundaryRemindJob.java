package com.redmoon.oa.job;

import com.redmoon.oa.sms.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import java.sql.SQLException;
import com.redmoon.oa.message.MessageDb;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.person.UserDb;
import java.util.Vector;
import java.util.Iterator;
import cn.js.fan.util.ResKeyException;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class SMSBoundaryRemindJob extends QuartzJobBean {
    public SMSBoundaryRemindJob() {
    }

    public void remind() throws SQLException{
        Config cfg = new Config();
        int boundary = cfg.getBoundary();
        int boundaryCount = 0;//当前标准即，多少条以内提醒
        int count = 0;//剩余的短信数量
        SMSRemindMgr srdMgr = new SMSRemindMgr();
        //当没有启用短信配额、当天已经提醒过、启用的是短信年配额并且不在有效期内时自动返回，不做提示。
        if(!SMSFactory.isUseSMS()){//短信没有启用
            return ;
        }
        if(boundary==Config.SMS_BOUNDARY_DEFAULT){//没有启用短信配额
            return ;
        }else if(boundary==Config.SMS_BOUNDARY_YEAR){//短信年配额
            SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
            if(sbyMgr.isInUse(new Date())){
                if(srdMgr.isRemind(Config.SMS_BOUNDARY_YEAR)){
                    //已经提醒过
                    return ;
                }
                count = sbyMgr.getRemainingCount();
                boundaryCount = srdMgr.getBoundary(Config.SMS_BOUNDARY_YEAR);

                if(count<boundaryCount){
                    /*SMSRemindDb srDb = new SMSRemindDb();
                    srDb = srDb.getSMSRemindDb(1);*/
                    String title = StrUtil.format(srdMgr.getTitle(Config.SMS_BOUNDARY_YEAR),new Object[]{count+""}) ;
                    String content = StrUtil.format(srdMgr.getContent(Config.SMS_BOUNDARY_YEAR),new Object[]{sbyMgr.getTotal()+"",count+""});
                    try {
                        send(title,content);
                        /*srDb.set("remind_date",new Date());
                        srDb.save();*/
                        srdMgr.saveDate(new Date(),Config.SMS_BOUNDARY_YEAR);
                    } catch (ErrMsgException ex) {
                        LogUtil.getLog(getClass()).error("execute3:" + ex.getMessage());
                    }
                }else{//无需提醒
                    return ;
                }
            }else{
                //不在配额有效期内
                return ;
            }
        }else if(boundary==Config.SMS_BOUNDARY_MONTH){//短信月配额
            if(srdMgr.isRemind(Config.SMS_BOUNDARY_MONTH)){
                //已经提醒过
                return ;
            }
            int month = DateUtil.getMonth(new Date());
            SMSBoundaryMonthMgr sbMDb = new SMSBoundaryMonthMgr();
            count = sbMDb.getRemainingCount(month);
            boundaryCount = srdMgr.getBoundary(Config.SMS_BOUNDARY_MONTH);

            if(count<boundaryCount){
                /*SMSRemindDb srDb = new SMSRemindDb();
                srDb = srDb.getSMSRemindDb(2);*/
                String title = StrUtil.format(srdMgr.getTitle(Config.SMS_BOUNDARY_MONTH),new Object[]{count+""}) ;
                String content = StrUtil.format(srdMgr.getContent(Config.SMS_BOUNDARY_MONTH),new Object[]{sbMDb.getTotal()+"",count+""});
                try {
                    send(title,content);
                    srdMgr.saveDate(new Date(),Config.SMS_BOUNDARY_MONTH);
                    /*srDb.set("remind_date",new Date());
                    srDb.save();*/
                } catch (ErrMsgException ex) {
                    LogUtil.getLog(getClass()).error("execute3:" + ex.getMessage());
                }
            }else{//无需提醒
                return ;
            }
        }
    }

    private void send(String title,String content) throws ErrMsgException {
        String sql = "select username from user_priv where priv='sms.admin' or priv='admin' or username='admin'  order by username asc";
        UserDb ud = new UserDb();
        Vector v = ud.list(sql);
        if(v!=null&&v.size()!=0){
            MessageDb md = new MessageDb();
            Iterator ir = v.iterator();
            while(ir.hasNext()){
                ud = (UserDb)ir.next();
                md.sendSysMsg(ud.getName(),title,content);
            }
        }
    }

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        try {
            remind();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }
}
