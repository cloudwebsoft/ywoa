package com.redmoon.clouddisk.bean;

/**
 * @author 古月圣
 * 
 */
public class UserBean {

	private String name;
	private String pwd;
	private String realName;
	private String deptName;
	private short gender;
	private long diskSpace;
	private byte failedReason;
	private long speed;

	public UserBean() {
		name = "";
		pwd = "";
		realName = "";
		deptName = "";
		gender = 0;
		diskSpace = 0;
		failedReason = 0x00;
		speed = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public short getGender() {
		return gender;
	}

	public void setGender(short gender) {
		this.gender = gender;
	}

	public long getDiskSpace() {
		return diskSpace;
	}

	public void setDiskSpace(long diskSpace) {
		this.diskSpace = diskSpace;
	}

	public byte getFailedReason() {
		return failedReason;
	}

	public void setFailedReason(byte failedReason) {
		this.failedReason = failedReason;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}

}
