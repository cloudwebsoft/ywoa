package cn.js.fan.module.cms.ui.menu;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.*;

import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.module.cms.Directory;
import cn.js.fan.module.cms.DocumentMgr;
import cn.js.fan.module.cms.LeafPriv;
import cn.js.fan.module.cms.site.SiteDb;
import cn.js.fan.module.cms.ui.menu.Leaf;
import cn.js.fan.util.*;


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
    public final static String CMS_COLUMN = "cms_column";
    public final static String FORUM_BOARD = "forum_board";
    public final static String BLOG_TYPE = "blog_type";
    public final static String FORUM_PLUGIN = "forum_plugin";
    public final static String FORUM_MINIPLUGIN = "forum_miniplugin";
    public final static String CMS_PLUGIN = "cms_plugin";
    public final static String CMS_SITE = "cms_site";

    public static String getMenuItem(HttpServletRequest request, Leaf lf) {
        if (lf.getPreCode().equals(FORUM_BOARD)) {
            com.redmoon.forum.Directory dir = new com.redmoon.forum.Directory();
            com.redmoon.forum.Leaf leaf = dir.getLeaf(com.redmoon.forum.Leaf.CODE_ROOT);
            String str = "";
            Vector children = leaf.getChildren();
            Iterator ir = children.iterator();
            while (ir.hasNext()) {
            	leaf = (com.redmoon.forum.Leaf) ir.next();
                str += "<li><img align=\"absmiddle\" src=\"" + request.getContextPath() + "/images/icons/arrow.gif\" />&nbsp;&nbsp;<a target=\"mainFrame\" href=\"" + request.getContextPath() + "/forum/admin/dir_frame.jsp?root_code=" + StrUtil.UrlEncode(leaf.getCode()) + "\">" + leaf.getName() + "</a></li>";
            }
            return str;
        }
        else if (lf.getPreCode().equals(BLOG_TYPE)) {
            com.redmoon.blog.Directory dir = new com.redmoon.blog.Directory();
            com.redmoon.blog.Leaf leaf = dir.getLeaf(com.redmoon.blog.Leaf.ROOTCODE);
            String str = "";
            Vector children = leaf.getChildren();
            Iterator ir = children.iterator();
            while (ir.hasNext()) {
            	leaf = (com.redmoon.blog.Leaf) ir.next();
                str += "<li><img align=\"absmiddle\" src=\"" + request.getContextPath() + "/images/icons/arrow.gif\" />&nbsp;&nbsp;<a target=\"mainFrame\" href=\"" + request.getContextPath() + "/blog/admin/dir_frame.jsp?root_code=" + StrUtil.UrlEncode(leaf.getCode()) + "\">" + leaf.getName() + "</a></li>";
            }
            return str;
        }
        else if (lf.getPreCode().equals(FORUM_PLUGIN)) {
        	com.redmoon.forum.plugin.PluginMgr pm = new com.redmoon.forum.plugin.PluginMgr();
        	String str = "";
        	Vector v = pm.getAllPlugin();
        	Iterator ir = v.iterator();
        	while (ir.hasNext()) {
        		com.redmoon.forum.plugin.PluginUnit pu = (com.redmoon.forum.plugin.PluginUnit)ir.next();
        		if (pu!=null && !pu.getAdminEntrance().trim().equals("")) {
        			str += "<li><IMG src=\"" + request.getContextPath() + "/images/icons/arrow.gif\" width=5 align=absMiddle>&nbsp;&nbsp;";
        			str += "<A href=\"" + pu.getAdminEntrance() + "\" target=mainFrame>" + pu.LoadString(request, "adminMenuItem") + "</A></li>";
        		}
        	}
        	return str;
        }
        else if (lf.getPreCode().equals(FORUM_MINIPLUGIN)) {
        	com.redmoon.forum.miniplugin.MiniPluginMgr mpm = new com.redmoon.forum.miniplugin.MiniPluginMgr();
        	Vector mv = mpm.getAllPlugin();
        	if (mv != null) {
				String str = "";
				Iterator ir = mv.iterator();
				while (ir.hasNext()) {
					com.redmoon.forum.miniplugin.MiniPluginUnit pu = (com.redmoon.forum.miniplugin.MiniPluginUnit) ir.next();
					String entrance = StrUtil.getNullStr(pu.getAdminEntrance()).trim();
					if (pu != null && pu.isPlugin() && !entrance.equals("")) {
						str += "<li><IMG src=\"images/arrow.gif\" width=5 align=absMiddle>&nbsp;&nbsp;<A href=\"" + pu.getAdminEntrance() + "\" target=mainFrame>" + pu.LoadString(request, "adminMenuItem") + "</A></li>";
					}
				}
				return str;
			}
        	else
        		return "";
        }
        else
            return "error";
    }
}
