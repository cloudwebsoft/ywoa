package com.redmoon.oa.netdisk;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import javax.imageio.ImageIO;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.person.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.*;

public class Attachment implements java.io.Serializable {
	int id;
	int docId;
	String name;
	String fullPath;
	String diskName;
	String visualPath;
	String userName;
	String connname;
	boolean current = true;

	java.util.Date versionDate;
	boolean share = false;
	boolean edit = false;
	String shareUser;
	int deleted = 0;
	java.util.Date deleteDate;
	long shareId;
	boolean refused = false;
	boolean edited = false;

	public boolean isCurrent() {
		return current;
	}

	public void setCurrent(boolean current) {
		this.current = current;
	}

	public java.util.Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(java.util.Date versionDate) {
		this.versionDate = versionDate;
	}

	public boolean isShare() {
		return share;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public String getShareUser() {
		return shareUser;
	}

	public void setShareUser(String shareUser) {
		this.shareUser = shareUser;
	}

	public int isDeleted() {
		return deleted;
	}

	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}

	public java.util.Date getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(java.util.Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public long getShareId() {
		return shareId;
	}

	public void setShareId(long shareId) {
		this.shareId = shareId;
	}

	public boolean isRefused() {
		return refused;
	}

	public void setRefused(boolean refused) {
		this.refused = refused;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

	String LOAD = "SELECT doc_id, name, fullpath, diskname, visualpath, orders, page_num, file_size, ext, uploadDate, publicShareDir,PUBLIC_SHARE_DEPTS,USER_NAME,IS_CURRENT,VERSION_DATE,IS_SHARE,IS_EDIT,SHARE_USER,IS_DELETED,DELETE_DATE,SHARE_ID,IS_REFUSED,IS_EDITED FROM netdisk_document_attach WHERE id=?";
	String SAVE = "update netdisk_document_attach set doc_id=?, name=?, fullpath=?, diskname=?, visualpath=?, orders=?, page_num=?, publicShareDir=?,PUBLIC_SHARE_DEPTS=?,IS_CURRENT=?,VERSION_DATE=?,IS_SHARE=?,IS_EDIT=?,SHARE_USER=?,IS_DELETED=?,DELETE_DATE=?,SHARE_ID=?,IS_REFUSED=?,file_size=? WHERE id=?";
	transient Logger logger = Logger.getLogger(Attachment.class.getName());

	public Attachment() {
		connname = Global.getDefaultDB();
	}

	public Attachment(int id) {
		connname = Global.getDefaultDB();
		if (connname.equals(""))
			logger.info("Attachment:默认数据库名为空！");
		this.id = id;
		loadFromDb();
	}

	public Attachment(int orders, int docId, int pageNum) {
		connname = Global.getDefaultDB();
		if (connname.equals(""))
			logger.info("Attachment:默认数据库名为空！");
		this.orders = orders;
		this.docId = docId;
		this.pageNum = pageNum;
		loadFromDbByOrders();
	}

	/**
	 * @param name
	 * @param docId
	 *            判断数据库里是否有重名文件数据存在
	 * @return
	 */
	public boolean isExist(String name, int docId) {
		String sql = "select id from netdisk_document_attach where doc_id=? and diskname=?";
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql, new Object[] {
					new Integer(docId), name });
			if (ri.hasNext())
				return true;
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return false;
	}

	public Attachment getAttachment(int id) {
		return new Attachment(id);
	}

	public String getDirCode() {
		Document doc = new Document();
		doc = doc.getDocument(docId);
		if (doc == null || !doc.isLoaded())
			return "";
		else
			return doc.getDirCode();
	}

	public Leaf getLeaf() {
		String dirCode = getDirCode();
		if (dirCode.equals(""))
			return null;
		Leaf lf = new Leaf();
		return lf.getLeaf(dirCode);
	}

	public boolean create() {
		String sql = "insert into netdisk_document_attach (fullpath,doc_id,name,diskname,visualpath,page_num,orders,file_size,ext,publicShareDir,uploaddate,USER_NAME,id,version_date,is_deleted) values (?,?,?,?,?,?,?,?,?,'',?,?,?,?,?)";
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, fullPath);
			pstmt.setInt(2, docId);
			pstmt.setString(3, name);
			pstmt.setString(4, diskName);
			pstmt.setString(5, visualPath);
			pstmt.setInt(6, pageNum);
			pstmt.setInt(7, orders);
			// 取得文件的大小
			// File f = new File(fullPath);
			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			String file_netdisk = cfg.get("file_netdisk");
			if (size == 0) {
				File f = new File(Global.getRealPath() + file_netdisk + "/"
						+ visualPath + "/" + diskName);
				if (f.exists())
					size = f.length();
			}
			pstmt.setLong(8, size);
			pstmt.setString(9, ext);
			if (userName == null || userName.equals("")) {
				userName = getLeaf().getRootCode();
			}
			pstmt.setTimestamp(10,
					new Timestamp(new java.util.Date().getTime()));
			pstmt.setString(11, userName);

			id = (int) SequenceManager
					.nextID(SequenceManager.OA_DOCUMENT_NETDISK_ATTACHMENT);

			pstmt.setLong(12, id);

			if (versionDate == null) {
				pstmt.setTimestamp(13, null);
			} else {
				pstmt.setTimestamp(13, new Timestamp(versionDate.getTime()));
			}
			
			pstmt.setInt(14, deleted);

			re = conn.executePreUpdate() >= 1 ? true : false;
		} catch (SQLException e) {
			logger.error("create:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	/**
	 * 文件夹删除判断是否存在文件
	 */
	public boolean isExitFile(String dirCode) {
		boolean re = false;
		ResultSet rs = null;
		String sql = "select id from netdisk_document_attach where doc_id = (select doc_id from netdisk_directory where code ="
				+ StrUtil.sqlstr(dirCode)
				+ ") and is_deleted = 0 and is_current = 1";
		Conn conn = new Conn(connname);
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				re = true;
				return re;
			}
		} catch (Exception e) {
			logger.error("isExitFile:" + e.getMessage());
		}
		return re;
	}

