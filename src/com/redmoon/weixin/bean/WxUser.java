package com.redmoon.weixin.bean;

import java.util.List;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-7-22下午04:52:00
 */
public class WxUser {
	/**
	 * @return the userid
	 */
	public String getUserid() {
		return userid;
	}
	/**
	 * @param userid the userid to set
	 */
	public void setUserid(String userid) {
		this.userid = userid;
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
	/**
	 * @return the department
	 */
	public List<Integer> getDepartment() {
		return department;
	}
	/**
	 * @param department the department to set
	 */
	public void setDepartment(List<Integer> department) {
		this.department = department;
	}
	private String userid;
	private String name;
	private List<Integer> department;
	


}
