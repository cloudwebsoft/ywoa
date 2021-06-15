package com.redmoon.forum.plugin.witkey;

import java.util.*;
import javax.servlet.http.*;
import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import java.sql.Timestamp;
import com.cloudwebsoft.framework.servlet.ServletResponseWrapperInclude;
import javax.servlet.RequestDispatcher;
import com.cloudwebsoft.framework.util.LogUtil;
import javax.servlet.jsp.JspWriter;

public class WitkeyViewListThread implements IPluginViewListThread {
    HttpServletRequest request;
    HttpServletResponse response;
    JspWriter out;

    public WitkeyViewListThread(HttpServletRequest request, HttpServletResponse response, JspWriter out, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
        this.response = response;
        this.out = out;
    }

    public String render(int position) {
        String str = "";

        switch (position) {
        case UIListThread.POS_RULE:
            String witkeyTaskType = ParamUtil.get(request, "witkeyTaskType");
            str += "[<a href='listtopic.jsp?pluginCode=" + WitkeyUnit.code +
                    "&witkeyTaskType=all&boardcode=" +
                    StrUtil.UrlEncode(boardCode) + "'>";
            if (witkeyTaskType.equals("all"))
                str += "<font color=red>所有任务</font>";
            else
                str += "所有任务";
            str += "</a>]&nbsp;&nbsp;";

            str += "[<a href='listtopic.jsp?pluginCode=" + WitkeyUnit.code +
                    "&witkeyTaskType=starting&boardcode=" +
                    StrUtil.UrlEncode(boardCode) + "'>";
            if (witkeyTaskType.equals("starting"))
                str += "<font color=red>正在进行</font>";
            else
                str += "正在进行";
            str += "</a>]&nbsp;&nbsp;";

            str += "[<a href='listtopic.jsp?pluginCode=" + WitkeyUnit.code +
                    "&witkeyTaskType=recommend&boardcode=" +
                    StrUtil.UrlEncode(boardCode) + "'>";
            if (witkeyTaskType.equals("recommend"))
                str += "<font color=red>推荐任务</font>";
            else
                str += "推荐任务";
            str += "</a>]&nbsp;&nbsp;";

            str += "[<a href='listtopic.jsp?pluginCode=" + WitkeyUnit.code +
                    "&witkeyTaskType=delay&boardcode=" +
                    StrUtil.UrlEncode(boardCode) + "'>";
            if (witkeyTaskType.equals("delay"))
                str += "<font color=red>延期任务</font>";
            else
                str += "延期任务";
            str += "</a>]&nbsp;&nbsp;";

            str += "[<a href='listtopic.jsp?pluginCode=" + WitkeyUnit.code +
                    "&witkeyTaskType=choosedraft&boardcode=" +
                    StrUtil.UrlEncode(boardCode) + "'>";
            if (witkeyTaskType.equals("choosedraft"))
                str += "<font color=red>正在选稿</font>";
            else
                str += "正在选稿";
            str += "</a>]&nbsp;&nbsp;";

            str += "[<a href='listtopic.jsp?pluginCode=" + WitkeyUnit.code +
                    "&witkeyTaskType=pay&boardcode=" +
                    StrUtil.UrlEncode(boardCode) + "'>";
            if (witkeyTaskType.equals("pay"))
                str += "<font color=red>等待支付</font>";
            else
                str += "等待支付";
            str += "</a>]&nbsp;&nbsp;";

            str += getBoardNote();
            break;
        default:
            break;
        }
        return str; // str;
    }

