<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%
String codeTop = ParamUtil.get(request, "code");
if (codeTop.equals("")) {
	codeTop = ParamUtil.get(request, "role_code");
	if (codeTop.equals("")) {
		codeTop = ParamUtil.get(request, "roleCode");
	}
}
RoleDb rdTop = new RoleDb();
rdTop = rdTop.getRoleDb(codeTop);
%>
<div id="tabs1">
  <ul>
    <!--<li id="menu0"><a href="user_role_m.jsp"><span>角色列表</span></a></li>-->
    <li id="menu1"><a href="user_role_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(codeTop)%>"><span><%=rdTop.getDesc()%></span></a></li>
<%
if (!codeTop.equals(RoleDb.CODE_MEMBER)) {
%>    
    <li id="menu2"><a href="user_role_user.jsp?role_code=<%=StrUtil.UrlEncode(codeTop)%>"><span>用户</span></a></li>
<%}%>    
    <li id="menu3"><a href="user_role_priv.jsp?roleCode=<%=StrUtil.UrlEncode(codeTop)%>"><span>权限</span></a></li>
    <li id="menu4"><a href="user_role_menu.jsp?roleCode=<%=StrUtil.UrlEncode(codeTop)%>"><span>菜单</span></a></li>
  </ul>
</div>

