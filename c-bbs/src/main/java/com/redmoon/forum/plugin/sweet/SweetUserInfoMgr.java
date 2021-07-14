package com.redmoon.forum.plugin.sweet;

import java.io.File;
import java.util.Calendar;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.BoardManagerDb;
import com.redmoon.forum.Privilege;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import org.apache.log4j.Logger;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.web.Global;

public class SweetUserInfoMgr {
    Logger logger = Logger.getLogger(SweetUserInfoMgr.class.getName());

    public SweetUserInfoMgr() {
    }

    public boolean add(HttpServletRequest request) throws ErrMsgException {
        SweetUserInfoForm suif = new SweetUserInfoForm();
        suif.checkAdd(request);

        return suif.sweetUserInfoDb.create();
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        SweetUserInfoForm suif = new SweetUserInfoForm();
        suif.checkDel(request);

        SweetUserInfoDb suid = this.getSweetUserInfoDb(suif.sweetUserInfoDb.getName());
        boolean re = suid.del();
        return re;
    }

    public boolean edit(HttpServletRequest request) throws ErrMsgException {
        SweetUserInfoForm suif = new SweetUserInfoForm();
        suif.checkEdit(request);

        return suif.sweetUserInfoDb.save();
    }

    public SweetUserInfoDb getSweetUserInfoDb(String name) {
        return new SweetUserInfoDb(name);
    }

    public synchronized boolean editPhoto(ServletContext application, HttpServletRequest request) throws
              ErrMsgException {
          String photo = "";
          FileUpload fileupload = new FileUpload();
          boolean re = false;
          int ret = 0;
          try {
              ret = fileupload.doUpload(application, request);
          } catch (Exception e) {
              logger.error("modifyLogo: " + e.getMessage());
              throw new ErrMsgException(e.getMessage());
          }

          userName = StrUtil.getNullString(fileupload.getFieldValue("userName"));
          if (userName.equals("")) {
              throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.sweet","ERR_USER_NAME"));//用户名不能为空
          }

          boardCode = StrUtil.getNullString(fileupload.getFieldValue("boardcode"));
          if (boardCode.equals("")) {
              throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.sweet","ERR_BOARD_NAME"));//版块名称不能为空！
          }

          SweetUserInfoDb suid = getSweetUserInfoDb(userName);

          Privilege privilege = new Privilege();
          String user = privilege.getUser(request);
          BoardManagerDb bm = new BoardManagerDb();
          bm = bm.getBoardManagerDb(boardCode, user);

          // 如果不是版主
          if (!bm.isLoaded()) {
              // 如果是用户本人编辑
              if (user.equals(userName)) {
                  if (suid.isChecked())
                      throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.sweet","ERR_MSG_CHECKED"));//信息已被版主审核，您不能再修改，请与版主联系
              }
              else
                  throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.plugin.sweet","ERR_PRIVILIGE"));//非法操作！您无此权限"
          }

          if (fileupload.getRet()==fileupload.RET_SUCCESS) {
              // 删除原来的photo
              try {
                  String sPhoto = StrUtil.getNullString(suid.getPhoto());
                  if (!sPhoto.equals("")) {
                      File file = new File(Global.getRealPath() + sPhoto);
                      file.delete();
                  }
              }
              catch (Exception e) {
                  logger.info(e.getMessage());
              }

              Vector v = fileupload.getFiles();
              FileInfo fi = null;
              if (v.size()>0)
                  fi = (FileInfo)v.get(0);
              String vpath = "";
              if (fi!=null) {
                  // 置保存路径
                  Calendar cal = Calendar.getInstance();
                  String year = "" + (cal.get(cal.YEAR));
                  String month = "" + (cal.get(cal.MONTH) + 1);
                  vpath = "forum/upfile/" +
                                    fi.getExt() + "/" + year + "/" + month + "/";
                  String filepath = Global.realPath + vpath;

                  fileupload.setSavePath(filepath);
                  // 置总的上传文件的大小的最大值为600K
                  fileupload.setMaxAllFileSize(600);
                  // 设置单个文件的最大值为200k
                  fileupload.setMaxFileSize(200);
                  // 设置合法的扩展名
                  String[] ext = {"gif", "jpg", "png", "jpeg"};
                  fileupload.setValidExtname(ext);
                  // 使用随机名称写入磁盘
                  fileupload.writeFile(true);
                  photo = fi.getDiskName();
                  photo = vpath + photo;
                  suid.setPhoto(photo);
                  re = suid.save();
              }
              else {
                  photo = "";
                  suid.setPhoto(photo);
                  re = suid.save();
              }
          }
          else {
              throw new ErrMsgException(fileupload.getErrMessage(request));
          }
          return re;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getUserName() {
        return userName;
    }

    public String getBoardCode() {
        return boardCode;
    }

    private String userName;
    private String boardCode;

}
