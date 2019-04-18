package com.redmoon.oa.address;

import java.util.Vector;

import cn.js.fan.util.ResKeyException;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

public class AddressVersionDb extends QObjectDb {
	/**
	 * 个人通讯录与公共通讯录版本
	 * @param user_name
	 * @param unitCode
	 * @return
	 */
	public boolean createForUnit(String user_name,String unitCode) {
		boolean re = false;
		Integer version = new Integer(0);
		try {
			
			re = create(new JdbcTemplate(), new Object[]{user_name, unitCode, version, new java.util.Date()});
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
	
	/**
	 * 获得公共通讯录或个人通讯录的版本号
	 * @param user_name
	 * @param unitCode
	 * @return
	 */
	public AddressVersionDb getAddressVersionDbOfUnit(String user_name,String unitCode) {
		String sql = "select id from " + getTable().getName() + " where unit_code=? and user_name=?";
		Vector v = list(sql, new Object[]{unitCode,user_name});
		if (v.size()>0) {
			return (AddressVersionDb)v.elementAt(0);
		}
		else {
			// 如果不存在，则创建
			createForUnit(user_name,unitCode);
			v = list(sql, new Object[]{unitCode,user_name});
			return (AddressVersionDb)v.elementAt(0);
		}
	}
}
