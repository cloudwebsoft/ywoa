package com.redmoon.kit.util;

/**
 * <p>Title: 文件上传组件Redmoon UploadBean</p>
 * <p>Description: 支持多个文件上载，文件保存时可以随机取名称，也可以取文件本来的名称</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 红月亮工作室</p>
 * @author 蓝风
 * @version 1.0
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.security.AntiXSS;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.cloudwebsoft.framework.security.ProtectSQLInjectException;
import com.cloudwebsoft.framework.security.ProtectXSSException;
import com.cloudwebsoft.framework.security.SecurityUtil;
import com.cloudwebsoft.framework.util.LogUtil;

public class FileUpload {
    boolean debug = false;
    public String savePath;
    String filepath, filename, contentType;
    public Dictionary fields;
    // 单个文件最大2048 * 1000K 即2G，window2000的默认最大值
    public int fileSize = 2048 * 1000;
    // 上传文件的扩展名
    String extname = "";
    String[] extnames = null;
    public Vector<FileInfo> files;

    // 存放临时文件名
    public Vector<String> tmpFiles = new Vector<>();

    static Hashtable<Integer, String> hash = new Hashtable<>();
    // 随机数字生成器
    static Random rand = new Random(System.currentTimeMillis());
    static long lastRandTime = System.currentTimeMillis();

    int ret = 1;

    public static final int RET_SUCCESS = 1;
    public static final int RET_FAIL = -1;
    public static final int RET_TOOLARGEALL = -2;
    public static final int RET_TOOLARGESINGLE = -3;
    public static final int RET_INVALIDEXT = -4;
    public static final int RET_XSS = -5;
    public static final int RET_SQL_INJ = -6;

    public HttpServletRequest request;

    public String getLastErrMsg() {
        return lastErrMsg;
    }

    private String lastErrMsg = "";

    public static final String TEMP_PATH = "FileUploadTmp";

    public FileUpload() {
        files = new Vector<>();
    }

    @Override
    protected void finalize() throws Throwable {
        // 删除临时文件
        // @task:因GC回收时需进行磁盘IO，会不会导致回收速度慢？
        // 2013/10/13，在UploadReaper中加入了临时文件超过12小时则删除，此处不作处理也没关系
        for (String fpath : tmpFiles) {
            File f = new File(fpath);
            f.delete();
        }
        super.finalize();
    }

    /**
     * 取文件尺寸默认最大值
     * @return int
     */
    public int getDefaultFileSize() {
        return fileSize;
    }

    /**
     * 置单个文件尺寸默认最大值，单位为K
     * @param size int
     */
    public void setMaxFileSize(int size) {
        fileSize = size;
    }

    /**
     * 取得所有的文件
     * @return Vector
     */
    public Vector<FileInfo> getFiles() {
        return files;
    }

    /**
     * 取上传doUpload()的返回值
     * @return int
     */
    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    /**
     * 置上传文件的保存路径
     * @param savePath String
     */
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    /**
     * 取得所有域的名称
     * @return Enumeration
     */
    public Enumeration getFields() {
        return fields.keys();
    }

    public Dictionary getFieldsDict() {
        return fields;
    }

    public void setFields(Dictionary fields) {
        this.fields = fields;
    }

    public void setFiles(Vector<FileInfo> files) {
        this.files = files;
    }

    /**
     * 取得上传文件的保存路径
     * @return String
     */
    public String getSavePath() {
        return savePath;
    }

    public int getMaxAllFileSize() {
        return maxAllFileSize;
    }

    public String getRealPath() {
        return realPath;
    }

    public static String getTmpPath() {
        return tmpPath;
    }

    public boolean isJspValid() {
        return jspValid;
    }

    public boolean getHtmValid() {
        return htmValid;
    }

    public String getFieldValue(String fieldName) {
        return getFieldValue(fieldName, true);
    }

    /**
     * 取得相应表单域的值
     * @param fieldName String 域的名称
     * @return String 当域不存在时，返回值为null
     */
    public String getFieldValue(String fieldName, boolean isTrim) {
        if (fields == null || fieldName == null) {
            return null;
        }
        String str = null;
        try {
            if (isTrim) {
                str = (String) fields.get(fieldName);
                if (str!=null) {
                    str = StrUtil.trim(str);
                }
            }
            else {
                str = (String) fields.get(fieldName);
            }
        }
        catch (ClassCastException e) {
            LogUtil.getLog(getClass()).error("field:" + fieldName + " class cast exception");
            LogUtil.getLog(getClass()).error(e);
            throw e;
        }
        return str;
    }

    public void setFieldValue(String fieldName, String value) {
        fields.put(fieldName, value);
    }

    public String[] getFieldValues(String fieldName) {
        Object obj = fields.get(fieldName);
        if (obj==null) {
            return null;
        }
        if (obj.getClass().isInstance(fieldName)) {
            String[] r = new String[1];
            r[0] = (String)obj;
            return r;
        }
        else {
            Vector<String> v = (Vector) obj;
            int len = v.size();
            String[] r = new String[len];
            for (int i=0; i<len; i++) {
                r[i] = v.get(i);
            }
            return r;
        }
    }

    /**
     * 分析文件名称
     * @param s String
     */
    public void parseFileName(String s) {
        if (s == null) {
            return;
        }

        int pos = s.indexOf("filename=\"");
        if (pos != -1) {
            filepath = s.substring(pos + 10, s.length() - 1);
            if ("".equals(filepath)) {
                return; // 未上传文件
            }
            filename = getUploadFileName(filepath);
            extname = getFileExt(filename);
        }
    }

    /**
     * 置文件内容类型
     * @param s String
     */
    public void setContentType(String s) {
        if (s == null) {
            return;
        }

        int pos = s.indexOf(": ");
        if (pos != -1) {
            contentType = s.substring(pos + 2, s.length());
        }
    }

    public void setMaxAllFileSize(int maxAllFileSize) {
        this.maxAllFileSize = maxAllFileSize;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public void setTmpPath(String tmpPath) {
        FileUpload.tmpPath = tmpPath;
    }

    public void setJspValid(boolean jspValid) {
        this.jspValid = jspValid;
    }

    public void setHtmValid(boolean htmValid) {
        this.htmValid = htmValid;
    }

    /**
     * 置合法的扩展名
     * @param extnames1 String[]
     */
    public void setValidExtname(String[] extnames1) {
        if (extnames1 == null) {
            return;
        }
        int len = extnames1.length;
        extnames = new String[len];
        System.arraycopy(extnames1, 0, extnames, 0, len);
    }

    /**
     * 判别扩展名是否合法
     * @param ext String 扩展名，如: zip、doc、xls、rar等
     * @return boolean
     */
    public boolean isValidExtname(String ext) {
        if (extnames == null) {
            if ("jsp".equalsIgnoreCase(ext) || "jspx".equalsIgnoreCase(ext)) {
                // 除非显式允许JSP文件
                return jspValid;
            }
            else if ("htm".equalsIgnoreCase(ext) || "html".equalsIgnoreCase(ext)){
                return htmValid;
            }
            else {
                return true;
            }
        }
        int len = extnames.length;
        for (String s : extnames) {
            // LogUtil.getLog(getClass()).info(getClass() + " exts=" + extnames[i] + ".");
            if (s.equalsIgnoreCase(ext)) {
                if ("jsp".equalsIgnoreCase(ext)) {
                    // 除非显示式允许JSP文件，否则即使配置允许上传JSP，而源码中未显示设置setJspValid(true)，也不允许
                    return jspValid;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取得出错信息
     * ret=-1 表示：上传失败
     * ret=-2 表示：文件总的大小超过了预设的最大值
     * ret=-3 表示：单个文件大小超过了预设的最大值
     * ret=-4 表示：扩展名非法
     * ret=1  表示：上传成功
     * @return String
     */
    public String getErrMessage() {
        String msg = "";
        switch (ret) {
        case RET_FAIL:
            msg = "Upload failed."; //"上传失败！";
            break;
        case RET_TOOLARGEALL:
            msg = "The total size of files exceed predefined max size " + getMaxAllFileSize() + "."; // "文件总的大小超过了预定最大值" + getMaxAllFileSize() + "K";
            break;
        case RET_TOOLARGESINGLE:
            msg = "Too large file. Every file should be limited in " + getDefaultFileSize() + " K"; // "文件太大,请把每个文件大小限制在" + this.getDefaultFileSize() + "K以内！";
            break;
        case RET_INVALIDEXT:
            msg = "文件扩展名非法： " + extname; // "扩展名非法！";
            break;
        case RET_SUCCESS:
            msg = "Success."; // "上传成功！";
            break;
        case RET_XSS:
            msg = "XSS Attack, " + lastErrMsg;
            break;
        case RET_SQL_INJ:
            msg = "SQL inject Attack, " + lastErrMsg;
            break;
        default:
            msg = "Error RET value."; // "上传返回值出错！";
        }
        return msg;
    }

    public String getErrMessage(HttpServletRequest request) {
        String msg = "";
        switch (ret) {
        case RET_FAIL:
            msg = SkinUtil.LoadString(request, "RET_FAIL"); // "Upload failed."; //"上传失败！";
            break;
        case RET_TOOLARGEALL:
            msg = SkinUtil.LoadString(request, "RET_TOOLARGEALL") + getMaxAllFileSize() + "K"; // "文件总的大小超过了预定最大值" + getMaxAllFileSize() + "K";
            break;
        case RET_TOOLARGESINGLE:
            msg = SkinUtil.LoadString(request, "RET_TOOLARGESINGLE") + getDefaultFileSize() + " K"; // "文件太大,请把每个文件大小限制在" + this.getDefaultFileSize() + "K以内！";
            break;
        case RET_INVALIDEXT:
            String str = "";
            if (extnames!=null) {
                for (String s : extnames) {
                    if ("".equals(str)) {
                        str = s;
                    } else {
                        str += "," + s;
                    }
                }
            }
            msg = extname + "," + StrUtil.format(SkinUtil.LoadString(request, "RET_INVALIDEXT"), new Object[] {str}); // "扩展名非法！";
            break;
        case RET_SUCCESS:
            msg = SkinUtil.LoadString(request, "RET_SUCCESS"); // "上传成功！";
            break;
        case RET_XSS:
            msg = "XSS Attack, " + lastErrMsg;
            break;
        case RET_SQL_INJ:
            msg = "SQL inject Attack, " + lastErrMsg;
            break;
        default:
            msg = SkinUtil.LoadString(request, "RET_ERROR"); // "上传返回值出错！";
        }
        return msg;
    }

    public int doUpload(ServletContext application, HttpServletRequest request) throws IOException {
        return doUpload(application, request, "utf-8");
    }

    public static String getTempPath() {
        if (tmpPath == null) {
            tmpPath = Global.getRealPath() + TEMP_PATH + "/";
            File f = new File(tmpPath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
        }
        return tmpPath;
    }

    /**
     * 上传文件
     * @param request HttpServletRequest
     * @return int
     * @throws IOException
     */
    public int doUpload(ServletContext application, HttpServletRequest request, String charset) throws IOException {
        this.request = request;
        //文件路径从config_cws.xml获取 jfy 20150104
        //realPath = application.getRealPath("/");
        realPath = Global.getRealPath();
        if (realPath.lastIndexOf("/")!=realPath.length()-1 && realPath.lastIndexOf("\\")!=realPath.length()-1) {
            realPath += "/";
        }

        ret = RET_SUCCESS;
        files.removeAllElements();

        int allFileSize = 0;

        ServletInputStream in = request.getInputStream();
        // final int maxcount = 2048; // 一行的最大字节数1024,如果设为128会使得当上传文件的路径比较长时被截断
        // 20210116由20480改为204800，因有些word文件的一行较长
        final int maxcount = 204800; // 一行的最大字节数204800,如果设为128会使得当上传文件的路径比较长时被截断，但是如果一行的字数大于10240时，就有可能会导致一句话被截断，从而出现乱码
        byte[] line = new byte[maxcount];
        byte[] oldline = new byte[maxcount];

        int oldi;
        int i = in.readLine(line, 0, maxcount);

        // debug = true;
        if (false) {
            // 写入文件，调试用
            File f2 = new File("d:/redmoon_upload.txt");
            FileOutputStream os2;
            os2 = new FileOutputStream(f2);
            OutputStreamWriter osw = new OutputStreamWriter(os2, charset);
            while (i != -1) {
                String d = new String(line, 0, i, charset);
                // os2.write(d.getBytes("ISO-8859-1"));
                osw.write(d);
                i = in.readLine(line, 0, maxcount);
                // LogUtil.getLog(getClass()).info(d);
            }
            osw.close();
            os2.close();
            return -1;
        }

        if (i < 3) { // 第一行小于3则上传出错
            ret = RET_FAIL;
            return ret;
        }

        int boundaryLength = i - 2; // 去除换行回车

        String boundary = new String(line, 0, boundaryLength); //-2是为了丢弃换行字符
        fields = new Hashtable();

        /**
         * Web站点主目录的位置为<%=request.getRealPath("/")%>
         JSP网页所在的目录位置<%=request.getRealPath("./")%>
         JSP网页所在目录上一层目录的位置<%=request.getRealPath("../")%>
         */
        // 检查临时文件目录是否存在，不存在，则创建
        if (tmpPath==null) {
            tmpPath = realPath + TEMP_PATH + "/";
            File f = new File(tmpPath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
        }

        boolean isFilterAttack = SecurityUtil.isFilter(request, request.getRequestURL().toString());

        try {
            while (i != -1) {
                String newLine = new String(line, 0, i); //第一行
                // 注意当WAP发送时，中间没有空格，如：Content-Disposition:form-data;name="boardcode"
                // if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
                if (newLine.startsWith("Content-Disposition:")) {
                    if (newLine.contains("filename=\"")) {
                        // 获取上传的文件
                        int pos = newLine.indexOf("name=\"");
                        String fieldName = newLine.substring(pos + 6, newLine.length() - 3);
                        // 此时 fieldName = filename1"; filename="C:\Documents and Settings\Administrator\My Documents\My Pictures\forum_isnews.gif
                        int index = fieldName.indexOf("\";");
                        fieldName = fieldName.substring(0, index);
                        fieldName = new String(fieldName.getBytes(), charset);
                        parseFileName(new String(line, 0, i - 2, charset));
                        if (filename == null) {
                            // 未上传文件
                            i = in.readLine(line, 0, maxcount);
                            continue;
                        }
                        if (!isValidExtname(extname)) {
                            ret = RET_INVALIDEXT; // 扩展名非法
                            // 继续读完，否则前端会报：网络异常，请检查您的网络连接是否正常!
                            byte[] buffer = new byte[maxcount * 1000];
                            while (i != -1) {
                                i = in.read(buffer);
                            }
                            return ret;
                        }
                        i = in.readLine(line, 0, maxcount);
                        setContentType(new String(line, 0, i - 2));
                        i = in.readLine(line, 0, maxcount); // 读空行

                        i = in.readLine(line, 0, maxcount);
                        newLine = new String(line, 0, i);

                        // StringBuffer filedata = new StringBuffer(1000); //文件数据
                        long thisfilesize = 0;

                        // 创建临时文件
                        String tmpFileName = getRandName() + "." + extname;
                        String tmpFilePath = tmpPath + tmpFileName;

                        // 记录临时文件的路径
                        tmpFiles.addElement(tmpFilePath);

                        File tmpFile = new File(tmpFilePath);
                        FileOutputStream os2;
                        os2 = new FileOutputStream(tmpFile);

                        while (i != -1 && !newLine.startsWith(boundary)) {
                            oldi = i;
                            // 复制line
                            if (i >= 0) {
                                System.arraycopy(line, 0, oldline, 0, i);
                            }

                            i = in.readLine(line, 0, maxcount);
                            if ((i == boundaryLength + 2 || i == boundaryLength + 4)
                                    && (new String(line, 0, i).startsWith(boundary))) { //如果是所有数据的最后一行或分界符
                                // filedata.append(new String(oldline, 0, oldi - 2, "ISO-8859-1"));
                                os2.write(oldline, 0, oldi - 2);

                                allFileSize += oldi - 2;
                                thisfilesize += oldi - 2;
                            } else {
                                // filedata.append(new String(oldline, 0, oldi, "ISO-8859-1"));
                                os2.write(oldline, 0, oldi);

                                allFileSize += oldi;
                                thisfilesize += oldi;
                            }
                            newLine = new String(line, 0, i);

                            // if (filedata.length() > fileSize * 1024) { // 图片尺寸要小于fileSize K
                            if (thisfilesize > fileSize * 1024) {
                                // 此处直接退出，当文件太大时会导致客户端出现“找不到服务器”错误，可能是因为in未readline所有request中的数据而致使产生浏览器报DNS错误
                                in.close();
                                tmpFile.delete();
                                os2.close();
                                ret = RET_TOOLARGESINGLE;
                                return ret;
                            }

                            // 如果超过预定上传文件总的大小
                            if (maxAllFileSize != -1 &&
                                    allFileSize > maxAllFileSize * 1024) {
                                ret = RET_TOOLARGEALL;
                                tmpFile.delete();
                                os2.close();
                                return ret;
                            }
                        }
                        //if (filedata.length()>fileSize*1024)//图片尺寸要小于fileSize
                        //	  ret = -3;		//放在此处不会出现找不到服务器的DNS错误，但是如果文件很大的话就很耗资源

                        // 关闭临地文件写入流
                        os2.close();

                        if (thisfilesize == 0) {
                            continue;
                        }

                        FileInfo fi = new FileInfo();
                        fi.fieldName = fieldName;
                        fi.name = filename;
                        fi.ext = extname;
                        fi.setTmpFilePath(tmpFilePath);
                        fi.clientPath = filepath;

                        // fi.data = filedata.toString();
                        fi.contentType = contentType;
                        fi.size = thisfilesize; // 或者filedata.length();//以K为单位
                        files.addElement(fi);
                    } else {
                        // 获取表单域的值
                        int pos = newLine.indexOf("name=\"");
                        String fieldName;

                        if (newLine.lastIndexOf(";") == newLine.length() - 3) {
                            // linux android上传
                            fieldName = newLine.substring(pos + 6, newLine.length() - 4);
                        } else {
                            // 有时可能会出现不规范的情况，如下所示，name="后面没有内容了
                            // -----------------------------7e5631420c18c4
                            // Content-Disposition: form-data; name="
                            if (newLine.length() - 3 > pos + 6) {
                                fieldName = newLine.substring(pos + 6, newLine.length() - 3);
                            }
                            else {
                                fieldName = "";
                                LogUtil.getLog(getClass()).error(newLine);
                                LogUtil.getLog(getClass()).error("fieldName is not valid.");
                                break;
                            }
                        }

                        // 如果是IE或其它浏览器上传，读取空行，如果是WAP上传，则下面会比前者多一行Content-Type:text/plain
                        i = in.readLine(line, 0, maxcount);
                        String seperateLine = new String(line, 0, i, charset);
                        if (seperateLine.startsWith("Content-Type")) {
                            i = in.readLine(line, 0, maxcount); // 读取空行
                        }

                        i = in.readLine(line, 0, maxcount);

                        newLine = new String(line, 0, i, charset);
                        // newLine = new String(line, 0, i);

                        StringBuilder fieldValue = new StringBuilder(maxcount);

                        while (i != -1 && !newLine.startsWith(boundary)) {
                            // 最后一行包含换行字符
                            // 因此我们必须检查当前行是否是最后一行
                            i = in.readLine(line, 0, maxcount);
                            if ((i == boundaryLength + 2 || i == boundaryLength + 4)
                                    && (new String(line, 0, i).startsWith(boundary))) {
                                fieldValue.append(newLine.substring(0, newLine.length() - 2));
                            } else {
                                fieldValue.append(newLine);
                            }
                            // newLine = new String(line, 0, i);
                            newLine = new String(line, 0, i, charset);
                        }

                        String fieldV = fieldValue.toString();
                        // 防攻击
                        if (isFilterAttack) {
                            try {
                                SecurityUtil.filter(fieldName, fieldV, false);
                            } catch (ProtectXSSException e) {
                                // DebugUtil.e(getClass(), "AntiXSS " + fieldName, fieldV);
                                lastErrMsg = "字段：" + fieldName + "，值：" + fieldV;
                                ret = RET_XSS;
                                return ret;
                                // fieldV = "Filtered by AntiXSS";
                            } catch (ProtectSQLInjectException e) {
                                // DebugUtil.e(getClass(), "AntiSQL " + fieldName, fieldV);
                                lastErrMsg = "字段：" + fieldName + "，值：" + fieldV;
                                ret = RET_SQL_INJ;
                                return ret;
                                // fieldV = "Filtered by AntiSQL";
                            }
                        }

                        // String fv = new String(fieldValue.toString().getBytes("ISO8859_1"), charset);
                        // fields.put(fieldName, fv);
                        Object obj = fields.get(fieldName);
                        if (obj != null) {
                            // 如果为字符串
                            if (obj instanceof String) {
                                Vector<String> v = new Vector<>();
                                v.addElement((String)obj); // 第一个对应于filed的值
                                v.addElement(fieldV);
                                fields.put(fieldName, v);
                            } else {
                                Vector<String> v = (Vector) obj;
                                v.addElement(fieldV);
                            }
                        } else {
                            fields.put(fieldName, fieldV);
                        }
                    }
                }
                i = in.readLine(line, 0, maxcount);
            }
        }
        finally {
            in.close();
        }

        return ret;
    }

    /**
     * 写入文件
     * @param isRandName 是否用随机的文件名
     */
    public void writeFile(boolean isRandName) {
        int size = files.size();
        if (size == 0) {
            return;
        }

        File f = new File(savePath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        java.util.Enumeration<FileInfo> e = files.elements();
        while (e.hasMoreElements()) {
            FileInfo fi = e.nextElement();
            if (!isRandName) {
                fi.write(savePath);
            } else { //防止文件名重复
                fi.write(savePath, getRandName() + "." + fi.getExt());
            }
        }
    }

    /**
     * 生成唯一数字ID
     * @return
     */
    public static String getRandName() {
        // 根据时间值，重置hash，否则hash会无限增大
        if (System.currentTimeMillis()-lastRandTime>20000) {
            hash.clear();
        }
        int id = 0;
        synchronized (hash) {
            // 生成一个唯一的随机数字
            id = rand.nextInt();
            while (hash.containsKey(id)) {
                id = rand.nextInt();
            }
            // 为当前用户保留该ID
            String data = "";
            hash.put(id, data);
        }
        lastRandTime = System.currentTimeMillis();
        return System.currentTimeMillis() + "" + Math.abs(id);
    }

    /**
     * 从上传的文件路径获取文件名
     * Windows浏览器发送完整的文件路径和名字
     * 但Linux/Unix和Mac浏览器只发送文件名字
     * @param filePath String
     * @return String
     */
    public static String getUploadFileName(String filePath) {
        String fileName = "";
        int pos = filePath.lastIndexOf("\\");
        if (pos != -1) {
            fileName = filePath.substring(pos + 1);
        } else {
            fileName = filePath;
        }

        return fileName;
    }

    public static String getFileExt(String fileName) {
        // 下面取到的扩展名错误，只有三位，而如html的文件则有四位
        // extName = fileName.substring(fileName.length() - 3, fileName.length()); //扩展名
        int dotindex = fileName.lastIndexOf(".");
        String extName = "";
        if (dotindex!=-1) {
            extName = fileName.substring(dotindex + 1, fileName.length());
            extName = extName.toLowerCase(); //置为小写
        }
        return extName;
    }

    public long getAllFileSize() {
        long allSize = 0;
        for (FileInfo fi : getFiles()) {
            allSize += fi.getSize();
        }
        return allSize;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }
    
    public HttpServletRequest getRequest() {
    	return request;
    }

    public int maxAllFileSize = -1; // 等于-1表示上传总大小不受限制

    public String realPath;
    public static String tmpPath = null;
    public boolean jspValid = false; // 是否允许JSP文件上传
    public boolean htmValid = false; // 是否允许HTM文件上传
}
