package com.redmoon.oa.flow;

import cn.js.fan.util.ErrMsgException;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;

public class CMSMultiFileUploadBean extends FileUpload {
    Vector attachments = new Vector();
    boolean isAttachmentsSeparated = false;

    public CMSMultiFileUploadBean() {
    }

    /**
     * 从files中分离出附件
     */
    public void separateAttachments() {
        Iterator ir = files.iterator();

        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            if (fi.fieldName.indexOf("attachment") != -1) {
                //不能在此删除，因为删的时候ir会受影响
                //files.remove(fi);
                attachments.addElement(fi);
            }
        }

        ir = attachments.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            files.remove(fi);
        }

        isAttachmentsSeparated = true;
    }

    public Vector getAttachments() {
        if (!isAttachmentsSeparated)
            separateAttachments();
        return attachments;
    }

    @Override
    public Vector getFiles() {
        if (!isAttachmentsSeparated)
            separateAttachments();
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
                fi.write(savePath, getRandName());
        }
    }

    public void writeAttachment(boolean isRandName) throws ErrMsgException {
        Vector files = getAttachments();
        int size = files.size();
        if (size == 0)
            return;
        java.util.Enumeration e = files.elements();
        while (e.hasMoreElements()) {
            FileInfo fi = (FileInfo) e.nextElement();
            if (!isRandName)
                fi.write(savePath, "");
            else { //防止文件名重复
                String newfilename = getRandName();
                fi.write(savePath,
                             newfilename + "." + fi.getExt());
            }
        }
    }
}
