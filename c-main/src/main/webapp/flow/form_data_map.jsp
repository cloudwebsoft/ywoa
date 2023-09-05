<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>表单映射</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
    <script src="../inc/common.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>
    <script src="../js/select2/select2.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	// 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String formCode = ParamUtil.get(request, "formCode");
	String sourceFormCode = "";
	String destFormCode = formCode;

	// 操作列链接 发起流程
	boolean isOpLinkFlow = false; // 是否为操作列链接
	String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
	if (!flowTypeCode.equals("")) {
		sourceFormCode = formCode;
		Leaf lf = new Leaf();
		lf = lf.getLeaf(flowTypeCode);
		if (lf != null) {
			destFormCode = lf.getFormCode();
			isOpLinkFlow = true;
		}
	}
%>
<table width="100%" align="center" cellPadding="0" cellSpacing="0" class="tabStyle_1" id="mapTable" style="padding:0px; margin:0px;">
  <tbody>
    <tr>
      <td height="28" colspan="4" class="tabStyle_1_title">表单数据映射</td>
    </tr>
    <tr>
      <td height="42" align="center">
		源表单
	  </td>
      <td height="42" colspan="3">
<%
FormDb fd = new FormDb();
String sql = "select code from " + fd.getTableName() + " where unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by code asc";	
Vector v = fd.list(sql);
Iterator ir = v.iterator();
String opts = "";
while (ir.hasNext()) {
	fd = (FormDb)ir.next();
	opts += "<option value='" + fd.getCode() + "'>" + fd.getName() + "</option>";
}
%>
          <select id="forms" name="forms">
              <option value="">请选择</option>
              <%=opts%>
          </select>
<script>
	var sources = [];
	var sourcesNest = [];
	var destFormNest = "";
	var sourceFormNest = "";

	// 同步执行，以免在onload初始化数据时，因sources未取到，而无法取出源字段的名称
	$.ajaxSettings.async = false;

	function onFormsChange() {
		$.getJSON('form_data_map_ajax.jsp', {"sourceFormCode":$("#forms").val()}, function(data) {
			sources = data.result;
			var opts = "";
			$.each(data.result, function(index,data){
				opts += "<option value='" + data.id + "'>" + data.name + "</option>";
			});
			$('#sourceField').html("<option value=''>无</option>" + opts);
			$('#sourceField').select2();
		});

		// 取源表单中的嵌套表格的字段
		$.getJSON('form_data_map_ajax.jsp', {"sourceFormCode":$("#forms").val(), "op":"getNestTableFields"}, function(data) {
			sourcesNest = data.result;
			sourceFormNest = data.formNest;
			var opts = "";
			$.each(data.result, function(index,data){
				opts += "<option value='" + data.id + "'>" + data.name + "</option>";
			});
			$('#sourceFieldNest').html("<option value=''>无</option>" + opts);
			$('#sourceFieldNest').select2();
		});
	}

	$(function() {
		$('#forms').select2();
		$('#forms').val(['<%=sourceFormCode%>']).trigger("change");
		onFormsChange();

		<%if (isOpLinkFlow) {
		%>
			$("#forms").prop("disabled", true);
		<%
		}%>

		$('#forms').change(function() {
			onFormsChange();
		})
	})
</script>
      </td>
    </tr>
    <tr>
      <td width="12%" align="center">
      映射</td>
      <td width="25%" id="sourceTd">
      <select id="sourceField">
	  </select>
      </td>
      <td width="25%">
		  <select id="destField">
		  </select>
      <%
		  MacroCtlMgr mm = new MacroCtlMgr();

		  fd = fd.getFormDb(destFormCode);
		  v = fd.getFields();
		  ir = v.iterator();
		  String json = "";
		  opts = "";
		  FormField destFieldNest = null;
		  while (ir.hasNext()) {
			  FormField ff = (FormField) ir.next();

			  if (ff.getType().equals(FormField.TYPE_MACRO)) {
				  MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				  if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
					  destFieldNest = ff;
				  }
			  }

			  if (json.equals(""))
				  json = "{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue() + "\"}";
			  else
				  json += ",{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue() + "\"}";

			  opts += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
		  }
	  %>
		  <script>
			  var dests = [<%=json%>];
			  $('#destField').html("<option value=''>无</option><%=opts%>");
			  $('#destField').select2();
		  </script>
      </td>
      <td width="38%" align="center">
      <input type="button" class="btn" value="添加" onclick="addMap()" />&nbsp;&nbsp;
      <input type="button" class="btn" value="全部添加" onclick="addAllMap()" title="映射字段名相同的字段"/>
      </td>
    </tr>
  </tbody>
