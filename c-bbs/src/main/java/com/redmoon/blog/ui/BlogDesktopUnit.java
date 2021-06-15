package com.redmoon.blog.ui;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.module.cms.ui.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.blog.*;
import com.redmoon.blog.Home;
import com.redmoon.blog.photo.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.DefaultRender;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BlogDesktopUnit implements IDesktopUnit {
    public BlogDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request, DesktopItemDb di) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
        String url = du.getPageList() + "?dirCode=" + StrUtil.UrlEncode(di.getModuleItem());
        return url;
    }

    public String displayRecommandBlog(HttpServletRequest request,
                                       DesktopItemDb di) {
        String str = "";
        BlogDb bd = BlogDb.getInstance();
        Vector rv = bd.getAllRecommandBlogs();
        int nsize = rv.size();
        if (nsize == 0)
            ;
        else {
            Iterator ir = rv.iterator();
            str += "<ul>";
            while (ir.hasNext()) {
                UserConfigDb ucd = (UserConfigDb) ir.next();
                str += "<li><a href='myblog.jsp?blogId=" + ucd.getId() +
                        "' target=_blank>" +
                        StrUtil.toHtml(StrUtil.getLeft(ucd.getTitle(), di.getTitleLen())) +
                        "</a></li>";

            }
            str += "</ul>";
        }
        return str;
    }

    public String displayNewBlogTopic(HttpServletRequest request,
                                      DesktopItemDb di) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());

        String str = "";
        str += "<ul>";
        MsgDb msd = null;
        MsgMgr mm = new MsgMgr();
        long[] newMsgs = null;
        BlogDb bd = new BlogDb();
        bd = bd.getBlogDb();
        newMsgs = bd.getNewBlogMsgs(di.getCount());
        if (newMsgs == null)
            return "";
        int newMsgsLen = newMsgs.length;
        if (newMsgsLen > di.getCount())
            newMsgsLen = di.getCount();
        HashMap props = di.getProps();
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
            str += "<li><a href='../" + du.getPageShow() + "?rootid=" +
                    msd.getId() + "' target='_blank'>" +
                    StrUtil.toHtml(StrUtil.getLeft(msd.getTitle(), di.getTitleLen()));
            if (isDateShow) {
                str += "&nbsp;[" + DateUtil.format(msd.getAddDate(), dateFormat) +
                        "]";
            }
            str += "</a></li>";
        }
        str += "</ul>";
        return str;
    }

    public String displayNewAddBlog(HttpServletRequest request,
                                    DesktopItemDb di) {
        String str = "";
        str += "<ul>";
        BlogDb bd = new BlogDb();
        long[] newBlogs = bd.getNewBlogs(di.getCount());

        if (newBlogs != null) {
            UserConfigDb ucd = new UserConfigDb();
            int newBlogsLen = newBlogs.length;
            for (int i = 0; i < newBlogsLen; i++) {
                ucd = ucd.getUserConfigDb(newBlogs[i]);
                str += "<li><a href='myblog.jsp?blogId=" + ucd.getId() +
                        "' title='" + StrUtil.toHtml(ucd.getTitle()) +
                        "'>" + StrUtil.toHtml(StrUtil.getLeft(ucd.getTitle(), di.getCount())) +
                        "</a></li>";
            }
        }
        str += "</ul>";
        return str;
    }

    public String displayNewUpdateBlog(HttpServletRequest request,
                                       DesktopItemDb di) {
        String str = "";
        str += "<ul>";
        BlogDb bd = new BlogDb();
        long[] newUpdateBlogs = bd.getNewUpdateBlogs(di.getCount());
        if (newUpdateBlogs != null) {
            UserConfigDb ucd = new UserConfigDb();
            int newBlogsLen = newUpdateBlogs.length;
            for (int i = 0; i < newBlogsLen; i++) {
                ucd = ucd.getUserConfigDb(newUpdateBlogs[i]);
                str += "<li><a href='myblog.jsp?blogId=" + ucd.getId() +
                        "' title='" + StrUtil.toHtml(ucd.getTitle()) +
                        "'>" + StrUtil.toHtml(StrUtil.getLeft(ucd.getTitle(), di.getCount())) +
                        "</a></li>";
            }
        }
        str += "</ul>";
        return str;
    }

    public String displayBlogFocus(HttpServletRequest request, DesktopItemDb di) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
        String str = "";
        Home home = Home.getInstance();
        int[] v = home.getFocusIds();
        int hotlen = v.length;
        if (hotlen == 0)
            ;
        else {
            HashMap props = di.getProps();
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

            MsgDb md = null;
            MsgMgr mm = new MsgMgr();
            str += "<ul>";
            for (int k = 0; k < hotlen; k++) {
                md = mm.getMsgDb(v[k]);
                if (md.isLoaded()) {
                    String color = StrUtil.getNullString(md.getColor());
                    if (color.equals("")) {
                        str += "<li><a href='" + Global.getRootPath() +
                                "/" + du.getPageShow() +
                                "?rootid=" +
                                md.getId() + "' target='_blank'>" +
                                StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md),
                                                di.getTitleLen()) +
                                "</a></li>";
                    } else {
                        str += "<li><a href='" + Global.getRootPath() +
                                "/" + du.getPageShow() +
                                "?rootid=" + md.getId() +
                                "' target=_blank><font color=" +
                                color +
                                ">" +
                                StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md),
                                                di.getTitleLen()) + "</font></a></li>";
                    }
                    if (isDateShow) {
                        str += "&nbsp;&nbsp;" +
                                DateUtil.format(md.getAddDate(), dateFormat);
                    }
                }
            }
            str += "</ul>";
        }
        return str;
    }

    public String dispalyFlashImages(HttpServletRequest request,
                                     DesktopItemDb di) {
        Home home = Home.getInstance();
        String str = "<script>";
        for (int i = 1; i <= 5; i++) {
            str += "imgUrl" + i + "=\"" +
                    StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
                    "url")) + "\";\n";
            str += "imgtext" + i + "=\"" +
                    StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
                    "text")) + "\";\n";
            str += "imgLink" + i + "=\"" +
                    StrUtil.getNullStr(home.getProperty("flash", "id", "" + i,
                    "link")) + "\";\n";
        }
        str += "</script>";
        return str;
    }

    public String displayBlogNotice(HttpServletRequest request,
                                    DesktopItemDb di) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
        String str = "";
        Home home = Home.getInstance();
        int[] v = home.getNoticeIds();
        int noticeLen = v.length;
        if (noticeLen == 0)
            ;
        else {
            HashMap props = di.getProps();
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

            MsgDb md = null;
            MsgMgr mm = new MsgMgr();
            for (int k = 0; k < noticeLen; k++) {
                md = mm.getMsgDb(v[k]);
                if (md.isLoaded()) {
                    String color = StrUtil.getNullString(md.getColor());
                    if (color.equals("")) {
                        str += "<a href='" + Global.getRootPath() + "/" +
                                du.getPageShow() +
                                "?rootid=" +
                                md.getId() + "' target='_blank'>" +
                                StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md),
                                                di.getTitleLen()) +
                                "</a>&nbsp;";
                    } else {
                        str += "<a href='" + Global.getRootPath() + "/" +
                                du.getPageShow() +
                                "?rootid=" + md.getId() +
                                "' target=_blank><font color=" +
                                color +
                                ">" +
                                StrUtil.getLeft(DefaultRender.RenderFullTitle(request, md),
                                                di.getTitleLen()) + "</font></a>&nbsp;";
                    }
                    if (isDateShow) {
                        str += DateUtil.format(md.getAddDate(), dateFormat) +
                                "&nbsp;&nbsp;";
                    }
                }
            }
        }
        return str;
    }

    public String displayBlogVerticalScroller(HttpServletRequest request,
                                              DesktopItemDb di) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
        String str = "<script>";
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
                        str += "myScroller1.addItem(\"<a href='" +
                                Global.getRootPath() + "/" +
                                du.getPageShow() +
                                "?rootid=" +
                                md.getId() + "' target='_blank'>" +
                                StrUtil.toHtml(StrUtil.getLeft(md.getTitle(),
                                                di.getTitleLen())) +
                                "</a>\");\n";
                    } else {
                        str += "myScroller1.addItem(\"<a href='" +
                                Global.getRootPath() + "/" +
                                du.getPageShow() +
                                "?rootid=" + md.getId() +
                                "' target=_blank><font color=" +
                                color +
                                ">" +
                                StrUtil.toHtml(StrUtil.getLeft(md.getTitle(),
                                                di.getTitleLen())) + "</font></a>\");\n";
                    }
                }
            }
        }
        str += "</script>";
        return str;
    }

    public String displayPostRank(HttpServletRequest request,
                                  DesktopItemDb di) {
        BlogDb bd = BlogDb.getInstance();
        long[] ids = bd.getPostRank(di.getCount());
        int len = ids.length;
        UserConfigDb ucd2 = new UserConfigDb();
        String str = "<ul>";
        for (int i = 0; i < len; i++) {
            UserConfigDb ucd = ucd2.getUserConfigDb(ids[i]);
            str += "<li><a href='" + Global.getRootPath() +
                    "/blog/myblog.jsp?blogId=" + ucd.getId() +
                    "' target=_blank>" +
                    StrUtil.toHtml(StrUtil.getLeft(ucd.getTitle(), di.getTitleLen())) +
                    "</a></li>";

        }
        str += "</ul>";
        return str;
    }

    public String displayReplyRank(HttpServletRequest request,
                                   DesktopItemDb di) {
        BlogDb bd = BlogDb.getInstance();
        long[] ids = bd.getReplyRank(di.getCount());
        int len = ids.length;
        UserConfigDb ucd2 = new UserConfigDb();
        String str = "<ul>";
        for (int i = 0; i < len; i++) {
            UserConfigDb ucd = ucd2.getUserConfigDb(ids[i]);
            str += "<li><a href='" + Global.getRootPath() +
                    "/blog/myblog.jsp?blogId=" + ucd.getId() +
                    "' target=_blank>" +
                    StrUtil.toHtml(StrUtil.getLeft(ucd.getTitle(), di.getTitleLen())) +
                    "</a></li>";

        }
        str += "</ul>";
        return str;
    }

    public String displayNewPhotos(HttpServletRequest request,
                                   DesktopItemDb di) {
        BlogDb bd = BlogDb.getInstance();
        int[] newPhotos = bd.getNewPhotos(di.getCount());
        String str = "<table cellpadding='5'>";
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

                str += "<td>";
                str += "<a href='showphoto.jsp?blogId=" + pd.getBlogId() +
                        "&id=" + pd.getId() + "'>";
                str += "<img src='" + attachmentBasePath +
                        pd.getImage() + "' alt='" + StrUtil.toHtml(pd.getTitle()) +
                        "' width=117 height=100 border=0>";
                str += "</a>";
                str += "</td>";
            }
        }
        str += "</table>";
        return str;
    }

    public String displayBlogStars(HttpServletRequest request,
                                   DesktopItemDb di) {
        BlogDb bd = BlogDb.getInstance();
        String star = bd.getStar();
        if (star.equals(""))
            return "";
        String[] nicks = StrUtil.split(star, ",");
        int len = nicks.length;
        UserConfigDb ucd = new UserConfigDb();
        UserDb user = new UserDb();

        String str = "";
        for (int i = 0; i < len; i++) {
            user = user.getUserDbByNick(nicks[i]);
            if (user==null) {
                user = new UserDb();
                continue;
            }
            ucd = ucd.getUserConfigDbByUserName(user.getName());
            if (ucd != null) {
                str += "<table width='100%' height='64'><tr>";
                str += "<td width='64'>";
                str += "<a href='#'>";
                if (user.getMyface().equals("")) {
                    str += "<img border=0 src='" + Global.getRootPath() +
                            "/forum/images/face/" + user.getRealPic() +
                            "' width=60 height=60/>";
                } else {
                    str += "<img border=0 src='" + user.getMyfaceUrl(request) +
                            "' width=60 height=60/>";
                }
                str += "</a>";
                str += "</td>";
                str +=
                        "<td width='104'><table><tr><td><a href='" +
                        Global.getRootPath() + "/blog/myblog.jsp?blogId=" +
                        ucd.getId() + "' class='person1'>" +
                        StrUtil.toHtml(StrUtil.getLeft(ucd.getTitle(), di.getCount())) +
                        "</a></td>";
                str += "<tr><td></td><tr><td><a href='" +
                        Global.getRootPath() + "/userinfo.jsp?username=" +
                        StrUtil.UrlEncode(user.getName()) +
                        "' class='person2'>[" + user.getNick() +
                        "]</a></td></tr></table></td>";
                str += "</tr></table>";
            } else
                ucd = new UserConfigDb();
        }

        return str;
    }

    public String display(HttpServletRequest request, DesktopItemDb di) {
        // System.out.println(getClass() + "di.getModuleItem()=" + di.getModuleItem());
        if (di.getModuleItem().startsWith("cws_")) {
            String var = di.getModuleItem().substring(4);
            if (var.equals("newBlogTopic")) {
                return displayNewBlogTopic(request, di);
            } else if (var.equals("newAddBlog")) {
                return displayNewAddBlog(request, di);
            } else if (var.equals("blogFocus")) {
                return displayBlogFocus(request, di);
            } else if (var.equals("blogNotice")) {
                return displayBlogNotice(request, di);
            }else if (var.equals("recommandBlog")) {
                return displayRecommandBlog(request, di);
            } else if (var.equals("postRank")) {
                return displayPostRank(request, di);
            } else if (var.equals("replyRank")) {
                return displayReplyRank(request, di);
            } else if (var.equals("newPhotos")) {
                return displayNewPhotos(request, di);
            } else if (var.equals("blogStars")) {
                return displayBlogStars(request, di);
            } else if (var.equals("verticalScroller")) {
                return displayBlogVerticalScroller(request, di);
            } else if (var.equals("flashImages")) {
                return dispalyFlashImages(request, di);
            } else if (var.equals("newUpdateBlog")) {
                return displayNewUpdateBlog(request, di);
            } else if (var.equals("blogScrollImages")) {
                return dispalyScrollImages(request, di);
            }
            else
                return di.getModuleItem();
        }
        else if (di.getModuleItem().startsWith("ad_")) {
            String strId = di.getModuleItem().substring(3);
            Home home = Home.getInstance();
            return home.getProperty("ads", "id", strId);
        } else {
            DesktopMgr dm = new DesktopMgr();
            DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
            BlogDb bd = new BlogDb();
            MsgMgr mm = new MsgMgr();
            String str = "";
            str += "<ul>";
            long[] msgIds = bd.getBlogMsgsOfKind(di.getCount(),
                                                 di.getModuleItem());
            int msgIdsLen = msgIds.length;
            MsgDb msd;
            for (int i = 0; i < msgIdsLen; i++) {
                msd = mm.getMsgDb(msgIds[i]);
                str += "<li><a href='../" + du.getPageShow() + "?rootid=" +
                        msd.getId() + "' target='_blank'>" + StrUtil.getLeft(StrUtil.toHtml(msd.getTitle()), di.getTitleLen()) +
                        " [" + DateUtil.format(msd.getAddDate(), "yyyy-MM-dd") +
                        "]</a></li>";
            }
            str += "</ul>";
            return str;
        }
    }

    public String dispalyScrollImages(HttpServletRequest request,
                                      DesktopItemDb di) {
        String str = "";
        str +=
                "<DIV id=demo style='OVERFLOW: hidden; WIDTH: 100%; COLOR: #ffffff'>";
        str +=
                "<TABLE cellSpacing=0 cellPadding=0 align=left border=0 cellspace='0'>";
        str += "<TBODY>";
        str += "<TR>";
        str += "<TD id=demo1 vAlign=top>";
        str +=
                "<table width='1710' height='116'  border='0' cellpadding='0' cellspacing='0'>";
        str += "<tr>";
        Home home = Home.getInstance();
        String[][] imgs = home.getScrollImages();
        int row = imgs.length;
        int len = di.getCount();
        if (row>len)
            row = len;
        for (int i = 0; i < row; i++) {
            str += "<td width='171'><div align='center'><a target=_blank href='" + imgs[i][1] + "'><img border=0 alt='" + imgs[i][2] + "' src='" + imgs[i][0] + "'></a></div></td>";
        }
        str += "</tr></table></TD><TD id=demo2 vAlign=top>&nbsp;</TD></TR></TBODY></TABLE></DIV>";
        str += "<SCRIPT>\n";
        str += "var speed3=15;\n";
        str += "demo2.innerHTML=demo1.innerHTML;\n";
        str += "function Marquee(){\n";
        str += "if(demo2.offsetWidth-demo.scrollLeft<=0)\n";
        str += "demo.scrollLeft-=demo1.offsetWidth;\n";
        str += "else{\n";
        str += "demo.scrollLeft++;\n";
        str += "}\n";
        str += "}\n";
        str += "var MyMar=setInterval(Marquee,speed3);\n";
        str += "demo.onmouseover=function() {clearInterval(MyMar)}\n";
        str +=
                "demo.onmouseout=function() {MyMar=setInterval(Marquee,speed3)}\n";
        str += "</SCRIPT>";
        return str;
    }

}
