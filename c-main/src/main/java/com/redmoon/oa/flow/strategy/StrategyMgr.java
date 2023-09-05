package com.redmoon.oa.flow.strategy;

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
import cn.js.fan.util.StrUtil;

import org.jdom.output.Format;

import com.redmoon.oa.kernel.License;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.net.URLDecoder;

public class StrategyMgr {
    static final String group = "FLOW_STRATEGY"; 
    static final String ALLRSTRATEGY = "ALL_FLOW_STRATEGY";

    public static final String FILENAME = "flow_user_sel_strategy.xml";
 
    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public StrategyMgr() {
        confURL = getClass().getResource("/" + FILENAME);
    }

    @SuppressWarnings("deprecation")
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
                LogUtil.getLog(StrategyMgr.class).error(e.getMessage());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LogUtil.getLog(StrategyMgr.class).error(e);
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
        try  {
        	RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            LogUtil.getLog(StrategyMgr.class).error(e.getMessage());
        }
    }

    public StrategyUnit getStrategyUnit(String code) {
        StrategyUnit pu = null;
        try {
    		pu = (StrategyUnit)RMCache.getInstance().getFromGroup(code, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getStrategyUnit:" + e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code) || (ecode.equals("x") && code.startsWith("x_"))) {
                        String name = child.getChildText("name");
                        String author = child.getChildText(
                                "author");
                        String className = child.getChildText("className");

                        pu = new StrategyUnit(ecode);
                        if (code.startsWith("x_")) {
                        	int x = StrUtil.toInt(code.substring(2), 1);
                        	pu.setX(x);
                        }
                        pu.setName(name);
                        pu.setAuthor(author);
                        pu.setClassName(className);
                        try {
                        	RMCache.getInstance().putInGroup(ecode, group, pu);
                        } catch (Exception e) {
                            LogUtil.getLog(getClass()).error("getStrategyUnit:" + e.getMessage());
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

    @SuppressWarnings("unchecked")
	public Vector getAllStrategy() {
        Vector v = null;
        try {
            v = (Vector) RMCache.getInstance().getFromGroup(ALLRSTRATEGY, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
            	License lic = License.getInstance();
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    if (code.equals("x")) {
                    	if (lic.isPlatformSrc()) {
                            v.addElement(getStrategyUnit(code));
                    	}
                    } else {
                        v.addElement(getStrategyUnit(code));
                    }
                }
                try {
                	RMCache.getInstance().putInGroup(ALLRSTRATEGY, group, v);
                }
                catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getAllStrategy:" + e.getMessage());
                }
            }
        }
        return v;
    }

    public void writemodify() {
        String indent = "    ";
        // boolean newLines = true;
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
