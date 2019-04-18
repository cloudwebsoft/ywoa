<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	FormQueryReportMgr rm = new FormQueryReportMgr();
	FormQueryReportDb rd = new FormQueryReportDb();
	JSONObject json = new JSONObject();
	boolean re = false;
	try {
		re = rm.create(request, rd, "form_query_report_create");
		rd = rd.getLastFormQueryReportDb(privilege.getUser(request));
	}
	catch (ErrMsgException e) {
		// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage(), true));
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("id", rd.getLong("id"));
		out.print(json);
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);
	}
	return;
}
else if (op.equals("edit")) {
	FormQueryReportMgr rm = new FormQueryReportMgr();
	FormQueryReportDb rd = new FormQueryReportDb();
	long id = ParamUtil.getLong(request, "id");
	rd = (FormQueryReportDb)rd.getQObjectDb(new Long(id));
	JSONObject json = new JSONObject();
	boolean re = false;
	try {
		re = rm.save(request, rd, "form_query_report_save");
	}
	catch (ErrMsgException e) {
		e.printStackTrace();
		// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage(), true));
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("id", id);
		out.print(json);
		return;
	}
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		json.put("id", id);
		out.print(json);
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		json.put("id", id);
		out.print(json);
	}
	return;
}
else if (op.equals("getCols")) {
	String content = ParamUtil.get(request, "content");
	%>
    	<option value="">无</option>	
	<%
	Vector[] ary = FormQueryReportRender.parseCell(content);
	Vector vtY = ary[1];
	Iterator ir = vtY.iterator();
	while (ir.hasNext()) {
    	FormQueryReportCell fqrc = (FormQueryReportCell)ir.next();
		%>
		<option value="<%=fqrc.getFieldName()%>"><%=fqrc.getValue()%></option>
		<%
	}
	Vector vtX = ary[0];
	ir = vtX.iterator();
	while (ir.hasNext()) {
    	FormQueryReportCell fqrc = (FormQueryReportCell)ir.next();
		// 有可能为空单元格
		if (fqrc.getFieldName().equals(""))
			continue;
		%>
		<option value="<%=fqrc.getFieldName()%>"><%=fqrc.getValue()%></option>
		<%
	}
	return;
}

long id = ParamUtil.getLong(request, "id", -1);
int queryId = ParamUtil.getInt(request, "query_id", -1);
String title = "";
String content = "";
String orderby = "", sort="";
if (id!=-1) {
	FormQueryReportDb rd = new FormQueryReportDb();
	rd = (FormQueryReportDb)rd.getQObjectDb(new Long(id));
	if (rd == null) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请先创建报表！"));
		return;
	}
	queryId = rd.getInt("query_id");
	title = rd.getString("title");
	content = rd.getString("content");
	orderby = rd.getString("orderby");
	sort = rd.getString("sort");
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>报表设计器</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/designer.css" />
<script type="text/javascript" src="../../inc/common.js"></script>
<script type="text/javascript" src="../../js/jquery.js"></script>
<script src="../../js/jquery.form.js"></script>
<script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link href="../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="../../js/jquery-showLoading/jquery.showLoading.js"></script>

<script language="javascript" type="text/javascript" src="../../js/tinymce/tinymce.min.js"></script>
<style>
html,body{height:100%}
</style>
<script>
function onBeforeSubmit() {
	// alert(tinyMCE.getInstanceById('content').getBody().innerHTML);
	// alert(tinyMCE.get('content').getContent());
	// alert(tinyMCE.activeEditor.getContent({source_view : true}));
	// alert(tinyMCE.activeEditor.getContent({format : 'raw'}));
}

$(document).ready(function() {
    var options = { 
        //target:        '#output2',   // target element(s) to be updated with server response 
        // beforeSubmit:  automaticOnmodule_codeSubmit,  // pre-submit callback 
        beforeSubmit:  onBeforeSubmit,  // pre-submit callback 
        success:       showResponse  // post-submit callback 
 
        // other available options: 
        //url:       url         // override for form's 'action' attribute 
        //type:      type        // 'get' or 'post', override for form's 'method' attribute 
        //dataType:  null        // 'xml', 'script', or 'json' (expected server response type) 
        //clearForm: true        // clear all form fields after successful submit 
        //resetForm: true        // reset the form after successful submit 
 
        // $.ajax options can be used here too, for example: 
        //timeout:   3000 
    }; 

    // bind to the form's submit event
    $('#formDesigner').submit(function() {
		// 必须加上此句才能提交成功
		$('#formDesigner').bind('form-pre-serialize', function(event, form, options, veto) {
			tinyMCE.triggerSave();
		});
		
        $(this).ajaxSubmit(options);
			
        return false;
    });
});

