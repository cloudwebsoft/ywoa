<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
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
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
if (op.equals("changeTheme")) {
	String theme = ParamUtil.get(request, "theme");
	cfg.put("aceTheme", theme);
	cfg.writemodify();
	response.sendRedirect("script_main.jsp");
	return;
}

String scriptStr = "";

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
<div style="text-align:center; border:1px solid #cccccc; margin:0px; height:30px; padding:1px; font-size:10pt">
主题&nbsp;
<select id="theme" size="1">
<optgroup label="Bright">
  <option value="ace/theme/chrome">Chrome</option>
  <option value="ace/theme/clouds">Clouds</option>
  <option value="ace/theme/crimson_editor">Crimson Editor</option>
  <option value="ace/theme/dawn">Dawn</option>
  <option value="ace/theme/dreamweaver">Dreamweaver</option>
  <option value="ace/theme/eclipse">Eclipse</option>
  <option value="ace/theme/github">GitHub</option>
  <option value="ace/theme/solarized_light">Solarized Light</option>
  <option value="ace/theme/textmate">TextMate</option>
  <option value="ace/theme/tomorrow">Tomorrow</option>
  <option value="ace/theme/xcode">XCode</option>
</optgroup>
<optgroup label="Dark">
  <option value="ace/theme/ambiance">Ambiance</option>
  <option value="ace/theme/chaos">Chaos</option>
  <option value="ace/theme/clouds_midnight">Clouds Midnight</option>
  <option value="ace/theme/cobalt">Cobalt</option>
  <option value="ace/theme/idle_fingers">idleFingers</option>
  <option value="ace/theme/kr_theme">krTheme</option>
  <option value="ace/theme/merbivore">Merbivore</option>
  <option value="ace/theme/merbivore_soft">Merbivore Soft</option>
  <option value="ace/theme/mono_industrial">Mono Industrial</option>
  <option value="ace/theme/monokai">Monokai</option>
  <option value="ace/theme/pastel_on_dark">Pastel on dark</option>
  <option value="ace/theme/solarized_dark">Solarized Dark</option>
  <option value="ace/theme/terminal">Terminal</option>
  <option value="ace/theme/tomorrow_night">Tomorrow Night</option>
  <option value="ace/theme/tomorrow_night_blue">Tomorrow Night Blue</option>
  <option value="ace/theme/tomorrow_night_bright">Tomorrow Night Bright</option>
  <option value="ace/theme/tomorrow_night_eighties">Tomorrow Night 80s</option>
  <option value="ace/theme/twilight">Twilight</option>    
  <option value="ace/theme/vibrant_ink">Vibrant Ink</option>
