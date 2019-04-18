<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.runqian.report4.util.ReportUtils"%>
<%@page import="com.runqian.report4.model.ReportDefine"%>
<%@page import="cn.js.fan.web.Global"%>
    <%
    	String path = request.getContextPath();
    	String graphUrl = request.getParameter("graphUrl");
    	String raqName = request.getParameter("raq");
    	//String raqPath = request.getSession().getServletContext().getRealPath("/")+"/reportFiles/"+raqName;
    	String raqPath = Global.getRealPath() +"/reportFiles/"+raqName;
    	ReportDefine rd = (ReportDefine)ReportUtils.read(raqPath);
    	int colCount = rd.getColCount();
    	String colNames = "";
    	for(int i=1;i<=colCount;i++){
    		colNames += rd.getCell(1,(short)i).getValue()==null?"":rd.getCell(1,(short)i).getValue().toString();
    		colNames +=","; 
    	}
    	colNames = colNames.substring(0,colNames.length()-1);
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>报表展示</title>
<link rel="stylesheet" type="text/css" media="screen" href="<%=path%>/mis2/gezComponents/jquery/jqueryui/jqueryuicss/jqueryui.css" /> 
<link rel="stylesheet" type="text/css" media="screen" href="<%=path%>/mis2/gezComponents/jquery/jqgrid/css/ui.jqgrid.css" /> 
<script src="<%=path%>/mis2/gezComponents/jquery/jquery.js" type="text/javascript"></script> 
<script src="<%=path%>/mis2/gezComponents/jquery/jqueryui/jqueryuijs/jqueryui.js" type="text/javascript"></script>
<script src="<%=path%>/mis2/gezComponents/jquery/jqgrid/js/i18n/grid.locale-en.js" type="text/javascript"></script>
<script src="<%=path%>/mis2/gezComponents/jquery/jqgrid/js/jquery.jqGrid.min.js" type="text/javascript"></script>
<script src="<%=path%>/mis2/gezComponents/jsUtils/JqGridUtil.js" type="text/javascript"></script>
<script type="text/javascript">
	$(document).ready(function(){
		var colNamesStr='<%=colNames%>';
		var colNamesArr = colNamesStr.split(",");
		var colCount = '<%=colCount%>';
		var colModel = [
			{name:"name",index:"name",width:"12%",sortable:false},
			{name:"AAT",index:"AAT",width:"6%",sortable:true,sorttype:"int"},
			{name:"SSE",index:"SSE",width:"6%",sortable:true,sorttype:"int"},
			{name:"PPT",index:"PPT",width:"6%",sortable:true,sorttype:"int"},
			{name:"ITC",index:"ITC",width:"6%",sortable:true,sorttype:"int"},
			{name:"PET",index:"PET",width:"12%",sortable:true,sorttype:"int"}
		];
		var jqUtil = new JqGridUtil();
		jqUtil.mSelect = false;
		jqUtil.jsonReader.reportitems = true;
		jqUtil.getGridData("demotable","<%=path%>/demoServlet","raq=<%=raqName%>",colNamesArr,colModel);
		$('#demotable').jqGrid('setGridParam',{onSelectRow:gridSelectRow});
	});
	function gridSelectRow(id){
		var rowData = $('#demotable').jqGrid('getRowData',id);
		$('#bframe').attr("src","<%=graphUrl %>?name="+rowData.name
							+"&AAT="+rowData.AAT
							+"&SSE="+rowData.SSE
							+"&PPT="+rowData.PPT
							+"&ITC="+rowData.ITC
							+"&PET="+rowData.PET);
	}
</script>
</head>
<body>
	<div>
		<table id="demotable"></table>
	</div>
	<div>
		<iframe id="bframe" src="<%=graphUrl %>" width="100%" height="100%"></iframe>
	</div>
</body>
</html>