function showResponse(responseText, statusText, xhr, $form) {
	var data = $.parseJSON(responseText);
	alert(data.msg);
	if (data.ret=="1") {
		window.location.href = "designer.jsp?id=" + data.id;
	}

	/*
	// 会导致右边栏的样式变化
	jAlert(data.msg, '提示', function(r) {
		if (data.ret=="1")
			window.location.href = "designer.jsp?id=" + data.id;
	});
	*/
}

function selQuery() {
	openWin("../form_query_list_sel.jsp?type=all&isSystem=true", 800, 600);
}

function doSelQuery(id, title) {
	if (id==<%=queryId%>)
		return;
	
	<%if (id!=-1) {%>
	if (!confirm("您确定要重新选择么？"))
		return;
	<%}%>
	
	$("#query_id").val(id);
	
	// $("#divRelatedQuery").html($("#divRelatedQuery").html() + "<div id='divQuery" + id + "'><a href='javascript:;' onclick=\"\">" + title + "</a><span style='color:red; font-size:14px; padding-left:5px; cursor:pointer; display:none' title='删除' onclick=\"delQuery('" + id + "')\">×</span></div>");
	$("#formDesigner").submit();
}

$(document).ready(function() {
	rendTinyMCE();
});
</script>
</head>
<body style="margin:0px; padding:0px">
<div id="mainDiv" style="width:100%; height:100%">
<form id="formDesigner" action="designer.jsp" method="post">
<table id="mainTable" border="0" cellspacing="0" style="width:100%;height:100%;margin:0px;padding:0px">
<tr>
	<td id="tdTinyMCE" valign="top">
    <textarea id="content" name="content"><%=content%></textarea>
	</td>
  	<td id="rightTd" valign="top" style="width:300px; padding:0px; font-size:12px">
      <div id="item_name" class="rebbonItem">
          <div>
            <div class="item_title">报表名称<span id="formName"></span></div>
            <div class="item_content">
                <input id="title" name="title" value="<%=title%>" />
                
                <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
                <input type="submit" value="保存" />
				<%if (id==-1) {%>
                <input name="op" value="add" type="hidden" />
                <%}else{%>
                <input name="op" value="edit" type="hidden" />
                <input type="button" value="查看" onClick="addTab('<%=title%>', 'flow/report/form_report_show_jqgrid.jsp?reportId=<%=id%>')" />
               <%}%>
                <input name="id" value="<%=id%>" type="hidden" />
                <input id="query_id" name="query_id" value="<%=queryId%>" type="hidden" />
            </div>
          </div>
          
          <div>
            <div class="item_title">查询结果集&nbsp;&nbsp;[<a href="javascript:;" onClick="selQuery()">选择</a>]</div>
            <div class="item_content">
				<div id="divRelatedQuery">
                <%
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb((int)queryId);
					if (fqd.isScript()) {
						%>
						<a href="javascript:;" onClick="addTab('<%=fqd.getQueryName()%>', 'flow/form_query_script.jsp?id=<%=fqd.getId()%>')"><%=fqd.getQueryName()%></a>
						<%
					}
					else {
						%>
						<a href="javascript:;" onClick="addTab('<%=fqd.getQueryName()%>', 'flow/designer/designer.jsp?id=<%=fqd.getId()%>')"><%=fqd.getQueryName()%></a>
						<%
					}
				}
				%>
                </div>

            </div>
          </div>

          <div>
            <div class="item_title">默认排序</div>
            <div class="item_content">
            <select id="orderby" name="orderby">
            <option value="">无</option>
			<%
            Vector[] ary = FormQueryReportRender.parseCell(content);
            Vector vtY = ary[1];
            Iterator irV = vtY.iterator();
            while (irV.hasNext()) {
                FormQueryReportCell fqrc = (FormQueryReportCell)irV.next();
                %>
                <option value="<%=fqrc.getFieldName()%>"><%=fqrc.getValue()%></option>
                <%
            }
            Vector vtX = ary[0];
            irV = vtX.iterator();
            while (irV.hasNext()) {
                FormQueryReportCell fqrc = (FormQueryReportCell)irV.next();
                // 有可能为空单元格
                if (fqrc.getFieldName().equals(""))
                    continue;
                %>
                <option value="<%=fqrc.getFieldName()%>"><%=fqrc.getValue()%></option>
                <%
            }
            %>
            </select>
            
            <select id="sort" name="sort">
            <option value="asc">升序</option>
            <option value="desc">降序</option>
            </select>
            
            <script>
			o("orderby").value = "<%=orderby%>";
			o("sort").value = "<%=sort%>";
			</script>
            </div>
          </div>
			
          <div>
            <div class="item_title">X轴字段</div>
            <div class="item_content">
				<%
				String optsX = "";
				String opts = "";
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb(queryId);
					if (!fqd.isScript()) {
						String formCode = fqd.getTableCode();
						FormDb fd = new FormDb();
						fd = fd.getFormDb(formCode);
						%>
						<script>
							o("formName").innerHTML = "&nbsp;(<%=fd.getName()%>)";
						</script>
						<%
						// 取得关联查询中默认的colProps
						String queryRelated = fqd.getQueryRelated();
						if (false && !queryRelated.equals("")) {
							int queryRelatedId = StrUtil.toInt(queryRelated, -1);
							FormQueryDb aqdRelated = fqd.getFormQueryDb(queryRelatedId);
							FormDb fdRelated = fd.getFormDb(aqdRelated.getTableCode());
							Iterator ir = fdRelated.getFields().iterator();
							while (ir.hasNext()) {
								FormField ff = (FormField)ir.next();
								// if (!ff.isCanList())
								// 	continue;
								opts += "<option value='rel." + ff.getName() + "'>" + ff.getTitle() + "</option>";
							}
						}
						
						Iterator ir = fd.getFields().iterator();
						while (ir.hasNext()) {
							FormField ff = (FormField)ir.next();
							// if (!ff.isCanList())
							// 	continue;
							opts += "<option value='" + ff.getName() + "'>[ " + ff.getTitle() + " ]</option>";
							optsX += "<option value='" + ff.getName() + "'>[ " + ff.getTitle() + " ]</option>";
						}
					}
					else {
						QueryScriptUtil qsu = new QueryScriptUtil();
						HashMap map = qsu.getCols(request, fqd);
						Iterator irMap = map.keySet().iterator();
						while (irMap.hasNext()) {
							String keyName = (String) irMap.next();

							opts += "<option value='" + keyName + "'>" + map.get(keyName) + "</option>";
							optsX += "<option value='" + keyName + "'>" + map.get(keyName) + "</option>";
						}
					}
					%>
                    标题<input type="text" id="fieldTitle" name="fieldTitle" />
					<select id="fieldName" name="fieldName">
					<%=optsX%>
                    </select>
                    <select id="formula" name="formula">
                      <option value="sum">求和</option>
                      <option value="average">平均值</option>
                      <option value="count">数量</option>
                    </select>
                    <input type="button" value="确定" onClick="insertXCol()" />
                <%}%>
            </div>
          </div>
          <div>
            <div class="item_title">X轴&nbsp;SQL字段</div>
            <div class="item_content">
            	<div>标题<input id="sqlFieldTitle" name="sqlFieldTitle" /></div>
                <div style="margin:5px 0px">请输入SQL</div>
                <div style="text-align:center">
				  <textarea id="sql" name="sql" style="width:98%; height:60px"></textarea>
                  <input type="button" value="确定" onClick="insertSQLCol()" style="margin-top:5px" />
                </div>
		    </div>
          </div>
          
          <div>
            <div class="item_title">X轴&nbsp;脚本字段</div>
            <div class="item_content">
            	<div>标题<input id="scriptFieldTitle" name="scriptFieldTitle" /></div>
                <div style="margin:5px 0px">请输入脚本</div>
                <div style="text-align:center">
				<textarea id="script" name="script" style="width:98%; height:60px"></textarea>
                <input type="button" value="确定" onClick="insertScriptCol()" style="margin-top:5px" />
				<input type="button" value="设计器" class="btn" onClick="curScriptCtlId='script'; ideWin=openWin('../../admin/script_frame.jsp', screen.width, screen.height);" />                
                </div>
		    </div>
          </div>
          
          <div>
            <div class="item_title">Y轴</div>
            <div class="item_content">
            	<%if (queryId!=-1) {%>
                    Y轴
                    <select id="fieldtype" name="fieldtype">
                    <option value="">请选择类型</option>
                    <option value="<%=FormQueryReportDb.TYPE_USER%>">姓名</option>
                    <option value="<%=FormQueryReportDb.TYPE_DEPT%>">部门</option>
                    <option value="<%=FormQueryReportDb.TYPE_FIELD%>">字段</option>
                    </select>
                    
                    <select id="fieldNameY" name="fieldNameY">
                    <%=opts%>
                    </select>

                    <div style="text-align:center; margin-top:3px"><input value="确定" onClick="insertYCol()" type="button" /></div>
                <%}%>
            </div>
          </div>          
          
          <div>
            <div class="item_title">Y轴&nbsp;脚本字段</div>
            <div class="item_content">
            	<div>标题<input id="scriptFieldTitleY" name="scriptFieldTitleY" /></div>
                <div style="margin:5px 0px">请输入脚本</div>
                <div style="text-align:center">
				<textarea id="scriptY" name="scriptY" style="width:98%; height:60px"></textarea>
                <input type="button" value="确定" onClick="insertScriptColY()" style="margin-top:5px" />
				<input type="button" value="设计器" class="btn" onClick="curScriptCtlId='scriptY'; ideWin=openWin('../../admin/script_frame.jsp', screen.width, screen.height);" />                
                </div>
		    </div>
          </div>          
          
      </div>
    </td>
