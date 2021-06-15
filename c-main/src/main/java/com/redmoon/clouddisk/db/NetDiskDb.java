package com.redmoon.clouddisk.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cn.js.fan.db.Conn;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.Config;
import com.redmoon.clouddisk.bean.CooperateLogBean;
import com.redmoon.clouddisk.bean.DirectoryBean;
import com.redmoon.clouddisk.bean.DocumentBean;
import com.redmoon.clouddisk.bean.NetDiskBean;
import com.redmoon.clouddisk.socketServer.ServerWorker;
import com.redmoon.clouddisk.socketServer.SocketMgr;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.netdisk.Attachment;
import com.redmoon.oa.netdisk.DocContent;
import com.redmoon.oa.netdisk.Document;
import com.redmoon.oa.netdisk.Leaf;

/**
 * @author 古月圣
 * 
 */
public class NetDiskDb {

	public final static byte NOT_EXIST = 0x00;
	public final static byte NOT_MODIFIED = 0x01;
	public final static byte CAN_UPDATE = 0x02;
	public final static byte CAN_COMMIT = 0x03;
	public final static byte IS_DELETED = 0x04;
	public final static byte IS_CONFLICT = 0x05;
	public final static byte DELETED_ADD = 0x06;
	public final static byte BROKEN_RESUME = 0x07;
	public final static byte BROKEN_RESUME_R = 0x08;

	private final static int FILE_ACTION_ADDED = 0x00000001;
	private final static int FILE_ACTION_FETCHING = 0x0000000a;

	private NetDiskBean netDiskBean;

	private String connname;

	// version_date 客户端文件修改时间
	// share_user 分享给我文件的用户
	// share_id 分享给我的文件本身的id
	// is_edit 分享给我的文件是否可编辑
	// is_editeed 分享给我的文件是否被编辑过了

	/**
	 * @param netDiskBean
	 */
	public NetDiskDb(NetDiskBean netDiskBean) {
		this.netDiskBean = netDiskBean;
		connname = Global.getDefaultDB();
	}

	/**
	 * @return 0 not exist, 1 not modified, 2 can update, 3 can commit, 4 is
	 *         deleted, 5 conflict
	 */
	public byte checkStatus() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "select id,doc_id,version_date,is_current,is_deleted from netdisk_document_attach "
				+ "where name=? and user_name=? and visualpath=? order by is_current desc,version_date desc";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			rs = pstmt.executeQuery();
			boolean isExist = false;
			int action = netDiskBean.getAction();
			Date verDate = new Date();
			while (rs.next()) {
				int isCurrent = rs.getInt("is_current");
				Date date = rs.getTimestamp("version_date");

				if (isCurrent == 1) {
					netDiskBean.setId(rs.getLong("id"));
					verDate = date;
				}

				if (action == FILE_ACTION_FETCHING) {
					return BROKEN_RESUME_R;
				}

				if (netDiskBean.getVersionDate() == null
						|| netDiskBean.getTempDate() == null) {
					return NOT_MODIFIED;
				}

				int isDeleted = rs.getInt("is_deleted");

				// 服务器端文件已删除
				if ((isDeleted == 1 || isDeleted == 2 || isDeleted == 3)
						&& isCurrent == 1) {
					if (action == FILE_ACTION_ADDED) {
						// 删除后新增同名文件
						return DELETED_ADD;
					} else {
						// 已删除
						return IS_DELETED;
					}
				}

				// 判断服务器端文件是否存在
				if (isDeleted == 0 && isCurrent == 1) {
					// netDiskBean.setIsShare(rs.getInt("is_share"));
					// if (netDiskBean.getIsShare() == ServerWorker.SHARED_FILE)
					// {
					// // 如果是共享文件,则判断该share_id对应文件的状态
					// netDiskBean.setUserName(rs.getString("share_user"));
					// return checkStatus();
					// }
					// netDiskBean.setTempDate(date);
					isExist = true;
				}

				// 传送尚未完成
				if (isDeleted == 5 && isCurrent == 1) {
					return BROKEN_RESUME;
				}

				// 客户端文件未有改动,且是最新版本
				if (isCurrent == 1
						&& date.compareTo(netDiskBean.getTempDate()) == 0
						&& date.compareTo(netDiskBean.getVersionDate()) == 0) {
					return NOT_MODIFIED;
				}

				// 客户端文件正在更新,且更新为最新版本
				if (isCurrent == 1
						&& netDiskBean.getTempDate().compareTo(
								netDiskBean.getVersionDate()) != 0
						&& verDate.compareTo(netDiskBean.getVersionDate()) == 0) {
					return NOT_MODIFIED;
				}

				// 客户端文件的改动是基于服务器端文件最新版本改动,则客户端文件提交,服务器端文件更新
				if (isCurrent == 1
						&& date.compareTo(netDiskBean.getTempDate()) == 0
						&& date.compareTo(netDiskBean.getVersionDate()) != 0) {
					netDiskBean.setDocId(rs.getLong("doc_id"));
					String diskName = netDiskBean.getName() + "_"
							+ DateUtil.format(date, "yyyyMMddHHmmss");
					netDiskBean.setDiskName(diskName);
					return CAN_COMMIT;
				}

				// 随文件夹一起删除的文件
				if (isCurrent == 0 && isDeleted == 2) {
					return DELETED_ADD;
				}

				// 客户端文件未有改动,最新修改时间为某个历史版本的时间,则客户端文件更新为最新版本
				if (isCurrent == 0
						&& date.compareTo(netDiskBean.getTempDate()) == 0
						&& date.compareTo(netDiskBean.getVersionDate()) == 0) {
					return CAN_UPDATE;
				}

				// 客户端文件是基于非最新版本的改动,则冲突
				if (isCurrent == 0
						&& netDiskBean.getTempDate().compareTo(
								netDiskBean.getVersionDate()) != 0
						&& verDate.compareTo(netDiskBean.getTempDate()) != 0) {
					return IS_CONFLICT;
				}
			}

