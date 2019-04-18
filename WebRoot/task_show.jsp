<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE>显示任务</TITLE>
<META http-equiv=Content-Type content=text/html; charset=utf-8 charset=utf-8>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="inc/nocache.jsp"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String querystring = fchar.getNullString(request.getQueryString());
String privurl = StrUtil.getUrl(request);

int rootid = ParamUtil.getInt(request, "rootid");
String strshowid = ParamUtil.get(request, "showid");
int showid = -1;
if (!strshowid.equals(""))
	showid = Integer.parseInt(strshowid);
	
String op = ParamUtil.get(request, "op");
if (op.equals("setProgress")) {
	int progress = ParamUtil.getInt(request, "progress", -1);
	if (progress<0 || progress>100) {
		out.print(StrUtil.Alert_Back("进度必须位于0-100之间！"));
		return;
	}
	TaskDb showTask = new TaskDb();
	showTask = showTask.getTaskDb(showid);
	if (showTask==null || !showTask.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "该任务已不存在！"));
		return;
	}
	showTask.setProgress(progress);
	if (progress==100) {
		showTask.setStatus(TaskDb.STATUS_FINISHED);
	}
	if (showTask.save()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "task_show.jsp?rootid=" + rootid + "&showid=" + showid + "&projectId=" + showTask.getProjectId()));
	}
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}

String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String myname = privilege.getUser(request);

if (showid==-1)
	showid = rootid;

int islocked = 0;

TaskDb showTask = new TaskDb();
showTask = showTask.getTaskDb(showid);
if (showTask==null || !showTask.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该任务已不存在！"));
	return;
}
%>
<SCRIPT language=JavaScript>
<!--
function checkclick(msg) {
	if(confirm(msg)) {
		event.returnValue=true;
	}
	else {
		event.returnValue=false;
	}
}

function SymError(){
  return true;
}

window.onerror = SymError;

function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function changefinish(obj,id,isfinish) {
	location.href="task_finish.jsp?isfinish="+isfinish+"&taskid="+id;
}

function OfficeOperate() {
	alert(redmoonoffice.ReturnMessage.substr(0, 4));
}

// 编辑文件
function editdoc(id, attachId){
	rmofficeTable.style.display = "";
	redmoonoffice.AddField("taskId", id);
	redmoonoffice.AddField("attachId", attachId);
	redmoonoffice.Open("<%=Global.getRootPath()%>/task_getfile.jsp?taskId=" + id + "&attachId=" + attachId);
}
//-->
</SCRIPT>
<SCRIPT language=JavaScript src="forum/images/nereidFade.js"></SCRIPT>
<SCRIPT language=JavaScript src="inc/common.js"></SCRIPT>
<SCRIPT>
function checkclick(msg){if(confirm(msg)){event.returnValue=true;}else{event.returnValue=false;}}
function copyText(obj) {var rng = document.body.createTextRange();rng.moveToElementText(obj);rng.select();rng.execCommand('Copy');}
var i=0;
function formCheck(){i++;if (i>1) {document.form.submit1.disabled = true;}return true;}
function presskey(eventobject){if(event.ctrlKey && window.event.keyCode==13){i++;if (i>1) {alert('帖子发送中，请耐心等待！');return false;}this.document.form.submit();}}

function chgProgress(progress) {
	if (!isNumeric(progress)) {
		alert("请输入数字！");
		return false;
	}
	window.location.href = "task_show.jsp?op=setProgress&progress=" + progress + "&rootid=<%=rootid%>&showid=<%=showid%>&projectId=<%=showTask.getProjectId()%>";	
}
</SCRIPT>
</HEAD>
<BODY text=#000000 bgColor=#ffffff leftMargin=0 topMargin=5 marginheight="0" marginwidth="0">
<%
long projectId = ParamUtil.getLong(request, "projectId", -1);
if (projectId==-1) {
%>
<%@ include file="task_inc_menu_top.jsp"%>
<%}else{%>
<%@ include file="project/prj_inc_menu_top.jsp"%>
<%}%>
<div class="spacerH"></div>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/>
<%
UserMgr um = new UserMgr();
TaskPrivilege tp = new TaskPrivilege();

