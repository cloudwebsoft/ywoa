package com.redmoon.oa.android.system;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.*;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.ui.menu.*;

/**
 * @author lichao
 * 应用列表接口(拥有发起权限)
 */
public class GetMobileAppIconCanStartAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //获取成功
	private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
	
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
	
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(skey);
		
		if(re){
			try {
				jReturn.put("res",RES_EXPIRED);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		HttpServletRequest request = ServletActionContext.getRequest();
		privilege.doLogin(request, getSkey());
		com.redmoon.oa.pvg.Privilege pe = new com.redmoon.oa.pvg.Privilege();
		boolean canSendNotice = true;
		if(!pe.isUserPrivValid(request, "notice") && !pe.isUserPrivValid(request, "notice.dept")){
			canSendNotice = false;
		}
		try {
			MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
			JSONArray jArr = mr.getMobileCanStartInfo(canSendNotice);
			if(jArr.length()==0){
				jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
			}else{
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS);	
				jResult.put("datas", jArr);
			}
			jReturn.put("result", jResult);
			jReturn.put("res", RES_SUCCESS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(GetMobileAppIconCanStartAction.class).error(e.getMessage());
		}
		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
