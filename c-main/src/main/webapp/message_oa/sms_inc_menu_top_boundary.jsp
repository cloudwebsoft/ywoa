<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<div id="tabs1">
  <ul>
  	<li id="menu1"><a href="sms_boundary_show.jsp"><span>短信配置</span></a></li>
  	<%
    	int boundaryType = SMSFactory.getBoundary();
    	if(boundaryType == com.redmoon.oa.sms.Config.SMS_BOUNDARY_YEAR){
     %>
    <li id="menu2"><a href="sms_boundary_year_list.jsp"><span>设置短信年配额</span></a></li>
    <li id="menu3"><a href="sms_remind_m.jsp?type=<%=boundaryType%>"><span>设置短信配额提醒</span></a></li>
    <%}else if(boundaryType == com.redmoon.oa.sms.Config.SMS_BOUNDARY_MONTH){ %>
    <li id="menu2"><a href="sms_boundary_month_list.jsp"><span>设置短信月配额</span></a></li>
    <li id="menu3"><a href="sms_remind_m.jsp?type=<%=boundaryType%>"><span>设置短信配额提醒</span></a></li>
    <%} %>
  </ul>
</div>