package com.redmoon.oa.emailpop3;

// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi
// Source File Name:   FJMailBean.java

import java.io.*;
import java.io.File;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Properties;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import cn.js.fan.mail.*;
import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.*;

import com.redmoon.kit.util.*;
import com.redmoon.oa.Config;
import com.redmoon.oa.pvg.*;
import com.sun.org.apache.bcel.internal.generic.RETURN;

import jxl.*;
import jxl.read.biff.*;
import sun.misc.*;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

// Referenced classes of package fjmail:
//            PopupAuthenticator

public class SendMail {
    public String host;
    public String mailFooter;
    public String mailFooterHTML;
    boolean sessionDebug;
    Message msg;
    Multipart multipart;
    Session session;
    PopupAuthenticator popAuthenticator;
    String username;
    String password;
    String errinfo;
    String port = "25";
    private String charset;

    String tempAttachFilePath;
    int emailId = 0;

    FileUpload fu = new FileUpload();
    
	final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

    public void setmailFooter(String s) throws Exception {
        mailFooter = s;
    }

    public void setmailFooterHTML(String s) throws Exception {
        mailFooterHTML = s;
    }

    public void initSession(String s) throws Exception {
        host = s;
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.transport.protocol", "smtp");
        session = Session.getDefaultInstance(properties, null);
        session.setDebug(sessionDebug);
        msg = new MimeMessage(session);
        msg.setSentDate(new Date());
        multipart = new MimeMultipart();
        msg.setContent(multipart);
    }

    public void initSession(String s, String port, String s1, String s2, boolean isSsl) throws
            Exception {
        this.port = port;
        host = s;
        username = s1;
        password = s2;
        Properties properties = System.getProperties();
        
        if (!isSsl) {
	        properties.put("mail.host", host);
	        properties.put("mail.transport.protocol", "smtp");
	        properties.put("mail.smtp.auth", "true");
	        properties.put("mail.smtp.port", port);
        }
        else {
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());        
	        properties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
	        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
	        properties.put("mail.smtp.auth", "true");
	        properties.setProperty("mail.smtp.host", host);
	
	        properties.setProperty("mail.smtp.port", port);
	        properties.setProperty("mail.smtp.socketFactory.port", port);   
        }

