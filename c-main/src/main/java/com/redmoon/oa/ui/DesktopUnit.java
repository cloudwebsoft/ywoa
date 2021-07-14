package com.redmoon.oa.ui;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.web.SkinUtil;
import org.apache.log4j.Logger;

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

    public void setDef(boolean def) {
        this.def = def;
    }

    public void setDefaultCol(int defaultCol) {
        this.defaultCol = defaultCol;
    }

    public void setDefaultOrder(int defaultOrder) {
        this.defaultOrder = defaultOrder;
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

    public boolean isDef() {
        return def;
    }

    public int getDefaultCol() {
        return defaultCol;
    }

    public int getDefaultOrder() {
        return defaultOrder;
    }

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
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
    private boolean def = false;
    private int defaultCol;
    private int defaultOrder;
    private boolean display = false;

}
