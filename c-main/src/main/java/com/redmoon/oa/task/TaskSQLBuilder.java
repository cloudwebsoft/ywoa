package com.redmoon.oa.task;

import cn.js.fan.util.*;

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
public class TaskSQLBuilder {
    public TaskSQLBuilder() {
    }

    public static String getUserJoinTask(String userName, String status) {
        if (status.equals("")) {
            String sql = "select distinct rootid from task where person = " +
                         StrUtil.sqlstr(userName) + " and type<" + TaskDb.TYPE_RESULT + " order by rootid desc";
            return sql;

        } else {
            String sql =
                    "select distinct rootid from task where status=" +
                    status +
                    " and person = " + StrUtil.sqlstr(userName) + " and type<" +
                    TaskDb.TYPE_RESULT + " order by rootid desc";
            return sql;
        }
    }

}
