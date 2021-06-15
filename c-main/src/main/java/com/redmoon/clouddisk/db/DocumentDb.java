package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.DocumentBean;

public class DocumentDb {

	private DocumentBean documentBean;

	private String connname;

	public DocumentDb() {
		super();
		connname = Global.getDefaultDB();
	}

	public DocumentDb(DocumentBean documentBean) {
		this.documentBean = documentBean;
		connname = Global.getDefaultDB();
	}

	/**
	 * @return
	 */
	public boolean isExist() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "select id from netdisk_document where class1=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, documentBean.getClass1());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				documentBean.setId(rs.getLong("id"));
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
	public boolean getRootId() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select id from netdisk_document where class1=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, documentBean.getClass1());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				documentBean.setId(rs.getLong("id"));
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
	public boolean getCurId() {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select max(id) from netdisk_document";
		try {
			rs = conn.executeQuery(sql);
			if (rs.next()) {
				documentBean.setId(rs.getLong("max(id)") + 1);
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
		String sql = "INSERT into netdisk_document (id, title, class1, type, voteoption, voteresult, nick, keywords, isrelateshow, can_comment, hit, template_id, parent_code, examine, isNew, author, flowTypeCode, modifiedDate) VALUES (?,?,?,?,?,?,?,?,?,?,0,?,?,?,?,?,?,?)";
		boolean re = false;
		try {
			// 插入文章标题及相关设置
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, documentBean.getId());
			pstmt.setString(2, documentBean.getTitle());
			pstmt.setString(3, documentBean.getClass1());
			pstmt.setInt(4, documentBean.getType());
			pstmt.setString(5, documentBean.getVoteOption());
			pstmt.setString(6, documentBean.getVoteResult());
			pstmt.setString(7, documentBean.getNick());
			pstmt.setString(8, "");
			pstmt.setInt(9, 1);
			pstmt.setInt(10, documentBean.getCanCommit());
			pstmt.setInt(11, documentBean.getTemplateId());
			pstmt.setString(12, documentBean.getParentCode());
			pstmt.setInt(13, documentBean.getExamine());
			pstmt.setInt(14, documentBean.getIsNew());
			pstmt.setString(15, documentBean.getAuthor());
			pstmt.setString(16, documentBean.getFlowTypeCode());
			pstmt.setTimestamp(17, new Timestamp(new Date().getTime()));
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
