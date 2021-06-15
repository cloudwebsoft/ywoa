package com.redmoon.forum.plugin;

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import org.apache.log4j.Logger;
import java.net.URLDecoder;
import java.util.*;
import com.redmoon.forum.security.IPMonitor;
import cn.js.fan.util.StrUtil;
import cn.js.fan.cache.jcs.RMCache;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ScoreExchangeConfig {
    public ScoreExchangeConfig() {
    }

    private final String CONFIG_FILENAME = "score_exchange.xml";
    private final String rootChild = "score";
    Logger logger;
    private String cfgpath;


    Document doc = null;
    Element root = null;
    final String cacheGroup = "forum_score_exchange_cfg";

    public void init() {
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("ScoreExchangeConfig:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("ScoreExchangeConfig:" + e.getMessage());
        }
    }

    public String getDescription(HttpServletRequest request, String name) {
        return SkinUtil.LoadString(request, "res.config.config_forum_score_exchange", name);
    }

    public Element getRootElement() {
        if (root == null) {
            init();
        }
        return root;
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(cacheGroup);
        } catch (Exception e) {
            logger.error("refresh:" + e.getMessage());
        }
    }

    public String getProperty(String name) {
        String v = null;
        try {
            v = (String) RMCache.getInstance().getFromGroup(name, cacheGroup);
        } catch (Exception e) {
            logger.error("getProperty1:" + e.getMessage());
        }
        if (v == null) {
            if (root == null)
                init();
            Element element = root.getChild("score").getChild(name);
            v = element.getValue();
            if (v != null) {
                try {
                    RMCache.getInstance().putInGroup(name, cacheGroup, v);
                } catch (Exception e) {
                    logger.error("getProperty2:" + e.getMessage());
                }
            }
        }
        return v;
    }

    public int getIntProperty(String name) {
        String p = getProperty(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        } else
            return 0;
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return p.equals("true");
    }

    public String[] getStringArrProperty(String name) {
        String[] p = null;
        p = StrUtil.split(getProperty(name), "\n");
        return p;
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
                } // end if
            }
        }
    }

    public boolean put(String name, String value) {
        if (root == null)
            init();
        Element which = root.getChild(rootChild).getChild(name);
        if (which == null)
            return false;
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
        refresh();
    }

    public boolean isNumeric(String name, String value) {
        if(name.equals("tax")){
            if(isNumeric(value) && Double.parseDouble(value) >= 0 && Double.parseDouble(value) < 1)
                return true;
            else
                return false;
        }else{
            if(StrUtil.isNumeric(value))
                return true;
            else
                return false;
        }
    }

    public static boolean isNumeric(String str) {
        int begin = 0;
        boolean once = true;
        str = str.trim();
        if (str == null || str.equals("")) {
            return false;
        }
        if (str.startsWith("+") || str.startsWith("-")) {
            if (str.length() == 1) {
                // "+" "-"
                return false;
            }
            begin = 1;
        }
        for (int i = begin; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                if (str.charAt(i) == '.' && once) {
                    // '.' can only once
                    once = false;
                } else {
                    return false;
                }
            }
        }
        if (str.length() == (begin + 1) && !once) {
            // "." "+." "-."
            return false;
        }
        return true;
    }

}
