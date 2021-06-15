package com.redmoon.forum.ui;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import cn.js.fan.web.Global;
import javax.servlet.ServletContext;
import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.FileInfo;
import java.util.Vector;
import cn.js.fan.web.SkinUtil;

public class ThemeForm extends AbstractForm {
    FileUpload fileUpload = null;

    public ThemeForm() {
    }

    public String checkCode(HttpServletRequest request) throws
            ErrMsgException {
        String code = fileUpload.getFieldValue("code");
        if (code.equals("") || code == null) {
            log(SkinUtil.LoadString(request, "res.forum.plugin.ThemeConfig",
                                    "height_is_numbic"));
        }
        return code;
    }

    public String checkName(HttpServletRequest request) throws
            ErrMsgException {
        String name = fileUpload.getFieldValue("name");
        if (name.equals("") || name == null) {
            log(SkinUtil.LoadString(request, "res.forum.plugin.ThemeConfig",
                                    "name_is_be_null"));
        }
        return name;
    }

    public String checkHeight(HttpServletRequest request) throws
            ErrMsgException {
        String height = fileUpload.getFieldValue("height");
        if (height.equals("") || height == null) {
            log(SkinUtil.LoadString(request, "res.forum.plugin.ThemeConfig",
                                    "height_is_null"));
        } else if (!StrUtil.isNumeric(height)) {
            log(SkinUtil.LoadString(request, "res.forum.plugin.ThemeConfig",
                                    "height_is_numbic"));
        }
        return height;
    }

    public String checkPicSrc(HttpServletRequest request) throws
            ErrMsgException {
        String picSrc = fileUpload.getFieldValue("picSrc");
        return picSrc;
    }

    public String checkFiles(HttpServletRequest request) throws ErrMsgException {
        Vector v = fileUpload.getFiles();
        FileInfo fi = null;
        String filename = "";
        if (v.size() > 0) {
            fi = (FileInfo) v.get(0);
            String filepath = Global.getRealPath() + Theme.basePath + "/";
            fileUpload.setSavePath(filepath);
            fileUpload.writeFile(true);
            filename = fi.getDiskName();
        }
        return filename;
    }

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        String[] extnames = {"gif", "jpg", "png"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
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

    public boolean checkCreate(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        init();
        doUpload(application, request);
        checkCode(request);
        checkName(request);
        checkHeight(request);
        checkFiles(request);
        report();
        return true;
    }


}
