package com.redmoon.kit.util;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 支持断点续传</p>
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
public class UploadDdxc {
    public long maxFileSize = 1024 * 600;  // 默认为600M

    // 在JDK1.5中已经出现concurrentmap比下面的方式，更利于高效同步
    static Map uploadFileInfos = Collections.synchronizedMap(new HashMap());

    // Vector fileList = new Vector();

    final String upload_type_file_header = "TYPE_FILE_HEADER";
    final String upload_type_thread_header = "TYPE_THREAD_HEADER";
    final String upload_type_segment = "TYPE_SEGMENT";
    final String upload_type_file_finished = "TYPE_FILE_FINISHED";

    final String thread_segment_ok = "Thread segment ok";
    final String thread_header_ok = "Thread header ok";
    // final String post_info_ok = "Post info ok";
    public final String file_finished_ok = "File finished ok";
    public final String file_header_ok = "File header ok";

    final String file_count_exceed_max = "Too many files are being uploaded. Please wait.";

    public String visualPath = ""; // 用于forum的断点续传，因为forum中上传时filepath=upfile/webedit/...，而实际路径为forum/upfile/webedit/...，所以需设visualPath = "forum/"

    public UploadDdxc() {
        // 下行已放入Global.init作初始化，所以不加也没关系
        UploadReaper.initInstance(UploadReaper.getReapInterval());
    }

    public Map getUploadFileInfos() {
        return uploadFileInfos;
    }

    public String receive(ServletContext application,
                          HttpServletRequest request) throws ErrMsgException {
        FileUpload fu = doUpload(application, request);
        return op(request, fu);
    }