    public String getHelpLink() {
        return WitkeySkin.LoadString(request, "helpLink");
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getBoardRule() {
        BoardDb sb = new BoardDb();
        sb = sb.getBoardDb(WitkeyUnit.code, boardCode);
        return sb.getBoardRule();
    }

    public String getBoardNote() {
        // 快速通道
        // 写slidemenu
        String relativePath = "plugin/witkey/inc_slidemenu.jsp?pageUrl=" +
                              StrUtil.
                              UrlEncode("forum/listtopic.jsp?boardcode=" +
                                        StrUtil.UrlEncode(boardCode) +
                                        "&pluginCode=" + WitkeyUnit.code +
                                        "&catalogCode=");
        try {
            RequestDispatcher rd = request.getRequestDispatcher(
                    relativePath);
            // 追加在插入plugin的语句之后
            rd.include(request,
                       new ServletResponseWrapperInclude(response, out));
            // 插入在header.jsp后
            // rd.include(request, response);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("render:" + e.getMessage());
        }
        return "";
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(WitkeyUnit.code, boardCode);
    }

    public String render(int position, MsgDb md) {
        return "";
    }

    public String getListtopicSql(HttpServletRequest request, String boardcode,
                                  String op, String timelimit, int threadType) {
        String witkeyTaskType = ParamUtil.get(request, "witkeyTaskType");

        String catalogCode = ParamUtil.get(request, "catalogCode");

        String orderBy = " ORDER BY t.msg_level desc,t.redate desc";

        String sql = "select w.msg_root_id from plugin_witkey w, sq_thread t where w.msg_root_id=t.id and t.boardcode=" +
                  StrUtil.sqlstr(boardCode) + " and t.check_status=" +
                  MsgDb.CHECK_STATUS_PASS;
        if (witkeyTaskType.equals("starting")) {
            long lDate = System.currentTimeMillis();
            Timestamp ts = new Timestamp(lDate);
            java.util.Date date = DateUtil.parse(ts.toString(),
                                                 "yyyy-MM-dd");
            sql = "select w.msg_root_id from plugin_witkey w, sq_thread t where w.msg_root_id=t.id and t.boardcode=" +
                  StrUtil.sqlstr(boardCode) + " and t.check_status=" +
                  MsgDb.CHECK_STATUS_PASS + " and w.end_date >= " +
                  StrUtil.sqlstr(Long.toString(date.getTime()));
        } else if (witkeyTaskType.equals("recommend")) {
            long lDate = System.currentTimeMillis();
            Timestamp ts = new Timestamp(lDate);
            java.util.Date date = DateUtil.parse(ts.toString(),
                                                 "yyyy-MM-dd");
            sql = "select w.msg_root_id from plugin_witkey w, sq_thread t where w.msg_root_id=t.id and t.boardcode=" +
                  StrUtil.sqlstr(boardCode) + " and t.check_status=" +
                  MsgDb.CHECK_STATUS_PASS + " and w.end_date >= " +
                  StrUtil.sqlstr(Long.toString(date.getTime())) +
                  " and w.level<>0";
        } else if (witkeyTaskType.equals("delay")) {
            sql = "select w.msg_root_id from plugin_witkey w, sq_thread t where w.msg_root_id=t.id and t.boardcode=" +
                  StrUtil.sqlstr(boardCode) +
                  " and t.check_status=" +
                  MsgDb.CHECK_STATUS_PASS +
                  " and w.status=2";
        } else if (witkeyTaskType.equals("choosedraft")) {
            sql = "select w.msg_root_id from plugin_witkey w, sq_thread t where w.msg_root_id=t.id and t.boardcode=" +
                  StrUtil.sqlstr(boardCode) +
                  " and t.check_status=" +
                  MsgDb.CHECK_STATUS_PASS +
                  " and w.status=3";
        } else if (witkeyTaskType.equals("pay")) {
            sql = "select w.msg_root_id from plugin_witkey w, sq_thread t where w.msg_root_id=t.id and t.boardcode=" +
                  StrUtil.sqlstr(boardCode) +
                  " and t.check_status=" +
                  MsgDb.CHECK_STATUS_PASS +
                  " and w.status=4";
        }
        if (!catalogCode.equals("") && !catalogCode.equals(Leaf.CODE_ROOT))
            sql += " and w.catalog_code=" + StrUtil.sqlstr(catalogCode);
        sql += orderBy;
        return sql;
    }

    private String boardCode;
}
