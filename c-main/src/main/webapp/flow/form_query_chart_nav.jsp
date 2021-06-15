<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<script src="../inc/nav.js"></script>
<%
int navId = ParamUtil.getInt(request, "id");
%>
<div id="tabs1">
  <ul>
 	<li id="menu0"><a href="form_query_chart_pie.jsp?id=<%=navId%>"><span>饼图</span></a></li>
 	<li id="menu1"><a href="form_query_chart_histogram.jsp?id=<%=navId%>"><span>柱状图</span></a></li>
 	<li id="menu2"><a href="form_query_chart_line.jsp?id=<%=navId%>"><span>折线图</span></a></li>
 	<li id="menu3"><a href="form_query_chart_tb.jsp?id=<%=navId%>"><span>同比</span></a></li>
 	<li id="menu4"><a href="report/designer.jsp?query_id=<%=navId%>"><span>报表设计</span></a></li>
  </ul>
</div>
