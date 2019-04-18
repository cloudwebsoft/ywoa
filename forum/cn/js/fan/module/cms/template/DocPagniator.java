package cn.js.fan.module.cms.template;

import cn.js.fan.db.Paginator;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.Document;
import cn.js.fan.web.Global;

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
public class DocPagniator extends Paginator {
    HttpServletRequest request;

    public DocPagniator(HttpServletRequest request, long total, int pagesize) {
        super(request, total, pagesize);
        this.request = request;
    }

    /**
     * 生成静态页面的页码
     * @param <any> Document
     * @return String
     */
    public String getHtmlCurPageBlock(Document doc, int curPage) {
        this.curPage = curPage;
        intpagenum();

        if (pagenumbegin == 0)
            return "";

        String str = "";
        String rootPath = "";
        if (!Global.virtualPath.equals(""))
            rootPath = "/" + Global.virtualPath;

        if (curpagenumblock > 1) { // 如果显示的是第二个页码段的页面
            str += "<a title='往前' href=\"" + rootPath + "/" + doc.getDocHtmlName(pagenumbegin - 1) + "\">" + "上一页" + "</a> ";
        }
        for (int i = pagenumbegin; i <= pagenumend; i++) {
            if (i == curPage)
                str += i + " ";
            else
                str += "[<a href=\"" + rootPath + "/" + doc.getDocHtmlName(i) + "\">" + i +
                        "</a>] ";
        }
        if (curpagenumblock < totalpagenumblock) { //如果显示的是第二个页码段的页面
            str += "<a title='往后' href=\"" + rootPath + "/" + doc.getDocHtmlName(pagenumend + 1) +
                    "\">" + "下一页" + "</a>";
        }
        str += "<script language='javascript'>\n";
        str += "<!--\n";
        str += "function selpage_onchange()\n";
        str += "{\n";
        str += "location.href=selpage.value\n";
        str += "}\n";
        str += "//-->\n";
        str += "</script>\n";
        str += "&nbsp;&nbsp;到第&nbsp;<select name=selpage onchange='selpage_onchange()'>";
        for (int k = 1; k <= totalpages; k++) {
            if (k!=curPage)
                str += "<option value=" + rootPath + "/" + doc.getDocHtmlName(k) + ">" + k + "</option>";
            else
                str += "<option selected value=" + rootPath + "/" + doc.getDocHtmlName(k) + ">" + k + "</option>";
        }
        str += "</select>&nbsp;页";
        return str;
    }
}
