package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.module.nav.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.*;
import com.redmoon.blog.photo.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.group.*;

/**
 * <p>Title: 博客总首页模板的元素显示</p>
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
public class BlogTemplateImpl extends VarPart {
    public BlogTemplateImpl() {
    }

    public String renderStar(HttpServletRequest request, List params) {
        BlogDb bd = BlogDb.getInstance();
        String star = bd.getStar();
        if (star.equals(""))
            return "";
        String[] nicks = StrUtil.split(star, ",");
        int len = nicks.length;
        UserConfigDb ucd = new UserConfigDb();
        UserDb user = new UserDb();

        StringBuffer str = new StringBuffer();
        for (int i = 0; i < len; i++) {
            user = user.getUserDbByNick(nicks[i]);
            if (user == null) {
                user = new UserDb();
                continue;
            }
            ucd = ucd.getUserConfigDbByUserName(user.getName());
            if (ucd != null) {
                str.append("<table width='100%' height='64'><tr>");
                str.append("<td width='64'>");
                str.append("<a target=_blank href='");
                str.append(Global.getRootPath());
                str.append("/blog/myblog.jsp?blogId=");
                str.append(ucd.getId());
                str.append("'>");
                if (user.getMyface().equals("")) {
                    str.append("<img border=0 style='border:1px solid #cccccc; padding:2px' src='");
                    str.append(Global.getRootPath());
                    str.append("/forum/images/face/");
                    str.append(user.getRealPic());
                    str.append("' width=96 height=96 />");
                } else {
                    str.append("<img border=0 style='border:1px solid #cccccc; padding:2px' src='");
                    str.append(user.getMyfaceUrl(request));
                    str.append("' width=96 height=96 />");
                }
                str.append("</a>");
                str.append("</td>");
                str.append("<td>");
                str.append("<table>");
                str.append("<tr><td><a href='");
                str.append(Global.getRootPath());
                str.append("/userinfo.jsp?username=");
                str.append(user.getName());
                str.append("' class='person1'>");
                str.append(user.getNick());
                str.append("</a></td></tr>");
                str.append("<tr><td><a href='");
                str.append(Global.getRootPath());
                str.append("/userinfo.jsp?username=");
                str.append(StrUtil.UrlEncode(user.getName()));
                str.append("'>");
                str.append(user.getNick());
                str.append("</a></td></tr>");
                str.append("<tr><td><a href='");
                str.append(Global.getRootPath());
                str.append("/blog/myblog.jsp?blogId=");
                str.append(ucd.getId());
                str.append("' class='person1'>");
                str.append(ucd.getSubtitle());
                str.append("</a></td></tr>");
                str.append("<tr><td><img src='");
                str.append(Global.getRootPath());
                str.append("/blog/template/images/addfriend.gif'>&nbsp;<a href='");
                str.append(Global.getRootPath());
                str.append("/forum/addfriend.jsp?friend=");
                str.append(StrUtil.UrlEncode(user.getName()));
                str.append("'>加为好友</a>&nbsp;<img src='");
                str.append(Global.getRootPath());
                str.append("/blog/template/images/sendmsg.gif'>&nbsp;<a href='#' onClick=\"msgOpenWin('" + Global.getRootPath() + "/message/message.jsp',320,260)\">短消息</a></td></tr>");
                str.append("</table>");
                str.append("</td></tr></table>");
            } else {
                ucd = new UserConfigDb();
            }
        }
        return str.toString();
    }

    public String toString(HttpServletRequest request, List params) {
        if (field.equals("star")) {
            return renderStar(request, params);
        }
        else if (field.equalsIgnoreCase("photoDir")) {
            return renderPhotoDir(request, params);
        }
        else if (field.equalsIgnoreCase("flashImage")) {
            return renderFlashImage(request, params);
        }
        else if (field.equalsIgnoreCase("focus")) {
            return renderFocus(request, params);
        }
        else if (field.equalsIgnoreCase("notice")) {
            return renderNotice(request, params);
        }
        else if (field.equalsIgnoreCase("vScroller")) {
            return renderVerticalScroller(request, params);
        }
        else if (field.equalsIgnoreCase("postRank")) {
            return renderPostRank(request, params);
        }
        else if (field.equalsIgnoreCase("replyRank")) {
            return renderReplyRank(request, params);
        } else if (field.equalsIgnoreCase("newPhoto")) {
            return renderNewPhoto(request, params);
        }
        else if (field.equalsIgnoreCase("listPhoto")) {
            return renderListPhoto(request, params);
        }
        else if (field.equalsIgnoreCase("recommandBlog")) {
            return renderRecommandBlog(request, params);
        }
        else if (field.equalsIgnoreCase("newArticle")) {
            return renderNewArticle(request, params);
        }
        else if (field.equalsIgnoreCase("newBlog")) {
            return renderNewBlog(request, params);
        }
        else if (field.equalsIgnoreCase("newUpdateBlog")) {
            return renderNewUpdateBlog(request, params);
        }
        else if (field.equalsIgnoreCase("listArticle")) {
            return renderListArticle(request, params);
        }
        else if (field.equalsIgnoreCase("ad")) {
            String strId = (String)props.get("id");
            Home home = Home.getInstance();

            return home.getProperty("ads", "id", strId);
        }
        else if (field.equalsIgnoreCase("group")) {
            // 朋友圈
            return renderListGroup(request, params);
        }
        else if (field.equalsIgnoreCase("nav")) {
            return renderNav(request, params);
        }
        else if (field.equalsIgnoreCase("hotArticle")) {
            return renderHotArticle(request, params);
        }
        return "";
    }

    /**
     * 显示导航条
     * @param request HttpServletRequest
     * @param params List
     * @return String
     */
    public String renderNav(HttpServletRequest request,
                            List params) {
        NavigationMgr nmr = new NavigationMgr();
        String type = Navigation.TYPE_BLOG;

        Vector v = nmr.getAllNav(type);
        Iterator ir = v.iterator();
        StringBuffer str = new StringBuffer();
        str.append("<ul>");
        while (ir.hasNext()) {
            Navigation nav = (Navigation) ir.next();
            String target = "";
            if (!nav.getTarget().equals(""))
                target = " target=" + nav.getTarget();
            str.append("<li>");

            String color = StrUtil.getNullString(nav.getColor());
            String name = "";
            if (color.equals(""))
                name = nav.getName();
            else
                name = "<font color='" + color + "'>" + nav.getName() +
                       "</font>";

            str.append("<a href='" + nav.getLink() +
                       "'" + target + " >" +
                       name + "</a>");
            str.append("</li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    /**
     * 显示圈子
     * @param request HttpServletRequest
     * @param params List
     * @return String
     */
    public String renderListGroup(HttpServletRequest request,
                                          List params) {
        int row = StrUtil.toInt((String)props.get("row"), 10);

        StringBuffer str = new StringBuffer();
        str.append("<ul>");

        String sql = GroupSQLBuilder.getListGroupSql(request);
        GroupDb gd = new GroupDb();
        QObjectBlockIterator qi = gd.getQObjects(sql, "", 0, row);

        while (qi.hasNext()) {
            gd = (GroupDb)qi.next();
            str.append("<li>");
            String clrName = StrUtil.toHtml(gd.getString("name"));
            String color = StrUtil.getNullStr(gd.getString("color"));
            if (!color.equals(""))
                clrName = "<font color=" + color + ">" + clrName + "</font>";
            if (gd.getInt("is_bold") == 1)
                clrName = "<strong>" + clrName + "</strong>";

            str.append("<a href=\"");
            str.append(Global.getRootPath());
            str.append("/forum/plugin/group/group.jsp?id=");
            str.append(gd.getLong("id"));
            str.append("\">");
            str.append(clrName);
            str.append("</a>");
            str.append("</li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    /**
     * 显示某类博客的文章
     * @param request HttpServletRequest
     * @param params List
     * @return String
     */
    public String renderListArticle(HttpServletRequest request,
                                          List params) {
        int row = StrUtil.toInt((String)props.get("row"), 10);

        String kind = (String)props.get("dircode");

        BlogDb bd = new BlogDb();
        MsgMgr mm = new MsgMgr();
        StringBuffer str = new StringBuffer();

        str.append("<ul>");
        long[] msgIds = bd.getBlogMsgsOfKind(row, kind);
        int msgIdsLen = msgIds.length;
        MsgDb msd;
        for (int i = 0; i < msgIdsLen; i++) {
            msd = mm.getMsgDb(msgIds[i]);
            str.append("<li>");
            str.append(formatDate(msd.getAddDate(), props));
            str.append("<a href='showblog.jsp?rootid=");
            str.append(msd.getId());
            str.append("' target='_blank'>");
            str.append(format(msd.getTitle(),props));
            str.append("</a></li>");
        }
        str.append("</ul>");
        return str.toString();

    }

    public String renderRecommandBlog(HttpServletRequest request,
                                       List params) {
        int start = StrUtil.toInt((String)props.get("start"), 0);
        int end = StrUtil.toInt((String)props.get("end"), 10);
        StringBuffer str = new StringBuffer();
        BlogDb bd = BlogDb.getInstance();
        Vector rv = bd.getAllRecommandBlogs();
        int nsize = rv.size();
        if (nsize == 0)
            ;
        else {
            if (start>=rv.size())
                return "";
            if (end>=rv.size())
                end = rv.size();
            str.append("<ul>");
            for (int i=start; i<end; i++) {
                UserConfigDb ucd = (UserConfigDb) rv.elementAt(i);
                str.append("<li><a href='myblog.jsp?blogId=");
                str.append(ucd.getId());
                str.append("' target=_blank>");
                str.append(format(ucd.getTitle(), props));
                str.append("</a></li>");
            }
            str.append("</ul>");
        }
        return str.toString();
    }

    public String renderNewArticle(HttpServletRequest request,
                                      List params) {
        StringBuffer str = new StringBuffer();
        int row = StrUtil.toInt((String)props.get("row"), 10);
        str.append("<ul>");
        MsgDb msd = null;
        MsgMgr mm = new MsgMgr();
        long[] newMsgs = null;
        BlogDb bd = new BlogDb();
        bd = bd.getBlogDb();
        newMsgs = bd.getNewBlogMsgs(row);
        if (newMsgs == null)
            return "";
        int newMsgsLen = newMsgs.length;
        if (newMsgsLen > row)
            newMsgsLen = row;
        boolean isDateShow = false;
        String dateFormat = "";
        String dt = (String) props.get("date");
        if (dt != null) {
            isDateShow = dt.equals("true") || dt.equals("yes");
            dateFormat = (String) props.get("dateFormat");
            if (dateFormat == null) {
                dateFormat = "yy-MM-dd";
            }
        }
        for (int i = 0; i < newMsgsLen; i++) {
            msd = mm.getMsgDb((int) newMsgs[i]);
            str.append("<li><a href='" + Global.getRootPath() + "/blog/showblog.jsp?rootid=");
            str.append(msd.getId());
            str.append("' target='_blank'>");
            str.append(format(DefaultRender.RenderFullTitle(request, msd), props));
            if (isDateShow) {
                str.append("&nbsp;[");
                str.append(DateUtil.format(msd.getAddDate(), dateFormat));
                str.append("]");
            }
            str.append("</a></li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    /**
     * 根据访问计数取得数天内最热的文章
     * @param request HttpServletRequest
     * @param params List
     * @return String
     */
    public String renderHotArticle(HttpServletRequest request,
                                      List params) {
        StringBuffer str = new StringBuffer();
        int row = StrUtil.toInt((String)props.get("row"), 10);
        int day = StrUtil.toInt((String)props.get("day"), 15);
        str.append("<ul>");
        MsgDb msd = null;
        MsgMgr mm = new MsgMgr();
        long[] hotMsgs = null;
        BlogDb bd = new BlogDb();
        bd = bd.getBlogDb();
        hotMsgs = bd.getHotBlogMsgs(row, day);
        if (hotMsgs == null)
            return "";
        int hotMsgsLen = hotMsgs.length;
        if (hotMsgsLen > row)
            hotMsgsLen = row;
        boolean isDateShow = false;
        String dateFormat = "";
        String dt = (String) props.get("date");
        if (dt != null) {
            isDateShow = dt.equals("true") || dt.equals("yes");
            dateFormat = (String) props.get("dateFormat");
            if (dateFormat == null) {
                dateFormat = "yy-MM-dd";
            }
        }
        for (int i = 0; i < hotMsgsLen; i++) {
            msd = mm.getMsgDb((int) hotMsgs[i]);
            str.append("<li><a href='" + Global.getRootPath() + "/blog/showblog.jsp?rootid=");
            str.append(msd.getId());
            str.append("' target='_blank'>");
            str.append(format(DefaultRender.RenderFullTitle(request, msd), props));
            if (isDateShow) {
                str.append("&nbsp;[");
                str.append(DateUtil.format(msd.getAddDate(), dateFormat));
                str.append("]");
            }
            str.append("</a></li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    public String renderNewBlog(HttpServletRequest request,
                                    List params) {
        StringBuffer str = new StringBuffer();
        int row = StrUtil.toInt((String)props.get("row"), 10);
        str.append("<ul>");
        BlogDb bd = new BlogDb();
        long[] newBlogs = bd.getNewBlogs(row);

        if (newBlogs != null) {
            UserConfigDb ucd = new UserConfigDb();
            int newBlogsLen = newBlogs.length;
            for (int i = 0; i < newBlogsLen; i++) {
                ucd = ucd.getUserConfigDb(newBlogs[i]);
                str.append("<li><a href='myblog.jsp?blogId=");
                str.append(ucd.getId());
                str.append("' title='");
                str.append(StrUtil.toHtml(ucd.getTitle()));
                str.append("'>");
                str.append(format(ucd.getTitle(), props));
                str.append("</a></li>");
            }
        }
        str.append("</ul>");
        return str.toString();
    }

    public String renderNewUpdateBlog(HttpServletRequest request,
                                       List params) {
        StringBuffer str = new StringBuffer();
        str.append("<ul>");
        BlogDb bd = new BlogDb();
        int row = StrUtil.toInt((String)props.get("row"), 10);

        long[] newUpdateBlogs = bd.getNewUpdateBlogs(row);
        if (newUpdateBlogs != null) {
            UserConfigDb ucd = new UserConfigDb();
            int newBlogsLen = newUpdateBlogs.length;
            for (int i = 0; i < newBlogsLen; i++) {
                ucd = ucd.getUserConfigDb(newUpdateBlogs[i]);
                str.append("<li><a href='myblog.jsp?blogId=");
                str.append(ucd.getId());
                str.append("' title='");
                str.append(StrUtil.toHtml(ucd.getTitle()));
                str.append("'>");
                str.append(format(ucd.getTitle(), props));
                str.append("</a></li>");
            }
        }
        str.append("</ul>");
        return str.toString();
    }

    public String renderVerticalScroller(HttpServletRequest request,
                                              List params) {
        StringBuffer str = new StringBuffer("<script>");
        Home home = Home.getInstance();
        int[] v = home.getVerticalScrollerIds();
        int hotlen = v.length;
        if (hotlen == 0)
            ;
        else {
            MsgDb md = null;
            MsgMgr mm = new MsgMgr();
            for (int k = 0; k < hotlen; k++) {
                md = mm.getMsgDb(v[k]);
                if (md.isLoaded()) {
                    // str += "myScroller1.addItem('中国<a href='http://dynamicdrive.com'>Dynamic Drive</a>, the net\'s #1 DHTML site!</b>');\n";
                    String color = StrUtil.getNullString(md.getColor());
                    if (color.equals("")) {
                        str.append("myScroller1.addItem(\"<a href='");
                        str.append(Global.getRootPath());
                        str.append("/blog/showblog.jsp?rootid=");
                        str.append(md.getId());
                        str.append("' target='_blank'>");
                        str.append(format(md.getTitle(),props));
                        str.append("</a>\");\n");
                    } else {
                        str.append("myScroller1.addItem(\"<a href='");
                        str.append(Global.getRootPath());
                        str.append("/blog/showblog.jsp?rootid=");
                        str.append(md.getId());
                        str.append("' target=_blank><font color=");
                        str.append(color);
                        str.append(">");
                        str.append(format(md.getTitle(),props));
                        str.append("</font></a>\");\n");
                    }
                }
            }
        }
        str.append("</script>");
        return str.toString();
    }

    public String renderPostRank(HttpServletRequest request,
                                  List params) {
        BlogDb bd = BlogDb.getInstance();
        int row = StrUtil.toInt((String)props.get("row"), 10);
        long[] ids = bd.getPostRank(row);
        int len = ids.length;
        UserConfigDb ucd2 = new UserConfigDb();
        StringBuffer str = new StringBuffer("<ul>");
        for (int i = 0; i < len; i++) {
            UserConfigDb ucd = ucd2.getUserConfigDb(ids[i]);
            str.append("<li><a href='");
            str.append(Global.getRootPath());
            str.append("/blog/myblog.jsp?blogId=");
            str.append(ucd.getId());
            str.append("' target=_blank>");
            str.append(format(ucd.getTitle(), props));
            str.append("</a></li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    public String renderReplyRank(HttpServletRequest request,
                                   List params) {
        BlogDb bd = BlogDb.getInstance();
        int row = StrUtil.toInt((String)props.get("row"), 10);
        long[] ids = bd.getReplyRank(row);
        int len = ids.length;
        UserConfigDb ucd2 = new UserConfigDb();
        StringBuffer str = new StringBuffer("<ul>");
        for (int i = 0; i < len; i++) {
            UserConfigDb ucd = ucd2.getUserConfigDb(ids[i]);
            str.append("<li><a href='");
            str.append(Global.getRootPath());
            str.append("/blog/myblog.jsp?blogId=");
            str.append(ucd.getId());
            str.append("' target=_blank>");
            str.append(format(ucd.getTitle(), props));
            str.append("</a></li>");
        }
        str.append("</ul>");
        return str.toString();
    }

    /**
     * 显示新照片，用于首页中显示两行照片
     * @param request HttpServletRequest
     * @param params List
     * @return String
     */
    public String renderListPhoto(HttpServletRequest request,
                                  List params) {
        int row = StrUtil.toInt((String)props.get("row"), 10);
        int pageNum = StrUtil.toInt((String)props.get("pageNum"), 1);

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();

        PhotoDb pd = new PhotoDb();
        ListResult lr = null;
        try {
            lr = pd.listResult(pd.getListPhotoSql(), pageNum, row);
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("renderListPhoto:" + e.getMessage());
            return "";
        }

        Iterator ir = lr.getResult().iterator();
        StringBuffer str = new StringBuffer();
        while (ir.hasNext()) {
            pd = (PhotoDb) ir.next();
            String attachmentBasePath = Global.getRootPath() + "/upfile/" +
                                        pd.photoBasePath + "/";
            if (pd.isRemote()) {
                boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
                if (isFtpUsed) {
                    attachmentBasePath = cfg.getProperty("forum.ftpUrl");
                    if (attachmentBasePath.lastIndexOf("/") !=
                        attachmentBasePath.length() - 1)
                        attachmentBasePath += "/";
                    attachmentBasePath += pd.photoBasePath + "/";
                }
            }
            str.append("<div class=\"index_photo_wrap\"><div class=\"index_photo_box\"><a href='showphoto.jsp?blogId=");
            str.append(pd.getBlogId());
            str.append("&photoId=");
            str.append(pd.getId());
            str.append("'>");
            str.append("<img src='");
            str.append(attachmentBasePath);
            str.append(pd.getImage());
            str.append("' alt='");
            str.append(StrUtil.toHtml(pd.getTitle()));
            str.append("' border=0></a></div>");
            str.append("<a title=\"");
            str.append(StrUtil.toHtml(pd.getTitle()));
            str.append("\" href=\"showphoto.jsp?blogId=");
            str.append(pd.getBlogId());
            str.append("&photoId=");
            str.append(pd.getId());
            str.append("\">");
            str.append(format(pd.getTitle(), props));
            str.append("</a>");
            str.append("</div>");
        }
        return str.toString();
    }

    public String renderNewPhoto(HttpServletRequest request,
                                   List params) {
        BlogDb bd = BlogDb.getInstance();
        int row = StrUtil.toInt((String)props.get("row"), 10);
        int[] newPhotos = bd.getNewPhotos(row);
        StringBuffer str = new StringBuffer("<table cellpadding='5'>");
        if (newPhotos != null) {
            int newPhotosLen = newPhotos.length;
            PhotoDb pd = new PhotoDb();

            com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();

            for (int i = 0; i < newPhotosLen; i++) {
                pd = pd.getPhotoDb(newPhotos[i]);

                String attachmentBasePath = Global.getRootPath() + "/upfile/" +
                                            pd.photoBasePath + "/";
                if (pd.isRemote()) {
                    boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
                    if (isFtpUsed) {
                        attachmentBasePath = cfg.getProperty("forum.ftpUrl");
                        if (attachmentBasePath.lastIndexOf("/") !=
                            attachmentBasePath.length() - 1)
                            attachmentBasePath += "/";
                        attachmentBasePath += pd.photoBasePath + "/";
                    }
                }

                str.append("<td>");
                str.append("<a href='showphoto.jsp?blogId=");
                str.append(pd.getBlogId());
                str.append("&photoId=");
                str.append(pd.getId());
                str.append("'>");
                str.append("<img src='");
                str.append(attachmentBasePath);
                str.append(pd.getImage());
                str.append("' alt='");
                str.append(StrUtil.toHtml(pd.getTitle()));
                str.append("' width=117 height=100 border=0>");
                str.append("</a>");
                str.append("</td>");
            }
        }
        str.append("</table>");
        return str.toString();
    }

    public String renderNotice(HttpServletRequest request,
                                    List params) {
        StringBuffer str = new StringBuffer();
        Home home = Home.getInstance();
        int[] v = home.getNoticeIds();
        int noticeLen = v.length;
        if (noticeLen == 0)
            ;
        else {
            MsgDb md = null;
            MsgMgr mm = new MsgMgr();
            str.append("<ul>");
            for (int k = 0; k < noticeLen; k++) {
                md = mm.getMsgDb(v[k]);
                if (md.isLoaded()) {
                    String color = StrUtil.getNullString(md.getColor());
                    if (color.equals("")) {
                        str.append("<li>");
                        str.append(formatDate(md.getAddDate(), props));
                        str.append("<a href='");
                        str.append(Global.getRootPath());
                        str.append("/blog/showblog.jsp?rootid=");
                        str.append(md.getId());
                        str.append("' target='_blank'>");
                        str.append(format(DefaultRender.RenderFullTitle(request, md),props));
                        str.append("</a></li>");
                    } else {
                        str.append("<li>");
                        str.append(formatDate(md.getAddDate(), props));
                        str.append("<a href='");
                        str.append(Global.getRootPath());
                        str.append("/blog/showblog.jsp?rootid=");
                        str.append(md.getId());
                        str.append("' target=_blank><font color=");
                        str.append(color);
                        str.append(">");
                        str.append(format(DefaultRender.RenderFullTitle(request, md),props));
                        str.append("</font></a></li>");
                    }
                }
            }
            str.append("</ul>");
        }
        return str.toString();
    }

    public String renderFocus(HttpServletRequest request, List params) {
        StringBuffer str = new StringBuffer();
        Home home = Home.getInstance();
        int[] v = home.getFocusIds();
        int hotlen = v.length;
        if (hotlen == 0)
            ;
        else {
            boolean isDateShow = false;
            String dateFormat = "";
            String dt = (String) props.get("date");
            if (dt != null) {
                isDateShow = dt.equals("true") || dt.equals("yes") || dt.equals("y");
                dateFormat = (String) props.get("dateFormat");
                if (dateFormat == null) {
                    dateFormat = "yy-MM-dd";
                }
            }

            MsgDb md = null;
            MsgMgr mm = new MsgMgr();

            int abstractLen = StrUtil.toInt((String)props.get("abstract"), 50);
            md = mm.getMsgDb(v[0]);
            str.append("<div id=\"abstract\"><a href=\"");
            str.append(Global.getRootPath());
            str.append("/blog/showblog.jsp?rootid=");
            str.append(md.getId());
            str.append("\" target=\"_blank\">");
            str.append(MsgUtil.getAbstract(request, md, abstractLen));
            str.append("</a></div>");

            if (hotlen>1) {
                str.append("<ul>");
                for (int k = 1; k < hotlen; k++) {
                    md = mm.getMsgDb(v[k]);
                    if (md.isLoaded()) {
                        String color = StrUtil.getNullString(md.getColor());
                        if (color.equals("")) {
                            str.append("<li><a href='");
                            str.append(Global.getRootPath());
                            str.append("/blog/showblog.jsp?rootid=");
                            str.append(md.getId());
                            str.append("' target='_blank'>");
                            str.append(format(DefaultRender.RenderFullTitle(request, md),props));
                            str.append("</a></li>");
                        } else {
                            str.append("<li><a href='");
                            str.append(Global.getRootPath());
                            str.append("/blog/showblog.jsp?rootid=");
                            str.append(md.getId());
                            str.append("' target=_blank><font color=");
                            str.append(color);
                            str.append(">");
                            str.append(format(DefaultRender.RenderFullTitle(request, md),props));
                            str.append("</font></a></li>");
                        }
                        if (isDateShow) {
                            str.append("&nbsp;&nbsp;");
                            str.append(DateUtil.format(md.getAddDate(), dateFormat));
                        }
                    }
                }
                str.append("</ul>");
            }
        }
        return str.toString();
    }

    public String renderFlashImage(HttpServletRequest request,
                                     List params) {
        Home home = Home.getInstance();
        StringBuffer str = new StringBuffer();
        str.append("<script>");
        for (int i = 1; i <= 5; i++) {
            str.append("imgUrl");
            str.append(i);
            str.append("=\"");
            str.append(StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,"url")));
            str.append("\";\n");
            str.append("imgtext");
            str.append(i);
            str.append("=\"");
            str.append(StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,"text")));
            str.append("\";\n");
            str.append("imgLink");
            str.append(i);
            str.append("=\"");
            str.append(StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,"link")));
            str.append("\";\n");
        }
        String w = (String)props.get("w");
        String h = (String)props.get("h");
        str.append("var focus_width=");
        str.append(StrUtil.toInt(w, 240));
        str.append(";\n");
        str.append("var focus_height=");
        str.append(StrUtil.toInt(h, 190));
        str.append(";\n");
        str.append("var text_height=18;\n");
        str.append("swf_height = focus_height+text_height;\n");
        str.append("var pics=imgUrl1+\"|\"+imgUrl2+\"|\"+imgUrl3+\"|\"+imgUrl4+\"|\"+imgUrl5;\n");
        str.append("var links=imgLink1+\"|\"+imgLink2+\"|\"+imgLink3+\"|\"+imgLink4+\"|\"+imgLink5;\n");
        str.append("var texts=imgtext1+\"|\"+imgtext2+\"|\"+imgtext3+\"|\"+imgtext4+\"|\"+imgtext5;\n");

        str.append("document.write('<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\" width=\"'+ focus_width +'\" height=\"'+ swf_height +'\">');");
        str.append("document.write('<param name=\"allowScriptAccess\" value=\"sameDomain\"><param name=\"movie\" value=\"");
        str.append(Global.getRootPath());
        str.append("/blog/images/home/focus.swf\"><param name=\"quality\" value=\"high\"><param name=\"bgcolor\" value=\"#F0F0F0\">');");
        str.append("document.write('<param name=\"menu\" value=\"false\"><param name=wmode value=\"opaque\">');");
        str.append("document.write('<param name=\"FlashVars\" value=\"pics='+pics+'&links='+links+'&texts='+texts+'&borderwidth='+focus_width+'&borderheight='+focus_height+'&textheight='+text_height+'\">');");
        str.append("document.write('</object>');");
        str.append("</script>");
        return str.toString();
    }

    public String renderPhotoDir(HttpServletRequest request, List params) {
        DirChildrenCache lccm = new DirChildrenCache(DirDb.ROOTCODE);
        Iterator ir = lccm.getDirList().iterator();
        StringBuffer str = new StringBuffer();
        str.append("<ul>");
        while (ir.hasNext()) {
            DirDb lf = (DirDb) ir.next();
            str.append("<li><a href='listblogphoto.jsp?dirCode=");
            str.append(lf.getCode());
            str.append("'>");
            str.append(lf.getName());
            str.append("</a></li>");

            lccm = new DirChildrenCache(lf.getCode());
            Vector v = lccm.getDirList();
            if (v.size()>0) {
                str.append("<li><ul>");
            }
            Iterator ir2 = v.iterator();
            while (ir2.hasNext()) {
                lf = (DirDb) ir2.next();
                str.append("<li><a href='listblogphoto.jsp?dirCode=");
                str.append(lf.getCode());
                str.append("'>");
                str.append(lf.getName());
                str.append("</a></li>");
            }
            if (v.size()>0) {
                str.append("</ul></li>");
            }
        }
        str.append("</ul>");
        return str.toString();
    }

}
