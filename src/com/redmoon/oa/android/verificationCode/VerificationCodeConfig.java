package com.redmoon.oa.android.verificationCode;

import java.io.FileInputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jdom.Document;
import java.util.Iterator;
import org.jdom.input.SAXBuilder;
import cn.js.fan.util.XMLProperties;
import org.jdom.Element;

/**
 * @Description: 短信验证码配置文件操作类
 * @author: lichao
 * @Date: 2015-8-6下午02:41:37
 */
public class VerificationCodeConfig {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_verification_code.xml";

    private String cfgpath;

    Logger logger;
    Document doc = null;
    Element root = null;

    public VerificationCodeConfig() {
        URL cfgURL = getClass().getClassLoader().getResource(CONFIG_FILENAME);
        
        cfgpath = cfgURL.getFile();
        properties = new XMLProperties(cfgpath);
        logger = Logger.getLogger(VerificationCodeConfig.class.getName());

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

    public String getExpireSecond() {
        Iterator ir = root.getChildren("verificationCode").iterator();
		
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            return e.getChild("expireSecond").getText();
        }
        
        return "";
    }

    public String getVerificationCodeStr() {
        Iterator ir = root.getChildren("verificationCode").iterator();
        
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            return e.getChild("verificationCodeStr").getText();
        }
        
        return "";
    }
    
    public String getVerificationCodeNum() {
        Iterator ir = root.getChildren("verificationCode").iterator();
        
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            return e.getChild("verificationCodeNum").getText();
        }
        
        return "";
    }
}
