<%@ page contentType="text/html;charset=utf-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>部门工作</title>
<script>
function setCols(cols) {
	frm.cols = cols;
}
function getCols() {
	return frm.cols;
}
</script>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<frameset id="frm" cols="200,*" frameborder="yes" border="1" framespacing="1">
  <frame src="flow_performance_dept_tree.jsp" name="leftFrame" >
  <frame src="flow_performance_user_list.jsp" name="midFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
