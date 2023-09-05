package com.redmoon.mail;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.mail.sender.MailSender;
import com.redmoon.mail.sender.SSLMailSender;
import com.redmoon.mail.sender.SimpleMailSender;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

public class MailUtil {

    public static int sendHtmlMail(String email, String subject, String content) {
        int r = 1;
        MailSenderInfo mailInfo = new MailSenderInfo();

        String mailserver = Global.getSmtpServer();
        int smtpPort = Global.getSmtpPort();
        String name = Global.getSmtpUser();
        String pwdRaw = Global.getSmtpPwd();

        mailInfo.setMailServerHost(mailserver);
        mailInfo.setMailServerPort(String.valueOf(smtpPort));
        // mailInfo.setValidate(true);
        mailInfo.setUserName(name);
        mailInfo.setPassword(pwdRaw);// 邮箱密码
        mailInfo.setFromAddress(Global.getEmail());
        mailInfo.setToAddress(email);

        content = StrUtil.format(content, new Object[]{StrUtil.UrlEncode(email), StrUtil.UrlEncode(email), email});

        mailInfo.setSubject(subject);
        mailInfo.setContent(content);

        // 注意这里用的是有SSL验证的Sender
        MailSender sender;
        if (Global.isSmtpSSL()) {
            sender = new SSLMailSender();
        } else {
            sender = new SimpleMailSender();
        }
        try {
            sender.sendHtmlMail(mailInfo);
        }
        catch (AddressException e) {
            r = -1;
            LogUtil.getLog(MailUtil.class).error(e);
        }
        catch (MessagingException e) {
            r = -2;
            LogUtil.getLog(MailUtil.class).error(e);
        }
        return r;
    }
}
