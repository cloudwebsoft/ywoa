package com.redmoon.sns.app;

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
import cn.js.fan.util.ErrMsgException;

/**
 *
 * <p>Title: 应用管理</p>
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
public class AppMgr {
    RMCache rmCache;
    static final String group = "SNS_APP";
    final String ALLSNSAPP = "ALL_SNS_APP";

    static Logger logger;
    public final String FILENAME = "config_sns_app.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public AppMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

            // System.out.println(PluginMgr.class.getName() + " xmlPath2=" + xmlPath);

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

    public static void reload() {
        isInited = false;
        try {
            RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public AppUnit getAppUnit(String code) {
        AppUnit pu = null;
        try {
            pu = (AppUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String resource = child.getChildText("resource");
                        String classUnit = child.getChildText("classUnit");
                        Element e1 = child.getChild("page");
                        String enterPage = StrUtil.getNullStr(e1.getChildText("enter"));                        
                        String adminPage = StrUtil.getNullStr(e1.getChildText("admin"));

                        pu = new AppUnit(code);
                        pu.setResource(resource);
                        pu.setAdminPage(adminPage);
                        pu.setClassUnit(classUnit);
                        pu.setEnterPage(enterPage);
                        try {
                            rmCache.putInGroup(code, group,
                                               pu);
                        } catch (Exception e) {
                            logger.error("getPluginUnit:" + e.getMessage());
                        }
                        return pu;
                    }
                }
            }
        }
        return pu;
    }

    public Vector getAllApp() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLSNSAPP, group);
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
                    v.addElement(getAppUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLSNSAPP, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllApp:" + e.getMessage());
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
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "utf-8");
        try {
            FileOutputStream fout = new FileOutputStream(xmlPath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
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
                            System.out.println(getClass() + " name=" + childContent.getName() + " " + property);
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

    public void delAppUnit(String code) {
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    root.removeContent(child);
                    writemodify();
                    reload();
                    break;
                }
            }
        }
    }

}
