<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>撰写消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">撰写消息</td>
    </tr>
  </tbody>
</table>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tr>
    <td align="center"><jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
      <br />
      <br />
      <%
String op = ParamUtil.get(request, "op");
boolean isSuccess = false;
try {
	if (op.equals("")) {
		isSuccess = Msg.AddMsg(application, request);
		
	}
	else if (op.equals("addDraft")) {
		isSuccess = Msg.AddDraftMsg(application, request);
	}
	/*
	else if (op.equals("sendDraft")) {
		isSuccess = Msg.sendDraft(request);
	}
	*/
}
catch (ErrMsgException e) {
	out.print(StrUtil.jAlert("消息发送失败：" + com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage()),"提示"));
	//e.printStackTrace();
}
%>
      <%if (isSuccess) {
      out.print(StrUtil.Alert_Redirect("操作成功！", "message.jsp"));
      }else{
      out.print(StrUtil.jAlert("操作失败！","提示"));
      }%>
    </td>
  </tr>
</table>
</body>
</html>
