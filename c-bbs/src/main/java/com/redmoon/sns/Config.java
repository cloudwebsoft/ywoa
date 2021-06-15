package com.redmoon.sns;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.io.*;
import java.net.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_sns.xml";
    private String cfgpath;
    Document doc = null;
    Element root = null;
    final String rootChild = "sns";
    public static Config config;
    private static Object initLock = new Object();

    public static Config getInstance() {
        if (config == null) {
            synchronized (initLock) {
                config = new Config();
                config.init();
            }
        }
        return config;
    }

    public void init() {
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
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        }
    }

    public void refresh() {
        init();
        properties.refresh();
    }

    public Element getRootElement() {
        return root;
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
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

    public String getDescription(String name) {
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null)
            return null;
        return which.getAttribute("desc").getValue();
    }
    
    public boolean isOpen() {
    	return getBooleanProperty("sns.isOpen");
    }

    /**
     * 更新name项的值，并保存至文件
     * @param name String
     * @param value String
     * @return boolean
     */
    public boolean put(String name, String value) {
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null)
            return false;
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
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
        refresh();
    }
}
