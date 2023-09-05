package com.redmoon.oa.android;

import java.io.*;
import java.net.*;

import cn.js.fan.util.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudwebsoft.framework.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

public class CloudConfig {
    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_cloud.xml";

    private String cfgpath;

    Document doc = null;
    Element root = null;

    public static CloudConfig cfg = null;
    private static final Object initLock = new Object();

    public CloudConfig() {
    }

    public void init() {
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
        } catch (org.jdom.JDOMException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error("init2:" + e.getMessage());
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

    public Element getRoot() {
        return root;
    }

    public static CloudConfig getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new CloudConfig();
                cfg.init();
            }
        }
        return cfg;
    }

    public static void reload() {
        cfg = null;
    }

    public String getProperty(String name) {
        return StrUtil.getNullStr(properties.getProperty(name));
    }

    public int getIntProperty(String name) {
        String p = getProperty(name);
        if (StrUtil.isNumeric(p)) {
            return Integer.parseInt(p);
        }else if ("-1".equals(p)) {
        	return -1;
        } else {
            return -65536;
        }
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return "true".equals(p);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public String getProperty(String name, String childAttributeName,
                              String childAttributeValue) {
        return StrUtil.getNullStr(properties.getProperty(name, childAttributeName,
                                      childAttributeValue));
    }

    public String getProperty(String name, String childAttributeName,
                              String childAttributeValue, String subChildName) {
        return StrUtil.getNullStr(properties.getProperty(name, childAttributeName,
                                      childAttributeValue, subChildName));
    }

    public void setProperty(String name, String childAttributeName,
                            String childAttributeValue, String value) {
        properties.setProperty(name, childAttributeName, childAttributeValue,
                               value);
    }

    public void setProperty(String name, String childAttributeName,
                            String childAttributeValue, String subChildName,
                            String value) {
        properties.setProperty(name, childAttributeName, childAttributeValue,
                               subChildName, value);
    }
    
    public boolean canUserLogin(HttpServletRequest request) throws ErrMsgException {
    	if (-1 == cfg.getIntProperty("diskSpace")){
    		return true;
    	}
    	// 检查磁盘空间是否已超出
    	if (cfg.getIntProperty("diskSpace") < cfg.getIntProperty("diskSpaceUsed")) {
    		throw new ErrMsgException("磁盘空间已超出，请联系管理员！");
    	}
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
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        reload();
    }

    public JSONArray getDevelopers() {
        JSONArray arr = new JSONArray();
        Element developers = root.getChild("developers");
        if (developers == null) {
            return arr;
        }

        List users = developers.getChildren("user");
        for (Object obj : users) {
            Element user = (Element)obj;
            JSONObject json = new JSONObject();
            json.put("userName", user.getChildText("userName"));
            json.put("userSecret", user.getChildText("userSecret"));
            arr.add(json);
        }
        return arr;
    }

    public boolean modifyDeveloper(String userName, String userSecret) {
        Element developers = root.getChild("developers");
        if (developers == null) {
            return false;
        }
        List users = developers.getChildren("user");
        for (Object obj : users) {
            Element user = (Element)obj;
            if (userName.equals(user.getChildText("userName"))) {
                user.getChild("userSecret").setText(userSecret);
                writemodify();
                return true;
            }
        }
        return false;
    }

    public boolean delDeveloper(String userName) {
        Element developers = root.getChild("developers");
        if (developers == null) {
            return false;
        }
        List users = developers.getChildren("user");
        for (Object obj : users) {
            Element user = (Element)obj;
            if (userName.equals(user.getChildText("userName"))) {
                developers.removeContent(user);
                writemodify();
                return true;
            }
        }
        return false;
    }

    public boolean addDeveloper(String userName, String userSecret) {
        Element developers = root.getChild("developers");
        if (developers == null) {
            return false;
        }
        Element user = new Element("user");
        Element elUserName = new Element("userName");
        elUserName.addContent(userName);
        Element elUserSecret = new Element("userSecret");
        elUserSecret.addContent(userSecret);
        user.addContent(elUserName);
        user.addContent(elUserSecret);

        developers.addContent(user);
        writemodify();
        return true;
    }

    public String getUserSecret(String userName) {
        Element developers = root.getChild("developers");
        if (developers == null) {
            return null;
        }
        List users = developers.getChildren("user");
        for (Object obj : users) {
            Element user = (Element)obj;
            if (userName.equals(user.getChildText("userName"))) {
                return user.getChildText("userSecret");
            }
        }
        return null;
    }
}
