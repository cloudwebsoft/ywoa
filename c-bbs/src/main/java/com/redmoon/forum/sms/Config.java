package com.redmoon.forum.sms;

import java.io.FileInputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jdom.Document;
import java.util.Iterator;
import org.jdom.input.SAXBuilder;
import cn.js.fan.util.XMLProperties;
import org.jdom.Element;
import java.net.URLDecoder;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_forum_sms.xml";

    private String cfgpath;

    Logger logger;
    Document doc = null;
    Element root = null;

    public Config() {
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        properties = new XMLProperties(cfgpath);

        logger = Logger.getLogger(Config.class.getName());

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
        return root;
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getIsUsedClassName() {
        Iterator ir = root.getChildren("sms").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String isUsed = e.getAttributeValue("isUsed");
            if (isUsed.equals("true")) {
                return e.getChildText("className");
            }
        }
        return "";
    }

    public String getIsUsedProperty(String prop) {
        Iterator ir = root.getChildren("sms").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String isUsed = e.getAttributeValue("isUsed");
            if (isUsed.equals("true")) {
                return e.getChildText(prop);
            }
        }
        return "";
    }

    public IMsgUtil getIsUsedIMsg() {
        String className = getIsUsedClassName();
        if (className.equals(""))
            return null;
        IMsgUtil imsg = null;
        try {
            Class cls = Class.forName(className);
            imsg = (IMsgUtil) cls.newInstance();
        } catch (ClassNotFoundException cnfe) {
            System.out.println("getIsUsedIMsg: ClassNotFoundException:" + cnfe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imsg;
    }

}
