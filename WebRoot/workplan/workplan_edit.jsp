<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.FormDAO"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.FormMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划 - 编辑</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>
<script src="../inc/flow_js.jsp"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script language="JavaScript" type="text/JavaScript">
<!--
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

var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function setUsers(users, userRealNames) {
	if (doWhat=="users") {
		form1.users.value = users;
		form1.userRealNames.value = userRealNames;
	}
	if (doWhat=="principal") {
		form1.principal.value = users;
		form1.principalRealNames.value = userRealNames;
	}
}

var doWhat = "";

function openWinUsers() {
	doWhat = "users";
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
}

function openWinPrincipal() {
	doWhat = "principal";
	selUserNames = form1.principal.value;
	selUserRealNames = form1.principalRealNames.value;
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	form1.deptNames.value = "";
	form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.deptNames.value=="") {
			form1.depts.value += ret[i][0];
			form1.deptNames.value += ret[i][1];
		}
		else {
			form1.depts.value += "," + ret[i][0];
			form1.deptNames.value += "," + ret[i][1];
		}
	}
	if (form1.depts.value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		form1.depts.value = "<%=DeptDb.ROOTCODE%>";
		form1.deptNames.value = "全部";
	}
}

function getDepts() {
	return form1.depts.value;
}

function form1_onsubmit() {
    var title = o("title").value;
    if (title == "") {
        jAlert("名称不能为空！","提示");
        o("title").focus();
        return false;
    }
    else if((title.indexOf("$") != -1) || (title.indexOf('"') != -1) || (title.indexOf("'") != -1))
    {
        jAlert("名称不能包含$、" + '"' + "、' 等字符！","提示");
        o("title").focus();
        return false;
    }
    if (o("beginDate").value=="") {
        jAlert("开始日期不能为空！","提示");
        o("beginDate").focus();
        return false;
    }
    if (o("endDate").value=="") {
        jAlert("结束日期不能为空！","提示");
        o("endDate").focus();
        return false;
    }
    if (o("principalRealNames").value == "")
    {
        jAlert("负责人不能为空！","提示");
        o("principalRealNames").focus();
        return false;
    }
	$('#workplanTable').showLoading();
}
//-->
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
WorkPlanMgr wpm = new WorkPlanMgr();
WorkPlanDb wpd = null;
// 由这里来检查权限
try {
	wpd = wpm.getWorkPlanDb(request, id, "edit");
}
catch (ErrMsgException e) {
	// out.print(StrUtil.Alert_Back(e.getMessage()));
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));	
	return;
}

String beginDate = DateUtil.format(wpd.getBeginDate(), "yyyy-MM-dd");
String endDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
long projectId = wpd.getProjectId();
int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (true || projectId==-1) {
	if (isShowNav==1) {
%>
		<%@ include file="workplan_show_inc_menu_top.jsp"%>
<%	}
}else{
	request.setAttribute("projectId", new Long(projectId));
%>
	  	<%@ include file="../project/prj_inc_menu_top.jsp"%>
<%}%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form action="workplan_do.jsp?op=modify&id=<%=id%>&isShowNav=<%=isShowNav%>" name="form1" method="post" enctype="multipart/form-data" onsubmit="return form1_onsubmit()">
<table id="workplanTable" class="tabStyle_1 percent80" width="600" border="0" align="center" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td colspan="2" noWrap class="tabStyle_1_title">修改计划</td>
    </tr>
    <tr>
      <td noWrap>计划名称：</td>
      <td><input name="title" id="title" size="60" maxLength="200" value="<%=wpd.getTitle()%>">
	  <input type=hidden name="id" value="<%=wpd.getId()%>"><font color="#FF0000">*</font></td>
    </tr>
    <tr>
      <td noWrap>进度</td>
      <td><input name="progress" value="<%=wpd.getProgress()%>" size="3" />
        &nbsp;%
      &nbsp;&nbsp;审核状态：
      <%
	  com.redmoon.oa.workplan.Privilege wpvg = new com.redmoon.oa.workplan.Privilege();
	  boolean isMaster = wpvg.isWorkPlanMaster(request);
	  if (isMaster || wpd.getCheckStatus()==WorkPlanDb.CHECK_STATUS_NOT) {%>
      <select id="checkStatus" name="checkStatus" title="审核">
      <option value="<%=WorkPlanDb.CHECK_STATUS_NOT%>">未审</option>
      <option value="<%=WorkPlanDb.CHECK_STATUS_PASSED%>">已审</option>
      </select>
      <script>
	  $('#checkStatus').val("<%=wpd.getCheckStatus()%>");
	  </script>
      <%}else{%>
      已审<input name="checkStatus" value="<%=WorkPlanDb.CHECK_STATUS_PASSED%>" type="hidden" />
      <%}%>
      </td>
    </tr>
	<%if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform()) {%>    
	<%if (true || projectId!=-1) {%>    
    <tr>
      <td nowrap="nowrap">关联项目</td>
      <td><%
		String prjName = "";
		FormMgr fm = new FormMgr();
		FormDb fd = fm.getFormDb("project");

		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
		com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(projectId);
		
		prjName = fdao.getFieldValue("name");
  		%>
          <input id="projectId" name="projectId" type="hidden" value="<%=projectId%>" />
          <input id="projectId_realshow" name="projectId_realshow" readonly="readonly" value="<%=prjName%>" />
          <input class="btn" type="button" onclick="openWinProjectList(o('projectId'))" value="选择" />
          <input class="btn" type="button" onclick="o('projectId').value=''; o('projectId_realshow').value='';" value="取消" />
          </td>
    </tr>
    <%}%>
    <%}%>
    <tr>
      <td noWrap>计划内容：</td>
      <td><textarea id="content" name="content" style="display:none"><%=wpd.getContent()%></textarea>
          <script>
			CKEDITOR.replace('content',
				{
					// skin : 'kama',
					toolbar : 'Middle'
				});
		  </script>
      </td>
    </tr>
    <tr>
      <td noWrap>有效期：</td>
      <td>
		开始日期：
		  <input readonly type="text" id="beginDate" name="beginDate" size="12" value="<%=beginDate%>">
		<font color="#FF0000">*</font>&nbsp;&nbsp;
        结束日期： 
		<input readonly type="text" id="endDate" name="endDate" size="12" value="<%=endDate%>">
