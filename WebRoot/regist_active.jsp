<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.*"
%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.regist" key="regist"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/skin.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<br>
<br>
<%
	String activeStr = ParamUtil.get(request, "activeStr");
    activeStr = cn.js.fan.security.ThreeDesUtil.decrypthexstr(com.redmoon.forum.Config.getInstance().getKey(), activeStr);
	UserDb ud = new UserDb();
	ud = ud.getUserDbByNick(activeStr);
	if (ud!=null && ud.isLoaded()) {
		ud.setCheckStatus(UserDb.CHECK_STATUS_PASS);
		boolean re = ud.save();
		Privilege privilege = new Privilege();
		privilege.doLogin(request, response, ud);
		String info = SkinUtil.LoadString(request, "res.label.regist", "regist_active_success");
		out.println(StrUtil.waitJump(info, 3, "forum/index.jsp"));
	}
	else {
		response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.regist", "regist_active_fail")));
		return;
	}
%><br>
<br>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
</html>


