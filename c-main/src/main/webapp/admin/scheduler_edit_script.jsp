<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.flow.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.ui.*,
				 com.cloudwebsoft.framework.base.*"
%>
<%@ page import="org.json.JSONObject" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv="admin";
    if (!privilege.isUserPrivValid(request,priv)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    
    String op = ParamUtil.get(request, "op");
    
    int id = ParamUtil.getInt(request, "id");
    
    JobUnitDb ju = new JobUnitDb();
    ju = (JobUnitDb)ju.getQObjectDb(new Integer(id));
    if (op.equals("edit")) {
        JSONObject json = new JSONObject();
        QObjectMgr qom = new QObjectMgr();
        try {
            if (qom.save(request, ju, "scheduler_edit")) {
                json.put("ret", 1);
                json.put("msg", "操作成功");
            }
            else {
                json.put("ret", 0);
                json.put("msg", "操作失败");
            }
        }
        catch (ErrMsgException e) {
            json.put("ret", 0);
            json.put("msg", e.getMessage());
        }
        out.print(json.toString());
        return;
    }
    
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style type="text/css" media="screen">
	#editor { 
		padding:10px;
		text-align:left;
        height:500px;
    }
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/ace-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script>
    function SelectDateTime(objName) {
        var dt = openWin("../util/calendar/time.htm", "266px", "185px");
    }

    function sel(dt) {
        if (dt != null)
            o("time").value = dt;
    }

    function openWin(url, width, height) {
        var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width=" + width + ",height=" + height);
        return newwin;
    }

    function trimOptionText(strValue) {
        // 注意option中有全角的空格，所以不直接用trim
        var r = strValue.replace(/^　*|\s*|\s*$/g, "");
        return r;
    }
</script>
</head>
<body>
<%@ include file="scheduler_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<form action="?op=edit" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<table width="98%" border="0" class="tabStyle_1 percent80">
    <tr>
      <td align="left" class="tabStyle_1_title">调度脚本</td>
    </tr>
    <tr>
      <td align="left">
    名称
    <input name="job_name" value="<%=ju.getString("job_name")%>" />
    &nbsp;
    <span id="spanKind">
    类型：
	<input id="kind0" name="kind" value="0" type="radio" checked />按日期
	<input id="kind1" name="kind" value="1" type="radio" />按星期
	<input id="kind2" name="kind" value="2" type="radio" />按间隔
	<input id="kind3" name="kind" value="3" type="radio" />按cron表达式
    </span>
      </td>
    </tr>
    <tr>
      <td align="left">
      <div id="div0" style="display:none">      
      开始时间：
<%
String cron = ju.getString("cron");
String[] ary = cron.split(" ");
if (ary[0].length()==1)
	ary[0] = "0" + ary[0];
if (ary[1].length()==1)
	ary[1] = "0" + ary[1];
if (ary[2].length()==1)
	ary[2] = "0" + ary[2];

String interval = "", intervalType = "";
int p = ary[0].indexOf("/");
if (p!=-1) {
	interval = ary[0].substring(p+1);
	intervalType = "s";
}
p = ary[1].indexOf("/");
if (p!=-1) {
	interval = ary[1].substring(p+1);
	intervalType = "m";
}
p = ary[2].indexOf("/");
if (p!=-1) {
	interval = ary[2].substring(p+1);
	intervalType = "h";
}
p = ary[3].indexOf("/");
if (p!=-1) {
	interval = ary[3].substring(p+1);
	intervalType = "d";
}

String monthDay = StrUtil.getNullStr(ju.getString("month_day"));

String t = "";
if (intervalType.equals("")) {
	t = ary[2] + ":" + ary[1] + ":" + ary[0];
}
%>
        <input style="WIDTH: 60px" id="time" name="time" size="20" value="<%=t%>" />
        <span id="spanMon">              
      	每月
        <input id="month_day" name="month_day" size="2" value="<%=monthDay%>" />
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
		<%
        String[] w = ary[5].split(",");
        for (int i=0; i<w.length; i++) {
        %>
        <script>
        setCheckboxChecked("weekDay", "<%=w[i]%>");
        </script>
        <%
        }
        %>
        </span>
        </div>
        
        <div id="divInterval" style="display:none">
        每隔
        <input id="interval" name="interval" size="3" value="<%=interval%>" />
        <input type="radio" id="intervalType" name="intervalType" value="d" />天
        <input type="radio" id="intervalType" name="intervalType" value="h" />时
        <input type="radio" id="intervalType" name="intervalType" value="m" />分
        <input type="radio" id="intervalType" name="intervalType" value="s" />秒
        <%
		if (!intervalType.equals("")) {
			%>
			<script>
			setRadioValue("intervalType", "<%=intervalType%>");
			</script>
			<%
		}
		%>
        </div>
        <div id="divCron" style="display:none">
        cron表达式
        <input id="mycron" name="mycron" value="<%=cron%>" />
        </div>        
        
	</td>
    </tr>
    <tr>
      <td align="center">
          <div id="editor"></div>
          <textarea id="editorScript" style="display: none"><%=StrUtil.getNullStr(ju.getString("data_map"))%></textarea>
      </td>
    </tr>
    <tr>
      <td align="center">
	  <input type="button" value="设计" class="btn btn-default" onclick="openIdeWin()" />
      &nbsp;&nbsp;
	  <input type="button" value="运行" class="btn btn-default" onclick="run();" />
      &nbsp;&nbsp;      
      <input class="btn" name="submit" type="submit" value="确定" />
      <input name="id" type="hidden" value="<%=id%>" />
      <input name="job_class" type="hidden" value="com.redmoon.oa.job.BeanShellScriptJob" />
      <input name="cron" type="hidden" />
      <input name="data_map" type="hidden" />      
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" /></td>
    </tr>
