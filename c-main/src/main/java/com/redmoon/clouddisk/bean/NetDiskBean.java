package com.redmoon.clouddisk.bean;

import java.util.Date;

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
	private Date createDate;
	private Date versionDate;
	private Date tempDate;
	private int isDeleted;
	private Date deleteDate;
	private long fileSize;
	private int packageCount;
	private long docId;
	private int isDoc;
	private int action;
	private String md5;

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
		action = 0;
		md5 = "";
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}

	public Date getTempDate() {
		return tempDate;
	}

	public void setTempDate(Date tempDate) {
		this.tempDate = tempDate;
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Date getDeleteDate() {
		return deleteDate;
	}

	public void setDeleteDate(Date deleteDate) {
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

	public int getPackageCount() {
		return packageCount;
	}

	public void setPackageCount(int packageCount) {
		this.packageCount = packageCount;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

}
