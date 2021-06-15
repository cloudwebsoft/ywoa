package com.redmoon.oa.sms;

import gnu.io.CommPort;
import gnu.io.PortInUseException;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.sendsms.GatewayException;
import cn.sendsms.InboundMessage;
import cn.sendsms.OutboundMessage;
import cn.sendsms.SendSMSException;
import cn.sendsms.Service;
import cn.sendsms.TimeoutException;
import cn.sendsms.AGateway.Protocols;
import cn.sendsms.InboundMessage.MessageClasses;
import cn.sendsms.helper.CommPortIdentifier;
import cn.sendsms.helper.SerialPort;
import cn.sendsms.modem.SerialModemGateway;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;


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
public class JDMobileMsgUtil implements IMsgUtil {
    Logger logger = Logger.getLogger(JDMobileMsgUtil.class.getName());
    private Service srv;
    public JDMobileMsgUtil(){
    	srv = Service.getInstance();
		String comName = "";
		Config config = new Config();
		comName = config.getIsUsedProperty("comName");
		SerialModemGateway gateway = null ;
		
		//有时由于信号问题,可能会引起超时,运行时若出现No Response 请把这句注释打开
        System.setProperty("sendsms.nocops",new String());
		
		gateway = new SerialModemGateway("modelCat", comName ,115200, "Wavecom", null);

		// 设置短信编码格式，默认为 PDU (如果只发送英文，请设置为TEXT)。
		//gateway.setProtocol(Protocols.TEXT);

		// 设置通道gateway是否处理接受到的短信
		gateway.setInbound(true);

		// 设置是否可发送短信
		gateway.setOutbound(true);

		// SIM PIN.
		gateway.setSimPin("0000");
		//srv.stopService();
		// 添加Gateway到Service对象，如果有多个Gateway，都要一一添加。
		if (srv.getGateway("modelCat") == null){
			try {
				srv.addGateway(gateway);
			} catch (GatewayException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		// 启动服务 
		try {
			srv.startService();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GatewayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SendSMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
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
        //System.out.println(this.getClass().getName()+"调用的send（UserDb）方法");
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
    	Conn conn = new Conn(Global.defaultDB);
		List<InboundMessage> msgList;
		
		try{
			//定义msgList
			msgList = new ArrayList<InboundMessage>();
			
			//读取所有短信到msgList
			srv.readMessages(msgList, MessageClasses.ALL);
			
			PreparedStatement pstmt = null;
	    	String sql = "insert into sms_receive_record (mobile,content,mydate,dest_addr) values (?,?,?,?)";
			String mobileNumber = "";
			String msgText = "";
			java.util.Date arriveDate = null;
			for (InboundMessage msg : msgList){
				mobileNumber = msg.getOriginator();
				msgText = msg.getText();
				arriveDate = msg.getDate();
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, mobileNumber);
				pstmt.setString(2, msgText);
				pstmt.setTimestamp(3, new Timestamp(arriveDate.getTime()));
				pstmt.setString(4, "");
	    		
				pstmt.executeUpdate();
				//读取完短信删除
				srv.deleteMessage(msg);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
				//srv.stopService();
			} catch (Exception e) {
				e.printStackTrace();
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
        //System.out.println(this.getClass().getName()+"调用的send（mobile）方法");
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
        //System.out.println(exNumber);
        //int id = Integer.parseInt(StrUtil.PadString(ssrd.getUserName(),'0',4,true));
        return sendSMS(msg, mobile, "1", ""+id, "vip", exNumber);
    }

    public boolean sendSMS(String content, String mobilePhones, String priority,
                           String messageFlag, String moduleName,
                           String exNumber) {
    	
		OutboundMessage msg;
		int id = 0;
		String sendMobile = null;
		String msgText = null;
		try {
			// 发送短信
			String sql = "select id,SENDMOBILE,MSGTEXT from sms_send_record where is_sended = 0";
			String sql1 = "";
			JdbcTemplate rmconn = new JdbcTemplate();
			ResultIterator ri = rmconn.executeQuery(sql);
			ResultRecord rr = null;
			while (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				id = rr.getInt(1);
        		sendMobile = rr.getString(2);
        		msgText = rr.getString(3);
        		msg = new OutboundMessage(sendMobile, msgText);
    			msg.setEncoding(OutboundMessage.MessageEncodings.ENCUCS2);
    			msg.setStatusReport(true);

    			boolean flag = srv.sendMessage(msg);
    			
    			if(flag){
    				sql1 = "update sms_send_record set is_sended = 1 where id = "+id;
    				rmconn.executeUpdate(sql1);
    			}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				//srv.stopService();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
        //System.out.println(this.getClass().getName()+"调用的send（UserDb）方法");
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
        //System.out.println(this.getClass().getName()+"调用的send（mobile）方法");
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
