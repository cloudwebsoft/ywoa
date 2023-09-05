<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.flow.query.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="org.json.*"%>
<%@ page import="cn.js.fan.web.SkinUtil" %>
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

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String isSystem = ParamUtil.get(request, "isSystem");
String scriptStr = "";
int id = ParamUtil.getInt(request, "id", -1);
FormQueryDb fqd = new FormQueryDb();
String queryName = "";
if (id!=-1) {
	fqd = fqd.getFormQueryDb(id);
	scriptStr = fqd.getScripts();
	queryName = fqd.getQueryName();
}

if (op.equals("create")) {
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		FormQueryMgr fqm = new FormQueryMgr();
		int fqdId = fqm.createForScript(request);
		if (fqdId!=0) {
			json.put("ret", "true");
			json.put("msg", "操作成功！");
			json.put("id", "" + fqdId);
		}
		else {
			json.put("ret", "false");
			json.put("msg", "操作失败！");
		}
	}
	catch (ErrMsgException e) {
		json.put("ret", "false");
		json.put("msg", e.getMessage());
	}
	out.print(json);
	return;
}
else if (op.equals("modify")) {
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		FormQueryMgr fqm = new FormQueryMgr();
		re = fqm.modifyForScript(request);
		if (re) {
			json.put("ret", "true");
			json.put("msg", "操作成功！");
			json.put("id", "" + fqd.getId());
		}
		else {
			json.put("ret", "false");
			json.put("msg", "操作失败！");
		}
	}
	catch (ErrMsgException e) {
		json.put("ret", "false");
		json.put("msg", e.getMessage());
	}
	out.print(json);
	return;
}

if (id==-1) {
	op = "create";
} else {
	op = "modify";
}
%>
<!DOCTYPE html>
<html>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>脚本</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style type="text/css" media="screen">
    #editor { 
        position: absolute;
        top: 95px;
        right: 0;
        bottom: 0;
        left: 0;
    }
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../inc/map.js"></script>
</HEAD>
<BODY>
<%@ include file="form_query_nav.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<div class="spacerH"></div>
<div style="text-align:center; margin:0px; height:25px; padding:1px; font-size:10pt; margin-top:0px">
查询名称&nbsp;<input id="queryName" name="name" value="<%=queryName%>" />
<input type="button" onclick="saveScript()" value="保存" class="btn" />
<input type="button" value="设计器" class="btn" onclick="openIdeWin()" />
<input type="button" value="查询" class="btn" onclick="addTab('<%=fqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_script_list_do.jsp?id=<%=fqd.getId()%>&op=query')" />
<script>
$.fn.outerHTML = function(){  return $("<p></p>").append(this.clone()).html(); }

function addCalcuField() {
	if (o("divCalcuField0"))
		$("#divCalcuField").append($("#divCalcuField0").outerHTML());
	else
		initDivCalcuField();
}

