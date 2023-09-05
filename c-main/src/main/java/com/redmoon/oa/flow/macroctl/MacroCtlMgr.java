package com.redmoon.oa.flow.macroctl;

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

public class MacroCtlMgr {
    RMCache rmCache;
    final static String group = "MACROCTL";

    public final String FILENAME = "oa_macro_ctl.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public MacroCtlMgr() {
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
                Resource resource = new ClassPathResource("oa_macro_ctl.xml");
                inputStream = resource.getInputStream();
                doc = sb.build(inputStream);
                root = doc.getRootElement();

                /*FileInputStream fin = new FileInputStream(xmlPath);
                doc = sb.build(fin);
                root = doc.getRootElement();
                fin.close();*/
                isInited = true;
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(MacroCtlMgr.class).error(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(MacroCtlMgr.class).error(e);
                    }
                }
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
        } catch (Exception e) {
            LogUtil.getLog(MacroCtlMgr.class).error(e.getMessage());
        }
    }

    public String getMacroText(String code, String elementName) {
        init();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    String text = child.getChildText(elementName);

                    return text;
                }
            }
        }
        return null;
    }

    public MacroCtlUnit getMacroCtlUnit(String code) {
        MacroCtlUnit pu = null;
        try {
            pu = (MacroCtlUnit) rmCache.getFromGroup(code, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getMacroUnit:" + e.getMessage());
        }
        if (pu == null) {
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
                        String className = child.getChildText("className");
                        String inputValue = child.getChildText("inputValue");
                        String fieldType = child.getChildText("fieldType");
                        String nestType = StrUtil.getNullStr(child.getChildText("nestType"));
                        String isDisplay = StrUtil.getNullStr(child.getAttributeValue("isDisplay"));

                        // 1.2版后增加version，version=2表示为新版宏控件，启用description
                        float version = StrUtil.toFloat(child.getChildText("version"), 1);

                        boolean isForm = "true".equals(StrUtil.getNullStr(child.getChildText("isForm")));
                        String formCode = StrUtil.getNullStr(child.getChildText("formCode"));

                        pu = new MacroCtlUnit(code);

                        pu.setName(name);
                        pu.setAuthor(author);
                        pu.setClassName(className);
                        pu.setInputValue(inputValue);
                        pu.setFieldType(fieldType);
                        pu.setNestType(StrUtil.toInt(nestType, MacroCtlUnit.NEST_TYPE_NONE));
                        pu.setDisplay(!"false".equals(isDisplay));
                        pu.setVersion(version);

                        pu.setForm(isForm);
                        pu.setFormCode(formCode);

                        try {
                            rmCache.putInGroup(code, group, pu);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error("getMacroUnit:" + e.getMessage());
                        }
                        return pu;
                    }
                }
            }
        } else {
            pu.renew();
        }

        return pu;
    }

    public Vector getAllMacroUnit() {
        init();
        Vector v = new Vector();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String code = child.getAttributeValue("code");
                v.addElement(getMacroCtlUnit(code));
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
        } catch (java.io.IOException e) {
        }
    }

}
