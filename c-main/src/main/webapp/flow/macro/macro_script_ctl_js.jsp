<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%

	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/javascript;charset=utf-8");

	Privilege pvg = new Privilege();
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	String fieldName = ParamUtil.get(request, "fieldName");

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
%>
var editor;
$(function() {
    editor = ace.edit("pre_<%=fieldName%>");
    // editor.setTheme("ace/theme/eclipse");
    // editor.setTheme("ace/theme/terminal");
	editor.setTheme("<%=cfg.get("aceTheme")%>");
    editor.getSession().setMode("ace/mode/java");
    
	editor.setOptions({
		readOnly: false,
		highlightActiveLine: true,
		highlightGutterLine: true
	})
	editor.renderer.$cursorLayer.element.style.opacity=0;    
	editor.setValue($('#<%=fieldName%>').val());	
});

// ajaxForm序列化提交数据之前的回调函数
function ctlOnBeforeSerialize() {
	// ajaxForm提交前，必须先提取赋值
	var isFlow = <%= flowId!=-1 ? true:false%>;
	if (isFlow) {
	    $('#<%=fieldName%>').val(editor.getValue());
	}
	else {
	    $('#<%=fieldName%>').val(editor.getValue());
	}
}

function getScript() {
	return editor.getValue();
}

function setScript(script) {
	editor.setValue(script);
}

<%
com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
String version = StrUtil.getNullStr(cfg.get("version"));
String spVersion = StrUtil.getNullStr(spCfg.get("version"));
String url = request.getContextPath() + "/admin/script_frame.jsp";
%>
var ideUrl = "<%=url%>";
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
			ideUrl = "<%=request.getContextPath()%>/admin/script_frame.jsp?cwsToken=" + cwsToken;
		}
	}
	else if (type=="getScript") {
		var data={
		    "type":"openerScript",
		    "version":"<%=version%>",
		    "spVersion":"<%=spVersion%>",
		    "scene": "ScriptCtl",		    
		    "data":getScript()
	    }
		ideWin.leftFrame.postMessage(data, '*');
	} else if (type == "setCwsToken") {
        cwsToken = d.cwsToken;
		ideUrl = "<%=request.getContextPath()%>/admin/script_frame.jsp?cwsToken=" + cwsToken;
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
