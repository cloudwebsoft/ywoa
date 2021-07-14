<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>转发消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr> 
    <td class="tdStyle_1" height="23">
        转发消息    </td>
  </tr>
  <tr> 
    <td bgcolor="#FFFFFF" height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0"></a></div>            </td>
            <td width="75"> 
              <div align="center"><a href="listdraft.jsp"><img src="../images/m_draftbox.gif" width="60" height="60" border="0"></a></div>            </td>
    <td width="75"><div align="center"><a href="listoutbox.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0"></a></div></td>
            <td width="75"> 
              <div align="center"><a href="send.jsp"><img src="../images/newpm.gif" width="60" height="60" border="0"></a></div>            </td>
            <td width="75"> 
              <div align="center"> <img src="../images/m_delete.gif" width="60" height="60"></div>            </td>
          </tr>
        </table>    </td>
  </tr>
  <tr> 
      <td bgcolor="#FFFFFF" valign="top">
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
          <tr> 
            <td></td>
          </tr>
        </table>
        <table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr> 
          <td height="35" align="center">
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
String op = ParamUtil.get(request, "op");
boolean isSuccess = false;
try {
	if (op.equals("")) {
		isSuccess = Msg.TransmitMsg(application, request);
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert_Back("消息转发失败：" + com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage())));	
}
%>
<%if (isSuccess) { %>
    消息转发成功！
<%} else {%>
	消息转发失败！
<%}%>
		    </td>
          </tr>
          <tr> 
            <td height="35">&nbsp;</td>
          </tr>
    </table>    </td>
  </tr>
</table>
</body>
</html>
