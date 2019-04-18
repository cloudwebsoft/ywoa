package com.redmoon.oa.address;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import cn.js.fan.web.Global;
import org.apache.log4j.Logger;
import java.util.*;
import com.redmoon.kit.util.FileInfo;

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
public class FileUpMgr {
    FileUpload fileUpload = null;
    Logger logger = Logger.getLogger(FileUpMgr.class.getName());
    public FileUpMgr() {
    }
    
    public FileUpload getFileUpload() {
    	return fileUpload;
    }
    
    public String uploadExcel(ServletContext application,
                         HttpServletRequest request) throws
           ErrMsgException {
       String excelFile = "";
       String upFile = "";
       doUpload(application,request);
       upFile = writeExcel(fileUpload);
       if (!upFile.equals(""))
           excelFile = Global.getRealPath() + upFile;
       else
           throw new ErrMsgException("文件不能为空！");
       return  excelFile;
   }

   public FileUpload doUpload(ServletContext application,
                               HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"xls","xlsx"};
        fileUpload.setValidExtname(extnames); //设置可上传的文件类型
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String writeExcel(FileUpload fu) {
        if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                vpath = "upfile/" +
                        fi.getExt() + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + vpath;
                fu.setSavePath(filepath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);

                //File f = new File(vpath + fi.getDiskName());
                //f.delete();
                //System.out.println("FleUpMgr " + fi.getName() + " " + fi.getFieldName() + " " + fi.getDiskName());
                return vpath + fi.getDiskName();
            }
        }
        return "";
    }
}
