<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="admin.workplan";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划类型管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
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
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("add")) {
	WorkPlanTypeMgr wptm = new WorkPlanTypeMgr();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = wptm.create(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "workplantype_list.jsp"));
	return;
}
else if (op.equals("del")) {
	WorkPlanTypeMgr wptm = new WorkPlanTypeMgr();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		re = wptm.del(request);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "workplantype_list.jsp"));
	return;
}
 %>
<%@ include file="workplan_inc_menu_top.jsp"%>
<script>
o("menu7").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" class="tabStyle_1_title">工作计划类型</td>
  </tr>
  <tr>
    <td colspan="2" align="center"><form id=form1 name="form1" action="?op=add" method=post>
        类型名称：
        <input name="name" maxlength="20">
		序号：
		<input name="orders" size="3" value="1" />
        &nbsp;
        <input class="btn" name="submit" type=submit value="添加">
        &nbsp;&nbsp;<a href="workplan_stat_year.jsp">统计全部</a>
    </form></td>
  </tr>
  <%
			  WorkPlanTypeDb wptd = new WorkPlanTypeDb();
			  String sql = "select id from work_plan_type where unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by orders";
			  Iterator ir = wptd.list(sql).iterator();
			  while (ir.hasNext()) {
			  	wptd = (WorkPlanTypeDb)ir.next();%>
  <tr>
    <td width="74%"><%=wptd.getName()%></td>
    <td width="26%" align="center"><a href="workplantype_edit.jsp?id=<%=wptd.getId()%>">编辑</a>&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='?op=del&id=<%=wptd.getId()%>'}})" style="cursor:pointer">删除</a>&nbsp;&nbsp;<a href="workplan_stat_year.jsp?typeId=<%=wptd.getId()%>">统计</a></td>
  </tr>
  <%}%>
</table>
</body>
</html>
