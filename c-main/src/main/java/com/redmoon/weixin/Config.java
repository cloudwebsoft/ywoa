package com.redmoon.weixin;


import java.io.*;
import java.net.*;
import java.util.List;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;
import org.apache.log4j.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Config {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_weixin.xml";

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
    
    public boolean isUserIdUseEmail() {
    	return getBooleanProperty("isUserIdUseEmail");
    }
    
    public boolean isUserIdUseAccount() {
        return getBooleanProperty("isUserIdUseAccount");
    }
    
    public boolean isUserIdUseMobile() {
        return getBooleanProperty("isUserIdUseMobile");
    }
    
    public String getSecretOfAgent(String agentId) {
    	return getAgentAttr(agentId, "secret");
    }

    public String getAgentAttr(String agentId, String attrName) {
        Element agentMenu = root.getChild("agentMenu");
        List<Element> menus = agentMenu.getChildren("item");

        for(Element ele:menus){
            String aId = ele.getChild("agentId").getText();
            if (aId.equals(agentId)) {
                return ele.getChild(attrName).getText();
            }
        }

        LogUtil.getLog(this.getClass()).error("未找到" + agentId + "的：" + attrName);
        return "";
    }

    /**
     * 取得默认应用的id
     * @return
     */
    public String getDefaultAgentId() {
        Element agentMenu = root.getChild("agentMenu");
        List<Element> menus = agentMenu.getChildren("item");
        for(Element ele:menus) {
            return ele.getChild("agentId").getText();
        }
        return "";
    }

    public boolean setAgent(String oldAgentId, String agentId, String agentName, String secret, String logo, String homeUrl) {
        Element agentMenu = root.getChild("agentMenu");
        List<Element> menus = agentMenu.getChildren("item");
        for(Element ele:menus){
            String aId = ele.getChild("agentId").getText();
            if (aId.equals(oldAgentId)) {
                ele.getChild("agentId").setText(agentId);
                ele.getChild("agentName").setText(agentName);
                ele.getChild("secret").setText(secret);
                ele.getChild("logo").setText(logo);
                ele.getChild("homeUrl").setText(homeUrl);

                String indent = "    ";
                boolean newLines = true;
                // XMLOutputter outp = new XMLOutputter(indent, newLines, "gb2312");
                Format format = Format.getPrettyFormat();
                format.setIndent(indent);
                format.setEncoding("utf-8");
                XMLOutputter outp = new XMLOutputter(format);
                try {
                    FileOutputStream fout = new FileOutputStream(cfgpath);
                    outp.output(doc, fout);
                    fout.close();
                }
                catch (java.io.IOException e) {}
                return true;
            }
        }
        return false;
    }
}
