package com.redmoon.oa.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import java.io.FileOutputStream;

import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.cache.jcs.RMCache;
import org.jdom.output.Format;
import java.net.URLDecoder;
import cn.js.fan.util.StrUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DesktopMgr {
    RMCache rmCache;
    final String group = "DESKTOP";
    final String ALLDESKTOP = "ALLDESKTOP";

    public static final String FILENAME = "config_desktop.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public DesktopMgr() {
        rmCache = RMCache.getInstance();

        confURL = getClass().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

            InputStream inputStream = null;
            SAXBuilder sb = new SAXBuilder();
            try {
                Resource resource = new ClassPathResource(FILENAME);
                inputStream = resource.getInputStream();
                doc = sb.build(inputStream);
                root = doc.getRootElement();

                isInited = true;
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(DesktopMgr.class).error(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(DesktopMgr.class).error(e);
                    }
                }
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
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
    }

    public DesktopUnit getDesktopUnit(String code) {
        DesktopUnit pu = null;
        try {
            pu = (DesktopUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getDesktopUnit:" + e.getMessage());
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
                        String pageList = child.getChildText(
                                "pageList");
                        String pageShow = child.getChildText(
                                "pageShow");
                        String className = child.getChildText("className");
                        String type = child.getChildText("type");
                        String isDefault = child.getChildText("isDefault");
                        String defaultCol = child.getChildText("defaultCol");
                        String defaultOrder = child.getChildText("defaultOrder");
                        String isDisplay = child.getChildText("isDisplay");
                        pu = new DesktopUnit(code);
                        pu.setName(name);
                        pu.setPageList(pageList);
                        pu.setPageShow(pageShow);
                        pu.setClassName(className);
                        pu.setType(type);
                        pu.setDef(isDefault.equals("true"));
                        pu.setDefaultCol(StrUtil.toInt(defaultCol, 1));
                        pu.setDefaultOrder(StrUtil.toInt(defaultOrder, 0));
                        pu.setDisplay(isDisplay.equals("true"));
                        try {
                            rmCache.putInGroup(code, group, pu);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error("getDesktopUnit:" + e.getMessage());
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

    public Vector getAllDeskTopUnit() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLDESKTOP, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
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
                    v.addElement(getDesktopUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLDESKTOP, group, v);
                }
                catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getAllDeskTopUnit:" + e.getMessage());
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
