<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="org.jfree.chart.ChartFactory,
				 org.jfree.chart.JFreeChart,
				 org.jfree.chart.plot.PlotOrientation,
				 org.jfree.chart.servlet.ServletUtilities,
 				 org.jfree.data.category.CategoryDataset,
				 org.jfree.data.general.DatasetUtilities,
				 org.jfree.chart.plot.*,
				 org.jfree.chart.labels.*,
				 org.jfree.chart.renderer.category.BarRenderer3D,
				 java.awt.*,
				 org.jfree.ui.*,
				 org.jfree.chart.axis.AxisLocation"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
	String filename = "";
	String graphURL = "";
	String strStartDate = "";
	String strEndDate = "";
	long lStartDate = 0;
	long lEndDate = 0;
	String op = StrUtil.getNullString(ParamUtil.get(request, "op"));

	String beginDate = ParamUtil.get(request, "beginDate");
	String endDate = ParamUtil.get(request, "endDate");
	
	if (op.equals("")) {
		if (beginDate.equals("")) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 1);
			beginDate = DateUtil.format(cal, "yyyy-MM-dd 00:00:00");
		}
		if (endDate.equals("")) {
			endDate = DateUtil.format(new java.util.Date(), "yyyy-MM-dd 00:00:00");
		}
	}
		
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator rsir = jt.executeQuery("select code from sq_board where type=" + Leaf.TYPE_BOARD + " order by layer,parent_code,orders");
	
	double[][] data = new double[2][rsir.size()];
	String[] columnKeys = new String[rsir.size()];

	Directory dir = new Directory();
	String sql = "select count(*) from sq_message where check_status=1 and boardcode=?";
	String sql2 = "select count(*) from sq_message where replyid=-1 and check_status=1 and boardcode=?";			
	
	if (!beginDate.equals("")) {
		sql += " and lydate>=" + StrUtil.sqlstr(DateUtil.toLongString(DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss")));
		sql2 += " and lydate>=" + StrUtil.sqlstr(DateUtil.toLongString(DateUtil.parse(beginDate, "yyyy-MM-dd HH:mm:ss")));
	}
	if (!endDate.equals("")) {
		sql += " and lydate<=" + StrUtil.sqlstr(DateUtil.toLongString(DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss")));
		sql2 += " and lydate<=" + StrUtil.sqlstr(DateUtil.toLongString(DateUtil.parse(endDate, "yyyy-MM-dd HH:mm:ss")));			
	}	
	
	int k = 0;
	ResultIterator ri;
	while (rsir.hasNext()) {
		ResultRecord rr = (ResultRecord)rsir.next();
		Leaf lf = dir.getLeaf(rr.getString(1));
		int count = 0, count2=0;
		ri = jt.executeQuery(sql, new Object[] {lf.getCode()});
		if (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			count = rr.getInt(1);
		}
		
		ri = jt.executeQuery(sql2, new Object[] {lf.getCode()});
		if (ri.hasNext()) {
			rr = (ResultRecord)ri.next();
			count2 = rr.getInt(1);
		}
		
		data[0][k] = count;
		data[1][k] = count2;
		columnKeys[k] = lf.getName();
		
		k++;
	}

	
	String[] rowKeys = {"全部贴数", "主题贴数"};

	CategoryDataset dataset = DatasetUtilities.createCategoryDataset(rowKeys, columnKeys, data);
	
	JFreeChart chart = ChartFactory.createBarChart3D("发贴量统计图", "日期", "发贴量", dataset, PlotOrientation.HORIZONTAL, true, true, false);
	CategoryPlot plot = chart.getCategoryPlot();
	plot.setBackgroundPaint(Color.white);
	plot.setDomainGridlinePaint(Color.pink);
	plot.setRangeGridlinePaint(Color.pink);
	BarRenderer3D renderer = new BarRenderer3D();
	renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
	renderer.setBaseItemLabelsVisible(true);
	renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.BASELINE_LEFT));
	renderer.setItemLabelAnchorOffset(20D);
	renderer.setItemMargin(0.1);
	plot.setRenderer(renderer);
	plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
	plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
	filename = ServletUtilities.saveChartAsPNG(chart, 700, 60*rsir.size(), null, session);
	graphURL = request.getContextPath() + "/servlet/DisplayChart?filename=" + filename;
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<script type="text/javascript" src="../../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../../util/jscalendar/calendar-win2k-2.css"); </style>
</head>
<body>
	<table width='100%' cellpadding='0' cellspacing='0' >
      <tr>
        <td class="head">统计</td>
      </tr>
    </table>
	<table width="100%" border="0">
	  <tr><td align="center">
	<form action="board_topic_post_statistic.jsp?op=query"  method="post">
		<input type="text" id="beginDate" name="beginDate" value="<%=beginDate%>" size="10">
		<script type="text/javascript">
    function catcalc(cal) {
        var date = cal.date;
        var time = date.getTime()
        var field = document.getElementById("endDate");
        time += 31*Date.DAY; // add one week
        var date2 = new Date(time);
        field.value = date2.print("%Y-%m-%d %H:%M:00");
    }
			
			Calendar.setup({
				inputField     :    "beginDate",
				ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
		        showsTime      :    true,            // will display a time selector
				singleClick    :    false,
				align          :    "Tl",
				step           :    1,
				onUpdate       :    catcalc
			});
		</script>
		&nbsp;&nbsp;<font color="#FF4B4B">至</font>&nbsp;&nbsp;
		<input type="text" id="endDate" name="endDate" value="<%=endDate%>" size="10">
		<script type="text/javascript">
			Calendar.setup({
				inputField     :    "endDate",
				ifFormat       :    "%Y-%m-%d %H:%M:00",       // format of the input field
		        showsTime      :    true,            // will display a time selector				
				singleClick    :    false,
				align          :    "Tl",
				step           :    1
			});
		</script>
		<input type="submit" value="查询" />
	</form>
</td>
	  </tr>
	  <tr>
	    <td align="center"><br />
          <%
	if(!graphURL.equals("")) {
%>
          <img src="<%=graphURL%>" border=0 usemap="#<%=filename%>" />
          <%
	}
%></td>
      </tr>
	</table>
</body>
</html>
