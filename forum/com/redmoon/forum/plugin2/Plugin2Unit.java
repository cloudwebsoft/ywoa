package com.redmoon.forum.plugin2;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ResBundle;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import com.redmoon.forum.plugin2.base.IPlugin2Unit;
import org.apache.log4j.Logger;

public class Plugin2Unit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public Plugin2Unit() {
    }

    public void renew() {
        if (logger==null) {
            logger = Logger.getLogger(this.getClass().getName());
        }
    }

    public Plugin2Unit(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setAdminEntrance(String adminEntrance) {
        this.adminEntrance = adminEntrance;
    }

    public void setClassUnit(String classUnit) {
        this.classUnit = classUnit;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setButton(String button) {
        this.button = button;
    }

    public void setAddTopicPage(String addTopicPage) {
        this.addTopicPage = addTopicPage;
    }

    public void setEditTopicPage(String editTopicPage) {
        this.editTopicPage = editTopicPage;
    }

    public void setAddReplyPage(String addReplyPage) {
        this.addReplyPage = addReplyPage;
    }

    public String getName(HttpServletRequest request) {
        ResBundle rb = new ResBundle(resource, SkinUtil.getLocale(request));
        return rb.get("name");
    }

    public String LoadString(HttpServletRequest request, String key) {
        ResBundle rb = new ResBundle(resource, SkinUtil.getLocale(request));
        if (rb == null)
            return "";
        else
            return rb.get(key);
    }

    public String getDesc(HttpServletRequest request) {
        ResBundle rb = new ResBundle(resource, SkinUtil.getLocale(request));
        return rb.get("desc");
    }

    public String getCode() {
        return code;
    }

    public String getResource() {
        return resource;
    }

    public String getAdminEntrance() {
        return adminEntrance;
    }

    public String getClassUnit() {
        return classUnit;
    }

    public String getDesc() {
        return desc;
    }

    public String getButton() {
        return button;
    }

    public String getAddTopicPage() {
        return addTopicPage;
    }

    public String getEditTopicPage() {
        return editTopicPage;
    }

    public String getAddReplyPage() {
        return addReplyPage;
    }

    public IPlugin2Unit getUnit() {
        IPlugin2Unit ipu = null;
        try {
            ipu = (IPlugin2Unit) Class.forName(classUnit).newInstance();
        } catch (Exception e) {
            logger.error("getUnit:" + e.getMessage());
        }
        return ipu;
    }

    public IPluginMsgAction getMsgAction() {
        return getUnit().getMsgAction();
    }

    private String code;
    private String resource;
    private String addTopicPage;
    private String adminEntrance;
    private String classUnit;
    private String desc;
    private String button;
    private String editTopicPage;
    private String addReplyPage;

}
