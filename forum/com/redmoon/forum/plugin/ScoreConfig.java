package com.redmoon.forum.plugin;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import org.apache.log4j.Logger;
import java.net.URLDecoder;
import java.util.*;
import cn.js.fan.util.*;

public class ScoreConfig {
    private final String CONFIG_FILENAME = "score.xml";
    Logger logger;
    private String cfgpath;

    Document doc = null;
    Element root = null;

    public ScoreConfig() {
        logger = Logger.getLogger(ScoreConfig.class.getName());
        init();
    }

    public void init() {
        URL cfgURL = getClass().getClassLoader().getResource(
                "/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("ConfigScore:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("ConfigScore:" + e.getMessage());
        }
    }

    public Element getRootElement() {
        if (root == null) {
            init();
        }
        return root;
    }

    public void set(String code, String property, String textValue) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    List list1 = child.getChildren();
                    if (list1 != null) {
                        Iterator ir1 = list1.listIterator();
                        while (ir1.hasNext()) {
                            Element childContent = (Element) ir1.next();
                            if (childContent.getName().equals(property)) {
                                childContent.setText(textValue);
                            }
                        }
                    }
                    break;
                }
            }
        }
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
    }
}