</tr>
</table>
</form>
</div>
<div id="result" style="display:none"></div>
</body>

<script>
var curScriptCtlId;
function getScript() {
	return $('#' + curScriptCtlId).val();
}

function setScript(script) {
	$('#' + curScriptCtlId).val(script);
}

function insertXCol() {
	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	/*
	var optText = $("#fieldName").find("option:selected").text();
	if (optText.indexOf("[")==0) {
		// 去掉嵌套表格字段两边的[ ]
		optText = optText.substring(1, optText.length-1).trim();	
	}
	*/
	
	var ft = $("#fieldTitle").val();
	if (ft=="") {
		alert("请输入标题！");
		o("fieldTitle").focus();
		return;
	}

	var str = "<input name='xCol' axis='X' fieldname='" + $("#fieldName").val() + "' value='" + ft + "' formula='" + $("#formula").val() + "'>";
	// 这样会使得插入表格的单元格时，表格会消失
	// tinyMCE.execCommand('mceInsertRawHTML', false, str);
	
	tinyMCE.activeEditor.focus();
	tinyMCE.activeEditor.selection.setContent(str);
	getCols();
}

var curNode;
function insertSQLCol() {
	if (o("sqlFieldTitle").value=="") {
		alert("标题不能为空！");
		return;
	}
	
	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	var fieldname = '<%=FormQueryReportCell.SQL_COL_PREFIX%>' + new Date().getTime();
	var str = "<input name='xCol' axis='X' fieldname='" + fieldname + "' value='" + o("sqlFieldTitle").value + "' formula='" + escape($("#sql").val()) + "'>";
	tinyMCE.activeEditor.focus();
	
	tinymce.activeEditor.selection.select(curNode);

	// 选中的可能是tr、td、&nbsp;及input、input
	if (curNode.nodeName=="TR") {
		// 取最后一个td
		curNode = $(curNode).children('td:last')[0];
	}
	
	// 当单元格中有空格时，空格与控件同时会被选中
	if (curNode.nodeName!="INPUT") {
		curNode.innerHTML = str;
	}
	else {
		tinyMCE.activeEditor.selection.setContent(str);
	}
}

