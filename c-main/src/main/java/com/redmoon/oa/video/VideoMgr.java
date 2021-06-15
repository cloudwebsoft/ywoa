package com.redmoon.oa.video;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.ui.LocalUtil;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResBundle;
import cn.js.fan.util.StrUtil;



/**
 * 视频会议操作类
 * @author mk
 *
 */
public class VideoMgr {
    private Logger logger = Logger.getLogger(VideoMgr.class.getName());
	private String siteName = "";                        //站点主机名：yimihome
	private  String adminName = "admin";                     //管理员用户名
	private  String adminPass = "";                    //管理员密码
	private String url = "";
	private String siteFullName = "";                    //站点全名：yimihome.infowarelab.cn
	private String isUse = "";
	private String sequence = "";
	private Config cfg = new Config();
	public VideoMgr(){
		siteFullName = cfg.get("siteName");
		url = "http://"+siteFullName+"/integration/xml";
		int num = siteFullName.indexOf(".");
		if (num==-1)
			num = 0;
		siteName = siteFullName.substring(0, num);
		sequence = cfg.get("sequence");
		isUse = cfg.get("isUse");
	}
	/**
	 * 获取管理员密码
	 */
	public void getAdminPassword(){
		UserDb uDb = new UserDb();
		uDb = uDb.getUserDb(adminName);
		adminPass = uDb.getPwdRaw();
	}
	/**
	 * 建立连接，发送接收xml
	 * @param urlAddr
	 * @param sendData
	 * @return
	 * @throws Exception
	 */
	public   String send(String urlAddr, String sendData){    
    	HttpURLConnection conn = null;     
    	OutputStreamWriter out = null;
    	BufferedReader br = null ;
        StringBuffer sb=new StringBuffer("");
        StringBuffer params = new StringBuffer();    
        params.append(sendData);
        try{    
        	URL url = new URL(urlAddr);    
            conn = (HttpURLConnection)url.openConnection();    
            conn.setDoOutput(true);    
            conn.setRequestMethod("POST");    
		    conn.setUseCaches(false);    
		    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");    
		    conn.setRequestProperty("Content-Length", String.valueOf(params.length()));    
		    conn.setDoInput(true);    
		    conn.setReadTimeout(30000);
		    conn.setConnectTimeout(30000);              //设置链接超时时间为30秒
		    conn.connect();    
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");    
            out.write(params.toString());    
            out.flush();    
            int code = conn.getResponseCode();
            if (code != 200) {    
            	logger.error(getClass().getName()+code);
            } else {    
            br=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
 		   	String s="";
 		   	while((s=br.readLine())!=null)
 		   	{     
 		   		sb.append(s+"\r\n");    
 		   	}
        }    
    }catch(Exception ex){   
    	logger.error("SendPostMessage is error"+ex.getMessage());
    }finally{   
    	if (conn!=null)
    		conn.disconnect();    
    	if(out!=null){
    		try {
    			out.close();
			} catch (Exception e) {
				logger.error(getClass().getName()+"关闭流失败！");
			}
    	}
		if(br!=null){
		    try {
				br.close();
			} catch (Exception e) {
				logger.error(getClass().getName()+"关闭流失败！");
			}		
		}
    }    
    return sb.toString();  
    } 
	/**
	 * 创建用户
	 * @param userName
	 * @param password
	 * @return
	 */
	public  String  createUser(String userName,String password){
		getAdminPassword();
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>createCemUser</action>");
		strXML.append("<password>"+adminPass+"</password>");
		strXML.append("<service>siteadmin</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+adminName+"</userName>");
		strXML.append("<version>30</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<roleIds><roleId>"+sequence+"</roleId></roleIds>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<nickname>"+userName+"</nickname>");
		strXML.append("<password>"+password+"</password>");
		strXML.append("<firstName>"+userName+"</firstName>");
		strXML.append("<gender>0</gender>");
		strXML.append("<deptId>1</deptId>");
		strXML.append("<userType>1</userType>");
		strXML.append("<enabled>true</enabled>");
		strXML.append("<forceCreate>true</forceCreate>");
		strXML.append("<forceUpdate>true</forceUpdate>");
		strXML.append("<email></email>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 根据传入的用户数组，批量添加用户。
	 * @param info
	 * @return List    包含添加成功的用户集和添加失败的用户集
	 */
	public   List  createUserByArr(String[][] info){
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> successList = new ArrayList<String>();
		List<String> failureList = new ArrayList<String>();
		if (info != null){
			for (int i=0;i<info.length;i++){
				String userName = info[i][1];           //获取用户名
				String password = info[i][2];           //获取密码
				if (!"".equals(userName)&&userName!=null){
					String responseXML = createUser(userName, password);
					boolean isSuccess = getResultByParseXML(responseXML);
					if (isSuccess){
						successList.add(userName);
					} else {
						failureList.add(userName);
					}
				}
			}
		}
		list.add(successList);
		list.add(failureList);
		return list;
	} 
	/**
	 * 编辑用户
	 * @param userName
	 * @param password
	 * @return
	 */
	public  String updateUser(String userName,String password){
		getAdminPassword();
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>updateCemUser</action>");
		strXML.append("<password>"+adminPass+"</password>");
		strXML.append("<service>siteadmin</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+adminName+"</userName>");
		strXML.append("<version>30</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<roleIds><roleId>"+sequence+"</roleId></roleIds>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<nickname>"+userName+"</nickname>");
		strXML.append("<password>"+password+"</password>");
		strXML.append("<firstName>"+userName+"</firstName>");
		strXML.append("<gender>0</gender>");
		strXML.append("<deptId>1</deptId>");
		strXML.append("<userType>1</userType>");
		strXML.append("<enabled>true</enabled>");
		strXML.append("<forceCreate>true</forceCreate>");
		strXML.append("<forceUpdate>true</forceUpdate>");
		strXML.append("<email></email>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 删除用户
	 * @param userName
	 * @return
	 */
	public  String delUser(String userName){
		getAdminPassword();
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>delCemUser</action>");
		strXML.append("<password>"+adminPass+"</password>");
		strXML.append("<service>siteadmin</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+adminName+"</userName>");
		strXML.append("<version>30</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<parameter>");
		strXML.append("<type>1</type>");
		strXML.append("<value>"+userName+"</value>");
		strXML.append("</parameter>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 修改用户密码
	 * @param userName
	 * @param newPassword
	 * @return
	 */
	public  String modifyUserPassword(String userName,String newPassword){
		getAdminPassword();
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>resetUserPassword</action>");
		strXML.append("<password>"+adminPass+"</password>");
		strXML.append("<service>siteadmin</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+adminName+"</userName>");
		strXML.append("<version>30</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<parameter>");
		strXML.append("<type>1</type>");
		strXML.append("<value>"+userName+"</value>");
		strXML.append("</parameter>");
		strXML.append("<password>"+newPassword+"</password>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 解析response的xml字符串，获取result结果
	 * @param result
	 * @return
	 */
	public  boolean getResultByParseXML(String result){
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element header = root.getChild("header");
			String reason = header.getChildText("reason");
			String returnStr = header.getChildText("result");
			if ("SUCCESS".equals(returnStr)){
				return true;
			} else {
				logger.error(getClass().getName()+reason);
				return false;
			}
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return false;
	}
	/**
	 * 解析response的xml字符串，获取reson结果 
	 * @param result
	 * @return
	 */
	public  String getReasonByParseXML(String result){
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element header = root.getChild("header");
			String reason = header.getChildText("reason");
			String returnStr = header.getChildText("result");
			if ("SUCCESS".equals(returnStr)){
				return "操作成功!";
			} else  {
				return reason;
			}
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return "操作失败!";
	}
	/**
	 * 获取异常信息
	 * @param result
	 * @return
	 */
	public  String getExceptionByParseXML(String result){
		String exception = "";
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element header = root.getChild("header");
			String exceptionID = header.getChildText("exceptionID");
			if ("0x0000002".equals(exceptionID)||"0x0000001".equals(exceptionID)){                                    //站点无效，将视频会议改为不启用
	 			if ("true".equals(isUse))
	 				cfg.put("isUse","false");
	 		}
			String exceptionInfo = LocalUtil.LoadString(null, "res.com.redmoon.oa.video.video_meeting",exceptionID);
			if ("".equals(exceptionInfo)){
	 			exception = exceptionID;
	 		} else {
	 			exception = exceptionInfo;
	 		}
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return exception;
	}
	/**
	 * 校验合法性
	 * @return
	 */
	public  boolean validate(){
		int num = siteFullName.indexOf(".");
		String suffix = "";
		boolean isSuccess = false;
		if (num!=-1)
			suffix = siteFullName.substring(num);
		if ("true".equals(isUse)&&".infowarelab.cn".equals(suffix))
			isSuccess = true;
		return isSuccess;
	}
	/**
	 * 根据传入的用户名数组，批量删除视频会议用户
	 * @param userNameArr
	 */
	public void delUserByArr(String[] userNameArr){
		if (userNameArr!=null){
			for(int i=0;i<userNameArr.length;i++){
				String returnString = delUser(userNameArr[i]);
				boolean isSuccess = getResultByParseXML(returnString);
				if (isSuccess)
					logger.info("删除视频会议用户："+userNameArr[i]+"成功");
				else 
					logger.error("删除视频会议用户："+userNameArr[i]+"失败");
			}
		}
	}
	/**
	 * 创建预约会议
	 * @param userName
	 * @param subject
	 * @param passwd
	 * @param startTime
	 * @param endTime
	 * @param conferencePattern
	 * @param attendeeAmount
	 * @param agenda
	 * @return
	 */
	public String createBespeakMeeting(String userName,String subject,String passwd,String startTime,String endTime,String conferencePattern,int attendeeAmount,String agenda){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>createReserveMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<version>50</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<subject>"+subject+"</subject>");
		strXML.append("<startTime>"+startTime+"</startTime>");
		strXML.append("<endTime>"+endTime+"</endTime>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("<attendeeAmount>"+attendeeAmount+"</attendeeAmount>");
		strXML.append("<hostName>"+userName+"</hostName>");
		strXML.append("<creator>"+userName+"</creator>");
		strXML.append("<openType>true</openType> ");
		strXML.append("<passwd>"+passwd+"</passwd> ");
		strXML.append("<conferencePattern>"+conferencePattern+"</conferencePattern> ");
		strXML.append("<agenda>"+agenda+"</agenda>");
		strXML.append("<mailTemplateLocal>zh_CN</mailTemplateLocal>");
		strXML.append("<beforehandTime>15</beforehandTime>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("<attendees></attendees>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 更新预约会议
	 * @param confKey
	 * @param userName
	 * @param subject
	 * @param passwd
	 * @param startTime
	 * @param endTime
	 * @param conferencePattern
	 * @param attendeeAmount
	 * @param agenda
	 * @return
	 */
	public String updateBespeakMeeting(String confKey,String userName,String subject,String passwd,String startTime,String endTime,String conferencePattern,int attendeeAmount,String agenda){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>updateReserveMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<version>50</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<confKey>"+confKey+"</confKey>");
		strXML.append("<subject>"+subject+"</subject>");
		strXML.append("<startTime>"+startTime+"</startTime>");
		strXML.append("<endTime>"+endTime+"</endTime>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("<attendeeAmount>"+attendeeAmount+"</attendeeAmount>");
		strXML.append("<hostName>"+userName+"</hostName>");
		strXML.append("<creator>"+userName+"</creator>");
		strXML.append("<openType>true</openType> ");
		strXML.append("<passwd>"+passwd+"</passwd> ");
		strXML.append("<conferencePattern>"+conferencePattern+"</conferencePattern> ");
		strXML.append("<agenda>"+agenda+"</agenda>");
		strXML.append("<mailTemplateLocal>zh_CN</mailTemplateLocal>");
		strXML.append("<beforehandTime>15</beforehandTime>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("<attendees></attendees>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 创建固定会议
	 * @param userName
	 * @param subject
	 * @param passwd
	 * @param startTime
	 * @param endTime
	 * @param conferencePattern
	 * @param attendeeAmount
	 * @param agenda
	 * @return
	 */
	public String createFixedMeeting(String userName,String subject,String passwd,String startTime,String endTime,String conferencePattern,int attendeeAmount,String agenda){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>createFixedMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<version>50</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<subject>"+subject+"</subject>");
		strXML.append("<startTime>"+startTime+"</startTime>");
		strXML.append("<endTime>"+endTime+"</endTime>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("<attendeeAmount>"+attendeeAmount+"</attendeeAmount>");
		strXML.append("<hostName>"+userName+"</hostName>");
		strXML.append("<creator>"+userName+"</creator>");
		strXML.append("<openType>true</openType> ");
		strXML.append("<passwd>"+passwd+"</passwd> ");
		strXML.append("<conferencePattern>"+conferencePattern+"</conferencePattern> ");
		strXML.append("<agenda>"+agenda+"</agenda>");
		strXML.append("<mailTemplateLocal>zh_CN</mailTemplateLocal>");
		strXML.append("<beforehandTime>15</beforehandTime>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("<attendees></attendees>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 更新固定会议
	 * @param confKey
	 * @param userName
	 * @param subject
	 * @param passwd
	 * @param startTime
	 * @param endTime
	 * @param conferencePattern
	 * @param attendeeAmount
	 * @param agenda
	 * @return
	 */
	public String updateFixedMeeting(String confKey,String userName,String subject,String passwd,String startTime,String endTime,String conferencePattern,int attendeeAmount,String agenda){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>updateFixedMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<version>50</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<confKey>"+confKey+"</confKey>");
		strXML.append("<subject>"+subject+"</subject>");
		strXML.append("<startTime>"+startTime+"</startTime>");
		strXML.append("<endTime>"+endTime+"</endTime>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("<attendeeAmount>"+attendeeAmount+"</attendeeAmount>");
		strXML.append("<hostName>"+userName+"</hostName>");
		strXML.append("<creator>"+userName+"</creator>");
		strXML.append("<openType>true</openType> ");
		strXML.append("<passwd>"+passwd+"</passwd> ");
		strXML.append("<conferencePattern>"+conferencePattern+"</conferencePattern> ");
		strXML.append("<agenda>"+agenda+"</agenda>");
		strXML.append("<mailTemplateLocal>zh_CN</mailTemplateLocal>");
		strXML.append("<beforehandTime>15</beforehandTime>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("<attendees></attendees>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 创建周期会议
	 * @param userName
	 * @param subject
	 * @param passwd
	 * @param startMinute
	 * @param endMinute
	 * @param startTime
	 * @param endTime
	 * @param repeatTypeKey
	 * @param repeatTypeValue
	 * @param conferencePattern
	 * @param attendeeAmount
	 * @param agenda
	 * @return
	 */
	public String createRegularMeeting(String userName,String subject,String passwd,String startMinute,String endMinute,String startTime,String endTime,String repeatTypeKey,String repeatTypeValue,String conferencePattern,int attendeeAmount,String agenda){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>createRegularMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<version>50</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<subject>"+subject+"</subject>");
		strXML.append("<startHourMinute>"+startMinute+"</startHourMinute>");
		strXML.append("<endHourMinute>"+endMinute+"</endHourMinute>");
		strXML.append("<effectiveTime>"+startTime+"T16:00:00"+"</effectiveTime>");    //周期会议只传重复日期、时间固定为T16:00:00
		strXML.append("<expirationTime>"+endTime+"T16:00:00"+"</expirationTime>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("<attendeeAmount>"+attendeeAmount+"</attendeeAmount>");
		strXML.append("<hostName>"+userName+"</hostName>");
		strXML.append("<creator>"+userName+"</creator>");
		strXML.append("<repeatTypeKey>"+repeatTypeKey+"</repeatTypeKey>");
		strXML.append("<repeatTypeValue>"+repeatTypeValue+"</repeatTypeValue>");
		strXML.append("<openType>true</openType> ");
		strXML.append("<passwd>"+passwd+"</passwd> ");
		strXML.append("<conferencePattern>"+conferencePattern+"</conferencePattern> ");
		strXML.append("<agenda>"+agenda+"</agenda>");
		strXML.append("<mailTemplateLocal>zh_CN</mailTemplateLocal>");
		strXML.append("<beforehandTime>15</beforehandTime>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("<attendees></attendees>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 更新周期会议
	 * @param confKey
	 * @param userName
	 * @param subject
	 * @param passwd
	 * @param startMinute
	 * @param endMinute
	 * @param startTime
	 * @param endTime
	 * @param repeatTypeKey
	 * @param repeatTypeValue
	 * @param conferencePattern
	 * @param attendeeAmount
	 * @param agenda
	 * @return
	 */
	public String updateRegularMeeting(String confKey,String userName,String subject,String passwd,String startMinute,String endMinute,String startTime,String endTime,String repeatTypeKey,String repeatTypeValue,String conferencePattern,int attendeeAmount,String agenda){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>updateRegularMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("<version>50</version>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<confKey>"+confKey+"</confKey>");
		strXML.append("<subject>"+subject+"</subject>");
		strXML.append("<startHourMinute>"+startMinute+"</startHourMinute>");
		strXML.append("<endHourMinute>"+endMinute+"</endHourMinute>");
		strXML.append("<effectiveTime>"+startTime+"T16:00:00"+"</effectiveTime>");     //周期会议只传重复日期、时间固定为T16:00:00
		strXML.append("<expirationTime>"+endTime+"T16:00:00"+"</expirationTime>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("<attendeeAmount>"+attendeeAmount+"</attendeeAmount>");
		strXML.append("<hostName>"+userName+"</hostName>");
		strXML.append("<creator>"+userName+"</creator>");
		strXML.append("<repeatTypeKey>"+repeatTypeKey+"</repeatTypeKey>");
		strXML.append("<repeatTypeValue>"+repeatTypeValue+"</repeatTypeValue>");
		strXML.append("<openType>true</openType> ");
		strXML.append("<passwd>"+passwd+"</passwd> ");
		strXML.append("<conferencePattern>"+conferencePattern+"</conferencePattern> ");
		strXML.append("<agenda>"+agenda+"</agenda>");
		strXML.append("<mailTemplateLocal>zh_CN</mailTemplateLocal>");
		strXML.append("<beforehandTime>15</beforehandTime>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("<attendees></attendees>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 根据用户名获取用户密码
	 * @param name
	 * @return
	 */
	public String getPasswordByName(String name){
		UserDb user = new UserDb();
		user = user.getUserDb(name);
		String password = user.getPwdRaw();
		return password;
	}
	/**
	 * 获取GMT时间（格林尼治标准时间）          GMT是中央时区,北京在东8区,相差8个小时 　　所以北京时间=GMT时间+八小时
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public   String getGMTTime(String dateTime){
		Date date = DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss");
		date = DateUtil.addHourDate(date, -8);
		String gmtTime  = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
		return gmtTime.replace(" ", "T");
	}
	/**
	 * 将gmt时间转换成北京时间    gmt+8
	 * @param dateTime
	 * @return
	 */
	public String getBJTimeByGMT(String dateTime){
		String dateTimeStr  = dateTime.replace("T", " ");
		Date date = DateUtil.parse(dateTimeStr,"yyyy-MM-dd HH:mm:ss");
		date = DateUtil.addHourDate(date, 8);
		return DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 查询会议列表，返回response xml
	 * @param userName
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public String getMeetingList(String userName,String startTime,String endTime){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>listSummaryMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<startDateStart>"+startTime+"</startDateStart>");
		strXML.append("<startDateEnd>"+endTime+"</startDateEnd>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	
	/**
	 * 查询历史会议
	 * @param userName
	 * @param startTime
	 * @param endTime
	 * @param startNum
	 * @param maxNum
	 * @return
	 */
	public String getHistoryMeetingList(String userName,String startTime,String endTime,int startNum,int maxNum){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>listHistoryMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<startFrom>"+startNum+"</startFrom>");
		strXML.append("<maximumNum>"+maxNum+"</maximumNum>");
		strXML.append("<openType>true</openType>");
		strXML.append("<startTime>"+startTime+"T01:00:00"+"</startTime>");
		strXML.append("<endTime>"+endTime+"T15:00:00"+"</endTime>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 获取分页信息     格式：startFrom+","+totalNum+","+returnNum;
	 * startFrom 起始数据    totalNum  总共多少条     返回数据条数
	 * @param result
	 * @return
	 */
	public String getPageInfo(String result){
		String pageInfo = "";
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element body = root.getChild("body");
			if (body==null)
				return "";
			String startFrom = body.getChildText("startFrom");
			String totalNum = body.getChildText("totalNum");
			String returnNum = body.getChildText("returnNum");
			pageInfo = startFrom+","+totalNum+","+returnNum;
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return pageInfo;
	}
	/**
	 * 获取历史会议集合
	 * @param result
	 * @return
	 */
	public List parseHistoryMeetingList(String result){
		List meetingList = new ArrayList();
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element body = root.getChild("body");
			Element meetings = null;
			if (body!=null)
				 meetings = body.getChild("historyMeetings");
			if (meetings!=null)
				meetingList =  meetings.getChildren("historyMeeting");
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return meetingList;
	}
	/**
	 * 查询我安排的会议列表
	 * @param userName
	 * @return
	 */
	public String getMineMeetingList(String userName){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>listSummaryPrivateMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<timeZoneId>45</timeZoneId>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 根据response返回的xml  查询会议列表
	 * @param result
	 * @return
	 */
	public List parseMeetingList(String result){
		List list = new ArrayList();
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element header = root.getChild("header");
			String returnStr = header.getChildText("result");
			if ("SUCCESS".equals(returnStr)){
				Element  body = root.getChild("body");
				Element meetings = null;
				if (body!=null)
					 meetings = body.getChild("meetings");
				if (meetings!=null){
					List meetingsList =  meetings.getChildren("meeting");
					if (meetingsList!=null){
						Iterator it = meetingsList.iterator();
						while (it!=null&&it.hasNext()) {
							list.add(it.next());
						}
					}
				}
			} else {
				logger.error(getClass().getName()+":"+"获取会议列表失败");
			}
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return list;
	}
	/**
	 * 主持人获取会议启动信息
	 * @param userName
	 * @param hostName
	 * @param confKey
	 * @return
	 */
	public String getMeetingStartInfo(String userName,String hostName,String confKey,String meetingPasswd ){
		String userPassword = getPasswordByName(userName);
		String realName = getRealNameByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>startMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<hostName>"+hostName+"</hostName >");
		strXML.append("<displayName>"+realName+"</displayName>");
		strXML.append("<confKey>"+confKey+"</confKey>");
		strXML.append("<meetingPwd>"+meetingPasswd+"</meetingPwd>");        
		strXML.append("<email></email>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}

	/**
	 * 获取会议加入信息
	 * @param userName
	 * @param confKey
	 * @return
	 */
	public String getMeetingJoinInfo(String userName,String confKey,String meetingPasswd){
		String userPassword = getPasswordByName(userName);
		String realName = getRealNameByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>joinMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<attendeeName>"+realName+"</attendeeName >");
		strXML.append("<confKey>"+confKey+"</confKey>");
		strXML.append("<meetingPwd>"+meetingPasswd+"</meetingPwd>");        //会议密码固定123456
		strXML.append("<email></email>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}

	
	/**
	 * 根据用户名获取用户的真实名称
	 * @param name
	 * @return
	 */
	public String getRealNameByName(String name){
		UserDb user = new UserDb();
		user = user.getUserDb(name);
		String realName = user.getRealName();
		return realName;
	}
	/**
	 * 获取CiUrl和Token          格式：ciURL+","+token    
	 * @param result
	 * @return
	 */
	public String getCiUrlAndToken(String result){
		String info = "";
		SAXBuilder sb = new SAXBuilder();
		StringReader xml = new StringReader(result);
		try {
			Document doc = sb.build(xml);
			Element root = doc.getRootElement();
			Element header = root.getChild("header");
			String returnStr = header.getChildText("result");
			if("SUCCESS".equals(returnStr)){
				Element body = root.getChild("body");
				if (body!=null){
					String ciURL = body.getChildText("ciURL");
					String token = body.getChildText("token");
					info = ciURL+","+token;
				}
			}
		} catch (JDOMException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error(getClass().getName()+":"+e.getMessage());
		}
		return info;
	}
	/**
	 * 删除会议
	 * @param userName
	 * @param confKey
	 * @return
	 */
	public String delMeeting(String userName,String confKey){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>deleteMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<confKey>"+confKey+"</confKey>");
		strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	/**
	 * 根据会议号,获取会议的详细信息
	 * @param userName
	 * @param confKey
	 * @return
	 */
	public String getMeetingDetail(String userName,String confKey){
		String userPassword = getPasswordByName(userName);
		String result = "";
		StringBuilder strXML = new StringBuilder();
		strXML.append("<?xml version='1.0' encoding='UTF-8'  ?>");
		strXML.append("<Message>");
		strXML.append("<header>");
		strXML.append("<action>readMeeting</action>");
		strXML.append("<password>"+userPassword+"</password>");
		strXML.append("<service>meeting</service>");
		strXML.append("<siteName>"+siteName+"</siteName>");
		strXML.append("<type>XML</type>");
		strXML.append("<userName>"+userName+"</userName>");
		strXML.append("</header>");
		strXML.append("<body>");
		strXML.append("<confKey>"+confKey+"</confKey>");
		// strXML.append("<webBaseUrl>http://"+siteFullName+"</webBaseUrl>");
		strXML.append("</body>");
		strXML.append("</Message>");
		result = send(url, strXML.toString());
		return result;
	}
	public Logger getLogger() {
		return logger;
	}
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	public String getAdminPass() {
		return adminPass;
	}
	public void setAdminPass(String adminPass) {
		this.adminPass = adminPass;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSiteFullName() {
		return siteFullName;
	}
	public void setSiteFullName(String siteFullName) {
		this.siteFullName = siteFullName;
	}
	public String getIsUse() {
		return isUse;
	}
	public void setIsUse(String isUse) {
		this.isUse = isUse;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public Config getCfg() {
		return cfg;
	}
	public void setCfg(Config cfg) {
		this.cfg = cfg;
	}
}
