package com.redmoon.mail;

import java.io.File;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.mail.sender.MailSender;
import com.redmoon.mail.sender.SSLMailSender;

//注意好像qq 不能接收txt 格式 的附件 ， 其他格式的可以，不知问题出在哪
public class TestSSLSender {
	public static void testGMailSender() {
		// 这个类主要是设置邮件
		MailSenderInfo mailInfo = new MailSenderInfo();
		
		String mailserver = Global.getSmtpServer();
		int smtp_port = Global.getSmtpPort();
		String name = Global.getSmtpUser();
		String pwd_raw = Global.getSmtpPwd();		
		
		mailInfo.setMailServerHost(mailserver);
		mailInfo.setMailServerPort(String.valueOf(smtp_port));
		// mailInfo.setValidate(true);
		mailInfo.setUserName(name);
		mailInfo.setPassword(pwd_raw);// 您的邮箱密码
		mailInfo.setFromAddress(Global.getEmail());
		mailInfo.setToAddress("bestfeng@163.com");
		mailInfo.setSubject("设置邮箱标题 如http://www.guihua.org 中国桂花网");
		mailInfo.setContent("设置邮箱内容 如http://www.guihua.org 中国桂花网 是中国最大桂花网站==");
		/*
		File f1 = new File("d:\\text.txt");
		File f2 = new File("d:\\text.rar");
		File f3 = new File("d:\\1.jpg");
		mailInfo.setAttachFileNames(new File[] { f1, f2, f3 });
		*/

		// 注意这里用的是有SSL验证的Sender
		MailSender sender = new SSLMailSender();
		try {// 发送两次，一次以html格式（此时附件会被发送），一次文本
			// sender.sendTextMail(mailInfo);
			sender.sendHtmlMail(mailInfo);
		} catch (MessagingException e) {
			LogUtil.getLog(TestSSLSender.class).error(e);
		}
	}

	public static void testQQSender() {
		// 这个类主要是设置邮件
		MailSenderInfo mailInfo = new MailSenderInfo();
		mailInfo.setMailServerHost("smtp.exmail.qq.com");
		mailInfo.setMailServerPort("465");
		// mailInfo.setValidate(true);
		mailInfo.setUserName("fangangfeng@cloudwebsoft.com");
		mailInfo.setPassword("cloudweb956");// 您的邮箱密码
		mailInfo.setFromAddress("fangangfeng@cloudwebsoft.com");
		mailInfo.setToAddress("bestfeng@163.com");
		mailInfo.setSubject("qq javamail 中车465 ssl ");
		mailInfo.setContent("qq javamail中文 465 ssl ,just a test");
		// File f1 = new File("d:\\text.txt");
		// mailInfo.setAttachFileNames(new File[] { f1 });
		// 注意这里用的是有SSL验证的Sender
		MailSender sender = new SSLMailSender();
		try {
			// 发送两次，一次以html格式（此时附件会被发送），一次文本
			// sender.sendTextMail(mailInfo);
			sender.sendHtmlMail(mailInfo);
		} catch (MessagingException e) {
			LogUtil.getLog(TestSSLSender.class).error(e);
		}
	}

	public static void main(String[] args) {
		TestSSLSender.testQQSender();
		// TestSSLSender.testGMailSender();
	}

}
