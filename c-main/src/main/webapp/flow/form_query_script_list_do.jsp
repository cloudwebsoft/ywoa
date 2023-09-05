<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.sql.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@ page import = "bsh.EvalError"%>
<%@ page import = "bsh.Interpreter"%>
<%@ page import = "net.sf.jsqlparser.statement.select.*"%>
<%@ page import = "net.sf.jsqlparser.parser.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String op = ParamUtil.get(request, "op");

	String nestFormCode = ParamUtil.get(request, "nestFormCode");
	String nestType = ParamUtil.get(request, "nestType");
	String parentFormCode = ParamUtil.get(request, "parentFormCode");
	String nestFieldName = ParamUtil.get(request, "nestFieldName");
	String parentId = ParamUtil.get(request, "parentId", "-1");
	
	int flowId = ParamUtil.getInt(request, "flowId", com.redmoon.oa.visual.FormDAO.NONEFLOWID);
	
	String action = ParamUtil.get(request, "action"); // action的值可能为：searchInResult，表示在结果中搜索
	String mode = ParamUtil.get(request, "mode");

	// 用于查询字段选择宏控件
	String openerFormCode = ParamUtil.get(request, "openerFormCode");
	String openerFieldName = ParamUtil.get(request, "openerFieldName");

	FormQueryDb aqd = new FormQueryDb();
	int id = ParamUtil.getInt(request, "id", -1);
	if (id==-1 || id==0) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " id=" + id));
		return;
	}

	QueryScriptUtil qsu = new QueryScriptUtil();
	
	if (op.equals("resetCol")) {
		aqd = aqd.getFormQueryDb(id);
		String colProps = qsu.getColProps(request, aqd);
		aqd.setColProps(colProps);
		boolean re = aqd.save();
		// out.print(StrUtil.Alert_Redirect("操作成功", "form_query_script_list_do.jsp?id=" + id));
		JSONObject json = new JSONObject();
		if (re) {
			json.put("ret", 1);
			String str = LocalUtil.LoadString(request,"res.common","info_op_success");
			json.put("msg", str);
		}
		else {
			json.put("ret", 0);
			String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			json.put("msg", str);
		}
		out.print(json);
		return;
	}

	JSONArray mapsNest = new JSONArray();
	JSONArray mapsCond = new JSONArray();
	
	// 嵌套表拉单
	if (mode.equals("sel")) {
		FormDb pForm = new FormDb();
		pForm = pForm.getFormDb(parentFormCode);
		FormField nestField = pForm.getFormField(nestFieldName);
		
		JSONObject json = null;
		try {
			// 拼装父窗口条件字段以作为参数传给form_query_script_list_ajax.jsp，从父窗口中得到条件字段的值
			String desc = StrUtil.decodeJSON(nestField.getDescription());
			json = new JSONObject(desc);
			nestFormCode = json.getString("destForm");
			mapsCond = (JSONArray)json.get("mapsCond");
			mapsNest = (JSONArray)json.get("mapsNest");
		} catch (JSONException e) {
			e.printStackTrace();
			String str = LocalUtil.LoadString(request,"res.flow.Flow","failedParse");
			out.print(SkinUtil.makeErrMsg(request, str));
			return;
		}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>查询结果</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../inc/map.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../js/json2.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery.form.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script src="designer/condition_value.js"></script>
<%
if (op.equals("selBatch")) {
		FormDb nestFd = new FormDb();
		nestFd = nestFd.getFormDb(nestFormCode);
		
		com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
		
		String jsonArrayStr = ParamUtil.get(request, "jsonArray"); // 获取被选的数据集
		JSONArray jsonAry = null;
		try {
			jsonAry = new JSONArray(jsonArrayStr);
		} catch (JSONException ex) {
			ex.printStackTrace();
			String str = LocalUtil.LoadString(request,"res.flow.Flow","formatError");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}		
		
		String ids = ParamUtil.get(request, "ids");
		String[] ary = StrUtil.split(ids, ",");
		if (jsonAry!=null) {				
			  // 遍历结果集
			  for (int i=0; i<jsonAry.length(); i++) {
				  JSONObject json = jsonAry.getJSONObject(i);
				  json = json.getJSONObject("value");
				  
				  // 根据映射关系赋值
				  JSONObject jsonObj2 = new JSONObject();				  
				  for (int k=0; k<mapsNest.length(); k++) {
					  JSONObject jsonObj = null;
					  try {
						  jsonObj = mapsNest.getJSONObject(k);

						  String sfield = (String) jsonObj.get("sourceField");
						  String dfield = (String) jsonObj.get("destField");
						  
						  jsonObj2.put(dfield, (String)json.get(sfield.toUpperCase()));

						  fdaoNest.setFieldValue(dfield, (String)json.get(sfield.toUpperCase()));
					  } catch (JSONException ex) {
						  ex.printStackTrace();
					  }
				  }

				  if (nestType.equals("detaillist")) {
					  JSONArray jsonAry2 = new JSONArray();
					  jsonAry2.put(jsonObj2);
					  %>
					  <script>
					  // 如果有父窗口
					  if (window.opener) {
						try {
						  window.opener.insertRow("<%=nestFormCode%>", <%=jsonAry2%>, "<%=nestFieldName%>");
						}
						catch (e) {
						}
					  }
					  </script>						
					  <%
					  continue;
				  }
			
				  fdaoNest.setFlowId(flowId);			
				  fdaoNest.setCwsId(parentId);
				  fdaoNest.setCreator(privilege.getUser(request));
				  fdaoNest.setUnitCode(privilege.getUserUnitCode(request));
				  boolean re = fdaoNest.create();
				  if (re) {
					  long fdaoId = fdaoNest.getId();
					  // 如果是嵌套表格2
					  if (nestType.equals("nest_sheet")) {
						  ModuleSetupDb msd = new ModuleSetupDb();
						  msd = msd.getModuleSetupDbOrInit(nestFormCode);
						  String[] fields = msd.getColAry(false, "list_field");
						  
						  int len = 0;
						  if (fields!=null)
							  len = fields.length;
						  
						  String tds = "";
						  String token = "#@#";
						  for (int n=0; n<len; n++) {
							  String fieldName = fields[n];
							  String v = StrUtil.getNullStr(fdaoNest.getFieldHtml(request, fieldName)); // fdao.getFieldValue(fieldName);

							  if (n==0) {
								  tds = v;
							  } else {
								  tds += token + v;
							  }
						  }
						  
				  
						  if (nestType.equals("nest_sheet")) {					
						  %>
							  <script>
							  // 如果有父窗口
							  if (window.opener) {
								try {
								  <%if (parentId==com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) {%>
								  window.opener.addTempCwsId("<%=nestFormCode%>", <%=fdaoId%>);
								  <%}%>
								  // alert(typeof(window.opener.insertRow_<%=nestFormCode%>));
								  window.opener.insertRow_<%=nestFormCode%>("<%=nestFormCode%>", <%=fdaoId%>, "<%=tds%>", "<%=token%>");
								}
								catch (e) {
								}
							  }
							  </script>				
						  <%
						  }
					  }
				  }
			  }
		  }
		  
		  if (nestType.equals("nest_sheet") || nestType.equals("detaillist")) {
		  %>
		  <script>
		  // 计算控件合计
		  window.opener.callCalculateOnload();		  
		  window.close();
		  </script>
		  <%
		  }
		  else {
		  %>
		  <script>
		  window.opener.location.reload();
		  window.close();
		  </script>
		  <%
		  }
		  
		  return;
	}
		
	if (!mode.equals("moduleTag") && !mode.equals("selField")) {
		if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
			FormQueryPrivilegeMgr aqpm = new FormQueryPrivilegeMgr();
			if (!aqpm.canUserQuery(request, id)) {
				out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
				String str = LocalUtil.LoadString(request,"res.flow.Flow","notAuthorizedQuery");
				out.print(SkinUtil.makeErrMsg(request, str));
				return;
			}
		}
	}
