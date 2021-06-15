<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.sql.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String mode = ParamUtil.get(request, "mode");
	int id = ParamUtil.getInt(request, "id", -1);
	
	// @task:权限检查
	if (!mode.equals("moduleTag") && !mode.equals("selField")) {
		if (!privilege.isUserPrivValid(request, "admin.flow.query")) {		
			FormQueryPrivilegeMgr aqpm = new FormQueryPrivilegeMgr();
			if (!aqpm.canUserQuery(request, id)) {
				out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
				out.print(SkinUtil.makeErrMsg(request, "您没有被授权该查询！"));
				return;
			}
		}
	}
	
	// 用于查询字段选择控件
	String openerFormCode = ParamUtil.get(request, "openerFormCode");
	String openerFieldName = ParamUtil.get(request, "openerFieldName");
	JSONArray mapsCond = new JSONArray();	

	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>查询结果</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../inc/map.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery.form.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script src="designer/condition_value.js"></script>
<script>
var condStr = "";
<%
if (mode.equals("selField")) {
		FormDb openerFd = new FormDb();
		openerFd = openerFd.getFormDb(openerFormCode);
		if (!openerFd.isLoaded()) {
			out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
			return;
		}
		
		FormField ff = openerFd.getFormField(openerFieldName);
		String strDesc = ff.getDescription();
		JSONObject json = null;
		
		try {
			json = new JSONObject(strDesc);
			// formCode = json.getString("formCode");
			id = StrUtil.toInt(json.getString("queryId"));
			// 解码，替换%sq %dq，即单引号、双引号
			// String filter = StrUtil.decodeJSON(json.getString("filter"));
			mapsCond = (JSONArray)json.get("mapsCond");
			
			JSONArray mapAry = (JSONArray)json.get("maps");
			
			String idField = (String)json.get("idField");
			String showField = (String)json.get("showField");
			
			%>
            <script>
			var mapAry = <%=mapAry%>;
			var idField = "<%=idField%>";
			var showField = "<%=showField%>";
			</script>
			<%			
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "json格式非法！"));
			return;
		}			
	}
	
	String op = ParamUtil.get(request, "op");
	
	FormQueryDb aqd = new FormQueryDb();
	if (id==-1) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "id不能为空！"));
		return;
	}
	
	aqd = aqd.getFormQueryDb(id);
	if (!aqd.isLoaded()) {
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "查询已不存在！"));
		return;
	}
for (int i=0; i<mapsCond.length(); i++) {
	JSONObject j = null;
	try {
		j = mapsCond.getJSONObject(i);
		String parentWinField = (String) j.get("destField");
		%>
		if (condStr=="")
			condStr = "<%=parentWinField%>=" + window.opener.o("<%=parentWinField%>").value;
		else
			condStr += "&<%=parentWinField%>=" + window.opener.o("<%=parentWinField%>").value;
		<%
	} catch (JSONException ex) {
		ex.printStackTrace();
	}
}
%>
</script>
<style>
body {
	margin:0px;
	padding:0px;
}
</style>
</head>
<body>
<%
if (mode.equals("moduleTag")) {
	String tagName = ParamUtil.get(request, "tagName");
	%>
	<%@ include file="../visual/module_inc_menu_top.jsp"%>
    <script>
	$("li[tagName='<%=tagName%>']").addClass("current");
	</script>
	<%
}
%>
<%
String sql = "select name,title,type,macroType,defaultValue,fieldType,canNull,fieldRule,canQuery,canList from form_field where formCode=? and canList=1";
sql += " order by orders asc";

FormDb fd = new FormDb();
fd = fd.getFormDb(aqd.getTableCode());

String colProps = "";

// 取得关联查询中默认的colProps
String queryRelated = aqd.getQueryRelated();
if (!queryRelated.equals("")) {
   	int queryRelatedId = StrUtil.toInt(queryRelated, -1);
	FormQueryDb aqdRelated = aqd.getFormQueryDb(queryRelatedId);
	FormDb fdRelated = fd.getFormDb(aqdRelated.getTableCode());
	Iterator ir = fdRelated.getFields().iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		if (!ff.isCanList())
			continue;
		
		if (colProps.equals(""))
			colProps = "{display: '" + ff.getTitle() + "', name : 'rel." + ff.getName() + "', width : " + ff.getWidth() + ", sortable : true, align: 'center', hide: false}";
		else
			colProps += ",{display: '" + ff.getTitle() + "', name : 'rel." + ff.getName() + "', width : " + ff.getWidth() + ", sortable : true, align: 'center', hide: false}";
	}
}

