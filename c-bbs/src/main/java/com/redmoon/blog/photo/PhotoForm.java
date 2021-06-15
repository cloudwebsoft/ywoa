package com.redmoon.blog.photo;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;

public class PhotoForm extends AbstractForm {
    public ForumFileUpload fileUpload;

    public PhotoForm() {
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new ForumFileUpload();
        com.redmoon.blog.Config cfg = com.redmoon.blog.Config.getInstance();
        int photoSize = cfg.getIntProperty("photoSize"); // photoSize以K为单位
        fileUpload.setMaxFileSize(photoSize);

        String[] extnames = {"jpg", "gif", "png", "bmp"};
        fileUpload.setValidExtname(extnames);//设置可上传的文件类型
  
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request, "utf-8");
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public long getBlogId() {
        return blogId;
    }

    public int getSort() {
        return sort;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    public long chkBlogId() {
        blogId = Long.parseLong(fileUpload.getFieldValue("blogId"));
        return blogId;
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

    public long chkId(HttpServletRequest request) {
        try {
            id = ParamUtil.getInt(request, "id");
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public long chkId() {
        try {
            id = Long.parseLong(fileUpload.getFieldValue("id"));
        }
        catch (Exception e) {
            log(e.getMessage());
        }
        return id;
    }

    public String chkDirection(HttpServletRequest request) {
        direction = ParamUtil.get(request, "direction");
        return direction;
    }

    public String chkDirCode() {
        dirCode = fileUpload.getFieldValue("dirCode");
        if (dirCode.equals(""))
            log("请选择分类");
        return dirCode;
    }


    public boolean chkIsLocked() {
        locked = !StrUtil.getNullStr(fileUpload.getFieldValue("isLocked")).equals("0");
        return locked;
    }

    public int chkSort() {
        String sSort = fileUpload.getFieldValue("sort");
        sort = StrUtil.toInt(sSort, 0);
        return sort;
    }

    public long chkCatalog() {
        String sCatalog= fileUpload.getFieldValue("catalog");
        catalog = StrUtil.toLong(sCatalog, 0);
        return catalog;
    }

    public boolean checkAdd(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        doUpload(application, request);
        chkTitle();
        chkBlogId();
        chkDirCode();
        chkIsLocked();
        chkSort();
        chkCatalog();
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
        chkDirCode();
        chkId();
        chkIsLocked();
        chkSort();
        chkCatalog();
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

    public void setBlogId(long blogId) {
        this.blogId = blogId;
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

    public void setDirCode(String dirCode) {
        this.dirCode = dirCode;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setCatalog(long catalog) {
        this.catalog = catalog;
    }

    public ForumFileUpload getFileUpload() {
        return fileUpload;
    }

    public String getDirCode() {
        return dirCode;
    }

    public boolean isLocked() {
        return locked;
    }

    public long getCatalog() {
        return catalog;
    }

    private long blogId;
    private int sort;
    private String title;
    private long id;
    private String direction;
    private String dirCode;
    private boolean locked = false;
    private long catalog = 0;

}
