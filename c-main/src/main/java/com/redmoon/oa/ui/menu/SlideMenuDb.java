package com.redmoon.oa.ui.menu;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultRecord;
import java.sql.SQLException;
import cn.js.fan.db.ResultIterator;
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
public class SlideMenuDb extends QObjectDb {
    public SlideMenuDb() {
    }

    public boolean isExist(long groupId, String code) {
        String sql = "select id from " + getTable().getName() + " where group_id=? and code=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {new Long(groupId), code});
        } catch (SQLException ex) {
        }
        if (ri.hasNext()) {
            return true;
        }
        return false;
    }

    public int getNextOrders(long groupId) {
        String sql = "select max(orders) from " + getTable().getName() + " where group_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {new Long(groupId)});
        } catch (SQLException ex) {
        }
        if (ri.hasNext()) {
            ResultRecord rr = (ResultRecord)ri.next();
            return rr.getInt(1) + 1;
        }
        return 0;
    }

    public void deleteOfGroup(long groupId) {
        String sql = "select id from " + getTable().getName() + " where group_id=?";
        Iterator ir = list(sql, new Object[]{new Long(groupId)}).iterator();
        while (ir.hasNext()) {
            SlideMenuDb smd = (SlideMenuDb)ir.next();
            try {
                smd.del();
            } catch (ResKeyException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }
    }
}
