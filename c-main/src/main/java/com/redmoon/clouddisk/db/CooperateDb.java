package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.CooperateBean;
import com.redmoon.clouddisk.bean.CooperateLogBean;
import com.redmoon.clouddisk.bean.DirectoryBean;
import com.redmoon.clouddisk.socketServer.ServerWorker;
import com.redmoon.clouddisk.socketServer.SocketMgr;
import com.redmoon.oa.netdisk.Attachment;
import com.redmoon.oa.netdisk.Document;
import com.redmoon.oa.netdisk.Leaf;

/**
 * @author 古月圣
 * 
 */
public class CooperateDb {

	private CooperateBean cooperateBean;

	private String connname;

	private static int IS_SHARE = 1;
	private static int RELEASE_SHARE = 0;

	/**
	 * @param cooperateBean
	 */
	public CooperateDb(CooperateBean cooperateBean) {
		this.cooperateBean = cooperateBean;
		connname = Global.getDefaultDB();
	}

	public boolean create() {
		Conn conn = new Conn(connname);
		String sql = "insert into netdisk_cooperate (dir_code,user_name,share_user,cooperate_date,visual_path,is_refused) value (?,?,?,?,?,?)";
		PreparedStatement pstm = null;
		boolean re = false;

		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, cooperateBean.getDirCode());
			pstm.setString(2, cooperateBean.getUserName());
			pstm.setString(3, cooperateBean.getShareUser());
			pstm.setTimestamp(4, new Timestamp((new Date()).getTime()));
			pstm.setString(5, cooperateBean.getVisualPath());
			pstm.setInt(6, cooperateBean.getUserName().equals(
					cooperateBean.getShareUser()) ? 2 : 0);

			re = pstm.executeUpdate() == 1 ? true : false;
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
	 * @Description: 将用户某个文件夹下的所有文件让其他人协作
	 * @return
	 */
	public void cooperate() {
		if (!updateShareDir(IS_SHARE)) {
			return;
		}

		String cooperateUser = cooperateBean.getShareUser();
		cooperateBean.setShareUser(cooperateBean.getUserName());

		// 把自己加入协作组
		cooperateBean.setIsRefused(2);
		boolean re = false;

		if (canBeShared()) {
			re = create();

			// 协作动态(日志)
			if (re) {
				CooperateLogBean cologBean = new CooperateLogBean();
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(cooperateBean.getUserName());
				cologBean.setAction(CooperateLogBean.ACTION_START);
				cologBean.setActionDate(new Date());
				cologBean.setActionName(cooperateBean.getVisualPath()
						+ (cooperateBean.getVisualPath().endsWith("/") ? ""
								: "/"));
				CooperateLogDb cologDb = new CooperateLogDb(cologBean);
				cologDb.create();
			}
		}

		cooperateBean.setIsRefused(0);
		ArrayList<CooperateBean> list = listAllBeShared();
		String[] shareTemp = cooperateUser.split(String
				.valueOf(ServerWorker.LAST_SEPT));
		for (int i = 0; i < shareTemp.length; i++) {
			// 本人已在上面处理了
			if (shareTemp[i].equals(cooperateBean.getUserName())) {
				continue;
			}
			cooperateBean.setUserName(shareTemp[i]);
			boolean inCooperation = false;
			for (CooperateBean coBean : list) {
				if (coBean.getUserName().equals(shareTemp[i])) {
					inCooperation = true;
					// 已经拒绝,重新邀请加入协作
					if (coBean.getIsRefused() == 1) {
						cooperateBean.setId(coBean.getId());
						update();
					}
					list.remove(coBean);
					break;
				}
			}

			// 如果未参加协作则邀请加入
			if (!inCooperation) {
				if (canBeShared()) {
					create();
				} else if (cooperateBean.getIsRefused() != 2) {
					cooperateBean.setIsRefused(0);
					update();
				}
			}
		}

		// list中剩余的元素为被解除协作的人
		for (CooperateBean coBean : list) {
			cooperateBean.setUserName(coBean.getUserName());
			re = delete();
		}
	}

