package com.redmoon.forum.ui;

import javax.servlet.http.HttpServletRequest;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.Config;
import com.redmoon.forum.ThreadTypeDb;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.MsgMgr;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.plugin.PluginMgr;
import com.redmoon.forum.plugin.PluginUnit;

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
public class ForumPage {
    public static boolean isHtmlPage = false;
    public static boolean isListTree = false;

    public static String COOKIE_IS_FRAME = "isFrame";
    public static String COOKIE_IS_SIDEBAR = "isSideBar";

    static {
        init();
    }

    public ForumPage() {
    }

    public static void init() {
        Config cfg = Config.getInstance();
        isHtmlPage = cfg.getBooleanProperty("forum.isHtmlPage");
        isListTree = cfg.getBooleanProperty("forum.isListTopicTree"); // 列表页采用树形显示
    }

    public static String getListTopicPage(HttpServletRequest request, String boardCode) {
        int mode = isListTree?1:0;
        return getListTopicPage(request, boardCode, mode);
    }

    /**
     * 当无子类别时，取得贴子列表页
     * @param request HttpServletRequest
     * @param mode int 0-平板 1-树形
     * @return String
     */
    public static String getListTopicPage(HttpServletRequest request, String boardCode, int mode) {
        return getListTopicPage(request, boardCode, mode, 1, ThreadTypeDb.THREAD_TYPE_NONE);
    }

    /**
     * 取得贴子列表页面的名称
     * @param request HttpServletRequest
     * @param boardCode String
     * @param mode int 0-平板 1-树形
     * @param CPages int
     * @param threadType int
     * @return String
     */
    public static String getListTopicPage(HttpServletRequest request, String boardCode, int mode, int CPages, int threadType) {
        String page = "";
        if (isHtmlPage) {
            page = "f-" + mode + "-" + boardCode + "-" + CPages + "-" + threadType + ".html";
        }
        else {
            if (mode==0) {
                page = "listtopic.jsp?boardcode=" + boardCode + "&CPages=" + CPages + "&threadType=" + threadType;
            }
            else if (mode==1) {
                page = "listtopic_tree.jsp?boardcode=" + boardCode + "&CPages=" +
                       CPages + "&threadType=" + threadType;
            }
        }
        return page;
    }

    public static String getShowTopicPage(HttpServletRequest request, long rootid) {
        return getShowTopicPage(request, rootid, 1);
    }

    public static String getShowTopicPage(HttpServletRequest request, long rootid, int CPages) {
        return getShowTopicPage(request, 0, rootid, rootid, CPages, "");
    }

    public static String getShowTopicPage(HttpServletRequest request, long rootid, int CPages, String anchor) {
        return getShowTopicPage(request, 0, rootid, rootid, CPages, anchor);
    }

    /**
     * 根据是否伪静态、插件、锚点等取得显示贴子页面的名称
     * @param request HttpServletRequest
     * @param mode int 0-平板 1-树形
     * @param rootid long
     * @param showid long
     * @param CPages int
     * @param anchor String
     * @return String
     */
    public static String getShowTopicPage(HttpServletRequest request, int mode, long rootid, long showid, int CPages, String anchor) {
        String page = "";
        // 根据挂在贴子上的pluginCode，获取显示页，目前此处还没有已定制好的插件显示页，便于将来扩展
        MsgMgr mm = new MsgMgr();
        MsgDb md = mm.getMsgDb(rootid);
        String pluginCode = md.getPluginCode();
        if (pluginCode!=null && !pluginCode.equals("")) {
            PluginMgr pm = new PluginMgr();
            PluginUnit pu = pm.getPluginUnit(md.getPluginCode());
            if (pu==null) {
            	LogUtil.getLog(ForumPage.class.getName()).error("Plugin " + md.getPluginCode() + " is not found.");
            }
            else {
	            page = pu.getShowTopicPage();
	            if (!page.equals("")) {
	                page = page + "?rootid=" + rootid + "&CPages=" + CPages;
	                return page;
	            }
            }
        }

        String showUserName = "";
        if (request!=null)
            showUserName = ParamUtil.get(request, "showUserName");
        if (mode==0) {
            // 只显示楼主的贴子
            if (!showUserName.equals("")) {
                page = "showtopic.jsp?rootid=" + rootid + "&CPages=" + CPages + "&showUserName=" + StrUtil.UrlEncode(showUserName);
                return page;
            }
        }

        if (isHtmlPage) {
            // t-0-200-1-201.html --> showtopic.jsp?rootid=200&CPages=1#201
            // t-1-200-200.html   --> showtopic_tree.jsp?rootid=200&showid=200
            if (mode == 1) {
                page = "t-" + mode + "-" + rootid + "-" + showid + ".html";
            } else {
                if (anchor.equals("")) {
                    page = "t-" + mode + "-" + rootid + "-" + CPages +
                           ".html";
                } else {
                    page = "t-" + mode + "-" + rootid + "-" + CPages + ".html#" + anchor;
                }
            }
        } else {
            if (mode == 0) {
                if (anchor.equals("")) {
                    page = "showtopic.jsp?rootid=" + rootid + "&CPages=" +
                           CPages;
                } else {
                    page = "showtopic.jsp?rootid=" + rootid + "&CPages=" +
                           CPages + "#" + anchor;
                }
            } else {
                page = "showtopic_tree.jsp?rootid=" + rootid + "&showid=" +
                       showid;
            }
        }

        return page;
    }
}
