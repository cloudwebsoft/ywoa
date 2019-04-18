<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "org.jawin.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
long workplanId = ParamUtil.getLong(request, "id");
String flag = ParamUtil.get(request, "flag");
com.redmoon.oa.workplan.Privilege wppvg = new com.redmoon.oa.workplan.Privilege();
boolean canManage = wppvg.canUserManageWorkPlan(request, (int)workplanId);
WorkPlanDb wpd = new WorkPlanDb();
wpd = wpd.getWorkPlanDb((int)workplanId);
String priv = "workplan";
String flowId = ParamUtil.get(request,"flowId");
String action = ParamUtil.get(request,"action");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}
String op = request.getParameter("op");
if ("import".equals(op))
{
	try
	{
		WorkPlanTaskMgr workPlanTaskMgr = new WorkPlanTaskMgr();
		workPlanTaskMgr.importProject(application, request, workplanId);
	}
	catch(ErrMsgException e)
	{
		 out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));     
		 return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=9; IE=8; IE=7; IE=EDGE"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <title>甘特图</title>

  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/core.css" />
 

  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

  <link rel=stylesheet href="../js/jQueryGantt/platform.css" type="text/css">
  <link rel=stylesheet href="../js/jQueryGantt/libs/dateField/jquery.dateField.css" type="text/css">
  <link rel=stylesheet href="../js/jQueryGantt/gantt.css" type="text/css">
  <style>
  .spacerH {
	  height:0px;
  }
  </style>
  <script src="../inc/common.js"></script>
  <script src="../js/jquery.js"></script>

  <script src="../js/jquery-ui/jquery-ui.js"></script>
  
  <script src="../js/jQueryGantt/libs/jquery.livequery.min.js"></script>
  <script src="../js/jQueryGantt/libs/jquery.timers.js"></script>
  <script src="../js/jQueryGantt/libs/platform.js"></script>
  <script src="../js/jQueryGantt/libs/date.js"></script>
  <script src="../js/jQueryGantt/libs/i18nJs.js"></script>
  <script src="../js/jQueryGantt/libs/dateField/jquery.dateField.js"></script>
  <script src="../js/jQueryGantt/libs/JST/jquery.JST.js"></script>
  <script src="../js/ajaxfileupload.js" type="text/javascript"></script>

  <script src="../js/jQueryGantt/ganttUtilities.js"></script>
  <script src="../js/jQueryGantt/ganttTask.js"></script>
  <script src="../js/jQueryGantt/ganttDrawer.js"></script>
  <script src="../js/jQueryGantt/ganttGridEditor.js"></script>
  <script src="../js/jQueryGantt/ganttMaster.js"></script>

  <script src="../js/tabpanel/Toolbar.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_workplan_gantt.css" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<link href="<%=SkinMgr.getSkinPath(request)%>/main.css" rel="stylesheet" type="text/css" />
<script>

<%
// 取得计划开始之日至今所有的假日
int curYear = DateUtil.getYear(new java.util.Date());
OACalendarDb oacal = new OACalendarDb();
String holidays = "#";
Iterator ir = oacal.getHolidays(DateUtil.getYear(wpd.getBeginDate())-1, curYear + 1).iterator();
while (ir.hasNext()) {
	oacal = (OACalendarDb)ir.next();
	holidays += DateUtil.format(oacal.getDate("oa_date"), "yyyy_MM_dd") + "#";
}
String satWorkingDays = "#";
ir = oacal.getSatdaysIsWorking(DateUtil.getYear(wpd.getBeginDate())-1, curYear + 1).iterator();
while (ir.hasNext()) {
	oacal = (OACalendarDb)ir.next();
	satWorkingDays += DateUtil.format(oacal.getDate("oa_date"), "yyyy_MM_dd") + "#";
}
String sunWorkingDays = "#";
ir = oacal.getSundaysIsWorking(DateUtil.getYear(wpd.getBeginDate())-1, curYear + 1).iterator();
while (ir.hasNext()) {
	oacal = (OACalendarDb)ir.next();
	sunWorkingDays += DateUtil.format(oacal.getDate("oa_date"), "yyyy_MM_dd") + "#";
}
%>

i18nJsHolidays = "<%=holidays%>";
i18nSatWorkingDays = "<%=satWorkingDays%>";
i18nSunWorkingDays = "<%=sunWorkingDays%>";

// console.log("i18nSatWorkingDays=" + i18nSatWorkingDays);

