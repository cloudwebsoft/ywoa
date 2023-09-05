package cn.js.fan.util.file;

import cn.js.fan.security.AntiXSS;
import com.cloudwebsoft.framework.util.LogUtil;

import java.io.*;

public class FileUtil extends Object {
    private String path; //文件完整路径名

    public FileUtil() {
    }

    public static String ReadFile(String filePath) throws FileNotFoundException {
        return ReadFile(filePath, "UTF-8");
    }

    /**
     * 读取文件filePath中的数据，并返回这个数据
     *
     * @param filePath String
     * @param charset  String
     * @return String
     * @throws FileNotFoundException
     */
    public static String ReadFile(String filePath, String charset) {
        StringBuilder returnStr = new StringBuilder();
        BufferedReader reader = null;
        InputStreamReader read = null;
        try {
            read = new InputStreamReader(new FileInputStream(filePath), charset);
            reader = new BufferedReader(read);
            String line = null;
            while ((line = reader.readLine()) != null) {
                // 读取一行数据并保存到currentRecord变量中
                returnStr.append(line).append("\r\n");
            }
        } catch (IOException e) { //错误处理
            LogUtil.getLog(FileUtil.class).error(e);
        } finally {
            if (read!=null) {
                try {
                    read.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
        }

        return returnStr.toString();
    }

    public static String readFile(InputStream inputStream, String charset) {
        StringBuilder returnStr = new StringBuilder();
        BufferedReader reader = null;
        InputStreamReader read = null;
        try {
            read = new InputStreamReader(inputStream, charset);
            reader = new BufferedReader(read);
            String line = null;
            while ((line = reader.readLine()) != null) {
                // 读取一行数据并保存到currentRecord变量中
                returnStr.append(line).append("\r\n");
            }
        } catch (IOException e) { //错误处理
            LogUtil.getLog(FileUtil.class).error(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (read!=null) {
                try {
                    read.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
        }

        return returnStr.toString();
    }

    /**
     * 非utf-8的方式
     *
     * @param filePath String
     * @param str      String
     * @throws FileNotFoundException
     */
    public static void WriteFile(String filePath, String str) throws
            FileNotFoundException {
        try {
            // 创建PrintWriter对象，用于写入数据到文件中
            PrintWriter pw = new PrintWriter(new FileOutputStream(filePath));
            //用文本格式打印整数Writestr
            pw.println(str);
            //清除PrintWriter对象
            pw.close();
        } catch (IOException e) {
            LogUtil.getLog(FileUtil.class).error(e);
        }
    }

    public static void WriteFile(String filefullpath, String str, String charset) {
        FileOutputStream fo = null;
        OutputStreamWriter osw = null;
        PrintWriter out1 = null;
        try {
            fo = new FileOutputStream(filefullpath);
            osw = new OutputStreamWriter(fo, charset);
            out1 = new PrintWriter(osw);
            out1.println(str);
        } catch (IOException e) {
            LogUtil.getLog(FileUtil.class).error(e);
        }
        finally {
            if (out1!=null) {
                out1.close();
            }
            if (osw!=null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (fo!=null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
        }
    }

    /**
     * 以utf-8编码的方式写文件
     *
     * @param filefullpath String
     * @param str          String
     */
    public static void WriteFileUTF8(String filefullpath, String str) {
        WriteFile(filefullpath, str, "UTF-8");
    }

    /**
     * 注意如果filePathDesc的目录不存在，则不会自动创建
     *
     * @param filePathSrc String
     * @param filePathDes String
     * @return boolean
     */
    public static boolean CopyFile(String filePathSrc, String filePathDes) {
        boolean re = false;
        File fSrc = new File(filePathSrc);
        if (!fSrc.exists()) {
            return false;
        }
        if (fSrc.isFile()) {
            FileInputStream input = null;
            FileOutputStream output = null;
            try {
                input = new FileInputStream(fSrc);
                output = new FileOutputStream(filePathDes);
                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = input.read(b)) != -1) {
                    output.write(b, 0, len);
                }
                output.flush();
                re = true;
            } catch (IOException e) {
                LogUtil.getLog(FileUtil.class).error(e);
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        LogUtil.getLog(FileUtil.class).error(e);
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        LogUtil.getLog(FileUtil.class).error(e);
                    }
                }
            }
        } else {
            LogUtil.getLog(FileUtil.class).error("Error:" + filePathSrc + " is not found！");
        }
        return re;
    }

    public static boolean appendFile(String desFilePath, String srcFilePath) {
        boolean re = true;
        RandomAccessFile rf = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            rf = new RandomAccessFile(desFilePath, "rw");
            // 定义一个类RandomAccessFile的对象，并实例化
            rf.seek(rf.length()); // 将指针移动到文件末尾
            fis = new FileInputStream(srcFilePath);
            bis = new BufferedInputStream(fis); // 读取文件的BufferedRead对象
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = bis.read(buf)) != -1) {
                rf.write(buf, 0, len);
            }
        } catch (IOException e) {
            LogUtil.getLog(FileUtil.class).error(e);
            re = false;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (fis != null) {
                try {
                    fis.close(); // 关闭文件
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (rf != null) {
                try {
                    rf.close();  // 关闭文件流
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
        }
        return re;
    }

    /**
     * 根据文件路径，取得文件名
     *
     * @param filePath String
     * @return String
     */
    public static String getFileName(String filePath) {
        int p = filePath.lastIndexOf("\\");
        if (p == -1) {
            p = filePath.lastIndexOf("/");
        }
        if (p == -1) {
            return filePath;
        }
        return filePath.substring(p + 1);
    }

    public static String getFileExt(String fileName) {
        // 下面取到的扩展名错误，只有三位，而如html的文件则有四位
        // extName = fileName.substring(fileName.length() - 3, fileName.length()); //扩展名
        if (fileName == null) {
            return "";
        }
        int dotindex = fileName.lastIndexOf(".");
        String extName = fileName.substring(dotindex + 1, fileName.length());
        return extName.toLowerCase(); // 置为小写
    }

    public static String getFileNameWithoutExt(String fileName) {
        int dotindex = fileName.lastIndexOf(".");
        String fName = fileName.substring(0, dotindex);
        fName = fName.toLowerCase(); // 置为小写
        return fName;
    }

    public static void del(String filepath) throws IOException {
        File f = new File(filepath); // 定义文件路径
        if (f.exists()) {
            if (f.isDirectory()) { // 判断是文件还是目录
                if (f.listFiles().length == 0) { // 若目录下没有文件则直接删除
                    f.delete();
                } else { // 若有则把文件放进数组，并判断是否有下级目录
                    File delFile[] = f.listFiles();
                    int i = f.listFiles().length;
                    for (int j = 0; j < i; j++) {
                        if (delFile[j].isDirectory()) {
                            del(delFile[j].getAbsolutePath()); // 递归调用del方法并取得子目录路径
                        }
                        delFile[j].delete(); // 删除文件
                    }
                }
                del(filepath); // 递归调用
            } else {
                f.delete();
            }
        }
    }

    /**
     * 在文件末尾追加数据
     *
     * @param fileName 文件名称
     * @param content  写入内容
     */
    public static void append(String fileName, String content) {
        FileOutputStream fo = null;
        OutputStreamWriter osw = null;
        PrintWriter out1 = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fo = new FileOutputStream(fileName, true);
            osw = new OutputStreamWriter(fo);
            out1 = new PrintWriter(osw);
            out1.println(content);
        } catch (IOException e) {
            LogUtil.getLog(FileUtil.class).error(e);
        } finally {
            if (out1!=null) {
                out1.close();
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    LogUtil.getLog(FileUtil.class).error(e);
                }
            }
        }
    }
}
