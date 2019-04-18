package com.redmoon.oa.netdisk;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;

public class PublicShare extends ObjectDb {
	private int id;
	private String name;
	private String userName;
	private String isCurrent;
	private java.util.Date createDate;
	private java.util.Date versionDate;
	private String isShare;
	private String isEdit;
	private String visualPath;
	private String shareUser;
	private String isDelete;
	private java.util.Date deleteDate;
	private int file_size;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFile_size(int file_size) {
		this.file_size = file_size;
	}

	public int getFile_size() {
		return file_size;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(String isCurrent) {
		this.isCurrent = isCurrent;
	}

	public java.util.Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(java.util.Date createDate) {
		this.createDate = createDate;
	}

	public java.util.Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(java.util.Date versionDate) {
		this.versionDate = versionDate;
	}

	public String getIsShare() {
		return isShare;
	}

	public void setIsShare(String isShare) {
		this.isShare = isShare;
	}

	public String getIsEdit() {
		return isEdit;
	}

	public void setIsEdit(String isEdit) {
		this.isEdit = isEdit;
	}

	public String getVisualPath() {
		return visualPath;
	}

	public void setVisualPath(String visualPath) {
		this.visualPath = visualPath;
	}

	public String getShareUser() {
		return shareUser;
	}

	public void setShareUser(String shareUser) {
		this.shareUser = shareUser;
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}

	public java.util.Date getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(java.util.Date deleteDate) {
		this.deleteDate = deleteDate;
	}

	public PublicShare() {
		init();
	}

	public PublicShare(int id) {
		this.id = id;
		init();
		load();
	}

	@Override
	public void initDB() {
		tableName = "net_disk";
		primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
		objectCache = new PublicShareCache(this);
		isInitFromConfigDB = false;

		// QUERY_CREATE =
		// "insert into " + tableName +
		// " (user_name,file_path,file_name,create_date,work_id) values (?,?,?,?,?)";
		QUERY_SAVE = "update " + tableName
				+ " set name=?,is_deleted=0 where id=?";
		// QUERY_LIST =
		// "select id from " + tableName + " where is_share=0 order by id desc";
		// QUERY_DEL = "delete from " + tableName + " where id=?";
		QUERY_LOAD = "select id,name,user_name,is_current,create_date,version_date,is_share,is_edit,visual_path,share_user,is_deleted,delete_date from "
				+ tableName + " where id=?";

	}

	public PublicShare getPublicShare(int id) {
		return (PublicShare) getObjectDb(new Integer(id));
	}

	/**
	 * del
	 * 
	 * @return boolean
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	@Override
	public boolean del() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
			ps.setInt(1, id);
			re = conn.executePreUpdate() == 1 ? true : false;
			if (re) {
				PublicShareCache rc = new PublicShareCache(this);
				primaryKey.setValue(new Integer(id));
				rc.refreshDel(primaryKey);
			}
		} catch (SQLException e) {
			logger.error("del: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return re;
	}

	/**
	 * 
	 * @param pk
	 *            Object
	 * @return Object
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	@Override
	public ObjectDb getObjectRaw(PrimaryKey pk) {
		return new PublicShare(pk.getIntValue());
	}

	/**
	 * load
	 * 
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	@Override
	public void load() {
		ResultSet rs = null;
		Conn conn = new Conn(connname);
		try {
			// QUERY_LOAD = "select name,reason,direction,type,myDate from " +
			// tableName + " where id=?";
			PreparedStatement ps = conn.prepareStatement(QUERY_LOAD);
			ps.setInt(1, id);
			rs = conn.executePreQuery();
			if (rs != null && rs.next()) {
				id = rs.getInt(1);
				name = rs.getString(2);
				userName = rs.getString(3);
				isCurrent = rs.getString(4);
				createDate = rs.getTimestamp(5);
				versionDate = rs.getTimestamp(6);
				isShare = rs.getString(7);
				isEdit = rs.getString(8);
				visualPath = rs.getString(9);
				shareUser = rs.getString(10);
				isDelete = rs.getString(11);
				deleteDate = rs.getTimestamp(12);
				loaded = true;
				primaryKey.setValue(new Integer(id));
			}
		} catch (SQLException e) {
			logger.error("load: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
	}

	/**
	 * save
	 * 
	 * @return boolean
	 * @throws ErrMsgException
	 * @throws ResKeyException
	 * @todo Implement this cn.js.fan.base.ObjectDb method
	 */
	@Override
	public boolean save() throws ErrMsgException {
		Conn conn = new Conn(connname);
		boolean re = false;
		try {
			PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
			String newName = name.substring(0, name.length() - 15);
			// ps.setTimestamp(1, new Timestamp(date.getTime()));
			ps.setString(1, newName);
			ps.setInt(2, id);
			re = conn.executePreUpdate() == 1 ? true : false;

			if (re) {
				PublicShareCache rc = new PublicShareCache(this);
				primaryKey.setValue(new Integer(id));
				rc.refreshSave(primaryKey);
			}
		} catch (SQLException e) {
			logger.error("save: " + e.getMessage());
		} finally {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		}
		return true;
	}

}
