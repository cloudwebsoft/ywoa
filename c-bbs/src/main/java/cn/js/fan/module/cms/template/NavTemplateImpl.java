package cn.js.fan.module.cms.template;

import java.util.*;

import cn.js.fan.module.nav.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.LeafChildrenCacheMgr;
import cn.js.fan.module.cms.Leaf;
import cn.js.fan.web.Global;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * $nav.2 获取博客的导航条
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class NavTemplateImpl extends VarPart {
    public NavTemplateImpl() {

    }

    public String toString(HttpServletRequest request, List params) {
        NavigationMgr nmr = new NavigationMgr();
        String type = field;
        if (type.equals(""))
            type = Navigation.TYPE_CMS;
        if (type.equals(Navigation.TYPE_CMS))
            return "<script src='" + Global.getRootPath() + "/doc/js/nav_" + type + ".js'></script>";
        else {
            Vector v = nmr.getAllNav(type);
            int count = 0;
            int size = v.size();
            Iterator ir = v.iterator();
            StringBuffer str = new StringBuffer();
            str.append("<ul>");
            while (ir.hasNext()) {
                Navigation nav = (Navigation) ir.next();
                String target = "";
                if (!nav.getTarget().equals(""))
                    target = "target=" + nav.getTarget();
                if (nav.getLink().equals(Navigation.LINK_COLUMN)) {
                    cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.
                                                      Config();
                    boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
                    LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.
                            ROOTCODE);
                    Vector vt = lccm.getDirList();
                    Iterator cir = vt.iterator();
                    int cc = vt.size();
                    int k = 0;
                    String path = Global.virtualPath.equals("") ? "/" :
                                  "/" + Global.virtualPath;
                    while (cir.hasNext()) {
                        Leaf lf = (Leaf) cir.next();
                        if (lf.getIsHome()) {
                            String url = "";
                            if (isHtml) {
                                url = path + lf.getListHtmlPath() + "/index." +
                                      cfg.getProperty("cms.html_ext");
                            } else {
                                url = path + "/doc_column_view.jsp?dir_code=" +
                                      StrUtil.UrlEncode(lf.getCode());
                            }
                            str.append("<li><a " + target + " href='" + url +
                                       "'>" +
                                       lf.getName() + "</a></li>");
                            if (k < cc)
                                str.append("<li class=\"seperator\"></li>");
                        }

                        k++;
                    }
                } else {
                    str.append("<li>");

                    String color = StrUtil.getNullString(nav.getColor());
                    String name = "";
                    if (color.equals(""))
                        name = nav.getName();
                    else
                        name = "<font color='" + color + "'>" + nav.getName() +
                               "</font>";

                    str.append("<a " + target + " href='" + nav.getLink() + "'>" +
                               name + "</a>");
                    str.append("</li>");
                    if (count < size)
                        str.append("<li class=\"seperator\"></li>");
                    count++;
                }
            }
            str.append("</ul>");
            return str.toString();
        }
    }

}
