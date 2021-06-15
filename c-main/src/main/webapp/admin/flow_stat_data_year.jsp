<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.workplan.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<%@page import="jofc2.OFCException"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	if (!privilege.isUserPrivValid(request, "admin.flow"))
		return;

	String typeCode = ParamUtil.get(request, "typeCode");
	if (typeCode.equals("")) {
		if (!privilege.isUserPrivValid(request, "admin"))
			return;
	}
	
	String leafName = "全部";
	if (!typeCode.equals("")) {
		Leaf lf = new Leaf();
		lf = lf.getLeaf(typeCode);
		leafName = lf.getName();
	}
	JSONObject json = new JSONObject();
	int showyear;
	Calendar cal = Calendar.getInstance();

	String strshowyear = request.getParameter("showyear");
	
	if (strshowyear != null)
		showyear = Integer.parseInt(strshowyear);
	else
		showyear = cal.get(Calendar.YEAR);

	int totalWorkflow = 0;
	int totalWorkplan = 0;
	int totalDoc = 0;

	LineChart chartFlow = new LineChart(LineChart.Style.NORMAL);
	LineChart chartWorkPlan = new LineChart(LineChart.Style.NORMAL);
	LineChart chartDoc = new LineChart(LineChart.Style.NORMAL);

	double max = 0; // Y轴最大值
	XAxis x = new XAxis(); // X轴
	List<LineChart.Dot> listFlow = new ArrayList<LineChart.Dot>();

	Calendar showCal = Calendar.getInstance();
	MyActionDb mad = new MyActionDb();
	Vector actionV;
	String sql;
	ArrayList al = new ArrayList();
	for (int i = 0; i <= 11; i++) {
		showCal.set(showyear, i, 1);

		String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		
		Calendar nextCal = Calendar.getInstance();
		if (i<=10)
			nextCal.set(showyear, i+1, 1);
		else
			nextCal.set(showyear+1, 0, 1);
		String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		
		// 已办流程
		if (typeCode.equals(""))	
			sql = "select id from flow where begin_date>=" + bdate + " and begin_date<" + edate + " order by begin_date desc";
		else
			sql = "select id from flow where type_code=" + StrUtil.sqlstr(typeCode) + " and begin_date>=" + bdate + " and begin_date<" + edate + " order by begin_date desc";		
				
		actionV = mad.list(sql);
		totalWorkflow += actionV.size();
		if (max<actionV.size())
			max = actionV.size();
		al.add(actionV.size());
		LineChart.Dot d = new LineChart.Dot(actionV.size(), "0xff0000"); //, 8, 0);
		listFlow.add(d);

		x.addLabels("" + (i+1)); //x轴的文字
	}
	
	// chartFlow.setAlpha(0.8f);
	chartFlow.addDots(listFlow);
	chartFlow.setColour("0xff0000");
	chartFlow.setHaloSize(3);
	chartFlow.setWidth(2);
	chartFlow.setDotSize(10);
		
	Chart flashChart = new Chart();
	flashChart.addElements(chartFlow);
	flashChart.addElements(chartWorkPlan);
	flashChart.addElements(chartDoc);
			
	YAxis y = new YAxis(); //y轴
	
	if (max<10)
		max = 10;
	y.setMax(max); //y轴最大值
	y.setSteps(max / 10 * 1.0); //步进
	flashChart.setYAxis(y);
	flashChart.setXAxis(x);

	Text yLegend = new Text("count", "font-size:14px; color:#000000");
	flashChart.setYLegend(yLegend);
	
	Text title = new Text(leafName + " " + showyear + "年   共计：" + totalWorkflow);
	flashChart.setTitle(title);
	
	String printString = "";
	try {
		printString = flashChart.toString();
	} catch (OFCException e) {
		printString = flashChart.toDebugString();
	}
	
	for(int i = 1; i<=al.size(); i++ ){
		json.put("monthData"+i , al.get(i-1));
	}
	json.put("total",totalWorkflow);
	json.put("leafName",leafName);
	out.print(json);
	//out.print(printString);
%>
