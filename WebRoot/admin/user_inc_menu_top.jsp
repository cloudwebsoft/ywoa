<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%
String menuUserName = ParamUtil.get(request, "userName");
if (menuUserName.equals(""))
	menuUserName = ParamUtil.get(request, "name");
%>
<div id="tabs1">
  <ul>
    <!-- <li id="menu1"><a href="organize/user_edit.jsp?name=<%=StrUtil.UrlEncode(menuUserName)%>"><span>用户信息</span></a></li> -->
    <li id="menu2"><a href="user_op.jsp?name=<%=StrUtil.UrlEncode(menuUserName)%>"><span>用户权限</span></a></li>
    <!--
    <li id="menu3"><a href="user_dept_modify.jsp?userName=<%=StrUtil.UrlEncode(menuUserName)%>"><span>所属部门</span></a></li>
    -->
    <li id="menu4"><a href="javascript:;" onclick="addTab('设置代理', '<%=request.getContextPath()%>/user/user_proxy_list.jsp?userName=<%=StrUtil.UrlEncode(menuUserName)%>', 800, 600)"><span>设置代理</span></a></li>
  </ul>
</div>

