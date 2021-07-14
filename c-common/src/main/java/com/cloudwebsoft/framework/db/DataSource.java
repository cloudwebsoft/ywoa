package com.cloudwebsoft.framework.db;

import cn.js.fan.web.Global;

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
public class DataSource {
    Connection connection = null;
    public DataSource() {
        connection = new Connection(Global.getDefaultDB());
    }

    public DataSource(String connName) {
        connection = new Connection(connName);
    }

    public Connection getConnection() {
        return connection;
    }

}
