package cn.js.fan.module.nav;

import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import cn.js.fan.web.Global;
import javax.servlet.ServletContext;
import java.util.Vector;
import com.redmoon.kit.util.FileInfo;
import java.util.Calendar;
import com.redmoon.forum.util.ForumFileUpload;

public class LinkForm extends AbstractForm {
    public ForumFileUpload fileUpload;

    public LinkForm() {
    }

    public ForumFileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new ForumFileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"jpg", "gif", "png", "swf"};
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

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
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

    private String url;

    public String chkUrl() {
        url = fileUpload.getFieldValue("url");
        if (url==null || url.equals("")) {
            log("链接必须填写！");
        }
        if (!SecurityUtil.isValidSqlParam(url))
            log("请勿使用' ; 等字符！");
        return url;
    }

    public String chkUserName() {
        userName = fileUpload.getFieldValue("userName");
        if (userName==null || userName.equals("")) {
            log("缺少用户名！");
        }
        if (!SecurityUtil.isValidSqlParam(userName))
            log("请勿使用' ; 等字符！");
        return userName;
    }

    public String chkTitle() {
        title = fileUpload.getFieldValue("title");
        if (title==null || title.equals("")) {
            log("文本必须填写！");
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
        chkUrl();
        chkTitle();
        chkUserName();
        chkKind();
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
        chkUrl();
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public ForumFileUpload getFileUpload() {
        return fileUpload;
    }

    private String userName;
    private int sort;
    private String kind;
    private String title;
    private int id;
    private String direction;

}
