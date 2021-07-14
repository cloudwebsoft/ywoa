package com.redmoon.oa.emailpop3.pop3;

import java.io.*;
import java.util.*;
import java.util.Properties;
import java.util.regex.*;

import javax.mail.*;
import javax.mail.internet.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.*;

public class MailHelper {
    private MimeMessage mimeMessage = null;
    private StringBuffer bodyText = new StringBuffer(); // 存放邮件内容

    public MailHelper(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;

        getMailContent((Part)mimeMessage);
    }

    public void setMimeMessage(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }

    public Message getMimeMessage() {
        return mimeMessage;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

	protected static String decodeText(String text)
			throws UnsupportedEncodingException {
		if (text == null)
			return null;
		if (text.startsWith("=?GB") || text.startsWith("=?gb"))
			text = MimeUtility.decodeText(text);
		else
			text = new String(text.getBytes("ISO8859_1"));
		return text;
	}
	 
    /**
     * 获得发件人的地址和姓名
     */
    public String getFrom() {
        String from = "";
        try {
            InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
            if (address!=null) {
                from = address[0].getAddress();
                
    		    try {
					from = decodeText(from);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }
        }
        catch (javax.mail.internet.AddressException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        if (from == null)
            from = "";

        return from;
    }

    public String getSendPerson() {
        String personal = "";
        try {
            InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
            if (address!=null && address[0].getPersonal()!=null)
                personal = address[0].getPersonal();
        }
        catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        if (personal == null)
            personal = "";
        return personal;
    }

    /**
     * 获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同 "to"----收件人 "cc"---抄送人地址 "bcc"---密送人地址
     * @param type String
     * @return String
     * @throws MessagingException
     */
    public String getMailAddress(String type) {
        String mailaddr = "";
        String addtype = type.toUpperCase();
        InternetAddress[] address = null;
        if (addtype.equals("TO") || addtype.equals("CC") ||
            addtype.equals("BCC")) {
            try {
                if (addtype.equals("TO")) {
                    address = (InternetAddress[]) mimeMessage.getRecipients(
                            Message.
                            RecipientType.TO);
                } else if (addtype.equals("CC")) {
                    address = (InternetAddress[]) mimeMessage.getRecipients(
                            Message.
                            RecipientType.CC);
                } else {
                    address = (InternetAddress[]) mimeMessage.getRecipients(
                            Message.
                            RecipientType.BCC);
                }
            }
            catch (MessagingException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    String email = address[i].getAddress();
                    if (email == null)
                        email = "";
                    else {
                        try {
                            email = MimeUtility.decodeText(email);
                        }
                        catch (java.io.UnsupportedEncodingException e) {
                            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                        }
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null)
                        personal = "";
                    else {
                        try {
                            personal = MimeUtility.decodeText(personal);
                        }
                        catch (java.io.UnsupportedEncodingException e) {
                            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
                        }
                    }
                    String compositeto = personal + "<" + email + ">";
                    mailaddr += "," + compositeto;
                }
                if (!mailaddr.equals("")) {
                	mailaddr = mailaddr.substring(1);
                }
            }
        }
        return mailaddr;
    }

    /**
     * 获得邮件主题
     */
    public String getSubject() {
        String subject = "";
        try {
            subject = StrUtil.getNullStr(mimeMessage.getSubject());
            // System.out.println(getClass() + " subject=" +  subject);

            /*
            编码类似=?GB2312?B?u/nS8svjt6jIw7j20NS7r0ZMQVNItPPP1Mnxzf4=?= 需重新解码
            subject = MimeUtility.decodeText(subject); 无效，不能转换BASE64
            */
            boolean isNeedDecode = false;
            if (subject.indexOf("=?x-unknown?") >= 0) {
                subject = subject.replaceAll("x-unknown", "gb2312"); // 将编码方式的信息由x-unkown改为gbk
                isNeedDecode = true;
            }
            else if (subject.indexOf("=?UNKNOWN") >= 0) {
                subject = subject.replaceAll("UNKNOWN", "gb2312");
                isNeedDecode = true;
            }
            else if (subject.indexOf("=?GB2312") >=0) {
                isNeedDecode = true;
            }
            else if (subject.indexOf("=?gb2312") >=0) {
                isNeedDecode = true;
            }

            if (isNeedDecode)
                subject = MimeUtility.decodeText(subject);
            else {
                String charset = getCharset(mimeMessage);
                // System.out.println(getClass() + " charset=" + charset);

                // 很多邮件不规范，会出现需要转码的问题，所以先尝试转码，然后判断是否需要转码
                String subject2 = new String(subject.getBytes("ISO-8859-1"),
                                             charset);
                // 判别处理前与处理后，是否多了?号，长度变短了，则说明对中文作了多余的转码
                if (StrUtil.UTF8Len(subject) > StrUtil.UTF8Len(subject2))
                    ;
                else
                    subject = subject2;
            }
        } catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        catch (java.io.UnsupportedEncodingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        // System.out.println(getClass() + " subject2=" +  subject);

        return subject;
    }

    public String getCharset(MimeMessage message) {
        Pattern encodeStringPattern = Pattern.compile(
                "=\\?(.+)\\?(B|Q)\\?(.+)\\?=",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        String[] CHARTSET_HEADER = new String[] {"Subject", "From", "To", "Cc",
                                   "Delivered-To"};
        try {
            Enumeration enu = message.getNonMatchingHeaderLines(CHARTSET_HEADER);
            while (enu.hasMoreElements()) {
                String header = (String) enu.nextElement();
                Matcher m = encodeStringPattern.matcher(header);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return "gb2312";
    }

    /**
     * 获得邮件发送日期
     */
    public Date getSentDate() {
        try {
            // LogUtil.getLog(getClass()).info("mimeMessage.getSentDate()=" + DateUtil.format(mimeMessage.getSentDate(), "yyyy-MM-dd HH:mm:ss"));
            return mimeMessage.getSentDate();
        } catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return null;
    }

    /**
     * 获得邮件正文内容
     */
    public String getBodyText() {
        String charset = getCharset(mimeMessage);
        // System.out.println(getClass() + " charset=" + charset);
        String text = bodyText.toString();
        // 很多邮件不规范，会出现需要转码的问题，所以先尝试转码，然后判断是否需要转码
        String text2 = "";
        try {
            text2 = new String(text.getBytes("ISO-8859-1"),
                               charset);
        }
        catch (java.io.UnsupportedEncodingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        // 判别处理前与处理后，是否多了?号，长度变短了，则说明对中文作了多余的转码
        if (StrUtil.UTF8Len(text) > StrUtil.UTF8Len(text2))
            ;
        else
            text = text2;

        return text;
    }

    /**
     * 解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件 主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     */
    public void getMailContent(Part part) {
        try {
            String contenttype = part.getContentType();
            // System.out.println(getClass() + " contenttype=" + contenttype);
            int nameindex = contenttype.indexOf("name");
            boolean conname = false;
            if (nameindex != -1)
                conname = true;
            // System.out.println(getClass() + " CONTENTTYPE: " + contenttype + " conname=" + conname);
            if (part.isMimeType("text/plain") && !conname) {
                if (bodyText.toString().equals("")) {

                    bodyText.append((String) part.getContent());
                    html = false;
                }
            } else if (part.isMimeType("text/html") && !conname) {
                if (bodyText.toString().equals("")) {
                    bodyText.append((String) part.getContent());
                    html = true;
                }
            } else if (part.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) part.getContent();

                int counts = multipart.getCount();
                for (int i = 0; i < counts; i++) {
                    getMailContent(multipart.getBodyPart(i));
                }
            } else if (part.isMimeType("message/rfc822")) {
                getMailContent((Part) part.getContent());
            }

            // System.out.println(getClass() + " getContent end");

        }
        catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (java.io.UnsupportedEncodingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }


    /**
     * 判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
     * @return boolean
     * @throws MessagingException
     */
    public boolean getReplySign() throws MessagingException {
        boolean replysign = false;
        String needreply[] = mimeMessage
                             .getHeader("Disposition-Notification-To");
        if (needreply != null) {
            replysign = true;
        }
        return replysign;
    }

    /**
     * 获得此邮件的Message-ID
     */
    public String getMessageId() throws MessagingException {
        return mimeMessage.getMessageID();
    }

    /**
     * 判断此邮件是否已读，如果未读返回返回false,反之返回true
     * @return boolean
     * @throws MessagingException
     */
    public boolean isNew() {
        boolean isnew = false;
        try {
            Flags flags = ((Message) mimeMessage).getFlags();
            Flags.Flag[] flag = flags.getSystemFlags();
            for (int i = 0; i < flag.length; i++) {
                if (flag[i] == Flags.Flag.SEEN) {
                    isnew = true;
                    break;
                }
            }
        }
        catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        return isnew;
    }
   
    /**
     * 判断此邮件是否是紧急状态
     * @return
     * @throws MessagingException 
     */
    public boolean isEmergent() throws MessagingException{
    	boolean isEmergent = false;
    	String emergent[] = mimeMessage.getHeader("X-Priority");
    	if(emergent == null){
    		return isEmergent;
    	}
    	String level = emergent[0];
    	if(level.equals("1")){
    		isEmergent = true;
    	}
    	return isEmergent;
    }

    /**
     * 判断此邮件是否包含附件
     */
    public boolean isContainAttach(Part part) throws Exception {
        boolean attachflag = false;
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null)
                    && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE))))
                    attachflag = true;
                else if (mpart.isMimeType("multipart/*")) {
                    attachflag = isContainAttach((Part) mpart);
                } else {
                    String contype = mpart.getContentType();
                    if (contype.toLowerCase().indexOf("application") != -1)
                        attachflag = true;
                    if (contype.toLowerCase().indexOf("name") != -1)
                        attachflag = true;
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachflag = isContainAttach((Part) part.getContent());
        }
        return attachflag;
    }

    public String getUid() {
        return uid;
    }

    public boolean isHtml() {
        return html;
    }

    private String uid;
    private boolean html = true;
}
