package com.redmoon.blog.template;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.*;
import com.redmoon.blog.link.*;
import com.redmoon.blog.photo.*;
import com.redmoon.blog.ui.*;
import com.redmoon.forum.*;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;

/**
 * <p>Title: 个人博客模板的元素显示</p>
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
public class BokeTemplateImpl extends VarPart {

    public BokeTemplateImpl() {
    }

    /**
     * 根据模板类型（main或sub），获取缓存键值，用于根据键值取出ITemplate，以免每次访问都解析模板
     * @param ucd UserConfigDb
     * @param templateType String 模板类型，仅main与sub需用到缓存，common不需要缓存
     * @return String
     */
    public static String getTemplateCacheKey(UserConfigDb ucd,
                                             String templateType) {
        if (ucd.isUserCss()) {
            return BlogUserTemplateDb.getTemplateCacheKey(ucd, templateType);
        } else {
            TemplateDb td = new TemplateDb();
            td = td.getTemplateDb(StrUtil.toInt(ucd.getSkin()));
            if (td == null) {
                td = new TemplateDb();
                td = td.getDefaultTemplateDb();
            }
            return td.getCacheKey(templateType);
        }
    }

    /**
     * 根据字段名取出模板相应的内容
     * @param ucd UserConfigDb
     * @param templateFieldName String
     * @return String
     */
    public static String getTemplateContent(UserConfigDb ucd,
                                            String templateFieldName) {
        if (ucd.isUserCss()) {
            BlogUserTemplateDb butd = new BlogUserTemplateDb();
            butd = butd.getBlogUserTemplateDb(ucd.getId());
            return butd.getString(templateFieldName);
        } else {
            TemplateDb td = new TemplateDb();
            td = td.getTemplateDb(StrUtil.toInt(ucd.getSkin()));
            if (td == null) {
                td = new TemplateDb();
                td = td.getDefaultTemplateDb();
            }
            return td.getString(templateFieldName);
        }
    }

    public String renderCalendar(HttpServletRequest request, UserConfigDb ucd) {
        String str = "";
        str += "<div id=div_cal></div>";

        str += "<script>\n";
        str += "var blogId=\"" + ucd.getId() + "\";\n";
        str += "</script>\n";
        str += "<script src=\"" + request.getContextPath() +
                "/blog/inc/calendar.js\">";
        str += "</script>\n";

        // 取得显示的年月
        int leftyear, leftmonth;
        try {
            leftyear = ParamUtil.getInt(request, "year");
            leftmonth = ParamUtil.getInt(request, "month");
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            leftyear = cal.get(cal.YEAR);
            leftmonth = cal.get(cal.MONTH) + 1;
        }
        if (leftmonth > 12)
            leftmonth = 12;
        if (leftmonth < 1)
            leftmonth = 1;

        str += "<script>\n";
        str += "newCalendar(\"div_cal\"," + leftyear + "," + leftmonth + ");\n";
        str += "</script>\n";

        // 取得year-month这个月中的所有日志，遍历后对日历初始化
        UserBlog bu = new UserBlog(ucd.getId());
        int[] dayCountAry = bu.getBlogDayCount(leftyear, leftmonth);
        int dayLen = dayCountAry.length;

        str += "<script>\n";
        for (int n = 1; n < dayLen; n++) {
            if (dayCountAry[n] > 0) {
                String totle_log = cn.js.fan.web.SkinUtil.LoadString(request,
                        "res.label.blog.left", "totle_log");
                totle_log = StrUtil.format(totle_log,
                                           new Object[] {"" + dayCountAry[n]});

                // alert(day<%=n%>.innerHTML);
                str += "day" + n + ".innerHTML=\"<table width=100% cellSpacing=0 cellPadding=1><tr><td align=center class=dayCell><a href='listdayblog.jsp?blogId=" +
                        ucd.getId() + "&y=" + leftyear + "&m=" + leftmonth +
                        "&d=" + n + "' title='" + totle_log + "'>" + n
                        + "</a></td></tr></table>\";\n";

            }
        }
        str += "</script>\n";
        return str;
    }

    /**
     * 显示博客文章
     * @param request
     * @param ucd
     * @param td
     * @return
     */
    public String renderSubShowblog(HttpServletRequest request,
                                    UserConfigDb ucd,
                                    TemplateDb td) {
        String str = "";
        MsgDb rootMsgDb = (MsgDb) request.getAttribute("rootMsgDb");
        try {
            // 写主题贴
            request.setAttribute("MsgDb", rootMsgDb);
            TemplateLoader tl = new TemplateLoader(request,
                    getTemplateCacheKey(ucd, TemplateDb.
                                        TEMPL_TYPE_SUB),
                    getTemplateContent(ucd, "sub_content"));
            str += tl.toString();

            // 写出标签
            com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.
                                            getInstance();
            if (cfg1.getBooleanProperty("forum.isTag")) {
                Vector vtag = rootMsgDb.getTags();
                str += "<div class='tag'>" + SkinUtil.LoadString(request,
                        "res.label.forum.showtopic",
                        "tag") + "：";
                if (vtag.size() > 0) {
                    Iterator irtag = vtag.iterator();
                    TagDb td2 = new TagDb();
                    while (irtag.hasNext()) {
                        TagMsgDb tmd = (TagMsgDb) irtag.next();
                        TagDb tag = td2.getTagDb(tmd.getLong("tag_id"));
                        if (td != null) {
                            str +=
                                    "<a class=\"linkTag\" target=\"_blank\" href=\"";
                            str += "listtag.jsp?tagId=" + tmd.getLong("tag_id") +
                                    ">";
                            str += tag.getString("name") + "</a>&nbsp;&nbsp;";
                        }
                    }

                }
                str += "</div>";
            }

            // 写出留下足迹
            str +=
                    "<div class='footprintBox'>[<a href=\"javascript:if (confirm('" +
                    SkinUtil.LoadString(request, "res.label.blog.myblog",
                                        "confirm_footprint") +
                    "')) window.location.href='../blog/footprint_do.jsp?msgId=" +
                    rootMsgDb.getRootid() + "&blogId=" + ucd.getId() + "'\">" +
                    SkinUtil.LoadString(request,
                                        "res.label.blog.user.userconfig",
                                        "is_footprint") + "</a>]</div>";
            str += "<div class='footprintBox2'>";
            UserMgr um = new UserMgr();
            com.redmoon.blog.Config bcfg = com.redmoon.blog.Config.getInstance();
            FootprintDb fd = new FootprintDb();
            String sql = "select msg_id,user_name from " +
                         fd.getTable().getName() + " where msg_id=" +
                         rootMsgDb.getId() + " order by add_date desc";

            ListResult lr = fd.listResult(sql, 1,
                                          bcfg.getIntProperty(
                                                  "footprintDisplayCount"));
            Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                fd = (FootprintDb) ir.next();
                UserDb ud = um.getUser(fd.getString("user_name"));
                if (ud.getGender().equals("M")) {
                    str += "<img src=\"../blog/skin/" + td.getString("code") +
                            "/images/man.png\">";
                } else {
                    str += "<img src=\"../blog/skin/" + td.getString("code") +
                            "/images/woman.png\">";
                }
                str += "&nbsp;<a href=\"userinfo.jsp?username=" +
                        StrUtil.UrlEncode(ud.getName()) + "\">" + ud.getNick() +
                        "</a>&nbsp;&nbsp;";
            }
            str += "</div>";

            // 写出跟贴
            /*
                         sql = "select id from sq_message where rootid=" +
                         rootMsgDb.getId() + " and check_status=" +
                         MsgDb.CHECK_STATUS_PASS + " ORDER BY lydate asc";
             long totalmsg = rootMsgDb.getMsgCount(sql, rootMsgDb.getboardcode(),
                                                  rootMsgDb.getId());
             */
            sql = SQLBuilder.getShowtopictreeSql(rootMsgDb.getId());
            long totalmsg = rootMsgDb.getMsgCount(sql, rootMsgDb.getboardcode(),
                                                  rootMsgDb.getId());

            int pagesize = 10;

            String op = ParamUtil.get(request, "op");
            if (op.equals("allcomm"))
                pagesize = (int) totalmsg;
            Paginator paginator = new Paginator(request, totalmsg, pagesize);
            int curpage = paginator.getCurPage();

            // 设置当前页数和总页数
            int totalpages = paginator.getTotalPages();
            if (totalpages == 0) {
                curpage = 1;
                totalpages = 1;
            }

            long start = (curpage - 1) * pagesize;
            long end;
            if (curpage==1)
            	end = curpage * pagesize;
            else
            	end = curpage * pagesize + 1;

            // MsgBlockIterator irmsg = rootMsgDb.getMsgs(sql,
            //         rootMsgDb.getboardcode(), rootMsgDb.getId(), start, end);

            MsgBlockIterator irmsg = rootMsgDb.getMsgs(sql,
                    rootMsgDb.getboardcode(), rootMsgDb.getId(), start, end);

            if (irmsg.hasNext() && curpage == 1) {
                irmsg.next(); // 跳过root贴子
            } while (irmsg.hasNext()) {
                MsgDb msgDb = (MsgDb) irmsg.next();
                request.setAttribute("MsgDb", msgDb);
                try {
                    tl = new TemplateLoader(request,
                                            getTemplateCacheKey(ucd, TemplateDb.
                            TEMPL_TYPE_SUB),
                                            getTemplateContent(ucd,
                            "sub_content"));
                    str += tl.toString();
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("renderSubShowblog:" +
                            e.getMessage());
                }
            }

            if (!op.equals("allcomm")) {
                str += "<div class='pageBlock'>";
                str +=
                        paginator.getCurPageBlock("showblog.jsp?boardcode=" +
                                                  rootMsgDb.getboardcode() +
                                                  "&rootid=" + rootMsgDb.getId());
                str += "</div>";
            }

            str += getAreaReply(request, rootMsgDb.getId());
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("toString:" + e.getMessage());
        }
        return str;
    }

    public String renderNewComment(HttpServletRequest request, UserConfigDb ucd,
                                   TemplateDb td) throws ErrMsgException {
        String str = "<ul>";
        String leftsql = SQLBuilder.getNewReplySqlOfBlog(ucd.getId()); // "select id from sq_message where isBlog=1 and replyRootName=" + StrUtil.sqlstr(userName) + " order by lydate desc";
        MsgDb leftMsgDb = new MsgDb();
        cn.js.fan.db.ListResult leftlr = leftMsgDb.list(leftsql, 1, 10);
        UserMgr leftum = new UserMgr();
        if (leftlr != null) {
            Vector leftv = leftlr.getResult();
            Iterator leftir = leftv.iterator();
            while (leftir.hasNext()) {
                leftMsgDb = (MsgDb) leftir.next();
                if (leftMsgDb.getMsgDb(leftMsgDb.getRootid()).getCheckStatus() ==
                    MsgDb.CHECK_STATUS_PASS) {
                    str += "<li>";
                    str += "[";
                    if (leftMsgDb.getName().equals("")) {
                        str += cn.js.fan.web.SkinUtil.LoadString(request,
                                "res.label.forum.showtopic", "anonym");
                    } else {
                        str += "<a href=\"userinfo.jsp?username=" +
                                leftMsgDb.getName() + "\">" +
                                leftum.getUser(leftMsgDb.getName()).
                                getNick() + "</a>";

                    }
                    str += "]";
                    str += "&nbsp;&nbsp;&nbsp;";
                    str += "<a href=\"showblog.jsp?rootid=" +
                            leftMsgDb.getRootid() + "&op=allcomm#" +
                            leftMsgDb.getId() + "\">" +
                            DefaultRender.RenderFullTitle(request, leftMsgDb) +
                            "</a>";
                    str += "</li>";
                }
            }
        }
        str += "</ul>";
        return str;
    }

    public String renderSubListdayblog(HttpServletRequest request,
                                       UserConfigDb ucd,
                                       TemplateDb td) {
        // 取得显示的年月
        int year, month, day = 1;
        try {
            year = ParamUtil.getInt(request, "y");
            month = ParamUtil.getInt(request, "m");
            day = ParamUtil.getInt(request, "d");
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(cal.YEAR);
            month = cal.get(cal.MONTH) + 1;
        }

        UserBlog ub = new UserBlog(ucd.getId());
        Vector v = ub.getBlogDayList(year, month, day);
        Iterator ir = v.iterator();
        MsgDb msgdb;
        String str = "";
        TemplateLoader tl = null;
        try {
            tl = new TemplateLoader(request,
                                    getTemplateCacheKey(ucd,
                    TemplateDb.TEMPL_TYPE_SUB),
                                    getTemplateContent(ucd, "sub_content"));
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("renderSubListdayblog:" +
                                             e.getMessage());
        } while (ir.hasNext()) {
            msgdb = (MsgDb) ir.next();
            request.setAttribute("MsgDb", msgdb);

            str += tl.toString();
        }

        return str;
    }

    /**
     * 显示博客首页
     * @param request HttpServletRequest
     * @param ucd UserConfigDb
     * @param td TemplateDb
     * @return String
     */
    public String renderSubMyBlog(HttpServletRequest request, UserConfigDb ucd,
                                  TemplateDb td) {
        String blogUserDir = ParamUtil.get(request, "blogUserDir");
        String sql = SQLBuilder.getMyblogSql(blogUserDir, ucd.getId());

        MsgDb msgdb = new MsgDb();
        int total = msgdb.getThreadsCount(sql,
                                          msgdb.
                                          getVirtualBoardcodeOfBlog(ucd.
                getId(), blogUserDir));

        int pagesize = 20;
        int curpage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

        Paginator paginator = new Paginator(request, total, pagesize);
        long start = (curpage - 1) * pagesize;
        long end = curpage * pagesize;

        ThreadBlockIterator irmsg = msgdb.getThreads(sql,
                msgdb.getVirtualBoardcodeOfBlog(ucd.getId(), blogUserDir),
                start, end);

        StringBuffer sb = new StringBuffer(); ;
        TemplateLoader tl = null;
        try {
            tl = new TemplateLoader(request,
                                    getTemplateCacheKey(ucd,
                    TemplateDb.TEMPL_TYPE_SUB),
                                    getTemplateContent(ucd,
                    "sub_content"));
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("renderSubMyBlog:" +
                                             e.getMessage());
        } while (irmsg.hasNext()) {
            msgdb = (MsgDb) irmsg.next();
            request.setAttribute("MsgDb", msgdb);
            sb.append(tl.toString());
        }

        sb.append("<div class='pageBlock'>");
        sb.append(paginator.getPageStatics(request));
        sb.append("&nbsp;&nbsp;");
        sb.append(paginator.getPageBlock(request,
                                         "myblog.jsp?blogId=" + ucd.getId()));
        sb.append("</div>");

        return sb.toString();
    }

    public String renderListphotoStyle1(HttpServletRequest request,
                                        UserConfigDb ucd,
                                        TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}",
                                         "[<a href='listphoto.jsp?blogId=" +
                                         ucd.getId() +
                                         "'>相册</a>]&nbsp;&nbsp;[<a href='listphoto_catalog.jsp?blogId=" +
                                         ucd.getId() +
                                         "'>专辑</a>]&nbsp;&nbsp;[列表]");

        str = str.replaceFirst("\\{remark\\}", "");

        PhotoDb pd = new PhotoDb();
        int curpage = ParamUtil.getInt(request, "CPages", 1);
        String sql;
        sql = "select id from " + pd.getTableName() + " where blog_id=" +
              ucd.getId() + " ORDER BY addDate desc";

        long total = 0;
        int pagesize = 20;

        ListResult lr = null;
        try {
            lr = pd.listResult(sql, curpage, pagesize);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("renderSubListPhoto:" +
                                             e.getMessage());
            return "error";
        }
        if (lr != null)
            total = lr.getTotal();

        String block = "";
        Paginator paginator = new Paginator(request, total, pagesize);
        Iterator irphoto = null;
        if (lr != null)
            irphoto = lr.getResult().iterator();
        if (irphoto != null) {
            while (irphoto.hasNext()) {
                pd = (PhotoDb) irphoto.next();
                block += "<li><span style='float:right'>" +
                        ForumSkin.formatDateTime(request, pd.getAddDate()) +
                        "</span>";
                block += "<a href='showphoto.jsp?photoId=" +
                        pd.getId() + "'>" + pd.getTitle() + "</a></li>";
            }
        }

        block += "<div class='pageBlock'>";
        block += paginator.getPageBlock(request, "listphoto.jsp");
        block += "</div>";
        return str.replaceFirst("\\{list\\}", block);
    }

    public String renderListphotoCatalog(HttpServletRequest request,
                                         UserConfigDb ucd,
                                         TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}",
                                         "[<a href='listphoto.jsp?blogId=" +
                                         ucd.getId() +
                                         "'>相册</a>]&nbsp;&nbsp;[专辑]&nbsp;&nbsp;[<a href='listphoto_style1.jsp?blogId=" +
                                         ucd.getId() + "'>列表</a>]");

        str = str.replaceFirst("\\{remark\\}", "");

        PhotoCatalogDb pcd = new PhotoCatalogDb();

        String sql;
        sql = "select id from " + pcd.getTable().getName() + " where blog_id=" +
              ucd.getId() + " ORDER BY add_date desc";

        long total = pcd.getQObjectCount(sql, "" + ucd.getId());
        int pagesize = 20;
        int curpage = ParamUtil.getInt(request, "CPages", 1);

        QObjectBlockIterator obi = pcd.getQObjects(sql, "" + ucd.getId(),
                (curpage - 1) * pagesize, curpage * pagesize);

        StringBuffer block = new StringBuffer(); ;
        Paginator paginator = new Paginator(request, total, pagesize);

        block.append("<div>");
        while (obi.hasNext()) {
            pcd = (PhotoCatalogDb) obi.next();
            block.append("<div class=\"catalog_box\">");
            block.append("<div class=\"catalog_photo_box\">");
            if (!StrUtil.getNullStr(pcd.getString("miniature")).equals(""))
                block.append("<a href='showphoto_catalog.jsp?catalog=" +
                             pcd.getLong("id") + "'>" + "<img border=0 src=\"" +
                             Global.getRootPath() + "/upfile/" +
                             PhotoDb.photoBasePath + "/" +
                             pcd.getString("miniature") + "\"></a>");
            block.append("</div>");
            block.append("<div class=\"catalog_title\"><a href='showphoto_catalog.jsp?catalog=" +
                         pcd.getLong("id") + "'>" + pcd.getString("title"));
            // block.append(ForumSkin.formatDateTime(request,
            //                                      pcd.getDate("add_date")));
            block.append("</a></div>");
            block.append("<div class=\"catalog_count\">" + pcd.getInt("photo_count") +
                         "&nbsp;张照片</div>");
            block.append("</div>");
        }
        block.append("</div>");

        block.append("<div class='pageBlock'>");
        block.append(paginator.getPageBlock(request, "listphoto.jsp"));
        block.append("</div>");
        return str.replaceFirst("\\{list\\}", block.toString());
    }

    /**
     * 用特效显示
     * @param request HttpServletRequest
     * @param ucd UserConfigDb
     * @param td TemplateDb
     * @return String
     */
    public String renderListphoto(HttpServletRequest request,
                                  UserConfigDb ucd,
                                  TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}",
                                         "[相册]&nbsp;&nbsp;[<a href='listphoto_catalog.jsp?blogId=" +
                                         ucd.getId() +
                                         "'>专辑</a>]&nbsp;&nbsp;[<a href='listphoto_style1.jsp?blogId=" +
                                         ucd.getId() + "'>列表</a>]");
        str = str.replaceFirst("\\{remark\\}", "");

        PhotoDb pd = new PhotoDb();
        int curpage = ParamUtil.getInt(request, "CPages", 1);
        String sql;
        sql = "select id from " + pd.getTableName() + " where blog_id=" +
              ucd.getId() + " ORDER BY addDate desc";

        int pagesize = 4;

        ObjectBlockIterator qbi = pd.getObjects(sql, (curpage - 1) * pagesize,
                                                curpage * pagesize);

        String block = "";
        // Paginator paginator = new Paginator(request, total, pagesize);

        int k = 0;
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String attachmentBasePath = request.getContextPath() + "/upfile/" +
                                    pd.photoBasePath + "/";
        boolean hasAny = false;
        while (qbi.hasNext()) {
            pd = (PhotoDb) qbi.next();
            hasAny = true;
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

            k++;
        }
        if (hasAny) {
            block = "<div id=\"photoDiv\"></div>";
            block += "<script>ajaxpage('" + Global.getRootPath() +
                    "/blog/listphoto_render.jsp?blogId=" + ucd.getId() +
                    "', 'photoDiv');</script>";
        }
        return str.replaceFirst("\\{list\\}", block);
    }

    public String renderListmusic(HttpServletRequest request,
                                  UserConfigDb ucd,
                                  TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}", "音乐");
        str = str.replaceFirst("\\{remark\\}", "");

        MusicDb mud = new MusicDb();

        String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
        if (strcurpage.equals(""))
            strcurpage = "1";
        if (!StrUtil.isNumeric(strcurpage)) {
            return StrUtil.makeErrMsg(SkinUtil.LoadString(request, "err_id"));
        }

        String sql = "select id from " + mud.getTable().getName() + " where blog_id=" +
              ucd.getId() + " ORDER BY sort asc, add_date desc";

        long total = mud.getQObjectCount(sql, "" + ucd.getId());
        int pagesize = 20;
        int curpage = StrUtil.toInt(strcurpage, 1);

        QObjectBlockIterator obi = mud.getQObjects(sql, "" + ucd.getId(),
                (curpage - 1) * pagesize, curpage * pagesize);

        Paginator paginator = new Paginator(request, total, pagesize);
        String block = "";

        while (obi.hasNext()) {
            mud = (MusicDb) obi.next();
            block += "<li><span style='float:right'>" +
                    ForumSkin.formatDateTime(request, mud.getDate("add_date")) +
                    "</span><a href='showmusic.jsp?musicId=" +
                    mud.getLong("id") + "'>" + mud.getString("title") +
                    "</a></li>";
        }

        block += "<div class='pageBlock'>";
        block += paginator.getPageBlock(request, "listmusic.jsp");
        block += "</div>";
        return str.replaceFirst("\\{list\\}", block);
    }

    public String renderListvideo(HttpServletRequest request,
                                  UserConfigDb ucd,
                                  TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}", "视频");
        str = str.replaceFirst("\\{remark\\}", "");

        VideoDb mud = new VideoDb();

        String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
        if (strcurpage.equals(""))
            strcurpage = "1";
        if (!StrUtil.isNumeric(strcurpage)) {
            return StrUtil.makeErrMsg(SkinUtil.LoadString(request, "err_id"));
        }

        String sql;
        sql = "select id from " + mud.getTable().getName() + " where blog_id=" +
              ucd.getId() + " ORDER BY sort asc, add_date desc";

        long total = mud.getQObjectCount(sql, "" + ucd.getId());
        int pagesize = 20;
        int curpage = StrUtil.toInt(strcurpage, 1);

        QObjectBlockIterator obi = mud.getQObjects(sql, "" + ucd.getId(),
                (curpage - 1) * pagesize, curpage * pagesize);

        Paginator paginator = new Paginator(request, total, pagesize);
        String block = "";

        while (obi.hasNext()) {
            mud = (VideoDb) obi.next();
            block += "<li><span style='float:right'>" +
                    ForumSkin.formatDateTime(request, mud.getDate("add_date")) +
                    "</span><a href='showvideo.jsp?videoId=" +
                    mud.getLong("id") + "'>" + mud.getString("title") +
                    "</a></li>";
        }

        block += "<div class='pageBlock'>";
        block += paginator.getPageBlock(request, "listvideo.jsp");
        block += "</div>";
        return str.replaceFirst("\\{list\\}", block);
    }

    /**
     * 以专辑方式显示照片
     * @param request HttpServletRequest
     * @param ucd UserConfigDb
     * @param td TemplateDb
     * @return String
     */
    public String renderShowPhotoCatalog(HttpServletRequest request,
                                         UserConfigDb ucd,
                                         TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        long catalog = ParamUtil.getLong(request, "catalog", -1);
        if (catalog == -1)
            return "Error catalog";

        long photoId = ParamUtil.getLong(request, "photoId", -1);

        PhotoCatalogDb pcd = new PhotoCatalogDb();
        pcd = (PhotoCatalogDb) pcd.getQObjectDb(new Long(catalog));
        if (pcd==null) {
            return "The catalog is not valid.";
        }

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}",
                                         StrUtil.toHtml(pcd.getString("title")) +
                                         "&nbsp;&nbsp;&nbsp;&nbsp;创建日期：" +
                                         ForumSkin.
                                         formatDateTime(request,
                pcd.getDate("add_date")));

        str = str.replaceFirst("\\{remark\\}", "");

        PhotoDb pd = new PhotoDb();
        String sql;
        sql = "select id from " + pd.getTableName() + " where catalog=" +
              catalog + " ORDER BY addDate desc";

        // System.out.println(getClass() + " sql="+ sql);

        int total = (int) pcd.getQObjectCount(sql,
                                              PhotoDb.CACHE_CATALOG_PREFIX +
                                              catalog);

        ObjectBlockIterator obi = pd.getObjects(sql,
                                                PhotoDb.CACHE_CATALOG_PREFIX +
                                                catalog,
                                                0, total);

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();

        String attachmentBasePath = request.getContextPath() + "/upfile/" +
                                    PhotoDb.photoBasePath + "/";
        String ftpBasePath = "";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            ftpBasePath = cfg.getProperty("forum.ftpUrl");
            if (ftpBasePath.lastIndexOf("/") !=
                ftpBasePath.length() - 1)
                ftpBasePath += "/";
            ftpBasePath += PhotoDb.photoBasePath + "/";
        }

        if (!obi.hasNext()) {
            str = str.replaceFirst("\\{list\\}", "");
            return str;
        }

        if (photoId == -1) {
            pd = (PhotoDb) obi.next();
            photoId = pd.getId();
        } else {
            pd = pd.getPhotoDb(photoId);
            if (!pd.isLoaded()) {
                str = str.replaceFirst("\\{list\\}",
                                       "The photoId is not valid.");
                return str;
            }
        }

        String basePath = attachmentBasePath;
        if (pd.isRemote()) {
            basePath = ftpBasePath;
        }

        obi.setIndex(pd);

        StringBuffer block = new StringBuffer();
        block.append("<table class=\"catalog_table\" width=100%><tr><td valign=\"middle\">");

        if (obi.hasPrevious()) {
            PhotoDb prevPd = (PhotoDb) obi.previous();
            block.append("<img style=\"cursor:hand\" src=\"images/btn_left.gif\" onClick=\"window.location.href='showphoto_catalog.jsp?catalog=" + catalog + "&photoId=" + prevPd.getId() + "'\"></td><td>");
            obi.next();
        }

        block.append("<a href=\"showphoto.jsp?photoId=" + pd.getId() + "&blodId=" + pd.getBlogId() + "\" target=_blank title=\"点击在新窗口中打开\"><img border=0 src=\"" +
                     basePath + pd.getImage() + "\" onload=\"javascript:if(this.width>screen.width*0.4) this.width=screen.width*0.4\"></a>");
        block.append("</td><td valign=\"middle\">");

        if (obi.hasNext()) {
            PhotoDb nextPd = (PhotoDb) obi.next();
            block.append("<img style=\"cursor:hand\" src=\"images/btn_right.gif\" onClick=\"window.location.href='showphoto_catalog.jsp?catalog=" + catalog + "&photoId=" + nextPd.getId() + "'\">");
        }

        block.append("</td></tr></table>");

        str = str.replaceFirst("\\{list\\}", block.toString());

        StringBuffer sb = new StringBuffer();
        sb.append(str);

        sb.append("<div class=\"catalog_all_small\"><div>专辑中的照片(共" + pcd.getInt("photo_count")  + "张)：</div>");

        obi.beforeFirst();

        while (obi.hasNext()) {
            pd = (PhotoDb) obi.next();
            if (pd.getId()==photoId)
                sb.append("<div class=\"catalog_small_box_sel\">");
            else
                sb.append("<div class=\"catalog_small_box\">");
            basePath = attachmentBasePath;
            if (pd.isRemote()) {
                basePath = ftpBasePath;
            }
            sb.append("<a href=\"showphoto_catalog.jsp?photoId=" + pd.getId() + "&catalog=" + catalog + "\"><img border=0 src=\"" +
                      basePath + pd.getImageSmall() + "\"></a>");
            sb.append("</div>");
        }

        sb.append("</div>");

        return sb.toString();
    }

    public String renderShowphoto(HttpServletRequest request,
                                  UserConfigDb ucd,
                                  TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        boolean canDel = false;
        com.redmoon.blog.Privilege privilege = new com.redmoon.blog.Privilege();
        try {
            if (privilege.canUserDo(request, ucd.getId(),
                                    com.redmoon.blog.Privilege.PRIV_ALL)) {
                canDel = true;
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }
        int id = ParamUtil.getInt(request, "photoId", -1);
        if (id == -1) {
            return "error id";
        }
        PhotoDb pd = new PhotoDb();
        pd = pd.getPhotoDb(id);

        pd.hit(); // 增大点击

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}",
                                         StrUtil.toHtml(pd.getTitle()) +
                                         "&nbsp;&nbsp;&nbsp;&nbsp;点击：" +
                                         pd.getHit() +
                                         "&nbsp;&nbsp;&nbsp;&nbsp;上传日期：" +
                                         ForumSkin.
                                         formatDateTime(request, pd.getAddDate()));
        if (ucd.isPhotoDig())
            str = str.replaceFirst("\\{remark\\}",
                                   "评论：<a href='#' onClick=\"openWin('photo_dig.jsp?photoId=" +
                                   id + "&op=good', 550, 117)\">好评</a>&nbsp;&nbsp;<a href=# onClick=\"openWin('photo_dig.jsp?photoId=" +
                                   id +
                                   "&op=bad', 550, 117)\">差评</a>&nbsp;当前分值：" +
                                   pd.getScore());
        else
            str = str.replaceFirst("\\{remark\\}", "");
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String attachmentBasePath = request.getContextPath() + "/upfile/" +
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

        String block = "<a href=\"" + attachmentBasePath + pd.getImage() +
                       "\" target=_blank title=\"点击在新窗口中打开\"><img border=0 src=\"" +
                       attachmentBasePath + pd.getImage() + "\" onload=\"javascript:if(this.width>screen.width*0.4) this.width=screen.width*0.4\"></a>";

        StringBuffer sb = new StringBuffer();

        if (!pd.isLocked()) {
            String sql;
            PhotoCommentDb pcd = new PhotoCommentDb();

            sql = "select id from " + pcd.getTable().getName() +
                  " where photo_id=" +
                  pd.getId() + " ORDER BY add_date asc";

            long total = pcd.getQObjectCount(sql, "" + pd.getId());
            int pagesize = 20;
            int curpage = ParamUtil.getInt(request, "CPages", 1);

            QObjectBlockIterator obi = pcd.getQObjects(sql, "" + pd.getId(),
                    (curpage - 1) * pagesize, curpage * pagesize);

            Paginator paginator = new Paginator(request, total, pagesize);

            sb.append("<div class=comment>");
            UserMgr um = new UserMgr();
            while (obi.hasNext()) {
                pcd = (PhotoCommentDb) obi.next();
                sb.append("<div class=comment_t>");
                sb.append("用户：");
                String userName = StrUtil.getNullStr(pcd.getString("user_name"));
                if (!userName.equals("")) {
                    sb.append("<a target=_blank href='" + Global.getRootPath() +
                              "/blog/userinfo.jsp?username=" +
                              StrUtil.UrlEncode(userName) + "'>");
                    sb.append(um.getUser(userName).getNick());
                    sb.append("</a>");
                } else {
                    sb.append("匿名");
                }
                sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                sb.append(ForumSkin.formatDateTime(request,
                        pcd.getDate("add_date")));
                if (canDel)
                    sb.append(
                            "&nbsp;&nbsp;&nbsp;&nbsp;[<a href=\"javascript:if (confirm('" +
                            SkinUtil.LoadString(request, "confirm_del") +
                            "')) window.location.href='showphoto.jsp?op=delComment&cmtId=" +
                            pcd.getLong("id") + "&photoId=" + pd.getId() +
                            "'\">删除</a>]");
                sb.append("</div><div class=comment_c>");
                sb.append(pcd.getString("content"));
                sb.append("</div>");
            }
            sb.append("</div>");

            sb.append("<div class='pageBlock'>");
            sb.append(paginator.getPageBlock(request,
                                             "showphoto.jsp?photoId=" +
                                             pd.getId()));
            sb.append("</div>");

            sb.append(getAreaPhotoComment(request, id));
        }

        str = str.replaceFirst("\\{list\\}", block + sb.toString());

        return str;
    }

    public String renderShowmusic(HttpServletRequest request,
                                  UserConfigDb ucd,
                                  TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        int musicId = ParamUtil.getInt(request, "musicId", -1);
        if (musicId == -1) {
            return SkinUtil.LoadString(request, "err_id");
        }
        MusicDb mud = new MusicDb();
        mud = (MusicDb) mud.getQObjectDb(new Long(musicId));
        if (mud==null) {
            return SkinUtil.LoadString(request, "err_id");
        }
        String link = StrUtil.getNullStr(mud.getString("link"));

        String title = StrUtil.toHtml(mud.getString("title")) +
                       "&nbsp;&nbsp;&nbsp;&nbsp;" +
                       ForumSkin.formatDateTime(request, mud.getDate("add_date"));

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}", title);
        str = str.replaceFirst("\\{remark\\}", "");

        StringBuffer block = new StringBuffer(200);
        block.append("<object classid='clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95' id='MediaPlayer1' width='428' height='68'>");
        block.append("<param name='AudioStream' value='-1'>");
        block.append("<param name='AutoSize' value='0'>");
        block.append("<param name='AutoStart' value='-1'>");
        block.append("<param name='AnimationAtStart' value='-1'>");
        block.append("<param name='AllowScan' value='-1'>");
        block.append("<param name='AllowChangeDisplaySize' value='-1'>");
        block.append("<param name='AutoRewind' value='0'>");
        block.append("<param name='BufferingTime' value='5'>");
        block.append("<param name='ClickToPlay' value='-1'>");
        block.append("<param name='CursorType' value='0'>");
        block.append("<param name='CurrentPosition' value='-1'>");
        block.append("<param name='CurrentMarker' value='0'>");
        block.append("<param name='DisplayBackColor' value='0'>");
        block.append("<param name='DisplayForeColor' value='16777215'>");
        block.append("<param name='DisplayMode' value='0'>");
        block.append("<param name='DisplaySize' value='2'>");
        block.append("<param name='Enabled' value='-1'>");
        block.append("<param name='EnableContextMenu' value='-1'>");
        block.append("<param name='EnablePositionControls' value='-1'>");
        block.append("<param name='EnableFullScreenControls' value='0'>");
        block.append("<param name='EnableTracker' value='-1'>");
        block.append("<param name='Filename' value='" +
                     (link.equals("") ? mud.getMusicUrl(request) : link) + "'>");
        block.append("<param name='InvokeURLs' value='-1'>");
        block.append("<param name='Language' value='-1'>");
        block.append("<param name='PlayCount' value='1'>");
        block.append("<param name='PreviewMode' value='0'>");
        block.append("<param name='Rate' value='1'>");
        block.append("<param name='SelectionStart' value='-1'>");
        block.append("<param name='SelectionEnd' value='-1'>");
        block.append("<param name='SendOpenStateChangeEvents' value='-1'>");
        block.append("<param name='SendWarningEvents' value='-1'>");
        block.append("<param name='SendErrorEvents' value='-1'>");
        block.append("<param name='SendKeyboardEvents' value='0'>");
        block.append("<param name='SendMouseClickEvents' value='0'>");
        block.append("<param name='SendMouseMoveEvents' value='0'>");
        block.append("<param name='SendPlayStateChangeEvents' value='-1'>");
        block.append("<param name='ShowCaptioning' value='0'>");
        block.append("<param name='ShowControls' value='-1'>");
        block.append("<param name='ShowAudioControls' value='-1'>");
        block.append("<param name='ShowDisplay' value='0'>");
        block.append("<param name='ShowGotoBar' value='0'>");
        block.append("<param name='ShowPositionControls' value='-1'>");
        block.append("<param name='ShowStatusBar' value='-1'>");
        block.append("<param name='ShowTracker' value='-1'>");
        block.append("<param name='TransparentAtStart' value='0'>");
        block.append("<param name='VideoBorderWidth' value='0'>");
        block.append("<param name='VideoBorderColor' value='0'>");
        block.append("<param name='VideoBorder3D' value='0'>");
        block.append("<param name='Volume' value='-40'>");
        block.append("<param name='WindowlessVideo' value='0'>");
        block.append("</object>");

        boolean canDel = false;
        com.redmoon.blog.Privilege privilege = new com.redmoon.blog.Privilege();
        
        if (!Privilege.isUserLogin(request)) {
        	block.append("<div class='warn_login' style='margin-top:5px'>登录后才能听本站歌曲!</div>");
        }
        
        try {
            if (privilege.canUserDo(request, ucd.getId(),
                                    com.redmoon.blog.Privilege.PRIV_ALL)) {
                canDel = true;
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }

        String sql;
        MusicCommentDb mcd = new MusicCommentDb();

        sql = "select id from " + mcd.getTable().getName() +
              " where music_id=" +
              mud.getLong("id") + " ORDER BY add_date asc";

        long total = mcd.getQObjectCount(sql, "" + mud.getLong("id"));
        int pagesize = 20;
        int curpage = ParamUtil.getInt(request, "CPages", 1);

        QObjectBlockIterator obi = mcd.getQObjects(sql, "" + mud.getLong("id"),
                                                   (curpage - 1) * pagesize,
                                                   curpage * pagesize);

        Paginator paginator = new Paginator(request, total, pagesize);

        block.append("<div class=comment>");
        UserMgr um = new UserMgr();
        while (obi.hasNext()) {
            mcd = (MusicCommentDb) obi.next();
            block.append("<div class=comment_t>");
            block.append("用户：");
            String userName = StrUtil.getNullStr(mcd.getString("user_name"));
            if (!userName.equals("")) {
                block.append("<a target=_blank href='" + Global.getRootPath() +
                             "/blog/userinfo.jsp?username=" +
                             StrUtil.UrlEncode(userName) + "'>");
                block.append(um.getUser(userName).getNick());
                block.append("</a>");
            } else {
                block.append("匿名");
            }
            block.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            block.append(ForumSkin.formatDateTime(request,
                                                  mcd.getDate("add_date")));
            if (canDel)
                block.append(
                        "&nbsp;&nbsp;&nbsp;&nbsp;[<a href=\"javascript:if (confirm('" +
                        SkinUtil.LoadString(request, "confirm_del") +
                        "')) window.location.href='showmusic.jsp?op=delComment&cmtId=" +
                        mcd.getLong("id") + "&musicId=" + mud.getLong("id") +
                        "'\">删除</a>]");
            block.append("</div><div class=comment_c>");
            block.append(mcd.getString("content"));
            block.append("</div>");
        }
        block.append("</div>");

        block.append("<div class='pageBlock'>");
        block.append(paginator.getPageBlock(request,
                                            "showphoto.jsp?id=" +
                                            mud.getLong("id")));
        block.append("</div>");

        if (mud.getInt("is_locked")==0) {
            block.append(getAreaMusicComment(request, musicId));
        }

        return str.replaceFirst("\\{list\\}", block.toString());
    }

    public String renderShowvideo(HttpServletRequest request,
                                  UserConfigDb ucd,
                                  TemplateDb td) {
        String common = getTemplateContent(ucd, "common_content");

        int videoId = ParamUtil.getInt(request, "videoId", -1);
        if (videoId == -1) {
            return SkinUtil.LoadString(request, "err_id");
        }
        VideoDb mud = new VideoDb();
        mud = (VideoDb) mud.getQObjectDb(new Long(videoId));
        if (mud==null) {
            return SkinUtil.LoadString(request, "err_id");
        }
        String link = StrUtil.getNullStr(mud.getString("link"));

        String title = StrUtil.toHtml(mud.getString("title")) +
                       "&nbsp;&nbsp;&nbsp;&nbsp;" +
                       ForumSkin.formatDateTime(request, mud.getDate("add_date"));

        // 替换其它标签
        String str = common.replaceFirst("\\{title\\}", title);
        str = str.replaceFirst("\\{remark\\}", "");

        StringBuffer block = new StringBuffer(200);
        block.append("<object classid='clsid:22D6F312-B0F6-11D0-94AB-0080C74C7E95' id='MediaPlayer1' width='428' height='370'>");
        block.append("<param name='AudioStream' value='-1'>");
        block.append("<param name='AutoSize' value='0'>");
        block.append("<param name='AutoStart' value='-1'>");
        block.append("<param name='AnimationAtStart' value='-1'>");
        block.append("<param name='AllowScan' value='-1'>");
        block.append("<param name='AllowChangeDisplaySize' value='-1'>");
        block.append("<param name='AutoRewind' value='0'>");
        block.append("<param name='BufferingTime' value='5'>");
        block.append("<param name='ClickToPlay' value='-1'>");
        block.append("<param name='CursorType' value='0'>");
        block.append("<param name='CurrentPosition' value='-1'>");
        block.append("<param name='CurrentMarker' value='0'>");
        block.append("<param name='DisplayBackColor' value='0'>");
        block.append("<param name='DisplayForeColor' value='16777215'>");
        block.append("<param name='DisplayMode' value='0'>");
        block.append("<param name='DisplaySize' value='2'>");
        block.append("<param name='Enabled' value='-1'>");
        block.append("<param name='EnableContextMenu' value='-1'>");
        block.append("<param name='EnablePositionControls' value='-1'>");
        block.append("<param name='EnableFullScreenControls' value='0'>");
        block.append("<param name='EnableTracker' value='-1'>");
        block.append("<param name='Filename' value='" +
                     (link.equals("") ? mud.getVideoUrl(request) : link) + "'>");
        block.append("<param name='InvokeURLs' value='-1'>");
        block.append("<param name='Language' value='-1'>");
        block.append("<param name='PlayCount' value='1'>");
        block.append("<param name='PreviewMode' value='0'>");
        block.append("<param name='Rate' value='1'>");
        block.append("<param name='SelectionStart' value='-1'>");
        block.append("<param name='SelectionEnd' value='-1'>");
        block.append("<param name='SendOpenStateChangeEvents' value='-1'>");
        block.append("<param name='SendWarningEvents' value='-1'>");
        block.append("<param name='SendErrorEvents' value='-1'>");
        block.append("<param name='SendKeyboardEvents' value='0'>");
        block.append("<param name='SendMouseClickEvents' value='0'>");
        block.append("<param name='SendMouseMoveEvents' value='0'>");
        block.append("<param name='SendPlayStateChangeEvents' value='-1'>");
        block.append("<param name='ShowCaptioning' value='0'>");
        block.append("<param name='ShowControls' value='-1'>");
        block.append("<param name='ShowAudioControls' value='-1'>");
        block.append("<param name='ShowDisplay' value='0'>");
        block.append("<param name='ShowGotoBar' value='0'>");
        block.append("<param name='ShowPositionControls' value='-1'>");
        block.append("<param name='ShowStatusBar' value='-1'>");
        block.append("<param name='ShowTracker' value='-1'>");
        block.append("<param name='TransparentAtStart' value='0'>");
        block.append("<param name='VideoBorderWidth' value='0'>");
        block.append("<param name='VideoBorderColor' value='0'>");
        block.append("<param name='VideoBorder3D' value='0'>");
        block.append("<param name='Volume' value='-40'>");
        block.append("<param name='WindowlessVideo' value='0'>");
        block.append("</object>");

        if (!Privilege.isUserLogin(request)) {
        	block.append("<div class='warn_login' style='margin-top:5px'>登录后才能看本站视频!</div>");
        }
        
        boolean canDel = false;
        com.redmoon.blog.Privilege privilege = new com.redmoon.blog.Privilege();
        try {
            if (privilege.canUserDo(request, ucd.getId(),
                                    com.redmoon.blog.Privilege.PRIV_ALL)) {
                canDel = true;
            }
        } catch (ErrMsgException e) {
            e.printStackTrace();
        }

        String sql;
        VideoCommentDb mcd = new VideoCommentDb();

        sql = "select id from " + mcd.getTable().getName() +
              " where video_id=" +
              mud.getLong("id") + " ORDER BY add_date asc";

        long total = mcd.getQObjectCount(sql, "" + mud.getLong("id"));
        int pagesize = 20;
        int curpage = ParamUtil.getInt(request, "CPages", 1);

        QObjectBlockIterator obi = mcd.getQObjects(sql, "" + mud.getLong("id"),
                                                   (curpage - 1) * pagesize,
                                                   curpage * pagesize);

        Paginator paginator = new Paginator(request, total, pagesize);

        block.append("<div class=comment>");
        UserMgr um = new UserMgr();
        while (obi.hasNext()) {
            mcd = (VideoCommentDb) obi.next();
            block.append("<div class=comment_t>");
            block.append("用户：");
            String userName = StrUtil.getNullStr(mcd.getString("user_name"));
            if (!userName.equals("")) {
                block.append("<a target=_blank href='" + Global.getRootPath() +
                             "/blog/userinfo.jsp?username=" +
                             StrUtil.UrlEncode(userName) + "'>");
                block.append(um.getUser(userName).getNick());
                block.append("</a>");
            } else {
                block.append("匿名");
            }
            block.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            block.append(ForumSkin.formatDateTime(request,
                                                  mcd.getDate("add_date")));
            if (canDel)
                block.append(
                        "&nbsp;&nbsp;&nbsp;&nbsp;[<a href=\"javascript:if (confirm('" +
                        SkinUtil.LoadString(request, "confirm_del") +
                        "')) window.location.href='showvideo.jsp?op=delComment&cmtId=" +
                        mcd.getLong("id") + "&videoId=" + mud.getLong("id") +
                        "'\">删除</a>]");
            block.append("</div><div class=comment_c>");
            block.append(mcd.getString("content"));
            block.append("</div>");
        }
        block.append("</div>");

        block.append("<div class='pageBlock'>");
        block.append(paginator.getPageBlock(request,
                                            "showvideo.jsp?id=" +
                                            mud.getLong("id")));
        block.append("</div>");

        if (mud.getInt("is_locked")==0) {
            block.append(getAreaVideoComment(request, videoId));
        }

        return str.replaceFirst("\\{list\\}", block.toString());
    }

    public String toString(HttpServletRequest request, List params) {
        TemplateDb td = (TemplateDb) request.getAttribute("template");
        UserConfigDb ucd = (UserConfigDb) request.getAttribute("UserConfigDb");

        HttpServletResponse response = (HttpServletResponse) request.
                                       getAttribute(
                                               "response");
        JspWriter out = (JspWriter) request.getAttribute("out");

        if (field.equals("main")) {
            // 根据request中attribute传过来的pageName参数，处理副模板
            String pageName = (String) request.getAttribute("pageName");
            if (pageName.equals("myblog")) {
                return renderSubMyBlog(request, ucd, td);
            } else if (pageName.equals("listdayblog")) {
                return renderSubListdayblog(request, ucd, td);
            } else if (pageName.equals("showblog")) {
                return renderSubShowblog(request, ucd, td);
            } else if (pageName.equals("listphoto")) {
                return renderListphoto(request, ucd, td);
            } else if (pageName.equals("listphoto_style1")) {
                return renderListphotoStyle1(request, ucd, td);
            } else if (pageName.equals("listphoto_catalog")) {
                return renderListphotoCatalog(request, ucd, td);
            } else if (pageName.equals("showphoto")) {
                return renderShowphoto(request, ucd, td);
            } else if (pageName.equals("showphoto_catalog")) {
                return renderShowPhotoCatalog(request, ucd, td);
            } else if (pageName.equals("listmusic")) {
                return renderListmusic(request, ucd, td);
            } else if (pageName.equals("showmusic")) {
                return renderShowmusic(request, ucd, td);
            } else if (pageName.equals("listvideo")) {
                return renderListvideo(request, ucd, td);
            } else if (pageName.equals("showvideo")) {
                return renderShowvideo(request, ucd, td);
            } else
                return "";
        } else if (field.equals("login")) {
            // return "<iframe marginwidth=\"0\" src=\"iframe_login.jsp\" frameborder=\"0\" width=\"181\" scrolling=\"no\"></iframe>";
            return NetUtil.gather(request, "utf-8",
                                  "http://" + request.getServerName() + ":" +
                                  request.getServerPort() +
                                  request.getContextPath() +
                                  "/blog/iframe_login.jsp");
        } else if (field.equals("calendar")) {
            return renderCalendar(request, ucd);
        } else if (field.equals("comment")) {
            String str = "";
            try {
                str = renderNewComment(request, ucd, td);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("toString1:" + e.getMessage());
            }
            return str;
        } else if (field.equals("newArticle")) {
            String str = "<ul>";
            MsgDb leftMsgDb = new MsgDb();
            String leftsql = SQLBuilder.getNewMsgOfBlog(ucd.getId());
            ThreadBlockIterator leftirmsg = leftMsgDb.getThreads(leftsql,
                    leftMsgDb.getVirtualBoardcodeOfBlog(ucd.getId(), ""), 0, 10);
            while (leftirmsg.hasNext()) {
                leftMsgDb = (MsgDb) leftirmsg.next();
                str += "<li><a href=\"showblog.jsp?rootid=" + leftMsgDb.getId() +
                        "\" title=" +
                        leftMsgDb.getTitle() +
                        "\">" +
                        DefaultRender.RenderTitle(request, leftMsgDb, 26) +
                        "</a></li>";
            }
            str += "</ul>";
            return str;
        } else if (field.equals("dir")) {
            String str = "<ul>";
            str += "<li><a href=\"" + request.getContextPath() +
                    "/blog/myblog.jsp?blogId=" + ucd.getId() + "\">" +
                    SkinUtil.
                    LoadString(request, "res.label.blog.left", "all_article") +
                    "</a></li>";
            str += "<li><a href=\"" + request.getContextPath() +
                    "/blog/myblog.jsp?blogUserDir=" + UserDirDb.DEFAULT +
                    "&blogId=" + ucd.getId() + "\">" + UserDirDb.getDefaultName() +
                    "</a></li>";
            str += "</ul>";

            UserDirDb sb1 = new UserDirDb();
            Vector leftv = sb1.list(ucd.getId());
            Iterator leftir = leftv.iterator();
            while (leftir.hasNext()) {
                UserDirDb as = (UserDirDb) leftir.next();
                str += "<li><a href=\"" + request.getContextPath() +
                        "/blog/myblog.jsp?blogId=" + ucd.getId() +
                        "&blogUserDir=" +
                        StrUtil.UrlEncode(as.getCode()) + "\">" +
                        StrUtil.toHtml(as.getDirName()) + "</a></li>";
            }
            return str;
        } else if (field.equals("link")) { // 友情链接
            String str = "<ul>";
            LinkDb leftld = new LinkDb();
            String listsql = "select id from " + leftld.getTableName() +
                             " where blog_id=" + ucd.getId() + " order by sort";
            Iterator leftirlink = leftld.list(listsql).iterator();
            while (leftirlink.hasNext()) {
                leftld = (LinkDb) leftirlink.next();
                str += "<li><a target=\"_blank\" href=\"" + leftld.getUrl() + "\" title=\"" + StrUtil.toHtml(leftld.getTitle()) + "\">";
                if (leftld.getImage() != null && !leftld.getImage().equals("")) {
                    str += "<img src=\"" + leftld.getImageUrl(request) +
                            "\" border=0>";
                } else {
                    str += StrUtil.toHtml(leftld.getTitle());
                }
                str += "</a></li>";
            }
            str += "</ul>";
            return str;
        } else if (field.equals("info")) { // 统计信息
            String str = "<ul>";
            str += "<li>" +
                    SkinUtil.LoadString(request, "res.label.blog.left",
                                        "article");
            str += ucd.getMsgCount() + "</li>";
            str += "<li>" +
                    SkinUtil.LoadString(request, "res.label.blog.left",
                                        "comment");
            str += ucd.getReplyCount() + "</li>";
            str += "<li>" +
                    SkinUtil.LoadString(request, "res.label.blog.left", "visit");
            str += ucd.getViewCount() + "</li>";
            str += "</ul>";
            return str;
        } else if (field.equals("photo")) {
            PhotoDb pd = new PhotoDb();
            String sql = "select id from " + pd.getTableName() +
                         " where blog_id=" +
                         ucd.getId() + " ORDER BY addDate desc";
            String str = "";
            ListResult lr = null;
            try {
                lr = pd.listResult(sql, 1, 10);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("toString2:" + e.getMessage());
            }
            Iterator irphoto = lr.getResult().iterator();
            while (irphoto.hasNext()) {
                pd = (PhotoDb) irphoto.next();
                str += "<li><a href='showphoto.jsp?photoId=" + pd.getId() +
                        "'>" +
                        StrUtil.toHtml(pd.getTitle()) + "</a></li>";
            }
            return str;
        } else if (field.equals("user")) {
            String str = "<div class=user>";
            UserDb leftUser = new UserDb();
            leftUser = leftUser.getUser(ucd.getUserName());
            if (ucd.getType() == UserConfigDb.TYPE_PERSON) {
                str += "<a target=_blank href=\"userinfo.jsp?username=" +
                        StrUtil.UrlEncode(ucd.getUserName()) + "\">" +
                        ucd.getPenName() + "</a>";
                str += "<br />";

                if (ucd.getIcon().equals("")) {
                    String myface = leftUser.getMyface();
                    String RealPic = leftUser.getRealPic();
                    if (myface.equals("")) {
                        str += "<img src=\"../forum/images/face/" + RealPic +
                                "\" />";
                    } else {
                        str += "<img src=\"" + leftUser.getMyfaceUrl(request) +
                                "\" />";
                    }
                } else {
                    str += "<img src=\"" + ucd.getIconUrl(request) + "\" />";
                }
                str += "<BR />";
                str += "<a href=\"user/myfriend.jsp?op=add&friend=" +
                        StrUtil.UrlEncode(leftUser.getName()) + "&privurl=" +
                        StrUtil.getUrl(request) + "\">";
                str +=
                        SkinUtil.LoadString(request,
                                            "res.label.forum.showtopic",
                                            "add_friend") +
                        "</a>";
            } else {
                // System.out.println(ucd.getIcon() + "---" + ucd.getIconUrl(request));
                if (!ucd.getIcon().equals("")) {
                    str += "<img src=\"" + ucd.getIconUrl(request) +
                            "\" /><BR />";
                }
                str +=
                        SkinUtil.LoadString(request,
                                            "res.label.blog.user.userconfig",
                                            "type_group") + "(<a href=\"" +
                        request.getContextPath() +
                        "/blog/blog_group_apply.jsp?blog_id=" + ucd.getId() +
                        "\">加入</a>)<br />";
                str +=
                        SkinUtil.LoadString(request, "res.label.blog.left",
                                            "creator");
                str += "：<a href=\"" + request.getContextPath() +
                        "/blog/userinfo.jsp?username=" +
                        StrUtil.UrlEncode(leftUser.getName()) +
                        "\" target=_blank>" +
                        leftUser.getNick() + "</a>";
            }
            str += "</div>";

            return str;
        } else if (field.equals("friend")) {
            String sql = "select id from sq_friend where name=" +
                         StrUtil.sqlstr(ucd.getUserName()) +
                         " order by rq desc";
            UserFriendDb ufd = new UserFriendDb();
            ListResult lr = null;
            try {
                lr = ufd.listResult(sql, 1, 200);
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("toString3:" + e.getMessage());
            }
            StringBuffer str = new StringBuffer();
            str.append("<ul>");

            Iterator ir = lr.getResult().iterator();
            UserMgr um = new UserMgr();
            while (ir.hasNext()) {
                ufd = (UserFriendDb) ir.next();
                UserDb ud = um.getUser(ufd.getFriend());

                UserConfigDb fucd = ucd.getUserConfigDbByUserName(ud.getName());
                if (fucd == null)
                    continue;
                str.append("<li>");
                String RealPic = StrUtil.getNullString(ud.getRealPic());
                String myface = StrUtil.getNullString(ud.getMyface());
                if (myface.equals("")) {
                    str.append("<img src=\"" + Global.getRootPath() +
                               "/forum/images/face/" + RealPic +
                               "\" width=16 height=16>");
                } else {
                    str.append("<img src=\"" + ud.getMyfaceUrl(request) +
                               "\" width=16 height=16>");
                }
                str.append(
                        "&nbsp;<a target=\"_blank\" href=\"userinfo.jsp?username=" +
                        StrUtil.UrlEncode(ud.getName()) + "\">" + ud.getNick() +
                        "</a>");
                str.append("</li>");
            }
            str.append("</ul>");
            return str.toString();
        } else if (field.equals("nav")) {
            Privilege pvg = new Privilege();
            String rootpath = request.getContextPath();
            UserMgr um = new UserMgr();
            StringBuffer str = new StringBuffer(200);
            str.append("<li><a href=" + rootpath + "/blog/myblog.jsp?blogId=" +
                       ucd.getId() + ">");
            str.append(SkinUtil.LoadString(request, "res.label.blog.header",
                                           "return_column"));
            str.append("</a></li>");
            str.append("<li><a href=\"#\" onClick=\"hopenWin('" + rootpath +
                       "/message/send.jsp?receiver=" +
                       StrUtil.UrlEncode(um.getUser(ucd.getUserName()).getNick()) +
                       "',320,260)\">");
            str.append(SkinUtil.LoadString(request, "res.label.blog.header",
                                           "message") +
                       "</a></li>");
            str.append("<li><a href=\"" + rootpath +
                       "/blog/listphoto.jsp?blogId=" +
                       ucd.getId() + "\">" +
                       SkinUtil.LoadString(request, "res.label.blog.header",
                                           "album") + "</a></li>");
            str.append("<li><a href=\"" + rootpath +
                       "/blog/listmusic.jsp?blogId=" +
                       ucd.getId() + "\">" +
                       SkinUtil.LoadString(request, "res.label.blog.user.photo",
                                           "music") + "</a></li>");
            str.append("<li><a href=\"" + rootpath +
                       "/blog/listvideo.jsp?blogId=" +
                       ucd.getId() + "\">" +
                       SkinUtil.LoadString(request, "res.label.blog.user.photo",
                                           "video") + "</a></li>");
            /*
             str.append("<li><a href=\"" + rootpath + "/blog/listfriend.jsp?blogId=" +
                    ucd.getId() + "\">" +
                    SkinUtil.LoadString(request,
                                        "res.label.blog.user.userconfig",
                                        "friends") + "</a></li>");
             */
            str.append("<li><a href=\"" + rootpath +
                       "/blog/user/frame.jsp?blogId=" +
                       ucd.getId() + "\">" +
                       SkinUtil.LoadString(request, "res.label.blog.header",
                                           "manage") + "</a></li>");
            if (pvg.isUserLogin(request)) {
                str.append("<li><a href=\"exit.jsp?privurl=" +
                           StrUtil.getUrl(request) + "\">" +
                           SkinUtil.LoadString(request, "res.label.blog.header",
                                               "exit") + "</a></li>");
            } else {
                str.append("<li><a href=\"door.jsp?privurl=" +
                           StrUtil.getUrl(request) + "\">" +
                           SkinUtil.LoadString(request, "res.label.blog.header",
                                               "login") + "</a></li>");
            }
            str.append("<li><a href=\"" + rootpath + "/blog/index.jsp\">" +
                       SkinUtil.LoadString(request, "res.label.blog.header",
                                           "blog_first_page") + "</a></li>");
            return str.toString();
        } else if (field.equals("rss")) {
            String blogUserDir = ParamUtil.get(request, "blogUserDir");
            return "<a title='RSS' href='" + request.getContextPath() +
                    "/forum/rss.jsp?op=blog&blogUserDir=" + blogUserDir +
                    "&blogId=" + ucd.getId() + "'><img src='" +
                    request.getContextPath() + "/images/rss.gif' border=0></a>";
        } else {
            props.put("htmlencode", "y");
            BeanUtil bu = new BeanUtil();
            Object obj = bu.getProperty(ucd, field);
            return format(obj, props);
        }
    }

    public String getAreaPhotoComment(HttpServletRequest request, long photoId) {
        // String querystring = StrUtil.getNullString(request.getQueryString());

        request.getServerName();

        String path = "http://" + request.getServerName() + ":" +
                      request.getServerPort() + request.getContextPath() +
                      "/blog/area_photo_comment.jsp?photoId=" + photoId;
        return NetUtil.gather(request, "utf-8", path);
    }

    public String getAreaMusicComment(HttpServletRequest request, long id) {
        // String querystring = StrUtil.getNullString(request.getQueryString());
        String path = "http://" + request.getServerName() + ":" +
                      request.getServerPort() + request.getContextPath() +
                      "/blog/area_music_comment.jsp?musicId=" + id;
        return NetUtil.gather(request, "utf-8", path);
    }

    public String getAreaVideoComment(HttpServletRequest request, long id) {
        // String querystring = StrUtil.getNullString(request.getQueryString());

        request.getServerName();

        String path = "http://" + request.getServerName() + ":" +
                      request.getServerPort() + request.getContextPath() +
                      "/blog/area_video_comment.jsp?videoId=" + id;
        return NetUtil.gather(request, "utf-8", path);
    }

    public String getAreaReply(HttpServletRequest request, long rootid) {
        String querystring = StrUtil.getNullString(request.getQueryString());
        String privurl = request.getRequestURL() + "?" +
                         StrUtil.UrlEncode(querystring, "utf-8");

        String path = "http://" + request.getServerName() + ":" +
                      request.getServerPort() + request.getContextPath() +
                      "/blog/area_reply.jsp?rootid=" +
                      rootid + "&privurl=" + privurl;
        return NetUtil.gather(request, "utf-8", path);
    }
}
