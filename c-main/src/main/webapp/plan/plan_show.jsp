<%@ page language="java"  contentType="text/html; charset=utf-8" %>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");

String userName="",title="",content="",mydate="",zdrq="";
PlanDb pd = new PlanDb();
pd = pd.getPlanDb(id);

String op = ParamUtil.get(request, "op");

userName = pd.getUserName();
content = pd.getContent();
title = pd.getTitle();
mydate = DateUtil.format(pd.getMyDate(), "yyyy-MM-dd HH:mm:ss");
zdrq = DateUtil.format(pd.getZdrq(), "yyyy-MM-dd");

String menuItem = ParamUtil.get(request, "menuItem");
if (menuItem.equals("")) {
	menuItem = "menu1";
}
boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>日程安排</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"/>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</HEAD>
<%
if (op.equals("setClosed")) {
	pd.setClosed(true);
	pd.save();
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_show.jsp?id=" + id));
	return;
}
%>
<BODY>
<div class="spacerH"></div>
<table width="90%" border="0" align="center" cellpadding="2" cellspacing="0" class="tabStyle_1 percent80">
  <tr>
    <td colspan="4" class="tabStyle_1_title">查看日程</td>
  </tr>
  <tr>
    <td width="13%" align="center">标&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;题</td>
    <td colspan="3"><%=title%></td>
  </tr>
  <tr>
    <td align="center">创建日期</td>
    <td colspan="3"><%=zdrq%></td>
  </tr>
  <tr>
    <td align="center">开始日期</td>
    <td width="41%"><%=mydate%></td>
    <td width="17%">结束日期：</td>
    <td width="29%" align="left">
	<%=DateUtil.format(pd.getEndDate(), "yyyy-MM-dd HH:mm:ss")%>
	</td>
  </tr>
  <tr>
    <td align="center">内 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;容</td>
    <td colspan="3" style="line-height:1.5"><%=StrUtil.toHtml(content)%>
    <%
	String actionLink = PlanMgr.renderAction(request, pd);
	if (!actionLink.equals("")) {
		out.print("<BR><font style='font-family:宋体;'>>></font>&nbsp;" + actionLink);
	}
	%>
    </td>
  </tr>
  <tr>
    <td align="center">是否完成</td>
    <td colspan="3" style="line-height:1.5"><%=pd.isClosed()?"<img src='../images/task_complete.png' style='width:16px'>":"<img src='../images/task_ongoing.png' style='width:16px'"%></td>
  </tr>
  <tr>
    <td align="center">是否便笺</td>
    <td colspan="3" style="line-height:1.5">
    <%=pd.isNotepaper()?"是":"否"%>
    </td>
  </tr>
  <tr>
    <td colspan="4" align="center"><input class="btn" type="button" onclick="window.location.href='plan_edit.jsp?isShared=<%=isShared%>&id=<%=id%>&menuItem=<%=menuItem %>'" value="编辑" />
&nbsp;&nbsp;&nbsp;&nbsp;
<input class="btn" type="button" onclick="jConfirm('您确定要删除么？','提示',function(r){if(!r){return;}else{window.location.href='plan_del.jsp?isShared=<%=isShared%>&userName=<%=StrUtil.UrlEncode(userName)%>&id=<%=id%>'}}) " value="删除" />
<%if (!pd.isClosed()) {%>
&nbsp;&nbsp;&nbsp;&nbsp;
<input class="btn" type="button" onclick="window.location.href='plan_show.jsp?isShared=<%=isShared%>&id=<%=id%>&op=setClosed'" value="完成" />
<%}%>
	</td>
  </tr>
</table>
</BODY>
</HTML>