</table>
<%
if (destFieldNest!=null) {
	// 取出目标表单中的嵌套表的字段
	String destFormCodeNest = destFieldNest.getDescription();
	try {
		if ("".equals(destFormCodeNest)) {
			destFormCodeNest = destFieldNest.getDefaultValueRaw();
		}
		destFormCodeNest = StrUtil.decodeJSON(destFormCodeNest);
		JSONObject jsonDefault = new JSONObject(destFormCodeNest);
		destFormCodeNest = jsonDefault.getString("destForm");
	} catch (JSONException e) {
		e.printStackTrace();
	}

	FormDb destFdNest = new FormDb();
	destFdNest = destFdNest.getFormDb(destFormCodeNest);
	%>
	<script>
	destFormNest = "<%=destFormCodeNest%>";
	</script>
	<%
	v = destFdNest.getFields();
	ir = v.iterator();
	json = "";
	opts = "";
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		if (json.equals(""))
			json = "{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue() + "\"}";
		else
			json += ",{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue() + "\"}";
		opts += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
	}
%>
<table id="mapTableNest" class="tabStyle_1" width="100%" border="0" align="center" cellspacing="0">
  <tr>
    <td colspan="4" class="tabStyle_1_title">嵌套表映射</td>
  </tr>
  <tr>
  <td width="12%" align="center">映射</td>
    <td width="25%" id="sourceFieldNestTd">
		<select id="sourceFieldNest"></select>
	</td>
    <td width="25%">
    <select id="destFieldNest"></select>
	<script>
		var destsNest = [<%=json%>];
		$(function() {
			$('#destFieldNest').html("<option value=''>无</option>" + "<%=opts%>");
			$('#destFieldNest').select2();
		})
    </script>    
    </td>
    <td width="38%" align="center"><input type="button" class="btn" value="添加" onclick="addMapNest()" /></td>
</tr>
</table>
<%}%>
<div style="text-align:center; margin-top:5px;"><input type="button" class="btn" value="确定" onclick="makeMap()" /></div>
</body>
<script>
// 检查类型是否匹配
function isTypeMatched(sourceValue, destValue, sources, dests) {
	for (var one in sources) {
		if (sources[one].id==sourceValue) {
			for (var key in dests) {
				if (dests[key].id==destValue) {
					// alert(dests[key].type + " " + sources[one].type);
					var isCheck = false;
					// 检查日期型及宏控件
					if (dests[key].type=="DATE" || dests[key].type=="DATE_TIME" || sources[one].type=="DATE" || sources[one].type=="DATE_TIME")
						isCheck = true;
					if (dests[key].type=="<%=FormField.TYPE_MACRO%>" || sources[one].type=="<%=FormField.TYPE_MACRO%>")
						isCheck = true;
					if (isCheck) {
						if (dests[key].type=="<%=FormField.TYPE_MACRO%>" || sources[one].type=="<%=FormField.TYPE_MACRO%>") {
							if (dests[key].macroType=="nest_table" && sources[one].macroType=="nest_table") {
								if (dests[key].defaultValue==sources[one].defaultValue) {
									// alert(true);
									return true;
								}
							}

							if (dests[key].macroType!=sources[one].macroType && sources[one].name!="ID") {
								alert("宏控件 " + dests[key].name + " 与 " + sources[one].name + "的类型不一致");
								return false;
							}
						}
						else {
							if (dests[key].type==sources[one].type) {
								return true;
							}
							else {
								alert("字段 " + dests[key].name + " 与 " + sources[one].name + "的类型不一致");
								return false;
							}
						}
					}
					else
						break;
				}
			}
			break;
		}
	}
	return true;
}

// 添加字段名相同的映射
function addAllMap() {
	for (one in sources) {
		for (key in dests) {
			if (sources[one].id==dests[key].id) {
				var trId = "tr_" + sources[one].id + "_" + dests[key].id;
				// 检测trId是否已存在
				var isFound = false;
				$("#mapTable tr").each(function(k){
					if ($(this).attr("id")==trId) {
						isFound = true;
						return;
					}
				});
				
				if (isFound) {
					alert("存在重复映射！");
					return;
				}
				
				var isNestTable = false;
				if (sources[one].macroType=="nest_table" || sources[one].macroType=="macro_detaillist_ctl" || sources[one].macroType=="nest_sheet") {
					isNestTable = true;
				}
				
				var tr = "<tr id='" + trId + "' sourceField='" + sources[one].id + "' destField='" + sources[one].id + "'>";
				tr += "<td align='center'>字段</td>";
				tr += "<td align='center'>" + sources[one].id + "</td>";
				tr += "<td align='center'>" + sources[one].name + "</td>";
				tr += "<td align='center'>";
				tr += "<span style='display:<%=isOpLinkFlow?"none":""%>'><input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;</span>";
				// 如果是嵌套表，则appendable才有效
				if (isNestTable)
					tr += "<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;";
				tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
				tr += "</tr>";
				$("#mapTable tr:last").after(tr);
				break;
			}
		}
	}
}

