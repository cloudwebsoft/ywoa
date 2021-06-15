package com.redmoon.clouddisk.bean;

import java.util.Date;

/**
 * @author 古月圣
 * 
 */
public class AttachmentBean {
	private long id;
	private long docId;
	private String userName;
	private String name;
	private String diskName;
	private String fullPath;
	private String visualPath;
	private int pageNum;
	private int orders;
	private long fileSize;
	private String ext;
	private String publicDir;
	private String publicDept;
	private Date uploadDate;

	public AttachmentBean() {
		id = 0;
		docId = 0;
		userName = "";
		name = "";
		diskName = "";
		visualPath = "";
		fullPath = "";
		pageNum = 1;
		orders = 0;
		fileSize = 0;
		ext = "";
		publicDir = "";
		publicDept = "";
		uploadDate = null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getVisualPath() {
		return visualPath;
	}

	public void setVisualPath(String visualPath) {
		this.visualPath = visualPath;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getPublicDir() {
		return publicDir;
	}

	public void setPublicDir(String publicDir) {
		this.publicDir = publicDir;
	}

	public String getPublicDept() {
		return publicDept;
	}

	public void setPublicDept(String publicDept) {
		this.publicDept = publicDept;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

}