			if (!isExist) {
				// 服务器端文件不存在
				return NOT_EXIST;
			} else {
				// 服务器端文件存在,但是未找到与客户端文件一致的版本,则冲突
				return IS_CONFLICT;
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return NOT_MODIFIED;
	}

	/**
	 * @Description: 对于文件路径中的文件夹,只要有一个文件夹处于协作状态,则该文件处于协作文件夹,相应的操作需要写协作日志
	 * @return
	 */
	public boolean checkCooperating(CooperateLogBean cooperateLogBean) {
		String[] dirs = netDiskBean.getVisualPath().split("/", 0);
		DirectoryBean directoryBean = new DirectoryBean();
		directoryBean.setParentCode(netDiskBean.getUserName());

		for (int i = 1; i < dirs.length; i++) {
			directoryBean.setName(dirs[i]);
			DirectoryDb directoryDb = new DirectoryDb(directoryBean);
			if (!directoryDb.isExist()) {
				return false;
			}

			Leaf leaf = new Leaf(directoryBean.getCode());
			if (leaf != null && leaf.isLoaded() && leaf.isShared()) {
				cooperateLogBean.setDirCode(leaf.getCode());
				cooperateLogBean.setUserName(netDiskBean.getUserName());
				cooperateLogBean.setActionDate(new Date());
				cooperateLogBean.setActionName(netDiskBean.getVisualPath()
						+ "/" + netDiskBean.getName());
				CooperateLogDb cologDb = new CooperateLogDb(cooperateLogBean);
				cologDb.create();
				return true;
			}

			directoryBean.setParentCode(directoryBean.getCode());
		}
		return false;
	}

