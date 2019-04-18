package com.redmoon.forum.util;

import java.io.*;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;
import com.redmoon.kit.util.*;

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
public class ForumFileUpload extends FileUploadExt {
    /**
     * 远程文件的上传路径，如:对应于PhotoDb，为photo/年/月
     */
    String remoteBasePath;

    public void setRemoteBasePath(String remoteBasePath) {
        this.remoteBasePath = remoteBasePath;
    }

    public ForumFileUpload() {
    }

    public int doUpload(ServletContext application, HttpServletRequest request, String charset) throws IOException {	
    	return super.doUpload(application, request, charset);
	}
    
    /**
	 * 写上传的文件，如果是远程，则上传至FTP，适合于单个文件的上传，多个文件在FTP时会多次连接，效率低
	 * 
	 * @param isRandName
	 *            boolean
	 */
    public void writeFile(boolean isRandName) {
        Vector files = getFiles();
        int size = files.size();
        if (size == 0)
            return;

        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            try {
                boolean retFtp = ftp.connect(cfg.getProperty(
                        "forum.ftpServer"),
                                             cfg.getIntProperty("forum.ftpPort"),
                                             cfg.getProperty("forum.ftpUser"),
                                             cfg.getProperty("forum.ftpPwd"), true);
                if (!retFtp) {
                    LogUtil.getLog(getClass()).error("writeFile:" +
                            ftp.getReplyMessage());
                } else {
                    if (remoteBasePath.lastIndexOf("/") !=
                        remoteBasePath.length() - 1)
                        remoteBasePath += "/";

                    java.util.Iterator ir = files.iterator();
                    while (ir.hasNext()) {
                        FileInfo fi = (FileInfo) ir.next();
                        try {
                            String fname = "";
                            if (!isRandName)
                                fname = fi.getName();
                            else {
                                fname = FileUpload.getRandName() + "." +
                                        fi.getExt();
                            }
                            fi.diskName = fname;
                            ftp.storeFile(remoteBasePath +
                                          fname, fi.getTmpFilePath());
                        } catch (IOException e1) {
                            LogUtil.getLog(getClass()).error(
                                    "AddNewWE: storeFile - " +
                                    e1.getMessage());
                        }
                    }
                }
            } finally {
                ftp.close();
            }
        } else {
            java.util.Iterator ir = files.iterator();
            while (ir.hasNext()) {
                FileInfo fi = (FileInfo) ir.next();
                if (!isRandName)
                    fi.write(savePath, "");
                else // 防止文件名重复
                    fi.write(savePath, getRandName() + "." + fi.getExt());
            }
        }
    }
}
