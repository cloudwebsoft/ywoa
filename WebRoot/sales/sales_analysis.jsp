<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="com.redmoon.oa.sale.Chart"%>
<%@page import="com.redmoon.oa.db.SQLUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>业绩分析</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/my_highcharts.js" ></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="sales_analysis_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 

</script>
<div class="spacerH"></div>
<%
Calendar cal = Calendar.getInstance();
int year = ParamUtil.getInt(request, "year", -1);
if (year==-1)
	year = cal.get(Calendar.YEAR);
	
String unitCode = privilege.getUserUnitCode(request);
	
String sql = "select id,product_name from form_table_sales_product_info where state='在售' and unit_code=" + StrUtil.sqlstr(unitCode);
String sqlPie = "select pro.product_name ,sum(real_sum)from form_table_sales_order o, form_table_sales_ord_product p ,form_table_sales_product_info pro where o.id=p.cws_id and p.product=pro.id and state='在售' and pro.unit_code="+StrUtil.sqlstr(unitCode)+" and  "+SQLFilter.year("o.order_date")+"="+year+" group by pro.product_name";
JdbcTemplate jt = new JdbcTemplate();
ResultIterator riPro = jt.executeQuery(sql);
Chart chart = new Chart();
JSONArray pieDate = chart.pieData(sqlPie);
String lineSql = "select "+SQLFilter.month("order_date")+",sum(real_sum)  from form_table_sales_order o, form_table_sales_ord_product p  where o.id=p.cws_id and "+SQLFilter.year("order_date")+"="+year+"   GROUP BY  "+SQLFilter.month("order_date");
JSONArray lineDate = chart.salesAnalysisLineDatas(lineSql);
JSONArray xCa = new JSONArray();
for(int i = 1;i<13;i++){
   xCa.add(i);
}

%>
<script >
var pieDate = <%=pieDate%>;
var pie_params = {data:pieDate,seriesName:'百分比',title:'订单走势图-按产品分析-<%=year%>',tooltip:'{series.name}: <b>{point.percentage:.1f}%</b>',plotOptions:'{point.name}：{point.y}'};
var line_params = {data:<%=lineDate%>,xCategories:<%=xCa%>,title:'订单走势图-<%=year%>',yTitle:'销售额(元)',unit:'元'};
$(function(){
	pieCharts('#pieContainer',pie_params);
	lineCharts('#pieContainer2',line_params);

})
   


</script>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="30" colspan="2" align="center">年度分析
    <select id="year" name="year" onchange="window.location.href='sales_analysis.jsp?year=' + o('year').value">
    <%
	int curYear = Calendar.getInstance().get(Calendar.YEAR);
	for (int i=curYear-20; i<=curYear; i++) {
	%>
    <option value="<%=i%>"><%=i%></option>
    <%}%>
    </select>
    <script>
	o("year").value = "<%=year%>";
	</script></td>
  </tr>
  <tr>
    <td width="50%" align="center">
    <div id="pieContainer" style="width: 50%;height: 200px"></div>
  
    </td>
    <td width="50%" align="center">&nbsp;
     <div id="pieContainer2" style="width: 100%;height: 200px"></div>
  

	</td>
  </tr>
</table>
<%
sql = "select sum(real_sum) from form_table_sales_ord_product p, form_table_sales_order o where o.unit_code=" + StrUtil.sqlstr(unitCode) + " and p.cws_id=o.id and o.order_date>=? and o.order_date<?";
Date[] ary = DateUtil.getDateSectOfYear(year);
ResultIterator ri = jt.executeQuery(sql, new Object[]{ary[0], ary[1]});
double sum = 0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	sum = rr.getDouble(1);
}
%>
<table width="98%" class="percent98" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td><%=year%>&nbsp;销售额：<%=sum%></td>
  </tr>
</table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
  <thead>
  <tr>
    <th width=40>&nbsp;</th>
<%
riPro.beforeFirst();
while (riPro.hasNext()) {
	ResultRecord rr = (ResultRecord)riPro.next();
%>  
    <th style="text-align:center;" width="600" colspan="2"><%=rr.getString("product_name")%><td></td>
<%}%>
  </tr>
  </thead>
<%
double[] totalPro = new double[(int)riPro.size()];
int[] countPro = new int[(int)riPro.size()];
for (int i=0; i<totalPro.length; i++) {
	totalPro[i] = 0;
	countPro[i] = 0;
}
for (int i=0; i<12; i++) {
%>
  <tr>
    <td class="month"><%=i+1%>月</td>
	<%
    riPro.beforeFirst();
    int k=0;
    while (riPro.hasNext()) {
        ResultRecord rr = (ResultRecord)riPro.next();
        int proId = rr.getInt(1);
        
        ary = DateUtil.getDateSectOfMonth(year, i);
        sql = "select sum(real_sum) from form_table_sales_ord_product p, form_table_sales_order o where o.unit_code=" + StrUtil.sqlstr(unitCode) + " and p.cws_id=o.id and p.product=" + proId + " and o.order_date>=? and o.order_date<?";
        ResultIterator ri2 = jt.executeQuery(sql, new Object[]{ary[0], ary[1]});
        double c = 0.0;
        if (ri2.hasNext()) {
            rr = (ResultRecord)ri2.next();
            c = rr.getDouble(1);
        }
		// System.out.println(getClass() + " k=" + k + " riPro.getTotal()=" + riPro.getTotal());
        totalPro[k] += c;
        sql = "select sum(num) from form_table_sales_ord_product p, form_table_sales_order o where o.unit_code=" + StrUtil.sqlstr(unitCode) + " and p.cws_id=o.id and p.product=" + proId + " and o.order_date>=? and o.order_date<?";
		ri2 = jt.executeQuery(sql, new Object[]{ary[0], ary[1]});
		int count = 0;
		if (ri2.hasNext()) {
            rr = (ResultRecord)ri2.next();
            count = rr.getInt(1);
			if (count<0)
				count = 0;
		}
		countPro[k] += count;
    %>
      <td>
        <%=count%> 
      </td>
      <td><a href="sales_order_product_list.jsp?productId=<%=proId%>&beginDate=<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>&endDate=<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>" target="_blank"><%=c%></a></td>
    <%	
		k++;
    }%>  
  </tr>
<%}%>
  <tr>
    <td class="month">合计</td>
    <%for (int i=0; i<totalPro.length; i++) {%>
    <td><%=countPro[i]%></td>
    <td><%=totalPro[i]%></td>
    <%}%>
  </tr>
</table>
<br />
<script>
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
			url: false,
			checkbox : false,
			
			singleSelect: true,
			resizable: false,
			showTableToggleBtn: true,
			showToggleBtn: false,
			
			onReload: onReload,

			autoHeight: true,
			width: document.documentElement.clientWidth,
			height: document.documentElement.clientHeight - 84
			}
		);
});

function onReload() {
	window.location.reload();
}
</script>

</body>
</html>
<style>
#grid tr td{width:295px;}
#grid tr td div{width:295px !important;}
#grid tr td.month{width:40px !important;}
#grid tr td.month div{width:40px !important;}
</style>  