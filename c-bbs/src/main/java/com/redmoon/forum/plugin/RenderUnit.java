package com.redmoon.forum.plugin;

import java.io.Serializable;
import com.redmoon.forum.plugin.base.IPluginRender;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;

/**
 *
 * <p>Title: 显示方式单元，详见render.xml</p>
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
public class RenderUnit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public RenderUnit(String code) {
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

    public String getName(HttpServletRequest request) {
        return SkinUtil.LoadString(request, "res.config.render", code);
    }

    public IPluginRender getRender() {
        IPluginRender ipu = null;
        try {
            ipu = (IPluginRender) Class.forName(className).newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ipu;
    }

    private String code;
    private String author;
    private String className;
    private String name;

}
