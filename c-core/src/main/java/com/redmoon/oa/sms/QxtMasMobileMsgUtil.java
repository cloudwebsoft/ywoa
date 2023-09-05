package com.redmoon.oa.sms;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;

public class QxtMasMobileMsgUtil implements IMsgUtil {
	public static void main(String[] args) {
		HashMap map = new HashMap();
		try {
			map.put("method", "sendSMS");
			map.put("username", "yimihome");
			map.put("password", BASE64Encoder("cloudweb123"));
			map.put("smstype", 2);
			map.put("mobile", "15052936215");
			map.put("content", StrUtil.UrlEncode("陈玥测试", "GBK"));
			map.put("extenno", "0001");
			String result = sendUrlRequest("http://sms.exintong.net:9038/servlet/UserServiceAPI?",map, "get", "");
			String[] smsBoundary = result.split(";");
			String remainingSms = "";
			if(smsBoundary != null){
				remainingSms = smsBoundary[0];
			}
		} catch (Exception e) {
			LogUtil.getLog(QxtMasMobileMsgUtil.class).error(e);
		}
		/*
		 * QxtMasMobileMsgUtil quMasMobileMsgUtil = new QxtMasMobileMsgUtil();
		 * String result = ""; try { UserApi userApi = new UserApi(); result =
		 * userApi.getUserApiHttpSoap11Endpoint().sendSms("oa",
		 * BASE64Encoder("123456"), 0, "张", "15052936215", 0, "");
		 * 
		 * } catch (Exception e) { // TODO Auto-generated catch block
		 * LogUtil.getLog(getClass()).error(e); }
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
	 * request a url
	 * 
	 * @param urlAddr
	 * @param map
	 * @param method
	 * @return
	 * @throws Exception
	 */
	public static String sendUrlRequest(String urlAddr, Map map, String method,
			String character) throws Exception {
		boolean isSuccess = false;
		StringBuffer params = new StringBuffer();
		HttpURLConnection conn = null;
		if (map != null) {
			Iterator it = map.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry element = (Map.Entry) it.next();

				params.append(element.getKey());
				params.append("=");
				params.append(strReplace(element.getValue().toString(),
						"{islongsms}", ""));
				params.append("&");

			}
		}
		if (params.length() > 0) {
			params.deleteCharAt(params.length() - 1);
		}

		try {
			character = isEmpty(character) ? "GBK" : character;
			String u = urlAddr
					+ (method.equalsIgnoreCase("GET") ? params.toString() : "");

			URL url = new URL(u);

			conn = (HttpURLConnection) url.openConnection();
			if (!method.equalsIgnoreCase("GET")) {
				conn.setDoOutput(true);
				conn.setRequestMethod(method);
				conn.setUseCaches(false);
				conn.setRequestProperty("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");

				conn.setRequestProperty("Content-type",
						"application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Language", "" + character);
				conn.setConnectTimeout(10000);
				conn.setRequestProperty("Accept-Charset", "" + character);
				// conn.setRequestProperty("Connection", "Close");
				conn.setRequestProperty("Content-length", String.valueOf(params
						.length()));
				conn.setDoInput(true);
				conn.connect();
			}
			if (!method.equalsIgnoreCase("GET")) {
				OutputStreamWriter out = new OutputStreamWriter(conn
						.getOutputStream(), character);
				out.write(new String(params.toString().getBytes("GBK"),
						character));
				out.flush();
				out.close();
			}
			InputStream in = conn.getInputStream();
			InputStreamReader r = new InputStreamReader(in, character);
			LineNumberReader din = new LineNumberReader(r);
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = din.readLine()) != null) {
				sb.append(line + "\n");
			}
			return sb.toString();
		} catch (Exception ex) {
			LogUtil.getLog(QxtMasMobileMsgUtil.class).error(ex);
		} finally {
			conn.disconnect();
		}
		return "";
	}

	public synchronized static String BASE64Encoder(String str)
			throws Exception {
		return new sun.misc.BASE64Encoder().encode(str.getBytes());
	}

	public synchronized void increaseTailAddr() {
	}

	public String getOrgAddr() {
		return "";
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
		String strURL = smscfg.getIsUsedProperty("qxtMasSendSmsUrl");
		String userName = smscfg.getIsUsedProperty("user_name");
		String pwd = smscfg.getIsUsedProperty("password");
		// urlencode 处理短信参数中的空格
		HashMap map = new HashMap();
		try {
			map.put("method", "getRecvSMS");
			map.put("username", userName);
			map.put("password", BASE64Encoder(pwd));
			String receveXml = sendUrlRequest(strURL, map, "get", "");
			LogUtil.getLog(getClass()).info("receive======" + receveXml);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}	
		return 0;
	}
	public String getSmsCount(){
		com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
		String strURL = smscfg.getIsUsedProperty("qxtMasSendSmsUrl");
		String userName = smscfg.getIsUsedProperty("user_name");
		String pwd = smscfg.getIsUsedProperty("password");
		// urlencode 处理短信参数中的空格
		HashMap map = new HashMap();
		String receveXml = "";
		try {
			map.put("method", "getRestMoney");
			map.put("username", userName);
			map.put("password", BASE64Encoder(pwd));
			receveXml = sendUrlRequest(strURL, map, "get", "");
			LogUtil.getLog(getClass()).info("receive======" + receveXml);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}	
		return receveXml;
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
		// int id =
		// Integer.parseInt(StrUtil.PadString(ssrd.getUserName(),'0',4,true));
		return sendSMS(msg, mobile, "1", "" + id, "vip", exNumber);
	}

	public boolean sendSMS(String content, String mobilePhones,
			String priority, String messageFlag, String moduleName,
			String exNumber) {
		com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
		String Signature = smscfg.getIsUsedProperty("Signature");
		String strURL = smscfg.getIsUsedProperty("qxtMasSendSmsUrl");;
		String userName = smscfg.getIsUsedProperty("user_name");;
		String pwd = smscfg.getIsUsedProperty("password");
		// urlencode 处理短信参数中的空格
		HashMap map = new HashMap();
		try {
			map.put("method", "sendSMS");
			map.put("username", userName);
			map.put("password", BASE64Encoder(pwd));
			map.put("smstype", 2);
			map.put("mobile", mobilePhones);
			map.put("content", StrUtil.UrlEncode(Signature + "" + content, "GB2312"));
			String result = sendUrlRequest(strURL, map, "get", "");
			LogUtil.getLog(getClass()).info("smsSend======" + result);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
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
		// LogUtil.getLog(getClass()).info(this.getClass().getName()+"调用的send（mobile）方法");
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
