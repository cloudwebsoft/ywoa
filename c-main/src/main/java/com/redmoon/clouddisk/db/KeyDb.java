package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import cn.js.fan.db.Conn;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.Config;
import com.redmoon.clouddisk.bean.KeyBean;
import com.redmoon.clouddisk.tools.ToolsUtil;

public class KeyDb {

	private KeyBean keyBean;
	public final static int KEY_LENGTH = 8;

	public final static int KEY_WRONG = 0;
	public final static int KEY_CORRECT = 1;
	public final static int KEY_OVERDUE = 2;

	private String connname;

	/**
	 * @param KeyBean
	 */
	public KeyDb(KeyBean keyBean) {
		this.keyBean = keyBean;
		connname = Global.getDefaultDB();
	}

	/**
	 * 
	 */
	public KeyDb() {
		super();
		connname = Global.getDefaultDB();
	}

	/**
	 * @return
	 */
	public boolean isExists() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select my_key from user_key where user_name=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, keyBean.getUserName());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public int getCurrentKey() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select my_key,update_date from user_key where user_name=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, keyBean.getUserName());
			rs = pstmt.executeQuery();
			Date date = new Date();
			if (rs.next()) {
				keyBean.setKey(rs.getString("my_key"));
				keyBean.setUpdateDate(rs.getTimestamp("update_date"));
				Config cfg = Config.getInstance();
				if (DateUtil.datediffMinute(date, keyBean.getUpdateDate()) >= cfg
						.getIntProperty("key_days") * 24 * 60) {
					// keyBean.setKey(ToolsUtil.randomString(KEY_LENGTH,
					// ToolsUtil.CASE_SENSITIVE));
					// keyBean.setUpdateDate(date);
					// update();
					return KEY_OVERDUE;
				} else {
					// keyBean.setUpdateDate(date);
					// update();
					return KEY_CORRECT;
				}
			} else {
				// keyBean.setKey(ToolsUtil.randomString(KEY_LENGTH,
				// ToolsUtil.CASE_SENSITIVE));
				String key = ToolsUtil.randomString(KeyDb.KEY_LENGTH,
						ToolsUtil.CASE_SENSITIVE);
				keyBean.setKey(key + key + key);
				keyBean.setUpdateDate(date);
				create();
				return KEY_CORRECT;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return KEY_WRONG;
	}

	/**
	 * @return
	 */
	public ArrayList<KeyBean> getAllUserKey() {
		Conn conn = new Conn(connname);
		ArrayList<KeyBean> list = new ArrayList<KeyBean>();
		ResultSet rs = null;
		String sql = "select my_key from user_key";
		try {
			rs = conn.executeQuery(sql);
			while (rs.next()) {
				KeyBean bean = new KeyBean();
				bean.setKey(rs.getString("my_key"));
				bean.setUserName(rs.getString("user_name"));
				bean.setUpdateDate(rs.getTimestamp("update_date"));
				list.add(bean);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return list;
	}

	/**
	 * @return
	 */
	public boolean create() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "insert into user_key (user_name,my_key,update_date) values (?,?,?)";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, keyBean.getUserName());
			pstmt.setString(2, keyBean.getKey());
			pstmt.setTimestamp(3, new Timestamp(new Date().getTime()));
			re = pstmt.executeUpdate() > 0 ? true : false;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	/**
	 * @return
	 */
	public boolean update() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update user_key set my_key=?,update_date=? where user_name=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, keyBean.getKey());
			pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			pstmt.setString(3, keyBean.getUserName());
			re = pstmt.executeUpdate() > 0 ? true : false;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}
}
