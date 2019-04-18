package com.redmoon.t;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QCache;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * 微博，分为个人型、班级型
 * @author fgf
 *
 */
public class TDb extends QObjectDb {
	/**
	 * 个人型
	 */
	public static final int KIND_USER = 0;
	/**
	 * 班级型
	 */
	public static final int KIND_CLASS = 1;
	
	public static final int STATUS_OPEN = 1;
	public static final int STATUS_CLOSE = 0;
	public static final int STATUS_FORCE_CLOSE = -1;
	
	public static String getStatusDesc(int status) {
		if (status==STATUS_OPEN)
			return "开启";
		else if (status==STATUS_FORCE_CLOSE)
			return "强制关闭";
		else
			return "关闭";
	}

	public TDb getTFromDb(String owner, int kind) {
		String sql = "select id from " + getTable().getName() + " where owner=? and kind=?";
		JdbcTemplate jt = new JdbcTemplate();
		try {
			ResultIterator ri = jt.executeQuery(sql, new Object[]{owner, new Integer(kind)});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return getTDb(rr.getLong(1));
			}
		}
		catch (SQLException e) {
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return null;
	}
	
	public TDb getTDb(long id) {
		return (TDb)getQObjectDb(new Long(id));
	}

    public TDb getTDb(String owner, int kind) {
        Long tid = null;
        try {
        	tid = (Long)QCache.getInstance().getFromGroup("tdb_owner_" + owner, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getTDb:" + e.getMessage());
        }
        if (tid==null) {
        	TDb tdb = getTFromDb(owner, kind);
        	if (tdb!=null) {
        		tid = new Long(tdb.getLong("id"));
	            try {
	            	QCache.getInstance().putInGroup("tdb_owner_" + owner, cacheGroup, tid);
	            }
	            catch (Exception e) {
	                LogUtil.getLog(getClass()).error("getTDb:" + e.getMessage());
	            }
        	}
        }
        if (tid==null)
        	return null;
        else
        	return getTDb(tid.longValue());
    }

}