<font color="#FF0000">*</font></tr>
    <tr>
      <td noWrap>计划类型：</td>
      <td>
	  <%
	  WorkPlanTypeDb wptd = new WorkPlanTypeDb();
	  String opts = "";
	  Iterator ir = wptd.listOfUnit(privilege.getUserUnitCode(request)).iterator();
	  while (ir.hasNext()) {
	  	wptd = (WorkPlanTypeDb)ir.next();
	  	opts += "<option value='" + wptd.getId() + "'>" + wptd.getName() + "</option>";
	  }
	  %>
	  <select name="typeId" id="typeId">
	  <%=opts%>
      </select>
	  <script>
	  o('typeId').value = "<%=wpd.getTypeId()%>";
	  </script>
      </td>
    </tr>
    <tr style="display:none">
      <td noWrap>发布范围（部门）：</td>
      <td>
	  <%
	  String[] arydepts = wpd.getDepts();
	  String[] aryusers = wpd.getUsers();
	  String[] principals = wpd.getPrincipals();
	  String depts = "";
	  String deptNames = "";
	  String users = "";
	  String userRealNames = "";
	  String principal = "";
	  String principalRealNames = "";
	  
	  int len = 0;
	  if (arydepts!=null) {
	  	len = arydepts.length;
		DeptDb dd = new DeptDb();
	  	for (int i=0; i<len; i++) {
			if (depts.equals("")) {
				depts = arydepts[i];
				dd = dd.getDeptDb(arydepts[i]);
				deptNames = dd.getName();
			}
			else {
				depts += "," + arydepts[i];
				dd = dd.getDeptDb(arydepts[i]);
				deptNames += "," + dd.getName();
			}
		}
	  }
	  // System.out.print(getClass() + " aryusers=" + aryusers + " userRealNames=" + userRealNames);
	  if (aryusers!=null) {
	  	len = aryusers.length;
	  	for (int i=0; i<len; i++) {
			if (users.equals("")) {
				users = aryusers[i];
				UserDb ud = new UserDb();
				ud = ud.getUserDb(aryusers[i]);
	  			// System.out.print(getClass() + " aryusers=" + aryusers + " aryusers[" + i + "]=" + aryusers[i]);
				userRealNames = ud.getRealName();
			}
			else {
				users += "," + aryusers[i];
				UserDb ud = new UserDb();
				ud = ud.getUserDb(aryusers[i]);
				userRealNames += "," + ud.getRealName();
			}
		}
	  }
	  %>
	  	  <input type="hidden" id="depts" name="depts" value="<%=depts%>">
          <textarea name="deptNames" cols="60" rows="5" readOnly wrap="yes" id="deptName"><%=deptNames%></textarea>
        &nbsp; <br />
        <input class="btn" title="添加部门" onClick="openWinDepts()" type="button" value="添 加" name="button">
        &nbsp;
        <input class="btn" title="清空部门" onClick="o('deptNames').value='';o('depts').value=''" type="button" value="清 空" name="button"></td>
    </tr>
    <tr>
      <td noWrap>负责人：</td>
      <td>
	  <%	   
		 if(principals == null){
		    principalRealNames = "";
		 }else{
			   len = principals.length;
			   for (int i=0; i<len; i++) {	
			   	  // System.out.println(getClass() + " principals[i]=" + principals[i]);
			   	  if(principal.equals("")){	 
				    principal = principals[i];
					UserDb ud = new UserDb();
		            ud = ud.getUserDb(principals[i]);
			        principalRealNames = ud.getRealName();
				  }else{
				    principal += "," + principals[i] ; 
					UserDb ud = new UserDb();
		            ud = ud.getUserDb(principals[i]);
			        principalRealNames += "," + ud.getRealName();
				  }	
			   }	
		 }
	  %>
	  <textarea name="principalRealNames" cols="60" rows="3" readOnly wrap="virtual" id="principalRealNames"><%=principalRealNames%></textarea>
	  <font color="#FF0000">*</font>
	  <input name="principal" type="hidden" id="principal" value="<%=principal%>">
        &nbsp; <br />
        <input class="btn" title="添加收件人" onClick="openWinPrincipal()" type="button" value="添 加" name="button">
        &nbsp;
        <input class="btn" onClick="o('principal').value='';o('principalRealNames').value=''" type="button" value="清 空" name="button"></td>
    </tr>
    <tr>
      <td noWrap>参与人：</td>
      <td>
          <textarea name="userRealNames" cols="60" rows="5" readOnly wrap="yes" id="userRealNames"><%=userRealNames%></textarea>
		  <input name="users" id="users" type="hidden" value="<%=users%>">
		  <br />
        <input class="btn" title="添加收件人" onClick="openWinUsers()" type="button" value="添 加" name="button">
        &nbsp;
        <input class="btn" onClick="o('users').value='';o('userRealNames').value=''" type="button" value="清 空" name="button"></td>
    </tr>    
    <tr>
      <td noWrap>备注：</td>
      <td><textarea name="remark" cols="60" rows="7" wrap="yes" id="remark"><%=wpd.getRemark()%></textarea></td>
    </tr>
    <tr>
      <td noWrap>提醒：</td>
      <td><input id="smsRemind" type="checkbox" CHECKED name="isMessageRemind" value="true">
          <label for="SMS_REMIND">消息提醒
          <%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
          <input name="isToMobile" value="true" type="checkbox" checked />
