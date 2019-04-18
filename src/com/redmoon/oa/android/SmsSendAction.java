package com.redmoon.oa.android;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.sms.SMSSendRecordDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class SmsSendAction {
	private String skey = "";
	private String result = "";
	private String mobile = "";
	private String content = "";
	private String isTimeSend = "";
	private String timeSend = "";
	public String getSkey() {
		return skey;
	}
	public void setSkey(String skey) {
		this.skey = skey;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getIsTimeSend() {
		return isTimeSend;
	}
	public void setIsTimeSend(String isTimeSend) {
		this.isTimeSend = isTimeSend;
	}
	public String getTimeSend() {
		return timeSend;
	}
	public void setTimeSend(String timeSend) {
		this.timeSend = timeSend;
	}
	
	public String execute() {
		JSONObject json = new JSONObject(); 
		
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		if(re){
			try {
				json.put("res","-2");
				json.put("msg","时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		com.redmoon.oa.sms.Config cfg = new com.redmoon.oa.sms.Config();
		/*
		String unitCode = privilege.getUserUnitCode(getSkey());
		try {
			int remain = cfg.canUnitSendSMS(unitCode);		
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			try {
				json.put("res","-1");
				json.put("msg",e.getMessage());
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		HttpServletRequest request = ServletActionContext.getRequest();
		try {
			SMSFilterMgr.filter(request, content);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			try {
				json.put("res","-1");
				json.put("msg",e.getMessage());
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		*/
		
		try {
			int realSendUserCount = 0;
			IMsgUtil imu = SMSFactory.getMsgUtil();
			String name = privilege.getUserName(getSkey());
			// long batch = SMSSendRecordDb.getBatchCanUse();
			// long batch = SMSSendBatchDb.getBatchCanUse(unitCode, name);
			String[] ary = StrUtil.split(getMobile(), ",");		
						
			int length = ary.length;//发送短信手机号码数
			if (ary.length>0) {			
				 for (int i=0; i<length; i++) {
					 try {
						boolean sendRet = imu.send(ary[i].trim(), content, name);
						if (sendRet) {
							realSendUserCount ++;							
						}
					 } catch (ErrMsgException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				 }
			// String smsSign = cfg.getSign(unitCode);
			int count = cfg.getDivNumber(content.length());
			int realSendCount = realSendUserCount * count;	 
			json.put("res","0");
			json.put("msg","发送完毕，本次共发送短信"+realSendCount+"条"); 
		  }			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		setResult(json.toString());
		return "SUCCESS";
	}	
}
