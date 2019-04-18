package com.redmoon.forum.plugin;

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
import com.redmoon.forum.plugin.base.IPluginScore;
import org.jdom.output.Format;
import java.net.URLDecoder;

/**
 *
 * <p>Title: 积分管理</p>
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
public class ScoreMgr {
    RMCache rmCache;
    final String group = "SCORE";
    final String ALLSCORE = "ALLSCORE";

    static Logger logger;
    public final String FILENAME = "score.xml";

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public ScoreMgr() {
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

    public String getScoreText(String code, String elementName) {
        init();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    String text = child.getChildText(elementName);

                    return text;
                }
            }
        }
        return null;
    }

    public ScoreUnit getScoreUnit(String code) {
        ScoreUnit pu = null;
        try {
            pu = (ScoreUnit)rmCache.getFromGroup(code, group);
        }
        catch (Exception e) {
            logger.error("getScoreUnit:" + e.getMessage());
        }
        if (pu==null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String name = child.getChildText("name");
                        String author = child.getChildText(
                                "author");
                        String className = child.getChildText("className");
                        String desc = child.getChildText("desc");
                        String type = child.getChildText("type");
                        String exchange = child.getChildText("exchange");
                        int intExchange = Integer.parseInt(exchange);
                        boolean bExchange = intExchange==1?true:false;
                        String danWei = child.getChildText("danWei");
                        String strreal = child.getChildText("real");
                        boolean isReal = true;
                        if (strreal.equals("0"))
                            isReal = false;

                        int addValue = Integer.parseInt(child.getChildText("add"));
                        int eliteValue = Integer.parseInt(child.getChildText("elite"));
                        int delValue = Integer.parseInt(child.getChildText("del"));
                        int registValue = Integer.parseInt(child.getChildText("regist"));
                        int loginValue = Integer.parseInt(child.getChildText("login"));
                        int replyValue = Integer.parseInt(child.getChildText("reply"));
                        int ratio = Integer.parseInt(child.getChildText("ratio"));
                        int addAttachmentValue = 0;
                        int delAttachmentValue = 0;
                        try {
                            addAttachmentValue = Integer.parseInt(child.
                                    getChildText("attachment_add"));
                        }
                        catch (Exception e) {
                            logger.error("getScoreUnit: code=" + code + " " + e.getMessage());
                        }
                        try {
                            delAttachmentValue = Integer.parseInt(child.
                                    getChildText("attachment_del"));
                        }
                        catch (Exception e) {
                            logger.error("getScoreUnit: code=" + code + " " + e.getMessage());
                        }
                        String display = child.getChildText("isDisplay");
                        int intdisplay = Integer.parseInt(display);
                        boolean isDisplay = intdisplay==1?true:false;

                        pu = new ScoreUnit(code);
                        pu.setName(name);
                        pu.setAuthor(author);
                        pu.setClassName(className);
                        pu.setExchange(bExchange);
                        pu.setAddValue(addValue);
                        pu.setEliteValue(eliteValue);
                        pu.setDelValue(delValue);
                        pu.setReplyValue(replyValue);
                        pu.setRegistValue(registValue);
                        pu.setLoginValue(loginValue);
                        pu.setDesc(desc);
                        pu.setType(type);
                        pu.setDanWei(danWei);
                        pu.setReal(false);
                        pu.setAddAttachmentValue(addAttachmentValue);
                        pu.setDelAttachmentValue(delAttachmentValue);
                        pu.setRatio(ratio);
                        pu.setDisplay(isDisplay);

                        try {
                            rmCache.putInGroup(code, group, pu);
                        } catch (Exception e) {
                            logger.error("getScoreUnit:" + e.getMessage());
                        }
                        return pu;
                    }
                }
            }
        }
        else {
            pu.renew();
        }
        return pu;
    }

    public Vector getAllScore() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLSCORE, group);
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
                    v.addElement(getScoreUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLSCORE, group, v);
                }
                catch (Exception e) {
                    logger.error("getAllEntrance:" + e.getMessage());
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

    /**
     * 取得对应于boardCode版面的所有ScoreUnit，此处可以考虑加入缓存
     * @param boardCode String
     * @return Vector
     */
    public Vector getAllScoreUnitOfBoard(String boardCode) {
        Vector v = new Vector();
        Vector vplugin = getAllScore();
        if (vplugin.size() > 0) {
            Iterator irplugin = vplugin.iterator();
            while (irplugin.hasNext()) {
                ScoreUnit pu = (ScoreUnit) irplugin.next();
                IPluginScore ipu = pu.getScore();
                if (ipu.isPluginBoard(boardCode)) {
                    v.addElement(pu);
                }
            }
        }
        return v;
    }
}
