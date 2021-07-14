package com.redmoon.oa.android;

import com.cloudwebsoft.framework.base.QObjectDb;


public class SystemUpDb extends QObjectDb {
	public SystemUpDb() {
		super();
	}

	public SystemUpDb getSystemUpDb(long id) {
		return (SystemUpDb) getQObjectDb(new Long(id));
	}

}
