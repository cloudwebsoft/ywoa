<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发送邮件</title>
<link href="mail.css" type="text/css" rel="stylesheet" />
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="sendmail" scope="page" class="com.redmoon.oa.emailpop3.SendMail" />
<jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe_gray">
  <tr>
    <td height="23" class="menuBoxC">&nbsp;&nbsp;群&nbsp;发 
      邮 件</td>
  </tr>
  <tr>
    <td valign="top"></p></td>
  </tr>
  <tr>
    <td height="9"><table width="100%" border="0" align="center" cellpadding="3" cellspacing="0">
      <tr>
        <td height="176" align="center" bgcolor="#FFFFFF"><%
String priv="read"; // "admin.emailgroup";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

sendmail.sendMailGroup(application, request, out);
%>
          <br />
          <div style="text-align:center"><a href="#" onclick="window.history.back()">点击此处返回</a></div></td>
      </tr>
    </table></td>
  </tr>
</table>
</body>
</html>
