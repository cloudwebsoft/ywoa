package com.redmoon.oa.visual;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import cn.js.fan.util.ResKeyException;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;

import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-8-26下午09:14:21
 */
public class ModuleImportTemplateDb extends QObjectDb {
	// org.json.JSONObject 会导致NotSerializableException
	// org.json.JSONObject fields = new org.json.JSONObject();
	JSONObject fields = new JSONObject();
	
	public JSONObject getFields() {
		return fields;
	}

	public void setFields(JSONObject fields) {
		this.fields = fields;
	}

	public void loadFields() {
		ResultRecord rr = getResultRecord();
		if (rr == null) {
			DebugUtil.e(getClass(), "loadFields", "getResultRecord is null. The templateId may not exist.");
			return;
		}
		Map map = rr.getMapIndex();
		Iterator ir = map.keySet().iterator();
		while (ir.hasNext()) {
			String key = (String)ir.next();
			fields.put(key.toLowerCase(), get(key));
		}
	}

	public ModuleImportTemplateDb getDefault(String formCode) {
		ModuleImportTemplateDb mitd = new ModuleImportTemplateDb();
		String sql = mitd.getTable().getSql("getDefault");
		Vector v = mitd.list(sql, new Object[]{formCode});
		if (v.size() > 0) {
			return (ModuleImportTemplateDb)v.elementAt(0);
		}
		else {
			return null;
		}
	}

	public boolean setDefault(String moduleCode, int id, boolean isDefault) {
		ModuleImportTemplateDb mitd = new ModuleImportTemplateDb();
		String sql = mitd.getTable().getSql("listForForm");
		Vector v = mitd.list(sql, new Object[]{moduleCode});
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			mitd = (ModuleImportTemplateDb)ir.next();
			if (mitd.getInt("id") == id) {
				mitd.set("is_default", isDefault ? 1 : 0);
			}
			else {
				mitd.set("is_default", isDefault ? 0: 1);
			}
			try {
				mitd.save();
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(e);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean load() throws SQLException {
		boolean re = super.load();
		loadFields();
		return re;
	}

	public ModuleImportTemplateDb getModuleImportTemplateDb(long id) {
		return (ModuleImportTemplateDb)getQObjectDb(id);
	}
}
