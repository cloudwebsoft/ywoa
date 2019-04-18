package com.redmoon.oa.video;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
public class Config {
    final String configxml = "config_video.xml"; //同步配置文件
    String xmlpath = "";
    org.jdom.Document doc = null;
    Element root = null;
    Logger logger = Logger.getLogger(Config.class.getName());
    public Config() {
        URL confURL = getClass().getClassLoader().getResource(configxml);
        xmlpath = confURL.getFile();
        try {
            xmlpath = URLDecoder.decode(xmlpath, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        SAXBuilder sb = new SAXBuilder();
        FileInputStream fin = null;
        try {
        	fin = new FileInputStream(xmlpath);
            doc = sb.build(fin); 
            root = doc.getRootElement();
        }  catch (java.lang.Exception e) {
        	logger.error(getClass().getName()+":"+"配置config_video.xml文件出现异常");
		} finally {
        	if (fin!=null){
        		try {
        			fin.close();
				} catch (Exception e) {
					logger.error(getClass().getName()+":"+"关闭流出现异常");
				}
        	}
        } 
    }

    public Element getRootElement() {
        return root;
    }

    public String getDescription(String name) {
        Element which = root.getChild(name);
        // System.out.println("name=" + name + " which=" + which);
        if (which == null)
            return null;
        return which.getAttribute("desc").getValue();
    }

    public String get(String name) {
        Element which = root.getChild(name);
        if (which == null)
            return null;
        return which.getText();
    }

    public int getInt(String elementName) {
        Element which = root.getChild(elementName);
        if (which == null)
            return -1;
        int r = -1;
        try {
            r = Integer.parseInt(which.getText());
        } catch (Exception e) {
            logger.error("getInt:" + e.getMessage());
        }
        return r;
    }

    public boolean put(String name, String value) {
        Element which = root.getChild(name);
        if (which == null)
            return false;
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        String indent = "    ";
        boolean newLines = true;
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "gb2312");
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
            FileOutputStream fout = new FileOutputStream(xmlpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
    }
    
   
}
