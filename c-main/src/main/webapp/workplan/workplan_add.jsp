<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.FormDb"%>
<%@ page import="com.redmoon.oa.flow.FormMgr"%>
<%@ page import="com.redmoon.oa.project.*"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
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

long projectId = ParamUtil.getLong(request, "projectId", -1);
if (!privilege.isUserPrivValid(request, priv)) {
	boolean isValid = false;
	if (projectId!=-1 && ProjectMemberChecker.isUserExist(projectId, privilege.getUser(request))) {
		isValid = true;
	}
	if (!isValid) {
	  %>
	  <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	  <%
	  out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	  return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加工作计划</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>
<script src="../inc/flow_js.jsp"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>


<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script language="JavaScript" type="text/JavaScript">
<!--
function findObj(theObj, theDoc) {
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

var doWhat = "";

function openWinUsers() {
	doWhat = "users";
	selUserNames = o('users').value;
	selUserRealNames = o('userRealNames').value;
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>', 800, 600);
}

function setUsers(users, userRealNames) {
	if (doWhat=="users") {
		o('users').value = users;
		o('userRealNames').value = userRealNames;
	}
	if (doWhat=="principal") {
		o('principal').value = users;
		o('principalRealNames').value = userRealNames;
	}
}

function openWinPrincipal() {
	doWhat = "principal";
	selUserNames = o('principal').value;
	selUserRealNames = o('principalRealNames').value;
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>', 800, 600);
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	o('deptNames').value = "";
	o('depts').value = "";
	for (var i=0; i<ret.length; i++) {
		if (o('deptNames').value=="") {
			o('depts').value += ret[i][0];
			o('deptNames').value += ret[i][1];
		}
		else {
			o('depts').value += "," + ret[i][0];
			o('deptNames').value += "," + ret[i][1];
		}
	}
	if (o('depts').value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		o('depts').value = "<%=DeptDb.ROOTCODE%>";
		o('deptNames').value = "全部";
	}
}

function getDepts() {
	return o('depts').value;
}

function getDept() {
	return o('depts').value;
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
	/*
	if (o("depts").value=="") {
		alert("发布范围不能为空！");
		return false;
	}
	*/
	// if (o("users").value=="") {
	//	alert("参与人不能为空！");
	//	return false;
	// }
	
	$('#workplanTable').showLoading();	
}
//-->
</script>
</head>
<body>
<%if (projectId==-1) {
  	if(action.equals("sel")){
%>
		<%@ include file="workplan_list_sel_menu_top.jsp"%>
		<script>
		o("menu1").className="current";
		</script>
	<%}else{%>
		<%@ include file="workplan_inc_menu_top.jsp"%>
		<script>
		o("menu3").className="current";
		</script>
	<%	}
}else{%>
	<%@ include file="../project/prj_inc_menu_top.jsp"%>
<%}%>
<div class="spacerH"></div>
<form action="workplan_do.jsp?op=add&action=<%=action%>" name="form1" method="post" enctype="multipart/form-data" onSubmit="return form1_onsubmit()">
<table id="workplanTable" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td colspan="2" class="tabStyle_1_title">添加计划</td>
    </tr>
    <tr>
      <td noWrap>计划名称：</td>
      <td><input name="title" id="title" size="60" maxLength="200">
	  <font color="#FF0000">*</font>	  </td>
    </tr>
    <tr>
      <td noWrap>计划模板：</td>
      <td>
      <span id="templateTitle"></span>
      &nbsp;&nbsp;<a href="javascript:;" onClick="selTemplate()">选择模板</a>
      <input name="templateId" type="hidden" />
      </td>
    </tr>
	<%
	if(!flowId.equals("")&&flowId!=null){
	%>
    <tr>
      <td noWrap>关联流程ID：</td>
      <td><input name="flowId" id="flowId" size="6" maxLength="100" value="<%=flowId%>"></td>
    </tr>
	<%}%>
	<%if (com.redmoon.oa.kernel.License.getInstance().isPlatform()) {%>
	<%if (true || projectId!=-1) {%>
    <tr>
      <td noWrap>关联项目：</td>
      <td><%
		String prjName = "";
		FormMgr fm = new FormMgr();
		FormDb fd = fm.getFormDb("project");

		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
		com.redmoon.oa.visual.FormDAO fdao = fdm.getFormDAO(projectId);
		
		prjName = fdao.getFieldValue("name");
	  %>
        <input id="projectId" name="projectId" type="hidden" value="<%=projectId%>" />
        <input name="projectId_realshow" readonly="readonly" value="<%=prjName%>" />
        <input class="btn" name="button2" type="button" onClick="openWinProjectList(o('projectId'))" value='选择' /></td>
    </tr>
    <%}%>
    <%}%>
    <tr>
      <td noWrap>计划内容：</td>
      <td><textarea id="content" name="content" style="display:none"></textarea>
          <script>		  
			CKEDITOR.replace('content',
				{
					// skin : 'kama',
					// toolbar : 'Middle'
					toolbar : [
						['Source'],  
						['Cut','Copy','Paste','PasteText','PasteFromWord'],  
						['Undo','Redo'],  
						['Outdent','Indent'],
						['Bold','Italic','Underline','Strike','-','Superscript'], 
						'/',		
						['Styles','Format','Font','FontSize'],  
						['TextColor','BGColor'],
						['Table','Smiley','SpecialChar']
					]
				});
		  </script>
          </td>
    </tr>
    <tr>
      <td noWrap>有效期：</td>
      <td>开始日期：
		<input readonly type="text" id="beginDate" name="beginDate" size="10">
		<font color="#FF0000">*</font>&nbsp;&nbsp;
        结束日期： 
		<input readonly type="text" id="endDate" name="endDate" size="10">
        <font color="#FF0000">*</font></td>
    </tr>
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
      </td>
    </tr>
    <tr style="display:none">
      <td noWrap>发布范围（部门）：</td>
      <td><input type="hidden" id="depts" name="depts">
          <textarea name="deptNames" cols="60" rows="5" readOnly wrap="yes" id="deptNames"></textarea>
&nbsp; <br />
<input class="btn" title="添加部门" onClick="openWinDepts()" type="button" value="添 加" name="button">
        &nbsp;
        <input class="btn" title="清空部门" onClick="o('deptNames').value='';o('depts').value=''" type="button" value="清 空" name="button"></td>
    </tr>
    <tr >
      <td noWrap>负责人：</td>
      <td><input name="principal" type="hidden" id="principal">
        <textarea name="principalRealNames" cols="60" rows="3" readOnly wrap="virtual" id="principalRealNames"></textarea>
        <font color="#FF0000">*</font>&nbsp; <br />
        <input class="btn" title="选择人员" onClick="openWinPrincipal()" type="button" value="选择" name="button">
        &nbsp;
        <input class="btn" title="清空人员" onClick="o('principalRealNames').value='';o('principal').value=''" type="button" value="清空" name="button"></td>
    </tr>
    <tr >
      <td noWrap>参与人：</td>
      <td><input name="users" id="users" type="hidden">
        <textarea name="userRealNames" cols="60" rows="5" readOnly wrap="yes" id="userRealNames"></textarea>
        <br />
        <input class="btn" title="选择人员" onClick="openWinUsers()" type="button" value="选择" name="button">
        &nbsp;
        <input class="btn" title="清空人员" onClick="o('users').value='';o('userRealNames').value=''" type="button" value="清空" name="button"></td>
    </tr>    
    <tr>
      <td noWrap>备注：</td>
      <td><textarea name="remark" cols="60" rows="7" wrap="yes" class="BigINPUT" id="remark"></textarea></td>
    </tr>
    <tr>
      <td noWrap>提醒：</td>
      <td><input id="smsRemind" type="checkbox" CHECKED name="isMessageRemind" value="true">
          <label for="SMS_REMIND">消息提醒&nbsp;
          <%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
          <input name="isToMobile" value="true" type="checkbox" checked />
短信提醒
<%}%>
          </label></td>
    </tr>
    <tr>
      <td colspan="2" height="22" noWrap>
		<script>initUpload()</script>		</td>
      </tr>
    <tr class="TableControl" align="middle">
      <td colSpan="2" align="center" noWrap><input name="submit" type="submit" class="btn" value=" 确 定 ">
        &nbsp;&nbsp;
        <input name="button" type="reset" class="btn" value=" 重 置 ">
        &nbsp;&nbsp;</td></tr>
  </tbody>
</table>
<br />
</form>
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
});

function selWorkplan(id,author,title,deptName,userName,endTime,days) {
	o("templateTitle").innerHTML = title
	o("templateId").value = id;
	return true;
}

function selTemplate() {
	openWin("workplan_list_sel.jsp?action=sel", 800, 600);
}
</script>
</html>
