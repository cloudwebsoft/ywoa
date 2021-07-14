package com.redmoon.forum.ui;

import cn.js.fan.db.Paginator;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ParamUtil;
import java.util.Enumeration;

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
public class ForumPaginator extends Paginator {
    public ForumPaginator(HttpServletRequest request, long total, int pagesize) {
        super(request, total, pagesize);
    }

    /**
     * 取得listtopic.jsp的页码
     * @param request HttpServletRequest
     * @param boardCode String
     * @param mode int 0平板 1树形
     * @param threadType int
     * @return String
     */
    public String getListTopicCurPageBlock(HttpServletRequest request, String boardCode, int mode, int threadType) {
        // 当plugin有特殊的showtopic列表要求时，使用动态页面
        String pluginCode = ParamUtil.get(request, "pluginCode");
        if (!pluginCode.equals("")) {
            Enumeration e = request.getParameterNames();
            String str = "";
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                // 因为在getCurPageBlock中会自动加上CPages，所以这里也需去除
                if (!name.equals("boardcode") && !name.equals("CPages")) {
                    String v = ParamUtil.get(request, name);
                    str += "&" + name + "=" + StrUtil.UrlEncode(v);
                }
            }
            String querystr = "boardcode=" + boardCode + str;
            return getCurPageBlock(request, "listtopic.jsp?" + querystr, "down");
        }

        intpagenum();

        if (pagenumbegin == 0)
            return "";

