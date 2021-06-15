<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.robot.*"%>
<%@ page import="cn.js.fan.module.cms.kernel.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
int id = ParamUtil.getInt(request, "id");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HEAD><TITLE>Administrator's Control Panel</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="default.css" rel="stylesheet" type="text/css">
<script src="../inc/common.js"></script>
<script>
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

function SelectDateTime(objName) {
	var dt = showModalDialog("../util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no;");
	if (dt!=null)
		findObj(objName).value = dt;
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
RobotDb rd = new RobotDb();
rd = (RobotDb)rd.getQObjectDb(new Integer(id));
if (op.equals("modify")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, rd, "cms_robot_save")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?id=" + id));
			return;
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}

if (op.equals("addJob")) {
	QObjectMgr qom = new QObjectMgr();
	JobUnitDb ju = new JobUnitDb();
	try {
		if (qom.create(request, ju, "scheduler_add"))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?id=" + id ));
		else
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}

if (op.equals("editJob")) {
	QObjectMgr qom = new QObjectMgr();
	int jobId = ParamUtil.getInt(request, "jobId");
	JobUnitDb ju = new JobUnitDb();
	ju = (JobUnitDb)ju.getQObjectDb(jobId);
	try {
	if (qom.save(request, ju, "scheduler_edit"))
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?id=" + id));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}

if (op.equals("delJob")) {
	JobUnitDb jud = new JobUnitDb();
	int delid = ParamUtil.getInt(request, "jobId");
	JobUnitDb ldb = (JobUnitDb)jud.getQObjectDb(new Integer(delid));
	if (ldb.del())
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "robot_edit.jsp?id=" + id));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="64%" class="head"><lt:Label res="res.label.forum.admin.robot_list" key="output_robot"/></td>
      <td width="36%" class="head"><TABLE width="312" border=0 align=right cellPadding=0 cellSpacing=0 summary="">
        <TBODY>
          <TR>
            <TD><A class=view 
            href="robot_list.jsp"><lt:Label res="res.label.forum.admin.robot_list" key="browse_robot"/></A></TD>
            <TD><A class=add 
            href="robot_add.jsp"><lt:Label res="res.label.forum.admin.robot_list" key="add_robot"/></A></TD>
            <TD><A class=other 
            href="robot_import.jsp"><lt:Label res="res.label.forum.admin.robot_list" key="import_robot"/></A></TD>
          </TR>
        </TBODY>
      </TABLE></td>
    </tr>
  </tbody>
</table>
<TABLE id=pagehead cellSpacing=0 cellPadding=0 width="100%" summary="" border=0><TBODY>
  <TR>
    <TD width="16%">&nbsp;</TD>
    <TD width="84%" class=actions>&nbsp;</TD>
  </TR></TBODY></TABLE>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <TBODY>
  <TR>
    <TD align="left" class="thead"><%=rd.getString("name")%></TD>
    </TR>
  <TR>
    <TD align="center" bgcolor="#FFFFFF">
	<textarea rows="30" style="width:98%"><%=rd.Export()%></textarea>
	</TD>
    </TR></TBODY></TABLE>
</BODY></HTML>
