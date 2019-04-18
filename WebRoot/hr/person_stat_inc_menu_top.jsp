<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<%@ page import="cn.js.fan.util.*"%>
<%
String mynameTop = ParamUtil.get(request, "userName");
if(mynameTop.equals("")){
	com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
	mynameTop = pvgTop.getUser(request);
}
%>
<div class="tabs1Box">
<DIV id="tabs1">
  <ul>
	<li id="menu1"><a href="<%=request.getContextPath()%>/hr/person_stat_chart.jsp?term=age"><span>年龄</span></a> </li>
	<li id="menu2"><a href="<%=request.getContextPath()%>/hr/person_stat_chart.jsp?term=people"><span>民族</span></a> </li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/hr/person_stat_chart.jsp?term=zzmm"><span>政治面貌</span></a> </li>
	<li id="menu4"><a href="<%=request.getContextPath()%>/hr/person_stat_chart.jsp?term=zgxl"><span>学历</span></a> </li>
  </ul>
</DIV>
</div>
