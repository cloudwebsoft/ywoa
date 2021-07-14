package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import cn.js.fan.db.Conn;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.CooperateBean;
import com.redmoon.clouddisk.bean.KeyBean;
import com.redmoon.clouddisk.bean.UserBean;
import com.redmoon.clouddisk.socketServer.ServerWorker;
import com.redmoon.clouddisk.tools.ToolsUtil;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;

/**
 * @author 古月圣
 * 
 */
public class UserDb {

	private UserBean userBean;

	private String connname;

	public final static byte FAILED_NOTEXISTS = 0x01;
	public final static byte FAILED_PWDERROR = 0x02;
	public final static byte FAILED_INVALID = 0x03;

	/**
	 * @param userBean
	 */
	public UserDb(UserBean userBean) {
		this.userBean = userBean;
		connname = Global.getDefaultDB();
	}

	// 查询密码
	/**
	 * @return
	 */
	public boolean getUserPwd() {
		com.redmoon.oa.person.UserDb user = new com.redmoon.oa.person.UserDb();
		user = user.getUserDb(userBean.getName());
		if (!user.isLoaded()) {
			AccountDb ad = new AccountDb();
			ad = ad.getAccountDb(userBean.getName());
			if (ad.isLoaded()) {
				user = user.getUserDb(ad.getUserName());
			} else {
				userBean.setFailedReason(FAILED_NOTEXISTS);
				return false;
			}
		} else if (!user.isValid()) {
			userBean.setFailedReason(FAILED_INVALID);
			return false;
		}

		userBean.setName(user.getName());
		userBean.setPwd(user.getPwdMD5());
		userBean.setRealName(user.getRealName());
		userBean.setGender((short) user.getGender());
		userBean.setDiskSpace(user.getDiskSpaceAllowed() > 0 ? user
				.getDiskSpaceAllowed() : user.getDiskQuota());

		KeyBean keyBean = new KeyBean();
		KeyDb keyDb = new KeyDb(keyBean);
		keyBean.setUserName(user.getName());
		int res = keyDb.getCurrentKey();

		if (res != KeyDb.KEY_CORRECT) {
			// keyBean.setKey(ToolsUtil.randomString(KeyDb.KEY_LENGTH,
			// ToolsUtil.CASE_SENSITIVE));
			String key = ToolsUtil.randomString(KeyDb.KEY_LENGTH,
					ToolsUtil.CASE_SENSITIVE);
			keyBean.setKey(key + key + key);
		}
		keyBean.setUpdateDate(new Date());
		return keyDb.update();
	}

