package com.redmoon.sns.ui;

import java.io.FileInputStream;
import java.net.URL;
import org.jdom.Document;
import java.io.FileOutputStream;
import org.jdom.output.XMLOutputter;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.apache.log4j.Logger;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import cn.js.fan.cache.jcs.RMCache;
import org.jdom.output.Format;

import com.redmoon.forum.Leaf;
import com.redmoon.forum.person.UserSet;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

public class SkinMgr {
    RMCache rmCache;
    static final String group = "SNS_SKIN";
    static final String ALLSKIN = "ALL_SNS_SKIN";

    static Logger logger;
    static final String FILENAME = "config_sns_skin.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public static String DEFAULT_SKIN_CODE = "default";

    public SkinMgr() {
        rmCache = RMCache.getInstance();

        logger = Logger.getLogger(this.getClass().getName());
        confURL = getClass().getClassLoader().getResource("/" + FILENAME);

    }

    public static void init() {
        if (!isInited) {
            xmlPath = confURL.getPath();
            xmlPath = URLDecoder.decode(xmlPath);

            SAXBuilder sb = new SAXBuilder();
            try {
                FileInputStream fin = new FileInputStream(xmlPath);
                doc = sb.build(fin);
                root = doc.getRootElement();
                fin.close();
                isInited = true;
            } catch (org.jdom.JDOMException e) {
                logger.error(e.getMessage());
            } catch (java.io.IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void setDefaultSkin(String skinCode) {
        init();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(skinCode)) {
                    child.setAttribute("default", "true");
                } else {
                    child.setAttribute("default", "false");
                }
            }
        }
        writemodify();
        reload();
        UserSet.defaultSkin = getDefaultSkinCode();
    }

    public Element getRootElement() {
        return root;
    }

    public void reload() {
        isInited = false;
        try  {
            rmCache.invalidateGroup(group);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 用于页面中获取skin
     * @param code String
     * @return Skin
     */
    public Skin getSkin(String code) {
        if (code==null || code.equals(""))
            return getSkinByCode(getDefaultSkinCode());

        Skin sk = getSkinByCode(code);
        if (sk==null)
            sk = getSkinByCode(getDefaultSkinCode());
        return sk;
    }

    private Skin getSkinByCode(String code) {
        Skin skin = null;
        try {
            skin = (Skin)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error("getSkin:" + e.getMessage());
        }
        if (skin==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String name = child.getChildText("name");
                        String author = child.getChildText("author");
                        String path = child.getChildText(
                                "path");
                        String tableBorderColor = child.getChildText("tableBorderColor");
                        boolean defaultSkin = child.getAttributeValue("default").equals("true");

                        skin = new Skin();
                        skin.setCode(code);
                        skin.setName(name);
                        skin.setAuthor(author);
                        skin.setPath(path);
                        skin.setTableBorderClr(tableBorderColor);
                        skin.setDefaultSkin(defaultSkin);
                        try {
                            rmCache.putInGroup(code, group,
                                               skin);
                        } catch (Exception e) {
                            logger.error("getSkin:" + e.getMessage());
                        }
                        break;
                    }
                }
            }
        }

        return skin;
    }

    public String getDefaultSkinCode() {
        Iterator ir = getAllSkin().iterator();
        while (ir.hasNext()) {
            Skin sk = (Skin)ir.next();
            if (sk.isDefaultSkin())
                return sk.getCode();
        }
        return DEFAULT_SKIN_CODE;
    }

    public Vector getAllSkin() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLSKIN, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v==null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    v.addElement(getSkinByCode(code));
                }
                try {
                    rmCache.putInGroup(ALLSKIN, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllSkin:" + e.getMessage());
                }
            }
        }
        return v;
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
        } catch (java.io.IOException e) {}
    }
    
    public static String getSkinPath(HttpServletRequest request) {
    	String skincode = UserSet.getSkin(request);
    	if (skincode.equals(""))
    		skincode = UserSet.defaultSkin;
    	SkinMgr skm = new SkinMgr();
    	Skin skin = skm.getSkin(skincode);
    	if (skin==null)
    		skin = skm.getSkin(UserSet.defaultSkin);
    	return skin.getPath();
    }

    public static String getSkinPath(HttpServletRequest request, Leaf curLeaf) {
    	String skincode = curLeaf.getSkin();
    	if (skincode.equals("") || skincode.equals(UserSet.defaultSkin)) {
    		skincode = UserSet.getSkin(request);

    		if (skincode==null || skincode.equals(""))
    			skincode = UserSet.defaultSkin;
    	}
    	
    	SkinMgr skm = new SkinMgr();
    	Skin skin = skm.getSkin(skincode);
  	
    	return  skin.getPath();
    	
    }
}
