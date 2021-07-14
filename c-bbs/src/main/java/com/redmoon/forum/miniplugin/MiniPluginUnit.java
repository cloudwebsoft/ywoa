package com.redmoon.forum.miniplugin;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ResBundle;
import cn.js.fan.web.SkinUtil;
import org.apache.log4j.Logger;

public class MiniPluginUnit implements Serializable {
    public final String TYPE_BOARD = "board"; // 应用于版块型
    public final String TYPE_ALLBOARD = "allBoard";  // 应用于所有版块
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public MiniPluginUnit() {
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
    }

    public MiniPluginUnit(String code) {
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

    public void setPlugin(boolean plugin) {
        this.plugin = plugin;
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

    public boolean isPlugin() {
        return plugin;
    }

    private String code;
    private String resource;
    private String adminEntrance;
    private boolean plugin = true;

}
