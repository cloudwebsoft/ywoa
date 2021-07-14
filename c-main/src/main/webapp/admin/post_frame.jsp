<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>部门人员</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<frameset cols="200,*" frameborder="NO" border="0" framespacing="0">
  <frame src="post_dept.jsp" name="leftFrame" >
  <frame src="dept_user.jsp" name="midFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
