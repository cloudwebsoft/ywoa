<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
%>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.util.*" %>
<html>
<head>
<title>完成任务项</title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
<script language="javascript">
<!--
//-->
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="task" scope="page" class="com.redmoon.oa.task.TaskMgr"/>
<%
String taskid = request.getParameter("taskid");
if (taskid==null || !fchar.isNumeric(taskid))
{
	out.println(fchar.makeErrMsg("标识不正确！"));
	return;
}
try {
	if (task.changeFinish(request, Integer.parseInt(taskid))) {
		out.println(fchar.Alert_Back("操作成功！"));
	} else {
		out.println(fchar.Alert_Back("操作失败！"));
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.Alert_Back(e.getMessage()));
}
%>
</body>
</html>


