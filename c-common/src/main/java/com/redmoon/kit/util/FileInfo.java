package com.redmoon.kit.util;

/**
 * <p>Title: 文件信息</p>
 * <p>Description: 用于FileUpload保存文件信息</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 *
 * @author fgf
 * @version 1.0
 */

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

import java.io.*;

public class FileInfo {
    public String name, contentType, fieldName;
    public String uploadSerialNo;

    /**
     * 客户端路径，含文件名，注意IE会发送文件的全部路径，而ff及mozila则只发送文件名
     */
    public String clientPath;
    public long size = 0; //文件大小
    public String ext = ""; //上传文件的扩展名
    public String diskName = "";//写入磁盘的文件名（有可能是随机产生的）

    public FileInfo() {
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 取得文件的原始名称
     * @return String 文件原始名称
     */
    public String getName() {
        return name;
    }

    /**
     * 取得文件扩展名
     * @return String
     */
    public String getExt() {
        return ext;
    }

    /**
     * 取得文件上传时表单中域的名称
     * @return String
     */
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * 以文件在客户端的名称写入磁盘
     * @param savepath String
     * @return boolean
     */
    public boolean write(String savepath) {
        return write(savepath, false);
    }

    /**
     *
     * @param savepath String
     * @param isRand boolean 用随机名称或者本来的名称
     * @return boolean
     */
    public boolean write(String savepath, boolean isRand) {
        String rname = "";
        if (isRand) {
            rname = FileUpload.getRandName() + "." + ext;
        }
        return write(savepath, rname);
    }

    /**
     * 把文件写入指定的路径，用于编辑文件时
     * @param fullPath String
     * @return boolean
     */
    public boolean writeToPath(String fullPath) {
        boolean re = false;
        re = copyFile(tmpFilePath, fullPath);
        return re;
    }

    /**
     * 把文件写入磁盘
     * @param savepath String 保存路径
     * @param newname String 新文件名
     * @return boolean true-写入成功 false-写入时出错
     */
    public boolean write(String savepath, String newname) {
        // if (data.length() == 0)
        //   return false;
        boolean re = false;

        if (savepath == null) {
            savepath = "";
        }

        // 如果目录不存在，则创建目录
        File f = new File(savepath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        if (StrUtil.isEmpty(newname)) {
            newname = name;
        }

        diskName = newname;

        re = copyFile(tmpFilePath, savepath + newname);

        return re;
    }

    public static boolean copyFile(String filePathSrc, String filePathDes) {
        boolean re = false;
        File fSrc = new File(filePathSrc);
        if (!fSrc.exists()) {
            return false;
        }
        try {
            if (fSrc.isFile()) {
                FileInputStream input = new FileInputStream(fSrc);
                FileOutputStream output = new FileOutputStream(filePathDes);
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    output.write(b, 0, len);
                }
                output.flush();
                output.close();
                input.close();
                re = true;
            } else {
                LogUtil.getLog(FileInfo.class).error("debug:" + filePathSrc + "已不存在！");
            }
        } catch (IOException e) {
            LogUtil.getLog(FileInfo.class).error(e);
        }
        return re;
    }

    public void setDiskName(String diskName) {
        this.diskName = diskName;
    }

    /**
     * 取得文件写入磁盘的名称
     * @return String
     */
    public String getDiskName() {
        return diskName;
    }

    /**
     * 取得文件大小
     * @return int
     */
    public long getSize() {
        return size;
    }

    /**
     * 取得文件MIME类型
     * @return String
     */
    public String getContentType() {
        return contentType;
    }

    public String getTmpFilePath() {
        return tmpFilePath;
    }

    public void setTmpFilePath(String tmpFilePath) {
        this.tmpFilePath = tmpFilePath;
    }

    public String getUploadSerialNo() {
        return uploadSerialNo;
    }


    private String tmpFilePath;
}
