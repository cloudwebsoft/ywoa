package com.redmoon.oa.post;

import java.util.Iterator;
import java.util.Vector;
import com.cloudwebsoft.framework.base.QObjectDb;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-23下午03:13:09
 */

public class PostUserDb extends QObjectDb {
	public PostUserDb() {
		super();
	}

	public PostUserDb getPostUserDb(int id) {
		return (PostUserDb) getQObjectDb(new Integer(id));
	}
	
	public PostUserDb getPostUserDb(String userName) {
		String sql = "select id from " + getTable().getName() + " where user_name=?";
		Iterator ir = list(sql, new Object[]{userName}).iterator();
		if (ir.hasNext()) {
			return (PostUserDb)ir.next();
		}
		return null;
	}
	
	/**
	 * wm修改，根据用户名返回用户职位Vector
	 * @Description: 
	 * @param userName
	 * @return
	 */
	public Vector getPostsUserDb(String userName) {
		String sql = "select id from " + getTable().getName() + " where user_name=?";
		Vector v = list(sql, new Object[]{userName});
		return v;
	}
}
