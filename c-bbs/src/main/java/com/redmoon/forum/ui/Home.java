package com.redmoon.forum.ui;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import cn.js.fan.web.SkinUtil;

import org.apache.log4j.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.ForumDb;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.MsgMgr;
import com.redmoon.forum.SQLBuilder;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.person.UserMgr;
import com.redmoon.forum.plugin.DefaultRender;

import cn.js.fan.cache.jcs.RMCache;

public class Home {
    final String HOTIDS = "FORUM_HOME_HOTIDS";
    final String FLASHIMAGES = "FORUM_HOME_FLASHIMAGES";
    final String SIDEBLOCKS = "FORUM_SIDE_BLOCKS";

    final String group = "FORUM_HOME_CACHE";

    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_forum_home.xml";

    private String cfgpath;

    Logger logger;

    public static Home home = null;

    private static Object initLock = new Object();

    Document doc = null;
    Element root = null;
    
    public Home() {
    }

    public void init() {
        logger = Logger.getLogger(Home.class.getName());
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);
        properties = new XMLProperties(cfgpath);
        
        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        }        
    }
    
    public Element getRoot() {
        return root;
    }

    public static Home getInstance() {
        if (home == null) {
            synchronized (initLock) {
                home = new Home();
                home.init();
            }
        }
        return home;
    }

    public String getDesc(HttpServletRequest request, String name) {
        return SkinUtil.LoadString(request, "res.config.config_forum_home", name);
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
    
    /**
     * 获取可显示的区块
     * @return
     */
    public Vector getBlocks() {
    	Vector v = null;
        try {
            v = (Vector) RMCache.getInstance().getFromGroup(SIDEBLOCKS, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        else {
	    	v = new Vector();
	    	
	    	List list = root.getChild("sideBar").getChild("blocks").getChildren();
	    	if (list==null)
	    		return v;
	    	Iterator ir = list.iterator();
	    	while (ir.hasNext()) {
	    		Element e = (Element)ir.next();
	    		String isDisplay = e.getAttribute("isDisplay").getValue();
	    		if (isDisplay.equals("true")) {
	    			String[] ary = new String[5];
	    			String t = e.getText();
	    			String[] blks = StrUtil.split(t, ",");
	    			ary[0] = blks[0];
	    			ary[1] = blks[1];
	    			ary[2] = blks[2];
	    			ary[3] = e.getAttribute("count").getValue();
	    			ary[4] = e.getAttributeValue("isNumber");
	    			v.addElement(ary);
	    		}
	    	}
	    	
            try {
                RMCache.getInstance().putInGroup(SIDEBLOCKS, group, v);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            
	    	return v;
        }
    }
    
    public Vector getBlockList(HttpServletRequest request, String blockItem, int count) {
    	Vector v = new Vector();
    	if (blockItem.equals("none"))
    		return v;
    	if (blockItem.equals("newTopic")) {
    		ForumDb forum = new ForumDb();
			Iterator slideIr = forum.getNewMsgs(count).iterator();
			while (slideIr.hasNext()) {
				MsgDb md = (MsgDb)slideIr.next();
				if (!md.isLoaded())
					continue;				
				String tp = DefaultRender.RenderFullTitle(request, md);
				if (!md.getColor().equals(""))
					tp = "<font color='" + md.getColor() + "'>" + tp + "</font>";
				if (md.isBold())
					tp = "<B>" + tp + "</B>";				
				v.addElement("<a title=\"" + tp + "\" href=\"" + ForumPage.getShowTopicPage(request, md.getId()) + "\">" + tp + "</a>");
			}					    		
    	}
    	else if (blockItem.equals("hotTopic")) {
    		ForumDb forum = new ForumDb();
			Iterator slideIr = forum.getMonthMaxVisitedMsgs(count + 10).iterator();
			int c = 0;
			while (slideIr.hasNext()) {
				MsgDb md = (MsgDb)slideIr.next();
				if (!md.isLoaded())
					continue;
				if (md.getCheckStatus()!=MsgDb.CHECK_STATUS_PASS)
					continue;
				String tp = DefaultRender.RenderFullTitle(request, md);
				if (!md.getColor().equals(""))
					tp = "<font color='" + md.getColor() + "'>" + tp + "</font>";
				if (md.isBold())
					tp = "<B>" + tp + "</B>";				
				v.addElement("<a title=\"" + tp + "\" href=\"" + ForumPage.getShowTopicPage(request, md.getId()) + "\">" + tp + "</a>");
				c++;
				if (c==10)
					break;
			}
    	}
    	else if (blockItem.equals("recommandTopic")) {
			Home home = Home.getInstance();
			int[] hotIds = home.getHotIds();
			int hotlen = hotIds.length;
			MsgMgr mm = new MsgMgr();
			if (hotlen>0) {
				for (int k=0; k<hotlen; k++) {
					MsgDb md = mm.getMsgDb(hotIds[k]);
					if (!md.isLoaded())
						continue;
					String tp = DefaultRender.RenderFullTitle(request, md);
					if (!md.getColor().equals(""))
						tp = "<font color='" + md.getColor() + "'>" + tp + "</font>";
					if (md.isBold())
						tp = "<B>" + tp + "</B>";				
					v.addElement("<a title=\"" + tp + "\" href=\"" + ForumPage.getShowTopicPage(request, md.getId()) + "\">" + tp + "</a>");
				}
			}    		
    	}
    	else if (blockItem.equals("star")) {
    		ForumDb forum = new ForumDb();
			String stars = forum.getStars();
			String[] starAry = StrUtil.split(stars, ",");
			int starLen = 0;
			if (starAry!=null)
				starLen = starAry.length;
			UserMgr um = new UserMgr();
			for (int i=0; i<starLen; i++) {
				UserDb user = um.getUserDbByNick(starAry[i]);
				if (user==null)
					continue;
				String myface = user.getMyface();
				String RealPic = user.getRealPic(); 
				StringBuffer sb = new StringBuffer();
				sb.append("<div style=\"width:68px;height:80px;float:left\">");
				sb.append("<a href=\"" + request.getContextPath() + "/userinfo.jsp?username=" + user.getName() + "\" target=\"_blank\">");
				if (myface==null || myface.equals("")) {
					sb.append("<img width=\"55\" src=\"" + request.getContextPath() + "/forum/images/face/" + RealPic + "\" alt=\"" + user.getNick() + "\" />");
				}else{
					sb.append("<img width=\"55\" src=\"" + user.getMyfaceUrl(request) + "\" alt=\" " + user.getNick() + "\" />");
				}
				sb.append("</a><br />");
				sb.append("<a href=\"" + request.getContextPath() + "/userinfo.jsp?username=" + user.getName() + "\" target=\"_blank\">" + user.getNick() + "</a>");
				sb.append("</div>");
				v.addElement(sb.toString());
			}    		
    	}
    	else if (blockItem.equals("rankPost")) {
			int sideK = 1;
			UserDb sideBarUd = new UserDb();
			Iterator irSideBarUser = sideBarUd.list(SQLBuilder.getRankAddCount(),0,count-1).iterator();
			while(irSideBarUser.hasNext()){
				sideBarUd = (UserDb)irSideBarUser.next();
				v.addElement("<a target=\"_blank\" href=\"" + request.getContextPath() + "/userinfo.jsp?username=" + StrUtil.UrlEncode(sideBarUd.getName()) + "\">" + sideBarUd.getNick() + "(" + sideBarUd.getAddCount() + ")</a>");
			}
    	}
    	else if (blockItem.equals("newUsers")) {
			UserDb sideBarUd = new UserDb();
			Iterator irSideBarUser = sideBarUd.list(SQLBuilder.getNewUsers(),0,count-1).iterator();
			while(irSideBarUser.hasNext()){
				sideBarUd = (UserDb)irSideBarUser.next();
				v.addElement("<a target=\"_blank\" href=\"" + request.getContextPath() + "/userinfo.jsp?username=" + StrUtil.UrlEncode(sideBarUd.getName()) + "\">" + sideBarUd.getNick() + "</a>");
			}    		
    	}
    	else if (blockItem.equals("managers")) {
    		UserDb sideBarUd = new UserDb();
			Iterator irSideBarUser = sideBarUd.list("select distinct name from sq_boardmanager",0,count-1).iterator();
			while(irSideBarUser.hasNext()){
				sideBarUd = (UserDb)irSideBarUser.next();
				v.addElement("<a target=\"_blank\" href=\"" + request.getContextPath() + "/userinfo.jsp?username=" + StrUtil.UrlEncode(sideBarUd.getName()) + "\">" + sideBarUd.getNick() + "</a>");
			}    		
    	}
    	else if (blockItem.equals("userDefine")) {
    		v.addElement(getProperty("sideBar.userDefine"));
    	}
    	return v;
    }

    public int[] getHotIds() {
        int[] v = new int[0];
        try {
            v = (int[]) RMCache.getInstance().getFromGroup(HOTIDS, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            String ids = StrUtil.getNullString(home.getProperty("hot"));
            if (!ids.equals("")) {
                ids = ids.replaceAll("，", ",");
                String[] sv = ids.split(",");
                int len = sv.length;
                v = new int[len];
                for (int i = 0; i < len; i++) {
                    v[i] = StrUtil.toInt(sv[i], -1);
                }
                if (v.length > 0) {
                    try {
                        RMCache.getInstance().putInGroup(HOTIDS, group, v);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        }
        if (v == null)
            return new int[0];
        else
            return v;
    }

    public String[][] getFlashImages() {
        String[][] v = null;
        try {
            v = (String[][]) RMCache.getInstance().getFromGroup(FLASHIMAGES, group);
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
    
    public void writemodify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            refresh();
        }
    }    
}
