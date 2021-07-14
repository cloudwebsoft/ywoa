package com.redmoon.oa.ui;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import java.sql.SQLException;
import java.util.Iterator;

public class PortalMenuDb extends QObjectDb {

    public boolean isExist(long portalId, String code) {
        String sql = "select id from " + getTable().getName() + " where portal_id=? and code=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {new Long(portalId), code});
            if (ri.hasNext()) {
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    public PortalMenuDb getPortalMenuDb(long portalId, String code) {
        String sql = "select id from " + getTable().getName() + " where portal_id=? and code=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {new Long(portalId), code});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return (PortalMenuDb)getQObjectDb(new Long(rr.getLong(1)));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int getNextOrders(long portalId) {
        String sql = "select max(orders) from " + getTable().getName() + " where portal_id=?";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql, new Object[] {new Long(portalId)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                return rr.getInt(1) + 1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public void deleteOfGroup(long portalId) {
        String sql = "select id from " + getTable().getName() + " where portal_id=?";
        Iterator ir = list(sql, new Object[]{new Long(portalId)}).iterator();
        while (ir.hasNext()) {
            PortalMenuDb smd = (PortalMenuDb)ir.next();
            try {
                smd.del();
            } catch (ResKeyException ex) {
                ex.printStackTrace();
            }
        }
    }
}
