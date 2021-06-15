package com.redmoon.forum;

import cn.js.fan.util.ErrMsgException;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.FTPUtil;
import java.io.IOException;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: 用于实现高级发贴时附件的上传</p>
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
public class MultiFileUpload extends FileUpload {
    Vector attachments = new Vector();
    boolean isAttachmentsSeparated = false;
    private FileInfo upFileInfo = null; // frmAnnounce中filename对应的文件

    public static final int DISKSPACEUSED_TOO_LARGE = -1;
    public static final int WRITE_ATTACHMENT_SUCCEED = 1;

    public MultiFileUpload() {
    }

    public FileInfo getUpFileInfo() {
        if (!isAttachmentsSeparated) {
            separateAttachmentsAndUpfile();
        }
        return upFileInfo;
    }

    /**
     * 从files中分离出附件及附件中通过AddSpecialFile上传的文件filename
     */
    public void separateAttachmentsAndUpfile() {
        // 分离附件
        Iterator ir = files.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            if (fi.fieldName.indexOf("attachment") != -1) {
                // 不能在此删除，因为删的时候ir会受影响
                // files.remove(fi);
                attachments.addElement(fi);
            }
        }

        ir = attachments.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            files.remove(fi);
        }

        // 分离附件中AddSpecialFile上传的文件
        String attachFileName = StrUtil.getNullString(getFieldValue("filename")).
                                trim();
        boolean isDdxc = false;
        String sisDdxc = StrUtil.getNullString(getFieldValue("isDdxc"));
        // 断点续传
        if (sisDdxc.equals("true"))
            isDdxc = true;
        if (!attachFileName.equals("")) {
            if (!isDdxc) {
                ir = attachments.iterator();
                while (ir.hasNext()) {
                    FileInfo fi = (FileInfo) ir.next();
                    if (fi.fieldName.equals(attachFileName)) {
                        // 单个文件可在此删除，多文件的时候不能这么删，因为删的时候ir会受影响
                        attachments.remove(fi);
                        upFileInfo = fi;
                        break;
                    }
                }
            } else { // 如果是断点续传
                // System.out.println("attachFileName=" + attachFileName + " upfi=" + upFileInfo);
                if (attachFileName.equals(""))
                    return;
                String strIndex = attachFileName.substring("attachment".length(),
                        attachFileName.length());
                int index = Integer.parseInt(strIndex);
                String[] attachFileNames = getFieldValues("attachFileName");
                String[] clientFilePaths = getFieldValues("clientFilePath");
                String name = getUploadFileName(clientFilePaths[index]);
                upFileInfo = new FileInfo();
                upFileInfo.diskName = attachFileNames[index];
                upFileInfo.name = name;
                upFileInfo.fieldName = attachFileName;
                upFileInfo.ext = getFileExt(attachFileNames[index]);
            }
        }

        isAttachmentsSeparated = true;
    }

    public Vector getAttachments() {
        if (!isAttachmentsSeparated)
            separateAttachmentsAndUpfile();
        return attachments;
    }

    @Override
    public Vector getFiles() {
        if (!isAttachmentsSeparated)
            separateAttachmentsAndUpfile();
        return files;
    }

    /**
     * 如果未离附件，则先分离附件，然后写其它HTMLCODE中的文件
     * @param isRandName boolean
     */
    @Override
    public void writeFile(boolean isRandName) {
        Vector files = getFiles();
        int size = files.size();
        if (size == 0)
            return;
        java.util.Enumeration e = files.elements();
        while (e.hasMoreElements()) {
            FileInfo fi = (FileInfo) e.nextElement();
            if (!isRandName)
                fi.write(savePath, "");
            else // 防止文件名重复
                fi.write(savePath, getRandName() + "." + fi.getExt());
        }
    }

    public int writeAttachment(boolean isRandName) throws ErrMsgException {
        Vector files = getAttachments();
        int size = files.size();
        if (size == 0)
            return WRITE_ATTACHMENT_SUCCEED;

        // 检查磁盘空间是否允许写Attachment
        Iterator ir = files.iterator();
        long fileSize = 0;
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo)ir.next();
            fileSize += fi.size;
        }

        Privilege privilege = new Privilege();
        if (!privilege.canUploadAttachment(request, fileSize))
            return DISKSPACEUSED_TOO_LARGE;

        Config cfg = Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            boolean retFtp = ftp.connect(cfg.getProperty(
                    "forum.ftpServer"),
                                         cfg.getIntProperty("forum.ftpPort"),
                                         cfg.getProperty("forum.ftpUser"),
                                         cfg.getProperty("forum.ftpPwd"), true);
            if (!retFtp) {
                ftp.close();
                throw new ErrMsgException(ftp.getReplyMessage());
            }
        }
        int allSize = 0;
        try {
            java.util.Enumeration e = files.elements();
            while (e.hasMoreElements()) {
                FileInfo fi = (FileInfo) e.nextElement();
                if (isFtpUsed) {
                    try {
                        String fname = "";
                        if (!isRandName)
                            fname = fi.getName();
                        else {
                            fname = FileUpload.getRandName() + "." +
                                    fi.getExt();
                        }
                        fi.diskName = fname;
                        ftp.storeFile(MsgDb.getCurAttVisualPath() + "/" +
                                      fname, fi.getTmpFilePath());
                    } catch (IOException e1) {
                        LogUtil.getLog(getClass()).error(
                                "AddNewWE: storeFile - " +
                                e1.getMessage());
                    }
                } else {
                    if (!isRandName)
                        fi.write(savePath, "");
                    else { // 防止文件名重复
                        String newfilename = getRandName();
                        fi.write(savePath,
                                 newfilename + "." + fi.getExt());
                    }
                }
                allSize += fi.getSize();
            }
        } finally {
            if (isFtpUsed) {
                ftp.close();
            }
        }
        // 更新用户的磁盘已用空间
        UserDb ud = new UserDb();
        ud = ud.getUser(privilege.getUser(request));
        ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + allSize);
        ud.save();

        return WRITE_ATTACHMENT_SUCCEED;
    }
}
