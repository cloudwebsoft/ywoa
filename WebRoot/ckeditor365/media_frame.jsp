<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.module.pvg.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.Config"%>
<%@ page import="cn.js.fan.security.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
	<head>
		<title>Placeholder Properties</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta content="noindex, nofollow" name="robots">
		<script>
		function Add(visualPath) {
			if (window.opener)
				window.opener.SetUrl(visualPath);
			else
				window.dialogArguments.SetUrl(visualPath);
			window.close();
		}
		</script>
</head>
<jsp:useBean id="privilege" scope="page" class="cn.js.fan.module.pvg.Privilege"/>
<%
String action = cn.js.fan.util.ParamUtil.get(request, "action");

String p = ParamUtil.get(request, "p");
Config cfg = Config.getInstance();

String userName = ParamUtil.get(request, "userName");
User user = new User();
user = user.getUser(userName);
if (user.isLoaded()) {
	String pwdMD5 = ThreeDesUtil.decrypthexstr(cfg.getKey(), p);
	if (user.getPwdMD5().equals(pwdMD5)) {
		privilege.doLogin(request, userName, pwdMD5);
	}
}

if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<frameset rows="*" cols="180,*" framespacing="0" border="0">
  <frame src="../../../../forum/admin/media_left.jsp?action=<%=action%>" name="leftFileFrame" >
  <frame src="../../../../forum/admin/media_list.jsp?action=<%=action%>" name="mainFileFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
