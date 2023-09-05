package com.redmoon.oa.flow;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import java.sql.*;
import cn.js.fan.db.ResultRecord;
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
public class WorkflowFavoriteDb extends QObjectDb {
    public WorkflowFavoriteDb() {
    }

    public boolean isExist(String userName, long flowId) {
        return getWorkflowFavoriteDb(userName, flowId)!=null;
    }

    public WorkflowFavoriteDb getWorkflowFavoriteDb(String userName, long flowId) {
        String sql = "select id from " + getTable().getName() + " where user_name=? and flow_id=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{userName, new Long(flowId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return (WorkflowFavoriteDb)getQObjectDb(new Long(rr.getLong(1)));
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return null;
    }
}
