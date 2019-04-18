package com.redmoon.t;

import com.cloudwebsoft.framework.base.QObjectDb;

public class TStatDb extends QObjectDb {
	public static String CODE_HOT_MSG = "hot_msg";
	public static String CODE_HOT_T = "hot_t";
	
	public TStatDb getTStatDb(String code) {
		return (TStatDb)getQObjectDb(code);
	}
}
