package com.redmoon.oa.post;

import java.util.Iterator;
import java.util.Vector;

import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-23下午03:13:09
 */

public class PostDb extends QObjectDb {
	public PostDb() {
		super();
	}

	public PostDb getPostDb(int id) {
		return (PostDb) getQObjectDb(new Integer(id));
	}
	
	/**
	 * 取得岗位的ID
	 * @Description: 
	 * @param name
	 * @param unitCode
	 * @return
	 */
	public int getIdByName(String name, String unitCode) {
		String sql = "select id from post where name=? and unit_code=?";
		PostDb db = new PostDb();
		
		Vector v = db.list(sql, new Object[] { name, unitCode });
		Iterator ir = v.iterator();
		if (ir.hasNext()) {
			PostDb pd = (PostDb)ir.next();
			return pd.getInt("id");
		}
		else {
			return -1;
		}
	}	
}
