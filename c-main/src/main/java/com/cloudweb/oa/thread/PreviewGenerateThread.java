package com.cloudweb.oa.thread;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.FileUtil;
import com.cloudweb.oa.utils.PreviewUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudwebsoft.framework.db.Connection;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowLinkDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.util.Pdf2Html;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PreviewGenerateThread extends Thread {
    String visualPath;
    String diskName;

    public PreviewGenerateThread(String visualPath, String diskName) {
        this.visualPath = visualPath;
        this.diskName = diskName;
    }

    @Override
    public void run() {
        try {
            // 停3秒，以使得obs防止刚上传就获取文件，有时会取不到
            sleep(3000);
        } catch (InterruptedException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        if (FileUtil.isOfficeFile(diskName)) {
            try {
                String previewfile = Global.getRealPath() + visualPath + "/" + diskName;
                SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
                IFileService fileService = SpringUtil.getBean(IFileService.class);
                if (sysProperties.isObjStoreEnabled()) {
                    fileService.copyToLocalFile(diskName, visualPath, diskName, Global.getRealPath() + visualPath);
                }
                PreviewUtil.createOfficeFilePreviewHTML(previewfile);
                if (sysProperties.isObjStoreEnabled()) {
                    File f = new File(Global.getRealPath() + visualPath + "/" + diskName);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        } else {
            if ("pdf".equals(StrUtil.getFileExt(diskName))) {
                String previewfile = Global.getRealPath() + visualPath + "/" + diskName;
                try {
                    SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
                    IFileService fileService = SpringUtil.getBean(IFileService.class);
                    if (sysProperties.isObjStoreEnabled()) {
                        fileService.copyToLocalFile(diskName, visualPath, diskName, Global.getRealPath() + visualPath);
                    }
                    Pdf2Html.createPreviewHTML(previewfile);
                    if (sysProperties.isObjStoreEnabled()) {
                        File f = new File(Global.getRealPath() + visualPath + "/" + diskName);
                        if (f.exists()) {
                            f.delete();
                        }
                    }
                } catch (IOException e) {
                    LogUtil.getLog(getClass()).error(e);
                }
            }
        }
    }
}