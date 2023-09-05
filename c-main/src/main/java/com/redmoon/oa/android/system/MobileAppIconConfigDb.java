package com.redmoon.oa.android.system;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.base.QObjectDb;


public class MobileAppIconConfigDb extends QObjectDb {
	public static final int TYPE_MENU = 1;
	public static final int TYPE_FLOW = 2;
	public static final int TYPE_MODULE = 3;
	public static final int TYPE_LINK = 4;
	public static final int TYPE_FRONT = 5;

	public static final int OA_NOTICE = 1;//通知
	public static final int OA_FLOW = 2;//流程
	public static final int OA_DAYILY = 3;//日报
	public static final int OA_TEAM = 4;//通讯录
	public static final int OA_CRM = 5;//Crm
	public static final int OA_INNER_MSG = 6;//内部邮件
	public static final int OA_SYSTEM_MSG = 7;//系统邮件
	// public static final int OA_NETDISK = 8;//网络硬盘
	public static final int OA_SCHEDULE = 9;//日程安排
	public static final int OA_FILECASE = 10;//文件柜
	public static final int OA_LOACTION = 11;//定位签到
	public static final int OA_LEADER_WORK = 12;//领导督办
	public static final int OA_PROJECT = 13;//项目管理
	public static final int OA_TASK = 14;//任务管理
	public static final int OA_MY_SCORE = 15;//我的积分
	public static final int OA_SIGN_UP = 16;//签退

	public MobileAppIconConfigDb() {
		super();
	}

	public String getListSql(String op, int type, String what) {
		String sql = "select id from mobile_app_icon_config where 1=1 ";
		if ("search".equals(op)) {
			if (!"".equals(what)) {
				sql += " and name like " + StrUtil.sqlstr("%" + what + "%") + "";
			}
			if (type != -1) {
				sql += " and type = " + type;
			}
		}

		sql += " order by orders desc, id asc";
		return sql;
	}

	public MobileAppIconConfigDb getMobileAppIconConfigDb(long id) {
		return (MobileAppIconConfigDb) getQObjectDb(new Long(id));
	}

}
