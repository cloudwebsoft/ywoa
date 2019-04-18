package com.redmoon.clouddisk.bean;

import java.util.Date;

/**
 * @Description:
 * @author: 郝炜
 * @Date: 2014-7-17上午09:31:15
 */
public class CooperateBean {
	private long id;
	private String dirCode;
	private String visualPath;
	private String name;
	private int isRefused;
	private String shareUser;
	private String userName;
	private String realName;
	private Date cooperateDate;

	public CooperateBean() {
		id = 0;
		dirCode = "";
		visualPath = "";
		name = "";
		isRefused = 0;
		shareUser = "";
		userName = "";
		realName = "";
		cooperateDate = null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDirCode() {
		return dirCode;
	}

	public void setDirCode(String dirCode) {
		this.dirCode = dirCode;
	}

	public int getIsRefused() {
		return isRefused;
	}

	public String getVisualPath() {
		return visualPath;
	}

	public void setVisualPath(String visualPath) {
		this.visualPath = visualPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIsRefused(int isRefused) {
		this.isRefused = isRefused;
	}

	public String getShareUser() {
		return shareUser;
	}

	public void setShareUser(String shareUser) {
		this.shareUser = shareUser;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Date getCooperateDate() {
		return cooperateDate;
	}

	public void setCooperateDate(Date cooperateDate) {
		this.cooperateDate = cooperateDate;
	}

}