Iterator ir = fd.getFields().iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	if (!ff.isCanList())
		continue;

	if (colProps.equals(""))
		colProps = "{display: '" + ff.getTitle() + "', name : '" + ff.getName() + "', width : " + ff.getWidth() + ", sortable : true, align: 'center', hide: false}";
	else
		colProps += ",{display: '" + ff.getTitle() + "', name : '" + ff.getName() + "', width : " + ff.getWidth() + ", sortable : true, align: 'center', hide: false}";
}

// System.out.println(getClass() + " colProps1=" + colProps);

String preProps = "{display: '流程号', name : 'flowId', width : 50, sortable : true, align: 'center', hide: false}";
preProps += ",{display: '标题', name : 'flowTitle', width : 150, sortable : false, align: 'center', hide: false}";
preProps += ",{display: '发起人', name : 'flowStarter', width : 60, sortable : false, align: 'center', hide: false}";
preProps += ",{display: '开始日期', name : 'flowBeginDate', width : 100, sortable : false, align: 'center', hide: false}";
preProps += ",{display: '状态', name : 'flowStatus', width : 60, sortable : false, align: 'center', hide: false}";

colProps += ",{display: '操作', name : '" + QueryScriptUtil.CWS_OP + "', width : 60, sortable : false, align: 'center', hide: false}";

if (colProps.equals(""))
	colProps = "[" + preProps + "]";
else
	colProps = "[" + preProps + "," + colProps + "]";

if (op.equals("resetCol")) {
	aqd = aqd.getFormQueryDb(id);
	aqd.setColProps(colProps);
	aqd.save();
	out.print(StrUtil.jAlert_Redirect("操作成功","提示", "form_query_list_do.jsp?id=" + id));
	return;
}

// System.out.println(getClass() + " colProps=" + colProps);
%>

<table id="queryTable" style="display:none"></table>
<div id="dlg" style="display:none"></div>
<script>
/*
var colM = [
	{display: '所在部门', name : 'department', width : 80, sortable : false, align: 'center'},
	{display: '姓名', name : 'real_name', width : 50, sortable : false, align: 'center'},
	];
*/
var colM = "";
<%
String colP = aqd.getColProps();
if (!colP.equals("") && !colP.equals("[]")) {
%>
	colM = <%=colP%>;
<%
}
else {
	// 如果加了引号，会致使flexprid解析失败，出现死循环，生成的列全是undefined，而且不会调用ajax获取数据
%>
	colM = <%=colProps%>;
<%
	// 初始化数据库中的colProps
	aqd = aqd.getFormQueryDb(id);
	aqd.setColProps(colProps);
	aqd.save();
}
%>
function getNameOfCol(display) {
	for (var i=0; i<colM.length; i++) {
		if (colM[i].display == display)
			return colM[i].name;
	}
	return "";
}

var rowDbClick = function(rowData) {
	// alert($(rowData).data("deptName").toString());
}

function saveColProps() {
	var str = '';
	$('th', $(".hDiv")).each(function(i) {
		var hide = $(this).css("display")=="none"? true:false;
		var sortable = true;
		var fieldName = getNameOfCol($(this).text());
		if (fieldName=="flowTitle")
			sortable = false;
		else if (fieldName=="flowStarter")
			sortable = false;
		else if (fieldName=="flowBeginDate")
			sortable = false;
		if (str=='') {
			str = "{display: '" + $(this).text() + "', name : '" + getNameOfCol($(this).text()) + "', width : " + ($(this).width()-8) + ", sortable : " + sortable + ", align: '" + $(this).attr("align") + "', hide: " + hide + "}";
		}
		else {
			str += ",{display: '" + $(this).text() + "', name : '" + getNameOfCol($(this).text()) + "', width : " + ($(this).width()-8) + ", sortable : " + sortable + ", align: '" + $(this).attr("align") + "', hide: " + hide + "}";
		}
	});
	
	str = "[" + str + "]";

	$.ajax({
	   type: "POST",
	   url: "form_query_list_ajax.jsp",
	   data: "op=modifyColProps&colProps=" + str + "&id=<%=id%>",
	   success: function(html){
			var json = jQuery.parseJSON(html);
			if (json.re==true) {
				// alert("保存成功！");
			}
			else {
				jAlert("保存列调整失败！","提示");
			}
		}
	});
}

var colSwitch = function(i, j) {
	saveColProps();
}

var colResize = function() {
	saveColProps();
}

var toggleCol = function(cid, visible) {
	if (visible)
		$("#" + cid).width(100);
	saveColProps();
}

var onReload = function() {
	window.location.href = "form_query_list_do.jsp?id=<%=id%>";
}

