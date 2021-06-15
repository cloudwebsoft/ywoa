package com.redmoon.forum.plugin.witkey;

import javax.servlet.http.*;
import cn.js.fan.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import org.apache.log4j.*;

public class WitkeyRender extends DefaultRender {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private boolean showAttachment = true;

    public WitkeyRender() {
    }

    public MsgPollDb RenderVote(HttpServletRequest request, MsgDb md) {
        UserDb user = new UserDb();
        user = user.getUser(md.getName());
        if (user.isValid()) {
            MsgPollDb mpd = new MsgPollDb();
            return (MsgPollDb) mpd.getQObjectDb(new Long(md.getId()));
        } else
            return null;
    }

    /*
        public String RenderVote(HttpServletRequest request, MsgDb md) {
            String str = "";
            String user = Privilege.getUser(request);
            // 如果为主题贴或浏览者本人所发的贴子，则可见
            if (md.getReplyid()==-1 || md.getName().equals(user))
                return md.getVoteOption();
            // 检查看贴人user是否为楼主
            SweetDb sdroot = new SweetDb();
            sdroot = sdroot.getSweetDb(md.getRootid());
            if (user.equals(sdroot.getName()))
                return md.getVoteOption();

            SweetMsgDb sm = new SweetMsgDb();
            sm = sm.getSweetMsgDb(md.getId());
            switch(sm.getSecretLevel()) {
                // 公众可见
                case SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC:
                    str = md.getVoteOption();
                    break;
                case SweetMsgDb.SECRET_LEVEL_MSG_USER:
                    // 检查用户是否在本贴内
                    SweetUserDb su = new SweetUserDb();
                    su = su.getSweetUserDb(md.getRootid(), user);
                    if (su.isLoaded())
                        str = md.getVoteOption();
                    else
                        str = null;

                    break;
                case SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED:
                    long replyid = md.getReplyid();
                    // 取得被回复贴
                    MsgDb msgDb = md.getMsgDb(replyid);
                    String reuser = msgDb.getName();
                    if (user.equals(reuser))
                        str = md.getVoteOption();
                    else // 不是被回复者
                        str = null; //SweetSkin.LoadString(request,
                                    //"MSG_CONTENT_NOTDISPLAY");
                    break;
                case SweetMsgDb.SECRET_LEVEL_MSG_OWNER:
                    str = null; //SweetSkin.LoadString(request,
                                    //"MSG_CONTENT_NOTDISPLAY");
                    break;
                default:
                    str = null;
                    break;
            }
            return str;
        }
     */
    /**
     * 根据规则判断是否显示content
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return String
     */
    public String RenderContent(HttpServletRequest request, MsgDb md) {
        String boardCode = md.getboardcode();
        UserDb userDb = new UserDb();
        userDb = userDb.getUser(md.getName());
        if (!userDb.isValid()) {
            showAttachment = false;
            return
                    "=======================\n\n该用户的所有言论均已被屏蔽！\n\n=======================";
        }
        String str = "";
        String user = Privilege.getUser(request);
        DefaultRender dr = new DefaultRender();

        // 如果为主题贴或浏览者本人所发的贴子，则可见
        if (md.getReplyid() == -1 || md.getName().equals(user)) {
            return dr.doRendContent(request, md);
        }
        // 检查看贴人user是否为楼主
        if (user.equals(md.getName())) {
            return dr.doRendContent(request, md);
        }
        // 取得贴子秘级
        WitkeyReplyDb wrd = new WitkeyReplyDb();
        wrd = wrd.getWitkeyReplyDb(md.getId());
        switch (wrd.getViewType()) {
        case WitkeyReplyDb.SECRET_LEVEL_FORUM_PUBLIC:
            str = dr.doRendContent(request, md);
            break;
        case WitkeyReplyDb.SECRET_LEVEL_MSG_USER:
            // 检查用户是否在本贴内
            WitkeyUserDb wud = new WitkeyUserDb();
            wud = wud.getWitkeyUserDb(md.getRootid(), user);
            if (wud.isLoaded()) {
                str = dr.doRendContent(request, md);
            } else
                str = WitkeySkin.LoadString(request,
                                           "MSG_CONTENT_NOTDISPLAY");
            break;
        case WitkeyReplyDb.SECRET_LEVEL_MSG_USER_REPLIED: // 被回复者可见
            long replyid = md.getReplyid();

            // logger.info("id=" + md.getId() + " replyid=" + replyid);
            // 取得被回复贴
            MsgDb msgDb = md.getMsgDb(replyid);
            String reuser = msgDb.getName();

            // logger.info("user=" + user + " reuser=" + reuser);
            if (user.equals(reuser))
                str = dr.doRendContent(request, md);
            else
                str = WitkeySkin.LoadString(request,
                                           "MSG_CONTENT_NOTDISPLAY");
            break;
        case WitkeyReplyDb.SECRET_LEVEL_MSG_OWNER:
            MsgMgr mm = new MsgMgr();
            msgDb = mm.getMsgDb(md.getRootid());
            String owneruser = msgDb.getName();
            if (user.equals(owneruser))
                str = dr.doRendContent(request, md);
            else
                str = WitkeySkin.LoadString(request,
                                            "MSG_CONTENT_NOTDISPLAY");
            break;
        default:
            BoardManagerDb bmd = new BoardManagerDb();
            bmd = bmd.getBoardManagerDb(boardCode, user);
            if (bmd != null && bmd.isLoaded())
                str = dr.doRendContent(request, md);
            else
                str = WitkeySkin.LoadString(request,
                                            "MSG_CONTENT_NOTDISPLAY");
            break;
        }
        return str;
    }

}
