package com.redmoon.forum.plugin.present;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.plugin.base.UIShowMsg;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.BoardDb;
import com.redmoon.forum.person.UserMgr;
import java.util.Iterator;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.base.ObjectBlockIterator;

public class PresentViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public PresentViewShowMsg(HttpServletRequest request, String boardCode, MsgDb msgDb) {
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
        String str = PresentSkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
        return str;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            str = ""; // getNote();
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            // 如果是根贴
            if (isRoot) {
                // str += "<script src='plugin/" + PresentUnit.code + "/script.js'></script>\n";
                str += "得分记录：&nbsp;&nbsp;";
                str += "<a href=# onclick=\"window.open('plugin/present/give.jsp?msgId=" + msgDb.getId() + "', '_blank', 'toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width=480,height=320')\">[我要评分]</a><BR>";

                PresentDb pd = new PresentDb();
                ObjectBlockIterator ir = pd.listPresentOfMsg(msgDb.getId());
                ScoreMgr sm = new ScoreMgr();
                UserMgr um = new UserMgr();
                while (ir.hasNext()) {
                    pd = (PresentDb)ir.next();
                    ScoreUnit su = sm.getScoreUnit(pd.getMoneyCode());
                    UserDb user = um.getUser(pd.getUserName());
                    str += "+" + pd.getScore() + "("+ su.getName(request) + ")&nbsp;&nbsp;<a target=_blank href='../userinfo.jsp?username=" + StrUtil.UrlEncode(user.getName()) + "'>" + user.getNick() + "</a>&nbsp;&nbsp;" + pd.getReason() + "<BR>";
                }
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
        return sb.isPluginBoard(PresentUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return PresentSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
