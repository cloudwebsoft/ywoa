package com.redmoon.forum.sms;

import javax.servlet.http.*;
import cn.js.fan.util.*;
import org.apache.log4j.*;

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
public class SMSSendRecordMgr {
    Logger logger = Logger.getLogger(SMSSendRecordMgr.class.getName());

    public SMSSendRecordMgr() {
    }

    public static String getSearchSendSMS(HttpServletRequest request) {
        String sql = "select id from sms_record";
        String con = "";

        String userName = ParamUtil.get(request, "userName");
        if (!userName.equals("")) {
            if (!con.equals(""))
                con += " and ";
            con += "userName like " + StrUtil.sqlstr("%" + userName + "%");
        }

        String sendMobile = ParamUtil.get(request, "sendMobile");
        if (!sendMobile.equals("")) {
            if (!con.equals(""))
                con += " and ";
            con += "sendMobile like " + StrUtil.sqlstr("%" + sendMobile + "%");
        }

        String msgText = ParamUtil.get(request, "msgText");
        if (!msgText.equals("")) {
            if (!con.equals(""))
                con += " and ";
            con += "msgText like " + StrUtil.sqlstr("%" + msgText + "%");
        }

        String strFromSendTime = ParamUtil.get(request, "fromSendTime");
        java.util.Date fromSendTime = DateUtil.parse(strFromSendTime,
                "yyyy-MM-dd");
        String strToSendTime = ParamUtil.get(request, "toSendTime");
        java.util.Date toSendTime = DateUtil.parse(strToSendTime, "yyyy-MM-dd");
        if (fromSendTime != null && toSendTime != null) {
            if (!con.equals(""))
                con += " and ";
            con += "sendTime >= " + StrUtil.sqlstr(strFromSendTime) + " and sendTime <= " +
                    StrUtil.sqlstr(strToSendTime);
        } else {
            if (fromSendTime != null) {
                if (!con.equals(""))
                    con += " and ";
                con += "sendTime >= " + StrUtil.sqlstr(strFromSendTime);
            }
            if (toSendTime != null) {
                if (!con.equals(""))
                    con += " and ";
                con += "sendTime <= " + StrUtil.sqlstr(strToSendTime);
            }
        }

        if (!con.equals("")) {
            con = " where " + con + " order by userName";
        }

        sql += con;
        // System.out.println("SMSSendRecordMgr.java sql=" + sql);
        return sql;
    }

    public static String getSearchSendSMS() {
        String sql = "select id from sms_record";
        return sql;
     }

}
