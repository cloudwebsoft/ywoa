<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 翻月
int showyear;
Calendar cal = Calendar.getInstance();
int curyear = cal.get(Calendar.YEAR);
String strshowyear = request.getParameter("showyear");
if (strshowyear!=null)
	showyear = Integer.parseInt(strshowyear);
else
	showyear = cal.get(cal.YEAR);

String userName = ParamUtil.get(request, "userName");
if(userName.equals("")){
	userName = privilege.getUser(request);
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作报告统计</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
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
  {"data-file":"mywork_stat_data_year.jsp<%=StrUtil.UrlEncode("?userName=" + StrUtil.UrlEncode(userName) + "&showyear=" + showyear)%>"} );
</script>
</head>
<body>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));

	return;
}
%>
<%@ include file="mywork_nav.jsp"%>
<script>
o("menu3").className="current";
</script>
<br />
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="30" align="center">
      <select name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='?showyear=' + y;">
        <%for (int y=curyear; y>=curyear-60; y--) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
    <script>
	showyear.value = "<%=showyear%>";
	</script>
  </tr>
  <tr>
    <td align="center">
	<div id="lineChart"></div></td>
  </tr>
  <tr>
    <td height="30" align="center">红色：流程&nbsp;&nbsp;&nbsp;&nbsp; 绿色：计划汇报 &nbsp;&nbsp;&nbsp;&nbsp;蓝色：文件</td>
  </tr>
</table>
</body>
</html>
