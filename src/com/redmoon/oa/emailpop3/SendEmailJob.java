package com.redmoon.oa.emailpop3;

import java.util.Calendar;
import java.util.Iterator;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.security.ThreeDesUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class SendEmailJob implements Job{

	
	public SendEmailJob(){
		super();
	}
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		String errinfo = "";
		com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
		
		
		String tableName = "email";
		Calendar now = Calendar.getInstance();
		//String sql_query = "select id from "+tableName+ " where msg_type = "+MailMsgDb.TYPE_DRAFT+" and send_time <= "+SQLFilter.getDateStr(StrUtil.FormatDate(now.getTime(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
		String sql_query = "select id from "+tableName+ " where msg_type = "+MailMsgDb.TYPE_DRAFT+" and send_time <= now()";

		MailMsgDb mailMsgDb = new MailMsgDb();
		Iterator i = mailMsgDb.list(sql_query).iterator();
		while(i.hasNext()){
			mailMsgDb = (MailMsgDb)i.next();
			mailMsgDb.setType(MailMsgDb.TYPE_INBOX);
			int id = mailMsgDb.getId();
			String receiver = mailMsgDb.getReceiver();
			String copyReceiver = mailMsgDb.getCopyReceiver();
			String blindReceiver = mailMsgDb.getBlindReceiver();
			String sender = mailMsgDb.getSender();
			String subject = mailMsgDb.getSubject();
			String content = mailMsgDb.getContent();
			int receipt_state = mailMsgDb.getReceiptState();
			int msg_level = mailMsgDb.getMsgLevel();
			
			
			String receitpState = String.valueOf(receipt_state).equals("0")?"":String.valueOf(receipt_state);
			String msgLevel = String.valueOf(msg_level).equals("0")?"":String.valueOf(msg_level);
			
			UserPop3Setup userpop3setup = new UserPop3Setup();
	        userpop3setup.getUserPop3Setup(sender);
	        String mailserver = userpop3setup.getMailServer();
	        String smtp_port = "" + userpop3setup.getSmtpPort();
	        String email_name = userpop3setup.getUser();
	        String email_pwd_raw = userpop3setup.getPwd();
	        
	        email_pwd_raw = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb", email_pwd_raw);
			
	        SendMail sendMail = new SendMail();
	        
	        try {
	        	sendMail.initSession(mailserver, smtp_port, email_name, email_pwd_raw, userpop3setup.isSsl());
	        	sendMail.setFrom(sender);
	        } catch (Exception e) {
	            errinfo = e.getMessage();
	            return;
	        }
			
	        try {
	        	
	        	receiver = receiver.replaceAll("，", ",");
		        String toAry[] = receiver.split(","); //接收者
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
	                
		        sendMail.initMsgCopy(toAry ,copySendAry, blindSendAry, subject, content, true,receitpState,sender,msgLevel);
	        	
	        	
	        	//sendMail.initMsg(receiver, subject, content, true);
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        }

	        // 处理附件
	        java.util.Iterator ir = mailMsgDb.getAttachments().iterator();
	        while (ir.hasNext()) {
	            Attachment att = (Attachment) ir.next();
	            try {
	            	sendMail.setAttachFile(att.getFullPath(), att.getName());
	            } catch (Exception e2) {
	               e2.printStackTrace();
	            }
	        }
			
	        try {
				sendMail.send();
			} catch (ErrMsgException e) {
				e.printStackTrace();
			}
			
			mailMsgDb = (MailMsgDb)mailMsgDb.getObjectDb(new Integer(id));
			mailMsgDb.setType(MailMsgDb.TYPE_SENDED);
			mailMsgDb.save();
		}
		
	}

}
