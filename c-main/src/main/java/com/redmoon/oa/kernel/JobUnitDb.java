package com.redmoon.oa.kernel;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.util.Vector;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.db.ResultIterator;
import java.sql.SQLException;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ErrMsgException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class JobUnitDb extends QObjectDb {
	
    public JobUnitDb() {
    }

    public boolean create(ParamChecker pck) throws ResKeyException,ErrMsgException {
        boolean re = super.create(pck);

        // 如果调度器已start，则调度此新加的job
        SchedulerManager.getInstance().scheduleJob(""+getInt("id"), pck.getString("job_class"), pck.getString("cron"), pck.getString("data_map"));
        return re;
    }

    public boolean save() throws ResKeyException {
        boolean re = super.save();
        // 如果调度器已start，则重新调度此job
        if (re)
            SchedulerManager.getInstance().rescheduleJob(getInt("id"), getString("cron"));
        return re;
    }

    public int getJobId(String jobClass, String dataMap) {
        String sql = "select id from " + table.getName() + " where job_class=? and data_map=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {jobClass, dataMap});
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getJobId:" + e.getMessage());
        }
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord)ri.next();
            return rr.getInt(1);
        }
        else
            return -1;
    }

    public void delJobOfWorkflow(String flowTypeCode) {
        String sql = "select id from " + table.getName() + " where data_map=" + StrUtil.sqlstr(flowTypeCode);
        Vector v = list(sql);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            JobUnitDb jud = (JobUnitDb)ir.next();
            try {
                jud.del();
            }
            catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error("delJobOfWorkflow:" + e.getMessage());
            }
        }
    }

    public boolean delJobOfWorkplan(int planId) {
        int jobId = getJobId("com.redmoon.oa.job.WorkplanJob", "" + planId);
        if (jobId==-1)
            return true;
        JobUnitDb ju = (JobUnitDb)getQObjectDb(new Integer(jobId));
        boolean re = false;
        try {
            re = ju.del();
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("delJobOfWorkplan:" + e.getMessage());
        }
        return re;
    }


    public boolean del() throws ResKeyException {
        boolean re = false;
        SchedulerManager.getInstance().delJob(getInt("id"));
        re = super.del();
        return re;
    }
}