短信提醒
<%}%>
</label></td>
    </tr>
    <tr class="TableControl" align="middle">
      <td colSpan="2" align="left" noWrap>
附件：
                      <%
					  java.util.Iterator attir = wpd.getAttachments().iterator();
					  while (attir.hasNext()) {
					  	Attachment att = (Attachment)attir.next();
					  %>
                        <div><img src="../images/attach2.gif" width="17" height="17">&nbsp;<a target="_blank" href="workplan_getfile.jsp?workPlanId=<%=wpd.getId()%>&attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='workplan_do.jsp?op=delattach&id=<%=wpd.getId()%>&workPlanId=<%=wpd.getId()%>&attachId=<%=att.getId()%>'}})" style="cursor:pointer" >删除</a></div>
                        <%}%>	  </td>
    </tr>
    <tr>
      <td colspan="2" noWrap>
		<script>initUpload()</script>		  </td>
      </tr>
    <tr class="TableControl" align="middle">
      <td colSpan="2" align="center" noWrap><input class="btn" name="submit" type="submit" value=" 确 定 ">
        &nbsp;&nbsp;
          <input class="btn" name="button" type="reset" value=" 重 置 ">
        &nbsp;&nbsp;</td>
    </tr>
  </tbody>
</table>
</form>
<%
JobUnitDb ju = new JobUnitDb();
int jobId = ju.getJobId("com.redmoon.oa.job.WorkplanJob", "" + id);
if (jobId!=-1) {
	ju = (JobUnitDb)ju.getQObjectDb(new Integer(jobId));
%>	
<form name="form2" action="workplan_do.jsp?op=editJob&isShowNav=<%=isShowNav%>" method="post" onSubmit="return form2_onsubmit()">
<table width="81%" border="0" align="center" class="main">
    <tr>
      <td align="left"><strong>调度计划&nbsp;&nbsp;<a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='workplan_do.jsp?op=delJob&id=<%=ju.get("id")%>&planId=<%=id%>&isShowNav=<%=isShowNav%>'}})" style="cursor:pointer">删除</a></strong></td>
    </tr>
    <tr>
      <td align="left"><input name="job_class" type="hidden" value="com.redmoon.oa.job.WorkplanJob">
          <input name="map_data" type="hidden" value="<%=id%>">
        名称：
        <input name="job_name" value="<%=ju.getString("job_name")%>">
        &nbsp;每月：
        <input name="month_day" size="2" value="<%=ju.getString("month_day")%>">
      号</td>
    </tr>
    <tr>
      <td align="left"> 开始时间
        <%
