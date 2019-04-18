package cn.js.fan.module.cms.plugin.wiki;

import java.io.*;
import java.net.*;
import java.util.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;
import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Config {
    final String RECOMMAND_IDS = "WIKI_HOME_RECOMMAND_IDS";

    final String group = "WIKI_HOME_CACHE";

    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_wiki.xml";

    private String cfgpath;

    Logger logger;

    Document doc = null;
    Element root = null;

    public static Config cfg = null;

    private static Object initLock = new Object();

    public Config() {
    }

    public void init() {
        logger = Logger.getLogger(Config.class.getName());
        URL cfgURL = getClass().getClassLoader().getResource(
                "/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        // System.out.println("blog.Home.java cfgpath=" + cfgpath);

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

    public static Config getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new Config();
                cfg.init();
                // System.out.println("blog.Home.java home=" + home + " home.doc=" + home.doc);
            }
        }
        return cfg;
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

    public int[] getRecommandIds() {
        int[] v = new int[0];
        try {
            v = (int[]) RMCache.getInstance().getFromGroup(RECOMMAND_IDS, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            String ids = StrUtil.getNullString(cfg.getProperty("recommand"));
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
                        RMCache.getInstance().putInGroup(RECOMMAND_IDS, group, v);
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

    public void refresh() {
    	cfg = null;
        try {
            RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error("refresh:" + e.getMessage());
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
