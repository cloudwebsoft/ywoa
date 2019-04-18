package com.redmoon.oa.android;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.worklog.WorkLogMgr;

public class MyworkDeleteAction {
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
		WorkLogMgr wl = new WorkLogMgr();		
		try {
			//re = wl.del(request);
			JdbcTemplate jd = new JdbcTemplate();
			String sql = "delete from work_log where id="+getId();
			int i = jd.executeUpdate(sql);
			if(i>0){
				json.put("res","0");
				json.put("msg","操作成功");
			}else{
				json.put("res","-1");
				json.put("msg","删除失败");
			}
		} catch (SQLException e) {
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

