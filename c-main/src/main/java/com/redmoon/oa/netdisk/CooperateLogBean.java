package com.redmoon.oa.netdisk;

import java.util.Date;

/**
 * @Description:
 * @author: 郝炜
 * @Date: 2014-7-17上午09:47:47
 */
public class CooperateLogBean {
	private long id;
	private String dirCode;
	private String userName;
	private String actionDesc;
	private int action;
	private String actionDate;
	private String actionName;

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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}


	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}





	public CooperateLogBean(long id, String dirCode, String userName,
			String actionDesc, int action, String actionDate, String actionName) {
		super();
		this.id = id;
		this.dirCode = dirCode;
		this.userName = userName;
		this.actionDesc = actionDesc;
		this.action = action;
		this.actionDate = actionDate;
		this.actionName = actionName;
	}

	public String getActionDesc() {
		return actionDesc;
	}

	public void setActionDesc(String actionDesc) {
		this.actionDesc = actionDesc;
	}

	public String getActionDate() {
		return actionDate;
	}

	public void setActionDate(String actionDate) {
		this.actionDate = actionDate;
	}

	public CooperateLogBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CooperateLogBean(String userName, String actionDesc,
			String actionDate, String actionName) {
		super();
		this.userName = userName;
		this.actionDesc = actionDesc;
		this.actionDate = actionDate;
		this.actionName = actionName;
	}


	

}
