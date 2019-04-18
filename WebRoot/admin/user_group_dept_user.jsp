<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理部门型用户组的用户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
<script>
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
}

function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
}

</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
String groupCode = ParamUtil.get(request, "group_code").trim();
UserGroupDb ugd = new UserGroupDb();
ugd = ugd.getUserGroupDb(groupCode);
%>
<%@ include file="user_group_op_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="tabTitle" cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td align="center">用户组&nbsp;-&nbsp;<%=ugd.getDesc()%>&nbsp;用户</td>
    </tr>
  </tbody>
</table>
<%
Iterator ir = ugd.getAllUserOfGroup().iterator();
String name;
String realname;
String genderdesc;
%>
<br>
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="25%">用户名</td>
      <td class="tabStyle_1_title" noWrap width="44%">真实姓名</td>
      <td class="tabStyle_1_title" noWrap width="31%">性别</td>
    </tr>
    <%
String userNames = "";
String userRealNames = "";	
while (ir.hasNext()) {
 	UserDb user = (UserDb)ir.next();
	name = user.getName();
	realname = user.getRealName();
	if (userNames.equals("")) {
		userNames = name;
		userRealNames = realname;
	}
	else {
		userNames += "," + name;
		userRealNames += "," + realname;
	}

	genderdesc = user.getGender()==0?"男":"女";
%>
    <tr>
      <td style="PADDING-LEFT: 10px">&nbsp;&nbsp;<a href="user_op.jsp?op=edit&name=<%=StrUtil.UrlEncode(name)%>"><%=name%></a></td>
      <td style="PADDING-LEFT: 10px"><%=realname%></td>
      <td><%=genderdesc%></td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
</html>