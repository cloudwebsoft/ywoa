package com.redmoon.oa.android.visual;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;

public class ModuleAttDelAction {
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
		
		try {
			com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment(id);
			re = att.del();
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
