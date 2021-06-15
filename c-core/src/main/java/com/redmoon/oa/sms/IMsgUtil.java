package com.redmoon.oa.sms;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.person.UserDb;

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
public interface IMsgUtil {
    public boolean send(UserDb user, String content, String sender) throws ErrMsgException ;
    public boolean send(String mobile, String content, String sender) throws ErrMsgException ;

    public boolean send(UserDb user, String content, String sender,boolean isTiming, java.util.Date timeSend,long batch) throws ErrMsgException ;
    public boolean send(String mobile, String content, String sender,boolean isTiming, java.util.Date timeSend,long batch) throws ErrMsgException ;
    public boolean send(SMSSendRecordDb ssrd) throws ErrMsgException;
    public int receive() throws ErrMsgException;
    public void checkSmsStatus() throws ErrMsgException;
    
    public int sendBatch(String[] users, String content, String sender) throws ErrMsgException;

}
