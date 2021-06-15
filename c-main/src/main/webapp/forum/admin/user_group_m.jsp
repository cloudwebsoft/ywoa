<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title><lt:Label res="res.label.forum.admin.user_group_m" key="user_group_manage"/></title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.forum.person.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head"><lt:Label res="res.label.forum.admin.user_group_m" key="user_group_manage"/></td>
    </tr>
  </tbody>
</table>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("add")) {
	try {
		if (usergroupmgr.add(request))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "user_group_m.jsp"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
if (op.equals("del")) {
	if (usergroupmgr.del(request))
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_success")));
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
}

String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
%>
<br>
<br>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="thead" style="PADDING-LEFT: 10px" noWrap width="18%"><lt:Label res="res.label.forum.admin.user_group_m" key="code"/></td>
      <td class="thead" noWrap width="15%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.user_group_m" key="desc"/></td>
      <td class="thead" noWrap width="11%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.user_group_m" key="display_order"/></td>
      <td class="thead" noWrap width="11%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.user_group_m" key="system"/></td>
      <td class="thead" noWrap width="11%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.user_group_m" key="is_guest"/></td>
      <td width="34%" noWrap class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label key="op"/></td>
    </tr>
    
<%
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td style="PADDING-LEFT: 10px">&nbsp;<img src="images/arrow.gif" align="absmiddle">&nbsp;<a href="user_group_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>"><%=code%></a></td>
      <td><a href="user_group_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>"><%=desc%></a></td>
      <td><a href="user_group_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>"><%=ug.getDisplayOrder()%></a></td>
      <td><%=ug.isSystem()?SkinUtil.LoadString(request, "yes"):SkinUtil.LoadString(request, "no")%></td>
      <td><%=ug.isGuest()?SkinUtil.LoadString(request, "yes"):SkinUtil.LoadString(request, "no")%></td>
      <td>
<%if (!ug.isSystem()) {%>
<a href="user_group_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>">[ <lt:Label key="op_edit"/> ]</a>
[ <a onClick="if (!confirm('<lt:Label res="res.label.forum.admin.user_group_m" key="confirm_del"/>')) return false" href="user_group_m.jsp?op=del&code=<%=StrUtil.UrlEncode(code)%>"><lt:Label key="op_del"/></a> ]
<%}%>
<%if (!ug.isSystem()) {%>
[ <a href="user_m.jsp?op=search&type=userName&groupCode=<%=StrUtil.UrlEncode(code)%>"><lt:Label res="res.label.forum.admin.user_group_m" key="user"/>
</a> ]
<%}%>
&nbsp;[&nbsp;<a href="user_group_priv_frame.jsp?groupCode=<%=StrUtil.UrlEncode(code)%>" target="_blank"><lt:Label res="res.label.forum.admin.user_group_m" key="privilege"/></a>&nbsp;]</td>
    </tr>
<%}%>
  </tbody>
</table>
<HR noShade SIZE=1>
<DIV style="WIDTH: 95%" align=right>
  <INPUT onclick="javascript:location.href='user_group_op.jsp';" type=button value="<lt:Label key="op_add"/>">
</DIV>
</body>
</html>