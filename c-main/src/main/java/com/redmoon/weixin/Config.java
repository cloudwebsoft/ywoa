package com.redmoon.weixin;


import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class Config {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_weixin.xml";

    Document doc = null;
    Element root = null;

    public static Config cfg = null;
    private static Object initLock = new Object();

    public static final int LOGIN_MODE_ACCOUNT = 0;
    public static final int LOGIN_MODE_OPENID = 1;
    public static final int LOGIN_MODE_UNIONID = 2;

    public Config() {
    }

    @SuppressWarnings("deprecation")
	public void init() {
        SAXBuilder sb = new SAXBuilder();
        try {
            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            String xml = configUtil.getXml(CONFIG_FILENAME);

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

                ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
                configUtil.putXml(CONFIG_FILENAME, doc);

                return true;
            }
        }
        return false;
    }
}
