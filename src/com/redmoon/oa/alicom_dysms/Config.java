package com.redmoon.oa.alicom_dysms;


import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

public class Config {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_alicom_sms.xml";

    private String cfgpath;

    Logger logger;

    Document doc = null;
    Element root = null;

    public static Config cfg = null;
    private static Object initLock = new Object();

    public Config() {
    }

    @SuppressWarnings("deprecation")
	public void init() {
        logger = Logger.getLogger(Config.class.getName());
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
        } catch (JDOMException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        }

    }

    public Element getRoot() {
        return root;
    }

    public synchronized static Config getInstance() {
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
    
    public boolean isUseAliDysms () {
    	return getBooleanProperty("isUse");
    }
    

}
