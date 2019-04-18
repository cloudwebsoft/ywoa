package com.redmoon.oa.archive;

import java.sql.SQLException;

import cn.js.fan.db.*;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;

public class ArchiveStatMgr {

	public ArchiveStatAge[] createStatAgeList(String depts) {
		ArchiveStatAge[] a = new ArchiveStatAge[6];

		String sql = "select count(*) from ARCHIVE_USER where birthday>=? and birthday<?";
		if (!depts.equals("")) {
			/*
			String[] ary = StrUtil.split(depts, ",");
			String str = "";
			for (int i=0; i<ary.length; i++) {
				if (str.equals("")) {
					str = StrUtil.sqlstr(ary[i]);
				}
				else
					str += "," + StrUtil.sqlstr(ary[i]);
			}
			*/
			sql += " and deptcode in (" + depts + ")";
		}
		// System.out.println(getClass() + " str=" + sql);

		JdbcTemplate jt = new JdbcTemplate();
		try {
			java.util.Date d2 = new java.util.Date();
			java.util.Date d1 = DateUtil.addDate(d2, -365*20);			
			ResultIterator ri = jt.executeQuery(sql, new Object[]{d1, d2});
			ResultRecord rr = null;
			if (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				a[0] = new ArchiveStatAge("1-20", rr.getInt(1));
			}
			else {
				a[0] = new ArchiveStatAge("1-20", 0);			
			}
			
			d2 = d1;
			d1 = DateUtil.addDate(d1, -365*10);
			ri = jt.executeQuery(sql, new Object[]{d1, d2});
			if (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				a[1] = new ArchiveStatAge("20-30", rr.getInt(1));
			}
			else {
				a[1] = new ArchiveStatAge("20-30", 0);			
			}
			
			d2 = d1;
			d1 = DateUtil.addDate(d1, -365*10);
			ri = jt.executeQuery(sql, new Object[]{d1, d2});
			if (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				a[2] = new ArchiveStatAge("30-40", rr.getInt(1));
			}
			else {
				a[2] = new ArchiveStatAge("30-40", 0);			
			}
			
			d2 = d1;
			d1 = DateUtil.addDate(d1, -365*10);
			ri = jt.executeQuery(sql, new Object[]{d1, d2});
			if (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				a[3] = new ArchiveStatAge("40-50", rr.getInt(1));
			}
			else {
				a[3] = new ArchiveStatAge("40-50", 0);			
			}
			
			d2 = d1;
			d1 = DateUtil.addDate(d1, -365*10);
			ri = jt.executeQuery(sql, new Object[]{d1, d2});
			if (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				a[4] = new ArchiveStatAge("50-60", rr.getInt(1));
			}
			else {
				a[4] = new ArchiveStatAge("50-60", 0);			
			}
			
			d2 = d1;
			d1 = DateUtil.addDate(d1, -365*10);
			ri = jt.executeQuery(sql, new Object[]{d1, d2});
			if (ri.hasNext()) {
				rr = (ResultRecord)ri.next();
				a[5] = new ArchiveStatAge("60-70", rr.getInt(1));
			}
			else {
				a[5] = new ArchiveStatAge("60-70", 0);			
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return a;
	}

}
