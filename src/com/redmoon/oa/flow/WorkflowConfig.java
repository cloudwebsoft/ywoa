package com.redmoon.oa.flow;

import java.io.*;
import java.net.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;
import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;
import java.util.*;

public class WorkflowConfig {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_flow.xml";

    private String cfgpath;

    Logger logger;

    org.jdom.Document doc = null;
    org.jdom.Element root = null;

    public static WorkflowConfig cfg = null;
    private static Object initLock = new Object();

    public Vector assessLevels;

    public WorkflowConfig() {
    }

    public void init() {
        logger = Logger.getLogger(WorkflowConfig.class.getName());
        URL cfgURL = getClass().getClassLoader().getResource(
                "/" + CONFIG_FILENAME);
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
            LogUtil.getLog(getClass()).error("init2:" + e.getMessage());
        }
    }

    public Element getRoot() {
        return root;
    }

    public static WorkflowConfig getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new WorkflowConfig();
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

    public Vector getAssessLevels() {
        return assessLevels;
    }

}
