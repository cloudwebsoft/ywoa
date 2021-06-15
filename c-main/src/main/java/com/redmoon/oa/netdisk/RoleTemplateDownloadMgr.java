package com.redmoon.oa.netdisk;

import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import java.sql.SQLException;

public class RoleTemplateDownloadMgr {
	int id;
	String userName;
	int rtId;
	transient Logger logger = Logger.getLogger(RoleTemplateMgr.class.getName());

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

	public int getRtId() {
		return rtId;
	}

	public void setRtId(int rtId) {
		this.rtId = rtId;
	}

	/**
	 * Qobject 新增 采用 create(JdbcTemplate ,object[]{})
	 * 
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean create() {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		RoleTemplateDownloadDb rtdlDb = new RoleTemplateDownloadDb();
		try {
			re = rtdlDb.create(jt, new Object[] { userName, rtId });
		} catch (ResKeyException e) {
			logger.error("create: " + e.getMessage());
		}
		return re;
	}

	/**
	 * Qobject 删除采用 del(JdbcTemplate ,object[]{})
	 * 
	 * @return
	 * @throws ErrMsgException
	 */
	public boolean del() {
		RoleTemplateDownloadDb rtdlDb = new RoleTemplateDownloadDb();
		boolean re = false;
		try {
			re = rtdlDb.del();
		} catch (ResKeyException e) {
			logger.error("del: " + e.getMessage());
		}
		return re;
	}

	/**
	 * @Description: 删除对应rtId的全部数据
	 * @return
	 */
	public boolean delByRtId() {
		String sql = "delete from netdisk_role_template_download where rt_id=" + rtId;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			int re = jt.executeUpdate(sql);
			return re >= 1;
		} catch (SQLException e) {
			logger.error("isExist: " + e.getMessage());
		} finally {
			RMCache rm = RMCache.getInstance();
			try {
				rm.clear();
			} catch (CacheException e) {
				logger.error("isExist: " + e.getMessage());
			}
		}
		return false;
	}

	/**
	 * @Description: 用户是否已下载指定模板
	 * @return
	 */
	public boolean isExists() {
		String sql = "select id from netdisk_role_template_download where user_name="
				+ StrUtil.sqlstr(userName) + " and rt_id=" + rtId;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri != null && ri.hasNext()) {
				return true;
			}
		} catch (SQLException e) {
			logger.error("isExists:" + e.getMessage());
		}
		return false;
	}
}