	/**
	 * create分三种情况 1.数据库里有记录,doc_id可以直接获取
	 * 2.数据库里没有记录,则需要去获取doc_id,如果服务端没有相应目录则按照客户端的目录结构新建服务端目录
	 * 
	 * @return
	 */
	public boolean create() {

		// String sql =
		// "insert into netdisk_document_attach (name,user_name,visualpath,is_current,create_date,version_date,is_share,is_edit,share_user,is_deleted,share_id,is_refused,id) values (?,?,?,?,?,?,?,?,?,'0',?,'0',?)";

		com.redmoon.oa.netdisk.Attachment att = new com.redmoon.oa.netdisk.Attachment();

		// 情况2
		if (netDiskBean.getDocId() <= 0) {
			// String parentCode = netDiskBean.getUserName();
			String parentCode = "-1";
			long id = 0;
			DirectoryBean directoryBean = null;
			DocumentBean documentBean = null;
			String[] dirs = netDiskBean.getVisualPath().split("/", 0);
			for (int i = 0; i < dirs.length; i++) {
				directoryBean = new DirectoryBean();
				documentBean = new DocumentBean();
				boolean flag = false;
				if (parentCode.equals("-1")) {
					Leaf leaf = new Leaf(netDiskBean.getUserName());
					if (leaf != null && leaf.isLoaded()) {
						directoryBean.setCode(netDiskBean.getUserName());
						flag = true;
					}
				} else {
					documentBean.setTitle(dirs[i]);
					directoryBean.setName(dirs[i]);
				}
				documentBean.setNick(netDiskBean.getUserName());
				documentBean.setParentCode(parentCode);
				directoryBean.setParentCode(parentCode);
				directoryBean.setRootCode(netDiskBean.getUserName());
				directoryBean.setLayer(i + 1);
				directoryBean.setOrders(1);
				directoryBean.setChildCount(0);

				DirectoryDb directoryDb = new DirectoryDb(directoryBean);
				DocumentDb documentDb = new DocumentDb(documentBean);

				if (!flag) {
					flag = directoryDb.isExist();
				}
				String code = "";
				if (flag) {
					code = directoryBean.getCode();
				} else {
					if (parentCode.equals("-1")) {
						Config cfg = Config.getInstance();
						boolean isRoot = cfg
								.getBooleanProperty("isCloudDiskRoot");
						documentBean.setTitle(isRoot ? "我的网盘" : "我的文档");
						directoryBean.setName(isRoot ? "我的网盘" : "我的文档");
						directoryBean.setDescription("根结点");
						code = netDiskBean.getUserName();
					}
				}

				if (code.equals("")) {
					code = RandomSecquenceCreator.getId(20);
				}

				documentBean.setClass1(code);
				directoryBean.setCode(code);

				if (!documentDb.isExist()) {
					// documentDb.getCurId();
					// id = documentBean.getId();
					id = SequenceManager
							.nextID(SequenceManager.OA_DOCUMENT_NETDISK);
					documentBean.setId(id);
					documentDb = new DocumentDb(documentBean);
					documentDb.create();
				}
				directoryBean.setDocId(documentBean.getId());
				directoryDb = new DirectoryDb(directoryBean);
				if (!flag) {
					directoryDb.getOrders();
					directoryDb.create();
				} else {
					directoryDb.update();
				}
				parentCode = code;
			}

			if (documentBean != null) {
				if (dirs.length == 2) {
					documentBean.setClass1(parentCode);
					DocumentDb documentDb = new DocumentDb(documentBean);
					documentDb.getRootId();
				}
				netDiskBean.setDocId(documentBean.getId());
			} else {
				return false;
			}
		}

		Document doc = new Document();
		doc = doc.getDocument((int) netDiskBean.getDocId());
		DocContent dc = doc.getDocContent(1);
		int orders = dc.getAttachmentMaxOrders() + 1;

		att.setDocId((int) netDiskBean.getDocId());
		att.setPageNum(1);
		att.setOrders(orders);
		att.setSize(0);

		att.setName(netDiskBean.getName());
		att.setDiskName(netDiskBean.getName());
		att.setVisualPath(netDiskBean.getVisualPath());
		att.setExt(StrUtil.getFileExt(netDiskBean.getName()));

		// uploaddate,USER_NAME,is_current,version_date,is_share,is_edit,share_user,is_deleted,share_id,is_refused)
		// values (?,?,?,?,?,?,?,?,'',?,?,?,?,?,?,?,?,?,?)";

		att.setUploadDate(new java.util.Date());
		att.setUserName(netDiskBean.getUserName());
		att.setCurrent(true);
		att.setVersionDate(netDiskBean.getVersionDate());
		att.setDeleted(5);

		boolean re = att.create();

		netDiskBean.setId(att.getId());

		// 设置已已用空间大小
		com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb(
				netDiskBean.getUserName());
		ud.setDiskSpaceUsed(getAllFileSize());
		ud.save();

		return re;
	}

