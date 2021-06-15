package com.redmoon.forum;

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

import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.security.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 *
 * <p>Title: 论坛配置</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_forum.xml";
    private String cfgpath;
    Document doc = null;
    Element root = null;
    final String rootChild = "forum";
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

        // 对ftpUrl进行规则化，如果不是以/结尾，则在末尾加上/
        String ftpUrl = getProperty("forum.ftpUrl");

        // System.out.println(getClass() + " init:" + ftpUrl);
        if (ftpUrl!=null) { // 防止配置文件中没有ftpUrl时，致升级或初始化出现问题
            if (ftpUrl.lastIndexOf("/") != ftpUrl.length() - 1) {
                ftpUrl += "/";
                properties.setProperty("forum.ftpUrl", ftpUrl);
            }
        }
        else {
            System.out.println(getClass() + " init:ftpUrl is not found in " + CONFIG_FILENAME);
        }

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

        visitInterval = getIntProperty("forum.visitInterval");
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
        // System.out.println("name=" + name + " which=" + which);
        if (which == null)
            return null;
        return which.getAttribute("desc").getValue();
    }

    public String getDescription(HttpServletRequest request, String name) {
        return SkinUtil.LoadString(request, "res.config.config_forum", name);
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
        if (name.indexOf("login")!=-1)
            LoginMonitor.initParam();
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

    public String getKey() {
        String key = getProperty("forum.key");
        if (key.length()<24) {
            key = StrUtil.PadString(key, ' ', 24, true);
        }
        else if (key.length()>24) {
            key = key.substring(0, 24);
        }
        return key;
    }

    public String getAttachmentPath() {
        return getProperty("forum.attachmentPath");
    }

    public String getRemoteBaseUrl() {
        return getProperty("forum.ftpUrl");
    }

    public int getVisitInterval() {
        return visitInterval;
    }

    private int visitInterval;
}
