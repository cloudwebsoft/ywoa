package com.redmoon.oa.emailpop3.pop3;

import javax.mail.*;

import java.security.Security;
import java.util.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.sun.mail.pop3.POP3Folder;
import com.redmoon.oa.emailpop3.MailMsgDb;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class GetMail {
    public static boolean debug = true;
    String user = "";
    String pwd = "";
    String server = "";
    Message[] msgs = null;
    Vector mailmsgs = null;
    int port = 110;

    Store store = null;
    POP3Folder folder = null;

    public GetMail(String server, String user, String pwd) {
        this.server = server;
        this.user = user;
        this.pwd = pwd;
    }

    public GetMail(String server, int port, String user, String pwd) {
        this.server = server;
        this.user = user;
        this.pwd = pwd;
        this.port = port;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        if (mailmsgs != null) {
            mailmsgs.removeAllElements();
            mailmsgs = null;
        }
    }

    public int receive(HttpServletRequest request, String emailAddress, boolean isDelete, boolean isSSL) throws ErrMsgException {
        int count = 0;
        try {
            GetMailStatus gms = GetMailStatus.getFromSession(request, emailAddress);
            if (gms==null) {
                gms = new GetMailStatus();
                gms.setStartTime(System.currentTimeMillis());
            }
            else {
                // 如果session中已存在，则根据session检查如果接收时间大于10秒，则重新接收
                if (!gms.isOver()) {
                    if ((System.currentTimeMillis()-gms.getStartTime())/1000 < 10) {
                        return -1;
                    }
                    else {
                        gms.setStartTime(System.currentTimeMillis());
                    }
                }
            }

            // LogUtil.getLog(getClass()).info(getClass() + " " + emailAddress + " begin receive");

			
            Properties props = System.getProperties();
            
            if (isSSL) {
				Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
				final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";            
				props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
				props.setProperty("mail.pop3.socketFactory.fallback", "false");
				props.setProperty("mail.pop3.port", String.valueOf(port));
				props.setProperty("mail.pop3.socketFactory.port", String.valueOf(port));
            }
            
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("pop3");
            store.connect(server, port, user, pwd);

            folder = (POP3Folder) store.getFolder("INBOX");

            if (folder == null)throw new ErrMsgException("无 POP3 INBOX");

            if (!isDelete)
                folder.open(Folder.READ_ONLY);
            else
                folder.open(Folder.READ_WRITE);

            // folder.getUnreadMessageCount();

            Message[] msgs = folder.getMessages();
            FetchProfile profile = new FetchProfile();
            profile.add(UIDFolder.FetchProfileItem.UID);
            folder.fetch(msgs, profile);

            int len = msgs.length;
            // 置新邮件数
            gms.setCount(folder.getUnreadMessageCount());
            GetMailStatus.storeIntoSession(request, emailAddress, gms);

            // @task:如果邮件很多，len很大，会因循环导致开销太大
            for (int i=0; i<len; i++) {
                Message msg = msgs[i];
                // 检查是否已存在相同邮件
                boolean isExist = MailMsgDb.isMailExist(folder.getUID(msg));

                if (isExist) {
                	// 从服务器上删除邮件
                    if (isDelete){
                        msg.setFlag(Flags.Flag.DELETED, true);
                    }
                    continue;
                    
                    /*
                    existCount ++;
                    if (existCount>5) {
                        gms.setOver(true);
                        GetMailStatus.storeIntoSession(request, emailAddress, gms);
                        break;
                    }
                    */
                }

                // 存入数据库
                MailHelper mh = new MailHelper((MimeMessage)msg);
                MailMsgDb mmd = new MailMsgDb();
                boolean re = mmd.create(mh, emailAddress, folder.getUID(msg));
                if (re) {
                    // 在session中保存邮件处理的状态
                    gms.setStoredCount(gms.getStoredCount() + 1);
                    GetMailStatus.storeIntoSession(request, emailAddress, gms);
                }
                count ++;

                // 从服务器上删除邮件
                if (isDelete)
                    msg.setFlag(Flags.Flag.DELETED, true);
            }
            // 置结束标
            gms.setOver(true);
            GetMailStatus.storeIntoSession(request, emailAddress, gms);

        } catch (MessagingException e) {
        	String err ="";
        	try {
                err =  StrUtil.Unicode2GB(e.getMessage());
            }
            catch (Exception ex){}
            LogUtil.getLog(getClass()).error("receive:" + err);
            throw new ErrMsgException(err);
//            if (e instanceof AuthenticationFailedException) {
//            	throw new ErrMsgException("用户名或密码不正确！");
//            }
//            else {
//	            try {
//	                throw new ErrMsgException(MimeUtility.decodeText(e.getMessage()));
//	            }
//	            catch (Exception ex){}
//            }
        }
        finally {
            close(true);
        }
        return count;
    }

    public void close(boolean expunge) {
        try {
            if (folder != null) folder.close(expunge);
        } catch (Exception ex2) {
            LogUtil.getLog(getClass()).error("receive:" + ex2.getMessage());
            //ex2.printStackTrace();
        }
        try {
            if (store != null) store.close();
        } catch (Exception ex2) {
            LogUtil.getLog(getClass()).error("receive:" + ex2.getMessage());
            ex2.printStackTrace();
        }
    }

    public void close() {
        close(false);
    }

    /**
     * 取得特定num的邮件
     * @param i 邮件标号
     * @return
     */
    public MailMsg getMessageOfNum(int i) {
        Store store = null;
        Folder folder = null;
        MailMsg mailmsg = null;
        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("pop3");
            store.connect(server, port, user, pwd);
            folder = store.getDefaultFolder();
            if (folder == null)throw new ErrMsgException("找不到默认目录");
            folder = folder.getFolder("INBOX");
            if (folder == null)throw new ErrMsgException("无 POP3 INBOX");

            folder.open(Folder.READ_ONLY);
            Message msg = folder.getMessage(i);
            mailmsg = new MailMsg(msg, true); //用true来取得全部信息，包括邮件正文
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
        } finally {
            try {
                if (folder != null) folder.close(false);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            try {
                if (store != null) store.close();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        return mailmsg;
    }

    /**
     * 删除邮件
     * @param i 邮件ID
     * @return
     */
    public boolean delMessageOfNum(String[] mailids) {
        Store store = null;
        Folder folder = null;
        MailMsg mailmsg = null;
        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("pop3");
            store.connect(server, port, user, pwd);
            folder = store.getDefaultFolder();
            if (folder == null)throw new ErrMsgException("找不到默认目录");
            folder = folder.getFolder("INBOX");
            if (folder == null)throw new ErrMsgException("无 POP3 INBOX");

            folder.open(Folder.READ_WRITE);
            int len = mailids.length;
            for (int i = 0; i < len; i++) {
                if (!StrUtil.isNumeric(mailids[i]))
                    return false;

                LogUtil.getLog(getClass()).info("msg deled num:" + mailids[i]);
                Message msg = folder.getMessage(Integer.parseInt(mailids[i]));
                msg.setFlag(Flags.Flag.DELETED, true);
            }
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
            return false;
        } finally {
            try {
                if (folder != null) folder.close(true); //为true则彻底删除，否则将其置入垃圾箱
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            try {
                if (store != null) store.close();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        return true;
    }

    public Attachment getAttachment(int msgnum, int attachnum) {
        Store store = null;
        Folder folder = null;
        MailMsg mailmsg = null;
        Attachment a = null;
        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("pop3");
            // LogUtil.getLog(getClass()).info(getClass() + " server=" + server + " port=" + port + " user=" +
            //                   user + " pwd=" + pwd);
            store.connect(server, port, user, pwd);
            folder = store.getDefaultFolder();
            if (folder == null)throw new ErrMsgException("找不到默认目录");
            folder = folder.getFolder("INBOX");
            if (folder == null)throw new ErrMsgException("无 POP3 INBOX");

            folder.open(Folder.READ_ONLY);
            Message msg = folder.getMessage(msgnum);
            mailmsg = new MailMsg(msg); // 用true来取得全部信息，包括邮件正文
            a = mailmsg.getAttachment(attachnum);
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
        } finally {
            try {
                if (folder != null) folder.close(false);
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            try {
                if (store != null) store.close();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        return a;
    }

    public MailMsg getMessage(Message message) {
        MailMsg msg = new MailMsg(message, false);
        try {
            /*            String from = ((InternetAddress)message.getFrom()[0]).getPersonal();
             if(from==null) from = ((InternetAddress)message.getFrom()[0]).getAddress();
                        mailContent = "来自: <a mailto='"+from+"'>"+from+"<br>";

                        String subject = message.getSubject();
                        mailContent += "主题: "+subject+"<br>";
                        //String d = message.getReceivedDate().toString();//接收时间
                        Part messagePart = message;
                        Object content = messagePart.getContent();
             */
            //if (content instanceof Multipart) {
            //  handleMultipart((Multipart)content);
            //} else {
            //  handlePart(messagePart);
            //}
            /*----------------------原始---------------------
                        if(content instanceof Multipart) {
                            messagePart = ((Multipart)content).getBodyPart(0);
                            //mailContent += "类型：复合文档"+"<BR>";
                        }

                        //mailContent += "内容说明: "+content.toString()+"<br>";
                        String contentType = messagePart.getContentType();
                        if(contentType.startsWith("text/plain") || contentType.startsWith("text/html")) {
                            InputStream is = messagePart.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String thisLine = reader.readLine();
                            while(thisLine!=null) {
                                mailContent += thisLine;
                                thisLine = reader.readLine();
                            }
                            is.close();
                        }
             */
        } catch (Exception ex) {
            LogUtil.getLog(getClass()).error(ex);
        }
        return msg;
    }

}
