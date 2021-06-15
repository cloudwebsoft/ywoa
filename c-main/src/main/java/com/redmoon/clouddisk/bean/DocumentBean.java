package com.redmoon.clouddisk.bean;

import java.util.Date;

public class DocumentBean {

	private long id;
	private String keyWords;
	private int isRelateShow;
	private String title;
	private String class1;
	private String author;
	private Date modifiedDate;
	private int canCommit;
	private int isHome;
	private String voteOption;
	private String voteResult;
	private int type;
	private int examine;
	private String nick;
	private int hit;
	private int templateId;
	private int pageCount;
	private String parentCode;
	private int isNew;
	private String flowTypeCode;
	private String summary;

	public DocumentBean() {
		id = 0;
		keyWords = "";
		isRelateShow = 1;
		title = "";
		class1 = "";
		author = "";
		modifiedDate = null;
		canCommit = 0;
		isHome = 0;
		voteOption = "";
		voteResult = "";
		type = 0;
		examine = 0;
		nick = "";
		hit = 0;
		templateId = -1;
		pageCount = 1;
		parentCode = "";
		isNew = 0;
		flowTypeCode = "";
		summary = "";
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String keyWords) {
		this.keyWords = keyWords;
	}

	public int getIsRelateShow() {
		return isRelateShow;
	}

	public void setIsRelateShow(int isRelateShow) {
		this.isRelateShow = isRelateShow;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getClass1() {
		return class1;
	}

	public void setClass1(String class1) {
		this.class1 = class1;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public int getCanCommit() {
		return canCommit;
	}

	public void setCanCommit(int canCommit) {
		this.canCommit = canCommit;
	}

	public int getIsHome() {
		return isHome;
	}

	public void setIsHome(int isHome) {
		this.isHome = isHome;
	}

	public String getVoteOption() {
		return voteOption;
	}

	public void setVoteOption(String voteOption) {
		this.voteOption = voteOption;
	}

	public String getVoteResult() {
		return voteResult;
	}

	public void setVoteResult(String voteResult) {
		this.voteResult = voteResult;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getExamine() {
		return examine;
	}

	public void setExamine(int examine) {
		this.examine = examine;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public int getIsNew() {
		return isNew;
	}

	public void setIsNew(int isNew) {
		this.isNew = isNew;
	}

	public String getFlowTypeCode() {
		return flowTypeCode;
	}

	public void setFlowTypeCode(String flowTypeCode) {
		this.flowTypeCode = flowTypeCode;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
