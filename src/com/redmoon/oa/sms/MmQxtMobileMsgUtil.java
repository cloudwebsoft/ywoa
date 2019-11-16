package com.redmoon.oa.sms;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Get;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;

import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class MmQxtMobileMsgUtil implements IMsgUtil{
	public static void main(String[] args) {
		System.out.print("aasd{&}asd".split("\\{\\&\\}"));
		/*
		try {
			String gbk = "iteye问答频道编码转换问题";  
        	String iso = new String(gbk.getBytes("UTF-8"),"ISO-8859-1");
	        System.out.println(iso);  
	        String utf8 = new String(iso.getBytes("ISO-8859-1"),"UTF-8");  
	        System.out.println(utf8);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String content = "测试";
		StringBuffer sb = new StringBuffer("http://api.duanxin.cm/?");
		//StringBuffer sb = new StringBuffer("http://api.duanxin.cm/?");
		sb.append("action=send&username=70202407");
		try {
			sb.append("&password="+SecurityUtil.MD5("18256051028"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append("&phone=13382792585");
		sb.append("&content="+URLEncoder.encode(content));
		sb.append("&encode=utf8");
		URL url;
		try {
			url = new URL(sb.toString());
		
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.setRequestMethod("post");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputline = in.readLine();
			System.out.println(inputline);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	public static String strReplace(String sBody, String sFrom, String sTo) {
		int i, j, k, l;
		if (sBody == null || sBody.equals(""))
			return "";
		if (sFrom == null || sFrom.equals(""))
			return sBody;
		i = 0;
		j = sFrom.length();
		k = sTo.length();
		StringBuffer sss = new StringBuffer(sBody.length());
		boolean bFirst = true;
		l = i;
		while (sBody.indexOf(sFrom, i) != -1) {
			i = sBody.indexOf(sFrom, i);
			sss.append(sBody.substring(l, i));
			sss.append(sTo);
			i += j;
			l = i;
		}
		sss.append(sBody.substring(l));
		return sss.toString();
	}

	/**
	 * Validate the string is empty or not
	 * 
	 * @param str
	 *            the String to be validated
	 * @return boolean not math return false else return true
	 */
	public synchronized static boolean isEmpty(String str) {

		return ((str == null) || (str.trim().equals(""))) ? true : false;
	}

	/**
	 * 用于通知等的发送
	 * 
	 * @param user
	 *            UserDb
	 * @param msgText
	 *            String
	 * @param sender
	 *            String
	 * @return boolean
	 * @throws ErrMsgException
	 */
	public boolean send(UserDb user, String msgText, String sender)
			throws ErrMsgException {
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
		// int id = ssrd.getId();
		// re = sendSMS(msgText, user.getMobile(), "1", ""+id, "vip", "9");
		// if (re) {
		// System.out.println(this.getClass().getName()+"调用的send（UserDb）方法");
		String[] contexts = divContext(msgText);
		for (int i = 0; i < contexts.length; i++) {
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
		com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
		String Signature = smscfg.getIsUsedProperty("Signature");
		String strURL = smscfg.getIsUsedProperty("qxtMasSendSmsUrl");;
		String userName = smscfg.getIsUsedProperty("user_name");;
		String pwd = smscfg.getIsUsedProperty("password");
		//String content = "duanxin.cm JAVA测试"; 
		StringBuffer sb = new StringBuffer(strURL);
		//StringBuffer sb = new StringBuffer("http://api.duanxin.cm/?");
		sb.append("action=getReply&username="+userName);
		try {
			sb.append("&password="+SecurityUtil.MD5(pwd));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//sb.append("&encode=utf8");
		URL url;
		try {
			System.out.println(getClass() + "url=" + sb.toString());
			url = new URL(sb.toString());
		
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			InputStream in = connection.getInputStream();
			InputStreamReader r = new InputStreamReader(in, "utf-8");
			LineNumberReader din = new LineNumberReader(r);
			String line = null;
			StringBuffer sbReceive = new StringBuffer();
			while ((line = din.readLine()) != null) {
				sbReceive.append(line + "\n");
			}
			//13912341234||企信通测试回复||2008-05-27 12:10:11||1068112227282
			SMSReceiveRecordDb srrdDb = new SMSReceiveRecordDb();
			String receiveStr= sbReceive.toString();
			if(receiveStr.indexOf("&") != -1){
				System.out.println(getClass()+ "receive info = " + receiveStr);
				String[] receiveResultAry = receiveStr.split("\\{\\&\\}");
				System.out.println(getClass() + "receiveResultAry size = " + receiveResultAry.length );
				if(receiveResultAry != null && receiveResultAry.length > 1){
					for(int i = 1; i < receiveResultAry.length; i++){
						System.out.println(getClass() + "receiveResultAry=" + receiveResultAry[i]);
						String[] receiveSmsAry = receiveResultAry[i].split("\\|\\|");
						System.out.println(getClass()+ "mobile info = " + receiveSmsAry[0]);
						System.out.println(getClass()+ "content info = " + receiveSmsAry[1]);
						System.out.println(getClass()+ "mydate info = " + receiveSmsAry[2]);
						System.out.println(getClass()+ "dest info = " + receiveSmsAry[3]);
						boolean re = srrdDb.create(new JdbcTemplate(), new Object[] {
							receiveSmsAry[0],
							receiveSmsAry[1],		
							receiveSmsAry[2],
							receiveSmsAry[3],
						});
						System.out.println(getClass()+ "re info = " + re);
						
					}	
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public boolean send(String mobile, String msgText, String sender)
			throws ErrMsgException {
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
		// int id = ssrd.getId();
		// re = sendSMS(msgText, mobile, "1", ""+id, "vip", "9");
		// System.out.println(this.getClass().getName()+"调用的send（mobile）方法");
		// 记录发送的短信
		String[] contexts = divContext(msgText);
		for (int i = 0; i < contexts.length; i++) {
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
		//String msg = ssrd.getMsgText() + " (" + realName + ")";
		String msg = ssrd.getMsgText();
		UserDb user = new UserDb(userName);

		String exNumber = StrUtil.PadString(user.getId() + "", '0', 4, true);
		// System.out.println(exNumber);
		// int id =
		// Integer.parseInt(StrUtil.PadString(ssrd.getUserName(),'0',4,true));
		return sendSMS(msg, mobile, "1", "" + id, "vip", exNumber);
	}
	
	public String getSmsCount(){
		com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
		String Signature = smscfg.getIsUsedProperty("Signature");
		String strURL = smscfg.getIsUsedProperty("qxtMasSendSmsUrl");;
		String userName = smscfg.getIsUsedProperty("user_name");;
		String pwd = smscfg.getIsUsedProperty("password");
		//String content = "duanxin.cm JAVA测试"; 
		StringBuffer sb = new StringBuffer(strURL);
		//StringBuffer sb = new StringBuffer("http://api.duanxin.cm/?");
		sb.append("action=getBalance&username="+userName);
		try {
			sb.append("&password="+SecurityUtil.MD5(pwd));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String inputline = "";
		URL url;
		try {
			url = new URL(sb.toString());
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.setRequestMethod("post");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			inputline = in.readLine();
			System.out.println(getClass()+ "sms send info = " +inputline);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inputline;
	}
	public boolean sendSMS(String content, String mobilePhones,
			String priority, String messageFlag, String moduleName,
			String exNumber) {
		com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
		String Signature = smscfg.getIsUsedProperty("Signature");
		String strURL = smscfg.getIsUsedProperty("qxtMasSendSmsUrl");;
		String userName = smscfg.getIsUsedProperty("user_name");;
		String pwd = smscfg.getIsUsedProperty("password");
		//String content = "duanxin.cm JAVA测试"; 
		StringBuffer sb = new StringBuffer(strURL);
		//StringBuffer sb = new StringBuffer("http://api.duanxin.cm/?");
		sb.append("action=send&username="+userName);
		try {
			sb.append("&password="+SecurityUtil.MD5(pwd));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append("&phone="+mobilePhones);
		sb.append("&content="+URLEncoder.encode(content));
		//sb.append("&encode=utf8");
		URL url;
		try {
			url = new URL(sb.toString());
		
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			//connection.setRequestMethod("post");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputline = in.readLine();
			System.out.println(getClass()+ "sms send info = " +inputline);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean send(UserDb user, String content, String sender,
			boolean isTiming, Date timeSend, long batch) throws ErrMsgException {
		if (!Config.isValidMobile(user.getMobile())) {
			LogUtil.getLog(getClass()).error(user.getRealName() + " 的手机号非法！");
			return false;
		}
		boolean re = false;
		SMSSendRecordDb ssrd = new SMSSendRecordDb();
		// int id = ssrd.getId();
		// re = sendSMS(msgText, user.getMobile(), "1", ""+id, "vip", "9");
		// if (re){
		// System.out.println(this.getClass().getName()+"调用的send（UserDb）方法");
		String[] contexts = divContext(content);
		for (int i = 0; i < contexts.length; i++) {
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
			boolean isTiming, Date timeSend, long batch) throws ErrMsgException {
		if (!Config.isValidMobile(mobile)) {
			return false;
		}
		boolean re = false;
		SMSSendRecordDb ssrd = new SMSSendRecordDb();
		// int id = ssrd.getId();
		// re = sendSMS(msgText, mobile, "1", ""+id, "vip", "9");
		// System.out.println(this.getClass().getName()+"调用的send（mobile）方法");
		// 记录发送的短信
		String[] contexts = divContext(content);
		for (int i = 0; i < contexts.length; i++) {
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

	public String[] divContext(String context) {
		if (false) {// 如果不按70个自动切分短信
			return new String[] { context };
		}

		String[] contexts = null;
		if (context == null || context.length() < Config.CONTEXT_DIV) {
			contexts = new String[] { context };
		} else {
			Vector v = new Vector();
			String temp = context;
			String item = "";
			while (temp.length() > Config.CONTEXT_DIV) {
				item = temp.substring(0, Config.CONTEXT_DIV);
				temp = temp.substring(Config.CONTEXT_DIV);
				v.add(item);
			}
			v.add(temp);
			int length = v.size();
			contexts = new String[length];
			Iterator ir = v.iterator();
			int i = 0;
			while (ir.hasNext()) {
				contexts[i] = (String) ir.next();
				i++;
			}
		}
		return contexts;
	}

    public int sendBatch(String[] users, String content, String sender) throws ErrMsgException {
    	return 0;
    }


}
