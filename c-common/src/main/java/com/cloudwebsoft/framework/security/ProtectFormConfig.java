package com.cloudwebsoft.framework.security;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 */
public class ProtectFormConfig {

    RMCache rmCache;

    private final String group = "CONFIG_PRORECT_FORM";
    private final String ALLUNIT = "ALLUNIT";

    private static Logger logger;
    private final String FILENAME = "config_protect_form.xml";

    private static Document doc = null;
    private static Element root = null;
    private static String xmlPath;
    private static boolean isInited = false;
    private static URL confURL;

    public ProtectFormConfig() {
        rmCache = RMCache.getInstance();
        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getResource("/" + FILENAME);
    }

    public static void init() {
        if (!isInited) {
            // xmlPath = confURL.getPath(); // 如果有空格，会转换为%20
            xmlPath = confURL.getFile();
            try {
                xmlPath = URLDecoder.decode(xmlPath, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

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

    /**
     * 取得所有的规则
     * @return
     */
    public Vector<ProtectFormUnit> getAllUnit() {
        Vector<ProtectFormUnit> v = null;
        try {
            v = (Vector<ProtectFormUnit>) rmCache.getFromGroup(ALLUNIT, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<ProtectFormUnit>();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    int type = StrUtil.toInt(child.getAttributeValue("type"), ProtectUnit.TYPE_INCLUDE);

                    String formCode = child.getChildText("formCode");
                    String fields = child.getChildText("fields");
                    List<String> fieldsAry = Arrays.asList(StringUtils.split(fields, ","));

                    ProtectFormUnit protectFormUnit = new ProtectFormUnit();
                    protectFormUnit.setFormCode(formCode);
                    protectFormUnit.setFields(fieldsAry);
                    protectFormUnit.setType(type);
                    v.addElement(protectFormUnit);
                }
                try {
                    rmCache.putInGroup(ALLUNIT, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllDeskTopUnit:" + e.getMessage());
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
    }
}
