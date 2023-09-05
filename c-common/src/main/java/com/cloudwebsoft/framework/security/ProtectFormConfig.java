package com.cloudwebsoft.framework.security;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

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

    private final String FILENAME = "config_protect_form.xml";

    private static Document doc = null;
    private static Element root = null;
    private static String xmlPath;
    private static boolean isInited = false;
    private static URL confURL;

    public ProtectFormConfig() {
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
                LogUtil.getLog(ProtectFormConfig.class).error(e);
            }

            IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
            doc = configUtil.getDocument("config_protect_form.xml");
            root = doc.getRootElement();
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

    /**
     * 取得所有的规则
     * @return
     */
    public Vector<ProtectFormUnit> getAllUnit() {
        Vector<ProtectFormUnit> v = null;
        try {
            v = (Vector<ProtectFormUnit>) rmCache.getFromGroup(ALLUNIT, group);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        if (v==null) {
            v = new Vector<>();
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
                    LogUtil.getLog(getClass()).error("getAllDeskTopUnit:" + e.getMessage());
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
