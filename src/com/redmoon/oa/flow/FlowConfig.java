package com.redmoon.oa.flow;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.jam.internal.javadoc.JavadocClassloadingException;

import cn.js.fan.util.ParamUtil;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

public class FlowConfig {
    // public: constructor to load driver and connect db
    boolean debug = true;
    final String configxml = "config_flow.xml"; //同步配置文件
    String xmlpath = "";
    org.jdom.Document doc = null;
    Element root = null;
   //  String deskey = "bluewind"; //DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错
    Logger logger = Logger.getLogger(FlowConfig.class.getName());
;
    public FlowConfig() {
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
        	logger.error(getClass().getName()+":"+"配置config_flow.xml文件出现异常");
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

    public boolean getIsDisplay(String elementName) {
        Element which = root.getChild(elementName);
        if (which == null)
            return false;
        Attribute attr = which.getAttribute("isDisplay");
        if (attr == null)
        	return true;
        else 
        	return which.getAttribute("isDisplay").getValue().equals("true");
    }
    public void modify(String elementName,HttpServletRequest request){
    	Element which = root.getChild(elementName);
    	String isDisplay = ParamUtil.get(request, "isDisplay");
    	String title = ParamUtil.get(request, "title");
    	String value = ParamUtil.get(request, "value");
    	if (which!=null){
    		which.setAttribute("isDisplay", isDisplay);
    		which.setAttribute("title", title);
    		which.setText(value);
    	}
    	writemodify();
    }
    
    public boolean checkChar(String name) {
    	int len = name.length();
        for(int i = 0; i < len; i++){
        	char ch = name.charAt(i);
        	if(ch == 34){
        		return false;
        	}
        }
		return true;
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
    
    public String getBtnName(String btnTag) {
    	return get(btnTag);
    }
    
    public String getBtnTitle(String btnTag) {
        Element which = root.getChild(btnTag);
        if (which == null)
            return null;
        return which.getAttributeValue("title");
    }
}
