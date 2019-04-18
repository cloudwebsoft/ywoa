package com.redmoon.oa.android;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.oa.worklog.WorkLogDb;
import com.redmoon.oa.worklog.WorkLogMgr;

public class MyworkDetailAction {
	private String skey = "";
	private String result = "";
	private int id;
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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
		
		WorkLogMgr wlm = new WorkLogMgr();
		WorkLogDb wld = null;
		HttpServletRequest request = ServletActionContext.getRequest();		
		Privilege pvg = new Privilege();
		pvg.doLogin(request, getSkey());
		try {
			wld = wlm.getWorkLogDb(request, getId());
		}
		catch (ErrMsgException e) {
			e.printStackTrace();
			try {
				json.put("res", "-3");
				json.put("msg", e.getMessage());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			setResult(json.toString());
			return "SUCCESS";
		}
		String content="",appraise="",mydate="";	
		if (wld!=null && wld.isLoaded()) {
			content = wld.getContent();
			appraise = wld.getAppraise();
			mydate = DateUtil.format(wld.getMyDate(), "yyyy-MM-dd HH:mm:ss");
		}		
		try {
			json.put("res", "0");
			json.put("msg", "操作成功");
			
			// json.put("content",privilege.delHTMLTag(StrUtil.getAbstract(request, content, 50000, "\r\n")));
			json.put("content", StrUtil.getAbstract(request, content, 50000, "\r\n"));
			
			// json.put("content", content);
			json.put("appraise", appraise);
			json.put("mydate", mydate);		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		setResult(json.toString());
		return "SUCCESS";
	}	
}
