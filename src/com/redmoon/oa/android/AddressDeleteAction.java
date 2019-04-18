package com.redmoon.oa.android;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.cloudweb.oa.service.AddressService;
import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressDeleteAction {
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
		AddressService am = new AddressService();
		try {
			re = am.del(id);
			if(re){
				json.put("res","0");
				json.put("msg","操作成功");
			}else{
				json.put("res","-1");
				json.put("msg","删除失败");
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		setResult(json.toString());
		return "SUCCESS";
	}
}
