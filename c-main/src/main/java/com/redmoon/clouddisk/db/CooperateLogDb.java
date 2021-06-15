package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.CooperateLogBean;
import com.redmoon.clouddisk.socketServer.ServerWorker;
import com.redmoon.oa.netdisk.Leaf;
import com.redmoon.oa.person.UserDb;

/**
 * @author 古月圣
 * 
 */
public class CooperateLogDb {

	private CooperateLogBean cooperateLogBean;

	private String connname;

	/**
	 * @param cooperateBean
	 */
	public CooperateLogDb(CooperateLogBean cooperateLogBean) {
		this.cooperateLogBean = cooperateLogBean;
		connname = Global.getDefaultDB();
	}

	public boolean create() {
		Conn conn = new Conn(connname);
		String sql = "insert into netdisk_cooperate_log (dir_code,user_name,action,action_date,action_name) value (?,?,?,?,?)";
		PreparedStatement pstm = null;
		boolean re = false;

		try {
			pstm = conn.prepareStatement(sql);
			pstm.setString(1, cooperateLogBean.getDirCode());
			pstm.setString(2, cooperateLogBean.getUserName());
			pstm.setInt(3, cooperateLogBean.getAction());
			pstm.setTimestamp(4, new Timestamp((new Date()).getTime()));
			pstm.setString(5, cooperateLogBean.getActionName());

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
	 * @Description:
	 * @return
	 */
	public boolean delete() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "delete from netdisk_cooperate_log where dir_code=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil
					.getNullStr(cooperateLogBean.getDirCode()));
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

	/**
	 * @Description:
	 * @return
	 */
	public boolean deleteSingle() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "delete from netdisk_cooperate_log where id=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, cooperateLogBean.getId());
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

	public ArrayList<String> list() {
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		int limits = cfg.getIntProperty("shareLogCount");
		if (limits < 0) {
			limits = 20;
		}

		String user = "";
		String sql = "select dir_code,user_name,action,action_date,action_name from netdisk_cooperate_log";
		if (cooperateLogBean.getUserName() != null
				&& !cooperateLogBean.getUserName().equals("")) {
			user = cooperateLogBean.getUserName();
			sql += " where dir_code in ("
					+ "select dir_code from netdisk_cooperate where user_name="
					+ StrUtil.sqlstr(user)
					+ " and is_refused=2"
					+ ")"
					+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
							+ limits
							: "")
					+ " order by action_date desc"
					+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
							+ limits : "");
		} else {
			sql += " where dir_code="
					+ StrUtil.sqlstr(cooperateLogBean.getDirCode())
					+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
							+ limits
							: "")
					+ " order by action_date desc"
					+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
							+ limits : "");
		}
		JdbcTemplate jt = new JdbcTemplate();
		ArrayList<String> li = new ArrayList<String>();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int action = rr.getInt("action");
				String actionName = rr.getString("action_name");
				Leaf leaf = new Leaf(rr.getString("dir_code"));
				String visualPath = "";
				if (user.equals(leaf.getRootCode())) {
					// 发起协作文件
					if (actionName.startsWith(user)) {
						visualPath = actionName.substring(user.length());
						if (!visualPath.startsWith("/")) {
							visualPath = "/" + visualPath;
						}
					} else {
						// 旧数据
						if (action == CooperateLogBean.ACTION_START
								|| action == CooperateLogBean.ACTION_JOININ
								|| action == CooperateLogBean.ACTION_REFUSE) {
							visualPath = leaf.getFilePath().substring(
									user.length());
							if (!visualPath.startsWith("/")) {
								visualPath = "/" + visualPath;
							}
						} else {
							visualPath = "/";
						}
					}
				} else {
					// 参与协作文件
					if (actionName.startsWith(leaf.getRootCode())) {
						visualPath = new UserDb(leaf.getRootCode())
								.getRealName()
								+ "@" + actionName;
					} else {
						if (action == CooperateLogBean.ACTION_START
								|| action == CooperateLogBean.ACTION_JOININ
								|| action == CooperateLogBean.ACTION_REFUSE) {
							visualPath = leaf.getFilePath().substring(
									leaf.getRootCode().length());
							if (!visualPath.startsWith("/")) {
								visualPath = "/" + visualPath;
							}
						} else {
							visualPath = "/";
						}
					}
				}
				actionName = actionName
						.substring(actionName.lastIndexOf("/") + 1);
				// 非文件夹
				if (!visualPath.endsWith("/")
						&& action != CooperateLogBean.ACTION_START
						&& action != CooperateLogBean.ACTION_JOININ
						&& action != CooperateLogBean.ACTION_REFUSE) {
					visualPath = visualPath.substring(0, visualPath.length()
							- actionName.length() - 1);
				}
				String userName = rr.getString("user_name");
				String realName = "";
				if (userName.equals(cooperateLogBean.getUserName())) {
					realName = "我";
				} else {
					UserDb ud = new UserDb(userName);
					realName = ud.getRealName();
				}
				String actionDate = DateUtil.format(rr.getDate("action_date"),
						"yyyy-MM-dd HH:mm:ss");
				StringBuilder sb = new StringBuilder();
				sb.append(actionDate).append(ServerWorker.LAST_SEPT).append(
						realName).append(ServerWorker.LAST_SEPT).append(action)
						.append(ServerWorker.LAST_SEPT).append(actionName)
						.append(ServerWorker.LAST_SEPT).append(visualPath);
				li.add(sb.toString());
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return li;
	}
}