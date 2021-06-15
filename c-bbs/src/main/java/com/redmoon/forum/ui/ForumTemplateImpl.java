package com.redmoon.forum.ui;

import com.cloudwebsoft.framework.template.VarPart;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import com.redmoon.forum.MsgDb;
import java.util.Iterator;
import java.util.Vector;
import com.redmoon.forum.ForumDb;
import cn.js.fan.web.Global;
import cn.js.fan.util.DateUtil;
import cn.js.fan.module.cms.ui.DesktopItemDb;
import java.util.HashMap;
import com.redmoon.forum.MsgMgr;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.ThreadBlockIterator;
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.plugin.DefaultRender;
import com.redmoon.forum.SQLBuilder;
import cn.js.fan.base.ObjectBlockIterator;
import com.redmoon.forum.person.UserDb;

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
public class ForumTemplateImpl extends VarPart {
    public ForumTemplateImpl() {
    }

    public String parseBoardCodeFromProps(HttpServletRequest request) {
        String boardCode = (String) props.get("boardcode");
        // System.out.println(getClass() + " boardCode=" + boardCode);
        if (boardCode==null) {
             if (request!=null)
                 boardCode = ParamUtil.get(request, "boardCode");
             else
                 boardCode = "";
        }
        return boardCode;
    }

    public String renderListTopic(HttpServletRequest request, List params) {
        String boardcode = parseBoardCodeFromProps(request);

        String more = StrUtil.getNullStr((String)props.get("more"));
        if (more.equalsIgnoreCase("true") || more.equalsIgnoreCase("yes") || more.equalsIgnoreCase("y")) {
            return Global.getRootPath() + "/forum/" + ForumPage.getListTopicPage(request, boardcode);
        }

        boolean isDateShow = false;
        String dateFormat = "";
        String dt = (String) props.get("date");
        if (dt != null) {
            isDateShow = dt.equalsIgnoreCase("true") || dt.equalsIgnoreCase("yes") || dt.equalsIgnoreCase("y");
            dateFormat = (String) props.get("dateFormat");
            if (dateFormat == null) {
                dateFormat = "yy-MM-dd";
            }
        }

        String target = StrUtil.getNullStr((String)props.get("target"));
        if (!target.equals(""))
            target = "target=\"" + target + "\"";

        String sql = SQLBuilder.getListTopicSql(boardcode);
        MsgDb md = new MsgDb();

        StringBuffer str = new StringBuffer();
        str.append("<ul>");
        int count = StrUtil.toInt((String) props.get("row"), 10);
        ThreadBlockIterator irmsg = md.getThreads(sql, boardcode, 0,
                                                  count);
        while (irmsg.hasNext()) {
            md = (MsgDb) irmsg.next();
            str.append("<li><a href='" + Global.getRootPath() + "/forum/" + ForumPage.getShowTopicPage(request, md.getId()) + "' " + target + " title='" +
                    StrUtil.toHtml(md.getTitle()) + "'>");
            if (!md.getColor().equals(""))
                str.append("<font color='" + md.getColor() + "'>");
            if (md.isBold())
                str.append("<b>");
            str.append(format(DefaultRender.RenderFullTitle(request, md), props));
            if (md.isBold())
                str.append("</b>");
            if (!md.getColor().equals(""))
                str.append("</font>");
            if (isDateShow) {
                str.append(" [" +
                        DateUtil.format(md.getAddDate(), dateFormat) +
                        "]");
            }
            str.append("</a></li>");
        }

        str.append("</ul>");
        return str.toString();
    }

    public String renderNotice(HttpServletRequest request, List params) {
        StringBuffer str = new StringBuffer();
        ForumDb fd = ForumDb.getInstance();
        Vector v = fd.getAllNotice();
        Iterator ir = v.iterator();
        String rootPath = Global.getRootPath();
        while (ir.hasNext()) {
            MsgDb md = (MsgDb) ir.next();
            str.append("<a href='" + rootPath +
                    "/forum/showtopic.jsp?rootid=" +
                    md.getId() + "' target='_blank'>");
            if (!md.getColor().equals(""))
                str.append("<font color='" + md.getColor() + "'>");
            if (md.isBold())
                str.append("<b>");
            str.append(format(md.getTitle(), props));
            if (md.isBold())
                str.append("</b>");
            if (!md.getColor().equals(""))
                str.append("</font>");
            str.append(" [" + DateUtil.format(md.getAddDate(), "yyyy-MM-dd") +
                    "]</a>&nbsp;&nbsp;&nbsp;");
        }
        return str.toString();
    }

