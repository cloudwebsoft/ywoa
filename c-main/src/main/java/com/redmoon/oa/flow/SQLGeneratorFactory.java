package com.redmoon.oa.flow;

import cn.js.fan.web.*;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.base.ISQLGenerator;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SQLGeneratorFactory {
    private static Object initLock = new Object();

    public static ISQLGenerator isg = null;

    public SQLGeneratorFactory() {
        super();
    }

    public static ISQLGenerator getSQLGenerator() {
        if (isg==null) {
            synchronized (initLock) {
                if (Global.db.equalsIgnoreCase(Global.DB_MYSQL)) {
                    isg = (ISQLGenerator)SpringUtil.getBean("MySQLSQLGenerator");
                }
                else if (Global.db.equalsIgnoreCase(Global.DB_SQLSERVER)) {
                    isg = (ISQLGenerator)SpringUtil.getBean("SQLServerSQLGenerator");
                }
                else if (Global.db.equalsIgnoreCase(Global.DB_ORACLE)) {
                    isg = (ISQLGenerator)SpringUtil.getBean("OracleSQLGenerator");
                }
                else if (Global.db.equalsIgnoreCase(Global.DB_POSTGRESQL)) {
                    isg = (ISQLGenerator)SpringUtil.getBean("PostGreSQLGenerator");
                }
            }
        }
        return isg;
    }
}
