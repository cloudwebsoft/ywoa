package com.redmoon.oa.flow;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class FlowConfig {

    final String configxml = "config_flow.xml"; //同步配置文件
    org.jdom.Document doc = null;
    Element root = null;

    public FlowConfig() {
        try {
            ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
            String xml = configUtil.getXml(configxml);

            SAXBuilder sb = new SAXBuilder();
            doc = sb.build(new InputSource(new StringReader(xml)));
            root = doc.getRootElement();
        }  catch (java.lang.Exception e) {
        	LogUtil.getLog(getClass()).error(getClass().getName()+":"+"配置config_flow.xml文件出现异常");
		}
    }

    public Element getRootElement() {
        return root;
    }

    public String getDescription(String name) {
        Element which = root.getChild(name);
        if (which == null) {
            return null;
        }
        return which.getAttribute("desc").getValue();
    }

    public String get(String name) {
        Element which = root.getChild(name);
        if (which == null) {
            return null;
        }
        return which.getText();
    }

    public int getInt(String elementName) {
        Element which = root.getChild(elementName);
        if (which == null) {
            return -1;
        }
        int r = -1;
        try {
            r = Integer.parseInt(which.getText());
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getInt:" + e.getMessage());
        }
        return r;
    }

    public boolean getIsDisplay(String elementName) {
        Element which = root.getChild(elementName);
        if (which == null) {
            return false;
        }
        Attribute attr = which.getAttribute("isDisplay");
        if (attr == null) {
            return true;
        } else {
            return "true".equals(which.getAttribute("isDisplay").getValue());
        }
    }

    public void modify(String elementName, String value, String title, String isDisplay){
    	Element which = root.getChild(elementName);
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
        if (which == null) {
            return false;
        }
        which.setText(value);
        writemodify();
        return true;
    }

    public void writemodify() {
        ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
        configUtil.putXml(configxml, doc);
    }
    
    public String getBtnName(String btnTag) {
    	return get(btnTag);
    }
    
    public String getBtnTitle(String btnTag) {
        Element which = root.getChild(btnTag);
        if (which == null) {
            return null;
        }
        return which.getAttributeValue("title");
    }

    public List<String> getPhrases() {
        List<String> lst = new ArrayList<>();
        Element e = root.getChild("phrases");
        List list = e.getChildren();
        if (list != null) {
            Iterator irChild = list.iterator();
            while (irChild.hasNext()) {
                Element eChild = (Element) irChild.next();
                lst.add(eChild.getText());
            }
        }
        return lst;
    }
}
