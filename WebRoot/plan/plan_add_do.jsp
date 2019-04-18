<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "cn.js.fan.util.*"%>
<html>
<head>
<title>增加日程</title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script language="javascript">
<!--
//-->
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%@ include file="../inc/inc.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="plan" scope="page" class="com.redmoon.oa.person.PlanMgr"/>
<%
String userName = ParamUtil.get(request, "userName");
boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
String op = ParamUtil.get(request, "op");
// out.print(request.getParameter("id"));
boolean re = false;
if(op.equals("add")){	
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = plan.create(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}catch (ErrMsgException e) {
		out.println(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		return;
	}
	if (re){
		out.println(StrUtil.jAlert_Redirect("制定日程成功！","提示", "plan.jsp?isShared=" + isShared + "&userName=" + StrUtil.UrlEncode(userName)));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
else if(op.equals("modify")){
	int id = ParamUtil.getInt(request, "id");
	
	try{%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = plan.modify(request);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}catch(ErrMsgException e){
		out.println(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		return;
	}
	if(re){
		out.println(StrUtil.jAlert_Redirect("编辑日程成功！","提示","plan_edit.jsp?isShared=" + isShared + "&id=" + id));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
}
%>
</body>
</html>