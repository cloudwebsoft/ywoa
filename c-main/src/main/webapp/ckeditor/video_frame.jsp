<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.module.pvg.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.Config"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int fn = ParamUtil.getInt(request, "fn", 1);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
	<head>
		<title>选择视频</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta content="noindex, nofollow" name="robots">
		<script>
		function Add(visualPath) {
			/*
			if (window.opener)
				window.opener.SetUrl(visualPath);
			else
				window.dialogArguments.SetUrl(visualPath);
			*/
			if (window.opener) {
				window.opener.CKEDITOR.tools.callFunction(<%=fn%>, visualPath);
				// alert(window.opener.CKEDITOR.dialog.get('flvPlayer').getContentElement('info','src'));
				//alert(window.opener.CKEDITOR.dialog('flvPlayer'));
			}
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
if (!userName.equals("")) {
	User user = new User();
	user = user.getUser(userName);
	if (user.isLoaded()) {
		String pwdMD5 = ThreeDesUtil.decrypthexstr(cfg.getKey(), p);
		if (user.getPwdMD5().equals(pwdMD5)) {
			privilege.doLogin(request, userName, pwdMD5);
		}
	}
}

if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String dir = ParamUtil.get(request, "dir");
%>
<frameset rows="*" cols="180,*" framespacing="0" border="0">
  <frame src="../cms/video_left.jsp?action=<%=action%>&dir=<%=StrUtil.UrlEncode(dir)%>" name="leftFileFrame" >
  <frame src="../cms/video_list.jsp?action=<%=action%>&dir=<%=StrUtil.UrlEncode(dir)%>" name="mainFileFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
