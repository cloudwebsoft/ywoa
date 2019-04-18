package com.redmoon.oa.android.sales;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;

public class SalesModuleDao {
	public static int getCountInfoById(String sql) {
		JdbcTemplate jt = null;
		int sum = 0;
		try {
			jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord record = (ResultRecord) ri.next();
				sum = record.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(SalesContractListAction.class).error(
					"ReakSumByContact SQLException:" + e.getMessage());
		}
		return sum;
	}

	/**
	 * 获得salePerson
	 * 
	 * @param userName
	 * @return
	 */
	public static String getSalesPerson(String userName) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(StrUtil.sqlstr(userName));
		UserDb ud = new UserDb();
		ud = ud.getUserDb(userName);
		String[] depts = ud.getAdminDepts();
		DeptUserDb dud = null;
		Vector v = null;
		Iterator ir = null;

		if (depts != null) {
			for (String dept : depts) {
				dud = new DeptUserDb();
				v = dud.list(dept);
				if (v != null) {
					ir = v.iterator();
					while (ir.hasNext()) {
						dud = (DeptUserDb) ir.next();
						buffer.append(",");
						buffer.append(StrUtil.sqlstr(dud.getUserName()));
					}
				}
			}
		}
		return buffer.toString();
	}
}
