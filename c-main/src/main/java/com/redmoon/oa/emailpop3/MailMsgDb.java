package com.redmoon.oa.emailpop3;

/**
 * <p>Title: 社区</p>
 * <p>Description: 社区</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: 镇江云网软件技术有限公司</p>
 * @author bluewind
 * @version 1.0
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.Conn;
import cn.js.fan.db.ListResult;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;

import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.emailpop3.pop3.MailHelper;
import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.IDesktopUnit;

public class MailMsgDb extends ObjectDb implements IDesktopUnit{
    int id;
    public String subject, content, receiver, sender,copyReceiver,blindReceiver;

    public Date sendTime;
    
    public int receiptState = 0;
    public int msgLevel = 0;
    public static final int TYPE_DRAFT = 0;
    public static final int TYPE_INBOX = 1;
    public static final int TYPE_DUSTBIN = 2;
    public static final int TYPE_SENDED = 3;
    public static final String MAIL_TYPE_CC="cc";
    public static final String MAIL_TYPE_BCC="bcc";
    public static final String MAIL_TYPE_TO="to";

    public int type = TYPE_DRAFT;

    public MailMsgDb() {
        init();
    }

    public MailMsgDb(int id) {
        this.id = id;
        init();
        load();
    }

    public void initDB() {
        tableName = "email";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new MailMsgCache(this);
        isInitFromConfigDB = false;
        QUERY_CREATE =
                "insert into email (id,subject,content,receiver,sender,msg_type,mydate,email_uid,person,is_html,email_addr,copy_receiver,blind_receiver,send_time,receipt_state,msg_level) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE =
                "update email set subject=?,content=?,receiver=?,sender=?,msg_type=?,is_readed=?,email_uid=?,copy_receiver=?,blind_receiver=?,send_time=?,receipt_state=?,msg_level=? where id=?";
        QUERY_LIST = "select id from email order by mydate desc";

        QUERY_DEL = "delete from email where id=?";
        QUERY_LOAD = "select subject,content,receiver,sender,msg_type,mydate,is_readed,email_uid,person,is_html,email_addr,copy_receiver,blind_receiver,send_time,receipt_state,msg_level from email where id=?";
    }

    public boolean create(ServletContext application, HttpServletRequest request,
                          String userName) throws
            ErrMsgException {
        MailMsgForm mf = new MailMsgForm(application, request, this);
        mf.checkCreate();
        
        EmailAddrDb emailAddrDb = null;
    	String to = mf.fileUpload.getFieldValue("to");
        String copyReceiver = mf.fileUpload.getFieldValue("cc");
        String blindReceiver = mf.fileUpload.getFieldValue("bcc");
        to = to.replaceAll("，", ",");
        String toAry[] = to.split(","); //接收者
        for(int i=0;i<toAry.length;i++){
        	emailAddrDb = new EmailAddrDb();
        	emailAddrDb.setEmailAddr(toAry[i]);
        	emailAddrDb.setUserName(userName);
        	emailAddrDb.setAddDate(new Date());
        	emailAddrDb.setDelete(false);
        	//emailAddrDb.create();
        }
        
        String[] copySendAry = null;
        if(!copyReceiver.equals("")){
        	copyReceiver = copyReceiver.replaceAll("，", ",");
        	copySendAry = copyReceiver.split(","); //抄送者
        	
        	 for(int i=0;i<copySendAry.length;i++){
        		emailAddrDb = new EmailAddrDb();
             	emailAddrDb.setEmailAddr(copySendAry[i]);
             	emailAddrDb.setUserName(userName);
             	emailAddrDb.setAddDate(new Date());
             	emailAddrDb.setDelete(false);
             	//emailAddrDb.create();
             }
        }
        String[] blindSendAry = null;
        if(!blindReceiver.equals("")){
        	blindReceiver = blindReceiver.replaceAll("，", ",");
        	blindSendAry = blindReceiver.split(","); //密送者
        	
        	for(int i=0;i<blindSendAry.length;i++){
        		emailAddrDb = new EmailAddrDb();
             	emailAddrDb.setEmailAddr(blindSendAry[i]);
             	emailAddrDb.setUserName(userName);
             	emailAddrDb.setAddDate(new Date());
             	emailAddrDb.setDelete(false);
             	//emailAddrDb.create();
             }
        }
        
        
        return create(mf.fileUpload);
    }

    private void saveMailAttachment(String visualPath, String fileName, InputStream in) {
        String diskName = FileUpload.getRandName() + "." + StrUtil.getFileExt(fileName);
        File storefile = new File(Global.getRealPath() + visualPath + File.separator + diskName);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            File f = new File(Global.getRealPath() + visualPath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            storefile.createNewFile();
            bos = new BufferedOutputStream(new FileOutputStream(storefile));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1) {
                bos.write(c);
                bos.flush();
            }

            IFileService fileService = SpringUtil.getBean(IFileService.class);
            fileService.write(Global.getRealPath() + visualPath + File.separator + diskName, visualPath, diskName, true);
            
            Attachment att = new Attachment();
            att.setEmailId(id);
            att.setName(fileName);
            att.setDiskName(diskName);
            att.setVisualPath(visualPath);
            att.setFileSize(storefile.length());
            att.create();
        }catch (java.io.IOException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        finally {
            try {
                bos.close();
            }
            catch (Exception e) {}
            try {
                bis.close();
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
    }

    public void saveAttachment(Part part, String filePath) {
        try {
            String fileName = "";
            if (part.isMimeType("multipart/*")) {
                Multipart mp = (Multipart) part.getContent();
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart mpart = mp.getBodyPart(i);
                    String disposition = mpart.getDisposition();
                    if ((disposition != null)
                        &&
                        ((disposition.equals(Part.ATTACHMENT)) || (disposition
                            .equals(Part.INLINE)))) {
                        fileName = StrUtil.getNullStr(mpart.getFileName());
                        if (fileName.toLowerCase().indexOf("gb2312") != -1 || fileName.toLowerCase().indexOf("gbk")!=-1 || fileName.toLowerCase().indexOf("gb18030") != -1) {
                            fileName = MimeUtility.decodeText(fileName);
                        }

                        // LogUtil.getLog(getClass()).info(getClass() + " saveMailAttachment fileName=" + fileName);

                        saveMailAttachment(filePath, fileName,
                                           mpart.getInputStream());
                    } else if (mpart.isMimeType("multipart/*")) {
                        saveAttachment(mpart, filePath);
                    } else {
                        fileName = mpart.getFileName();
                        if ((fileName != null)
                            && (fileName.toLowerCase().indexOf("gb2312") != -1 || fileName.toLowerCase().indexOf("gb18030") != -1))  {
                            fileName = MimeUtility.decodeText(fileName);
                            saveMailAttachment(filePath, fileName,
                                               mpart.getInputStream());
                        }
                    }
                }
            } else if (part.isMimeType("message/rfc822")) {
                saveAttachment((Part) part.getContent(), filePath);
            }
        }
        catch (MessagingException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        catch (IOException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }

    /**
     * 创建收到的邮件
     * @param mh MailHelper
     * @param emailAddress String
     * @param uid String
     * @return boolean
     * @throws ErrMsgException
     * @throws MessagingException 
     */
    public boolean create(MailHelper mh, String emailAddress, String uid) throws ErrMsgException, MessagingException {
        id = (int) SequenceManager.nextID(SequenceManager.OA_EMAIL);
        subject = mh.getSubject();
        content = mh.getBodyText();
        receiver = mh.getMailAddress(MAIL_TYPE_TO);
        //receiver = emailAddress;
        sender = mh.getFrom();
        type = TYPE_INBOX;
        readed = mh.isNew();
        myDate = mh.getSentDate();
        this.uid = uid;
        person = mh.getSendPerson();
        html = mh.isHtml();
        emailAddr = emailAddress;
        copyReceiver = mh.getMailAddress(MAIL_TYPE_CC);
        blindReceiver = mh.getMailAddress(MAIL_TYPE_BCC);
        receiptState = mh.getReplySign()?1:0;
        sendTime = mh.getSentDate();
        msgLevel = mh.isEmergent()?1:0;
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            // "insert email (id,subject,content,receiver,sender,type,mydate) values (?,?,?,?,?,?,NOW())";

            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_EMAIL);
            ps.setInt(1, id);
            ps.setString(2, subject);
            ps.setString(3, content);
            ps.setString(4, receiver);
            ps.setString(5, sender);
            ps.setInt(6, type);
            if (myDate==null)
                ps.setTimestamp(7, null);
            else
                ps.setTimestamp(7, new Timestamp(myDate.getTime()));
            ps.setString(8, uid);
            ps.setString(9, person);
            ps.setInt(10, html?1:0);
            ps.setString(11, emailAddr);
            ps.setString(12, copyReceiver);
            ps.setString(13, blindReceiver);
            if(sendTime == null){
            	ps.setTimestamp(14, null);
            }
            else {
            	//java.util.Date sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd HH:mm");
            	ps.setTimestamp(14, new Timestamp(sendTime.getTime()));
			}
            ps.setInt(15, receiptState);
            ps.setInt(16, msgLevel);

            re = conn.executePreUpdate() == 1 ? true : false;

            if (re) {
                MailMsgCache mc = new MailMsgCache(this);
                mc.refreshCreate();

                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String vpath = cfg.get("file_email") + "/" + year + "/" + month;

                saveAttachment((Part) mh.getMimeMessage(), vpath);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            // throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean modify(ServletContext application, HttpServletRequest request,
                          String userName) throws
            ErrMsgException {
        MailMsgForm mf = new MailMsgForm(application, request, this);
        mf.checkModify();
        
        EmailAddrDb emailAddrDb = null;
    	String to = mf.fileUpload.getFieldValue("to");
        String copyReceiver = mf.fileUpload.getFieldValue("cc");
        String blindReceiver = mf.fileUpload.getFieldValue("bcc");
        to = to.replaceAll("，", ",");
        String toAry[] = to.split(","); //接收者
        for(int i=0;i<toAry.length;i++){
        	emailAddrDb = new EmailAddrDb();
        	emailAddrDb.setEmailAddr(toAry[i]);
        	emailAddrDb.setUserName(userName);
        	emailAddrDb.setAddDate(new Date());
        	emailAddrDb.setDelete(false);
        	//emailAddrDb.create();
        }
        
        String[] copySendAry = null;
        if(!copyReceiver.equals("")){
        	copyReceiver = copyReceiver.replaceAll("，", ",");
        	copySendAry = copyReceiver.split(","); //抄送者
        	
        	 for(int i=0;i<copySendAry.length;i++){
        		emailAddrDb = new EmailAddrDb();
             	emailAddrDb.setEmailAddr(copySendAry[i]);
             	emailAddrDb.setUserName(userName);
             	emailAddrDb.setAddDate(new Date());
             	emailAddrDb.setDelete(false);
             	//emailAddrDb.create();
             }
        }
        String[] blindSendAry = null;
        if(!blindReceiver.equals("")){
        	blindReceiver = blindReceiver.replaceAll("，", ",");
        	blindSendAry = blindReceiver.split(","); //密送者
        	
        	for(int i=0;i<blindSendAry.length;i++){
        		emailAddrDb = new EmailAddrDb();
             	emailAddrDb.setEmailAddr(blindSendAry[i]);
             	emailAddrDb.setUserName(userName);
             	emailAddrDb.setAddDate(new Date());
             	emailAddrDb.setDelete(false);
             	//emailAddrDb.create();
             }
        }
        
        
        return save(mf.fileUpload);
    }

    public boolean delMsg(String[] ids) throws ErrMsgException {
        int len = ids.length;
        String str = "";
        for (int i = 0; i < len; i++)
            if (str.equals(""))
                str += ids[i];
            else
                str += "," + ids[i];
        str = "(" + str + ")";
        String sql = "select id from email where id in " + str;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            ResultSet rs = conn.executeQuery(sql);
            if (rs!=null) {
                while (rs.next()) {
                    getMailMsgDb(rs.getInt(1)).del();
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("delMsg:" + e.getMessage());
            throw new ErrMsgException("删除消息失败！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return true;
    }

    public MailMsgDb getMailMsgDb(int id) {
        return (MailMsgDb)getObjectDb(new Integer(id));
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public Vector getAttachments() {
        return attachments;
    }

    public java.util.Date getMyDate() {
        return myDate;
    }

    public boolean isReaded() {
        return readed;
    }

    public String getUid() {
        return uid;
    }

    public String getPerson() {
        return person;
    }

    public boolean isHtml() {
        return html;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public boolean create(FileUpload fu) throws ErrMsgException {
        Conn conn = null;
        boolean re = false;
        try {
            conn = new Conn(connname);
            // "insert email (id,subject,content,receiver,sender,type,mydate) values (?,?,?,?,?,?,NOW())";

            PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
            id = (int) SequenceManager.nextID(SequenceManager.OA_EMAIL);
            
            copyReceiver = fu.getFieldValue("cc");
            blindReceiver = fu.getFieldValue("bcc");
            ps.setInt(1, id);
            ps.setString(2, subject);
            ps.setString(3, content);
            ps.setString(4, receiver);
            ps.setString(5, sender);
            ps.setInt(6, type);
            ps.setTimestamp(7, new Timestamp(new java.util.Date().getTime()));
            ps.setString(8, uid);
            ps.setString(9, person);
            ps.setInt(10, html?1:0);
            ps.setString(11, emailAddr);
            ps.setString(12, copyReceiver);
            ps.setString(13, blindReceiver);
            if(sendTime == null){
            	ps.setTimestamp(14, null);
            }
            else {
            	ps.setTimestamp(14, new Timestamp(sendTime.getTime()));
			}
            ps.setInt(15, receiptState);
            ps.setInt(16, msgLevel);

            re = conn.executePreUpdate() == 1;

            MailMsgCache mc = new MailMsgCache(this);
            mc.refreshCreate();

            if (re) {
                if (fu.getRet() == FileUpload.RET_SUCCESS) {
                    // 置保存路径
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH) + 1;

                    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                    String vpath = cfg.get("file_email") + "/" + year + "/" + month;

                    IFileService fileService = SpringUtil.getBean(IFileService.class);

                    Vector<FileInfo> v = fu.getFiles();
                    FileInfo fi;
                    for (FileInfo fileInfo : v) {
                        fi = fileInfo;
                        fileService.write(fi, vpath);

                        Attachment att = new Attachment();
                        att.setEmailId(id);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setVisualPath(vpath);
                        att.setFileSize(fi.getSize());
                        re = att.create();
                    }
                }

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String file_email = cfg.get("file_email");
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(Calendar.YEAR));
                String month = "" + (cal.get(Calendar.MONTH) + 1);

                String vpath = file_email + "/" + year + "/" + month;
                String filepath = Global.getRealPath() + vpath;

                // 被转发邮件的附件
                String[] attachmentFiles = fu.getFieldValues("attachmentFiles");
                LogUtil.getLog(getClass()).info("create: attachmentFiles=" + attachmentFiles);
                if (attachmentFiles!=null) {
                    int len = attachmentFiles.length;

                    LogUtil.getLog(getClass()).info("create: attachmentFiles.len" + attachmentFiles.length);

                    Attachment attMail;
                    for (int i=0; i<len; i++) {
                        attMail = new Attachment(StrUtil.toInt(attachmentFiles[i]));

                        // 将转发邮件的附件同时也另存至草稿箱
                        String newDiskName = RandomSecquenceCreator.getId() + "." +
                                         StrUtil.getFileExt(attMail.getDiskName());

                        IFileService fileService = SpringUtil.getBean(IFileService.class);
                        fileService.copy(attMail.getVisualPath(), attMail.getDiskName(), vpath, newDiskName);

                        Attachment att = new Attachment();
                        att.setEmailId(id);
                        att.setName(attMail.getName());
                        att.setDiskName(newDiskName);
                        att.setVisualPath(vpath);
                        re = att.create();
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
            throw new ErrMsgException("数据库操作错误！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public int createEmail(FileUpload fu) throws ErrMsgException {
    	Conn conn = null;
    	boolean re;
    	try {
    		conn = new Conn(connname);

    		PreparedStatement ps = conn.prepareStatement(QUERY_CREATE);
    		id = (int) SequenceManager.nextID(SequenceManager.OA_EMAIL);
    		
    		copyReceiver = fu.getFieldValue("cc");
    		blindReceiver = fu.getFieldValue("bcc");
    		ps.setInt(1, id);
    		ps.setString(2, subject);
    		ps.setString(3, content);
    		ps.setString(4, receiver);
    		ps.setString(5, sender);
    		ps.setInt(6, type);
    		ps.setTimestamp(7, new Timestamp(new java.util.Date().getTime()));
    		ps.setString(8, uid);
    		ps.setString(9, person);
    		ps.setInt(10, html?1:0);
    		ps.setString(11, emailAddr);
    		ps.setString(12, copyReceiver);
    		ps.setString(13, blindReceiver);
    		if(sendTime == null){
    			ps.setTimestamp(14, null);
    		}
    		else {
    			ps.setTimestamp(14, new Timestamp(sendTime.getTime()));
    		}
    		ps.setInt(15, receiptState);
    		ps.setInt(16, msgLevel);
    		
    		re = conn.executePreUpdate() == 1 ? true : false;
    		
    		MailMsgCache mc = new MailMsgCache(this);
    		mc.refreshCreate();
    		
    		if (re) {
    			if (fu.getRet() == FileUpload.RET_SUCCESS) {
    				// 置保存路径
    				Calendar cal = Calendar.getInstance();
    				int year = cal.get(Calendar.YEAR);
    				int month = cal.get(Calendar.MONTH) + 1;
    				
    				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    				String vpath = cfg.get("file_email") + "/" + year + "/" + month;

                    IFileService fileService = SpringUtil.getBean(IFileService.class);
                    Vector<FileInfo> v = fu.getFiles();
                    for (FileInfo fi : v) {
                        fileService.write(fi, vpath);

                        Attachment att = new Attachment();
                        att.setEmailId(id);
                        att.setName(fi.getName());
                        att.setDiskName(fi.getDiskName());
                        att.setVisualPath(vpath);
                        att.setFileSize(fi.getSize());
                        re = att.create();
                    }
    			}
    			
    			com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    			String file_email = cfg.get("file_email");
    			Calendar cal = Calendar.getInstance();
    			String year = "" + (cal.get(Calendar.YEAR));
    			String month = "" + (cal.get(Calendar.MONTH) + 1);
    			
    			String vpath = file_email + "/" + year + "/" + month;

    			// 被转发邮件的附件
    			String[] attachmentFiles = fu.getFieldValues("attachmentFiles");
    			LogUtil.getLog(getClass()).info("create: attachmentFiles=" + attachmentFiles);
    			if (attachmentFiles!=null) {
    				int len = attachmentFiles.length;
    				
    				LogUtil.getLog(getClass()).info("create: attachmentFiles.len" + attachmentFiles.length);
    				
    				Attachment attMail;
                    for (String attachmentFile : attachmentFiles) {
                        attMail = new Attachment(StrUtil.toInt(attachmentFile));

                        // 将转发邮件的附件同时也另存至草稿箱
                        String newDiskName = RandomSecquenceCreator.getId() + "." +
                                StrUtil.getFileExt(attMail.getDiskName());
                        // 拷贝文件
                        IFileService fileService = SpringUtil.getBean(IFileService.class);
                        fileService.copy(attMail.getVisualPath(), attMail.getDiskName(), vpath, newDiskName);

                        Attachment att = new Attachment();
                        att.setEmailId(id);
                        att.setName(attMail.getName());
                        att.setDiskName(newDiskName);
                        att.setVisualPath(vpath);
                        re = att.create();
                    }
    			}
    		}
    	} catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
    		throw new ErrMsgException("数据库操作错误！");
    	} catch (IOException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
    	    conn.close();
    	}
    	return id;
    }

    public boolean save(FileUpload fu) throws ErrMsgException {
        boolean re = save();
        if (re) {
            if (fu.getRet() == FileUpload.RET_SUCCESS) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);

                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String vpath = cfg.get("file_email") + "/" + year + "/" + month;

                IFileService fileService = SpringUtil.getBean(IFileService.class);
                Vector<FileInfo> v = fu.getFiles();
                for (FileInfo fi : v) {
                    fileService.write(fi, vpath);

                    Attachment att = new Attachment();
                    att.setEmailId(id);
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setVisualPath(vpath);
                    att.setFileSize(fi.getSize());
                    re = att.create();
                }
            }
        }

        return re;
    }

    @Override
    public ObjectDb getObjectDb(Object primaryKeyValue) {
        MailMsgCache uc = new MailMsgCache(this);
        primaryKey.setValue(primaryKeyValue);
        return uc.getObjectDb(primaryKey);
    }

    /**
     * 删除所有对应于sender的邮件
     * @param email String 邮箱名称
     * @return boolean
     */
    public boolean delOfSender(String email) {
        boolean re = false;
        ResultSet rs;
        Conn conn = new Conn(connname);
        try {
            String sql = "select id from email where sender=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            rs = conn.executePreQuery();
            if (rs!=null) {
                while (rs.next()) {
                    MailMsgDb mmd = getMailMsgDb(rs.getInt(1));
                    re = mmd.del();
                }
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        return re;
    }

    @Override
    public synchronized boolean del() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_DEL);
            ps.setInt(1, id);
            re = conn.executePreUpdate() == 1;
            if (re) {
                MailMsgCache mc = new MailMsgCache(this);
                mc.refreshDel(primaryKey);
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        } finally {
            conn.close();
        }
        if (re) {
            // 删除附件
            if (attachments!=null) {
                Iterator ir = attachments.iterator();
                while (ir.hasNext()) {
                    Attachment att = (Attachment)ir.next();
                    att.del();
                }
            }

            MailMsgCache uc = new MailMsgCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshDel(primaryKey);

        }
        return re;
    }

    public int getObjectCount(String sql) {
        MailMsgCache uc = new MailMsgCache(this);
        return uc.getObjectCount(sql);
    }

    public int getMessageCount(String sql) {
        return getObjectCount(sql);
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        MailMsgCache dcm = new MailMsgCache(this);
        return dcm.getObjectBlock(query, startIndex);
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new MailMsgDb(pk.getIntValue());
    }

    public synchronized boolean save() {
        boolean re = false;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(QUERY_SAVE);
            ps.setString(1, subject);
            ps.setString(2, content);
            ps.setString(3, receiver);
            ps.setString(4, sender);
            ps.setInt(5, type);
            ps.setInt(6, readed?1:0);
            ps.setString(7, uid);
            ps.setString(8, copyReceiver);
            ps.setString(9, blindReceiver);
            if(sendTime == null){
            	ps.setTimestamp(10, null);
            }
            else {
            	ps.setTimestamp(10, new Timestamp(sendTime.getTime()));
			}
            ps.setInt(11, receiptState);
            ps.setInt(12, msgLevel);

            ps.setInt(13, id);
            re = conn.executePreUpdate() == 1 ? true : false;
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        if (re) {
            MailMsgCache uc = new MailMsgCache(this);
            primaryKey.setValue(new Integer(id));
            uc.refreshSave(primaryKey);
        }
        return re;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    public Attachment getAttachment(int attId) {
        Iterator ir = attachments.iterator();
        while (ir.hasNext()) {
            Attachment at = (Attachment)ir.next();
            if (at.getId()==attId)
                return at;
        }
        return null;
    }

    public boolean delAttachment(int attachId) {
        Attachment att = new Attachment(attachId);
        if (att==null)
            return false;
        boolean re = att.del();
        MailMsgCache rc = new MailMsgCache(this);
        primaryKey.setValue(new Integer(id));
        rc.refreshSave(primaryKey);
        return re;
    }

    /**
     * 检查邮件是否已被收取
     * @param uid String
     * @return boolean
     */
    public static boolean isMailExist(String uid) {
        String sql = "select id from email where email_uid=?";
        boolean re = false;
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {uid});
            if (ri.hasNext()) {
                re = true;
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(MailMsgDb.class).error(StrUtil.trace(e));
        }
        return re;
    }

    public synchronized void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null && rs.next()) {
                subject = rs.getString(1);
                content = rs.getString(2);
                receiver = rs.getString(3);
                sender = rs.getString(4);
                type = rs.getInt(5);
                myDate = rs.getTimestamp(6);
                readed = rs.getInt(7)==1;
                uid = rs.getString(8);
                person = StrUtil.getNullStr(rs.getString(9));
                html = rs.getInt(10)==1;
                emailAddr = rs.getString(11);
                copyReceiver = rs.getString(12);
                blindReceiver = rs.getString(13);
                sendTime = rs.getTimestamp(14);
                receiptState = rs.getInt(15);
                msgLevel = rs.getInt(16);

                loaded = true;

                primaryKey.setValue(new Integer(id));

                String LOAD_DOCUMENT_ATTACHMENTS =
                        "SELECT id FROM email_attach WHERE emailId=?";
                attachments = new Vector();
                pstmt = conn.prepareStatement(LOAD_DOCUMENT_ATTACHMENTS);
                pstmt.setInt(1, id);
                rs = conn.executePreQuery();
                if (rs != null) {
                    while (rs.next()) {
                        int aid = rs.getInt(1);
                        Attachment am = new Attachment(aid);
                        attachments.addElement(am);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("load: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public void setAttachments(Vector attachments) {
        this.attachments = attachments;
    }

    public void setMyDate(java.util.Date myDate) {
        this.myDate = myDate;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    @Override
    public ListResult listResult(String listsql, int curPage, int pageSize) throws
            ErrMsgException {
        int total = 0;
        ResultSet rs = null;
        Vector result = new Vector();

        ListResult lr = new ListResult();
        lr.setTotal(total);
        lr.setResult(result);

        Conn conn = new Conn(connname);
        try {
            // 取得总记录条数
            String countsql = SQLFilter.getCountSql(listsql);
            rs = conn.executeQuery(countsql);
            if (rs != null && rs.next()) {
                total = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }

            if (total != 0)
                conn.setMaxRows(curPage * pageSize); // 尽量减少内存的使用

            rs = conn.executeQuery(listsql);
            if (rs == null) {
                return lr;
            } else {
                rs.setFetchSize(pageSize);
                int absoluteLocation = pageSize * (curPage - 1) + 1;
                if (rs.absolute(absoluteLocation) == false) {
                    return lr;
                }
                do {
                    MailMsgDb mmd = getMailMsgDb(rs.getInt(1));
                    result.addElement(mmd);
                } while (rs.next());
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
            throw new ErrMsgException("数据库出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }

        lr.setResult(result);
        lr.setTotal(total);
        return lr;
    }

    private Vector<Attachment> attachments;
    private java.util.Date myDate;
    private boolean readed = false;
    /**
     * 邮件唯一标识
     */
    private String uid;
    /**
     * 邮件用户名，如：存至发件箱时，保存用户名
     */
    private String person;
    private boolean html = true;
    private String emailAddr;

	public String getCopyReceiver() {
		return copyReceiver;
	}

	public void setCopyReceiver(String copyReceiver) {
		this.copyReceiver = copyReceiver;
	}

	public String getBlindReceiver() {
		return blindReceiver;
	}

	public void setBlindReceiver(String blindReceiver) {
		this.blindReceiver = blindReceiver;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public int getReceiptState() {
		return receiptState;
	}

	public void setReceiptState(int receiptState) {
		this.receiptState = receiptState;
	}

	public int getMsgLevel() {
		return msgLevel;
	}

	public void setMsgLevel(int msgLevel) {
		this.msgLevel = msgLevel;
	}

	@Override
	public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
		DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String str = "";
        EmailPop3Db epd = new EmailPop3Db();
        Privilege privilege = new Privilege();
        Iterator ir1 = epd.getEmailPop3DbOfUser(privilege.getUser(request)).iterator();
        String email = "";
        while (ir1.hasNext()) {
        	epd = (EmailPop3Db)ir1.next();
        	if(ir1.hasNext()){
        		email += StrUtil.sqlstr(epd.getEmail())+",";
        	}else{
        		email += StrUtil.sqlstr(epd.getEmail());
        	}
        }
        
        if(!email.equals("")){
        	MailMsgDb mmd = new MailMsgDb();
            String sql = "select id,sender,subject,mydate,is_readed,email_addr from email where email_addr in (" + email + ") and msg_type=" + MailMsgDb.TYPE_INBOX+" and is_readed=0 order by mydate desc";
            try {
                ListResult lr = mmd.listResult(sql, 1, uds.getCount());
                Iterator ir = lr.getResult().iterator();
                String emailSubject = "";
                if(ir.hasNext()) {
                	str += "<table class='article_table'>";
                	while (ir.hasNext()) {
                    	mmd = (MailMsgDb)ir.next();
                    	if(mmd.getSubject().equals("") || mmd.getSubject() == null){
                    		emailSubject = "无主题";
                    	}else{
                    		emailSubject = StrUtil.toHtml(mmd.getSubject());
                    	}
                        str += "<tr><td class='article_content'><a title='" + emailSubject + "' href='" + du.getPageShow()+"?id="+ mmd.getId()+"&from=desktop&emailAddr="+mmd.getEmailAddr() + "'>" +
                        emailSubject + "</a></td><td class='article_time'>[" +
                                DateUtil.format(mmd.getMyDate(), "yyyy-MM-dd") +
                                "]</td></tr>";
                    }
                	str += "</table>";
                }else{
                	str = "<div class='no_content'><img title='暂无邮件' src='images/desktop/no_content.jpg'></div>";
                }
            }
            catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
            
        }else{
        	str = "<div class='no_content'><img src='images/desktop/no_content.jpg'></div>";
        }
        
        return str;
	}

	@Override
	public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds) {
		DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
	}

}