// 查询字段选择控件
if (mode.equals("selField")) {
		
		FormDb openerFd = new FormDb();
		openerFd = openerFd.getFormDb(openerFormCode);
		if (!openerFd.isLoaded()) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","formNotExist");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}
		
		FormField ff = openerFd.getFormField(openerFieldName);
		String strDesc = ff.getDescription();
		JSONObject json = null;
		
		try {
			json = new JSONObject(strDesc);
			// formCode = json.getString("formCode");
			try {
				id = StrUtil.toInt(json.getString("queryId"));
			}
			catch (JSONException e) {
				id = json.getInt("queryId");
			}
			// 解码，替换%sq %dq，即单引号、双引号
			// String filter = StrUtil.decodeJSON(json.getString("filter"));
			mapsCond = (JSONArray)json.get("mapsCond");
			
			JSONArray mapAry = (JSONArray)json.get("maps");
			
			String idField = (String)json.get("idField");
			String showField = (String)json.get("showField");
			// IE8下，脚本不能写在<html>前，否则页面会显示为空白
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
			String str = LocalUtil.LoadString(request,"res.flow.Flow","illegalFormat");
			out.print(StrUtil.jAlert_Back(str,"提示"));
			return;
		}			
	}
	
	if (id==-1) {
		// out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
		// out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "id不能为空！"));
		String str = LocalUtil.LoadString(request,"res.flow.Flow","idNotEmpty");
		out.print(StrUtil.jAlert_Back(str,"提示"));
		return;
	}			
	aqd = aqd.getFormQueryDb(id);
