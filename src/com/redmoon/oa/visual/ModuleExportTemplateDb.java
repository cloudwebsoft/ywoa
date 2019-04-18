package com.redmoon.oa.visual;

import com.cloudwebsoft.framework.base.QObjectDb;

public class ModuleExportTemplateDb extends QObjectDb {
	
	public ModuleExportTemplateDb getModuleExportTemplateDb(long id) {
		return (ModuleExportTemplateDb)getQObjectDb(new Long(id));
	}	

}
