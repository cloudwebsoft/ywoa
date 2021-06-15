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
import cn.js.fan.util.StrUtil;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class EmotMgr {
    public FileUpload fileUpload = null;
    public Logger logger;

    public static final String EMOTPATH = "editor_full/images/emot_files/";

    public static final String cacheGroup = "forum.ui.emot";

    public EmotMgr() {
    }

    public static String[] getEmots(String emotGroup) {
        String[] files = null;
        try {
            files = (String[])RMCache.getInstance().getFromGroup(emotGroup, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(EmotMgr.class).error(e.getMessage());
        }
        if (files==null) {
            FileViewer fileViewer = new FileViewer(Global.realPath +
                    EMOTPATH + "emot" + emotGroup + "/");
            fileViewer.init();
            files = new String[fileViewer.files.size()];
            int i = 0;
            while (fileViewer.nextFile()) {
                files[i] = EMOTPATH + "emot" + emotGroup + "/" + fileViewer.getFileName();
                i++;
            }
            try {
                RMCache.getInstance().putInGroup(emotGroup, cacheGroup, files);
            }
            catch (Exception e) {
                LogUtil.getLog(EmotMgr.class).error(e.getMessage());
            }
        }
        return files;
    }

    public void uploadImg(ServletContext application,
                            HttpServletRequest request) throws
            ErrMsgException {
        doUpload(application, request);
        String upFile = writeEmot(fileUpload);
        if (upFile.equals("")) {
            throw new ErrMsgException("File can't be null！");
        }
    }

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        // fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"gif", "jpg", "bmp", "png"};
        fileUpload.setValidExtname(extnames); // 设置可上传的文件类型
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

    public String writeEmot(FileUpload fu) {
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            if (fi != null) {
                String group = fu.getFieldValue("group");
                String filepath = Global.getRealPath() + EMOTPATH + "emot" + group + "/";
                fu.setSavePath(filepath);
                fi.write(filepath, getNextEmoteName(group) + "." + fi.getExt());
                try {
                    RMCache.getInstance().remove(group, cacheGroup);
                }
                catch (Exception e) {
                    logger.error("writeEmot:" + e.getMessage());
                }
                return EMOTPATH + "emot" + group + "/" + fi.getDiskName();
            }
        }
        return "";
    }

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(cacheGroup);
        } catch (Exception e) {
            logger.error("refresh:" + e.getMessage());
        }
    }

    public int getNextEmoteName(String group) {
        int max = 0;
        String filepath = Global.getRealPath() + EMOTPATH + "emot" + group + "/";
        String currentName = "";
        FileViewer fv = new FileViewer(filepath);
        fv.init();
        while (fv.nextFile()) {
            currentName = fv.getFileName();
            int p = 0;
            int m = currentName.indexOf(".");
            String f = currentName.substring(0, m);
            if (StrUtil.isNumeric(f)) {
                p = Integer.parseInt(f);
                if (p > max) {
                    max = p;
                }
            }
        }
        return max+1;
    }
}
