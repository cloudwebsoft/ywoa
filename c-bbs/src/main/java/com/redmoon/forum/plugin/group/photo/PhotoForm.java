package com.redmoon.forum.plugin.group.photo;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.web.Global;
import com.redmoon.forum.util.ForumFileUpload;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.forum.Privilege;

public class PhotoForm extends AbstractForm {
    public ForumFileUpload fileUpload;

    public PhotoForm() {
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new ForumFileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"jpg", "gif", "png", "bmp"};
        fileUpload.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public long getGroupId() {
        return groupId;
    }

    public int getSort() {
        return sort;
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

    public long chkBlogId() {
        groupId = Long.parseLong(fileUpload.getFieldValue("groupId"));
        return groupId;
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

    public String chkDirection(HttpServletRequest request) {
        try {
            direction = ParamUtil.get(request, "direction");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return direction;
    }

    public String chkUserName(HttpServletRequest request) {
        Privilege pvg = new Privilege();
        userName = pvg.getUser(request);
        return userName;
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

    public boolean checkAdd(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        chkTitle();
        chkBlogId();
        chkUserName(request);
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
        chkBlogId();
        chkTitle();
        chkSort();
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

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setSort(int sort) {
        this.sort = sort;
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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ForumFileUpload getFileUpload() {
        return fileUpload;
    }

    public String getUserName() {
        return userName;
    }

    private long groupId;
    private int sort;
    private String title;
    private int id;
    private String direction;
    private String userName;

}
