<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.*"%>
<%
String userName = ParamUtil.get(request, "userName");
%>
<html>
<head>
<title>ճ</title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="common.css" type="text/css">
<script language="javascript">
<!--
//-->
</script>
</head>
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
<body bgcolor="#FFFFFF" text="#000000">
<%@ include file="../inc/inc.jsp"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="ppm" scope="page" class="com.redmoon.oa.person.PlanPeriodicityMgr"/>
<%	
	String op = ParamUtil.get(request, "op");
	if (op.equals("del")) {
		//PlanPeriodicityMgr ppm = new PlanPeriodicityMgr();
		boolean re = false;
		try {%>
			<script>
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			</script>
			<%
			re = ppm.del(request);
		}catch (ErrMsgException e) {
			out.print(fchar.jAlert_Back(e.getMessage(),"提示"));
		}
		if (re) {%>
				<script>
					$(".loading").css({"display":"none"});
					$(".treeBackground").css({"display":"none"});
					$(".treeBackground").removeClass("SD_overlayBG2");
				</script>
				<%
			out.print(fchar.jAlert_Redirect("周期性任务删除成功！","提示", "plan_periodicity.jsp?userName=" + StrUtil.UrlEncode(userName)));
		}
		return;
	}else{
		boolean re = false;
		try {%>
			<script>
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			</script>
			<%
			re = ppm.create(request);
		}
		catch (ErrMsgException e) {
			out.println(fchar.jAlert_Back(e.getMessage(),"提示"));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
			return;
		}
		if (re){
			%>
			<script>
				$(".loading").css({"display":"none"});
				$(".treeBackground").css({"display":"none"});
				$(".treeBackground").removeClass("SD_overlayBG2");
			</script>
			<%
			out.println(fchar.jAlert_Redirect("添加周期性任务成功","提示", "plan_periodicity.jsp?userName=" + StrUtil.UrlEncode(userName)));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		}
	}
	%>
</body>
</html>