<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.emailpop3.EmailPop3Db"%>
<%@page import="com.redmoon.oa.emailpop3.MailMsgDb"%>
<%@page import="com.redmoon.oa.emailpop3.MailMsgMgr"%>
<%@page import="com.redmoon.oa.ui.LocalUtil"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="sendmail" scope="page" class="com.redmoon.oa.emailpop3.SendMail" />
<jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
int emailId = ParamUtil.getInt(request, "emailId", -1);
int draftId = ParamUtil.getInt(request, "draftId", -1);
int subMenu = ParamUtil.getInt(request, "subMenu", -1);
int id = ParamUtil.getInt(request, "id", -1);//草稿箱邮箱发件成功的时候，删除草稿箱里的数据
int subMenuButton =subMenu * 4 -2;
String userName = privilege.getUser(request);

String email = "";
if (emailId!=-1) {
	EmailPop3Db epd = new EmailPop3Db();
	epd = epd.getEmailPop3Db(emailId);
	email = epd.getEmail();
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>发送邮件</title>
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />
</head>

<body>
<div class="inbox-initial-send">
	  <div class="inbox-initial-sendOK">
	  	  <img src="images/inbox-icon-ok.png" width="54" height="54"/>&nbsp;&nbsp;&nbsp;
	  	  <img src="images/inbox-icon-hook.png" width="54" height="54"/>&nbsp;&nbsp;&nbsp;
	  	    <%
				String priv = "read";
				if (!privilege.isUserPrivValid(request, priv)) {
					out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request,
							cn.js.fan.web.SkinUtil.LoadString(request,
									"pvg_invalid")));
					return;
				}
				sendmail.getMailInfo(application, request);
				
				boolean re = false;
				if(sendmail.geterrinfo() == null ||  sendmail.geterrinfo().equals("")){
					if(draftId != -1){
						MailMsgMgr mmm = new MailMsgMgr();
						mmm.del(draftId, false);
					}
					re = sendmail.send();
				}
				
				
				if (re){
					out.println("发送成功！");
				}
				else {
					
					String errMsg = sendmail.geterrinfo();
					
					if(errMsg.equals("扩展名非法")){
						errMsg = LocalUtil.LoadString(request,"res.label.email_m","ext");
					}else if(errMsg.equals("文件大小不能超过30M")){
						errMsg = LocalUtil.LoadString(request,"res.label.email_m","fileLarge");
					}else if(errMsg.equals("上传失败")){
						errMsg = LocalUtil.LoadString(request,"res.label.email_m","uploadFail");
					}else if(errMsg.equals("No recipient addresses")){
						errMsg = LocalUtil.LoadString(request,"res.label.email_m","noAddress");
					}else if(errMsg.equals("Exception reading response")){
						errMsg = LocalUtil.LoadString(request,"res.label.email_m","readException");
					}
					out.println(LocalUtil.LoadString(request,"res.label.email_m","reason") + errMsg);
				}
				
			%>
	  </div>
   	  <div class="inbox-initial-send-btnbox">
   	  	<%if(re){ 
	   	  	MailMsgDb mailMsgDb = new MailMsgDb();
			mailMsgDb = mailMsgDb.getMailMsgDb(sendmail.getEmailId());
			
			if(id != -1){
				MailMsgMgr mmm = new MailMsgMgr();
				mmm.del(id,false);
			}
			
			EmailPop3Db epd = new EmailPop3Db();
			epd = epd.getEmailPop3Db(privilege.getUser(request), mailMsgDb.getEmailAddr());
   	  	%>
	    <div class="inbox-initial-send-btn" onclick="window.location.href = 'in_box.jsp?id=<%=epd.getId() %>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton-1 %>'">返回收件箱</div>
	    <div class="inbox-initial-send-btn" onclick="window.location.href = 'mail_show.jsp?id=<%=mailMsgDb.getId() %>&emailAddr=<%=mailMsgDb.getEmailAddr() %>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton %>&box=<%=MailMsgDb.TYPE_SENDED %>'">查看已发邮件</div>
	    <div class="inbox-initial-send-btn"  onclick="window.location.href = 'send_mail.jsp?emailId=<%=epd.getId() %>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton-1 %>&writeMail=1'">继续写信</div>
	    <%} %>
	  </div>
  </div>
</body>
</html>
