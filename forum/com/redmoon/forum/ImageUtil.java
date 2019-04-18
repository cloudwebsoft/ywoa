package com.redmoon.forum;

import cn.js.fan.util.file.image.*;
import com.redmoon.kit.util.*;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import com.redmoon.kit.util.FileInfo;
import cn.js.fan.web.Global;


/**
 * <p>Title:论坛图片处理 </p>
 *
 * <p>Description: 水印操作</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ImageUtil {
    FileUpload fileUpload = null;
    static Logger logger;
    public ImageUtil() {
    }

    public void WaterMark(FileInfo fi) {
        Config cfg = Config.getInstance();
        int pos = cfg.getIntProperty("forum.waterMarkPos");
        int alpha = cfg.getIntProperty("forum.waterMarkTransparence");
        float a = 0.0f;
        if (alpha>100)
            a = 1.0f;
        else
            a = (float)alpha / 100;
        WaterMarkUtil wmu = new WaterMarkUtil(pos); // WaterMarkUtil.POS_LEFT_TOP);
        // wmu.setOffsetX(150);
        // wmu.setOffsetY(0);
        wmu.mark(fi.getTmpFilePath(),
                  fi.getTmpFilePath(), realPath + "images/watermark.gif", a);
        // Font font = new Font("隶书", Font.BOLD, 20);
        // Color color = new Color(255, 0, 0);
        // wmu.mark(fi.getTmpFilePath(), fi.getTmpFilePath(), Global.AppName, font,
        //         color, 1f);
    }

    public FileUpload doUpload(ServletContext application,
                              HttpServletRequest request) throws
           ErrMsgException {
       fileUpload = new FileUpload();
       String[] extnames = {"gif"};
       fileUpload.setValidExtname(extnames);
       int ret = 0;
       try {
           ret = fileUpload.doUpload(application, request);
           if (ret != fileUpload.RET_SUCCESS) {
               throw new ErrMsgException(fileUpload.getErrMessage(request));
           }
       } catch (IOException e) {
           logger.error("doUpload:" + e.getMessage());
       }
       return fileUpload;
   }

   public void modify(ServletContext application,
                      HttpServletRequest request, String imagesName) throws ErrMsgException {
       doUpload(application, request);
       String filename = getFilename(fileUpload, imagesName);
       if (filename.equals("")) {
           throw new ErrMsgException("filename can't be null！");
       }
   }

   public String getFilename(FileUpload fu, String imagesName) {
       if (fu.getRet() == fu.RET_SUCCESS) {
           Vector v = fu.getFiles();
           FileInfo fi = null;
           if (v.size() > 0) {
               fi = (FileInfo) v.get(0);
           }
           String vpath = "";
           if (fi != null) {
               vpath = "images/";
               String filepath = Global.getRealPath() + vpath;
               fu.setSavePath(filepath);
               if(!fi.write(filepath, imagesName)) {
                    logger.error("rename filename error!");
               }
               return vpath + fi.getDiskName();
           }
       }
       return "";
   }


    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getRealPath() {
        return realPath;
    }

    private String realPath;
}
