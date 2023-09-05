package com.redmoon.oa.flow;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class PaperMgr {

	/**
	 * 判断是否已经公文存档
	 * @param flowId
	 * @return
	 */
	public static boolean isArchiveGovDone(int flowId) {
		String tableName = FormDb.getTableName("archive_files");
		
		String sql = "select id from " + tableName + " where flow=" + flowId;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
			if (ri.hasNext())
				return true;			
		} catch (SQLException e) {
			LogUtil.getLog(PaperMgr.class).error(e);
		}

		return false;
	}
}
