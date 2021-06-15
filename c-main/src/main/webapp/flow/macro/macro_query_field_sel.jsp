<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.query.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
* 用于当表单设计时，选择自由查询选择器宏控件，弹出本窗口
*/

String op = ParamUtil.get(request, "op");
if (op.equals("getCols")) {
    response.setContentType("text/html;charset=utf-8");

    String opts = "";
	int queryId = ParamUtil.getInt(request, "queryId");

	FormQueryDb fqd = new FormQueryDb();
	fqd = fqd.getFormQueryDb(queryId);
	if (!fqd.isScript()) {
		String formCode = fqd.getTableCode();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		opts += "<option value='" + FormSQLBuilder.PRIMARY_KEY_ID + "'>主键ID</option>";
		Iterator ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			// if (!ff.isCanList())
			// 	continue;
			opts += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
		}
	}
	else {
		QueryScriptUtil qsu = new QueryScriptUtil();
		HashMap map = qsu.getCols(request, fqd);
		Iterator irMap = map.keySet().iterator();
		while (irMap.hasNext()) {
			String keyName = (String) irMap.next();

			opts += "<option value='" + keyName + "'>" + map.get(keyName) + "</option>";
		}
	}
		
	out.print(opts);
	return;
}

String formCode = ParamUtil.get(request, "formCode");
if (formCode.equals("")) {
    response.setContentType("text/html;charset=utf-8");
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请先创建表单，然后编辑表单时插入此控件！"));
	return;
}
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "formCode", formCode, getClass().getName());
}
catch (ErrMsgException e) {
    response.setContentType("text/html;charset=utf-8");
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>   
<title>查询字段选择器</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>

<link href="../../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="../../js/jquery-showLoading/jquery.showLoading.js"></script>

<script>
function window_onload() {
}
</script>
</head>
<body onload="window_onload()">
<%
// 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
%>
<table class="tabStyle_1" id="mapTable" style="padding:0px; margin:0px;" width="100%" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td height="28" colspan="5" class="tabStyle_1_title">&nbsp;请选择</td>
    </tr>
    <tr>
      <td height="22" align="left">
        查询
      </td>
      <td height="22" colspan="4" align="left">
      <input id="queryId" name="queryId" type="hidden" />
      <span id="queryTitle"></span>
        <a href="javascript:;" onClick="selQuery()">选择查询</a>
        <span id="spanField">
        记录
        <select id="otherField" name="otherField">
		</select>
        <span style="display:none">
        ，显示
        <select id="otherShowField" name="otherShowField">
        </select>
        </span>
        </span>
        &nbsp;&nbsp;<input type="button" value="确定" onClick="doSel()"></td>
    </tr>
    <tr>
      <td height="22" colspan="5" align="left" class="tabStyle_1_title">条件映射
		&nbsp;[<a href="javascript:" onclick="addFields()">添加映射</a>]      
      </td>
    </tr>
    <tr>
      <td height="22" colspan="5" align="left">
      <div id="divFields">
      <%
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
	  
        int count = 0;
        String sub_nav_tag_url = "{'field1':'sqlfield1'}";
        JSONObject jsonObj = new JSONObject(sub_nav_tag_url);
        Iterator ir3 = jsonObj.keys();
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            %>
            <div id="divField<%=count%>">
              <div>
              <select id="field<%=count%>" name="field">
                <option value="">无</option>
                <option value="<%=FormSQLBuilder.PRIMARY_KEY_ID%>">主键ID</option>
                <%
                Iterator ir = fd.getFields().iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField)ir.next();
                    if (!ff.isCanQuery())
                        continue;
                    %>
                    <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                	<%
                }
                %>
                </select>
              <font style="font-family:宋体">-&gt;</font>
              <span id="spanQueryField<%=count%>"></span>
              <a href='javascript:;' onClick="if ($(this).parent().parent().parent().children().length==1) {alert('至少需映射一个条件字段！'); return;} var pNode=this.parentNode; pNode.parentNode.parentNode.removeChild(pNode.parentNode);">×</a>
              </div>
            </div>
        <%
            count++;
        }
        %>
        </div>
      </td>
    </tr>
    <tr>
      <td height="22" colspan="5" align="left" class="tabStyle_1_title">字段映射</td>
    </tr>
    <tr>
      <td width="8%" height="22" align="left">映射</td>
      <td width="23%" align="left">
      <select id="sourceField" name="sourceField">
      </select></td>
      <td width="5%" align="center">至</td>
      <td width="37%" align="left">
	  <select id="destField" name="destField">
      <%
	  Iterator ir = fd.getFields().iterator();
	  String json = "";
	  while (ir.hasNext()) {
	  	FormField ff = (FormField)ir.next();
		%>
          <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
        <%
		if (json.equals(""))
			json = "{'id':'" + ff.getName() + "', 'name':'" + ff.getTitle() + "', 'type':'" + ff.getType() + "', 'macroType':'" + ff.getMacroType() + "', 'defaultValue':'" + ff.getDefaultValue() + "'}";
		else
			json += ",{'id':'" + ff.getName() + "', 'name':'" + ff.getTitle() + "', 'type':'" + ff.getType() + "', 'macroType':'" + ff.getMacroType() + "', 'defaultValue':'" + ff.getDefaultValue() + "'}";		  
	  }
	  %>
      </select>    
      </td>
      <td width="27%" height="22" align="left"><input type="button" class="btn" value="添加" onclick="addMap()" /></td>
    </tr>
  </tbody>
