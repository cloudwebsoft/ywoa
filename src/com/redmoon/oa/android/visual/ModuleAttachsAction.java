package com.redmoon.oa.android.visual;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormParser;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.visual.FormDAO;

public class ModuleAttachsAction {
	private String skey = "";
	private String formCode;
	private String result = "";
	private long id = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public String execute() {
		// 手机客户端 —— 新增 判断 需要显示的列
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		try {
			boolean re = privilege.Auth(getSkey());
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			if (formCode != null && !formCode.trim().equals("")) {
				FormDb formDb = new FormDb(formCode);
				com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formDb);
				if (id != 0) {
					com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(id);
					Iterator itFiles = fdao.getAttachments().iterator();
					JSONArray filesArr = new JSONArray();
					while (itFiles.hasNext()) {
						JSONObject fileObj = new JSONObject();
						com.redmoon.oa.visual.Attachment am = (com.redmoon.oa.visual.Attachment) itFiles
								.next();
						String name = am.getName();
						String downUrl = "/public/visual/visual_getfile.jsp";
						int attId = am.getId();
						fileObj.put("name", name);
						fileObj.put("downloadUrl", downUrl);
						fileObj.put("id", attId);
						filesArr.put(fileObj);
					}
					json.put("files", filesArr);
					json.put("res", "0");
					json.put("msg", "操作成功");
					setResult(json.toString());
				} else {
					json.put("res", "-1");
					json.put("msg", "附件不存在");
					setResult(json.toString());
				}
			} else {
				json.put("res", "-1");
				json.put("msg", "表单编码为空！");
				setResult(json.toString());
			}
			

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "SUCCESS";
	}

}
