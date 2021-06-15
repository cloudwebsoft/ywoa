package com.redmoon.oa.workplan;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import java.sql.*;
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
public class WorkPlanFavoriteDb extends QObjectDb {
    public WorkPlanFavoriteDb() {
    }

    public boolean isExist(String userName, long workplanId) {
        return getWorkPlanFavoriteDb(userName, workplanId)!=null;
    }

    public WorkPlanFavoriteDb getWorkPlanFavoriteDb(String userName, long workplanId) {
        String sql = "select id from " + getTable().getName() + " where user_name=? and workplan_id=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{userName, new Long(workplanId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return (WorkPlanFavoriteDb)getQObjectDb(new Long(rr.getLong(1)));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
