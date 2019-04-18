<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.oacalendar.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "org.json.*"%>
<%
long workplanId = ParamUtil.getLong(request, "id");
long hisId = ParamUtil.getLong(request, "hisId", -1);
WorkPlanDb wpd = new WorkPlanDb();
wpd = wpd.getWorkPlanDb((int)workplanId);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=9; IE=8; IE=7; IE=EDGE"/>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <title>甘特图</title>

  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/core.css" />
  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_workplan_gantt.css" />

  <link rel=stylesheet href="../js/jQueryGantt/platform.css" type="text/css">
  <link rel=stylesheet href="../js/jQueryGantt/libs/dateField/jquery.dateField.css" type="text/css">
  <link rel=stylesheet href="../js/jQueryGantt/gantt.css" type="text/css">
  
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

  <script src="../js/jQueryGantt/ganttUtilities.js"></script>
  <script src="../js/jQueryGantt/ganttTask.js"></script>
  <script src="../js/jQueryGantt/ganttDrawer.js"></script>
  <script src="../js/jQueryGantt/ganttGridEditor.js"></script>
  <script src="../js/jQueryGantt/ganttMaster.js"></script>

  <script src="../js/tabpanel/Toolbar.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
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
ir = oacal.getSatdaysIsWorking(DateUtil.getYear(wpd.getBeginDate())-1, curYear + 1).iterator();
while (ir.hasNext()) {
	oacal = (OACalendarDb)ir.next();
	sunWorkingDays += DateUtil.format(oacal.getDate("oa_date"), "yyyy_MM_dd") + "#";
}
%>

i18nJsHolidays = "<%=holidays%>";
i18nSatWorkingDays = "<%=satWorkingDays%>";
i18nSunWorkingDays = "<%=sunWorkingDays%>";
 
function initGantt() {
  var workSpace = $("#workSpace");
  workSpace.css({width:$(window).width(), height:$(window).height() - 60});
  // loadGanttFromServer();
}
</script>
</head>
<body style="background-color: #fff;" onresize="initGantt()">
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu7").className="current";
</script>
<div class="spacerH"></div>
<div style="height:22px; font-size:12px; padding:5px">
<%
WorkPlanLogDb wpld = new WorkPlanLogDb();
wpld = (WorkPlanLogDb)wpld.getQObjectDb(new Long(hisId));
UserDb user = new UserDb();
user = user.getUserDb(wpld.getString("user_name"));
%>
<%=user.getRealName()%>&nbsp;&nbsp;<%=DateUtil.format(wpld.getDate("create_date"), "yyyy-MM-dd")%>
</div>

<div id="workSpace" style="padding:0px; overflow-y:auto; overflow-x:hidden;border:1px solid #e5e5e5;position:relative;margin:0px"></div>

