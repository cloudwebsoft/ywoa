package com.redmoon.forum.plugin.reward;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.plugin.base.UIShowMsg;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.person.UserMgr;

public class RewardViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public RewardViewShowMsg(HttpServletRequest request, String boardCode, MsgDb msgDb) {
        this.request = request;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
        if (msgDb.getReplyid()==-1)
            isRoot = true;
    }

    public String getQuickReplyFormElement() {
        String str = "";
        return "";
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb, String userId) {
        return "";
    }

    public String getQucikReplyNote() {
        String str = "";
        return str;
    }

    public String getNote() {
        UserMgr um = new UserMgr();
        String str = RewardSkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
        return str;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            str = getNote();
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            // 如果是根贴
            String skinCode = "default";
            String skinPath = RewardSkin.getSkinPath(skinCode);

            RewardDb rd = new RewardDb();
            rd = rd.getRewardDb(msgDb.getId());

            RewardDb rootRd = null;
            MsgDb rootMsgDb = null;
            if (!isRoot) {
                rootRd = rd.getRewardDb(msgDb.getRootid());
                rootMsgDb = msgDb.getMsgDb(msgDb.getRootid());
            }
            else {
                rootRd = rd;
                rootMsgDb = msgDb;
            }
            String moneyCode = rootRd.getMoneyCode();
            ScoreMgr sm = new ScoreMgr();
            ScoreUnit su = sm.getScoreUnit(moneyCode);
            String moneyName = "";
            if (su!=null)
               moneyName = su.getName(request);

            if (isRoot) {
                str += "<b>";
                str += moneyName + "  " + RewardSkin.LoadString(request, "score") + rd.getScore();
                str += "  " + RewardSkin.LoadString(request, "score_given") + rd.getScoreGiven() + "  " + RewardSkin.LoadString(request, "score_sy") + (rd.getScore() - rd.getScoreGiven());
                if (rd.isEnd())
                    str += "  " + RewardSkin.LoadString(request, "is_end");
                else {
                    // str += "<script src='plugin/" + RewardUnit.code + "/script.js'></script>\n";
                }
                str += "</b>";
            }
            else {
                if (rd.isLoaded()) {
                    str += "<font color=red>" + moneyName + " " +
                            RewardSkin.LoadString(request, "user_score") +
                            rd.getScore() + "</font>";
                } else {
                    if (!rootRd.isEnd()) { // 未结贴，楼主送分
                        RewardPrivilege privilege = new RewardPrivilege();
                        if (privilege.isOwner(request, msgDb.getRootid())) {
                            // 浏览者不是楼主
                            if (!Privilege.getUser(request).equals(msgDb.
                                    getName())) {
                                // 如果分数还没有送出过
                                str += "   <input name=score" + rd.getMsgId() +
                                        " size=5>    <input type=button onClick=\"window.open('plugin/" +
                                        RewardUnit.code +
                                        "/givescore.jsp?msgId=" +
                                        msgDb.getId() + "&score=' + score" +
                                        msgDb.getId() + ".value)\" value=" +
                                        RewardSkin.LoadString(request,
                                        "give_score") +
                                        ">";
                            }
                        }
                    }
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
        RewardUnit ru = new RewardUnit();
        return ru.isPluginBoard(boardCode);
    }

    public String LoadString(String key) {
        return RewardSkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
