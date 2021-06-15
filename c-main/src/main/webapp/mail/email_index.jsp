<%@page import="com.redmoon.oa.emailpop3.EmailPop3Db"%>
<%@page import="java.util.Vector"%>
<%@ page contentType="text/html;charset=utf-8"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
//检查用户是否已设置有邮箱，没有则重定向至邮箱配置界面
EmailPop3Db epd = new EmailPop3Db();
Vector v = epd.getEmailPop3DbOfUser(privilege.getUser(request));
if (v.size()!=0) {
	response.sendRedirect("mail_frame.jsp");
	return;
}
%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>邮箱设置</title>
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />
<script>
	function setEmail(){
		//window.location.href = "pop3_setup.jsp";
		window.location.href = "email_pop_set.jsp";
	}
</script>

</head>

<body>
<!--设置初始第一步-->
<div class="inbox-set-initial">
  <div>
    <div class="inbox-set-initial-tx">请设置您的邮箱！</div>
    <div class="inbox-set-initial-btn" onclick="setEmail()">我要设置</div>
  </div>
</div>
</body>
</html>
