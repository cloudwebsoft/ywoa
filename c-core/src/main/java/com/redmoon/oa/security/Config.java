package com.redmoon.oa.security;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

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

public class Config {

    private XMLProperties properties;
    private Element root = null;

    public static Config cfg = null;

    private static final Object initLock = new Object();

    public Config() {
    }

    public XMLProperties getProperties() {
        return properties;
    }

    public void init() {
        try {
            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            String xml = configUtil.getXml("config_security");

            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(new InputSource(new StringReader(xml)));
            root = doc.getRootElement();
            properties = new XMLProperties("config_security", doc, true);
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        }
    }

    public Element getRoot() {
        return root;
    }

    public static Config getInstance() {
        if (cfg == null || cfg.properties == null) {
            synchronized (initLock) {
                cfg = new Config();
                cfg.init();
            }
        }
        return cfg;
    }

    public String getProperty(String name) {
        String str = "";
        try {
            str = StrUtil.getNullStr(properties.getProperty(name));
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return str;
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
        return "true".equals(getProperty(name));
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
        properties.setProperty(name, childAttributeName, childAttributeValue, value);
        refresh();
    }

    public void setProperty(String name, String childAttributeName,
                            String childAttributeValue, String subChildName,
                            String value) {
        properties.setProperty(name, childAttributeName, childAttributeValue, subChildName, value);
        refresh();
    }

    public void refresh() {
        cfg = null;
    }

    /**
     * 判断是否强制修改初始密码
     *
     * @return boolean
     */
    public boolean isForceChangeInitPassword() {
        return getBooleanProperty("password.isForceChangeInitPassword");
    }

    public boolean isForceChangeWhenWeak() {
        return getBooleanProperty("password.isForceChangeWhenWeak");
    }

    public int getStrenthLevelMin() {
        return getIntProperty("password.strenthLevelMin");
    }

    /**
     * 取得初始密码
     *
     * @return String
     */
    public String getInitPassword() {
        return getProperty("password.initPassword");
    }

    /**
     * 是否防暴力破解
     *
     * @return boolean
     */
    public boolean isDefendBruteforceCracking() {
        return getBooleanProperty("isDefendBruteforceCracking");
    }

    /**
     * 是否记住用户名
     *
     * @return boolean
     */
    public boolean isRememberUserName() {
        return getBooleanProperty("isRememberUserName");
    }

    /**
     * 框架页允许的地址
     *
     * @return String
     */
    public String getFrameAllowFrom() {
        return getProperty("frameAllowFrom");
    }
}
