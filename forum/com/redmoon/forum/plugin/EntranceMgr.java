package com.redmoon.forum.plugin;

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
import com.redmoon.forum.plugin.base.IPluginEntrance;
import org.jdom.output.Format;
import java.net.URLDecoder;

/**
 *
 * <p>Title:通行证管理 </p>
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
public class EntranceMgr {
    RMCache rmCache;
    final static String group = "ENTRANCE";
    final String ALLENTRANCE = "ALLENTRANCE";

    static Logger logger;
    public final String FILENAME = "entrance.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public EntranceMgr() {
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

    public static void reload() {
        isInited = false;
        try  {
            RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public EntranceUnit getEntranceUnit(String code) {
        EntranceUnit pu = null;
        try {
            pu = (EntranceUnit)rmCache.getFromGroup(code, group);
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
                        String name = child.getChildText("name");
                        String author = child.getChildText(
                                "author");
                        String className = child.getChildText("className");
                        String desc = child.getChildText("desc");
                        String adminEntrance = child.getChildText("adminEntrance");
                        String userCenterPage = child.getChildText("usercenterPage");

                        pu = new EntranceUnit(code);
                        pu.setName(name);
                        pu.setAuthor(author);
                        pu.setClassName(className);
                        pu.setDesc(desc);
                        pu.setAdminEntrance(adminEntrance);
                        pu.setUserCenterPage(userCenterPage);
                        try {
                            rmCache.putInGroup(code, group, pu);
                        } catch (Exception e) {
                            logger.error("getEntranceUnit:" + e.getMessage());
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

    public Vector getAllEntrance() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLENTRANCE, group);
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
                    v.addElement(getEntranceUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLENTRANCE, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllEntrance:" + e.getMessage());
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

    /**
     * 取得对应于boardCode版面的所有EntranceUnit，此处可以考虑加入缓存
     * @param boardCode String
     * @return Vector
     */
    public Vector getAllEntranceUnitOfBoard(String boardCode) {
        Vector v = null;
        try {
            v = (Vector)RMCache.getInstance().getFromGroup("all_unit_" + boardCode, group);
        }
        catch (Exception e) {
            logger.error("getAllEntranceUnitOfBoard1:" + e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            Vector vplugin = getAllEntrance();
            if (vplugin.size() > 0) {
                Iterator irplugin = vplugin.iterator();
                while (irplugin.hasNext()) {
                    EntranceUnit pu = (EntranceUnit) irplugin.next();
                    IPluginEntrance ipu = pu.getEntrance();
                    // logger.info("getAllEntranceUnitOfBoard:" + ipu);
                    if (ipu.isPluginBoard(boardCode)) {
                        v.addElement(pu);
                    }
                }
            }
            try {
                RMCache.getInstance().putInGroup("all_unit_" + boardCode, group,
                                                 v);
            }
            catch (Exception e) {
                logger.error("getAllEntranceUnitOfBoard2:" + e.getMessage());
            }
        }
        return v;
    }
}