String sql = "";
String title="",mydate="",content="",initiator="",showtitle="",filename="",extname="",person="";
int id;
int orders = 1,type=0;
int pagesize = 10;
int isfinish = 0;
int expression = 1;
int thisrootid = -1;// 这个任务项的rootid

id = showTask.getId();
content = showTask.getContent();
initiator = showTask.getInitiator();
mydate = DateUtil.format(showTask.getMyDate(), "yyyy-MM-dd HH:mm");
title = showTask.getTitle();
showtitle = title;
orders = showTask.getOrders();
type = showTask.getType();
isfinish = showTask.getStatus();
expression = showTask.getExpression();
filename = showTask.getFileName();
extname = showTask.getExt();
person = showTask.getPerson();
thisrootid = showTask.getRootId();
%>
<TABLE borderColor=#d3d3d3 cellSpacing=0 cellPadding=0 width="98%" align=center >
  <TBODY>
  <TR>
    <TD height=25><TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="right-title">
          <TBODY>
            <TR> 
              <TD class="tabStyle_1_title"> 
                <%if (expression!=0) { %>
                <img align="absmiddle" src="forum/images/emot/em<%=expression%>.gif" border=0> 
                <%}%>
                <%=fchar.toHtml(title)%></TD>
              <TD class="tabStyle_1_title" width="37%" align=left> 
                <%
			  boolean isinitiator = myname.equals(initiator)?true:false;//是否为发起人
			  boolean isperson = myname.equals(person)?true:false;//是否为承办人
			  boolean cannewtask = privilege.isUserPrivValid(request,"task");//是否能发起任务
			  %>
              <%if (type==TaskDb.TYPE_TASK && isfinish!=TaskDb.STATUS_DISCARD) { %>
                <%if (isperson && showTask.getParentId()!=TaskDb.NOPARENT) {%>
					<img src=images/task/icon-result.gif align="absmiddle">&nbsp;<a href="task_add.jsp?op=addresult&person=<%=StrUtil.UrlEncode(person)%>&parentid=<%=id%>&projectId=<%=showTask.getProjectId()%>&privurl=<%=privurl%>">汇报办理结果</a> 
                <%}%>
                <%if (isinitiator && showTask.getParentId()!=TaskDb.NOPARENT) {%>
					&nbsp;&nbsp;<img src=images/task/icon-hurry.gif align="absmiddle">&nbsp;<a href="task_add.jsp?op=hurry&person=<%=StrUtil.UrlEncode(person)%>&parentid=<%=id%>&projectId=<%=showTask.getProjectId()%>&privurl=<%=privurl%>">催办</a> 
                <%}%>
				<%if (showTask.getParentId()!=TaskDb.NOPARENT) {%>
					<%if (isperson && cannewtask) {%>
						&nbsp;&nbsp;<img src=images/task/icon-subtask.gif align="absmiddle">&nbsp;<a href="task_add.jsp?op=newsubtask&parentid=<%=id%>&projectId=<%=showTask.getProjectId()%>&privurl=<%=privurl%>">分配子任务</a>
					<%}%>
				<%}else{%>
					<%if (isinitiator && showTask.getStatus()==TaskDb.STATUS_NOTFINISHED) {%>
						&nbsp;&nbsp;<img src=images/task/icon-subtask.gif align="absmiddle">&nbsp;<a href="task_add.jsp?op=newsubtask&parentid=<%=id%>&projectId=<%=showTask.getProjectId()%>&privurl=<%=privurl%>">分配任务</a>
					<%}%>
				<%}%>
              <%}%>
              <%if (type==TaskDb.TYPE_SUBTASK && isfinish!=TaskDb.STATUS_DISCARD) {%>
				  <%if (isperson) {%>
					  &nbsp;&nbsp;<img src=images/task/icon-result.gif align="absmiddle">&nbsp;<a href="task_add.jsp?op=addresult&person=<%=StrUtil.UrlEncode(person)%>&projectId=<%=showTask.getProjectId()%>&parentid=<%=id%>&privurl=<%=privurl%>">汇报办理结果</a> 
				  <%}%>
				  <%if (isinitiator) {%>
					  &nbsp;&nbsp;<img src=images/task/icon-hurry.gif align="absmiddle">&nbsp;&nbsp;<a href="task_add.jsp?op=hurry&person=<%=StrUtil.UrlEncode(person)%>&projectId=<%=showTask.getProjectId()%>&parentid=<%=id%>&privurl=<%=privurl%>">催办</a> 
				  <%}%>
				  <%if ((isperson || isinitiator) && cannewtask) {%>
					  &nbsp;&nbsp;<img src=images/task/icon-subtask.gif align="absmiddle">&nbsp;&nbsp;<a href="task_add.jsp?op=newsubtask&parentid=<%=id%>&projectId=<%=showTask.getProjectId()%>&privurl=<%=privurl%>">分配子任务</a> 
				  <%}%>
              <%}%>
              <%if (type==TaskDb.TYPE_HURRY && isfinish!=TaskDb.STATUS_DISCARD) { //催办%>
				  <%if (isperson) {%>
					  &nbsp;&nbsp;<img src=images/task/icon-result.gif align="absmiddle">&nbsp;<a href="task_add.jsp?op=addresult&person=<%=StrUtil.UrlEncode(person)%>&projectId=<%=showTask.getProjectId()%>&parentid=<%=id%>&privurl=<%=privurl%>">汇报办理结果</a> 
				  <%}%>
              <%}%></TD>
            </TR>
          </TBODY>
        </TABLE> </TD>
