<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.group.photo.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title><lt:Label res="res.label.blog.user.photo" key="title"/></title>
<link href="../../../../common.css" rel="stylesheet" type="text/css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
body {
	margin-top: 0px;
	margin-left: 0px;
	margin-right: 0px;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
long groupId = ParamUtil.getLong(request, "groupId");

if (!GroupPrivilege.isMember(request, groupId)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(groupId));
if (gd==null) {
	out.print(StrUtil.Alert_Back("该朋友圈不存在!")); // SkinUtil.LoadString(request,"res.label.blog.user.userconfig", "activate_blog_fail")));
	return;
}

PhotoMgr lm = new PhotoMgr();
PhotoDb ld = new PhotoDb();

String op = StrUtil.getNullString(request.getParameter("op"));

if (op.equals("add")) {
	try {
		if (lm.add(application, request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "photo.jsp?groupId=" + groupId));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
if (op.equals("edit")) {
	if (!GroupPrivilege.isManager(request, groupId)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	try {
		if (lm.modify(application, request)) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "photo.jsp?groupId=" + groupId));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
if (op.equals("del")) {
	if (!GroupPrivilege.isManager(request, groupId)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	if (lm.del(application, request)) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "photo.jsp?groupId=" + groupId));
	}
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	return;
}
%><br>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg(SkinUtil.LoadString(request, "err_id")));
	return;
}
int total = 0;
int pagesize = 10;
int curpage = Integer.parseInt(strcurpage);
	
String sql = "select id from " + ld.getTableName() + " where group_id=" + groupId + " order by sort desc, id desc";
ListResult lr = ld.listResult(sql, curpage, pagesize);
Paginator paginator = new Paginator(request, lr.getTotal(), pagesize);
%>
<table width="95%" border="0" align="center" class="p9">
  <tr>
    <td align="right"><%=paginator.getPageStatics(request)%>&nbsp;</td>
  </tr>
</table>
<table style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td width="21%" height="24" noWrap bgcolor="#617AA9" class="thead" style="PADDING-LEFT: 10px; color:#FFFFFF"><lt:Label res="res.label.blog.user.photo" key="name"/></td>
      <td width="26%" noWrap bgcolor="#617AA9" class="thead" style="PADDING-LEFT: 10px; color:#FFFFFF"><lt:Label res="res.label.blog.user.photo" key="pic"/></td>
      <td width="6%" noWrap bgcolor="#617AA9" class="thead" style="PADDING-LEFT: 10px; color:#FFFFFF">排序号</td>
      <td width="17%" noWrap bgcolor="#617AA9" class="thead" style="PADDING-LEFT: 10px; color:#FFFFFF"><lt:Label res="res.label.blog.user.photo" key="operate"/></td>
      <td width="30%" noWrap bgcolor="#617AA9" class="thead" style="PADDING-LEFT: 10px; color:#FFFFFF">&nbsp;</td>
    </tr>
<%
Iterator ir = lr.getResult().iterator();
int i=100;
while (ir.hasNext()) {
	i++;
 	ld = (PhotoDb)ir.next();
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
	  <form name="form<%=i%>" action="?op=edit&groupId=<%=groupId%>" method="post" enctype="MULTIPART/FORM-DATA">
      <td style="PADDING-LEFT: 10px">&nbsp;<img src="../../../images/readme.gif" align="absmiddle">&nbsp;<input name=title value="<%=ld.getTitle()%>"></td>
      <td>
        <input name="filename" type="file" style="width: 200px">		</td>
      <td><input name="sort" value="<%=ld.getSort()%>" size="3"></td>
      <td>
	  <%if (GroupPrivilege.isManager(request, groupId)) {%>
	  [ <a href="javascript:form<%=i%>.submit()"><lt:Label res="res.label.blog.user.photo" key="modify"/></a> ] [ <a onClick="if (!confirm('<lt:Label res="res.label.blog.user.photo" key="del_confirm"/>')) return false" href="?op=del&id=<%=ld.getId()%>&groupId=<%=groupId%>"><lt:Label res="res.label.blog.user.photo" key="del"/></a> ] 
	  <%}%>
	  <input name="id" value="<%=ld.getId()%>" type="hidden">
	  <input name="groupId" value="<%=groupId%>" type="hidden">	  </td>
	  <td><span style="PADDING-LEFT: 10px">
	    <%=com.redmoon.forum.ForumSkin.formatDate(request, ld.getAddDate())%>
	    <%if (ld.getImage().equals("")) {%>
        <%}else{
		%>
			<a href="<%=ld.getPhotoUrl(request)%>" target="_blank"><img src="<%=ld.getPhotoUrl(request)%>" width="32" height="32" border="0" align="absmiddle"></a>
        <%}%>
      </span></td>
	  </form>
    </tr>
<%}%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <form action="?op=add&groupId=<%=groupId%>" method="post" enctype="multipart/form-data" name="addform1">
        <td style="PADDING-LEFT: 10px">&nbsp;<img src="../../../images/readme.gif" align="absmiddle">
            <input name=title value="">        </td>
        <td>
          <input type="file" name="filename" style="width: 200px">        </td>
        <td>&nbsp;</td>
        <td colspan="2"><input name="submit" type=submit value="<lt:Label res="res.label.blog.user.photo" key="add"/>" width=80 height=20>
          <input name="groupId" value="<%=groupId%>" type="hidden">
(
  <lt:Label res="res.label.blog.user.photo" key="modify_pic_description"/>
)</td>
      </form>
    </tr>
</tbody></table>
<table width="95%" border="0" align="center" class="p9">
  <tr>
    <td align="right">&nbsp;
        <%
				String querystr = "groupId=" + groupId;
				out.print(paginator.getPageBlock(request,"?"+querystr));
				%></td>
  </tr>
</table>
</body>
</html>