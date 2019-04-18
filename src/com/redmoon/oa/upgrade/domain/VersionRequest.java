package com.redmoon.oa.upgrade.domain;

public class VersionRequest {
	//用户标识
	private String customer;
	//版本号
	private String version;
	//补丁包下载路径
	private String url;
	//是否vip
	private boolean vip;
	//客户端IP
	private String ip;
	//OA用户数
	private String allUsers;
	//登陆总次数
	private String loginTotals;
	//手机端登陆总数
	private String phoneLoginTotals;
	//手机端使用人数
	private String phoneUsers;
	//文件柜文件数
	private String documentNums;
	//内部消息数
	private String messageNums;
	//通知数
	private String noticeNums;
	//流程数
	private String flowNums;
	//工作记事数
	private String workNoteCount;
	//是否商业用户
	private String isBiz;
	//是否注册
	private String isRegisted;

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isVip() {
		return vip;
	}

	public void setVip(boolean vip) {
		this.vip = vip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAllUsers() {
		return allUsers;
	}

	public void setAllUsers(String allUsers) {
		this.allUsers = allUsers;
	}

	public String getLoginTotals() {
		return loginTotals;
	}

	public void setLoginTotals(String loginTotals) {
		this.loginTotals = loginTotals;
	}

	public String getPhoneLoginTotals() {
		return phoneLoginTotals;
	}

	public void setPhoneLoginTotals(String phoneLoginTotals) {
		this.phoneLoginTotals = phoneLoginTotals;
	}

	public String getPhoneUsers() {
		return phoneUsers;
	}

	public void setPhoneUsers(String phoneUsers) {
		this.phoneUsers = phoneUsers;
	}
	
	public String getDocumentNums() {
		return documentNums;
	}

	public void setDocumentNums(String documentNums) {
		this.documentNums = documentNums;
	}

	public String getMessageNums() {
		return messageNums;
	}

	public void setMessageNums(String messageNums) {
		this.messageNums = messageNums;
	}

	public String getNoticeNums() {
		return noticeNums;
	}

	public void setNoticeNums(String noticeNums) {
		this.noticeNums = noticeNums;
	}

	public String getFlowNums() {
		return flowNums;
	}

	public void setFlowNums(String flowNums) {
		this.flowNums = flowNums;
	}

	public String getWorkNoteCount() {
		return workNoteCount;
	}

	public void setWorkNoteCount(String workNoteCount) {
		this.workNoteCount = workNoteCount;
	}

	public String getIsBiz() {
		return isBiz;
	}

	public void setIsBiz(String isBiz) {
		this.isBiz = isBiz;
	}

	public String getIsRegisted() {
		return isRegisted;
	}

	public void setIsRegisted(String isRegisted) {
		this.isRegisted = isRegisted;
	}
	
}
