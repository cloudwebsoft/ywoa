package com.redmoon.oa.android;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.redmoon.oa.person.PlanMgr;

public class PlandEditAction {
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
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		session.setAttribute(Constant.OA_NAME,privilege.getUserName(getSkey()));
		session.setAttribute(Constant.OA_UNITCODE,privilege.getUserUnitCode(getSkey()));
		PlanMgr pm = new PlanMgr();
		try {
			re = pm.modifyByParameter(request);
			if(re){
				json.put("res","0");
				json.put("msg","修改成功");
			}else{
				json.put("res","-2");
				json.put("msg","修改失败");
			}
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
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		setResult(json.toString());
		return "SUCCESS";
	}		
}
