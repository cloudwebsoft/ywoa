package com.redmoon.oa.usermobile;


import com.cloudwebsoft.framework.base.QObjectDb;

public class UserMobileDb extends QObjectDb {
	public UserMobileDb getUserMobileDb(long id){
		return (UserMobileDb)getQObjectDb(id);
	}
	
	
}
