package com.redmoon.oa.emailpop3.pop3;

import java.util.Date;
import java.util.Vector;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MailMsg {
    String[] Recipients = null;
    String Subject = "";
    String Sender = "";
    String content = "";
    Date ReceiveDate = null;
    Date SentDate = null;
    Vector attachs = null;
    Message message = null;
    String From = "";
    int Size = 0;
    String ID = "";
    int dispnum = 0; // 正文、附件在邮件中的序号
    boolean HasAttachment = false;

    public MailMsg() {
        attachs = new Vector();
    }

    public MailMsg(Message message) {
        this.message = message;
    }

    /**
     * 对邮件进行初始化
     * @param message
     * @param isdetail 是否取出详细信息
     */
    public MailMsg(Message message, boolean isdetail) {
        this.message = message;
        attachs = new Vector();
        init(isdetail);
    }

    public Message getMessage() {
        return message;
    }

    /**
     * 取得序号为num的附件
     * @param num
     */
    public Attachment getAttachment(int num) {
        Attachment a = null;
        try {
            Part messagePart = message;
            Object content = messagePart.getContent();

            if (!(content instanceof Multipart))
                return null;
            Multipart multipart = (Multipart) content;
            Part part = multipart.getBodyPart(num);

            String disposition = part.getDisposition();
            if (disposition.equalsIgnoreCase(Part.ATTACHMENT) ||
                disposition.equalsIgnoreCase(Part.INLINE)) {
                a = new Attachment(part, num);
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("getAttachment: " + e.getMessage());
        }
        return a;
    }

    /**
     * 有的邮件的Subject经编码后，形式为=?GB2312?B?u/nS8svjt6jIw7j20NS7r0ZMQVNItPPP1Mnxzf4=?=，但经测试javamail的MimeUtility.decodeText不能解码，因此创建此方法
     * @param subjectStr String
     * @return String
     */
    public static String decodeSubject(String subjectStr) {
        // String subjectStr = "=?GB2312?B?u/nS8svjt6jIw7j20NS7r0ZMQVNItPPP1Mnxzf4=?=";
        if (!subjectStr.startsWith("=?") || !subjectStr.endsWith("?=") ||
            subjectStr.indexOf("?B?") <= 0)
            return subjectStr;
        String subj = subjectStr;
        // String code = subjectStr.substring(2, subjectStr.indexOf("?B?"));
        String code = "gb2312";
        subjectStr = subjectStr.substring(code.length() + 5, subjectStr.length() - 2);
        sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
        try {
            subjectStr = new String(decoder.decodeBuffer(subjectStr), code);
        } catch (Exception e) {
            LogUtil.getLog(MailMsg.class).error(e);
            return subj;
        }
        return subjectStr;
    }

    public void init(boolean isdetail) {
        if (message == null)
            return;
        try {
            From = ((InternetAddress) message.getFrom()[0]).getPersonal(); //取出第一个发送者
            if (From == null)
                From = ((InternetAddress) message.getFrom()[0]).getAddress();
            Sender = ((InternetAddress) message.getFrom()[0]).getAddress();
            Subject = message.getSubject();

            /*
            编码类似=?GB2312?B?u/nS8svjt6jIw7j20NS7r0ZMQVNItPPP1Mnxzf4=?= 需重新解码
            Subject = MimeUtility.decodeText(Subject); 无效，不能转换BASE64
            */
            boolean isNeedDecode = false;
            if (Subject.indexOf("=?x-unknown?") >= 0) {
                Subject = Subject.replaceAll("x-unknown", "gb2312"); // 将编码方式的信息由x-unkown改为gbk
                isNeedDecode = true;
            }
            else if (Subject.indexOf("=?UNKNOWN") >= 0) {
                Subject = Subject.replaceAll("UNKNOWN", "gb2312");
                isNeedDecode = true;
            }
            else if (Subject.indexOf("=?GB2312") >=0) {
                Subject = MimeUtility.decodeText(Subject);
                isNeedDecode = true;
            }
            else if (Subject.indexOf("=?gb2312") >=0) {
                Subject = MimeUtility.decodeText(Subject);
                isNeedDecode = true;
            }

            String charset = getCharset(message);

            if (isNeedDecode)
                Subject = decodeSubject(Subject);
            else {
                // 很多邮件不规范，会出现需要转码的问题，所以先尝度转码，然后判断是否需要转码
                if (charset.equalsIgnoreCase("gb2312") || charset.equals("") || message.getContentType().indexOf("multipart/mixed")!=-1 || charset.equalsIgnoreCase("GBK")) {
                    String Subject2 = new String(Subject.getBytes("ISO-8859-1"),
                                         "gb2312");

                    // 判别处理前与处理后，是否多了?号，长度变短了，则说明作了多余的转码
                    if (StrUtil.UTF8Len(Subject) > StrUtil.UTF8Len(Subject2))
                        ;
                    else
                        Subject = Subject2;
                }
            }

            Address[] addrs = message.getAllRecipients();
            if (addrs != null) {
                Recipients = new String[addrs.length];
                for (int i = 0; i < addrs.length; i++) {
                    Recipients[i] = ((InternetAddress) addrs[i]).getAddress();
                }
            }
            ReceiveDate = message.getReceivedDate(); //接收时间
            SentDate = message.getSentDate();
            Size = message.getSize();

            ID = "" + message.getMessageNumber();

            // 取得附件
            Part messagePart = message;

            // 因不支持GBK编码，此行当编码为GBK时会报异常，好象还搜不到较好的解决方法
            Object content = messagePart.getContent();

            attachs.removeAllElements();

            dispnum = 0; // 初始化序号

            if (content instanceof Multipart) {
                // 处理正文及附件部分
                handleMultipart((Multipart) content, isdetail);
            } else {
                // 处理正文部分
                handlePart(messagePart, isdetail);
            }
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
    }

    /**
     * 获取邮件的MIME类型
     * @param part Part
     * @return String 为空表示未能获取到，对于有些以multipart***格式的，就获取不到
     * @throws MessagingException
     */
    public String getCharset(Part part) throws MessagingException {
        String charset = "";
        String contentType = part.getContentType();
        if (contentType.startsWith("text/plain") ||
            contentType.startsWith("text/html")) {

            int p = contentType.indexOf("charset");
            if (p != -1) {
                p = contentType.indexOf("=", p + 7);
                if (p != -1)
                    charset = contentType.substring(p + 1).trim();
            }
            if (charset.startsWith("\""))
                charset = charset.substring(1);
            if (charset.endsWith("\""))
                charset = charset.substring(0, charset.length() - 1);
        }
        return charset;
    }


    public boolean hasAttachment() {
        return HasAttachment;
    }

    public void handleMultipart(Multipart multipart, boolean isdetail) throws
            MessagingException, IOException {
        for (int i = 0, n = multipart.getCount(); i < n; i++) {
            handlePart(multipart.getBodyPart(i), isdetail);
            dispnum++;
        }
    }

    public void handlePart(Part part, boolean isdetail) throws
            MessagingException,
            IOException {
        String disposition = part.getDisposition();
        String contentType = part.getContentType();
        if (disposition == null) { // When just body,只有body时
            //Debug.println("Null: " + contentType);
            if (isdetail) {
                if (contentType.startsWith("text/plain") ||
                    contentType.startsWith("text/html")) {

                    String charset = "utf-8";
                    int p = contentType.indexOf("charset");
                    if (p != -1) {
                        p = contentType.indexOf("=", p + 7);
                        if (p != -1)
                            charset = contentType.substring(p + 1).trim();
                    }
                    if (charset.startsWith("\""))
                        charset = charset.substring(1);
                    if (charset.endsWith("\""))
                        charset = charset.substring(0, charset.length() - 1);

                    InputStream is = part.getInputStream();
                    BufferedReader reader = new BufferedReader(new
                            InputStreamReader(is, charset));
                    String thisLine = reader.readLine();
                    while (thisLine != null) {
                        content += thisLine;
                        thisLine = reader.readLine();
                    }
                    is.close();

                    if (contentType.startsWith("text/plain"))
                        content = StrUtil.toHtml(content);
                }
            }
        } else if (disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
            //attachs.addElement(StrUtil.UnicodeToGB(getISOFileName(part)));//这行与下行都对
            HasAttachment = true;
            if (isdetail) {
                Attachment a = new Attachment(part, dispnum); //取得附件信息
                attachs.addElement(a);
            }
            //saveFile(part.getFileName(), part.getInputStream());
        } else if (disposition.equalsIgnoreCase(Part.INLINE)) {
            HasAttachment = true;
            if (isdetail) {
                //attachs.addElement(StrUtil.UnicodeToGB(getISOFileName(part)));
                Attachment a = new Attachment(part, dispnum);
                attachs.addElement(a);
            }
            //saveFile(part.getFileName(), part.getInputStream());
        } else { // Should never happen
            LogUtil.getLog(getClass()).error("Other: " + disposition);
        }
    }

    /**
     *   @从BodyPart中提取使用ISO-8859-1编吗的文件名，与part.getFileName()得到的结果是一样的
     *   @因为BodyPart.getFilename()过程已经对文件名作了一次编码，有时不能直接使用
     */
    public static String getISOFileName(Part body) {
        //设置一个标志，判断文件名从Content-Disposition中获取还是从Content-Type中获取
        boolean flag = true;
        if (body == null) {
            return null;
        }
        String[] cdis;
        try {
            cdis = body.getHeader("Content-Disposition");
        } catch (Exception e) {
            return null;
        }
        if (cdis == null) {
            flag = false;
        }
        if (!flag) {
            try {
                cdis = body.getHeader("Content-Type");
            } catch (Exception e) {
                return null;
            }
        }
        if (cdis == null) {
            return null;
        }
        if (cdis[0] == null) {
            return null;
        }
        //从Content-Disposition中获取文件名
        if (flag) {
            int pos = cdis[0].indexOf("filename=");
            if (pos < 0) {
                return null;
            }
            //如果文件名带引号
            if (cdis[0].charAt(cdis[0].length() - 1) == '"') {
                return cdis[0].substring(pos + 10, cdis[0].length() - 1);
            }
            return cdis[0].substring(pos + 9, cdis[0].length());
        } else {
            int pos = cdis[0].indexOf("name=");
            if (pos < 0) {
                return null;
            }
            //如果文件名带引号
            if (cdis[0].charAt(cdis[0].length() - 1) == '"') {
                return cdis[0].substring(pos + 6, cdis[0].length() - 1);
            }
            return cdis[0].substring(pos + 5, cdis[0].length());
        }
    }

    public Vector getAttachments() {
        return attachs;
    }

    public static void saveFile(String filename,
                                InputStream input) throws IOException {
        if (filename == null) {
            filename = File.createTempFile("xx", ".out").getName();
        }
        // Do no overwrite existing file
        File file = new File("e:/mail.doc"); //+filename);
        for (int i = 0; file.exists(); i++) {
            file = new File(filename + i);
        }
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        BufferedInputStream bis = new BufferedInputStream(input);
        int aByte;
        while ((aByte = bis.read()) != -1) {
            bos.write(aByte);
        }
        bos.flush();
        bos.close();
        bis.close();
    }

    public String getID() {
        return ID;
    }

    public int getSize() {
        return this.Size;
    }

    public void setSize(int s) {
        this.Size = s;
    }

    public Date getSentDate() {
        return this.SentDate;
    }

    public void setSentDate(Date d) {
        this.SentDate = d;
    }

    public String getSubject() {
        return this.Subject;
    }

    public void setSubject(String s) {
        this.Subject = s;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String s) {
        this.content = s;
    }

    public Date getReceiveDate() {
        return this.ReceiveDate;
    }

    public void setReceiveDate(Date d) {
        this.ReceiveDate = d;
    }

    public void setSender(String s) {
        this.Sender = s;
    }

    public String getSender() {
        return this.Sender;
    }

    public String getFrom() {
        return this.From;
    }

    public String[] getRecipients() {
        return this.Recipients;
    }

    public void setFrom(String f) {
        this.From = f;
    }

    public boolean isDraft() {
        try {
            if (message.isSet(Flags.Flag.DRAFT))
                return true;
            else
                return false;
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("isDraft:" + e.getMessage());
        }
        return false;
    }

    public boolean isDeleted() {
        try {
            if (message.isSet(Flags.Flag.DELETED))
                return true;
            else
                return false;
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("isDeleted:" + e.getMessage());
        }
        return false;
    }

    public boolean isSeen() {
        try {
            // Check if DELETED flag is set of this message
            if (message.isSet(Flags.Flag.SEEN))
                return true;
            else
                return false;
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("isSeen:" + e.getMessage());
        }
        return false;
    }

}
