package com.redmoon.forum.miniplugin.index;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.net.*;

import cn.js.fan.util.*;
import org.apache.log4j.*;
import cn.js.fan.cache.jcs.RMCache;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.MsgDb;
import com.redmoon.forum.MsgMgr;
import com.redmoon.forum.plugin.DefaultRender;
import com.redmoon.forum.ui.ForumPage;

import cn.js.fan.web.SkinUtil;
import cn.js.fan.web.Global;

public class Index {
    final String FLASHIMAGES = "FORUM_INDEX_FLASHIMAGES";

    final String group = "FORUM_INDEX_CACHE";

    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_forum_index.xml";

    private String cfgpath;

    Logger logger;

    public static Index home = null;

    private static Object initLock = new Object();

    public Index() {
    }

    public void init() {
        logger = Logger.getLogger(Index.class.getName());
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);
        properties = new XMLProperties(cfgpath);
    }

    public static Index getInstance() {
        if (home == null) {
            synchronized (initLock) {
                home = new Index();
                home.init();
            }
        }
        return home;
    }

    public String getProperty(String name) {
        return StrUtil.getNullStr(properties.getProperty(name));
    }

    public int getIntProperty(String name) {
        String p = getProperty(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        } else
            return -65536;
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return p.equals("true");
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
        refresh();
    }

    public String getProperty(String name, String childAttributeName,
                              String childAttributeValue) {
        return StrUtil.getNullStr(properties.getProperty(name, childAttributeName,
                                      childAttributeValue));
    }

    public String getProperty(String name, String childAttributeName,
                              String childAttributeValue, String subChildName) {
        return StrUtil.getNullStr(properties.getProperty(name, childAttributeName,
                                      childAttributeValue, subChildName));
    }

    public void setProperty(String name, String childAttributeName,
                            String childAttributeValue, String value) {
        properties.setProperty(name, childAttributeName, childAttributeValue,
                               value);
        refresh();
    }

    public void setProperty(String name, String childAttributeName,
                            String childAttributeValue, String subChildName,
                            String value) {
        properties.setProperty(name, childAttributeName, childAttributeValue,
                               subChildName, value);
        refresh();
    }

    public String getColumnTitle(HttpServletRequest request, int id) {
        String code = getProperty("blocks", "id", "" + id);
        if (code.equals("newtopic")) {
            return SkinUtil.LoadString(request, "res.label.forum.miniplugin.newelitetop", "newtopic");
        }
        else if (code.equals("newelite")) {
            return SkinUtil.LoadString(request, "res.label.forum.miniplugin.newelitetop", "elitetopic");
        }
        else if (code.equals("newtop")) {
            return SkinUtil.LoadString(request, "res.label.forum.miniplugin.newelitetop", "toptopic");
        }
        else if (code.equals("custom")) {
        	return StrUtil.getNullString(home.getProperty("blocks", "id", "name" + id));
        }
        else
            return code;
    }

    public String getColumnContent(HttpServletRequest request, int id, int n, int count) {
        String code = getProperty("blocks", "id", "" + id);
        return list(request, id, code, n, count);
    }

    public String list(HttpServletRequest request, int id, String code, int n, int count) {
        String str = "<ul>";
        if (code.equals("newtopic")) {
            NewEliteTop net = new NewEliteTop();
            Iterator ir = net.listNewMsg(n).iterator();
            int k = 1;
            while (ir.hasNext()) {
                com.redmoon.forum.MsgDb msg = (com.redmoon.forum.MsgDb)ir.next();
                // 用下句可能会使得当UBB产生图片时被截断出现显示问题
				// String tp = DefaultRender.RenderTitle(request, msg, count);
				String tp = cn.js.fan.util.StrUtil.getLeft(msg.getTitle(), count);				
				if (!msg.getColor().equals(""))
					tp = "<font color='" + msg.getColor() + "'>" + tp + "</font>";
				if (msg.isBold())
					tp = "<B>" + tp + "</B>";                
                str += "<li><img src=\"" + Global.getRootPath() + "/forum/miniplugin/index/images/num_" + k + ".gif\" />&nbsp;<a href=\"showtopic.jsp?rootid=" + msg.getId() + "\" title=\"" + DefaultRender.RenderFullTitle(request, msg) + "\">" + tp + "</a></li>";
                k++;
            }
        } else if (code.equals("newelite")) {
            NewEliteTop net = new NewEliteTop();
            Iterator ir = net.listEliteMsg(n).iterator();
            int k = 1;
            while (ir.hasNext()) {
                com.redmoon.forum.MsgDb msg = (com.redmoon.forum.MsgDb)ir.next();
				String tp = cn.js.fan.util.StrUtil.getLeft(msg.getTitle(), count);
				if (!msg.getColor().equals(""))
					tp = "<font color='" + msg.getColor() + "'>" + tp + "</font>";
				if (msg.isBold())
					tp = "<B>" + tp + "</B>";                
                str += "<li><img src=\"" + Global.getRootPath() + "/forum/miniplugin/index/images/num_" + k + ".gif\" />&nbsp;<a href=\"showtopic.jsp?rootid=" + msg.getId() + "\" title=\"" + DefaultRender.RenderFullTitle(request, msg) + "\">" + tp + "</a></li>";
                k++;
            }
        } else if (code.equals("newtop")) {
            NewEliteTop net = new NewEliteTop();
            Iterator ir = net.listTopMsg(n).iterator();
            int k = 1;
            while (ir.hasNext()) {
                com.redmoon.forum.MsgDb msg = (com.redmoon.forum.MsgDb)ir.next();
				String tp = cn.js.fan.util.StrUtil.getLeft(msg.getTitle(), count);
				if (!msg.getColor().equals(""))
					tp = "<font color='" + msg.getColor() + "'>" + tp + "</font>";
				if (msg.isBold())
					tp = "<B>" + tp + "</B>";                
                str += "<li><img src=\"" + Global.getRootPath() + "/forum/miniplugin/index/images/num_" + k + ".gif\" />&nbsp;<a href=\"showtopic.jsp?rootid=" + msg.getId() + "\" title=\"" + DefaultRender.RenderFullTitle(request, msg) + "\">" + tp + "</a></li>";
                k++;
            }
        }
        else if (code.equals("custom")) {
			MsgMgr mm = new MsgMgr();
			MsgDb md = null;
			String[] v = StrUtil.split(home.getProperty("blocks", "id", "ids" + id), ",");
			int len = 0;
			if (v!=null)
				len = v.length;
			if (len!=0) {
				for (int k=0; k<len; k++) {
					long msgId = StrUtil.toInt(v[k], -1);
					if (msgId==-1)
						continue;
					md = mm.getMsgDb(msgId);
					if (md.isLoaded()) {
						String tp = cn.js.fan.util.StrUtil.getLeft(md.getTitle(), count);
						if (!md.getColor().equals(""))
							tp = "<font color='" + md.getColor() + "'>" + tp + "</font>";
						if (md.isBold())
							tp = "<B>" + tp + "</B>";                
		                str += "<li><img src=\"" + Global.getRootPath() + "/forum/miniplugin/index/images/num_" + (k+1) + ".gif\" />&nbsp;<a href=\"showtopic.jsp?rootid=" + md.getId() + "\" title=\"" + DefaultRender.RenderFullTitle(request, md) + "\">" + tp + "</a></li>";
					}
				}
			}
        }
        str += "</ul>";
        return str;
    }

    public String[][] getFlashImages() {
        String[][] v = null;
        try {
            v = (String[][]) RMCache.getInstance().getFromGroup(FLASHIMAGES,
                    group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            v = new String[5][3];
            for (int i = 1; i <= 5; i++) {
                String url = StrUtil.getNullString(home.getProperty("flash",
                        "id", "" + i, "url"));
                String link = StrUtil.getNullString(home.getProperty("flash",
                        "id", "" + i, "link"));
                String text = StrUtil.getNullString(home.getProperty("flash",
                        "id", "" + i, "text"));
                if (!url.equals("")) {
                    v[i - 1][0] = url;
                    v[i - 1][1] = link;
                    v[i - 1][2] = text;
                }
            }

            if (v.length > 0) {
                try {
                    RMCache.getInstance().putInGroup(FLASHIMAGES, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return v;
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
