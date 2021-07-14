<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>消息回复</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">消息中心</td>
    </tr>
  </tbody>
</table>
<table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td width="75"><div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><a href="mysend.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><a href="send.jsp"><img src="../images/newpm.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"> <img src="../images/m_delete.gif" width="60" height="60" /></div></td>
  </tr>
</table>

<br />
<table width="68%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr>
    <td width="100%" height="35" align="center">
      <jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
boolean isSuccess = false;
try {
	isSuccess = Msg.AddMsg(application, request);
}
catch (ErrMsgException e) {
	out.println(SkinUtil.makeErrMsg(request, "消息发送失败："+e.getMessage()));
}
%>
      <% if (isSuccess) { %>
      消息发送成功！
      <% } %>
    </td>
  </tr>
</table>
</body>
</html>
