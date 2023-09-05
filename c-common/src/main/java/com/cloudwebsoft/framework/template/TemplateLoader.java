package com.cloudwebsoft.framework.template;

import java.io.File;
import java.io.IOException;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import java.io.StringReader;

/**
 * <p>Title: 用以载入模板</p>
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
public class TemplateLoader {
    HttpServletRequest request;

    String fileName = null;

    static final String cacheGroup = "cws.framework.TemplateLoader";

    /**
     * the parsed result template
     */
    ITemplate template = null;

    /**
     * the template file path
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * load the template. record the file timestamp.
     *
     * @param fileName String
     * @throws IOException
     */
    public TemplateLoader(HttpServletRequest request, String fileName) throws IOException,ErrMsgException {
        this.request = request;
        this.fileName = fileName;

        load();
    }

    /**
     * 当系统模板有更新时，清除缓存组，让所有模板重新载入
     */
    public static void refreshTemplate(String templateCacheKey) {
        try {
            RMCache.getInstance().remove(templateCacheKey, cacheGroup);
        } catch (Exception e) {
            LogUtil.getLog("com.cloudwebsoft.framework.template.TemplateLoader").error("refreshTemplate:" + e.getMessage());
        }
    }

    /**
     * 从字符串载入模板
     * @param request HttpServletRequest
     * @param cacheKey String 缓存键值
     * @param templateStr String
     * @throws IOException
     * @throws ErrMsgException
     */
    public TemplateLoader(HttpServletRequest request, String cacheKey, String templateStr) throws IOException, ErrMsgException {
        this.request = request;
        loadFromString(cacheKey, templateStr);
    }

    public void loadFromString(String cacheKey, String templateStr) throws IOException, ErrMsgException {
        try {
            template = (ITemplate) RMCache.getInstance().getFromGroup(
                    cacheKey,
                    cacheGroup);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("loadFromString1:" + e.getMessage());
        }
        if (template == null) {
            Parser parser = new Parser();
            StringReader sr = new StringReader(templateStr);
            template = parser.parse(sr);
            if (template != null) {
                try {
                    RMCache.getInstance().putInGroup(cacheKey, cacheGroup,
                                                     template);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("loadFromString2:" +
                                                     e.getMessage());
                }
            }
        }
    }

    /**
     * each time a template is required,
     * the template loader check the file timestamp,
     * if the file timestamp is changed, the template loader reload the template.
     * if not return the current template
     *
     * @throws IOException
     * @return ITemplate
     */
    public ITemplate getTemplate() {
        return template;
    }

    /**
     *
     * @return
     */
    public boolean isFileModified() {
        long theFiletime = new File(fileName).lastModified();
        Long lmt = null;
        long lastModifiedTime = 0;
        try {
            lmt = (Long) RMCache.getInstance().getFromGroup(
                    "lastModifiedTime_" + fileName,
                    cacheGroup);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("isFileModified:" + e.getMessage());
        }
        if (lmt!=null)
            lastModifiedTime = lmt.longValue();

        boolean re = lastModifiedTime != theFiletime;
        return re;
    }

    public void loadFromFile() throws IOException,ErrMsgException {
        Parser parser = new Parser();
        // LogUtil.getLog(getClass()).info(getClass().getName() + " fileName=" + fileName);
        template = parser.parse(fileName);
        long lastModifiedTime = new File(fileName).lastModified();

        try {
            RMCache.getInstance().putInGroup("lastModifiedTime_" + fileName, cacheGroup,
                    new Long(lastModifiedTime));
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("loadFromFile:" + e.getMessage());
        }
        // LogUtil.getLog(getClass()).info("template=" + template + " lastModifiedTime=" + lastModifiedTime);
    }

    /**
     *
     * @throws IOException
     */
    public void load() throws IOException,ErrMsgException {
        // LogUtil.getLog(getClass()).info(getClass().getName() + " isFileModified=" + isFileModified());

        if (isFileModified()) {
            loadFromFile();
            if (template!=null) {
                try {
                    RMCache.getInstance().putInGroup(fileName, cacheGroup,
                            template);
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("load:" + e.getMessage());
                }
            }
        } else {
            try {
                template = (ITemplate) RMCache.getInstance().getFromGroup(
                        fileName,
                        cacheGroup);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("load:" + e.getMessage());
            }
            if (template == null) {
                loadFromFile();
                if (template != null) {
                    try {
                        RMCache.getInstance().putInGroup(fileName, cacheGroup,
                                template);
                    } catch (Exception e) {
                        LogUtil.getLog(getClass()).error("load:" + e.getMessage());
                    }
                }
            }
        }
    }

    public String toString() {
        // LogUtil.getLog(getClass()).info("toString: fileName=" + fileName);
        // LogUtil.getLog(getClass()).info("dirCode=" + request.getAttribute("dirCode"));

        return template.toString(request, null);
    }

}
