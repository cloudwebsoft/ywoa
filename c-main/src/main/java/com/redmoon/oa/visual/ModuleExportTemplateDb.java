package com.redmoon.oa.visual;

import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Iterator;
import java.util.Vector;

public class ModuleExportTemplateDb extends QObjectDb {
	
	public ModuleExportTemplateDb getModuleExportTemplateDb(long id) {
		return (ModuleExportTemplateDb)getQObjectDb(id);
	}

	public ModuleExportTemplateDb getDefault(String formCode) {
		ModuleExportTemplateDb moduleExportTemplateDb = new ModuleExportTemplateDb();
		String sql = moduleExportTemplateDb.getTable().getSql("getDefault");
		Vector v = moduleExportTemplateDb.list(sql, new Object[]{formCode});
		if (v.size() > 0) {
			return (ModuleExportTemplateDb)v.elementAt(0);
		}
		else {
			return null;
		}
	}

	public boolean setDefault(String moduleCode, int id, boolean isDefault) {
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(moduleCode);
		ModuleExportTemplateDb mitd = new ModuleExportTemplateDb();
		String sql = mitd.getTable().getSql("listForForm");
		Vector v = mitd.list(sql, new Object[]{msd.getFormCode()});
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			mitd = (ModuleExportTemplateDb)ir.next();
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
}
