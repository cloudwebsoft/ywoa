package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.ResumeBrokenBean;

public class ResumeBrokenDb {

	public final static int TYPE_LAST_PACKAGE = 1;
	public final static int TYPE_LOST_PACKAGE = 2;

	private ResumeBrokenBean resumeBrokenBean;

	private String connname;

	/**
	 * @param ResumeBrokenBean
	 */
	public ResumeBrokenDb(ResumeBrokenBean resumeBrokenBean) {
		this.resumeBrokenBean = resumeBrokenBean;
		connname = Global.getDefaultDB();
	}

	/**
	 * 
	 */
	public ResumeBrokenDb() {
		super();
		connname = Global.getDefaultDB();
	}

	/**
	 * @Description: 获取断点
	 * @return
	 */
	public boolean getLastPackageNo() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select package_no from netdisk_resume_broken where user_name=? and att_id=? and package_type=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, resumeBrokenBean.getUserName());
			pstmt.setLong(2, resumeBrokenBean.getAttId());
			pstmt.setInt(3, TYPE_LAST_PACKAGE);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				resumeBrokenBean.setPackageNo(rs.getInt(1));
				return true;
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
	 * @Description: 获取丢包
	 * @return
	 */
	public ArrayList<Integer> getAllUserKey() {
		Conn conn = new Conn(connname);
		ArrayList<Integer> list = new ArrayList<Integer>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select package_no from netdisk_resume_broken where user_name=? and att_id=? and package_type=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, resumeBrokenBean.getUserName());
			pstmt.setLong(2, resumeBrokenBean.getAttId());
			pstmt.setInt(3, TYPE_LOST_PACKAGE);
			rs = conn.executeQuery(sql);
			while (rs.next()) {
				list.add(rs.getInt(1));
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
	 * @Description: 插入断点
	 * @return
	 */
	public boolean create() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "insert into netdisk_resume_broken (user_name,att_id,package_no,package_type) values (?,?,?,?)";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, resumeBrokenBean.getUserName());
			pstmt.setLong(2, resumeBrokenBean.getAttId());
			pstmt.setInt(3, resumeBrokenBean.getPackageNo());
			pstmt.setInt(4, resumeBrokenBean.getPackageType());
			re = pstmt.executeUpdate() == 1 ? true : false;
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
	 * @Description: 更新断点
	 * @return
	 */
	public boolean update() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_resume_broken set package_no=? where user_name=? and att_id=? and package_type=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, resumeBrokenBean.getPackageNo());
			pstmt.setString(2, resumeBrokenBean.getUserName());
			pstmt.setLong(3, resumeBrokenBean.getAttId());
			pstmt.setInt(4, TYPE_LAST_PACKAGE);
			re = pstmt.executeUpdate() == 1 ? true : false;
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

	public boolean del() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "delete from netdisk_resume_broken where user_name=? and att_id=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, resumeBrokenBean.getUserName());
			pstmt.setLong(2, resumeBrokenBean.getAttId());
			re = pstmt.executeUpdate() >= 0 ? true : false;
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
