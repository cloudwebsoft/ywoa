package com.redmoon.oa.sms;

import cn.js.fan.util.*;

import com.redmoon.oa.person.*;
import java.util.Date;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MobileMsgUtil implements IMsgUtil {

    /**
     * 短信发送，用于调度中实际发送短信
     * @param ssrd SMSSendRecordDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean send(SMSSendRecordDb ssrd) throws ErrMsgException {
        return true;
    }
    public int receive() throws ErrMsgException {
        return 1;
    }

    /**
     * 检查短信发送后的状态，是否发送成功
     * @throws ErrMsgException
     */
    public void checkSmsStatus() throws ErrMsgException {

    }

    /**
     * 发送短消息时，一起发送短信，本方法只记录待发送的短信至sms_send_record表中
     * @param user UserDb
     * @param msgText String
     * @param sender String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean send(UserDb user, String msgText, String sender) throws
            ErrMsgException {
        // 记录发送的短信
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        ssrd.setUserName(sender);
        ssrd.setSendMobile(user.getMobile());
        ssrd.setReceiver(user.getRealName());
        ssrd.setMsgText(msgText);
        return ssrd.create();
    }

    /**
     * 通过短信发送页面，发送短信，本方法只记录待发送的短信至sms_send_record表中
     * @param mobile String
     * @param msgText String
     * @param sender String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean send(String mobile, String msgText, String sender) throws
            ErrMsgException {
        // 记录发送的短信
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        ssrd.setUserName(sender);
        ssrd.setSendMobile(mobile);
        ssrd.setMsgText(msgText);
        return ssrd.create();
    }

    public boolean send(UserDb user, String content, String sender,
                        boolean isTiming, Date timeSend, long batch) throws
            ErrMsgException {
            // 记录发送的短信
      SMSSendRecordDb ssrd = new SMSSendRecordDb();
      ssrd.setUserName(sender);
      ssrd.setSendMobile(user.getMobile());
      ssrd.setReceiver(user.getRealName());
      ssrd.setMsgText(content);
      ssrd.setTiming(isTiming);
      ssrd.setTimeSend(timeSend);
      ssrd.setBatch(batch);
      return ssrd.create();
    }

    public boolean send(String mobile, String content, String sender,
                        boolean isTiming, Date timeSend, long batch) throws
            ErrMsgException {
        //记录发送的短信
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        ssrd.setUserName(sender);
        ssrd.setSendMobile(mobile);
        ssrd.setMsgText(content);
        ssrd.setTiming(isTiming);
        ssrd.setTimeSend(timeSend);
        ssrd.setBatch(batch);
        return ssrd.create();

    }

    public int sendBatch(String[] users, String content, String sender) throws ErrMsgException {
    	return 0;
    }

}
