package com.redmoon.oa.post;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

/**
 * @Description:
 * @author:
 * @Date: 2016-2-23下午03:13:09
 */

public class PostUserMgr {
	private int id;
	private int postId;
	private String userName;
	private int orders;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getOrders() {
		return orders;
	}

	public void setOrders(int orders) {
		this.orders = orders;
	}

	public Vector listByPostId() {
		String sql = "select id from post_user where post_id=?";
		PostUserDb puDb = new PostUserDb();
		return puDb.list(sql, new Object[] { postId });
	}

	public PostUserDb postByUserName() {
		PostUserDb db = null;
		String sql = "select id from post_user where user_name=?";
		PostUserDb puDb = new PostUserDb();
		Vector vec = puDb.list(sql, new Object[] { userName });
		Iterator it = vec.iterator();
		if (it.hasNext()) {
			db = (PostUserDb) it.next();
		}
		return db;
	}
	
	public boolean createSingle(String userName, int postId, int orders) {
		boolean re = false;
		try {
			// 删除原岗位
			PostUserDb pud = new PostUserDb();
			pud = pud.getPostUserDb(userName);
			if (pud!=null) {
				pud.del();
			}
			else {
				pud = new PostUserDb();
			}
			
			JdbcTemplate jt = new JdbcTemplate();			
			re = pud.create(jt, new Object[] { postId,
					userName, orders });
			
			// 更新人员基本信息表的岗位
			String sql = "update form_table_personbasic set job_level=? where user_name=?";
			jt.executeUpdate(sql, new Object[] { postId, userName });
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}		
		return re;
	}

	public boolean create(String userNames) {
		String[] users = StrUtil.split(userNames, ",");
		boolean re = false;
		for (String user : users) {
			re = createSingle(user, postId, orders);
		}
		return re;
	}

	public boolean isExist() {
		String sql = "select id from post_user where user_name=?";
		PostUserDb puDb = new PostUserDb();
		Vector v = puDb.list(sql, new Object[] { userName });
		Iterator it = v.iterator();
		if (it.hasNext()) {
			PostUserDb db = (PostUserDb) it.next();
			id = db.getInt("id");
			return true;
		}
		return false;
	}

	public boolean delBatch(String ids) {
		String[] idAry = StrUtil.split(ids, ",");
		PostUserDb puDb = new PostUserDb();
		boolean re = true;
		JdbcTemplate jt = new JdbcTemplate();
		for (String idStr : idAry) {
			try {
				int id = StrUtil.toInt(idStr);
				puDb = puDb.getPostUserDb(id);
				if (puDb.isLoaded()) {
					re &= puDb.del();
					
					// 更新人员基本信息表的岗位
					String sql = "update form_table_personbasic set job_level='' where user_name=?";
					jt.executeUpdate(sql, new Object[] { userName });
				}
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			} catch (SQLException e) {
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
		if (jt != null) {
			jt.close();
		}
		return re;
	}
}
