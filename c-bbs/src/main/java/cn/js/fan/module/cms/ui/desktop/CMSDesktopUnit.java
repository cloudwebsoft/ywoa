package cn.js.fan.module.cms.ui.desktop;

import javax.servlet.http.*;

import cn.js.fan.module.cms.ui.*;
import cn.js.fan.util.*;

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
public class CMSDesktopUnit implements IDesktopUnit {

    public CMSDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request, DesktopItemDb di) {
        DesktopMgr dm = new DesktopMgr();
        DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
        String url = du.getPageList() + "?kind=" + StrUtil.UrlEncode(di.getModuleItem());
        return url;
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

    public String display(HttpServletRequest request, DesktopItemDb di) {
        // System.out.println(getClass() + "di.getModuleItem()=" + di.getModuleItem());
        if (di.getModuleItem().startsWith("cws_")) {
            String var = di.getModuleItem().substring(4);
            if (var.equals("flashImages")) {
                return dispalyFlashImages(request, di);
            } else if (var.equals("scrollImages")) {
                return dispalyScrollImages(request, di);
            } else
                return di.getModuleItem();
        }
        else if (di.getModuleItem().equals("focus")) {
            Home home = Home.getInstance();
            return home.getProperty("focus.abstract");
        }
        else if (di.getModuleItem().startsWith("ad_")) {
            String strId = di.getModuleItem().substring(3);
            Home home = Home.getInstance();
            return home.getProperty("ads", "id", strId);
        } else {
            throw new IllegalArgumentException("The module item " + di.getModuleItem() + " is not defined");
        }
    }
}
