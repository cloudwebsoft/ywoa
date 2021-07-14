<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%
String userNameTop = ParamUtil.get(request, "userName");
com.redmoon.oa.pvg.Privilege privilegeInc = new com.redmoon.oa.pvg.Privilege();
if(userNameTop.equals("")){
	userNameTop = privilegeInc.getUser(request);
}
String myrealTop = new UserDb(userNameTop).getRealName();
boolean isMe = userNameTop.equals(privilegeInc.getUser(request));
boolean isSharedTop = ParamUtil.getBoolean(request, "isShared", false);
String titleTop;
if (isSharedTop) {
	titleTop = "共享日程";
}
else {
	titleTop = isMe ? "我的日程" : myrealTop + "的日程";
}
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a class="black" href="plan.jsp?isShared=<%=isSharedTop%>&userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span><%=titleTop %></span></a></li>
    <%if (isMe && !isSharedTop) { %>
    <li id="menu2"><a class="black" href="plan_periodicity.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>周期性事务</span></a></li>
    <li id="menu3"><a class="black" href="plan_list.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>全部日程</span></a></li>
    <%
	if (userNameTop.equals("") || new com.redmoon.oa.pvg.Privilege().getUser(request).equals(userNameTop)) {
		%>
        <li id="menu5"><a class="black" href="plan_list.jsp?mode=iMake&userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span title="我制定的其他人员的日程">我制定的</span></a></li>
		<%
	}
	%>
    <li id="menu4"><a class="black" href="plan_add.jsp?userName=<%=StrUtil.UrlEncode(userNameTop)%>"><span>添加</span></a></li>
    <%} %>
  </ul>
</div>

