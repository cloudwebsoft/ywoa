<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%if (!privilege.isUserLogin(request)) {%>
<table width="320" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
  <tr> 
    <td><li>您的登录已过期，请重新登录，如果不是会员请先注册。</td>
  </tr>
</table>
<%
return;
}%>
<%
boolean isSuccess = false;
try {
	isSuccess = Msg.IKnow(request);
}
catch (ErrMsgException e) {
	out.println("操作失败："+e.getMessage());
	return;
}
if (isSuccess) {
	// out.println(StrUtil.Alert_Back("操作成功！"));
}
%>