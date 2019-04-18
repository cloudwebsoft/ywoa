<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.findpwd" key="findpwd"/> - <%=Global.AppName%>2</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<jsp:useBean id="sendmail" scope="page" class="cn.js.fan.mail.SendMail"/>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String name = ParamUtil.get(request, "name");
if (name.equals("")) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.findpwd", "need_user_name")));
	return;
}
UserDb user = new UserDb();
user = user.getUserDbByNick(name);
if (user==null || !user.isLoaded()) {
	String str = SkinUtil.LoadString(request, "res.label.findpwd", "user_not_exist");
	str = StrUtil.format(str, new Object[] {name} );
	out.print(SkinUtil.makeErrMsg(request, str));
	return;
}

if (user.getQuestion().equals("")) {
	String str = SkinUtil.LoadString(request, "res.label.findpwd", "question_empty");
	out.print(SkinUtil.makeErrMsg(request, str));
	return;	
}

String answer = ParamUtil.get(request, "answer");
if (!answer.equals("")) {
	if (answer.equals(user.getAnswer())) {
		String to = user.getEmail();
		// to = "dsfadasfasfasfd@bndsfdafsadff.com";
		String mailserver = Global.smtpServer;
		int smtp_port = Global.smtpPort;
		String smtpname = Global.smtpUser;
		String pwd_raw = Global.smtpPwd;
		sendmail.initSession(mailserver, smtpname, pwd_raw);		
		String senderName = Global.AppName;
		senderName = StrUtil.GBToUnicode(senderName);
		senderName = senderName + "<" + Global.email + ">";		
		String subject = StrUtil.format(SkinUtil.LoadString(request, "res.label.findpwd", "subject"), new Object[] { Global.AppName });
		String content = StrUtil.format(SkinUtil.LoadString(request, "res.label.findpwd", "content"), new Object[] { user.getRawPwd() }); // "您的密码为：" + user.getRawPwd();
		sendmail.initMsg(to, senderName, subject, content, true);
		try {
			if (sendmail.send())
				out.println(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "res.label.findpwd", "sended") + to));
			else {
				out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "res.label.findpwd", "fail_reason") + sendmail.getErrMsg()));
			}
		}
		catch (Exception e) {
			out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		}
		finally {
			sendmail.clear();		
		}
		return;
	}
	else {
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "res.label.findpwd", "err_answer")));
	}
}
%>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<form name=form1 action="findpwd2.jsp" method="post" onSubmit="return form1_onsubmit()">
<table width="46%" class="tableCommon60" border="1" align="center" cellpadding="1" cellspacing="0">
  <thead>
  <tr>
    <td height="26" colspan="2" align="center" class="text_title">
	<lt:Label res="res.label.findpwd" key="answer_question"/></td>
  </tr>
  </thead>
  <tr>
    <td width="14%"><lt:Label res="res.label.findpwd" key="user_name"/></td>
    <td width="86%"><%=name%>
      <input name="name" type=hidden value="<%=name%>" /></td>
  </tr>
  <tr>
    <td><lt:Label res="res.label.findpwd" key="question"/></td>
    <td><%=user.getQuestion()%></td>
  </tr>
  <tr>
    <td><lt:Label res="res.label.findpwd" key="answer"/></td>
    <td><input name="answer" style="width:120" /></td>
  </tr>
  <tr>
    <td colspan="2" align="center"><input name="submit" type=submit value="<lt:Label key="ok"/>" /></td>
    </tr>
  <tr>
    <td colspan="2"></td>
  </tr>
</table>
</form>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<script language="javascript">
<!--
function form1_onsubmit(){
	errmsg = "";
	if (form1.answer.value=="")
		errmsg += '<lt:Label res="res.label.findpwd" key="need_answer"/>\n'
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</html>