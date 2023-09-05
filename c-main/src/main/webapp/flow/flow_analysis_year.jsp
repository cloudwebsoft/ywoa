<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
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

String isCompare = ParamUtil.get(request, "isCompare");
if (isCompare.equals(""))
	isCompare = "false";

Leaf lf = new Leaf();
lf = lf.getLeaf(typeCode);

int showyear;
Calendar cal = Calendar.getInstance();
int curyear = cal.get(Calendar.YEAR);
String strshowyear = request.getParameter("showyear");
if (strshowyear!=null) {
    showyear = Integer.parseInt(strshowyear);
} else {
    showyear = cal.get(Calendar.YEAR);
}
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>流程效率分析</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<SCRIPT LANGUAGE= "Javascript" SRC= "../inc/common.js"></SCRIPT>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<SCRIPT LANGUAGE= "Javascript" SRC= "../FusionChartsXT/FusionCharts.js"></SCRIPT>
</head>
<body>
<%@ include file="../admin/flow_inc_menu_top.jsp"%>
<script>
o("menu11").className="current";
</script>
<div class="spacerH"></div>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="30" align="center">
      <select name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='?typeCode=<%=StrUtil.UrlEncode(typeCode)%>&showyear=' + y + '&isCompare=<%=isCompare%>';">
        <%for (int y=curyear; y>=curyear-60; y--) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
      <select name="isCompare" onchange="window.location.href='?typeCode=<%=StrUtil.UrlEncode(typeCode)%>&showyear=<%=showyear%>&isCompare=' + this.value;">
      <option value="false">不排序</option>
      <option value="true">排序</option>
      </select>
    <script>
	o("showyear").value = "<%=showyear%>";
	o("isCompare").value = "<%=isCompare%>";
	</script>
  </tr>
  <tr>
    <td align="center">
<%
WorkflowActionDb wad = new WorkflowActionDb();
boolean isCom = isCompare.equals("true");
for (int i=0; i<12; i++) {%>
<textarea id="preXml<%=i%>" style="display:none">
<chart caption='<%=lf.getName()%> <%=i+1%>月份' xAxisName='动作节点' yAxisName='平均绩效' numberPrefix='' showValues='0'>
<%
Iterator ir = WorkflowActionDb.getActionsPerformance(typeCode, showyear, i, isCom).iterator();
ArrayList arrayObj = new ArrayList();
ArrayList arrayObj2 = new ArrayList();
while (ir.hasNext()) {
	wad = (WorkflowActionDb)ir.next();
	%>
    <set label='<%=wad.getJobName()%>' value='<%=wad.getAveragePerformance()*100%>' />
	<%
	arrayObj.add("'"+wad.getJobName()+"'");
    arrayObj2.add(wad.getAveragePerformance()*100);
}
%>
<trendLines>
<line startValue='60' color='009933' displayvalue='合格' />
</trendLines>
</chart>
</textarea>

<div id="chartDiv<%=i%>"></div>

<script type="text/javascript">
//var myChart<%=i%> = new FusionCharts("../FusionChartsXT/Column3D.swf","myChartId<%=i%>", "600","300");　　　　
//myChart<%=i%>.setXMLData(o("preXml<%=i%>").value);
//myChart<%=i%>.render("chartDiv<%=i%>");

$(function () {
 			$("#chartDiv<%=i%>").highcharts({
		        chart: {
		        	width:'1000',
		            type: 'column',
		            margin: 75,
		            options3d: {
		                enabled: true,
		                alpha: 10,
		                beta: 10,
		                depth: 70
		            }
		        },
		        title: {
		            text: '<%=lf.getName()%> <%=i+1%>月份'
		        },
		        legend:{
		        	itemDistance:1
		        },
		        plotOptions: {
		            column: {
		                depth: 25,
		                pointWidth: 30
		            }
		        },
		        xAxis: {
		        	gridZIndex:10,
		            categories: <%=arrayObj%>,
		            title:{
		            	text:'动作节点'
		            }
		        },
		        yAxis: {
		        	min : 0,
		            opposite: false,
		            title:{
		            	text:'平均绩效'
		            }
		        },
		        series: [{
		        	name: '平均绩效',
		            data: <%=arrayObj2%>,
		        }]
		    });
 		})
</script>
<%}%>
    </td>
  </tr>
  <tr>
    <td height="30" align="center">&nbsp;</td>
  </tr>
</table>
</body>
</html>
