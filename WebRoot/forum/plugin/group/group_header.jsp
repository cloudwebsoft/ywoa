<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%
long headerId = ParamUtil.getLong(request, "id");
GroupDb headerGroup = new GroupDb();
headerGroup = (GroupDb)headerGroup.getQObjectDb(new Long(headerId));
if (headerGroup==null) {
	return;
}
%>
  <div class="top">
    <div class="liveNav xw">
      <div class="logo whiteLink l"><a href="<%=request.getContextPath()%>/"><%=Global.AppName%></a></div>
      <div class="nav whiteLink r"><a href="<%=request.getContextPath()%>/index.jsp">首页</a> - <a href="<%=request.getContextPath()%>/forum/plugin/group/group_list.jsp">朋友圈</a> - <a href="<%=request.getContextPath()%>/forum/index.jsp">论坛</a>
	   <%if (com.redmoon.blog.Config.getInstance().isBlogOpen) {%>
	   - <a href="<%=request.getContextPath()%>/blog/index.jsp" target="_blank">博客</a><%}%>
	   &nbsp;&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/door.jsp" >登录</a> | <a href="<%=request.getContextPath()%>/regist.jsp" target="_blank">注册</a></div>
    </div>
  </div>
  <div class="name xw">
  	<%
	String bannerUrl = headerGroup.getBannerUrl(request);
	%>
  	<div id="banner" style="float:right; padding-right:5px">
	<%if (!bannerUrl.equals("")) {%>
	<img src="<%=headerGroup.getBannerUrl(request)%>" />
	<%}%>
	</div>
    <h1> <%=headerGroup.getString("name")%><span></span></h1>
    <h2> <%=StrUtil.getNullString(headerGroup.getString("description"))%></h2>
  </div>
  <div class="gpMenu">
    <div class="xw">
      <ul class="menu menuLink" style="font-weight:bold; font-size:14px;">
        <li id="Top_Inner1_index" class="current"><a href="group.jsp?id=<%=headerId%>">首页</a></li>
        <li id="Top_Inner1_topic"><a href="group_thread.jsp?id=<%=headerId%>">话题</a></li>
        <li id="Top_Inner1_partylist"><a href="group_activity.jsp?id=<%=headerId%>">活动</a></li>
        <li id="Top_Inner1_photocatalog"><a href="group_photo.jsp?id=<%=headerId%>">相册</a></li>
        <li id="Top_Inner1_adminmember"><a href="group_member.jsp?id=<%=headerId%>">成员</a></li>
        <li id="Top_Inner1_adminbase"><a href="manager/frame.jsp?id=<%=headerId%>" target="_blank">管理</a></li>
      </ul>
    </div>
  </div>
