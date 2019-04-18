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
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String unitCode = privilege.getUserUnitCode(request);

String depts = ParamUtil.get(request, "depts");
if (depts.equals(""))
	depts = unitCode;
Calendar cal = Calendar.getInstance();
int year = ParamUtil.getInt(request, "year", -1);
if (year==-1)
	year = cal.get(Calendar.YEAR);	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>业绩分析 - 客户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<SCRIPT LANGUAGE= "Javascript" SRC= "../FusionCharts/FusionCharts.js"></SCRIPT>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function getDepts() {
	return o("depts").value;
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	o("spanDeptNames").innerText = "";
	o("depts").value = "";
	for (var i=0; i<ret.length; i++) {
		if (o("spanDeptNames").innerText=="") {
			o("depts").value += ret[i][0];
			o("spanDeptNames").innerText += ret[i][1];
		}
		else {
			o("depts").value += "," + ret[i][0];
			o("spanDeptNames").innerText += "," + ret[i][1];
		}
	}
	if (o("depts").value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		o("depts").value = "<%=DeptDb.ROOTCODE%>";
		o("spanDeptNames").innerText = "全部";
	}
	window.location.href = "sales_analysis_user.jsp?year=<%=year%>&depts=" + o("depts").value;
}
</script>
</head>
<body>
<%@ include file="sales_analysis_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<div class="spacerH"></div>
<%
Vector v = new Vector();
JdbcTemplate jt = new JdbcTemplate();
if (depts.equals(unitCode)) {
	v = privilege.getUsersHavePriv("sales.user", unitCode);
}
else {
	String[] deptAry = StrUtil.split(depts, ",");
	String ds = "";
	for (int i=0; i<deptAry.length; i++) {
		try {
			com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "depts", deptAry[i], getClass().getName());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
		
		if (ds.equals(""))
			ds = StrUtil.sqlstr(deptAry[i]);
		else
			ds += "," + StrUtil.sqlstr(deptAry[i]);
	}
	String sql = "select user_name from dept_user where dept_code in (" + ds + ")";
	ResultIterator ri = jt.executeQuery(sql);
	UserMgr um = new UserMgr();
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		UserDb user = um.getUserDb(rr.getString(1));
		v.addElement(user);
	}
}

String strXML = "<graph caption='" + StrUtil.UrlEncode("订单走势图 - 按客户类型分析 - " + year) + "' decimalPrecision='2' baseFontSize='12' baseFontSize='12' showNames='1' showValues='1' decimalPrecision='2' formatNumberScale='0' formatNumber='0'>";
Iterator ir = v.iterator();
while (ir.hasNext()) {
	UserDb user = (UserDb)ir.next();

	Date[] ary = DateUtil.getDateSectOfYear(year);
	String sql = "select sum(p.real_sum) from form_table_sales_order o, form_table_sales_ord_product p where p.cws_id=o.id and o.cws_creator=? and o.order_date>=? and o.order_date<?";
	ResultIterator ri2 = jt.executeQuery(sql, new Object[]{user.getName(), ary[0], ary[1]});
	double c = 0;
	if (ri2.hasNext()) {
		ResultRecord rr = (ResultRecord)ri2.next();
		c = rr.getDouble(1);
	}
	strXML += "<set name='" + StrUtil.UrlEncode(user.getRealName()) + "' value='" + NumberUtil.round(c, 2) + "' />";
}
strXML += "</graph>";
%>
<table width="98%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td colspan="2" align="center">
    &nbsp;部门：<span id="spanDeptNames">
	<%
	String[] arydepts = StrUtil.split(depts, ",");  	  
	int len = 0;
	String deptNames = "";
	if (arydepts!=null) {
	  len = arydepts.length;
	  DeptDb dd = new DeptDb();
	  for (int i=0; i<len; i++) {
		  dd = dd.getDeptDb(arydepts[i]);
		  if (deptNames.equals("")) {
			  deptNames = dd.getName();
		  }
		  else {
			  deptNames += "," + dd.getName();
		  }
	  }
	}
	if (deptNames.equals(""))
		deptNames = "全部";
	%>
    <%=deptNames%>
    </span>
    &nbsp;&nbsp;<a href="javascript:;" onclick="openWinDepts()">选择部门</a>
    <input id="depts" name="depts" type="hidden" value="<%=depts%>" />
    &nbsp;&nbsp;年度分析
    <select id="year" name="year" onchange="window.location.href='sales_analysis_customer.jsp?year=' + o('year').value">
    <%
	int curYear = Calendar.getInstance().get(Calendar.YEAR);
	for (int i=curYear-20; i<=curYear; i++) {
	%>
    <option value="<%=i%>"><%=i%></option>
    <%}%>
    </select>
    <script>
	o("year").value = "<%=year%>";
	</script>
    </td>
  </tr>
  <tr>
    <td width="50%">
    <jsp:include page="../FusionCharts/Includes/FusionChartsRenderer.jsp" flush= "true">
    <jsp:param name="chartSWF" value= "../FusionCharts/FCF_Pie2D.swf" />    
    <jsp:param name="strURL" value= "" />    
    <jsp:param name="strXML" value= "<%=strXML%>" />    
    <jsp:param name="chartId" value= "myPie" />    
    <jsp:param name="chartWidth" value= "400" />    
    <jsp:param name="chartHeight" value= "200" />    
    <jsp:param name="debugMode" value= "false" />    
    <jsp:param name="registerWithJS" value= "false" />    
    </jsp:include>    
    </td>
    <td width="50%">
