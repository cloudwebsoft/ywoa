<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.SpConfig" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%
// 节点在控件中的内部名称
String op = ParamUtil.get(request, "op");

String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
String internalName = ParamUtil.get(request, "internalName");
String mode = ParamUtil.get(request, "mode");

String scriptStr = "";
WorkflowPredefineMgr wpfm = new WorkflowPredefineMgr();
WorkflowPredefineDb wpd = new WorkflowPredefineDb();
wpd = wpd.getDefaultPredefineFlow(flowTypeCode);
if (wpd==null) {
	%>
 	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(SkinUtil.makeInfo(request, "请先创建并保存流程图！"));
	return;
}
String scripts = wpd.getScripts();

Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();

String eventType = ParamUtil.get(request, "eventType");
if ("".equals(eventType)) {
	if ("".equals(internalName)) {
		eventType = "onFinish";
	} else {
		eventType = "validate";
	}
}

if ("".equals(internalName)) {
	// out.print(StrUtil.p_center("请选择节点！"));
	// return;
	if ("onFinish".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getOnFinishScript(scripts));
	}
	else if ("discard".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getDiscardScript(scripts));
	}
	else if ("deleteValidate".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getDeleteValidateScript(scripts));
	}
	else if ("recall".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getRecallScript(scripts));
	}
	else if ("preInit".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getPreInitScript(scripts));
	}
	else {
		// form_js
		scriptStr = FileUtil.ReadFile(Global.getRealPath() + "flow/form_js/form_js_" + lf.getFormCode() + ".jsp", "utf-8");
		scriptStr = StrUtil.HtmlEncode(scriptStr);
	}
}
else {
	if ("validate".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getValidateScript(scripts, internalName));
	}
	else if ("actionReturn".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getActionReturnScript(scripts, internalName));
	}
	else if ("preDispose".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getActionPreDisposeScript(scripts, internalName));
	}
	else if ("actionActive".equals(eventType)) {
		scriptStr = StrUtil.getNullStr(wpfm.getActionActiveScript(scripts, internalName));
	}
	else {
		scriptStr = StrUtil.getNullStr(wpfm.getActionFinishScript(scripts, internalName));
	}
}

