package com.cloudweb.oa.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

@Component
public class FileViewer {
    File myDir;
    File[] fileAry;
    public Vector files;
    Iterator currentFileView;
    File currentFile;
    String path;

    public FileViewer() {
        path = new String("");
        files = new Vector();
    }

    public FileViewer(String path) {
        this.path = path;
        files = new Vector();
    }

    /**
     * 设置浏览的路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /***
     * 返回当前目录路径
     */
    public String getDirectory() {
        return myDir.getPath();
    }

    /**
     * 刷新列表
     */
    public void init() {
        myDir = new File(path);
        files.clear();
        fileAry = myDir.listFiles();
        if (fileAry!=null) {
            // 重新装入路径下文件
            for (int i = 0; i < fileAry.length; i++) {
                if (fileAry[i].getName().lastIndexOf(".gif") != -1 ||
                        fileAry[i].getName().lastIndexOf(".jpg") != -1 ||
                        fileAry[i].getName().lastIndexOf(".bmp") != -1 ||
                        fileAry[i].getName().lastIndexOf(".png") != -1) {
                    files.add(fileAry[i]);
                }
            }
            currentFileView = files.iterator();
        }
        else {
            currentFileView = new Vector().iterator();
        }
    }

    /**
     * 移动当前文件集合的指针指到下一个条目
     * @return 成功返回true,否则false
     */
    public boolean nextFile() {
        while (currentFileView.hasNext()) {
            currentFile = (File) currentFileView.next();
            return true;
        }
        return false;
    }

    /**
     * 返回当前指向的文件对象的文件名称
     */
    public String getFileName() {
        return currentFile.getName();
    }

    /**
     * 返回当前指向的文件对象的文件尺寸
     */
    public String getFileSize() {
        return new Long(currentFile.length()).toString();
    }

    /**
     * 返回当前指向的文件对象的最后修改日期
     */
    public String getFileTimeStamp() {
        return new Date(currentFile.lastModified()).toString();
    }

    /**
     * 返回当前指向的文件对象是否是一个文件目录
     */
    public boolean getFileType() {
        return currentFile.isDirectory();
    }

    /**
     * 返回当前指向的文件对象
     */
    public boolean delete() {
        return currentFile.delete();
    }

    /**
     * 更名文件
     */
    public boolean renameTo(File dest) {
        return currentFile.renameTo(dest);
    }

    public File getCurrentFile() {
        return currentFile;
    }
}