</table>
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
        jAlert("名称不能为空！", "提示");
		o("job_name").focus();
		return false;
	}
	var t = form1.time.value;	
	var ary = t.split(":");
	if (ary.length>=3) {
		if (ary[2].indexOf("0")==0 && ary[2].length>1)
			ary[2] = ary[2].substring(1, ary[2].length);
	}
	if (ary.length>=2) {
		if (ary[1].indexOf("0")==0 && ary[1].length>1)
			ary[1] = ary[1].substring(1, ary[1].length);
	}
	if (ary.length>=1) {
		if (ary[0].indexOf("0")==0 && ary[0].length>1)
			ary[0] = ary[0].substring(1, ary[0].length);
	}
		
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
		o("cron").value = cron;
	}
	else if (kind=="1") {
		if (weekDay=="") {
            jAlert("请选择星期几！", "提示");
			return false;
		}
		if (dayOfMonth=="")
			dayOfMonth = "?";
		var cron = ary[2] + " " + ary[1] + " " + ary[0] + " ? * " + weekDay;
		o("cron").value = cron;
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
		o("cron").value = cron;
	}
	else if (kind=="3") {
		if (o("mycron").value=="") {
            jAlert("请输入cron表达式！", "提示");
			o("mycron").focus();
			return false;
		}
		o("cron").value = o("mycron").value;
	}
	o("data_map").value = editor.getValue();

    $.ajax({
        url: "scheduler_edit_script.jsp?op=edit",
        type: "post",
        dataType: "json",
        contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
        data: $('#form1').serialize(),
        beforeSend: function (XMLHttpRequest) {
            $('body').showLoading();
        },
        success: function(data, status){
            jAlert(data.msg, "提示");
        },
        complete: function (XMLHttpRequest, status) {
            $('body').hideLoading();
        },
        error: function(XMLHttpRequest, textStatus){
            alert(XMLHttpRequest.responseText);
        }
    });
	
	return false;
}

$(function() {
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
			
			$("#month_day").val("");			
		}
		else if ($(this).val()=="3") {
			$("#div0").hide();
			$("#divInterval").hide();
			$("#divCron").show();
		}
	});
	
	<%if (!monthDay.equals("")) {%>
		$("#div0").show();
		$("#spanMon").show();
		$("#spanWeek").hide();
		$("#divInterval").hide();
		$("#divCron").hide();
		
		setRadioValue("kind", "0");
	<%}else if (!w[0].equals("?")) {%>
		$("#div0").show();
		$("#spanMon").hide();
		$("#spanWeek").show();
		$("#divInterval").hide();
		$("#divCron").hide();
		
		setRadioValue("kind", "1");
	<%}else if (!interval.equals("")) {%>
		$("#div0").hide();
		$("#spanMon").hide();
		$("#spanWeek").hide();
		$("#divInterval").show();
		$("#divCron").hide();
		
		setRadioValue("kind", "2");
	<%}%>
});

<%
    com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
    com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
    String version = StrUtil.getNullStr(oaCfg.get("version"));
    String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>
var ideUrl = "script_frame.jsp";
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
            ideUrl = "script_frame.jsp?cwsToken=" + cwsToken;
        }
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
	} else if (type == "setCwsToken") {
        cwsToken = d.cwsToken;
        ideUrl = "script_frame.jsp?cwsToken=" + cwsToken;
    }
}

$(function() {
    // 20210307 可能因为脚本超长或格式问题，editor中只能显示前半段，改为通过setScript显示
    setScript($('#editorScript').val());

	$('#time').datetimepicker({
     	lang:'ch',
     	datepicker:false,
     	format:'H:i:00',
     	step:1
	});
	
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