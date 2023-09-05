package com.redmoon.oa.flow;

import java.util.Vector;

import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;

public class FormViewDb extends QObjectDb {
	/**
	 * 显示视图，已弃用，视图是通用的
	 */
	public static final int KIND_SHOW = 0;
	/**
	 * 编辑视图
	 */
	public static final int KIND_EDIT = 1;
	
	public FormViewDb getFormViewDb(int id) {
		return (FormViewDb)getQObjectDb(new Integer(id));
	}
	
	public static String getKindDesc(int kind) {
		if (kind==KIND_SHOW) {
			return "显示";
		}
		else { 
			return "编辑";
		}
	}
	
	public Vector getViews(String formCode) {
		String sql = "select id from " + getTable().getName() + " where form_code=" + StrUtil.sqlstr(formCode) + " order by id desc";
		return list(sql);
	}

	public Vector list(String formCode, String op, String name) {
		String sql = "select id from " + getTable().getName() + " where form_code=" + StrUtil.sqlstr(formCode);
		if ("search".equals(op)) {
			if (!"".equals(name)) {
				sql += " and name like " + StrUtil.sqlstr("%" + name + "%");
			}
		}

		sql += " order by id asc";
		return list(sql);
	}

}
