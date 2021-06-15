package cn.js.fan.module.cms.ui;

import java.io.*;

import javax.servlet.http.*;

import cn.js.fan.web.*;
import org.apache.log4j.*;

public class DesktopUnit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());
    public static final String TYPE_LIST = "list";
    public static final String TYPE_DOCUMENT = "document";

    public DesktopUnit(String code) {
        this.code = code;
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPageList(String pageList) {
        this.pageList = pageList;
    }

    public void setPageShow(String pageShow) {
        this.pageShow = pageShow;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public String getPageList() {
        return pageList;
    }

    public String getPageShow() {
        return pageShow;
    }

    public String getType() {
        return type;
    }

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, "res.config.DesktopUnit", code);
    }

    public IDesktopUnit getIDesktopUnit() {
        IDesktopUnit ipu = null;
        try {
            ipu = (IDesktopUnit) Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.error("getIDesktopUnit:" + e.getMessage());
        }
        return ipu;
    }

    private String code;
    private String className;
    private String name;
    private String pageList;
    private String pageShow;
    private String type;

}
