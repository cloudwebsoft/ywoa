package com.redmoon.oa.netdisk;

import com.mysql.jdbc.Util;


/**
 * @author 古月圣
 * 
 */
public class NetDiskBean {
	private long id;
	private String name;
	private String userName;
	private String diskName;
	private String visualPath;
	private int isCurrent;
	private String createDate;
	private String versionDate;
	private String tempDate;
	private int isDeleted;
	private String deleteDate;
	private long fileSize;
	private String size;

	private long docId;
	private int isDoc;

	public NetDiskBean() {
		id = 0;
		name = "";
		userName = "";
		diskName = "";
		visualPath = "";
		isCurrent = 1;
		createDate = null;
		versionDate = null;
		isDeleted = 0;
		deleteDate = null;
		isDoc = 1;
		fileSize = 0;
		docId = 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getVisualPath() {
		return visualPath;
	}

	public void setVisualPath(String visualPath) {
		this.visualPath = visualPath;
	}

	public int getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(int isCurrent) {
		this.isCurrent = isCurrent;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(String versionDate) {
		this.versionDate = versionDate;
	}

	public String getTempDate() {
		return tempDate;
	}

	public void setTempDate(String tempDate) {
		this.tempDate = tempDate;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(String deleteDate) {
		this.deleteDate = deleteDate;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public long getDocId() {
		return docId;
	}

	public int getIsDoc() {
		return isDoc;
	}

	public void setIsDoc(int isDoc) {
		this.isDoc = isDoc;
	}
	
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}

	public NetDiskBean(int id ,String name, int current,
			long fileSize, String versionDate) {
		super();
		this.id = id;
		this.name = name;
		this.isCurrent = current;
		this.size = UtilTools.getFileSize(fileSize);
		this.versionDate = versionDate;
	}

	

}
