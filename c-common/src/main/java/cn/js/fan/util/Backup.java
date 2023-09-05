package cn.js.fan.util;

import java.io.*;
import cn.js.fan.util.file.*;
import cn.js.fan.db.*;
import com.cloudwebsoft.framework.util.LogUtil;

public class Backup {
    public Backup() {
    }

    public static void copyDirectory(String filedes, String filesrc) throws
            IOException {
        (new File(filedes)).mkdirs();
        File[] file = (new File(filesrc)).listFiles();
        if (file == null) // 如果filesrc不存在则file为null
            return;
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                FileInputStream input = new FileInputStream(file[i]);
                FileOutputStream output = new FileOutputStream(filedes + "/" +
                        file[i].getName());
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    output.write(b, 0, len);
                }
                output.flush();
                output.close();
                input.close();
            }
            if (file[i].isDirectory()) {
                copyDirectory(filedes + "/" + file[i].getName(),
                              filesrc + "/" + file[i].getName());
            }
        }
    }

    public void generateZipFile(String srcpath, String zipfilepath) throws
            IOException {
        Compress cps = new Compress();
        cps.zipDirectory(srcpath, zipfilepath);
    }

    public boolean BackupDB(String dbName, String poolName, String dbfile) {
        // MS SQL SERVER 备份
        String sql = "BACKUP DATABASE " + dbName + " TO DISK = '" +
                     dbfile + "' with init"; // 重写

        Conn conn = new Conn(poolName);
        try {
            conn.executeUpdate(sql);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            return false;
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

}