	/**
	 * 回收站还原
	 */
	public boolean restore() {
		boolean re = false;
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		Config config = new Config();
		// 判定还原文件是否重名

		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		File file = new File(filePath + visualPath + "/" + diskName);
		File fl = new File(filePath + visualPath + "/" + name);
		String sql = "";
		String beforeName = name.substring(0,name.lastIndexOf("."));
		String behindName = name.substring(name.lastIndexOf("."));
		String theName = beforeName + "(1)" + behindName;
		try {
			if (fl.exists()) {
				re = file.renameTo(new File(filePath + visualPath + "/" + theName));
				sql = "update netdisk_document_attach set IS_DELETED = 0 ,IS_CURRENT = 1, name="
						+ StrUtil.sqlstr(theName)
						+ ",diskname="
						+ StrUtil.sqlstr(theName) + " where id=? ";
				// logger.info("del:id=" + id);
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, id);
				re = conn.executePreUpdate() >= 1 ? true : false;
				pstmt.close();
			} else {
				re = file
						.renameTo(new File(filePath + visualPath + "/" + name));
				sql = "update netdisk_document_attach set IS_DELETED = 0 ,IS_CURRENT = 1,name="
						+ StrUtil.sqlstr(name)
						+ ",diskname="
						+ StrUtil.sqlstr(name) + " where id=? ";
				// logger.info("del:id=" + id);
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, id);
				re = conn.executePreUpdate() >= 1 ? true : false;
				pstmt.close();
				
			}
			// 更新用户的磁盘已用空间
			UserDb ud = new UserDb();
			Document doc = new Document();
			doc = doc.getDocument(docId);
			ud = ud.getUserDb(doc.getNick());
			ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + size);
			re = ud.save();
			
			sql = "select code from netdisk_directory where doc_id = " + docId;
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				String dirCode = rs.getString(1);
				Leaf lf = new Leaf(dirCode);
				while (!dirCode.equals(userName)) { // 依次将上级目录变为非删除状态
					sql = "update netdisk_directory set isDeleted = 0 where code = "
							+ StrUtil.sqlstr(dirCode);
					re = conn.executeUpdate(sql) >= 1 ? true : false;
					dirCode = lf.getParentCode();
					lf = new Leaf(dirCode);
				}
			}
			sql = "update netdisk_document_attach set orders=orders+1 where doc_id=? and page_num=? and orders>?";
			PreparedStatement pstmt1 = conn.prepareStatement(sql);
			pstmt1.setInt(1, docId);
			pstmt1.setInt(2, pageNum);
			pstmt1.setInt(3, orders);
			conn.executePreUpdate();
		} catch (SQLException e) {
			logger.error("restore:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	/**
	 * 逻辑删除,修改该文件所有版本的is_deleted为true，is_current为false，
	 * 同时修改netdisk_document_attach表的orders，更新用户的磁盘已用空间
	 * 
	 * @return
	 */
	public boolean delLogical() {
		// setDeleted(true);
		// setDeleteDate(new java.util.Date());
		// rs = save();
		// return save();
		boolean re = delAttLogic();

		// 更新其后的附件的orders
		if (re) {
			PreparedStatement pstmt = null;
			Conn conn = new Conn(connname);
			try {
				String sql = "update netdisk_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, docId);
				pstmt.setInt(2, pageNum);
				pstmt.setInt(3, orders);
				conn.executePreUpdate();

				// 更新用户的磁盘已用空间
				UserDb ud = new UserDb();
				Document doc = new Document();
				doc = doc.getDocument(docId);
				ud = ud.getUserDb(doc.getNick());
				ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() - size);
				re = ud.save();
			} catch (Exception e) {
				logger.error("delLogical:" + e.getMessage());
			} finally {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			}
		}
		return re;
	}

	// 彻底删除
	public boolean del() {
		//String sql = "delete from netdisk_document_attach where id=?";
		String sql = "update netdisk_document_attach set is_deleted = 3 where id = ? ";
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			// logger.info("del:id=" + id);
			pstmt.setInt(1, id);
			re = conn.executePreUpdate() >= 1 ? true : false;
			pstmt.close();
			// 更新其后的附件的orders
			//sql = "update netdisk_document_attach set orders=orders-1 where doc_id=? and page_num=? and orders>?";
			//pstmt = conn.prepareStatement(sql);
			//pstmt.setInt(1, docId);
			//pstmt.setInt(2, pageNum);
			//pstmt.setInt(3, orders);
			//conn.executePreUpdate();

			// 更新用户的磁盘已用空间
			// UserDb ud = new UserDb();
			// Document doc = new Document();
			// doc = doc.getDocument(docId);
			// ud = ud.getUserDb(doc.getNick());
			// ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() - size);
			// ud.save();
		} catch (SQLException e) {
			logger.error("del:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		// 删除文件
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String file_netdisk = cfg.get("file_netdisk");
		File fl = new File(Global.getRealPath() + file_netdisk + "/"
				+ visualPath + "/" + diskName);
		fl.delete();
		return re;
	}

	/**
	 * 通过DocId找到Id 便于直接删除 （上传模板时使用）
	 * 
	 * @return
	 */
	public boolean getIdByDocId() {
		boolean re = false;
		Conn conn = new Conn(connname);
		String sql = "select id from netdisk_document_attach where doc_id = "
				+ docId;
		ResultSet rs = null;
		try {
			rs = conn.executeQuery(sql);
			while (rs != null && rs.next()) {
				id = rs.getInt(1);
				re = del();
			}
		} catch (Exception e) {
			logger.error("getIdByDocId:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	/**
	 * 文件重命名
	 * 
	 * @param newName
	 * @return
	 */
	public boolean changeName2(String newName) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String year = String.valueOf(calendar.get(Calendar.YEAR));
		String month = "";
		String day = "";
		ResultSet rs = null;

		long id_new = SequenceManager
				.nextID(SequenceManager.OA_DOCUMENT_NETDISK_ATTACHMENT);
		// System.out.println(id);
		int month_int = calendar.get(Calendar.MONTH) + 1;
		if (month_int < 10) {
			month = String.valueOf(month_int);
			month = "0" + month;
		} else {
			month = String.valueOf(month_int);
		}
		int day_int = calendar.get(Calendar.DAY_OF_MONTH);
		if (day_int < 10) {
			day = String.valueOf(Calendar.DAY_OF_MONTH);
			day = "0" + day;
		} else {
			day = String.valueOf(Calendar.DAY_OF_MONTH);
		}
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		String time_tail = year + month + day + hour + minute + second;
		// System.out.println(year+month+day+hour+minute+second);
		boolean re = false;
		boolean res = false;
		boolean ree = false;
		String sql = "SELECT doc_id, name, fullpath, diskname, visualpath, orders, page_num, file_size, ext, uploadDate, publicShareDir,PUBLIC_SHARE_DEPTS,USER_NAME,IS_CURRENT,VERSION_DATE,IS_SHARE,IS_EDIT,SHARE_USER,IS_DELETED,DELETE_DATE,SHARE_ID,IS_REFUSED,IS_EDITED FROM netdisk_document_attach WHERE id=?";
		Conn conn = new Conn(connname);
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				docId = rs.getInt(1);
				name = rs.getString(2);
				name += "_" + time_tail;
				fullPath = rs.getString(3);
				diskName = rs.getString(4);
				visualPath = rs.getString(5);
				orders = rs.getInt(6);
				pageNum = rs.getInt(7);
				size = rs.getLong(8);
				ext = rs.getString(9);
				uploadDate = rs.getTimestamp(10);
				publicShareDir = rs.getString(11);
				publicShareDepts = StrUtil.getNullStr(rs.getString(12));
				userName = StrUtil.getNullStr(rs.getString(13));
				// 以下用于网盘
				current = false;
				versionDate = rs.getTimestamp(15);
				share = rs.getInt(16) == 1;
				edit = rs.getInt(17) == 1;
				shareUser = StrUtil.getNullStr(rs.getString(18));
				deleted = 0;
				deleteDate = rs.getTimestamp(20);
				shareId = rs.getLong(21);
				refused = rs.getInt(22) == 1;
				edited = rs.getInt(23) == 1;

				loaded = true;

				sql = "insert into netdisk_document_attach (id,doc_id, name, fullpath, diskname, visualpath, orders, page_num, file_size, ext, publicShareDir,PUBLIC_SHARE_DEPTS,USER_NAME,IS_CURRENT,VERSION_DATE,IS_SHARE,IS_EDIT,SHARE_USER,IS_DELETED,DELETE_DATE,SHARE_ID,IS_REFUSED,IS_EDITED) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement pstmt_new = conn.prepareStatement(sql);
				pstmt_new.setLong(1, id_new);
				pstmt_new.setInt(2, docId);
				pstmt_new.setString(3, name);
				pstmt_new.setString(4, fullPath);
				pstmt_new.setString(5, diskName);
				pstmt_new.setString(6, visualPath);
				pstmt_new.setInt(7, orders);
				pstmt_new.setInt(8, pageNum);
				pstmt_new.setLong(9, size);
				pstmt_new.setString(10, ext);

				pstmt_new.setString(11, publicShareDir);
				pstmt_new.setString(12, publicShareDepts);
				pstmt_new.setString(13, userName);
				// IS_CURRENT=?,VERSION_DATE=?,IS_SHARE=?,IS_EDIT=?,SHARE_USER=?,IS_DELETED=?,DELETE_DATE=?,SHARE_ID=?,IS_REFUSED=?
				pstmt_new.setInt(14, current ? 1 : 0);
				if (versionDate == null)
					pstmt_new.setTimestamp(15, null);
				else
					pstmt_new.setTimestamp(15, new Timestamp(versionDate
							.getTime()));

				pstmt_new.setInt(16, share ? 1 : 0);
				pstmt_new.setInt(17, edit ? 1 : 0);
				pstmt_new.setString(18, shareUser);
				pstmt_new.setInt(19, deleted);
				pstmt_new.setTimestamp(20, new Timestamp(new java.util.Date()
						.getTime()));
				pstmt_new.setLong(21, shareId);
				pstmt_new.setInt(22, refused ? 1 : 0);
				pstmt_new.setInt(23, edited ? 1 : 0);
				ree = conn.executePreUpdate() >= 1 ? true : false;

				sql = "update netdisk_document_attach set name = "
						+ StrUtil.sqlstr(newName) + " where id = ?";
				PreparedStatement pstmt_now = conn.prepareStatement(sql);
				pstmt_now.setLong(1, id);
				res = conn.executePreUpdate() >= 1 ? true : false;
			}
			re = res && ree;
			pstmt.close();

		} catch (Exception e) {
			logger.error("changeName_Inset:" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	/**
	 * 文件重命名后得到新的ID
	 */
	public int theNewId(String newName) {
		int newAttId = 0;
		ResultSet rs = null;
		String sql = "select id from netdisk_document_attach where name = "
				+ StrUtil.sqlstr(newName) + " order by uploadDate desc";
		Conn conn = new Conn(connname);

		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				newAttId = rs.getInt(1);
			}
		} catch (SQLException e) {
			logger.error("theNewId:" + e.getMessage());
			e.printStackTrace();
		}

		return newAttId;
	}

	/**
	 * 文件重命名
	 * 
	 * @param newName
	 * @return
	 */
	public boolean changeName(String newName, int attDocId) {
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		boolean re = false;
		ResultSet rs = null;
		String newExt = newName.substring(newName.lastIndexOf(".") + 1);
		if (cfg.getBooleanProperty("isRenameBak")) {
			try {
				loadFromDb();
				Conn conn = new Conn(connname);
				String sql = "select id from netdisk_document_attach where doc_id = "
						+ attDocId
						+ " and diskname = "
						+ StrUtil.sqlstr(newName);
				rs = conn.executeQuery(sql);
				if (rs != null && rs.next()) {
					return false;
				}
				Config config = new Config();
				String filePath = Global.getRealPath()
						+ config.get("file_netdisk") + "/";
				File file = new File(filePath + visualPath + "/" + name);
				if (versionDate == null) {
					versionDate = new Date(file.lastModified());
				}
				String vdate = DateUtil.format(versionDate, "yyyyMMddHHmmss");
				String oldName = name + "_" + vdate;
				String bakName = name;
				String user_name = userName;
				String theFilePath = visualPath;
				deleted = 1;
				current = true; // 只有current为true才会出现在回收站里,这里做重命名的备份就是为了能在回收站里查找到
				ext = newExt;
				// name = oldName;
				diskName = oldName;
				re = save();

				if (re) {
					// 重命名目录下真实文件的名称
					file = new File(filePath + visualPath + "/" + name);
					if (file.exists()) {
						re = file.renameTo(new File(filePath + visualPath + "/"
								+ oldName));
						if (file.exists()) {
							re = file.delete();
						}
					}

					if (re) {
						deleted = 0;
						current = true;
						name = newName;
						diskName = newName;
						re = create();

						if (re) {
							// 重命名目录下真实文件的名称
							re = FileUtil.CopyFile(filePath + visualPath + "/"
									+ oldName, filePath + visualPath + "/"
									+ newName);
						}
						if (re) {
							delAttLogic(user_name, bakName, theFilePath);
						}
					}
				}
			} catch (Exception e) {
				logger.error("changeName_Inset:" + e.getMessage());
				e.printStackTrace();
			}
		} else if (!(cfg.getBooleanProperty("isreNameBak"))) {
			try {
				Config config = new Config();
				String filePath = Global.getRealPath()
						+ config.get("file_netdisk") + "/";

				loadFromDb();
				if (versionDate == null) {
					versionDate = new Date();
				} else if (versionDate != null) {
					File file = new File(filePath + visualPath + "/" + name);
					versionDate = new Date(file.lastModified());
				}
				String vdate = DateUtil.format(versionDate, "yyyyMMddHHmmss");
				String oldName = name + "_" + vdate;
				int fileId = id;
				deleted = 1;
				current = false;
				// name = oldName;
				diskName = oldName;
				re = save();
				if (re) {
					// 重命名目录下真实文件的名称
					File file = new File(filePath + visualPath + "/" + name);
					if (file.exists()) {
						re = file.renameTo(new File(filePath + visualPath + "/"
								+ oldName));
						if (file.exists()) {
							re = file.delete();
						}
					}
					if (re) {
						deleted = 0;
						current = true;
						name = newName;
						diskName = newName;
						re = create();
						if (re) {
							// 重命名目录下真实文件的名称
							re = FileUtil.CopyFile(filePath + visualPath + "/"
									+ oldName, filePath + visualPath + "/"
									+ newName);
						}
					}

					if (re) {
						id = fileId;
						del();
						file = new File(filePath + visualPath + "/" + oldName);
						if (file.exists()) {
							re = file.delete();
						}
					}
				}
			} catch (Exception e) {
				logger.error("changeName_Inset:" + e.getMessage());
				e.printStackTrace();
			}
		}
		return re;

	}

	/**
	 * 找到文件所在目录的文件夹
	 * 
	 * @return
	 */
	public String getParentDir() {
		String visualPath = getVisualPath();
		int x = visualPath.indexOf("/");
		if (x < 0) {
			String parentDir = "全部文件";
			return parentDir;
		} else {
			String visualPaths[] = visualPath.split("/");
			int i = visualPaths.length;
			String parentDir = visualPaths[i - 1];
			return parentDir;
		}
	}

	/**
	 * 通过DOCID找到该文件的文件夹CODE；
	 * 
	 * @return
	 */
	public String getCodeByDocId() {
		String sql = "Select code from netdisk_directory where doc_id = "
				+ docId;
		ResultSet rs = null;
		String dirCode = "";
		Conn conn = new Conn(connname);
		try {
			rs = conn.executeQuery(sql);
			if (rs != null && rs.next()) {
				dirCode = rs.getString(1);
			}
			if (dirCode.equals("")) {
				dirCode = userName;
			}
		} catch (Exception e) {
			logger.error("getCodeByDocId:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		return dirCode;
	}

	/**
	 * 逻辑删除
	 * 
	 * @return
	 */
	public boolean delAttLogic() {
		// 为逻辑删除前的原数据修改diskname 以便后面逻辑删除一次性完成。
		// 删除文件之前先把当前版本的文件备份为文件名_版本时间戳
		if (!isLoaded()) {
			loadFromDb();
		}
		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		File file = new File(filePath + visualPath + "/" + name);
		if (versionDate == null) {
			versionDate = new Date(file.lastModified());
		}
		String vdate = DateUtil.format(versionDate, "yyyyMMddHHmmss");
		String FilePath = visualPath;
		diskName = name + "_" + vdate;
		deleted = 1;
		boolean re = save();
		File oldFile = new File(filePath + visualPath + "/" + diskName);
		if (re) {
			// 逻辑删除（重命名为文件名_版本时间戳）对应文件夹下的文件
			if (oldFile.exists()) {
				oldFile.delete();
			}
			file.renameTo(new File(filePath + visualPath + "/" + diskName));
		}
		// 把所有对应原文件的版本文件全部逻辑删除,即is_current=0时。
		String theName = name;
		Conn conn = new Conn(connname);
		String sql = "update netdisk_document_attach set IS_DELETED = 1,DELETE_DATE="
				+ StrUtil.sqlstr(DateUtil.format(new Date(),
						"yyyy-MM-dd HH:mm:ss"))
				+ ",IS_CURRENT = 0 where name ="
				+ StrUtil.sqlstr(theName)
				+ " and is_deleted = 0 and visualpath = "
				+ StrUtil.sqlstr(FilePath)
				+ " and USER_NAME = "
				+ StrUtil.sqlstr(userName) + " and is_current=0";
		try {
			re = conn.executeUpdate(sql) >= 0 ? true : false;
		} catch (Exception e) {
			logger.error("delLogic:" + e.getMessage());
		}
		return re;
	}
	
	/**
	 * 逻辑删除
	 * 在直接删除文件夹的情况下的逻辑删除  is_deleted = 2
	 * @return
	 */
	public boolean delAttLogic(int thisid) {
		// 为逻辑删除前的原数据修改diskname 以便后面逻辑删除一次性完成。
		// 删除文件之前先把当前版本的文件备份为文件名_版本时间戳
		id = thisid;
		if (!isLoaded()) {
			loadFromDb();
		}
		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		File file = new File(filePath + visualPath + "/" + name);
		if (versionDate == null) {
			versionDate = new Date(file.lastModified());
		}
		String vdate = DateUtil.format(versionDate, "yyyyMMddHHmmss");
		String FilePath = visualPath;
		diskName = name + "_" + vdate;
		deleted = 2;
		boolean re = save();
		File oldFile = new File(filePath + visualPath + "/" + diskName);
		if (re) {
			// 逻辑删除（重命名为文件名_版本时间戳）对应文件夹下的文件
			if (oldFile.exists() && file.exists()) {
				oldFile.delete();
			}
			file.renameTo(new File(filePath + visualPath + "/" + diskName));
		}
		// 把所有对应原文件的版本文件全部逻辑删除,即is_current=0时。
		String theName = name;
		Conn conn = new Conn(connname);
		String sql = "update netdisk_document_attach set IS_DELETED = 1,DELETE_DATE="
				+ StrUtil.sqlstr(DateUtil.format(new Date(),
						"yyyy-MM-dd HH:mm:ss"))
				+ ",IS_CURRENT = 0 where name ="
				+ StrUtil.sqlstr(theName)
				+ " and is_deleted = 0 and visualpath = "
				+ StrUtil.sqlstr(FilePath)
				+ " and USER_NAME = "
				+ StrUtil.sqlstr(userName) + " and is_current=0";
		try {
			re = conn.executeUpdate(sql) >= 0 ? true : false;
		} catch (Exception e) {
			logger.error("delLogic:" + e.getMessage());
		}
		return re;
	}

	/**
	 * 历史版本 获取数据
	 * 
	 * @param attId
	 * @return
	 */
	public List<NetDiskBean> queryHistoryLog(int attId) {
		List<NetDiskBean> hisortyLogList = new ArrayList<NetDiskBean>();
		Attachment att = new Attachment(attId);
		String sql = "select id,name,is_current,file_size,version_date from netdisk_document_attach where name ="
				+ StrUtil.sqlstr(att.getName())
				+ "and user_name = "
				+ StrUtil.sqlstr(att.getUserName())
				+ " and page_num=1 and visualPath = "
				+ StrUtil.sqlstr(att.getVisualPath())
				+ " and is_deleted = 0 order by is_current desc,version_date desc";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			ResultRecord record = null;
			while (ri.hasNext()) {
				record = (ResultRecord) ri.next();
				int id = record.getInt("id");
				String name = record.getString("name");
				int current = record.getInt("is_current");
				long size = record.getLong("file_size");
				String versionDate = DateUtil.format(record
						.getDate("version_date"), "yyyy-MM-dd HH:mm");
				NetDiskBean historyLogBean = new NetDiskBean(id, name, current,
						size, versionDate);
				hisortyLogList.add(historyLogBean);
			}

		} catch (SQLException e) {
			logger.error("queryHistoryLog:" + e.getMessage());
		}
		return hisortyLogList;
	}

	/**
	 * 返回所有历史版本的 json数据
	 * 
	 * @param attId
	 * @return
	 */
	public JSONObject queryMyHistoryLogByAjax(int attId) {
		List<NetDiskBean> list = queryHistoryLog(attId);
		JSONArray historyLogs = JSONArray.fromObject(list);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", 1);
		jsonObject.put("message", "成功");
		jsonObject.put("total", list.size());
		jsonObject.put("historyLogs", historyLogs);
		return jsonObject;

	}

	/**
	 * 得到预览大图的图片路径
	 * 
	 */
	public JSONObject getImgSrc(int attId) {
		Attachment att = new Attachment(attId);
		Config config = new Config();
		String realPath = Global.getRealPath() + "/"
				+ config.get("file_netdisk") + "/" + att.getVisualPath() + "/"
				+ att.getName();
		// String img = "<img src = '" + imgSrc + "'/>";
		File input = new File(realPath);
		JSONObject jsonObject = new JSONObject();
		try {
			BufferedImage image = ImageIO.read(input);
			if (image == null) {
				jsonObject.put("ret", 0);
				jsonObject.put("msg", "图片不存在！");
			} else {
				int w = image.getWidth();
				int h = image.getHeight();
				jsonObject.put("ret", 1);
				jsonObject.put("width", w);
				jsonObject.put("height", h);
				jsonObject.put("downloadUrl","getfile.jsp?op=1&id="+attId);
			}
		} catch (Exception e) {
			logger.error("getImgSrc:" + e.getMessage());
			jsonObject.put("ret", 0);
			jsonObject.put("msg", "getImgSrc:图片不存在");
		}
		return jsonObject;
	}

	/**
	 * 重命名时需要的逻辑删除
	 * 
	 * @param userName
	 * @param oldName
	 * @param FilePath
	 * @return
	 */
	public boolean delAttLogic(String userName, String oldName, String FilePath) {
		// 为逻辑删除前的原数据修改diskname 以便后面逻辑删除一次性完成。
		if (!isLoaded()) {
			loadFromDb();
		}
		// Config config = new Config();
		// String filePath = Global.getRealPath() + config.get("file_netdisk") +
		// "/";
		// if (versionDate == null) {
		// File file = new File(filePath + visualPath + "/" + name);
		// versionDate = new Date(file.lastModified());
		// }
		String vdate = DateUtil.format(new Date(), "yyyyMMddHHmmss");
		diskName = name + "_" + vdate;
		name = oldName;
		visualPath = FilePath;
		// save();
		// 把所有对应原文件的版本文件全部逻辑删除。
		boolean re = false;
		Conn conn = new Conn(connname);
		String sql = "update netdisk_document_attach set IS_DELETED = 1,DELETE_DATE='"
				+ vdate
				+ "',IS_CURRENT = 0 where name ="
				+ StrUtil.sqlstr(oldName)
				+ " and is_deleted = 0 and visualpath = "
				+ StrUtil.sqlstr(FilePath)
				+ " and USER_NAME = "
				+ StrUtil.sqlstr(userName);
		try {
			re = conn.executeUpdate(sql) >= 1 ? true : false;
		} catch (Exception e) {
			logger.error("delLogic:" + e.getMessage());
		}
		return re;
	}

	/**
	 * txt预览
	 */
	public String getTxtInfo(Attachment att) {
		String txtName = att.getName();
		String type = att.getExt();
		String visualPath = att.getVisualPath();
		Config configPath = new Config();
		String txtPath = Global.getRealPath() + configPath.get("file_netdisk")
				+ "/";
		File file = new File(txtPath + visualPath + "/" + txtName);
		String filePath = file + "";
		String txtResult = "";
		try {
			if (type.equals("txt")) {
				txtResult = UtilTools.getTextFromTxt(filePath);
			} else {
				txtResult = UtilTools.getTextFormPdf(filePath);
			}
			return txtResult;
		} catch (Exception e) {
			logger.error("getTxtInfo:" + e.getMessage());
		}
		return txtResult;
	}

	/**
	 * 还原过期版本
	 * 
	 * @param attId
	 * @return
	 */
	public boolean restoreCurrent(int attId) {
		boolean re = false;
		loadFromDb();
		deleted = 0;
		current = false;
		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		File file = new File(filePath + visualPath + "/" + name);
		if (versionDate == null) {
			versionDate = new Date(file.lastModified());
		}
		String vdate = DateUtil.format(new Date(), "yyyyMMddHHmmss");
		diskName = name + "_" + vdate;
		deleteDate = new Date();
		String oldVisualPath = visualPath;
		int oldDocId = docId;
		Attachment att = new Attachment(attId);
		long oldSize = att.getSize();
		Date oldVisDate = att.getVersionDate();
		try {
			re = save();
			if (re) {
				deleted = 0;
				current = true;
				diskName = name;
				visualPath = oldVisualPath;
				// Leaf lf = new Leaf();
				// docId = lf.getCurrentDocId(userName);
				docId = oldDocId;
				size = oldSize;
				versionDate = oldVisDate;
				re = create();
			}
		} catch (Exception e) {
			logger.error("restoreCurrent:" + e.getMessage());
		}

		return re;
	}

	// 还原版本文件时对最新文件进行改名
	public boolean renameCurrent() {
		boolean re = false;
		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		File file = new File(filePath + visualPath + "/" + name);
		File fl = new File(filePath + visualPath + "/" + diskName);
		if (versionDate == null) {
			versionDate = new Date(file.lastModified());
		}
		// String vdate = diskName;
		// vdate = name + "_"+vdate;
		if (file.exists()) {
			re = file
					.renameTo(new File(filePath + visualPath + "/" + diskName));
			if (fl.exists()) {
				re = file.delete();
			}
		}
		return re;
	}

	// 还原版本文件时复制出最新文件
	public boolean getCurrentCopy() {
		boolean re = false;
		loadFromDb();
		Config config = new Config();
		String filePath = Global.getRealPath() + config.get("file_netdisk")
				+ "/";
		try {
			re = FileUtil.CopyFile(filePath + visualPath + "/" + diskName,
					filePath + visualPath + "/" + name);
		} catch (Exception e) {
			logger.error("getCurrentCopy:" + e.getMessage());
		}
		return re;
	}
	
	// 预览图片左右翻(public)
	public int showNextImg(int attId, String arrow, int isImgSearch) {
		Attachment att = new Attachment(attId);
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		String exts = cfg.getProperty("exttype_1");
		String[] ss = exts.split(",");
		String newExtFirst = "";
		String newExtElse = "";
		int temp = 0;
		int temp2 = 0;
		for (int i = 0; i < ss.length; i++) {
			if (i == 0) {
				newExtFirst = "'" + ss[i] + "'";
			} else {
				newExtElse += ",'" + ss[i] + "'";
			}
		}
		String sql = "";
		if(isImgSearch == 0){
			 sql = "select id from netdisk_document_attach where visualPath = "
				+ StrUtil.sqlstr(att.getVisualPath())
				+ " and user_name = "
				+ StrUtil.sqlstr(att.getUserName())
				+ "and is_deleted = 0 and ext in ("
				+ newExtFirst + newExtElse + ")";
		}else{
			sql = "select id from netdisk_document_attach where user_name = "
				+ StrUtil.sqlstr(att.getUserName())
				+ "and is_deleted = 0 and ext in ("
				+ newExtFirst + newExtElse + ")";
		}
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.getRows() == 1) {
				return attId;
			}
			int index = 0;
			boolean findDirect = false;
			boolean isNextId = false;
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int id = rr.getInt(1);
				if (arrow.equals("left")) {
					// id与当前预览id一致时
					if (attId == id) {
						if (index == 0) {
							// 当前图片为第一张时，返回最后一张图片的id
							findDirect = true;
							continue;
						} else {
							// 其他情况返回前一张图片的id
							return temp;
						}
					}
					// 当前id为第一个时，直接去找最后一张图片的id
					if (findDirect) {
						if (!ri.hasNext()) {
							return id;
						}
					} else {
						temp = id;
						index++;
					}
				} else {
					// id与当前预览id一致时
					if (attId == id) {
						if (!ri.hasNext()) {
							// 当前图片为最后一张图片时，返回第一张图片的id
							return temp;
						} else {
							// 其他情况返回后一张图片的id
							isNextId = true;
							continue;
						}
					}
					if (isNextId) {
						return id;
					} else {
						// 记录第一张图片的id
						if (index++ == 0) {
							temp = id;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("showLeftImg:" + e.getMessage());
		} finally {
			jt.close();
		}
		return temp2;
	}

	// 预览图片左右翻(self 与public效果一样)
	public int showNextImgSelf(int attId, String arrow, int isImgSearch) {
		Attachment att = new Attachment(attId);
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		String exts = cfg.getProperty("exttype_1");
		String[] ss = exts.split(",");
		String newExtFirst = "";
		String newExtElse = "";
		int temp = 0;
		int temp2 = 0;
		for (int i = 0; i < ss.length; i++) {
			if (i == 0) {
				newExtFirst = "'" + ss[i] + "'";
			} else {
				newExtElse += ",'" + ss[i] + "'";
			}
		}
		String sql = "";
		if(isImgSearch == 0){
			 sql = "select id from netdisk_document_attach where visualPath = "
				+ StrUtil.sqlstr(att.getVisualPath())
				+ " and user_name = "
				+ StrUtil.sqlstr(att.getUserName())
				+ " and ext in ("
				+ newExtFirst + newExtElse + ")";
		}else{
			sql = "select id from netdisk_document_attach where user_name = "
				+ StrUtil.sqlstr(att.getUserName())
				+ " and ext in ("
				+ newExtFirst + newExtElse + ")";
		}
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.getRows() == 1) {
				return attId;
			}
			int[] ids = new int[ri.getRows()];
			int k = 0;
			int s = 0;
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				ids[k] = rr.getInt(1);
				if(arrow.equals("left")){
					if(attId == ids[k]){
						temp2 = temp;
					}
					temp=rr.getInt(1);
				}else if(arrow.equals("right")){
					if(attId == ids[k]){
						if (!ri.hasNext()) {
							s = 0;
						} else {
							s = k+1;
						}
					}
				}
				k++;
			}
			if(arrow.equals("left")){
				if(temp2!=0){
					return temp2;
				}else{
					temp2 = ids[k-1];
					return temp2;
				}
			}else{
				if(s==0){
					temp2 = ids[0];
				}else{
					temp2 = ids[s];
				}
			}	
		}catch (Exception e) {
			logger.error("showLeftImg:" + e.getMessage());
		} finally {
			jt.close();
		}
		return temp2;
	}
	

	public boolean save() {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement pstmt = conn.prepareStatement(SAVE);
			pstmt.setInt(1, docId);
			pstmt.setString(2, name);
			pstmt.setString(3, fullPath);
			pstmt.setString(4, diskName);
			pstmt.setString(5, visualPath);
			pstmt.setInt(6, orders);
			pstmt.setInt(7, pageNum);
			pstmt.setString(8, publicShareDir);
			pstmt.setString(9, publicShareDepts);

			// IS_CURRENT=?,VERSION_DATE=?,IS_SHARE=?,IS_EDIT=?,SHARE_USER=?,IS_DELETED=?,DELETE_DATE=?,SHARE_ID=?,IS_REFUSED=?
			pstmt.setInt(10, current ? 1 : 0);
			if (versionDate == null)
				pstmt.setTimestamp(11, null);
			else
				pstmt.setTimestamp(11, new Timestamp(versionDate.getTime()));

			pstmt.setInt(12, share ? 1 : 0);
			pstmt.setInt(13, edit ? 1 : 0);
			pstmt.setString(14, shareUser);
			pstmt.setInt(15, deleted);
			pstmt.setTimestamp(16,
					new Timestamp(new java.util.Date().getTime()));
			pstmt.setLong(17, shareId);
			pstmt.setInt(18, refused ? 1 : 0);
			pstmt.setLong(19, size);

			pstmt.setInt(20, id);
			re = conn.executePreUpdate() >= 1 ? true : false;
		} catch (SQLException e) {
			logger.error("save:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDocId() {
		return this.docId;
	}

	public void setDocId(int di) {
		this.docId = di;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiskName() {
		return this.diskName;
	}

	public void setDiskName(String dn) {
		this.diskName = dn;
	}

	public String getFullPath() {
		return this.fullPath;
	}

	public void setFullPath(String f) {
		this.fullPath = f;
	}

	public String getVisualPath() {
		return this.visualPath;
	}

	public int getOrders() {
		return orders;
	}

	public int getPageNum() {
		return pageNum;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public long getSize() {
		return size;
	}

	public String getExt() {
		return ext;
	}

	public void setVisualPath(String vp) {
		this.visualPath = vp;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public void setPublicShareDir(String publicShareDir) {
		this.publicShareDir = publicShareDir;
	}

	public void setPublicShareDepts(String publicShareDepts) {
		this.publicShareDepts = publicShareDepts;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public static String getIcon(String ext) {
		if (ext == null)
			return "default.png";
		if (ext.equals("gif") || ext.equals("jpg") || ext.equals("png") 
				|| ext.equals("jpeg") || ext.equals("bmp") || ext.equals("jpe"))
			return "bmp.png";
		else if (ext.equals("doc") || ext.equals("docx") || ext.equals("wps")
				|| ext.equals("wpt"))
			return "doc.png";
		else if (ext.equals("pdf"))
			return "pdf.png";
		else if (ext.equals("apk"))
			return "apk.png";
		else if (ext.equals("txt"))
			return "txt.png";
		else if (ext.equals("xls") || ext.equals("xlsx") || ext.equals("et"))
			return "xls.png";
		else if (ext.equals("html") || ext.equals("htm"))
			return "html.png";
		else if (ext.equals("ppt") || ext.equals("pptx") || ext.equals("dps"))
			return "ppt.png";
		else if (ext.equals("rar") || ext.equals("zip"))
			return "zip.png";
		else if (ext.equals("wmv") || ext.equals("wma") || ext.equals("mp3"))
			return "mp3.png";
		else if (ext.equals("avi") || ext.equals("mov") || ext.equals("avi")
				|| ext.equals("rmvb") || ext.equals("flv") || ext.equals("wmv")
				|| ext.equals("3gp") || ext.equals("mp4"))
			return "mp4.png";
		return "default.png";
	}

	public String getIcon() {
		return getIcon(ext);
	}

	public void loadFromDb() {
		Conn conn = new Conn(connname);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(LOAD);
			pstmt.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				docId = rs.getInt(1);
				name = rs.getString(2);
				fullPath = rs.getString(3);
				diskName = rs.getString(4);
				visualPath = rs.getString(5);
				orders = rs.getInt(6);
				pageNum = rs.getInt(7);
				size = rs.getInt(8);
				ext = rs.getString(9);
				uploadDate = rs.getTimestamp(10);
				publicShareDir = rs.getString(11);
				publicShareDepts = StrUtil.getNullStr(rs.getString(12));
				userName = StrUtil.getNullStr(rs.getString(13));

				// 以下用于网盘
				current = rs.getInt(14) == 1;
				versionDate = rs.getTimestamp(15);
				share = rs.getInt(16) == 1;
				edit = rs.getInt(17) == 1;
				shareUser = StrUtil.getNullStr(rs.getString(18));
				deleted = rs.getInt(19);
				deleteDate = rs.getTimestamp(20);
				shareId = rs.getLong(21);
				refused = rs.getInt(22) == 1;
				edited = rs.getInt(23) == 1;

				loaded = true;
			}
		} catch (SQLException e) {
			logger.error("loadFromDb:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	public void loadFromDbByOrders() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			String LOADBYORDERS = "SELECT id, name, fullpath, diskname, visualpath, file_size, ext, uploadDate, publicShareDir, PUBLIC_SHARE_DEPTS, USER_NAME,IS_CURRENT,VERSION_DATE,IS_SHARE,IS_EDIT,SHARE_USER,IS_DELETED,DELETE_DATE,SHARE_ID,IS_REFUSED,IS_EDITED FROM netdisk_document_attach WHERE orders=? and doc_id=? and page_num=?";
			pstmt = conn.prepareStatement(LOADBYORDERS);
			pstmt.setInt(1, orders);
			pstmt.setInt(2, docId);
			pstmt.setInt(3, pageNum);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				id = rs.getInt(1);
				name = rs.getString(2);
				fullPath = rs.getString(3);
				diskName = rs.getString(4);
				visualPath = rs.getString(5);
				size = rs.getInt(6);
				ext = rs.getString(7);
				uploadDate = rs.getTimestamp(8);
				publicShareDir = StrUtil.getNullStr(rs.getString(9));
				publicShareDepts = StrUtil.getNullStr(rs.getString(10));
				userName = StrUtil.getNullStr(rs.getString(11));

				// 以下用于网盘
				current = rs.getInt(12) == 1;
				versionDate = rs.getTimestamp(13);
				share = rs.getInt(14) == 1;
				edit = rs.getInt(15) == 1;
				shareUser = StrUtil.getNullStr(rs.getString(16));
				deleted = rs.getInt(17);
				deleteDate = rs.getTimestamp(18);
				shareId = rs.getLong(19);
				refused = rs.getInt(20) == 1;
				edited = rs.getInt(21) == 1;

				loaded = true;
			}
		} catch (SQLException e) {
			logger.error("loadFromDbByOrders:" + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	public java.util.Date getUploadDate() {
		return uploadDate;
	}

	public String getPublicShareDir() {
		return publicShareDir;
	}

	public String getPublicShareDepts() {
		return publicShareDepts;
	}

	public String getUserName() {
		return userName;
	}

	public ListResult listResult(String listsql, int curPage, int pageSize)
			throws ErrMsgException {
		int total = 0;
		ResultSet rs = null;
		Vector result = new Vector();

		ListResult lr = new ListResult();
		lr.setTotal(total);
		lr.setResult(result);

		Conn conn = new Conn(connname);
		try {
			// 取得总记录条数
			String countsql = SQLFilter.getCountSql(listsql);
			rs = conn.executeQuery(countsql);
			if (rs != null && rs.next()) {
				total = rs.getInt(1);
			}
			if (rs != null) {
				rs.close();
				rs = null;
			}

			if (total != 0)
				conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

			rs = conn.executeQuery(listsql);
			if (rs == null) {
				return lr;
			} else {
				rs.setFetchSize(pageSize);
				int absoluteLocation = pageSize * (curPage - 1) + 1;
				if (rs.absolute(absoluteLocation) == false) {
					return lr;
				}
				do {
					Attachment ug = getAttachment(rs.getInt(1));
					result.addElement(ug);
				} while (rs.next());
			}
		} catch (SQLException e) {
			logger.error(e.getMessage());
			throw new ErrMsgException("数据库出错！");
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}

		lr.setResult(result);
		lr.setTotal(total);
		return lr;
	}

	public void setUploadDate(java.util.Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	/**
	 * 是否以链接方式被共享
	 * 
	 * @return
	 */
	public boolean isLinkShared() {
		String sql = "select id from netdisk_public_attach where att_id=" + id;
		try {
			return listResult(sql, 1, 1).getResult().size() > 0;
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private int orders = 0;
	private int pageNum = 1;
	private boolean loaded = false;
	private long size = 0;
	private String ext;
	private java.util.Date uploadDate;
	private String publicShareDir;
	private String publicShareDepts;
	// private String userName;
}
