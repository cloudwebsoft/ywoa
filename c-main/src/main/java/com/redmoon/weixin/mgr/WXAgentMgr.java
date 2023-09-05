package com.redmoon.weixin.mgr;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.weixin.bean.WxAgent;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.util.HttpUtil;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-8-5上午09:45:34
 */
public class WXAgentMgr extends WXBaseMgr{
	/**
	 * 应用列表
	 * @Description: 
	 * @return
	 */
	public List<WxAgent> agentList(){
		String accessToken = getToken();
		List<WxAgent> agents = new ArrayList<WxAgent>();
		String result = HttpUtil.MethodGet(Constant.AGENT_LIST+accessToken);
		if (result != null && !result.equals("")) {
			JSONObject json;
			try {
				json = new JSONObject(result);
				if (json != null && !json.isNull(Constant.ERRCODE)) {
					int errorCode = json.getInt(Constant.ERRCODE);
					if (errorCode == Constant.ERROR_CODE_SUCCESS) {
						String agentList = json.getString(Constant.AGENTLIST);
						agents = JSON.parseArray(agentList, WxAgent.class);
					}
				}
			} catch (JSONException e) {
				LogUtil.getLog(WXAgentMgr.class).info(e.getMessage());
			}
		}
		return agents;
	}
	
	
	

}