function addMap() {
	if ($('#sourceField').val()=="" || $('#destField').val()=="") {
		alert("请选择表单域");
		return;
	}
	// 如果类型匹配
	if (isTypeMatched($('#sourceField').val(), $('#destField').val(), sources, dests)) {
		var trId = "tr_" + $('#sourceField').val() + "_" + $('#destField').val();
		// 检测trId是否已存在
		var isFound = false;
		$("#mapTable tr").each(function(k){
			if ($(this).attr("id")==trId) {
				isFound = true;
				return;
			}
		});
		
		if (isFound) {
			alert("存在重复映射！");
			return;
		}
		
		var isNestTable = false;
		for (var one in sources) {
			if (sources[one].id==$('#sourceField').val()) {
				if (sources[one].macroType=="nest_table" || sources[one].macroType=="macro_detaillist_ctl" || sources[one].macroType=="nest_sheet") {
					isNestTable = true;
					break;
				}
			}
		}
		
		var tr = "<tr id='" + trId + "' sourceField='" + $('#sourceField').val() + "' destField='" + $('#destField').val() + "'>";
		tr += "<td align='center'>字段</td>";
		tr += "<td align='center'>" + $('#sourceField').find("option:selected").text() + "</td>";
		tr += "<td align='center'>" + $('#destField').find("option:selected").text() + "</td>";
		tr += "<td align='center'>";
		tr += "<span style='display:<%=isOpLinkFlow?"none":""%>'><input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;</span>";
		// 如果是嵌套表，则appendable才有效
		if (isNestTable)
			tr += "<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;";
		tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
		tr += "</tr>";
		$("#mapTable tr:last").after(tr);

		$('#sourceField').val(['']).trigger("change");
		$('#destField').val(['']).trigger("change");
	}
	else
		; // alert("类型不匹配，无法映射！");

}

function addMapNest() {
	if ($('#sourceFieldNest').val()=="" || $('#destFieldNest').val()=="") {
		alert("请选择表单域！");
		return;
	}
	// 如果类型匹配
	if (isTypeMatched($('#sourceFieldNest').val(), $('#destFieldNest').val(), sourcesNest, destsNest)) {
		var trId = "tr_" + $('#sourceFieldNest').val() + "_" + $("#destFieldNest").val();
		// 检测trId是否已存在
		var isFound = false;
		$("#mapTableNest tr").each(function(k){
			if ($(this).attr("id")==trId) {
				isFound = true;
				return;
			}
		});
		
		if (isFound) {
			alert("存在重复映射！");
			return;
		}
		
		var isNestTable = false;
		for (var one in sourcesNest) {
			if (sourcesNest[one].id==$('#sourceFieldNest').val()) {
				if (sources[one].macroType=="nest_table" || sources[one].macroType=="macro_detaillist_ctl" || sources[one].macroType=="nest_sheet") {
					isNestTable = true;
					break;
				}
			}
		}
		
		var tr = "<tr id='" + trId + "' sourceField='" + $('#sourceFieldNest').val() + "' destField='" + $('#destFieldNest').val() + "'>";
		tr += "<td align='center'>字段</td>";
		tr += "<td align='center'>" + $("#sourceFieldNest").find("option:selected").text() + "</td>";
		tr += "<td align='center'>" + $("#destFieldNest").find("option:selected").text() + "</td>";
		tr += "<td align='center'>";
		tr += "<span style='display:<%=isOpLinkFlow?"none":""%>'><input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;</span>";
		// 如果是嵌套表，则appendable才有效
		if (isNestTable)
			tr += "<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;";
		tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
		tr += "</tr>";
		$("#mapTableNest tr:last").after(tr);

		$("#sourceFieldNest").val(['']).trigger("change");
		$("#destFieldNest").val(['']).trigger("change");
	}
	else
		; // alert("类型不匹配，无法映射！");

}

