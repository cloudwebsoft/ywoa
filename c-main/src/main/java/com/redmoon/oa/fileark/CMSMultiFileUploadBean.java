package com.redmoon.oa.fileark;

import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.util.ErrMsgException;

import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;

public class CMSMultiFileUploadBean extends FileUpload {
    Vector attachments = new Vector();
    boolean isAttachmentsSeparated = false;

    Vector titleImages = new Vector();
    boolean isTitleImageSeparated = false;

    public CMSMultiFileUploadBean() {
    }

    /**
     * 从files中分离出附件
     */
    public void separateAttachments() {
        Iterator ir = files.iterator();

        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // if (fi.getFieldName().indexOf("attachment") != -1) {
            if (fi.getFieldName().indexOf("att") == 0) {
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
        if (!isAttachmentsSeparated) {
            separateAttachments();
        }
        return attachments;
    }

    public Vector getTitleImages() {
        if (!isTitleImageSeparated) {
            separateTitleImages();
        }
        return titleImages;
    }

    /**
     * 从files中分离出标题附件
     */
    public void separateTitleImages() {
        Vector vFileImage = new Vector();
        Iterator ir = files.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            if (fi.getFieldName().equals("titleImage")) {
                vFileImage.addElement(fi);
            }
            else if (fi.getFieldName().indexOf("titleImage") != -1) {
                //不能在此删除，因为删的时候ir会受影响
                //files.remove(fi);
                titleImages.addElement(fi);
            }
        }

        ir = titleImages.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            files.remove(fi);
        }

        // 删除掉附件中的fileImage，因为在上传时，在FormData中已经append了dropFiles，而dropFiles中含有了fileImage
        ir = vFileImage.iterator();
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            files.remove(fi);
        }

        isTitleImageSeparated = true;
    }

    @Override
    public Vector getFiles() {
        if (!isAttachmentsSeparated) {
            separateAttachments();
        }
        if (!isTitleImageSeparated) {
            separateTitleImages();
        }
        return files;
    }

    /**
     * 如果未分离附件，则先分离附件，然后写其它HTMLCODE中的文件
     * @param isRandName boolean
     */
    @Override
    public void writeFile(boolean isRandName) {
        Vector files = getFiles();
        int size = files.size();
        if (size == 0) {
            return;
        }
        java.util.Enumeration e = files.elements();
        while (e.hasMoreElements()) {
            FileInfo fi = (FileInfo) e.nextElement();
            if (!isRandName) {
                fi.write(savePath, "");
            } else {
                // 防止文件名重复
                fi.write(savePath, getRandName());
            }
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
                fi.write(savePath, newfilename + "." + fi.getExt());
            }
        }
    }

    public void writeTitleImages(boolean isRandName) throws ErrMsgException {
        Vector files = getTitleImages();
        int size = files.size();
        if (size == 0) {
            return;
        }
        java.util.Enumeration e = files.elements();
        while (e.hasMoreElements()) {
            FileInfo fi = (FileInfo) e.nextElement();
            if (!isRandName)
                fi.write(savePath, "");
            else { //防止文件名重复
                String newfilename = getRandName();
                fi.write(savePath, newfilename + "." + fi.getExt());
            }
        }
    }
}
