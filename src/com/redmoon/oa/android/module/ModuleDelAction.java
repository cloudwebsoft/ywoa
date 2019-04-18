package com.redmoon.oa.android.module;

import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.visual.ModuleAttachsAction;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.visual.ModuleSetupDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-8-14上午10:08:42
 */
public class ModuleDelAction {
	private String skey = "";
	private String result = "";
	private int id;
	private String moduleCode = "";
	
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
	
	/**
	 * @return the moduleCode
	 */
	public String getModuleCode() {
		return moduleCode;
	}

	/**
	 * @param moduleCode the moduleCode to set
	 */
	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
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
			FormDb fd = new FormDb();
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDb(moduleCode);
			String formCode = msd.getString("form_code");
			fd = fd.getFormDb(formCode);
			FormDAO fdao = new FormDAO(fd);
			fdao = fdao.getFormDAO(id, fd);
			re = fdao.del();
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
