package com.redmoon.oa.sms;

import java.io.FileInputStream;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jdom.Document;
import java.util.Iterator;
import org.jdom.input.SAXBuilder;
import cn.js.fan.util.XMLProperties;
import org.jdom.Element;
import java.net.URLDecoder;
import org.jdom.output.XMLOutputter;
import java.io.FileOutputStream;
import org.jdom.output.Format;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import java.sql.SQLException;
import cn.js.fan.util.StrUtil;

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
public class Config {
    private XMLProperties properties;
    private final String CONFIG_FILENAME = "config_sms.xml";

    public static final int SMS_BOUNDARY_DEFAULT = 0;
    public static final int SMS_BOUNDARY_YEAR = 1;
    public static final int SMS_BOUNDARY_MONTH = 2;
    public static final int CONTEXT_DIV = 70;

    private String cfgpath;

    Logger logger;
    Document doc = null;
    Element root = null;

    public Config() {
        URL cfgURL = getClass().getClassLoader().getResource("/" +
                CONFIG_FILENAME);
        cfgpath = cfgURL.getFile();
        cfgpath = URLDecoder.decode(cfgpath);

        properties = new XMLProperties(cfgpath);

        logger = Logger.getLogger(Config.class.getName());

        SAXBuilder sb = new SAXBuilder();
        try {
            FileInputStream fin = new FileInputStream(cfgpath);
            doc = sb.build(fin);
            root = doc.getRootElement();
            fin.close();
        } catch (org.jdom.JDOMException e) {
            logger.error("Config:" + e.getMessage());
        } catch (java.io.IOException e) {
            logger.error("Config:" + e.getMessage());
        }
    }

    public Element getRootElement() {
        return root;
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public void setUsed(String code, boolean isUsed) {
        Iterator ir = root.getChildren().iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String c = e.getAttributeValue("code");
            if (c.equals(code)) {
                e.setAttribute("isUsed", isUsed ? "true" : "false");
            }
        }
    }

