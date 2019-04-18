<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%
com.redmoon.forum.util.SeoConfig scfg = new com.redmoon.forum.util.SeoConfig();
String seoTitle = scfg.getProperty("seotitle");
String seoKeywords = scfg.getProperty("seokeywords");
String seoHead = scfg.getProperty("seohead");

String strIsFrame = ParamUtil.get(request, "isFrame");
if (!strIsFrame.equals("")) {
	int maxAge = 60 * 60 * 24 * 30; // 保存一月
	if (strIsFrame.equals("y")) {
		CookieBean.addCookie(response, com.redmoon.forum.ui.ForumPage.COOKIE_IS_FRAME, "y", "/", maxAge);
	}
	else {
		CookieBean.addCookie(response, com.redmoon.forum.ui.ForumPage.COOKIE_IS_FRAME, "n", "/", maxAge);
		String url = ParamUtil.get(request, "url");
		// 判断url是否为本站
		if (!com.cloudwebsoft.framework.security.SecurityUtil.isUrlValid(request, url)) {
			com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();	
			com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "Phishing frame.jsp url=" + url);
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "op_invalid")));
			return;
		}
		
		response.sendRedirect(url);
		return;
	}
}
String skinPath = SkinMgr.getSkinPath(request);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE><%=Global.AppName%> <%=seoTitle%></TITLE>
<%=seoHead%>
<META http-equiv=Content-Type content=text/html; charset=utf-8>
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<META name="keywords" content="<%=seoKeywords%>">
<META name="description" content="">
<style type="text/css">
body {
margin: 0px;
}
</style>
<script type="text/javascript">
function frameBtn_onClick(){
	var obj = document.getElementById('boardMenuTd');
	var frameBtn = document.getElementById('frameBtn');
	var frameBtnBar = document.getElementById('frameBtnBar');
	if(obj.style.display == 'none'){
		obj.style.display = '';
		frameBtnBar.style.left = '177px';
		frameBtn.style.backgroundPosition = '0';
	}else{
		obj.style.display = 'none';
		frameBtnBar.style.left = '0px';
		frameBtn.style.backgroundPosition = '-11';
	}
}

if(top != self) {
	top.location = self.location;
}
</script>
</head>
<body scroll="no">
<%
String mainUrl = ParamUtil.get(request, "mainUrl");
try {
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();		
	// 验证url是否为本站
	com.redmoon.oa.security.SecurityUtil.validateUrl(request, pvg, "mainUrl", mainUrl, getClass().getName());
	// 防XSS
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "mainUrl", mainUrl, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (mainUrl.equals("")) {
	mainUrl = "index.jsp";
}
%>
<table border="0" cellPadding="0" cellSpacing="0" height="100%" width="100%">
<tr>
<td align="middle" id="boardMenuTd" valign="center" width="180">
<iframe name="frame_left" frameborder="0" src="frame_left.jsp" scrolling="auto" style="height: 100%; visibility: inherit; width: 180px; z-index: 1"></iframe>
<td style="width: 100%">
<table id="frameBtnBar" border="0" cellPadding="0" cellSpacing="0" width="11" height="100%" style="position: absolute; left: 177px; background-repeat: repeat-y; background-position: -177px">
<tr><td onClick="frameBtn_onClick()" width="11" height="49"><img id="frameBtn" src="images/board_tree/none.gif" alt="" border="0" width="11" height="49" /></td></tr>
</table>
<iframe frameborder="0" scrolling="yes" name="frame_main" src="<%=mainUrl%>" style="height: 100%; visibility: inherit; width: 100%; z-index: 1;overflow: auto;"></iframe>
</td>
</tr>
</table>
</body>
</html>