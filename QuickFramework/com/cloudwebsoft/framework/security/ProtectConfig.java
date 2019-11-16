package com.cloudwebsoft.framework.security;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import org.jdom.Document;
import java.io.FileOutputStream;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.apache.log4j.Logger;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.cache.jcs.RMCache;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;

public class ProtectConfig {
    RMCache rmCache;
    final String group = "CONFIG_PRORECT";
    final String ALLPRORECT = "ALLPRORECT";
    final String ALLUNPRORECT = "ALLUNPRORECT";

    static Logger logger;
    public final String FILENAME = "config_protect.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public ProtectConfig() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource(FILENAME);
    }

    public static void init() {
        if (!isInited) {
            // xmlPath = confURL.getPath(); // 如果有空格，会转换为%20
            xmlPath = confURL.getFile();
            try {
                xmlPath = URLDecoder.decode(xmlPath, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            SAXBuilder sb = new SAXBuilder();
            try {
                FileInputStream fin = new FileInputStream(xmlPath);
                doc = sb.build(fin);
                root = doc.getRootElement();
                fin.close();
                isInited = true;
            } catch (org.jdom.JDOMException e) {
                logger.error(e.getMessage());
            } catch (java.io.IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public Element getRootElement() {
        return root;
    }

    public void reload() {
        isInited = false;
        try  {
            rmCache.invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Vector<ProtectUnit> getAllProtectUnit() {
        Vector<ProtectUnit> v = null;
        try {
            v = (Vector<ProtectUnit>) rmCache.getFromGroup(ALLPRORECT, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<ProtectUnit>();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    int type = StrUtil.toInt(child.getAttributeValue("type"), ProtectUnit.TYPE_INCLUDE);
                    String exclude = child.getAttributeValue("exclude");
                    if (exclude.equals("true")) {
                    	continue;
                    }
                    String rule = child.getText();
                    ProtectUnit pu = new ProtectUnit();
                    pu.setRule(rule);
                    pu.setType(type);
                    v.addElement(pu);
                }
                try {
                    rmCache.putInGroup(ALLPRORECT, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllDeskTopUnit:" + e.getMessage());
                }
            }
        }
        return v;
    }

    public Vector<ProtectUnit> getAllUnProtectUnit() {
        Vector<ProtectUnit> v = null;
        try {
            v = (Vector<ProtectUnit>) rmCache.getFromGroup(ALLUNPRORECT, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<ProtectUnit>();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    int type = StrUtil.toInt(child.getAttributeValue("type"), ProtectUnit.TYPE_INCLUDE);
                    String exclude = child.getAttributeValue("exclude");
                    if (exclude.equals("false")) {
                    	continue;
                    }
                    String rule = child.getText();
                    ProtectUnit pu = new ProtectUnit();
                    pu.setRule(rule);
                    pu.setType(type);
                    v.addElement(pu);
                }
                try {
                    rmCache.putInGroup(ALLUNPRORECT, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllDeskTopUnit:" + e.getMessage());
                }
            }
        }
        return v;
    }    

    public void writemodify() {
        String indent = "    ";
        boolean newLines = true;
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "utf-8");
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(xmlPath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
    }
}

