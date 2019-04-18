<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.flow.macroctl.*"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="org.json.JSONObject"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>表单映射域</title>
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

.search_div{
	height: 40px;
	border-bottom: 1px solid gainsboro;
	margin-left: 15px;
	vertical-align: middle;
				
				
			}
.search_div select{
	
	color: #007aff;
	height: 37px;
}

	.search_div label{
				
				color: #aaa;
			}
</style>

	<body>
		<header class="mui-bar mui-bar-nav">
			
			<h1 class="mui-title">请选择</h1>
			<a id='done' class="mui-btn mui-btn-link mui-pull-right mui-btn-blue ">完成</a>
		
		</header>
		
		
		
		<div id="pullrefresh"   class="mui-scroll-wrapper" style="margin-top: 90px;">
			  <div class="mui-scroll">
			  		<form class="search_form mui-input-group" id="search_content">
						
					</form>
			    <!--数据列表-->
			    <ul class="mui-table-view mui-table-view-chevron" id="form_field_ul">
			   		
			    </ul>
			  </div>
			  
		  </div>
			
	 <script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/macro/open_window_macro.js"></script>
  	 <script type="text/javascript" src="../js/mui.min.js"></script>
  	 <script src="../js/mui.picker.min.js"></script>
 	 <script type="text/javascript" src="../js/mui.pullToRefresh.js"></script>
 	 <script type="text/javascript" src="../js/mui.pullToRefresh.material.js"></script>
 	 <script type="text/javascript" src="../js/config.js"></script>
 	 <script src = "../js/mui.module_list_sel.js"></script>
 	 <script src="../js/jq_mydialog.js"></script>
	 <script src="../../js/jquery.raty.min.js"></script>
 	 <script>
	<%
			String skey = ParamUtil.get(request,"skey");
			String desc = ParamUtil.get(request,"desc");
			
			desc = ModuleFieldSelectCtl.formatJSONStr(desc);
			// System.out.println(getClass() + " desc=" + desc);
			
			JSONObject obj = new JSONObject(desc);
			String formCode = obj.getString("sourceFormCode");
			String idField = obj.getString("idField");
			String showField = obj.getString("showField");
			String openerFormCode = obj.getString("formCode");
			String openerFieldName = ParamUtil.get(request,"openerFieldName");
			String parentFields = ParamUtil.get(request,"parentFields");
			int isWx = ParamUtil.getInt(request,"isWx",0);
		%>
		var skey = '<%=skey%>';
		var params = {
						"skey": "<%=skey%>",
						// "op":"search",
						"action":"",
						"formCode":"<%=formCode%>",
						"orderBy":"<%=idField%>",
						"sort":"desc",
						"filter":"",
						"byFieldName":"<%=idField%>",
						"showFieldName":"<%=showField%>",
						"openerFormCode":"<%=openerFormCode%>",
						"openerFieldName":"<%=openerFieldName%>",
						"action":"afterGetClientValue"
						
					 };
		var parentFields = '<%=parentFields%>';
		if(parentFields != ""){
			var par = JSON.parse(parentFields);
			for(var o in par) {
		        params[o]= par[o];  
		    }  
		}
		var options = {"isWx":<%=isWx%>,"ulContainer":"#form_field_ul","ajax_params":params,"url":"../../public/android/module_list_sel.jsp","ajaxDatasType":LIST_TYPE.FORM_FIELD};
		// console.log(params);
		var content = document.querySelector('.mui-content');
		var PullToRefrshListApi = new mui.PullToRefrshList(
				content,options);
		PullToRefrshListApi.loadListDate();
	</script>
	</body>
</html>
