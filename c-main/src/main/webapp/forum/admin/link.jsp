<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.module.nav.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<title><lt:Label res="res.label.forum.admin.link" key="link_manage"/></title>
<script src="../../inc/common.js"></script>
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
LinkMgr lm = new LinkMgr();
LinkDb ld = new LinkDb();
String userName = ld.USER_SYSTEM;
String kind = ParamUtil.get(request, "kind");
String op = StrUtil.getNullString(request.getParameter("op"));

if (op.equals("add")) {
	try {
		if (lm.add(application, request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "link.jsp?kind=" + kind + "&userName=" + StrUtil.UrlEncode(userName)));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
if (op.equals("edit")) {
	try {
		if (lm.modify(application, request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "link.jsp?kind=" + kind + "&userName=" + StrUtil.UrlEncode(userName)));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
if (op.equals("move")) {
	try {
		if (lm.move(request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "link.jsp?kind=" + kind + "&userName=" + StrUtil.UrlEncode(userName)));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
if (op.equals("del")) {
	if (lm.del(application, request)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "link.jsp?kind=" + kind + "&userName=" + StrUtil.UrlEncode(userName)));
	}
	else {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_del")));
	}
	return;
}

String user = privilege.getUser(request);
if (!userName.equals(user)) {
	if (!privilege.isMasterLogin(request)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
%>
<%@ include file="link_nav.jsp"%>
<%
String item = "1";
if (kind.equals("second")) {
	item = "2";
}
else if (kind.equals("three")) {
	item = "3";
}
%>
<script>
$("menu<%=item%>").className="active";
</script>
<br>
<br>
<table cellSpacing="0" cellPadding="3" width="95%" align="center" class="frame_gray">
  <tbody>
    <tr>
      <td class="thead" noWrap width="21%"><lt:Label res="res.label.forum.admin.link" key="name"/></td>
      <td class="thead" noWrap width="22%"><lt:Label res="res.label.forum.admin.link" key="link"/></td>
      <td class="thead" noWrap width="30%"><lt:Label res="res.label.forum.admin.link" key="image"/></td>
	  <td width="27%" noWrap class="thead"><lt:Label key="op"/></td>
    </tr>
<%
String sql = ld.getListSql(kind, ld.USER_SYSTEM);
Iterator ir = ld.list(sql).iterator();
int i=100;
while (ir.hasNext()) {
	i++;
 	ld = (LinkDb)ir.next();
	%>
    <tr>
	  <form name="form<%=i%>" action="?op=edit&kind=<%=kind%>" method="post" enctype="MULTIPART/FORM-DATA">
      <td><input name=title value="<%=ld.getTitle()%>"></td>
      <td><input name=url value="<%=ld.getUrl()%>" size="30"></td>
      <td class="highlight">
        <input name="filename" type="file" style="width: 200px">
		</td>
      <td>
	  [ <a href="javascript:form<%=i%>.submit()"><lt:Label key="op_edit"/></a> ] [ <a onClick="if (!confirm('<lt:Label key="confirm_del"/>')) return false" href="?op=del&id=<%=ld.getId()%>&kind=<%=kind%>&userName=<%=StrUtil.UrlEncode(userName)%>"><lt:Label key="op_del"/></a> ] [<a href="?op=move&direction=up&id=<%=ld.getId()%>&kind=<%=kind%>&userName=<%=StrUtil.UrlEncode(userName)%>"><lt:Label res="res.label.forum.admin.link" key="move_up"/></a>] [<a href="?op=move&direction=down&id=<%=ld.getId()%>&kind=<%=kind%>&userName=<%=StrUtil.UrlEncode(userName)%>"><lt:Label res="res.label.forum.admin.link" key="move_down"/></a>] 
	  <input name="id" value="<%=ld.getId()%>" type="hidden">
	  <input name="userName" value="<%=ld.USER_SYSTEM%>" type="hidden">
	  <input name="kind" value="<%=kind%>" type="hidden">
	  </td>
	  </form>
    </tr>
    <tr>
      <td colspan="4">
	  <%if (ld.getImage()==null || ld.getImage().equals("")) {%>
	  <%}else{
	  	if (StrUtil.getFileExt(ld.getImage()).equals("swf")) {%>
			<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0" width="88" height="31">
            <param name="movie" value="<%=ld.getImageUrl(request)%>">
            <param name="quality" value="high">
            <embed src="<%=ld.getImageUrl(request)%>" quality="high" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash" width="88" height="31"></embed>
            </object>
		<%}else{%>
		  <img src="<%=ld.getImageUrl(request)%>" width="88" height="31">
	  <%}
	  }%>
	  </td>
    </tr>
<%}%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
	<form action="?op=add&kind=<%=kind%>" method="post" enctype="multipart/form-data" name="addform1">
      <td><input name=title value=""></td>
      <td><input name=url value="" size="30"></td>
      <td><span class="stable">
        <input type="file" name="filename" style="width: 200px">
      </span></td>
      <td><INPUT type=submit height=20 width=80 value="<lt:Label key="op_add"/>">
        <input name="userName" value="<%=ld.USER_SYSTEM%>" type="hidden">
        <input name="kind" value="<%=kind%>" type="hidden">
        </td>
	</form>
    </tr>
    <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
      <td colspan="4" style="PADDING-LEFT: 10px"><lt:Label res="res.label.forum.admin.link" key="howto_del"/></td>
    </tr>
  </tbody>
</table>
</body>
</html>