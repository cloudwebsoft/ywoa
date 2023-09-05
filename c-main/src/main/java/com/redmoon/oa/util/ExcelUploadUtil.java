package com.redmoon.oa.util;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;

import javax.servlet.ServletContext;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;

import java.io.IOException;

import cn.js.fan.web.Global;
import java.util.*;

import com.redmoon.kit.util.FileInfo;

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
public class ExcelUploadUtil {
    FileUpload fileUpload = null;

    public ExcelUploadUtil() {
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public String uploadExcel(ServletContext application,
                              HttpServletRequest request) throws
            ErrMsgException {
        String excelFile = "";
        doUpload(application, request);

        if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fileUpload.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            excelFile = fi.getTmpFilePath();
        }

        return excelFile;
    }

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        // 每个文件的大小限制
        fileUpload.setMaxFileSize(Global.FileSize);
        String[] extnames = {"xls", "xlsx"};
        // 设置可上传的文件类型
        fileUpload.setValidExtname(extnames);
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }
}
