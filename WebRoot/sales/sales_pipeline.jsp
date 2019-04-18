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
<%!
public String[][] bubbleSort(String[][] array){  
  for (int i = 0; i < array.length-1; i++) {  
	  for (int j = 0; j < array.length-i-1; j++) {  
		  if (StrUtil.toInt(array[j][1])>StrUtil.toInt(array[j+1][1])) {  
			  String[] temp = array[j];
			  array[j] = array[j+1];  
			  array[j+1] = temp;
		  }
	  }
  }         
  return array;
}
%>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>销售漏斗</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/my_highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/funnel.js"></script>
</head>
<body>
<%
String priv = "sales";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"), true));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">销售漏斗</td>
    </tr>
  </tbody>
</table>
<%

String strXML = "<chart baseFontSize='12' showNames='1' showValues='1' numberSuffix='' numberScaleValue='1000,1000' numberScaleUnit='K,M' decimalPrecision='0' isSliced='1' slicingDistance='5'>";
SelectMgr sm = new SelectMgr();
SelectDb sd = sm.getSelect("sales_chance_state");
Vector stateV = sd.getOptions();
Iterator ir = stateV.iterator();

String unitCode = privilege.getUserUnitCode(request);

String sql = "select count(*) from form_table_sales_chance where unit_code=" + StrUtil.sqlstr(unitCode);
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
int sum = 0;
if (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	sum = rr.getInt(1);
}

String[][] ary = new String[stateV.size()][3];

sql = "select count(*) as ct from form_table_sales_chance where state=? and unit_code=" + StrUtil.sqlstr(unitCode);
int i=0;
while(ir.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)ir.next();
	String val = sod.getValue();
	String optName = sod.getName();
	ri = jt.executeQuery(sql, new Object[]{val});
	int c = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		c = rr.getInt(1);
	}
	ary[i][0] = optName;
	ary[i][1] = String.valueOf(c);
	ary[i][2] = val;
	i++;
	
	// System.out.println("<set name='" + optName + "' value='" + c + "' link='n-sales_chance_list.jsp?state=" + sod.getValue() + "' />");
	// strXML += "<set name='" + StrUtil.UrlEncode(optName) + "' value='" + c + "' link='n-sales_chance_list.jsp?state=" + sod.getValue() + "' />";
}

ary = bubbleSort(ary);
for (i=0; i<ary.length; i++) {
	strXML += "<set name='" + StrUtil.UrlEncode(ary[i][0]) + "' value='" + ary[i][1] + "' link='n-sales_chance_list.jsp?state=" + ary[i][2] + "' />";
}
strXML += "</chart>";

Chart chart = new Chart();
String funelSql = "select s.name,count(c.id) from form_table_sales_chance c,oa_select_option s where c.state = s.value and s.code = 'sales_chance_state'and c.unit_code=" + StrUtil.sqlstr(unitCode)+" group by s.name";
JSONArray funnelArr = chart.pieData(funelSql);



%>
<script>
$(function () {
	var params = {data:<%=funnelArr%>,seriesName:'商机次数'};
	funnelChart("#funnelCharts",params);

   
});

</script>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td width="39%">&nbsp;</td>
    <td width="61%">&nbsp;</td>
  </tr>
  <tr>
    <td>
		<div id="funnelCharts" style="width: 100%;height: 100%"></div>
    </td>
    <td>
<table class="tabStyle_1 percent98" width="98%" border="0" cellspacing="0" cellpadding="0">
  <thead>
  <tr>
    <td width="16%"  class="tabStyle_1_title" >商机阶段</td>
    <td width="11%" class="tabStyle_1_title">商机数</td>
    <td width="14%" class="tabStyle_1_title">所占比例</td>
    <td width="19%" class="tabStyle_1_title">预计商机金额</td>
    <td width="20%" class="tabStyle_1_title">平均成交可能性</td>
    <td width="20%" class="tabStyle_1_title">商机需求产品</td>
  </tr>
  </thead>
<%
FormDb fdPro = new FormDb();
fdPro = fdPro.getFormDb("sales_product_info");

FormDAO fdaoPro = new FormDAO();

ir = stateV.iterator();
while (ir.hasNext()) {
	SelectOptionDb sod = (SelectOptionDb)ir.next();
	String color = sod.getColor();
	sql = "select count(*) from form_table_sales_chance where state=? and unit_code=" + StrUtil.sqlstr(unitCode);
	ri = jt.executeQuery(sql, new Object[]{sod.getValue()});
	int c = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		c = rr.getInt(1);
	}
	
	sql = "select sum(p.zj) from form_table_sales_cha_product p, form_table_sales_chance c where c.unit_code=" + StrUtil.sqlstr(unitCode) + " and p.cws_id=c.id and c.state=?" ;
	ri = jt.executeQuery(sql, new Object[]{sod.getValue()});
	double zj = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		zj = rr.getDouble(1);
	}

	sql = "select sum(possibility) from form_table_sales_chance where state=? and unit_code=" + StrUtil.sqlstr(unitCode);
	ri = jt.executeQuery(sql, new Object[]{sod.getValue()});
	double poss = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		poss = rr.getDouble(1);
		if (poss==-1)
			poss = 0;
	}
%>
  <tr>
    <td><a target="_blank" href="sales_chance_list.jsp?op=search&state=<%=sod.getValue()%>"><font color="<%=color%>"><%=sod.getName()%></font></a></td>
    <td><%=c%></td>
    <td><%=sum == 0?0:NumberUtil.round((double)c/sum*100, 1)%>%</td>
    <td><%=NumberUtil.round(zj, 2)%></td>
    <td><%=sum == 0?0:NumberUtil.round(poss/sum, 1)%>%</td>
    <td>
    <%
	sql = "select count(*), p.product from form_table_sales_cha_product p, form_table_sales_chance c where c.unit_code=" + StrUtil.sqlstr(unitCode) + " and p.cws_id=c.id and c.state=? group by p.product";
	ri = jt.executeQuery(sql, new Object[]{sod.getValue()});
	
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		c = rr.getInt(1);
		long productId = StrUtil.toLong(rr.getString(2), -1);
		if (productId==-1)
			continue;
		
		fdaoPro = fdaoPro.getFormDAO(productId, fdPro);
		%>
		<%=fdaoPro.getFieldValue("product_name")%>&nbsp;&nbsp;&nbsp;&nbsp;<%=c%><%=fdaoPro.getFieldValue("measure_unit")%><br />
		<%
	}
	%>
    </td>
  </tr>
<%}%>
</table>
    </td>
  </tr>
</table>

</body>
</html>
