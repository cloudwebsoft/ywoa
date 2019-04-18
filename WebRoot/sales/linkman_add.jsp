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
<meta name="renderer" content="ie-stand">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加联系人</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>

</head> 
<body>
<%@ include file="linkman_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="sales";
if (!privilege.isUserPrivValid(request, priv)) {
	// out.println(fchar.makeErrMsg("对不起，您不具有发布工作计划的权限！"));
	// return;
}

RequestAttributeElement rae = new RequestAttributeElement();
RequestAttributeMgr ram = new RequestAttributeMgr();
String op = ParamUtil.get(request, "op");
if (op.equals("listOfCustomer")) {
	ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/linkman_list.jsp?op=listOfCustomer&customerId=" + ParamUtil.getLong(request, "customerId")));
}
else {
	String userName = ParamUtil.get(request, "userName");
	ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/linkman_list.jsp?userName=" + StrUtil.UrlEncode(userName)));
}
%>
<%@ include file="../visual_add.jsp"%>
</body>
</html>
