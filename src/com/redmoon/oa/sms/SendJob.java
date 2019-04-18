package com.redmoon.oa.sms;

import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import org.quartz.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.ResultIterator;

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
public class SendJob implements Job {
    com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
    int smsSendInterval = StrUtil.toInt(smscfg.getIsUsedProperty("sendInterval"), 6000); // 6秒
    int sendIncludeMinute = StrUtil.toInt(smscfg.getIsUsedProperty("sendIncludeMinute"), 10); // 10分钟内的待发送短信
    int receiveInterval = StrUtil.toInt(smscfg.getIsUsedProperty("receiveInterval"), 2000); // 2秒
    int sendMaxCountOnFail = StrUtil.toInt(smscfg.getIsUsedProperty("sendMaxCountOnFail"), 1); // 1次

    public SendJob() {
    }

    public void sendSms() {
        try {
            // 为防止因多线程调度，而致反复发送短信，置时间戳
            java.util.Date dd = new java.util.Date();
            java.util.Date d = DateUtil.addMinuteDate(dd, -sendIncludeMinute);
            long t = System.currentTimeMillis();
            String sql = "update sms_send_record set msg_flag='" + t + "' where is_sended=0 and msg_id=-1 and sendtime>? and send_count<? and is_timing=0 and msg_flag = '0'";
            JdbcTemplate jt = new JdbcTemplate();
            jt.executeUpdate(sql, new Object[]{d, new Integer(sendMaxCountOnFail)});

            // 选择已被置时间戳的记录，以避免与别的线程发送同一条记录
            sql = "select id from sms_send_record where is_sended=0 and msg_id=-1 and msg_flag='" + t + "' and sendtime>? and send_count<? and is_timing=0 order by id asc";
            IMsgUtil imu = SMSFactory.getMsgUtil();
            // 检查短信发送状态
            imu.checkSmsStatus();

            ResultIterator ri = jt.executeQuery(sql, new Object[] {d, new Integer(sendMaxCountOnFail)});
            // LogUtil.getLog(getClass()).info(getClass() + " 待发送短信条数：" + ri.size() + " 短信起始时间为：" + DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));

            SMSSendRecordDb ssrd2 = new SMSSendRecordDb();
            int remainCount = smscfg.canSendSMS(ri.size());//剩余条数

            while (ri.hasNext() && remainCount > 0) {
                ResultRecord rr = (ResultRecord) ri.next();
                SMSSendRecordDb ssrd = ssrd2.getSMSSendRecordDb(rr.getInt(1));
                // 超出最大次数，则不发送
                // if (ssrd.getSendCount()>=sendMaxCountOnFail)
                //    continue;
                // System.out.println(getClass() + " msgText=" + ssrd.getMsgText() + " " + ssrd.getUserName());
                // sender.send(ssrd);


                    //System.out.print("ccc");
                boolean re = imu.send(ssrd);
                if (re) {
                    remainCount --;
                    ssrd.setSended(true);
                    ssrd.save();
                }

                // 延迟数秒
                // Thread.sleep(smsSendInterval);
                /*
                if (System.currentTimeMillis() - lastReceiveTime >= receiveInterval) {
                    receiveSms();
                    Thread.sleep(smsSendInterval);
                    lastReceiveTime = System.currentTimeMillis();
                }
                */
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("sendSms:" + e.getMessage());
        }

        try {
            // 定时发送
            // 为防止因多线程调度，而致反复发送短信，置时间戳
            java.util.Date dd = new java.util.Date();
            java.util.Date d = DateUtil.addMinuteDate(dd, -sendIncludeMinute);
            long t = System.currentTimeMillis();
            String sql = "update sms_send_record set msg_flag='" + t + "' where is_sended=0 and msg_id=-1 and is_timing=1 and send_count<? and (time_send>? and time_send<?) and msg_flag = '0'";
            //String sql = "update sms_send_record set msg_flag='" + t + "' where is_sended=0 and msg_id=-1 and sendtime>? and send_count<? and is_timing=0 and msg_flag = '0'";
            JdbcTemplate jt = new JdbcTemplate();
            jt.executeUpdate(sql, new Object[]{new Integer(sendMaxCountOnFail),d,new java.util.Date()});

            // 选择已被置时间戳的记录，以避免与别的线程发送同一条记录

            sql = "select id from sms_send_record where is_sended=0 and msg_id=-1 and msg_flag='" + t + "' and is_timing=1 and send_count<? and (time_send>? and time_send<?) order by id asc";
            IMsgUtil imu = SMSFactory.getMsgUtil();
            // 检查短信发送状态
            imu.checkSmsStatus();

            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Integer(sendMaxCountOnFail),d,new java.util.Date()});
            // LogUtil.getLog(getClass()).info(getClass() + " 待发送短信条数：" + ri.size() + " 短信起始时间为：" + DateUtil.format(d, "yyyy-MM-dd HH:mm:ss"));

            SMSSendRecordDb ssrd2 = new SMSSendRecordDb();
            int remainCount = smscfg.canSendSMS(ri.size());//剩余条数

            while (ri.hasNext() && remainCount > 0) {
                ResultRecord rr = (ResultRecord) ri.next();
                SMSSendRecordDb ssrd = ssrd2.getSMSSendRecordDb(rr.getInt(1));
                // 超出最大次数，则不发送
                // if (ssrd.getSendCount()>=sendMaxCountOnFail)
                //    continue;
                // System.out.println(getClass() + " msgText=" + ssrd.getMsgText() + " " + ssrd.getUserName());
                // sender.send(ssrd);
                //System.out.print("ccc");
                boolean re = imu.send(ssrd);
                if (re) {
                    remainCount --;
                    ssrd.setSended(true);
                    ssrd.save();
                }

                // 延迟数秒
                // Thread.sleep(smsSendInterval);
                /*
                if (System.currentTimeMillis() - lastReceiveTime >= receiveInterval) {
                    receiveSms();
                    Thread.sleep(smsSendInterval);
                    lastReceiveTime = System.currentTimeMillis();
                }
                */
            }
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("sendSms:" + e.getMessage());
        }



    }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     * @todo Implement this org.quartz.Job method
     */
    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        try {
            sendSms();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }
}
