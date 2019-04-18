<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@page import="com.redmoon.oa.ui.LocalUtil"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>删除流程</title>
<link href="common.css" rel="stylesheet" type="text/css">
<%@ include file="inc/nocache.jsp"%>
<style type="text/css">
<!--
.style2 {font-size: 14px}

-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script type="text/javascript" src="js/jquery1.7.2.min.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

try {
	int flow_id = ParamUtil.getInt(request, "flow_id");
	WorkflowMgr wm = new WorkflowMgr();
	WorkflowDb wf = wm.getWorkflowDb(flow_id);
	// 20170508 fgf 逻辑删除
	wf.setStatus(WorkflowDb.STATUS_DELETED);
	wf.save();
	// wf.del();
	String str = LocalUtil.LoadString(request,"res.flow.Flow","deleteCompleted");
	%>
	<script>
		window.location.href="flow/flow_list.jsp?displayMode=1&toa=ok&msg=<%=str%>";
	</script>
	<%
}
catch (ErrMsgException e) {
	out.print(fchar.Alert_Back(e.getMessage()));
}
%>
</body>
</html>