</table>
</body>
<script language="javascript">
<!--
var dests = [<%=json%>];

function addMap() {
	if (sourceField.value=="" || destField.value=="") {
		alert("请选择查询！");
		return;
	}

	var trId = "tr_" + sourceField.value + "_" + destField.value;
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
	
	var tr = "<tr id='" + trId + "' sourceField='" + sourceField.value + "' destField='" + destField.value + "'>";
	tr += "<td>字段</td>";
	tr += "<td>" + sourceField.options[sourceField.selectedIndex].text + "</td>";
	tr += "<td align='center'>至</td>";
	tr += "<td>" + destField.options[destField.selectedIndex].text + "</td>";
	tr += "<td>";
	tr += "<a href='javascript:;' onclick=\"$('#" + trId + "').remove()\">删除</a></td>";
	tr += "</tr>";
	
	$("#mapTable tr:last").after(tr);
	
	sourceField.value = '';
	destField.value = '';
}

function makeMap() {
	// 组合成json字符串{sourceForm:..., destForm:..., maps:[{sourceField:..., destField:..., editable:true, appendable:true},...{...}]}
	var maps = "";
	$("#mapTable tr").each(function(k){
		// 判断是否为描述映射的行
		if ($(this)[0].id!="") {
			if ($(this).attr("id").indexOf("tr_")==0) {
				if (maps=="") {
					maps = "{'sourceField': '" + $(this).attr('sourceField') + "', 'destField':" + $(this).attr('destField') + "'}";
				}
				else {
					maps += ",{'sourceField': '" + $(this).attr('sourceField') + "', 'destField':'" + $(this).attr('destField') + "'}";
				}
			}
		}
	});
	
	var condMaps = "";
	var fieldAry = document.getElementsByName("field");
	var queryFieldAry = document.getElementsByName("queryField");
	
	// destField为父窗口中的表单域
	for (var i=0; i<fieldAry.length; i++) {
		// 如果未设条件
		if (queryFieldAry[i].value=="")
			continue;
			
		if (condMaps=="") {
			condMaps = "{'sourceField': '" + queryFieldAry[i].value + "', 'destField':'" + fieldAry[i].value + "'}";
		}
		else {
			condMaps += ",{'sourceField': '" + queryFieldAry[i].value + "', 'destField':'" + fieldAry[i].value + "'}";
		}
	}
		
	maps = "'maps':[" + maps + "], 'mapsCond':[" + condMaps + "]";
	return maps;
}

// 对字符串中的引号进行编码，以免引起json解析问题
function encodeJSON(jsonString) {
	jsonString = jsonString.replace(/=/gi, "%eq");
	jsonString = jsonString.replace(/\{/gi, "%lb");
	jsonString = jsonString.replace(/\}/gi, "%rb");
	jsonString = jsonString.replace(/,/gi, "%co"); // 逗号
	jsonString = jsonString.replace(/\"/gi, "%dq");
	jsonString = jsonString.replace(/'/gi, "%sq");
	jsonString = jsonString.replace(/\r\n/g, "%rn"); // 回车换行
	jsonString = jsonString.replace(/\n/g, "%n");
	return jsonString;
}

function doSel() {	
	// {formCode:, idField:, showField:, filter:, maps:[{sourceField:, destField:},{sourceField:, destField:}], isParentSaveAndReload:, isMine:}
	var maps = makeMap();
	
	var json = "{'formCode':'<%=formCode%>', 'queryId':'" + o("queryId").value + "', 'idField':" + o("otherField").value + "', 'showField':'" + o("otherField").value + "', 'isParentSaveAndReload':'true', " + maps + "}";
	// 不能带有单或双引号，会使得赋值后，IE源码混乱，出现?号
	json = json.replace(/'/gi, "");
	window.opener.setSequence(json, "<%=fd.getName()%>_" + otherShowField.options[otherShowField.selectedIndex].text);
	window.close();
}


var fieldsMapStr = "";

function selQuery() {
	openWin("../../flow/form_query_list_sel.jsp?type=all", 800, 600);
	fieldsMapStr = "";
}

function doSelQuery(id, title) {
	if (id==$('#queryId').val())
		return;
	
	$("#queryId").val(id);
	$("#queryTitle").html(title);
	
	$.ajax({
	   type: "POST",
	   url: "../../visual/getQueryCondField.do",
	   data: "id=" + id,
	   success: function(html){
			$("#spanQueryField0").html(html);
		}
	});
	
	getCols(id);
}

// 取得列
function getCols(queryId) {
	$.ajax({
		type: "post",
		url: "macro_query_field_sel.jsp",
		data : {
			queryId: queryId,
			op: "getCols"
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#mapTable').showLoading();				
		},
		success: function(data, status){
			$("#sourceField").empty();
			$("#sourceField").append(data);
			
			$("#otherField").empty();
			$("#otherField").append(data);
			
			$("#otherShowField").empty();
			$("#otherShowField").append(data);
		},
		complete: function(XMLHttpRequest, status){
			$('#mapTable').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}

$.fn.outerHTML = function(){  return $("<p></p>").append(this.clone()).html(); }

function addFields() {
	if (o("queryId").value=="") {
		alert("请先选择查询！");
		return;
	}

	if (fieldsMapStr=="")
		fieldsMapStr = $("#divField0").outerHTML();
	$("#divFields").append(fieldsMapStr);
}

//-->
</script>
</html>