var ge;  //this is the hugly but very friendly global var for the gantt editor
var prj;
$(document).ready(function(){
    $("#importDiv").dialog({
        autoOpen : false,
        modal: true,
        title : "文件导入",
        closeText : "关闭",
        buttons: {
                    "取消": function() {
                        $(this).dialog("close");
                    },
                    "确定": function() {
                        var path = "?op=import&action=<%=action%>&id=<%=workplanId%>";
                        
                        $("#importFrm").attr("action",path);
                        $("#importFrm").submit();
                        $(this).dialog("close");
                    }
                }
    });
   $("#remindDlg").dialog({
            autoOpen:false,
            title:"提示",
            modal: true,
            bgiframe: true,
            width: 320,
            height: 220,
            // bgiframe:true,
            buttons: {
                "取消": function() {
                    $(this).dialog("close");
                },
                "确定": function() {
                    if ($('#reason').val()=="") {
                        jAlert("请填写原因！","提示");
                        return;
                    }
                    var reason = $('#reason').val();

                    $(this).dialog("close");
                      
                      $.ajax({
                          type: "post",
                          url: "workplan_gantt_do.jsp",
                          data: {
                              op: "gantt",
                              workplanId: "<%=workplanId%>",
                              data: JSON.stringify(prj),
                              reason: reason
                          },
                          dataType: "html",
                          beforeSend: function(XMLHttpRequest){
                              $('#workSpace').showLoading();
                          },
                          success: function(data, status){
                              data = $.parseJSON(data);
                              if (data.ret=="0") {
                                  jAlert(data.msg, "提示");
                              }
                              else {
                                  jAlert_Redirect(data.msg, "提示", "workplan_gantt.jsp?id=<%=workplanId%>&flag=<%=flag%>");
                              }
                          },
                          complete: function(XMLHttpRequest, status){
                              $('#workSpace').hideLoading();                
                          },
                          error: function(XMLHttpRequest, textStatus){
                              // 请求出错处理
                              jAlert(XMLHttpRequest.responseText,"提示");
                          }
                      });
                                        
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable:true
            }); 

  var toolbar = new Toolbar({
    renderTo : 'toolbar',
    //border: 'top',
    items : [{
      type : 'button',
      text : '保存',
      bodyStyle : 'save',
      useable : 'T',
      handler : function(){
		saveGanttOnServer();
      }
    },{
      type : 'button',
      text : '撤销',
      bodyStyle : 'undo',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('undo.gantt');
      }
    },{
      type : 'button',
      text : '恢复',
      bodyStyle : 'redo',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('redo.gantt');
      }
    },{
      type : 'button',
      text : '插入上方',
      bodyStyle : 'insert_above',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('addAboveCurrentTask.gantt');
      }
    },{
      type : 'button',
      text : '插入下方',
      bodyStyle : 'insert_below',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('addBelowCurrentTask.gantt');
      }
    },{
      type : 'button',
      text : '左移',
      bodyStyle : 'outdent',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('outdentCurrentTask.gantt');
      }
    },{
      type : 'button',
      text : '右移',
      bodyStyle : 'indent',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('indentCurrentTask.gantt');
      }
    },{
      type : 'button',
      text : '上移',
      bodyStyle : 'moveup',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('moveUpCurrentTask.gantt');
      }
    },{
      type : 'button',
      text : '下移',
      bodyStyle : 'movedown',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('moveDownCurrentTask.gantt');
      }
    },{
      type : 'button',
      text : '放大',
      bodyStyle : 'zoom_in',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('zoomPlus.gantt');
      }
    },{
      type : 'button',
      text : '缩小',
      bodyStyle : 'zoom_out',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('zoomMinus.gantt');
      }
    },{
      type : 'button',
      text : '删除',
      bodyStyle : 'dustbin',
      useable : 'T',
      handler : function(){
		$('#workSpace').trigger('deleteCurrentTask.gantt');
      }
    }
	<%
	int exportFlag = 1;
	DispatchPtr app = null;
	try {
		app = new DispatchPtr("MSProject.Application");
	}
	catch (NoClassDefFoundError e) {
		exportFlag = 0;
	}
	catch (UnsatisfiedLinkError e) {
		exportFlag = -1;
	}
	catch(Exception e) {
		exportFlag = 0;
	}
	
	%>
	,{
      type : 'button',
      text : '导出',
      bodyStyle : 'export',
      useable : 'T',
      handler : function(){
		<%if (exportFlag==1) {%>
		window.open('workplan_gantt_export.jsp?workplanId=<%=workplanId%>');
		<%}else if (exportFlag==0) {%>
		//jAlert("请检查jawin环境配置！","提示");
		jAlert("请在服务器端安装ms project！","提示");
		<%}else if (exportFlag==-1) {%>
		jAlert("请在服务器端安装ms project！","提示");
		<%}%>
      }
	}
	,{
      type : 'button',
      text : '导入',
      bodyStyle : 'import',
      useable : 'T',
      handler : function(){
		showImport();
      }
	},{
      type : 'button',
      text : '模板下载',
      bodyStyle : 'importModel',
      useable : 'T',
      handler : function(){
        downModel("workplan_gantt_template.jsp");
      }
    },{
      type : 'button',
      text : '清空',
      bodyStyle : 'clearGantt',
      useable : 'T',
      handler : function(){
		clearGantt();
      }
	},{
      type : 'button',
      text : '全屏',
      bodyStyle : 'fullscreen',
      useable : 'T',
      handler : function(){
		openWin('workplan_gantt.jsp?id=<%=workplanId%>&flag=fullscreen', screen.width, screen.height);
      }
	}
	
	]
  });

  toolbar.render();
  //load templates
  $("#ganttemplates").loadTemplates();

  // here starts gantt initialization
  ge = new GanttMaster();
  var workSpace = $("#workSpace");
  workSpace.css({width:$(window).width() + 600, height:$(window).height() - 100});
  //workSpace.css({width:$(window).width(), height:$(window).height() - 80});
  ge.init(workSpace);

  //inject some buttons (for this demo only)
  <%if (canManage) {%>
  $(".ganttButtonBar div").append("<button onclick=\"window.open('workplan_gantt_export.jsp?workplanId=<%=workplanId%>');\" class='button'>导出</button>")
          .append("<button onclick='clearGantt();' class='button'>清空</button>")
          .append("<button onclick=\"openWin('workplan_gantt.jsp?id=<%=workplanId%>&flag=fullscreen', screen.width, screen.height);\" class='button'>全屏</button>")
          .append("&nbsp;&nbsp;");
          
          //.append("<button onclick='getFile();' class='button'>导出</button>");
  <%}%>
  $(".ganttButtonBar h1").html("");//<img src='twGanttSmall.png'>");
  $(".ganttButtonBar div").addClass('buttons');
  //overwrite with localized ones
  loadI18n();

  //simulate a data load from a server.
  loadGanttFromServer();

  //fill default Teamwork roles if any
  if (!ge.roles || ge.roles.length == 0) {
    setRoles();
  }

  //fill default Resources roles if any
  if (!ge.resources || ge.resources.length == 0) {
    setResource();
  }
   
});