        String rootpath = Global.getRootPath();
        StringBuffer sb = new StringBuffer(200);
        sb.append(
                "<table border=\"0\" cellSpacing=\"1\" cellPadding=\"0\" class='table_page'>");
        sb.append("<tr align=middle>");
        sb.append("<td class=table_page_title title='" +
                  LoadString(request, "topic_count") + "'>" + total + "</td>");
        sb.append("<td class=table_page_title title='" +
                  LoadString(request, "page_count") + "'>" + pageSize + "</td>");
        sb.append("<td class=table_page_title >" + curPage + "/" + totalpages +
                  LoadString(request, "page") + "</td>");
        if (curpagenumblock > 1)
            sb.append("<td class=table_page_list><a href=\"" + ForumPage.getListTopicPage(request, boardCode, mode, 1, threadType) +
                      "\"><img src=\"" + rootpath +
                      "/images/first.gif\" alt=" +
                      LoadString(request, "page_first") +
                      " width=\"9\" height=\"8\" border=\"0\"></a></td>");
        if (curpagenumblock > 1) { // 如果显示的是第二个页码段的页面
            sb.append("<td><a href=\"" + ForumPage.getListTopicPage(request, boardCode, mode, pagenumbegin - 1, threadType) +
                      "\"><img src=\"" + rootpath +
                      "/images/previous.gif\" alt=" +
                      LoadString(request, "previous") +
                      " width=\"8\" height=\"8\" border=\"0\"></a></td>");
        }

        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append("<td class=table_page_cur>" + i + "</td>");
            else
                sb.append("<td class=table_page_list><a href=\"" + ForumPage.getListTopicPage(request, boardCode, mode, i, threadType) + "\">" + i + "</a></td>");
        }

        if (curpagenumblock < totalpagenumblock) { // 如果显示的是第二个页码段的页面
            sb.append("<td class=table_page_list><a href=\"" + ForumPage.getListTopicPage(request, boardCode, mode,  pagenumend + 1, threadType) + "\"><img src=\"" + rootpath +
                      "/images/next.gif\" alt='" + LoadString(request, "after") +
                      "' width=\"8\" height=\"8\" border=\"0\"></a></td>");
        }
        if (curpagenumblock < totalpagenumblock)
            sb.append("<td class=table_page_list><a href=\"" + ForumPage.getListTopicPage(request, boardCode, mode, totalpages, threadType) + "\"><img src=\"" + rootpath +
                      "/images/last.gif\" alt='" +
                      LoadString(request, "page_last") +
                      "' width=\"9\" height=\"8\" border=0></a></td>");
        sb.append("<td class='table_page_num'><input name=\"pageNum\" type=\"text\" size=\"2\" onKeyDown=\"" +
                  "page_presskey(this.value)\"></td>");
        sb.append("<td class=table_page_btn><input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  "changepage(pageNum.value)\"></td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function changepage(num)\n");
        sb.append("{\n");
        if (ForumPage.isHtmlPage) {
            if (mode==1) {
                sb.append("window.location.href=\"f-1-" + boardCode + "-\"+num+\"-" + threadType + ".html\";\n");
            }
            else {
                sb.append("window.location.href=\"f-0-" + boardCode + "-\"+num+\"-" + threadType + ".html\";\n");
            }
        }
        else {
            if (mode==1) {
                sb.append("window.location.href=\"listtopic_tree.jsp?boardcode=" +
                          boardCode + "&threadType=" + threadType + "&CPages=\"+num;\n");
            }
            else {
                sb.append("window.location.href=\"listtopic.jsp?boardcode=" +
                          boardCode + "&threadType=" + threadType + "&CPages=\"+num;\n");
            }
        }
        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("function page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append("changepage(num)\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("</script>\n");
        return sb.toString();
    }

    public String getShowTopicCurPageBlock(HttpServletRequest request, long rootid, String prefix) {
        // 当plugin有特殊的showtopic列表要求时，使用动态页面
        String pluginCode = ParamUtil.get(request, "pluginCode");
        if (!pluginCode.equals("")) {
            Enumeration e = request.getParameterNames();
            String str = "";
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                if (!name.equals("rootid") && !name.equals("CPages")) {
                    String v = ParamUtil.get(request, name);
                    str += "&" + name + "=" + StrUtil.UrlEncode(v);
                }
            }
            String querystr = "rootid=" + rootid + str;
            return getCurPageBlock(request, "showtopic.jsp?" + querystr, prefix);
        }

        int mode = 0; // 表示平板显示，区别于listtopic中是用来表示静态
        intpagenum();

        if (pagenumbegin == 0)
            return "";

        String rootpath = Global.getRootPath();
        StringBuffer sb = new StringBuffer(200);
        sb.append(
                "<table border=\"0\" cellSpacing=\"1\" cellPadding=\"0\" class='table_page'>");
        sb.append("<tr align=middle>");
        sb.append("<td class=table_page_title title='" +
                  LoadString(request, "topic_count") + "'>" + total + "</td>");
        sb.append("<td class=table_page_title title='" +
                  LoadString(request, "page_count") + "'>" + pageSize + "</td>");
        sb.append("<td class=table_page_title >" + curPage + "/" + totalpages +
                  LoadString(request, "page") + "</td>");
        if (curpagenumblock > 1)
            sb.append("<td class=table_page_list><a href=\"" + ForumPage.getShowTopicPage(request, mode, rootid, rootid, 1, "") +
                      "\"><img src=\"" + rootpath +
                      "/images/first.gif\" alt=" +
                      LoadString(request, "page_first") +
                      " width=\"9\" height=\"8\" border=\"0\"></a></td>");
        if (curpagenumblock > 1) { // 如果显示的是第二个页码段的页面
            sb.append("<td class=table_page_lis><a href=\"" + ForumPage.getShowTopicPage(request, mode, rootid, rootid, pagenumbegin - 1, "") +
                      "\"><img src=\"" + rootpath +
                      "/images/previous.gif\" alt=" +
                      LoadString(request, "previous") +
                      " width=\"8\" height=\"8\" border=\"0\"></a></td>");
        }

        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append("<td class=table_page_cur>" + i + "</td>");
            else
                sb.append("<td class=table_page_list><a href=\"" + ForumPage.getShowTopicPage(request, mode, rootid, rootid, i, "") + "\">" + i + "</a></td>");
        }

        if (curpagenumblock < totalpagenumblock) { // 如果显示的是第二个页码段的页面
            sb.append("<td class=table_page_list><a href=\"" + ForumPage.getShowTopicPage(request, mode, rootid, rootid, pagenumend + 1, "") + "\"><img src=\"" + rootpath +
                      "/images/next.gif\" alt='" + LoadString(request, "after") +
                      "' width=\"8\" height=\"8\" border=\"0\"></a></td>");
        }
        if (curpagenumblock < totalpagenumblock)
            sb.append("<td class=table_page_list><a href=\"" + ForumPage.getShowTopicPage(request, mode, rootid, rootid, totalpages, "") + "\"><img src=\"" + rootpath +
                      "/images/last.gif\" alt='" +
                      LoadString(request, "page_last") +
                      "' width=\"9\" height=\"8\" border=0></a></td>");
        sb.append("<td class=table_page_num><input name=\"" + prefix + "pageNum\" type=\"text\" size=\"2\" onKeyDown=\"" +
                  "page_presskey(this.value)\"></td>");
        sb.append("<td class=table_page_btn><input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  "changepage(" + prefix + "pageNum.value)\"></td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function changepage(num)\n");
        sb.append("{\n");
        sb.append("if (num==null || num=='') num=1\n");
        if (ForumPage.isHtmlPage) {
            sb.append("window.location.href=\"t-0-" + rootid + "-\"+num+\".html\";\n");
        }
        else {
            sb.append("window.location.href=\"showtopic.jsp?rootid=" +
                      rootid + "&CPages=\"+num;\n");
        }

        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("function page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append("changepage(num)\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("</script>\n");
        return sb.toString();
    }
}
