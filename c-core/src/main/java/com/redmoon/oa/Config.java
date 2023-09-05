package com.redmoon.oa;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
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
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;

public class Config {
    Document doc = null;
    Element root = null;

    private static XMLProperties properties = null;
    public static Config cfg = null;
    private static final Object initLock = new Object();

    public Config() {
        init();
    }

    public void init() {
        try {
            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            String xml = configUtil.getXml("config");

            SAXBuilder sb = new SAXBuilder();
            doc = sb.build(new InputSource(new StringReader(xml)));
            root = doc.getRootElement();
            properties = new XMLProperties("config", doc, true);
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
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
        properties = null;
    }

    public boolean isRoleSwitchable() {
        return getBooleanProperty("isRoleSwitchable");
    }

    public boolean isDeptSwitchable() {
        return getBooleanProperty("isDeptSwitchable");
    }

    public Element getRootElement() {
        return root;
    }

    public String getDescription(String name) {
        Element which = root.getChild("oa").getChild(name);
        if (which == null) {
            return null;
        }
        Attribute att = which.getAttribute("desc");
        if (att != null) {
            return att.getValue();
        } else {
            return "";
        }
    }

    public void setIsDisplay(String name, boolean isDisplay) {
        Element which = root.getChild("oa").getChild(name);
        if (which == null) {
            return;
        }
        Attribute att = which.getAttribute("isDisplay");
        if (att != null) {
            att.setValue(String.valueOf(isDisplay));
        } else {
            which.setAttribute("isDisplay", String.valueOf(isDisplay));
        }
        writemodify();
    }

    public String get(String name) {
        return StrUtil.getNullStr(properties.getProperty("oa." + name));
    }

    public int getInt(String name) {
        return StrUtil.toInt(get(name), 0);
    }

    public boolean getBoolean(String name) {
        return "true".equals(get(name));
    }

    public boolean getBooleanProperty(String name) {
        return "true".equals(get(name));
    }

    public boolean put(String name, String value) {
        Element which = root.getChild("oa").getChild(name);
        if (which == null) {
            return false;
        }
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
        configUtil.putXml("config", doc);

        reload();
    }

    public String getKey() {
        return cfg.get("key");
    }
}
