package com.redmoon.forum.plugin.dig;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.net.*;

import cn.js.fan.util.*;
import org.apache.log4j.*;
import org.jdom.Document;
import org.jdom.Element;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.input.SAXBuilder;
import java.io.FileInputStream;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;
import org.jdom.output.XMLOutputter;
import java.io.FileOutputStream;
import org.jdom.output.Format;

public class DigConfig {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_forum_dig.xml";

    private String cfgpath;

    Logger logger;

    Document doc = null;
    Element root = null;

    public static DigConfig cfg = null;

    private static Object initLock = new Object();

    public DigConfig() {
    }

    public void init() {
        logger = Logger.getLogger(DigConfig.class.getName());
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
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        }

    }

    public Element getRoot() {
        return root;
    }

    public static DigConfig getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new DigConfig();
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

    public String getDescription(HttpServletRequest request, String name) {
        return SkinUtil.LoadString(request, "res.forum.plugin.dig", name);
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
        }

        cfg = null;
    }
}
