package com.redmoon.oa.basic;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

public class RegionDb extends QObjectDb {
	public RegionDb() {
		super();
	}

	public RegionDb getRegionDB(long id) {
		return (RegionDb) getQObjectDb(new Long(id));
	}

	public String getRegionName(int parent_id) throws SQLException {
		String sql = "select region_name from  oa_china_region where region_id = "
				+ parent_id;
		JdbcTemplate jd = new JdbcTemplate();
		ResultIterator ri = jd.executeQuery(sql);
		ResultRecord rr = null;
		String region_name = "";
		if (ri.hasNext()) {
			rr = (ResultRecord) ri.next();
			region_name = rr.getString(1);
		}
		return region_name;
	}

	public boolean isRegionType(int id) throws SQLException {
		boolean re = false;
		String sql = "select region_type from  oa_china_region where region_id = "
				+ id;
		JdbcTemplate jd = new JdbcTemplate();
		ResultIterator ri = jd.executeQuery(sql);
		ResultRecord rr = null;
		int region_type = 0;
		if (ri.hasNext()) {
			rr = (ResultRecord) ri.next();
			region_type = rr.getInt(1);
			if (region_type == 3) {
				return re;
			}
		}
		return true;
	}

}