$(function() {

  
   

});
 
function initGantt() {
  var workSpace = $("#workSpace");
  workSpace.css({width:$(window).width(), height:$(window).height() - 60});
  // loadGanttFromServer();
}
function showImport()
{
	$("#importDiv").dialog("open");
}
function downModel(url)
{
    //window.location.href="template/project_template.mpp";
    
    window.open(url);
    
} 
</script>
</head>
<body style="background-color: #fff;" onresize="initGantt()">
<%
if (!flag.equals("fullscreen")) {
%>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
//o("menu2").className="gantt_tab_sel";
o("menu2").className="current";
</script>
<div class="spacerH" style="padding:0px"></div>
<%}%>
<div id="toolbar" style="height:38px; margin-bottom:0px"></div>

<div id="workSpace" style="padding:0px; overflow-y:auto; overflow-x:hidden;border:1px solid #e5e5e5;position:relative;margin:0px"></div>

<div id="taZone" style="display:none;">
  <textarea rows="8" cols="150" id="ta">
  	<%
	String jsonStr = wpd.getGantt();
	if (!jsonStr.equals("")) {
		JSONObject json = new JSONObject(jsonStr);
		if (!canManage) {
			json.put("canWrite", false);
		}
		else {
			json.put("canWrite", true);
		}
		out.print(json.toString());
	}
	else {
		int dur = 0;
		try {
			dur = oacal.getWorkDayCount(wpd.getBeginDate(), wpd.getEndDate());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));			
			return;
		}
	%>
    {"tasks":[{"id":-1,"name":"<%=wpd.getTitle()%>","code":"","level":0,"status":"STATUS_ACTIVE","start":<%=DateUtil.toLong(wpd.getBeginDate())%>,"duration":<%=dur%>,"end":<%=DateUtil.toLong(wpd.getEndDate())%>,"startIsMilestone":true,"endIsMilestone":false,"assigs":[]}],"selectedRow":0,"deletedTaskIds":[],"canWrite":true,"canWriteOnParent":false }
	<%
	}
	%>
  </textarea>
  <button onclick="loadGanttFromServer();">load</button>
