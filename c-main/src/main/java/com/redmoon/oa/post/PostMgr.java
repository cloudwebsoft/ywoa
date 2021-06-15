package com.redmoon.oa.post;

import java.util.Vector;


/**
 * @Description:
 * @author:
 * @Date: 2016-2-23下午03:13:09
 */

public class PostMgr {
	private int id;
	private String name;
	private String unitCode;
	private String description;
	private int orders;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public boolean isExist() {
		String sql = "select id from post where name=? and unit_code=?";
		PostDb db = new PostDb();
		
		Vector v = db.list(sql, new Object[] { name, unitCode });
		return v.size() > 0;
	}
	

}
