package com.redmoon.oa.licenceValidate;

import com.cloudwebsoft.framework.base.QObjectDb;


/**
 * @Description: 册信息保存类
 * @author: lichao
 * @Date: 2015-8-28下午02:09:16
 */
public class RegistInfoDb extends QObjectDb {

	public RegistInfoDb() {
		super();
	}

	public RegistInfoDb getRegistInfoDb(int id) {
		return (RegistInfoDb) getQObjectDb(new Integer(id));
	}
}
