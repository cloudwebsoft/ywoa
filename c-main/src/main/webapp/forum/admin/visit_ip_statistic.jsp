<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="org.jfree.chart.*,
org.jfree.chart.labels.*,
org.jfree.chart.plot.*,
org.jfree.data.general.*,
org.jfree.chart.servlet.*,
java.text.*,
java.util.*,
com.cloudwebsoft.framework.db.*,
cn.js.fan.db.*,
cn.js.fan.util.*,
com.redmoon.forum.*,
com.redmoon.forum.util.*,
cn.js.fan.module.pvg.*,
java.awt.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Dir Statistic</title>
<script language="javascript" src="../../inc/common.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">统计</td>
  </tr>
</table>
<%
String op = ParamUtil.get(request, "op");
	
VisitTopicLogDb bvld = new VisitTopicLogDb();
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");

if (op.equals("")) {
	if (beginDate.equals("")) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		beginDate = DateUtil.format(cal, "yyyy-MM-dd 00:00:00");
	}
	if (endDate.equals("")) {
		endDate = DateUtil.format(new java.util.Date(), "yyyy-MM-dd 23:59:59");
	}
}
%>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr>
    <td height=20 align="left" class="thead">来访者位置统计</td>
  </tr>
  <tr>
    <td valign="top"><br>
        <TABLE width="92%" border=0 align=center cellPadding=0 cellSpacing=1>
          <TBODY>
          <form name="fmFilter" action="visit_ip_statistic.jsp?op=search" method="post">
            <TR>
              <TD height=23 colspan="7" align="center">&nbsp;
                  <lt:Label res="res.label.forum.score_transfer" key="beginDate"/>
                  <input type="text" id="beginDate" name="beginDate" size="20">
                  &nbsp;
                  <lt:Label res="res.label.forum.score_transfer" key="endDate"/>
                &nbsp;
                <input type="text" id="endDate" name="endDate" size="20">
                &nbsp;
                <script type="text/javascript">
    function catcalc(cal) {
        var date = cal.date;
        var time = date.getTime()
        // use the _other_ field
        var field = document.getElementById("endDate");
        time += 31*Date.DAY;
        var date2 = new Date(time);
        field.value = date2.print("%Y-%m-%d %H:%M:00");
    }

    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1,                // show all years in drop-down boxes (instead of every other year as default)
		onUpdate       :    catcalc
    });

    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
        showsTime      :    true,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
		
	fmFilter.beginDate.value = "<%=beginDate%>";
	fmFilter.endDate.value = "<%=endDate%>";
</script>
                <input name="submit" type="submit" value="<lt:Label key="ok"/>"></TD>
            </TR>
          </form>
          </TBODY>
          
        </TABLE>
      <br>
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td align="center">
<%	
	String sql = "select count(*) as s, ip_address from " + bvld.getTable().getName();
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = null;
	if (!beginDate.equals("")) {
		sql += " where add_date>=?";
	}
	if (!endDate.equals("")) {
		if (!beginDate.equals(""))
			sql += " and add_date<=?";
		else
			sql += " where add_date<=?";
	}
	sql += " group by ip_address order by s desc";
	
	// out.print(sql + "---" + beginDate + "---" + endDate);
	if (beginDate.equals("") && endDate.equals(""))
		ri = jt.executeQuery(sql);
	else if (beginDate.equals("") && !endDate.equals("")) {
		ri = jt.executeQuery(sql, new Object[] {DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss")});
	}
	else if (!beginDate.equals("") && endDate.equals("")) {
		ri = jt.executeQuery(sql, new Object[] {DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss")});
	}
	else {
		ri = jt.executeQuery(sql, new Object[] {DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss"), DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss")});
	}

	DefaultPieDataset dataset = new DefaultPieDataset();
	while (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		// System.out.println(getClass() + " " + rr.getInt(2));
		dataset.setValue(StrUtil.getNullStr(rr.getString("ip_address")), rr.getInt(1));
	}
		
//通过工厂类生成JFreeChart对象
JFreeChart chart = ChartFactory.createPieChart3D("来访者分布图", dataset, true, false, false);
PiePlot pieplot = (PiePlot) chart.getPlot();
pieplot.setLabelFont(new Font("宋体", 0, 12));
//没有数据的时候显示的内容
pieplot.setNoDataMessage("无数据显示");
pieplot.setCircular(false);
pieplot.setLabelGap(0.02D);

// {0}表示section名，{1}表示section的值，{2}表示百分比。可以自定义。而new DecimalFormat("0.00%")表示小数点后保留两位
pieplot.setLabelGenerator(new StandardPieSectionLabelGenerator(("{0}: {1}({2})"), NumberFormat.getNumberInstance(),new DecimalFormat("0.00%")));

// pieplot.setLegendLabelGenerator(new StandardPieItemLabelGenerator("{0} {2}"));  
	
String filename = ServletUtilities.saveChartAsPNG(chart, 800, 600, null, session);
String graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename;
%>
<img src="<%= graphURL %>" width=800 height=600 border=0 usemap="#<%= filename %>">			
			
			</td>
          </tr>
      </table>
      <br></td>
  </tr>
</table>
</body>
</html>