</div>

<style>
  .resEdit {
    padding: 15px;
  }

  .resLine {
    width: 95%;
    padding: 3px;
    margin: 5px;
    border: 1px solid #d0d0d0;
  }

  body {
    overflow: hidden;
  }
</style>

<form id="gimmeBack" style="display:none;" action="../gimmeBack.jsp" method="post" target="_blank"><input type="hidden" name="prj" id="gimBaPrj"></form>

<script type="text/javascript">




function loadGanttFromServer(taskId, callback) {

  //this is a simulation: load data from the local storage if you have already played with the demo or a textarea with starting demo data
  loadFromLocalStorage();

  //this is the real implementation
  /*//var taskId = $("#taskSelector").val();
   var prof = new Profiler("loadServerSide");
   prof.reset();

   $.getJSON("ganttAjaxController.jsp", {CM:"LOADPROJECT",taskId:taskId}, function(response) {
   //console.debug(response);
   if (response.ok) {
   prof.stop();
   ge.loadProject(response.project);
   ge.checkpoint(); //empty the undo stack
   if (typeof(callback)=="function")
   callback(response);
   }else {
   jsonErrorHandling(response);
   }
   });*/
}


function saveGanttOnServer() {

  //this is a simulation: save data to the local storage or to the textarea
  saveInLocalStorage();
  
  if (window.opener) {
  	window.opener.location.reload();
  }

  /*var prj = ge.saveProject();
   delete prj.resources;
   delete prj.roles;
   var prof = new Profiler("saveServerSide");
   prof.reset();

   if (ge.deletedTaskIds.length>0){
   if (!confirm("TASK_THAT_WILL_BE_REMOVED\n"+ge.deletedTaskIds.length))
   return;
   }

   $.ajax("ganttAjaxController.jsp", {
   dataType:"json",
   data: {CM:"SVPROJECT",prj:JSON.stringify(prj)},
   type:"POST",

   success: function(response) {
   if (response.ok) {
   prof.stop();
   if (response.project){
   ge.loadProject(response.project); //must reload as "tmp_" ids are now the good ones
   } else {
   ge.reset();
   }
   } else {
   var errMsg="Errors saving project\n";
   if (response.message)
   errMsg=errMsg+response.message+"\n";

   for (var i=0;i<response.errorMessages.length;i++){
   errMsg=errMsg+response.errorMessages[i]+"\n";
   }
   alert(errMsg);
   }
   }

   });*/
}


//-------------------------------------------  Create some demo data ------------------------------------------------------
function setRoles() {
  ge.roles = [
    {
      id:"tmp_1",
      name:"Project Manager"
    },
    {
      id:"tmp_2",
      name:"Worker"
    },
    {
      id:"tmp_3",
      name:"Stakeholder/Customer"
    }
  ];
}

function setResource() {
  var res = [];
  for (var i = 1; i <= 10; i++) {
    res.push({id:"tmp_" + i,name:"Resource " + i});
  }
  ge.resources = res;
}


function clearGantt() {
  ge.reset();
}

function loadI18n() {
  GanttMaster.messages = {
    "CHANGE_OUT_OF_SCOPE":"NO_RIGHTS_FOR_UPDATE_PARENTS_OUT_OF_EDITOR_SCOPE",
    "START_IS_MILESTONE":"开始时间为里程碑",
    "END_IS_MILESTONE":"结束时间为里程碑",
    "TASK_HAS_CONSTRAINTS":"TASK_HAS_CONSTRAINTS",
    "GANTT_ERROR_DEPENDS_ON_OPEN_TASK":"GANTT_ERROR_DEPENDS_ON_OPEN_TASK",
    "GANTT_ERROR_DESCENDANT_OF_CLOSED_TASK":"GANTT_ERROR_DESCENDANT_OF_CLOSED_TASK",
    "TASK_HAS_EXTERNAL_DEPS":"TASK_HAS_EXTERNAL_DEPS",
    "GANNT_ERROR_LOADING_DATA_TASK_REMOVED":"GANNT_ERROR_LOADING_DATA_TASK_REMOVED",
    "ERROR_SETTING_DATES":"ERROR_SETTING_DATES",
    "CIRCULAR_REFERENCE":"CIRCULAR_REFERENCE",
    "CANNOT_DEPENDS_ON_ANCESTORS":"CANNOT_DEPENDS_ON_ANCESTORS",
    "CANNOT_DEPENDS_ON_DESCENDANTS":"CANNOT_DEPENDS_ON_DESCENDANTS",
    "INVALID_DATE_FORMAT":"无效的时间格式",
    "TASK_MOVE_INCONSISTENT_LEVEL":"任务不能在不同级别移动",

    "GANT_QUARTER_SHORT":"trim.",
    "GANT_SEMESTER_SHORT":"sem."
  };
}