<%
// 当mode为moduleTag时，将会有moduleId参数传进来
int moduleId = ParamUtil.getInt(request, "moduleId", -1);
String moduleFormCode = ParamUtil.get(request, "moduleCode");
String tagName = ParamUtil.get(request, "tagName");
int pageSize = ParamUtil.getInt(request, "rp", 20);
%>
$("#queryTable").flexigrid
(
{
url: 'form_query_list_ajax.jsp?id=<%=id%>&mode=<%=mode%>&moduleId=<%=moduleId%>&moduleFormCode=<%=StrUtil.UrlEncode(moduleFormCode)%>&tagName=<%=StrUtil.UrlEncode(tagName)%>&openerFormCode=<%=openerFormCode%>&openerFieldName=<%=openerFieldName%>',
params:[
	{id:'<%=id%>'}
],
dataType: 'json',
colModel : colM,
buttons : [
<%if (!mode.equals("selField")) {%>
	<%if (!aqd.getChartPie().equals("")) {%>
		{name: '饼图', bclass: 'pie', onpress : action},
	<%}%>
	<%if (!aqd.getChartHistogram().equals("")) {%>
		{name: '柱状图', bclass: 'histogram', onpress : action},
	<%}%>
	<%if (!aqd.getChartLine().equals("")) {%>
		{name: '折线图', bclass: 'chartLine', onpress : action},
	<%}%>
	<%if (!aqd.getChartTb().equals("")) {%>
		{name: '同比', bclass: 'tb', onpress : action},
	<%}%>
	<%if (!mode.equals("moduleTag")) {%>	
		{name: '设计器', bclass: 'designQuery', onpress : action},
	<%}%>
		{name: '搜索', bclass: 'search', onpress : action},
	<%if (privilege.isUserPrivValid(request, "admin.flow.query")) {%>		
		{name: '重置列', bclass: 'resetCol', onpress : action},
	<%}%>
		{name: '导出', bclass: 'export', onpress : action},
<%}else{%>
	{name: '选择', bclass: 'pass', onpress : action},
<%}%>		
		{separator: true}
	],
/*
searchitems : [
	{display: 'ISO', name : 'iso'},
	{display: 'Name', name : 'name', isdefault: true}
	],
sortname: "iso",
sortorder: "asc",
*/
<%if (mode.equals("selField")) {%>
checkbox: true,
<%}%>
// rpOptions: [10,15,20,25,50],//可选择设定的每页结果数
usepager: true,
//title: '查询结果 -  <%=aqd.getQueryName()%>',
useRp: true,
rp: <%=pageSize%>,
singleSelect: true,
resizable: false,
showTableToggleBtn: true,
onRowDblclick: rowDbClick,
onColSwitch: colSwitch,
onColResize: colResize,
onToggleCol: toggleCol,
onChangeSort: changeSort,

preProcess: preProcess,

autoHeight:true,
width: document.documentElement.clientWidth,
height: document.documentElement.clientHeight - 113
}
);

var gridData;

function preProcess(data) {
	gridData = data;
	return data;
}

function changeSort(sortname, sortorder) {
	// 不允许按关联表排序
	if(sortname.indexOf("rel.")==0) {
		return;
	}
	if (!sortorder)
		sortorder = "desc";
	// alert(sortname + " " + sortorder);
	$("#queryTable").flexOptions({url : 'form_query_list_ajax.jsp?mode=<%=mode%>&id=<%=id%>&orderBy=' + sortname + '&sort=' + sortorder + '&openerFormCode=<%=openerFormCode%>&openerFieldName=<%=openerFieldName%>'});
	$("#queryTable").flexReload();
}

$.ajaxSetup({
  error: function(xhr, status, error) {
    jAlert("An AJAX error occured: " + status + "\nError: " + error,"提示");
  }
});

