// Decompiled by Jad v1.5.7g. Copyright 2000 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/SiliconValley/Bridge/8617/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi
// Source File Name:   FJMailBean.java

package cn.js.fan.mail;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;

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
    int port = 25;
    private String charset;

    String subtype = "related"; // Construct a MimeMultipart object of the given subtype. A unique boundary string is generated and this string is setup as the "boundary" parameter for the contentType field.

    String tempAttachFilePath;
    
    public SendMail() {
        host = "fserver";
        mailFooter = ""; // "\n\n\n===========此邮件由Bluewind发送========\n\n";
        mailFooterHTML = ""; // "<br><br><br>===========此邮件由Bluewind发送========<br><br>";
        
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
    
    public SendMail(String smtpCharset) {
        host = "fserver";
        mailFooter = ""; // "\n\n\n===========此邮件由Bluewind发送========\n\n";
        mailFooterHTML = ""; // "<br><br><br>===========此邮件由Bluewind发送========<br><br>";
        
        if (smtpCharset.equals("")) {
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
        } else {
        	charset = smtpCharset;
        }
    }

    public void setmailFooter(String s) throws Exception {
        mailFooter = s;
    }

    public void setmailFooterHTML(String s) throws Exception {
        mailFooterHTML = s;
    }

    /**
     * 以HTML的方式发送
     * @param smtpServer String
     * @param smtpPort int
     * @param to String
     * @param subject String
     * @param content String
     * @return boolean
     */
    public boolean send(String smtpServer, int smtpPort, String smtpUser, String smtpPwd, String to, String from, String subject,
                        String content) {
        clear();

        boolean re = false;
        try {
            initSession(smtpServer, smtpPort, smtpUser, smtpPwd);
            // from = StrUtil.GBToUnicode(from);
            // from += "<fgf163@pub.zj.jsinfo.net>";
            initMsg(to, from, subject, content, true);
            re = send();
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("send(,,,,):" + e.getMessage());
        }
        return re;
    }

    public void initSession(String host, int port, String username,
                            String password, String subtype, boolean isSsl) {
        this.port = port;
        this.host = host;
        this.username = username;
        this.password = password;
        this.subtype = subtype;

        try {
        	// 信任所有SSL证书，java在请求某些不受信任的https网站时会报：PKIX path building failed
        	if (isSsl) {
        		SslUtils.ignoreSsl();
        	}
        	
            Properties properties = System.getProperties();

            if (!isSsl) {
	            properties.put("mail.host", host);
	            properties.put("mail.transport.protocol", "smtp");
	            properties.put("mail.smtp.auth", "true");
	            properties.put("mail.smtp.port", "" + port);
            } else {
            	Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

                // 协议名称设置为smtps，会使用SSL
                properties.setProperty("mail.transport.protocol", "smtps");
                properties.setProperty("mail.smtp.ssl.enable", "true");

    	        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    	        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
                properties.setProperty("mail.smtp.socketFactory.port", "" + port);
    	        properties.setProperty("mail.smtp.auth", "true");
    	        properties.setProperty("mail.smtp.host", host);
    	        properties.setProperty("mail.smtp.port", String.valueOf(port));
                properties.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

                // 如果出现：454 Command not permitted when TLS active
                properties.setProperty("mail.smtp.starttls.enable", "false");
            }
            
            // 防止超时，10秒
	        properties.setProperty("mail.smtp.connectiontimeout", "10000");
	        properties.setProperty("mail.smtp.timeout", "10000");

            PopupAuthenticator popupauthenticator = new PopupAuthenticator();
            popupauthenticator.init(username, password);
            session = Session.getInstance(properties, popupauthenticator);
            session.setDebug(sessionDebug);
            msg = new MimeMessage(session);
            msg.setSentDate(new Date());
            if ("".equals(subtype)) {
                multipart = new MimeMultipart();
            } else {
                multipart = new MimeMultipart(subtype);
            }
            msg.setContent(multipart);
        } catch (Exception e) {
            errinfo += e.getMessage();
            LogUtil.getLog(getClass()).error("initSession: " + e.getMessage());
        }
    }
    
    public void initSession(String host, int port, String username,
            String password, String subtype) {
    	initSession(host, port, username, password, subtype, false);
    }

    public void initSession(String host, String username, String password,
                            String subtype) {
        initSession(host, 25, username, password, subtype);
    }

    public void initSession(String host, String username, String password) throws
            Exception {
        initSession(host, 25, username, password, "");
    }

    public void initSession(String host, int port, String username, String password) throws
            Exception {
        initSession(host, port, username, password, "");
    }

    public void initMsg(String to, String from, String subject, String body,
                        boolean flag) {
        setSendTo(to);
        setFrom(from);
        setSubject(subject);
        setBody(body, flag);
    }

    /**
     * 初始化信息
     * @param flag 是否用HTML的方式发信
     * @throws java.lang.Exception
     */
    public void initMsg(String to[], String from, String subject, String body,
                        boolean flag) throws Exception {
        setSendTo(to);
        setFrom(from);
        setSubject(subject);
        setBody(body, flag);
    }

    public void initMsg(String to, String subject, String body, boolean flag) throws
            Exception {
        setSendTo(to);
        setSubject(subject);
        setBody(body, flag);
    }

    public void setFrom(String from) {
        try {
            msg.setFrom(new InternetAddress(from));
        } catch (Exception e) {
            errinfo += e.getMessage();
            LogUtil.getLog(getClass()).error("setFrom: " + e.getMessage());
        }
    }

    void setSendTo(String to[]) {
        for (int i = 0; i < to.length; i++)
            setSendTo(to[i]);
    }

    void setSendTo(String to) {
        try {
            InternetAddress ainternetaddress[] = {
                                                 new InternetAddress(to)
            };
            msg.setRecipients(javax.mail.Message.RecipientType.TO,
                              ainternetaddress);
        } catch (Exception e) {
            errinfo += e.getMessage();
            LogUtil.getLog(getClass()).error("setSendTo: " + e.getMessage());
        }
    }

    void setCopyTo(String to[]) throws Exception {
        for (int i = 0; to != null && i > to.length; i++)
            setCopyTo(to[i]);
    }

    void setCopyTo(String to) throws Exception {
        InternetAddress ainternetaddress[] = {
                                             new InternetAddress(to)
        };
        msg.setRecipients(javax.mail.Message.RecipientType.CC, ainternetaddress);
    }

    //JavaMail中的邮件主题需要进行BASE64编码，格式形如：
    //=?GB2312?B?xPq1xMPcwuvS0b6t1tjWw6Osx+u+ob/stcfCvKOssqLQ3rjEw9zC66Oh?=
    //所以，直接使用msg.setSubject("中文主题")，或者msg.setSubject("中文主题".getBytes("8859_1"), "GB2312"))都一样会出现乱码。在设置邮件主题前需要将主题字串的字节编码为BASE64格式，并添加编码头
    void setSubject(String subject) {
        try {
            /*
            // 当文字多，或者有: &符号时，会出现乱码
            BASE64Encoder base64encoder = new BASE64Encoder();
            msg.setSubject("=?GB2312?B?" +
                           base64encoder.encode(subject.getBytes()) +
                           "?=");
            */

            // 让javamail决定用什么方式来编码 ，编码内容的字符集是系统字符集
            // mimeMsg.setSubject( MimeUtility.encodeText( Subject) );
            // 使用指定的base64方式编码,并指定编码内容的字符集是gb2312,应该是根据系统当前的字符集来选择编码
        	
            //msg.setSubject( MimeUtility.encodeText( subject, "gb2312", "B")); //B为base64方式
        	msg.setSubject( MimeUtility.encodeText( subject, charset, "B"));
        } catch (Exception e) {
            errinfo += e.getMessage();
            LogUtil.getLog(getClass()).error("setSubject: " + e.getMessage());
        }
    }

    /**
     * 置正文类型
     * @param flag 如果为真，则表示用HTML的方式
     * @throws java.lang.Exception
     */
    void setBody(String body, boolean flag) {
        try {
            MimeBodyPart mimebodypart = new MimeBodyPart();
            if (flag) {
                String htmlContent = getContent(body);
//                mimebodypart.setContent(StrUtil.GBToUnicode(htmlContent +
//                        mailFooterHTML),
//                                        "text/html");
                //LogUtil.getLog(getClass()).debug("Sendmail setBody: " + htmlContent + mailFooterHTML);
                mimebodypart.setContent(htmlContent + mailFooterHTML, "text/html; charset=UTF-8");
                //调用处理html文件中的图片方法
                processHtmlImage();
            } else
                mimebodypart.setText(body + mailFooter);
            multipart.addBodyPart(mimebodypart);
        } catch (Exception e) {
            errinfo += e.getMessage();
            LogUtil.getLog(getClass()).error("setBody: " + e.getMessage());
        }

    }

    public void setAttachFile(String filepath) throws Exception {
        MimeBodyPart mimebodypart = new MimeBodyPart();
        FileDataSource filedatasource = new FileDataSource(filepath);

        mimebodypart.setDataHandler(new DataHandler(filedatasource));
        mimebodypart.setFileName(StrUtil.UTF8ToUnicode(filedatasource.getName()));
        multipart.addBodyPart(mimebodypart);
    }

    public void setAttachFile(String filepath, String name) throws Exception {
        MimeBodyPart mimebodypart = new MimeBodyPart();
        FileDataSource filedatasource = new FileDataSource(filepath);

        mimebodypart.setDataHandler(new DataHandler(filedatasource));
        mimebodypart.setFileName(StrUtil.UTF8ToUnicode(name));
        multipart.addBodyPart(mimebodypart);
    }

    public void setAttachFile(String attach[]) throws Exception {
        for (int i = 0; i < attach.length; i++)
            setAttachFile(attach[i]);
    }

    public boolean send() {
        try {
            Transport.send(msg);
        } catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(e);
            errinfo = e.getMessage();
            return false;
        }
        return true;
    }

    public String getErrMsg() {
        return errinfo;
    }

    /**
     * 取得上传后的邮件信息，包括附件，将其置于cleanup中，以备session消亡时自动删除
     * @param application
     * @param request
     */
    public void getMailInfo(ServletContext application,
                            HttpServletRequest request) {
        //修改文件默认路径从CONFIG_CWS.XML获取 jfy 20150104
    	//String realPath = application.getRealPath("/");
    	String realPath = Global.getRealPath();
        if (realPath.lastIndexOf("/")!=realPath.length()-1)
            realPath += "/";
        tempAttachFilePath = realPath + "upfile/Attach/";

        FileUpload mfu = new FileUpload();
        mfu.setSavePath(tempAttachFilePath); //取得目录

        try {
            if (mfu.doUpload(application, request) == -3) {
                LogUtil.getLog(getClass()).error("文件太大,请把文件大小限制在30K以内!</p>");
                return;
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("getRequestInfo:" + e.getMessage());
        }
        // 取得表单中域的信息
        String to = StrUtil.getNullString(mfu.getFieldValue("to"));
        String subject = StrUtil.getNullString(mfu.getFieldValue("subject"));
        String content = StrUtil.getNullString(mfu.getFieldValue("content"));
        try {
            initMsg(to, subject, content, true);
        } catch (Exception e1) {
            LogUtil.getLog(getClass()).error("SendMail initMsg:" + e1.getMessage());
        }

        // 处理附件
        mfu.writeFile(true); //用随机方式命名文件，如果不这样会使得覆盖别人的文件，或者自己的同名文件，但是恰恰session到期，而被删除
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
        java.util.Enumeration e = mfu.getFiles().elements();
        while (e.hasMoreElements()) {
            lc++;
            FileInfo fi = (FileInfo) e.nextElement();
            cln = new CleanUp(tempAttachFilePath + fi.diskName);
            session.setAttribute("bindings.listener" + lc, cln);
            try {
                setAttachFile(tempAttachFilePath + fi.diskName, fi.name);
            } catch (Exception e2) {
                LogUtil.getLog(getClass()).error("getMailInfo setAttachFile:" + e2.getMessage());
            }
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
        }

        public void valueUnbound(HttpSessionBindingEvent e) {
            //	The user's session expired; delete the image file.
            File delFile = new File(m_filename);
            if (delFile != null) {
                delFile.delete();
            }
        }
    }

    // 处理html页面上的图片方法如下：
    private void processHtmlImage() throws MessagingException {
        for (int i = 0; i < arrayList1.size(); i++) {
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource("d:/zjrj/" + (String) arrayList1.get(i));
            messageBodyPart.setDataHandler(new DataHandler(source));
            String contentId = "<" + (String) arrayList2.get(i) + ">";
            messageBodyPart.setHeader("Content-ID", contentId);
            messageBodyPart.setFileName((String) arrayList1.get(i));
            multipart.addBodyPart(messageBodyPart);
        }
    }

    //处理要发送的html文件，主要是针对html文件中的图片
    private String getContent(String mailContent) {
        Pattern pattern;
        Matcher matcher;

        pattern = Pattern.compile("src=([^>]*[^/].(?:jpg|jpeg|bmp|gif))(?:\\\"|\\'|\\s)", Pattern.CASE_INSENSITIVE);
        // pattern = Pattern.compile("src=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^'\">\\s]+))", Pattern.CASE_INSENSITIVE);

        matcher = pattern.matcher(mailContent);
        while (matcher.find()) {
            String path = matcher.group(1);
            if (path.indexOf("http://") != -1) {
                // LogUtil.getLog(getClass()).info("不需要处理图片！");
            } else {
                if (path.indexOf("\"")==0 || path.indexOf("'")==0)
                    path = path.substring(1);
                arrayList1.add(path);
            }
        }

        String afterReplaceStr = mailContent;

        //在html文件中用"cid:"+Content-ID来替换原来的图片链接
        for (int m = 0; m < arrayList1.size(); m++) {
            arrayList2.add(createRandomStr());
            String addString = "cid:" + (String) arrayList2.get(m);
            String str = (String) arrayList1.get(m);
            afterReplaceStr = afterReplaceStr.replaceAll((String) arrayList1.get(m),
                    addString);
        }
        // LogUtil.getLog(getClass()).info(afterReplaceStr);
        return afterReplaceStr;
    }

    //产生一个随机字符串，为了给图片设定Content-ID值
    private String createRandomStr() {
        char[] randomChar = new char[8];
        for (int i = 0; i < 8; i++) {
            randomChar[i] = (char) (Math.random() * 26 + 'a');
        }
        String replaceStr = new String(randomChar);
        return replaceStr;
    }

    private ArrayList arrayList1 = new ArrayList();
    private ArrayList arrayList2 = new ArrayList();
    /**
     * clear
     */
    public void clear() {
        try {
            if (subtype.equals(""))
                multipart = new MimeMultipart();
            else
                multipart = new MimeMultipart(subtype);
            msg.setContent(multipart);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("clear: " + e.getMessage());
        }
    }

}
