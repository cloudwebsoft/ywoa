<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.io.File"%>
<%@ page import = "java.util.Calendar"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="plan" scope="page" class="com.redmoon.oa.person.PlanMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserLogin(request))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>日程安排</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
</HEAD>
<BODY>
<%
out.print(plan.changeCld_Script("plan.jsp"));
%>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
$("menu1").className="current";
</script>
<table class="tabStyle_1 percent60">
  <tr> 
    <td class="tabStyle_1_title">日程安排</td>
  </tr>
  <tr> 
    <td align="center" valign="top"> <p><br>
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
	String caltable = plan.newCalendar(privilege.getUser(request),year,month);
	out.print(caltable);
%>
        <br>
        &nbsp;&nbsp; 
      </p></td>
  </tr>
</table>
</BODY>
</HTML>
