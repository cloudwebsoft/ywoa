<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.task.TaskDb"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>发起任务</title>
<link href="common.css" rel="stylesheet" type="text/css">
<STYLE>TABLE {
	BORDER-TOP: 0px; BORDER-LEFT: 0px; BORDER-BOTTOM: 1px
}
TD {
	BORDER-RIGHT: 0px; BORDER-TOP: 0px
}
</STYLE>
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
		expressspan.innerHTML = "无";
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
	frmAnnounce.content.value = IframeID.document.body.innerHTML;
}

function window_onload() {
	cws_Size(320);
}
</script>
</head>
<body leftmargin="0" topmargin="5" onLoad="window_onload()">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flowId = ParamUtil.getInt(request, "flowId");
int actionId = ParamUtil.getInt(request, "actionId");
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);

int taskId = wa.getTaskId();
// 检查对应于动作的任务是否已存在，如果已存在，则重定向至修改任务
TaskDb td = new TaskDb();
td = td.getTaskDb(taskId);
if (td!=null && td.isLoaded()) {
	response.sendRedirect("task_show.jsp?rootid=" + td.getId() + "&showid=" + td.getId());
	return;
}

String op = fchar.getNullStr(request.getParameter("op"));
if (op.equals(""))
{
	out.print(fchar.Alert("您未选择操作方式！"));
	return;
}
String typedesc = "";//类型描述
int type=0;
if (op.equals("newflowtask"))
{
	type = 0;
	typedesc = "发起新任务";
	priv="task";
	if (!privilege.isUserPrivValid(request,priv))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String parentid = fchar.getNullStr(request.getParameter("parentid"));
String privurl=ParamUtil.get(request, "privurl");
String person = ParamUtil.get(request, "person");
%>
<table width="498" border="1" align="center" cellpadding="4" cellspacing="0" class="main">
  <form id="frmAnnounce" name=frmAnnounce method="post" action="task_add_do.jsp" enctype="MULTIPART/FORM-DATA" onSubmit="return frmAnnounce_onsubmit()">
    <TBODY>
      <tr> 
        <td class="right-title"> <font color="#FFFFFF"><%=typedesc%> 
          <input type=hidden name=type value="<%=type%>">
          <input type=hidden name=op value="<%=op%>">
          &nbsp; 表情 <span id=expressspan>无</span> 
          <input type=hidden name=expression value="0">
          <input type=hidden name=parentid value="<%=parentid%>">
          <input type=hidden name=privurl value="<%=privurl%>">
          </font></td>
      </tr>
    </TBODY>
    <TBODY>
      
      <tr> 
        <td width="486"> 任务标题 
          <input name="title" type="text" id="topic" size="55"  title="不得超过 25 个汉字或50个英文字符" maxlength="80">
        <span class="tablebody1">
        <input name="jobCode" type="hidden">
        </span> <span class="tablebody1">
        <input name="person" type="hidden">
        </span></td>
      </tr>
      <tr> 
        <td><table width="100%" border=0 cellspacing=0 cellpadding=0>
            <tr> 
              <td class=tablebody1 valign=top height=30> 附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件 
                <input type="file" name="filename" size=40>
                限制：200K 
				<input name="content" type="hidden">				<input name="actionId" type="hidden" value="<%=actionId%>"></td>
            </tr>
          </table></td>
      </tr>
      <tr>
        <td>流程附件
		  <%
		  int doc_id = wf.getDocId();
		  DocumentMgr dm = new DocumentMgr();
		  Document doc = dm.getDocument(doc_id);
		  
		  java.util.Vector attachments = doc.getAttachments(1);
		  java.util.Iterator ir = attachments.iterator();
  
		  while (ir.hasNext()) {
		  	Attachment am = (Attachment) ir.next(); %>
          <table width="61%"  border="0" cellpadding="0" cellspacing="0" bordercolor="#D6D3CE">
            <tr>
              <td width="5%" height="24" align="right"><img src="images/attach.gif" /></td>
              <td width="73%">&nbsp; <a target="_blank" href="<%=am.getVisualPath() + "/" + am.getDiskName()%>"><%=am.getName()%></a><br />
              </td>
              <td width="22%"><input type=checkbox name="attachIds" value="<%=am.getId()%>" checked>
                附加至任务</td>
            </tr>
          </table>
        <%}%>
		</td>
      </tr>
      <tr> 
        <td><%@ include file="editor_full/editor.jsp"%></td>
      </tr>
      <tr align="center" bordercolor="#0078bf">
        <td><b>点击表情图可加入相应的表情</b><br>
            <a href="#" onClick="changeexpression(0)">无&nbsp;</a>
            <%
for (int i=1; i<=71; i++)
{
	out.println("<img src=\"forum/images/emot/em"+i+".gif\" border=0 onclick=\"changeexpression("+i+")\" style=\"CURSOR: hand\">&nbsp;");
	if (i%14==0)
		out.print("<BR>");
}
%>        </td>
      </tr>
      <tr> 
        <td align="center">
            <input name="submit" type=submit value="发起任务">
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
          <input name="reset" type=reset value=" 重 设 ">          </td>
      </tr>
    </TBODY>
  </form>
</table>
</body>
</html>
