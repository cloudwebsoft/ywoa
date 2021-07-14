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
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

import java.io.*;
import java.net.URL;

import org.apache.log4j.Logger;

import java.net.URLDecoder;

public class Config {
    boolean debug = true;
    final String configxml = "config.xml";
    String xmlpath = "";
    Document doc = null;
    Element root = null;
    Logger logger = Logger.getLogger(Config.class.getName());

    private static XMLProperties properties = null;
    public static Config cfg = null;
    private static Object initLock = new Object();

    public Config() {
        init();
    }

    public void init() {
        URL confURL = getClass().getResource("/" + configxml);
        xmlpath = confURL.getFile();
        xmlpath = URLDecoder.decode(xmlpath);
        if (properties==null) {
            properties = new XMLProperties(xmlpath);
        }

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(xmlpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
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
        return get(name).equals("true");
    }

    public boolean getBooleanProperty(String name) {
        return get(name).equals("true");
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
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(xmlpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {
        }
        reload();
    }
}
