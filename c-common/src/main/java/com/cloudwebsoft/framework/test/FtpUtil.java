package com.cloudwebsoft.framework.test;

import java.io.*;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.net.ftp.*;
import cn.js.fan.util.StrUtil;

public class FtpUtil {
    FTPClient ftp = new FTPClient();

    public FtpUtil() throws Exception {
    }

    public boolean connect(String host, int port, String userName, String password, boolean isPassiveMode) throws
            IOException {
        ftp.connect(host, port);
        boolean re = ftp.login(userName,
                               password);
        if (!re) {
            close();
        }
        else {
            if (isPassiveMode)
                ftp.enterLocalPassiveMode();
            else
                ftp.enterLocalActiveMode();
        }
        return re;
        // ftp.changeWorkingDirectory(workDirectory);
    }

    /**
     * 上传文件
     * @param ftpPath String 过程路径，如果含有子目录，则自动判别，没有则自动创建
     * @param filePath String
     * @return boolean
     * @throws IOException
     */
    public boolean storeFile(String ftpPath, String filePath) throws IOException {
        ftp.setFileType(ftp.BINARY_FILE_TYPE);
        // ftp.setFileType(ftp.ASCII_FILE_TYPE);

        String fileName;
        if (ftpPath.equals(""))
            return false;
        else {
            if (ftpPath.startsWith("/"))
                ftpPath = ftpPath.substring(1);
            String[] paths = ftpPath.split("/");
            int len = paths.length;
            fileName = paths[len-1];
            for (int i=0; i<len-1; i++) {
                ftp.changeWorkingDirectory(paths[i]);
                if (ftp.getReplyCode()!=250) {
                    ftp.makeDirectory(paths[i]);
                    ftp.changeWorkingDirectory(paths[i]);
                }
            }
        }
        FileInputStream is = new FileInputStream(filePath);
        boolean re = ftp.storeFile(fileName, is); // new BufferedInputStream(is));
        is.close();

        return ftp.getReplyCode()==226;
    }

    public void close() throws IOException {
        if (ftp != null) {
            ftp.disconnect();
            ftp = null;
        }
    }

    /**
     * 删除文件
     * @param path String 如：/111/222/2.jpg
     * @throws IOException
     */
    public void del(String path) throws IOException {
        ftp.dele(path);
    }

    public int getReplyCode() {
        return ftp.getReplyCode();
    }

    public String getReplyMessage() {
        return ftp.getReplyString();
    }

    public void test1() throws Exception {
        if (!connect("127.0.0.1", 21, "cws", "1", true)) {
            return;
        }
        //如果是中文名必需进行字符集转换
        //boolean bMakeFlag = ftp.makeDirectory(new String("测试目录".getBytes(
        //  "gb2312"), "iso-8859-1")); //在服务器创建目录

        del("/111/222/2.jpg");

/*
        FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles != null) {
            for (int i = 0; i < ftpFiles.length; i++) {
                if (ftpFiles[i].isFile()) {
                    FTPFile ftpf = new FTPFile();
                    LogUtil.getLog(getClass()).info("EXECUTE_PERMISSION=" +
                                       ftpf.hasPermission(FTPFile.GROUP_ACCESS,
                            FTPFile.EXECUTE_PERMISSION));
                    LogUtil.getLog(getClass()).info("READ_PERMISSION=" +
                                       ftpf.hasPermission(FTPFile.USER_ACCESS,
                            FTPFile.READ_PERMISSION));
                    LogUtil.getLog(getClass()).info("EXECUTE_PERMISSION=" +
                                       ftpf.hasPermission(FTPFile.USER_ACCESS,
                            FTPFile.EXECUTE_PERMISSION));
                    LogUtil.getLog(getClass()).info("WRITE_PERMISSION=" +
                                       ftpf.hasPermission(FTPFile.USER_ACCESS,
                            FTPFile.WRITE_PERMISSION));
                    LogUtil.getLog(getClass()).info("READ_PERMISSION=" +
                                       ftpf.hasPermission(FTPFile.WORLD_ACCESS,
                            FTPFile.READ_PERMISSION));
                }
                //LogUtil.getLog(getClass()).info(ftpFiles[i].getUser());
            }
        }
 */
        //下载服务器文件
        //FileOutputStream fos = new FileOutputStream("e:/23456.html");
        //ftp.retrieveFile("1.html", fos);
        //fos.close();
        //改变ftp目录
        //ftp.changeToParentDirectory();//回到父目录
        //ftp.changeWorkingDirectory("");//转移工作目录
        //ftp.completePendingCommand();//
        //删除ftp服务器文件
        //ftp.deleteFile("");
        //注销当前用户，
        //ftp.logout();
        //ftp.structureMount("");
        close();
    }

    /**
     * 封装好的东西是好用，碰到这种问题，我们就。。。up
     * @param args String[]
     */
    public static void main(String[] args) {
        try {
            FtpUtil ftpApache1 = new FtpUtil();
            ftpApache1.test1();
        } catch (Exception e) {
            LogUtil.getLog(FtpUtil.class).error(e);
        }
    }
}
