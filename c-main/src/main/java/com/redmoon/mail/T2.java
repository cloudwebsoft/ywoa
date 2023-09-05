package com.redmoon.mail;

import com.cloudwebsoft.framework.util.LogUtil;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class T2 {
	String host = "";
	String user = "";
	String password = "";

	public void setHost(String host) {
		this.host = host;
	}

	public void setAccount(String user, String password) {
		this.user = user;
		this.password = password;
	}

	public void send(String from, String to, String subject, String content) {
		Properties props = new Properties();
		props.put("mail.smtp.host", host);// 指定SMTP服务器
		props.put("mail.smtp.auth", "true");// 指定是否需要SMTP验证

		try {
			Session mailSession = Session.getDefaultInstance(props);
			mailSession.setDebug(true);// 是否在控制台显示debug信息

			Message message = new MimeMessage(mailSession);
			message.setFrom(new InternetAddress(from));// 发件人
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));// 收件人
			message.setSubject(subject);// 邮件主题
			message.setText(content);// 邮件内容
			message.saveChanges();
			Transport transport = mailSession.getTransport("smtp");
			transport.connect(host, user, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

	public static void main(String args[]) {
		T2 sm = new T2();
		sm.setHost("smtp.gmail.com");// 指定要使用的邮件服务器
		sm.setAccount("jixiuf@gmail.com", "21709891");// 指定帐号和密码

		sm.send("jixiuf@gmail.com", "jixiuf@qq.com", "title", "new java");
	}

}
