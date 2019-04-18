package com.redmoon.clouddisk.bean;

import java.util.Date;

public class VersionBean {
	private int id;
	private String fileName;
	private String fileVersion;
	private String filePath;
	private Date createDate;
	private int isCurrent;
	private int isWow64;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public int getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(int isCurrent) {
		this.isCurrent = isCurrent;
	}

	public int getIsWow64() {
		return isWow64;
	}

	public void setIsWow64(int isWow64) {
		this.isWow64 = isWow64;
	}

}
