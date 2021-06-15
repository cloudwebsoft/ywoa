<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>删除任务项</title>
<%@ include file="inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="task" scope="page" class="com.redmoon.oa.task.TaskMgr"/>
<br>
<%
int delid = ParamUtil.getInt(request, "delid");
int rootid = ParamUtil.getInt(request, "rootid");
String privurl = "task_show.jsp?rootid=" + rootid + "&showid=" + rootid;
try {
	TaskDb td = task.getTaskDb(delid);
	String url = "task.jsp";
	if (td.getProjectId()!=-1) {
		url = "project_task_list.jsp?parentId=" + td.getProjectId() + "&formCode=project&projectId=" + td.getProjectId();
	}
	if (task.del(request,delid)) {
	%>
		<BR><ol>删除成功！</ol>
	<%
		out.println(fchar.waitJump("<a href='" + url + "'>返回！</a>",3, privurl));
	} else {%>
		<p align=center>删除失败！</p>
	<%}
}
catch (ErrMsgException e) {
	out.print(fchar.makeErrMsg(e.getMessage()));
}
%>
</body>
</html>