function insertScriptCol() {
	if (o("scriptFieldTitle").value=="") {
		alert("标题不能为空！");
		return;
	}
	
	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	var fieldname = '<%=FormQueryReportCell.SCRIPT_COL_PREFIX%>' + new Date().getTime();
	var str = "<input name='xCol' axis='X' fieldname='" + fieldname + "' value='" + o("scriptFieldTitle").value + "' formula='" + escape($("#script").val()) + "'>";
	tinyMCE.activeEditor.focus();
	
	tinymce.activeEditor.selection.select(curNode);

	// 选中的可能是tr、td、&nbsp;及input、input
	if (curNode.nodeName=="TR") {
		// 取最后一个td
		curNode = $(curNode).children('td:last')[0];
	}
	
	// 当单元格中有空格时，空格与控件同时会被选中
	if (curNode.nodeName!="INPUT") {
		curNode.innerHTML = str;
	}
	else {
		tinyMCE.activeEditor.selection.setContent(str);
	}
}

function insertScriptColY() {
	if (o("scriptFieldTitleY").value=="") {
		alert("标题不能为空！");
		return;
	}
	
	if ($('#fieldtype').val()=="") {
		alert("请选择Y轴的类型！");
		return;
	}

	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	var fieldname = '<%=FormQueryReportCell.SCRIPT_COL_PREFIX%>' + new Date().getTime();
	var str = "<input name='yCol' axis='Y' fieldname='" + fieldname + "' value='" + o("scriptFieldTitleY").value + "' fieldtype='" + $("#fieldtype").val() + "' formula='" + escape($('#scriptY').val()) + "'>";

	tinyMCE.activeEditor.focus();
	
	tinymce.activeEditor.selection.select(curNode);

	// 选中的可能是tr、td、&nbsp;及input、input
	if (curNode.nodeName=="TR") {
		// 取最后一个td
		curNode = $(curNode).children('td:first')[0];
	}
	
	// 当单元格中有空格时，空格与控件同时会被选中
	if (curNode.nodeName!="INPUT") {
		curNode.innerHTML = str;
	}
	else {
		tinyMCE.activeEditor.selection.setContent(str);
	}
}

