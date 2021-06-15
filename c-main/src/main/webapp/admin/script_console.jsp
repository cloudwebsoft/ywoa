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
<%@ page import="bsh.EvalError"%>
<%@ page import="bsh.Interpreter"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>控制台</title>
<link type="text/css" rel="stylesheet" href="../images/ide/ide.css" />

<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body style="font-size:12px; line-height:1.5">
<div class="taskbar">
	<div class="taskbar-spacer"></div>
	<div id="btnClear" class="console-btn" title="清空">
		<img src="../images/ide/btn-clear.png" />
	</div>
</div>
<div id="info" class="info">

</div>

<div id="note" class="note">
&nbsp;正在编译中...
</div>

<div id="run" class="run">
&nbsp;正在运行中...
</div>
</body>
<style>

</style>
<script>
function clear() {
	$('#info').html('');
}

function showResult(msg) {
	$('#info').append("<div class='result'>" + msg + "</div>");
}

function showCompileNote(isShow) {
	if (isShow) {
		$('#note').show();
	}
	else {
		$('#note').hide();	
	}
}

function showRunNote(isShow) {
	if (isShow) {
		$('#run').show();
	}
	else {
		$('#run').hide();	
	}
}

var lineNum = 0;
function insertErrMsg(item) {
	lineNum++;
	
	var num = item.num;
	var err = item.err;
	var desc = item.desc;
	var col = item.col;
	var varStr = item.var;
	var strDiv = "<span class='num'>" + num + "</span>";
	strDiv += "<span class='err'>" + err + "</span>";
	strDiv += "<span class='desc'>" + desc + "</span>";
	
	strDiv = "<div class='msg' id='err" + lineNum + "' num='" + num + "' col='" + col + "' var='" + varStr + "'>" + strDiv + "</div>";
	$('#info').append(strDiv);
	
	$('#err' + lineNum).dblclick(function() {
		// alert($(this).html());
		
		var editor = window.top.mainScriptFrame.getEditor();
		editor.gotoLine($(this).attr('num'));
		
		if (varStr!=null && varStr!="") {
			editor.find(varStr,{
			    backwards: false,
			    wrap: false,
			    caseSensitive: false,
			    wholeWord: false,
			    regExp: false
			});
		}
	});
	
	$('#err' + lineNum).mouseout( function() {
		$(this).parent().find("div").each(function(i){ $(this).removeClass("msg-over"); });
	});  
	
	$('#err' + lineNum).mouseover( function() {
		$(this).addClass("msg-over"); 
	}); 	
}

$(function() {
	$('.console-btn').mouseout(function() {
		$(this).removeClass("console-btn-over");
	});
	
	$('.console-btn').mouseover(function() {
		$(this).addClass("console-btn-over");
	});	
	
	$('#btnClear').click(function() {
		$('#info').html("");
	});
});
</script>
</html>