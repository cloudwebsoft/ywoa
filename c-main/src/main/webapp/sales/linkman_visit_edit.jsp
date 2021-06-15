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
<meta name="renderer" content="ie-stand">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>编辑行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>

</head> 
<body>
<%
request.setAttribute("isShowVisitTag", "true");
/*
%>
<%@ include file="linkman_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<%
String priv="sales.user";
if (!privilege.isUserPrivValid(request, priv)) {
	// return;
}
*/

long visitId = ParamUtil.getLong(request, "id");
FormDb vfd = new FormDb();
vfd = vfd.getFormDb("day_lxr");
com.redmoon.oa.visual.FormDAO vfdao = new com.redmoon.oa.visual.FormDAO();
vfdao = vfdao.getFormDAO(visitId, vfd);

long linkmanId = StrUtil.toLong(vfdao.getFieldValue("lxr"));
FormDb fd = new FormDb();
fd = fd.getFormDb("sales_linkman");

com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
fdao = fdao.getFormDAO(linkmanId, fd);

long customerId = StrUtil.toLong(fdao.getFieldValue("customer"));
if (!SalePrivilege.canUserSeeCustomer(request, customerId)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="../visual_edit.jsp"%>
</body>
</html>
