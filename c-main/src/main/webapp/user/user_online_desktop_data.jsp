<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	if (!privilege.isUserPrivValid(request, "read"))
		return;
	
	int totalWorkflow = 0;
	int totalWorkplan = 0;
	int totalDoc = 0;

	BarChart chartFlow = new BarChart(BarChart.Style.GLASS);

	double max = 0; // Y轴最大值
	XAxis x = new XAxis(); // X轴
	
	String sql = "select name from users where isValid=1 order by online_time desc";
	UserDb user = new UserDb();
	Vector v = user.list(sql);
	Iterator ir = v.iterator();
	int k = 0;
	while (ir.hasNext()) {
		user = (UserDb)ir.next();
        BarChart.Bar bar = new BarChart.Bar(user.getOnlineTime(), user.getRealName());       //条标题，显示在x轴上
        bar.setColour("0xff0000"); //颜色
        bar.setTooltip("#val#");            //鼠标移动上去后的提示 
       
        chartFlow.addBars(bar);

		k++;
		x.addLabels(user.getRealName()); //x轴的文字
		
		if (user.getOnlineTime()>max)
			max = user.getOnlineTime();
		
		if (k >= 10) {
			break;
		}
	}

	// chartFlow.setAlpha(0.8f);
	chartFlow.setColour("0x00ff00");

	Chart flashChart = new Chart();
	flashChart.addElements(chartFlow);

	YAxis y = new YAxis(); //y轴
	
	// if (max<10)
	// 	max = 10;
	y.setMax(max); //y轴最大值
	y.setSteps(max / 10 * 1.0); //步进
	flashChart.setYAxis(y);
	flashChart.setXAxis(x);

	Text yLegend = new Text("online", "font-size:14px; color:#736AFF");
	flashChart.setYLegend(yLegend);
	
	Text title = new Text("在线排行");
	flashChart.setTitle(title);
	
	
	out.print(flashChart.toString());
%>
