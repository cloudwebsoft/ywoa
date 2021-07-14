package cn.js.fan.sso;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import cn.js.fan.util.StrUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;

public class Config {
    // public: constructor to load driver and connect db
    boolean debug = true;
    final String configxml = "ssoconfig.xml";
    String xmlpath = "";
    Document doc = null;
    Element root = null;
    String deskey = "bluewind"; //DES密钥长度为64bit，1个字母为八位，需8个字母，不能超过8个，否则会出错

    public Config() {
        URL confURL = getClass().getResource("/" + configxml);
        xmlpath = confURL.getFile();
        xmlpath = URLDecoder.decode(xmlpath);

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(xmlpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {} catch (java.io.IOException e) {
        }
    }

    public Element getRootElement() {
        return root;
    }

    public String getLoginURL(String kind) {
        Element webapp = root.getChild("webapp");

        java.util.List webapplist = webapp.getChildren();
        Iterator ir = webapplist.iterator();
        String attr = "";
        while (ir.hasNext()) {
            Element a = (Element) ir.next(); //得到第i个field元素
            attr = a.getAttribute("kind").getValue();
            if (attr != null && attr.equals(kind)) {
                Element loginurl = a.getChild("loginurl");
                if (loginurl != null)
                    return loginurl.getText();
                break;
            }
        }
        return null;
    }

    public String getDefaultURL(String kind) {
        Element webapp = root.getChild("webapp");
        java.util.List webapplist = webapp.getChildren();
        Iterator ir = webapplist.iterator();
        String attr = "";
        while (ir.hasNext()) {
            Element a = (Element) ir.next(); //得到第i个field元素
            attr = a.getAttribute("kind").getValue();
            if (attr != null && attr.equals(kind)) {
                Element loginurl = a.getChild("defaulturl");
                if (loginurl != null)
                    return loginurl.getText();
                break;
            }
        }
        return null;
    }

    public String getKey() {
        Element which = root.getChild("key");
        if (which == null)
            return null;
        String s = StrUtil.getNullStr(which.getText());
        /*
            byte[] key = deskey.getBytes(); //DES密钥长度为64bit
            String re = "";
            try {
              byte[] dstr = SecurityUtil.decodehexstr(pwd, key);
              if (dstr != null)
                re = new String(dstr);
            }
            catch (java.lang.Exception e) {
                System.out.println("DES decode error:" + e.getMessage());
            }
            return re;
         */
        return s;
    }


    public String getDoorUrl() {
        Element which = root.getChild("doorurl");
        if (which == null)
            return null;
        String url = StrUtil.getNullStr(which.getText());
        return url;
    }

    public boolean setKey(String pwd) {
        Element which = root.getChild("site").getChild("key");
        if (which == null)
            return false;
        /*
            byte[] key = deskey.getBytes();//DES密钥长度为64bit
            try {
              pwd = SecurityUtil.encode2hex(pwd.getBytes(), key);
            }
            catch (Exception e) {
                System.out.println("DES encode error:" + e.getMessage());
            }
              }
         */
        which.setText(pwd);
        writemodify();
        return true;
    }

    public void writemodify() {
        String indent = "    ";
        boolean newLines = true;
        // XMLOutputter outp = new XMLOutputter(indent, newLines, "gb2312");
        Format format = Format.getPrettyFormat();
        format.setIndent(indent);
        format.setEncoding("gb2312");
        XMLOutputter outp = new XMLOutputter(format);

        try {
            FileOutputStream fout = new FileOutputStream(xmlpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
    }
}
