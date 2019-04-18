<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<html>
<head>
<title>编辑日程</title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
</head>
<body>
<%@ include file="inc/inc.jsp"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="plan" scope="page" class="com.redmoon.oa.person.PlanMgr"/>
<%
boolean re = false;
int id = ParamUtil.getInt(request, "id");
try {
	re = plan.modify(request);
}
catch (ErrMsgException e) {
	out.println(fchar.Alert_Back(e.getMessage()));
	return;
}
if (re)
	out.println(fchar.Alert_Redirect("修改日程成功！", "plan_edit.jsp?id=" + id));
%>
</body>
</html>