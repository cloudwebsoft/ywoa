package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.AttachmentBean;

/**
 * @author 古月圣
 * 
 */
public class AttachmentDb {

	private AttachmentBean attachmentBean;

	private String connname;

	/**
	 * @param AttachmentBean
	 */
	public AttachmentDb(AttachmentBean attachmentBean) {
		this.attachmentBean = attachmentBean;
		connname = Global.getDefaultDB();
	}

	/**
	 * 
	 */
	public AttachmentDb() {
		super();
		connname = Global.getDefaultDB();
	}

	/**
	 * @return
	 */
	public boolean isExist() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select id from netdisk_document_attach where doc_id=? and name=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, attachmentBean.getDocId());
			pstmt.setString(2, attachmentBean.getName());
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
	public boolean create() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "insert into netdisk_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num,orders,file_size,ext,publicShareDir,uploaddate,USER_NAME) values (?,?,?,?,?,?,?,?,?,'',?,?)";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, attachmentBean.getFullPath());
			pstmt.setLong(2, attachmentBean.getDocId());
			pstmt.setString(3, attachmentBean.getName());
			pstmt.setString(4, attachmentBean.getDiskName());
			pstmt.setString(5, attachmentBean.getVisualPath());
			pstmt.setInt(6, attachmentBean.getPageNum());
			pstmt.setInt(7, attachmentBean.getOrders());
			pstmt.setLong(8, attachmentBean.getFileSize());
			pstmt.setString(9, attachmentBean.getExt());
			pstmt.setTimestamp(10, new Timestamp(new Date().getTime()));
			pstmt.setString(11, attachmentBean.getUserName());
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
