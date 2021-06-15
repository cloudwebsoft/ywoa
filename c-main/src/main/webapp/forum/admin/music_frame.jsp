<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择文件</title>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String action = cn.js.fan.util.ParamUtil.get(request, "action");
%>
<frameset rows="*" cols="180,*" framespacing="0" border="0">
  <frame src="music_left.jsp?action=<%=action%>" name="leftFileFrame" >
  <frame src="music_list.jsp?action=<%=action%>" name="mainFileFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
