package com.redmoon.forum.ui;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import java.util.*;
import org.jdom.*;
import com.redmoon.kit.util.FileInfo;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

/**
 * <p>Title: </p>
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
public class ThemeUploadMgr {
    FileUpload fileUpload = null;
    public Logger logger;
    public ThemeUploadMgr() {
    }

    public void modify(ServletContext application,
                       HttpServletRequest request) throws ErrMsgException {
        String filename = "", code = "", name = "", height = "", path = "",
                banner = "";
        doUpload(application, request);
        Vector v = fileUpload.getFiles();
        FileInfo fi = null;

        if (v.size() > 0) {
            // 删除原来的图片
            fi = (FileInfo) v.get(0);
            path = "upfile/forum/theme";
            String filepath = Global.getRealPath() + path;
            fileUpload.setSavePath(filepath);
            fileUpload.writeFile(true);
            filename = fi.getDiskName();
        }
        code = fileUpload.getFieldValue("code");
        name = fileUpload.getFieldValue("name");
        height = fileUpload.getFieldValue("height");
        if (!StrUtil.isNumeric(height))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.plugin.ThemeConfig", "height_is_numbic"));
        ThemeConfig tc = new ThemeConfig();
        if (!filename.equals("")) {
            banner = filename;
            tc.set(code, "banner", banner);
            tc.set(code, "path", "/" + path);
        }
        tc.set(code, "height", height);
        tc.set(code, "name", name);
        tc.writemodify();

        ThemeMgr tm = new ThemeMgr();
        tm.reload();
    }

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        String[] extnames = {"gif", "jpg", "png"};
        fileUpload.setValidExtname(extnames);
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public void create(ServletContext application,
                       HttpServletRequest request) throws ErrMsgException {
        String filename = "", code = "", name = "", height = "", path = "",
                banner = "";
        doUpload(application, request);
        Vector v = fileUpload.getFiles();
        FileInfo fi = null;
        if (v.size() > 0) {
            // 删除原来的图片
            fi = (FileInfo) v.get(0);
            path = "upfile/forum/theme";
            String filepath = Global.getRealPath() + path;
            fileUpload.setSavePath(filepath);
            fileUpload.writeFile(true);
            filename = fi.getDiskName();
        }
        code = fileUpload.getFieldValue("code");
        name = fileUpload.getFieldValue("name");
        height = fileUpload.getFieldValue("height");

        ThemeConfig tc = new ThemeConfig();
        banner = filename;

        Element root = tc.getRootElement();
        List list = root.getChildren();
        Element theme = new Element("theme");
        theme.setAttribute(new Attribute("code", code));
        Element elementName = new Element("name");
        elementName.setText(name);
        theme.addContent(elementName);

        Element elementPath = new Element("path");
        elementPath.setText("/" + path);
        theme.addContent(elementPath);

        Element elementBanner = new Element("banner");
        elementBanner.setText(banner);
        theme.addContent(elementBanner);

        Element elementHeight = new Element("height");
        elementHeight.setText(height);
        theme.addContent(elementHeight);
        list.add(theme);
        tc.writemodify();

        ThemeMgr tm = new ThemeMgr();
        tm.reload();
    }

    public void del(HttpServletRequest request) throws ErrMsgException {
        ThemeConfig tc = new ThemeConfig();
        Element root = tc.getRootElement();
        List list = root.getChildren();
        String code = ParamUtil.get(request, "code");
        if (list != null) {
            Iterator ir = list.listIterator();
            while (ir.hasNext()) {
                Element child = (Element) ir.next();
                String ecode = child.getAttributeValue("code");
                if (ecode.equals(code)) {
                    root.removeContent(child);
                    tc.writemodify();
                    break;
                }
            }
        }

        ThemeMgr tm = new ThemeMgr();
        tm.reload();
    }
}
