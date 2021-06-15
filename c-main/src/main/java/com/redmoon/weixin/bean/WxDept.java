package com.redmoon.weixin.bean;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-7-22下午02:54:29
 */
public class WxDept {
	private int id;
	private int parentid;
	private String name;
	private int order;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the parentid
	 */
	public int getParentid() {
		return parentid;
	}
	/**
	 * @param parentid the parentid to set
	 */
	public void setParentid(int parentid) {
		this.parentid = parentid;
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
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	public WxDept(){
		
	}
	public WxDept(int id, int parentid, String name, int order) {
		super();
		this.id = id;
		this.parentid = parentid;
		this.name = name;
		this.order = order;
	}
	

}
