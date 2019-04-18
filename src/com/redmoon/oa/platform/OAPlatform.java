package com.redmoon.oa.platform;

import java.util.ArrayList;
import java.util.List;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;

import com.cloudwebsoft.framework.db.JdbcTemplate;

public class OAPlatform implements IPlatformRelated {

	@Override
	public List<String> getRoles(String name) {

		List<String> roleCodes = null;

		String sql = "select roleCode from user_of_role where userName=" + SQLFilter.sqlstr(name) ;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = new ResultIterator();
			ri = jt.executeQuery(sql);
			if (ri != null) {
				roleCodes = new ArrayList<String>();
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					roleCodes.add(rr.getString(1));
				}
			}
		} catch (Exception e) {

		}finally{
			jt.close();
		}
		return roleCodes;
	}
	
	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEcid() {
		// TODO Auto-generated method stub
		return "";
	}
}