    public String getIsUsedClassName() {
        Iterator ir = root.getChildren("sms").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String isUsed = e.getAttributeValue("isUsed");
            if (isUsed.equals("true")) {
                return e.getChildText("className");
            }
        }
        return "";
    }

    public void setBoundary(int boundary) {
        Element which = root.getChild("sms").getChild("boundary");
        if (which != null) {
            which.setText(boundary + "");
        }
        writemodify();
    }

    public void setIsUsed(boolean isUsed) {
        Iterator ir = root.getChildren("sms").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            e.setAttribute("isUsed", isUsed ? "true" : "false");
        }
        writemodify();
    }

    public String getIsUsedProperty(String prop) {
        Iterator ir = root.getChildren("sms").iterator();
        while (ir.hasNext()) {
            Element e = (Element) ir.next();
            String isUsed = e.getAttributeValue("isUsed");
            if (isUsed.equals("true")) {
                return e.getChildText(prop);
            }
        }
        return "";
    }

    public void setMonthTotal(int total) {
        Element which = root.getChild("sms").getChild("monthTotal");
        if (which != null) {
            which.setText(total + "");
        }
        writemodify();
    }

    public void saveYearBoundary(int total, String beginDate, String endDate) {
        Element which = root.getChild("sms").getChild("yearTotal");
        if (which != null) {
            which.setText(total + "");
        }
        which = root.getChild("sms").getChild("beginDate");
        if (which != null) {
            which.setText(beginDate);
        }
        which = root.getChild("sms").getChild("endDate");
        if (which != null) {
            which.setText(endDate);
        }
        writemodify();
    }

    public void saveYearRemind(String yearRemindTitle,String yearRemindContent,int yearBoundary){
        Element which = root.getChild("sms").getChild("yearRemindTitle");
        if (which != null) {
            which.setText(yearRemindTitle);
        }
        which = root.getChild("sms").getChild("yearRemindContent");
        if (which != null) {
            which.setText(yearRemindContent);
        }
        which = root.getChild("sms").getChild("yearBoundary");
        if (which != null) {
            which.setText(yearBoundary+"");
        }
        writemodify();
    }

    public void saveMonthRemind(String monthRemindTitle,String monthRemindContent,int monthBoundary){
        Element which = root.getChild("sms").getChild("monthRemindTitle");
        if (which != null) {
            which.setText(monthRemindTitle);
        }
        which = root.getChild("sms").getChild("monthRemindContent");
        if (which != null) {
            which.setText(monthRemindContent);
        }
        which = root.getChild("sms").getChild("monthBoundary");
        if (which != null) {
            which.setText(monthBoundary+"");
        }
        writemodify();
    }

    public void saveYearRemindDate(String yearRemindDate){
        Element which = root.getChild("sms").getChild("yearRemindDate");
        if (which != null) {
            which.setText(yearRemindDate);
        }
        writemodify();
    }

    public void saveMonthRemindDate(String monthRemindDate){
        Element which = root.getChild("sms").getChild("monthRemindDate");
        if (which != null) {
            which.setText(monthRemindDate);
        }
        writemodify();
    }


    public IMsgUtil getIsUsedIMsg() {
        String className = getIsUsedClassName();
        if (className.equals(""))
            return null;
        IMsgUtil imsg = null;
        try {
            Class cls = Class.forName(className);
            imsg = (IMsgUtil) cls.newInstance();
        } catch (ClassNotFoundException cnfe) {
            System.out.println("getIsUsedIMsg: ClassNotFoundException:" +
                               cnfe.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imsg;
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
            FileOutputStream fout = new FileOutputStream(cfgpath);
            outp.output(doc, fout);
            fout.close();
        } catch (java.io.IOException e) {}
    }

    public int getBoundary() {
        return StrUtil.toInt(getProperty("sms.boundary"), SMS_BOUNDARY_DEFAULT);
    }

    /**
     * 判断单条短信是否可以被发送，当短信配额设置错误时不能发送任何短信。暂时没用
     * @return boolean
     * @throws SQLException
     */
    public boolean canSendSMS() throws SQLException {
        int boundary = getBoundary();
        if (boundary == SMS_BOUNDARY_DEFAULT) { //没有进行短信配额设置
            return true;
        } else if (boundary == SMS_BOUNDARY_MONTH) { //按月设置
            int month = DateUtil.getMonth(new Date());
            SMSBoundaryMonthMgr sbmMgr = new SMSBoundaryMonthMgr();
            int remainCount = sbmMgr.getRemainingCount(month);
            if (remainCount > 0) {
                return true;
            } else {
                return false;
            }
        } else if (boundary == SMS_BOUNDARY_YEAR) { //按年设置
            SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
            Date beginDate = sbyMgr.getBeginDate();
            Date endDate = sbyMgr.getEndDate();
            Date now = new Date();
            if (now.before(endDate) && now.after(beginDate)) {
                int remainCount = sbyMgr.getRemainingCount();
                if (remainCount > 0) {
                    return true;
                } else {
                    return false;
                }
            } else { //不在生效期内，按照没有进行短信配额设置计
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一组短信是否可以被发送，返回可以发送的条数。当短信配额设置错误时不能发送任何短信。sendJob专用
     * count 表示一组短信的条数
     * @param count int
     * @return boolean
     */
    public int canSendSMS(int count) throws SQLException {
        int boundary = StrUtil.toInt(getProperty("sms.boundary"),
                                     SMS_BOUNDARY_DEFAULT);
        if (boundary == SMS_BOUNDARY_DEFAULT) { //没有进行短信配额设置
            return count;
        } else if (boundary == SMS_BOUNDARY_MONTH) { //按月设置
            int month = DateUtil.getMonth(new Date());
            SMSBoundaryMonthMgr sbmMgr = new SMSBoundaryMonthMgr();
            int remainCount = sbmMgr.getRemainingCount(month);
            if (remainCount > count) {
                return count;
            } else {
                return remainCount;
            }
        } else if (boundary == SMS_BOUNDARY_YEAR) { //按年设置
            SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
            Date beginDate = sbyMgr.getBeginDate();
            Date endDate = sbyMgr.getEndDate();
            Date now = new Date();
            if (now.before(endDate) && now.after(beginDate)) {
                int remainCount = sbyMgr.getRemainingCount();
                if (remainCount > count) {
                    return count;
                } else {
                    return remainCount;
                }
            } else { //不在生效期内，按照没有进行短信配额设置计
                return count;
            }
        }
        return 0;
    }

    /**
     * 判断一组短信是否可以被发送，返回可以发送的条数。当短信配额设置错误时不能发送任何短信。页面发短信专用
     * count 表示一组短信的条数
     * @param count int
     * @param msgLength int 短信长度，每超过70字会自动分成2条发送
     * @return boolean
     */
    public int canSendSMS(int count, int msgLength) throws SQLException {
        if (!SMSFactory.isUseSMS())
            return 0;

        int boundary = StrUtil.toInt(getProperty("sms.boundary"),
                                     SMS_BOUNDARY_DEFAULT);
        if (boundary == SMS_BOUNDARY_DEFAULT) { //没有进行短信配额设置
            return count;
        } else if (boundary == SMS_BOUNDARY_MONTH) { //按月设置
            int month = DateUtil.getMonth(new Date());
            SMSBoundaryMonthMgr sbmMgr = new SMSBoundaryMonthMgr();
            int remainCount = sbmMgr.getRemainingCount(month);
            if (remainCount > (count * getDivNumber(msgLength))) {
                return count;
            } else {
                return remainCount;
            }
        } else if (boundary == SMS_BOUNDARY_YEAR) { //按年设置
            SMSBoundaryYearMgr sbyMgr = new SMSBoundaryYearMgr();
            Date beginDate = sbyMgr.getBeginDate();
            Date endDate = sbyMgr.getEndDate();
            Date now = new Date();
            if (now.before(endDate) && now.after(beginDate)) {
                int remainCount = sbyMgr.getRemainingCount();
                if (remainCount > (count) * getDivNumber(msgLength)) {
                    return count;
                } else {
                    return remainCount;
                }
            } else { //不在生效期内，按照没有进行短信配额设置计
                return count;
            }
        }
        return 0;
    }

    /**
     * 根据短信长度返回自动切分条数
     * @param msgLength int
     * @return int
     */
    public int getDivNumber(int msgLength) {
        if (false) { //不自动切分
            return 1;
        }
        if (msgLength % CONTEXT_DIV == 0) {
            return msgLength / CONTEXT_DIV;
        }
        return (msgLength / CONTEXT_DIV + 1);
    }

    /**
     * 判断是否为合法的手机号
     * @param mobile String
     * @return boolean
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null) {
            return false;
        } else if (mobile.length() != 11) {
            return false;
        } else if (!mobile.startsWith("1")) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否为合法的移动号码
     * @param mobile String
     * @return boolean
     */
    public static boolean isValidYDMobile(String mobile) {
        if (isValidMobile(mobile)) {
            if (!mobile.startsWith("139") && !mobile.startsWith("138") &&
                !mobile.startsWith("137")
                && !mobile.startsWith("136") && !mobile.startsWith("135") &&
                !mobile.startsWith("134")
                && !mobile.startsWith("150") && !mobile.startsWith("151") &&
                !mobile.startsWith("152")
                && !mobile.startsWith("158") && !mobile.startsWith("159") &&
                !mobile.startsWith("188")) {
                // System.out.println(getClass()+"ddd");
                return false;
            }
        }
        return true;
    }
}