<div id="taZone" style="display:none;">
  <textarea rows="8" cols="150" id="ta">
  	<%
	String jsonStr = wpld.getString("gantt");
	if (!jsonStr.equals("")) {
		JSONObject json = new JSONObject(jsonStr);
		json.put("canWrite", false);

		out.print(json.toString());
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

var ge;  //this is the hugly but very friendly global var for the gantt editor
$(function() {

  //load templates
  $("#ganttemplates").loadTemplates();

  // here starts gantt initialization
  ge = new GanttMaster();
  var workSpace = $("#workSpace");
  // workSpace.css({width:$(window).width() - 20, height:$(window).height() - 100});
  workSpace.css({width:$(window).width() + 600, height:$(window).height() - 60});
  ge.init(workSpace);

  //inject some buttons (for this demo only)

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


  /*/debug time scale
  $(".splitBox2").mousemove(function(e){
    var x=e.clientX-$(this).offset().left;
    var mill=Math.round(x/(ge.gantt.fx) + ge.gantt.startMillis)
    $("#ndo").html(x+" "+new Date(mill))
  });*/

});


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
    "START_IS_MILESTONE":"START_IS_MILESTONE",
    "END_IS_MILESTONE":"END_IS_MILESTONE",
    "TASK_HAS_CONSTRAINTS":"TASK_HAS_CONSTRAINTS",
    "GANTT_ERROR_DEPENDS_ON_OPEN_TASK":"GANTT_ERROR_DEPENDS_ON_OPEN_TASK",
    "GANTT_ERROR_DESCENDANT_OF_CLOSED_TASK":"GANTT_ERROR_DESCENDANT_OF_CLOSED_TASK",
    "TASK_HAS_EXTERNAL_DEPS":"TASK_HAS_EXTERNAL_DEPS",
    "GANNT_ERROR_LOADING_DATA_TASK_REMOVED":"GANNT_ERROR_LOADING_DATA_TASK_REMOVED",
    "ERROR_SETTING_DATES":"ERROR_SETTING_DATES",
    "CIRCULAR_REFERENCE":"CIRCULAR_REFERENCE",
    "CANNOT_DEPENDS_ON_ANCESTORS":"CANNOT_DEPENDS_ON_ANCESTORS",
    "CANNOT_DEPENDS_ON_DESCENDANTS":"CANNOT_DEPENDS_ON_DESCENDANTS",
    "INVALID_DATE_FORMAT":"INVALID_DATE_FORMAT",
    "TASK_MOVE_INCONSISTENT_LEVEL":"TASK_MOVE_INCONSISTENT_LEVEL",

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
  var prj = ge.saveProject();
  // 是否支持本地存储
  if (false && localStorage) {
    localStorage.setObject("teamworkGantDemo", prj);
  } else {
    $("#ta").val(JSON.stringify(prj));
  }

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
    </div></div>
  --></div>

  <div class="__template__" type="TASKSEDITHEAD"><!--
  <table class="gdfTable" cellspacing="0" cellpadding="0">
    <thead>
    <tr style="height:40px">
      <th class="gdfColHeader" style="width:35px;"></th>
      <th class="gdfColHeader" style="width:25px;"></th>
      <th class="gdfColHeader gdfResizable" style="width:200px;">名称</th>
      <th class="gdfColHeader gdfResizable" style="width:80px;">开始</th>
      <th class="gdfColHeader gdfResizable" style="width:80px;">结束</th>
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
  	<th class="gdfCell" align="right" style="cursor:pointer;"><span class="taskRowIndex">(#=obj.getRow()+1#)</span> </th>
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
  <table width="100%">
    <tr>
      <td>
        <table cellpadding="5">
          <tr>
            <td>
                      参与者
          <select id="resource" name="resource">
          </select>
          <div style="display:none"><label for="code">编码</label><br><input type="text" name="code" id="code" value="" class="formElements"></div>
          </td>
           </tr><tr>
            <td><label for="name">名称</label><input type="text" name="name" id="name" value=""  size="35" class="formElements"></td>
          </tr>
          <tr></tr>
            <td>
              <label for="description">描述</label><br>
              <textarea rows="5" cols="30" id="description" name="description" class="formElements"></textarea>
            </td>
          </tr>
        </table>
      </td>
      <td valign="top">
        <table cellpadding="5">
          <tr>
          <td colspan="2"><label for="status">状态</label>&nbsp;<span id="status" class="taskStatus" status=""></span>
          </td>
          <tr>
          <td colspan="2">
          <label for="progress">进度</label><input type="text" name="progress" id="progress" value="" size="3" class="formElements">
          <label for="workplanRelated">关联计划ID&nbsp;</label><input type="text" name="workplanRelated" id="workplanRelated" value="" size="3" class="formElements">
          </td>
          </tr>
          <tr>
          <td><label for="start">开始</label><br><input type="text" name="start" id="start"  value="" class="date" size="10" class="formElements"><input type="checkbox" id="startIsMilestone"> </td>
          <td rowspan="2" class="graph" style="padding-left:50px"><label for="duration">工期</label><br><input type="text" name="duration" id="duration" value=""  size="5" class="formElements"></td>
        </tr><tr>
          <td>
          <label for="end">结束</label><br><input type="text" name="end" id="end" value="" class="date"  size="10" class="formElements"><input type="checkbox" id="endIsMilestone">
          <br />
          <br />
            评价<select id="assess" name="assess">
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
            </select>
          </td>
        </table>
      </td>
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

  <div style="text-align: right; padding-top: 20px"><button id="saveButton" class="button big">保存</button></div>
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