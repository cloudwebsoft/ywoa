package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import cn.js.fan.db.Conn;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.bean.DirectoryBean;
import com.redmoon.oa.netdisk.CooperateMgr;
import com.redmoon.oa.netdisk.Directory;
import com.redmoon.oa.netdisk.Leaf;

public class DirectoryDb {

	private DirectoryBean directoryBean;

	private String connname;

	/**
	 * 
	 */
	public DirectoryDb() {
		super();
		connname = Global.getDefaultDB();
	}

	/**
	 * @param directoryBean
	 */
	public DirectoryDb(DirectoryBean directoryBean) {
		this.directoryBean = directoryBean;
		connname = Global.getDefaultDB();
	}

	/**
	 * @return
	 */
	public boolean isExist() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select code from netdisk_directory where parent_code=? and name=? and isDeleted=0";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, directoryBean.getParentCode());
			pstmt.setString(2, directoryBean.getName());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				directoryBean.setCode(rs.getString("code"));
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
		String sql = "insert into netdisk_directory (code,name,parent_code,description,orders,root_code,child_count,layer,type,add_date,doc_id) values (?,?,?,?,?,?,?,?,?,?,?)";
		boolean re = false;
		try {
			// 插入文章标题及相关设置
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, directoryBean.getCode());
			pstmt.setString(2, directoryBean.getName());
			pstmt.setString(3, directoryBean.getParentCode());
			pstmt.setString(4, directoryBean.getDescription());
			pstmt.setInt(5, directoryBean.getOrders());
			pstmt.setString(6, directoryBean.getRootCode());
			pstmt.setInt(7, 0);
			pstmt.setInt(8, directoryBean.getLayer());
			pstmt.setInt(9, directoryBean.getType());
			pstmt.setTimestamp(10, new Timestamp(new Date().getTime()));
			pstmt.setLong(11, directoryBean.getDocId());
			re = pstmt.executeUpdate() > 0 ? true : false;
			if(re){
				CooperateMgr cm = new CooperateMgr();
				cm.isParentFolderRefused(directoryBean.getCode());
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

	public boolean update() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_directory set child_count=child_count+1 where code=?";
		boolean re = false;
		try {
			// 插入文章标题及相关设置
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, directoryBean.getParentCode());
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
	public boolean getOrders() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean re = false;
		String sql = "select max(orders) from netdisk_directory where parent_code=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, directoryBean.getParentCode());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				directoryBean.setOrders(rs.getInt("max(orders)") + 1);
				return true;
			} else {
				directoryBean.setOrders(0);
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
		return re;
	}

	/**
	 * @Description: 根据路径取对应的dir_code
	 * @return
	 */
	public String getDirCodeByPath(String path) {
		String[] dirs = path.split("/", 0);
		directoryBean.setParentCode(dirs[0]);
		String dirCode = "";

		for (int i = 1; i < dirs.length; i++) {
			directoryBean.setName(dirs[i]);
			if (!isExist()) {
				return null;
			}
			directoryBean.setParentCode(directoryBean.getCode());
			dirCode = directoryBean.getCode();
		}
		return dirCode;
	}

