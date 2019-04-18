package com.redmoon.oa.robot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLProperties;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.crm.AssessLevel;
import com.redmoon.oa.visual.FormDAO;

public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_robot.xml";

    private String cfgpath;

    Logger logger;

    Document doc = null;
    Element root = null;

    public static Config cfg = null;
    private static Object initLock = new Object();

    // Map rules;
    Map<String, Group> groups;
    Map<String, ScoreRule> scoreRules;

    public Config() {
    }

    public void init() {
        logger = Logger.getLogger(Config.class.getName());
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

/*            rules = new HashMap();
            List list = cfg.root.getChild("rules").getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element e = (Element) ir.next();
                    rules.put(e.getAttribute("code").getValue(), e.getText());
                }
            }*/
            boolean isRobotOpen = getBooleanProperty("isRobotOpen");
            if (isRobotOpen) {
                groups = new HashMap<String, Group>();
                List list = cfg.root.getChild("groups").getChildren();
                if (list != null) {
                    Iterator ir = list.iterator();
                    while (ir.hasNext()) {
                        Element e = (Element) ir.next();
                        String id = e.getAttributeValue("id");
                        String name = e.getAttributeValue("name");
                        String loginUrlAuto = e.getChildText("loginUrlAuto");
                        String loginUrl = e.getChildText("loginUrl");
                        String docShowUrl = e.getChildText("docShowUrl");
                        String isRedbagOpen = e.getChildText("isRedbagOpen");
                        String isFilearkShareOpen = e.getChildText("isFilearkShareOpen");
                        String filearkShareDefaultImg = e.getChildText("filearkShareDefaultImg");

                        String isJoinGift = e.getChildText("isJoinGift");
                        String isDocShare = e.getChildText("isDocShare");

                        Group gp = new Group();
                        gp.setId(id);
                        gp.setName(name);
                        gp.setLoginUrl(loginUrl);
                        gp.setLoginUrlAuto(loginUrlAuto);
                        gp.setDocShowUrl(docShowUrl);
                        gp.setRedbagOpen("true".equals(isRedbagOpen));
                        gp.setFilearkShareOpen("true".equals(isFilearkShareOpen));
                        gp.setFilearkShareDefaultImg(filearkShareDefaultImg);
                        gp.setJoinGift("true".equals(isJoinGift));
                        gp.setDocShare("true".equals(isDocShare));
                        groups.put(id, gp);
                    }
                }

                // 载入积分规则
                scoreRules = new HashMap<String, ScoreRule>();
                String sql = "select id from form_table_score_setup";
                FormDAO fdao = new FormDAO();
                try {
                    Iterator ir = fdao.list("score_setup", sql).iterator();
                    while (ir.hasNext()) {
                        fdao = (FormDAO) ir.next();
                        String code = fdao.getFieldValue("code");
                        String name = fdao.getFieldValue("name");
                        String value = fdao.getFieldValue("value");
                        ScoreRule sr = new ScoreRule();
                        sr.setCode(code);
                        sr.setName(name);
                        sr.setValue(StrUtil.toInt(value, 1));
                        scoreRules.put(code, sr);
                    }
                } catch (ErrMsgException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (org.jdom.JDOMException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error("init2:" + e.getMessage());
        }
    }
    
    public Map getGroups() {
    	return groups;
    }
    
    public Map getScoreRules() {
    	return scoreRules;
    }
    
    public Group getGroup(String groupId) {
    	return (Group)groups.get(groupId);
    }
    
/*    public Map getRules() {
    	return rules;
    }
*/
    public Element getRoot() {
        return root;
    }

    public static Config getInstance() {
        if (cfg == null) {
            synchronized (initLock) {
                cfg = new Config();
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
    
/* 
 * 直接setProperty就能保存，如果再次modify，反而会因为缓存被恢复了   
 * public void modify() {
        String indent = "    ";
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
        
        reload();
    }    */

}