<%
strXML = "<graph caption='" + StrUtil.UrlEncode("订单走势图 - " + year) + "' baseFontSize='12' yAxisNam='Units' baseFontSize='12' showNames='1' showValues='1' decimalPrecision='0' formatNumberScale='0' formatNumber='0'>";
String sql = "select sum(real_sum) from form_table_sales_order o, form_table_sales_ord_product p where o.unit_code=" + StrUtil.sqlstr(unitCode) + " and o.id=p.cws_id and o.order_date>=? and o.order_date<?";
boolean isZero = true;
for (int i=0; i<12; i++) {
	Date[] ary = DateUtil.getDateSectOfMonth(year, i);
	ResultIterator ri = jt.executeQuery(sql, new Object[]{ary[0], ary[1]});
	double c = 0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		c = rr.getDouble(1);
		if (c != 0.0) {
			isZero = false;
		}
	}
	strXML += "<set name='" + (i+1) + StrUtil.UrlEncode("月") + "' value='" + c + "' />";
	
}
strXML += "</graph>";
if (isZero) {
	strXML = "<graph caption='" + StrUtil.UrlEncode("订单走势图 - " + year) + "' baseFontSize='12' yAxisNam='Units' baseFontSize='12' showNames='1' showValues='1' decimalPrecision='0' formatNumberScale='0' formatNumber='0'></graph>";
}
%>    
    <jsp:include page="../FusionCharts/Includes/FusionChartsRenderer.jsp" flush= "true">
    <jsp:param name="chartSWF" value= "../FusionCharts/FCF_Line.swf" />    
    <jsp:param name="strURL" value= "" />    
    <jsp:param name="strXML" value= "<%=strXML%>" />    
    <jsp:param name="chartId" value= "myLine" />    
    <jsp:param name="chartWidth" value= "400" />    
    <jsp:param name="chartHeight" value= "200" />    
    <jsp:param name="debugMode" value= "false" />    
    <jsp:param name="registerWithJS" value= "false" />    
    </jsp:include>
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
    <th width="40">&nbsp;</th>
<%
ir = v.iterator();
while (ir.hasNext()) {
	UserDb user = (UserDb)ir.next();
%>  
    <td width="130"><%=user.getRealName()%></td>
<%}%>
  </tr>
  </thead>
  <tbody>
<%
double[] totalAry = new double[(int)v.size()];
for (int i=0; i<totalAry.length; i++) {
	totalAry[i] = 0;
}
for (int i=0; i<12; i++) {
%>
  <tr>
    <td><%=i+1%>月</td>
	<%
    int k=0;
	ir = v.iterator();
	while (ir.hasNext()) {
		UserDb user = (UserDb)ir.next();
		        
        ary = DateUtil.getDateSectOfMonth(year, i);
        sql = "select sum(p.real_sum) from form_table_sales_order o, form_table_sales_ord_product p where p.cws_id=o.id and o.cws_creator=? and o.order_date>=? and o.order_date<?";
        ResultIterator ri2 = jt.executeQuery(sql, new Object[]{user.getName(), ary[0], ary[1]});
        double c = 0.0;
        if (ri2.hasNext()) {
            ResultRecord rr = (ResultRecord)ri2.next();
            c = rr.getDouble(1);
        }
		// System.out.println(getClass() + " k=" + k + " riPro.getTotal()=" + riPro.getTotal());
        totalAry[k] += c;
    %>
      <td style="width:130px;"><a target="_blank" href="sales_order_user_list.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>&beginDate=<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>&endDate=<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>"><%=c%></a></td>
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
  </tbody>
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