function makeMap() {
	// 组合成json字符串{sourceForm:..., destForm:..., maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
	var maps = "";
	$("#mapTable tr").each(function(k){
		// 判断是否为描述映射的行
		if ($(this)[0].id!="") {
			if ($(this).attr("id").indexOf("tr_")==0) {
				var editable = o($(this).attr("id") + "_editable").checked?"true":"false";
				var appendable = "false";
				if (o($(this).attr('id') + "_appendable")) {
					appendable = $("#" + $(this).attr("id") + "_appendable")[0].checked?"true":"false";
				}
				if (maps=="") {
					maps = "{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\", \"editable\":\"" + editable + "\", \"appendable\":\"" + appendable + "\"}";
				}
				else {
                    maps += ",{\"sourceField\": \"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\", \"editable\":\"" + editable + "\", \"appendable\":\"" + appendable + "\"}";
                }
			}
		}
	});
	
	var mapsNest = "";
	$("#mapTableNest tr").each(function(k){
		// 判断是否为描述映射的行
		if ($(this)[0].id!="") {
			if ($(this).attr("id").indexOf("tr_")==0) {
				// alert($(this).attr("id"));
				var editable = o($(this).attr("id") + "_editable").checked?"true":"false";
				var appendable = "false";
				if (o($(this).attr('id') + "_appendable")) {
					appendable = $("#" + $(this).attr("id") + "_appendable")[0].checked?"true":"false";
				}
				if (mapsNest=="") {
					mapsNest = "{\"sourceField\":\"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\", \"editable\":\"" + editable + "\", \"appendable\":\"" + appendable + "\"}";
				}
				else {
                    mapsNest += ",{\"sourceField\":\"" + $(this).attr('sourceField') + "\", \"destField\":\"" + $(this).attr('destField') + "\", \"editable\":\"" + editable + "\", \"appendable\":\"" + appendable + "\"}";
				}
			}
		}
	});	
	
	maps = "{\"sourceForm\":\"" + $('#forms').val() + "\", \"destForm\":\"<%=destFormCode%>\", \"maps\":[" + maps + "], \"sourceFormNest\":\"" + sourceFormNest + "\", \"destFormNest\":\"" + destFormNest + "\", \"mapsNest\":[" + mapsNest + "]}";
	window.opener.setSequence(maps, "表单映射");
	window.close();
}

$(function() {
    var maps = window.opener.getMaps();
    if (maps && maps!="") {
		maps = $.parseJSON(maps);
		$("#forms").val([maps.sourceForm]).trigger("change");
		$.each(maps.maps, function(index, data){
			var trId = "tr_" + data.sourceField + "_" + data.destField;
			var isNestTable = false;
			for (var one in sources) {
				if (sources[one].id==data.sourceField) {
					if (sources[one].macroType=="nest_table" || sources[one].macroType=="macro_detaillist_ctl" || sources[one].macroType=="nest_sheet") {
						isNestTable = true;
						break;
					}
				}
			}

			// 找到表单域名称
			var sourceFieldTitle = "", destFieldTitle = "";
			$.each(sources, function(index, dt) {
				if (dt.id==data.sourceField) {
					sourceFieldTitle = dt.name;
				}
			})
			$.each(dests, function(index, dt) {
				if (dt.id==data.destField) {
					destFieldTitle = dt.name;
				}
			})

			var tr = "<tr id='" + trId + "' sourceField='" + data.sourceField + "' destField='" + data.destField + "'>";
			tr += "<td align='center'>字段</td>";
			tr += "<td align='center'>" + sourceFieldTitle + "</td>";
			tr += "<td align='center'>" + destFieldTitle + "</td>";
			tr += "<td align='center'>";
			tr += "<span style='display:<%=isOpLinkFlow?"none":""%>'><input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;</span>";
			// 如果是嵌套表，则appendable才有效
			if (isNestTable)
				tr += "<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;";
			tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
			tr += "</tr>";
			$("#mapTable tr:last").after(tr);
		})

		$.each(maps.mapsNest, function(index, data){
			var trId = "tr_" + data.sourceField + "_" + data.destField;

			// 找到表单域名称
			var sourceFieldTitle = "", destFieldTitle = "";
			$.each(sourcesNest, function(index, dt) {
				if (dt.id==data.sourceField) {
					sourceFieldTitle = dt.name;
				}
			})
			$.each(destsNest, function(index, dt) {
				if (dt.id==data.destField) {
					destFieldTitle = dt.name;
				}
			})

			var tr = "<tr id='" + trId + "' sourceField='" + data.sourceField + "' destField='" + data.destField + "'>";
			tr += "<td align='center'>字段</td>";
			tr += "<td align='center'>" + sourceFieldTitle + "</td>";
			tr += "<td align='center'>" + destFieldTitle + "</td>";
			tr += "<td align='center'>";
			tr += "<span style='display:<%=isOpLinkFlow?"none":""%>'><input id='" + trId + "_editable' type='checkbox' value='true'>修改&nbsp;&nbsp;<input id='" + trId + "_appendable' type='checkbox' value='true'>添加&nbsp;&nbsp;</span>";
			tr += "&nbsp;&nbsp<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
			tr += "</tr>";
			$("#mapTableNest tr:last").after(tr);
		})
    }
})
</script>
</html>