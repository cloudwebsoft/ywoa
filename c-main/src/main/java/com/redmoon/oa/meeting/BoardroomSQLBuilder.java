package com.redmoon.oa.meeting;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.*;

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
public class BoardroomSQLBuilder {
    public static final String RESULT_APPLY = "-";
    public static final String RESULT_AGREE = "是";
    public static final String RESULT_DISAGREE = "否";
    public static final String RESULT_USED = "使用";
    public static final String RESULT_END = "结束";

    public BoardroomSQLBuilder() {
    }

    public static String getBoardroomApplySearchSql() {
        String sql =
                "select id from form_table_hysqd where myresult = " +
                StrUtil.sqlstr(RESULT_APPLY) + " and cws_status <> -1 order by flowId desc";
        return sql;
    }

    public static String getBoardroomAgreeSearchSql() {
        String sql =
                "select id from form_table_hysqd where myresult = " +
                StrUtil.sqlstr(RESULT_AGREE) + " order by flowId desc";
        return sql;
    }

    public static String getBoardroomDisagreeSearchSql() {
        String sql =
                "select id from form_table_hysqd where myresult = " +
                StrUtil.sqlstr(RESULT_DISAGREE) + " order by flowId desc";
        return sql;
    }

    public static String getBoardroomUsedSearchSql() {
        String sql = "";
        if (Global.db.equals(Global.DB_SQLSERVER)) {
            sql =
            "select id from form_table_hysqd where myresult = " +
            StrUtil.sqlstr(RESULT_AGREE) +
                " and start_date < getDate() and end_date > getDate() order by flowId desc";
        }
        else if (Global.db.equals(Global.DB_ORACLE)) {
            sql =
            "select id from form_table_hysqd where myresult = " +
            StrUtil.sqlstr(RESULT_AGREE) +
                " and start_date < sysdate and end_date > sysdate order by flowId desc";
        }
        else {
            sql =
            "select id from form_table_hysqd where myresult = " +
            StrUtil.sqlstr(RESULT_AGREE) +
                " and start_date < now() and end_date > now() order by flowId desc";
        }

        return sql;
    }

    public static String getBoardroomEndSearchSql() {
        String sql = "";
        if (Global.db.equals(Global.DB_SQLSERVER)) {
            sql =
                    "select id from form_table_hysqd where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                    " and end_date < getDate() order by flowId desc";
        } else if (Global.db.equals(Global.DB_ORACLE)) {
            sql =
                    "select id from form_table_hysqd where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                    " and end_date < sysdate order by flowId desc";
        } else {
            sql =
                    "select id from form_table_hysqd where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                    " and end_date < now() order by flowId desc";
        }
        return sql;
    }

    public static String getBoardroomSearchSql(String boardroomId) {
        String sql;
        if ("".equals(boardroomId)) {
        	sql =
                "select id from form_table_hysqd where myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +" order by flowId desc";
        }
        else {
        	sql =
                "select id from form_table_hysqd where hyshi = " +
                StrUtil.sqlstr(boardroomId) + " and  myresult = " +
                    StrUtil.sqlstr(RESULT_AGREE) +
                " order by flowId desc";        	
        }
        return sql;
    }

}
