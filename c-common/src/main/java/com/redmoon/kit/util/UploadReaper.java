package com.redmoon.kit.util;

import java.io.*;
import java.util.*;

import cn.js.fan.web.Global;

/**
 *
 * <p>Title: 从临时文件目录中定时删除文件</p>
 *
 * <p>Description: 在Global中初始化，每隔6小时删除断点续传，每隔12小时，删除FileUploadTmp目录中未能被及时删除的临时文件</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UploadReaper extends Thread {

    private static long reapInterval = 1000*60*60*6; // 每隔6小时刷新一次
    private static UploadReaper uploadReaper = null;

    private final long expireTimeLen = 1000*60*60*6; // 超期时长为6小时
    private final long hourms = 1000 * 60 * 60; // 1小时的毫秒数

    private final int MAX_HOUR = 12; // 临时文件超过12小时，将会被删除

    public UploadReaper(long reapInterval) {
        UploadReaper.reapInterval = reapInterval;
        // Make the timer be a daemon thread so that it won't keep the VM from
        // shutting down if there are no other threads.
        this.setDaemon(true);

        start();
    }

    public static long getReapInterval() {
        return reapInterval;
    }

    /**
     * 单态模式
     * @param reapInterval long
     */
    public static synchronized void initInstance(long reapInterval) {
        if (uploadReaper==null) {
            uploadReaper = new UploadReaper(reapInterval);
        }
    }

    @Override
    public void run() {
        while (true) {
            // 删除FileUploadTmp下的临时文件，如果时间为12小时前则删除
            java.util.Date now = new java.util.Date();
            File[] tempFiles = (new File(Global.realPath + FileUpload.TEMP_PATH)).listFiles();
            if (tempFiles != null) {
                for (int i = 0; i < tempFiles.length; i++) {
                    if (tempFiles[i].isFile()) {
                        // 超过12小时
                        if ((now.getTime() - tempFiles[i].lastModified()) / hourms >= MAX_HOUR ) {
                            tempFiles[i].delete();
                        }
                    }
                }
            }

            // 遍历链表，如果有超期的文件则将其线程所对应的文件块删除（一个线程对应于一个文件块，一个文件块在上传时由多个Segment组成）
            // 并将其从链表中删除
            Map m = UploadDdxc.uploadFileInfos;
            Set s = m.keySet(); // Needn't be in synchronized block
            Vector expireUfis = new Vector();
            synchronized (m) { // Synchronizing on m, not s!
                Iterator i = s.iterator(); // Must be in synchronized block
                while (i.hasNext()) {
                    UploadFileInfo ufi = (UploadFileInfo) i.next();
                    if (System.currentTimeMillis() - ufi.getReceiveTime() >
                        expireTimeLen) {
                        // 记录将删除的key
                        expireUfis.addElement(ufi.getFileId());
                        // 删除线程及相关文件
                        Vector v = ufi.getUploadThreadInfos();
                        Iterator ir = v.iterator();
                        while (ir.hasNext()) {
                            UploadThreadInfo uti = (UploadThreadInfo) ir.next();
                            // 删除文件块（每个线程对应一个文件块）
                            String blockPath = FileUpload.getTmpPath() + ufi.getBlockName(uti.getBlockId());
                            File file = new File(blockPath);
                            if (!(file.exists())) {
                                file.delete();
                            }
                        }
                    }
                }
                // 从链表中删除超期的FileUploadInfo
                Iterator ir = expireUfis.iterator();
                while (ir.hasNext()) {
                    String fileId = (String)ir.next();
                    UploadDdxc.uploadFileInfos.remove(fileId);
                }
            }

            try {
                sleep(reapInterval);
            }
            catch (InterruptedException ie) { }
        }
    }

    public static synchronized UploadReaper getInstance() {
        if (uploadReaper==null) {
            initInstance(reapInterval);
        }
        return uploadReaper;
    }

}
