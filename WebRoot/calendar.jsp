<%@ page contentType="text/html;charset=gb2312"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.io.File"%>
<%@ page import = "java.util.Calendar"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="calsheet" scope="page" class="com.redmoon.oa.CalendarSheet"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE>日程安排</TITLE>
<META http-equiv=Content-Type content="text/html; charset=gb2312">
<link rel="stylesheet" href="common.css" type="text/css">
<%@ include file="inc/nocache.jsp" %>
<%
out.print(calsheet.changeCld_Script("calendar.jsp"));
%>
<META content="MSHTML 6.00.2600.0" name=GENERATOR></HEAD>
<BODY bgColor=#ffffff leftMargin=0 topMargin=3 marginheight="3" marginwidth="0">
<%
	int displayYear = 0;
	int displayMonth = 0;
	String stryear = request.getParameter("displayYear");
	String strmonth = request.getParameter("displayMonth");
	try {
		if (stryear!=null)
			displayYear = Integer.parseInt(stryear);
		if (strmonth!=null)
			displayMonth = Integer.parseInt(strmonth);
	}
	catch (Exception e){
		out.println(e.getMessage());
		return;
	}
	
	Calendar cal = Calendar.getInstance();
	int year = 0;
	int month = 0;
	if (stryear==null)
		year = cal.get(cal.YEAR);
	else
		year = displayYear;
	if (strmonth==null)
		month = cal.get(cal.MONTH);
	else
		month = displayMonth;
	String caltable = calsheet.newCalendar(year,month);
	out.print(caltable);
%>
</BODY>
</HTML>
