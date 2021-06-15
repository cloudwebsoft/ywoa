package cn.js.fan.module.cms;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.io.*;
import java.net.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.*;
import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Config {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_cms.xml";

    private String cfgpath;

    Logger logger;
    org.jdom.Document doc = null;
    Element root = null;

    final String rootChild = "cms";

    final String cacheGroup = "cms_cfg";

    public Config() {
        logger = Logger.getLogger(Config.class.getName());
    }

    public void init() {
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
            logger.error("Config:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("Config:" + e.getMessage());
        }
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(cacheGroup);
        }
        catch (Exception e) {
            logger.error("refresh:" + e.getMessage());
        }
    }

    public Element getRootElement() {
        if (root==null)
            init();
        return root;
    }

    public String getProperty(String name) {
        String v = null;
        try {
            v = (String) RMCache.getInstance().getFromGroup(name, cacheGroup);
        }
        catch (Exception e) {
            logger.error("getProperty1:" + e.getMessage());
        }
        if (v==null) {
            if (root == null)
                init();
            v = properties.getProperty(name);
            if (v!=null) {
                try {
                    RMCache.getInstance().putInGroup(name, cacheGroup, v);
                }
                catch (Exception e) {
                    logger.error("getProperty2:" + e.getMessage());
                }
            }
        }
        return v;
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
    }

    public String getDescription(String name) {
        if (root==null)
            init();
        Element which = root.getChild(rootChild).getChild(name);
        // System.out.println("name=" + name + " which=" + which);
        if (which == null)
            return null;
        return which.getAttribute("desc").getValue();
    }

    public boolean put(String name, String value) {
        if (root==null)
            init();
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null)
            return false;
        which.setText(value);
        writemodify();
        if (name.equals("refresh_doc_relate_interval"))
            DocCacheMgr.FULLTEXTMAXLIFE = getIntProperty("cms.refresh_doc_relate_interval");
        return true;
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
        } catch (java.io.IOException e) {}
        refresh();
    }
}
