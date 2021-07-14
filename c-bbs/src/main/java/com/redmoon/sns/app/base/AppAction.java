package com.redmoon.sns.app.base;

import java.sql.SQLException;

import com.cloudwebsoft.framework.db.JdbcTemplate;

public abstract class AppAction implements IAction {
	public boolean del(String appCode, int action, long actionId) {
		JdbcTemplate jt = new JdbcTemplate();
		String sql = "delete from sns_app_action where app_code=? and app_action=? and action_id=?";
		boolean re = false;
		try {
			re = jt.executeUpdate(sql, new Object[]{appCode, new Integer(action), new Long(actionId)})==1;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}	
}
