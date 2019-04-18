<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%
String codeTop = ParamUtil.get(request, "code");
if (codeTop.equals("")) {	
	codeTop = ParamUtil.get(request, "group_code");
}
UserGroupDb ugdTop = new UserGroupDb();
ugdTop = ugdTop.getUserGroupDb(codeTop);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="user_group_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(codeTop)%>"><span><%=ugdTop.getDesc()%></span></a></li>
    <li id="menu2"><a href="user_group_user.jsp?group_code=<%=StrUtil.UrlEncode(codeTop)%>"><span>用户</span></a></li>
    <li id="menu3"><a href="user_group_priv.jsp?group_code=<%=StrUtil.UrlEncode(codeTop)%>&desc=<%=StrUtil.UrlEncode(ugdTop.getDesc())%>"><span>权限</span></a></li>
  </ul>
</div>

