<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.util.file.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

String formCode = msd.getString("form_code");

String scripts = msd.getString("scripts");

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();

String eventType = ParamUtil.get(request, "eventType");
if (eventType.equals("")) {
	eventType = "validate";
}

String scriptStr = "";
switch (eventType) {
	case "validate":
		scriptStr = StrUtil.getNullStr(msd.getScript("validate"));
		break;
	case "create":
		scriptStr = StrUtil.getNullStr(msd.getScript("create"));
		break;
	case "save":
		scriptStr = StrUtil.getNullStr(msd.getScript("save"));
		break;
	case "form_js":
		scriptStr = FileUtil.ReadFile(Global.getRealPath() + "flow/form_js/form_js_" + formCode + ".jsp", "utf-8");
		scriptStr = StrUtil.HtmlEncode(scriptStr);
		break;
	case "import_validate":
		scriptStr = StrUtil.getNullStr(msd.getScript("import_validate"));
		break;
	case "import_create":
		scriptStr = StrUtil.getNullStr(msd.getScript("import_create"));
		break;
	case "see":
		scriptStr = StrUtil.getNullStr(msd.getScript("see"));
		break;
	case "preProcess":
		scriptStr = StrUtil.getNullStr(msd.getScript("preProcess"));
		break;
	default:
		scriptStr = StrUtil.getNullStr(msd.getScript("del"));
		break;
}
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>脚本</title>
	<style type="text/css" media="screen">
		#editor {
			position: absolute;
			top: 40px;
			right: 0;
			bottom: 0;
			left: 0;
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
	<script src="../js/bootstrap/js/bootstrap.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<script src="../js/layui/layui.js" charset="utf-8"></script>
</head>
<body style="padding:0px; margin:0px">
<div class="form-inline form-group text-center">
	<select id="eventType" name="eventType" onchange="onEventTypeChange()" class="form-control">
		<option value="preProcess">预处理事件</option>
		<option value="validate">验证事件</option>
		<option value="see">查看事件</option>
		<option value="create">添加事件</option>
		<option value="save">修改事件</option>
		<option value="del">删除事件</option>
		<option value="form_js">前台脚本</option>
		<option value="import_validate">导入前验证事件</option>
		<option value="import_create">导入后事件</option>
	</select>
<script>
o("eventType").value = "<%=eventType%>";
</script>
<input type="button" onclick="saveScript()" value="保存" class="btn btn-default" />
<input type="button" value="设计器" class="btn btn-default" onclick="openIdeWin()" />
</div>
<textarea id="content" style="display:none"><%=scriptStr %></textarea>
<pre id="editor"></pre>
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script>
	function openWinMax(url) {
		return window.open(url, '', 'scrollbars=yes,resizable=yes,channelmode'); // 开启一个被F11化后的窗口起作用的是最后那个特效
	}

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
function onEventTypeChange() {
	var type = o("eventType").value;
	window.location.href = "module_scripts.jsp?code=<%=code%>&eventType=" + type;
}

function saveScript() {
	$.ajax({
		type: "post",
		url: "updateScript.do",
		method: "post",
		data : {
			code: "<%=code%>",
			eventType: o("eventType").value,
			script: editor.getValue()
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			layer.msg(data.msg);
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
var ideUrl = "../admin/script_frame.jsp?formCode=<%=formCode%>";
var ideWin;
var cwsToken = "";

function openIdeWin() {
	ideWin = openWinMax(ideUrl);
}

var onMessage = function(e) {
	var d = e.data;
	var data = d.data;
	var type = d.type;
	if (type=="setScript") {
		setScript(data);
		if (d.cwsToken!=null) {
			cwsToken = d.cwsToken;
			ideUrl = "../admin/script_frame.jsp?formCode=<%=formCode%>&cwsToken=" + cwsToken;
		}
	}
	else if (type=="getScript") {
		var data={
		    "type":"openerScript",
		    "version":"<%=version%>",
		    "spVersion":"<%=spVersion%>",
		    "scene": "module." + $('#eventType').val(),
		    "data":getScript()
	    }
		ideWin.leftFrame.postMessage(data, '*');
	}
	else if (type == "setCwsToken") {
		cwsToken = d.cwsToken;
		ideUrl = "../admin/script_frame.jsp?formCode=<%=formCode%>&cwsToken=" + cwsToken;
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