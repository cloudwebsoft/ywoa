<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%
	String deptCode = ParamUtil.get(request, "typeCode");

	

%>
<div class="tabs1Box">
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="flow_stat_month.jsp?typeCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>月统计</span></a></li>
    <li id="menu2"><a href="flow_stat_year.jsp?typeCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>年统计</span></a></li>
	<li id="menu3"><a href="flow_stat_person_month.jsp?typeCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>人员月统计</span></a></li>
	<li id="menu4"><a href="flow_stat_person_year.jsp?typeCode=<%=StrUtil.UrlEncode(deptCode)%>"><span>人员年统计</span></a></li>
  </ul>
</div>
</div>