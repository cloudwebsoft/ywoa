package com.redmoon.kit.util;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.File;
import java.util.Vector;
import java.io.FileOutputStream;
import javax.servlet.ServletContext;
import java.util.Hashtable;
import javax.servlet.http.HttpSession;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title:上传时统计文件被读取的字节 </p>
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
public class FileUploadExt extends FileUpload {
    public static final String UPLOADSTATUS = "upload_status";

    public static final int RET_CANCELED = -5;

    public FileUploadExt() {
    }

    @Override
    public String getErrMessage() {
        if (ret==RET_CANCELED) {
            return "Upload is canceled.";
        }
        else
            return super.getErrMessage();
    }

    @Override
    public String getErrMessage(HttpServletRequest request) {
        if (ret==RET_CANCELED) {
            return SkinUtil.LoadString(request, "RET_CANCEL"); // "Upload failed."; //"上传失败！";
        }
        else
            return super.getErrMessage(request);
    }

    /**
     * 上传文件
     * @param request HttpServletRequest
     * @return int
     * @throws IOException
     */
    @Override
    public int doUpload(ServletContext application, HttpServletRequest request, String charset) throws
            IOException {
        this.request = request;

        realPath = application.getRealPath("/");
        // LogUtil.getLog(getClass()).info(getClass() + " realPath=" + realPath + " realPath.lastIndexOf(\"\\\")=" + realPath.lastIndexOf("\\") + " realPath.length()=" + realPath.length());
        if (realPath.lastIndexOf("/")!=realPath.length()-1 && realPath.lastIndexOf("\\")!=realPath.length()-1)
            realPath += "/";

        ret = RET_SUCCESS;
        files.removeAllElements();

        // 获取上传流水号，每次上传对应一个流水号
        String uploadSerialNo = request.getParameter("uploadSerialNo");

        // LogUtil.getLog(getClass()).info(getClass() + " uploadSerialNo=" + uploadSerialNo);

        if (uploadSerialNo==null)
            uploadSerialNo = "default";

        // 置session中的UPLOADSTATUS
        HttpSession session = request.getSession(true);
        FileUploadStatus status = (FileUploadStatus) session.getAttribute(
                UPLOADSTATUS);
        if (status == null) {
            status = new FileUploadStatus();
            session.setAttribute(UPLOADSTATUS, status);
        }
        FileUploadStatusInfo fusi = status.get(uploadSerialNo);
        if (fusi == null) {
            fusi = new FileUploadStatusInfo(uploadSerialNo);
            fusi.setRequestContentLength(request.getContentLength());
            status.add(fusi);
        }

        int allFileSize = 0;

        ServletInputStream in = request.getInputStream();
        // final int maxcount = 2048; // 一行的最大字节数1024,如果设为128会使得当上传文件的路径比较长时被截断
        final int maxcount = 20480; // 一行的最大字节数20480,如果设为128会使得当上传文件的路径比较长时被截断，但是如果一行的字数大于10240时，就有可能会导致一句话被截断，从而出现乱码
        byte[] line = new byte[maxcount];
        byte[] oldline = new byte[maxcount];

        int oldi;
        int i = in.readLine(line, 0, maxcount);
        // 置上传字节
        fusi.setBytesRead(i);

        // debug = true;
        if (debug) {
            // 写入文件，调试用
            File f2 = new File("c:/redmoon_upload.txt");
            FileOutputStream os2;
            os2 = new FileOutputStream(f2);
            OutputStreamWriter osw = new OutputStreamWriter(os2, charset);
            while (i != -1) {
                String d = new String(line, 0, i, charset);
                // os2.write(d.getBytes("ISO-8859-1"));
                osw.write(d);
                i = in.readLine(line, 0, maxcount);
                fusi.setBytesRead(fusi.getBytesRead() + i);
                LogUtil.getLog(getClass()).info(d);
            }
            osw.close();
            os2.close();
            if (true) {
                return -1;
            }
        }

        if (i < 3) { // 第一行小于3则上传出错
            ret = this.RET_FAIL;
            fusi.setRet(ret);
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

        while (i != -1) {
            String newLine = new String(line, 0, i); //第一行
            // 注意当WAP发送时，中间没有空格，如：Content-Disposition:form-data;name="boardcode"
            // if (newLine.startsWith("Content-Disposition: form-data; name=\"")) {
            if (newLine.startsWith("Content-Disposition:")) {
                if (newLine.indexOf("filename=\"") != -1) {
                    // 获取上传的文件
                    int pos = newLine.indexOf("name=\"");
                    String fieldName = newLine.substring(pos + 6,
                            newLine.length() - 3);
                    // 此时 fieldName = filename1"; filename="C:\Documents and Settings\Administrator\My Documents\My Pictures\forum_isnews.gif
                    int index = fieldName.indexOf("\";");
                    fieldName = fieldName.substring(0, index);
                    fieldName = new String(fieldName.getBytes(), charset);
                    if (debug) {
                        LogUtil.getLog(getClass()).info("filename=" + new String(line, 0, i - 2, charset));
                    }
                    parseFileName(new String(line, 0, i - 2, charset));
                    if (filename == null) {
                        // 未上传文件
                        i = in.readLine(line, 0, maxcount);
                        fusi.setBytesRead(fusi.getBytesRead() + i);

                        continue;
                    }
                    if (filename != null && !isValidExtname(extname)) {
                        if (debug) {
                            LogUtil.getLog(getClass()).info("extname=" + extname);
                        }
                        ret = RET_INVALIDEXT; // 扩展名非法
                        fusi.setRet(ret);
                        return ret;
                    }
                    i = in.readLine(line, 0, maxcount);
                    fusi.setBytesRead(fusi.getBytesRead() + i);

                    setContentType(new String(line, 0, i - 2));
                    i = in.readLine(line, 0, maxcount); // 读空行
                    fusi.setBytesRead(fusi.getBytesRead() + i);

                    i = in.readLine(line, 0, maxcount);
                    fusi.setBytesRead(fusi.getBytesRead() + i);

                    newLine = new String(line, 0, i);

                    // StringBuffer filedata = new StringBuffer(1000); //文件数据
                    long thisfilesize = 0;

                    // 创建临时文件
                    String tmpFileName = getRandName() + "." + extname;
                    String tmpFilePath = tmpPath + tmpFileName;

                    // 记录临时文件的路径
                    tmpFiles.addElement(tmpFilePath);

                    File tmpFile = new File( tmpFilePath );
                    FileOutputStream os2 = new FileOutputStream(tmpFile);

                    while (i != -1 && !newLine.startsWith(boundary)) {
                        // 取消上传
                        if (fusi.isCancel()) {
                            status.del(fusi.getSerialNo());
                            ret = RET_CANCELED;
                            fusi.setRet(ret);
                            return ret;
                        }

                        oldi = i;
                        for (int k = 0; k < i; k++) {
                            oldline[k] = line[k]; // 复制line
                        }

                        i = in.readLine(line, 0, maxcount);
                        fusi.setBytesRead(fusi.getBytesRead() + i);

                        if ((i == boundaryLength + 2 || i == boundaryLength + 4)
                            && (new String(line, 0, i).startsWith(boundary))) { //如果是所有数据的最后一行或分界符
                            // filedata.append(new String(oldline, 0, oldi - 2,
                            //        "ISO-8859-1"));
                            os2.write(oldline, 0, oldi-2);

                            allFileSize += oldi - 2;
                            thisfilesize += oldi - 2;
                        } else {
                            // filedata.append(new String(oldline, 0, oldi,
                            //         "ISO-8859-1"));
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
                            fusi.setRet(ret);
                            return ret;
                        }

                        // 如果超过预定上传文件总的大小
                        if (maxAllFileSize != -1 &&
                            allFileSize > maxAllFileSize * 1024) {
                            ret = RET_TOOLARGEALL;
                            tmpFile.delete();
                            os2.close();

                            fusi.setRet(ret);
                            return ret;
                        }
                    }
                    //if (filedata.length()>fileSize*1024)//图片尺寸要小于fileSize
                    //	  ret = -3;		//放在此处不会出现找不到服务器的DNS错误，但是如果文件很大的话就很耗资源

                    // 关闭临地文件写入流
                    os2.close();

                    if (thisfilesize == 0) {
                        if (debug) {
                            LogUtil.getLog(getClass()).info("FileUpload 文件" + filename + "长度为 0 ！");
                        }
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
                    if (debug) {
                        LogUtil.getLog(getClass()).info(fi.name + ": " + fi.size + " " +
                                           fi.ext + " " + fi.contentType);
                    }

                    fi.uploadSerialNo = uploadSerialNo;

                    files.addElement(fi);
                } else {
                    // 获取表单域的值
                    int pos = newLine.indexOf("name=\"");
                    String fieldName = newLine.substring(pos + 6,
                            newLine.length() - 3);

                    // 如果是IE或其它浏览器上传，读取空行，如果是WAP上传，则下面会比前者多一行Content-Type:text/plain
                    i = in.readLine(line, 0, maxcount);
                    fusi.setBytesRead(fusi.getBytesRead() + i);

                    String seperateLine = new String(line, 0, i, charset);
                    // LogUtil.getLog(getClass()).info(getClass() + " seperateLine=" + seperateLine);
                    if (seperateLine.startsWith("Content-Type")) {
                        i = in.readLine(line, 0, maxcount); // 读取空行
                        fusi.setBytesRead(fusi.getBytesRead() + i);

                    }
                    i = in.readLine(line, 0, maxcount);
                    fusi.setBytesRead(fusi.getBytesRead() + i);

                    // LogUtil.getLog(getClass()).info("reqeust getCharacterEncoding: " + request.getCharacterEncoding()); // 取得的值为null

                    newLine = new String(line, 0, i, charset);
                    // newLine = new String(line, 0, i);

                    StringBuffer fieldValue = new StringBuffer(maxcount);

                    while (i != -1 && !newLine.startsWith(boundary)) {
                        // 最后一行包含换行字符
                        // 因此我们必须检查当前行是否是最后一行
                        i = in.readLine(line, 0, maxcount);
                        fusi.setBytesRead(fusi.getBytesRead() + i);

                        if ((i == boundaryLength + 2 || i == boundaryLength + 4)
                            && (new String(line, 0, i).startsWith(boundary))) {
                            fieldValue.append(newLine.substring(0,
                                    newLine.length() - 2));
                        } else {
                            fieldValue.append(newLine);
                        }
                        // newLine = new String(line, 0, i);
                        newLine = new String(line, 0, i, charset);
                    }
                    // String fv = new String(fieldValue.toString().getBytes("ISO8859_1"), charset);
                    // fields.put(fieldName, fv);
                    Object obj = fields.get(fieldName);
                    // LogUtil.getLog(getClass()).info(getClass() + " fieldName=" + fieldName + " value=" + fieldValue.toString());
                    if (obj!=null) {
                        // 如果为字符串
                        if (obj instanceof String) {
                            Vector v = new Vector();
                            v.addElement(obj); // 第一个对应于filed的值
                            v.addElement(fieldValue.toString());
                            fields.put(fieldName, v);
                        }
                        else {
                            Vector v = (Vector) obj;
                            v.addElement(fieldValue.toString());
                        }
                    }
                    else {
                        fields.put(fieldName, fieldValue.toString());
                        // LogUtil.getLog(getClass()).info(getClass() + " fieldName=" + fieldValue.toString());
                    }
                }
            }
            i = in.readLine(line, 0, maxcount);
            fusi.setBytesRead(fusi.getBytesRead() + i);
        }
        in.close();

        fusi.setFinish(true);
        fusi.setRet(ret);

        fusi.setBytesRead(fusi.getBytesRead() + 1); // 为了加结束符

        // LogUtil.getLog(getClass()).info(getClass() + " upload bytes=" + fusi.getBytesRead());
        // LogUtil.getLog(getClass()).info(getClass() + " content length=" + request.getContentLength());

        return ret;
    }

}