    public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        FileUpload fu = new FileUpload();
        fu.setMaxFileSize((int) maxFileSize); // 每个文件最大30000K 即近300M
        // String[] ext = {"htm", "gif", "bmp", "jpg", "png", "rar", "doc", "hs", "ppt", "rar", "zip", "jar"};
        // fu.setValidExtname(ext);
        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret == fu.RET_TOOLARGESINGLE) {
                throw new ErrMsgException(fu.getErrMessage());
            }
            if (ret == fu.RET_INVALIDEXT) {
                throw new ErrMsgException(fu.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fu;
    }

    public String op(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        String uploadType = fu.getFieldValue("uploadType");
        if (uploadType == null) {
            LogUtil.getLog(getClass()).error("want uploadType");
            Enumeration enu = fu.getFields();
            while (enu.hasMoreElements()) {
                String key = (String)enu.nextElement();
                LogUtil.getLog(getClass()).info("op: key=" + key + " value=" + fu.fields.get(key));
            }
            return "want uploadType";
        }
        String re = "";
        if (uploadType.equals(upload_type_file_header)) {
            re = ReceiveUploadFileHeader(request, fu);
        } else if (uploadType.equals(upload_type_thread_header)) {
            re = ReceiveUploadThreadHeader(fu);
        } else if (uploadType.equals(upload_type_segment)) {
            re = ReceiveUploadThreadFileSegment(fu);
        } else if (uploadType.equals(upload_type_file_finished)) {
            re = ReceiveUploadFileFinished(request, fu);
        }
        else
            throw new ErrMsgException("Upload type error.");
        return re;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setRandName(boolean randName) {
        this.randName = randName;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isRandName() {
        return randName;
    }

    /**
     * ReceiveUploadFileHeader(FileUpload fu)
     */
    public String ReceiveUploadFileHeader(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        // 检查总的上传文件是否已达到最大值，如果是则reap，reap后如果还是最大值，则抛出异常
        if (uploadFileInfos.size()>=Global.maxUploadingFileCount)
            return file_count_exceed_max;

        // 接收文件，如果是文件头，则将其记录于fileList
        String fileId = fu.getFieldValue("fileId");
        if (fileId==null)
            throw new ErrMsgException("want fileId");

        String clientFilePath = fu.getFieldValue("clientFilePath");
        if (debug)
            LogUtil.getLog(getClass()).info("clientFilePath=" + clientFilePath);
        String filepath;
        if (visualPath.equals(""))
            filepath = fu.getFieldValue("filepath");
        else
            filepath = visualPath + "/" + fu.getFieldValue("filepath");

        UploadFileInfo ufi = new UploadFileInfo();
        ufi.setFileId(fileId);
        ufi.setClientFilePath(clientFilePath);
        ufi.setFilePath(filepath); // 保存文件的路径
        ufi.setFileName(FileUtil.getFileName(clientFilePath));

        uploadFileInfos.put(fileId, ufi);
        // LogUtil.getLog(getClass()).info("ReceiveUploadFileHeader fileId=" + fileId);
        return file_header_ok;
    }

    public String ReceiveUploadThreadHeader(FileUpload fu) throws
            ErrMsgException {
        // 如果是线程的头部
        String fileId = fu.getFieldValue("fileId");
        if (fileId == null)
            throw new ErrMsgException("want fileId");
        String sBlockId = fu.getFieldValue("blockId");
        if (sBlockId == null)
            throw new ErrMsgException("want blockId");
        int blockId = Integer.parseInt(sBlockId);

        UploadThreadInfo uti = new UploadThreadInfo();
        uti.setFileId(fileId);
        uti.setBlockId(blockId);
        // 记录于UploadFileInfo中
        UploadFileInfo ufi = (UploadFileInfo) uploadFileInfos.get(fileId);
        if (ufi == null) {
            LogUtil.getLog(getClass()).error("ReceiveUploadThreadHeader fileId=" + fileId);
            throw new ErrMsgException(
                    "There are no file header for this thread.");
        }
        ufi.addUploadThreadInfo(uti);
        try {
            // 创建文件块
            String blockPath = fu.getTmpPath() + ufi.getBlockName(blockId);
            File file = new File(blockPath);
            if (!(file.exists()))
                file.createNewFile(); // 如果文件不存在，创建此文件
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("创建块文件失败！" + e.getMessage());
        }
        return thread_header_ok;
    }

    public String ReceiveUploadThreadFileSegment(FileUpload fu) throws
            ErrMsgException {
        String fileId = fu.getFieldValue("fileId");
        if (fileId == null)
            throw new ErrMsgException("want fileId");
        // 如果是线程的segment，则写至文件块中的相应的位置
        String sBlockId = fu.getFieldValue("blockId");
        if (sBlockId == null)
            throw new ErrMsgException("want blockId!");
        String sOffset = fu.getFieldValue("offset");
        if (sOffset == null)
            throw new ErrMsgException("want offset!");
        String sLength = fu.getFieldValue("length");
        if (sLength == null)
            throw new ErrMsgException("want length!");

        int blockId = Integer.parseInt(sBlockId);
        int offset = Integer.parseInt(sOffset);
        int length = Integer.parseInt(sLength);

        Vector v = fu.getFiles();
        if (v.size() > 0) {
            FileInfo fi = (FileInfo) v.get(0);
            String tmpPath = fi.getTmpFilePath();
            File file = new File(tmpPath);
            // 长度不相等，说明上传过程中出错！要求客户端重新再传
            if (length != file.length()) {
                LogUtil.getLog(getClass()).error("segment length=" + length + " tmp file length=" + file.length());
                throw new ErrMsgException("segment error");
            }
            // 追加数据至fileId文件的blockId块
            UploadFileInfo ufi = (UploadFileInfo) uploadFileInfos.get(fileId);
            if (ufi == null)
                throw new ErrMsgException("want thread header");
            String blockPath = fu.getTmpPath() + ufi.getBlockName(blockId);
            try {
                RandomAccessFile rf = new RandomAccessFile(blockPath, "rw");
                // 定义一个类RandomAccessFile的对象，并实例化
                // rf.seek(rf.length()); // 将指针移动到文件末尾
                // 当传输过程中出错，或者传输中途被取消时，就有可能会使得续传时，开始处不一定是在文件的末尾
                // 因此上句不能使用，应该使用offset作为文件指针的偏移量
                rf.seek(offset);

                File tmpFile = new File(fi.getTmpFilePath());
                // FileInputStream fis = new FileInputStream(fi.getTmpFilePath());
                FileInputStream fis = new FileInputStream(tmpFile);
                BufferedInputStream bis = new BufferedInputStream(fis); // 读取文件的BufferedRead对象
                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = bis.read(buf)) != -1) {
                    rf.write(buf, 0, len);
                }
                bis.close();
                fis.close(); // 关闭文件
                rf.close();  // 关闭文件流
                tmpFile.delete(); // 删除上传得到的临时文件
            } catch (IOException e) {
                LogUtil.getLog(getClass()).error("ReceiveUploadThreadFileSegment: " + e.getMessage());
            }
        } else
            throw new ErrMsgException("want file");
        return thread_segment_ok;
    }

    public String ReceiveUploadFileFinished(HttpServletRequest request, FileUpload fu) throws
            ErrMsgException {
        // 如果是文件的结束，则将文件整理并拷贝至相应的目录，然后删除临时文件
        String fileId = fu.getFieldValue("fileId");
        if (fileId == null)
            throw new ErrMsgException("want fileId");

        UploadFileInfo ufi = (UploadFileInfo) uploadFileInfos.get(fileId);
        // LogUtil.getLog(getClass()).info("ReceiveUploadFileFinished:" + ufi.getFilePath() + " ---- " + ufi.getClientFilePath());
        if (ufi == null)
                throw new ErrMsgException("want thread header");
        // 在UploadFIleInfo指定的保存位置写入文件
        String fullSavePath = ufi.getFullSavePath(fu.getRealPath(), randName);
        try {
            // 创建文件块
            File file = new File(fullSavePath);
            // LogUtil.getLog(getClass()).info("fullSavePath" + fullSavePath);
            if (!(file.exists()))
                file.createNewFile(); // 如果文件不存在，创建此文件
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("ReceiveUploadFileFinished:" + StrUtil.trace(e) + " fullSavePath=" + fullSavePath);
        }

        ufi.setState(ufi.state_finished);
        Vector v = ufi.getUploadThreadInfos();
        int size = v.size();
        // 组合文件
        for (int i = 0; i < size; i++) {
            UploadThreadInfo uti = (UploadThreadInfo) v.get(i);
            String blockPath = fu.getTmpPath() +
                               ufi.getBlockName(uti.getBlockId());
            if (debug) {
                LogUtil.getLog(getClass()).info(uti.getFileId() + " begin=" + uti.getBegin() +
                            " end=" + uti.getEnd());
                LogUtil.getLog(getClass()).info("fullSavePath=" + fullSavePath + " fu.getRealPath()=" + fu.getRealPath());
                LogUtil.getLog(getClass()).info("blockPath=" + blockPath);
            }
            FileUtil.appendFile(fullSavePath, blockPath);
            // 删除上传线程产生的临时文件
            File file = new File(blockPath);
            file.delete();
        }
        // 从链表中删除UploadFileInfo
        uploadFileInfos.remove(fileId);

        return file_finished_ok;
    }

    private boolean debug = true;
    private boolean randName = true;

}
