<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Thread Type Manage</title>
<link href="default.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="ttmgr" scope="page" class="com.redmoon.forum.ThreadTypeMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String boardCode = ParamUtil.get(request, "boardCode");
Directory dir = new Directory();
Leaf lf = dir.getLeaf(boardCode);

String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("add")) {
	try {
		if (ttmgr.add(request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "board_threadtype_list.jsp?boardCode=" + boardCode));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
if (op.equals("edit")) {
	try {
		if (ttmgr.modify(request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "board_threadtype_list.jsp?boardCode=" + boardCode));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
}
if (op.equals("del")) {
	if (ttmgr.del(request)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "board_threadtype_list.jsp?boardCode=" + boardCode));
		return;
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request,"info_op_fail")));
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head"><%=lf.getName()%>&nbsp;
        -&nbsp;
        <lt:Label res="res.label.forum.admin.board_threadtype_list" key="subtype"/></td>
    </tr>
  </tbody>
</table>
<%
ThreadTypeDb ttd = new ThreadTypeDb();
String sql = "select id from " + ttd.getTableName() + " where board_code=" + StrUtil.sqlstr(boardCode) + " order by display_order";
Iterator ir = ttd.list(sql).iterator();
%>
<br>
<br>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="thead" style="PADDING-LEFT: 10px" noWrap width="22%"><lt:Label res="res.label.forum.admin.board_threadtype_list" key="name"/></td>
      <td class="thead" noWrap width="16%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.board_threadtype_list" key="color"/></td>
      <td class="thead" noWrap width="20%"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.board_threadtype_list" key="display_order"/></td>
      <td width="42%" noWrap class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">
      <lt:Label res="res.label.forum.admin.board_threadtype_list" key="operate"/></td>
    </tr>
<%
int i=100;
while (ir.hasNext()) {
	i++;
 	ttd = (ThreadTypeDb)ir.next();
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
	<form name="form<%=i%>" action="?op=edit" method="post">
      <td style="PADDING-LEFT: 10px">&nbsp;<img src="images/arrow.gif" align="absmiddle">&nbsp;<input name="name" value="<%=ttd.getName()%>"></td>
      <td>
      <select name="color" style="width:80px">
        <option value="" style="COLOR: black" selected><lt:Label res="res.label.forum.admin.board_threadtype_list" key="color_no"/></option>
        <option style="BACKGROUND: #000088" value="#000088"></option>
        <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
        <option style="BACKGROUND: #008800" value="#008800"></option>
        <option style="BACKGROUND: #008888" value="#008888"></option>
        <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
        <option style="BACKGROUND: #00a010" value="#00a010"></option>
        <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
        <option style="BACKGROUND: #111111" value="#111111"></option>
        <option style="BACKGROUND: #333333" value="#333333"></option>
        <option style="BACKGROUND: #50b000" value="#50b000"></option>
        <option style="BACKGROUND: #880000" value="#880000"></option>
        <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
        <option style="BACKGROUND: #888800" value="#888800"></option>
        <option style="BACKGROUND: #888888" value="#888888"></option>
        <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
        <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
        <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
        <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
        <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
        <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
        <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
        <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
        <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
        <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
        <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
        <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
        <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
        <option style="BACKGROUND: #000000" value="#000000"></option>
      </select>
	  <script>
	  form<%=i%>.color.value = "<%=ttd.getColor()%>";
	  </script>
		<input name="id" value="<%=ttd.getId()%>" type="hidden">
        <input name="boardCode" value="<%=boardCode%>" type="hidden"></td>
      <td><span style="PADDING-LEFT: 10px">
        <input name="displayOrder" value="<%=ttd.getDisplayOrder()%>" size="2">
      </span></td>
      <td>
	  [ <a href="javascript:form<%=i%>.submit()"><%=SkinUtil.LoadString(request,"op_edit")%></a> ] [ <a onClick="if (!confirm('<lt:Label res="res.label.forum.admin.board_threadtype_list" key="confirm_del"/>')) return false" href="?op=del&id=<%=ttd.getId()%>&boardCode=<%=boardCode%>"><%=SkinUtil.LoadString(request,"op_del")%></a> ] </td>
	  </form>
    </tr>
<%}%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
	<form name="addform1" action="?op=add" method="post">
      <td style="PADDING-LEFT: 10px">
	  &nbsp;<img src="images/arrow.gif" align="absmiddle">
	  <input name=name value=""></td>
      <td>
        <SELECT name="color" style="width:80px">
          <option value="" STYLE="COLOR: black" selected><lt:Label res="res.label.forum.admin.board_threadtype_list" key="color_no"/></option>
          <option style="BACKGROUND: #000088" value="#000088"></option>
          <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
          <option style="BACKGROUND: #008800" value="#008800"></option>
          <option style="BACKGROUND: #008888" value="#008888"></option>
          <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
          <option style="BACKGROUND: #00a010" value="#00a010"></option>
          <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
          <option style="BACKGROUND: #111111" value="#111111"></option>
          <option style="BACKGROUND: #333333" value="#333333"></option>
          <option style="BACKGROUND: #50b000" value="#50b000"></option>
          <option style="BACKGROUND: #880000" value="#880000"></option>
          <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
          <option style="BACKGROUND: #888800" value="#888800"></option>
          <option style="BACKGROUND: #888888" value="#888888"></option>
          <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
          <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
          <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
          <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
          <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
          <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
          <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
          <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
          <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
          <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
          <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
          <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
          <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
          <option style="BACKGROUND: #000000" value="#000000"></option>
        </SELECT>
        <input name="boardCode" value="<%=boardCode%>" type="hidden"></td>
      <td><span style="PADDING-LEFT: 10px">
        <input name="displayOrder" size="2">
      </span></td>
      <td><INPUT onclick="return addform1.submit()" type="button" value="<%=SkinUtil.LoadString(request,"op_add")%>"> </td>
	</form>
    </tr>
  </tbody>
</table>
</body>
</html>