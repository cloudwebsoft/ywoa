<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin.flow";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String typeCode = ParamUtil.get(request, "typeCode");
if (typeCode.equals("")) {
	if (!privilege.isUserPrivValid(request, "admin"))
		return;
}

// 翻月
int showyear,showmonth;
Calendar cal = Calendar.getInstance();
int curyear = cal.get(Calendar.YEAR);
String strshowyear = request.getParameter("showyear");
String strshowmonth = request.getParameter("showmonth");
if (strshowyear!=null)
	showyear = Integer.parseInt(strshowyear);
else
	showyear = cal.get(cal.YEAR);
if (strshowmonth!=null)
	showmonth = Integer.parseInt(strshowmonth);
else
	showmonth = cal.get(cal.MONTH)+1;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作记事统计</title>
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
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/swfobject.js"></script>
<script type="text/javascript">
//swfobject.embedSWF(
//  "../flash/open-flash-chart.swf", "lineChart",
//  "1500", "380", "9.0.0", "expressInstall.swf",
//  {"data-file":"flow_performance_data_person_month.jsp<%=StrUtil.UrlEncode("?typeCode=" + typeCode + "&showyear=" + showyear + "&showmonth=" + showmonth)%>"} );

$(function () {
var url = "flow_performance_data_person_month.jsp?typeCode=<%=StrUtil.UrlEncode(typeCode) %>&showyear=<%= showyear%>&showmonth=<%=showmonth%>";
	$.ajax({
 		type:"get",
 		url:url,
 		success:function(data,status){
 			data = $.parseJSON(data);
 			$('#lineChart').highcharts({
		        chart: {
		            type: 'column',
		            margin: 45,
		            options3d: {
		                enabled: true,
		                alpha: 0,
		                beta: 0,
		                depth: 70
		            }
		        },
		        title: {
		            text: data.leafName + "<%=showyear%> 年<%=showmonth%>月统计   共计：" + data.total
		        },
		        plotOptions: {
		            column: {
		                depth: 25
		            }
		        },
		        legend: {
		            enabled: false
		        },
		        xAxis: {
		            categories: data.name
		        },
		        yAxis: {
		        	min : 0,
		            opposite: false
		        },
		        series: [{
		            data: data.datas
		        }]
		    });
 		},
 		error:function(XMLHttpRequest, textStatus){
 			alert(XMLHttpRequest.responseText);
 		}
	});
    
});
</script>
</head>
<body>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));

	return;
}
%>
<%@ include file="flow_performance_stat_menu_top.jsp"%>
<script>
$("menu3").className="current";
</script>
<br />
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="30" align="center">
      <select name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='?typeCode=<%=StrUtil.UrlEncode(typeCode)%>&showyear=' + y;">
        <%for (int y=curyear; y>=curyear-60; y--) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
    <script>
	showyear.value = "<%=showyear%>";
	</script>
      <%
for (int i=1; i<=12; i++) {
	if (showmonth==i)
		out.print("<a href='flow_stat_person_month.jsp?typeCode="+StrUtil.UrlEncode(typeCode)+"&showyear="+showyear+"&showmonth="+i+"'><font color=red>"+i+"月</font></a>&nbsp;");
	else
		out.print("<a href='flow_stat_person_month.jsp?typeCode="+StrUtil.UrlEncode(typeCode)+"&showyear="+showyear+"&showmonth="+i+"'>"+i+"月</a>&nbsp;");

}
%>
  </tr>
  <tr>
    <td align="center">
	<div id="lineChart" ></div>	</td>
  </tr>
  <tr>
    <td height="30" align="center">&nbsp;</td>
  </tr>
</table>
</body>
</html>
