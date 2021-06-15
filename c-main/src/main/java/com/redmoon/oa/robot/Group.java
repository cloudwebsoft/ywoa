package com.redmoon.oa.robot;

public class Group {
	/**
	 * QQ群号码
	 */
	String id;
	/**
	 * 群名称
	 */
	String name;
	/**
	 * 自动登录群OA的地址，需加米宝为好友
	 */
	String loginUrlAuto;
	
	/**
	 * 手工登录群OA的地址
	 */
	String loginUrl;
	
	/**
	 * 播报时显示文件柜中文档的地址
	 */
	String docShowUrl;
	
	/**
	 * 是否开启红包功能
	 */
	boolean redbagOpen = true;
	
	/**
	 * 是否开启文件柜分享至QQ群功能
	 */
	boolean filearkShareOpen = true;
	
	String filearkShareDefaultImg = "";
	
	boolean joinGift = true;
	boolean docShare = true;

	public boolean isDocShare() {
		return docShare;
	}

	public void setDocShare(boolean docShare) {
		this.docShare = docShare;
	}

	public boolean isJoinGift() {
		return joinGift;
	}

	public void setJoinGift(boolean joinGift) {
		this.joinGift = joinGift;
	}

	public String getFilearkShareDefaultImg() {
		return filearkShareDefaultImg;
	}

	public void setFilearkShareDefaultImg(String filearkShareDefaultImg) {
		this.filearkShareDefaultImg = filearkShareDefaultImg;
	}

	public boolean isFilearkShareOpen() {
		return filearkShareOpen;
	}

	public void setFilearkShareOpen(boolean filearkShareOpen) {
		this.filearkShareOpen = filearkShareOpen;
	}

	public boolean isRedbagOpen() {
		return redbagOpen;
	}

	public void setRedbagOpen(boolean redbagOpen) {
		this.redbagOpen = redbagOpen;
	}

	public String getDocShowUrl() {
		return docShowUrl;
	}

	public void setDocShowUrl(String docShowUrl) {
		this.docShowUrl = docShowUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginUrlAuto() {
		return loginUrlAuto;
	}

	public void setLoginUrlAuto(String loginUrlAuto) {
		this.loginUrlAuto = loginUrlAuto;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	
}