//-------------------------------------------  Open a black popup for managing resources. This is only an axample of implementation (usually resources come from server) ------------------------------------------------------
function openResourceEditor() {
  var editor = $("<div>");
  editor.append("<h2>Resource editor</h2>");
  editor.addClass("resEdit");

  for (var i in ge.resources) {
    var res = ge.resources[i];
    var inp = $("<input type='text'>").attr("pos", i).addClass("resLine").val(res.name);
    editor.append(inp).append("<br>");
  }

  var sv = $("<div>save</div>").css("float", "right").addClass("button").click(function() {
    $(this).closest(".resEdit").find("input").each(function() {
      var el = $(this);
      var pos = el.attr("pos");
      ge.resources[pos].name = el.val();
    });
    ge.editor.redraw();
    closeBlackPopup();
  });
  editor.append(sv);

  var ndo = createBlackPage(800, 500).append(editor);
}

//-------------------------------------------  Get project file as JSON (used for migrate project from gantt to Teamwork) ------------------------------------------------------
function getFile() {
  $("#gimBaPrj").val(JSON.stringify(ge.saveProject()));
  $("#gimmeBack").submit();
  $("#gimBaPrj").val("");

  /*  var uriContent = "data:text/html;charset=utf-8," + encodeURIComponent(JSON.stringify(prj));
   console.debug(uriContent);
   neww=window.open(uriContent,"dl");*/
}


//-------------------------------------------  LOCAL STORAGE MANAGEMENT (for this demo only) ------------------------------------------------------
/*
Storage.prototype.setObject = function(key, value) {
  this.setItem(key, stringify(value));
};

Storage.prototype.getObject = function(key) {
  return this.getItem(key) && parse(this.getItem(key));
};
*/

function loadFromLocalStorage() {
  var ret;
  if (false && localStorage) {
    if (localStorage.getObject("teamworkGantDemo")) {
      ret = localStorage.getObject("teamworkGantDemo");
    }
  } else {
    // $("#taZone").show();
  }
  if (!ret || !ret.tasks || ret.tasks.length == 0)
    ret = $.parseJSON($("#ta").val());
  ge.loadProject(ret);
  ge.checkpoint(); //empty the undo stack
}

