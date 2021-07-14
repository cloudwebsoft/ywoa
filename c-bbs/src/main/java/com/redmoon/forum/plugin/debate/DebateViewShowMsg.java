package com.redmoon.forum.plugin.debate;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.plugin.base.UIShowMsg;
import com.redmoon.forum.plugin.BoardDb;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.servlet.ServletResponseWrapperInclude;
import javax.servlet.jsp.JspWriter;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ParamUtil;

public class DebateViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    HttpServletResponse response;
    boolean isRoot = false;
    JspWriter out;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public DebateViewShowMsg(HttpServletRequest request, HttpServletResponse response, JspWriter out, String boardCode, MsgDb msgDb) {
        this.request = request;
        this.response = response;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
        this.out = out;
        if (msgDb.getReplyid()==-1)
            isRoot = true;
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb, String userId) {
        String sql;
        long rootid = rootMsgDb.getId();
        String viewpointType = ParamUtil.get(request, "viewpointType");
        if (userId.equals(""))
            sql = "select m.id from sq_message m left join plugin_debate_viewpoint v on m.id=v.msg_id where m.rootid=" + rootid +
                  " and m.check_status=" + MsgDb.CHECK_STATUS_PASS + " and v.viewpoint_type=" + viewpointType +
                  " ORDER BY m.lydate asc";
        else {
            sql = "select m.id from sq_message m left join plugin_debate_viewpoint v on m.id=v.msg_id where m.rootid=" + rootid +
                  " and m.check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and m.name=" + StrUtil.sqlstr(userId) + " and v.viewpoint_type=" + viewpointType +
                  " ORDER BY m.lydate asc";
        }
        return sql;
    }

    public String getQuickReplyFormElement() {
        String str = "";
        return "";
    }

    public String getQucikReplyNote() {
        String str = "";
        return str;
    }

    public String getNote() {
        // UserMgr um = new UserMgr();
        // String str = DebateSkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
        // return str;
        return "";
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            // getNote();
            break;
        case UIShowMsg.POS_AFTER_NOTE:
            String relativePath = "plugin/debate/inc_showtopic_after_note.jsp?rootid=" + msgDb.getRootid();
            try {
                RequestDispatcher rd = request.getRequestDispatcher(
                        relativePath);
                // 追加在插入plugin的语句之后
                rd.include(request,
                           new ServletResponseWrapperInclude(response, out));
                // 插入在header.jsp后
                // rd.include(request, response);
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("render:" + e.getMessage());
            }
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            // 如果是根贴
            if (!isRoot) {
                DebateViewpointDb dvd = new DebateViewpointDb();
                dvd = dvd.getDebateViewpointDb(msgDb.getId());
                if (dvd.getType()==dvd.TYPE_SUPPORT) {
                    str += "正方";
                }
                else if (dvd.getType()==dvd.TYPE_OPPOSE) {
                    str += "反方";
                }
                else
                    str += "第三方";
            }
            break;
        case UIShowMsg.POS_QUICK_REPLY_NOTE:
            str = getQucikReplyNote();
            break;
        case UIShowMsg.POS_QUICK_REPLY_ELEMENT:
            str = getQuickReplyFormElement();
            break;
        default:
        }
        return str;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setMsgDb(MsgDb msgDb) {
        this.msgDb = msgDb;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public MsgDb getMsgDb() {
        return msgDb;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(DebateUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return DebateSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
