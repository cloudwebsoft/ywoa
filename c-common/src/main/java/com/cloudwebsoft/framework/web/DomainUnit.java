package com.cloudwebsoft.framework.web;

import java.io.*;

import javax.servlet.http.*;

import cn.js.fan.web.*;
import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.util.*;
import java.util.HashMap;
import java.util.Map;

public class DomainUnit implements Serializable {
    public static Map IDispatchers = new HashMap();

    public DomainUnit(String subDomain) {
        this.subDomain = subDomain;
    }

    public void setCode(String subDomain) {
        this.subDomain = subDomain;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

    public String getCode() {
        return subDomain;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public boolean isUsed() {
        return used;
    }

    public String getUrl() {
        return url;
    }

    public String getSubDomain() {
        return subDomain;
    }

    public String getExclude() {
        return exclude;
    }

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, "res.config.domain", subDomain);
    }

    public IDomainDispatcher getIDomainDispatcher() {
        IDomainDispatcher idd = (IDomainDispatcher)IDispatchers.get(className);
        if (idd!=null)
            return idd;
        else {
            try {
                idd = (IDomainDispatcher) Class.forName(className).newInstance();
                IDispatchers.put(className, idd);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e.getMessage());
            }
            return idd;
        }
    }

    public String[] getExcludeSubDomains() {
        return excludeSubDomains;
    }

    public boolean isRegexMatch() {
        return regexMatch;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setExcludeSubDomains(String[] excludeSubDomains) {
        this.excludeSubDomains = excludeSubDomains;
    }

    public void setRegexMatch(boolean regexMatch) {
        this.regexMatch = regexMatch;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    private String className;
    private String name;
    private boolean used = true;
    private String url;
    private String subDomain;
    private String exclude;
    private String[] excludeSubDomains;
    private boolean regexMatch;
    private boolean redirect = true;

}
