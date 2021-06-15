package com.redmoon.forum.util;

import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.FTPUtil;
import com.redmoon.forum.Config;

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
public class ForumFileUtil {
    public ForumFileUtil() {
    }

    public void delFtpFile(String ftpFilePath) {
        Config cfg = Config.getInstance();
        FTPUtil ftp = new FTPUtil();
        try {
            boolean retFtp = ftp.connect(cfg.getProperty(
                    "forum.ftpServer"),
                                         cfg.getIntProperty("forum.ftpPort"),
                                         cfg.getProperty("forum.ftpUser"),
                                         cfg.getProperty("forum.ftpPwd"), true);
            if (!retFtp) {
                LogUtil.getLog(getClass()).error("delFtpFile:" +
                                                 ftp.getReplyMessage());
            }
            else
                ftp.del(ftpFilePath);
        } finally {
            ftp.close();
        }
    }
}
