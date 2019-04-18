package com.redmoon.oa.android;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MessageMgr;

public class MessageDeleteAction {
	private String skey = "";
	private String result = "";
	private String[] ids;
	
	private boolean dustbin = false;
	
	private boolean restore = false;
	
	public boolean isDustbin() {
		return dustbin;
	}
	public void setDustbin(boolean dustbin) {
		this.dustbin = dustbin;
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
	public String[] getIds() {
		return ids;
	}
	public void setIds(String[] ids) {
		this.ids = ids;
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
		
		MessageMgr MsgMgr = new MessageMgr();
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session = request.getSession();
		session.setAttribute(Constant.OA_NAME,privilege.getUserName(getSkey()));
		session.setAttribute(Constant.OA_UNITCODE,privilege.getUserUnitCode(getSkey()));
		try {
			if (restore) {
				re = MsgMgr.doDustbin(request, false);								
			}
			else {
				if (dustbin) {
					re = MsgMgr.delMsg(request);
				}
				else {
					re = MsgMgr.doDustbin(request, true);				
				}
			}
			if(re){
				json.put("res","0");
				json.put("msg","操作成功");
			}else{
				json.put("res","-1");
				json.put("msg","删除失败");
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
	public void setRestore(boolean restore) {
		this.restore = restore;
	}
	public boolean isRestore() {
		return restore;
	}
}
