package com.redmoon.oa.idiofileark;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.*;

public class IdiofilearkForm extends AbstractForm {
    IdiofilearkDb md;
    HttpServletRequest request;
    ServletContext application;

    public FileUpload fileUpload;

    public IdiofilearkForm() {
    }

    public IdiofilearkForm(ServletContext application, HttpServletRequest request,
                       IdiofilearkDb md) {
        this.application = application;
        this.request = request;
        this.md = md;
    }

    public FileUpload doUpload() throws
            ErrMsgException {
        fileUpload = new FileUpload();
        //fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        // String[] extnames = {"jpg", "gif", "png"};
        // fileUpload.setValidExtname(extnames);//设置可上传的文件类型
        Config cfg = new Config();
        fileUpload.setMaxAllFileSize(cfg.getInt("shortMsgFileSize"));
        String exts = cfg.get("shortMsgFileExt");
        String[] extAry = StrUtil.split(exts, ",");
        if (extAry!=null)
            fileUpload.setValidExtname(extAry);
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

    public String chkIp() {
        md.ip = request.getRemoteAddr();
        return md.ip;
    }

    public String chkTitle() throws ErrMsgException {
        String title = fileUpload.getFieldValue("title");
        if (title.equals("")) {
            log("标题必须填写！");
        }
        Config cfg = new Config();
        int minLen = cfg.getInt("shortMsgTitleLengthMin");
        int maxLen = cfg.getInt("shortMsgTitleLengthMax");
        if (title.trim().length() > maxLen || title.trim().length() < minLen) {
            throw new ErrMsgException("短消息标题字数大于" + minLen + "小于" + maxLen);
        }
        if (!SQLFilter.isValidSqlParam(title)) {
            log("请勿使用' ; 等字符！");
        }
        md.title = title;
        return title;
    }

    public String chkDocTitle() throws ErrMsgException {
        String title = fileUpload.getFieldValue("title");
        if (title.equals("")) {
            log("标题必须填写！");
        }
        md.title = title;
        return title;
    }

    public String chkContent() throws ErrMsgException{
        String content = fileUpload.getFieldValue("content");
        // if (content.equals("")) {
        //    log("内容必须填写！");
        // }
        //if (!SQLFilter.isValidSqlParam(content)) {
       //     log("请勿使用' ; 等字符！");
       // }
        Config cfg = new Config();
        int minLen = cfg.getInt("shortMsgContentLengthMin");
        int maxLen = cfg.getInt("shortMsgContentLengthMax");
        if (content.trim().length() > maxLen || content.trim().length() < minLen) {
            throw new ErrMsgException("短消息内容字数大于" + minLen + "小于" + maxLen);
        }
        md.content = content;
        return content;
    }

    public String chkDocContent() throws ErrMsgException{
        String content = fileUpload.getFieldValue("content");
        if (content.equals(""))
            content = " ";
        md.content = content;
        return content;
    }

    public String chkDirCode() {
        String dirCode = fileUpload.getFieldValue("dirCode");
        md.setDirCode(dirCode);
        return dirCode;
    }

    public String checkIp() {
        String ip = request.getRemoteAddr();
        md.ip = ip;
        return ip;
    }

    public int chkId() {
        String strId = fileUpload.getFieldValue("id");
        int id = StrUtil.toInt(strId, -1);
        if (id==-1)
            log("标识非法");
        else {
            md.setId(id);
        }
        return id;
    }

    public boolean checkCreate() throws ErrMsgException {
        init();
        doUpload();
        chkTitle();
        chkContent();
        chkIp();
        report();
        return true;
    }

    public boolean checkAddDoc() throws ErrMsgException {
        init();
        doUpload();
        chkDocTitle();
        chkDocContent();
        chkDirCode();
        chkIp();
        report();
        return true;
    }

    public boolean checkModifyDoc() throws ErrMsgException {
        init();
        doUpload();
        chkDocTitle();
        chkDocContent();
        chkDirCode();
        chkId();
        chkIp();
        report();
        return true;
    }

    public boolean checkTransmit() throws ErrMsgException {
        init();
        doUpload();
        /**
        md.setTitle(ParamUtil.get(request, "title"));
        md.setContent(ParamUtil.get(request, "content"));
        chkIp();
        md.receiver = ParamUtil.get(request, "receiver");
        md.setDraft(false);
        */
       chkId();
       md.setTitle(fileUpload.getFieldValue("title"));
       md.setContent(fileUpload.getFieldValue("content"));
       chkIp();

       report();
       return true;
    }

    public FileUpload getFileUpload() {
        return this.fileUpload;
    }

    public IdiofilearkDb getMsgDb() {
        return md;
    }
}
