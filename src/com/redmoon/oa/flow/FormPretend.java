package com.redmoon.oa.flow;

import java.io.StringReader;
import java.security.PublicKey;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import cn.js.fan.web.Global;

import com.redmoon.oa.kernel.License;

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
public class FormPretend {
    static FormPretend formPretend = null;
    Element root;
    private static Object initLock = new Object();

    boolean valid = false;

    /**
     * 单位注册码
     */
    private String key;

    public FormPretend() {

    }

    public static FormPretend getInstance() {
        if (formPretend==null) {
            synchronized (initLock) {
                formPretend = new FormPretend();
                formPretend.init();
            }
        }
        return formPretend;
    }

    public void init() {
        veri();
    }

    public void toXML(String licenseXMLString) {
        SAXBuilder sb = new SAXBuilder();
        try {
            StringReader sr = new StringReader(licenseXMLString);
            org.jdom.Document doc = sb.build(sr);
            sr.close();

            root = doc.getRootElement();

            // System.out.println("License userCount=" + root.getChild("userCount").getText());
            // 4.0后增加该项
            if (root.getChild("key")!=null)
                key = root.getChild("key").getText();

        } catch (org.jdom.JDOMException e) {
            System.out.println("init:" + e.getMessage());
        } catch (java.io.IOException e) {
            System.out.println("init:" + e.getMessage());
        }
    }

    /**
     * 根据公钥对license.dat进行验证并重建XML Document
     * @return boolean
     */
    public boolean veri() {
        String str1 = "licen";
        String str2 = "se.dat";
        String str = str1 + str2;
        String filePath = Global.getAppPath() + "WEB-INF/";
        try {
            java.io.ObjectInputStream in =
                    new java.io.ObjectInputStream(new
                                                  java.io.FileInputStream(
                    filePath + "publickey.dat"));
            PublicKey pubkey = (PublicKey) in.readObject();
            in.close();

            in = new java.io.ObjectInputStream(new
                                               java.io.FileInputStream(
                    filePath + str));
            // 取得license.xml
            String info = (String) in.readObject();
            // 取得签名
            byte[] signed = (byte[]) in.readObject();
            in.close();

            java.security.Signature
                    signetcheck = java.security.
                                  Signature.getInstance("DSA");
            signetcheck.initVerify(pubkey);
            signetcheck.update(info.getBytes("UTF-8"));
            if (signetcheck.verify(signed)) {
                toXML(info);
                valid = true;
            } else {
                valid = false;
                // System.out.println("Cloud Web license is invalid.");
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        // 置为试用版
        if (!valid) {
            License.getInstance().setUserCount(License.TEST_VERSTION_USER_COUNT);
        }
        return valid;
    }
}
