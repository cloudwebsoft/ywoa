<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = fchar.getNullStr(request.getParameter("op"));
if (op.equals("")) {
	out.print(fchar.Alert("您未选择操作方式！"));
	return;
}
UserMgr um = new UserMgr();
String typedesc = ""; // 类型描述
int type=0;
if (op.equals("new")) {
	type = 0;
	typedesc = "发起新任务";
	priv="task";
	if (!privilege.isUserPrivValid(request,priv))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
else if (op.equals("newsubtask")) {
	type = 1;
	typedesc = "发起子任务";
	priv="task";
	if (!privilege.isUserPrivValid(request,priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发起任务</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("util/jscalendar/calendar-win2k-2.css"); </style>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
<script>
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function changeexpression(i) {
	frmAnnounce.expression.value = i;
	if (i==0)
	{
		expressspan.innerHTML = "无";
		return;
	}
	expressspan.innerHTML = "<img align=absmiddle src=sq/forum/images/emot/em"+i+".gif>";
}

function setPerson(deptCode, deptName, user, realName) {
	frmAnnounce.person.value = user;
	frmAnnounce.jobCode.value = deptCode;
	frmAnnounce.userRealName.value = realName;
}

function frmAnnounce_onsubmit() {
<%if (op.equals("newsubtask")) {%>	  
	if (frmAnnounce.beginDate.value=="") {
		alert("开始日期不能为空！");
		frmAnnounce.beginDate.focus();
		return false;
	}
	if (frmAnnounce.endDate.value=="") {
		alert("结束日期不能为空！");
		frmAnnounce.endDate.focus();
		return false;
	}
<%}%>
}

function window_onload() {
}

var attachCount = 1;
function AddAttach() {
	updiv.innerHTML += "附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件&nbsp;&nbsp;<input type='file' name='filename" + attachCount + "' size=40><br />";
	attachCount += 1;
}
</script>
</head>
<body onLoad="window_onload()">
<%
long projectId = ParamUtil.getLong(request, "projectId", -1);
if (projectId==-1) {
%>
<%@ include file="task_inc_menu_top.jsp"%>
<%}else{%>
<%@ include file="project/prj_inc_menu_top.jsp"%>
<%}%>
<br>
<%
if (op.equals("addresult"))
{
	type = 2;
	typedesc = "汇报办理结果";
}
if (op.equals("hurry"))
{
	type = 3;
	typedesc = "催办";
}
String parentid = fchar.getNullStr(request.getParameter("parentid"));

// 如果父节点，或者根节点为已完成状态，则不可添加
TaskDb ptd = new TaskDb();
ptd = ptd.getTaskDb(Integer.parseInt(parentid));
if (ptd.getStatus()==ptd.STATUS_FINISHED) {
	out.print(StrUtil.Alert_Back("任务已处于完成状态"));
	return;
}
if (ptd.getStatus()==ptd.STATUS_DISCARD) {
	out.print(StrUtil.Alert_Back("任务已处于作废状态"));
	return;
}

TaskDb rootTask = ptd.getTaskDb(ptd.getRootId());
if (rootTask.getStatus()==rootTask.STATUS_FINISHED) {
	out.print(StrUtil.Alert_Back("根任务已处于完成状态"));
	return;
}
if (rootTask.getStatus()==rootTask.STATUS_DISCARD) {
	out.print(StrUtil.Alert_Back("根任务已处于作废状态"));
	return;
}

String privurl = ParamUtil.get(request, "privurl");
String person = ParamUtil.get(request, "person");

TaskDb td = null;
if (!op.equals("new")) {
	int pid = Integer.parseInt(parentid);
	td = new TaskDb();
	td = td.getTaskDb(pid);
	String icon = "";
  	if (td.getType()==0)
		icon = "images/task/icon-task.gif";
  	else if (td.getType()==1)
		icon = "images/task/icon-subtask.gif";
  	else if (td.getType()==2)
		icon = "images/task/icon-result.gif";
  	else if (td.getType()==3)
		icon = "images/task/icon-hurry.gif";
  	else
		icon = "images/task/icon-task.gif";
%>
<table align="center" class="percent98" width="98%"><tr><td width="80"><img src="<%=icon%>" border=0>&nbsp;<strong>父任务</strong>：</td>
  <td>
<%if (td.getExpression()!=0) { %>
	<img align="absmiddle" src="forum/images/emot/em<%=td.getExpression()%>.gif" border=0> 
<%}%>  
<a href="task_show.jsp?rootid=<%=td.getRootId()%>&showid=<%=parentid%>"><%=td.getTitle()%></a></td>
</tr></table>
<%}%>
<form id="frmAnnounce" name=frmAnnounce method="post" action="task_add_do.jsp?op=<%=op%>" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
<table width="98%" border="0" align="center" cellpadding="4" cellspacing="0" class="tabStyle_1 percent98">
    <TBODY>
      <tr>
        <td colspan="2" align="center" class="tabStyle_1_title"><span id=expressspan></span>&nbsp;<%=typedesc%>  
          <input type=hidden name=type value="<%=type%>">
          <input type=hidden name=op value="<%=op%>">
          <input type=hidden name=expression value="0">
          <input type=hidden name=parentid value="<%=parentid%>">
          <input type=hidden name=privurl value="<%=privurl%>">
          <input type=hidden name=projectId value="<%=projectId%>">	    </td> 
      </tr>
    </TBODY>
    <TBODY>
      <tr>
        <td width="10%" align="center">承&nbsp;&nbsp;办&nbsp;&nbsp;者</td>
        <td><%if (op.equals("hurry") || op.equals("addresult")) {//如果是催办或汇报办理结果，则不需再选择承办人%>
        <%=um.getUserDb(person).getRealName()%>
<input type="hidden" name="person" readonly size=40 value="<%=person%>">
<%}else{
	String userRealName = "";
	if (!person.equals("")) {
		com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
		ud = ud.getUserDb(person);
		if (ud!=null && ud.isLoaded()) {
			userRealName = ud.getRealName();
		}
	}
%>
	<input type="hidden" name="person" size=40 value="<%=person%>">
	<input type="text" name="userRealName" readonly size="40" value="<%=userRealName%>">
	<a href=# onClick="javascript:showModalDialog('user_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')">选择承办人</a> 
<%}%>		  <span class="tablebody1">
<input name="jobCode" type="hidden">
<input type="checkbox" name="isUseMsg" value="true" checked>
消息提醒&nbsp;
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
<input name="isToMobile" value="true" type="checkbox" checked />
短信提醒
<%}%>
</span></td>
      </tr>
      <tr>
        <td align="center">标&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;题</td> 
        <td><input name="title" type="text" id="topic" size="55" value="">        </td>
      </tr>
<%if (op.equals("newsubtask")) {%>	  
      <tr>
        <td align="center">开始日期</td>
        <td><input readonly type="text" id="beginDate" name="beginDate" size="10">
            <font color="red">*</font><script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
            </script>&nbsp;&nbsp;&nbsp;&nbsp;结束日期&nbsp;
<input readonly type="text" id="endDate" name="endDate" size="10" />
            <font color="red">*</font>
            <script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
            </script></td>
      </tr>
      <tr>
        <td align="center">是否私密</td>
        <td><select name="secret">
          <option value="false">否</option>
          <option value="true">是</option>
        </select>
(仅发起人与承办人可见)</td>
      </tr>
<%}%> 
		<tr>
		  <td align="center">表&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;情</td>
        <td><iframe src="task_iframe_emote.jsp" height="25" width="610" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
             
        <a href="#" onClick="changeexpression(0)">取消表情&nbsp;</a></td>
      </tr>
      <tr>
        <td align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件</td> 
        <td><input type="file" name="filename" size=40>
				<textarea name="content_hid" style="display:none"> </textarea>
				<input name="button" type=button onClick="AddAttach()" value="增加附件">
            <div id=updiv name=updiv></div></td>
      </tr>
      <tr>
        <td colspan="2">
          <textarea id="content" name="content"></textarea>
          <script>
			CKEDITOR.replace('content', 
				{
					// skin : 'kama',
					toolbar : 'Middle'
				});
		  </script>
        </td>
      </tr>
      <tr style="display:none">
        <td align="left" valign="middle">备注：</td>
        <td align="left" valign="middle"><textarea name="remark" cols="60" rows="3"></textarea></td>
      </tr>
      <tr>
        <td colspan="2" align="center">
        <input type=submit value=" 确 定 " class="btn"></td> 
      </tr>
    </TBODY>
</table>
<br />
</form>
</body>
</html>
