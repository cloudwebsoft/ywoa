package com.redmoon.oa.worklog.domain;

import com.redmoon.oa.person.UserDb;

/**
 * 工作汇报扩展表基础类
 * @author jfy
 * @date Jul 9, 2015
 */
public class WorkLogExpand {
	//评论
	public static final String REVIEW_TYPE = "0";
	//点赞
	public static final String PRAISE_TYPE = "1";
	private int id;
	//工作汇报ID
	private int workLogId;
	//评论者
	private String userName;
	private String name;
	//评论内容
	private String review;
	//评论时间
	private String reviewTime;
	//类型
	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWorkLogId() {
		return workLogId;
	}

	public void setWorkLogId(int workLogId) {
		this.workLogId = workLogId;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
