package com.redmoon.oa.job;

import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.dept.DeptDb;

import java.sql.SQLException;
import java.util.*;

/**
 * @Description: 置用户单位，fgf 20161223该类仅用于启动中，没必要，已放弃，在DeptUserMgr中新增syncUnit方法，用于用户表及人事基本信息表中的单位的同步
 * @author: 古月圣
 * @Date: 2015-12-3上午09:32:46
 */
public class SetUserUnitCodeJob implements Job {
	public SetUserUnitCodeJob() {
	}

	Logger logger = Logger.getLogger(SuperCheckJob.class.getName());

	/**
	 * @Description:
	 * @param jobExecutionContext
	 * @throws JobExecutionException
	 */
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		executeJob();
	}

	@SuppressWarnings("unchecked")
	public void executeJob() {
		String sql = "select code from department where dept_type="
				+ DeptDb.TYPE_UNIT + " and code<>"
				+ StrUtil.sqlstr(DeptDb.ROOTCODE)
				+ " order by layer desc,orders desc";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ArrayList<String> list = new ArrayList<String>();
		try {
			ri = jt.executeQuery(sql);
			while (ri != null && ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				list.add(rr.getString(1));
			}
		} catch (SQLException e) {
			logger.error("SetUserUnitCodeJob error:" + e.getMessage());
		}
		for (String deptCode : list) {
			DeptDb deptDb = new DeptDb(deptCode);
			Vector vector = new Vector();
			try {
				vector = deptDb.getAllChild(vector, deptDb);
			} catch (ErrMsgException e) {
				logger.error("SetUserUnitCodeJob error:" + e.getMessage());
			}
			vector.add(deptDb);
			Iterator it = vector.iterator();
			while (it.hasNext()) {
				DeptDb ddb = (DeptDb) it.next();
				// 20161223 fgf SQL语句存在问题
/*				sql = "update users set unit_code="
						+ StrUtil.sqlstr(deptCode) 
						+ " where unit_code<>"
						+ StrUtil.sqlstr(ddb.getCode())
						+ " and exists (select id from dept_user where user_name=name and dept_code="
						+ StrUtil.sqlstr(ddb.getCode()) + ")";*/
				
    			sql = "update users set unit_code="
    				+ StrUtil.sqlstr(deptCode)
    				+ " where name in (select user_name from dept_user d where d.dept_code=" 
    				+ StrUtil.sqlstr(ddb.getCode())
    				+ ")";				
				try {
					jt.executeUpdate(sql);
				} catch (SQLException e) {
					logger.error("SetUserUnitCodeJob error:" + e.getMessage());
				}
			}
		}
		// 清缓存
		RMCache rmcache = RMCache.getInstance();
		try {
			rmcache.clear();
		} catch (CacheException e) {
			logger.error("SetUserUnitCodeJob error:" + e.getMessage());
		}
	}
}
