package com.redmoon.oa.visual;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-8-26下午09:14:21
 */
public class ModuleImportTemplateDb extends QObjectDb {
	JSONObject fields = new JSONObject();
	
	public JSONObject getFields() {
		return fields;
	}

	public void setFields(JSONObject fields) {
		this.fields = fields;
	}

	public void loadFields() {
		ResultRecord rr = getResultRecord();
		Map map = rr.getMapIndex();
		Iterator ir = map.keySet().iterator();
		while (ir.hasNext()) {
			String key = (String)ir.next();
			try {
				fields.put(key.toLowerCase(), get(key));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean load() throws SQLException {
		boolean re = super.load();
		loadFields();
		return re;
	}

	public ModuleImportTemplateDb getModuleImportTemplateDb(long id) {
		return (ModuleImportTemplateDb)getQObjectDb(new Long(id));
	}
}
