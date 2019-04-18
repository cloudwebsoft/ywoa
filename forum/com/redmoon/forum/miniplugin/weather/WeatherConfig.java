package com.redmoon.forum.miniplugin.weather;

import cn.js.fan.util.XMLProperties;
import java.net.URL;
import org.jdom.Element;
import org.apache.log4j.Logger;
import org.jdom.Document;
import java.io.FileInputStream;
import org.jdom.input.SAXBuilder;
import cn.js.fan.util.StrUtil;
import cn.js.fan.cache.jcs.RMCache;
import java.util.Vector;
import cn.js.fan.base.BaseConfig;
import java.util.List;
import java.util.Iterator;
import java.net.URLDecoder;

public class WeatherConfig extends BaseConfig {
    RMCache rmCache;

    private XMLProperties properties;
    private final String CONFIG_FILENAME = "miniplugin_weather.xml";

    private String cfgpath;

    Logger logger;
    Document doc = null;
    Element root = null;

    //String DESKey = "fgfkeydw";//DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错

    public WeatherConfig() {
        rmCache = RMCache.getInstance();
        URL cfgURL = getClass().getClassLoader().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        properties = new XMLProperties(cfgpath);

        logger = Logger.getLogger(WeatherConfig.class.getName());

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("WeatherConfig:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("WeatherConfig:" + e.getMessage());
        }
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

}
