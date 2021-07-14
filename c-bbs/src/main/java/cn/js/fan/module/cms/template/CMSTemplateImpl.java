package cn.js.fan.module.cms.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.module.cms.*;
import cn.js.fan.module.cms.site.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import cn.js.fan.module.nav.LinkDb;
import com.cloudwebsoft.framework.util.*;

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
public class CMSTemplateImpl extends VarPart {
    public CMSTemplateImpl() {
    }

    public String renderScrollImg(HttpServletRequest request, List params) {
        int w = StrUtil.toInt((String)props.get("w"), 121);
        String h = (String)props.get("h");
        if (h==null)
            h = "116";

        String speed = (String)props.get("speed");
        if (speed==null)
            speed = "30";

        boolean isTitle = StrUtil.getNullStr((String)props.get("title")).equals("y");

        SiteScrollImgDb ssid = new SiteScrollImgDb();
        Vector v = ssid.listOfSite(Leaf.ROOTCODE, SiteScrollImgDb.KIND_SCROLL);

        StringBuffer str = new StringBuffer();
        str.append("<DIV id=scrollImgDiv style='OVERFLOW: hidden; WIDTH: 100%;'>");
        str.append("<TABLE cellSpacing=0 cellPadding=0 align=left border=0 cellspace='0'>");
        str.append("<TBODY>");
        str.append("<TR>");
        str.append("<TD id=scrollImgTd1 vAlign=top>");
        str.append("<table width='" + (v.size()*w+100) + "' height='" + h + "'  border='0' cellpadding='0' cellspacing='0'>");
        str.append("<tr>");

        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            ssid = (SiteScrollImgDb)ir.next();
            str.append("<td width='" + w + "'>");
            str.append("<div align='center'><a target=_blank href='" + StrUtil.getNullStr(ssid.getString("link")) + "'><img border=0 width='" + w + "' height='" + h + "' alt='" + ssid.getString("title") + "' src='" + ssid.getString("url") + "' style='margin-bottom:3px'></a>");
            if (isTitle) {
                str.append("<BR>" + format(StrUtil.getNullStr(ssid.getString("title")), props));
            }
            str.append("</div>");
            str.append("</td>");
        }

        str.append("</tr></table></TD><TD id=scrollImgTd2 vAlign=top>&nbsp;</TD></TR></TBODY></TABLE></DIV>");
        str.append("<SCRIPT>\n");
        str.append("var speed3=" + speed + ";\n");
        str.append("scrollImgTd2.innerHTML=scrollImgTd1.innerHTML;\n");
        str.append("function Marquee(){\n");
        str.append("if(scrollImgTd2.offsetWidth-scrollImgDiv.scrollLeft<=0)\n");
        str.append("scrollImgDiv.scrollLeft-=scrollImgTd1.offsetWidth;\n");
        str.append("else{\n");
        str.append("scrollImgDiv.scrollLeft++;\n");
        str.append("}\n");
        str.append("}\n");
        str.append("var MyMar=setInterval(Marquee,speed3);\n");
        str.append("scrollImgDiv.onmouseover=function() {clearInterval(MyMar)}\n");
        str.append("scrollImgDiv.onmouseout=function() {MyMar=setInterval(Marquee,speed3)}\n");
        str.append("</SCRIPT>");

