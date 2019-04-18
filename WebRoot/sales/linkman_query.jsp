<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>高级查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
</head>
<body>
<%@ include file="linkman_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<div class="spacerH"></div>
<%
RequestAttributeElement rae = new RequestAttributeElement();
RequestAttributeMgr ram = new RequestAttributeMgr();
String userName = ParamUtil.get(request, "userName");
try {	
	com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

ram.addAttribute(request, rae.createHidden("userName", userName));
%>
<%@ include file="../visual_query.jsp"%>
</body>
</html>
