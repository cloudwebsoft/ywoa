<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>编辑任务</title>
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
	frmAnnounce.person.value = user;
	frmAnnounce.jobCode.value = deptCode;
}

function frmAnnounce_onsubmit() {
}

function window_onload() {
}

var attachCount = 1;
function AddAttach() {
	updiv.insertAdjacentHTML("BeforeEnd", "<input type='file' name='filename" + attachCount + "' size=40><br />");
	attachCount += 1;
}

function OfficeOperate() {
	alert(frmAnnounce.redmoonoffice.ReturnMessage.substr(0, 4));
}

// 编辑文件
function editdoc(id, attachId)
{
	rmofficeTable.style.display = "";
	frmAnnounce.redmoonoffice.AddField("taskId", id);
	frmAnnounce.redmoonoffice.AddField("attachId", attachId);
	frmAnnounce.redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/task_getfile.jsp?taskId=" + id + "&attachId=" + attachId);
}
</script>
</head>
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
<br>
<%
int i=0;
int editid = ParamUtil.getInt(request, "editid");
String privurl = request.getParameter("privurl");
String title="",content="",filename="",extname="",person="";
int expression=0;
int type=0;

TaskDb task = new TaskDb();
task = task.getTaskDb(editid);

String op = ParamUtil.get(request, "op");
if (op.equals("delattach")) {
	int attId = ParamUtil.getInt(request, "attachId");
	boolean re = task.delAttachment(attId);
	if (re)
		out.print(StrUtil.Alert("删除成功！"));
	else
		out.print(StrUtil.Alert("删除失败！"));
	task = task.getTaskDb(editid);
}

int rootid = task.getRootId();
title = task.getTitle();
content = task.getContent();
expression = task.getExpression();
filename = task.getFileName();
extname = task.getExt();
person = task.getPerson();
type = task.getType();

TaskDb rootTask = task.getTaskDb(rootid);
%>
<table width="100%" class="percent98" border="0">
  <tr>
    <td height="24"><strong>&nbsp;&nbsp;<img src=images/task/icon-task.gif align="absmiddle">&nbsp;根任务：
        <%if (rootTask.getExpression()!=0) { %>
      <img align="absmiddle" src="forum/images/emot/em<%=rootTask.getExpression()%>.gif" border=0>
      <%}%>
    <a href="task_show.jsp?rootid=<%=rootid%>&showid=<%=rootid%>"><%=task.getTaskDb(rootid).getTitle()%></a></strong></td>
  </tr>
</table>
<form  name=frmAnnounce method="post" action="task_edit_do.jsp" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
<table width="98%" border="0" align="center" cellpadding="4" cellspacing="0" class="tabStyle_1 percent98">
    <TBODY>
      <tr>
        <td colspan="2" class="tabStyle_1_title"><span id=expressspan> 
		  <%if (expression!=0) {%>
          <img align="absmiddle" src="forum/images/emot/em<%=expression%>.gif" border=0><%}%>
          </span>&nbsp;          
          <input type=hidden name=expression value="<%=expression%>">
          <input type=hidden name=type value="<%=type%>">
        <a href="task_show.jsp?rootid=<%=rootid%>&showid=<%=editid%>"><%=title%></a></td> 
      </tr>
    </TBODY>
    <TBODY>
	<%if (task.getParentId()!=task.NOPARENT) {%>
      <tr>
        <td width="10%" align="center">承&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;办</td>
        <td width="90%"><input type="text" name="person" readonly size=40 value="<%=person%>">
          <a href=# onClick="javascript:showModalDialog('user_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')">选择承办人</a>
		  <input type="checkbox" name="isUseMsg" value="true" checked>消息提醒
		&nbsp;
		<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
        <input name="isToMobile" value="true" type="checkbox" checked />
短信提醒
<%}%></td>
      </tr>
	<%}%>
      <tr>
        <td align="center">标&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;题</td> 
        <td><input name="title" type="text" id="title" size="55" value="<%=title%>" title="不得超过 25 个汉字或50个英文字符" maxlength="80"> 
          <font color="#FFFFFF"> 
          <input type=hidden name=privurl value="<%=privurl%>">
          <input type=hidden name="editid" value="<%=editid%>">
          </font> <span class="tablebody1">
          <input name="jobCode" type="hidden" value="<%=task.getJobCode()%>">
          </span>
		  <%if (task.getParentId()==task.NOPARENT) {%>
          <input type="hidden" name="person" value="<%=person%>">
		  <%}%></td>
      </tr>
