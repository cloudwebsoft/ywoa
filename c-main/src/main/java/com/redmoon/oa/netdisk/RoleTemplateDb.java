package com.redmoon.oa.netdisk;

import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.base.QObjectDb;

public class RoleTemplateDb extends QObjectDb {
	transient Logger logger = Logger.getLogger(RoleTemplateDb.class.getName());
	public RoleTemplateDb getRoleTempletDb(long id) {
        return (RoleTemplateDb) getQObjectDb(new Long(id));
    }
	
	
}
