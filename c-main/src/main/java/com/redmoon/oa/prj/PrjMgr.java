package com.redmoon.oa.prj;

import java.sql.SQLException;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-3-23下午06:05:46
 */
public class PrjMgr {
	/**
	 * 取得项目负责人
	 * @Description: 
	 * @param prjId
	 * @return
	 */
	public String getPrjManager(long prjId) {
		String sql = "select prj_manager from form_table_prj where id=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{new Long(prjId)});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getString(1);
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 取得任务执行人
	 * @Description: 
	 * @param taskId
	 * @return
	 */
	public String getTaskManager(long taskId) {
		String sql = "select zrr from form_table_prj_task where id=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		try {
			ri = jt.executeQuery(sql, new Object[]{new Long(taskId)});
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getString(1);
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * 浏览项目或任务日报的时候，使当天的普通日报与项目或任务相关联
	 * @Description: 
	 * @param workLogId
	 * @param code
	 * @param prjOrTaskId
	 * @return
	 */
	public int relateWithPrjOrTask(long workLogId, String code, long prjOrTaskId) {
		String sql = "select id from visual_module_worklog where workLog_id=? and form_code=? and cws_id=?";
		ResultIterator ri;
		try {
			JdbcTemplate jt = new JdbcTemplate();
			ri = jt.executeQuery(sql, new Object[]{new Long(workLogId), code, new Long(prjOrTaskId)});
			if (ri.hasNext()) {
				return -1;
			}
			
			// 在有效期内的且未完成的项目进行关联
			if (isPrjOrTaskCanWriteDayWork(code, prjOrTaskId)) {
				sql = "insert into visual_module_worklog (workLog_id, form_code, cws_id) values (?,?,?)";
				return jt.executeUpdate(sql, new Object[]{new Long(workLogId), code, new Long(prjOrTaskId)});
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error("relateWithPrjOrTask: " + e.getMessage());
		}
		
		return 0;
	}
	
	/**
	 * 判断能否写汇报，项目或任务需在有效期内，且进度小于100
	 * @Description: 
	 * @param code
	 * @param prjOrTaskId
	 * @return
	 */
	public static boolean isPrjOrTaskCanWriteDayWork(String code, long prjOrTaskId) {
		// 在有效期内的且未完成的项目进行关联
		if (code.equals(PrjConfig.CODE_PRJ)) {
			FormDb fd = new FormDb(PrjConfig.CODE_PRJ);
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(prjOrTaskId, fd);
			
			java.util.Date bd = DateUtil.parse(fdao.getFieldValue("prj_begindate"), "yyyy-MM-dd HH:mm:ss");
			java.util.Date ed = DateUtil.parse(fdao.getFieldValue("prj_enddate"), "yyyy-MM-dd HH:mm:ss");
			// ed = DateUtil.addDate(DateUtil.parse(DateUtil.format(ed, "yyyy-MM-dd"), "yyyy-MM-dd"), 1);
			
			java.util.Date curDate = new java.util.Date();
			if (DateUtil.compare(bd, curDate)==2 || DateUtil.compare(bd, curDate)==0) {
				if (DateUtil.compare(ed, curDate)==1) {
					if (fdao.getFieldValue("status").equals(PrjConfig.STATUS_DOING) 
							|| fdao.getFieldValue("status").equals("project_status") // 基础数据宏控件的默认值
							) {
						if (StrUtil.toInt(fdao.getFieldValue("prj_progress"), -1)<100) {
							return true;
						}
					}
				}
			}
		}
		else if (code.equals(PrjConfig.CODE_TASK)) {
			FormDb fd = new FormDb(PrjConfig.CODE_TASK);
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(prjOrTaskId, fd);
			
			// 检查任务是否未完成，且在有效期内
			java.util.Date bd = DateUtil.parse(fdao.getFieldValue("task_begintime"), "yyyy-MM-dd HH:mm:ss");
			java.util.Date ed = DateUtil.parse(fdao.getFieldValue("task_endtime"), "yyyy-MM-dd HH:mm:ss");
			// ed = DateUtil.addDate(DateUtil.parse(DateUtil.format(ed, "yyyy-MM-dd"), "yyyy-MM-dd"), 1);
			
			java.util.Date curDate = new java.util.Date();
			if (DateUtil.compare(bd, curDate)==2 || DateUtil.compare(bd, curDate)==0) {
				if (DateUtil.compare(ed, curDate)==1) {
					if (fdao.getFieldValue("status").equals(PrjConfig.STATUS_DOING)) {
						if (StrUtil.toInt(fdao.getFieldValue("task_progress"), -1)<100) {
							return true;
						}
					}
				}
			}
		}		
		return false;
	}	
	
	/**
	 * 写日报的时候关联当前所有未完成的且在有效期内的项目或任务，只同步需每天写日报的项目或任务
	 * @Description:
	 * @param workLogId
	 * @param userName
	 */
	public void relateWithCurPrjAndTask(long workLogId, String userName) {
		String sql = "select id,is_everyday from form_table_prj where status=? and prj_manager=? and (prj_progress<100 or prj_progress is null) and prj_begindate>=? and prj_enddate<?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		java.util.Date curDate = new java.util.Date();
		curDate = DateUtil.getDate(DateUtil.getYear(curDate), DateUtil.getMonth(curDate), DateUtil.getDay(curDate));
		try {
			ri = jt.executeQuery(sql, new Object[]{PrjConfig.STATUS_DOING, userName, curDate, curDate});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				long prjId = rr.getLong(1);
				int isEveryDay = rr.getInt(2);
				if (isEveryDay==1) {
					relateWithPrjOrTask(workLogId, PrjConfig.CODE_PRJ, prjId);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error("relateWithCurPrjAndTask: " + e.getMessage());
		}
		sql = "select id,is_everyday from form_table_prj_task where status=? and zrr=? and (task_progress<100 or task_progress is null) and task_begintime>=? and task_endtime<?";
		try {
			ri = jt.executeQuery(sql, new Object[]{PrjConfig.STATUS_DOING, userName, curDate, curDate});
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				long taskId = rr.getLong(1);
				int isEveryDay = rr.getInt(2);
				if (isEveryDay==1) {
					relateWithPrjOrTask(workLogId, PrjConfig.CODE_TASK, taskId);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error("relateWithCurPrjAndTask: " + e.getMessage());
		}		
		
		
	}	

}
