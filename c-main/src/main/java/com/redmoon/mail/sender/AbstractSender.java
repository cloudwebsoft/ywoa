package com.redmoon.mail.sender;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import com.redmoon.mail.MailSenderInfo;

/**
 * 该类实现了sendHtmlMail  
 * sendTextMail  ,只是分别将text ,html 部分的信息装到Message 对像中，而像from , to , port 等，未进行操作
 * 但是此类并不能直接使用，
 * 它自已定义了一个populateCommonInfo（）
 * 需要子类进行实现，将MailSenderInfo 信息封装到Message 对像中
 * 因为有些连接 需要用到SSL ,而有些则不用，是以让它们的子类分别在populateCommonInfo中进行信息的组装
 * ，究竟是用ssl 还是不用，需要客户自已判断,然后使用相应的子类
 * 已知的两个子类SimpleMailSender 
 * SSLMailSender
 *   
 * @author Administrator
 *
 */
public abstract class AbstractSender implements MailSender {

	protected Message populateCommonInfo(MailSenderInfo mailInfo)
			throws MessagingException {
		return null;
	}

	@Override
	public void sendHtmlMail(MailSenderInfo mailInfo) throws AddressException,
			MessagingException {

		Message msg = populateCommonInfo(mailInfo);

		Multipart mainPart = new MimeMultipart();
		// 创建一个包含HTML内容的MimeBodyPart

		// 设置信件的附件
		if (mailInfo.getAttachFileNames()!=null) {
			for (File f : mailInfo.getAttachFileNames()) {
				BodyPart filePart = new MimeBodyPart();
				DataHandler dh = new DataHandler(new FileDataSource(f));
				filePart.setFileName(f.getName());//
				filePart.setDataHandler(dh);
	
				mainPart.addBodyPart(filePart);
			}
		}

		BodyPart html = new MimeBodyPart();
		// 设置HTML内容
		html.setContent(mailInfo.getContent(), "text/html; charset=utf-8");
		mainPart.addBodyPart(html);
		// 将MiniMultipart对象设置为邮件内容
		msg.setContent(mainPart);

		Transport.send(msg);

	}

	@Override
	public void sendTextMail(MailSenderInfo mailInfo) throws AddressException,
			MessagingException {
		Message msg = populateCommonInfo(mailInfo);
		msg.setText(mailInfo.getContent());
		Transport.send(msg);

	}

}