String cron = ju.getString("cron");
String[] ary = cron.split(" ");
if (ary[0].length()==1)
	ary[0] = "0" + ary[0];
if (ary[1].length()==1)
	ary[1] = "0" + ary[1];
if (ary[2].length()==1)
	ary[2] = "0" + ary[2];
String t = ary[2] + ":" + ary[1] + ":" + ary[0];
%>
          <input  name="time" id="time" size="6" value="<%=t%>">
        &nbsp;
        在
        <input name="weekDay" type="checkbox" value="1">
        星期日
        <input name="weekDay" type="checkbox" value="2">
        星期一
        <input name="weekDay" type="checkbox" value="3">
        星期二
        <input name="weekDay" type="checkbox" value="4">
        星期三
        <input name="weekDay" type="checkbox" value="5">
        星期四
        <input name="weekDay" type="checkbox" value="6">
        星期五
        <input name="weekDay" type="checkbox" value="7">
        星期六
        <input name="submit3" type="submit" value=" 确 定 " class="btn">
        <input name="planId" type="hidden" value="<%=id%>">
        <input name="id" type="hidden" value="<%=ju.getInt("id")%>">
        <input name="cron" type="hidden">
        <input name="data_map" type="hidden">
		<input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden">
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
      </td>
    </tr>
</table>
  </form>
<%}
else {
%>
<form name="form2" action="workplan_do.jsp?op=addJob&isShowNav=<%=isShowNav%>" method="post" onSubmit="return form2_onsubmit()">
<table class="tabStyle_1 percent80" width="600" border="0" align="center">
  <tr>
    <td class="tabStyle_1_title" align="left">调度计划</td>
  </tr>
  <tr>
    <td align="left"><input name="job_class" type="hidden" value="com.redmoon.oa.job.WorkplanJob">
      &nbsp;每月：
      <input name="month_day" size="2">
      号<input name="job_name" type="hidden" value="<%=wpd.getTitle()%>"></td>
  </tr>
  <tr>
    <td align="left"> 开始时间
      <input  value="12:00:00" name="time" id="time" size="6">
      &nbsp;
      在
      <input name="weekDay" type="checkbox" value="1">
      星期日
      <input name="weekDay" type="checkbox" value="2">
      星期一
      <input name="weekDay" type="checkbox" value="3">
      星期二
      <input name="weekDay" type="checkbox" value="4">
      星期三
      <input name="weekDay" type="checkbox" value="5">
      星期四
      <input name="weekDay" type="checkbox" value="6">
      星期五
      <input name="weekDay" type="checkbox" value="7">
      星期六
      <div align="center"><input class="btn" name="submit2" type="submit" value=" 确 定 "></div>
      <input name="id" type="hidden" value="<%=id%>">
      <input name="cron" type="hidden">
      <input name="data_map" type="hidden">
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden"></td>
  </tr>
</table>
</form>
<%}%>
</body>
<script>
$(function() {
	$('#beginDate').datetimepicker({
		lang:'ch',
		timepicker:false,
		format:'Y-m-d'
	});
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
		format:'Y-m-d'
	});
	 $('#time').datetimepicker({
		datepicker:false,
		format:'H:i:00',
		step:5
	});
});

function getDept() {
	return form1.depts.value;
}

/**function SelectDateTime(objName) {
	var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
	if (dt!=null)
		findObj(objName).value = dt;
}*/
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        findObj(objName).value = dt.substring(0, 5);
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
function form2_onsubmit() {
	var t = form2.time.value;
	var ary = t.split(":");
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = form2.month_day.value;
	if (weekDay=="" && dayOfMonth=="") {
		jAlert("请填写每月几号或者星期几！","提示");
		return false;
	}
	if (weekDay=="")
		weekDay = "?";
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	if (dayOfMonth=="")
		dayOfMonth = "?";
	var cron = ary[2] + " " + ary[1] + " " + ary[0] + " " + dayOfMonth + " * " + weekDay;
	form2.cron.value = cron;
	form2.data_map.value = "<%=id%>";
}

function trimOptionText(strValue) 
{
	// 注意option中有全角的空格，所以不直接用trim
	var r = strValue.replace(/^　*|\s*|\s*$/g,"");
	return r;
}
</script>
</html>