        return str.toString();
    }

    public String renderFlashImage(HttpServletRequest request, List params) {
        String strId = (String) props.get("id");
        int id = StrUtil.toInt(strId, -1);
        if (id == -1)
            return "The id of AdFlashImage is invalid(id=" + strId + ").";
        SiteFlashImageDb sfid = new SiteFlashImageDb();
        sfid = (SiteFlashImageDb) sfid.getQObjectDb(new Integer(id));
        if (sfid == null) {
            return "Ad Flash Image is null where id=" + id;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<div id='fs" + id + "' class='focus'>\n");
        sb.append("    <div id='fsPic" + id + "' class='fPic'>\n");
        for (int i = 1; i <= 5; i++) {
            sb.append("        <div class='fcon' style='display: none;'>\n");
            sb.append("            <a target='_blank' href='" + StrUtil.getNullStr(sfid.getString("link" + i)) + "'><img src='" + StrUtil.getNullStr(sfid.getString("url" + i)) + "' style='opacity: 1;'></a>\n");
            sb.append("            <span class='shadow'><a target='_blank' href='" + StrUtil.getNullStr(sfid.getString("link" + i)) + "'>" + StrUtil.getNullStr(sfid.getString("title" + i)) + "</a></span>\n");
            sb.append("        </div>\n");
        }
        sb.append("    </div>\n");
        sb.append("    <div class='fbg'>\n");
        sb.append("         <div class='fs-btn' id='fsBtn" + id + "'>\n");
        for (int i = 1; i <= 5; i++) {
            sb.append("         <a href='javascript:void(0)' hidefocus='true' target='_self' class=''><i>" + i + "</i></a>\n");
        }
        sb.append("         </div>\n");
        sb.append("     </div>\n");
        sb.append("</div>\n");
        sb.append("<script type='text/javascript'>\n");
        sb.append("        Qfast.add('widgets', { path: '" + request.getContextPath() + "/js/terminator2.2.min.js', type: 'js', requires: ['fx'] });\n");
        sb.append("        Qfast(false, 'widgets', function () {\n");
        sb.append("           K.tabs({\n");
        sb.append("                    id: 'fs" + id + "',   // 焦点图包裹id\n");
        sb.append("                    conId: 'fsPic" + id + "',  // 大图域包裹id\n");
        sb.append("                    tabId:'fsBtn" + id + "',\n");
        sb.append("                    tabTn:'a',\n");
        sb.append("                    conCn: '.fcon', // 大图域配置class\n");
        sb.append("                    auto: 1,   //自动播放 1或0\n");
        sb.append("                    effect: 'fade',   // 效果配置\n");
        sb.append("                    eType: 'click', // 鼠标事件\n");
        sb.append("                    pageBt:true,//是否有按钮切换页码\n");
        sb.append("                    bns: ['.prev', '.next'],// 前后按钮配置class\n");
        sb.append("                    interval: 5000  // 停顿时间\n");
        sb.append("            })\n");
        sb.append("        })\n");
        sb.append("</script>\n");

        return sb.toString();
    }

    public String renderFlashImageXXX(HttpServletRequest request, List params) {
        String strId = (String) props.get("id");
        int id = StrUtil.toInt(strId, -1);
        if (id == -1)
            return "The id of AdFlashImage is invalid(id=" + strId + ").";
        SiteFlashImageDb sfid = new SiteFlashImageDb();
        sfid = (SiteFlashImageDb) sfid.getQObjectDb(new Integer(id));
        if (sfid == null) {
            return "Ad Flash Image is null where id=" + id;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<script>");
        for (int i = 1; i <= 5; i++) {
            sb.append("imgUrl" + i + "=\"" +
                      StrUtil.getNullStr(sfid.getString("url" + i)) +
                      "\";\n");
            sb.append("imgtext" + i + "=\"" +
                      StrUtil.getNullStr(sfid.getString("title" + i)) +
                      "\";\n");
            sb.append("imgLink" + i + "=\"" +
                      StrUtil.getNullStr(sfid.getString("link" + i)) +
                      "\";\n");
        }

        String w = (String) props.get("w");
        if (w == null)
            w = "260";
        String h = (String) props.get("h");
        if (h == null)
            h = "215";
        sb.append("var focus_width=" + w + "\n");
        sb.append("var focus_height=" + h + "\n");
        sb.append("var text_height=18\n");
        sb.append("var swf_height = focus_height+text_height\n");

        sb.append(
                "var pics=imgUrl1+\"|\"+imgUrl2+\"|\"+imgUrl3+\"|\"+imgUrl4+\"|\"+imgUrl5\n");
        sb.append("var links=imgLink1+\"|\"+imgLink2+\"|\"+imgLink3+\"|\"+imgLink4+\"|\"+imgLink5\n");
        sb.append("var texts=imgtext1+\"|\"+imgtext2+\"|\"+imgtext3+\"|\"+imgtext4+\"|\"+imgtext5\n");
        sb.append("document.write('<object classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\" width='+ focus_width +' height='+ swf_height +'>');\n");
        sb.append("document.write('<param name=\"allowScriptAccess\" value=\"sameDomain\"><param name=\"movie\" value=\"" +
                  Global.getRootPath() + "/images/home/focus.swf\"><param name=\"quality\" value=\"high\"><param name=\"bgcolor\" value=\"#dfdfdf\">');\n");
        sb.append("document.write('<param name=\"menu\" value=\"false\"><param name=wmode value=\"opaque\">');\n");
        sb.append("document.write(\"<param name='FlashVars' value='pics=\"+pics+\"&links=\"+links+\"&texts=\"+texts+\"&borderwidth=\"+focus_width+\"&borderheight=\"+focus_height+\"&textheight=\"+text_height+\"'>\");\n");
        sb.append("document.write(\"<embed src='" + Global.getRootPath() + "/images/home/focus.swf' wmode='opaque' FlashVars='pics=\"+pics+\"&links=\"+links+\"&texts=\"+texts+\"&borderwidth=\"+focus_width+\"&borderheight=\"+focus_height+\"&textheight=\"+text_height+\"' menu='false' bgcolor='#dfdfdf' quality='high' width='\"+ focus_width +\"' height='\"+ focus_height +\"' allowScriptAccess='sameDomain' type='application/x-shockwave-flash' pluginspage='http://www.macromedia.com/go/getflashplayer' />\");\n");
        sb.append("document.write('</object>');\n");

        sb.append("</script>");
        return sb.toString();
    }

    public String renderAd(HttpServletRequest request, List params) {
        String strId = (String) props.get("id");
        int id = StrUtil.toInt(strId, -1);
        if (id == -1)
            return "The id of Ad is invalid.(id=" + strId + ")";
        SiteAdDb sad = new SiteAdDb();
        sad = (SiteAdDb) sad.getQObjectDb(new Integer(id));
        if (sad == null) {
            return "Ad is null where id=" + id;
        }
        return sad.getString("content");
    }

    public String parseDirCodeFromProps(HttpServletRequest request) {
        String dirCode = (String) props.get("dir");
        if (dirCode==null) {
            if (request!=null)
                dirCode = ParamUtil.get(request, "dirCode");
            else
                dirCode = "";
        }
        return dirCode;
    }

    public String renderListDoc(HttpServletRequest request, List params) {
        String dirCode = parseDirCodeFromProps(request);
        String parentCode = "";
        boolean isListParent = false;

        if (dirCode.equals("")) {
            parentCode = StrUtil.getNullStr((String) props.get("parentcode"));
            if (!parentCode.equals("")) {
                isListParent = true;
                dirCode = parentCode;
            }
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf == null)
            return "The leaf with dirCode=" + dirCode + " or parentCode=" + parentCode + " is not exist.";

        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.
                                          Config();
        boolean isHtml = cfg.getBooleanProperty("cms.html_doc");

        String more = StrUtil.getNullStr((String)props.get("more"));
        if (more.equalsIgnoreCase("true") || more.equalsIgnoreCase("yes") || more.equalsIgnoreCase("y")) {
            if (isListParent)
                return "List with parentCode has no more pages.";
            if (isHtml) {
                String sql = SQLBuilder.getDirDocListSql(dirCode);
                Document doc = new Document();
                int total = doc.getDocCount(sql);
                int pageSize = cfg.getIntProperty("cms.listPageSize");
                ListDocPagniator paginator = new ListDocPagniator(request, total,
                        pageSize);
                int pageNo = paginator.pageNum2No(1);
                return request.getContextPath() + "/" + lf.getListHtmlNameByPageNo(pageNo);
            } else {
                return request.getContextPath() + "/" + "doc_list_view.jsp?dirCode=" + StrUtil.UrlEncode(dirCode);
            }
        }

        Document doc = new Document();
        int count = StrUtil.toInt((String) props.get("row"), 10);

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

        String url = "";

        String rootPath = Global.getRootPath();
        if (request != null)
            rootPath = request.getContextPath();

        String target = StrUtil.getNullStr((String)props.get("target"));
        if (!target.equals(""))
            target = "target=\"" + target + "\"";

        String query;
        if (!isListParent) {
            query = SQLBuilder.getDirDocListSql(dirCode);
        }
        else {
            query = SQLBuilder.getParentDirDocListSql(dirCode);
        }

        Iterator ir = doc.list(query, count).iterator();
        StringBuffer str = new StringBuffer();
        str.append("<ul>");
        while (ir.hasNext()) {
            doc = (Document) ir.next();
            if (doc.getType() == Document.TYPE_LINK) {
                url = doc.getSource();
            } else {
                if (!isHtml)
                    url = Global.getRootPath() + "/doc_view.jsp?id=" + doc.getId();
                else
                    url = Global.getRootPath() + "/" + doc.getDocHtmlName(1);
            }
            if (DateUtil.compare(new java.util.Date(), doc.getExpireDate()) ==
                2) {
                str.append("<li>");
                if (isDateShow) {
                    str.append("<span style='float:right'>" +
                            DateUtil.format(doc.getCreateDate(),
                                            dateFormat) + "</span>&nbsp;");
                }
                str.append("<a title=" + StrUtil.toHtml(doc.getTitle()) + " href='" + url + "' " + target + " >");
                if (doc.isBold())
                    str.append("<B>");
                if (!doc.getColor().equals("")) {
                    str.append("<font color='" + doc.getColor() + "'>");
                }
                str.append(format(doc.getTitle(), props));
                if (!doc.getColor().equals(""))
                    str.append("</font>");
                if (doc.isBold())
                    str.append("</B>");

                str.append("</a>");

                if (doc.getIsNew() == 1) {
                    str.append("&nbsp;<img border=0 src='" + rootPath +
                            "/images/i_new.gif'>");
                }

                str.append("</li>");
            } else {
                str.append("<li>");
                if (isDateShow) {
                    str.append("<span style='float:right'>" +
                            DateUtil.format(doc.getCreateDate(),
                                            dateFormat) +
                            "</span>&nbsp;");
                }
                str.append("<a title=" + StrUtil.toHtml(doc.getTitle()) + " href='" + url + "'>" +
                        format(doc.getTitle(), props));
                str.append("</a>");
                str.append("</li>");
            }
        }
        str.append("</ul>");
        return str.toString();
    }

    public String renderLink(HttpServletRequest request, List params) {
        LinkDb ld = new LinkDb();

        String kind = StrUtil.getNullStr((String) props.get("kind"));
        if (kind.equals(""))
            kind = LinkDb.KIND_DEFAULT;

        String listsql = ld.getListSql(kind, LinkDb.USER_SYSTEM);
        Iterator irlink = ld.list(listsql).iterator();

        StringBuffer buf = new StringBuffer();
        buf.append("<ul>");
        while (irlink.hasNext()) {
            ld = (LinkDb) irlink.next();
            buf.append("<li><a title=\"" + ld.getTitle()  + "\" target=\"_blank\" href=\"" + ld.getUrl() + "\">");
            if (!ld.getImage().equals("")) {
                buf.append("<img src='" +
                           ld.getImageUrl(request) + "' border=0>");
            } else {
                buf.append(ld.getTitle());
            }
            buf.append("</a></li>");
        }
        buf.append("</ul>");
        return buf.toString();
    }

    public String includeFile(HttpServletRequest request, List params) {
        String page = StrUtil.getNullStr((String) props.get("page"));
        if (page.endsWith("htm") || page.endsWith("html")) {
            try {
                if (!page.startsWith("http://")) {
                    page = Global.getRealPath() + page;
                }
                // System.out.println(getClass() + "includeFile:" + page);
                TemplateLoader tl = new TemplateLoader(request, page);
                return tl.toString();
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
        }
        else {
            if (page.startsWith("request.")) {
                int p = page.indexOf(".");
                String attr = page.substring(p+1);
                page = (String)request.getAttribute(attr);
            }
            if (!page.startsWith("http://")) {
                page = Global.getFullRootPath() + "/" + page;
            }
            String charset = StrUtil.getNullStr((String) props.get("charset"));
            if (charset.equals(""))
                charset = "utf-8";
            return NetUtil.gather(request, charset, page);
        }
        return "";
    }

    public String toString(HttpServletRequest request, List params) {
        if (field.equalsIgnoreCase("flashImage")) {
            return renderFlashImage(request, params);
        } else if (field.equalsIgnoreCase("ad")) {
            return renderAd(request, params);
        } else if (field.equalsIgnoreCase("scrollImg")) {
            return renderScrollImg(request, params);
        } else if (field.equalsIgnoreCase("listDoc")) {
            return renderListDoc(request, params);
        } else if (field.equalsIgnoreCase("link")) {
            return renderLink(request, params);
        }
        else if (field.equalsIgnoreCase("include")) {
            return includeFile(request, params);
        }
        else
            return "Field " + field + " is not defined.";
    }


}
