package cn.js.fan.util.file;

import java.io.*;

import org.apache.log4j.Logger;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import java.net.URI;

public class FileUtil extends Object {
    private String path; //文件完整路径名
    public FileUtil() {
    }

    public static String ReadFile(String filePath) throws FileNotFoundException {
        return ReadFile(filePath, "UTF-8");
    }

    /**
     * 读取文件filePath中的数据，并返回这个数据
     * @param filePath String
     * @param charset String
     * @return String
     * @throws FileNotFoundException
     */
    public static String ReadFile(String filePath, String charset) throws FileNotFoundException {
        String returnStr = "";
        BufferedReader reader = null;
        try {
            InputStreamReader read = new InputStreamReader (new FileInputStream(filePath),charset);
            reader = new BufferedReader(read);
            String line = null;
            while ((line = reader.readLine()) != null) {
                // 读取一行数据并保存到currentRecord变量中
                returnStr += line + "\r\n";
            }
            read.close();
        } catch (IOException e) { //错误处理
            System.out.println(FileUtil.class.getName() + " " + e.getMessage());
        }
        finally {
            try {
                reader.close();
            }
            catch (Exception e) {
            }
        }

        return returnStr;
    }

    /**
     * 非utf-8的方式
     * @param filePath String
     * @param str String
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
            //错误处理
            System.out.println("写入文件错误:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void WriteFile(String filefullpath, String str, String charset) {
        try {
            FileOutputStream fo = new FileOutputStream(filefullpath);
            OutputStreamWriter osw = new OutputStreamWriter(fo, charset);
            PrintWriter out1 = new PrintWriter(osw);
            out1.println(str);
            out1.close();
            osw.close();
            fo.close();
        } catch (IOException e) {
            System.out.println("FileUtil.java writeFile:" + e.getMessage());
        }
    }

    /**
     * 以utf-8编码的方式写文件
     * @param filefullpath String
     * @param str String
     */
    public static void WriteFileUTF8(String filefullpath, String str) {
        WriteFile(filefullpath, str, "UTF-8");
    }

    /**
     * 注意如果filePathDesc的目录不存在，则不会自动创建
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
                System.out.print("Error:" + filePathSrc + " is not found！");
            }
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
        return re;
  }

  public static boolean AppendFile(String desFilePath, String srcFilePath) {
      boolean re = true;
      try {
          RandomAccessFile rf = new RandomAccessFile(desFilePath, "rw");
          // 定义一个类RandomAccessFile的对象，并实例化
          rf.seek(rf.length()); // 将指针移动到文件末尾
          FileInputStream fis = new FileInputStream(srcFilePath);
          BufferedInputStream bis = new BufferedInputStream(fis); // 读取文件的BufferedRead对象
          byte[] buf = new byte[1024];
          int len = 0;
          int totalNum = 0;
          while ((len=bis.read(buf)) != -1) {
              rf.write(buf, 0, len);
              totalNum += len;
          }
          bis.close();
          fis.close(); // 关闭文件
          rf.close();  // 关闭文件流
      } catch (IOException e) {
          re = false;
          System.out.println(FileUtil.class.getName() + " AppendFile:" + e.getMessage());
      }
      return re;
  }

  /**
   * 根据文件路径，取得文件名
   * @param filePath String
   * @return String
   */
  public static String getFileName(String filePath) {
      int p = filePath.lastIndexOf("\\");
      if (p==-1) {
          p = filePath.lastIndexOf("/");
      }
      if (p==-1) {
          return filePath;
      }
      return filePath.substring(p+1);
  }

  public static String getFileExt(String fileName) {
      // 下面取到的扩展名错误，只有三位，而如html的文件则有四位
      // extName = fileName.substring(fileName.length() - 3, fileName.length()); //扩展名
      if (fileName==null) {
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
          }
      } else {
          f.delete();
      }
  }
  
  /**
   * 在文件末尾追加数据
   * @param fileName 文件名称
   * @param content 写入内容
   */
  public static void append(String fileName,String content){
	 try {
		 File file = new File(fileName);
		 if(!file.exists()){
			 file.createNewFile();
		 }
		  FileOutputStream fo = new FileOutputStream(fileName,true);
	      OutputStreamWriter osw = new OutputStreamWriter(fo);
	      PrintWriter out1 = new PrintWriter(osw);
	      out1.println(content);
	      out1.close();
	      osw.close();
	      fo.close();
	  }catch(IOException e){
		  System.out.println(FileUtil.class.getName() + " append:" + e.getMessage());
		  e.printStackTrace();
	  }
	  
  }
}
