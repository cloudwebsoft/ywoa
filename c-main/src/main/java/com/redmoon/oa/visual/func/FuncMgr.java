package com.redmoon.oa.visual.func;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import cn.js.fan.util.XMLProperties;
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

public class FuncMgr {
    RMCache rmCache;
    final String group = "FuncMgr";
    final String ALLFUNC = "ALLFUNC";

    public static final String FILENAME = "config_func.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public FuncMgr() {
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
                LogUtil.getLog(FuncMgr.class).error(e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(FuncMgr.class).error(e);
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

    public FuncUnit getFuncUnit(String code) {
    	FuncUnit pu = null;
        try {
            pu = (FuncUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getFuncUnit:" + e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                for (Object o : list) {
                    Element child = (Element) o;
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
                        pu.setDisplay(!"false".equals(isDisplay));
                        pu.setVersion(version);

                        try {
                            rmCache.putInGroup(ecode, group, pu);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error(e);
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
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        if (v==null) {
        */
            Vector<FuncUnit> v = new Vector<>();
            init();
            List list = root.getChildren();
            if (list != null) {
                for (Object o : list) {
                    Element child = (Element) o;
                    String code = child.getAttributeValue("code");
                    v.addElement(getFuncUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLFUNC, group, v);
                }
                catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getAllEntrance:" + e.getMessage());
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
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}
