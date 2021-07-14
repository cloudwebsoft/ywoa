<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%
	String topCode = ParamUtil.get(request,"code");
	int topPrjId = ParamUtil.getInt(request,"id",0);
	String topDayUrl = "myWorkManageInit";
	if (!"".equals(topCode)) {
		topDayUrl = "queryMyWork";
	}
 %>
<div class="tabs1Box">
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/mywork/<%=topDayUrl %>?code=<%=topCode %>&id=<%=topPrjId %>"><span>日报</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/mywork/queryMyWeekWork?code=<%=topCode %>&id=<%=topPrjId %>&logType=1"><span>周报</span></a></li>
    <li id="menu3"><a href="<%=request.getContextPath()%>/mywork/queryMyMonthWork?code=<%=topCode %>&id=<%=topPrjId %>&logType=2"><span>月报</span></a></li>
    
  </ul>
</div>
</div>

