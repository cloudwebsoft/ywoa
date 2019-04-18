<%@ page contentType="text/html; charset=utf-8"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ page import="com.redmoon.oa.ui.*"%>
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
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function changeexpression(i)
{
	frmAnnounce.expression.value = i;
	if (i==0)
	{
		expressspan.innerHTML = "";
		return;
	}
	expressspan.innerHTML = "<img align=absmiddle src=forum/images/emot/em"+i+".gif>";
}

function setPerson(deptCode, deptName, user)
{
	if (frmAnnounce.person.value=="") {
		frmAnnounce.person.value = user;
		frmAnnounce.jobCode.value = deptCode;
	}
	else {
		frmAnnounce.person.value += "," + user;
		frmAnnounce.jobCode.value += "," + deptCode;
	}
}

function frmAnnounce_onsubmit() {
	if (frmAnnounce.beginDate.value=="" || frmAnnounce.endDate.value=="") {
		alert("开始和结束日期不能为空！");
		return false;
	}
}

function window_onload() {
}

var attachCount = 1;
function AddAttach() {
	updiv.insertAdjacentHTML("BeforeEnd", "<table width=100% style='padding:0px;margin:0px;border:0px' cellspacing='0'><tr><td style='border:0px;padding:0px'><input type='file' name='filename" + attachCount + "' size=40></td></tr></table>");
	attachCount += 1;
}
</script>
</head>
<%
String priv="task";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<body onLoad="window_onload()">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
long projectId = ParamUtil.getLong(request, "projectId", -1);
if (projectId==-1) {
%>
<%@ include file="task_inc_menu_top.jsp"%>
<%}else{%>
<%@ include file="project/prj_inc_menu_top.jsp"%>
<%}%>
<div class="spacerH"></div>
<%
String op = fchar.getNullStr(request.getParameter("op"));
if (op.equals("")) {
	out.print(fchar.Alert("您未选择操作方式！"));
	return;
}
String typedesc = "";// 类型描述
int type=0;
if (op.equals("new")) {
	type = 0;
	typedesc = "发起新任务";
	priv="task";
	if (!privilege.isUserPrivValid(request,priv)){
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
if (op.equals("newsubtask")) {
	type = 1;
	typedesc = "发起子任务";
	priv="task";
	if (!privilege.isUserPrivValid(request,priv))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
if (op.equals("addresult")) {
	type = 2;
	typedesc = "汇报办理结果";
}
if (op.equals("hurry")) {
	type = 3;
	typedesc = "催办";
}
String parentid = fchar.getNullStr(request.getParameter("parentid"));
String privurl=fchar.UnicodeToGB(request.getParameter("privurl"));
String person = fchar.UnicodeToGB(fchar.getNullStr(request.getParameter("person")));

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "privurl", privurl, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

%>
<table width="988" border="0" align="center" cellpadding="4" cellspacing="0" class="tabStyle_1 percent80">
  <form id="frmAnnounce" name=frmAnnounce method="post" action="task_add_do.jsp?op=<%=op%>" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
    <TBODY>
      <tr>
        <td colspan="2" class="tabStyle_1_title"><span id=expressspan></span>&nbsp;<%=typedesc%> 
          <input type=hidden name=type value="<%=type%>">
          <input type=hidden name=op value="<%=op%>">
          <input type=hidden name=expression value="0">
          <input type=hidden name=parentid value="<%=parentid%>">
          <input type=hidden name=privurl value="<%=privurl%>">
          <input type=hidden name=projectId value="<%=projectId%>">
        </td> 
      </tr>
    </TBODY>
    <TBODY>
      <tr>
        <td width="11%" align="center">任务标题</td> 
        <td width="89%"><input name="title" type="text" id="topic" size="55"  title="不得超过 25 个汉字或50个英文字符" maxlength="80">
        <span class="tablebody1">
        <input name="jobCode" type="hidden">
        </span> <span class="tablebody1">
        <input name="person" type="hidden">
        </span></td>
      </tr>
      <tr>
        <td align="center">开始日期</td>
        <td><input readonly type="text" id="beginDate" name="beginDate" size="10"><span style="color:red">*</span>
		<script type="text/javascript">
        Calendar.setup({
            inputField     :    "beginDate",      // id of the input field
            ifFormat       :    "%Y-%m-%d",       // format of the input field
            showsTime      :    false,            // will display a time selector
            singleClick    :    false,           // double-click mode
            align          :    "Tl",           // alignment (defaults to "Bl")		
            step           :    1                // show all years in drop-down boxes (instead of every other year as default)
        });
        </script>
        &nbsp;结束日期&nbsp;&nbsp;
        <input readonly type="text" id="endDate" name="endDate" size="10"><span style="color:red">*</span>
        <script type="text/javascript">
            Calendar.setup({
                inputField     :    "endDate",      // id of the input field
                ifFormat       :    "%Y-%m-%d",       // format of the input field
                showsTime      :    false,            // will display a time selector
                singleClick    :    false,           // double-click mode
                align          :    "Tl",           // alignment (defaults to "Bl")		
                step           :    1                // show all years in drop-down boxes (instead of every other year as default)
            });
        </script>
        </td>
      </tr>
      <tr>
        <td align="center">是否私密</td>
        <td><select name="secret">
            <option value="false">否</option>
            <option value="true">是</option>
          </select>
(仅发起人与承办人可见)</td>
      </tr>
      <tr>
        <td align="center">表&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;情</td>
        <td><iframe src="task_iframe_emote.jsp" height="25" width="610" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
            
        <a href="#" onClick="changeexpression(0)">取消表情&nbsp;</a></td>
      </tr>
      <tr>
        <td align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件</td> 
        <td><input type="file" name="filename" size=40>
				<input class="btn" name="button" type=button onClick="AddAttach()" value="增加附件">
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
        <td align="center">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注</td>
        <td align="left"><textarea name="remark" cols="60" rows="3"></textarea></td>
      </tr>
      <tr>
        <td colspan="2" align="center">
        <input name="submit" type=submit value="发起任务" class="btn"></td> 
      </tr>
    </TBODY>
  </form>
</table>
<br />
</body>
</html>
