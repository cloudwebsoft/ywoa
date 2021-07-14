package com.redmoon.forum.ui.menu;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.ui.SkinMgr;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.forum.ui.Skin;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.StrUtil;

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
public class PresetLeaf {
    final String LOGIN = "login";
    final String STYLE = "style";

    public static String getMenuItem(HttpServletRequest request, Leaf lf) {
        if (lf.getPreCode().equals("login")) {
            if (!Privilege.isUserLogin(request)) {
                return "<a style=\"width:" + lf.getWidth() + "px\" href=\"" + request.getContextPath() + "/door.jsp\">" + SkinUtil.LoadString(request, "res.label.forum.menu", "login") + "</a>";
            }
            else {
                return "<a style=\"width:" + lf.getWidth() + "px\" href=\"" + request.getContextPath() + "/regist.jsp\">" + SkinUtil.LoadString(request, "res.label.forum.menu", "regist") + "</a>";
            }
        }
        else if (lf.getPreCode().equals("exit")) {
            if (Privilege.isUserLogin(request)) {
                return "<a style=\"width:" + lf.getWidth() + "px\" href=\"" + request.getContextPath() + "/exit.jsp\">" + SkinUtil.LoadString(request, "res.label.forum.menu", "exit") + "</a>";
            }
        }
        else if (lf.getPreCode().equals("view_frame")) {
            String rootpath = request.getContextPath();
            String str = "<script type=\"text/javascript\">\n";
            str += "if(top==self){\n";
            str += "document.write('<a style=\"width:" + lf.getWidth() + "px\" href=\"" + rootpath +
                    "/forum/frame.jsp?isFrame=y&mainUrl=" +
                    StrUtil.HtmlEncode(StrUtil.getUrl(request)) + "\" target=\"_top\">" +
                    SkinUtil.LoadString(request, "res.label.forum.menu", "view_frame") +
                    "</a>');\n";
            str += "}else{\n";
            str += "document.write('<a style=\"width:" + lf.getWidth() + "px\" href=\"" + rootpath + "/forum/frame.jsp?isFrame=n&url=" +
                    StrUtil.HtmlEncode(StrUtil.getUrl(request)) + "\" target=\"_top\">" +
                    SkinUtil.
                    LoadString(request, "res.label.forum.menu", "view_flat") +
                    "</a>');\n";
            str += "}</script>";
            return str;
        }
        else if (lf.getPreCode().equals("style")) {
            String rootpath = request.getContextPath();
            SkinMgr hskmgr = new SkinMgr();
            Vector hv = hskmgr.getAllSkin();
            Iterator hir = hv.iterator();
            String hskinmenu = "<a style=\"" + lf.getWidth() + "px\" href='#' width=\"" + lf.getWidth() + "\">" + SkinUtil.LoadString(request, "res.label.forum.menu", "style") + "</a>";
            hskinmenu += "<ul>";
            while (hir.hasNext()) {
                Skin hskin = (Skin) hir.next();
                if (hskinmenu.equals(""))
                    hskinmenu += "<li><a href=\"" + rootpath +
                            "/forum/userset.jsp?op=setSkin&skinCode=" +
                            hskin.getCode() + "\">" + hskin.getName() +
                            "</a></li>";
                else
                    hskinmenu += "<li><a href=\"" + rootpath +
                            "/forum/userset.jsp?op=setSkin&skinCode=" +
                            hskin.getCode() + "\">" + hskin.getName() +
                            "</a></li>";
            }
            hskinmenu += "</ul>";
            return hskinmenu;
        }
        return "";
    }
}
