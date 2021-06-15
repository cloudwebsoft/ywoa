package com.redmoon.oa.emailpop3;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.db.SQLFilter;
import com.redmoon.oa.person.UserDb;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import java.util.Date;

import cn.js.fan.web.Global;
import javax.servlet.ServletContext;
import cn.js.fan.util.StrUtil;

public class MailMsgForm extends AbstractForm {
    MailMsgDb md;
    HttpServletRequest request;
    ServletContext application;

    public FileUpload fileUpload;

    public MailMsgForm() {
    }

    public MailMsgForm(ServletContext application, HttpServletRequest request, MailMsgDb md) {
        this.application = application;
        this.request = request;
        this.md = md;
    }

    public FileUpload doUpload() throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        // String[] extnames = {"jpg", "gif", "png"};
        // fileUpload.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String chkSubject() {
        String subject = fileUpload.getFieldValue("subject");
        /**if (subject.equals("")) {
            log("标题必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(subject))
            log("请勿使用' ; 等字符！");
            */
        md.subject = subject;
        return subject;
    }
    
    public Date chkSendTime() {
    	String sendTime = fileUpload.getFieldValue("send_time");
    	java.util.Date sendDate = null;
    	if(sendTime.equals("")){
    		sendDate = null;
    	}else{
    		sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
    	}
    	
    	md.sendTime = sendDate;
    	return sendDate;
    }
    
    
    public int chkReceiptState() {
    	String receiptState = fileUpload.getFieldValue("receipt_state");
    	int i = 0;
    	if(receiptState.equals("")){
    		i = 0;
    	}else{
    		i = Integer.valueOf(receiptState);
    	}
    	
    	md.receiptState = i;
    	return i;
    }
    
    public int chkMsgLevel() {
    	String msgLevel = fileUpload.getFieldValue("msg_level");
    	int i = 0;
    	if(msgLevel.equals("")){
    		i = 0;
    	}else{
    		i = Integer.valueOf(msgLevel);
    	}
    	
    	md.msgLevel = i;
    	return i;
    }
  

    public String chkContent() {
        String content = fileUpload.getFieldValue("content");
       /** if (content.equals("")) {
            log("内容必须填写！");
        }*/
        // if (!SQLFilter.isValidSqlParam(content))
        //     log("请勿使用' ; 等字符！");
        md.content = content;
        return content;
    }

    public int chkId() {
        String strid = fileUpload.getFieldValue("id");
        if (!StrUtil.isNumeric(strid))
            log("id必须为数字！");

        int id = Integer.parseInt(strid);
        md.id = id;
        return id;
    }

    public String chkTo() {
        String to = fileUpload.getFieldValue("to");
        /**if (to.equals("")) {
            log("接收者必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(to))
            log("请勿使用' ; 等字符！");
            */
        md.receiver = to;
        return to;
    }

    public String chkFrom() {
        String from = fileUpload.getFieldValue("email");
        // System.out.println("from=" + from);
        if (from.equals("")) {
            log("发送者必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(from))
            log("请勿使用' ; 等字符！");
        md.sender = from;
        md.setEmailAddr(from);
        return from;
    }

    public int chkType() {
        String strtype = fileUpload.getFieldValue("type");
        int type = md.TYPE_DRAFT;
        if (strtype == null || strtype.equals(""))
            md.type = type;
        else
            md.type = Integer.parseInt(strtype);
        return md.type;
    }
    
    public String chkCopyReceiver() {
        String cc = fileUpload.getFieldValue("cc");
        
        md.copyReceiver = cc;
        return cc;
    }
    
    public String chkBlindReceiver() {
    	String bcc = fileUpload.getFieldValue("bcc");
    	
    	md.blindReceiver = bcc;
    	return bcc;
    }

    public boolean checkCreate() throws ErrMsgException {
        init();
        doUpload();
        chkTo();
        chkFrom();
        chkSubject();
        chkSendTime();
        chkMsgLevel();
        chkReceiptState();
        chkContent();
        report();
        return true;
    }

    public boolean checkModify() throws ErrMsgException {
        init();
        doUpload();
        chkId();
        chkTo();
        chkFrom();
        chkSubject();
        chkSendTime();
        chkMsgLevel();
        chkCopyReceiver();
        chkBlindReceiver();
        chkReceiptState();
        chkContent();
        report();
        return true;
    }
}
