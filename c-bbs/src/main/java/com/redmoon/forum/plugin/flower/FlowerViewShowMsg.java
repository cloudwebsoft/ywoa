package com.redmoon.forum.plugin.flower;

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

public class FlowerViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public FlowerViewShowMsg(HttpServletRequest request, String boardCode, MsgDb msgDb) {
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
        String str = FlowerSkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
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
                FlowerDb fd = new FlowerDb();
                fd = fd.getFlowerDb(msgDb.getId());
                // str += "<script src='plugin/" + FlowerUnit.code + "/script.js'></script>\n";
                FlowerConfig fc = new FlowerConfig();
                ScoreMgr sm = new ScoreMgr();
                ScoreUnit su = sm.getScoreUnit(fc.getProperty("moneyCode"));
                String d = "这将消费您的" + su.getName(request) + " " + fc.getProperty("flower") + su.getDanWei();
                String e = "这将消费您的" + su.getName(request) + " " + fc.getProperty("egg") + su.getDanWei();
                str += "<a href=# onclick=\"if (confirm('您确定要送鲜花吗？" + d +"')) window.open('plugin/flower/give.jsp?msgId=" + msgDb.getId() + "&type=1', '_blank', 'toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width=480,height=320')\">送鲜花<img border=0 src='../images/flower.gif'></a>&nbsp;(" + fd.getFlowerCount() + "个)";
                str += "&nbsp;&nbsp;&nbsp;<a href=# onclick=\"if (confirm('您确定要扔鸡蛋吗？" + e + "')) window.open('plugin/flower/give.jsp?msgId=" + msgDb.getId() + "&type=0', '_blank', 'toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width=480,height=320')\">扔鸡蛋<img border=0 src='../images/egg.gif'></a>&nbsp;(" + fd.getEggCount() + "个)";
                str += "<BR>";
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
        return sb.isPluginBoard(FlowerUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return FlowerSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
