package com.redmoon.oa.worklog;

import java.io.*;
import java.net.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;
import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;
import java.util.*;

public class Config {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_work_log.xml";

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
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);
        properties = new XMLProperties(cfgpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            
            /*
            List list = cfg.root.getChild("levels").getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element e = (Element) ir.next();
                    AssessLevel al = new AssessLevel();
                    al.setCode(e.getChildText("code"));
                    al.setName(e.getChildText("name"));
                    al.setScore(StrUtil.toInt(e.getChildText("score")));
                    al.setPercent(StrUtil.toDouble(e.getChildText("percent")));
                    al.setDesc(e.getChildText("desc"));
                    assessLevels.addElement(al);
                }
            }
            */

            fin.close();
        } catch (org.jdom.JDOMException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error("init2:" + e.getMessage());
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
            }
        }
        return cfg;
    }

    public static void reload() {
        cfg = null;
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
    }

    public void setProperty(String name, String childAttributeName,
                            String childAttributeValue, String subChildName,
                            String value) {
        properties.setProperty(name, childAttributeName, childAttributeValue,
                               subChildName, value);
    }

}