if ("saveNodeScript".equals(op)) {
	String script = ParamUtil.get(request, "script");
	boolean re = false;
	com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
	try {
		if ("validate".equals(eventType)) {
			re = wpfm.saveValidateScript(wpd, internalName, script);
		} else if ("actionReturn".equals(eventType)) {
			re = wpfm.saveActionReturnScript(wpd, internalName, script);
		} else if ("preDispose".equals(eventType)) {
			re = wpfm.saveActionPreDisposeScript(wpd, internalName, script);
		} else if ("actionActive".equals(eventType)) {
			re = wpfm.saveActionActiveScript(wpd, internalName, script);
		} else if ("preInit".equals(eventType)) {
			re = wpfm.savePreInitScript(wpd, script);
		}
		else {
			re = wpfm.saveActionFinishScript(wpd, internalName, script);
		}
		if (re) {
			json.put("ret", "true");
			json.put("msg", "操作成功！");
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
else if ("saveOnFinishScript".equals(op)) {
	String script = ParamUtil.get(request, "script");
	boolean re = false;
	com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
	try {
		if ("onFinish".equals(eventType)) {
			re = wpfm.saveOnFinishScript(wpd, script);
		}
		else if ("discard".equals(eventType)) {
			re = wpfm.saveDiscardScript(wpd, script);
		}
		else if ("recall".equals(eventType)) {
			re = wpfm.saveRecallScript(wpd, script);
		}
		else if ("deleteValidate".equals(eventType)) {
			re = wpfm.saveDeleteValidateScript(wpd, script);
		}
		else if ("form_js".equals(eventType)) {
			FileUtil.WriteFile(Global.getRealPath() + "/flow/form_js/form_js_" + lf.getFormCode() + ".jsp", script, "utf-8");
			re = true;
		}
		else if ("preInit".equals(eventType)) {
			re = wpfm.savePreInitScript(wpd, script);
		}
		
		if (re) {
			json.put("ret", "true");
			json.put("msg", "操作成功！");
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
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>脚本</title>
<style type="text/css" media="screen">
    #editor {
        position: absolute;
        top: 45px;
        right: 0;
        bottom: 0;
        left: 0;
    }
</style>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/bootstrap/js/bootstrap.min.js"></script>
<script src="../js/layui/layui.js" charset="utf-8"></script>
<script>
var ideWin;
</script>
</head>
<body style="padding:0px; margin:0px">
<div class="form-inline form-group" style="text-align:center; border:1px solid #cccccc; margin:0; height:45px; padding:1px; font-size:10pt">
	<select id="eventType" name="eventType" style="width:150px" onchange="onEventTypeChange()">
		<%if (!"".equals(internalName)) {%>
		<option value="validate">节点验证事件</option>
		<option value="preDispose">节点预处理事件</option>
		<option value="actionActive">节点激活事件</option>
		<option value="actionFinish">节点流转事件</option>
		<option value="actionReturn">节点返回事件</option>
		<%}%>
		<option value="preInit">流程初始化事件</option>
		<option value="onFinish">流程结束事件</option>
		<option value="discard">流程放弃事件</option>
		<option value="recall">流程撤回事件</option>
		<option value="deleteValidate">删除流程验证事件</option>
		<option value="form_js">前台脚本(javascript)</option>
	</select>
	<script>
		$(function() {
			var inDegree = window.parent.GetActionProperty("curSelected", "inDegree");
			if (inDegree == "0") {
				$("#eventType option[value='actionActive']").remove();
			}

			o("eventType").value = "<%=eventType%>";
		})
	</script>
	<%if (!internalName.equals("")) {%>
	<input type="button" onClick="saveScript()" value="保存" class="btn btn-default"/>
	<%} else {%>
	<input type="button" onClick="saveOnFinishScript()" value="保存" class="btn btn-default"/>
	<%}%>

	<input type="button" value="设计器" class="btn btn-default" onclick="openIdeWin()"/>
</div>

<textarea id="content" style="display:none"><%=scriptStr %></textarea>
<div id="editor"></div>
    
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
	
	editor.setValue($('#content').val());
</script>
</body>
<script>
	var layer;
	layui.use('layer', function(){
		layer = layui.layer;
	});

	function onEventTypeChange() {
		var type = o("eventType").value;
		if (type == "onFinish" || type == "discard" || type == "form_js" || type == "deleteValidate" || type == "recall") {
			window.location.href = "flow_designer_script_view.jsp?flowTypeCode=<%=StrUtil.UrlEncode(flowTypeCode)%>&eventType=" + type;
		} else {
			window.location.href = "flow_designer_script_view.jsp?internalName=<%=internalName%>&flowTypeCode=<%=StrUtil.UrlEncode(flowTypeCode)%>&eventType=" + type;
		}
	}

	function saveScript() {
		$.ajax({
			type: "post",
			url: "flow_designer_script_view.jsp",
			data: {
				op: "saveNodeScript",
				flowTypeCode: "<%=flowTypeCode%>",
				internalName: "<%=internalName%>",
				eventType: o("eventType").value,
				script: editor.getValue()
			},
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				// ShowLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				layer.msg(data.msg);
			},
			complete: function (XMLHttpRequest, status) {
				// HideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	}

	function getScript() {
		return editor.getValue();
	}

	function setScript(script) {
		editor.setValue(script);
	}

	function saveOnFinishScript() {
		$.ajax({
			type: "post",
			url: "flow_designer_script_view.jsp",
			data: {
				op: "saveOnFinishScript",
				flowTypeCode: "<%=flowTypeCode%>",
				eventType: o("eventType").value,
				script: editor.getValue()
			},
			dataType: "html",
			beforeSend: function (XMLHttpRequest) {
				//ShowLoading();
			},
			success: function (data, status) {
				data = $.parseJSON(data);
				layer.msg(data.msg);
			},
			complete: function (XMLHttpRequest, status) {
				//HideLoading();
			},
			error: function (XMLHttpRequest, textStatus) {
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
	var ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>";
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
				ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>&cwsToken=" + cwsToken;
			}
		} else if (type == "getScript") {
			var data = {
				"type": "openerScript",
				"version": "<%=version%>",
				"spVersion": "<%=spVersion%>",
				"scene": "flow." + $('#eventType').val(),
				"data": getScript()
			};
			// ideWin.mainScriptFrame.postMessage(data, '*');
			ideWin.leftFrame.postMessage(data, '*');
		} else if (type == "setCwsToken") {
			cwsToken = d.cwsToken;
			ideUrl = "script_frame.jsp?formCode=<%=lf.getFormCode()%>&cwsToken=" + cwsToken;
		}
	};

	$(function () {
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