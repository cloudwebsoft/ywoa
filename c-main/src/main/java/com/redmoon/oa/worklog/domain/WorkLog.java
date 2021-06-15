package com.redmoon.oa.worklog.domain;

import java.util.Date;
import java.util.List;

/**
 * work_log表基础类
 * @author jfy
 * @date Jul 9, 2015
 */
public class WorkLog {
	//主键ID
	private int id;
	//发布者
	private String userName;
	//发布内容
	private String content;
	//发布时间
	private String myDate;
	//编写时间
	private Date realDate;
	//类型
	private String type;
	//评论次数
	private int reviewCount;
	//点赞次数
	private int praiseCount;
	//附件
	private List<WorkLogAttach> workLogAttachs;
	//扩展属性，评论及点赞
	private List<WorkLogExpand> workLogExpands;
	//扩展属性，点赞 列表 -lzm添加
	private List<WorkLogExpand> workLogPraises;
	//开始时间
	private String beginDate;
	//结束时间
	private String endDate;
	//查询条件：内容
	private String condContent;
	//当前页编号
	private int curPage;
	//每页显示条数
	private int pageSize;
	//周数
	private int logItem;
	//年
	private int logYear;
	//页面展示时间
	private String showTitle;
	
	/**
	 * 如果为prj表示项目，prj_task表示任务
	 */
	private String formCode;
	
	/**
	 * 日志对应的项目或者任务的ID
	 */
	private int prjId;
	
	/**
	 * 进度
	 */
	private int process;
	
	public String getFormCode() {
		return formCode;
	}

	public void setFormCode(String formCode) {
		this.formCode = formCode;
	}

	public int getPrjId() {
		return prjId;
	}

	public void setPrjId(int prjId) {
		this.prjId = prjId;
	}

	public List<WorkLogAttach> getWorkLogAttachs() {
		return workLogAttachs;
	}

	public void setWorkLogAttachs(List<WorkLogAttach> workLogAttachs) {
		this.workLogAttachs = workLogAttachs;
	}

	public List<WorkLogExpand> getWorkLogExpands() {
		return workLogExpands;
	}

	public void setWorkLogExpands(List<WorkLogExpand> workLogExpands) {
		this.workLogExpands = workLogExpands;
	}

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMyDate() {
		return myDate;
	}

	public void setMyDate(String myDate) {
		this.myDate = myDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	public int getPraiseCount() {
		return praiseCount;
	}

	public void setPraiseCount(int praiseCount) {
		this.praiseCount = praiseCount;
	}

	public String getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCondContent() {
		return condContent;
	}

	public void setCondContent(String condContent) {
		this.condContent = condContent;
	}

	public int getCurPage() {
		return curPage;
	}

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public Date getRealDate() {
		return realDate;
	}

	public void setRealDate(Date realDate) {
		this.realDate = realDate;
	}

	public int getLogItem() {
		return logItem;
	}

	public void setLogItem(int logItem) {
		this.logItem = logItem;
	}

	public int getLogYear() {
		return logYear;
	}

	public void setLogYear(int logYear) {
		this.logYear = logYear;
	}

	public String getShowTitle() {
		return showTitle;
	}

	public void setShowTitle(String showTitle) {
		this.showTitle = showTitle;
	}

	/**
	 * @param process the process to set
	 */
	public void setProcess(int process) {
		this.process = process;
	}

	/**
	 * @return the process
	 */
	public int getProcess() {
		return process;
	}

	/**
	 * @return the workLogPraises
	 */
	public List<WorkLogExpand> getWorkLogPraises() {
		return workLogPraises;
	}

	/**
	 * @param workLogPraises the workLogPraises to set
	 */
	public void setWorkLogPraises(List<WorkLogExpand> workLogPraises) {
		this.workLogPraises = workLogPraises;
	}
	

	
	
}
