package com.redmoon.forum.miniplugin;

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

public class MiniPluginMgr {
    RMCache rmCache;
    final String group = "MINIPLUGIN";
    final String ALLPLUGIN = "ALLMINIPLUGIN";

    static Logger logger;
    public final String FILENAME = "miniplugin.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public MiniPluginMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource("/" + FILENAME);
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

    public MiniPluginUnit getMiniPluginUnit(String code) {
        MiniPluginUnit pu = null;
        try {
            pu = (MiniPluginUnit)rmCache.getFromGroup(code, group);
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
                        String adminEntrance = child.getChildText(
                                "adminEntrance");
                        String strPlugin = child.getChildText("isPlugin");

                        pu = new MiniPluginUnit(code);
                        if (strPlugin.equals("true"))
                            pu.setPlugin(true);
                        else
                            pu.setPlugin(false);
                        pu.setResource(resource);
                        pu.setAdminEntrance(adminEntrance);
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
        else {
            pu.renew();
        }
        return pu;
    }

    public Vector getAllPlugin() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLPLUGIN, group);
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
                    v.addElement(getMiniPluginUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLPLUGIN, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllPlugin:" + e.getMessage());
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
                             // System.out.println(getClass() + " name=" + childContent.getName() + " " + property);
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

     public void delPluginUnit(String code) {
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