	// 查询所属和管辖部门和人员的全部信息,用于生成客户端文件协作的组织机构树
	/**
	 * @return
	 */
	public String getUserDept(String visualPath) {
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();

		try {
			String sql = "select dept_code from dept_user where user_name="
					+ StrUtil.sqlstr(userBean.getName()) + " order by orders";
			rs = conn.executeQuery(sql);
			while (rs.next()) {
				String deptCode = rs.getString("dept_code");
				if (deptCode != null && !deptCode.equals("")) {
					list.add(deptCode);
				}
			}
			rs.close();

			sql = "select deptcode from user_admin_dept where username="
					+ StrUtil.sqlstr(userBean.getName()) + " order by id";
			rs = conn.executeQuery(sql);
			while (rs.next()) {
				String deptCode = rs.getString("deptcode");
				if (deptCode != null && !deptCode.equals("")) {
					if (!list.contains(deptCode)) {
						list.add(deptCode);
					}
				}
			}
			rs.close();

			for (String code : list) {
				sql = "select name,parentcode from department where code="
						+ StrUtil.sqlstr(code);
				rs = conn.executeQuery(sql);
				while (rs.next()) {
					sb.append(code).append(ServerWorker.FIRST_SEPT).append(
							StrUtil.getNullStr(rs.getString("name"))).append(
							ServerWorker.FIRST_SEPT).append(
							StrUtil.getNullStr(rs.getString("parentcode")))
							.append(ServerWorker.FIRST_SEPT);
				}
				rs.close();
				sql = "select name,realname from dept_user,users where user_name=name and dept_code="
						+ StrUtil.sqlstr(code)
						+ " and name<>"
						+ StrUtil.sqlstr(userBean.getName());
				rs = conn.executeQuery(sql);
				while (rs.next()) {
					sb.append(StrUtil.getNullStr(rs.getString("name"))).append(
							ServerWorker.SECOND_SEPT).append(
							StrUtil.getNullStr(rs.getString("realname")))
							.append(ServerWorker.SECOND_SEPT);

					CooperateBean coBean = new CooperateBean();
					coBean.setVisualPath(visualPath);
					coBean.setUserName(rs.getString("name"));
					coBean.setShareUser(userBean.getName());
					CooperateDb coDb = new CooperateDb(coBean);

					sb.append(coDb.isShared()).append(ServerWorker.SECOND_SEPT);
				}
				sb.append(ServerWorker.LAST_SEPT);
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return sb.toString();
	}

	/**
	 * @Description: 查询文件夹协作设置中所需的组织架构树
	 * @param visualPath
	 * @return
	 */
	public String getAllDepts(String visualPath) {
		StringBuilder sb = new StringBuilder();
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();

		DeptDb dd = new DeptDb();
		Vector v1 = dd
				.list("select * from department order by layer asc,orders asc");
		Iterator it1 = v1.iterator();
		while (it1.hasNext()) {
			DeptDb dDb = (DeptDb) it1.next();
			DeptUserDb dud = new DeptUserDb();
			Vector v2 = dud.list(dDb.getCode());
			Iterator it2 = v2.iterator();
			StringBuilder sb2 = new StringBuilder();
			while (it2.hasNext()) {
				DeptUserDb duDb = (DeptUserDb) it2.next();
				if (map.containsKey(duDb.getUserName())) {
					continue;
				}
				sb2.append(duDb.getUserName()).append(ServerWorker.SECOND_SEPT)
						.append(duDb.getUserRealName()).append(
								ServerWorker.SECOND_SEPT);

				map.put(duDb.getUserName(), true);

				CooperateBean coBean = new CooperateBean();
				coBean.setVisualPath(visualPath);
				coBean.setUserName(duDb.getUserName());
				coBean.setShareUser(userBean.getName());
				CooperateDb coDb = new CooperateDb(coBean);

				sb2.append(coDb.isShared()).append(ServerWorker.SECOND_SEPT);
			}
			if (!sb2.toString().equals("")) {
				sb.append(dDb.getCode()).append(ServerWorker.FIRST_SEPT)
						.append(dDb.getName()).append(ServerWorker.FIRST_SEPT)
						.append(dDb.getParentCode()).append(
								ServerWorker.FIRST_SEPT).append(sb2.toString())
						.append(ServerWorker.LAST_SEPT);
			} else if (dDb.getChildCount() > 0) {
				sb.append(dDb.getCode()).append(ServerWorker.FIRST_SEPT)
						.append(dDb.getName()).append(ServerWorker.FIRST_SEPT)
						.append(dDb.getParentCode()).append(
								ServerWorker.FIRST_SEPT).append(
								ServerWorker.SECOND_SEPT).append(
								ServerWorker.SECOND_SEPT).append(
								ServerWorker.SECOND_SEPT).append(
								ServerWorker.LAST_SEPT);
			}
		}
		return sb.toString();
	}

	// 查询所属部门
	/**
	 * @return
	 */
	public boolean getDeptName() {
		Conn conn = new Conn(connname);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String deptName = "";
		String parent = "";

		try {
			String sql = "select name,parentcode from dept_user,department where user_name=? and code=dept_code";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userBean.getName());
			rs = stmt.executeQuery();
			while (rs.next()) {
				deptName = rs.getString("name");
				parent = rs.getString("parentcode");
			}
			rs.close();
			if (!parent.equals(ServerWorker.ROOT)) {
				sql = "select name from department where code=?";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, parent);
				rs = stmt.executeQuery();
				while (rs.next()) {
					deptName = rs.getString("name") + "-" + deptName;
				}
			}
			userBean.setDeptName(deptName);
			return true;
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
