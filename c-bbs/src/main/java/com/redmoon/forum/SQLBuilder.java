package com.redmoon.forum;

import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.plugin.PluginMgr;
import com.redmoon.forum.plugin.PluginUnit;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import com.redmoon.forum.plugin.base.IPluginUI;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.plugin.base.IPluginViewListThread;
import java.util.Iterator;

/**
 * <p>Title:SQL语句构造器 </p>
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
public class SQLBuilder {
    public static final String REQUEST_ATTRIBUTE_SQL_LIST = "req_att_sql_list";

    public SQLBuilder() {
    }

    public static String getShowtopictreeSql(long rootid) {
        String sql = "select id from sq_message where rootid=" + rootid +
                     " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                     " ORDER BY orders";
        return sql;
    }

    public static String getShowtopicSql(HttpServletRequest request,
                                         HttpServletResponse response,
                                         JspWriter out, MsgDb rootMsgDb,
                                         String userId) {
        String sql;
        String pluginCode = ParamUtil.get(request, "pluginCode");
        if (!pluginCode.equals("")) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(pluginCode);
            IPluginUI ipu = pu.getUI(request, response, out);
            IPluginViewShowMsg pv = ipu.getViewShowMsg(rootMsgDb.getboardcode(),
                    rootMsgDb);
            sql = pv.getShowtopicSql(request, rootMsgDb, userId);
            if (!sql.equals(""))
                return sql;
        }

        long rootid = rootMsgDb.getId();
        if (userId.equals(""))
            sql = "select id from sq_message where rootid=" + rootid +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " ORDER BY lydate asc"; // orders"; 这样会使得顺序上不按时间，平板式时会让人觉得奇怪
        else {
            sql = "select id from sq_message where rootid=" + rootid +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and name=" + StrUtil.sqlstr(userId) +
                  " ORDER BY lydate asc";
        }

        return sql;
    }

    public static String getNewMsgOfBlog(long blogId) {
        return "select id from sq_thread where blog_id=" + blogId +
                " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                " and isBlog=1 ORDER BY lydate desc";
    }

    public static String getMyblogSql(String blogUserDir,
                                      long blogId) {
        String sql = "";
        if (blogUserDir.equals(""))
            // sql = "select id from sq_message where replyid=-1 and name=" + StrUtil.sqlstr(userName) + " and isBlog=1 ORDER BY lydate desc";
            sql = "select id from sq_thread where blog_id=" + blogId +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and isBlog=1 ORDER BY lydate desc";
        else
            // sql = "select id from sq_message where replyid=-1 and name=" + StrUtil.sqlstr(userName) + " and blogUserDir=" + StrUtil.sqlstr(blogUserDir) + " and isBlog=1 ORDER BY lydate desc";
            sql = "select id from sq_thread where blog_id=" +
                  blogId + " and blogUserDir=" +
                  StrUtil.sqlstr(blogUserDir) +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and isBlog=1 ORDER BY lydate desc";
        return sql;
    }

    /**
     * 用于blog/user/listtopic.jsp页面
     * @return String
     */
    public static String getListtopicSqlOfBlog(long blogId, String blogUserDir) {
        String sql;
        if (blogUserDir.equals(""))
            sql = "select id from sq_thread where blog_id=" + blogId +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and isBlog=1 ORDER BY lydate desc";
        else
            sql = "select id from sq_thread where blog_id=" + blogId +
                  " and blogUserDir=" + StrUtil.sqlstr(blogUserDir) +
                  " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and isBlog=1 ORDER BY lydate desc";
        return sql;
    }

    public static String getListcommentSqlOfBlog(long blogId) {
        return "select id from sq_message where blog_id=" + blogId +
                " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                " and isBlog=1 and replyid<>-1 ORDER BY lydate desc";
    }

    public static String getNewReplySqlOfBlog(long blogId) {
        return "select id from sq_message where blog_id=" + blogId +
                " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                " and isBlog=1 and replyid<>-1 order by lydate desc";
    }

    public static void setListForPluginAttribute(HttpServletRequest request, String pluginCode) {
        request.setAttribute("list_for_plugin", pluginCode);
    }

    /**
     * 列出版块中的thread，用于ForumTemplateImpl.java
     * @param boardCode String
     * @return String
     */
    public static String getListTopicSql(String boardCode) {
        if (!boardCode.equals("")) {
            return "select id from sq_thread where boardcode=" +
                    StrUtil.sqlstr(boardCode) + " and check_status=" +
                    MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                    MsgDb.LEVEL_TOP_BOARD +
                    " ORDER BY msg_level desc,redate desc";
        } else {
            // 取出最新贴子，如按下行，则取出的贴子中置顶贴会排在前面，效果不好，看起来就不是最新贴子了
            /*
            return "select id from sq_thread where check_status=" +
                    MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                    MsgDb.LEVEL_TOP_BOARD +
                    " ORDER BY msg_level desc,redate desc";
            */
           return "select id from sq_thread where check_status=" +
                   MsgDb.CHECK_STATUS_PASS + " ORDER BY lydate desc";
        }
    }

    /**
     *
     * @param request HttpServletRequest
     * @param boardcode String
     * @param op String
     * @param timelimit String
     * @param threadType int
     * @return String
     */
    public static String getListtopicSql(HttpServletRequest request,
                                         HttpServletResponse response,
                                         JspWriter out, String boardcode,
                                         String op, String timelimit,
                                         int threadType) {
        String sql = "";
        String pluginCode = "";
        if (request!=null)
            pluginCode = ParamUtil.get(request, "pluginCode");
        if (pluginCode.equals("")) {
            // 当RSS调用时，request为null
            if (request!=null)
                pluginCode = StrUtil.getNullStr((String)request.getAttribute("list_for_plugin"));
        }
        if (!pluginCode.equals("")) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(pluginCode);
            IPluginUI ipu = pu.getUI(request, response, out);
            IPluginViewListThread pv = ipu.getViewListThread(boardcode);
            sql = pv.getListtopicSql(request, boardcode, op, timelimit,
                                     threadType);
            if (!sql.equals(""))
                return sql;
        }

        // 在populateBoardArrayToJSP中会使用到getListtopicSql，并且置request为null，所以此处要判断
        if (timelimit.equals("all")) {
            if (op.equals("showelite")) {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) +
                          " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                          " and thread_type=" + threadType +
                          " and iselite=1 ORDER BY msg_level desc,redate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) +
                          " and check_status=" + MsgDb.CHECK_STATUS_PASS +
                          " and iselite=1 ORDER BY msg_level desc,redate desc";
                }
            } else {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and thread_type=" +
                          threadType + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " ORDER BY msg_level desc,redate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " ORDER BY msg_level desc,redate desc";
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
                          " and iselite=1 ORDER BY msg_level desc,redate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " and iselite=1 ORDER BY msg_level desc,redate desc";
                }
            } else {
                if (threadType != ThreadTypeDb.THREAD_TYPE_NONE) {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and thread_type=" +
                          threadType + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " ORDER BY msg_level desc,redate desc";
                } else {
                    sql = "select id from sq_thread where boardcode=" +
                          StrUtil.sqlstr(boardcode) + " and check_status=" +
                          MsgDb.CHECK_STATUS_PASS + " and msg_level<=" +
                          MsgDb.LEVEL_TOP_BOARD +
                          " and lydate>" + StrUtil.sqlstr("" + afterDay) +
                          " ORDER BY msg_level desc,redate desc";
                }
            }
        }
        return sql;
    }

    public static String getRankExperience() {
        String sql = "select name from sq_user order by experience desc";
        return sql;
    }

    public static String getRankCredit() {
        String sql = "select name from sq_user order by credit desc";
        return sql;
    }

    public static String getRankGold() {
        String sql = "select name from sq_user order by gold desc";
        return sql;
    }

    public static String getRankAddCount() {
        String sql = "select name from sq_user order by addCount desc";
        return sql;
    }
    
    public static String getNewUsers() {
		String sql = "select name from sq_user where isValid=1 ORDER BY RegDate desc";
    	return sql;
    }

}
