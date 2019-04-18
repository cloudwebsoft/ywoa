<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.worklog.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%
java.util.Date dt = DateUtil.parse(ParamUtil.get(request, "date"), "yyyy-MM-dd");
String userName = ParamUtil.get(request, "userName");
UserDb user = new UserDb();
user = user.getUserDb(userName);
int logType = ParamUtil.getInt(request, "logType", WorkLogDb.TYPE_NORMAL);
%>
<div style="line-height:1.5">
<%
WorkLogDb wld = new WorkLogDb();
if (logType==WorkLogDb.TYPE_NORMAL) {
	wld = wld.getWorkLogDb(userName, dt);
	if (wld==null) {
	%>
		无
	<%
	}
	else {
		%>
		<%=wld.getContent()%><a href="javascript:;" onclick="addTab('<%=user.getRealName()%> 日报', '<%=request.getContextPath()%>/ymoa/showWorkLogById.action?workLogId=<%=wld.getId()%>')">查看详细</a>
		<%
	}
}
else {
	int year = ParamUtil.getInt(request, "year");
	int item = ParamUtil.getInt(request, "item");
	wld = wld.getWorkLogDb(userName, logType, year, item);
	if (wld==null) {
	%>
		无
	<%
	}
	else {
		if (logType==WorkLogDb.TYPE_WEEK) {
		%>
			<%=wld.getContent()%><a href="javascript:;" onclick="addTab('<%=user.getRealName()%> 周报', '<%=request.getContextPath()%>/ymoa/showWorkLogById.action?workLogId=<%=wld.getId()%>')">查看详细</a>
		<%
		}
		else {
		%>
			<%=wld.getContent()%><a href="javascript:;" onclick="addTab('<%=user.getRealName()%> 月报', '<%=request.getContextPath()%>/ymoa/showWorkLogById.action?workLogId=<%=wld.getId()%>')">查看详细</a>
		<%
		}
	}
}
%>
</div>
