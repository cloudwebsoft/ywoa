package com.redmoon.oa.visual;

import java.io.*;
import java.net.*;
import java.util.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.*;
import org.jdom.input.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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
public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "visual_config.xml";

    private String cfgpath;

    Document doc = null;
    Element root = null;

    public Config() {
        URL cfgURL = getClass().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        // properties = new XMLProperties(cfgpath);

        InputStream inputStream = null;
        SAXBuilder sb = new SAXBuilder();
        try {
            Resource resource = new ClassPathResource(CONFIG_FILENAME);
            inputStream = resource.getInputStream();
            doc = sb.build(inputStream);
            root = doc.getRootElement();
            properties = new XMLProperties(CONFIG_FILENAME, doc);

            /*FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();*/
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
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

    public String getAttachmentPath(String formCode) {
        Iterator ir = root.getChildren("module").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String code = e.getAttributeValue("code");
            if (code.equals(formCode)) {
                return e.getChildText("attachmentPath");
            }
        }
        // return null;
        return "upfile/visual/" + formCode;
    }

    public String getView(String formCode, String viewType) {
        Iterator ir = root.getChildren("module").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String code = e.getAttributeValue("code");
            if (code.equals(formCode)) {
                e = e.getChild("view");
                return e.getChildText(viewType);
            }
        }
        return null;
    }

    public IModuleChecker getIModuleChecker(String formCode) {
        Iterator ir = root.getChildren("module").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String code = e.getAttributeValue("code");
            if (code.equals(formCode)) {
                IModuleChecker ifv = null;
                try {
                    ifv = (IModuleChecker) Class.forName(e.getChildText(
                            "checker")).newInstance();
                    return ifv;
                } catch (Exception exp) {
                    LogUtil.getLog(getClass()).warn("getIModuleChecker:" + exp.getMessage());
                }
            }
        }
        return null;
    }
}
