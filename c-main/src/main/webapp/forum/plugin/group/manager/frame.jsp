<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Frameset//EN">
<HTML><HEAD><TITLE>朋友圈管理 - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META content="MSHTML 6.00.3790.259" name=GENERATOR></HEAD>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "err_not_login")));
	return;
}
long id = ParamUtil.getLong(request, "id");
GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(id));
if (gd==null) {
	out.print(SkinUtil.makeErrMsg(request, "该圈子不存在!"));
	return;
}
if (!GroupPrivilege.isMember(request, id)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String frameSrc = "group_thread.jsp?id=" + id;
String op = ParamUtil.get(request, "op");
if (op.equals("addTopic"))
	frameSrc = "../../../addtopic_new.jsp?boardcode=" + GroupUnit.code + "&threadType=0&addFlag=" + GroupUnit.code + "&groupId=" + id + "&privurl=" + request.getContextPath() + "/forum/plugin/group/manager/group_thread.jsp?id="+id;
else if (op.equals("addPhoto"))
	frameSrc = "photo.jsp?groupId=" + id;
%>
<FRAMESET border=0 
frameSpacing=0 rows=49,* frameBorder=NO cols=*><FRAME name=topFrame 
src="top.jsp?id=<%=id%>" noResize scrolling=no><FRAMESET 
border=0 frameSpacing=0 rows=* frameBorder=NO cols=128,*><FRAME name=leftFrame 
src="menu.jsp?id=<%=id%>" 
noResize scrolling=yes>
<FRAME name=mainFrame src="<%=frameSrc%>" scrolling=yes></FRAMESET></FRAMESET><noframes></noframes></HTML>
