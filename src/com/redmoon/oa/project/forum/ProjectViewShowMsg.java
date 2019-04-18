package com.redmoon.oa.project.forum;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.plugin.base.UIShowMsg;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.ForumSkin;
import com.redmoon.forum.setup.UserLevelDb;
import com.redmoon.forum.person.UserMgr;
import com.redmoon.forum.person.UserDb;

public class ProjectViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public ProjectViewShowMsg(HttpServletRequest request, String boardCode, MsgDb msgDb) {
        this.request = request;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
        if (msgDb.getReplyid()==-1)
            isRoot = true;
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb, String userId) {
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

    public String getNote() {
        UserMgr um = new UserMgr();
        String str = ProjectSkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
        return str;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            str = ""; // getNote();
            break;
        case UIShowMsg.POS_BEFORE_MSG:
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
        return sb.isPluginBoard(ProjectUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return ProjectSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
