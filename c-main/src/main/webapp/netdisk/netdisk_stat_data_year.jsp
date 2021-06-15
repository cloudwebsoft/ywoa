<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	if (!privilege.isUserPrivValid(request, "admin"))
		return;

	int showyear;
	Calendar cal = Calendar.getInstance();

	String strshowyear = request.getParameter("showyear");

	if (strshowyear != null)
		showyear = Integer.parseInt(strshowyear);
	else
		showyear = cal.get(Calendar.YEAR);

	int total = 0;

	double max = 0; // Y轴最大值
	XAxis x = new XAxis(); // X轴

	BarChart chartFlow = new BarChart(BarChart.Style.GLASS);

	Calendar showCal = Calendar.getInstance();
	Vector actionV;
	String sql;
	for (int i = 0; i <= 11; i++) {
		showCal.set(showyear, i, 1);

		String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");

		Calendar nextCal = Calendar.getInstance();
		if (i <= 10)
			nextCal.set(showyear, i + 1, 1);
		else
			nextCal.set(showyear + 1, 0, 1);
		String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");

		sql = "select count(*) from netdisk_document_attach where uploadDate>=" + bdate + " and uploadDate<" + edate;
				
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		int count = 0;
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			count = rr.getInt(1);
		}
		
		total += count;
		if (max < count)
			max = count;

		BarChart.Bar bar = new BarChart.Bar(count, i + "月"); //条标题，显示在x轴上
		bar.setColour("0xff0000"); //颜色
		bar.setTooltip("#val# 个"); //鼠标移动上去后的提示         
		chartFlow.addBars(bar);

		x.addLabels("" + (i + 1)); //x轴的文字
	}

	// chartFlow.setAlpha(0.8f);
	chartFlow.setColour("0xff0000");

	Chart flashChart = new Chart();
	flashChart.addElements(chartFlow);

	YAxis y = new YAxis(); //y轴

	if (max < 10)
		max = 10;
	y.setMax(max); //y轴最大值
	y.setSteps(max / 10 * 1.0); //步进
	flashChart.setYAxis(y);
	flashChart.setXAxis(x);

	Text yLegend = new Text("数量", "font-size:14px; color:#736AFF");
	flashChart.setYLegend(yLegend);

	Text title = new Text(showyear + "年   共计：" + total);
	flashChart.setTitle(title);

	out.print(flashChart.toString());
%>
