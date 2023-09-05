<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
if (!dirCode.equals("")) {
	Leaf lf = new Leaf();
	lf = lf.getLeaf(dirCode);
	if (lf==null) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
		<%
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "目录不存在"));
		return;	
	}
}
LeafPriv lp;
// 如果dirCode为空，则表示统计全部
if ("".equals(dirCode)) {
	lp = new LeafPriv(Leaf.ROOTCODE);
}
else {
	lp = new LeafPriv(dirCode);
}
if (!lp.canUserExamine(privilege.getUser(request))) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

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
<title>文件柜统计</title>
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
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/swfobject.js"></script>
<script type="text/javascript">
//swfobject.embedSWF(
// "../flash/open-flash-chart.swf", "lineChart",
//  "650", "380", "9.0.0", "expressInstall.swf",
//  {"data-file":"fileark_stat_data_year.jsp<%=StrUtil.UrlEncode("?dirCode=" + dirCode + "&showyear=" + showyear)%>"} );

﻿$(function () {
var url = "fileark_stat_data_year.jsp?dirCode=<%=StrUtil.UrlEncode(dirCode) %> &showyear=<%= showyear%>";
	$.ajax({
 		type:"get",
 		url:url,
 		success:function(data,status){
 			data = $.parseJSON(data);
 			$('#lineChart').highcharts({
		        chart: {
		            type: 'column',
		            margin: 75,
		            options3d: {
		                enabled: true,
		                alpha: 15,
		                beta: 15,
		                depth: 70
		            }
		        },
		        title: {
		            text: '<%=showyear%>年度统计   共计:'+data.total
		        },
		        plotOptions: {
		            column: {
		                depth: 25
		            }
		        },
		        xAxis: {
		            categories: ["一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"]
		        },
		        yAxis: {
		            opposite: false
		        },
		        series: [{
		            name: '个数',
		            data: [data.mData1, data.mData2, data.mData3, data.mData4, data.mData5, data.mData6, data.mData7, data.mData8, data.mData9, data.mData10, data.mData11,data.mData12]
		        }]
		    });
 		},
 		error:function(XMLHttpRequest, textStatus){
 			alert(XMLHttpRequest.responseText);
 		}
	});
    
});						
</script>
<%if (dirCode.equals("")) {%>
<script type="text/javascript">
//swfobject.embedSWF(
//  "../flash/open-flash-chart.swf", "pieChart",
//  "650", "380", "9.0.0", "expressInstall.swf",
 // {"data-file":"fileark_stat_data_year.jsp<%=StrUtil.UrlEncode("?chart=pie&dirCode=" + dirCode + "&showyear=" + showyear)%>"} );
 
$(function () {
var url = "fileark_stat_data_year.jsp?chart=pie&dirCode=<%=StrUtil.UrlEncode(dirCode) %> &showyear=<%= showyear%>";
var i = 0;
	$.ajax({
 		type:"get",
 		url:url,
 		success:function(data,status){
 			data = $.parseJSON(data);
	 			$('#pieChart').highcharts({
		        chart: {
		            type: 'pie',
		            options3d: {
		                enabled: true,
		                alpha: 45,
		                beta: 0
		            }
		        },
		        title: {
		            text: '<%=showyear%>年度统计   共计:'+data.total
		        },
		        plotOptions: {
		            pie: {
		                allowPointSelect: true,
		                cursor: 'pointer',
		                depth: 35,
		                dataLabels: {
		                    enabled: true,
		                    format: '{point.name}'
		                }
		            }
		        },
		        series: [{
		            type: 'pie',
		            data: data.jsa
		        }]
		    });
 		},
 		error:function(XMLHttpRequest, textStatus){
 			alert(XMLHttpRequest.responseText);
 		}
	});
});	

</script>
<%}%>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">
        统计信息
      </td>
    </tr>
  </tbody>
</table>
<%
int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);
if (isShowNav==1) {
%>
<%}%>
<br />
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="30" align="center">
      <select name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='?dirCode=<%=dirCode%>&isShowNav=<%=isShowNav%>&showyear=' + y;">
        <%for (int y=curyear; y>=curyear-60; y--) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
    <script>
	o("showyear").value = "<%=showyear%>";
	</script>
  </tr>
  <tr><td></td></tr>
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
  <tr></tr>
</table>
<br/>
<br/>
</body>
</html>
