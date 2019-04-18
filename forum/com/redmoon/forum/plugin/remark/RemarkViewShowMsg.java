package com.redmoon.forum.plugin.remark;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import com.cloudwebsoft.framework.base.*;
import org.apache.log4j.Logger;

public class RemarkViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;
    Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public RemarkViewShowMsg(HttpServletRequest request, String boardCode,
                             MsgDb msgDb) {
        this.request = request;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
        if (msgDb.getReplyid() == -1)
            isRoot = true;
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb,
                                  String userId) {
        return "";
    }

    public String getQuickReplyFormElement() {
        String str = "";
        return "";
    }

    public String getQucikReplyNote() {
        String str = "";
        return str;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            if (true) {
                RemarkDb rd = new RemarkDb();
                rd = (RemarkDb) rd.getQObjectDb(new Long(msgDb.getId()));
                if (rd!=null) {
                    if (!StrUtil.getNullStr(rd.getString("sign")).equals("")) {
                        RemarkConfig rc = RemarkConfig.getInstance();
                        // System.out.println(getClass() + " attr=" + request.getAttribute("img_sing_css_writed"));
                        if (request.getAttribute("img_sing_css_writed")==null) {
                            request.setAttribute("img_sing_css_writed", "y");
                            str += "<LINK href='plugin/" + RemarkUnit.code + "/remark.css' type=text/css rel=stylesheet />";
                        }
                        str += "<img id='rmk_sign_" + msgDb.getId() + "' class='img_sign' src='" + rc.getSignUrl(rd.getString("sign")) + "'>";
                    }
                }
            }
            break;
        case UIShowMsg.POS_AFTER_MSG:
            RemarkDb rd = new RemarkDb();
            rd = (RemarkDb)rd.getQObjectDb(new Long(msgDb.getId()));
            if (rd!=null) {
                str += RemarkSkin.LoadString(request, "remark_sep");
                String manager = "";
                if (rd.getString("manager")!=null) {
                    UserDb ud = new UserDb();
                    ud = ud.getUser(rd.getString("manager"));
                    manager = ud.getNick();
                }
                else
                    manager = UserDb.ADMIN;
                str += manager + "&nbsp;&nbsp;" + ForumSkin.formatDateTime(request, rd.getDate("remark_date")) + "<BR>";
                str += StrUtil.getNullStr(rd.getString("content"));
            }
            break;
        case UIShowMsg.POS_QUICK_REPLY_NOTE:
            break;
        case UIShowMsg.POS_QUICK_REPLY_ELEMENT:
            break;
        case UIShowMsg.POS_TOPIC_OPERATE_MENU:
            str += "<a href='" + request.getContextPath() + "/forum/plugin/remark/topic_remark.jsp?msgId=" + msgDb.getId() + "&boardcode=" + StrUtil.UrlEncode(msgDb.getboardcode()) + "&privurl=" + StrUtil.getUrl(request) + "'>评价贴子</a>";
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
        return sb.isPluginBoard(RemarkUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return RemarkSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;

}
