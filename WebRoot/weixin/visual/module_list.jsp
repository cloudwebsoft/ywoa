<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="com.redmoon.weixin.mgr.WXUserMgr"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="org.json.JSONObject"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="com.redmoon.oa.visual.*"%>
<%
String moduleCode = ParamUtil.get(request,"moduleCode");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(moduleCode);
if (msd==null) {
	out.print("模块不存在！");
	return;
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title><%=msd.getString("name")%></title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta name="viewport"
			content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<meta content="telephone=no" name="format-detection" />
		<link rel="stylesheet" href="../css/mui.css">
		<link rel="stylesheet" href="../css/my_dialog.css" />
		<link rel="stylesheet" href="../css/iconfont.css" />
		<link rel="stylesheet" type="text/css"
			href="../css/mui.picker.min.css" />
	</head>
	<style>
.mui-input-row .input-icon {
	width: 50%;
	float: left;
}

.mui-input-row a {
	margin-right: 10px;
	float: right;
	text-align: left;
	line-height: 1.5;
}

.search_div {
	height: 40px;
	border-bottom: 1px solid gainsboro;
	margin-left: 15px;
	vertical-align: middle;
}

.search_div select {
	color: #007aff;
	height: 37px;
}

.search_div label {
	color: #aaa;
}
</style>

	<body>
		<div class="mui-content">
			<div class="mui-content-padded">
			</div>
			<div id="pullrefresh" class="mui-scroll-wrapper"
				style="margin-top: 60px;">
				<div class="mui-scroll">
					<form class="search_form mui-input-group" id="search_content">

					</form>
					<!--数据列表-->
					<ul class="mui-table-view mui-table-view-chevron">

					</ul>
				</div>
			</div>
		</div>
		<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
		<script src="../js/macro/open_window_macro.js"></script>
		<script type="text/javascript" src="../js/mui.min.js"></script>
		<script src="../js/mui.picker.min.js"></script>
		<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
		<script type="text/javascript"
			src="../js/mui.pullToRefresh.material.js"></script>
		<script type="text/javascript" src="../js/config.js"></script>
		<script src="../js/visual/module_list.js"></script>
		<script src="../js/jq_mydialog.js"></script>
		<script src="../../js/jquery.raty.min.js"></script>
		<script>		
		<%
		Privilege pvg = new Privilege();
		pvg.auth(request);
		String skey = pvg.getSkey();
		%>
		var skey = '<%=skey%>';		
		var moduleCode = '<%=moduleCode%>';
		var options = {"ajax_params":{"skey":skey,"moduleCode":moduleCode},"url":"../../public/android/module/list"};
		var content = document.querySelector('.mui-content');
		var PullToRefrshListApi = new mui.PullToRefrshList(
				content,options);
		PullToRefrshListApi.loadListDate();
	</script>

		<jsp:include page="../inc/navbar.jsp">
			<jsp:param name="skey" value="<%=skey%>" />
			<jsp:param name="isBarBottomShow" value="false"/>
		</jsp:include>
	</body>
</html>