function insertYCol() {
	var selection = tinyMCE.activeEditor.selection.getNode().getAttribute("fieldname");
	if (selection) {
		var obj = tinyMCE.activeEditor.selection.getNode();
		obj.setAttribute("fieldname", $("#fieldNameY").val());
		obj.setAttribute("value", $("#fieldNameY").find("option:selected").text());
		obj.setAttribute("fieldtype", $("#fieldtype").val());
		obj.setAttribute("axis", "Y");
		// 先删再加上才能正确显示，否则不刷新
		// obj.outerHTML = "";
		var str = obj.outerHTML;
		$(obj).remove();
		
		tinyMCE.activeEditor.selection.setContent(str);
		
		// 按以下写法也不刷新
		// tinyMCE.isNotDirty = true;
		// tinyMCE.activeEditor.nodeChanged();
		// tinyMCE.activeEditor.selection.collapse();
		// tinyMCE.activeEditor.execCommand("mceRepaint");
		// tinyMCE.activeEditor.render();
		
		return;
	}
	
	// 注意fieldname必须为小写，否则会被tinyMCE过滤掉
	// alert($("#fieldName").find("option:selected").text());
	if ($('#fieldtype').val()=="") {
		alert("请选择Y轴的类型！");
		return;
	}
	
	var str = "<input name='yCol' axis='Y' fieldname='" + $("#fieldNameY").val() + "' value='" + $("#fieldNameY").find("option:selected").text() + "' fieldtype='" + $("#fieldtype").val() + "'>";
	// 这样会使得插入至表格的单元格时，表格会消失
	// tinyMCE.execCommand('mceInsertRawHTML', false, str);
	
	tinyMCE.activeEditor.focus();
	tinyMCE.activeEditor.selection.setContent(str);
}

function test() {
	// tinyMCE.execCommand('mceInsertContent',false,"<p><img src=\"../images/house.jpg\" alt=\"\" width=\"588\" height=\"419\" /></p>");
	// alert(tinyMCE.getInstanceById('content').getBody().innerHTML);
	// var selection = tinyMCE.activeEditor.selection.getContent({format : 'raw'});
	
	var selection = tinyMCE.activeEditor.selection.getNode().fieldname;
	alert(selection);
}

