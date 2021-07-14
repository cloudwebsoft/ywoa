package com.redmoon.oa.hr;

import java.util.Iterator;

import com.redmoon.oa.visual.FormDAO;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class PersonMgr {
	public static FormDAO getPerson(int personId) {
		String sql = "select id from form_table_personbasic where id=" + personId;
		FormDAO fdao = new FormDAO();
		Iterator ir;
		try {
			ir = fdao.list("personbasic", sql).iterator();
			if (ir.hasNext()) {
				return (FormDAO)ir.next();
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}
	
	public static FormDAO getPerson(String userName) {
		String sql = "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(userName);
		FormDAO fdao = new FormDAO();
		Iterator ir;
		try {
			ir = fdao.list("personbasic", sql).iterator();
			if (ir.hasNext()) {
				return (FormDAO)ir.next();
			}			
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}	
}
