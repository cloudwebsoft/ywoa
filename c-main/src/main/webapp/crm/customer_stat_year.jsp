<%@ page contentType="text/html; charset=utf-8"%>
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

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>客户年统计</title>
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
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript">
//swfobject.embedSWF(
// "../flash/open-flash-chart.swf", "lineChart",
//  "650", "380", "9.0.0", "expressInstall.swf",
// {"data-file":"customer_stat_data_year.jsp<%=StrUtil.UrlEncode("?userName=" + StrUtil.UrlEncode(userName) + "&showyear=" + showyear)%>"} );


$(function () {
	var url = "customer_stat_data_year.jsp?userName=<%=StrUtil.UrlEncode(userName)%>&showyear=<%=showyear%>";
	$.ajax({
 		type:"get",
 		url:url,
 		success:function(data,status){
 			data = $.parseJSON(data);
 			$('#lineChart').highcharts({
	        title: {
	            text: '客户<%=showyear%>年统计   发现的客户数：'+data.totalFound+'     ，分配的客户数：'+data.totalDistr+'     ，联系的客户数：    '+data.totalContacted,
	            x: -20 //center
	        },
	        subtitle: {
	            text: '',
	            x: -20
	        },
	        xAxis: {
	            categories: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月']
	        },
	        yAxis: {
	            title: {
	                text: '客户数'
	            },
	            min:0,
	            plotLines: [{
	                value: 0,
	                width: 1,
	                color: '#808080'
	            }]
	        },
	        tooltip: {
	            
	            valueSuffix: '人'
	        },
	        legend: {
	            layout: 'vertical',
	            align: 'right',
	            verticalAlign: 'middle',
	            borderWidth: 0
	        },
	        series: [{
	            name: '发现的客户',
	            data: [data.foundC1,data.foundC2,data.foundC3,data.foundC4,data.foundC5,data.foundC6,data.foundC7,data.foundC8,data.foundC9,data.foundC10,
	            		data.foundC11,data.foundC12,data.foundC13,data.foundC14,data.foundC15,data.foundC16,data.foundC17,data.foundC18,data.foundC19,data.foundC20,
	            		data.foundC21,data.foundC22,data.foundC23,data.foundC24,data.foundC25,data.foundC26,data.foundC27,data.foundC28,data.foundC29,data.foundC30,data.foundC31
		            ]
	        }, {
	            name: '分配的客户',
	            data: [data.allocationC1,data.allocationC2,data.allocationC3,data.allocationC4,data.allocationC5,data.allocationC6,data.allocationC7,data.allocationC8,data.allocationC9,data.allocationC10,
						data.allocationC11,data.allocationC12,data.allocationC13,data.allocationC14,data.allocationC15,data.allocationC16,data.allocationC17,data.allocationC18,data.allocationC19,data.allocationC20,
						data.allocationC21,data.allocationC22,data.allocationC23,data.allocationC24,data.allocationC25,data.allocationC26,data.allocationC27,data.allocationC28,data.allocationC29,data.allocationC30,data.allocationC31
					],
				color: '#f77d11'
	        }, {
	            name: '联系的客户',
	            data: [data.contactC1,data.contactC2,data.contactC3,data.contactC4,data.contactC5,data.contactC6,data.contactC7,data.contactC8,data.contactC9,data.contactC10,
	            		data.contactC11,data.contactC12,data.contactC13,data.contactC14,data.contactC15,data.contactC16,data.contactC17,data.contactC18,data.contactC19,data.contactC20,
	            		data.contactC21,data.contactC22,data.contactC23,data.contactC24,data.contactC25,data.contactC26,data.contactC27,data.contactC28,data.contactC29,data.contactC30,data.contactC31
					],
				color: '#54f1a2'
	        }]
	    });
 		},
 		error:function(XMLHttpRequest, textStatus){
 			alert(XMLHttpRequest.responseText);
 		}
	});
	
})
</script>
</head>
<body>
<%
if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));

	return;
}
%>
<%@ include file="../sales/customer_inc_menu_top.jsp"%>
<script>
o("menu8").className="current";
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
    <td height="30" align="center">蓝色：发现的客户&nbsp;&nbsp;&nbsp;&nbsp; 橙色：分配的客户 &nbsp;&nbsp;&nbsp;&nbsp;绿色：联系的客户</td>
  </tr>
</table>
</body>
</html>
