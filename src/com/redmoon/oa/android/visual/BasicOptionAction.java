package com.redmoon.oa.android.visual;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.basic.SelectOptionDb;

public class BasicOptionAction {
	private String skey = "";
	private String result = "";
	private String codes = "";

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

	public String getCodes() {
		return codes;
	}

	public void setCodes(String codes) {
		this.codes = codes;
	}

	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		JSONObject result = new JSONObject();		
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			String[] codeArr = codes.split(",");
			if (codeArr != null && codeArr.length > 0) {
				for (String code : codeArr) {
					Vector vec = SelectMgr.getOptions(code);
					Iterator ir = vec.iterator();
					JSONArray options = new JSONArray();
					while (ir.hasNext()) {
						SelectOptionDb sod = (SelectOptionDb) ir.next();
						JSONObject optionJson = new JSONObject();
						optionJson.put("id",sod.getId());
						optionJson.put("name", sod.getName());
						optionJson.put("value", sod.getValue());
						options.put(optionJson);
					}
					result.put(code + "SelectOptions", options);
				}
				json.put("result", result);
				json.put("res", "0");
				json.put("msg", "操作成功！");
			} else {
				json.put("res", "-1");
				json.put("msg", "编码为空！");
			}
		} catch (JSONException e) {
			Logger.getLogger(BasicOptionAction.class).error(e.getMessage());
		} finally {
			setResult(json.toString());
		}
		return "SUCCESS";

	}

}
