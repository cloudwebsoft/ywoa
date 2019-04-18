package cn.js.fan.module.nav;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.pvg.UserCheck;
import cn.js.fan.module.pvg.UserMgr;
import cn.js.fan.util.ErrMsgException;
import org.apache.log4j.Logger;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ParamUtil;
import java.util.Iterator;
import cn.js.fan.db.ResultIterator;
import java.util.Vector;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.StrUtil;
import cn.js.fan.module.cms.LeafChildrenCacheMgr;
import cn.js.fan.module.cms.Leaf;
import cn.js.fan.web.Global;
import cn.js.fan.util.file.FileUtil;

public class NavigationMgr {
    Logger logger = Logger.getLogger(NavigationMgr.class.getName());
    RMCache rmCache;
    String cachePrix = "cn.js.fan.module.nav";

    public NavigationMgr() {
        rmCache = RMCache.getInstance();
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        NavigationCheck uc = new NavigationCheck();
        uc.checkAdd(request);

        Navigation nav = new Navigation();
        boolean re = nav.insert(uc.getName(), uc.getLink(), uc.getColor(), uc.getTarget(), uc.getType());
        if (re) {
            createJSFile(uc.getType());
            try {
                rmCache.remove(cachePrix + uc.getType());
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        NavigationCheck uc = new NavigationCheck();
        uc.checkDel(request);

        Navigation nav = new Navigation(uc.getCode(), uc.getType());
        boolean re = nav.del();
        if (re) {
            createJSFile(uc.getType());

            try {
                rmCache.remove(cachePrix + uc.getType());
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;
    }

    public boolean move(HttpServletRequest request) throws ErrMsgException {
        NavigationCheck uc = new NavigationCheck();
        uc.checkMove(request);

        Navigation nav = new Navigation(uc.getCode(), uc.getType());
        boolean re = nav.move(uc.getDirection());
        if (re) {
            createJSFile(uc.getType());

            try {
                rmCache.remove(cachePrix + uc.getType());
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;
    }

    public boolean update(HttpServletRequest request) throws ErrMsgException {
        NavigationCheck uc = new NavigationCheck();
        uc.checkUpdate(request);

        Navigation nav = new Navigation();
        nav.setName(uc.getName());
        nav.setLink(uc.getLink());
        nav.setNewName(uc.getNewName());
        nav.setColor(uc.getColor());
        nav.setTarget(uc.getTarget());
        nav.setType(uc.getType());
        nav.setCode(uc.getCode());
        boolean re = nav.store();
        if (re) {
            createJSFile(uc.getType());

            try {
                rmCache.remove(cachePrix + uc.getType());
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return re;
    }

    public Navigation getNav(String code, String type) {
        return new Navigation(code, type);
    }

    public Vector getAllNav(String type) {
        Vector navlist = (Vector)rmCache.get(cachePrix + type);
        if (navlist!=null)
            return navlist;
        else {
            Navigation nav = new Navigation();
            // 取得所有导航条栏目名称
            Vector v = nav.getAllNavName(type);
            try {
                if (v != null) {
                    Vector vt = new Vector();
                    Iterator ir = v.iterator();
                    // 取得所有栏目置入vt
                    while( ir.hasNext()) {
                        Navigation n = getNav((String)ir.next(), type);
                        vt.addElement(n);
                    }
                    // 将vt置于缓存
                    rmCache.put(cachePrix + type, vt);
                    return vt;
                }
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return navlist;
    }

    /**
     * 创建JS文件，用于显示CMS导航条
     * @return boolean
     */
    public void createJSFile(String type) {
        if (!type.equals(Navigation.TYPE_CMS))
            return;
        NavigationMgr nmr = new NavigationMgr();
        if (type.equals(""))
            type = Navigation.TYPE_CMS;

        Vector v = nmr.getAllNav(type);
        int count = 0;
        int size = v.size();
        Iterator ir = v.iterator();
        StringBuffer str = new StringBuffer();
        str.append("<ul>");
        while (ir.hasNext()) {
            Navigation nav = (Navigation)ir.next();
            String target = "";
            if (!nav.getTarget().equals(""))
                target = "target=" + nav.getTarget();
            if (nav.getLink().equals(Navigation.LINK_COLUMN)) {
                cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
                boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
                LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(Leaf.ROOTCODE);
                Vector vt = lccm.getDirList();
                Iterator cir = vt.iterator();
                int cc = vt.size();
                int k = 0;
                String path = Global.virtualPath.equals("")?"/":"/" + Global.virtualPath + "/";
                while (cir.hasNext()) {
                    Leaf lf = (Leaf) cir.next();

                    if (lf.getIsHome()) {
                        String url = "";
                        if (isHtml) {
                            url = path + lf.getListHtmlPath() + "/index." +
                                          cfg.getProperty("cms.html_ext");
                        } else {
                            url = path + "doc_column_view.jsp?dirCode=" + StrUtil.UrlEncode(lf.getCode());
                        }
                        str.append("<li><a " + target + " href='" + url +
                                   "'>" +
                                   lf.getName() + "</a></li>");
                        if (k < cc)
                            str.append("<li class='seperator'></li>");
                    }
                    k++;
                }
            }
            else {
                str.append("<li>");

                String color = StrUtil.getNullString(nav.getColor());
                String name = "";
                if (color.equals(""))
                    name = nav.getName();
                else
                    name = "<font color='" + color + "'>" + nav.getName() +
                           "</font>";

                str.append("<a target='_parent' href='" + nav.getLink() + "'>" +
                           name + "</a>");
                str .append("</li>");
                if (count < size)
                    str.append("<li class='seperator'></li>");
                count++;
            }
        }
        str.append("</ul>");
        String s = str.toString();
        s = "document.write(\"" + s + "\");";

        FileUtil fu = new FileUtil();
        fu.WriteFile(Global.getRealPath() + "doc/js/nav_" + type + ".js",
             s, "UTF-8");
    }

}
