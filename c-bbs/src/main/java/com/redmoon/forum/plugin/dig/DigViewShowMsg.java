package com.redmoon.forum.plugin.dig;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import com.cloudwebsoft.framework.base.*;
import org.apache.log4j.Logger;

public class DigViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;
    Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public DigViewShowMsg(HttpServletRequest request, String boardCode,
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

    public String getNote() {
        UserMgr um = new UserMgr();
        String str = DigSkin.LoadString(request, "LABEL_MSG_OWNER") +
                     "<a href='../userinfo.jsp?username=" +
                     StrUtil.UrlEncode(msgDb.getName()) + "'>" +
                     um.getUser(msgDb.getName()).getNick() + "</a>";
        return str;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            if (msgDb.isRootMsg()) {
                str += "<div style='text-align:center'>&nbsp;&nbsp;<a target=_blank href='" + request.getContextPath() + "/forum/plugin/dig/dig.jsp?msgId=" + msgDb.getId()  + "'><img alt='" + DigSkin.LoadString(request, "dig") + "' src='" + request.getContextPath() + "/forum/plugin/dig/images/dig.gif' border=0>" + "</a>";
                str += "&nbsp;&nbsp;&nbsp;&nbsp;<a target=_blank href='" + request.getContextPath() + "/forum/plugin/dig/undig.jsp?msgId=" + msgDb.getId()  + "'><img alt='" + DigSkin.LoadString(request, "undig") + "' src='" + request.getContextPath() + "/forum/plugin/dig/images/undig.gif' border=0>" + "</a></div>";
                str += "当前积分：" + NumberUtil.round(msgDb.getScore(), 1) + "</br>";

                DigConfig dc = DigConfig.getInstance();

                int digRecordSecretLevel = dc.getIntProperty("digRecordSecretLevel");
                // 0 public 1 owner 2 manager
                boolean canShowRecord = false;
                if (digRecordSecretLevel==0)
                    canShowRecord = true;
                else if (digRecordSecretLevel==1) {
                    Privilege pvg = new Privilege();
                    if (pvg.isManager(request, msgDb.getboardcode()))
                        canShowRecord = true;
                    else if (pvg.isUserLogin(request)) {
                        if (pvg.getUser(request).equals(msgDb.getName())) {
                            canShowRecord = true;
                        }
                    }
                }
                else if (digRecordSecretLevel==2) {
                    Privilege pvg = new Privilege();
                    if (pvg.isManager(request, msgDb.getboardcode()))
                        canShowRecord = true;
                }

                if (canShowRecord) {
                    int digRecordShowInTopicCount = dc.getIntProperty(
                            "digRecordShowInTopicCount");
                    if (digRecordShowInTopicCount > 0) {
                        str += "掘客记录：<BR>";
                        DigDb digDb = new DigDb();
                        String sql = "select id from " + digDb.getTable().getName() + " where msg_id=" + msgDb.getId() + " order by dig_date desc";
                        QObjectBlockIterator qi = digDb.getQObjects(sql, ""+msgDb.getId(), 0, digRecordShowInTopicCount);
                        int k = 0;
                        ScoreMgr sm = new ScoreMgr();
                        UserMgr um = new UserMgr();
                        while (qi.hasNext()) {
                            digDb = (DigDb) qi.next();
                            ScoreUnit su = sm.getScoreUnit(digDb.getString(
                                    "score_code"));
                            UserDb user = um.getUser(digDb.getString(
                                    "user_name"));

                            str += "<a href='" + request.getContextPath() + "/userinfo.jsp?username=" + StrUtil.UrlEncode(user.getName()) + "'>" + user.getNick() + "</a>&nbsp;&nbsp;";
                            str += su.getName(request) + "&nbsp;&nbsp;消耗：" + digDb.getInt("pay");
                            str += "&nbsp;&nbsp;得分：" + NumberUtil.round(digDb.getDouble("reward"), 3);
                            str += "&nbsp;&nbsp;日期：" + ForumSkin.formatDateTime(request, digDb.getDate("dig_date")) + "<BR>";
                            k ++;
                            if (k==digRecordShowInTopicCount) {
                                break;
                            }
                        }

                        str += "[<a href='plugin/" + DigUnit.code + "/dig_list.jsp?msgId=" + msgDb.getId() + "' target='_blank'><i>查看更多挖掘记录...</i></a>]<BR>";

                    }
                }
            }
            break;
        case UIShowMsg.POS_QUICK_REPLY_NOTE:
            break;
        case UIShowMsg.POS_QUICK_REPLY_ELEMENT:
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
        return sb.isPluginBoard(DigUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return DigSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
