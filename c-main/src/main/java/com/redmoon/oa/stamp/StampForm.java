package com.redmoon.oa.stamp;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;

public class StampForm extends AbstractForm {
    public FileUpload fileUpload;

    public StampForm() {
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"bmp", "gif", "jpg", "png"};
        fileUpload.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String getUserNames() {
        return userNames;
    }

    public int getSort() {
        return sort;
    }

    public String getKind() {
        return kind;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    public String chkUserName() {
        userNames = fileUpload.getFieldValue("userNames");
        if (!SecurityUtil.isValidSqlParam(userNames))
            log("请勿使用' ; 等字符！");
        return userNames;
    }

    public String chkRoleCodes() {
        roleCodes = fileUpload.getFieldValue("roleCodes");
        if ((roleCodes==null || roleCodes.equals("")) && (userNames==null || userNames.equals(""))) {
            log("用户名或角色必须填写其一！");
        }
        if (!SecurityUtil.isValidSqlParam(roleCodes))
            log("请勿使用' ; 等字符！");
        return roleCodes;
    }

    public String chkTitle() {
        title = fileUpload.getFieldValue("title");
        if (title==null || title.equals("")) {
            log("名称必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(title))
            log("请勿使用' ; 等字符！");
        return title;
    }

    public int chkId(HttpServletRequest request) {
        try {
            id = ParamUtil.getInt(request, "id");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public int chkId() {
        try {
            id = Integer.parseInt(fileUpload.getFieldValue("id"));
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public String chkPwd() {
        pwd = fileUpload.getFieldValue("pwd");
        if (pwd==null || pwd.equals("")) {
            log("密码必须填写！");
        }
        return pwd;
    }

    public String chkDirection(HttpServletRequest request) {
        try {
            direction = ParamUtil.get(request, "direction");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return direction;
    }

    public int chkSort() {
        String sSort = fileUpload.getFieldValue("sort");
        try {
            sort = Integer.parseInt(sSort);
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return sort;
    }

    public String chkKind() {
        kind = fileUpload.getFieldValue("kind");
        if (kind==null || kind.equals("")) {
            log("类别必须填写！");
        }
        return kind;
    }

    public boolean checkAdd(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        chkTitle();
        chkUserName();
        chkKind();
        chkPwd();
        chkRoleCodes();
        report();
        return true;
    }

    public boolean checkDel(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        report();
        return true;
    }

    public boolean checkModify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        chkUserName();
        chkTitle();
        chkKind();
        chkPwd();
        chkRoleCodes();
        chkId();
        report();
        return true;
    }

    public boolean checkMove(HttpServletRequest request) throws ErrMsgException {
        init();
        chkId(request);
        chkDirection(request);
        report();
        return true;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setRoleCodes(String roleCodes) {
        this.roleCodes = roleCodes;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public String getPwd() {
        return pwd;
    }

    public String getRoleCodes() {
        return roleCodes;
    }

    private String userNames;
    private int sort;
    private String kind;
    private String title;
    private int id;
    private String direction;
    private String pwd;
    private String roleCodes;
}