%>

<script>
var condStr = "";
<%
for (int i=0; i<mapsCond.length(); i++) {
	JSONObject j = null;
	try {
		j = mapsCond.getJSONObject(i);
		String parentWinField = (String) j.get("destField");
		%>
		if (condStr=="")
			condStr = "<%=parentWinField%>=" + encodeURIComponent(window.opener.o("<%=parentWinField%>").value);
		else
			condStr += "&<%=parentWinField%>=" + encodeURIComponent(window.opener.o("<%=parentWinField%>").value);
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

FormQueryDb fqd = new FormQueryDb();
fqd = fqd.getFormQueryDb(id);
%>

<table id="queryTable" style="display:none"></table>
<div id="dlg" style="display:none"></div>

<form name="hidForm" action="" method="post" style="display:none">
<input name="mode" type="hidden" value="<%=mode%>" />
<input name="op" type="hidden" />
<input name="ids" type="hidden" />
<input type="hidden" name="nestType" value="<%=nestType%>" />
<input type="hidden" name="parentFormCode" value="<%=parentFormCode%>" />
<input type="hidden" name="nestFieldName" value="<%=nestFieldName%>" />
<input type="hidden" name="parentId" value="<%=parentId%>" />
<input type="hidden" name="jsonArray" />
<input type="hidden" name="nestFormCode" value="<%=nestFormCode%>" />
<input type="hidden" name="id" value="<%=id%>" />
<input type="hidden" name="flowId" value="<%=flowId %>" />
</form>

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
	String colProps = qsu.getColProps(request, fqd);
	if (qsu.getMapIndex()==null) {
		// 也可能是没有记录
		// out.print(SkinUtil.makeErrMsg(request, "脚本运行错误！"));
		// return;
	}
	
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
		
		// 如果action=sel，则表头中第一列为checkbox
		if (fieldName=="")
			return;
		
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
	   data : {
			op: "modifyColProps",
			colProps: str,
			id: "<%=id%>"
	   },
	   // data: "op=modifyColProps&colProps=" + str + "&id=<%=id%>",
	   // contentType: "application/x-www-form-urlencoded; charset=utf-8",
	   // contentType: "application/x-www-form-urlencoded; charset=ISO8859_1",
	   success: function(html){
			var json = jQuery.parseJSON(html);
			if (json.re==true) {
				// alert("保存成功！");
			}
			else {
				jAlert('<lt:Label res="res.flow.Flow" key="columnAdjustmentFailed"/>','提示');
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
	window.location.href = "form_query_script_list_do.jsp?id=<%=id%>";
}

<%
// 当mode为moduleTag时，将会有moduleId参数传进来
int moduleId = ParamUtil.getInt(request, "moduleId", -1);
String moduleFormCode = ParamUtil.get(request, "moduleCode");
String tagName = ParamUtil.get(request, "tagName");
%>
var flex = $("#queryTable").flexigrid
(
{
<%if (mode.equals("moduleTag")) {%>
url: '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&mode=<%=mode%>&moduleId=<%=moduleId%>&moduleFormCode=<%=StrUtil.UrlEncode(moduleFormCode)%>&tagName=<%=StrUtil.UrlEncode(tagName)%>',
<%}else if (mode.equals("changeCondValue")) {
	String param = "";
	Enumeration paramNames = request.getParameterNames();
	while (paramNames.hasMoreElements()) {
		String paramName = (String) paramNames.nextElement();
		String[] paramValues = ParamUtil.getParameters(request, paramName);
		if (paramValues.length == 1) {
			String paramValue = paramValues[0];
			// 过滤掉id、mode
			if (paramName.equals("id") || paramName.equals("mode"))
				;
			else
				param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
		}
	}	
%>
url: '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&mode=<%=mode%><%=param%>&nestFormCode=<%=StrUtil.UrlEncode(nestFormCode)%>&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&' + condStr,
<%
}
else if (mode.equals("sel") || mode.equals("selField")) {
	// 接收当接单在结果中搜索时传入的参数
	String param = "";
	// 20170927 fgf 为将{request.param}参数带入form_query_script_list_ajax.jsp加入了true，但注意要过滤掉op
	if (true || action.equals("searchInResult")) {
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			String[] paramValues = ParamUtil.getParameters(request, paramName);
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				// String paramValue = ParamUtil.get(request, paramName); // 中文会出现乱码
				// 过滤掉id、mode等，只传在form_query_script_conds_ajax中传来的条件参数
				if (paramName.equals("op") || paramName.equals("id") || paramName.equals("mode") || 
					paramName.equals("nestFormCode") || paramName.equals("nestType") ||
					paramName.equals("parentFormCode") || paramName.equals("nestFieldName") || paramName.equals("parentId"))
					;
				else {
					param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
				}
			}
		}
	}
%>
	url: '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&parentId=<%=parentId%>&action=<%=action%>&mode=<%=mode%>&nestFormCode=<%=StrUtil.UrlEncode(nestFormCode)%>&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&openerFormCode=<%=openerFormCode%>&openerFieldName=<%=openerFieldName%><%=param%>&' + condStr,
<%
}
else if (mode.equals("filter")) {
	// 在查询结果中过滤
	// 接收当接单在结果中搜索时传入的参数
	String param = "";
	// if (action.equals("searchInResult")) {
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			String[] paramValues = ParamUtil.getParameters(request, paramName);
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				// String paramValue = ParamUtil.get(request, paramName); // 中文会出现乱码
				// 过滤掉id、mode等，只传在form_query_script_conds_ajax中传来的条件参数
				if (paramName.equals("id") || paramName.equals("mode") || 
					paramName.equals("nestFormCode") || paramName.equals("nestType") ||
					paramName.equals("parentFormCode") || paramName.equals("nestFieldName") || paramName.equals("parentId"))
					{
					;
					}else {
					param += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
				}
			}
		}
	// }	
	%>
	url: '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&action=<%=action%>&mode=<%=mode%>&nestFormCode=<%=StrUtil.UrlEncode(nestFormCode)%>&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&openerFormCode=<%=openerFormCode%>&openerFieldName=<%=openerFieldName%><%=param%>&' + condStr,	
	<%
} else {%>
	url: '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&action=<%=action%>&mode=<%=mode%>&nestFormCode=<%=StrUtil.UrlEncode(nestFormCode)%>&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&openerFormCode=<%=openerFormCode%>&openerFieldName=<%=openerFieldName%>&' + condStr,
<%}%>
params:[
	{id:'<%=id%>'}
],
dataType: 'json',
colModel : colM,
buttons : [
<%
// sel表示用于嵌套表格2拉单，selField表示查询字段选择窗体宏控件
if (!mode.equals("sel") && !mode.equals("selField")) {%>
	<%if (!mode.equals("moduleTag")) {%>
		<%if (privilege.isUserPrivValid(request, "admin.flow.query")) {%>
			{name: '<lt:Label res="res.flow.Flow" key="designer"/>', bclass: 'designQuery', onpress : action},
			{name: '<lt:Label res="res.flow.Flow" key="condition"/>', bclass: 'search', onpress : action},
			{name: '<lt:Label res="res.flow.Flow" key="search"/>', bclass: 'search', onpress : action},
		<%}%>
	<%}%>
	{name: '<lt:Label res="res.flow.Flow" key="export"/>', bclass: 'export', onpress : action},
<%} else {%>
	{name: '<lt:Label res="res.flow.Flow" key="choose"/>', bclass: 'pass', onpress : action},
<%}%>
<%if (privilege.isUserPrivValid(request, "admin.flow.query")) {%>
	{name: '<lt:Label res="res.flow.Flow" key="resetColumn"/>', bclass: 'resetCol', onpress : action},
<%}%>
<%if (mode.equals("sel") || mode.equals("selField")) {%>
	{name: '<lt:Label res="res.flow.Flow" key="search"/>', bclass: 'search', onpress : action},	
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

<%if (mode.equals("sel") || mode.equals("selField")) {%>
checkbox: true,
<%}%>
usepager: true,
//title: '查询结果 -  <%=aqd.getQueryName()%>',
useRp: true,
rp: 20,
singleSelect: true,
resizable: false,
showTableToggleBtn: true,
onRowDblclick: rowDbClick,
onColSwitch: colSwitch,
onColResize: colResize,
onToggleCol: toggleCol,
onChangeSort: changeSort,
onChangePage: changePage,

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

// var mapOfNest = new Map();
<%
/*
for (int k=0; k<mapsNest.length(); k++) {
	JSONObject jsonObj = null;
	try {
		jsonObj = mapsNest.getJSONObject(k);
		String sfield = (String) jsonObj.get("sourceField");
		String dfield = (String) jsonObj.get("destField");
		%>
		mapOfNest.put(sfield, dfield);
		<%
	} catch (JSONException ex) {
		ex.printStackTrace();
	}
}
*/
%>

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

function changePage(newp) {
	doGetMapIDS();
	flex.flexReload();
}

var orderBy = "";
var sort = "";

function changeSort(sortname, sortorder) {
	orderBy = sortname;
	sort = sortorder;
	
	// 不允许按关联表排序
	if(sortname.indexOf("rel.")==0) {
		return;
	}
	if (!sortorder)
		sortorder = "desc";
	// alert(sortname + " " + sortorder);
	
<%if (mode.equals("moduleTag")) {%>
	$("#queryTable").flexOptions({url : '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&mode=<%=mode%>&moduleId=<%=moduleId%>&moduleFormCode=<%=StrUtil.UrlEncode(moduleFormCode)%>&tagName=<%=StrUtil.UrlEncode(tagName)%>&orderBy=' + sortname + '&sort=' + sortorder});
<%}else{%>
	$("#queryTable").flexOptions({url : '<%=request.getContextPath()%>/flow/form_query_script_list_ajax.jsp?id=<%=id%>&mode=<%=mode%>&nestFormCode=<%=StrUtil.UrlEncode(nestFormCode)%>&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&' + condStr + '&openerFormCode=<%=openerFormCode%>&openerFieldName=<%=openerFieldName%>&orderBy=' + sortname + '&sort=' + sortorder});
<%}%>
	
	$("#queryTable").flexReload();
}

$.ajaxSetup({
  error: function(xhr, status, error) {
    jAlert("An AJAX error occured: " + status + "\nError: " + error,"提示");
  }
});

function action(com,grid) {
	if (com=='<lt:Label res="res.flow.Flow" key="resetColumn"/>') {
		jConfirm('<lt:Label res="res.flow.Flow" key="isResetColumn"/>','提示',function(r){
			if(!r){return;}
			else{
				$.ajax({
					type: "post",
					url: "<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp",
					data : {
						id: "<%=id%>",
						op: "resetCol",
						mode: "<%=mode%>"
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						// $('#bodyBox').showLoading();				
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="1") {
							isDocDistributed = true;
							// flex.flexReload();
							window.location.reload();
						}
						else {
							jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>','提示');
						}					
					},
					complete: function(XMLHttpRequest, status){
						// $('#bodyBox').hideLoading();				
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						jAlert(XMLHttpRequest.responseText,'提示');
					}
				});
			}
		})
	}
	else if (com=='<lt:Label res="res.flow.Flow" key="export"/>') {
<%if (mode.equals("moduleTag")) {%>
		window.open("<%=request.getContextPath()%>/flow/form_query_script_result_export_to_excel.jsp?id=<%=id%>&mode=<%=mode%>&moduleId=<%=moduleId%>&moduleFormCode=<%=StrUtil.UrlEncode(moduleFormCode)%>&tagName=<%=StrUtil.UrlEncode(tagName)%>&orderBy" + orderBy + "&sort=" + sort);
<%}else{%>
		window.open('<%=request.getContextPath()%>/flow/form_query_script_result_export_to_excel.jsp?id=<%=id%>&mode=<%=mode%>&nestFormCode=<%=StrUtil.UrlEncode(nestFormCode)%>&parentFormCode=<%=StrUtil.UrlEncode(parentFormCode)%>&nestFieldName=<%=StrUtil.UrlEncode(nestFieldName)%>&' + condStr + '&orderBy=' + orderBy + '&sort=' + sort);
<%}%>
	}
	else if (com=='<lt:Label res="res.flow.Flow" key="designer"/>') {
		addTab("<%=aqd.getQueryName()%>", "<%=request.getContextPath()%>/flow/form_query_script.jsp?id=<%=id%>");
	}
	else if (com=='<lt:Label res="res.flow.Flow" key="choose"/>') {
		<%if ("selField".equals(mode)) {%>
		selField();
		<%}else{%>
		selBatch();
		<%}%>
	}
	else if (com=='<lt:Label res="res.flow.Flow" key="condition"/>') {
		$.ajax({
			type: "post",
			url: "form_query_script_conds_ajax.jsp",
			data : {
				id: "<%=id%>",
				formCode: "<%=parentFormCode%>"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				// $('#bodyBox').showLoading();				
			},
			success: function(data, status){
				$("#dlg").html(data.trim());
				$('#dlg').find('input').each(function() {
					if (typeof($(this).attr('kind')) != 'undefined') {
						if ($(this).attr('kind') == 'date') {
							$(this).datetimepicker({
				            	lang:'ch',
				            	timepicker:false,
				            	format:'Y-m-d'
							});
						} else if ($(this).attr('kind') == 'datetime') {
							$(this).datetimepicker({
				            	lang:'ch',
				            	format:'Y-m-d H:i:00',
					            step:10
							});
						}
					}
				});
				$("#dlg").dialog({
					title: '<lt:Label res="res.flow.Flow" key="condition"/>',
					modal: true,
					// bgiframe:true,
					buttons: {
						'<lt:Label res="res.flow.Flow" key="cancel"/>': function() {
							$(this).dialog("close");
						},
						'<lt:Label res="res.flow.Flow" key="sure"/>': function() {
							$('#formConds').submit();
							$(this).dialog("close");
						}
					},
					closeOnEscape: true,
					draggable: true,
					resizable:true,
					width:550,
					height:400
					});
			},
			complete: function(XMLHttpRequest, status){
				// $('#bodyBox').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});

	}	
	else if (com=='<lt:Label res="res.flow.Flow" key="search"/>') {
		$.ajax({
			type: "post",
			url: "form_query_script_conds_ajax.jsp",
			data : {
				<%if (mode.equals("sel")) {%>
				action: "<%=mode%>",
				parentFormCode:"<%=parentFormCode%>",
				nestFormCode:"<%=nestFormCode%>",
				nestFieldName:"<%=nestFieldName%>",
				nestType:"<%=nestType%>",
				parentId:"<%=parentId%>",
				id: "<%=id%>"
				<%}else if (mode.equals("selField")) {%>
				action: "<%=mode%>",
				openerFormCode: "<%=openerFormCode%>",
				openerFieldName: "<%=openerFieldName%>",
				id: "<%=id%>"
				<%}else{%>
				action: "filter",
				id: "<%=id%>"
				<%}%>
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				// $('#bodyBox').showLoading();				
			},
			success: function(data, status){
				$("#dlg").html(data.trim());
				$('#dlg').find('input').each(function() {
					if (typeof($(this).attr('kind')) != 'undefined' && $(this).attr('kind') == 'date') {
						$(this).datetimepicker({
			            	lang:'ch',
			            	timepicker:false,
			            	format:'Y-m-d'
			          });
					}
				});
				$("#dlg").dialog({
					title: '<lt:Label res="res.flow.Flow" key="condition"/>',
					modal: true,
					// bgiframe:true,
					buttons: {
						'<lt:Label res="res.flow.Flow" key="cancel"/>': function() {
							$(this).dialog("close");
						},
						'<lt:Label res="res.flow.Flow" key="sure"/>': function() {
							$('#formConds').submit();
							$(this).dialog("close");
						}
					},
					closeOnEscape: true,
					draggable: true,
					resizable:true,
					width:550,
					height:400
					});
			},
			complete: function(XMLHttpRequest, status){
				// $('#bodyBox').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});

	}	
}

function showSaveCondResponse(data)  {
	if (data.re=="true") {
		// alert("操作成功！");
		$("#queryTable").flexReload(); 
	}
	else {
		jAlert('<lt:Label res="res.flow.Flow" key="failCondition"/>','提示');
	}
}

function selBatch() {
	doGetMapIDS();
	
	var ids = "";
	if (map.size()==0) {
		jAlert('<lt:Label res="res.flow.Flow" key="selectRecord"/>','提示');
		return;
	}
	
	jConfirm('<lt:Label res="res.flow.Flow" key="selectIt"/>','提示',function(r){
		if(!r){return;}
		else{
			var jsonArray = "";
			for (i = 0; i < map.elements.length; i++) {
				if (ids=="") {
					ids = map.elements[i].key;
					jsonArray = JSON.stringify(map.get(map.elements[i].key));
				}
				else {
					ids += "," + map.elements[i].key;
					jsonArray += "," + JSON.stringify(map.get(map.elements[i].key));
				}
			}
			
			jsonArray = "[" + jsonArray + "]"; // 格式为 [{"key:"", "value":{"ID:"765","MYTITLE":"some"}},...]
			
			// alert(jsonArray);
			
			hidForm.action = "form_query_script_list_do.jsp";
			hidForm.op.value = "selBatch";
			hidForm.ids.value = ids;
			hidForm.jsonArray.value = jsonArray;
			hidForm.submit();
		}
	})
}

function selField() {	
	doGetMapIDS();
	
	var ids = "";
	if (map.size()==0) {
		jAlert('<lt:Label res="res.flow.Flow" key="selectRecord"/>','提示');
		return;
	}
	
	if (map.size()>1) {
		jAlert('<lt:Label res="res.flow.Flow" key="onlySelect"/>','提示');
		return;
	}
	
	jConfirm('<lt:Label res="res.flow.Flow" key="selectIt"/>','提示',function(r){
		if(!r){return;}
		else{
			var id = map.elements[0].key;
			var json = map.get(id).value;
			
			// 如果json中的值为空字符串，则取出来的为null
			// console.log(JSON.stringify(json));
			
			var idFieldValue = eval("json." + idField);
			if (!idFieldValue)
				idFieldValue = "";
			
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