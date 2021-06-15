package com.redmoon.oa.netdisk;

import com.cloudwebsoft.framework.base.QObjectDb;

public class RoleTemplateDownloadDb extends QObjectDb {
	public RoleTemplateDownloadDb getRoleTempletDownloadDb(long id) {
		return (RoleTemplateDownloadDb) getQObjectDb(new Long(id));
	}
}
