package com.redmoon.oa.security;

import com.cloudwebsoft.framework.base.QObjectDb;
import java.util.Vector;

/**
 * <p>Title: 管理服务器内外网IP</p>
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
public class ServerIPDb extends QObjectDb {
    public ServerIPDb() {
    }

    public ServerIPDb getServerIPPrivByIP(String ip) {
        String sql = "select id from " + getTable().getName() + " where ip=?";
        Vector v = list(sql, new Object[]{ip});
        if (v.size()>0) {
            return (ServerIPDb)v.elementAt(0);
        }
        return null;
    }
}
