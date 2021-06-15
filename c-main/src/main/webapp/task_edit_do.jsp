<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.*"
%>
<html>
<head>
<title>编辑任务处理</title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
<script language="javascript">
<!--
//-->
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="task" scope="page" class="com.redmoon.oa.task.TaskMgr" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<br>
<%
boolean isSuccess = false;
String privurl = "";
try {
	isSuccess = task.edit(application, request);
	privurl = task.getprivurl();
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back("修改失败："+e.getMessage()));
	return;
}
if (isSuccess)
{
%>
<ol>修改成功!</ol>
<%
out.println(fchar.waitJump("<a href='"+privurl+"'>回到前页！</a>",3,privurl));
}
%>
</body>
</html>


