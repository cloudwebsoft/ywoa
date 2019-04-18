package com.redmoon.oa.android;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.notice.NoticeMgr;
import com.redmoon.oa.notice.NoticeReplyMgr;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

public class NoticeDeleteAction {
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
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			HttpServletRequest request = ServletActionContext.getRequest();
			privilege.doLogin(request, getSkey());
			com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
			boolean isNoticeAll = pvg.isUserPrivValid(request, "notice");
			boolean isNoticeMgr = pvg.isUserPrivValid(request, "notice.dept");
			if (!isNoticeAll && !isNoticeMgr) {
				json.put("res", "-1");
				json.put("msg", "权限非法");
				setResult(json.toString());
				return "SUCCESS";
			}
			NoticeMgr nd = new NoticeMgr();
			re = nd.del(request, id);
			NoticeReplyMgr nrm = new NoticeReplyMgr();
			if (re) {
				re = nrm.delReply(id);
				if(re){
					json.put("res", "0");
					json.put("msg", "操作成功");	
				}else{
					json.put("res", "-1");
					json.put("msg", "删除失败");
				}
				
			} else {
				json.put("res", "-1");
				json.put("msg", "删除失败");
			}
		} catch (ErrMsgException e) {
			Logger.getLogger(e.getClass()).error(e.getMessage());
		} catch (JSONException e) {
			Logger.getLogger(e.getClass()).error(e.getMessage());
		}
		setResult(json.toString());
		return "SUCCESS";
	}
}
