package com.redmoon.oa.android;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

public class MyinfoListAction {
	private String skey = "";
	private String result = "";

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
		if (re) {
			try {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String username = privilege.getUserName(getSkey());
		UserMgr um = new UserMgr();
		UserDb user = um.getUserDb(username);
		try {
			if (user == null || !user.isLoaded()) {
				json.put("res", "-1");
				json.put("msg", "该用户已不存在！");
				setResult(json.toString());
				return "SUCCESS";
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			  json.put("res","0");
			  json.put("msg","操作成功");		 
			  json.put("RealName",StrUtil.getNullStr(user.getRealName()));
			  json.put("name",StrUtil.getNullStr(user.getName()));
			  json.put("isValid",user.getValid());	  
			  json.put("Password",StrUtil.getNullStr(user.getPwdRaw()));
			  json.put("Email",StrUtil.getNullStr(user.getEmail()));
			  json.put("Phone",StrUtil.getNullStr(user.getPhone()));
			  json.put("mobile",StrUtil.getNullStr(user.getMobile()));
			  json.put("QQ",StrUtil.getNullStr(user.getQQ()));
			  json.put("IDCard",StrUtil.getNullStr(user.getIDCard()));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		
		LogUtil.getLog(getClass()).info("execute: result=" + json.toString());
		
		return "SUCCESS";
	}
}
