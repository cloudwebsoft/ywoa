package com.redmoon.forum.plugin;

import java.io.*;

import javax.servlet.http.*;

import cn.js.fan.web.*;
import com.redmoon.forum.plugin.base.*;
import org.apache.log4j.*;

/**
 *
 * <p>Title: 积分单元，详见score.xml</p>
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
public class ScoreUnit implements Serializable {
    public static String TYPE_FORUM = "forum";
    public static String TYPE_BOARD = "board";
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public ScoreUnit() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, "res.config.score", code);
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
    }

    public ScoreUnit(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddValue(int addValue) {
        this.addValue = addValue;
    }

    public void setReplyValue(int replyValue) {
        this.replyValue = replyValue;
    }

    public void setEliteValue(int eliteValue) {
        this.eliteValue = eliteValue;
    }

    public void setDelValue(int delValue) {
        this.delValue = delValue;
    }

    public void setRegistValue(int registValue) {
        this.registValue = registValue;
    }

    public void setLoginValue(int loginValue) {
        this.loginValue = loginValue;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExchange(boolean exchange) {
        this.exchange = exchange;
    }

    public void setDanWei(String danWei) {
        this.danWei = danWei;
    }

    public void setReal(boolean real) {
        this.real = real;
    }

    public void setAddAttachmentValue(int addAttachmentValue) {
        this.addAttachmentValue = addAttachmentValue;
    }

    public void setDelAttachmentValue(int delAttachmentValue) {
        this.delAttachmentValue = delAttachmentValue;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public int getAddValue() {
        return addValue;
    }

    public int getReplyValue() {
        return replyValue;
    }

    public int getEliteValue() {
        return eliteValue;
    }

    public int getDelValue() {
        return delValue;
    }

    public int getRegistValue() {
        return registValue;
    }

    public int getLoginValue() {
        return loginValue;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public boolean isExchange() {
        return exchange;
    }

    public String getDanWei() {
        return danWei;
    }

    public boolean isReal() {
        return real;
    }

    public int getAddAttachmentValue() {
        return addAttachmentValue;
    }

    public int getDelAttachmentValue() {
        return delAttachmentValue;
    }

    public int getRatio() {
        return ratio;
    }

    public boolean isDisplay() {
        return display;
    }

    public IPluginScore getScore() {
        IPluginScore ipu = null;
        try {
            ipu = (IPluginScore) Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.error("getScore:" + e.getMessage());
        }
        return ipu;
    }

    private String code;
    private String author;
    private String className;
    private String name;
    private int addValue;
    private int replyValue;
    private int eliteValue;
    private int delValue;
    private int registValue;
    private int loginValue;
    private void jbInit() throws Exception {
    }

    private String desc;
    private String type;
    private boolean exchange;
    private String danWei;
    private boolean real;
    private int addAttachmentValue = 0;
    private int delAttachmentValue = 0;
    private int ratio;
    private boolean display = true;

}
