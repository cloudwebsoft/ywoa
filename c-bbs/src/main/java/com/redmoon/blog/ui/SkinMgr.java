package com.redmoon.blog.ui;

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

public class SkinMgr {
    RMCache rmCache;
    static final String group = "BLOG_SKIN";
    static final String ALLSKIN = "ALL_BLOG_SKIN";

    static Logger logger;
    static public final String FILENAME = "skin_blog.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public static final String DEFAULT_SKIN_CODE = "default";

    public SkinMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getResource("/" + FILENAME);
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

    /**
     * 用于页面中获取skin
     * @param code String
     * @return Skin
     */
    public Skin getSkin(String code) {
        if (code==null || code.equals(""))
            return getSkinByCode(DEFAULT_SKIN_CODE);

        Skin sk = getSkinByCode(code);
        if (sk==null)
            sk = getSkinByCode(DEFAULT_SKIN_CODE);
        return sk;
    }

    private Skin getSkinByCode(String code) {
        Skin sk = null;
        try {
            sk = (Skin)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (sk==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String name = child.getChildText("name");
                        String author = child.getChildText("author");
                        String path = child.getChildText(
                                "path");

                        sk = new Skin();
                        sk.setCode(code);
                        sk.setName(name);
                        sk.setAuthor(author);
                        sk.setPath(path);
                        try {
                            rmCache.putInGroup(code, group,
                                               sk);
                        } catch (Exception e) {
                            logger.error("getSkin:" + e.getMessage());
                        }
                        return sk;
                    }
                }
            }
        }
        return sk;
    }

    public Vector getAllSkin() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLSKIN, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    v.addElement(getSkinByCode(code));
                }
                try {
                    rmCache.putInGroup(ALLSKIN, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllSkin:" + e.getMessage());
                }
            }
        }
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
