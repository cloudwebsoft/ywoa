package com.redmoon.forum.tools;

import cn.js.fan.db.ResultIterator;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

public class ChinaRegionDb extends QObjectDb {

	public String getRegionName(int regionId) {
		if (regionId==0)
			return "";
		ChinaRegionDb crd = (ChinaRegionDb)getQObjectDb(new Integer(regionId));
		if (crd==null)
			return "";
		return crd.getString("region_name");
	}
}
