package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.UserMgr;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.plugin.base.IPluginRender;
import com.redmoon.forum.plugin.DefaultRender;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.plugin2.Plugin2Mgr;
import com.redmoon.forum.plugin2.Plugin2Unit;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.ParamUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ArticleTemplateImpl extends VarPart {
    public ArticleTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        String pageName = (String) request.getAttribute("pageName");

        MsgDb msgDb = (MsgDb) request.getAttribute("MsgDb");
        if (field.equals("title")) {
                    if (msgDb.getLayer()==3) {
                        return SkinUtil.LoadString(request, "res.label.blog.myblog", "boke_reply") + DefaultRender.RenderFullTitle(request, msgDb);
                    }else {
                        if (pageName.equals("myblog") || pageName.equals("listdayblog")) {
                            return "<a href=\"showblog.jsp?rootid=" + msgDb.getId() + "\">" + DefaultRender.RenderFullTitle(request, msgDb) + "</a><a name=\"#" + msgDb.getId() + "\"></a>";
                        }
                        else
                            return DefaultRender.RenderFullTitle(request, msgDb);
                    }
                }
                else if (field.equals("content")) {
                    if (pageName.equals("myblog") || pageName.equals("listdayblog")) {
                        return StrUtil.ubbWithoutAutoLink(request,
                                                          MsgUtil.getAbstract(request,
                                msgDb, 2000));
                    } else {
                        IPluginRender render = new com.redmoon.forum.plugin.render.
                                               RenderMM();
                        StringBuffer str = new StringBuffer();

                        int type = msgDb.getType();

                        MsgPollDb mpd = null;
                        mpd = render.RenderVote(request, msgDb);
                        if (type == 1 && mpd != null) {
                            str.append("<table width=\"100%\" border=\"1\" cellpadding=\"4\" cellspacing=\"0\" borderColor=\"#edeced\">");
                            String ctlType = "radio";
                            if (mpd.getInt("max_choice") > 1)
                                ctlType = "checkbox";
                            Vector options = mpd.getOptions(msgDb.getId());
                            int len = options.size();

                            int[] re = new int[len];
                            int[] bfb = new int[len];
                            int total = 0;
                            int k = 0;
                            for (k = 0; k < len; k++) {
                                MsgPollOptionDb opt = (MsgPollOptionDb) options.
                                                      elementAt(k);
                                re[k] = opt.getInt("vote_count");
                                total += re[k];
                            }
                            if (total != 0) {
                                for (k = 0; k < len; k++) {
                                    bfb[k] = (int) Math.round((double) re[k] / total *
                                            100);
                                }
                            }

                            str.append("<form action=\"" + request.getContextPath() +
                                    "/forum/vote.jsp?privurl=" + StrUtil.getUrl(request) +
                                    "\" name=formvote method=\"post\">");
                            str.append("<tr><td colspan=\"2\" bgcolor=\"#EBECED\"><b>");
                            str.append(SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                                        "vote"));
                            java.util.Date epDate = mpd.getDate("expire_date");
                            if (epDate != null) {
                                str.append("&nbsp;");
                                str.append(SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                        "vote_expire_date"));
                                str.append("&nbsp;" + ForumSkin.formatDate(request, epDate));
                            }
                            if (mpd.getInt("max_choice") == 1) {
                                str.append(SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                        "vote_type_single"));
                            } else {
                                str.append(SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                        "vote_type_multiple"));
                                str.append(mpd.getInt("max_choice"));
                            }
                            str.append("</b></td></tr><tr>");
                            int barId = 0;
                            String showVoteUser = ParamUtil.get(request, "showVoteUser");
                            UserMgr um = new UserMgr();
                            for (k = 0; k < len; k++) {
                                MsgPollOptionDb opt = (MsgPollOptionDb) options.
                                                      elementAt(k);
                                str.append("<td width=\"46%\">" + (k + 1) + ".");
                                str.append("<input type=\"" + ctlType +
                                        "\" name=votesel value=\"" + k + "\">");
                                str.append("&nbsp;" +
                                        StrUtil.toHtml(opt.getString("content")) +
                                        "</td>");
                                str.append("<td width=\"54%\"><img src=\"" +
                                        request.getContextPath() +
                                        "/forum/images/vote/bar" + barId +
                                        ".gif\" width=\"" + (bfb[k] - 8) +
                                        "%\" height=10>&nbsp;&nbsp;<strong>" + re[k]);
                                str.append(SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                        "vote_unit"));
                                str.append("</strong>&nbsp;" + bfb[k] + "%");
                                if (showVoteUser.equals("1")) {
                                    String[] userAry = StrUtil.split(opt.getString(
                                            "vote_user"), ",");
                                    if (userAry != null) {
                                        int userLen = userAry.length;
                                        String userNames = "&nbsp;";
                                        for (int n = 0; n < userLen; n++) {
                                            UserDb ud = um.getUser(userAry[n]);
                                            if (userNames.equals(""))
                                                userNames = ud.getNick();
                                            else
                                                userNames += ",&nbsp;" + ud.getNick();
                                        }
                                        str.append(userNames);
                                    }
                                }
                                str.append("</td></tr>");
                                barId++;
                                if (barId == 10)
                                    barId = 0;
                            }
                            str.append("<tr><td colspan=\"2\" align=\"center\"><input name=\"button\" type=\"button\" onClick=\"window.location.href='?rootid=" +
                                    msgDb.getId() + "&showVoteUser=1'\" value=\"" +
                                    SkinUtil.LoadString(request,
                                                        "res.label.forum.showtopic",
                                                        "vote_show_user") + "\">&nbsp;");
                            if (epDate != null) {
                                if (DateUtil.compare(epDate, new java.util.Date()) == 1) {
                                    str.append("<input name=\"submit\" type=\"submit\" value=\"" +
                                            SkinUtil.LoadString(request,
                                            "res.label.forum.showtopic", "vote") +
                                            "\">");
                                } else {
                                    str.append("<b>");
                                    str.append(SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                            "vote_end"));
                                    str.append("</b>");
                                }
                            } else {
                                str.append("<input type=\"submit\" value=\"" +
                                        SkinUtil.
                                        LoadString(request, "res.label.forum.showtopic",
                                                   "vote") + "\">");
                            }
                            str.append("<input type=hidden name=boardcode value=\"" +
                                    msgDb.getboardcode() + "\">");
                            str.append("<input type=hidden name=voteid value=\"" +
                                    msgDb.getId() + "\"></td>");
                            str.append("</tr></form></table>");
                        }
                        if (!msgDb.getPlugin2Code().equals("")) {
                            Plugin2Mgr p2m = new Plugin2Mgr();
                            Plugin2Unit p2u = p2m.getPlugin2Unit(msgDb.getPlugin2Code());
                            str.append(p2u.getUnit().getRender().rend(request, msgDb));
                        }
                        str.append(render.RenderContent(request, msgDb));
                        String att = render.RenderAttachment(request, msgDb);
                        if (!att.equals(""))
                            str.append("<BR>" + att);
                        return str.toString();
                    }
        }
        else if (field.equals("hit")) {
            if (msgDb.isRootMsg()) {
                String str = SkinUtil.LoadString(request,
                                                 "res.label.blog.myblog",
                                                 "view") + "：&nbsp;" + msgDb.getHit();
                return str;
            } else {
                return "";
            }
        }
        else if (field.equals("more")) {
            if (msgDb.isRootMsg()) {
                String str = SkinUtil.LoadString(request,
                                                 "res.label.blog.myblog",
                                                 "view") + "：&nbsp;" + msgDb.getHit();
                return str;
            }
            else {
                // layer=3为博主的回复，不需要再加“回复”链接
                if (msgDb.getLayer()==3) {
                    return "";
                }
                else {
                    Privilege pvg = new Privilege();
                    if (!MsgDb.isOwner(pvg.getUser(request), msgDb)) {
                        return "";
                    }
                    else {
                        return "<a href='#form' onClick='frmAnnounce.replyid.value=" +
                            msgDb.getId() + "'>" +
                            SkinUtil.LoadString(request,
                                                "res.label.blog.myblog",
                                                "reply") + "</a>";
                    }
                }
            }
        } else if (field.equals("time")) {
            return ForumSkin.formatDateTime(request, msgDb.getAddDate());
        } else if (field.equals("author")) {
            if (msgDb.getName().equals("")) {
                return SkinUtil.LoadString(request, "res.label.forum.showtopic",
                                           "anonym");
            } else {
                UserMgr um = new UserMgr();
                UserDb user = um.getUser(msgDb.getName());
                return "<a target=\"_blank\" href=\"userinfo.jsp?username=" + StrUtil.UrlEncode(user.getName()) + "\">" + user.getNick() + "</a>";
            }
        }
        else {
            props.put("htmlencode", "y");
            BeanUtil bu = new BeanUtil();
            Object obj = bu.getProperty(msgDb, field);
            return format(obj, props);
        }
    }
}
