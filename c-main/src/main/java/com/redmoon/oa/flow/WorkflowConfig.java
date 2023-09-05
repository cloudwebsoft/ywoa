package com.redmoon.oa.flow;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.Vector;

public class WorkflowConfig {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_flow.xml";

    org.jdom.Document doc = null;
    org.jdom.Element root = null;

    public static WorkflowConfig cfg = null;
    private static Object initLock = new Object();

    public Vector assessLevels;

    public WorkflowConfig() {
    }

    public void init() {
        try {
            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            String xml = configUtil.getXml(CONFIG_FILENAME);

            SAXBuilder sb = new SAXBuilder();
            doc = sb.build(new InputSource(new StringReader(xml)));
            root = doc.getRootElement();
            properties = new XMLProperties("config", doc, true);
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
        } else {
            return -65536;
        }
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return "true".equals(p);
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
