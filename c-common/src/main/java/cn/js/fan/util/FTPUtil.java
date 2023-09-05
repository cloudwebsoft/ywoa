package cn.js.fan.util;

import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FTPUtil {
    FTPClient ftp;
    String message;

    public FTPUtil() {
    }

    public FTPClient getFTPClient() {
        return ftp;
    }

    public boolean isConnected() {
        return ftp.isConnected();
    }

    public String getMessage() {
        return message;
    }

    public boolean connect(String host, int port, String userName, String password, boolean isPassiveMode) {
        ftp = new FTPClient();

        boolean re = false;
        try {
            ftp.connect(host, port);
            message = ftp.getReplyString();
            re = ftp.login(userName,
                                   password);
            message = ftp.getReplyString();
            if (!re) {
                message = ftp.getReplyString();
                close();
            } else {
                if (isPassiveMode) {
                    ftp.enterLocalPassiveMode();
                } else {
                    ftp.enterLocalActiveMode();
                }
            }
        }
        catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
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
        ftp.changeWorkingDirectory("");

        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        // ftp.setFileType(ftp.ASCII_FILE_TYPE);

        String fileName;
        if ("".equals(ftpPath)) {
            return false;
        } else {
            if (ftpPath.startsWith("/")) {
                ftpPath = ftpPath.substring(1);
            }
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
        message = ftp.getReplyString();
        return ftp.getReplyCode()==226;
    }

    public boolean completePendingCommand() throws IOException {
        return ftp.completePendingCommand();
    }

    public void close() {
        if (ftp != null && ftp.isConnected()) {
            try {
                ftp.disconnect();
            }
            catch (IOException e) {
                LogUtil.getLog(getClass()).error(e);
            }
            ftp = null;
        }
    }

    /**
     * 删除文件
     * @param path String 如：/111/222/2.jpg
     * @throws IOException
     */
    public int del(String path) {
        int re = -1;
        try {
            re = ftp.dele(path);
        }
        catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return re;
    }

    public int getReplyCode() {
        return ftp.getReplyCode();
    }

    public String getReplyMessage() {
        if (message!=null)
            return message;
        else
            return ftp.getReplyString();
    }

    public void test() throws Exception {
        if (!connect("127.0.0.1", 21, "cws", "1", true)) {
            return;
        }
        //如果是中文名必需进行字符集转换
        //boolean bMakeFlag = ftp.makeDirectory(new String("测试目录".getBytes(
        //  "gb2312"), "iso-8859-1")); //在服务器创建目录

        // ftp.changeWorkingDirectory("/123");

        // storeFile("/111/222/5.rar", "c:/5.rar");

        del("/111/222/2.jpg");

/*
        // LogUtil.getLog(getClass()).info("ftp.systemName=" + ftp.getSystemName());
        FTPFile[] ftpFiles = ftp.listFiles();
        if (ftpFiles != null) {
            for (int i = 0; i < ftpFiles.length; i++) {
                LogUtil.getLog(getClass()).info("ftp file name=" + StrUtil.Unicode2GB(ftpFiles[i].getName()));
                // LogUtil.getLog(getClass()).info(ftpFiles[i].isFile());
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

/*
    public static void main(String[] args) {
        try {
            FtpUtil ftpApache1 = new FtpUtil();
            ftpApache1.test();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
    }
*/
}
