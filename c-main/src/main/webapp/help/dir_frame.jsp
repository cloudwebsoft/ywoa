<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>管理目录</title>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//if (!privilege.isUserPrivValid(request, com.redmoon.oa.pvg.PrivDb.PRIV_ADMIN)) {
//  out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
//	return;
//}

String root_code = ParamUtil.get(request, "root_code");
try {
	// 防XSS
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
%>
<frameset rows="*,150" cols="*">
  <frame src="dir_top_ajax.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>" name="dirmainFrame">
  <frame src="dir_bottom.jsp" name="dirbottomFrame">
</frameset>
<noframes><body>
</body></noframes>
</html>