function initDivCalcuField() {
	  $.ajax({
		type: "POST",
		url: "form_query_script_calcu_field_ajax.jsp",
		data : {
			id: <%=id%>
		},		 
		success: function(html) {
			  $("#divCalcuField").html(html);
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});
}
</script>
<%if (id!=-1) {%>
&nbsp;&nbsp;<a href="javascript:" onclick="addCalcuField()">添加合计字段</a>
          <div id="divCalcuField" style="text-align:left; margin-top:3px;">
          <div style="float:left">合计字段：</div>          
          <%
          if (id!=-1) {
			  QueryScriptUtil qsu = new QueryScriptUtil();
			  
			  Map mapFieldTitle = qsu.getCols(request, fqd);
			  Map mapFieldType = qsu.getMapFieldType();
			  
              int curCalcuFieldCount = 0;
              String statDesc = fqd.getStatDesc();
              if (statDesc.equals("")) {
				  statDesc = "{}";
			  }
              JSONObject json = new JSONObject(statDesc);
              Iterator ir3 = json.keys();
              while (ir3.hasNext()) {
                  String key = (String) ir3.next();
                  %>
                    <div id="divCalcuField<%=curCalcuFieldCount%>" style="float:left">
                    <select id="calcFieldCode<%=curCalcuFieldCount%>" name="calcFieldCode">
                    <option value="">无</option>
                    <%
					  Iterator irField = mapFieldTitle.keySet().iterator();
					  while (irField != null && irField.hasNext()) {
						  String fieldName = (String)irField.next();
						  
						  Integer iType = (Integer)mapFieldType.get(fieldName.toUpperCase());
						  int fieldType = FormField.FIELD_TYPE_VARCHAR;
						  fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
					
						  if (fieldType==FormField.FIELD_TYPE_INT
							  || fieldType==FormField.FIELD_TYPE_FLOAT
							  || fieldType==FormField.FIELD_TYPE_DOUBLE
							  || fieldType==FormField.FIELD_TYPE_PRICE
							  || fieldType==FormField.FIELD_TYPE_LONG
							  ) {
						  %>
						  <option value="<%=fieldName%>"><%=mapFieldTitle.get(fieldName.toUpperCase())%></option>
						  <%}
					  }
                    %>
                    </select>
                    <select id="calcFunc<%=curCalcuFieldCount%>" name="calcFunc">
                    <option value="0">求和</option>
                    <option value="1">求平均值</option>
                    </select>
                    <a href='javascript:;' onclick="var pNode=this.parentNode; pNode.parentNode.removeChild(pNode);">×</a>
                    &nbsp;
                    </div>
                    <script>
                    $("#calcFieldCode<%=curCalcuFieldCount%>").val("<%=key%>");
                    $("#calcFunc<%=curCalcuFieldCount%>").val("<%=json.get(key)%>");
                    </script>
                  <%
                  curCalcuFieldCount ++;
              }
          }
          %>
          </div>
	<script>
		$(function() {
			$('#editor').css("top", "120px");
		})
	</script>
<%}%>          
</div>
<!--ie8 doesn't keep newlines in regular divs. Use  pre  or  <div style="whitespace:pre" -->
<pre id="editor"><%=StrUtil.HtmlEncode(scriptStr)%></pre>

</BODY>
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script>
    var editor = ace.edit("editor");
    // editor.setTheme("ace/theme/eclipse");
    // editor.setTheme("ace/theme/terminal");
	editor.setTheme("<%=cfg.get("aceTheme")%>");
    editor.getSession().setMode("ace/mode/java");
    
	editor.setOptions({
		readOnly: true,
		highlightActiveLine: true,
		highlightGutterLine: true
	})
	editor.renderer.$cursorLayer.element.style.opacity=0;
</script>
<script>
function saveScript() {
	if (o("queryName").value=="") {
		jAlert("请填写名称！","提示");
		return;
	}
	
	// 字段合计描述字符串处理
	var calcCodesStr = "";
	var calcFuncs = $("select[name='calcFunc']");
	
	var map = new Map();
	var isFound = false;
	$("select[name='calcFieldCode']").each(function(i) {
		if ($(this).val()!="") {
			if (!map.containsKey($(this).val()))
				map.put($(this).val(), $(this).val());
			else {
				isFound = true;
				jAlert($(this).find("option:selected").text() + "存在重复！","提示");
				return false;
			}

			if (calcCodesStr=="")
				calcCodesStr = "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
			else
				calcCodesStr += "," + "\"" + $(this).val() + "\":\"" + calcFuncs.eq(i).val() + "\"";
		}
	})
	if (isFound)
		return;

	calcCodesStr = "{" + calcCodesStr + "}";	
	
	$.ajax({
		type: "post",
		url: "form_query_script.jsp",
		data : {
			op: "<%=op%>",
			queryName: o("queryName").value,
			id: <%=id%>,
			isSystem: "<%=isSystem%>",
			script: editor.getValue(),
			statDesc: calcCodesStr
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			<%if (op.equals("create")) {%>
				if (data.ret=="true") {
					jAlert(data.msg,"提示");
					window.location.href = "form_query_script.jsp?id=" + data.id + "&isSystem=<%=isSystem%>";
				}
				else
					jAlert(data.msg,"提示");
			<%}else{%>
				jAlert(data.msg,"提示");			
			<%}%>
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});	
}

function getScript() {
	return editor.getValue();
}

function setScript(script) {
	editor.setValue(script);
}

<%
	com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
	com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
	String version = StrUtil.getNullStr(oaCfg.get("version"));
	String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>
var ideUrl = "../admin/script_frame.jsp";
var ideWin;
var cwsToken = "";

function openIdeWin() {
	ideWin = openWinMax(ideUrl);
}

var onMessage = function (e) {
	var d = e.data;
	var data = d.data;
	var type = d.type;
	if (type == "setScript") {
		setScript(data);
		if (d.cwsToken!=null) {
			cwsToken = d.cwsToken;
			ideUrl = "../admin/script_frame.jsp?cwsToken=" + cwsToken;
		}
	} else if (type == "getScript") {
		var data = {
			"type": "openerScript",
			"version": "<%=version%>",
			"spVersion": "<%=spVersion%>",
			"scene": "query.script",
			"data": getScript()
		};
		ideWin.leftFrame.postMessage(data, '*');
	} else if (type == "setCwsToken") {
		cwsToken = d.cwsToken;
		ideUrl = "../admin/script_frame.jsp?cwsToken=" + cwsToken;
	}
};

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
</HTML>