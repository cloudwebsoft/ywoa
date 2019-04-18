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
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="net.sf.json.JSONObject"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<%
	JSONObject json = new JSONObject();
	if (!privilege.isUserLogin(request)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	// 翻月
	int showyear;
	Calendar cal = Calendar.getInstance();

	String strshowyear = request.getParameter("showyear");
	
	if (strshowyear != null)
		showyear = Integer.parseInt(strshowyear);
	else
		showyear = cal.get(Calendar.YEAR);

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
	
	int totalFound = 0;
	int totalDistr = 0;
	int totalContacted = 0;
	ArrayList<Integer> list1 = new ArrayList<Integer>();
	ArrayList<Integer> list2 = new ArrayList<Integer>();
	ArrayList<Integer> list3 = new ArrayList<Integer>();
	LineChart chartFlow = new LineChart(LineChart.Style.NORMAL);
	LineChart chartWorkPlan = new LineChart(LineChart.Style.NORMAL);
	LineChart chartDoc = new LineChart(LineChart.Style.NORMAL);

	double max = 0; // Y轴最大值
	XAxis x = new XAxis(); // X轴
	List<LineChart.Dot> listFlow = new ArrayList<LineChart.Dot>();
	List<LineChart.Dot> listWorkPlan = new ArrayList<LineChart.Dot>();
	List<LineChart.Dot> listDoc = new ArrayList<LineChart.Dot>();

	JdbcTemplate jt = new JdbcTemplate();

	Calendar showCal = Calendar.getInstance();
	MyActionDb mad = new MyActionDb();
	Vector actionV;
	for (int i = 0; i <= 11; i++) {
		showCal.set(showyear, i, 1);

		String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		
		Calendar nextCal = Calendar.getInstance();
		if (i<=10)
			nextCal.set(showyear, i+1, 1);
		else
			nextCal.set(showyear+1, 0, 1);
		String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
		
		// 发现的客户
		String sql = "select count(*) from form_table_sales_customer where founder=" + StrUtil.sqlstr(userName) + " and find_date>=" + bdate + " and find_date<" + edate;
		ResultIterator ri = jt.executeQuery(sql);
		int cc = 0;
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			// System.out.println(getClass() + " " + rr.getInt(1) + " " + sql);
			cc = rr.getInt(1);
			totalFound += rr.getInt(1);
			list1.add(cc);
			if (max<rr.getInt(1))
				max = rr.getInt(1);			
			LineChart.Dot d = new LineChart.Dot(cc, "0x00ff00", 3, 1);
			listFlow.add(d);
		}		

		// 分配的客户
		cc = 0;
		sql = "select count(distinct user_name) from oa_sales_customer_distr where user_name=" + StrUtil.sqlstr(userName) + " and create_date>=" + bdate + " and create_date<" + edate;
		ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			cc = rr.getInt(1);
			totalDistr += rr.getInt(1);
			list2.add(cc);
			if (max<rr.getInt(1))
				max = rr.getInt(1);			
			LineChart.Dot d = new LineChart.Dot(cc, "0x00ff00", 3, 1);
			listWorkPlan.add(d);
		}
				
		// 联系的客户
		cc = 0;
		sql = "select count(*) from form_table_day_work_plan d, form_table_day_lxr l where d.id=l.cws_id and l.is_visited='是' and d.cur_user=" + StrUtil.sqlstr(userName) + " and d.mydate>=" + bdate + " and d.mydate<" + edate;
		ri = jt.executeQuery(sql);
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			cc = rr.getInt(1);
			totalContacted += rr.getInt(1);
			list3.add(cc);
			if (max<rr.getInt(1))
				max = rr.getInt(1);			
			LineChart.Dot d = new LineChart.Dot(cc, "0x0000ff", 3, 1);
			listDoc.add(d);
		}


		x.addLabels("" + (i+1)); //x轴的文字
	}
	
	// chartFlow.setAlpha(0.8f);
	chartFlow.addDots(listFlow);
	chartFlow.setColour("0xff0000");
	chartFlow.setHaloSize(3);
	chartFlow.setWidth(2);
	chartFlow.setDotSize(10);

	chartWorkPlan.setAlpha(0.8f);
	chartWorkPlan.addDots(listWorkPlan);
	chartWorkPlan.setColour("0x00ff00");
	chartWorkPlan.setHaloSize(3);	
	
	chartDoc.setAlpha(0.8f);
	chartDoc.addDots(listDoc);
	chartDoc.setColour("0x0000ff");
	chartDoc.setHaloSize(3);	
		
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

	Text yLegend = new Text("count", "font-size:14px; color:#736AFF");
	flashChart.setYLegend(yLegend);
	
	Text title = new Text(showyear + "年   发现的客户数:" + totalFound + " 分配的客户数:" + totalDistr + " 联系的客户数:" + totalContacted, "font-size:15px; color:#736AFF");
	flashChart.setTitle(title);
	
	String printString = "";
	try {
		printString = flashChart.toString();
	} catch (OFCException e) {
		printString = flashChart.toDebugString();
	}
	json.put("totalFound",totalFound);
	json.put("totalDistr",totalDistr);
	json.put("totalContacted",totalContacted);
	for(int i = 1 ; i<=list1.size(); i++){
		json.put("foundC"+i , list1.get(i-1));
	}
	for(int i = 1 ; i<=list2.size(); i++){
		json.put("allocationC"+i , list2.get(i-1));
	}
	for(int i = 1 ; i<=list3.size(); i++){
		json.put("contactC"+i , list3.get(i-1));
	}
	out.print(json);
	//out.print(printString);
%>