	/**
	 * @Description: 根据路径取对应的dir_code
	 * @return
	 */
	public boolean getDirCodeByPath() {
		String[] dirs = cooperateBean.getVisualPath().split("/", 0);
		DirectoryBean directoryBean = new DirectoryBean();
		directoryBean.setParentCode(cooperateBean.getUserName());

		for (int i = 1; i < dirs.length; i++) {
			directoryBean.setName(dirs[i]);
			DirectoryDb directoryDb = new DirectoryDb(directoryBean);
			if (!directoryDb.isExist()) {
				return false;
			}
			directoryBean.setParentCode(directoryBean.getCode());
			cooperateBean.setDirCode(directoryBean.getCode());
		}
		return true;
	}

	/**
	 * @Description: 设置netdisk_directory中的文件夹为协作文件夹
	 */
	public boolean updateShareDir(int isShare) {
		if (!getDirCodeByPath()) {
			return false;
		}

		Conn conn = new Conn(connname);
		String sql = "update netdisk_directory set isShared=" + isShare
				+ " where code=" + StrUtil.sqlstr(cooperateBean.getDirCode());
		boolean re = false;

		try {
			re = conn.executeUpdate(sql) == 1 ? true : false;
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
	 * @Description: 更新协作状态，是否接收
	 * @return
	 */
	public boolean update() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_cooperate set is_refused=? where id=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, cooperateBean.getIsRefused());
			pstmt.setLong(2, cooperateBean.getId());
			re = pstmt.executeUpdate() == 1 ? true : false;

			if (re && cooperateBean.getIsRefused() != 0) {
				load();
				CooperateLogBean cologBean = new CooperateLogBean();
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(cooperateBean.getUserName());
				cologBean
						.setAction(cooperateBean.getIsRefused() == 1 ? CooperateLogBean.ACTION_REFUSE
								: CooperateLogBean.ACTION_JOININ);
				cologBean.setActionDate(new Date());
				cologBean.setActionName(cooperateBean.getVisualPath()
						+ (cooperateBean.getVisualPath().endsWith("/") ? ""
								: "/"));
				CooperateLogDb cologDb = new CooperateLogDb(cologBean);
				cologDb.create();
			}
			if (re && cooperateBean.getIsRefused() == 2) {
				com.redmoon.oa.netdisk.LeafPriv lp = new com.redmoon.oa.netdisk.LeafPriv();
				try {
					lp.setDirCode(cooperateBean.getDirCode());
					lp.add(cooperateBean.getUserName(), 1);
				} catch (Exception e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				}
			}
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
	 * @Description: 解除某人的协作,visual_path,user_name,share_user确定唯一对象
	 * @return
	 */
	public boolean delete() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "delete from netdisk_cooperate where visual_path=? and user_name=? and share_user=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil
					.getNullStr(cooperateBean.getVisualPath()));
			pstmt.setString(2, StrUtil.getNullStr(cooperateBean.getUserName()));
			pstmt
					.setString(3, StrUtil.getNullStr(cooperateBean
							.getShareUser()));
			re = pstmt.executeUpdate() == 1 ? true : false;

