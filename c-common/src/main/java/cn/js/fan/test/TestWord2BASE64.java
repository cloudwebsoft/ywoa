package cn.js.fan.test;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.file.FileUtil;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;

import com.cloudwebsoft.framework.util.LogUtil;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

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
public class TestWord2BASE64 {
    public TestWord2BASE64() {
    }

    public boolean Hex2WordFile(String filePathSrc, String filePathDes) {
        String hexString = "";

        SecurityUtil su = new SecurityUtil();
        boolean re = false;
        BufferedReader file = null;
        FileOutputStream output = null;
        String strline = "";
        try {
            file = new BufferedReader(new FileReader(filePathSrc));
            output = new FileOutputStream(filePathDes);
            // 读取一行数据并保存到currentRecord变量中
            strline = file.readLine();
            while (strline!=null) {
                byte[] bytes = su.hexstr2byte(strline);
                output.write(bytes, 0, bytes.length);
                strline = file.readLine();
            }
            re = true;
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        finally {
            try {
                output.flush();
                output.close();
                file.close();
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        return re;
    }

    public String Word2HexString(String filePathSrc, String filePathDes) {
        String hexString = "";

         SecurityUtil su = new SecurityUtil();
         boolean re = false;
         File fSrc = new File(filePathSrc);
         if (!fSrc.exists())
             return "";
         try {
             if (fSrc.isFile()) {
                 FileInputStream input = new FileInputStream(fSrc);
                 byte[] b = new byte[1024 * 5];
                 int len;
                 while ((len = input.read(b)) != -1) {
                     hexString += su.byte2hex(b);
                 }
                 input.close();
             } else {
                 System.out.print("debug:" + filePathSrc + "已不存在！");
             }
         } catch (IOException e) {
             LogUtil.getLog(getClass()).error(e);
         }

        return hexString;
    }

    public boolean Word2HexFile(String filePathSrc, String filePathDes) {
        String hexString = "";

        SecurityUtil su = new SecurityUtil();
        boolean re = false;
        File fSrc = new File(filePathSrc);
        if (!fSrc.exists())
            return re;
        try {
            if (fSrc.isFile()) {
                FileInputStream input = new FileInputStream(fSrc);
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    hexString += su.byte2hex(b);
                }
                input.close();
                re = true;
            } else {
                System.out.print("debug:" + filePathSrc + "已不存在！");
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        try {
            FileUtil fut = new FileUtil();
            fut.WriteFile(filePathDes, hexString);
        }
        catch (FileNotFoundException e) {
            LogUtil.getLog(getClass()).info(e.getMessage());
        }
        return re;
    }

    public boolean Word2BASE64File(String filePathSrc, String filePathDes) {
        String hexString = Word2BASE64String(filePathSrc);
        boolean re = true;
        try {
            FileUtil fut = new FileUtil();
            fut.WriteFile(filePathDes, hexString);
        }
        catch (FileNotFoundException e) {
            LogUtil.getLog(getClass()).info(e.getMessage());
        }
        return re;
    }

    public String Word2BASE64String(String filePathSrc) {
        String str = "";

        SecurityUtil su = new SecurityUtil();
        boolean re = false;
        File fSrc = new File(filePathSrc);
        if (!fSrc.exists())
            return "";
        try {
            if (fSrc.isFile()) {
                FileInputStream input = new FileInputStream(fSrc);
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    str += new BASE64Encoder().encode(b);
                }
                input.close();
            } else
                System.out.print("debug:Word2BASE64String" + filePathSrc + "已不存在！");
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }

        return str;
    }

    public boolean BASE642WordFile(String filePathSrc, String filePathDes) {
        String hexString = "";

        SecurityUtil su = new SecurityUtil();
        boolean re = false;
        BufferedReader file = null;
        FileOutputStream output = null;
        String strline = "";
        try {
            file = new BufferedReader(new FileReader(filePathSrc));
            output = new FileOutputStream(filePathDes);
            // 读取一行数据并保存到currentRecord变量中
            strline = file.readLine();
            while (strline!=null) {
                byte[] bytes = new BASE64Decoder().decodeBuffer(strline);
                output.write(bytes, 0, bytes.length);
                strline = file.readLine();
            }
            re = true;
        } catch (IOException e) { //错误处理
            LogUtil.getLog(getClass()).info("读取数据错误.");
        }
        finally {
            try {
                output.flush();
                output.close();
                file.close();
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).info(e.getMessage());
            }
        }

        return re;
    }

    public boolean BASE64String2WordFile(String base64Str, String filePathDes) {
        boolean re = false;
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(filePathDes);
            byte[] bytes = new BASE64Decoder().decodeBuffer(base64Str);
            output.write(bytes, 0, bytes.length);
            re = true;
        } catch (IOException e) { //错误处理
            LogUtil.getLog(getClass()).info("读取数据错误.");
        } finally {
            try {
                output.flush();
                output.close();
            } catch (Exception e) {
                LogUtil.getLog(getClass()).info(e.getMessage());
            }
        }

        return re;
    }

    public static void main(String[] args) throws Exception {
        TestWord2BASE64 twb = new TestWord2BASE64();
        // twb.Word2HexFile("c:/doc1.doc", "c:/dochex.txt");
        // twb.Hex2WordFile("c:/dochex.txt", "c:/doc1_tran.doc");

        // twb.Word2BASE64File("c:/aaa.doc", "c:/aaa.txt");
        // twb.BASE642WordFile("c:/aaa.txt", "c:/555.doc");

        String str = twb.Word2BASE64String("c:/aaa.doc");
        twb.BASE64String2WordFile(str, "c:/666.doc");
    }
}
