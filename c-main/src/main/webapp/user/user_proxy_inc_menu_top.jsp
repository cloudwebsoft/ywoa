<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<div class="tabs1Box">
<div id="tabs1">
<%
String menuUserName = ParamUtil.get(request, "userName");
%>
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/user/user_proxy_list.jsp?userName=<%=StrUtil.UrlEncode(menuUserName)%>"><span><lt:Label res="res.flow.Flow" key="setProxy"/></span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/user/user_proxy_add.jsp?userName=<%=StrUtil.UrlEncode(menuUserName)%>"><span><lt:Label res="res.flow.Flow" key="addProxy"/></span></a></li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/flow/flow_list_my_proxy.jsp?userName=<%=StrUtil.UrlEncode(menuUserName)%>"><span><lt:Label res="res.flow.Flow" key="myProxy"/></span></a></li>
    <li id="menu4"><a href="<%=request.getContextPath()%>/flow/flow_list_proxy.jsp?userName=<%=StrUtil.UrlEncode(menuUserName)%>"><span><lt:Label res="res.flow.Flow" key="proxied"/></span></a></li>
  </ul>
</div>
</div>