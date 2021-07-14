package com.redmoon.forum.plugin.activity;

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

public class ActivityViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public ActivityViewShowMsg(HttpServletRequest request, String boardCode, MsgDb msgDb) {
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
        String str = ActivitySkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
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
                ActivityDb fd = new ActivityDb();
                fd = fd.getActivityDb(msgDb.getId());
                str += "<BR>组织者：" + StrUtil.toHtml(fd.getOrganizer()) + "<BR>";
                str += "联系电话：" + StrUtil.toHtml(fd.getTel()) + "<BR>";
                str += "截止时间：" + ForumSkin.formatDate(request, fd.getExpireDate()) + "<BR>";
                String uc = "" + fd.getUserCount();
                if (fd.getUserCount()==-1)
                    uc = "不限";
                str += "参与人数：" + uc + "<BR>";
                int level = fd.getUserLevel();
                UserLevelDb uld = new UserLevelDb();
                uld = uld.getUserLevelDb(level);
                str += "参与者等级要求：" + StrUtil.toHtml(uld.getDesc()) + "<BR>";
                String moneyCode = fd.getMoneyCode();
                if (!moneyCode.equals("")) {
                    ScoreMgr sm = new ScoreMgr();
                    ScoreUnit su = sm.getScoreUnit(moneyCode);
                    str += "参与得分币种：" + su.getName(request) + "<BR>";
                    str += "加入得分：" + fd.getAttendMoneyCount() + "<BR>";
                    str += "退出减分：" + fd.getExitMoneyCount() + "<BR>";
                }
                str += "<BR>&nbsp;&nbsp;&nbsp;<a href='#' onClick=\"window.open('plugin/activity/activity_do.jsp?op=attend&msgId=" + msgDb.getId() + "&type=0', '_blank', 'toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width=480,height=320')\"><img border=0 src='plugin/activity/images/apply.gif'></a>";
                str += "&nbsp;&nbsp;&nbsp;<a href='#' onClick=\"window.open('plugin/activity/activity_do.jsp?op=exit&msgId=" + msgDb.getId() + "&type=0', '_blank', 'toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width=480,height=320')\"><img border=0 src='plugin/activity/images/exit.gif'></a><BR>";
                String users = fd.getUsers().trim(); // SQLSERVER中默认为一个空格
                if (!users.equals("")) {
                    String[] ary = StrUtil.split(users, ",");
                    String s = "";
                    if (ary!=null) {
                        int len = ary.length;
                        UserMgr um = new UserMgr();
                        for (int i = 0; i < len; i++) {
                            UserDb ud = um.getUser(ary[i]);
                            if (s.equals(""))
                                s =
                                        "<a target=_blank href='../userinfo.jsp?username=" +
                                        StrUtil.UrlEncode(ud.getName()) + "'>" +
                                        StrUtil.toHtml(ud.getNick()) + "</a>";
                            else {
                                s +=
                                        "，<a target=_blank href='../userinfo.jsp?username=" +
                                        StrUtil.UrlEncode(ud.getName()) + "'>" +
                                        StrUtil.toHtml(ud.getNick()) + "</a>";
                            }
                        }
                    }
                    str += "<BR><b>参与人员：</b><BR>" + s + "<BR>";
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
        return sb.isPluginBoard(ActivityUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return ActivitySkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
