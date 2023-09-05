package cn.js.fan.web;

import cn.js.fan.util.XMLProperties;

import java.io.*;

import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.entity.SysConfig;
import com.cloudweb.oa.service.ISysConfigService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Element;
import org.jdom.Document;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import cn.js.fan.security.SecurityUtil;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;

import java.net.URLDecoder;

public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_sys.xml";
    private final String ADMIN_PWD = "Application.admin_pwd";

    private String cfgpath;

    Document doc = null;
    Element root = null;

    //String DESKey = "fgfkeydw";//DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错

    public Config() {
        try {
            IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
            String xml = configUtil.getXml("config_sys");

            SAXBuilder sb = new SAXBuilder();
            doc = sb.build(new InputSource(new StringReader(xml)));
            root = doc.getRootElement();
            properties = new XMLProperties("config_sys", doc, true);
        } catch (JDOMException | IOException e) {
            LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
        }
    }

    public Config(boolean isNotUseCache) {
        if (isNotUseCache) {
            try {
                ISysConfigService sysConfigService = SpringUtil.getBean(ISysConfigService.class);
                SysConfig sysConfig = sysConfigService.getSysConfig("config_sys");
                String xml = sysConfig.getXml();
                SAXBuilder sb = new SAXBuilder();
                doc = sb.build(new InputSource(new StringReader(xml)));
                root = doc.getRootElement();
                properties = new XMLProperties("config_sys", doc, true);
            } catch (JDOMException | IOException e) {
                LogUtil.getLog(getClass()).error("Config:" + e.getMessage());
            }
        }
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getAdminPwdMD5() {
        return properties.getProperty(ADMIN_PWD);
    }

    public boolean setAdminPwdMD5(String pwd) {
        try {
            pwd = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        properties.setProperty(ADMIN_PWD, pwd);
        return true;
    }

    public Vector getDBInfos() {
        Element which = root.getChild("DataBase");
        if (which == null) {
            return new Vector();
        }

        Vector v = new Vector();

        List list = which.getChildren("db");
        Iterator ir = list.iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            DBInfo di = new DBInfo();
            di.name = e.getChildTextTrim("name");
            String strDefault = e.getChildTextTrim("Default");
            di.isDefault = strDefault.equals("true");
            di.DBDriver = e.getChildTextTrim("DBDriver");
            di.ConnStr = e.getChildTextTrim("ConnStr");
            di.PoolName = e.getChildTextTrim("PoolName");
            String UsePool = e.getChildTextTrim("UsePool");
            di.isUsePool = UsePool.equals("true");
            v.addElement(di);
        }
        return v;
    }
}
