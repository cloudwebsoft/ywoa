package com.redmoon.oa.prj;

import java.io.FileInputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;

import com.cloudwebsoft.framework.util.LogUtil;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-3-7下午03:00:48
 */
public class PrjConfig {
	
	public static final String PRJ = "项目";
	public static final String PRJ_TASK = "任务";

	public static final String ARRANGE = "安排";
	public static final String REPORT = "复命";
	public static final String CHECK = "评价";
	
	public static final String CODE_PRJ = "prj";
	public static final String CODE_TASK = "prj_task";
	
	public static final String STATUS_REMOVED = "已作废";
	
	public static final String STATUS_DOING = "未完成";
	
	public static final String STATUS_DONE = "已完成";

    // public: constructor to load driver and connect db
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_prj.xml";

    private String cfgpath;

    Logger logger;

    Document doc = null;
    Element root = null;

    public static PrjConfig cfg = null;
    private static Object initLock = new Object();

    public Vector assessLevels;

    public PrjConfig() {
    }

    public void init() {
        logger = Logger.getLogger(PrjConfig.class.getName());
        URL cfgURL = getClass().getClassLoader().getResource(
                "/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);
        properties = new XMLProperties(cfgpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();

            fin.close();
        } catch (org.jdom.JDOMException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error("init2:" + e.getMessage());
        }
    }

    public Element getRoot() {
        return root;
    }

    public static PrjConfig getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new PrjConfig();
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
        } else
            return -65536;
    }

    public boolean getBooleanProperty(String name) {
        String p = getProperty(name);
        return p.equals("true");
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

    public Vector getAssessLevels() {
        return assessLevels;
    }
}
