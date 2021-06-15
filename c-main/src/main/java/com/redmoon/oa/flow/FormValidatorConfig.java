package com.redmoon.oa.flow;

import com.redmoon.oa.base.IFormValidator;
import java.io.FileInputStream;
import java.net.URL;
import cn.js.fan.util.StrUtil;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import cn.js.fan.util.XMLProperties;
import org.jdom.Element;
import java.util.Vector;
import java.util.Iterator;
import java.net.URLDecoder;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
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
public class FormValidatorConfig {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "form_validator.xml";

    private String cfgpath;

    Logger logger;
    Document doc = null;
    Element root = null;

    public FormValidatorConfig() {
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        properties = new XMLProperties(cfgpath);

        logger = Logger.getLogger(FormValidatorConfig.class.getName());

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("Config:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("Config:" + e.getMessage());
        }
    }

    public Element getRootElement() {
        return root;
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public int getIntProperty(String name) {
        String p = getProperty(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        }
        else
            return -65536;
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public Vector getAllValidator() {
        root.getChildren("form");
        return null;
    }

    public IFormValidator getIFormValidatorOfForm(String formCode) {
        Iterator ir = root.getChildren("form").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String code = e.getAttributeValue("code");
            String flowTypeCode = "";
            if (code == null || code.equals("")) {
            	code = e.getAttributeValue("flowTypeCode");
            	FormDb fd = new FormDb(formCode);
            	flowTypeCode = fd.getFlowTypeCode();
            }
            String isUsed = e.getChildText("isUsed");
            // LogUtil.getLog(getClass()).info("getIFormValidatorOfForm:" + code + " isUsed=" + isUsed + " " + e.getChildText("extraData"));
            if ((flowTypeCode.equals("") && code.equals(formCode)) || flowTypeCode.equals(code)) {
                IFormValidator ifv = null;
                try {
                    ifv = (IFormValidator) Class.forName(e.getChildText(
                            "className")).newInstance();

                    ifv.setIsUsed("true".equals(isUsed));
                    ifv.setExtraData(e.getChildText("extraData"));

                    return ifv;
                } catch (Exception exp) {
                    logger.error(exp.getMessage());
                    exp.printStackTrace();
                }
            }
        }
        return null;
    }
}

class FormValidatorUnit {
    public String className;
    public String formCode;

    public FormValidatorUnit() {

    }
}
