package com.redmoon.forum.plugin.dig;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.ThreadTypeDb;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.plugin.base.IPluginViewListThread;
import com.redmoon.forum.plugin.base.UIListThread;
import com.redmoon.forum.SQLBuilder;

public class DigViewListThread implements IPluginViewListThread {
    HttpServletRequest request;

    public DigViewListThread(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
    }

    public String render(int position) {
        String str = "";

        switch (position) {
        case UIListThread.POS_RULE:
            int threadType = StrUtil.toInt(ParamUtil.get(request, "threadType"), ThreadTypeDb.THREAD_TYPE_NONE);

            String digListMode = ParamUtil.get(request, "digListMode");
            str = "[<a href='listtopic.jsp?boardcode=" + StrUtil.UrlEncode(boardCode) + "&threadType=" + threadType + "'>";
            if (!digListMode.equals("normal"))
                str += "<font color=red>掘客排列</font>";
            else
                str += "掘客排列";
            str += "</a>]&nbsp;&nbsp;";

            str += "[<a href='listtopic.jsp?pluginCode=" + DigUnit.code + "&digListMode=normal&boardcode=" + StrUtil.UrlEncode(boardCode) + "&threadType=" + threadType + "'>";
            if (digListMode.equals("normal"))
                str += "<font color=red>日期排列</font>";
            else
                str += "日期排列";
            str += "</a>]";

            // 使SQLBuilder中getListtopicSql时，能够调用本plugin的getListtopicSql方法
            SQLBuilder.setListForPluginAttribute(request, DigUnit.code);

            break;
        default:
            break;
        }
        return str; // str;
    }

    public String render(int position, MsgDb md) {
        String str = "";
        switch (position) {
        case UIListThread.POS_TOPIC_TITLE:
            DigUnit du = new DigUnit();
            // 避开总置顶贴中不属掘客的贴子
            if (du.isPluginBoard(md.getboardcode())) {
                str = "<font color='#cccccc'>[积分：" +
                      NumberUtil.round(md.getScore(), 1) + "]</font>";
            }
            break;
        default:
            break;
        }
        return str;
    }

    public String getHelpLink() {
        return DigSkin.LoadString(request, "helpLink");
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = sb.getBoardDb(DigUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        String note = "";
        return note;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(DigUnit.code, boardCode);
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode,
                                  String op, String timelimit, int threadType) {
        String digListMode = ParamUtil.get(request, "digListMode");
        if (digListMode.equals("normal"))
            return "";
        String sql = "";
        // 在populateBoardArrayToJSP中会使用到getListtopicSql，并且置request为null，所以此处要判断
        if (timelimit.equals("all")) {
            if (op.equals("showelite")) {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) +
                          " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                          " and thread_type=" + threadType +
                          " and iselite=1 ORDER BY msg_level desc, score desc, lydate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) +
                          " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                          " and iselite=1 ORDER BY msg_level desc, score desc, lydate desc";
                }
            } else {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and thread_type=" +
                          threadType + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " ORDER BY msg_level desc, score desc, lydate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " ORDER BY msg_level desc, score desc, lydate desc";
                }
            }
        } else {
            long cur = System.currentTimeMillis();
            long dlt = Long.parseLong(timelimit) * 24 * 60 * 60000;
            long afterDay = cur - dlt;

            if (op.equals("showelite")) {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and thread_type=" +
                          threadType +
                          " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " and iselite=1 ORDER BY msg_level desc, score desc, lydate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " and iselite=1 ORDER BY msg_level desc, score desc, lydate desc";
                }
            } else {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and thread_type=" +
                          threadType + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " ORDER BY msg_level desc, score desc, lydate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " ORDER BY msg_level desc, score desc, lydate desc";
                }
            }
        }

        // System.out.println(getClass() + " sql=" + sql);

        return sql;
    }

    private String boardCode;
}
