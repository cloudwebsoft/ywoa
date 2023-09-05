package com.redmoon.oa.sms;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;

import com.redmoon.oa.person.*;

import com.redmoon.oa.sys.DebugUtil;

import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.redmoon.oa.db.SequenceManager;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Vector;
import java.util.Iterator;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.*;


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
public class RoyaMasMobileMsgUtil implements IMsgUtil {

    public synchronized void increaseTailAddr() {
    }

    public String getOrgAddr() {
        return "";
    }

    /**
     * 用于通知等的发送
     * @param user UserDb
     * @param msgText String
     * @param sender String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean send(UserDb user, String msgText, String sender) throws
            ErrMsgException {
        if (!Config.isValidMobile(user.getMobile())) {
            LogUtil.getLog(getClass()).error(user.getRealName() + " 的手机号非法！");
            return false;
        }

        long batch = 0l;
        try {
            batch = SMSSendRecordDb.getBatchCanUse();
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex.getMessage());
        }

        boolean re = false;
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        //int id = ssrd.getId();
        // re = sendSMS(msgText, user.getMobile(), "1", ""+id, "vip", "9");
        // if (re) {
        String[] contexts = divContext(msgText);
        for(int i = 0; i < contexts.length; i ++){
            ssrd.setUserName(sender);
            ssrd.setSendMobile(user.getMobile());
            ssrd.setMsgText(contexts[i]);
            ssrd.setReceiver(user.getRealName());
            ssrd.setBatch(batch);
            re = ssrd.create();
        }
        return re;
    }

    public void checkSmsStatus() {
    }

    public int receive() {
        return 0;
    }

    public boolean send(String mobile, String msgText, String sender) throws
            ErrMsgException {
        if (!Config.isValidMobile(mobile)) {
            return false;
        }
        long batch = 0l;
        try {
            batch = SMSSendRecordDb.getBatchCanUse();
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex.getMessage());
        }

        boolean re = false;
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        //int id = ssrd.getId();
        // re = sendSMS(msgText, mobile, "1", ""+id, "vip", "9");
        // 记录发送的短信
        String[] contexts = divContext(msgText);
        for(int i = 0; i < contexts.length; i ++){
            ssrd.setUserName(sender);
            ssrd.setSendMobile(mobile);
            ssrd.setMsgText(contexts[i]);
            ssrd.setOrgAddr("");
            ssrd.setBatch(batch);
            re = ssrd.create();
        }
        return re;
    }

    public boolean send(SMSSendRecordDb ssrd) {
    	String mobile = ssrd.getSendMobile();
		int id = ssrd.getId();
		String userName = ssrd.getUserName();
		UserDb userDb = new UserDb();
		String realName = "";
		if(userName.equals("系统")){
			realName = "系统";
		}else{
			userDb = userDb.getUserDb(userName);
			if(userDb != null){
				realName = userDb.getRealName();
			}else{
				realName = "系统";
			}
		}
		String msg = ssrd.getMsgText();
		com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
		if (cfg.getBooleanProperty("isSmsWithSign")) {
            msg += " (" + realName + ")";
        }

		UserDb user = new UserDb(userName);
        String exNumber = StrUtil.PadString(user.getId()+"",'0',4,true) ;
        return sendSMS(msg, mobile, "1", String.valueOf(id), "vip", exNumber);
    }

    public boolean sendSMS(String content, String mobilePhones, String priority,
                           String messageFlag, String moduleName,
                           String exNumber) {
        com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
        String royaMasSendSmsUrl = smscfg.getIsUsedProperty("royaMasSendSmsUrl");
        String strURL = royaMasSendSmsUrl.trim();
        //urlencode 处理短信参数中的空格
        String contentSms = content;
        try {
            contentSms = java.net.URLEncoder.encode(contentSms, "GBK");
        } catch (UnsupportedEncodingException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(getClass() + "SendSms to RoyaMas: "+ e);
        }

//        new Exception().printStackTrace();

        String response = "";
        try {
            if (true) {
                strURL += "?MobilePhones=" + mobilePhones
                        + "&Content=" + contentSms
                        + "&Priority=" + priority
                        + "&ExNumber=" + 9 + exNumber
                        + "&MessageFlag=" + messageFlag
                        + "&ModuleName=" + moduleName;
                URL objURL = new URL(strURL);
                URLConnection objConn = objURL.openConnection();
                objConn.setDoInput(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        objConn.getInputStream()));
                String line = br.readLine();
                while (line != null) {
                    response += line;
                    line = br.readLine();
                }
                br.close();
                DebugUtil.i(getClass(), "sendSMS", "SendSMS to " + mobilePhones + " Content=" + content + " response=" + response);
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
            return false;
        }
        return true;
    }

    public boolean send(UserDb user, String content, String sender,
                        boolean isTiming, Date timeSend, long batch) throws
            ErrMsgException {
        if (!Config.isValidMobile(user.getMobile())) {
            LogUtil.getLog(getClass()).error(user.getRealName() + " 的手机号非法！");
            return false;
        }
        boolean re = false;
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        //int id = ssrd.getId();
        // re = sendSMS(msgText, user.getMobile(), "1", ""+id, "vip", "9");
        // if (re){
        String[] contexts = divContext(content);
        for(int i = 0; i < contexts.length; i ++){
            ssrd.setUserName(sender);
            ssrd.setSendMobile(user.getMobile());
            ssrd.setMsgText(contexts[i]);
            ssrd.setReceiver(user.getRealName());
            ssrd.setTiming(isTiming);
            ssrd.setTimeSend(timeSend);
            ssrd.setBatch(batch);
            re = ssrd.create();
         }
        return re;
    }

    public boolean send(String mobile, String content, String sender,
                        boolean isTiming, Date timeSend, long batch) throws
            ErrMsgException {
        if (!Config.isValidMobile(mobile)) {
            return false;
        }
        boolean re = false;
        SMSSendRecordDb ssrd = new SMSSendRecordDb();
        //int id = ssrd.getId();
        // re = sendSMS(msgText, mobile, "1", ""+id, "vip", "9");
        // 记录发送的短信
        String[] contexts = divContext(content);
        for(int i = 0; i < contexts.length; i ++){
            ssrd.setUserName(sender);
            ssrd.setSendMobile(mobile);
            ssrd.setMsgText(contexts[i]);
            ssrd.setOrgAddr("");
            ssrd.setTiming(isTiming);
            ssrd.setTimeSend(timeSend);
            ssrd.setBatch(batch);
            re = ssrd.create();
        }
        return re;
    }

    public String[] divContext(String context){
        if(true){//如果不按70个自动切分短信
            return new String[]{context};
        }

        String[] contexts = null;
        if(context==null||context.length()<Config.CONTEXT_DIV){
            contexts = new String[]{context};
        }else{
            Vector v = new Vector();
            String temp = context;
            String item = "";
            while(temp.length()>Config.CONTEXT_DIV){
                item = temp.substring(0, Config.CONTEXT_DIV);
                temp = temp.substring(Config.CONTEXT_DIV);
                v.add(item);
            }
            v.add(temp);
            int length = v.size();
            contexts = new String[length];
            Iterator ir = v.iterator();
            int i = 0;
            while(ir.hasNext()){
                contexts[i] = (String)ir.next();
                i ++;
            }
        }
        return contexts;
    }

    public int sendBatch(String[] users, String content, String sender) throws ErrMsgException {
    	if (users==null || users.length==0) {
    		return 0;
    	}
    	
    	int len = users.length;
    	
        long batch = 0l;
        try {
            batch = SMSSendRecordDb.getBatchCanUse();
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex.getMessage());
        }
    	
		UserDb user = new UserDb();
    	JdbcTemplate jt = new JdbcTemplate(new com.cloudwebsoft.framework.db.
                Connection(cn.js.fan.web.Global.getDefaultDB()));
		try {
			for (int i=0; i<len; i++) {
				String receiver = users[i];
				user = user.getUserDb(receiver);
				if (user==null) {
					user = new UserDb();
				}
				
		        String sql = 
	                "insert into sms_send_record (id,userName,sendMobile,msgText,";
		        	sql += "receiver, batch) values (";
		        int id = (int) SequenceManager.nextID(SequenceManager.OA_SMS_SEND_RECORD);
		        sql += id + "," + StrUtil.sqlstr(sender) + "," + StrUtil.sqlstr(user.getMobile()) + ",";
		        sql += StrUtil.sqlstr(content) + "," + StrUtil.sqlstr(receiver) + "," + batch;
                sql += ")";

				jt.addBatch(sql);
			}
			jt.executeBatch();
		} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
			return 0;
		}         	
    	
    	return len;
    }

}
