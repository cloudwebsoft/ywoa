package com.redmoon.oa.android;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.person.PlanDb;

public class PlandClosedAction {
	private String skey = "";
	private String result = "";
	private int id;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
		pd = pd.getPlanDb(getId());
		pd.setClosed(true);
		try {
			re = pd.save();
			if(re){
				json.put("res","0");
				json.put("msg","操作成功");
			}else{
				json.put("res","-1");
				json.put("msg","操作失败");
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
