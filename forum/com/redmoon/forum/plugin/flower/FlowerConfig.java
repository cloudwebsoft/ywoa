package com.redmoon.forum.plugin.flower;

import java.io.FileInputStream;
import java.net.URL;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import java.net.URLDecoder;
import cn.js.fan.web.SkinUtil;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.security.LoginMonitor;
import org.jdom.output.XMLOutputter;
import java.io.FileOutputStream;
import org.jdom.output.Format;
import cn.js.fan.cache.jcs.RMCache;

public class FlowerConfig {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_" + FlowerUnit.code + ".xml";

    private String cfgpath;

    Logger logger = Logger.getLogger(this.getClass().getName());
    Document doc = null;
    public Element root = null;

    final String cacheGroup = "Flower_cfg";

    //String DESKey = "fgfkeydw";//DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错

    public FlowerConfig() {
    }

    public void init() {
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
            logger.error("Config:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("Config:" + e.getMessage());
        }
    }

    public Element getRootElement() {
        if (root==null)
            init();
        return root;
    }

    public String getProperty(String name) {
        String v = null;
        try {
            v = (String) RMCache.getInstance().getFromGroup(name, cacheGroup);
        }
        catch (Exception e) {
            logger.error("getProperty1:" + e.getMessage());
        }
        if (v==null) {
            if (root == null)
                init();
            v = properties.getProperty(name);
            if (v!=null) {
                try {
                    RMCache.getInstance().putInGroup(name, cacheGroup, v);
                }
                catch (Exception e) {
                    logger.error("getProperty2:" + e.getMessage());
                }
            }
        }
        return v;
    }

    public int getIntProperty(String name) {
        String p = getProperty(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        }
        else
            return -65536;
    }

    public String getDescription(HttpServletRequest request, String name) {
        return SkinUtil.LoadString(request, "res.config.config_flower", name);
    }

    public boolean put(String name, String value) {
        if (root==null)
            init();
        Element which = root.getChild(name);
        if (which == null)
            return false;
        which.setText(value);
        writemodify();
        return true;
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public void writemodify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("gb2312");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
        refresh();
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(cacheGroup);
        }
        catch (Exception e) {
            logger.error("refresh:" + e.getMessage());
        }
    }
}
