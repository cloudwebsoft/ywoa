package com.redmoon.forum.plugin.witkey;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import org.apache.log4j.Logger;
import java.sql.Timestamp;

public class WitkeyViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;
    boolean isRoot = false;
    Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public WitkeyViewShowMsg(HttpServletRequest request, String boardCode,
                             MsgDb msgDb) {
        this.request = request;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
        if (msgDb.getReplyid() == -1)
            isRoot = true;
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb,
                                  String userId) {
        String sql;
        long rootid = rootMsgDb.getId();
        String replyType = ParamUtil.get(request, "replytype");
        String userName = ParamUtil.get(request, "userName");
        // System.out.print("userName=" + userName);

        if (userId.equals(""))
            if (userName.equals(""))
                sql = "select m.id from sq_message m left join plugin_witkey_reply r on m.id=r.msg_id where m.rootid=" +
                      rootid +
                      " and m.check_status=" + MsgDb.CHECK_STATUS_PASS +
                      " and r.reply_type=" + replyType +
                      " ORDER BY m.lydate asc";
            else
                sql = "select m.id from sq_message m left join plugin_witkey_reply r on m.id=r.msg_id where m.rootid=" +
                      rootid +
                      " and m.check_status=" + MsgDb.CHECK_STATUS_PASS +
                      " and r.reply_type=" + replyType +
                      " and r.user_name=" + userName +
                      " ORDER BY m.lydate asc";
        else {
            sql = "select m.id from sq_message m left join plugin_witkey_reply r on m.id=r.msg_id where m.rootid=" +
                  rootid +
                  " and m.check_status=" + MsgDb.CHECK_STATUS_PASS +
                  " and m.name=" + StrUtil.sqlstr(userId) +
                  " and r.reply_type=" + replyType +
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
        UserMgr um = new UserMgr();
        String str = WitkeySkin.LoadString(request, "LABEL_MSG_OWNER") +
                     "<a href='../userinfo.jsp?username=" +
                     StrUtil.UrlEncode(msgDb.getName()) + "'>" +
                     um.getUser(msgDb.getName()).getNick() + "</a>";
        return str;
    }

    public String render(int position) {
        String str = "";
        WitkeyDb wd = new WitkeyDb();
        wd = wd.getWitkeyDb(msgDb.getRootid());

        switch (position) {
        case UIShowMsg.POS_NOTE:
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            if (msgDb.getReplyid() == -1) {
                str += "<br>任务号：" + msgDb.getId() + "<br>";

                str += "悬赏分类：";
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(wd.getCatalogCode());
                String pCode = lf.getParentCode();
                String plink = "";
                plink = lf.getName();
                while (!pCode.equals("root")) {
                    Leaf pleaf = lf.getLeaf(pCode);
                    // 防止当parentCode出错时，陷入死循环
                    if (pleaf == null || !pleaf.isLoaded())
                        break;
                    plink = pleaf.getName() + "-->" + plink;
                    pCode = pleaf.getParentCode();
                }
                str += plink + "<br>";

                ScoreMgr sm = new ScoreMgr();
                ScoreUnit su = sm.getScoreUnit(wd.getMoneyCode());
                str += "悬赏类型：" + su.getName() + "<br>";
                str += "悬赏金额：" + wd.getScore() + " × 80% = " +
                        (int) (wd.getScore() * 0.8) + "<br>";

                if (wd.getStatus() == 1) {
                    long msgId = wd.getMsgId();
                    WitkeyReplyDb wrd = new WitkeyReplyDb();
                    wrd = wrd.getWitkeyReplyDb(msgId);

                    UserDb ud = new UserDb();
                    ud = ud.getUser(wrd.getUserName());

                    str += "悬赏任务状态：已经中标<br>";
                    str += "中标用户：<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(wrd.getUserName()) + "'>" + ud.getNick() + "</a><br>";
                    str += "中标编号：<a href='showtopic_tree.jsp?rootid=" + msgDb.getRootid() + "&showid=" + msgId + "'>" + msgId + "</a>";
                }else{
                    long endDate = Long.parseLong(wd.getEndDate());
                    Timestamp st = new Timestamp(endDate);

                    str += "结束时间：" + DateUtil.format(st, "yyyy-MM-dd") + "<br>";

                    str +=
                            "悬赏任务状态：<span id=bidExpire name=bidExpire></span><br><br>";
                    java.util.Date curDate = new java.util.Date();
                    curDate.setTime(System.currentTimeMillis());
                    String spareTime = "";
                    int[] r = DateUtil.dateDiffDHMS(st, curDate);
                    spareTime = r[0] + "|" + r[1] + "|" + r[2] + "|" + r[3];
                    str += "<script src='plugin/" + WitkeySkin.code +
                            "/script.js'></script>\n";
                    str += "<script>\n";
                    str += "showSpareTime('" + spareTime + "');\n";
                    str += "</script>\n";
                }

                String querystring = StrUtil.getNullString(request.
                        getQueryString());
                String privurl = request.getRequestURL() + "?" +
                                 StrUtil.UrlEncode(querystring);

                com.redmoon.forum.Leaf msgLeaf = new com.redmoon.forum.Leaf();
                msgLeaf = msgLeaf.getLeaf(boardCode);

                String replypage = "addreply_new.jsp";
                if (msgLeaf.getWebeditAllowType() ==
                    com.redmoon.forum.Leaf.WEBEDIT_ALLOW_TYPE_REDMOON_FIRST) {
                    replypage = "addreply_we.jsp";
                }

                Privilege privilege = new Privilege();
                String userName = privilege.getUser(request);

                WitkeyUserDb wud = new WitkeyUserDb();
                wud = wud.getWitkeyUserDb(msgDb.getId(), userName);

                str +=
                        "<table border='0' cellpadding='0' cellspacing='0'>";
                str += "<tr>";
                if (!userName.equals(msgDb.getName())) {
                    //判断该用户是否已经报过名
                    if (wud == null || !wud.isLoaded()) {
                        str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'>";
                        str += "<a href='plugin/witkey/userinfo_add.jsp?msgId=" +
                                msgDb.getId() + "&userName=" + userName +
                                "&boardCode=" + boardCode +
                                "'>我要报名</a></td>";
                        str += "<td width='5' align='center'>&nbsp;</td>";
                    } else {
                        str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='" +
                                replypage + "?boardcode=" + boardCode +
                                "&replyid=" + msgDb.getId() +
                                "&replytype=0&privurl=" + privurl +
                                "'>我要投稿</a></td>";
                        str += "<td width='5' align='center'>&nbsp;</td>";
                    }
                }
                str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='plugin/witkey/userinfo_list.jsp?msgId=" +
                        msgDb.getId() + "&userName=" + userName +
                        "'>查看报名</a></td>";
                str += "<td width='5' align='center'>&nbsp;</td>";
                str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='showtopic.jsp?rootid=" +
                        msgDb.getRootid() + "&pluginCode=" + WitkeyUnit.code +
                        "&replytype=" + WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION +
                        "'>查看交稿</a></td>";
                str += "<td width='5' align='center'>&nbsp;</td>";
                if (!userName.equals(msgDb.getName())) {
                    str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='" +
                            replypage + "?boardcode=" + boardCode + "&replyid=" +
                            msgDb.getId() + "&replytype=1&privurl=" + privurl +
                            "'>我要提问</a></td>";
                    str += "<td width='5' align='center'>&nbsp;</td>";
                }
                str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='showtopic.jsp?rootid=" +
                        msgDb.getRootid() + "&pluginCode=" + WitkeyUnit.code +
                        "&replytype=" + WitkeyReplyDb.REPLY_TYPE_COMMUNICATION +
                        "'>任务交流</a></td>";
                str += "<td width='5' align='center'>&nbsp;</td>";
                str += "<td width='65' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='plugin/witkey/recommend_task.jsp'>任务推荐</a></td>";
                str += "<td width='5' align='center'>&nbsp;</td>";
                str += "<td width='65' height='22' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='plugin/witkey/correlation_task.jsp?catalogCode=" +
                        wd.getCatalogCode() + "'>相关任务</a></td>";
                str += "<td width='5' align='center'>&nbsp;</td>";
                str += "</tr>";
                str += "</table>";
                str += "<br>";
            }else{
                Privilege privilege = new Privilege();
                String userName = privilege.getUser(request);

                long msgId = msgDb.getRootid();
                MsgMgr mm = new MsgMgr();
                MsgDb md = mm.getMsgDb(msgId);

                WitkeyReplyDb wrd = new WitkeyReplyDb();
                wrd = wrd.getWitkeyReplyDb(msgDb.getId());

                str += "<br>";
                str += "<br>";
                str += "<table border='0' align='left' cellpadding='0' cellspacing='0'>";
                str += "<tr>";

                if(wrd.getReplyType() == WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION){
                    // if (!userName.equals(msgDb.getName())) {
                        str += "<td width='65' height='22' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a target=_blank href='plugin/witkey/evaluation.jsp?msgId=" +
                                msgDb.getId() + "&boardCode=" + boardCode + "'>点评</a></td>";
                        str += "<td width='5' align='center'>&nbsp;</td>";
                    // }
                }
                if (userName.equals(md.getName()) && wrd.getReplyType() == WitkeyReplyDb.REPLY_TYPE_CONTRIBUTION && wd.getMsgId() == -1 ) {
                    str += "<td width='65' height='22' align='center' style='background-image:url(plugin/debate/images/btn_bg.gif)'><a href='javascript:if (confirm(\"您确定要让该用户中标么？\")) window.location.href=\"plugin/witkey/witkey_do.jsp?msgId=" +
                            msgDb.getId() + "&op=check\"'>任务中标</a></td>";
                    str += "<td width='5' align='center'>&nbsp;</td>";
                }
                str += "</tr>";
                str += "</table>";
                str += "<br>";
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
        return sb.isPluginBoard(WitkeyUnit.code, boardCode);
    }

    public String LoadString(String key) {
        return WitkeySkin.LoadString(request, key);
    }

    private String boardCode;
    private MsgDb msgDb;
}
