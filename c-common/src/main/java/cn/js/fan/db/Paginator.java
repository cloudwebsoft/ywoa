package cn.js.fan.db;

import javax.servlet.http.*;
import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class Paginator implements java.io.Serializable {
    public long total = 0;
    public int pageSize = 20;
    public int curPage = 1;
    public int totalpages = 0;

    public int pagenumbegin = 0; // 当前页码显示的开始页码
    public int pagenumend = 0; // 在页面中显示的结束页码
    public int numperpage = 10; // 每页显示的页码数
    public int totalpagenumblock = 0; // 共有多少个页码块，如[pagenumbegin,2,3,4,5,...,pagenumend]
    public int curpagenumblock = 0; // 当前处在第多少个页码块，如[pagenumbegin,12...,pagenumend]

    public Paginator(HttpServletRequest request) {
        getCPage(request);
    }

    public Paginator(HttpServletRequest request, long total, int pagesize) {
        getCPage(request);
        init(total, pagesize);
    }

    public void init(long total, int pagesize) {
        this.total = total;
        this.pageSize = pagesize;
        totalpages = (int) Math.ceil((double) total / pagesize);
        if (curPage > totalpages)
            curPage = totalpages;
        if (curPage <= 0)
            curPage = 1;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long t) {
        this.total = t;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int p) {
        this.pageSize = p;
    }

    public int getTotalPages() {
        return totalpages;
    }

    public int getCurrentPage() {
        return curPage;
    }

    public int getCurPage() {
        return curPage;
    }

    public int getCPage(HttpServletRequest request) {
        // 当通过模板生成静态页面时，request可能为null，如：ListDocPagniator
        if (request==null)
            return 1;
        String strcurpage = StrUtil.getNullStr(request.getParameter("CPages"));
        if (!StrUtil.isNumeric(strcurpage)) {
            curPage = 1;
        } else {
            try {
                curPage = Integer.parseInt(strcurpage);
                // 防止攻击
                if (curPage <= 0)
                    curPage = 1;
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("getCPage:" + StrUtil.trace(e));
                curPage = 1;
            }
        }
        return curPage;
    }

    // 用当前页作为参数来初始化页码pagenumblock中用到的数据
    public void intpagenum() {
        if (totalpages == 0) {
            pagenumbegin = 0;
            pagenumend = 0;
            return;
        }

        // 总的页码块数
        totalpagenumblock = (int) totalpages / numperpage;
        if (totalpages % numperpage > 0)
            totalpagenumblock += 1;
        //if (TPages % numperpage > 0)
        //  totalpagenumblock += 1;

        curpagenumblock = (int) (curPage / numperpage);
        if (curPage % numperpage > 0)
            curpagenumblock += 1;
        pagenumbegin = (curpagenumblock - 1) * numperpage + 1;
        if (pagenumbegin == 0)
            pagenumbegin = 1;
        if (totalpagenumblock == curpagenumblock)
            pagenumend = totalpages; //当最后一个页码块即为当前块时，使结束页码为总页码，避免当不足numperpage时会使得超出
        else
            pagenumend = pagenumbegin + numperpage - 1;
    }

    public String getPageBlock(HttpServletRequest request, String url) {
        return getPageBlock(request, url, "");
    }

    public String getPageStatics(HttpServletRequest request) {
        String page = StrUtil.format(SkinUtil.LoadString(request,
                "page_statics"), new Object[] {"" + getTotal(),
                                     "" + getPageSize(), "" + getCurrentPage(),
                                     "" + getTotalPages()});
        return page;
    }

    public String LoadString(HttpServletRequest request, String key) {
        return SkinUtil.LoadString(request, "res.cn.js.fan.db.Paginator", key);
    }

    public String getPageBlock(HttpServletRequest request, String url,
                               String pre) {
        intpagenum();

        if (pagenumbegin == 0)
            return "";

        StringBuffer sb = new StringBuffer(200);
        if (curpagenumblock > 1) { //如果显示的是第二个页码段的页面
            sb.append("<a title='" + LoadString(request, "previous") +
                      "' href=\"" + url + "&CPages=" + (pagenumbegin - 1) +
                      "\">" + "<<" + "</a> ");
        }
        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append(i + " ");
            else
                sb.append("[<a href=\"" + url + "&CPages=" + i + "\">" + i +
                          "</a>] ");
        }
        if (curpagenumblock < totalpagenumblock) { //如果显示的是第二个页码段的页面
            sb.append("<a title='" + LoadString(request, "after") + "' href=\"" +
                      url + "&CPages=" + (pagenumend + 1) + "\">" + ">>" +
                      "</a>");
        }
        sb.append("<input name=\"" + pre + "pageNum\" type=\"text\" size=\"2\" style=\"width:30px\" onKeyDown=\"" +
                  pre + "page_presskey(this.value)\">");
        sb.append("<input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  pre + "changepage(" + pre + "pageNum.value)\">");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function " + pre + "changepage(num)\n");
        sb.append("{\n");
        sb.append("window.location.href=\"" + url + "&CPages=\"+num;\n");
        sb.append("}\n");
        sb.append("function " + pre + "page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append(pre + "changepage(num);\nwindow.event.cancelBubble=true;");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("</script>\n");
        return sb.toString();
    }

    public String getCurPageBlock(String url) {
        return getCurPageBlock(url, "");
    }

    /**
     *
     * @param url String
     * @param pre String 前缀，防止页面中多次调用本函数时导致HTML对象有重名
     * @return String
     */
    public String getCurPageBlock(String url, String pre) {
        intpagenum();

        if (pagenumbegin == 0)
            return "";

        StringBuffer sb = new StringBuffer(200);
        if (curpagenumblock > 1) { //如果显示的是第二个页码段的页面
            sb.append("<a title='往前' href=\"" + url + "&CPages=" +
                      (pagenumbegin - 1) + "\">" + "<<" + "</a> ");
        }

        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append(i + " ");
            else
                sb.append("[<a href=\"" + url + "&CPages=" + i + "\">" + i +
                          "</a>] ");
        }

        if (curpagenumblock < totalpagenumblock) { //如果显示的是第二个页码段的页面
            sb.append("<a title='往后' href=\"" + url + "&CPages=" +
                      (pagenumend + 1) + "\">" + ">>" + "</a>");
        }

        sb.append("<input class='pageNum' id=\"" + pre + "pageNum\" name=\"" + pre + "pageNum\" type=\"text\" size=\"2\" style=\"width:30px\" onKeyDown=\"" +
                  pre + "page_presskey(this.value)\">");
        sb.append("<input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  pre + "changepage(" + pre + "pageNum.value)\">");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function " + pre + "changepage(num)\n");
        sb.append("{\n");
        sb.append("window.location.href=\"" + url + "&CPages=\"+num;\n");
        sb.append("}\n");
        sb.append("function " + pre + "page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append(pre + "changepage(num);\nwindow.event.cancelBubble=true;");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("</script>\n");
        return sb.toString();
    }


    /**
     *
     * @param url String
     * @param pre String 前缀，防止页面中多次调用本函数时导致HTML对象有重名
     * @return String
     */
    public String getCurPageBlockWap(String url, String pre) {
        intpagenum();

        if (pagenumbegin == 0)
            return "";

        StringBuffer sb = new StringBuffer(200);
        if (curpagenumblock > 1) { //如果显示的是第二个页码段的页面
            sb.append("<a title='往前' href=\"" + url + "&CPages=" +
                      (pagenumbegin - 1) + "\">" + "<<" + "</a> ");
        }

        if (curPage>1)
            sb.append("<a href=\"" + url + "&CPages=" + (curPage-1) + "\">上一页</a>&nbsp;");

        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append(i + " ");
            else
                sb.append("[<a href=\"" + url + "&CPages=" + i + "\">" + i +
                          "</a>] ");
        }

        if (curPage < pagenumend)
            sb.append("&nbsp;<a href=\"" + url + "&CPages=" + (curPage+1) + "\">下一页</a>");

        if (curpagenumblock < totalpagenumblock) { //如果显示的是第二个页码段的页面
            sb.append("<a title='往后' href=\"" + url + "&CPages=" +
                      (pagenumend + 1) + "\">" + ">>" + "</a>");
        }

        sb.append("<input class='pageNum' id=\"" + pre + "pageNum\" name=\"" + pre + "pageNum\" type=\"text\" size=\"2\" style=\"width:30px\" onKeyDown=\"" +
                  pre + "page_presskey(this.value)\">");
        sb.append("<input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  pre + "changepage(" + pre + "pageNum.value)\">");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function " + pre + "changepage(num)\n");
        sb.append("{\n");
        sb.append("window.location.href=\"" + url + "&CPages=\"+num;\n");
        sb.append("}\n");
        sb.append("function " + pre + "page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append(pre + "changepage(num);\nwindow.event.cancelBubble=true;");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("</script>\n");
        return sb.toString();
    }


    public String getCurPageBlock(HttpServletRequest request, String url) {
        return getCurPageBlock(request, url, "");
    }

    /**
     *
     * @param url String
     * @param pe String
     * @return String
     */
    public String getCurPageBlock(HttpServletRequest request, String url,
                                  String pre) {
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
            sb.append("<td class=table_page_list><a href=\"" + url +
                      "&CPages=1\"><img src=\"" + rootpath +
                      "/images/first.gif\" alt=" +
                      LoadString(request, "page_first") +
                      " width=\"9\" height=\"8\" border=\"0\"></a></td>");
        if (curpagenumblock > 1) { // 如果显示的是第二个页码段的页面
            sb.append("<td><a href=\"" + url + "&CPages=" + (pagenumbegin - 1) +
                      "\"><img src=\"" + rootpath +
                      "/images/previous.gif\" alt=" +
                      LoadString(request, "previous") +
                      " width=\"8\" height=\"8\" border=\"0\"></a></td>");
        }

        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append("<td class=table_page_cur>" + i + "</td>");
            else
                sb.append("<td class=table_page_list><a href=\"" + url +
                          "&CPages=" + i + "\">" + i + "</a></td>");
        }

        if (curpagenumblock < totalpagenumblock) { // 如果显示的是第二个页码段的页面
            sb.append("<td class=table_page_list><a href=\"" + url + "&CPages=" +
                      (pagenumend + 1) + "\"><img src=\"" + rootpath +
                      "/images/next.gif\" alt='" + LoadString(request, "after") +
                      "' width=\"8\" height=\"8\" border=\"0\"></a></td>");
        }
        if (curpagenumblock < totalpagenumblock)
            sb.append("<td class=table_page_list><a href=\"" + url + "&CPages=" +
                      totalpages + "\"><img src=\"" + rootpath +
                      "/images/last.gif\" alt='" +
                      LoadString(request, "page_last") +
                      "' width=\"9\" height=\"8\" border=0></a></td>");
        sb.append("<td class=table_page_list><input name=\"pageNum" + pre + "\" type=\"text\" size=\"2\" style=\"width:30px\" onKeyDown=\"" +
                  pre + "page_presskey(this.value)\"></td>");
        sb.append("<td class=table_page_list><input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  pre + "changepage(pageNum" + pre + ".value)\"></td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function " + pre + "changepage(num)\n");
        sb.append("{\n");
        sb.append("window.location.href=\"" + url + "&CPages=\"+num;\n");
        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("function " + pre + "page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append(pre + "changepage(num)\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("</script>\n");
        return sb.toString();
    }

    /**
     * 获取分页页码用于ajax方式获得下一页内容
     * @param pre String
     * @return String
     */
    public String getCurPageBlockAjax(String pre) {
        intpagenum();

        if (pagenumbegin == 0)
            return "";

        StringBuffer sb = new StringBuffer(200);
        if (curpagenumblock > 1) { //如果显示的是第二个页码段的页面
            sb.append("<a title='往前' href=\"javascript:void(0);\" onclick=\"ajaxChangePage(" +
                      (pagenumbegin - 1) + ")\">" + "<<" + "</a> ");
        }

        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                sb.append(i + " ");
            else
                sb.append("[<a href=\"javascript:void(0);\" onclick=\"ajaxChangePage(" + i + ")\">" + i +
                          "</a>] ");
        }

        if (curpagenumblock < totalpagenumblock) { //如果显示的是第二个页码段的页面
            sb.append("<a title='往后' href=\"javascript:void(0);\" onclick=\"ajaxChangePage(" +
                      (pagenumend + 1) + ")\">" + ">>" + "</a>");
        }

        sb.append("<input name=\"" + pre + "pageNum\" type=\"text\" size=\"2\" style=\"width:30px\" onKeyDown=\"" +
                  pre + "page_presskey(this.value)\">");
        sb.append("<input type=\"button\" name=\"GO\" value=\"GO\" onClick=\"" +
                  pre + "ajaxChangePage(" + pre + "pageNum.value)\">");

        sb.append("<script language='javascript'>\n");
        sb.append("<!--\n");
        sb.append("function " + pre + "page_presskey(num) {\n");
        sb.append("if (window.event.keyCode==13) {\n");
        sb.append(pre + "ajaxChangePage(num);\nwindow.event.cancelBubble=true;");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("//-->\n");
        sb.append("</script>\n");
        return sb.toString();
    }
}
