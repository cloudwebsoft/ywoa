package com.redmoon.oa.message;

import cn.js.fan.base.AbstractForm;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.db.SQLFilter;
import com.cloudwebsoft.framework.util.IPUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import javax.servlet.ServletContext;
import com.redmoon.oa.Config;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.DateUtil;
import java.util.Date;
import java.util.Calendar;

public class MessageForm extends AbstractForm {
    MessageDb md;
    HttpServletRequest request;
    ServletContext application;

    public FileUpload fileUpload;

    public MessageForm() {
    }

    public MessageForm(ServletContext application, HttpServletRequest request,
                       MessageDb md) {
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
            if (ret != FileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " +
                                          fileUpload.getErrMessage());
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public String chkIp() {
        md.ip = IPUtil.getRemoteAddr(request);
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
        md.title = title;
        return title;
    }

    public String chkContent() throws ErrMsgException{
        String content = StrUtil.getNullStr(fileUpload.getFieldValue("content"));
        // if (content.equals("")) {
        //    log("内容必须填写！");
        // }
        Config cfg = new Config();
        int minLen = cfg.getInt("shortMsgContentLengthMin");
        int maxLen = cfg.getInt("shortMsgContentLengthMax");
        if (content.trim().length() > maxLen || content.trim().length() < minLen) {
            throw new ErrMsgException("短消息内容字数须大于" + minLen + " 小于" + maxLen);
        }
        md.content = content;
        return content;
    }

    public String chkReceiver() {
        String receiver = fileUpload.getFieldValue("receiver");
        String receiver1 = fileUpload.getFieldValue("receiver1");
        String receiver2 = fileUpload.getFieldValue("receiver2");
        if (receiver.equals("")) {
            log("接收者必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(receiver)) {
            log("请勿使用' ; 等字符！");
        }
        // receiver 为用 , 分隔的字符串
        // UserDb user = new UserDb();
        //user = user.getUserDb(receiver);
        //if (!user.isLoaded()) {
        //    log("接收者" + receiver + "不存在!");
        // }
        md.receiversjs = receiver;
        if(receiver1 != null && !"".equals(receiver1)&&!"".equals(receiver)){
        	receiver += "," + receiver1;
        }
        if(receiver2 != null && !"".equals(receiver2)&&!"".equals(receiver)){
        	receiver += "," + receiver2;
        }
        md.receiver = receiver;
        md.receiversAll = receiver;
        
        return receiver;
    }
    
    public String chkReceiver2() {
        String receiver2 = fileUpload.getFieldValue("receiver2");
        if (!SQLFilter.isValidSqlParam(receiver2)) {
            log("请勿使用' ; 等字符！");
        }
        md.receiversms = receiver2;
        
        return receiver2;
    }
    
    public String chkReceiver1() {
        String receiver1 = fileUpload.getFieldValue("receiver1");
        if (!SQLFilter.isValidSqlParam(receiver1)) {
            log("请勿使用' ; 等字符！");
        }
        
        md.receiverscs = receiver1;
        
        return receiver1;
    }

    public String checkIp() {
        String ip = IPUtil.getRemoteAddr(request);
        md.ip = ip;
        return ip;
    }

    public int chkType() {
        String type = fileUpload.getFieldValue("type");
        if (type == null || type.equals("")) {
            type = "0";
        }
        md.type = Integer.parseInt(type);
        return md.type;
    }

    public boolean chkIsDraft() {
        String strDraft = fileUpload.getFieldValue("isDraft");
        if (strDraft == null) {
            md.setBox(MessageDb.OUTBOX);
            return false;
        }
        if (strDraft.equals("true")) {
            md.setBox(MessageDb.DRAFT);
            return true;
        } else {
            md.setBox(MessageDb.INBOX);
            return false;
        }
    }
    
    public boolean chkIsToOutBox() {
        String isToOutBox = fileUpload.getFieldValue("isToOutBox");
        if (isToOutBox == null) {
            md.isToOutBox = false;
            return false;
        }
        if (isToOutBox.equals("true")) {
            md.isToOutBox = true;
            return true;
        } else {
            md.isToOutBox = false;
            return false;
        }
    }
    
    public int chkIsSent() {
    	String sendNow = StrUtil.getNullString(fileUpload.getFieldValue("send_now"));
    	if (sendNow.equals("no")) {
            md.isSent = 0;
        } else {
        	md.isSent = 1;
        }
    	return md.isSent;
    }
    
    public String chkSendTime() throws ErrMsgException {
    	String sendNow = StrUtil.getNullString(fileUpload.getFieldValue("send_now"));
    	String sendTime = StrUtil.getNullString(fileUpload.getFieldValue("send_time"));
    	if(sendNow.equals("no")) {
    		if(!sendTime.equals("")) {
    			try {
        			Date d = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm:ss");
        		} catch(Exception e) {
        			throw new ErrMsgException("日期格式非法！");
        		}
        		md.sendTime = sendTime;
        		return sendTime;
    		} else {
    			throw new ErrMsgException("请选择定时发送日期！");
    		}
    	} else {
    		Calendar now = Calendar.getInstance();
    		sendTime = StrUtil.FormatDate(now.getTime(), "yyyy-MM-dd HH:mm:ss");
    		md.sendTime = sendTime;
    		return sendTime;
    	}
    }
    
    public String chkRq() throws ErrMsgException {
    	String sendNow = StrUtil.getNullString(fileUpload.getFieldValue("send_now"));
    	String sendTime = StrUtil.getNullString(fileUpload.getFieldValue("send_time"));
    	if(sendNow.equals("no")) {
    		if(!sendTime.equals("")) {
    			try {
        			Date d = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm:ss");
        		} catch(Exception e) {
        			throw new ErrMsgException("日期格式非法！");
        		}
        		md.rq = sendTime;
        		return md.rq ;
    		} else {
    			throw new ErrMsgException("请选择定时发送日期！");
    		}
    	} else {
    		Calendar now = Calendar.getInstance();
    		md.rq  = StrUtil.FormatDate(now.getTime(), "yyyy-MM-dd HH:mm:ss");
    		return md.rq ;
    	}
    }
    
    public int chkReceiptState() throws ErrMsgException {
    	String receiptState = StrUtil.getNullString(fileUpload.getFieldValue("receipt_state"));
    	md.receiptState = StrUtil.toInt(receiptState, MessageDb.DO_NOT_NEED_RECEIPT);
    	return md.receiptState;
    }
    
    public int chkMsgLevel() throws ErrMsgException {
    	String msgLevel = StrUtil.getNullString(fileUpload.getFieldValue("msg_level"));
    	md.msgLevel = StrUtil.toInt(msgLevel, MessageDb.MSG_LEVEL_NORMAL);
    	return md.msgLevel;
    }
    
    public boolean checkCreate() throws ErrMsgException {
        init();
        doUpload();
        chkTitle();
        chkContent();
        chkReceiver();
        chkReceiver1();
        chkReceiver2();
        chkIp();
        chkIsDraft();
        chkIsToOutBox();
        chkIsSent();
        chkSendTime();
        chkRq();
        chkReceiptState();
        chkMsgLevel();
        report();
        return true;
    }

    public boolean checkTransmit() throws ErrMsgException {
        init();
        String receiverjs1 = ParamUtil.get(request, "receiver");
        String receivercs1 = ParamUtil.get(request, "receiver1");
        String receiverms1 = ParamUtil.get(request, "receiver2");
        String receiversAll = receiverjs1;
        if(!"".equals(receivercs1)){
        	receiversAll += "," + receivercs1;
        }
        if(!"".equals(receiverms1)){
        	receiversAll += "," + receiverms1;
        }
        md.setTitle(ParamUtil.get(request, "title"));
        md.setContent(ParamUtil.get(request, "content"));
        chkIp();
        md.receiver = receiversAll;
        //md.receiversAll = md.receiver;
        md.receiversAll = receiversAll;
        md.setBox(MessageDb.INBOX);
        md.isToOutBox = ParamUtil.get(request, "isToOutBox").equals("true");
        md.isSent = ParamUtil.get(request, "send_now").equals("yes") ? 1 : 0;
        md.sendTime = StrUtil.FormatDate(Calendar.getInstance().getTime(), "yyyy-MM-dd HH:mm:ss");
        md.receiptState = ParamUtil.getInt(request, "receipt_state", MessageDb.DO_NOT_NEED_RECEIPT);
        md.msgLevel = ParamUtil.getInt(request, "msg_level", MessageDb.MSG_LEVEL_NORMAL);
        md.receiverscs = receivercs1;
        md.receiversms = receiverms1;
        md.receiversjs = receiverjs1;
        report();
        return true;
    }

    public FileUpload getFileUpload() {
        return this.fileUpload;
    }
}
