package com.redmoon.oa.android;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

import com.opensymphony.xwork2.ActionSupport;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MessageMgr;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.notice.NoticeReplyMgr;

public class MessageReadFlagAction extends ActionSupport {
	private String result = "";

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	private String skey = "";

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id;

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			MessageMgr msg = new MessageMgr();
			MessageDb md = msg.getMessageDb(getId());
			md.setReaded(true);//设置短消息状态为已读
			md.save();
			if (md==null || !md.isLoaded()) {
				json.put("res","-1");
				json.put("msg","消息不存在！");
				setResult(json.toString());
				return "SUCCESS";
			}else{
				json.put("res", "0");
				json.put("msg", "更新成功！");
			}	
		} catch (JSONException e) {
			Logger.getLogger(MessageReadFlagAction.class).error(e.getMessage());
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(MessageReadFlagAction.class).error(e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
