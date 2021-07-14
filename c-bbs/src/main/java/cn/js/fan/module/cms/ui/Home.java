package cn.js.fan.module.cms.ui;

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
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.input.SAXBuilder;
import java.io.FileInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import java.io.FileOutputStream;
import org.jdom.output.Format;
import java.util.List;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.Global;
import java.util.Vector;
import org.jdom.Attribute;

public class Home {
    final String FLASHIMAGES = "CMS_HOME_FLASHIMAGES";
    final String SCROLLIMAGES = "CMS_HOME_SCROLLIMAGES";
    final String group = "CMS_HOME_CACHE";

    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_cms_home.xml";

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

        } finally {
            refresh();
        }
    }

    public void delImgFromImageScroll(String id) throws ErrMsgException {
        List list = root.getChild("images").getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String eid = child.getAttributeValue("id");
                if (eid.equals(id)) {
                    root.getChild("images").removeContent(child);
                    writemodify();
                    break;
                }
            }
        }
    }

    /**
     * 对滚动图片排序,xml文件中的排序
     */
    public void sortScrollImages() {
        init();
        List list = root.getChild("images").getChildren();
        Vector v = new Vector();
        for (int i=0; i<list.size(); i++) {
            Element e = (Element)list.get(i);
            int order = StrUtil.toInt(e.getChildText("order"), 1);

            String id = e.getAttributeValue("id");
            String url = e.getChildText("url");
            String link = e.getChildText("link");
            String text = e.getChildText("text");

            String[] a = new String[5];
            a[0] = id;
            a[1] = url;
            a[2] = link;
            a[3] = text;
            a[4] = "" + order;

            int size = v.size();
            if (i==0) {
                v.addElement(a);
            }
            else {
                String[] first = (String[])v.elementAt(0);
                String[] last = (String[])v.elementAt(size-1);
                if (order<StrUtil.toInt(first[4])) {
                    v.add(0, a);
                }
                else if (order>StrUtil.toInt(last[4])) {
                    v.addElement(a);
                }
                else {
                    for (int j=1; j<=size-1; j++) {
                        String[] b = (String[])v.elementAt(j-1);
                        String[] c = (String[])v.elementAt(j);
                        int bo = StrUtil.toInt(b[4]);
                        int co = StrUtil.toInt(c[4]);
                        if (order>=bo && order<co) {
                            v.add(j, a);
                            break;
                        }
                    }
                }
            }
        }


        Element images = root.getChild("images");
        images.removeChildren("img");
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            String[] a = (String[])ir.next();
            Element img = new Element("img");
            img.setAttribute(new Attribute("id", a[0]));
            Element u = new Element("url");
            u.setText(a[1]);
            img.addContent(u);
            Element l = new Element("link");
            l.setText(a[2]);
            img.addContent(l);
            Element t = new Element("text");
            t.setText(a[3]);
            img.addContent(t);
            Element o = new Element("order");
            o.setText(a[4]);
            img.addContent(o);

            images.addContent(img);
        }
        writemodify();
    }

    public String[][] getScrollImages() {
        String[][] v = null;
        try {
            v = (String[][]) RMCache.getInstance().getFromGroup(SCROLLIMAGES,
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
            List list = root.getChild("images").getChildren();
            int rows = list.size();
            v = new String[rows][3];
            Iterator ir = list.iterator();
            int i = 0;
            while (ir.hasNext()) {
                Element e = (Element)ir.next();
                v[i][0] = e.getChildText("url");
                v[i][1] = e.getChildText("link");
                v[i][2] = e.getChildText("text");
                i++;
            }

            if (v.length > 0) {
                try {
                    RMCache.getInstance().putInGroup(SCROLLIMAGES, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return v;
    }
}
