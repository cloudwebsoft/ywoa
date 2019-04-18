package com.redmoon.mail.sender;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.redmoon.mail.MailSenderInfo;

public interface MailSender {
	public void sendTextMail(MailSenderInfo mailInfo) throws AddressException,
			MessagingException;

	public void sendHtmlMail(MailSenderInfo mailInfo) throws AddressException,
			MessagingException;

}
