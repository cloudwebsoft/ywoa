package cn.js.fan.sso;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

public class Config {
    String CONFIG_FILENAME = "config_sso.xml";
    Document doc = null;
    Element root = null;

    public Config() {
        InputStream inputStream = null;
        try {
            Resource resource = new ClassPathResource(CONFIG_FILENAME);
            inputStream = resource.getInputStream();
            SAXBuilder sb = new SAXBuilder();
            doc = sb.build(inputStream);
            root = doc.getRootElement();
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error(e);
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

    public String getLoginURL(String kind) {
        Element webapp = root.getChild("webapp");

        java.util.List webapplist = webapp.getChildren();
        Iterator ir = webapplist.iterator();
        String attr = "";
        while (ir.hasNext()) {
            Element a = (Element) ir.next();
            attr = a.getAttribute("kind").getValue();
            if (attr != null && attr.equals(kind)) {
                Element loginurl = a.getChild("loginurl");
                if (loginurl != null) {
                    return loginurl.getText();
                }
                break;
            }
        }
        return null;
    }

    public String getDefaultURL(String kind) {
        Element webapp = root.getChild("webapp");
        java.util.List webapplist = webapp.getChildren();
        Iterator ir = webapplist.iterator();
        String attr = "";
        while (ir.hasNext()) {
            Element a = (Element) ir.next(); //得到第i个field元素
            attr = a.getAttribute("kind").getValue();
            if (attr != null && attr.equals(kind)) {
                Element loginurl = a.getChild("defaulturl");
                if (loginurl != null) {
                    return loginurl.getText();
                }
                break;
            }
        }
        return null;
    }

    public String getKey() {
        Element which = root.getChild("key");
        if (which == null) {
            return null;
        }
        return StrUtil.getNullStr(which.getText());
    }

    public boolean setKey(String pwd) {
        Element which = root.getChild("site").getChild("key");
        if (which == null) {
            return false;
        }
        which.setText(pwd);
        return true;
    }
}
