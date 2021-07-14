<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.workplan.*"%>
<%@ page import="com.redmoon.oa.task.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	if (!privilege.isUserLogin(request)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	// 翻月
	int showyear, showmonth;
	Calendar cal = Calendar.getInstance();

	String strshowyear = request.getParameter("showyear");
	String strshowmonth = request.getParameter("showmonth");
	
	if (strshowyear != null)
		showyear = Integer.parseInt(strshowyear);
	else
		showyear = cal.get(Calendar.YEAR);
	if (strshowmonth != null)
		showmonth = Integer.parseInt(strshowmonth);
	else
		showmonth = cal.get(Calendar.MONTH) + 1;

	String userName = ParamUtil.get(request, "userName");
	if (userName.equals("")) {
		userName = privilege.getUser(request);
	}

	if (!userName.equals(privilege.getUser(request))) {
		if (!(privilege.canAdminUser(request, userName))) {
			out.print(SkinUtil.LoadString(request, "pvg_invalid"));
			return;
		}
	}
	
	int totalWorkflow = 0;
	int totalWorkplan = 0;
	int totalDoc = 0;
	int totalTask = 0;

	int days = DateUtil.getDayCount(showyear, showmonth - 1);

	BarChart chartFlow = new BarChart(BarChart.Style.GLASS);
	BarChart chartWorkPlan = new BarChart(BarChart.Style.GLASS);
	BarChart chartDoc = new BarChart(BarChart.Style.GLASS);
	BarChart chartTask = new BarChart(BarChart.Style.GLASS);

	double max = 0; // Y轴最大值
	XAxis x = new XAxis(); // X轴
	List<LineChart.Dot> listFlow = new ArrayList<LineChart.Dot>();
	List<LineChart.Dot> listWorkPlan = new ArrayList<LineChart.Dot>();
	List<LineChart.Dot> listDoc = new ArrayList<LineChart.Dot>();
	List<LineChart.Dot> listTask = new ArrayList<LineChart.Dot>();

	Calendar showCal = Calendar.getInstance();
	MyActionDb mad = new MyActionDb();
	Vector actionV;
	for (int i = 1; i <= days; i++) {
		showCal.set(showyear, showmonth - 1, i);

		String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		Calendar nextCal = Calendar.getInstance();
		nextCal.set(showyear, showmonth - 1, i + 1);
		String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		
		// 已办流程
		String sql = "select id from flow_my_action where (user_name=" + StrUtil.sqlstr(userName) + " or proxy=" + StrUtil.sqlstr(userName) + ") and is_checked=1 and check_date>=" + bdate + " and check_date<" + edate;
		actionV = mad.list(sql);
		totalWorkflow += actionV.size();
		if (max<actionV.size())
			max = actionV.size();

        BarChart.Bar bar = new BarChart.Bar(actionV.size(),i+"月");       //条标题，显示在x轴上
        bar.setColour("0xff0000"); //颜色
        bar.setTooltip("#val# 个");            //鼠标移动上去后的提示         
        chartFlow.addBars(bar); 
	        
		// 工作计划
		sql = "select id from work_plan_annex where user_name=" + StrUtil.sqlstr(userName) + " and add_date>=" + bdate + " and add_date<" + edate;
		WorkPlanAnnexDb wad = new WorkPlanAnnexDb();
		Vector wplanV = wad.list(sql);		
		totalWorkplan += wplanV.size();		
		if (max<wplanV.size())
			max = wplanV.size();
        bar = new BarChart.Bar(wplanV.size(),i+"月");       //条标题，显示在x轴上
        bar.setColour("0x00ff00"); //颜色
        bar.setTooltip("#val# 个");            //鼠标移动上去后的提示    
        chartWorkPlan.addBars(bar); 
				
		// 文件柜
		sql = "select id from document where nick=" + StrUtil.sqlstr(userName) + " and createDate>=" + bdate + " and createDate<" + edate;
		com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();
		Vector docV = doc.list(sql, 500);
		totalDoc += docV.size();
		if (max<docV.size())
			max = docV.size();
        bar = new BarChart.Bar(docV.size(),i+"月");       //条标题，显示在x轴上
        bar.setColour("0x0000ff"); //颜色
        bar.setTooltip("#val# 个");            //鼠标移动上去后的提示    
        chartDoc.addBars(bar);
		
		/*
		// 任务
		sql = "select id from task where person=" + StrUtil.sqlstr(userName) + " and mydate>=" + bdate + " and mydate<=" + edate;
		TaskDb td = new TaskDb();
		Vector taskV = td.list(sql);		
		totalTask += taskV.size();
		if (max<taskV.size())
			max = taskV.size();
        bar = new BarChart.Bar(taskV.size(),i+"月");       //条标题，显示在x轴上
        bar.setColour("0xffff00"); //颜色
        bar.setTooltip("#val# 个");            //鼠标移动上去后的提示    
        chartTask.addBars(bar);
		*/

		x.addLabels("" + i); //x轴的文字
	}
	
	// chartFlow.setAlpha(0.8f);
	chartFlow.setColour("0xff0000");

	chartWorkPlan.setAlpha(0.8f);
	chartWorkPlan.setColour("0x00ff00");
	
	chartDoc.setAlpha(0.8f);
	chartDoc.setColour("0x0000ff");
	
	chartTask.setAlpha(0.8f);
	chartTask.setColour("0xffff00");
		
	Chart flashChart = new Chart();
	flashChart.addElements(chartFlow);
	flashChart.addElements(chartWorkPlan);
	flashChart.addElements(chartDoc);
	flashChart.addElements(chartTask);
			
	YAxis y = new YAxis(); //y轴
	
	if (max<10)
		max = 10;
	y.setMax(max); //y轴最大值
	y.setSteps(max / 10 * 1.0); //步进
	flashChart.setYAxis(y);
	flashChart.setXAxis(x);

	Text yLegend = new Text("count", "font-size:14px; color:#736AFF");
	flashChart.setYLegend(yLegend);
	
	Text title = new Text(showyear + "年" + showmonth + "月统计   流程:" + totalWorkflow + " 计划:" + totalWorkplan + " 文档:" + totalDoc + " 任务:" + totalTask, "font-size:15px; color:#736AFF");
	flashChart.setTitle(title);
	
	
	out.print(flashChart.toString());
%>
