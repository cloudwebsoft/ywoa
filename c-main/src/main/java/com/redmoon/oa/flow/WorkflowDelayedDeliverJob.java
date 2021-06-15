package com.redmoon.oa.flow;

import org.quartz.Job;
import org.quartz.JobExecutionException;
import org.quartz.JobExecutionContext;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import java.sql.*;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class WorkflowDelayedDeliverJob implements Job {
    public WorkflowDelayedDeliverJob() {
    }

    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        String sql = "select id from flow_action where status=" + WorkflowActionDb.STATE_DELAYED +
                     " and date_delayed<=?";
        WorkflowActionDb nextwa = new WorkflowActionDb();
        WorkflowDb wf = new WorkflowDb();

        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {new java.util.Date()});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                nextwa = nextwa.getWorkflowActionDb(rr.getInt(1));

                wf = wf.getWorkflowDb(nextwa.getFlowId());

                String[] users = StrUtil.split(nextwa.getUserName(), ",");
                // System.out.println(getClass() + " nextwa.getUserName()=" + nextwa.getUserName());
                LogUtil.getLog(getClass()).info("nextwa.getUserName()=" + nextwa.getUserName());

                nextwa.setStatus(WorkflowActionDb.STATE_DOING);
                try {
                    nextwa.save();
                } catch (ErrMsgException ex1) {
                    ex1.printStackTrace();
                    LogUtil.getLog(getClass()).error(StrUtil.trace(ex1));
                    return;
                }

                // 取得其前一个节点
                Iterator ir = nextwa.getLinkFromActions().iterator();
                if (ir.hasNext()) {
                    WorkflowActionDb wa = (WorkflowActionDb) ir.next();

                    MyActionDb myad = new MyActionDb();
                    myad = myad.getMyActionDbOfActionChecked(wa);

                    // 通知用户办理
                    int len = users.length;
                    for (int n = 0; n < len; n++) {
                        // System.out.println(getClass() + " users[" + n + "]=" + users[n]);
                        LogUtil.getLog(getClass()).info("users[" + n + "]=" + users[n]);
                        if (users[n]==null || "".equals(users[n])) {
                            continue;
                        }
                        try {
                            MyActionDb mad = wf.notifyUser(users[n],
                                                           new java.util.Date(),
                                                           myad.getId(), nextwa,
                                                           nextwa, WorkflowActionDb.STATE_DOING,
                                                           (long) wf.getId());
                        } catch (ErrMsgException ex2) {
                            ex2.printStackTrace();
                            LogUtil.getLog(getClass()).error(StrUtil.trace(ex2));
                        }
                        // addTmpUserNameActived(mad);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }

    }
}
