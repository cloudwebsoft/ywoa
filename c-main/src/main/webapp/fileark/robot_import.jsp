<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.fileark.robot.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html>
<html>
<TITLE>Robot Import</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</HEAD>
<BODY>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
RobotDb rd = new RobotDb();
if (op.equals("import")) {
	String name = ParamUtil.get(request, "name");
	String robotStr = ParamUtil.get(request, "robotStr");
	if (name.equals("")) {
		out.print(StrUtil.jAlert_Back("机器人名称不能为空！","提示"));
		return;
	}
	boolean re = false;
	try {
		re = rd.Import(name, robotStr);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	catch (ResKeyException e1) {
		out.print(StrUtil.jAlert_Back(e1.getMessage(request),"提示"));
		return;	
	}	
	if (re) {
		out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_operate_success"),"提示", "robot_list.jsp"));
		return;
	}
	else {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"),"提示"));
		return;
	}
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td width="64%" class="tdStyle_1">导入机器人</td>
      <td width="36%" class="tdStyle_1"><TABLE width="312" border=0 align=right cellPadding=0 cellSpacing=0 summary="">
        <TBODY>
          <TR>
            <TD><A class=view 
            href="robot_list.jsp">浏览机器人</A></TD>
            <TD><A class=add 
            href="robot_add.jsp">添加新机器人</A></TD>
            <TD><A class=other 
            href="robot_import.jsp">导入机器人</A></TD>
          </TR>
        </TBODY>
      </TABLE></td>
    </tr>
  </tbody>
</table>
<TABLE id=pagehead cellSpacing=0 cellPadding=0 width="100%" border=0>
<TBODY>
  <TR>
    <TD width="16%">&nbsp;</TD>
    <TD width="84%" class=actions>&nbsp;</TD>
  </TR></TBODY></TABLE>
<TABLE width="98%" align="center" cellPadding=3 cellSpacing=1 class="tabStyle_1 percent98">
<form name=form1 action="?op=import" method="post">
  <TBODY>
  <TR>
    <TD align="left" class="tabStyle_1_title">导入</TD>
    </TR>
  <TR>
    <TD height="24" align="left" bgcolor="#FFFFFF">名称：<input name="name"></TD>
  </TR>
  <TR>
    <TD align="center" bgcolor="#FFFFFF"><textarea name="robotStr" rows="25" style="width:98%"></textarea></TD>
    </TR>
  <TR>
    <TD align="center" bgcolor="#FFFFFF">
	<input type=submit value="确定">
	</TD>
  </TR>
  </TBODY>
</form>
</TABLE>
</BODY></HTML>
