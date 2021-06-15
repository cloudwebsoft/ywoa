package com.redmoon.oa.project;

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
public class ProjectFavoriteDb extends QObjectDb {
    public ProjectFavoriteDb() {
    }

    public boolean isExist(String userName, long flowId) {
        return getProjectFavoriteDb(userName, flowId)!=null;
    }

    public ProjectFavoriteDb getProjectFavoriteDb(String userName, long flowId) {
        String sql = "select id from " + getTable().getName() + " where user_name=? and project_id=?";
        try {
            JdbcTemplate jt = new JdbcTemplate();
            ResultIterator ri = jt.executeQuery(sql, new Object[]{userName, new Long(flowId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return (ProjectFavoriteDb)getQObjectDb(new Long(rr.getLong(1)));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
