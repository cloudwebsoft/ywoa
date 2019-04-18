<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.flow.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.job.*,
				 com.redmoon.oa.ui.*,
				 com.cloudwebsoft.framework.base.*"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>调度脚本-添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style type="text/css" media="screen">
/*
    #editor { 
        position: absolute;
        top: 64px;
        right: 0;
        bottom: 0;
        left: 0;
    }
*/
	#editor { 
		padding:10px;
		text-align:left;
        height:500px;
    }
</style>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}
function SelectDateTime(objName) {
	var dt = openWin("../util/calendar/time.htm","266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt) {
    if (dt!=null)
        findObj("time").value = dt;
}
function openWin(url,width,height,divId)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
  return newwin;
}
function trimOptionText(strValue) 
{
	// 注意option中有全角的空格，所以不直接用trim
	var r = strValue.replace(/^　*|\s*|\s*$/g,"");
	return r;
}

</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	JobUnitDb ju = new JobUnitDb();
	try {
	if (qom.create(request, ju, "scheduler_add")) {
		SchedulerManager sm = SchedulerManager.getInstance();
		sm.shutdown(); // 结束调度
		sm.startWhenIsShutdown(); // 重启调度			
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "scheduler_list.jsp"));
	}
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
%>
<%@ include file="scheduler_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<form action="?op=add" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<div style="text-align:center; margin:0px; padding:1px">
<table width="94%" border="0" align="center" class="tabStyle_1 percent80">
    <tr>
      <td class="tabStyle_1_title" align="left"><strong>调度脚本</strong></td>
    </tr>
    <tr>
      <td align="left">
    名称：
    <input name="job_name" />
    &nbsp;
    <span id="spanKind">
    类型：
	<input id="kind" name="kind" value="0" type="radio" checked />按日期
	<input id="kind" name="kind" value="1" type="radio" />按星期
	<input id="kind" name="kind" value="2" type="radio" />按间隔
	<input id="kind" name="kind" value="3" type="radio" />按cron表达式
    </span>    
    </td>
    </tr>
    <tr>
      <td align="left">
      <div id="div0">
      	开始时间：
        <input style="WIDTH: 70px" value="12:00:00" id="time" name="time" size="20" readonly="readonly" />
        <!-- &nbsp;<img style="CURSOR: hand" onclick="SelectDateTime('time')" src="../images/form/clock.gif" align="absmiddle" width="18" height="18" /> -->
        <span id="spanMon">
        每月
        <input id="month_day" name="month_day" size="2" />
        号
        </span>
        <span id="spanWeek" style="display:none">
        <input name="weekDay" type="checkbox" value="1" />
        星期日
        <input name="weekDay" type="checkbox" value="2" />
        星期一
        <input name="weekDay" type="checkbox" value="3" />
        星期二
        <input name="weekDay" type="checkbox" value="4" />
        星期三
        <input name="weekDay" type="checkbox" value="5" />
        星期四
        <input name="weekDay" type="checkbox" value="6" />
        星期五
        <input name="weekDay" type="checkbox" value="7" />
        星期六
        </span>
      </div>
      <div id="divInterval" style="display:none">
      每隔
      <input id="interval" name="interval" size="3" />
      <input type="radio" id="intervalType" name="intervalType" value="d" />天
      <input type="radio" id="intervalType" name="intervalType" value="h" />时
      <input type="radio" id="intervalType" name="intervalType" value="m" />分
      <input type="radio" id="intervalType" name="intervalType" value="s" />秒
      </div>
      <div id="divCron" style="display:none">
      cron表达式
      <input id="mycron" name="mycron" />
      </div>        
      </td>
    </tr>
    <tr>
      <td align="center">
        <pre id="editor"></pre>
      </td>
    </tr>
    <tr>
      <td align="center">
      <%
		String url = "script_frame.jsp";
	  %>
	  <input type="button" value="设计器" class="btn" onclick="ideWin=openWinMax('<%=url%>');" />
      &nbsp;&nbsp;
	  <input type="button" value="运行" class="btn" onclick="run();" />
      &nbsp;&nbsp;      
      <input name="submit" class="btn" type="submit" value="确定" />
        <input name="cron" type="hidden" />
        <input name="data_map" type="hidden" />
        <input name="job_class" type="hidden" value="com.redmoon.oa.job.BeanShellScriptJob" />
        <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />      
      </td>
    </tr>