</optgroup>
</select>
<script>
$('#theme').val('<%=cfg.get("aceTheme")%>');
</script>
<input type="button" value="确定" class="btn" onclick="ok();" />
&nbsp;&nbsp;
<input type="button" value="运行" class="btn" onclick="run();" />
&nbsp;&nbsp;
<input type="button" value="关闭" class="btn" onclick="window.top.close();" />
</div>
<!--ie8 doesn't keep newlines in regular divs. Use  pre  or  <div style="whitespace:pre" -->
<pre id="editor">
<%=scriptStr%>
</pre>
<form id="formRun" action="script_run.jsp" target="newWin">
<input name="myscript" style="display:none" />
</form>
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script src="../js/ace-noconflict/ext-language_tools.js" type="text/javascript" charset="utf-8"></script>
<script>
<!--
	// 注意此行要写在ace.edit("editor")前面
	var langTools = ace.require("ace/ext/language_tools");
		
    var editor = ace.edit("editor");
    // editor.setTheme("ace/theme/eclipse");
    // editor.setTheme("ace/theme/terminal");
	editor.setTheme("<%=cfg.get("aceTheme")%>");
    editor.getSession().setMode("ace/mode/java");
    editor.setShowPrintMargin(false);//显示打印边线
        
	data=[
		{meta:"class",caption:"WorkflowDb",value:"WorkflowDb",score:1},
		{meta:"function",caption:"Aimulation",value:"Aimulation",score:0}
	]
    
    var mytags = {
    	// identifierRegexps: [/[a-zA-Z_0-9\.\$\-\u00A2-\uFFFF]/],
        getCompletions: function(editor, session, pos, prefix, callback) {
        	// console.log("prefix=" + prefix);
            if (prefix.length === 0) {
                 return callback(null, []);
            } else {
                 return callback(null, data);
            }
        }
	}

	editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: true,
            enableLiveAutocompletion: true
        });

    // 配自定义补全要放在setOptions之后
	langTools.setCompleters([mytags]);
    
	// editor.completers = [mytags];
	
	/*	
    langTools.addCompleter({
        getCompletions: function(editor, session, pos, prefix, callback) {
        console.log("prefix=" + prefix);
            if (prefix.length === 0) {
                 return callback(null, []);
             } else {
                 return callback(null, data);
            }
        }
    });
    */
    
    /*
    var wordList = [{caption: "-a", value: "-a", score: 300, meta: "ICC2Option"}];
	var icc2Completer = {
		getCompletions: function(editor, session, pos, prefix, callback) {
		
		   var curLine = session.getDocument().getLine(pos.row);
		   console.log("curLine:" + curLine);
		   var curTokens = curLine.slice(0, pos.column).split(/\s+/);
		   console.log("curTokens:" + curTokens);
		   var curCmd = curTokens[0];
		   console.log("curCmd:" + curCmd);
		   if (!curCmd) return;
		   var lastToken = curTokens[curTokens.length-1];
		   console.log("lastToken:" + lastToken + "**" + lastToken.match(/^-/));
		   
		   // var candidates = [];
		   return callback(null, wordList);
	  	}
	 }
	 langTools.addCompleter(icc2Completer);    
     */
     
    var wordList = [{caption: "getParent", value: "getParent", score: 300, meta: "cws function"}];
	var cwsCompleter = {
		getCompletions: function(editor, session, pos, prefix, callback) {
		   var curLine = session.getDocument().getLine(pos.row);
		   // 用空格切分
		   var curTokens = curLine.slice(0, pos.column).split(/\s+/);
		   var lastToken = curTokens[curTokens.length-1];
		   var p = lastToken.lastIndexOf(".");
		   if (p==-1) {
		   	return;
		   }
		   // 取句点前面的变量
		   lastToken = lastToken.substring(0, p);
		   
		   // 取变量对应的类名
		   var reg = eval("/([0-9a-zA-Z_-]+?)( +)" + lastToken + "/gi");
		   reg.test(editor.getValue());
		   var clsName = RegExp.$1;
		   // console.log("RegExp.$1=" + RegExp.$1);

           console.log("clsName=" + clsName);	   
           clsName = getClassName(clsName);
           console.log("clsName2=" + clsName);	   
           
		   $.ajax({
				type: "post",
				url: "script_do.jsp",
				data: {
					op: "getMethods",
					// importSection: importStr,
					pkgName: clsName
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
				},
				success: function(data, status){
					data = $.parseJSON(data);
		                  callback(null, data.map(function(ea) {  
		                      return {caption: ea.caption, value: ea.value, score: ea.score, meta: ea.meta}  
		                  }));
				},
				complete: function(XMLHttpRequest, status){
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
				}
			});
	  	}
	 }
	 langTools.addCompleter(cwsCompleter);   
	 
	 var importSectionOld = "";
	 var classJsonArr; // JSON数组，存放所有包面的类名，按是否显式指排序
	 
	editor.getSession().on('change', function(e) {
		/*
		editor.getSession().setAnnotations([{
		    row: 0,
		    column: 10,
		    text: "some error",
		    type: "error" // also warning and information
		}]);
		*/

	    var curLine = editor.getSession().getDocument().getLine(e.start.row);
	    // console.log(curLine);
	   
        // 取出import部分
        var content = editor.getValue();
        var lastImport = content.lastIndexOf("import ");
        var lastReturn = content.indexOf("\n", lastImport);
        var importStr = content.substring(0, lastReturn);	    
	    // console.log("importStr=" + importStr + " importSectionOld=" + importSectionOld);
	    // 判断import是否有变化
	    if (importStr!=importSectionOld && false) {
	    	importSectionOld = importStr;
	    	// console.log("importSectionOld=" + importStr);
	    	
			$.ajax({
					type: "post",
					url: "script_do.jsp",
					data: {
						op: "getAllClasses",
						importSection: importStr
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
					},
					success: function(data, status){
						try {
							classJsonArr = $.parseJSON(data);
							console.log("classJsonArr=" + classJsonArr);
					    }
					    catch (e) {alert(e)}						
					},
					complete: function(XMLHttpRequest, status){
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
					}
				});		    	
	    }
	});

	function getClassName(prefix) {
		if (prefix=="String") {
			return "java.lang.String";
		}
		else if (prefix=="Vector" || prefix=="Iterator" || prefix=="List") {
			return "java.util." + prefix;
		}
		
		var fullName = "";
		if (classJsonArr) {
			$.each(classJsonArr, function (i, obj) {
			    if (obj.name==prefix) {
			    	fullName = obj.fullName;
			    	return;
			    }
			});
		}
		return fullName;
	}

-->
</script>
</body>
<script>
$(function() {
	if (window.top.opener) {
		try {
			editor.setValue(window.top.opener.getScript());
		}
		catch(e) {}
	}
	
	$('#theme').change(function() {
		window.location.href = "script_main.jsp?op=changeTheme&theme=" + $('#theme').val();
	});
});

function ok() {
	if (window.top.opener) {
		window.top.opener.setScript(editor.getValue());
	}
	window.top.close();
}

function insertScript(script) {
	// editor.setValue(editor.getValue() + script);
	editor.insert(script);
	window.focus();
}

var newWin;
function run() {
	o("myscript").value = editor.getValue();
	if (!newWin || newWin.closed)
		newWin = openWin("script_run.jsp", 640, 480);
	else {
		newWin.focus();
		newWin.run();
	}
	// alert(newWin.name);
	// $("#formRun").attr("target", newWin.name);
	// o("formRun").submit();
}

function getScript() {
	return editor.getValue();
}
</script>
</html>