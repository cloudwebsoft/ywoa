<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<script src="../inc/nav.js"></script>
<%
int lefttype = ParamUtil.getInt(request, "type", AddressDb.TYPE_USER);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="account_list.jsp"><span>通讯录</span></a></li>
    <li id="menu2"><a href="account_add.jsp"><span>查询</span></a></li>
  </ul>
</div>
