package com.redmoon.oa.flow;

import com.cloudwebsoft.framework.base.QObjectDb;
import cn.js.fan.db.ListResult;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Iterator;

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
public class FormQueryReportDb extends QObjectDb {
    public static final String TYPE_USER = "user";
    public static final String TYPE_DEPT = "dept";
    public static final String TYPE_FIELD = "field";

    public FormQueryReportDb() {
    }

    /**
     * 取得某用户设计的最后一个报表，用于创建操作后，取得ID
     * @param userName String
     * @return FormQueryReportDb
     */
    public FormQueryReportDb getLastFormQueryReportDb(String userName) {
        String sql = "select id from " + getTable().getName() + " where user_name=? order by id desc";
        try {
            ListResult lr = listResult(sql, new Object[] {userName}, 1, 1);
            Iterator ir = lr.getResult().iterator();
            if (ir.hasNext()) {
               FormQueryReportDb fqrd = (FormQueryReportDb)ir.next();
               return fqrd;
            }
        } catch (ResKeyException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return null;
    }
}