    public String renderListUser(HttpServletRequest request, List params) {
        String type = StrUtil.getNullStr((String)props.get("type"));
        String orderBy;
        if (type.equals("online"))
            orderBy = "online_time";
        else {
            orderBy = "RegDate";
        }

        String more = StrUtil.getNullStr((String)props.get("more"));
        if (more.equalsIgnoreCase("true") || more.equalsIgnoreCase("yes") || more.equalsIgnoreCase("y")) {
            if (type.equals("online")) {
                return Global.getRootPath() + "/forum/stats.jsp?type=online";
            }
            else {
                return Global.getRootPath() + "/listmember.jsp";
            }
        }

        UserDb ud = new UserDb();
        int count = StrUtil.toInt((String)props.get("row"), 10);
        // String sort = StrUtil.getNullStr((String)props.get("sort"));
        // if (sort.equals(""))
        //    sort = "desc";

        String target = StrUtil.getNullStr((String)props.get("target"));
        if (!target.equals(""))
            target = "target=\"" + target + "\"";

        StringBuffer str = new StringBuffer();
        str.append("<ul>");

        ObjectBlockIterator oi = ud.listUserRank(orderBy, count);
        String rootPath = Global.getRootPath();
        while (oi.hasNext()) {
            ud = (UserDb) oi.next();
            str.append("<li>");
            str.append("<a " + target + " href='" + rootPath + "/userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>");
            str.append("</li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    public String renderHot(HttpServletRequest request, List params) {
        StringBuffer str = new StringBuffer();
        MsgMgr mm = new MsgMgr();
        MsgDb md = null;
        Home home = Home.getInstance();
        int[] v = home.getHotIds();
        int hotlen = v.length;
        if (hotlen != 0) {
            boolean isDateShow = false;
            String dateFormat = "";
            String dt = (String) props.get("date");
            if (dt != null) {
                isDateShow = dt.equals("true") || dt.equals("yes") ||
                             dt.equals("y");
                dateFormat = (String) props.get("dateFormat");
                if (dateFormat == null) {
                    dateFormat = "yy-MM-dd";
                }
            }

            String rootPath = Global.getRootPath();

            str.append("<ul>");

            for (int k = 0; k < hotlen; k++) {
                md = mm.getMsgDb(v[k]);
                if (md.isLoaded()) {
                    str.append("<li><a href='" + rootPath +
                            "/forum/showtopic.jsp?rootid=" + md.getId() +
                            "' title='" + md.getTitle() + "'>");
                    if (!md.getColor().equals(""))
                        str.append("<font color='" + md.getColor() + "'>");
                    if (md.isBold())
                        str.append("<b>");
                    str.append(format(md.getTitle(), props));
                    if (md.isBold())
                        str.append("</b>");
                    if (!md.getColor().equals(""))
                        str.append("</font>");
                    str.append("</a>&nbsp;");
                    if (isDateShow) {
                        str.append(" [" +
                                DateUtil.format(md.getAddDate(), dateFormat) +
                                "]");
                    }
                    str.append("</li>");
                }
            }
        }
        str.append("</ul>");
        return str.toString();
    }

    public String toString(HttpServletRequest request, List params) {
        if (field.equalsIgnoreCase("notice")) {
            return renderNotice(request, params);
        } else if (field.equalsIgnoreCase("hot")) {
            return renderHot(request, params);
        }
        else if (field.equalsIgnoreCase("listTopic")) {
            return renderListTopic(request, params);
        }
        else if (field.equalsIgnoreCase("listUser")) {
            return renderListUser(request, params);
        }
        else
            return "";
    }
}
