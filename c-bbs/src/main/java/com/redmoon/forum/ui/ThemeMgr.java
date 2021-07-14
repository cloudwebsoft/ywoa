package com.redmoon.forum.ui;

import java.io.*;
import java.util.*;
import java.net.URL;
import org.jdom.Document;
import org.jdom.output.*;
import org.jdom.input.*;
import org.jdom.Element;
import org.apache.log4j.Logger;
import java.net.URLDecoder;
import com.redmoon.kit.util.FileUpload;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import com.redmoon.kit.util.FileUpload;
import org.apache.log4j.Logger;
import org.jdom.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import cn.js.fan.cache.jcs.RMCache;


public class ThemeMgr {
    RMCache rmCache;
    final String group = "THEME";
    final String ALLTHEME = "ALLTHEME";

    static Logger logger;
    public final String FILENAME = "theme.xml";
    FileUpload fileUpload = null;


    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public ThemeMgr() {
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
        if (root == null) {
            init();
        }
        return root;
    }

    public void reload() {
        isInited = false;
        try {
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Theme getTheme(String code) {
        Theme tm = null;
        try {
            tm = (Theme) rmCache.getFromGroup(code, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (tm == null) {
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
                        String banner = child.getChildText("banner");
                        String height = child.getChildText("height");
                        tm = new Theme();
                        tm.setCode(code);
                        tm.setName(name);
                        tm.setAuthor(author);
                        tm.setPath(path);
                        tm.setBanner(banner);
                        tm.setHeight(height);
                        try {
                            rmCache.putInGroup(code, group,
                                               tm);
                        } catch (Exception e) {
                            logger.error("getTheme:" + e.getMessage());
                        }
                        return tm;
                    }
                }
            }
        }
        return tm;
    }

    public Vector getAllTheme() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLTHEME, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v == null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    v.addElement(getTheme(code));
                }
                try {
                    rmCache.putInGroup(ALLTHEME, group, v);
                } catch (Exception e) {
                    logger.error("getAllTheme:" + e.getMessage());
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
        finally {
            reload();
        }
    }

    public void modify(ServletContext application,
                       HttpServletRequest request) throws ErrMsgException {
        String filename = "", code = "", name = "", height = "";
        ThemeForm tf = new ThemeForm();
        tf.init();
        tf.doUpload(application, request);
        code = tf.checkCode(request);
        name = tf.checkName(request);
        height = tf.checkHeight(request);
        tf.report();

        filename = tf.checkFiles(request);
        String picSrc = tf.checkPicSrc(request);
        if (!StrUtil.isNumeric(height)) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.plugin.ThemeConfig", "height_is_numbic"));
        }
        if (!filename.equals("")) {
            delimg(picSrc);
            set(code, "banner", filename);
            set(code, "path", "/" + Theme.basePath);
        }
        set(code, "height", height);
        set(code, "name", name);
        writemodify();
    }

    public void create(ServletContext application,
                       HttpServletRequest request) throws ErrMsgException {
        ThemeForm tf = new ThemeForm();
        tf.init();
        tf.doUpload(application, request);
        String code = tf.checkCode(request);
        String name = tf.checkName(request);
        String height = tf.checkHeight(request);
        tf.report();

        String filename = tf.checkFiles(request);
        if (filename.equals("") || filename==null) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin.ThemeConfig", "file_is_null"));
        }

        List list = root.getChildren();
        Element theme = new Element("theme");
        theme.setAttribute(new Attribute("code", code));
        Element elementName = new Element("name");
        elementName.setText(name);
        theme.addContent(elementName);

        Element elementPath = new Element("path");
        elementPath.setText("/" + Theme.basePath);
        theme.addContent(elementPath);

        Element elementBanner = new Element("banner");
        elementBanner.setText(filename);
        theme.addContent(elementBanner);

        Element elementHeight = new Element("height");
        elementHeight.setText(height);
        theme.addContent(elementHeight);
        list.add(theme);
        writemodify();
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
                }
            }
        }
    }

    public void del(HttpServletRequest request) throws ErrMsgException {
        List list = root.getChildren();
        String picsrc = "";
        String code = ParamUtil.get(request, "code");
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    picsrc = Global.getRealPath() + child.getChildText("path") +
                             "/" + child.getChildText("banner");
                    root.removeContent(child);
                    writemodify();
                    break;
                }
            }
        }
        delimg(picsrc);
    }

    public void delimg(String src) throws ErrMsgException {
        try {
            File file = new File(src);
            file.delete();
        } catch (Exception e) {
            logger.error("ThemeMgr modify fail!");
        }
    }
}
