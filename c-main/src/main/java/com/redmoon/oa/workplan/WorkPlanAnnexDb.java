package com.redmoon.oa.workplan;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.flow.WorkflowDb;

import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ResultRecord;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkPlanAnnexDb extends QObjectDb {
	public static final int CHECK_STATUS_WAIT = 0;
	public static final int CHECK_STATUS_PASSED = 1;
	public static final int CHECK_STATUS_UNPASS = 2;

	public static final int CHECK_STATUS_NONE = 100;

    /**
     * 日报
     */
    public static final int TYPE_NORMAL = 0;
    /**
     * 周报
     */
    public static final int TYPE_WEEK = 1;
    /**
     * 月报
     */
    public static final int TYPE_MONTH = 2;

    /**
     * 通过流程汇报，在content中记录流程ID;
     */
    public static final int TYPE_FLOW = 3;

    public WorkPlanAnnexDb() {
        super();
    }

    public String getSqlForListWorkplanAnnexDayOfTask(long taskId, int year, int month) {
        return "select id from work_plan_annex where task_id=" + taskId + " and (annex_type=" + TYPE_NORMAL + " or annex_type=" + TYPE_FLOW + ") and " + SQLFilter.year("add_date") + "=" + StrUtil.sqlstr(String.valueOf(year)) + " and " + SQLFilter.month("add_date") + "=" + StrUtil.sqlstr(String.valueOf(month)) + " order by add_date asc";
    }

    public String getSqlForListWorkplanAnnexDayOfWorkplan(int id, int year, int month) {
        return "select id from work_plan_annex where workplan_id=" + id + " and (annex_type=" + TYPE_NORMAL + " or annex_type=" + TYPE_FLOW + ") and " + SQLFilter.year("add_date") + "=" + StrUtil.sqlstr(String.valueOf(year)) + " and " + SQLFilter.month("add_date") + "=" + StrUtil.sqlstr(String.valueOf(month)) + " order by add_date asc";
    }

    public String getSqlForSearch(int id, Date beginDate, Date endDate, String what) {
        String sql = "select id from work_plan_annex where workplan_id=" + id + " and annex_type=" + TYPE_NORMAL;
        if (beginDate!=null) {
            sql += " and add_date>=" + SQLFilter.getDateStr(DateUtil.format(beginDate, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (endDate!=null) {
            sql += " and add_date<=" + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (!"".equals(what)) {
            sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
        }
        return sql;
    }

    public boolean create(JdbcTemplate jt, ParamChecker paramChecker) throws
            ResKeyException, ErrMsgException {
        boolean re = super.create(jt, paramChecker);
        return re;
    }

    /**
     * 从数据库中取得计划的实际进度，以便于在修改计划进度信息时重置计划的进度
     * @return int
     */
    public int getToalProgressFromDb() {
        String sql = "select sum(progress) from " + getTable().getName() + " where workplan_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(getLong("workplan_id"))});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return rr.getInt(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return -1;
    }

    public void writeAttachment(HttpServletRequest request, FileUpload fu, long annexId) throws ErrMsgException {
       if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            Iterator ir = v.iterator();
            Calendar cal = Calendar.getInstance();
            String year = "" + (cal.get(cal.YEAR));
            String month = "" + (cal.get(cal.MONTH) + 1);
            com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
            String vpath = cfg.get("file_workplan") + "/" + year + "/" + month + "/";
            // 置保存路径
            String filepath = Global.getRealPath() + vpath;
            // 置路径
            fu.setSavePath(filepath);
            while (ir.hasNext()) {
                FileInfo fi = (FileInfo) ir.next();

                // 使用随机名称写入磁盘
                fi.write(fu.getSavePath(), true);

                WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
                wfaa.setAnnexId(annexId);
                String visualPath = year + "/" + month;
                wfaa.setVisualPath(visualPath);
                wfaa.setName(fi.getName());
                wfaa.setDiskName(fi.getDiskName());
                wfaa.setOrders(0);
                wfaa.setWorkplanId(getLong("workplan_id"));
                wfaa.create();
            }
        }
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = false;
        re = super.del(jt);
        if (re) {
            WorkPlanAnnexAttachment wfaa = new WorkPlanAnnexAttachment();
            wfaa.delAttachments(getLong("id"));
        }
        return re;
    }

    /**
     * 取得计划中所有进度的最后一天
     * @param workplanId
     * @return
     */
    public Date getMaxEndDate(long workplanId) {
        String sql = "select max(end_date) from " + getTable().getName() + " where workplan_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql,
                                                new Object[] {new Long(workplanId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return rr.getDate(1);
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return null;
    }

    /**
     * 取得计划实际完成时间
     * @param taskId long
     * @return Date
     */
    public Date getRealCompleteDate(long taskId) {
        String sql = "select id from " + getTable().getName() + " where task_id=? and progress=?";
        Vector v = list(sql, new Object[]{new Long(taskId), new Integer(100)});
        if (v.size()>0) {
            Iterator ir = v.iterator();
            if (ir.hasNext()) {
                WorkPlanAnnexDb wpad = (WorkPlanAnnexDb)ir.next();
                return wpad.getDate("add_date");
            }
            return null;
        }
        else
            return null;
    }

    /**
     * 根据年份，取出周报，年报
     * @param year int
     * @param type int 类型，1周报，2月报
     * @param item int 周数或月份
     * @return WorkPlanAnnexDb
     */
    public WorkPlanAnnexDb getWorkPlanAnnexDb(int workplanId, int year, int type, int item) {
        String sql = "select id from " + getTable().getName() + " where workplan_id=? and annex_year=? and annex_type=? and annex_item=?";
        Iterator ir = list(sql, new Object[]{new Integer(workplanId), new Integer(year), new Integer(type), new Integer(item)}).iterator();
        if (ir.hasNext()) {
            WorkPlanAnnexDb wad = (WorkPlanAnnexDb)ir.next();
            return wad;
        }
        return null;
    }

    /**
     * 取得计划的任务项的某天的日报
     * @param workplanId
     * @param date
     * @return
     */
    public WorkPlanAnnexDb getWorkPlanTaskAnnexDb(int workplanId, long taskId, Date date) {
        String sql = "select id from work_plan_annex where workplan_id=? and task_id=? and annex_type=? and add_date=" + SQLFilter.getDateStr(DateUtil.format(date, "yyyy-MM-dd"), "yyyy-MM-dd");
        Iterator ir = list(sql, new Object[]{new Integer(workplanId), new Long(taskId), new Integer(TYPE_NORMAL)}).iterator();
        if (ir.hasNext()) {
            WorkPlanAnnexDb wad = (WorkPlanAnnexDb)ir.next();
            return wad;
        }
        return null;
    }
    
    /**
     * 当汇报流程创建时，创建汇报记录
     * @param taskId
     * @param flowId
     * @param userName
     * @return
     */
    public boolean createForReportFlow(long taskId, int flowId, String userName) {
    	WorkPlanTaskDb wptd = new WorkPlanTaskDb();
    	wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(taskId));
    	long workPlanId = wptd.getLong("work_plan_id");
    	
    	WorkPlanDb wpd = new WorkPlanDb();
    	wpd = wpd.getWorkPlanDb((int)workPlanId);
    	int oldProgress = wptd.getInt("progress");
    	
        String sql = "insert into work_plan_annex (id,workplan_id,flow_id,user_name,add_date,progress,check_status,task_id,old_progress,annex_type,annex_year, content) values(?,?,?,?,?,?,?,?,?,?,?,' ')";
        boolean re = false;
        long id = (long) SequenceManager.nextID(SequenceManager.OA_WORKPLAN_ANNEX);
        int year = DateUtil.getYear(new java.util.Date());
        try {
        	JdbcTemplate jt = new JdbcTemplate();
        	re = jt.executeUpdate(sql, new Object[]{new Long(id), new Long(workPlanId), new Integer(flowId), userName, new java.util.Date(), new Integer(0), new Integer(CHECK_STATUS_WAIT), new Long(taskId), new Integer(oldProgress), new Integer(TYPE_FLOW), new Integer(year)})==1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return re;
    }
    
    public WorkflowDb getWorkflowDb() {
    	WorkflowDb wf = new WorkflowDb();
    	return wf.getWorkflowDb(getInt("flow_id"));
    }
    
    /**
     * 取得任务的汇报流程中最后一条记录
     * @return
     */
    public WorkflowDb getWorkflowDbOfTask(long taskId, Date date) {
    	String sql = "select id from " + getTable().getName() + " where task_id=" + taskId + " and annex_type=" + TYPE_FLOW + " and add_date=" + SQLFilter.getDateStr(DateUtil.format(date, "yyyy-MM-dd"), "yyyy-MM-dd") + " order by add_date desc";
    	try {
			Iterator ir = listResult(sql, 1, 1).getResult().iterator();
			if (ir.hasNext()) {
				WorkPlanAnnexDb wpad = (WorkPlanAnnexDb)ir.next();
				return wpad.getWorkflowDb();
			}
		} catch (ResKeyException e) {
			e.printStackTrace();
		}
		return null;
    }

}
