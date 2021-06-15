package com.redmoon.oa.sale;

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

import com.cloudwebsoft.framework.util.LogUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ContractConfig {
  // public: constructor to load driver and connect db
  boolean debug = true;
  final String configxml = "config_sales_contract.xml";
  String xmlpath = "";
  Document doc = null;
  Element root = null;

  public ContractConfig() {
    URL confURL = getClass().getResource("/" + configxml);
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
    Element  which = root.getChild("contract").getChild(name);
    // System.out.println("name=" + name + " which=" + which);
    if (which==null)
      return null;
    return which.getAttribute("desc").getValue();
  }

  public String get(String name) {
    Element  which = root.getChild("contract").getChild(name);
    if (which==null)
      return null;
    return which.getText();
  }

  public int getInt(String elementName) {
      Element which = root.getChild("contract").getChild(elementName);
      if (which==null)
          return -1;
      int r = -1;
      try {
          r = Integer.parseInt(which.getText());
      }
      catch (Exception e) {
          LogUtil.getLog(getClass()).error("getInt:" + e.getMessage());
      }
      return r;
  }

  public boolean getBooleanProperty(String elementName) {
      Element which = root.getChild("contract").getChild(elementName);
      if (which==null)
          return false;
      return which.getText().equals("true");
  }

  public boolean put(String name, String value) {
    Element  which = root.getChild("contract").getChild(name);
    if (which==null)
      return false;
    which.setText(value);
    writemodify();
    return true;
  }

  public void writemodify() {
    String indent = "    ";
    // boolean newLines = true;
    // XMLOutputter outp = new XMLOutputter(indent, newLines, "gb2312");
    Format format = Format.getPrettyFormat();
    format.setIndent(indent);
    format.setEncoding("utf-8");
    XMLOutputter outp = new XMLOutputter(format);
    try {
      FileOutputStream fout = new FileOutputStream(xmlpath);
      outp.output(doc, fout);
      fout.close();
    }
    catch (java.io.IOException e) {}
  }
}
