package com.redmoon.oa.visual.func;

import java.io.FileInputStream;
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

public class FuncMgr {
    RMCache rmCache;
    final String group = "FuncMgr";
    final String ALLFUNC = "ALLFUNC";

    static Logger logger;
    public final String FILENAME = "config_func.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public FuncMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource(FILENAME);
    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

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

    public FuncUnit getFuncUnit(String code) {
    	FuncUnit pu = null;
        try {
            pu = (FuncUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error("getFuncUnit:" + e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equalsIgnoreCase(code)) {
                        String name = child.getChildText("name");
                        String author = child.getChildText("author");
                        String className = child.getChildText("className");
                        String data = child.getChildText("data");
                        String isDisplay = StrUtil.getNullStr(child.getAttributeValue("isDisplay"));
                        
                        // 1.2版后增加version，version=2表示为新版宏控件，启用description
                        float version = StrUtil.toFloat(child.getChildText("version"), 1);
                        
                        pu = new FuncUnit(ecode);
                        
                        pu.setName(name);
                        pu.setAuthor(author);
                        pu.setClassName(className);
                        pu.setData(data);
                        pu.setDisplay(!isDisplay.equals("false"));
                        pu.setVersion(version);
                        
                        try {
                            rmCache.putInGroup(ecode, group, pu);
                        } catch (Exception e) {
                            logger.error("getFuncUnit:" + e.getMessage());
                        }
                        return pu;
                    }
                }
            }
        }
        else {
            pu.renew();
        }

        return pu;
    }

    public Vector getAllFuncUnit() {
    	/*
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLSCORE, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
        */
            Vector v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    v.addElement(getFuncUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLFUNC, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllEntrance:" + e.getMessage());
                }
            }
        // }
        return v;
    }

    public void writemodify() {
        String indent = "    ";
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
