package com.redmoon.forum.ui;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
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
public class FaceMgr {
    FileUpload fileUpload = null;
    public Logger logger;
    public FaceMgr() {
    }

    public String uploadImg(ServletContext application,
                            HttpServletRequest request) throws
            ErrMsgException {
        String imgFile = "";
        String upFile = "";
        doUpload(application, request);
        upFile = writeFace(fileUpload);
        if (!upFile.equals("")) {
            imgFile = Global.getRealPath() + upFile;
        } else {
            throw new ErrMsgException("file can't be null！");
        }
        return imgFile;
    }

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        //fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"gif", "jpg", "bmp", "png"};
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

    public String writeFace(FileUpload fu) {
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            String vpath = "";
            if (fi != null) {
                vpath = "forum/images/face/";
                String filepath = Global.getRealPath() + vpath;
                fu.setSavePath(filepath);
                fi.write(filepath, "face" + getNextFace() + "." + fi.getExt());
                return vpath + fi.getDiskName();
            }
        }
        return "";
    }

    public int getNextFace() {
        int max = 0;
        String filepath = Global.getRealPath() + "forum/images/face/";
        String currentName = "";
        FileViewer fv = new FileViewer(filepath);
        fv.init();
        while (fv.nextFile()) {
            currentName = fv.getFileName();
            int p = 0;
            if (currentName.indexOf("face") != -1) {
                int m = currentName.indexOf(".");
                if (!currentName.substring(4, m).equals("")) {
                     p = Integer.parseInt(currentName.substring(4, m));
                }
                if (p > max) {
                    max = p;
                }
            }
        }
        return max+1;
    }
}
