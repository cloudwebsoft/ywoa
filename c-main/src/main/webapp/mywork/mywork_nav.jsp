<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="com.redmoon.oa.worklog.WorkLogDb"%>
<%
String userNameNav = cn.js.fan.util.ParamUtil.get(request, "userName");
int logType2 = cn.js.fan.util.ParamUtil.getInt(request,"logType",0);
String addNameTile = "";
if(logType2 == WorkLogDb.TYPE_WEEK){
	addNameTile = "添加周报";
}else if(logType2== WorkLogDb.TYPE_MONTH){
	addNameTile ="添加月报";
}else{
	addNameTile = "添加日报";
}

if(userNameNav.equals("")){
	com.redmoon.oa.pvg.Privilege pvgNav = new com.redmoon.oa.pvg.Privilege();
	userNameNav = pvgNav.getUser(request);
}
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="mywork.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameNav)%>"><span>工作日报</span></a></li>
    <li id="menu5"><a href="mywork_list_week.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameNav)%>"><span>周报</span></a></li>
    <li id="menu6"><a href="mywork_list_month.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameNav)%>"><span>月报</span></a></li>
    <li id="menu2"><a href="mywork_stat.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameNav)%>"><span>月统计</span></a></li>
    <li id="menu3"><a href="mywork_stat_year.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameNav)%>"><span>年统计</span></a></li>
    <li id="menu4"><a href="mywork_add.jsp?userName=<%=cn.js.fan.util.StrUtil.UrlEncode(userNameNav)%>"><span><%=addNameTile %></span></a></li>
</ul>
</div>

