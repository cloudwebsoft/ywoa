package com.redmoon.oa.sms;

import cn.js.fan.db.Conn;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;

import com.redmoon.oa.person.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.Iterator;
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
public class KWTMobileMsgUtil implements IMsgUtil {
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
    	Conn conn1 = new Conn(Global.defaultDB);
    	Config config = new Config();
    	String smsPath = config.getIsUsedProperty("smsPath");
    	
    	try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String dburl = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+smsPath;// 此为NO-DSN方式
	    	Connection conn = DriverManager.getConnection(dburl);
	    	Statement stmt = conn.createStatement();
	    	
	    	PreparedStatement pstmt = null;
	    	String sql = "insert into sms_receive_record (mobile,content,mydate,dest_addr) values (?,?,?,?)";
	    	ResultSet rs = stmt.executeQuery("select * from InBox");
	    	
	    	while(rs.next()){
	    		String id = rs.getString("ID");
	    		String mobileNumber = rs.getString("mbno");
	    		String msgText = rs.getString("Msg");
	    		java.sql.Date arriveDate = rs.getDate("ArriveTime");
	    		//String comport = rs.getString("4");
	    		
	    		pstmt = conn1.prepareStatement(sql);
	    		pstmt.setString(1, mobileNumber);
				pstmt.setString(2, msgText);
				pstmt.setTimestamp(3, new Timestamp(arriveDate.getTime()));
				pstmt.setString(4, "");
	    		
				pstmt.executeUpdate();
				
				stmt.executeUpdate("delete from InBox where ID="+id);
				
 	    	}
	    	rs.close();
	    	stmt.close();
	    	conn.close();
		} catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
		}finally {
			if (conn1 != null) {
				conn1.close();
				conn1 = null;
			}
		}
    	
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
		String msg = ssrd.getMsgText() + " (" + realName + ")";
		//String msg = ssrd.getMsgText();
		UserDb user = new UserDb(userName);

        String exNumber = StrUtil.PadString(user.getId()+"",'0',4,true) ;
        //int id = Integer.parseInt(StrUtil.PadString(ssrd.getUserName(),'0',4,true));
        return sendSMS(msg, mobile, "1", ""+id, "vip", exNumber);
    }

    public boolean sendSMS(String content, String mobilePhones, String priority,
                           String messageFlag, String moduleName,
                           String exNumber) {
    	
    	Config config = new Config();
    	String smsPath = config.getIsUsedProperty("smsPath");
    	
    	try {
    		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String dburl = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+smsPath;// 此为NO-DSN方式
	    	Connection conn = DriverManager.getConnection(dburl);
	    	Statement stmt = conn.createStatement();
	    	
	    	
    		String sql = "select id from sms_send_record where is_sended = 0";
        	SMSSendRecordDb smsSendRecordDb = new SMSSendRecordDb();
        	Iterator ir = smsSendRecordDb.list(sql).iterator();
        	SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	while(ir.hasNext()){
        		smsSendRecordDb = (SMSSendRecordDb)ir.next();
        		int id = smsSendRecordDb.getId();
        		String userName = smsSendRecordDb.getUserName();
        		String sendMobile = smsSendRecordDb.getSendMobile();
        		String msgText = smsSendRecordDb.getMsgText();
        		Date sendTime = smsSendRecordDb.getSendTime();
        		
        		if(sendTime == null){
        			sendTime = new Date();
        		}
        		String sendDate = sd.format(sendTime);
        		String kwtSql = "insert into OutBox(username,Mbno,Msg,SendTime,report,ComPort,V1,V2,V3,V4,V5) values ('"+userName+"','"+sendMobile+"','"+msgText+"','"+sendDate+"',0,0,'','','','','')";
        		boolean flag = stmt.execute(kwtSql);
        		if(!flag){
        			SMSSendRecordDb sendRecordDb = new SMSSendRecordDb();
        			String updateSql = "select id from sms_send_record where id="+id;
        			sendRecordDb = (SMSSendRecordDb)sendRecordDb.list(updateSql).get(0);
        			sendRecordDb.setSended(true);
        			sendRecordDb.save();
        		}
        	}
			
	    	
	    	stmt.close();
	    	conn.close();
		} catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
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
        //LogUtil.getLog(getClass()).info(this.getClass().getName()+"调用的send（mobile）方法");
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
        if(false){//如果不按70个自动切分短信
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
    	return 0;
    }

}
