package com.redmoon.forum;

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
import cn.js.fan.util.*;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.treasure.TreasureUserDb;
import org.jdom.output.Format;
import cn.js.fan.web.SkinUtil;
import java.net.URLDecoder;
import java.util.zip.DataFormatException;
import org.jdom.Attribute;

/**
 *
 * <p>Title: 管理灌水宝贝config_treasure.xml</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TreasureMgr {
    RMCache rmCache;
    final String group = "TREASURE";
    final String ALLENTRANCE = "ALLTREASURE";

    static Logger logger;
    public final String FILENAME = "config_treasure.xml";
    private XMLProperties properties;

    public static Document doc = null;
    public static Element root = null;
    public static String xmlPath;
    public static boolean isInited = false;
    public static URL confURL;

    public TreasureMgr() {
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
        if (root == null) {
            init();
        }
        return root;
    }

    public void reload() {
        isInited = false;
        try {
            rmCache.invalidateGroup(group);
        } catch (Exception e) {
            logger.error("reload:" + e.getMessage());
        }
    }

    public boolean use(HttpServletRequest request, String userName, String code,
                       long msgId) throws ErrMsgException {
        if (code.equals("changeColorRed")) {
            // 检查用户有无此宝贝
            TreasureUserDb tud = new TreasureUserDb();
            tud = tud.getTreasureUserDb(userName, code);
            if (!tud.isLoaded() || tud.getAmount() == 0) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.forum.TreasureMgr", "err_no_treasure"));
            }

            MsgDb md = new MsgDb();
            md = md.getMsgDb(msgId);
            java.util.Date d = md.getColorExpire();
            try {
                if (md.ChangeColor(userName, "red",
                                   DateUtil.addDate(d,
                        getTreasureUnit(code).getDay()), StrUtil.getIp(request))) {
                    // 将宝贝的数目减去1
                    int amount = tud.getAmount();
                    amount -= 1;
                    if (amount > 0) {
                        tud.setAmount(tud.getAmount() - 1);
                        tud.save();
                    } else {
                        tud.del();
                    }
                }
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }
        else if (code.equals("changeBold")) {
            // 检查用户有无此宝贝
            TreasureUserDb tud = new TreasureUserDb();
            tud = tud.getTreasureUserDb(userName, code);
            if (!tud.isLoaded() || tud.getAmount() == 0) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.forum.TreasureMgr", "err_no_treasure"));
            }

            MsgDb md = new MsgDb();
            md = md.getMsgDb(msgId);
            java.util.Date d = md.getColorExpire();
            try {
                if (md.ChangeBold(userName, 1,
                                  DateUtil.addDate(d,
                        getTreasureUnit(code).getDay()), StrUtil.getIp(request))) {
                    // 将宝贝的数目减去1
                    int amount = tud.getAmount();
                    amount -= 1;
                    if (amount > 0) {
                        tud.setAmount(tud.getAmount() - 1);
                        tud.save();
                    } else {
                        tud.del();
                    }
                }
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }
        else if (code.equals("changeOnTop")) {
            // 检查用户有无此宝贝
            TreasureUserDb tud = new TreasureUserDb();
            tud = tud.getTreasureUserDb(userName, code);
            if (!tud.isLoaded() || tud.getAmount() == 0) {
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        "res.forum.TreasureMgr", "err_no_treasure"));
            }

            java.util.Date d = new java.util.Date();
            MsgMgr mm = new MsgMgr();
            if (mm.setOnTop(request, msgId, MsgDb.LEVEL_TOP_BOARD,
                            DateUtil.addDate(d,
                                             getTreasureUnit(code).getDay()))) {
                // 将宝贝的数目减去1
                int amount = tud.getAmount();
                amount -= 1;
                if (amount > 0) {
                    tud.setAmount(tud.getAmount() - 1);
                    tud.save();
                } else {
                    tud.del();
                }
            }

        }
        else
            throw new ErrMsgException("Threasure " + code + " is not found.");
        return true;
    }

    public void setTreasureCount(String code, int count) {
        init();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                logger.info("ecode=" + ecode + " code=" + code);
                if (ecode.equals(code)) {
                    child.getChild("count").setText("" + count);
                    writemodify();
                    return;
                }
            }
        }
    }
    /**
     *
     * 通过code来检查得到下面子元素，如果没有该元素，就从price结点下来找score元素。
     * @param code String
     * @param strTemp String
     * @return Element
     */
    public Element getElement(String code, String name) {
        init();
        List list = root.getChildren();
        if (list != null) {
            Iterator ir = list.iterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                logger.info("ecode=" + ecode + " code=" + code);
                if (ecode.equals(code)) {
                    if (child.getChild(name) != null) {
                        return child.getChild(name);
                    } else {
                        //如果name不在child结点元素下那检查price下面的元素.

                        Iterator sir = child.getChild("price").getChildren().
                                       iterator();
                        while (sir.hasNext()) {
                            Element score = (Element) sir.next();
                            String scode = score.getAttributeValue("code");
                            if (scode.equals(name)) {
                                return score.getChild("value");
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void setTreasure(String code, String name, String value) {
        Element element = getElement(code, name);

        if (element != null) {
            element.setText(value);
        }
    }

    public void addPrice(String code, String name, String value) throws
            ErrMsgException {
        Element element = getElement(code, name);
        if (element != null) {
            throw new ErrMsgException("已有相同编码！");
        }
        element = getElement(code, "price");
        Element socreelement = new Element("score");
        socreelement.setAttribute(new Attribute("code", name));
        Element velement = new Element("value");
        velement.setText(value);
        socreelement.addContent(velement);
        element.addContent(socreelement);
    }


    public String getProperty(String name) {
        String v = null;
        if (v == null) {
            if (root == null) {
                init();
            }
            v = properties.getProperty(name);
        }
        return v;
    }


    public TreasureUnit getTreasureUnit(String code) {
        TreasureUnit tu = null;
        try {
            tu = (TreasureUnit) rmCache.getFromGroup(code, group);
        } catch (Exception e) {
            logger.error("getTreasureUnit1:" + e.getMessage());
        }
        if (tu == null) {
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String ecode = child.getAttributeValue("code");
                    if (ecode.equals(code)) {
                        String name = child.getChildText("name");
                        String className = child.getChildText("className");
                        String desc = child.getChildText("desc");
                        String image = child.getChildText("image");
                        String sCount = child.getChildText("count");
                        int count = 0;
                        if (StrUtil.isNumeric(sCount)) {
                            count = Integer.parseInt(sCount);
                        }
                        String sDay = child.getChildText("day");
                        int day = 0;
                        if (StrUtil.isNumeric(sDay)) {
                            day = Integer.parseInt(sDay);
                        }

                        Element ePrice = child.getChild("price");
                        List listScore = ePrice.getChildren("score");
                        Vector price = new Vector();
                        if (listScore != null) {
                            Iterator irScore = listScore.iterator();
                            while (irScore.hasNext()) {
                                Element eScore = (Element) irScore.next();
                                String scoreCode = eScore.getAttributeValue(
                                        "code");
                                String scoresValue = eScore.getChildText(
                                        "value");
                                double value = 0;
                                /*
                                 if (StrUtil.isNumeric(scoresValue)) {
                                     value = Double.parseDouble(scoresValue);
                                 }
                                 */
                                value = StrUtil.toDouble(scoresValue, 0.0);
                                TreasurePrice ts = new TreasurePrice();
                                ts.scoreCode = scoreCode;
                                ts.value = value;
                                price.addElement(ts);
                            }
                        }

                        tu = new TreasureUnit(code);
                        tu.setName(name);
                        tu.setClassName(className);
                        tu.setDesc(desc);
                        tu.setImage(image);
                        tu.setCount(count);
                        tu.setPrice(price);
                        tu.setDay(day);
                        try {
                            rmCache.putInGroup(code, group, tu);
                        } catch (Exception e) {
                            logger.error("getTreasureUnit2:" + e.getMessage());
                        }
                    }
                }
            } else {
                logger.error("getTreasureUnit:Error when parsing！");
            }
        } else {
            tu.renew();
        }
        return tu;
    }

    public Vector getAllTreasure() {
        Vector v = null;
        try {
            v = (Vector) rmCache.getFromGroup(ALLENTRANCE, group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (v == null) {
            v = new Vector();
            init();
            List list = root.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element child = (Element) ir.next();
                    String code = child.getAttributeValue("code");
                    v.addElement(getTreasureUnit(code));
                }
                try {
                    rmCache.putInGroup(ALLENTRANCE, group, v);
                } catch (Exception e) {
                    logger.error("getAllTreasure:" + e.getMessage());
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
        finally {
            reload();
        }
    }

}
