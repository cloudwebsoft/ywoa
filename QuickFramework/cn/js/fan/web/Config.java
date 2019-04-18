package cn.js.fan.web;

import cn.js.fan.util.XMLProperties;
import java.net.URL;
import org.jdom.Element;
import org.apache.log4j.Logger;
import org.jdom.Document;
import java.io.FileInputStream;
import org.jdom.input.SAXBuilder;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.kernel.ISchedulerUnit;
import cn.js.fan.kernel.Scheduler;
import java.net.URLDecoder;

public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_cws.xml";
    private final String ADMIN_PWD = "Application.admin_pwd";

    private String cfgpath;

    Logger logger;
    Document doc = null;
    Element root = null;

    //String DESKey = "fgfkeydw";//DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错

    public Config() {
        URL cfgURL = getClass().getClassLoader().getResource("/" + CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);
        // getFile()与getPath()获得的值是一样的
        // System.out.println("Config: cfgURL path=" + cfgURL.getFile());
        // System.out.println("Config: cfgURL path2=" + URLDecoder.decode(cfgURL.getFile()));
        // System.out.println("Config: cfgURL path3=" + cfgURL.getPath());
        // System.out.println("Config: cfgURL path4=" + URLDecoder.decode(cfgURL.getPath()));
        properties = new XMLProperties(cfgpath);

        logger = Logger.getLogger(Config.class.getName());

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
        // System.out.println("Config: itis=" + logger);

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
            logger.error(e.getMessage());
        }
        properties.setProperty(ADMIN_PWD, pwd);
        return true;
    }

    // 初始化调度中心，加载调度项
    public void initScheduler() {
        // 清空原来的调度项
        Scheduler.getInstance().ClearUnits();

        Element which = root.getChild("scheduler");
        if (which == null)
            return;

        List list = which.getChildren();

        Iterator ir = list.iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String className = e.getTextTrim();

            try {
                Class cls = Class.forName(className);

                ISchedulerUnit isu = (ISchedulerUnit)cls.newInstance();

                isu.registSelf();
                // logger.info(isu);
            } catch (Exception e1) {
                e1.printStackTrace();
                logger.error("initScheduler:" + e1.getMessage());
            }
        }
    }

    public Vector getDBInfos() {
        Element which = root.getChild("DataBase");
        if (which == null)
            return new Vector();

        Vector v = new Vector();

        List list = which.getChildren("db");
        Iterator ir = list.iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            DBInfo di = new DBInfo();
            di.name = e.getChildTextTrim("name");
            // logger.info("di.name=" + di.name);
            String strDefault = e.getChildTextTrim("Default");
            if (strDefault.equals("true"))
                di.isDefault = true;
            else
                di.isDefault = false;
            di.DBDriver = e.getChildTextTrim("DBDriver");
            di.ConnStr = e.getChildTextTrim("ConnStr");
            di.PoolName = e.getChildTextTrim("PoolName");
            String UsePool = e.getChildTextTrim("UsePool");
            if (UsePool.equals("true"))
                di.isUsePool = true;
            else
                di.isUsePool = false;
            v.addElement(di);
        }
        return v;
    }
}
