<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Vector"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<HTML><HEAD><TITLE>title</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="../../../admin/default.css" type=text/css rel=stylesheet>
<META content="MSHTML 6.00.3790.259" name=GENERATOR></HEAD>
<BODY bgColor=#9aadcd leftMargin=0 topMargin=0>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<BR>
<%
String rootpath = request.getContextPath();
long id = ParamUtil.getLong(request, "id");
GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(id));
%>
<table width="100%" border="0" cellpadding="5">
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="../group.jsp?id=<%=id%>" target="_blank">我的圈子</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="group_prop.jsp?id=<%=id%>" target="mainFrame">圈子属性</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="group_member.jsp?id=<%=id%>" target="mainFrame">成员管理</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="../../../addtopic_new.jsp?boardcode=<%=GroupUnit.code%>&threadType=0&addFlag=<%=GroupUnit.code%>&groupId=<%=id%>&privurl=<%=request.getContextPath()+"/forum/plugin/group/manager/group_thread.jsp?id="+id%>" target="mainFrame">发布文章</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="../../../addtopic_new.jsp?boardcode=<%=GroupUnit.code%>&threadType=0&addFlag=<%=GroupUnit.code%>&groupId=<%=id%>&pluginCode=<%=com.redmoon.forum.plugin.activity.ActivityUnit.code%>&privurl=<%=request.getContextPath()+"/forum/plugin/group/manager/group_thread.jsp?id="+id%>" target="mainFrame">发布活动</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="group_thread.jsp?id=<%=id%>" target="mainFrame">文章管理</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="photo.jsp?groupId=<%=id%>" target="mainFrame">相册管理</a></td>
  </tr>
  <tr>
    <td><img src="../../../../images/arrow.gif">&nbsp;<a href="link.jsp?kind=group&userName=<%=id%>" target="mainFrame">友情链接</a></td>
  </tr> 
</table>
</BODY></HTML>