			if (re) {
				CooperateLogBean cologBean = new CooperateLogBean();
				cologBean.setDirCode(cooperateBean.getDirCode());
				cologBean.setUserName(cooperateBean.getUserName());
				cologBean.setAction(CooperateLogBean.ACTION_CANCEL);
				cologBean.setActionDate(new Date());
				cologBean.setActionName(cooperateBean.getVisualPath()
						+ (cooperateBean.getVisualPath().endsWith("/") ? ""
								: "/"));
				CooperateLogDb cologDb = new CooperateLogDb(cologBean);
				cologDb.create();
			}
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
	 * @Description: 解除某文档的协作
	 * @return
	 */
	public boolean release() {
		boolean re = false;
		if (!getDirCodeByPath()) {
			re = true;
		} else {
			if (updateShareDir(RELEASE_SHARE)) {
				Conn conn = new Conn(connname);
				PreparedStatement pstmt = null;
				String sql = "delete from netdisk_cooperate where dir_code=?";
				try {
					pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, StrUtil.getNullStr(cooperateBean
							.getDirCode()));
					re = pstmt.executeUpdate() >= 1 ? true : false;
				} catch (SQLException e) {
					LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				} finally {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				}
			}
		}

		if (re) {
			// 删除协作日志
			CooperateLogBean cologBean = new CooperateLogBean();
			cologBean.setDirCode(cooperateBean.getDirCode());
			CooperateLogDb cologDb = new CooperateLogDb(cologBean);
			re = cologDb.delete();
		}
		return re;
	}

	/**
	 * 当客户端点击分享按钮以后，服务器端判断文件是否已经分享给某人,或者某人已拒绝
	 * 
	 * @return true can be shared, false has been shared
	 */
	public boolean canBeShared() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select id,is_refused from netdisk_cooperate where visual_path=? and user_name=? and share_user=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil
					.getNullStr(cooperateBean.getVisualPath()));
			pstmt.setString(2, StrUtil.getNullStr(cooperateBean.getUserName()));
			pstmt
					.setString(3, StrUtil.getNullStr(cooperateBean
							.getShareUser()));
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cooperateBean.setId(rs.getLong("id"));
				cooperateBean.setIsRefused(rs.getInt("is_refused"));
				return false;
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

	/**
	 * @Description: 客户端获取已经被邀请参与协作的人
	 * @return: 被邀请者拒绝与否 0 被邀请未操作 1 被邀请已拒绝 2 被邀请已接收 3 未邀请
	 */
	public int isShared() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select id,is_refused from netdisk_cooperate where visual_path=? and user_name=? and share_user=?";
		int isRefused = 3;

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil
					.getNullStr(cooperateBean.getVisualPath()));
			pstmt.setString(2, StrUtil.getNullStr(cooperateBean.getUserName()));
			pstmt
					.setString(3, StrUtil.getNullStr(cooperateBean
							.getShareUser()));
			rs = pstmt.executeQuery();
			if (rs.next()) {
				isRefused = rs.getInt("is_refused");
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return isRefused;
	}

	/**
	 * @Description: 获取所有我参与的协作的文件夹集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> listSharedFile() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		// is_refused 0未操作 1已拒收 2已接收
		// String sql =
		// "select att.id, share_id,share_user,realname,att.name,is_refused,is_deleted,version_date from netdisk_document_attach att,users where user_name=? and is_share=1 and is_deleted=0 and is_refused<>1 and share_user=users.name";
		String sql = "select id,dir_code,is_refused from netdisk_cooperate where user_name=? and is_refused<>1 and share_user<>?";
		// 去掉默认的路径:用户名/我的网盘(如果有的话)
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(cooperateBean.getUserName()));
			pstmt.setString(2, StrUtil.getNullStr(cooperateBean.getUserName()));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				// 字符串结构:netdisk_cooperate表id;用户名;用户姓名;协作文件夹名;是否拒收;文件夹中文件id1'文件名'文件相对路径'文件版本时间'是否删除;id2'文件名'文件相对路径'文件版本时间'是否删除;...
				StringBuilder sb = new StringBuilder();
				Leaf leaf = new Leaf(rs.getString("dir_code"));
				com.redmoon.oa.person.UserDb userDb = new com.redmoon.oa.person.UserDb(
						leaf.getRootCode());

				String defaultPath = userDb.getName()
						+ (cfg.getBooleanProperty("isCloudDiskRoot") ? "" : "/"
								+ SocketMgr.MYDISK + "/");

				sb.append(rs.getLong("id")).append(ServerWorker.LAST_SEPT)
						.append(userDb.getName())
						.append(ServerWorker.LAST_SEPT).append(
								userDb.getRealName()).append(
								ServerWorker.LAST_SEPT).append(leaf.getName())
						.append(ServerWorker.LAST_SEPT).append(
								rs.getInt("is_refused")).append(
								ServerWorker.LAST_SEPT);
				Vector docs = leaf.getChildren();
				docs.add(leaf);
				for (int i = 0; i < docs.size(); i++) {
					Leaf lf = (Leaf) docs.get(i);
					Document doc = new Document(lf.getDocId());
					Vector atts = doc.getAttachments(1);
					for (int j = 0; j < atts.size(); j++) {
						Attachment att = (Attachment) atts.get(j);

						// 去掉默认的路径:用户名/我的网盘(如果有的话)
						String visualPath = att.getVisualPath();
						if (visualPath.indexOf(defaultPath) == 0) {
							visualPath = visualPath.substring(defaultPath
									.length());
						}

						sb.append(att.getId()).append(ServerWorker.FIRST_SEPT)
								.append(att.getName()).append(
										ServerWorker.FIRST_SEPT).append(
										visualPath).append(
										ServerWorker.FIRST_SEPT).append(
										DateUtil.format(att.getVersionDate(),
												"yyyy-MM-dd HH:mm:ss")).append(
										ServerWorker.FIRST_SEPT).append(
										att.isDeleted()).append(
										ServerWorker.LAST_SEPT);
					}
				}
				/*
				 * sb.append(rs.getString("id")).append(ServerWorker.LAST_SEPT)
				 * .append(rs.getString("share_id")).append(
				 * ServerWorker.LAST_SEPT).append(
				 * rs.getString("share_user")).append(
				 * ServerWorker.LAST_SEPT).append(
				 * rs.getString("realname")).append(
				 * ServerWorker.LAST_SEPT).append( rs.getString("name")).append(
				 * ServerWorker.LAST_SEPT).append( DateUtil.format(
				 * rs.getTimestamp("version_date"),
				 * "yyyy-MM-dd HH:mm:ss")).append(
				 * ServerWorker.LAST_SEPT).append(
				 * rs.getString("is_refused")).append(
				 * ServerWorker.LAST_SEPT).append( rs.getString("is_deleted"));
				 */
				list.add(sb.toString());
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
	 * @Description: 获取已经被邀请参加协作的所有人
	 * @return
	 */
	public ArrayList<CooperateBean> listAllBeShared() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<CooperateBean> list = new ArrayList<CooperateBean>();
		String sql = "select id,user_name,is_refused from netdisk_cooperate where visual_path=? and share_user=? and user_name<>?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil
					.getNullStr(cooperateBean.getVisualPath()));
			pstmt.setString(2, StrUtil.getNullStr(cooperateBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(cooperateBean.getUserName()));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				CooperateBean coBean = new CooperateBean();
				coBean.setId(rs.getLong("id"));
				coBean.setUserName(rs.getString("user_name"));
				coBean.setIsRefused(rs.getInt("is_refused"));
				list.add(coBean);
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
	 * @Description: 根据id加载协作信息
	 * @return
	 */
	public void load() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select dir_code,user_name,share_user,is_refused,cooperate_date,visual_path from netdisk_cooperate where id=?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, cooperateBean.getId());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				cooperateBean.setDirCode(rs.getString("dir_code"));
				cooperateBean.setUserName(rs.getString("user_name"));
				cooperateBean.setShareUser(rs.getString("share_user"));
				cooperateBean.setIsRefused(rs.getInt("is_refused"));
				cooperateBean.setCooperateDate(rs
						.getTimestamp("cooperate_date"));
				cooperateBean.setVisualPath(rs.getString("visual_path"));
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}
}