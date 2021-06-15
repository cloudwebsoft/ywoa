package cn.js.fan.module.cms.robot;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import com.cloudwebsoft.framework.util.LogUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_robot.xml";
    private String cfgpath;
    Document doc = null;
    Element root = null;

    final String rootChild = "robot";

    final String cacheGroup = "cws_robot";

    public void init() {
        URL cfgURL = getClass().getResource("/config_robot.xml");

        this.cfgpath = cfgURL.getFile();
        this.cfgpath = URLDecoder.decode(this.cfgpath);

        this.properties = new XMLProperties(this.cfgpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(this.cfgpath);
            this.doc = sb.build(fin);
            this.root = this.doc.getRootElement();
            fin.close();
        } catch (JDOMException e) {
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        }
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup("cws_robot");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("refresh:" + e.getMessage());
        }
    }

    public Element getRootElement() {
        if (this.root == null)
            init();
        return this.root;
    }

    public String getProperty(String name) {
        String v = null;
        try {
            v = (String) RMCache.getInstance().getFromGroup(name, "cws_robot");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getProperty1:" + e.getMessage());
        }
        if (v == null) {
            if (this.root == null)
                init();
            v = this.properties.getProperty(name);
            if (v != null) {
                try {
                    RMCache.getInstance().putInGroup(name, "cws_robot", v);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getProperty2:" + e.getMessage());
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
        return -65536;
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return p.equals("true");
    }

    public void setProperty(String name, String value) {
        this.properties.setProperty(name, value);
    }

    public String getDescription(String name) {
        if (this.root == null)
            init();
        Element which = this.root.getChild("robot").getChild(name);

        if (which == null)
            return null;
        return which.getAttribute("desc").getValue();
    }

    public boolean put(String name, String value) {
        if (this.root == null)
            init();
        Element which = this.root.getChild("robot").getChild(name);
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
            FileOutputStream fout = new FileOutputStream(this.cfgpath);
            outp.output(this.doc, fout);
            fout.close();
        } catch (IOException e) {
        }
        refresh();
    }
}