package com.redmoon.edm;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.redmoon.mail.MailSenderInfo;
import com.redmoon.mail.sender.MailSender;
import com.redmoon.mail.sender.SSLMailSender;
import com.redmoon.mail.sender.SimpleMailSender;

public class EDMJob implements Job {
	
    public EDMJob() {
    }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     * @todo Implement this org.quartz.Job method
     */
    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
    	send();
    }
    
    public void send() {
    	Config cfg = Config.getInstance();
    	
		String subject1 = cfg.getProperty("root.mailSubject1");
		String content1 = cfg.getProperty("root.mailContent1");
		String subject2 = cfg.getProperty("root.mailSubject2");
		String content2 = cfg.getProperty("root.mailContent2");	
		
		int sendTotal = cfg.getIntProperty("root.sendTotal");

         	
    }
    
    /**
     * 置{$field}的值
     * @param str
     * @param field
     * @param val
     * @return
     */
    public static String setFieldValue(String str, String field, String val) {
        Pattern p = Pattern.compile(
                "\\{\\$(" + field + ")\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            // String field = m.group(1);
            m.appendReplacement(sb, val);
        }
        m.appendTail(sb);

        return sb.toString();
    }    
    
    public int sendMail(String email, long id, String subject, String content) {
    	// 替换跟踪打开链接中的id，放在邮件的最前面
    	// 置试用客户ID的值
    	content = setFieldValue(content, "id", String.valueOf(id));
    	
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
		mailInfo.setToAddress(email);
		
		content = StrUtil.format(content, new Object[]{StrUtil.UrlEncode(email), StrUtil.UrlEncode(email), email});

		mailInfo.setSubject(subject);
		mailInfo.setContent(content);
		
		int r = 1;

		// 注意这里用的是有SSL验证的Sender
		MailSender sender;
		if (Global.isSmtpSSL()) {
			sender = new SSLMailSender();
		} else {
			sender = new SimpleMailSender();
		}
		try {// 发送两次，一次以html格式（此时附件会被发送），一次文本
			// sender.sendTextMail(mailInfo);
			sender.sendHtmlMail(mailInfo);
			// System.out.println("邮件已发送");
		} catch (AddressException e) {
			// System.err.println("发送失败");
			r = -1;
			e.printStackTrace();
		} catch (MessagingException e) {
			// System.err.println("发送失败");
			r = -2;
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
    }
}