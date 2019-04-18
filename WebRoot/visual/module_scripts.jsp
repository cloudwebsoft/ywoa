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
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
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
if (eventType.equals("validate"))
	scriptStr = StrUtil.getNullStr(msd.getScript("validate"));
else if (eventType.equals("create"))
	scriptStr = StrUtil.getNullStr(msd.getScript("create"));
else if (eventType.equals("save")) {
	scriptStr = StrUtil.getNullStr(msd.getScript("save"));
}
else if (eventType.equals("form_js")) {
	scriptStr = FileUtil.ReadFile(Global.getRealPath() + "flow/form_js/form_js_" + formCode + ".jsp", "utf-8");
	scriptStr = StrUtil.HtmlEncode(scriptStr);
}
else
	scriptStr = StrUtil.getNullStr(msd.getScript("del"));

if (op.equals("saveScript")) {
	String script = ParamUtil.get(request, "script");
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		if (eventType.equals("form_js")) {
			FileUtil.WriteFile(Global.getRealPath() + "/flow/form_js/form_js_" + formCode + ".jsp", script, "utf-8");
			re = true;
		}
		else {
			re = msd.saveScript(eventType, script);
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
	catch (ResKeyException e) {
		json.put("ret", "false");
		json.put("msg", e.getMessage(request));
	}
	catch (ErrMsgException e) {
		json.put("ret", "false");
		json.put("msg", e.getMessage());
	}	
	out.print(json);
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>脚本</title>
<style type="text/css" media="screen">
    #editor { 
        position: absolute;
        top: 34px;
        right: 0;
        bottom: 0;
        left: 0;
    }
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
</head>
<body style="padding:0px; margin:0px">
<div style="text-align:center; margin:0px; height:30px; padding:1px; font-size:10pt">
<select id="eventType" name="eventType" onchange="onEventTypeChange()">
<option value="validate">验证事件</option>
<option value="create">添加事件</option>
<option value="save">修改事件</option>
<option value="del">删除事件</option>
<option value="form_js">前台脚本</option>
</select>
<script>
o("eventType").value = "<%=eventType%>";
</script>
<input type="button" onclick="saveScript()" value="保存" class="btn" />
<%
String url = "../admin/script_frame.jsp?formCode=" + formCode;
%>
<input type="button" value="设计器" class="btn" onclick="ideWin=openWinMax('<%=url%>');" />

<help:HelpDocTag id="717" type="content" size="100"></help:HelpDocTag>

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
		url: "module_scripts.jsp",
		data : {
			op: "saveScript",
			code: "<%=code%>",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1",	
			eventType: o("eventType").value,
			script: editor.getValue()
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			alert(data.msg);
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
		    "scene": "module." + $('#eventType').val(),		    
		    "data":getScript()
	    }
		ideWin.leftFrame.postMessage(data, '*');
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
</HTML>