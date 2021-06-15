package com.redmoon.forum.plugin.sweet;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.base.IPluginViewShowMsg;
import com.redmoon.forum.plugin.base.UIShowMsg;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.Privilege;
import java.util.Iterator;
import java.util.Vector;
import com.redmoon.forum.person.UserMgr;

public class SweetViewShowMsg implements IPluginViewShowMsg {
    HttpServletRequest request;

    /**
     *
     * @param request HttpServletRequest
     * @param boardCode String
     * @param msgDb MsgDb 当在每个贴子的显示区时，msgDb为对应的贴子，当在NOTE区域时，msgDb为根贴，当在快速回复区时,msgDb也为根贴
     */
    public SweetViewShowMsg(HttpServletRequest request, String boardCode, MsgDb msgDb) {
        this.request = request;
        this.msgDb = msgDb;
        this.boardCode = boardCode;
    }

    public String getShowtopicSql(HttpServletRequest request, MsgDb rootMsgDb, String userId) {
        return "";
    }

    public String getQuickReplyFormElement() {
        String str = "";
        String username = Privilege.getUser(request);
        // 如果浏览者为楼主
        if (username.equals(msgDb.getName()))
            str = SweetViewAddReply.getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC);
        else {
            SweetUserDb su = new SweetUserDb();
            su = su.getSweetUserDb(msgDb.getId(), username);
            if (su.isLoaded()) {
                // 如果用户是申请者
                if (su.getType() == su.TYPE_APPLIER) {
                }
                else // 默认密级为楼主可见
                    str = SweetViewAddReply.getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_MSG_OWNER);
            }
            else {
                // 如果用户尚未申请
                str = SweetSkin.LoadString(request, "LABEL_NOTE_CANNOT_QUICKREPLY");
            }
        }
        return str;
    }

    public String getQucikReplyNote() {
        String str = "";
        String username = Privilege.getUser(request);
        if (username.equals(msgDb.getName()))
            str = SweetSkin.LoadString(request, "LABEL_NOTE_OWNER");
        else {
            SweetUserDb su = new SweetUserDb();
            su = su.getSweetUserDb(msgDb.getId(), username);
            if (su.isLoaded()) {
                if (su.getType() == su.TYPE_APPLIER) {
                    str = SweetSkin.LoadString(request, "LABEL_NOTE_CANNOT_QUICKREPLY");
                }
            }
            else {
                str = SweetSkin.LoadString(request, "LABEL_NOTE_CANNOT_QUICKREPLY");
            }
        }
        return str;
    }

    public String getNote() {
        SweetDb sd = new SweetDb();
        sd = sd.getSweetDb(msgDb.getRootid());

        UserMgr um = new UserMgr();
        String str = SweetSkin.LoadString(request, "LABEL_MSG_OWNER") + "<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(msgDb.getName()) + "'>" + um.getUser(msgDb.getName()).getNick() + "</a>";
        String str2 = "&nbsp;" + SweetSkin.LoadString(request, "LABEL_MSG_NOTE");
        str2 = str2.replaceFirst("\\$state", sd.getStateDesc(request));
        str += str2;
        if (sd.getState()==sd.STATE_MARRY){
            String spousestr = SweetSkin.LoadString(request, "spouse");
            spousestr.replaceFirst("\\$spouse", sd.getSpouse());
            str += "&nbsp;" + spousestr;
        }
        // 取得所有的追求者
        SweetUserDb su = new SweetUserDb();
        Vector v = su.getAllPersuater(sd.getMsgRootId());
        Iterator ir = v.iterator();
        String puser = "";
        while (ir.hasNext()) {
            SweetUserDb sud = (SweetUserDb)ir.next();
            puser += "&nbsp;<a href='../userinfo.jsp?username=" + StrUtil.UrlEncode(sud.getName()) + "'>" + um.getUser(sud.getName()).getNick() + "</a>" + "(" + sud.getTypeDesc(request, sud.getType()) + ")";
        }
        puser = SweetSkin.LoadString(request, "persuaterCount").replaceFirst("\\$usercount", "" + v.size()) + puser;
        str += "，" + puser;
        str += "&nbsp;&nbsp;[<a href='plugin/" + SweetUnit.code + "/user_m.jsp?msgRootId=" + msgDb.getRootid() + "'>" + SweetSkin.LoadString(request, "LABEL_MANAGE_USER") + "</a>]";
        return str;
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIShowMsg.POS_NOTE:
            str = getNote();
            break;
        case UIShowMsg.POS_BEFORE_MSG:
            // 如果是回贴
            if (msgDb.getReplyid()!=-1) {
                String privurl = StrUtil.getUrl(request);
                SweetMsgDb sm = new SweetMsgDb();
                sm = sm.getSweetMsgDb(msgDb.getId());
                MsgDb rootmsg = msgDb.getMsgDb(msgDb.getRootid());
                // 如果是楼主在看此贴
                if (Privilege.getUser(request).equals(rootmsg.getName())) {
                    // 如果本贴的动作为申请加入
                    if (sm.getUserAction() == sm.USER_ACTION_APPLY) {
                        // 查询用户申请是否已通过
                        SweetUserDb su = new SweetUserDb();
                        su = su.getSweetUserDb(rootmsg.getId(), msgDb.getName());
                        if (su.getType()==su.TYPE_APPLIER) {
                            str += "&nbsp;<a href='addreply.jsp?replyid=" +
                                    msgDb.getId() + "&pluginForm=" +
                                    SweetViewAddReply.FORM_ACCEPT_APPLY +
                                    "&privurl=" + privurl + "'>" +
                                    SweetSkin.LoadString(request,
                                    "BUTTON_ACCEPT") +
                                    "</a>&nbsp;";
                            str += "&nbsp;<a href='addreply.jsp?replyid=" +
                                    msgDb.getId() + "&pluginForm=" +
                                    SweetViewAddReply.FORM_DECLINE_APPLY +
                                    "&privurl=" + privurl + "'>" +
                                    SweetSkin.LoadString(request,
                                    "BUTTON_DECLINE") +
                                    "</a>";
                        }
                        else { // 如果用户已被加入
                            str += SweetSkin.LoadString(request, "LABEL_APPLY_PASS");
                        }
                    }
                    else if (sm.getUserAction() == sm.USER_ACTION_APPLY_MARRY) { // 如果动作为申请结婚
                        str += "&nbsp;<a href='addreply.jsp?replyid=" +
                                msgDb.getId() + "&pluginForm=" +
                                SweetViewAddReply.FORM_ACCEPT_APPLY_MARRY +
                                "&privurl=" + privurl + "'>" +
                                SweetSkin.LoadString(request,
                                "BUTTON_ACCEPT_MARRY") +
                                "</a>&nbsp;";
                        str += "&nbsp;<a href='addreply.jsp?replyid=" +
                                msgDb.getId() + "&pluginForm=" +
                                SweetViewAddReply.FORM_DECLINE_APPLY_MARRY +
                                "&privurl=" + privurl + "'>" +
                                SweetSkin.LoadString(request,
                                "BUTTON_DECLINE_MARRY") +
                                    "</a>";
                    }
                }
                else {
                    // 如果看此贴的不是楼主
                    if (sm.getUserAction() == sm.USER_ACTION_APPLY) {
                        str += SweetSkin.LoadString(request, "MSG_TYPE_APPLY");
                    }
                }
            }
            else {
                str += SweetSkin.LoadString(request, "MSG_TYPE_FIRST");
                String privurl = StrUtil.getUrl(request);
                // 加入申请加入按钮
                str += "&nbsp;&nbsp;<a href='addreply.jsp?replyid=" +
                        msgDb.getId() + "&privurl=" + privurl + "'>" +
                        SweetSkin.LoadString(request,
                        "BUTTON_APPLY_PERSUATE") +
                                    "</a>&nbsp;&nbsp;";
                // 加入申请结婚按钮
                /*
                str += "&nbsp;<a href='addreply.jsp?replyid=" +
                        msgDb.getId() + "&pluginForm=" +
                        SweetViewAddReply.FORM_APPLY_MARRY +
                        "&privurl=" + privurl + "'>" +
                        SweetSkin.LoadString(request,
                        "BUTTON_APPLY_MARRY") +
                                    "</a>&nbsp;";
                 */
                // 加入查看详细信息按钮
                String btn = SweetSkin.LoadString(request, "BUTTON_SHOW_DETAIL");
                btn = btn.replaceFirst("\\#userName", StrUtil.UrlEncode(msgDb.getName()));
                btn = btn.replaceFirst("\\#boardcode", StrUtil.UrlEncode(msgDb.getboardcode()));
                str += btn;
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
        SweetUnit sut = new SweetUnit();
        return sut.isPluginBoard(boardCode);
    }

    private String boardCode;
    private MsgDb msgDb;
}
