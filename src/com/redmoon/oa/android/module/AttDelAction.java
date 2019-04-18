package com.redmoon.oa.android.module;

import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.visual.ModuleAttachsAction;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-8-13下午08:24:47
 */
public class AttDelAction {
	private String skey = "";
	private String result = "";
	private int id;
	
	private boolean isFlow = false;
	
	public boolean isFlow() {
		return isFlow;
	}
	public void setFlow(boolean isFlow) {
		this.isFlow = isFlow;
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
		
		try {
			if (isFlow) {
				com.redmoon.oa.flow.Attachment att = new com.redmoon.oa.flow.Attachment(id);
				re = att.del();
			}
			else {
				com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(id);
				re = att.del();
			}
			if(re){
				json.put("res","0");
				json.put("msg","操作成功");
			}else{
				json.put("res","-1");
				json.put("msg","删除失败");
			}
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ModuleAttachsAction.class).error(e.getMessage());
		}	
		setResult(json.toString());
		return "SUCCESS";
	}
}
