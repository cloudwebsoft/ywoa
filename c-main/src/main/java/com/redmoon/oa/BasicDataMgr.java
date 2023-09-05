package com.redmoon.oa;

import java.io.FileInputStream;
import java.net.URL;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import java.io.FileOutputStream;
import org.jdom.Element;
import java.util.List;
import java.util.Iterator;
import org.jdom.output.Format;
import java.net.URLDecoder;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class BasicDataMgr {
    String moduleName;

    String xmlPath;
    Document doc = null;
    Element root = null;

    public BasicDataMgr(String moduleName) {
        this.moduleName = moduleName;
        String xmlFile = "module/" + moduleName + "_basic.xml";
        URL confURL = getClass().getResource("/" + xmlFile);
        xmlPath = confURL.getFile();
        xmlPath = URLDecoder.decode(xmlPath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(xmlPath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("BasicDataMgr1:" + e.getMessage());
        } catch (java.io.IOException e) {
            com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error("BasicDataMgr2:" + e.getMessage());
        }
    }

    public Element getRootElement() {
      return root;
    }

    /**
     * 取得项目的描述
     * @param itemCode String
     * @param itemValue String
     * @return String
     */
    public String getItemText(String itemCode, String itemValue) {
        DataItem di = getDataItem(itemCode);
        String[][] options = di.options;
        if (options!=null) {
            int len = options.length;
            for (int i=0; i<len; i++) {
                if (options[i][1].equals(itemValue)) {
                    return options[i][0];
                }
            }
        }
        return "";
    }

    public String[][] getOptions(String itemCode) {
        DataItem di = getDataItem(itemCode);
        if (di==null) {
            return null;
        }
        return di.options;
    }

    public DataItem getDataItem(String itemCode) {
      List childs = root.getChildren();
      if (childs==null) {
          return null;
      }
      Iterator ir = childs.iterator();
      int i = 0;
      DataItem di = new DataItem();
      String[][] r = null;
      while (ir.hasNext()) {
          Element e = (Element)ir.next();
          // 找到对应的itemName
          if (e.getAttributeValue("code").equalsIgnoreCase(itemCode)) {
              List chs = e.getChild("options").getChildren();
              if (chs!=null) {
                  r = new String[chs.size()][2];
                  Iterator chir = chs.iterator();
                  while (chir.hasNext()) {
                      Element e1 = (Element)chir.next();
                      r[i][0] = e1.getText();
                      r[i][1] = e1.getAttributeValue("value");
                      i++;
                  }
                  di.options = r;
              }
              di.defaultValue = e.getChild("defaultValue").getText();
              break;
          }
      }
      return di;
    }

    /**
     * 取得项目的options
     * @param itemCode String
     * @return String
     */
    public String getOptionsStr(String itemCode) {
        DataItem di = getDataItem(itemCode);
        if (di==null)
            return "";
        String[][] r = di.options;
        if (r==null)
            return "";        
        String defaultValue = di.defaultValue;
        int len = r.length;
        String str = "";
        for (int i=0; i<len; i++) {
            if (r[i][1].equals(defaultValue))
                str += "<option value='" + r[i][1] + "' selected>" + r[i][0] + "</option>";
            else
                str += "<option value='" + r[i][1] + "'>" + r[i][0] + "</option>";
        }
        return str;
    }

    public boolean addOption(String value,String itemCode) {
      List childs = root.getChildren();
      boolean re = false;
      if (childs==null)
          return re;
      Iterator ir = childs.iterator();
      while (ir.hasNext()) {
          Element e = (Element) ir.next();
          if (e.getAttributeValue("code").equals(itemCode)) {
              Element options = e.getChild("options");
              Element which = new Element("option");
              which.addContent(value);
              which.setAttribute("value", value);
              options.addContent(which);
              writemodify();
              re = true;
              break;
          }
      }
      return re;
    }

    public boolean delOption(String value,String itemCode) {
      List childs = root.getChildren();
      boolean re = false;
      if (childs==null)
          return re;
      Iterator ir = childs.iterator();
      while (ir.hasNext()) {
          Element e = (Element) ir.next();
          if (e.getAttributeValue("code").equals(itemCode)) {
              List chs = e.getChild("options").getChildren();
              if (chs!=null) {
                  Iterator chir = chs.iterator();
                  while (chir.hasNext()) {
                      Element e1 = (Element) chir.next();
                      if (e1.getAttributeValue("value").equals(value)) {
                          re = e.getChild("options").removeContent(e1);
                          if(re){
                              writemodify();
                          }
                          return re;
                      }
                  }
              }
          }
      }
      return re;
    }

    public boolean setDefaultValue(String value, String itemCode) {
        List childs = root.getChildren();
        boolean re = false;
        if (childs == null)
            return re;
        Iterator ir = childs.iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            if (e.getAttributeValue("code").equals(itemCode)) {
                e.getChild("defaultValue").setAttribute("value", value);
                e.getChild("defaultValue").setText(value);
                writemodify();
                re = true;
                break;
            }
        }
        return re;
    }


    public void writemodify() {
      String indent = "    ";
      boolean newLines = true;
      // XMLOutputter outp = new XMLOutputter(indent, newLines, "utf-8");
      Format format = Format.getPrettyFormat();
      format.setIndent(indent);
        format.setEncoding("utf-8");
        XMLOutputter outp = new XMLOutputter(format);
        try {
        FileOutputStream fout = new FileOutputStream(xmlPath);
        outp.output(doc, fout);
        fout.close();
      }
      catch (java.io.IOException e) {}
  }

  class DataItem {
      String[][] options;
      String defaultValue;
  }
}
