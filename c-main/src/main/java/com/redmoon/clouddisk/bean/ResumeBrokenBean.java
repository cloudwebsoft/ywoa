package com.redmoon.clouddisk.bean;

/**
 * @Description:
 * @author: 郝炜
 * @Date: 2015-1-20下午02:28:53
 */
public class ResumeBrokenBean {
	private int id;
	private String userName;
	private long attId;
	private int packageNo;
	private int packageType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getAttId() {
		return attId;
	}

	public void setAttId(long attId) {
		this.attId = attId;
	}

	public int getPackageNo() {
		return packageNo;
	}

	public void setPackageNo(int packageNo) {
		this.packageNo = packageNo;
	}

	public int getPackageType() {
		return packageType;
	}

	public void setPackageType(int packageType) {
		this.packageType = packageType;
	}

}
