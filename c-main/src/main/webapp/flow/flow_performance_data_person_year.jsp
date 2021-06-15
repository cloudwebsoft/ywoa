<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="jofc2.model.axis.*"%>
<%@ page import="jofc2.model.elements.*"%>
<%@ page import="jofc2.model.*"%>
<%@page import="jofc2.OFCException"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" /><%
	if (!privilege.isUserPrivValid(request, "admin.flow"))
		return;

	String deptCode = ParamUtil.get(request, "typeCode");
	if (deptCode.equals("")) {
		if (!privilege.isUserPrivValid(request, "admin"))
			return;
	}
	
	String leafName = "全部";
	if (!deptCode.equals("")) {
		DeptDb leaf = null;
		DeptMgr dir = new DeptMgr();
		leaf = dir.getDeptDb(deptCode);
		leafName = leaf.getName();
	}
	JSONArray datas = new JSONArray();
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

	double max = 200; // Y轴最大值
	XAxis x = new XAxis(); // X轴
	List<LineChart.Dot> listFlow = new ArrayList<LineChart.Dot>();

	Calendar showCal = Calendar.getInstance();
	MyActionDb mad = new MyActionDb();
	Vector actionV;
	String sql;
	double sum_perf = 0.0;
	double perf = 0.0;
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
		//if (typeCode.equals(""))	
			//sql = "select id from flow where begin_date>=" + bdate + " and begin_date<" + edate + " order by begin_date desc";
		//else
			//sql = "select id from flow where type_code=" + StrUtil.sqlstr(typeCode) + " and begin_date>=" + bdate + " and begin_date<" + edate + " order by begin_date desc";		
				
		DeptMgr dm = new DeptMgr();
		DeptUserDb du = new DeptUserDb();
		DeptDb deptDb = new DeptDb();
		deptDb = deptDb.getDeptDb(deptCode);
		Vector dv = new Vector();
		deptDb.getAllChild(dv, deptDb);
		String depts = StrUtil.sqlstr(deptCode);
		Iterator ird = dv.iterator();
		while (ird.hasNext()) {
			deptDb = (DeptDb)ird.next();
			depts += "," + StrUtil.sqlstr(deptDb.getCode());
		}
		
		DeptUserDb jd = new DeptUserDb();
		UserDb ud = new UserDb();
		
		String sql_user = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ")";	
		int curpage = ParamUtil.getInt(request, "CPages", 1);
		int pagesize = ParamUtil.getInt(request, "pageSize", 40);
		
		ListResult lr = jd.listResult(sql_user,curpage,pagesize);
		Iterator iterator = lr.getResult().iterator();
		long total = lr.getTotal();
		Paginator paginator = new Paginator(request, total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
		
		String user_name="" ,realName="";
	   
		JdbcTemplate jt = new JdbcTemplate();
	    sum_perf = 0.0;
		Vector v = lr.getResult();
		StringBuffer sb = new StringBuffer();
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			DeptUserDb pu = (DeptUserDb)ir.next();
			if (!pu.getUserName().equals("")){
				ud = ud.getUserDb(pu.getUserName());
				user_name  = ud.getName();
				realName = ud.getRealName(); 
			}
	
		    sql = "select sum(performance) from flow_my_action where (user_name=" + StrUtil.sqlstr(user_name) + " or proxy=" + StrUtil.sqlstr(user_name) + ") and is_checked=1 and checker="+StrUtil.sqlstr(user_name)+" and receive_date>="+bdate+" and receive_date<="+edate+"";		

			//System.out.println("bdate:"+bdate);
			//System.out.println("edate:"+edate);
			ResultIterator ri = jt.executeQuery(sql);
			perf = 0.0;
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				perf = rr.getDouble(1);
			}
			sum_perf += perf;
			
		}
				
		//actionV = mad.list(sql);
		totalWorkflow += sum_perf;
		datas.add(sum_perf);
		if (max<sum_perf)
			max = sum_perf;

		LineChart.Dot d = new LineChart.Dot(perf, "0xff0000"); //, 8, 0);
		listFlow.add(d);

		x.addLabels("" + realName); //x轴的文字
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
	
	Text title = new Text(leafName + " " + showyear + "年   共计：" + sum_perf);
	flashChart.setTitle(title);
	
	String printString = "";
	try {
		printString = flashChart.toString();
	} catch (OFCException e) {
		printString = flashChart.toDebugString();
	}
	
	json.put("datas",datas);
	json.put("total",totalWorkflow);
	json.put("leafName",leafName);
	out.print(json);
	//out.print(printString);
%>
