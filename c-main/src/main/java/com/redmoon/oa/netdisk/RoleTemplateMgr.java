package com.redmoon.oa.netdisk;

import java.io.File;

import net.sf.json.JSONObject;

import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.RoleDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class RoleTemplateMgr {
	private RoleTemplateDb roleTemplateDb;

	public RoleTemplateMgr() {
		super();
		roleTemplateDb = new RoleTemplateDb();
		// TODO Auto-generated constructor stub
	}

	int id;
	String roleCode;
	String dirCode;
	String visualPath;
	String connname;
	transient Logger logger = Logger.getLogger(RoleTemplateMgr.class.getName());

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}

	public String getDirCode() {
		return dirCode;
	}

	public void setDirCode(String dirCode) {
		this.dirCode = dirCode;
	}

	public String getVisualPath() {
		return visualPath;
	}

	public void setVisualPath(String visualPath) {
		this.visualPath = visualPath;
	}

	public RoleTemplateDb getRoleTempletDB(long id) {
		RoleTemplateDb addr = new RoleTemplateDb();
		long theID = (long) id;
		return addr.getRoleTempletDb(theID);
	}

	// /**
	// * @return 上传模板
	// */
	// public boolean updateTemplate() {
	// boolean re = false;
	// Leaf leaf = new Leaf(dirCode);
	// Config config = new Config();
	// String filePath = Global.getRealPath() + config.get("file_netdisk")
	// + "/";
	// String visualFullPath = leaf.getFullPath(leaf.getCode());
	// String templetPath = Global.getRealPath()
	// + config.get("file_role_template") + "/";
	//
	// visualPath = roleCode + "/" + leaf.getName();
	// File file = new File(filePath + visualFullPath);
	// File fl = new File(templetPath + roleCode + "/" + leaf.getName());
	// try {
	// if (isExist()) {
	// if (fl.exists()) {
	// FileUtil.del(fl.getAbsolutePath()); // 如果存在，则删除原文件夹
	// // if(style.equals("oa")){
	// // leaf = new Leaf(dirCode);
	// // int docId = leaf.getDocId();
	// // re = leaf.remove(dirCode); //删除文件家数据
	// // Attachment att = new Attachment();
	// // att.docId = docId;
	// // re = att.getIdByDocId(); //删除文件数据
	// // }
	// }
	// re = update();//修改表里数据
	//
	// // 删除对应的下载表里的数据
	// RoleTemplateDownloadMgr rtdMgr = new RoleTemplateDownloadMgr();
	// rtdMgr.setRtId(id);
	// rtdMgr.delByRtId();
	// } else {
	//
	// re = roleTemplateDb.create();
	// }
	// fl.mkdirs(); // 新建文件夹
	// UtilTools.copyDir(file, fl);
	// } catch (Exception e) {
	// logger.error("updateTemplet: " + e.getMessage());
	// re = false;
	// }
	// return re;
	// }
	//
	// /**
	// * @return OA里的角色模板上传方法
	// * @throws ErrMsgException
	// */
	// public boolean uploadRoleTemplate(HttpServletRequest request)
	// throws ErrMsgException {
	// boolean re = false;
	// String roleCodes = ParamUtil.get(request, "ids");
	// String rootCode = ParamUtil.get(request, "root_code");
	// Leaf leaf = new Leaf();
	// leaf.setRootCode(rootCode);
	// String dirCode = leaf.getNewestCode(); // 自定义方法
	// if (!dirCode.equals("")) {
	// String[] ary = StrUtil.split(roleCodes, ",");
	// if (ary == null)
	// return re;
	// leaf = new Leaf(dirCode);
	// for (int i = 0; i < ary.length; i++) {
	// String roleCode = ary[i];
	// RoleTemplateMgr rt = new RoleTemplateMgr();
	// rt.setDirCode(dirCode);
	// rt.setRoleCode(roleCode);
	// re = rt.updateTemplate();
	// }
	// } else {
	// return false;
	// }
	// return re;
	// }

	// /**
	// * @return 上传模板前判断模板是否存在
	// */
	// public boolean isExist() {
	// String sql = "select id from netdisk_role_template where role_code="
	// + StrUtil.sqlstr(roleCode);
	// RoleTemplateDb rtDb = new RoleTemplateDb();
	// Vector vec = rtDb.list(sql);
	// Iterator it = vec.iterator();
	// if (it.hasNext()) {
	// RoleTemplateDb rt = (RoleTemplateDb) it.next();
	// id = rt.getInt("id");
	// // roleCode = rt.getString("role_code");
	// // dirCode = rt.getString("dir_code");
	// // visualPath = rt.getString("visual_path");
	// return true;
	// }
	// return false;
	// }

	/**
	 * Qobject 修改采用 update(JdbcTemplate ,object[]{})
	 * 
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean update() throws ErrMsgException {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		RoleTemplateDb rtDb = new RoleTemplateDb();
		rtDb = rtDb.getRoleTempletDb(id);
		try {
			re = rtDb.save(jt, new Object[] { dirCode, visualPath, id });
		} catch (SQLException e) {
			logger.error("update: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}
		return re;
	}

	/**
	 * Qobject 删除采用 del(JdbcTemplate ,object[]{})
	 * 
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean del() throws ResKeyException, ErrMsgException, SQLException {
		RoleTemplateDb rtDb = new RoleTemplateDb();
		rtDb = rtDb.getRoleTempletDb(id);
		boolean re = false;
		try {
			re = rtDb.del();
		} catch (ResKeyException e) {
			logger.error("del: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}
		return re;
	}

	/**
	 * @return
	 */
	public boolean load() {
		RoleTemplateDb rtDb = new RoleTemplateDb();
		rtDb = rtDb.getRoleTempletDb(id);
		if (rtDb.isLoaded()) {
			roleCode = rtDb.getString("role_code");
			dirCode = rtDb.getString("dir_code");
			visualPath = rtDb.getString("visual_path");
			return true;
		}
		return false;
	}

	public Vector list() {
		RoleTemplateDb rtDb = new RoleTemplateDb();
		return rtDb.list();
	}

	/**
	 * @Description: 获取用户名对应的所有的模板
	 * @param userName
	 * @return
	 */
	public Vector getAllTemplateByUser(String userName) {
		UserDb userDb = new UserDb(userName);
		RoleDb[] roles = userDb.getRoles();

		if (roles == null || roles.length == 0) {
			return null;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("select id from netdisk_role_template where role_code in (");

		for (int i = 0; i < roles.length; i++) {
			sql.append(StrUtil.sqlstr(roles[i].getCode())).append(
					i == roles.length - 1 ? ")" : ",");
		}

		RoleTemplateDb rtDb = new RoleTemplateDb();
		return rtDb.list(sql.toString());
	}

	private static ArrayList<File> list = new ArrayList<File>();

	/**
	 * @Description: 返回未下载的集合
	 * @return
	 */
	public ArrayList<File> getUserRoleNeedDownload(String userName) {
		if (!list.isEmpty()) {
			list.clear();
		}
		Vector vec = getAllTemplateByUser(userName);
		RoleTemplateDownloadMgr rtdMgr = new RoleTemplateDownloadMgr();
		rtdMgr.setUserName(userName);
		Config cfg = new Config();
		String rootPath = Global.getRealPath() + cfg.get("file_role_template")
				+ File.separator;

		Iterator it = vec.iterator();
		while (it.hasNext()) {
			RoleTemplateDb rtDb = (RoleTemplateDb) it.next();
			rtdMgr.setRtId(rtDb.getInt("id"));
			if (!rtdMgr.isExists()) {
				String path = rootPath + rtDb.getString("visual_path");
				UtilTools.getAllFileInDir(path, list);
				rtdMgr.create();
			}
		}
		return list;
	}

	/**
	 * 1,显示文件夹被分享过的角色
	 * 
	 * @param unitCode
	 * @param dirCode
	 * @return
	 * @throws SQLException
	 */
	public JSONObject showRolesByTemplete(String unitCode, String dirCode)
			throws SQLException {
		JSONObject jsonRes = new JSONObject();
		RoleDb roleDb = new RoleDb();
		HashMap<String, Integer> hashMap = getRolesByDirCode(dirCode);// 文件夹被分享过的角色
		Vector vec = roleDb.getRolesOfUnit(unitCode);
		Iterator it = vec.iterator();
		net.sf.json.JSONArray jsonArr = new net.sf.json.JSONArray();
		while (it.hasNext()) {
			RoleDb rd = (RoleDb) it.next();
			JSONObject json = new JSONObject();
			String code = rd.getCode();
			String name = rd.getDesc();
			json.put("code", code);
			json.put("name", name);
			if (hashMap != null) {
				if (hashMap.containsKey(code)) {
					json.put("isChecked", true);
				} else {
					json.put("isChecked", false);
				}
			} else {
				json.put("isChecked", false);
			}
			jsonArr.add(json);
		}

		jsonRes.put("roles", jsonArr);
		jsonRes.put("result", true);
		return jsonRes;
	}

	/**
	 * 根据文件夹的code 获得所有被分享的角色code
	 * 
	 * @param dirCode
	 * @return
	 * @throws SQLException
	 */
	public HashMap<String, Integer> getRolesByDirCode(String dirCode)
			throws SQLException {
		HashMap<String, Integer> hashMap = null;
		String sql = "SELECT role_code FROM netdisk_role_template WHERE dir_code = ?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[] { dirCode });
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				if (hashMap == null) {
					hashMap = new HashMap<String, Integer>();
				}
				hashMap.put(record.getString("role_code"), 1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("getRolesByDirCode:" + e.getMessage());
			throw new SQLException(e.getMessage());
		}
		return hashMap;
	}

	/**
	 * 1，角色分享及取消文件夹 2，将system用户的文件 分享给 其他角色 下的所有用户 物理文件夹 根据用户名实现文件夹的copy 数据库遍历
	 * 物理文件夹 添加相应文件夹以及附件
	 * 
	 * @param checkRoleCodes
	 *            选中角色code
	 * @param unCheckRoleCodes
	 *            未选中的角色的code
	 * @param dirCode
	 *            文件夹code
	 * @return
	 * @throws ErrMsgException
	 * @throws SQLException
	 */
	public JSONObject uploadTemplate(String checkRoleCodes,
			String unCheckRoleCodes, String dirCode) throws ErrMsgException,
			SQLException {
		JSONObject json = new JSONObject();
		boolean result = false;
		String[] checkRolesArr = checkRoleCodes.split(",");// 选中角色id
		String[] uncheckRolesArr = unCheckRoleCodes.split(",");// 未选中的角色id
		result = insertAndCancleRoleTemplate(checkRolesArr, uncheckRolesArr,
				dirCode);// 角色选择文件夹 及取消文件夹
		json.put("result", result);
		return json;

	}

	/**
	 * 批量将文件上传及取消给个一个或多个角色模板
	 * 
	 * @param checkRolesArr
	 * @param unCheckRolesArr
	 * @param dirCode
	 * @return
	 * @throws ErrMsgException
	 * @throws SQLException
	 */
	public boolean insertAndCancleRoleTemplate(String[] checkRolesArr,
			String[] unCheckRolesArr, String dirCode) throws ErrMsgException,
			SQLException {
		boolean flag = true;
		boolean result = true;
		if (unCheckRolesArr != null && unCheckRolesArr.length > 0) {
			for (String uncheckRoleId : unCheckRolesArr) {
				flag = isExistDirAndRoles(dirCode, uncheckRoleId);// 判断角色模板是否存在
				if (flag == true) {// 存在则删除
					result &= delRoleDir(uncheckRoleId, dirCode);
				}
			}
		}
		if (checkRolesArr != null && checkRolesArr.length > 0) {
			for (String roleId : checkRolesArr) {
				flag = isExistDirAndRoles(dirCode, roleId);// 判断角色模板是否存在
				if (flag == false) {
					result &= createRoleTemplate(roleId, dirCode);
					result &= copeDirToRoleUsers(roleId, dirCode);// 文件夹的物理拷贝以及数据库文件插入
				}
			}
		}
		return result;

	}

	/**
	 * 根据文件夹 code 和角色模板的code 删除角色模板中的记录
	 * 
	 * @param roleCode
	 * @param dirCode
	 * @return
	 * @throws SQLException
	 */
	public boolean delRoleDir(String roleCode, String dirCode)
			throws SQLException {
		JdbcTemplate jt = null;
		jt = new JdbcTemplate();
		boolean flag = false;
		String sql = "delete from netdisk_role_template where dir_code= ? and role_code = ?";
		try {
			flag = jt.executeUpdate(sql, new Object[] { dirCode, roleCode }) >= 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("deleteRoleTemplate" + e.getMessage());
			throw new SQLException(e.getMessage());
		} finally {
			try {
				RMCache.getInstance().clear();
			} catch (CacheException e) {
				// TODO Auto-generated catch block
				logger.error("updateCache" + e.getMessage());
				throw new SQLException(e.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 根据文件夹 code 角色code 判断角色模板记录 是否存在
	 * 
	 * @param dir_code
	 * @param role_code
	 * @return
	 * @throws SQLException
	 */
	public boolean isExistDirAndRoles(String dir_code, String role_code)
			throws SQLException {
		boolean flag = false;
		String sql = "select count(*) from netdisk_role_template where dir_code= ? and role_code = ?";
		JdbcTemplate jt = null;
		jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(sql, new Object[] { dir_code, role_code });
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				int i = record.getInt(1);
				if (i > 0) {
					flag = true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("isExistDirAndRoles" + e.getMessage());
			throw new SQLException(e.getMessage());
		}
		return flag;
	}

	/**
	 * 根据roleCode以及dirCode创建角色模板文件记录
	 * 
	 * @param roleCode
	 * @param dirCode
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean createRoleTemplate(String roleCode, String dirCode)
			throws ErrMsgException {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			id = (int) SequenceManager
					.nextID(SequenceManager.OA_NETDISK_ROLE_TEMPLATE);
			re = roleTemplateDb.create(jt,
					new Object[] { id, roleCode, dirCode });
		} catch (ResKeyException e) {
			logger.error("create: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}
		return re;
	}

	/**
	 * tmplateDown下载
	 * 
	 * @param userName
	 * @param id2
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean createRoleTemplateDown(String userName, int id2)
			throws ErrMsgException {
		boolean flag = false;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			flag = roleTemplateDb.create(jt, new Object[] { userName, id2 });
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			logger.error("create: " + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}
		return flag;
	}

	/**
	 * 将文件夹批量上传给某个角色下的所有用户
	 * 
	 * @param roleId
	 * @param dirCode
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean copeDirToRoleUsers(String roleId, String dirCode)
			throws ErrMsgException {
		RoleTemplateDownloadMgr rtdMgr = new RoleTemplateDownloadMgr();
		boolean flag = true;
		RoleDb roleDb = new RoleDb(roleId);
		Leaf leaf = new Leaf(dirCode);
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String directorySrc = Global.getRealPath() + cfg.get("file_netdisk")
				+ "/" + leaf.getParentCode() + "/" + leaf.getName();

		File sysShareFileToRoles = new File(directorySrc); // 需要被拷贝的文件夹
		Vector usersVector = roleDb.getAllUserOfRole(); // 一个角色下的所有用户
		// 遍历角色下的所有用户 实现物理文件夹的拷贝，以及数据库文件的拷贝
		for (Iterator iterator = usersVector.iterator(); iterator.hasNext();) {
			UserDb userDb = (UserDb) iterator.next();
			String userName = userDb.getName();
			rtdMgr.setRtId(id);
			rtdMgr.setUserName(userName);
			if (!rtdMgr.isExists()) {
				if (flag) {
					String filePath = Global.getRealPath()
							+ cfg.get("file_netdisk") + "/" + userName + "/"
							+ leaf.getName();
					File f = new File(filePath);
					if (sysShareFileToRoles.exists() && !f.exists()) {
						UtilTools.copyDir(sysShareFileToRoles, f);// 物理文件夹的拷贝
						flag &= addShareDirAndAttToRoleUser(userName, dirCode);// 文件及文件附件信息的拷贝
						if (flag) {
							flag = rtdMgr.create();
						}
					}
					
				}
			}

		}
		return flag;
	}

	/**
	 * 为用户新增文件夹 以及文件夹下所有文件
	 * 
	 * @param userName
	 * @param leafCode
	 * @throws ErrMsgException
	 */
	public boolean addShareDirAndAttToRoleUser(String userName, String leafCode)
			throws ErrMsgException {
		// 先判断是否存在根目录，不存在则进行网盘初始化
		Leaf rleaf = new Leaf(userName);
		if (rleaf == null || !rleaf.isLoaded()) {
			rleaf.AddUser(userName);
		}
		boolean flag = true;
		flag = addDirAndAttToUser(userName, leafCode);
		return flag;

	}

	/**
	 * 新增文件夹以及文件夹下附件给用户
	 * 
	 * @param userName
	 * @param leafCode
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean addDirAndAttToUser(String userName, String leafCode)
			throws ErrMsgException {
		boolean flag = true;
		Leaf leaf = new Leaf(leafCode);

		if (leaf == null || !leaf.isLoaded()) {
			return false;
		}
		// 只有system根目录下的文件夹才可以进行角色模板
		if (leaf.getParentCode().equals("system")) {
			
			try {
				Leaf lf = new Leaf();
				lf.setName(leaf.getName());
				lf.setParentCode(userName);
				if (lf.isNameExists()) {
					leaf = new Leaf(lf.getCode());
				} else {
					// 创建角色模板的根目录
					Directory dir = new Directory();
					dir.setParentCode(userName);
					dir.setName(leaf.getName());
					dir.setType(Leaf.TYPE_DOCUMENT);
					//根据parentCode和UserName
					flag = dir.AddChild();
					// 获取新用户的leaf
					leaf = new Leaf(dir.getCode());
				}
				
				if (flag) {
					flag = insertDirAndAtt(leaf);
				}
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				logger.error("新建文件夹 以及文件夹下附件给改用户：" + e.getMessage());
				throw new ErrMsgException(e.getMessage());
			}
		}
		return flag;

	}

	/**
	 * 根据物理文件 向文件夹以及附件表中插入数据
	 * 
	 * @param leaf
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean insertDirAndAtt(Leaf leaf) throws ErrMsgException {
		boolean flag = true;
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String dirPath = Global.getRealPath() + cfg.get("file_netdisk");
		File path = new File(dirPath + "/" + leaf.getFilePath());
		File[] files = path.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (!file.getName().equals("新建文件夹")) {
					Directory dir = new Directory();
					dir.setParentCode(leaf.getCode());
					dir.setName(file.getName());
					dir.setType(Leaf.TYPE_DOCUMENT);
					try {
						flag = dir.AddChild();
						if (flag) {
							insertDirAndAtt(new Leaf(dir.getCode()));
						}
					} catch (ErrMsgException e) {
						// TODO Auto-generated catch block
						logger.error("insertDirAndAtt" + e.getMessage());
						throw new ErrMsgException(e.getMessage());
					}
				}
			} else if (file.isFile()) {
				int doc_id = leaf.getDocId();
				String name = file.getName();
				int index = name.lastIndexOf("_");
				boolean isBackFile = false;
				if(index != -1){
					if(name.substring(index+1,name.length()).length() == 14){
						isBackFile = true;
					}
				}
				if(!isBackFile){
					Attachment attach = new Attachment();
					attach.setDocId(doc_id);
					attach.setVisualPath(leaf.getFilePath());
					attach.setVersionDate(new Date(file.lastModified()));
					attach.setName(name);
					String ext = name.substring(name.lastIndexOf(".") + 1);
					attach.setDiskName(file.getName());
					attach.setExt(ext);
					attach.setPageNum(1);
					flag = attach.create();
				}
				
			}
		}
		return flag;
	}

	/**
	 * 新增用户 新增用户属于一个或多个角色 将角色中分享的文件夹拷贝给该用户
	 * 
	 * @param userName
	 * @throws ErrMsgException
	 */
	public boolean copyDirsAndAttToNewUser(String userName)
			throws ErrMsgException {
		boolean flag = true;
		try {
			UserDb user = new UserDb();
			user = user.getUserDb(userName);
			RoleDb[] rd = user.getRoles();// 用户属于的多个角色
			RoleTemplateDownloadMgr rtdm = new RoleTemplateDownloadMgr();// 获得用户的角色模板
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			for (RoleDb roleDb : rd) {
				// 角色下 分享的一个或者多个文件夹
				List<RoleTemplateDb> roleTemplateDbs = queryDirsByRoleCode(roleDb
						.getCode());
				if (roleTemplateDbs != null && roleTemplateDbs.size() > 0) {
					for (RoleTemplateDb roleTempateDb : roleTemplateDbs) {
						int tm_id = roleTempateDb.getInt("id");
						rtdm.setRtId(tm_id);
						rtdm.setUserName(userName);
						if (!rtdm.isExists()) {
							// 被分享文件夹
							Leaf leaf = new Leaf(roleTempateDb
									.getString("dir_code"));
							// 分享文件夹的服务器物理路径
							String directorySrc = Global.getRealPath()
									+ cfg.get("file_netdisk") + "/"
									+ leaf.getParentCode() + "/"
									+ leaf.getName();
							// 创建需要被拷贝的文件夹
							File sysDir = new File(directorySrc);
							// 拷贝地址
							String filePath = Global.getRealPath()
									+ cfg.get("file_netdisk") + "/" + userName
									+ "/" + leaf.getName();
							File f = new File(filePath);
							// 如果拷贝文件夹不存在
							if (sysDir.exists() && !f.exists()) {
								UtilTools.copyDir(sysDir, f);// 物理文件夹拷贝
								flag &= addDirAndAttToUser(userName, leaf
										.getCode());// 数据库记录的拷贝
								if (flag) {
									rtdm.create();
								}
							}
						}

					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("用户角色初始化时：" + e.getMessage());
			throw new ErrMsgException(e.getMessage());

		}

		return flag;
	}

	/**
	 * 根据roleCode,获得该文件夹被分享的所有leafCode
	 * 
	 * @param roleCode
	 * @return
	 * @throws SQLException
	 */
	public List<RoleTemplateDb> queryDirsByRoleCode(String roleCode)
			throws SQLException {
		List<RoleTemplateDb> roleTempate = new ArrayList<RoleTemplateDb>();
		String sql = "select id from netdisk_role_template where role_code = ?";
		JdbcTemplate jt = null;
		jt = new JdbcTemplate();
		ResultIterator ri = null;

		try {
			ri = jt.executeQuery(sql, new Object[] { roleCode });
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				int id = record.getInt("id");
				RoleTemplateDb rtd = roleTemplateDb.getRoleTempletDb(id);
				roleTempate.add(rtd);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("根据RoleCode查询dirCode：" + e.getMessage());
			throw new SQLException(e.getMessage());
		}
		return roleTempate;
	}

	/**
	 * 根据userName删除该网络硬盘下的所有数据
	 */
	public void delDirsByUserName(String userName) {
		Leaf leaf = new Leaf(userName);
		leaf.del(leaf);

	}

}