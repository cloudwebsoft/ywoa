<%@ page contentType="text/html; charset=utf-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
	<head>
		<title>Placeholder Properties</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta content="noindex, nofollow" name="robots">

       <script language="javascript">
	var oEditor;
	var FCKLang;
	var FCKMedias;
	try {
	oEditor = window.parent.InnerDialogLoaded() ;
	FCKLang = oEditor.FCKLang ;
	FCKMedias = oEditor.FCKMedias ;
	}
	catch (e) {}

window.onload = function ()
{
	try {
    oEditor.FCKLanguageManager.TranslatePage( document ) ;
    window.parent.SetOkButton( true ) ;
	}
	catch (e) {}
}

function Add(visualPath) {
	FCKMedias.Add(visualPath);
	window.parent.close();
}

function Ok() {
    return true ;
}
		</script>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
/*
if (!privilege.isMasterLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

String action = cn.js.fan.util.ParamUtil.get(request, "action");
%>
<frameset rows="*" cols="180,*" framespacing="0" border="0">
  <frame src="../../../../forum/admin/media_left.jsp?action=<%=action%>" name="leftFileFrame" >
  <frame src="../../../../forum/admin/media_list.jsp?action=<%=action%>" name="mainFileFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
