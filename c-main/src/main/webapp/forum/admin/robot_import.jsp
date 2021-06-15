<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.robot.*"%>
<%@ page import="cn.js.fan.module.cms.kernel.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HEAD><TITLE>Administrator's Control Panel</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="default.css" rel="stylesheet" type="text/css">
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
if (op.equals("import")) {
	String name = ParamUtil.get(request, "name");
	String robotStr = ParamUtil.get(request, "robotStr");
	if (name.equals("")) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.admin.robot_list", "error_no_null")));
		return;
	}
	boolean re = false;
	try {
		re = rd.Import(name, robotStr);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	catch (ResKeyException e1) {
		out.print(StrUtil.Alert_Back(e1.getMessage(request)));
		return;	
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "robot_list.jsp"));
		return;
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		return;
	}
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="64%" class="head"><lt:Label res="res.label.forum.admin.robot_list" key="import_robot"/></td>
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
<TABLE id=pagehead cellSpacing=0 cellPadding=0 width="100%" summary="" border=0>
<TBODY>
  <TR>
    <TD width="16%">&nbsp;</TD>
    <TD width="84%" class=actions>&nbsp;</TD>
  </TR></TBODY></TABLE>
  <TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class=maintable style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
<form name=form1 action="?op=import" method="post">
  <TBODY>
  <TR>
    <TD align="left" class="thead"><lt:Label res="res.label.forum.admin.robot_list" key="input"/>：</TD>
    </TR>
  <TR>
    <TD height="24" align="left" bgcolor="#FFFFFF"><lt:Label res="res.label.forum.admin.robot_list" key="name"/>：<input name="name"></TD>
  </TR>
  <TR>
    <TD align="center" bgcolor="#FFFFFF"><textarea name="robotStr" rows="25" style="width:98%"></textarea></TD>
    </TR>
  <TR>
    <TD align="center" bgcolor="#FFFFFF">
	<input name="submit" type="submit" value="<lt:Label key="ok"/>">
	</TD>
  </TR>
  </TBODY>
</form>
</TABLE>
</BODY></HTML>
