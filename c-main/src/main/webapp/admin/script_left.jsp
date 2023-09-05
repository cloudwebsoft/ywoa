<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("getTables")) {
	String dbSource = ParamUtil.get(request, "dbSource");
	%>
		<option value="">请选择</option>	
	<%
	try {
		JdbcTemplate jt = new JdbcTemplate(dbSource);
		Iterator ir = jt.getTableNames().iterator();
		while (ir.hasNext()) {
			String tableName = (String)ir.next();
			%>
			<option value="<%=tableName%>"><%=tableName%></option>		
			<%
		}
	}
	catch(Exception e) {
		e.printStackTrace();
	}
	return;
}
else if (op.equals("getFields")) {
	String table = ParamUtil.get(request, "table");
	String dbSource = ParamUtil.get(request, "dbSource");
	
	String sql = "select * from " + table;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		for (int i = 1; i <= colCount; i++) {
			%>
			<div><a href="javascript:;" onClick="window.top.mainScriptFrame.insertScript(' <%=rm.getColumnName(i)%>');"><%=rm.getColumnName(i)%></a></div>
			<%
		}
	}
	finally {
		conn.close();
	}
	return;
}
else if (op.equals("getFormFields")) {
	String formCode = ParamUtil.get(request, "formCode");
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	Iterator ir = fd.getFields().iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		%>
        <div class="item"><a title="点击插入字段" href="javascript:;" onClick="selField('<%=ff.getName()%>')"><%=ff.getTitle()%></a></div>
		<%
	}
	return;
}
else if (op.equals("getScriptTemplate")) {
	long id = ParamUtil.getLong(request, "id");
	FormDb fd = new FormDb();
	fd = fd.getFormDb("script_template");
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
	fdao = fdao.getFormDAO(id, fd);
	out.print(fdao.getFieldValue("script"));
	return;
}
else if (op.equals("getLoadSql")) {
	String dbSource = ParamUtil.get(request, "dbSource");
	String tableName = ParamUtil.get(request, "tableName");
	String sql = "select * from " + tableName;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		String fields = "", token = "";
		for (int i = 1; i <= colCount; i++) {
			if (fields.equals("")) {
				fields = rm.getColumnName(i);
			}
			else {
				fields += ", " + rm.getColumnName(i);
			}
		}
		sql = "sql = \"select " + fields + " from " + tableName + "\";";
		out.print(sql);
	}
	finally {
		conn.close();
	}
	return;	
}
else if (op.equals("getInsertSql")) {
	String dbSource = ParamUtil.get(request, "dbSource");
	String tableName = ParamUtil.get(request, "tableName");
	String sql = "select * from " + tableName;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		String fields = "", token = "";
		for (int i = 1; i <= colCount; i++) {
			if (fields.equals("")) {
				fields = rm.getColumnName(i);
				token = "?";
			}
			else {
				fields += ", " + rm.getColumnName(i);
				token += ", ?";
			}
		}
		sql = "sql = \"insert into " + tableName + " (" + fields + ") values (" + token + ")\";";
		out.print(sql);
	}
	finally {
		conn.close();
	}
	return;
}
else if (op.equals("getUpdateSql")) {
	String dbSource = ParamUtil.get(request, "dbSource");
	String tableName = ParamUtil.get(request, "tableName");
	String sql = "select * from " + tableName;
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		String fields = "";
		for (int i = 1; i <= colCount; i++) {
			if (fields.equals("")) {
				fields = rm.getColumnName(i) + "=?";
			}
			else {
				fields += ", " + rm.getColumnName(i) + "=?";
			}
		}
		sql = "// 修改语句，请检查主键是否正确\r\n";	
		sql += "sql = \"update " + tableName + " set " + fields + " where id=?\";";
		out.print(sql);
	}
	finally {
		conn.close();
	}
	return;
}
else if (op.equals("getDeleteSql")) {
	String dbSource = ParamUtil.get(request, "dbSource");
	String tableName = ParamUtil.get(request, "tableName");
	String sql = "// 删除语句，请检查主键是否正确\r\n";
	sql += "sql = \"delete from " + tableName + " where id=?\";";
	out.print(sql);

	return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>脚本设计器 - 菜单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<style>
html {
	min-height: 100%;
	_height:100%;
	height:100%;
}
body {
	margin: 0;
	padding: 0;
	min-height: 100%;
	_height:100%;
	height:100%;
}

.tabDiv {
	min-height: 100%;	
	_height: 100%;
	height: 100%;
	padding-top: 10px;
}

.item {
	padding:3px;
	margin-left:5px;
}
</style>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery.form.js"></script>
</head>
<body style="margin:0px;padding:0px">
<%
String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
if (!formCode.equals("")) {
	fd = fd.getFormDb(formCode);
}
%>
<div id="tabs" style="height:100%">
    <ul>
      <li><a href="#tabs-1">表单</a></li>
      <li><a href="#tabs-2">脚本库</a></li>
      <li><a href="#tabs-3">数据源</a></li>
    </ul>
    <div id="tabs-1" class="tabDiv">
    	<div class="item">
    	<strong>表单</strong>：
    	<select id="formCode" name="formCode">
        <option value="">请选择</option>        
        <%
		Iterator ir = fd.list().iterator();
		while (ir.hasNext()) {
			FormDb fd2 = (FormDb)ir.next();
			%>
			<option value="<%=fd2.getCode()%>"><%=fd2.getName()%></option>
			<%
		}
		%>
        </select>
        <script>
		$('#formCode').val('<%=formCode%>');
		o("formCode").value = "<%=formCode%>";
		</script>
        </div>
		<%
		String formTableName = "";
		if (!formCode.equals("")) {
			formTableName = FormDb.getTableName(formCode);
		}
		%>
        <script>
		var formTableName = "<%=formTableName%>";
		</script>
        <div class="item">
        SQL：<a href="javascript:;" onClick="addLoadSql('<%=Global.getDefaultDB()%>', formTableName)">选择</a>
        &nbsp;&nbsp;<a href="javascript:;" onClick="addInsertSql('<%=Global.getDefaultDB()%>', formTableName)">添加</a>
        &nbsp;&nbsp;<a href="javascript:;" onClick="getDeleteSql('<%=Global.getDefaultDB()%>', formTableName)">删除</a>
        &nbsp;&nbsp;<a href="javascript:;" onClick="getUpdateSql('<%=Global.getDefaultDB()%>', formTableName)">修改</a>
        </div>
 
        <div id="formFieldsDiv">
		<%
        ir = fd.getFields().iterator();
        while (ir.hasNext()) {
            FormField ff = (FormField)ir.next();
            %>
            <div class="item"><a title="点击插入字段" href="javascript:;" onClick="selField('$<%=ff.getName()%>')"><%=ff.getTitle()%></a></div>
            <%
        }
        %>
        </div>
    </div>
    <div id="tabs-2" class="tabDiv">
		<%
        String scriptFormCode = "script_template";
        String sql = "select id from " + FormDb.getTableName(scriptFormCode) + " order by id desc";
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        ir = fdao.list(scriptFormCode, sql).iterator();
        while (ir.hasNext()) {
            fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
            %>
            <div class="item"><a title="点击插入脚本" href="javascript:;" onClick="selScriptTemplate(<%=fdao.getId()%>)"><%=fdao.getFieldValue("name")%></a></div>
            <%
        }
        %>
    </div>
    <div id="tabs-3" class="tabDiv">
        <div class="item">
        <strong>数据源</strong>：
        <select id="dbSource" name="dbSource">
        <option value="">请选择</option>
        <%
        cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
        ir = cfg.getDBInfos().iterator();
        while (ir.hasNext()) {
            DBInfo di = (DBInfo)ir.next();
            %>
            <option value="<%=di.name%>" <%=di.isDefault?"selected":""%>><%=di.name%></option>
            <%
        }
        %>
        </select>
        </div>
        <div class="item">
        表名：
        <select id="tables" name="tables">
        </select>
        </div>
        <div class="item">
        SQL：
        <a href="javascript:;" onClick="addLoadSql(o('dbSource').value, o('tables').value)">选择</a>
        &nbsp;&nbsp;<a href="javascript:;" onClick="addInsertSql(o('dbSource').value, o('tables').value)">添加</a>
        &nbsp;&nbsp;<a href="javascript:;" onClick="getDeleteSql(o('dbSource').value, o('tables').value)">删除</a>
        &nbsp;&nbsp;<a href="javascript:;" onClick="getUpdateSql(o('dbSource').value, o('tables').value)">修改</a>
        </div>        
        <div class="item">
        	<div id="fieldsDiv"></div>
        </div>
    </div>
</div>
</body>
<script>
$(function(){
	// 创建tabs
	$('#tabs').tabs();
		
	$('#dbSource').change(function() {
		if ($(this).val()=="")
			return;
		// 取所选数据源的表名
		var str = "op=getTables&dbSource=" + $(this).val();
		var myAjax = new cwAjax.Request(
			"script_left.jsp", 
			{ 
				method:"post", 
				parameters:str,
				onComplete:doGetTableOptions,
				onError:errFunc
			}
		);		
	});
	
	$('#tables').change(function() {
		if ($(this).val()=="")
			return;
		// 取所选数据源的表名
		var str = "op=getFields&table=" + $(this).val() + "&dbSource=" + $('#dbSource').val();
		var myAjax = new cwAjax.Request(
			"script_left.jsp", 
			{ 
				method:"post", 
				parameters:str,
				onComplete:doGetFields,
				onError:errFunc
			}
		);
	});
	
	$('#formCode').change(function() {
		formTableName = "ft_" + $(this).val();
		if ($(this).val()=="")
			return;
		// 取所选数据源的表名
		var str = "op=getFormFields&formCode=" + $(this).val();
		var myAjax = new cwAjax.Request(
			"script_left.jsp", 
			{
				method:"post", 
				parameters:str,
				onComplete:doGetFormFields,
				onError:errFunc
			}
		);		
	});	
	
	// 取默认数据源的表名
	var str = "op=getTables&dbSource=<%=Global.getDefaultDB()%>";
	var myAjax = new cwAjax.Request(
		"script_left.jsp", 
		{ 
			method:"post", 
			parameters:str,
			onComplete:doGetTableOptions,
			onError:errFunc
		}
	);	
});

function doGetTableOptions(response) {
	var rsp = response.responseText.trim();
	// alert(rsp);
	$("#tables").empty();
	$("#tables").append(rsp);
}

function doGetFields(response) {
	var rsp = response.responseText.trim();
	// alert(rsp);
	$("#fieldsDiv").html(rsp);
}

function doGetFormFields(response) {
	var rsp = response.responseText.trim();
	// alert(rsp);
	$("#formFieldsDiv").html(rsp);
}

var errFunc = function(response) {
	window.status = response.responseText;
}
	
function selField(fieldName) {
	window.top.mainScriptFrame.insertScript(" " + fieldName);
}
	
function selScriptTemplate(id) {
	$.ajax({
		type: "post",
		url: "script_left.jsp",
		data : {
			op: "getScriptTemplate",
			id : id
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			window.top.mainScriptFrame.insertScript("\r\n\r\n" + data.trim());
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}	

function addLoadSql(dbSource, tableName) {
	if (tableName=="") {
		alert("请选择数据表！");
		return;
	}
	$.ajax({
		type: "post",
		url: "script_left.jsp",
		data : {
			op: "getLoadSql",
			dbSource : dbSource,
			tableName : tableName
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			window.top.mainScriptFrame.insertScript("\r\n\r\n" + data.trim());
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}


function addInsertSql(dbSource, tableName) {
	if (tableName=="") {
		alert("请选择数据表！");
		return;
	}
	$.ajax({
		type: "post",
		url: "script_left.jsp",
		data : {
			op: "getInsertSql",
			dbSource : dbSource,
			tableName : tableName
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			window.top.mainScriptFrame.insertScript("\r\n\r\n" + data.trim());
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function getUpdateSql(dbSource, tableName) {
	if (tableName=="") {
		alert("请选择数据表！");
		return;
	}
	
	$.ajax({
		type: "post",
		url: "script_left.jsp",
		data : {
			op: "getUpdateSql",
			dbSource : dbSource,
			tableName : tableName
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			window.top.mainScriptFrame.insertScript("\r\n\r\n" + data.trim());
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function getDeleteSql(dbSource, tableName) {
	if (tableName=="") {
		alert("请选择数据表！");
		return;
	}
	
	$.ajax({
		type: "post",
		url: "script_left.jsp",
		data : {
			op: "getDeleteSql",
			dbSource : dbSource,
			tableName : tableName
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			window.top.mainScriptFrame.insertScript("\r\n\r\n" + data.trim());
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}
</script>
</html>
