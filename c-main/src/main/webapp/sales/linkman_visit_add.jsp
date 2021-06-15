<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
</head> 
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
request.setAttribute("isShowVisitTag", "true");
String op = ParamUtil.get(request, "op");
%>

<%if (op.equals("listOfCustomer")) {%>
<%@ include file="customer_inc_nav.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<%}else{%>
<%@ include file="linkman_inc_menu_top.jsp"%>
<script>
o("menu4").className="current"; 
</script>
<%}%>
<div class="spacerH"></div>
<%
String priv="sales.user";
if (!privilege.isUserPrivValid(request, priv)) {
	// return;
}

long linkmanId = ParamUtil.getLong(request, "linkmanId", -1);
String action = ParamUtil.get(request, "action");
FormDb fd = new FormDb();
fd = fd.getFormDb("sales_linkman");
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
fdao = fdao.getFormDAO(linkmanId, fd);

// System.out.println(getClass() + " " + fdao.getFieldValue("customer"));

long customerId = StrUtil.toLong(fdao.getFieldValue("customer"), -1);
if (customerId!=-1 && !SalePrivilege.canUserSeeCustomer(request, customerId)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}

RequestAttributeElement rae = new RequestAttributeElement();
RequestAttributeMgr ram = new RequestAttributeMgr();
ram.addAttribute(request, rae.createHidden("linkmanId", "" + linkmanId));
ram.addAttribute(request, rae.createHidden("action", action));
if (op.equals("listOfCustomer")) {
	ram.addAttribute(request, rae.createHidden("op", op));
	ram.addAttribute(request, rae.createHidden("customerId", "" + ParamUtil.getLong(request, "customerId")));
	ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/customer_visit_list.jsp?customerId=" + ParamUtil.getLong(request, "customerId") + "&action=" + action));
}
else if (linkmanId!=-1) {
	ram.addAttribute(request, rae.createHidden("linkmanId", "" + linkmanId));
	ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/linkman_visit_list.jsp?linkmanId=" + linkmanId + "&action=" + action));
}
else {
	ram.addAttribute(request, rae.createHidden(RequestAttributeElement.NAME_FORWARD, "sales/sales_user_action_list.jsp?action=" + action));
}
%>
<%@ include file="../visual_add.jsp"%>
</body>
</html>