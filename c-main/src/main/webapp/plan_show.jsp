<%@ page language="java"  contentType="text/html; charset=utf-8" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD><TITLE>计划</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</HEAD>
<BODY>
<%@ include file="plan_inc_menu_top.jsp"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");

String username="",title="",content="",mydate="",zdrq="";
PlanDb pd = new PlanDb();
pd = pd.getPlanDb(id);

username = pd.getUserName();
content = pd.getContent();
title = pd.getTitle();
mydate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
zdrq = DateUtil.format(pd.getZdrq(), "yyyy-MM-dd");
%>
<br>
<table width="90%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent80">
  <tr>
    <td colspan="4" class="tabStyle_1_title">查看日程</td>
  </tr>
  <tr>
    <td width="19%" class="stable">标&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
    <td colspan="3" class="stable"><%=title%></td>
  </tr>
  <tr bgcolor="#FFFFFF">
    <td class="stable">制定日期：</td>
    <td width="35%" class="stable"><%=zdrq%></td>
    <td width="17%" class="stable">办理时间：</td>
    <td width="29%" align="center" bgcolor="#FFFFFF" class="stable"><%=mydate%></td>
  </tr>
  <tr bgcolor="#FFFFFF">
    <td valign="top" bgcolor="#FFFFFF" class="stable">内 &nbsp;&nbsp;&nbsp;容：</td>
    <td colspan="3" class="stable"><%=content%> </td>
  </tr>
</table>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td width="100%" align="center" valign="top"><br>
      <p><a onclick="return confirm('您确定要删除么？')" href="plan_del.jsp?id=<%=id%>">删除日程</a> </p></td>
  </tr>
</table>
</BODY>
</HTML>
