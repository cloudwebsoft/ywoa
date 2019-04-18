package com.redmoon.sns;

import java.util.Vector;

import cn.js.fan.util.DateUtil;

import com.cloudwebsoft.framework.base.QObjectDb;

public class ActionDb extends QObjectDb {
	public static final int ACTION_BLOG = 0;
	public static final int ACTION_PHOTO = 1;
	public static final int ACTION_VIDEO = 2;
	public static final int ACTIOn_MOOD = 3;
	
	public Vector getActions(String userName, int recentDays) {
		java.util.Date today = DateUtil.parse(DateUtil.format(new java.util.Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
		java.util.Date d = DateUtil.addDate(today, -recentDays);
		String sql = "select id from " + getTable().getName() + " where user_name=? and create_date>=? order by create_date desc";
		return list(sql, new Object[]{userName, d});
	}
}
