<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
long id = ParamUtil.getLong(request, "id");

GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(id));
if (gd==null) {
	out.print(StrUtil.Alert_Back("该圈子不存在!"));
	return;
}
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_not_login")));
	return;
}

GroupUserDb bgu = new GroupUserDb();
GroupUserDb tmpbgu = bgu.getGroupUserDb(id, privilege.getUser(request));
if ((tmpbgu!=null && tmpbgu.isLoaded()) || gd.getString("creator").equals(privilege.getUser(request))) {
	out.print(StrUtil.Alert_Back("您已申请或已加入了该圈子!"));
	return;
}
%>
<html>
<head>
<title><%=gd.getString("name")%> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=GroupSkin.getSkin(gd.getString("skin_code")).getPath()%>/css.css" rel="stylesheet" type="text/css" />
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<div id="wrap">
<%@ include file="group_header.jsp"%>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	try {
        int max = GroupConfig.getInstance().getIntProperty("max_group_attend");
        if (gd.getAttendCount(privilege.getUser(request))>max) {
            out.print(StrUtil.Alert_Back("您加入的圈子超过了允许的最大数目" + max + "!"));
			return;
        }
		if (qom.create(request, bgu, "plugin_group_user_create")) {
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.waitJump(SkinUtil.LoadString(request, "info_op_success"), 3, "group.jsp?id=" + id));
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
%>
<div class="content xw">
<table width="482" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe_gray">
<form id=form1 action="?op=add" method=post>
  <tr>
    <td colspan="2" align="center" class="block">
	<DIV class=title><DIV class=cName>申请加入&nbsp;-&nbsp;<%=gd.getString("name")%></div></DIV></td>
  </tr>
  <tr>
    <td width="119" height="22" bgcolor="#F2F2F2"><br>
	<div style="font-size:14px" align="center"><lt:Label res="res.label.blog.blog_group_apply" key="apply_reason"/></div></td>
    <td width="363" height="22" bgcolor="#F2F2F2">
	<textarea name="apply_reason" cols="40" id="apply_reason" rows="5"></textarea>
	<input name="id" value="<%=id%>" type="hidden">
	<input name="group_id" value="<%=id%>" type="hidden">
	<input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden"></td>
  </tr>
  <tr>
    <td colspan="2" align="center" bgcolor="#F2F2F2">
	<div align="center">
      <input type="submit" name="Submit" value="<lt:Label key="submit"/>">
      &nbsp;&nbsp;
      <input type="reset" name="Submit2" value="<lt:Label key="reset"/>"></div>
    </td>
  </tr></form>
</table>	  
<br>
</div>
</div>
<%@ include file="group_footer.jsp"%>	
</body>
</html>