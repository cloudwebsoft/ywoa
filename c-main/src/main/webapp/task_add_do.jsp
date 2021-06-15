<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.web.*"
import = "cn.js.fan.util.*"
%>
<%@ page import="java.util.Calendar" %>
<html>
<head>
<title>创建任务</title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="../common.css" type="text/css">
<script language="javascript">
<!--
//-->
</script>
<link href="common.css" rel="stylesheet" type="text/css">
</head>
<body>
<br>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="task" scope="page" class="com.redmoon.oa.task.TaskMgr" />
<%
boolean isSuccess = false;
try {
	isSuccess = task.Add(application, request);
}
catch (ErrMsgException e) {
	out.println(fchar.Alert_Back("操作失败："+e.getMessage()));
	return;
}
String privurl = task.getprivurl();
//if (privurl==null || privurl.equals("")) {
	privurl = "task_show.jsp?rootid=" + task.getRootid() + "&showid=" + task.getId() + "&projectId=" + task.getProjectId();
//}
String op = ParamUtil.get(request, "op");
// System.out.println("privurl=" + privurl);
if (isSuccess) {
	if (op.equals("new")) {
		privurl = "task_add.jsp?op=newsubtask&parentid=" + task.getId() + "&projectId=" + task.getProjectId();
	%>
		<ol><strong>撰写任务完毕！请点击下面的链接分配任务，或者等待页面自动跳转</strong></ol>
	<%		
		out.print(StrUtil.waitJump("<a href='"+privurl+"'>分配任务！</a>", 3, privurl));
		return;
	}
	else {
%>
	<ol>
	  操作成功!
	</ol>
<%	out.println(fchar.waitJump("<a href='"+privurl+"'>回到前页！</a>",3,privurl));
	}
}
%>
</body>
</html>


