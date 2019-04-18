package com.redmoon.oa;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class SpConfig {
  // public: constructor to load driver and connect db
  boolean debug = true;
  final String configxml = "config_sp.xml";
  String xmlpath = "";
  Document doc = null;
  Element root = null;
  String deskey = "bluewind";//DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错
  Logger logger = Logger.getLogger(SpConfig.class.getName());

  @SuppressWarnings("deprecation")
public SpConfig() {
    URL confURL = getClass().getClassLoader().getResource(configxml);
    xmlpath = confURL.getFile();
    xmlpath = URLDecoder.decode(xmlpath);

    SAXBuilder sb = new SAXBuilder();
    try {
      FileInputStream fin = new FileInputStream(xmlpath);
      doc = sb.build(fin);
      root = doc.getRootElement();
      fin.close();
    }
    catch (org.jdom.JDOMException e) {}
    catch (java.io.IOException e) {
    }
  }

  public Element getRootElement() {
    return root;
  }

  public String getDescription(String name) {
    Element which = root.getChild("sp").getChild(name);
    // System.out.println("name=" + name + " which=" + which);
    if (which==null)
      return null;
    Attribute att = which.getAttribute("desc");
    if (att!=null)
    	return att.getValue();
    else
    	return "";
  }

  public String get(String name) {
    Element  which = root.getChild("sp").getChild(name);
    if (which==null)
      return null;
    return which.getText();
  }

  public int getInt(String elementName) {
      Element which = root.getChild("sp").getChild(elementName);
      if (which==null)
          return -1;
      int r = -1;
      try {
          r = Integer.parseInt(which.getText());
      }
      catch (Exception e) {
          logger.error("getInt:" + e.getMessage());
      }
      return r;
  }

  public boolean getBooleanProperty(String elementName) {
      Element which = root.getChild("sp").getChild(elementName);
      if (which==null)
          return false;
      return which.getText().equals("true");
  }

  public boolean put(String name, String value) {
    Element  which = root.getChild("sp").getChild(name);
    if (which==null)
      return false;
    which.setText(value);
    writemodify();
    return true;
  }

  public void writemodify() {
    String indent = "    ";
    // XMLOutputter outp = new XMLOutputter(indent, newLines, "gb2312");
    Format format = Format.getPrettyFormat();
    format.setIndent(indent);
    format.setEncoding("gb2312");
    XMLOutputter outp = new XMLOutputter(format);
    try {
      FileOutputStream fout = new FileOutputStream(xmlpath);
      outp.output(doc, fout);
      fout.close();
    }
    catch (java.io.IOException e) {}
  }
}
