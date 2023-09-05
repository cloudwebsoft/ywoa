<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<%@page import="jofc2.OFCException"%>
<%@page import="org.json.JSONObject"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	if (!privilege.isUserPrivValid(request, "admin.flow")) {
		return;
	}
	
	String typeCode = ParamUtil.get(request, "typeCode");
	if (typeCode.equals("")) {
		if (!privilege.isUserPrivValid(request, "admin")) {
			return;
		}
	}
	JSONObject json = new JSONObject();
	String leafName = "全部";
	if (!"".equals(typeCode)) {
		Leaf lf = new Leaf();
		lf = lf.getLeaf(typeCode);
		leafName = lf.getName();
	}

	int showyear, showmonth;
	Calendar cal = Calendar.getInstance();

	String strshowyear = request.getParameter("showyear");
	String strshowmonth = request.getParameter("showmonth");
	
	if (strshowyear != null) {
		showyear = Integer.parseInt(strshowyear);
	} else {
		showyear = cal.get(Calendar.YEAR);
	}
	if (strshowmonth != null) {
		showmonth = Integer.parseInt(strshowmonth);
	} else {
		showmonth = cal.get(Calendar.MONTH) + 1;
	}
	
	int totalWorkflow = 0;
	int totalWorkplan = 0;
	int totalDoc = 0;

	int days = DateUtil.getDayCount(showyear, showmonth - 1);

	BarChart chartFlow = new BarChart(BarChart.Style.GLASS);

	double max = 0; // Y轴最大值
	XAxis x = new XAxis(); // X轴

	Calendar showCal = Calendar.getInstance();
	MyActionDb mad = new MyActionDb();
	Vector actionV;
	String sql;
	ArrayList al = new ArrayList();
	for (int i = 1; i <= days; i++) {
		showCal.set(showyear, showmonth - 1, i);

		String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		Calendar nextCal = Calendar.getInstance();
		nextCal.set(showyear, showmonth - 1, i + 1);
		String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");

		if ("".equals(typeCode)) {
			sql = "select id from flow where begin_date>=" + bdate + " and begin_date<" + edate + " order by begin_date desc";
		} else {
			sql = "select id from flow where type_code=" + StrUtil.sqlstr(typeCode) + " and begin_date>=" + bdate + " and begin_date<" + edate + " order by begin_date desc";
		}
		
		actionV = mad.list(sql);
		totalWorkflow += actionV.size();
		al.add(actionV.size());
		if (max<actionV.size()) {
			max = actionV.size();
		}

        BarChart.Bar bar = new BarChart.Bar(actionV.size(),i+"月");       //条标题，显示在x轴上
        bar.setColour("0xff0000"); //颜色
        bar.setTooltip("#val# 个");            //鼠标移动上去后的提示 
       
        chartFlow.addBars(bar); 

		x.addLabels("" + i); //x轴的文字
	}

	// chartFlow.setAlpha(0.8f);
	chartFlow.setColour("0xff0000");

	Chart flashChart = new Chart();
	flashChart.addElements(chartFlow);

			
	YAxis y = new YAxis(); //y轴
	
	if (max<10) {
		max = 10;
	}
	y.setMax(max); //y轴最大值
	y.setSteps(max / 10 * 1.0); //步进
	flashChart.setYAxis(y);
	flashChart.setXAxis(x);

	Text yLegend = new Text("count", "font-size:14px; color:#736AFF");
	flashChart.setYLegend(yLegend);
	
	Text title = new Text(leafName + " " + showyear + "年" + showmonth + "月统计   共计：" + totalWorkflow);
	flashChart.setTitle(title);

	for(int i = 1 ; i<=al.size() ; i++){
		json.put("dayNum"+i, al.get(i-1));
	}
	json.put("leafName",leafName);
	json.put("total",totalWorkflow);
	out.print(json);
%>
