package com.redmoon.t;


import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;


/**
 * 关注记录
 * @author fgf
 *
 */
public class FollowDb extends QObjectDb {
	
	/**
	 * 判断用户是否关注某微博
	 * @param userName
	 * @param tid
	 * @return
	 */
	public boolean isUserFollowT(long tid, String userName) {
		String sql = "select id from " + getTable().getName() + " where t_id=? and user_name=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{new Long(tid), userName});
			if (ri.hasNext()) {
				// ResultRecord rr = (ResultRecord)ri.next();
				return true;
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean disFollow(long tid, String userName) {
		String sql = "delete from " + getTable().getName() + " where t_id=? and user_name=?";
		JdbcTemplate jt = new JdbcTemplate();
		boolean re = false;
		try {
			re = jt.executeUpdate(sql, new Object[]{new Long(tid), userName})==1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return re;
	}
}
