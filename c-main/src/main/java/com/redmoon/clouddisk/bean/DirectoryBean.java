package com.redmoon.clouddisk.bean;

import java.util.Date;

public class DirectoryBean {

	private String code;
	private String name;
	private int isHome;
	private String description;
	private String parentCode;
	private String rootCode;
	private int orders;
	private int childCount;
	private Date addDate;
	private int isLocked;
	private int type;
	private int layer;
	private long docId;
	private int templateId;
	private String pluginCode;
	private int isShared;

	public DirectoryBean() {
		code = "";
		name = "";
		isHome = 0;
		description = "";
		parentCode = "";
		rootCode = "";
		orders = 0;
		childCount = 0;
		addDate = null;
		isLocked = 0;
		type = 0;
		layer = 1;
		docId = -1;
		templateId = -1;
		pluginCode = "default";
		isShared = 0;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIsHome() {
		return isHome;
	}

	public void setIsHome(int isHome) {
		this.isHome = isHome;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public String getRootCode() {
		return rootCode;
	}

	public void setRootCode(String rootCode) {
		this.rootCode = rootCode;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public int getChildCount() {
		return childCount;
	}

	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public int getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(int isLocked) {
		this.isLocked = isLocked;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public String getPluginCode() {
		return pluginCode;
	}

	public void setPluginCode(String pluginCode) {
		this.pluginCode = pluginCode;
	}

	public int getIsShared() {
		return isShared;
	}

	public void setIsShared(int isShared) {
		this.isShared = isShared;
	}

}
