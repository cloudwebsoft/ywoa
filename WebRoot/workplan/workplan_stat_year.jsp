<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="workplan.admin";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int typeId = ParamUtil.getInt(request, "typeId", -1);

int showyear;
Calendar cal = Calendar.getInstance();
int curyear = cal.get(Calendar.YEAR);
String strshowyear = request.getParameter("showyear");
if (strshowyear!=null)
	showyear = Integer.parseInt(strshowyear);
else
	showyear = cal.get(Calendar.YEAR);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划统计</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/swfobject.js"></script>
<script type="text/javascript">
swfobject.embedSWF(
  "../flash/open-flash-chart.swf", "lineChart",
  "650", "380", "9.0.0", "expressInstall.swf",
  {"data-file":"workplan_stat_data_year.jsp<%=StrUtil.UrlEncode("?typeId=" + typeId + "&showyear=" + showyear)%>"} );
</script>
<%if (typeId==-1) {%>
<script type="text/javascript">
swfobject.embedSWF(
  "../flash/open-flash-chart.swf", "pieChart",
  "650", "380", "9.0.0", "expressInstall.swf",
  {"data-file":"workplan_stat_data_year.jsp<%=StrUtil.UrlEncode("?chart=pie&typeId=" + typeId + "&showyear=" + showyear)%>"} );
</script>
<%}%>
</head>
<body>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (isShowNav==1) {
%>
<%@ include file="workplan_inc_menu_top.jsp"%>
<%}%>
<br />
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="30" align="center">
      <select name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='?typeId=<%=typeId%>&isShowNav=<%=isShowNav%>&showyear=' + y;">
        <%for (int y=curyear; y>=curyear-60; y--) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
    <script>
	showyear.value = "<%=showyear%>";
	</script>
  工作计划统计
  </tr>
  <tr>
    <td align="center">
<div id="lineChart"></div>
	</td>
  </tr>
  <tr>
    <td align="center"><br />
<div id="pieChart" style="padding-top:15px"></div>
	</td>
  </tr>
</table>
</body>
</html>