function saveInLocalStorage() {
  prj = ge.saveProject();
  // 是否支持本地存储
  if (false && localStorage) {
    localStorage.setObject("teamworkGantDemo", prj);
  } else {
    $("#ta").val(JSON.stringify(prj));
  }
  
  var reason = "";
  <%if (wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_PASSED) {%>
  
		 $("#remindDlg").dialog("open");
  <%}else{%>
		$.ajax({
			type: "post",
			url: "workplan_gantt_do.jsp",
			data: {
				op: "gantt",
				workplanId: "<%=workplanId%>",
				data: JSON.stringify(prj),
				reason: reason
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#workSpace').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				
				if (data.ret=="0") {
					jAlert(data.msg, "提示");
				}
				else {
					jAlert_Redirect(data.msg, "提示", "workplan_gantt.jsp?id=<%=workplanId%>&flag=<%=flag%>");
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#workSpace').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		}); 
		
  <%}%>
}

</script>

<div id="gantEditorTemplates" style="display:none;">
  <div class="__template__XXX" type="GANTBUTTONS"><!--
  <div class="ganttButtonBar">
    <h1 style="float:left;">task tree/gantt</h1>
    <div class="buttons">
    <button onclick="$('#workSpace').trigger('undo.gantt');" class="button textual" title="undo"><span class="teamworkIcon">&#39;</span></button>
    <button onclick="$('#workSpace').trigger('redo.gantt');" class="button textual" title="redo"><span class="teamworkIcon">&middot;</span></button>
    <span class="ganttButtonSeparator"></span>
    <button onclick="$('#workSpace').trigger('addAboveCurrentTask.gantt');" class="button textual" title="insert above"><span class="teamworkIcon">l</span></button>
    <button onclick="$('#workSpace').trigger('addBelowCurrentTask.gantt');" class="button textual" title="insert below"><span class="teamworkIcon">X</span></button>
    <span class="ganttButtonSeparator"></span>
    <button onclick="$('#workSpace').trigger('indentCurrentTask.gantt');" class="button textual" title="indent task"><span class="teamworkIcon">.</span></button>
    <button onclick="$('#workSpace').trigger('outdentCurrentTask.gantt');" class="button textual" title="unindent task"><span class="teamworkIcon">:</span></button>
    <span class="ganttButtonSeparator"></span>
    <button onclick="$('#workSpace').trigger('moveUpCurrentTask.gantt');" class="button textual" title="move up"><span class="teamworkIcon">k</span></button>
    <button onclick="$('#workSpace').trigger('moveDownCurrentTask.gantt');" class="button textual" title="move down"><span class="teamworkIcon">j</span></button>
    <span class="ganttButtonSeparator"></span>
    <button onclick="$('#workSpace').trigger('zoomMinus.gantt');" class="button textual" title="zoom out"><span class="teamworkIcon">)</span></button>
    <button onclick="$('#workSpace').trigger('zoomPlus.gantt');" class="button textual" title="zoom in"><span class="teamworkIcon">(</span></button>
    <span class="ganttButtonSeparator"></span>
    <button onclick="$('#workSpace').trigger('deleteCurrentTask.gantt');" class="button textual" title="delete"><span class="teamworkIcon">&cent;</span></button>
    <%if (canManage) {%>
      &nbsp; &nbsp; &nbsp; &nbsp;
      <button onclick="saveGanttOnServer();" class="button first big" title="save">保存</button>
    <%}%>
    </div></div>
  --></div>

  <div class="__template__" type="TASKSEDITHEAD"><!--
  <table class="gdfTable" cellspacing="0" cellpadding="0">
    <thead>
    <tr style="height:40px">
      <th class="gdfColHeader" style="width:35px;"></th>
      <th class="gdfColHeader" style="width:25px;"></th>
      <th class="gdfColHeader gdfResizable" style="width:200px;">名称</th>
      <th class="gdfColHeader gdfResizable" style="width:120px;">开始</th>
      <th class="gdfColHeader gdfResizable" style="width:120px;">结束</th>
      <th class="gdfColHeader gdfResizable" style="width:50px;">工期</th>
      <th class="gdfColHeader gdfResizable" style="width:80px;">前置任务</th>
      <th class="gdfColHeader gdfResizable" style="width:80px;">责任人</th>
      <th class="gdfColHeader gdfResizable" style="width:80px;">关联计划</th>
      <th class="gdfColHeader gdfResizable" style="width:80px;">评价</th>
    </tr>
    </thead>
  </table>
  --></div>

  <div class="__template__" type="TASKROW"><!--
  <tr taskId="(#=obj.id#)" class="taskEditRow" level="(#=level#)">
    <th class="gdfCell editTask" align="right" style="cursor:pointer;"><span class="taskRowIndex">(#=obj.getRow()+1#)</span> <span class="teamworkIcon" style="font-size:12px;" >e</span></th>
    <td class="gdfCell" align="center"><div class="taskStatus cvcColorSquare" status="(#=obj.status#)"></div></td>
    <td class="gdfCell indentCell" style="padding-left:(#=obj.level*10#)px;"><input type="text" name="name" value="(#=obj.name#)" style="(#=obj.level>0?'border-left:2px dotted orange':''#)"></td>

    <td class="gdfCell"><input type="text" name="start"  value="" class="date"></td>
    <td class="gdfCell"><input type="text" name="end" value="" class="date"></td>
    <td class="gdfCell"><input type="text" name="duration" value="(#=obj.duration#)"></td>
    <td class="gdfCell"><input type="text" name="depends" value="(#=obj.depends#)" (#=obj.hasExternalDep?"readonly":""#)></td>
    <td class="gdfCell taskAssigs" align="center">(#=obj.getAssigsString()#)</td>
    <td class="gdfCell workplanRelated" align="center"></td>
    <td class="gdfCell assess" align="center"></td>
  </tr>
  --></div>

  <div class="__template__" type="TASKEMPTYROW"><!--
  <tr class="taskEditRow emptyRow" >
    <th class="gdfCell" align="right"></th>
    <td class="gdfCell" align="center"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
    <td class="gdfCell"></td>
  </tr>
  --></div>

  <div class="__template__" type="TASKBAR"><!--
  <div class="taskBox" taskId="(#=obj.id#)" >
    <div class="layout (#=obj.hasExternalDep?'extDep':''#)">
      <div class="taskStatus" status="(#=obj.status#)"></div>
      <div class="taskProgress" style="width:(#=obj.progress>100?100:obj.progress#)%; background-color:(#=obj.progress>100?'red':'rgb(153,255,51);'#);"></div>
      <div class="milestone (#=obj.startIsMilestone?'active':''#)" ></div>

      <div class="taskLabel"></div>
      <div class="milestone end (#=obj.endIsMilestone?'active':''#)" ></div>
    </div>
  </div>
  --></div>


  <div class="__template__" type="CHANGE_STATUS"><!--
    <div class="taskStatusBox">
      <div class="taskStatus cvcColorSquare" status="STATUS_ACTIVE" title="活动"></div>
      <div class="taskStatus cvcColorSquare" status="STATUS_DONE" title="完成"></div>
      <div class="taskStatus cvcColorSquare" status="STATUS_FAILED" title="失败"></div>
      <div class="taskStatus cvcColorSquare" status="STATUS_SUSPENDED" title="挂起"></div>
      <div class="taskStatus cvcColorSquare" status="STATUS_UNDEFINED" title="未定义"></div>
    </div>
  --></div>


  <div class="__template__" type="TASK_EDITOR"><!--
    <div class="ganttTaskEditor">
        <table width="100%" cellpadding="5" class="tabStyle_1 percent98">
        	  <thead>
              <tr height="30px">
                <td class="tabStyle_1_title" colspan="4" align="center">编辑任务</td>
              </tr>
              </thead>
              <tr height="30px">
                <td width="12%"> 参与者 </td>
                <td width="40%"><select id="resource" name="resource">
                    <%
              UserMgr um = new UserMgr();
              String[] principalAry = wpd.getPrincipals();
              int len = principalAry.length;
              for (int i=0; i<len; i++) {
                if (principalAry[i].equals(""))
                    continue;
                UserDb user = um.getUserDb(principalAry[i]);
                %>
                    <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
                    <%
              }
              
              String[] userAry = wpd.getUsers();
              len = userAry.length;
              for (int i=0; i<len; i++) {
                if (userAry[i].equals(""))
                    continue;
                // 过滤掉负责人
                boolean isFound = false;
                for (int j=0; j<principalAry.length; j++) {
                    if (principalAry[j].equals(userAry[i])) {
                        isFound = true;
                        break;
                    }
                }
                if (isFound)
                    continue;
                UserDb user = um.getUserDb(userAry[i]);
                %>
                    <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
                    <%
              }	
                  
              %>
                  </select>
                  <div style="display:none">
                    <label for="code">编码</label>
                    <br>
                    <input type="text" name="code" id="code" value="">
                  </div></td>
                <td width="13%"><label for="status">状态</label></td>
                <td width="35%"><span id="status" class="taskStatus" status=""></span></td>
              </tr>
              <tr height="30px">
                <td><label for="name">名称</label></td>
                <td><input type="text" name="name" id="name" value=""  size="35"></td>
                <td><label for="progress">进度</label></td>
                <td><input type="text" name="progress" id="progress" value="" size="3">&nbsp;%</td>
              </tr>
              <tr>
                <td><label for="description">描述</label>
                  <br></td>
                <td><textarea rows="3" cols="30" id="description" name="description"></textarea></td>
                <td><label for="workplanRelated">关联计划ID</label></td>
                <td><input type="text" name="workplanRelated" id="workplanRelated" value="" size="3"></td>
              </tr>
              <tr height="30px">
                <td>评价 </td>
                <td><select id="assess" name="assess">
                    <%
                SelectMgr sm = new SelectMgr();
                SelectDb sd = sm.getSelect("workplan_assess");
                Vector vsd = sd.getOptions();
                Iterator irsd = vsd.iterator();
                while (irsd.hasNext()) {
                    SelectOptionDb sod = (SelectOptionDb)irsd.next();
                    %>
                    <option value="<%=sod.getValue()%>"><%=sod.getName()%></option>
                    <%
                }
                %>
                  </select></td>
                <td>汇报流程</td>
                <td><select id="reportFlowType" name="reportFlowType">
                    <option value="">直接汇报</option>
                    <%
                com.redmoon.oa.flow.Leaf rootlf = new com.redmoon.oa.flow.Leaf();
                rootlf = rootlf.getLeaf(com.redmoon.oa.flow.Leaf.CODE_ROOT);
                com.redmoon.oa.flow.DirectoryView flowdv = new com.redmoon.oa.flow.DirectoryView(rootlf);
                flowdv.ShowDirectoryAsOptions(request, out, rootlf, rootlf.getLayer());
                %>
                  </select></td>
              </tr>
              <tr height="30px">
                <td><label for="start">开始</label></td>
                <td><input type="text" name="start" id="start"  value="" size="15" >
                  <input type="checkbox" id="startIsMilestone">
                  <img title='里程碑' align='absmiddle' src='../images/workplan/milestone.png'></td>
                <td><label for="end">结束</label></td>
                <td><input type="text" name="end" id="end" value="" size="15">
                  <input type="checkbox" id="endIsMilestone">
                  <img title='里程碑' align='absmiddle' src='../images/workplan/milestone.png'></td>
              </tr>
              <tr height="30px">
                <td><label for="duration">工期</label></td>
                <td><input type="text" name="duration" id="duration" value=""  size="5"></td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
              </tr>
            </table>  
      <div style="display:none">
        <h2>任务</h2>
        <table  cellspacing="1" cellpadding="0" width="100%" id="assigsTable">
          <tr>
            <th style="width:100px;">name</th>
            <th style="width:70px;">role</th>
            <th style="width:30px;">est.wklg.</th>
            <th style="width:30px;" id="addAssig"><span class="teamworkIcon" style="cursor: pointer">+</span></th>
          </tr>
        </table>
      </div>
      <div style="text-align: right; padding-top: 20px">
        <button id="saveButton" class="button big">确定</button>
      </div>
    </div>
  --></div>


  <div class="__template__" type="ASSIGNMENT_ROW"><!--
  <tr taskId="(#=obj.task.id#)" assigId="(#=obj.assig.id#)" class="assigEditRow" >
    <td ><select name="resourceId"  class="formElements" (#=obj.assig.id.indexOf("tmp_")==0?"":"disabled"#) ></select></td>
    <td ><select type="select" name="roleId"  class="formElements"></select></td>
    <td ><input type="text" name="effort" value="(#=getMillisInHoursMinutes(obj.assig.effort)#)" size="5" class="formElements"></td>
    <td align="center"><span class="teamworkIcon delAssig" style="cursor: pointer">d</span></td>
  </tr>
  --></div>
  <div id="importDiv" style="display:none;" >
     <form name="importFrm" id="importFrm" action="" method="post" enctype="multipart/form-data">
     	<div class="dialog_margin">
		   文件：&nbsp;&nbsp;&nbsp;<input id="upload" name="upload" type="file" style="margin-top:0.2cm"/>
        </div>
       </form>
</div>

<div id="remindDlg" style="display:none;">
计划已审核通过，请输入修改原因
<br />
<textarea id="reason" name="reason" style="width:100%; height:100px"></textarea>
</div>

<script type="text/javascript">
  $.JST.loadDecorator("ASSIGNMENT_ROW", function(assigTr, taskAssig) {

    var resEl = assigTr.find("[name=resourceId]");
    for (var i in taskAssig.task.master.resources) {
      var res = taskAssig.task.master.resources[i];
      var opt = $("<option>");
      opt.val(res.id).html(res.name);
      if (taskAssig.assig.resourceId == res.id)
        opt.attr("selected", "true");
      resEl.append(opt);
    }

    var roleEl = assigTr.find("[name=roleId]");
    for (var i in taskAssig.task.master.roles) {
      var role = taskAssig.task.master.roles[i];
      var optr = $("<option>");
      optr.val(role.id).html(role.name);
      if (taskAssig.assig.roleId == role.id)
        optr.attr("selected", "true");
      roleEl.append(optr);
    }

    if (taskAssig.task.master.canWrite) {
      assigTr.find(".delAssig").click(function() {
        var tr = $(this).closest("[assigId]").fadeOut(200, function() {
          $(this).remove();
        });
      });
    }


  });
</script>



</body>
</html>