</TR></TBODY></TABLE>
	  <table bordercolor=#d3d3d3 cellspacing=0 cellpadding=0 width="98%" align=center 
border=0>
  <tbody> 
  <tr> 
    <td valign=top align=left height=78> 
      <table cellspacing=0 cellpadding=3 width="100%" border=0>
          <tbody>
            <tr bgcolor=#ffffff> 
              <td valign=top align=left height=106> <table style="WORD-BREAK: break-all" 
            height="100%" cellspacing=0 cellpadding=0 width="99%" border=0>
                  <tbody>
                    <tr height=20> 
                      <td> <a name=#content<%=id%>></a> 
						<%if (showTask.getType()==TaskDb.TYPE_TASK) {%>
						  <%if (isfinish==TaskDb.STATUS_FINISHED) {%>
						  <img src="images/task/icon-yes.gif">
						  <%}else if (isfinish==TaskDb.STATUS_NOTFINISHED){%>
						  <img src="images/task/icon-notyet.gif">
						  <%}else if (isfinish==TaskDb.STATUS_DISCARD) {%>
						  <img src="images/task/icon-no.gif">
						  <%}else if (isfinish==TaskDb.STATUS_ARRANGED) {%>
						  <img src="images/task/icon-arranged.gif">
						  <%}else if (isfinish==TaskDb.STATUS_URGENT) {%>
						  <img src="images/task/icon-urgent.gif">
						  <%}%>
					    <%}else{%>
                        <img src="forum/images/posttime.gif" border=0>
						<%}%>
						&nbsp;发布时间：<span title="<%=mydate%>"><%=mydate%></span> 
						&nbsp;<!--[<a href="#<%=id%>">任务树中位置</a>]-->
						<%if (showTask.getType()==TaskDb.TYPE_TASK || showTask.getType()==TaskDb.TYPE_SUBTASK) {%>
						|&nbsp;&nbsp;开始：<%=DateUtil.format(showTask.getBeginDate(), "yyyy-MM-dd")%>
						&nbsp;结束：<%=DateUtil.format(showTask.getEndDate(), "yyyy-MM-dd")%>
						<%}%>
						<% if (tp.canEdit(request, id)) {%> 
                        &nbsp;&nbsp;[<a href="task_edit.jsp?editid=<%=id%>&projectId=<%=showTask.getProjectId()%>&privurl=<%=privurl%>">编辑</a>]
						&nbsp;
						[<a onClick="checkclick('您确定要删除吗?')" href="task_del.jsp?delid=<%=id%>&rootid=<%=rootid%>">删除</a>]<%}%>
						<%if (type==TaskDb.TYPE_TASK) {%>
							<input type="checkbox" name="isfinish" value="<%=TaskDb.STATUS_NOTFINISHED%>"
							<%if(isfinish==TaskDb.STATUS_NOTFINISHED){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_NOTFINISHED%>')">
							未安排 
							<input type="checkbox" name="isfinish1" value="<%=TaskDb.STATUS_ARRANGED%>"
							<%if(isfinish==TaskDb.STATUS_ARRANGED){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_ARRANGED%>')">
							已安排 
							<input type="checkbox" name="isfinish2" value="<%=TaskDb.STATUS_FINISHED%>"
							<%if(isfinish==TaskDb.STATUS_FINISHED){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_FINISHED%>')">
							完成 							
							<input type="checkbox" name="isfinish3" value="<%=TaskDb.STATUS_URGENT%>"
							<%if(isfinish==TaskDb.STATUS_URGENT){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_URGENT%>')">
							迫切 
							<input type="checkbox" name="isfinish4" value="<%=TaskDb.STATUS_DISCARD%>"
							<%if(isfinish==TaskDb.STATUS_DISCARD){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_DISCARD%>')">
							作废
						<%}else if (type==TaskDb.TYPE_SUBTASK) {%>
							<input type="checkbox" name="isfinish" value="<%=TaskDb.STATUS_RECEIVED%>"							
							<%if(isfinish==TaskDb.STATUS_RECEIVED){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_RECEIVED%>')">
							<%=TaskDb.getTaskStatusDesc(TaskDb.STATUS_RECEIVED)%> 
							<input type="checkbox" name="isfinish1" value="<%=TaskDb.STATUS_DOING%>"
							<%if(isfinish==TaskDb.STATUS_DOING){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_DOING%>')">
							<%=TaskDb.getTaskStatusDesc(TaskDb.STATUS_DOING)%> 
							<input type="checkbox" name="isfinish2" value="<%=TaskDb.STATUS_WATI%>"
							<%if(isfinish==TaskDb.STATUS_WATI){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_WATI%>')">
							<%=TaskDb.getTaskStatusDesc(TaskDb.STATUS_WATI)%> 
							<input type="checkbox" name="isfinish3" value="<%=TaskDb.STATUS_FINISHED_NORMAL%>"
							<%if(isfinish==TaskDb.STATUS_FINISHED_NORMAL){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_FINISHED_NORMAL%>')">
							<%=TaskDb.getTaskStatusDesc(TaskDb.STATUS_FINISHED_NORMAL)%> 
							<input type="checkbox" name="isfinish4" value="<%=TaskDb.STATUS_FINISHED_EXPIRE%>"
							<%if(isfinish==TaskDb.STATUS_FINISHED_EXPIRE){%>checked<%}%> onClick="changefinish(this,'<%=id%>','<%=TaskDb.STATUS_FINISHED_EXPIRE%>')">
							<%=TaskDb.getTaskStatusDesc(TaskDb.STATUS_FINISHED_EXPIRE)%> 
						<%}%>
						<%if (type==TaskDb.TYPE_TASK || type==TaskDb.TYPE_SUBTASK) {%>
						进度&nbsp;<input name="progress" style="width:20px" onchange="chgProgress(this.value)" value="<%=showTask.getProgress()%>" onKeyDown="if (window.event.keyCode==13) chgProgress(this.value)">&nbsp;%
						<%}%>						</td>
                    </tr>
                    <tr height=8>
                      <td> <hr width="100%" size=1 style="border-top:1px dotted #cccccc">                      </td>
                    </tr>
                    <tr valign=top> 
                      <td height="78"> <span id="topiccontent" name="topiccontent">
                <%
				if (thisrootid==showTask.getRootId() && !tp.canUserSee(request, showTask))//如果是根任务
					out.print("&nbsp;&nbsp;............");
				else {
					// content = fchar.toHtml(content);
					// out.println(fchar.ubb(content,true)); // 会使插入的表情出问题
					  java.util.Iterator attir = showTask.getAttachments().iterator();
					  while (attir.hasNext()) {
					  	Attachment att = (Attachment)attir.next();
						String ext = StrUtil.getFileExt(att.getDiskName());
					  %>
						 <%if (ext.equalsIgnoreCase("gif") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("bmp")) {%>
							<img style="cursor:hand" onClick="window.open('<%=request.getContextPath()%>/<%=att.getVisualPath()%>/<%=att.getDiskName()%>')" src="<%=request.getContextPath()%>/<%=att.getVisualPath()%>/<%=att.getDiskName()%>" onload="if(this.width>screen.width-333) this.width=screen.width-333" border="0" align="absmiddle"><BR>
						 <%}%>
                      <%}%>				
				<%					
					out.print(content);
				}
				%>
                        </span>
						<%if ((showTask.getType()==TaskDb.TYPE_TASK || showTask.getType()==TaskDb.TYPE_SUBTASK) && !showTask.getRemark().equals("")) {%>						
						<BR>
						<br>
						<div><em>----------------备注----------------</em></div>
						<div>
						<%=showTask.getRemark()%>						</div>
						<%}%>					  </td>
                    </tr>
                    <tr valign=top> 
                      <td height="13"> 
                        <hr width="100%" size=1 style="border-top:1px dotted #cccccc">                      </td>
                    </tr>
                    <tr valign=top height=15>
                      <td height="22" valign="top">发起人：<%=um.getUserDb(initiator).getRealName()%> &nbsp;&nbsp;&nbsp;&nbsp;
					  <%if (showTask.getParentId()!=-1) {%>
					  承办人：<%=um.getUserDb(person).getRealName()%> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					  <%}else{%>
					  根任务
					  <%}%>
					  &nbsp;&nbsp;发起时间：<%=mydate%></td>
                    </tr>
                    <tr valign=top height=15>
                      <td>附件：
                        <%
					  java.util.Iterator attir = showTask.getAttachments().iterator();
					  while (attir.hasNext()) {
					  	Attachment att = (Attachment)attir.next();
						String ext = StrUtil.getFileExt(att.getDiskName());
					  %>
                         <div style="height:30px;margin-top:10px;"><img src="images/attach.gif" align="absmiddle"><a target="_blank" href="task_getfile.jsp?taskId=<%=showTask.getId()%>&attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;
						 <!--
						 <%if (ext.equals("doc") || ext.equals("xls")) {%>
							<a href="javascript:editdoc('<%=showTask.getId()%>', '<%=att.getId()%>')" title="编辑Office文件"><img src="netdisk/images/btn_edit_office.gif" width="16" height="16" border="0" align="absmiddle"></a>
						 <%}%>
						 -->
						 </div>
                      <%}%>
		  <table id="rmofficeTable" name="rmofficeTable" style="display:none" width="29%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#CCCCCC">
            <tr>
              <td height="22" align="center" bgcolor="#E3E3E3"><strong>&nbsp;编辑Office文件</strong></td>
            </tr>
            <tr>
              <td align="center" bgcolor="#FFFFFF"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
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
                  <!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />--></td>
            </tr>
          </table>					  </td>
                    </tr>
                  </tbody>
                </table></td>
            </tr>
          </tbody>
        </table>
    </td>
  </tr>
  </tbody>
</table>
<%
sql = "select id from task where rootid="+rootid+" ORDER BY orders";
ListResult lr = showTask.listResult(sql);

int layer = 1;
int i = 1;
boolean isshow = false;
Iterator ir = lr.getResult().iterator();
if (ir.hasNext()) {
	// 写根任务
	TaskDb td = (TaskDb)ir.next();
	id = td.getId();
	title = td.getTitle();
	type = td.getType();
	expression = td.getExpression();
	initiator = td.getInitiator();
	person = td.getPerson();
	layer = td.getLayer();
	isfinish = td.getStatus();
	
	isshow = tp.canUserSee(request, td);
%>
<table style="border:1px solid #edeced" cellspacing=0 cellpadding=1 width="98%" align=center>
  <tbody> 
  <tr> 
    <td height="21" align=left valign="middle" noWrap bgcolor=#f8f8f8>
	<div style="margin-left:5px;width:373px;float:left">
	<a name=#<%=id%>></a>
		  <%if (td.getType()==TaskDb.TYPE_TASK) {%>	  
          <%if (isfinish==TaskDb.STATUS_FINISHED) {%>
          <img src="images/task/icon-yes.gif">
          <%}else if (isfinish==TaskDb.STATUS_NOTFINISHED){%>
          <img src="images/task/icon-notyet.gif">
          <%}else if (isfinish==TaskDb.STATUS_DISCARD) {%>
          <img src="images/task/icon-no.gif">
	      <%}else if (isfinish==TaskDb.STATUS_ARRANGED) {%>
          <img src="images/task/icon-arranged.gif">
		  <%}else if (isfinish==TaskDb.STATUS_URGENT) {%>
          <img src="images/task/icon-urgent.gif">
		  <%}
		  }else if (td.getType()==td.TYPE_SUBTASK) {%>
		  <%=TaskDb.getTaskStatusDesc(showTask.getStatus())%>
		  <%}%>
        <%
	  if (isshow)
	  {
		  if (type==0)
			out.println("<img src=images/task/icon-task.gif>");
		  else if (type==1)
			out.println("<img src=images/task/icon-subtask.gif>");
		  else if (type==2)
			out.println("<img src=images/task/icon-result.gif>");
		  else if (type==3)
			out.println("<img src=images/task/icon-hurry.gif>");
		  else
			out.println("<img src=images/task/icon-task.gif>");
		  %>
				  <%if (expression!=0) { %>
				  <img align="absmiddle" src="forum/images/emot/em<%=expression%>.gif" border=0>
				  <%}%>
        <% if (id!=showid) { %>
	        <a href="task_show.jsp?rootid=<%=rootid%>&showid=<%=id%>&projectId=<%=showTask.getProjectId()%>"><%=title%></a> 
        <% } else { %>
        <font color=red><%=title%></font>
        <% }
	}else {%> 
	............
	<%}%>
	</div>
	<table title="<%=td.getProgress()%>%" width="100px" border="0" cellpadding="0" cellspacing="0" style="height:6px;float:left;margin-top:5px">
      <tr>
        <td bgcolor="#CCCCCC" style="height:6px;padding:0px"><img src="forum/images/vote/bar7.gif" width="<%=showTask.getProgress()%>%" height="6" /></td>
      </tr>
    </table></td>
    </tr>
  </tbody> 
</table>
<%
}
		while (ir.hasNext())
		{
		  i++;
		  TaskDb td = (TaskDb)ir.next();
		  id = td.getId();
		  TaskDb parentTd = td.getTaskDb(td.getParentId());
		  layer = td.getLayer();
		  initiator = td.getInitiator();
		  mydate = DateUtil.format(td.getMyDate(), "yyyy年MM月dd日");
		  title = td.getTitle();
		  type = td.getType();
		  isfinish = td.getStatus();
		  expression = td.getExpression();
 		  person = td.getPerson();
		  
		  isshow = tp.canUserSee(request, td);
	  %>
<table cellspacing=0 cellpadding=0 width="98%" align=center border=0 style="padding:0; margin:0">
  <tbody> 
  <tr> 
    <td height="13" align=left noWrap style="padding:0; margin:0" class="highlight">
	<%
	int divWidth = 380;
	if (td.getType()==TaskDb.TYPE_RESULT || td.getType()==TaskDb.TYPE_HURRY) {
		divWidth = 480;
	}
	%>
	<div style="float:left;width:<%=divWidth%>px;white-space: nowrap;word-break: keep-all;overflow: hidden;text-overflow: ellipsis;">
    <img src="" width=30 height=1 style="visibility:hidden">
	<%
	layer = layer-1;
	for (int k=1; k<=layer-1; k++)
	{ %>
      <img src="forum/images/bbs_dir/line.gif" width=18>
      <% }%>
        <img src="forum/images/bbs_dir/join.gif" width="18" height="22">
        <%if (td.getType()==TaskDb.TYPE_TASK) {
          if (isfinish==TaskDb.STATUS_FINISHED) {%>
          <img src="images/task/icon-yes.gif">
          <%}else if (isfinish==TaskDb.STATUS_NOTFINISHED){%>
          <img src="images/task/icon-notyet.gif">
          <%}else if (isfinish==TaskDb.STATUS_DISCARD) {%>
          <img src="images/task/icon-no.gif">
	      <%}else if (isfinish==TaskDb.STATUS_ARRANGED) {%>
          <img src="images/task/icon-arranged.gif">
		  <%}else if (isfinish==TaskDb.STATUS_URGENT) {%>
          <img src="images/task/icon-urgent.gif">
		  <%}
		}%>
        <%
	  if (isshow) {
		  if (type==0)
			out.println("<img src=images/task/icon-task.gif>");
		  else if (type==1)
			out.println("<img src=images/task/icon-subtask.gif>");
		  else if (type==2)
			out.println("<img src=images/task/icon-result.gif>");
		  else if (type==3)
			out.println("<img src=images/task/icon-hurry.gif>");
		  else
			out.println("<img src=images/task/icon-task.gif>");
		  %>	  
		  <%if (expression!=0) { %>
			<img align="absmiddle" src="forum/images/emot/em<%=expression%>.gif" border=0>
	      <%}%>
	      <%
		  if (id!=showid) { %>
	      	<a href="task_show.jsp?rootid=<%=rootid%>&showid=<%=id%>&projectId=<%=td.getProjectId()%>" title="<%=title%>"><%=title%></a> &nbsp;&nbsp;&nbsp;&nbsp;
          <% } else { %>
		  	<font color=red><%=title%></font><a name="#<%=showid%>"></a>&nbsp;&nbsp;<!--<a href="#content<%=showid%>">回到顶部</a>-->
		  <% }%>
		</div>
		<%if (td.getType()==TaskDb.TYPE_SUBTASK) {%>
		<table title="<%=td.getProgress()%>%" width="100px" border="0" cellpadding="0" cellspacing="0" style="height:6px;float:left;margin-top:5px;margin-left:0px">
          <tr>
            <td bgcolor="#CCCCCC" style="height:6px;padding:0px"><img src="forum/images/vote/bar7.gif" width="<%=td.getProgress()%>%" height="6" /></td>
          </tr>
        </table>
		<%}%>		
		<div style="float:left;margin-left:10px">
		<%if (td.getType()==TaskDb.TYPE_SUBTASK) {%>
		  [<%=TaskDb.getTaskStatusDesc(td.getStatus())%>]
		<%}%>	  
		  <%if (type==0 || type==1 || type==3) {%>
			  [&nbsp;
			  <%
			  UserDb ud = new UserDb();
			  ud = ud.getUserDb(initiator);
			  out.print(ud.getRealName());
			  %>
			  →
			  <%
			  ud = ud.getUserDb(person);
			  out.print(ud.getRealName());
			  %>
			  &nbsp;<%=mydate%>&nbsp;]
		  <%}else if (type==2) {%>
		  	  [汇报人：<%
			  UserDb ud = new UserDb();
			  ud = ud.getUserDb(initiator);
			  out.print(ud.getRealName());
			  %>]
		  <%}%>
		</div>
	   <%
	   }
	   else
	   		out.print("............");
	   %>      </td>
    </tr>
  </tbody> 
</table>
<%	}%>
</BODY>
</HTML>
