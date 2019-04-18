<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%
int kindTop = ParamUtil.getInt(request, "kind", -1);
Privilege pvgTop = new Privilege();
SelectKindPriv skpTop = new SelectKindPriv();
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="basic_select_list.jsp"><span>基础数据</span></a></li>
    <%if (pvgTop.isUserPrivValid(request, "admin") || skpTop.canUserAppend(pvgTop.getUser(request), kindTop)) { %>
    <li id="menu2"><a href="basic_select_add.jsp?kind=<%=kindTop%>"><span>增加</span></a></li>
    <%}%>
    <%if (pvgTop.isUserPrivValid(request, "admin")) { %>
    <li id="menu3"><a href="basic_select_kind_list.jsp"><span>类型</span></a></li>
    <%} %>
  </ul>
</div>

