<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%
String actionTop = ParamUtil.get(request, "action");
%>
<script src="../inc/nav.js"></script>
<div id="tabs1">
  <ul>
 	<li id="menu0"><a href="form_report_list.jsp"><span>流程报表</span></a></li>
    <%if (!"sel".equals(actionTop)) {%>
 	<li id="menu2"><a href="javascript:;" onclick="addTab('报表设计器', '<%=request.getContextPath()%>/flow/report/designer.jsp')"><span>报表设计器</span></a></li>
    <%}%>
  </ul>
</div>