</table>
</div>
</form>
</body>
<script>
var newWin;
function run() {
	if (!newWin || newWin.closed)
		newWin = openWin("script_run.jsp", 640, 480);
	else {
		newWin.focus();
		newWin.run();
	}
}

function getScript() {
	return editor.getValue();
}

function setScript(script) {
	editor.setValue(script);
}

var editor = ace.edit("editor");
// editor.setTheme("ace/theme/eclipse");
editor.setTheme("<%=cfg.get("aceTheme")%>");
editor.getSession().setMode("ace/mode/java");

editor.setOptions({
	readOnly: true,
	highlightActiveLine: true,
	highlightGutterLine: true
})
editor.renderer.$cursorLayer.element.style.opacity=0;
	
function form1_onsubmit() {
	if (o("job_name").value=="") {
		alert("名称不能为空！");
		o("job_name").focus();
		return false;
	}
	var t = form1.time.value;
	var ary = t.split(":");
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = form1.month_day.value;
	var kind = getRadioValue("kind");
	if (kind=="0") {
		if (dayOfMonth=="") {
			alert("请填写每月几号！");
			o("month_day").focus();
			return false;
		}
		if (dayOfMonth!="" && (parseInt(dayOfMonth)>31 || parseInt(dayOfMonth)<0)) {
			alert("每月天数不能小于0，且不能大于31");
			return false;
		}
		if (dayOfMonth=="")
			dayOfMonth = "?";
		var cron = ary[2] + " " + ary[1] + " " + ary[0] + " " + dayOfMonth + " * ?";
		form1.cron.value = cron;
	}
	else if (kind=="1") {
		if (weekDay=="") {
			alert("请选择星期几！");
			return false;
		}
		if (dayOfMonth=="")
			dayOfMonth = "?";
		var cron = ary[2] + " " + ary[1] + " " + ary[0] + " ? * " + weekDay;
		form1.cron.value = cron;
	}
	else if (kind=="2") {
		var type = getRadioValue("intervalType");
		if (type=="") {
			alert("请选择天、时、分或秒！");
			return false;
		}
		var val = o("interval").value;
		if (val=="") {
			alert("请输入时间间隔！");
			o("interval").focus();
			return false;
		}
		var cron = "";
		if (type=="s") {
			cron = "0/" + val + " * * * * ?";
		}
		else if (type=="m") {
			cron = "* 0/" + val + " * * * ?";
		}
		else if (type=="h") {
			cron = "* * 0/" + val + " * * ?";
		}
		else if (type=="d") {
			cron = "* * * 0/" + val + " * ?";
		}
		form1.cron.value = cron;
	}
	else if (kind=="3") {
		if (o("mycron").value=="") {
			alert("请输入cron表达式！");
			o("mycron").focus();
			return false;
		}
		o("cron").value = o("mycron").value;
	}
	
	form1.data_map.value = editor.getValue();
}	

$(function() {
	$('#time').datetimepicker({
     	lang:'ch',
     	datepicker:false,
     	format:'H:i:00',
     	step:1
	});
	$("#spanKind :radio").click(function() {
		if ($(this).val()=="0") {
			$("#div0").show();
			$("#divInterval").hide();
			$("#divCron").hide();
			
			$("#spanMon").show();
			$("#spanWeek").hide();
			$("input[name='weekDay']").each(function() {
				$(this).attr("checked", false);
			});
		}
		if ($(this).val()=="1") {
			$("#div0").show();
			$("#divInterval").hide();
			$("#divCron").hide();
			
			$("#spanMon").hide();
			$("#spanWeek").show();
			
			$("#month_day").val("");
		}		
		else if ($(this).val()=="2") {
			$("#div0").hide();
			$("#divInterval").show();
			$("#divCron").hide();
		}
		else if ($(this).val()=="3") {
			$("#div0").hide();
			$("#divInterval").hide();
			$("#divCron").show();
		}
	});
});

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
		    "scene":"scheduler.script",		    
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
</html>