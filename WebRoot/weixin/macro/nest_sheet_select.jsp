<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="org.json.JSONObject"%>
<%@page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<%@page import="nl.bitwalker.useragentutils.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>嵌套表格2</title>
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta name="viewport"
			content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
		<meta name="apple-mobile-web-app-capable" content="yes">
		<meta name="apple-mobile-web-app-status-bar-style" content="black">
		<meta content="telephone=no" name="format-detection" />
		<link rel="stylesheet" href="../css/mui.css">
		<link rel="stylesheet" href="../css/my_dialog.css" />
	</head>

	<body>
		<header class="mui-bar mui-bar-nav">
		<h1 class="mui-title">
			明细表
		</h1>

		</header>
		<div class="mui-content" id="op_con">
		</div>

		<div id="pullrefresh" class="mui-scroll-wrapper"
			style="margin-top: 90px;">
			<div class="mui-scroll">
				<!--数据列表-->
				<ul class="mui-table-view mui-table-view-chevron" id="ul_nest_sheet_select">

				</ul>
			</div>

		</div>

		<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
		<script src="../js/macro/open_window_macro.js"></script>
		<script type="text/javascript" src="../js/mui.min.js"></script>
		<script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
		<script type="text/javascript"
			src="../js/mui.pullToRefresh.material.js"></script>
		<script type="text/javascript" src="../js/config.js"></script>
		<script src="../js/mui.module_list_sel.js"></script>
		<script src="../js/jq_mydialog.js"></script>
		<script>
 	<%String skey = ParamUtil.get(request, "skey");
			String fieldCode = ParamUtil.get(request, "code");
			String flowId = ParamUtil.get(request, "flowId");
			String actionId = ParamUtil.get(request, "actionId");
			String dFormCode = ParamUtil.get(request, "dFormCode");
			String sFormCode = ParamUtil.get(request, "sFormCode");
					
			String parentFields = "";
			boolean isMobile = false;
			UserAgent ua = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
			OperatingSystem os = ua.getOperatingSystem();
			if (DeviceType.MOBILE.equals(os.getDeviceType())) {
				isMobile = true;
			}
			if (isMobile) {
				parentFields = StrUtil.getNullStr(request.getParameter("parentFields"));
				parentFields = new String(parentFields.getBytes("ISO-8859-1"), "UTF-8");
			}
			else {
				parentFields = ParamUtil.get(request, "parentFields");
			}
			// System.out.println(getClass() + " parentFields=" + parentFields);

			String parentCode = StrUtil.getNullStr(ParamUtil.get(request, "parentCode"));
			String pageType = StrUtil.getNullStr(ParamUtil.get(request, "pageType"));
			if (pageType.equals("")) {
				pageType = "flow";
			}
			int cwsId = ParamUtil.getInt(request, "cwsId", -1);
			String parentModuleCode = ParamUtil.get(request, "parentModuleCode");

			boolean isEditable = ParamUtil.getBoolean(request, "isEditable", false);
			int isWx = ParamUtil.getInt(request, "isWx", 0);
			if (parentCode.equals("") && !flowId.equals("0")
					&& !flowId.equals("-1")) {
				WorkflowDb wf = new WorkflowDb();
				int fId = StrUtil.toInt(flowId);
				wf = wf.getWorkflowDb(fId);
				if (wf != null) {
					Leaf lf = new Leaf();
					lf = lf.getLeaf(wf.getTypeCode());
					if (lf != null) {
						parentCode = lf.getFormCode();
						
						com.redmoon.oa.flow.FormDb fd = new com.redmoon.oa.flow.FormDb();
						fd = fd.getFormDb(lf.getFormCode());
						com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
						fdao = fdao.getFormDAO(fId, fd);
						cwsId = (int)fdao.getId();
					}
				}
			}
			String urlParams = "?pageType=" + pageType + "&parentModuleCode="
					+ parentModuleCode + "&cwsId=" + cwsId + "&skey=" + skey
					+ "&code=" + fieldCode + "&flowId=" + flowId + "&actionId="
					+ actionId + "&dFormCode=" + dFormCode + "&sFormCode="
					+ sFormCode + "&parentCode=" + parentCode + "&isEditable="
					+ isEditable + "&parentFields="
					+ StrUtil.UrlEncode(parentFields)
					+ "&parentId=" + cwsId
					+ "&cwsId=" + cwsId;
			// System.out.println(getClass() + " parentFields=" + parentFields);
			// System.out.println(getClass() + " parentFields=" + StrUtil.UrlEncode(parentFields));%>
var skey = '<%=skey%>';
var params = {
				"formCode":"<%=dFormCode%>",
				"flowId":"<%=flowId%>",
				"nestFieldName":"<%=fieldCode%>",
				"actionId":"<%=actionId%>",
				"parentCode":"<%=parentCode%>",
				"skey":"<%=skey%>",
				"pageType":"<%=pageType%>",
				"isEditable":<%=isEditable%>,
				"parentModuleCode":"<%=parentModuleCode%>",
				"cwsId":<%=cwsId%>,
				"pageType":"<%=pageType%>"
							
			 };
// console.log("urlParams=<%=urlParams%>");
var options = {"isWx":<%=isWx%>,"ulContainer":"#ul_nest_sheet_select","ajax_params":params,"url":"../../public/android/nest_sheet_view.jsp","ajaxDatasType":LIST_TYPE.NEST_SHEET_SELECT,"urlParams":'<%=urlParams%>'};
var content = document.querySelector('.mui-content');
var PullToRefrshListApi = new mui.PullToRefrshList(content,options);
PullToRefrshListApi.loadListDate();
	</script>
	</body>
</html>
