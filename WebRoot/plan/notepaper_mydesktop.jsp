<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.oacalendar.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("close")) {
	int id = ParamUtil.getInt(request, "id");
	PlanDb pd = new PlanDb();
	pd = pd.getPlanDb(id);
	pd.setClosed(true);
	pd.save();
	out.print("true");
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>便笺</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../desktop/lib/js/jquery/jquery.nicescroll.min.js"></script>
<style type="text/css">
<!--
body, td, th {
	font-size: 14px;
}
* {
	margin:0;
	padding:0;
}

.memo_area {
    background: url("../desktop/theme/default/images/memo_background.png") no-repeat scroll left 0 transparent;
    bottom: 0;
    left: 0;
    padding: 10px 10px 10px;
    position: absolute;
    right: 0;
    top: 0;
    z-index: 0;
}
html.ie6_0 .memo_area {
    background: none repeat scroll 0 center transparent;
}
.memo_area textarea, input {
    outline: 0 none;
    resize: none;
}
.memo_area_content {
    background: none repeat scroll 0 0 transparent;
    border: medium none;
    cursor: pointer;
    font: 12px/1.5 tahoma,helvetica,clean,sans-serif;
    height: 132px;
    overflow: hidden;
    padding: 0;
    width: 141px;
    word-wrap: break-word;
}
.memo_area_content_plus {
    cursor: text;
    margin: 0;
    overflow-x: inherit;
    overflow-y: scroll;
    padding: 0;
    width: 160px;
}
-->
</style>
</head>
<body>


<div class="memo_area" id="memo_area">
  <div class="memo_area_content" id="memo_area_content" >
    <div style="margin:0px; padding:0px;">
    <span style="float:right; cursor:pointer; height:25px; font-size:14px; color:#666" title="日程" onclick="addTab('日程', '<%=request.getContextPath()%>/plan/plan_list.jsp')">≡&nbsp;</span>
    <span style="float:right; cursor:pointer; height:25px; font-size:14px; color:#666" title="添加便笺" onclick="addTab('添加便笺', '<%=request.getContextPath()%>/plan/plan_add.jsp?action=addNotepaper')">+&nbsp;</span>
    </div>
    <div id="content" style="clear:both; margin:0px">
    <%
    PlanDb pd = new PlanDb();
    String sql = "select id from " + pd.getTableName() + " where userName=" + StrUtil.sqlstr(privilege.getUser(request)) + " and is_notepaper=1 and is_closed=0 order by myDate desc";
    int count = 10;
	Iterator ir = pd.listResult(sql, 1, count).getResult().iterator();
    while (ir.hasNext()) {
        pd = (PlanDb)ir.next();
        %>
        <div style="clear:both" id="notepaper<%=pd.getId()%>" onmousemove="o('notepaperImg<%=pd.getId()%>').style.display=''" onmouseout="o('notepaperImg<%=pd.getId()%>').style.display='none';">
        <a href="javascript:;" onclick="addTab('便笺', '<%=request.getContextPath()%>/plan/plan_show.jsp?id=<%=pd.getId()%>')"><%=DateUtil.format(pd.getMyDate(), "yy-MM-dd")%>&nbsp;<%=pd.getTitle()%></a>
        <img id="notepaperImg<%=pd.getId()%>" title="置为已完成" style="display:none; cursor:pointer" src="<%=request.getContextPath()%>/images/tick.png" onclick="setClosed(<%=pd.getId()%>)" />
        </div>
        <%
    }
    %>
    </div>
    <script>
    var curPlanClosedId;
    
    var errFunc = function(response) {
		alert('Error ' + response.status + ' - ' + response.statusText);
		alert(response.responseText);
    }
    
    function doClosed(response){
		var rsp = response.responseText.trim();
		if (rsp.trim()=="true") {
		o("notepaper" + curPlanClosedId).style.display = "none";
		}
    }
    
    function setClosed(planId) {
		curPlanClosedId = planId;
		var params = "op=close&id=" + planId;
		var myAjax = new cwAjax.Request( 
			"<%=request.getContextPath()%>/plan/notepaper_desktop.jsp",
			{
				method:"post",
				parameters:params,
				onComplete:doClosed,
				onError:errFunc
			}
		);
		return;
    }
	
	$(document).ready(function() {
		$("#memo_area_content").niceScroll({touchbehavior:false,cursorcolor:"#666",cursoropacitymax:0.8,cursorborder:"1px solid #ccc",horizrailenabled:false});
	});
    </script>  
  </div>
</div>
</body>
</html>