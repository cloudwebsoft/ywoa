<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.robot.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html>
<html>
<head>
<title>Robot List</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="56%" class="tdStyle_1">采集机器人</td>
      <td width="44%" class="tdStyle_1"><TABLE width="312" border=0 align=right cellPadding=0 cellSpacing=0 summary="">
        <TBODY>
          <TR>
            <TD><A class=view 
            href="robot_list.jsp">浏览机器人</A></TD>
            <TD><A class=add 
            href="robot_add.jsp">添加新机器人</A></TD>
            <TD><A class=other 
            href="robot_import.jsp">导入机器人</A></TD>
          </TR>
        </TBODY>
      </TABLE></td>
    </tr>
  </tbody>
</table>
<%
RobotDb rd = new RobotDb();

String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	int id = ParamUtil.getInt(request, "id");
	rd = (RobotDb)rd.getQObjectDb(new Integer(id));
	boolean re = rd.del();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "robot_list.jsp"));
	else
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));	
}

if (op.equals("copy")) {
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	int id = ParamUtil.getInt(request, "id");
	rd = (RobotDb)rd.getQObjectDb(new Integer(id));
	boolean re = rd.copy();
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"提示", "robot_list.jsp"));
	else
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));	
}

String sql = rd.getTable().getQueryList();

int total = (int)rd.getQObjectCount(sql);

int pagesize = total; 	// 20;

int curpage,totalpages;
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
totalpages = paginator.getTotalPages();
curpage	= paginator.getCurrentPage();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}	

QObjectBlockIterator oir = rd.getQObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
%>
<br>
<table class="tabStyle_1 percent98" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="44%">名称</td>
      <td class="tabStyle_1_title" noWrap width="16%">采集个数</td>
      <td class="tabStyle_1_title" noWrap width="17%">页面编码</td>
      <td width="23%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
while (oir.hasNext()) {
 	rd = (RobotDb)oir.next();
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td><%=rd.getString("name")%></td>
      <td><%=rd.getInt("gather_count")%></td>
      <td><%=rd.getString("charset")%> </td>
      <td align="center">
	  [<a href="robot_edit.jsp?robotId=<%=rd.getInt("id")%>">修改</a>]&nbsp;[<a href="#" onClick="jConfirm('您确定要删除吗？','提示',function(r){if(!r){return;}else{window.location.href='robot_list.jsp?op=del&id=<%=rd.getInt("id")%>'}}) ">删除</a>]&nbsp;[<a href="robot_do.jsp?op=gather&robotId=<%=rd.getInt("id")%>">采集</a>]&nbsp;[<a href="robot_list.jsp?op=copy&id=<%=rd.getInt("id")%>">复制</a>]&nbsp;[<a href="robot_export.jsp?op=export&id=<%=rd.getInt("id")%>">导出</a>]</td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
</html>