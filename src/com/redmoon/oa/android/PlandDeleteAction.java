package com.redmoon.oa.android;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.SkinUtil;

import com.redmoon.oa.person.PlanDb;

public class PlandDeleteAction {
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
		PlanDb pd = new PlanDb();
		pd = pd.getPlanDb(id);
		if (pd == null || !pd.isLoaded())
			try {
				json.put("res","-1");
				json.put("msg","该计划已不存在");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	   
		String userName = pd.getUserName();
		if (!userName.equals(privilege.getUserName(getSkey()))) {
			try {
				json.put("res","-1");
				json.put("msg","权限非法");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			re = pd.del();
			if(re){
				json.put("res","0");
				json.put("msg","操作成功");
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		setResult(json.toString());
		return "SUCCESS";
	}	
}
