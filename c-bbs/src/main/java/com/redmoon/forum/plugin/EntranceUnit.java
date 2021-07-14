package com.redmoon.forum.plugin;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.web.*;
import com.redmoon.forum.plugin.base.*;
import org.apache.log4j.*;

/**
 *
 * <p>Title: 通行证单元</p>
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
public class EntranceUnit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    static Map IPluginEntrances = new HashMap();

    public EntranceUnit(String code) {
        this.code = code;
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
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

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setAdminEntrance(String adminEntrance) {
        this.adminEntrance = adminEntrance;
    }

    public void setUserCenterPage(String userCenterPage) {
        this.userCenterPage = userCenterPage;
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

    public String getDesc(HttpServletRequest request) {
        return SkinUtil.LoadString(request, "res.config.entrance", code);
    }

    public String getDesc() {
        return desc;
    }

    public String getAdminEntrance() {
        return adminEntrance;
    }

    public String getUserCenterPage() {
        return userCenterPage;
    }

    public IPluginEntrance getEntrance() {
        IPluginEntrance ipu = (IPluginEntrance)IPluginEntrances.get(code);
        if (ipu==null) {
            try {
                ipu = (IPluginEntrance) Class.forName(className).newInstance();
                IPluginEntrances.put(code, ipu);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return ipu;
    }

    private String code;
    private String author;
    private String className;
    private String name;
    private String desc;
    private String adminEntrance;
    private String userCenterPage;

}
