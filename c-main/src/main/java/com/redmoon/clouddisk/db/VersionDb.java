package com.redmoon.clouddisk.db;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.VersionBean;

public class VersionDb {

	private VersionBean versionBean;

	private String connname;

	public VersionDb(VersionBean versionBean) {
		this.versionBean = versionBean;
		connname = Global.getDefaultDB();
	}

	// 当前程序是否是最新的版本号
	/**
	 * @return
	 */
	public boolean isLastVersion() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select file_version,file_name from netdisk_version where is_current=1 and is_wow64="
				+ versionBean.getIsWow64();
		try {
			rs = conn.executeQuery(sql);
			if (rs.next()) {
				versionBean.setFileName(rs.getString("file_name"));
				String verNo = StrUtil.getNullString(rs
						.getString("file_version"));
				if (verNo.equals(versionBean.getFileVersion())) {
					return true;
				} else {
					versionBean.setFileVersion(verNo);
					return false;
				}
			} else {
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

	// 获取文件全路径
	/**
	 * @return
	 */
	public boolean getFullPath() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select file_path,file_name from netdisk_version where is_current=1 and is_wow64=0";
		try {
			rs = conn.executeQuery(sql);
			if (rs.next()) {
				versionBean.setFilePath(Global.getAppPath()
						+ StrUtil.getNullString(rs.getString("file_path"))
						+ File.separator + rs.getString("file_name"));
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

	// 获取文件全路径
	/**
	 * @return
	 */
	public boolean getFullPath64() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select file_path,file_name from netdisk_version where is_current=1 and is_wow64=1";
		try {
			rs = conn.executeQuery(sql);
			if (rs.next()) {
				versionBean.setFilePath(Global.getAppPath()
						+ StrUtil.getNullString(rs.getString("file_path"))
						+ File.separator + rs.getString("file_name"));
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

}
