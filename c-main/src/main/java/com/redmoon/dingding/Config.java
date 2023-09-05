package com.redmoon.dingding;


import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

public class Config {

    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_dingding.xml";

    Document doc = null;
    Element root = null;

    public static Config cfg = null;
    private static Object initLock = new Object();

    public Config() {
    }

    @SuppressWarnings("deprecation")
	public void init() {
        try {
            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            String xml = configUtil.getXml(CONFIG_FILENAME);

            SAXBuilder sb = new SAXBuilder();
            doc = sb.build(new InputSource(new StringReader(xml)));
            root = doc.getRootElement();
            properties = new XMLProperties(CONFIG_FILENAME, doc, true);
        } catch (JDOMException | IOException e) {
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
        } else {
            return -65536;
        }
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
    public boolean isUseDingDing(){
        return  getBooleanProperty("isUse");
    }
    public int  isUserIdUse() {
    	return getIntProperty("isUserIdUse");
    }
    public String getCropId() {
        return getProperty("CORP_ID");
    }
    public String getCropSecret() {
        return getProperty("CORP_SECRET");
    }
    public String getToken() {
        return getProperty("TOKEN");
    }
    public String getAesKey() {
        return getProperty("AES_KEY");
    }
    public String getEventChangeReviceURL() {
        return getProperty("EventChangeReviceURL");
    }

}