function action(com,grid) {
	if (com=='饼图') {
		addTab("饼图", "<%=request.getContextPath()%>/flow/form_query_chart_pie_show.jsp?id=<%=id%>");
	}
	else if (com=='柱状图') {
		addTab("柱状图", "<%=request.getContextPath()%>/flow/form_query_chart_histogram_show.jsp?id=<%=id%>");
	}
	else if (com=='折线图') {
		addTab("折线图", "<%=request.getContextPath()%>/flow/form_query_chart_line_show.jsp?id=<%=id%>");
	}
	else if (com=='同比') {
		addTab("同比图", "<%=request.getContextPath()%>/flow/form_query_chart_tb_show.jsp?id=<%=id%>");
	}
	else if (com=='重置列') {
		jConfirm("您确定要重置列么？","提示",function(r){
			if(!r){return;}
			else{
				window.location.href = "form_query_list_do.jsp?id=<%=id%>&op=resetCol";
			}
		})
	}
	else if (com=='导出') {
		window.open("form_query_result_export_to_excel.jsp?id=<%=id%>");
	}
	else if (com=='设计器') {
		window.location.href = "<%=request.getContextPath()%>/flow/designer/designer.jsp?id=<%=id%>&op=resetCol";
	}
	else if (com=='选择') {
		selField();
	}	
	else if (com=="搜索") {
		<%
			FormQueryConditionDb formQueryConditionDb = new FormQueryConditionDb();
			List<String> list = formQueryConditionDb.listCondFieldByQueryId(id);
			String fieldsSelected = StringUtils.join(list.toArray(), ",");
		%>
		$.get(
				"designer/ajax_condition_value.jsp",
				{id:<%=id%>, fieldsSelected:"<%=fieldsSelected%>", formCode:"<%=aqd.getTableCode()%>", "mode":"<%=mode%>", moduleFormCode:"<%=StrUtil.UrlEncode(moduleFormCode)%>", "tagName":"<%=tagName%>"},
				function(data) {
					$("#dlg").html(data);
					$('#dlg').find('input').each(function() {
						if (typeof($(this).attr('kind')) != 'undefined' && $(this).attr('kind') == 'date') {
							$(this).datetimepicker({
				            	lang:'ch',
				            	timepicker:false,
				            	format:'Y-m-d'
				          });
						}
					});
					var options = {
						success:   showSaveCondResponse,  // post-submit callback 
						dataType:  'json'        // 'xml', 'script', or 'json' (expected server response type) 
					};
					$('#formConditionFieldCode').submit(function() {
						$(this).ajaxSubmit(options);
						return false;
					});
										
					$("#dlg").dialog({
						title: "条件",
						modal: true,
						// bgiframe:true,
						buttons: {
							"取消": function() {
								$(this).dialog("close");
							},
							"确定": function() {
								$('#formConditionFieldCode').submit();
								$(this).dialog("close");
							}
						},
						closeOnEscape: true,
						draggable: true,
						resizable:true,
						width:550,
						height:250
						});
				}
			 );

	}
}

function showSaveCondResponse(data)  {
	if (data.re=="true") {
		// alert("操作成功！");
		$("#queryTable").flexReload(); 
	}
	else {
		jAlert("保存条件失败！","提示");
	}
}

var map = new Map();

function doGetMapIDS() {
	map = new Map();
	// $(".cth input[type='checkbox'][value!='on']:checked", queryTable.bDiv).each(function(i) {
	// jquery1.8.3得按以下写法
	$(".cth input[type='checkbox'][value!='on']:checked").each(function(i) {
		
		// console.log(JSON.stringify(gridData.rows[$(this).closest("tr").index()]));
		var id = $(this).val().substring(3); // 去掉前面的row
		if (!map.containsKey(id)) {
			map.put(id, gridData.rows[$(this).closest("tr").index()]);			
		}
	});
}

function selField() {	
	doGetMapIDS();
	
	var ids = "";
	if (map.size()==0) {
		jAlert("请选择记录！","提示");
		return;
	}
	
	if (map.size()>1) {
		jAlert("只能选择一条记录！","提示");
		return;
	}
	
	jConfirm("您确定要选择么？","提示",function(r){
		if(!r){return;}
		else{
			var id = map.elements[0].key;
			var json = map.get(id).value;
			
			// 如果json中的值为空字符串，则取出来的为null
			console.log(JSON.stringify(json));
			
			
			var idFieldValue = "";
			if (idField=="<%=FormSQLBuilder.PRIMARY_KEY_ID%>") {
				idFieldValue = id;
			}
			else {
				idFieldValue = eval("json." + idField);
				if (!idFieldValue)
					idFieldValue = "";
			}
			
			var showFieldValue = eval("json." + showField);
			if (!showFieldValue)
				showFieldValue = "";
		
			var funs = "";
			
			for (var i=0; i<mapAry.length; i++) {
				var jsonObj = mapAry[i];
				var destF = jsonObj.destField;
				var sourceF = jsonObj.sourceField;
				
				var sourceFVal = eval("json." + sourceF);
				if (!sourceFVal)
					sourceFVal = "";
					
				setOpenerFieldValue(destF, sourceFVal);
			}
			// alert("idFieldValue=" + idFieldValue + " showFieldValue=" + showFieldValue);
			window.opener.setIntpuObjValue(idFieldValue, showFieldValue);
		
			window.close();
		}
	})
}

function setOpenerFieldValue(openerField, val) {
	window.opener.o(openerField).value = val;
}
</script>
</body>
</html>