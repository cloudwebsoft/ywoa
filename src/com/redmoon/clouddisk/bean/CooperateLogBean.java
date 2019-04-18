package com.redmoon.clouddisk.bean;

import java.util.Date;

/**
 * @Description:
 * @author: 郝炜
 * @Date: 2014-7-17上午09:47:47
 */
public class CooperateLogBean {

	public final static int ACTION_START = 0; // 发起协作
	public final static int ACTION_JOININ = 1; // 加入协作
	public final static int ACTION_REFUSE = 2; // 拒绝加入协作
	public final static int ACTION_CANCEL = 3; // 被取消协作
	public final static int ACTION_UPLOAD = 4; // 上传
	public final static int ACTION_UPDATE = 5; // 更新
	public final static int ACTION_DELETE = 6; // 删除

	private long id;
	private String dirCode;
	private String userName;
	private int action;
	private Date actionDate;
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

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

}