	/**
	 * 客户端提交文件后，服务器端更新版本 netDiskBean.getIsCurrent() 是把客户端与服务器端文件做个对比，新为1，旧则为0
	 * 当比较结果新，即为1时，服务器端的当前记录将被置为历史版本 当比较结果为旧，即为0时，则说明客户端需还原历史版本
	 * 
	 * @return
	 */
	public boolean update() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_document_attach set is_current="
				+ (netDiskBean.getIsCurrent() == 1 ? "0" : "1")
				+ ",diskname=? where diskname=? and user_name=? and visualpath=? and is_current=? and is_deleted=0";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getDiskName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(4, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			pstmt.setInt(5, netDiskBean.getIsCurrent());
			re = pstmt.executeUpdate() == 1 ? true : false;

			// 设置已已用空间大小
			com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb(
					netDiskBean.getUserName());
			ud.setDiskSpaceUsed(getAllFileSize());
			ud.save();
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
	 * 更新有编辑权限的被分享者的对应share_id为当前id
	 * 
	 * @return
	 */
	public boolean updateSharedFile(long oldId) {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_document_attach set version_date=?,share_id=? where share_id=? and is_edit=1";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, new Timestamp(netDiskBean.getVersionDate()
					.getTime()));
			pstmt.setLong(2, netDiskBean.getId());
			pstmt.setLong(3, oldId);
			re = pstmt.executeUpdate() > -1 ? true : false;
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
	 * @Description: 更新分享者以及其他被分享者的信息(某一可编辑的被分享者编辑分享文件后执行)
	 * @param oldId
	 * @return
	 */
	public boolean updateShareAndOtherShared(long oldId) {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_document_attach set version_date=?,share_id=? where share_id=?";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, new Timestamp(netDiskBean.getVersionDate()
					.getTime()));
			pstmt.setLong(2, netDiskBean.getId());
			pstmt.setLong(3, oldId);
			re = pstmt.executeUpdate() > -1 ? true : false;
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
	 * 取得当前版本的修改时间
	 */
	public void getVersionDate() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select version_date from netdisk_document_attach where name=? and user_name=? and visualpath=? and is_current=1 and is_deleted=0";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			rs = pstmt.executeQuery();
			if (rs.next()) {
				netDiskBean.setTempDate(rs.getTimestamp("version_date"));
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

	/**
	 * 取出某个版本的文件名
	 * 
	 * @return
	 */
	public String getFileName() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select diskname from netdisk_document_attach where "
				+ "name=? and user_name=? and visualpath=? and version_date=? and is_deleted=0";
		String name = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			pstmt.setTimestamp(4, new Timestamp(netDiskBean.getVersionDate()
					.getTime()));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				name = rs.getString("diskname");
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return name;
	}

	/**
	 * 列出所有的历史版本
	 * 
	 * @return
	 */
	public ArrayList<String> listHistory() {
		Conn conn = new Conn(connname);
		ArrayList<String> list = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		int limits = cfg.getIntProperty("historyCount");
		if (limits < 0) {
			limits = 20;
		}
		String sql = "select version_date from netdisk_document_attach where name=? and user_name=? and visualpath=? and is_deleted=0"
				+ (Global.db.equalsIgnoreCase(Global.DB_ORACLE) ? " and rownum<="
						+ limits
						: "")
				+ " order by is_current desc,version_date desc"
				+ (Global.db.equalsIgnoreCase(Global.DB_MYSQL) ? " limit "
						+ limits : "");
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(DateUtil.format(rs.getTimestamp("version_date"),
						"yyyy-MM-dd HH:mm:ss"));
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return null;
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return list;
	}

	/**
	 * 列出所有的网盘文件
	 * 
	 * @task:需要增加字段以确定是否属于网盘，因为网盘根目录可配置为与根节点合并
	 * @return
	 */
	public HashMap<String, String> listAll() {
		Conn conn = new Conn(connname);
		HashMap<String, String> map = new HashMap<String, String>();
		ResultSet rs = null;

		// 去掉默认的路径:用户名/我的网盘(如果有的话)
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		boolean isCloudDiskRoot = cfg.getBooleanProperty("isCloudDiskRoot");
		String defaultPath = netDiskBean.getUserName()
				+ (isCloudDiskRoot ? "" : "/" + SocketMgr.MYDISK);

		String sql = "select id,name,visualpath,version_date,is_deleted from netdisk_document_attach where user_name="
				+ StrUtil.sqlstr(netDiskBean.getUserName())
				+ " and is_share=0 and ((is_current=1&&is_deleted<>5) || (is_current=0&&is_deleted=2)) and (visualpath="
				+ StrUtil.sqlstr(defaultPath)
				+ " or visualpath like "
				+ StrUtil.sqlstr(defaultPath + "/%") + ")";
		try {
			rs = conn.executeQuery(sql);
			while (rs.next()) {
				String visualPath = rs.getString("visualpath");
				if (visualPath.indexOf(defaultPath) == 0) {
					visualPath = visualPath.substring(defaultPath.length());
				}
				String name = rs.getString("name");
				String path = visualPath
						+ (visualPath.equals("") || visualPath.endsWith("/") ? ""
								: "/") + name;
				int del = rs.getInt("is_deleted");
				boolean isDeleted = !(del == 0);
				if (map.containsKey(path)) {
					if (isDeleted) {
						continue;
					}
				}

				StringBuilder sb = new StringBuilder();
				sb.append(rs.getString("id")).append(ServerWorker.LAST_SEPT)
						.append(name).append(ServerWorker.LAST_SEPT).append(
								visualPath).append(ServerWorker.LAST_SEPT)
						.append(
								DateUtil.format(
										rs.getTimestamp("version_date"),
										"yyyy-MM-dd HH:mm:ss")).append(
								ServerWorker.LAST_SEPT).append(del);
				map.put(path, sb.toString());
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			return null;
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return map;
	}

	/**
	 * 获取某个历史版本，用于做历史版本下载
	 * 
	 * @return
	 */
	public String getUrlName() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select id,diskname from netdisk_document_attach where "
				+ "name=? and user_name=? and visualpath=? and version_date=? and is_deleted=0";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			pstmt.setTimestamp(4, new Timestamp(netDiskBean.getVersionDate()
					.getTime()));
			rs = pstmt.executeQuery();

			String diskName = null;
			while (rs.next()) {
				netDiskBean.setId(rs.getLong("id"));
				diskName = rs.getString("diskname");
			}
			return diskName;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return null;
	}

	public boolean getCurrentId() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select id from netdisk_document_attach where name=? and user_name=? "
				+ "and visualpath=? and is_current=1 and is_deleted=0";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, StrUtil.getNullStr(netDiskBean.getName()));
			pstmt.setString(2, StrUtil.getNullStr(netDiskBean.getUserName()));
			pstmt.setString(3, StrUtil.getNullStr(netDiskBean.getVisualPath()));
			rs = pstmt.executeQuery();

			if (rs.next()) {
				netDiskBean.setId(rs.getLong("id"));
			}
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

	public ArrayList<String> listFilesForZip(String basePath, String zipPath) {
		ArrayList<String> list = new ArrayList<String>();
		Conn conn = new Conn(connname);
		ResultSet rs = null;
		String sql = "select name,visualPath from netdisk_document_attach where user_name="
				+ StrUtil.sqlstr(netDiskBean.getUserName())
				+ " and (visualPath like "
				+ StrUtil.sqlstr(basePath + "/" + zipPath + "/%")
				+ " or visualPath="
				+ StrUtil.sqlstr(basePath + "/" + zipPath)
				+ ") and is_current=1 and is_deleted=0";
		try {
			rs = conn.executeQuery(sql);

			while (rs.next()) {
				String visualPath = rs.getString("visualPath");
				visualPath = visualPath.substring(basePath.length() + 1);
				if (!visualPath.endsWith("/")) {
					visualPath += "/";
				}
				visualPath += rs.getString("name");
				list.add(visualPath);
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
	 * 客户端删除，置删除标志 删除的时候不需要改文件名为bakName，修改所有的版本文件为删除状态 直接调用delLogic
	 * 
	 * @return
	 */
	public boolean delete() {
		Attachment att = new Attachment();
		boolean re = att.delAttLogic(netDiskBean.getUserName(), netDiskBean
				.getName(), netDiskBean.getVisualPath());
		if (re) {
			// 判断是否为协作文件夹,如果是则写协作日志
			CooperateLogBean cooperateLogBean = new CooperateLogBean();
			cooperateLogBean.setAction(CooperateLogBean.ACTION_DELETE);
			checkCooperating(cooperateLogBean);
		}

		return re;
	}

	public boolean load() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select user_name,name,diskname,visualpath,version_date from netdisk_document_attach where id=? and is_deleted=0";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, netDiskBean.getId());
			rs = pstmt.executeQuery();

			if (rs.next()) {
				netDiskBean.setUserName(StrUtil.getNullStr(rs
						.getString("user_name")));
				netDiskBean.setName(StrUtil.getNullStr(rs.getString("name")));
				netDiskBean.setDiskName(StrUtil.getNullStr(rs
						.getString("diskname")));
				netDiskBean.setVisualPath(StrUtil.getNullStr(rs
						.getString("visualpath")));
				netDiskBean.setTempDate(rs.getTimestamp("version_date"));
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
	 * 更新文件的大小
	 * 
	 * @return
	 */
	public boolean updateFileSize() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		String sql = "update netdisk_document_attach set file_size=?,is_deleted=0 where id=? and is_deleted=5";
		boolean re = false;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, netDiskBean.getFileSize());
			pstmt.setLong(2, netDiskBean.getId());
			re = pstmt.executeUpdate() == 1 ? true : false;

			// 设置已已用空间大小
			com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb(
					netDiskBean.getUserName());
			ud.setDiskSpaceUsed(getAllFileSize());
			ud.save();
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
	 * @Description: 获取网盘文件夹已用大小
	 * @return
	 */
	public long getAllFileSize() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "select sum(file_size) from netdisk_document_attach where is_current=1 and is_deleted=0 and user_name=?";
		long allSize = 0L;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, netDiskBean.getUserName());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				allSize = rs.getLong(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return allSize;
	}
}