<%if (task.getType()==TaskDb.TYPE_TASK || task.getType()==TaskDb.TYPE_SUBTASK) {%>	  
      <tr>
        <td align="center">开始日期</td>
        <td><input readonly type="text" id="beginDate" name="beginDate" size="10" value="<%=DateUtil.format(task.getBeginDate(), "yyyy-MM-dd")%>">
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
<input readonly type="text" id="endDate" name="endDate" size="10" value="<%=DateUtil.format(task.getEndDate(), "yyyy-MM-dd")%>">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>&nbsp;</td>
      </tr>
      <tr>
        <td align="center">是否私密</td>
        <td><select id="secret" name="secret">
          <option value="false">否</option>
          <option value="true">是</option>
        </select>
          <script>
			frmAnnounce.secret.value = "<%=task.isSecret()?"true":"false"%>";
          </script>
(仅发起人与承办人可见)</td>
      </tr>
<%}%>	  
      <tr>
        <td align="center">表&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;情</td>
        <td><iframe src="task_iframe_emote.jsp?expression=<%=expression%>" height="25" width="610" marginwidth="0" marginheight="0" frameborder="0" scrolling="yes"></iframe>
             
          <a href="#" onClick="changeexpression(0)">取消表情&nbsp;</a></td>
      </tr>
      <%if (task.getType()==TaskDb.TYPE_TASK || task.getType()==TaskDb.TYPE_SUBTASK) {%>	  	        
      <tr>
        <td align="center">备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注</td>
        <td><input name="remark" value="<%=task.getRemark()%>" size="40" /></td>
      </tr>
      <%}%>
      <tr>
        <td align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件</td> 
        <td><input type="file" name="filename" size=40>
                <input name="button" type=button onClick="AddAttach()" value="增加附件">
				<div id=updiv name=updiv></div></td>
      </tr>
      <tr>
        <td colspan="2">
          <textarea id="content" name="content"><%=content%></textarea>
          <script>
			CKEDITOR.replace('content', 
				{
					// skin : 'kama',
					toolbar : 'Middle'
				});
		  </script>
          <table id="rmofficeTable" name="rmofficeTable" style="display:none" width="29%"  border="0" align="center" cellpadding="0" cellspacing="1">
            <tr>
              <td height="22" align="center"><strong>&nbsp;编辑Office文件</strong></td>
            </tr>
            <tr>
              <td align="center"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                  <param name="Encode" value="utf-8" />
                  <param name="BackColor" value="0000ff00" />
                  <param name="Server" value="<%=Global.server%>" />
                  <param name="Port" value="<%=Global.port%>" />
                  <!--设置是否自动上传-->
                  <param name="isAutoUpload" value="1" />
                  <!--设置文件大小不超过1M-->
                  <param name="MaxSize" value="1024000" />
                  <!--设置自动上传前出现提示对话框-->
                  <param name="isConfirmUpload" value="1" />
                  <!--设置IE状态栏是否显示信息-->
                  <param name="isShowStatus" value="0" />
                  <param name="PostScript" value="<%=Global.virtualPath%>/task_office_upload.jsp" />
				  <%
                  com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
                  %>
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />                  
                </object>
                  <!--<input name="remsg" type="button" onclick='alert(frmAnnounce.redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />--></td>
            </tr>
        </table>
        </td>
      </tr>
      <%
	  Vector vatt = task.getAttachments();
	  if (vatt.size()>0) {
	  %>
      <tr>
        <td>附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
        <td><%
					  java.util.Iterator attir = vatt.iterator();
					  while (attir.hasNext()) {
					  	Attachment att = (Attachment)attir.next();
						String ext = StrUtil.getFileExt(att.getDiskName());
					  %>
          <img src="images/attach2.gif" align="absmiddle" /><a target="_blank" href="task_getfile.jsp?taskId=<%=task.getId()%>&attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;[<a onclick="return confirm('您确定要删除么？')" href="task_edit.jsp?op=delattach&editid=<%=editid%>&attachId=<%=att.getId()%>&privurl=<%=StrUtil.UrlEncode(privurl)%>">删除</a>]
          <%if (ext.equals("doc") || ext.equals("xls")) {%>
          &nbsp;&nbsp;<a href="javascript:editdoc('<%=task.getId()%>', '<%=att.getId()%>')" title="编辑Office文件"><img src="netdisk/images/btn_edit_office.gif" border="0" align="absmiddle" /></a>
          <%}%>
        <%}%></td>
      </tr>
      <%}%>
      <tr>
        <td colspan="2" align="center">
        <input class="btn" name="submit" type=submit value=" 确 定 "></td>
      </tr>
    </TBODY>
</table>
</form>
</body>
</html>
