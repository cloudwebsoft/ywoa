<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>消息回复</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<script src="../../inc/common.js"></script>
<script src="../../inc/upload.js"></script>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
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
<%
String title,receiver;
title = ParamUtil.get(request, "title");
receiver = ParamUtil.get(request, "receiver");

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "title", title, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "receiver", receiver, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
  
%>
<table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td width="75"><div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><a href="listoutbox.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><img src="../images/newpm.gif" width="60" height="60" border="0" /></div></td>
    <td width="75"><div align="center"><img src="../images/m_delete.gif" width="60" height="60" /></div></td>
  </tr>
</table>
<br />
<form name="form1" method="post" action="myreplytodb.jsp" enctype="multipart/form-data">
<table class="tabStyle_1 percent80" width="80%" border="0" cellspacing="0" cellpadding="3" align="center">
  <tr>
    <td class="tabStyle_1_title" height="26" colspan="2" align="center">回复消息</td>
  </tr>
  <tr>
    <td height="26" align="center">接 收 者：</td>
    <td height="26"><%
				com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
			  	ud = ud.getUserDb(receiver);
				String userRealName = ud.getRealName();
			    %>
        <input type="hidden" name="receiver" value="<%=receiver %>" />
        <input type="hidden" name="isDraft" value="false" />
		<%=userRealName%>
        <input type="hidden" name="userRealName" class="input1" size="20" maxlength="20" value="<%=userRealName %>" />    </td>
  </tr>
  <tr>
    <td height="26" align="center">消息标题：</td>
    <td height="26"><input type="text" name="title" class="input1" size="40" value="<%=title%>" />
    </td>
  </tr>
  <tr>
    <td height="26" align="center">消息内容：</td>
    <td height="26"><textarea name="content" cols="50" rows="16"></textarea>    </td>
  </tr>
  <tr>
    <td height="26" align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
    <td height="26"><script>initUpload()</script></td>
  </tr>
  <tr>
    <td align="right">发送时间：</td>
    <td align="left"><input type="radio" name="send_now" value="yes" id="send_now_0" checked="checked" onclick="setVisibility()" />
        <label for="send_now_0" onclick="setVisibility()">立即发送</label>
        <br />
        <input type="radio" name="send_now" value="no" id="send_now_1" onclick="setVisibility()" />
        <label for="send_now_1" onclick="setVisibility()">定时发送</label>
        <input name="date" readonly="readonly" />
        <img onclick="SelectDate('date','yyyy-mm-dd')" src="../../images/form/calendar.gif" align="absmiddle" />
        <input name="time" readonly="readonly" />
        <img onclick="SelectDateTime('time')" src="../../images/form/clock.gif" align="absmiddle" />
        <input name="send_time" type="hidden" />    </td>
  </tr>
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>  
  <tr>
    <td align="right">手机短信提醒：</td>
    <td align="left"><input type="radio" name="isToMobile" value="true" id="isToMobile_0" checked="checked" />
        <label for="isToMobile_0">是</label>
        <input type="radio" name="isToMobile" value="false" id="isToMobile_1" />
        <label for="isToMobile_1">否</label>    </td>
  </tr>
  <tr>
    <td align="right">保存到发件箱：</td>
    <td align="left"><input type="radio" name="isToOutBox" value="true" id="isToOutBox_0" checked="checked" />
        <label for="isToOutBox_0">是</label>
        <input type="radio" name="isToOutBox" value="false" id="isToOutBox_1" />
        <label for="isToOutBox_1">否</label>    </td>
  </tr>
  <tr>
    <td align="right">是否需要回执：</td>
    <td align="left"><input type="radio" name="receipt_state" value="1" id="receipt_state_0" checked="checked" />
        <label for="receipt_state_0">是</label>
        <input type="radio" name="receipt_state" value="0" id="receipt_state_1" />
        <label for="receipt_state_1">否</label>    </td>
  </tr>
  <tr>
    <td align="right">消息等级：</td>
    <td align="left"><input type="radio" name="msg_level" value="0" id="msg_level_0" checked="checked" />
        <label for="msg_level_0">普通</label>
        <input type="radio" name="msg_level" value="1" id="msg_level_1" />
        <label for="msg_level_1">紧急</label>    </td>
  </tr>
  <tr>
    <td align="right">&nbsp;</td>
    <td align="left">&nbsp;</td>
  </tr>
<%}%>  
  
  
  <tr>
    <td height="26" colspan="2" align="center">
      <input type="submit" name="Submit" value="发送" class="btn" />
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="reset" name="Submit2" value="重写" class="btn" /></td>
  </tr>
</table>
<br />
</form>
</body>
</html>
