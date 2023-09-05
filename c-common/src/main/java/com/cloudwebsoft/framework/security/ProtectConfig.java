package com.cloudwebsoft.framework.security;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ProtectConfig {
    RMCache rmCache;
    private final String group = "CONFIG_PRORECT";
    private final String ALLPRORECT = "ALLPRORECT";
    private final String ALLUNPRORECT = "ALLUNPRORECT";
    private final String FILENAME = "config_protect.xml";

    private static Document doc = null;
    private static Element root = null;
    private static String xmlPath;
    private static boolean isInited = false;
    private static URL confURL;

    public ProtectConfig() {
        rmCache = RMCache.getInstance();
        confURL = getClass().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            // xmlPath = confURL.getPath(); // 如果有空格，会转换为%20
            xmlPath = confURL.getFile();
            try {
                xmlPath = URLDecoder.decode(xmlPath, "utf-8");
            } catch (UnsupportedEncodingException e) {
                LogUtil.getLog(ProtectConfig.class).error(e);
            }

            IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
            doc = configUtil.getDocument("config_protect.xml");
            root = doc.getRootElement();

            isInited = true;
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

    public Vector<ProtectUnit> getAllProtectUnit() {
        Vector<ProtectUnit> v = null;
        try {
            v = (Vector<ProtectUnit>) rmCache.getFromGroup(ALLPRORECT, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<ProtectUnit>();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    int type = StrUtil.toInt(child.getAttributeValue("type"), ProtectUnit.TYPE_INCLUDE);
                    String exclude = child.getAttributeValue("exclude");
                    if ("true".equals(exclude)) {
                    	continue;
                    }
                    String rule = child.getText();
                    ProtectUnit pu = new ProtectUnit();
                    pu.setRule(rule);
                    pu.setType(type);
                    v.addElement(pu);
                }
                try {
                    rmCache.putInGroup(ALLPRORECT, group, v);
                }
                catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getAllDeskTopUnit:" + e.getMessage());
                }
            }
        }
        return v;
    }

    public Vector<ProtectUnit> getAllUnProtectUnit() {
        Vector<ProtectUnit> v = null;
        try {
            v = (Vector<ProtectUnit>) rmCache.getFromGroup(ALLUNPRORECT, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<>();
            init();
            List<Element> list = root.getChildren();
            if (list != null) {
                for (Element child : list) {
                    int type = StrUtil.toInt(child.getAttributeValue("type"), ProtectUnit.TYPE_INCLUDE);
                    String exclude = child.getAttributeValue("exclude");
                    if ("false".equals(exclude)) {
                        continue;
                    }
                    String rule = child.getText();
                    ProtectUnit pu = new ProtectUnit();
                    pu.setRule(rule);
                    pu.setType(type);
                    v.addElement(pu);
                }
                try {
                    rmCache.putInGroup(ALLUNPRORECT, group, v);
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
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
}

