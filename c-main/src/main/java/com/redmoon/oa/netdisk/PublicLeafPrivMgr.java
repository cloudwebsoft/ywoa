package com.redmoon.oa.netdisk;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.ErrMsgException;

public class PublicLeafPrivMgr {
	 public transient Logger logger = Logger.getLogger(getClass().getName());
	/**
	 * 修改权限
	 * @param lp
	 * @return
	 * @throws JSONException 
	 */
	public JSONObject modifyPriv(PublicLeafPriv lp) throws JSONException{
		JSONObject json = new JSONObject();
		boolean flag = lp.save();
		try {
			json.put("result", flag);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error("modifyPriv"+e.getMessage());
			throw new JSONException(e.getMessage());
		}
		return json;
		
		
	}

}