	/**
	 * @Description: 获取全部个人文件夹路径(不再使用)
	 * @return
	 */
	public String getAllDirs2() {
		Leaf leaf = new Leaf(directoryBean.getRootCode());
		Vector vec = new Vector();
		try {
			vec = leaf.getAllChild(vec, leaf);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		Iterator it = vec.iterator();
		StringBuilder sb = new StringBuilder();
		HashMap<String, String> map = new HashMap<String, String>();
		while (it.hasNext()) {
			Leaf lf = (Leaf) it.next();
			String visualPath = getDirPathWithoutRoot(lf);
			// sb.append(visualPath).append(it.hasNext() ? ";" : "");
			map.put(visualPath, lf.getAddDate());
		}
		vec = new Vector();
		try {
			vec = leaf.getAllDelChild2(vec, leaf);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		it = vec.iterator();
		// if (!sb.toString().equals("") && it.hasNext()) {
		// sb.append(";");
		// }
		while (it.hasNext()) {
			Leaf lf = (Leaf) it.next();
			String visualPath = getDirPathWithoutRoot(lf);
			if (map.containsKey(visualPath)) {
				String date = map.get(visualPath);
				// 当前文件夹新建的日期>=被删除的文件夹的新建日期,则认为文件夹的当前状态为正常状态,否则为删除状态
				if (date.compareTo(lf.getAddDate()) < 0) {
					map.remove(visualPath);
					map.put("|" + visualPath, lf.getAddDate());
				}
			} else {
				map.put("|" + visualPath, lf.getAddDate());
			}
			// sb.append("|").append(visualPath).append(it.hasNext() ? ";" :
			// "");
		}

		// 遍历map
		int i = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			sb.append(key).append(++i < map.size() ? ";" : "");
		}
		return sb.toString();
	}

	/**
	 * @Description: 获取全部个人文件夹路径
	 * @return
	 */
	public String getAllDirs() {
		String sql = "select code from netdisk_directory where root_code="
				+ StrUtil.sqlstr(directoryBean.getRootCode());
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		// String[] 为二维数组,分别记录日期和删除状态
		HashMap<String, String[]> map = new HashMap<String, String[]>();

		try {
			ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				String code = rr.getString(1);
				Leaf leaf = new Leaf(code);
				String visualPath = getDirPathWithoutRoot(leaf);
				if (map.containsKey(visualPath)) {
					String[] info = map.get(visualPath);
					// 始终以最新的日期为当前文件夹
					if (info[0].compareTo(leaf.getAddDate()) < 0) {
						info[0] = leaf.getAddDate();
						info[1] = leaf.isDeleted() ? "|" : "";
						map.put(visualPath, info);
					}
				} else {
					String[] info = new String[] { leaf.getAddDate(),
							leaf.isDeleted() ? "|" : "" };
					map.put(visualPath, info);
				}

			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}

		StringBuilder sb = new StringBuilder();
		// 遍历map
		int i = 0;
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			String key = entry.getKey();
			String[] val = entry.getValue();
			sb.append(val[1] + key).append(++i < map.size() ? ";" : "");
		}
		return sb.toString();
	}

	/**
	 * @Description: 提交个人文件夹
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean setDir() throws ErrMsgException {
		String[] dirs = directoryBean.getName().split("/", 0);
		directoryBean.setParentCode(directoryBean.getRootCode());
		boolean re = true;

		Leaf leaf = new Leaf(directoryBean.getRootCode());
		if (leaf == null || !leaf.isLoaded()) {
			leaf = new Leaf();
			leaf.initRootOfUser(directoryBean.getRootCode());
		}

		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].equals("")) {
				continue;
			}
			directoryBean.setName(dirs[i]);
			if (!isExist()) {
				Directory dir = new Directory();
				dir.setName(directoryBean.getName());
				dir.setParentCode(directoryBean.getParentCode());
				dir.setType(Leaf.TYPE_DOCUMENT);
				dir.setUserName(directoryBean.getRootCode());
				re = dir.AddChild();
				CooperateMgr cm = new CooperateMgr();
				cm.isParentFolderRefused(dir.getCode());
				if (!re) {
					return false;
				}
			}
			directoryBean.setParentCode(directoryBean.getCode());
		}
		return re;
	}

	/**
	 * @Description: 取得文件夹相对路径路径
	 * @return
	 */
	public String getDirPathWithoutRoot(Leaf leaf) {
		// 取得文件虚拟路径
		String parentcode = leaf.getParentCode();
		Leaf plf = new Leaf();
		String filePath = "";
		if (!parentcode.equals("-1")) {
			// 非根目录取节点名称
			filePath = leaf.getName();
			while (!parentcode.equals(leaf.getRootCode())) {
				plf = plf.getLeaf(parentcode);
				if (plf == null) {
					break;
				}
				parentcode = plf.getParentCode();
				filePath = plf.getName() + "\\" + filePath;
			}
		}
		return filePath;
	}
}