function rendTinyMCE() {
	tinymce.init({
		selector: "#content",
		width : "100%",
		height : o("mainDiv").clientHeight - dlt,
		language : 'zh_CN',
		plugins: [
			"advlist autolink lists link image charmap print preview anchor",
			"searchreplace visualblocks code fullscreen",
			"insertdatetime media table contextmenu paste"
		],
		
		// skin : 'charcoal',
		valid_elements : '*[*]',
		
		setup: function(editor) {
			editor.on('NodeChange', function(e) {
				curNode = tinymce.activeEditor.selection.getNode();
				// 如果单元格仅为控件，则选择到的是控件，如果有空格，则所选的为 空格+控件
				if (e.element.innerHTML.indexOf("input")==-1) {
					if (curNode.nodeName!="INPUT")
						return;
				}
				else {
					$(e.element).find("input").each(function() {
						curNode = this;
					});
				}
				
				// alert(curNode.getAttribute("axis"));
				
				// 因未判断事件，所以会被调用两次，鼠标按下一次，弹起一次
				if ($(curNode).attr("axis")=="X") {
					if ($(curNode).attr("fieldName").indexOf("<%=FormQueryReportCell.SQL_COL_PREFIX%>")==0) {
						$("#sqlFieldTitle").val($(curNode).attr("value"));
						$("#sql").val(unescape($(curNode).attr("formula")));
					}
					else if ($(curNode).attr("fieldName").indexOf("<%=FormQueryReportCell.SCRIPT_COL_PREFIX%>")==0) {
						$("#scriptFieldTitle").val($(curNode).attr("value"));
						$("#script").val(unescape($(curNode).attr("formula")));
					}
					else {
						$("#fieldName").val($(curNode).attr("fieldname"));
						$("#formula").val($(curNode).attr("formula"));
						$("#fieldTitle").val($(curNode).val());
					}
				}
				else {
					if ($(curNode).attr("fieldName").indexOf("<%=FormQueryReportCell.SCRIPT_COL_PREFIX%>")==0) {
						$("#scriptFieldTitleY").val($(curNode).attr("value"));
						$("#scriptY").val(unescape($(curNode).attr("formula")));
					}
					else {					
						$("#fieldNameY").val($(curNode).attr("fieldname"));
						$("#fieldtype").val($(curNode).attr("fieldtype"));
					}
				}
				
			});
		},
		
		toolbar: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image"
	});
	
}

// window.setInterval("reinitIframe()", 200);

function toScreenHeight(id, minus) {
    var height;

    if (typeof(window.innerHeight) == "number") //non-IE
    	height = window.innerHeight;
    else if (document.documentElement && document.documentElement.clientHeight) //IE 6+ strict mode
    	height = document.documentElement.clientHeight;
    else if (document.body && document.body.clientHeight) //IE 4 compatible / IE quirks mode
    	height = document.body.clientHeight;

    document.getElementById(id).style.height = (height - minus) + "px";
}

var dlt = 112;
function resizeTiny() {	
	if (!tinymce)
		return;
	var w = o("tdTinyMCE").clientWidth;
	// 减去110，可能是相当于工具栏部分的高度
	var h = o("mainDiv").clientHeight - dlt;
	tinymce.get("content").theme.resizeTo(w, h);
}

$(window).resize(function() {
  resizeTiny();
});

// 取得列
function getCols() {
	$.ajax({
		type: "post",
		url: "designer.jsp",
		data : {
			id: "<%=id%>",
			content: tinyMCE.get("content").getContent(),
			op: "getCols"
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#mainDiv').showLoading();				
		},
		success: function(data, status){
			$("#orderBy").empty();
			$("#orderBy").append(data);
		},
		complete: function(XMLHttpRequest, status){
			$('#mainDiv').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}


<%
com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
String version = StrUtil.getNullStr(oaCfg.get("version"));
String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>

var ideWin;
var onMessage = function(e) {
	var d = e.data;
	var data = d.data;
	var type = d.type;
	if (type=="setScript") {
		setScript(data);
	}
	else if (type=="getScript") {
		var data={
		    "type":"openerScript",
		    "version":"<%=version%>",
		    "spVersion":"<%=spVersion%>",
		    "data":getScript()
	    }
		ideWin.mainScriptFrame.postMessage(data, '*');
	}
}

$(function() {
     if (window.addEventListener) { // all browsers except IE before version 9
         window.addEventListener("message", onMessage, false);
     } else {
         if (window.attachEvent) { // IE before version 9
             window.attachEvent("onmessage", onMessage);
         }
     }
});
</script>
</html>