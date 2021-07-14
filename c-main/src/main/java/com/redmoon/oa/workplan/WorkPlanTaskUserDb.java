package com.redmoon.oa.workplan;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;
import java.util.Iterator;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.sql.*;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ResKeyException;

import com.redmoon.oa.oacalendar.OACalendarDb;
import cn.js.fan.util.*;
import java.util.Vector;

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
public class WorkPlanTaskUserDb extends QObjectDb {
    public WorkPlanTaskUserDb() {
    }

    /**
     * 判断在任务中，用户是否已存在
     * @param taskId long
     * @param userName String
     * @return boolean
     */
    public boolean isTaskUserExist(long taskId, String userName) {
        String sql = "select id from " + getTable().getName() + " where task_id=? and user_name=?";
        Iterator ir = list(sql, new Object[] {new Long(taskId), userName}).iterator();
        if (ir.hasNext()) {
            return true;
        }
        return false;
    }

    /**
     * 取得最后一个用户的orders
     */
    public int getMaxOrdersOfTaskUsers(long taskId) {
        int orders = 0;
        String sql = "select max(orders) from " + getTable().getName() + " where task_id=? order by orders asc";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new Long(taskId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                orders = rr.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return orders;
    }

    /**
     * 根据顺序号取得任务，用于上移、下移
     * @param workplanId
     * @param orders
     * @return
     */
    public WorkPlanTaskUserDb getTaskUserByOrders(long taskId, int orders) {
        String sql = "select id from " + getTable().getName() + " where task_id=? and orders=?";
        Iterator ir = list(sql, new Object[] {new Long(taskId), new Integer(orders)}).iterator();
        if (ir.hasNext()) {
            return (WorkPlanTaskUserDb) ir.next();
        }
        return null;
    }

    /**
     * 取出计划中某人在某月每天的工作负荷，存于数组中
     * @param userName String
     * @param workplanId int
     * @param year int
     * @param month int 从0开始
     * @return double[] 单位为天
     */
    public double[] getBurthenOfWorkPlan(String userName, int workplanId, int year, int month) {
        int dayCount = DateUtil.getDayCount(year, month);
        double[] ary = new double[dayCount];
        for (int i=0; i<dayCount; i++)
            ary[i] = 0;

        String s = year + "-" + (month+1) + "-1";
        java.util.Date start = DateUtil.parse(s, "yyyy-MM-dd");
        java.util.Date end = DateUtil.addMinuteDate(DateUtil.addMonthDate(start, 1), -1);

        // 取出参与的任务
        String sql = "select t.id from work_plan_task t, work_plan_task_user u where t.work_plan_id=? and u.user_name=? and t.id=u.task_id and ((t.start_date<=? and t.end_date>=?) or (t.start_date>=? and t.end_date<=?) or (t.start_date<=? and t.end_date>=?))";

        String taskUserSql = "select id from work_plan_task_user where task_id=? and user_name=?";
        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        Iterator ir = wptd.list(sql, new Object[]{new Integer(workplanId), userName, start, start, start, end, end, end}).iterator();
        while (ir.hasNext()) {
            wptd = (WorkPlanTaskDb)ir.next();

            java.util.Date ts = wptd.getDate("start_date");
            java.util.Date te = wptd.getDate("end_date");

            // 取出任务落在该月范围内的区间
            if (DateUtil.compare(ts, start)==2)
                ts = start;
            if (DateUtil.compare(te, end)==1)
                te = end;

            double dur = 0;
            Iterator irUser = list(taskUserSql, new Object[]{new Long(wptd.getLong("id")), userName}).iterator();
            if (irUser.hasNext()) {
                WorkPlanTaskUserDb wptud = (WorkPlanTaskUserDb)irUser.next();
                dur = wptud.getDouble("duration");
            }

            // 算出任务周期内每天的平均工作日，即将工作天数分摊至任务周期内的每一天
            double av_dur = dur / wptd.getInt("duration");

            // 加入至ary中
            int is = DateUtil.getDay(ts);
            int ie = DateUtil.getDay(te);
            LogUtil.getLog(getClass()).info("getTaskBurthen: taskId=" + wptd.getLong("id") + " dur=" + dur + " duration=" + wptd.getInt("duration") + " is=" + is + " ie=" + ie);
            OACalendarDb oacal = new OACalendarDb();
            for (int i=is; i<=ie; i++) {
                // 如果是工作日才计算
                java.util.Date d = DateUtil.addDate(ts, i-is);
                oacal = (OACalendarDb) oacal.getQObjectDb(d);
                if (oacal.getInt("date_type") == OACalendarDb.DATE_TYPE_WORK) {
                    ary[i-1] += av_dur;
                }
            }
        }

        return ary;
    }

    /**
     * 取出某人于某月的工作负荷，存于数组中
     * @param userName String 用户名
     * @param year int 年份
     * @param month int 从0开始
     * @return double[] 单位为天
     */
    public double[] getBurthen(String userName, int year, int month) {
        int dayCount = DateUtil.getDayCount(year, month);
        double[] ary = new double[dayCount];
        for (int i=0; i<dayCount; i++)
            ary[i] = 0;

        String s = year + "-" + (month+1) + "-1";
        java.util.Date start = DateUtil.parse(s, "yyyy-MM-dd");
        java.util.Date end = DateUtil.addMinuteDate(DateUtil.addMonthDate(start, 1), -1);

        // 取出参与的任务
        String sql = "select t.id from work_plan_task t, work_plan_task_user u where u.user_name=? and t.id=u.task_id and ((t.start_date<=? and t.end_date>=?) or (t.start_date>=? and t.end_date<=?) or (t.start_date<=? and t.end_date>=?))";

        String taskUserSql = "select id from work_plan_task_user where task_id=? and user_name=?";
        WorkPlanTaskDb wptd = new WorkPlanTaskDb();
        Iterator ir = wptd.list(sql, new Object[]{userName, start, start, start, end, end, end}).iterator();
        while (ir.hasNext()) {
            wptd = (WorkPlanTaskDb)ir.next();

            java.util.Date ts = wptd.getDate("start_date");
            java.util.Date te = wptd.getDate("end_date");

            // 取出任务落在该月范围内的区间
            if (DateUtil.compare(ts, start)==2)
                ts = start;
            if (DateUtil.compare(te, end)==1)
                te = end;

            double dur = 0;
            Iterator irUser = list(taskUserSql, new Object[]{new Long(wptd.getLong("id")), userName}).iterator();
            if (irUser.hasNext()) {
                WorkPlanTaskUserDb wptud = (WorkPlanTaskUserDb)irUser.next();
                dur = wptud.getDouble("duration");
            }

            // 算出任务周期内每天的平均工作日，即将工作天数分摊至任务周期内的每一天
            double av_dur = dur / wptd.getInt("duration");

            // 加入至ary中
            int is = DateUtil.getDay(ts);
            int ie = DateUtil.getDay(te);
            LogUtil.getLog(getClass()).info("getTaskBurthen: taskId=" + wptd.getLong("id") + " dur=" + dur + " duration=" + wptd.getInt("duration") + " is=" + is + " ie=" + ie);
            OACalendarDb oacal = new OACalendarDb();
            for (int i=is; i<=ie; i++) {
                // 如果是工作日才计算
                java.util.Date d = DateUtil.addDate(ts, i-is);
                oacal = (OACalendarDb) oacal.getQObjectDb(d);
                if (oacal.getInt("date_type") == OACalendarDb.DATE_TYPE_WORK) {
                    ary[i-1] += av_dur;
                }

            }
        }

        return ary;
    }


    /**
     * 删除属于某任务的用户
     * @param taskId long
     */
    public void delOfTask(long taskId) {
        String sql = "select id from " + getTable().getName() + " where task_id=?";
        Iterator ir = list(sql, new Object[]{new Long(taskId)}).iterator();
        while (ir.hasNext()) {
            WorkPlanTaskUserDb wptud = (WorkPlanTaskUserDb)ir.next();
            try {
                wptud.del();
            } catch (ResKeyException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 取得任务中的所有用户
     * @param taskId long
     * @return Vector
     */
    public Vector getTaskUsers(long taskId) {
        String sql = "select id from " + getTable().getName() + " where task_id=?";
        return list(sql, new Object[] {new Long(taskId)});
    }
    /**
     * 根据任务ID和参与人删除对应记录
     * @param taskId long
     * @param userName String
     * @return boolean
     */
    public boolean delByTaskAndUser(long taskId, String userName) {
        String sql = "select id from " + getTable().getName() + " where task_id=? and user_name=?";
        Iterator ir = list(sql, new Object[] {new Long(taskId), userName}).iterator();
        WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
        while(ir.hasNext())
        {
        	wptud = (WorkPlanTaskUserDb)ir.next();
        	try {
				wptud.del();
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
        }
        return true;
    }
}
