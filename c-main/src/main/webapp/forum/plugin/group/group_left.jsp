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
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%
long leftId = ParamUtil.getLong(request, "id");
GroupDb leftGroup = new GroupDb();
leftGroup = (GroupDb)leftGroup.getQObjectDb(new Long(leftId));
if (leftGroup==null) {
	return;
}
UserMgr leftum = new UserMgr();
%>
<div class="lw">
      <div class="baseInfo leftBlock">
        <div id="Left_Basic1_circleName" style="font-size:14px; font-weight:bold; color:#333;"><%=leftGroup.getString("name")%></div>
        <div class="image">
		<%
		String leftLogoUrl = leftGroup.getLogoUrl(request);
		if (!leftLogoUrl.equals("")) {
		%>
		<img src="<%=leftLogoUrl%>" id="Left_Basic1_img" width="120" alt="<%=leftGroup.getString("name")%>" />
		<%}%>
		</div>
        <div class="info"> 圈主：<a target="_blank" href="../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(leftGroup.getString("creator"))%>" id="Left_Basic1_creator"><%=leftum.getUser(leftGroup.getString("creator")).getNick()%></a><br />
          成员：<%=leftGroup.getInt("user_count")%><br />
          <p class="l">简介：</p>
          <p class="l"><%=StrUtil.toHtml(leftGroup.getString("description"))%></p>
          <div class="hackbox"></div>
        </div>
      </div>
      <div class="member leftBlock1">
        <div class="title">
          <div class="cName"> 活跃圈友</div>
        </div>
        <div class="txt blackLink" style="padding-left: 5px;"> <span id="Activity" class="memberList">
          <%
GroupUserDb leftgu = new GroupUserDb();
String leftsql = leftgu.getListUserSql(leftId, "activeUser");
QObjectBlockIterator leftobi = leftgu.getQObjects(leftsql, 0, 6);
while (leftobi.hasNext()) {
	leftgu = (GroupUserDb)leftobi.next();
	UserDb user = leftum.getUser(leftgu.getString("user_name"));
%>
          <span> <a href="../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank">
          <%if (user.getMyface().equals("")) {%>
          <img src="../../images/face/<%=user.getRealPic()%>" alt='<%=user.getNick()%>' border="0" width="50">
          <%}else{%>
          <img src="<%=user.getMyfaceUrl(request)%>" alt='<%=user.getNick()%>' border="0" height="50">
          <%}%>
          <%=user.getNick()%></a></span>
          <%}%>
          </span>
          <div class="more"> <a href="group_member.jsp?id=<%=leftId%>&kind=activeUser">更多...</a></div>
        </div>
      </div>
      <div class="member leftBlock1">
        <div class="title">
          <div class="cName"> 最新加入</div>
        </div>
        <div class="txt blackLink" style="padding-left: 5px;"> <span id="New" class="memberList">
          <%
leftsql = leftgu.getListUserSql(leftId, "newUser");
leftobi = leftgu.getQObjects(leftsql, 0, 6);
while (leftobi.hasNext()) {
	leftgu = (GroupUserDb)leftobi.next();
	UserDb user = leftum.getUser(leftgu.getString("user_name"));
%>
          <span> <a href="../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank">
          <%if (user.getMyface().equals("")) {%>
          <img src="../../images/face/<%=user.getRealPic()%>" alt='<%=user.getNick()%>' border="0" width="50">
          <%}else{%>
          <img src="<%=user.getMyfaceUrl(request)%>" alt='<%=user.getNick()%>' border="0" height="50">
          <%}%>
          <%=user.getNick()%></a></span>
          <%}%>
          </span>
          <div class="more"> <a href="group_member.jsp?id=<%=leftId%>&kind=newUser">更多...</a></div>
        </div>
      </div>
    </div>