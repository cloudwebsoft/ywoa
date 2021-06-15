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
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@page import="com.redmoon.oa.sale.Chart"%>
<%@page import="net.sf.json.JSONArray"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>业绩分析 - 客户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/my_highcharts.js" ></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="sales_analysis_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<%
Calendar cal = Calendar.getInstance();
int year = ParamUtil.getInt(request, "year", -1);
if (year==-1)
	year = cal.get(Calendar.YEAR);

String analysisType = ParamUtil.get(request, "analysisType");
if (analysisType.equals(""))
	analysisType = "sales_customer_type";
	
Map map = new HashMap();
// 键为基础数据编码，值为字段名
map.put("sales_customer_type", "customer_type");
map.put("customer_jyms", "jyms");
map.put("customer_rygm", "rygm");
map.put("qyxz", "enterType");

SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect(analysisType);
Vector vsd = sd.getOptions();
Iterator irsd = vsd.iterator();

String unitCode = privilege.getUserUnitCode(request);
String sql = "";
JdbcTemplate jt = new JdbcTemplate();
Chart chart = new Chart();
String sqlPie = "select s.name,sum(p.real_sum) from form_table_sales_order o, form_table_sales_ord_product p, form_table_sales_customer c,oa_select_option s";
sqlPie += " where o.unit_code="+StrUtil.sqlstr(unitCode)+" and p.cws_id=o.id and o.cws_id=c.id and "+SQLFilter.year("o.order_date")+"="+year+" and c."+map.get(analysisType)+" = s.value and s.code = "+StrUtil.sqlstr(analysisType)+" group by s.name";
JSONArray pieDate = chart.pieData(sqlPie);


String lineSql = "select "+SQLFilter.month("order_date")+",sum(real_sum)  from form_table_sales_order o, form_table_sales_ord_product p  where o.id=p.cws_id and "+SQLFilter.year("order_date")+"="+year+"   GROUP BY  "+SQLFilter.month("order_date");
JSONArray lineDate = chart.salesAnalysisLineDatas(lineSql);
JSONArray xCa = new JSONArray();
for(int i = 1;i<13;i++){
   xCa.add(i);
}

	

%>
<script >

$(function(){
	var pieDate = <%=pieDate%>;
	var checkText=$("#analysisType").find("option:selected").text();
	var pie_params = {data:pieDate,seriesName:'百分比',title:'订单走势图-'+checkText+'-<%=year%>',tooltip:'{series.name}: <b>{point.percentage:.1f}%</b>',plotOptions:'{point.name}：{point.y}'};
	var line_params = {data:<%=lineDate%>,xCategories:<%=xCa%>,title:'订单走势图-<%=year%>',yTitle:'销售额(元)',unit:'元'};
	pieCharts('#pieContainer',pie_params);
	lineCharts('#lineContainer',line_params);

})
   


</script>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="30" colspan="2" align="center">年度分析
    <select id="year" name="year" onchange="window.location.href='sales_analysis_customer.jsp?year=' + o('year').value">
    <%
	int curYear = Calendar.getInstance().get(Calendar.YEAR);
	for (int i=curYear-20; i<=curYear; i++) {
	%>
    <option value="<%=i%>"><%=i%></option>
    <%}%>
    </select>
    
    <select id="analysisType" name="analysisType" onchange="window.location.href='sales_analysis_customer.jsp?year=<%=year%>&analysisType=' + this.value">
    <option value="sales_customer_type">客户类型</option>
    <option value="customer_jyms">经营模式</option>
    <option value="customer_rygm">人员规模</option>
    <option value="qyxz">单位性质</option>
    </select>
    <script>
	o("year").value = "<%=year%>";
	o("analysisType").value = "<%=analysisType%>";
	</script>
    
    </td>
  </tr>
  <tr>
   <td width="50%" align="center">
    <div id="pieContainer" style="width: 50%;height: 200px"></div>
  
    </td>
    <td width="50%" align="center">&nbsp;
     <div id="lineContainer" style="width: 100%;height: 200px"></div>
  

	</td>
  </tr>
</table>
<%
sql = "select sum(real_sum) from form_table_sales_ord_product p, form_table_sales_order o where p.cws_id=o.id and o.order_date>=? and o.order_date<?";
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
    <th width="40">&nbsp;</th>
<%
irsd = vsd.iterator();
while (irsd.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)irsd.next();
%>  
    <th width="120"><%=sod.getName()%></td>
<%}%>
  </tr>
  </thead>
<%
double[] totalAry = new double[(int)vsd.size()];
for (int i=0; i<totalAry.length; i++) {
	totalAry[i] = 0;
}
for (int i=0; i<12; i++) {
%>
  <tr>
    <td><%=i+1%>月</td>
	<%
    int k=0;
	irsd = vsd.iterator();
	while (irsd.hasNext()) {
		SelectOptionDb sod = (SelectOptionDb)irsd.next();
		        
        ary = DateUtil.getDateSectOfMonth(year, i);
        sql = "select sum(p.real_sum) from form_table_sales_customer c, form_table_sales_order o, form_table_sales_ord_product p where p.cws_id=o.id and o.cws_id=c.id and c." + map.get(analysisType) + "=" + StrUtil.sqlstr(sod.getValue()) + " and o.order_date>=? and o.order_date<?";
        ResultIterator ri2 = jt.executeQuery(sql, new Object[]{ary[0], ary[1]});
        double c = 0.0;
        if (ri2.hasNext()) {
            ResultRecord rr = (ResultRecord)ri2.next();
            c = rr.getDouble(1);
        }
		// System.out.println(getClass() + " k=" + k + " riPro.getTotal()=" + riPro.getTotal());
        totalAry[k] += c;
    %>
      <td><a target="_blank" href="sales_order_customer_list.jsp?analysisType=<%=analysisType%>&analysisTypeValue=<%=StrUtil.UrlEncode(sod.getValue())%>&beginDate=<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>&endDate=<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>"><%=c%></a></td>
    <%	
		k++;
    }%>  
  </tr>
<%}%>
  <tr>
    <td>&nbsp;</td>
    <%for (int i=0; i<totalAry.length; i++) {%>
    <td><%=totalAry[i]%></td>
    <%}%>
  </tr>
</table>
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