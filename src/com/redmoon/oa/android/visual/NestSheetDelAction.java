package com.redmoon.oa.android.visual;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.macroctl.SQLCtl;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.FormUtil;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-2-14上午10:07:19
 */
public class NestSheetDelAction extends BaseAction {
	private String skey = "";
	private String result = "";
	private long id = -1;
	
	private String formCode = "";
	

	/**
	 * @return the formCodeRelated
	 */
	public String getFormCodeRelated() {
		return formCodeRelated;
	}
	/**
	 * @param formCodeRelated the formCodeRelated to set
	 */
	public void setFormCodeRelated(String formCodeRelated) {
		this.formCodeRelated = formCodeRelated;
	}
	/**
	 * 嵌套表单的编码
	 */
	private String formCodeRelated = "";
	
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
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
		privilege.doLogin(request, getSkey());
		
		try {
			if (id==-1) {
				json.put("res", "-1");
				json.put("msg", "缺少ID！");
				setResult(json.toString());
				return "SUCCESS";				
			}
			
			FormMgr fm = new FormMgr();
			FormDb fdRelated = fm.getFormDb(formCodeRelated);
			com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
			String cwsId = "";
			boolean isNestSheet = true;
			try {
				FormDAO fdao = new FormDAO();
				fdao = fdao.getFormDAO(id, fdRelated);
				cwsId = fdao.getCwsId();				

				re = fdm.del(request, isNestSheet, formCodeRelated);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				json.put("res", "-1");
				json.put("msg", e.getMessage());
				setResult(json.toString());
				return "SUCCESS";
			}

			if(re) {
				json.put("res","0");
				json.put("msg","操作成功！");
				
				FormDb pfd = new FormDb();
				pfd = pfd.getFormDb(formCode);
				
				json.put("sums", FormUtil.getSums(fdRelated, pfd, cwsId));
			} else {
				json.put("res","-1");
				json.put("msg","操作失败！");
			}
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ModuleAttachsAction.class).error(e.getMessage());
		}	
		setResult(json.toString());
		return "SUCCESS";
	}
	
	/**
	 * @param formCode the formCode to set
	 */
	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}
	
	/**
	 * @return the formCode
	 */
	public String getFormCode() {
		return formCode;
	}

}

