package com.redmoon.sns.app;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.jsp.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.sns.app.base.IAction;
import com.redmoon.sns.app.base.IAppUnit;

import org.apache.log4j.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: 应用单元</p>
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
public class AppUnit implements Serializable {

    public AppUnit() {
    }

    public AppUnit(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setAdminPage(String adminPage) {
        this.adminPage = adminPage;
    }

    public void setClassUnit(String classUnit) {
        this.classUnit = classUnit;
    }

    public void setEnterPage(String enterPage) {
        this.enterPage = enterPage;
    }

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, resource, "name");
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

    public String getAdminPage() {
        return adminPage;
    }

    public String getClassUnit() {
        return classUnit;
    }

    public String getEnterPage() {
        return enterPage;
    }

    /**
     * 取得插件unit类的实例，如InfoUnit
     * @return IPluginUnit
     */
    public IAppUnit getUnit() {
    	IAppUnit ipu = null;
        try {
            ipu = (IAppUnit) Class.forName(classUnit).newInstance();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return ipu;
    }

    public IAction getAction() {
        return getUnit().getAction();
    }

    private String code;
    private String resource;
    private String adminPage;
    private String classUnit;
    private String enterPage;

}
