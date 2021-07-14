package cn.js.fan.module.cms.template;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.module.cms.*;
import cn.js.fan.web.*;

/**
 * <p>Title: 分页</p>
 *
 * <p>Description: 对分页采取倒排的方式，即第1页对应的是实际的第N页，第2页对应的是实际的第n-1页，第i页对应实际的第n-i+1页</p>
 * 以pageNo表示页面的实际名称中含有的no.，pageNum表示页码
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ListDocPagniator extends Paginator {
    HttpServletRequest request;

    static int pagePosition = 0;

     public ListDocPagniator(HttpServletRequest request, long total, int pagesize) {
         super(request, total, pagesize);
         this.request = request;
     }

     /**
      * 取得对应于页码pageNum的实际生成页面的no
      * @param totalPages int
      * @param pageNum int
      * @return int
      */
     public int pageNum2No(int pageNum) {
         return totalpages - pageNum + 1;
     }

     /**
      * 取得对应于实际页面名称no的页码pageNum
      * @param pageNum int
      * @return int
      */
     public int pageNo2Num(int pageNo) {
         return totalpages - pageNo + 1;
     }

     /**
      * 生成静态页面的页码
      * @param lf Leaf
      * @param curPage int 当前页码
      * @return String
      */
     public String getHtmlCurPageBlock(Leaf lf, int curPage) {
         this.curPage = curPage;
         intpagenum();

         if (pagenumbegin == 0)
             return "";

         String str = "";
         String rootPath = "";
         if (!Global.virtualPath.equals(""))
             rootPath = "/" + Global.virtualPath;

         cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();

         if (curpagenumblock > 1) { // 如果显示的是第二个页码段的页面
             str += "<a title='往前' href='" + rootPath + "/" + lf.getListHtmlNameByPageNo(pageNum2No(pagenumbegin - 1)) + "'>" + "上一页" + "</a> ";
         }
         for (int i = pagenumbegin; i <= pagenumend; i++) {
             if (i == curPage)
                 str += i + " ";
             else
                 str += "[<a href='" + rootPath + "/" + lf.getListHtmlNameByPageNo(pageNum2No(i)) + "'>" + i +
                         "</a>] ";
         }
         if (curpagenumblock < totalpagenumblock) { //如果显示的是第二个页码段的页面
             str += "<a title='往后' href='" + rootPath + "/" + lf.getListHtmlNameByPageNo(pageNum2No(pagenumend + 1)) +
                     "'>" + "下一页" + "</a>";
         }

         String pre = "_" + pagePosition;
         str += "<input name=" + "pageNum" + pre + " size=2 style=width:30px onKeyDown=" +
                   "page_presskey" + pre + "(this.value)>";
         str += "<input type=button value=GO onClick=" +
                   "changepage" + pre + "(" + "pageNum" + pre + ".value)>";

           str += "<script language='javascript'>";
           str += "function changepage" + pre + "(num){";
           str += "window.location.href='" + lf.getCode() + "_" + "'+(" + totalpages +
                   " - num + 1)+'." + cfg.getProperty("cms.html_ext") + "';";
           str += "}";
           str += "function page_presskey" + pre + "(num) {";
           str += "if (window.event.keyCode==13) {";
           str += "changepage(num);window.event.cancelBubble=true;";
           str += "}";
           str += "}</script>";

        pagePosition++;
        if (pagePosition>500) {
            pagePosition = 0;
        }
/*
         str += "<script language='javascript'>";
         str += "function selpage_onchange()";
         str += "{";
         str += "location.href=selpage.value;";
         str += "}";
         str += "</script>";
         str += "&nbsp;&nbsp;到第&nbsp;<select name=selpage onchange='selpage_onchange()'>";
         for (int k = 1; k <= totalpages; k++) {
             if (k!=curPage)
                 str += "<option value=" + rootPath + "/" + lf.getListHtmlName(pageNum2No(k)) + ">" + k + "</option>";
             else
                 str += "<option selected value=" + rootPath + "/" + lf.getListHtmlName(pageNum2No(k)) + ">" + k + "</option>";
         }
         str += "</select>&nbsp;页";
*/
         return str;
    }
}