        PopupAuthenticator popupauthenticator = new PopupAuthenticator();
        popupauthenticator.init(username, password);
        session = Session.getInstance(properties, popupauthenticator);
        session.setDebug(sessionDebug);
        msg = new MimeMessage(session);
        msg.setSentDate(new Date());
        multipart = new MimeMultipart();
        msg.setContent(multipart);
    }

    public void initSession(String s, String s1, String s2) throws Exception {
        host = s;
        username = s1;
        password = s2;
        Properties properties = System.getProperties();
        properties.put("mail.host", host);
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");

        PopupAuthenticator popupauthenticator = new PopupAuthenticator();
        popupauthenticator.init(username, password);
        session = Session.getInstance(properties, popupauthenticator);
        session.setDebug(sessionDebug);
        msg = new MimeMessage(session);
        msg.setSentDate(new Date());
        multipart = new MimeMultipart();
        msg.setContent(multipart);
    }

    public SendMail() {
        host = "fserver";
        mailFooter = ""; // "\n\n\n===========此邮件由腾图OA发送========\n\n";
        mailFooterHTML = ""; // "<br><br><br>===========此邮件由腾图OA发送========<br><br>";
        
        // 判断系统编码
        charset = System.getProperty("sun.jnu.encoding");
		if (charset == null || charset.equals("")) {
			String os = System.getProperty("os.name");
			if (os.toLowerCase().startsWith("linux")) {
				charset = "UTF-8";
			}
		}
		if (!charset.toLowerCase().startsWith("gb")) {
			charset = "UTF-8";
		}
    }

    public void initMsg(String s, String s1, String s2, String s3, boolean flag) throws
            Exception {
    	setSendTo(s);
        setFrom(s1);
        setSubject(s2);
        setBody(s3, flag);
    }

    /**
     * 初始化信息
     * @param as
     * @param s
     * @param s1
     * @param s2
     * @param flag 是否用HTML的方式发信
     * @throws java.lang.Exception
     */
    public void initMsg(String as[], String s, String s1, String s2,
                        boolean flag) throws Exception {
        setSendTo(as);
        setFrom(s);
        setSubject(s1);
        setBody(s2, flag);
    }

    /**
     * 初始化邮件信息
     * @param to String
     * @param subject String
     * @param content String
     * @param flag boolean 是否用HTML的方式发信
     * @throws Exception
     */
    public void initMsg(String to, String subject, String content, boolean flag) throws
            Exception {
        setSendTo(to);
        setSubject(subject);
        setBody(content, flag);
    }
    
    /**
     * 初始化邮件信息
     * @param to String
     * @param subject String
     * @param content String
     * @param flag boolean 是否用HTML的方式发信
     * @throws Exception
     */
    public void initMsgCopy(String to[],String copyTo[],String blindTo[], String subject, String content, boolean flag,String receipt_state,String from,String msg_level) throws
    Exception {
    	setSendTo(to);
    	if(copyTo != null){
    		setCopyTo(copyTo);
    	}
    	if(blindTo != null){
    		setBlindTo(blindTo);
    	}
    	if(!receipt_state.equals("")){
    		setReplySign(from);
    	}
    	if(!msg_level.equals("")){
    		setMsgLevel(msg_level);
    		
    	}
    	setSubject(subject);
    	setBody(content, flag);
    }

    //设置回执人员
    public void setReplySign(String from) throws MessagingException{
    	msg.setHeader("Disposition-Notification-To", from);
    }
    //设置紧急级别 1:紧急   3:普通    5:低
    public void setMsgLevel(String msg_level) throws MessagingException{
    	msg.setHeader("X-Priority", "1");
    }
    
    public void setFrom(String s) throws Exception {
        msg.setFrom(new InternetAddress(s));
    }

    void setSendTo(String as[]) throws Exception {
    	InternetAddress[] ainternetaddress = new InternetAddress[as.length];
        for (int i = 0; i < as.length; i++){
           // setSendTo(as[i]);
        	ainternetaddress[i] = new InternetAddress(String.valueOf(as[i]));
        }
        msg.setRecipients(javax.mail.Message.RecipientType.TO, ainternetaddress);

    }

    void setSendTo(String s) throws MessagingException {
        InternetAddress ainternetaddress[] = {
                                             new InternetAddress(s)
        };
        msg.setRecipients(javax.mail.Message.RecipientType.TO, ainternetaddress);
    }

    void setCopyTo(String as[]) throws Exception {
    	InternetAddress[] ainternetaddress = new InternetAddress[as.length];
        for (int i = 0; i < as.length; i++){
            //setCopyTo(as[i]);
        	ainternetaddress[i] = new InternetAddress(String.valueOf(as[i]));
        }
        msg.setRecipients(javax.mail.Message.RecipientType.CC, ainternetaddress);
    }

    void setCopyTo(String s) throws Exception {
        InternetAddress ainternetaddress[] = {
                                             new InternetAddress(s)
        };
        msg.setRecipients(javax.mail.Message.RecipientType.CC, ainternetaddress);
    }

  //密送
    void setBlindTo(String as[]) throws Exception {
    	InternetAddress[] ainternetaddress = new InternetAddress[as.length];
        for (int i = 0; i < as.length; i++){
        	//setBlindTo(as[i]);
        	ainternetaddress[i] = new InternetAddress(String.valueOf(as[i]));
        }
        msg.setRecipients(javax.mail.Message.RecipientType.BCC, ainternetaddress);
    }
    
    void setBlindTo(String s) throws Exception {
        InternetAddress ainternetaddress[] = {
                                             new InternetAddress(s)
        };
        msg.setRecipients(javax.mail.Message.RecipientType.BCC, ainternetaddress);
    }
    //JavaMail中的邮件主题需要进行BASE64编码，格式形如：
    //=?GB2312?B?xPq1xMPcwuvS0b6t1tjWw6Osx+u+ob/stcfCvKOssqLQ3rjEw9zC66Oh?=
    //所以，直接使用msg.setSubject("中文主题")，或者msg.setSubject("中文主题".getBytes("8859_1"), "GB2312"))都一样会出现乱码。在设置邮件主题前需要将主题字串的字节编码为BASE64格式，并添加编码头
    void setSubject(String s) throws MessagingException, UnsupportedEncodingException {
        //BASE64Encoder base64encoder = new BASE64Encoder();
        //msg.setSubject(MimeUtility.encodeText(s, "UTF-8", "B"));
        //msg.setSubject("=?GB2312?B?" + base64encoder.encode(s.getBytes()) +
                    //   "?=");
        msg.setSubject( MimeUtility.encodeText( s, charset, "B"));
    }

    /**
     * 置正文类型
     * @param s
     * @param flag 如果为真，则表示用HTML的方式
     * @throws java.lang.Exception
     */
    void setBody(String s, boolean flag) throws MessagingException {
        MimeBodyPart mimebodypart = new MimeBodyPart();
        if (flag) {
//            mimebodypart.setContent(StrUtil.GBToUnicode(s + mailFooterHTML),
//                                    "text/html");
        	mimebodypart.setContent(s + mailFooterHTML, "text/html; charset=UTF-8");
        } else {
            mimebodypart.setText(s + mailFooter);
        }
        multipart.addBodyPart(mimebodypart);
    }

    public void setAttachFile(String s) throws Exception {
        MimeBodyPart mimebodypart = new MimeBodyPart();
        FileDataSource filedatasource = new FileDataSource(s);

        mimebodypart.setDataHandler(new DataHandler(filedatasource));
        // mimebodypart.setFileName(StrUtil.GBToUnicode(filedatasource.getName()));

        mimebodypart.setFileName(MimeUtility.encodeText(filedatasource.getName(), charset, "B"));

        multipart.addBodyPart(mimebodypart);
    }

    public void setAttachFile(String filePath, String fileName) throws Exception {
        MimeBodyPart mimebodypart = new MimeBodyPart();
        FileDataSource filedatasource = new FileDataSource(filePath);
        mimebodypart.setDataHandler(new DataHandler(filedatasource));

        // mimebodypart.setFileName(StrUtil.GBToUnicode(fileName)); // 会出现乱码

        /*
        // 会出现乱码
        BASE64Encoder enc = new BASE64Encoder();
        String fn = "=?GBK?B?"+enc.encode(fileName.getBytes("ISO-8859-1"))+"?=";
        mimebodypart.setFileName(fn);
        */

        mimebodypart.setFileName(MimeUtility.encodeText(fileName, charset, "B"));

        multipart.addBodyPart(mimebodypart);
    }

    public void setAttachFile(String as[]) throws Exception {
        for (int i = 0; i < as.length; i++)
            setAttachFile(as[i]);
    }

    public boolean send() throws ErrMsgException {
        boolean re = send(msg);
        boolean isSaveToSendBox = StrUtil.getNullStr(fu.getFieldValue(
                "isSaveToSendBox")).equals("true");
        if (re && isSaveToSendBox) {
            // 存至发件箱
            try {
                MailMsgDb mmd = new MailMsgDb();

                String to = StrUtil.getNullString(fu.getFieldValue("to"));
                String copyReceiver = StrUtil.getNullStr(fu.getFieldValue("cc"));
                String blindReceiver = StrUtil.getNullStr(fu.getFieldValue("bcc"));
                String username = StrUtil.getNullString(fu.getFieldValue("username"));
                String subject = StrUtil.getNullString(fu.getFieldValue("subject"));
                String content = StrUtil.getNullString(fu.getFieldValue("content"));
                String email = StrUtil.getNullString(fu.getFieldValue("email"));
                String send_time = StrUtil.getNullStr(fu.getFieldValue("send_time"));
                String receipt_state = StrUtil.getNullStr(fu.getFieldValue("receipt_state"));
                String msg_level = StrUtil.getNullStr(fu.getFieldValue("msg_level"));

                to = to.replaceAll("，", ",");
                String toAry[] = to.split(","); //接收者
                
                EmailAddrDb emailAddrDb = null;
                
                for(int i=0;i<toAry.length;i++){
                	emailAddrDb = new EmailAddrDb();
                	emailAddrDb = emailAddrDb.getEmailAddrDb(toAry[i], username);
                	if(emailAddrDb == null){//当为空的时候，将接收人的邮箱地址保存到数据库
                		emailAddrDb = new EmailAddrDb();
                		emailAddrDb.setEmailAddr(toAry[i]);
                    	emailAddrDb.setUserName(username);
                    	emailAddrDb.setAddDate(new Date());
                    	emailAddrDb.setDelete(false);
                    	
                    	emailAddrDb.create();
                	}else{//当不空的时候，更新接收人的邮箱地址
                		emailAddrDb.setAddDate(new Date());
                		try {
							emailAddrDb.save();
						} catch (ResKeyException e) {
                            LogUtil.getLog(getClass()).error(e);
						}
                	}
                	
                }
                
                String[] copySendAry = null;
                if(!copyReceiver.equals("")){
                	copyReceiver = copyReceiver.replaceAll("，", ",");
                	copySendAry = copyReceiver.split(","); //抄送者
                	
                	 for(int i=0;i<copySendAry.length;i++){
                		 emailAddrDb = new EmailAddrDb();
                		emailAddrDb = emailAddrDb.getEmailAddrDb(copySendAry[i], username);
                     	if(emailAddrDb == null){//当为空的时候，将抄送者的邮箱地址保存到数据
                     		emailAddrDb = new EmailAddrDb();
                     		emailAddrDb.setEmailAddr(copySendAry[i]);
                         	emailAddrDb.setUserName(username);
                         	emailAddrDb.setAddDate(new Date());
                         	emailAddrDb.setDelete(false);
                         	
                         	emailAddrDb.create();
                     	}else{//当不为空的时候，更新抄送者的邮箱地址
                     		emailAddrDb.setAddDate(new Date());
                     		try {
     							emailAddrDb.save();
     						} catch (ResKeyException e) {
                                LogUtil.getLog(getClass()).error(e);
     						}
                     	}
                     }
                	
                }
                String[] blindSendAry = null;
                if(!blindReceiver.equals("")){
                	blindReceiver = blindReceiver.replaceAll("，", ",");
                	blindSendAry = blindReceiver.split(","); //密送者
                	for(int i=0;i<blindSendAry.length;i++){
                		emailAddrDb = new EmailAddrDb();
                		emailAddrDb = emailAddrDb.getEmailAddrDb(blindSendAry[i], username);
                     	if(emailAddrDb == null){//当为空的时候，将密送者的邮箱地址保存到数据库
                     		emailAddrDb = new EmailAddrDb();
                     		emailAddrDb.setEmailAddr(blindSendAry[i]);
                         	emailAddrDb.setUserName(username);
                         	emailAddrDb.setAddDate(new Date());
                         	emailAddrDb.setDelete(false);
                         	
                         	emailAddrDb.create();
                     	}else{//当不为空的时候，更新密送者的邮箱地址
                     		emailAddrDb.setAddDate(new Date());
                     		try {
     							emailAddrDb.save();
     						} catch (ResKeyException e) {
                                LogUtil.getLog(getClass()).error(e);
     						}
                     	}
                     }
                }
                
                
                mmd.setSubject(subject);
                mmd.setContent(content);
                mmd.setReceiver(to);
                mmd.setSender(email);
                mmd.setType(MailMsgDb.TYPE_SENDED);
                mmd.setPerson(username);
                mmd.setHtml(true);
                mmd.setEmailAddr(email);
                mmd.setCopyReceiver(copyReceiver);
                mmd.setBlindReceiver(blindReceiver);
                if(send_time.equals("")){
                	mmd.setSendTime(null);
                }else{
                	java.util.Date sendDate = DateUtil.parse(send_time, "yyyy-MM-dd HH:mm");
                	mmd.setSendTime(sendDate);
                }
                mmd.setReceiptState(receipt_state.equals("")? 0 : 1);
                mmd.setMsgLevel(msg_level.equals("") ? 0 : 1);

                emailId = mmd.createEmail(fu);
                
                
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        return re;
    }

    public boolean send(Message msg){
        try {
            Transport.send(msg);
        }
        catch (SendFailedException sendfailedexception) {
            LogUtil.getLog(getClass()).error("send1:" + sendfailedexception.getMessage());
            errinfo = sendfailedexception.getMessage();
            return false;
        }
        catch (MessagingException e) {
            // LogUtil.getLog(getClass()).error("send2:" + e.getMessage());
            LogUtil.getLog(getClass()).error("send2:" + StrUtil.trace(e));
            errinfo = e.getMessage();
            return false;
        }
        return true;
    }

    public String geterrinfo() {
    	
        return errinfo;
    }

    public void sendMailGroup(ServletContext application,
                            HttpServletRequest request, JspWriter out) throws MessagingException, IOException, ErrMsgException {
        //设置文件路径为公用路径CONFIG_CWS.XML
    	//tempAttachFilePath = application.getRealPath("/") + "upfile/Attach/";
        tempAttachFilePath = Global.getRealPath() + "upfile/Attach/";

        FileUpload mfu = new FileUpload();
        mfu.setSavePath(tempAttachFilePath); // 取得目录

        try {
            if (mfu.doUpload(application, request) != FileUpload.RET_SUCCESS) {
                out.print("getMailInfo1:" + mfu.getErrMessage());
                return;
            }
        } catch (IOException e) {
            out.print("getMailInfo2:" + e.getMessage());
        }
        // 取得表单中域的信息
        String to = StrUtil.getNullString(mfu.getFieldValue("to"));
        String subject = StrUtil.getNullString(mfu.getFieldValue("subject"));
        String content = StrUtil.getNullString(mfu.getFieldValue("content"));
        String email = StrUtil.getNullString(mfu.getFieldValue("email"));
        String username = StrUtil.getNullString(mfu.getFieldValue("username"));

        // 检查email是否被注入
        UserPop3Setup userpop3setup = new UserPop3Setup();
        boolean isExist = userpop3setup.getUserPop3Setup(username, email);
        if (!isExist) {
        	com.redmoon.oa.LogUtil.log(username, StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SMTP MX 注入 email=" + email);
        	// throw new ErrMsgException(SkinUtil.LoadString(request, "op_invalid"));
        	out.print(SkinUtil.LoadString(request, "op_invalid"));
        	return;
        }
        
        String mailserver = userpop3setup.getMailServer();
        int smtp_port = userpop3setup.getSmtpPort();
        String email_name = userpop3setup.getUser();
        String email_pwd_raw = userpop3setup.getPwd();
        email_pwd_raw = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", email_pwd_raw);

        try {
            initSession(mailserver, "" + smtp_port, email_name, email_pwd_raw, userpop3setup.isSsl());
            setFrom(email);
        } catch (Exception e) {
            errinfo = e.getMessage();
            out.print(errinfo);
            return;
        }

        // 处理附件
        mfu.writeFile(true); // 用随机方式命名文件，如果不这样会使得覆盖别人的文件，或者自己的同名文件，但是恰恰session到期，而被删除

        // Add a listener for when this session becomes invalid.
        // We will want to delete the image file at this time
        CleanUp cln = null; // Clean up the image file
        HttpSession session = null;
        session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
        }
        String strlc = (String) session.getAttribute("lc"); //listenercount
        if (strlc == null) {
            strlc = "0";
        }
        int lc = Integer.parseInt(strlc);

        String excelFilePath = null;
        java.util.Iterator ir = mfu.getFiles().iterator();
        while (ir.hasNext()) {
            lc++;
            FileInfo fi = (FileInfo) ir.next();
            cln = new CleanUp(tempAttachFilePath + fi.diskName);
            session.setAttribute("bindings.listener" + lc, cln);

            if ("excelFile".equals(fi.getFieldName())) {
                excelFilePath = tempAttachFilePath + fi.diskName;
                continue;
            }
            try {
                setAttachFile(tempAttachFilePath + fi.diskName, fi.name);
            } catch (Exception e2) {
                LogUtil.getLog(getClass()).error("getMailInfo setAttachFile:" + e2.getMessage());
            }
        }

        setSubject(subject);

        String type = StrUtil.getNullStr(mfu.getFieldValue("type"));
        if ("input".equals(type) || "".equals(type)) {
            setBody(content, true);

            to = to.replaceAll("，", ",");
            String[] ary = StrUtil.split(to, ",");
            if (ary==null) {
                out.print("Email地址不能为空！");
                return;
            }
            int len = ary.length;
            for (int i=0; i<len; i++) {
                setSendTo(ary[i].trim());
                if (send()) {
                    out.print("<div>发送至" + ary[i] + "成功！</div>");
                } else {
                    out.print("<div>发送至" + ary[i] + "失败:" + errinfo + "</div>");
                }
            }
        }
        else if ("excel_addr".equals(type)) {
            setBody(content, true);

            if (excelFilePath == null) {
                out.print("请选择Excel文件！");
                return;
            }

            try {
                Workbook book = Workbook.getWorkbook(new java.io.File(
                        excelFilePath));
                // 获取sheet表的总行数、总列数
                jxl.Sheet rs = book.getSheet(0);
                int rsRows = rs.getRows();
                int rsColumns = rs.getColumns();
                Cell cc;
                String strc[] = new String[rsColumns];
                for (int i = 0; i < rsRows; i++) {
                    for (int j = 0; j < rsColumns; j++) {
                        cc = rs.getCell(j, i);
                        strc[j] = cc.getContents().trim();
                    }
                    String addr = strc[0];
                    try {
                        setSendTo(addr);
                    }
                    catch (AddressException e) {
                        out.print("<div>发送至" + addr + "失败:" + e.getMessage() + "</div>");
                        LogUtil.getLog(getClass()).error(e);
                        continue;
                    }
                    if (send())
                        out.print("<div>发送至" + addr + "成功！</div>");
                    else {
                        out.print("<div>发送至" + addr + "失败:" + errinfo + "</div>");
                    }
                }
            } catch (BiffException | IOException ex) {
                out.print("发送失败：" + ex.getMessage());
                LogUtil.getLog(getClass()).error(ex);
            }
        }
        else if ("excel_addr_content".equals(type)) {
            if (excelFilePath==null) {
                out.print("请选择Excel文件！");
                return;
            }
            try {
                 Workbook book = Workbook.getWorkbook(new java.io.File(
                         excelFilePath));
                 // 获取sheet表的总行数、总列数
                 jxl.Sheet rs = book.getSheet(0);
                 int rsRows = rs.getRows();
                 int rsColumns = rs.getColumns();
                 Cell cc;
                 String strc[] = new String[rsColumns];

                 MimeBodyPart oldBodyPart = null;
                 for (int i = 0; i < rsRows; i++) {
                     for (int j = 0; j < rsColumns; j++) {
                         cc = rs.getCell(j, i);
                         strc[j] = cc.getContents();
                     }
                     String addr = strc[0];
                     String body = StrUtil.toHtml(strc[1]);
                     try {
                         setSendTo(addr);
                     }
                     catch (AddressException e) {
                         out.print("<div>发送至" + addr + "失败:" + e.getMessage() + "</div>");
                         LogUtil.getLog(getClass()).error(e);
                         continue;
                    }

                     if (oldBodyPart!=null)
                         multipart.removeBodyPart(oldBodyPart);
                     MimeBodyPart mimebodypart = new MimeBodyPart();
                     mimebodypart.setContent(StrUtil.GBToUnicode(body),
                                                 "text/html");
                     multipart.addBodyPart(mimebodypart);

                     oldBodyPart = mimebodypart;

                     if (send()) {
                         out.print("<div>发送至" + addr + "成功！</div>");
                     } else {
                         out.print("<div>发送至" + addr + "失败:" + errinfo + "</div>");
                     }
                 }
             } catch (BiffException | IOException ex) {
                 out.print("发送失败：" + ex.getMessage());
                 LogUtil.getLog(getClass()).error(ex);
             }
        }
    }

    /**
     * 取得上传后的邮件信息，包括附件，将其置于cleanup中，以备session消亡时自动删除
     * @param application
     * @param request
     */
    public void getMailInfo(ServletContext application,
                            HttpServletRequest request) {
    	//tempAttachFilePath = application.getRealPath("/") + "upfile/Attach/";
        tempAttachFilePath = Global.getRealPath() + "upfile/Attach/";
       Config config = new Config();
       String emailFileSizeLimit = config.get("email_file_size_limit");
       fu.setMaxFileSize(Integer.valueOf(emailFileSizeLimit)); //设置文件大小
       fu.setSavePath(tempAttachFilePath); //取得目录

        
        int ret = -1;
        try {
        	ret = fu.doUpload(application, request);
            if (ret != FileUpload.RET_SUCCESS ) {
            	if(ret == FileUpload.RET_INVALIDEXT){
            		errinfo = "扩展名非法";
            	}else if(ret == FileUpload.RET_TOOLARGESINGLE || ret == FileUpload.RET_TOOLARGEALL){
            		errinfo = "文件大小不能超过30M";
            	}else if(ret == FileUpload.RET_FAIL){
            		errinfo = "上传失败";
            	}
                return;
            }
        } catch (IOException e) {
        	errinfo = e.getMessage();
        	return;
        }
        // 取得表单中域的信息
        String to = StrUtil.getNullString(fu.getFieldValue("to"));
        String copyTo = StrUtil.getNullString(fu.getFieldValue("cc"));//抄送者
        String blindTo = StrUtil.getNullString(fu.getFieldValue("bcc"));//密送者
        String subject = StrUtil.getNullString(fu.getFieldValue("subject"));
        String content = StrUtil.getNullString(fu.getFieldValue("content"));
        String email = StrUtil.getNullString(fu.getFieldValue("email"));
        String username = StrUtil.getNullString(fu.getFieldValue("username"));
        String receipt_state = StrUtil.getNullString(fu.getFieldValue("receipt_state"));
        String msg_level = StrUtil.getNullString(fu.getFieldValue("msg_level"));

        UserPop3Setup userpop3setup = new UserPop3Setup();
        userpop3setup.getUserPop3Setup(username, email);
        String mailserver = userpop3setup.getMailServer();
        int smtp_port = userpop3setup.getSmtpPort();
        String email_name = userpop3setup.getUser();
        String email_pwd_raw = userpop3setup.getPwd();
        email_pwd_raw = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", email_pwd_raw);

        try {
            initSession(mailserver, "" + smtp_port, email_name, email_pwd_raw, userpop3setup.isSsl());
            setFrom(email);
        } catch (Exception e) {
            errinfo = e.getMessage();
            return;
        }

        try {
        	to = to.replaceAll("，", ",");
            String toAry[] = to.split(","); //接收者
            String[] copySendAry = null;
            if(!copyTo.equals("")){
            	copyTo = copyTo.replaceAll("，", ",");
            	copySendAry = copyTo.split(","); //抄送者
            }
            String[] blindSendAry = null;
            if(!blindTo.equals("")){
            	blindTo = blindTo.replaceAll("，", ",");
            	blindSendAry = blindTo.split(","); //密送者
            }
            initMsgCopy(toAry ,copySendAry, blindSendAry, subject, content, true,receipt_state,email,msg_level);
        } catch (Exception e1) {
            LogUtil.getLog(getClass()).error("SendMail initMsg:" + e1.getMessage());
        }

        // 处理附件
        fu.writeFile(true); // 用随机方式命名文件，如果不这样会使得覆盖别人的文件，或者自己的同名文件，但是恰恰session到期，而被删除

        // Add a listener for when this session becomes invalid.
        // We will want to delete the image file at this time
        CleanUp cln = null; // Clean up the image file
        HttpSession session = null;
        session = request.getSession(false);
        if (session == null)
            session = request.getSession(true);
        String strlc = (String) session.getAttribute("lc"); //listenercount
        if (strlc == null)
            strlc = "0";
        int lc = Integer.parseInt(strlc);
        // The first time through for this user session:
        java.util.Iterator ir = fu.getFiles().iterator();
        while (ir.hasNext()) {
            lc++;
            FileInfo fi = (FileInfo) ir.next();
            cln = new CleanUp(tempAttachFilePath + fi.diskName);
            session.setAttribute("bindings.listener" + lc, cln);
            try {
                setAttachFile(tempAttachFilePath + fi.diskName, fi.name);
            } catch (Exception e2) {
                LogUtil.getLog(getClass()).error("getMailInfo setAttachFile:" + e2.getMessage());
            }
        }

        // 被转发邮件的附件
        String[] attachmentFiles = fu.getFieldValues("attachmentFiles");
        if (attachmentFiles!=null) {
            int len = attachmentFiles.length;
            Attachment att;
            for (int i=0; i<len; i++) {
                int id = StrUtil.toInt(attachmentFiles[i]);
                att = new Attachment(id);
                try {
                    setAttachFile(Global.getRealPath() + att.getVisualPath() +
                                  "/" + att.getDiskName(), att.getName());
                } catch (Exception ex) {
                    LogUtil.getLog(getClass()).error("getMailInfo setAttachFile2:" + ex.getMessage());
                }
            }
        }
    }

    public void getMailInfo(HttpServletRequest request, MailMsgDb mmd) {
        Privilege privilege = new Privilege();
        // 取得表单中域的信息
        String to = mmd.getReceiver();
        String subject = mmd.getSubject();
        String content = mmd.getContent();
        String email = mmd.getSender();
        String copyReceiver = mmd.getCopyReceiver();
        String blindReceiver = mmd.getBlindReceiver();
        String receipt_state = mmd.getReceiptState()==0?"":"1";
        String msg_level = mmd.getMsgLevel()==0?"":"1";
        

        UserPop3Setup userpop3setup = new UserPop3Setup();
        userpop3setup.getUserPop3Setup(privilege.getUser(request), email);
        String mailserver = userpop3setup.getMailServer();
        String smtp_port = "" + userpop3setup.getSmtpPort();
        String email_name = userpop3setup.getUser();
        String email_pwd_raw = userpop3setup.getPwd();
        email_pwd_raw = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", email_pwd_raw);

        try {
            initSession(mailserver, smtp_port, email_name, email_pwd_raw, userpop3setup.isSsl());
            setFrom(email);
        } catch (Exception e) {
            errinfo = e.getMessage();
            return;
        }

        try {
        	
        	to = to.replaceAll("，", ",");
            String toAry[] = to.split(","); //接收者
            
            EmailAddrDb emailAddrDb = null;
            
            for(int i=0;i<toAry.length;i++){
            	emailAddrDb = new EmailAddrDb();
            	emailAddrDb.setEmailAddr(toAry[i]);
            	emailAddrDb.setUserName(privilege.getUser(request));
            	emailAddrDb.setAddDate(new Date());
            	emailAddrDb.setDelete(false);
            	
            	emailAddrDb.create();
            }
            
            String[] copySendAry = null;
            if(!copyReceiver.equals("")){
            	copyReceiver = copyReceiver.replaceAll("，", ",");
            	copySendAry = copyReceiver.split(","); //抄送者
            	
            	 for(int i=0;i<copySendAry.length;i++){
            		emailAddrDb = new EmailAddrDb();
                 	emailAddrDb.setEmailAddr(copySendAry[i]);
                 	emailAddrDb.setUserName(privilege.getUser(request));
                 	emailAddrDb.setAddDate(new Date());
                 	emailAddrDb.setDelete(false);
                 	emailAddrDb.create();
                 }
            	
            }
            String[] blindSendAry = null;
            if(!blindReceiver.equals("")){
            	blindReceiver = blindReceiver.replaceAll("，", ",");
            	blindSendAry = blindReceiver.split(","); //密送者
            	
            	for(int i=0;i<blindSendAry.length;i++){
            		emailAddrDb = new EmailAddrDb();
                 	emailAddrDb.setEmailAddr(blindSendAry[i]);
                 	emailAddrDb.setUserName(privilege.getUser(request));
                 	emailAddrDb.setAddDate(new Date());
                 	emailAddrDb.setDelete(false);
                 	emailAddrDb.create();
                 }
            }
        	
           initMsgCopy(toAry ,copySendAry, blindSendAry, subject, content, true,receipt_state,email,msg_level);
        	
            //initMsg(to, subject, content, true);
        } catch (Exception e1) {
            LogUtil.getLog(getClass()).error("SendMail initMsg:" + e1.getMessage());
        }

        // 处理附件
        java.util.Iterator ir = mmd.getAttachments().iterator();
        while (ir.hasNext()) {
            Attachment att = (Attachment) ir.next();
            try {
                setAttachFile(att.getFullPath(), att.getName());
            } catch (Exception e2) {
                LogUtil.getLog(getClass()).error("getMailInfo1 setAttachFile:" + e2.getMessage());
            }
        }
    }
    
    /**
     * 回执
     * @param request
     * @throws ParseException 
     */
    public void getMailInfo(HttpServletRequest request) throws ParseException {
    	Privilege privilege = new Privilege();
    	
    	String from = ParamUtil.get(request, "from");
    	String to = ParamUtil.get(request, "to");
    	String subject = "已读："+ParamUtil.get(request, "subject");
    	String sendTime = ParamUtil.get(request, "sendTime");
    	SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String content = "这是邮件收条, "+sd.format(sd.parse(sendTime))+"发给"+from+",主题为"+ParamUtil.get(request, "subject")+"的信件已被接收<br/>此收条只表明收件人的计算机上曾显示过此邮件 ";
    	String copyReceiver = "";
    	String blindReceiver = "";
    	String  receipt_state = "";
    	String  msg_level = "";
    	
    	UserPop3Setup userpop3setup = new UserPop3Setup();
    	userpop3setup.getUserPop3Setup(privilege.getUser(request), from);
    	String mailserver = userpop3setup.getMailServer();
    	String smtp_port = "" + userpop3setup.getSmtpPort();
    	String email_name = userpop3setup.getUser();
    	String email_pwd_raw = userpop3setup.getPwd();
    	email_pwd_raw = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", email_pwd_raw);
    	
    	try {
    		initSession(mailserver, smtp_port, email_name, email_pwd_raw, userpop3setup.isSsl());
    		setFrom(from);
    	} catch (Exception e) {
    		errinfo = e.getMessage();
    		return;
    	}
    	
    	try {
    		to = to.replaceAll("，", ",");
    		String toAry[] = to.split(","); //接收者
    		String[] copySendAry = null;
    		if(!copyReceiver.equals("")){
    			copyReceiver = copyReceiver.replaceAll("，", ",");
    			copySendAry = copyReceiver.split(","); //抄送者
    		}
    		String[] blindSendAry = null;
    		if(!blindReceiver.equals("")){
    			blindReceiver = blindReceiver.replaceAll("，", ",");
    			blindSendAry = blindReceiver.split(","); //密送者
    		}
    		
    		initMsgCopy(toAry ,copySendAry, blindSendAry, subject, content, true,receipt_state,from,msg_level);
    		
    		
    	} catch (Exception e1) {
    		LogUtil.getLog(getClass()).error("SendMail initMsg:" + e1.getMessage());
    	}
    
    }

    /**
     *	This class is used to remove the temporary image file when the
     *	client session expires.
     */
    class CleanUp implements HttpSessionBindingListener {
        String m_filename = null; // Image filename

        public CleanUp(String filename) {
            m_filename = filename;
        }

        public void valueBound(HttpSessionBindingEvent e) {
            // The user's session has begun;	m_filename	indicates
            // the name of the image file that will be used for this user's session.
            //LogUtil.getLog(getClass()).info("Bound event: " + e.toString()
            //	 + "\n m_filename: " + m_filename);
        }

        public void valueUnbound(HttpSessionBindingEvent e) {
            //	The user's session expired; delete the image file.
            File delFile = new File(m_filename);
            if (delFile != null) {
                delFile.delete();
            }
            //LogUtil.getLog(getClass()).info("Unbound event: " + e.toString()
            //	+ "\n m_filename: " + m_filename);
        }
    }

	public int getEmailId() {
		return emailId;
	}

	public void setEmailId(int emailId) {
		this.emailId = emailId;
	}

}
