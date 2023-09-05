<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.dept.DeptMgr" %>
<%@ page import="com.redmoon.oa.dept.DeptUserDb" %>
<%@ page import="com.redmoon.oa.flow.MyActionDb" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="jofc2.OFCException" %>
<%@ page import="jofc2.model.Chart" %>
<%@ page import="jofc2.model.Text" %>
<%@ page import="jofc2.model.axis.XAxis" %>
<%@ page import="jofc2.model.axis.YAxis" %>
<%@ page import="jofc2.model.elements.BarChart" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.*" %>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
		return;
	}

    String deptCode = ParamUtil.get(request, "typeCode");
    if (deptCode.equals("")) {
        if (!privilege.isUserPrivValid(request, "admin")) {
			return;
		}
    }
    String leafName = "全部";
    List<String> name = new ArrayList<>();
    List<Double> datas = new ArrayList<>();
    JSONObject json = new JSONObject();
    if (!deptCode.equals("")) {
        DeptDb leaf = null;
        DeptMgr dir = new DeptMgr();
        leaf = dir.getDeptDb(deptCode);
        leafName = leaf.getName();
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
    double max = 200; // Y轴最大值
    XAxis x = new XAxis(); // X轴

    Calendar showCal = Calendar.getInstance();
    MyActionDb mad = new MyActionDb();
    Vector actionV;
    String sql;
    int i = 1;

    showCal.set(showyear, showmonth - 1, i);

    String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
    Calendar nextCal = Calendar.getInstance();
    nextCal.set(showyear, showmonth - 1, days + 1);
    String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");

    DeptMgr dm = new DeptMgr();
    DeptUserDb du = new DeptUserDb();

    DeptDb deptDb = new DeptDb();
    deptDb = deptDb.getDeptDb(deptCode);
    Vector dv = new Vector();
    deptDb.getAllChild(dv, deptDb);
    String depts = StrUtil.sqlstr(deptCode);
    Iterator ird = dv.iterator();
    while (ird.hasNext()) {
        deptDb = (DeptDb) ird.next();
        depts += "," + StrUtil.sqlstr(deptDb.getCode());
    }

    DeptUserDb jd = new DeptUserDb();
    UserDb ud = new UserDb();

    String sql_user = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ")";
    //System.out.println("depts:"+depts);
    int curpage = ParamUtil.getInt(request, "CPages", 1);
    int pagesize = ParamUtil.getInt(request, "pageSize", 40);

    ListResult lr = jd.listResult(sql_user, curpage, pagesize);
    Iterator iterator = lr.getResult().iterator();
    long total = lr.getTotal();
    Paginator paginator = new Paginator(request, total, pagesize);
    // 设置当前页数和总页数
    int totalpages = paginator.getTotalPages();
    if (totalpages == 0) {
        curpage = 1;
        totalpages = 1;
    }

    String user_name = "", realName = "";

	double sum_perf = 0.0;
    JdbcTemplate jt = new JdbcTemplate();
    jt.setAutoClose(false);
    Vector v = lr.getResult();
    try {
		for (Object o : v) {
			DeptUserDb pu = (DeptUserDb) o;
			if (!pu.getUserName().equals("")) {
				ud = ud.getUserDb(pu.getUserName());
				user_name = ud.getName();
				realName = ud.getRealName();
				name.add(realName);
			}

			sql = "select sum(performance) from flow_my_action where (user_name=" + StrUtil.sqlstr(user_name) + " or proxy=" + StrUtil.sqlstr(user_name) + ") and is_checked=1 and checker=" + StrUtil.sqlstr(user_name) + " and receive_date>=" + bdate + " and receive_date<=" + edate + "";
			ResultIterator ri = jt.executeQuery(sql);
			double perf = 0.0;
			if (ri.hasNext()) {
				ResultRecord rr = ri.next();
				perf = rr.getDouble(1);
			}
			sum_perf += perf;

			//actionV = mad.list(sql);
			totalWorkflow += perf;
			datas.add(perf);
			if (max < sum_perf) {
				max = sum_perf;
			}

			BarChart.Bar bar = new BarChart.Bar(perf, realName);       //条标题，显示在x轴上
			bar.setColour("0xff0000"); //颜色
			bar.setTooltip("#val# 分");            //鼠标移动上去后的提示

			chartFlow.addBars(bar);
			x.addLabels("" + realName); //x轴的文字
		}
	} finally {
    	jt.close();
	}

    // chartFlow.setAlpha(0.8f);
    chartFlow.setColour("0xff0000");

    Chart flashChart = new Chart();
    flashChart.addElements(chartFlow);

    YAxis y = new YAxis(); //y轴

    if (max < 10) {
		max = 10;
	}
    y.setMax(max); //y轴最大值
    y.setSteps(max / 10 * 1.0); //步进
    flashChart.setYAxis(y);
    flashChart.setXAxis(x);

    Text yLegend = new Text("count", "font-size:14px; color:#736AFF");
    flashChart.setYLegend(yLegend);

    Text title = new Text(leafName + " " + showyear + "年" + showmonth + "月统计   共计：" + sum_perf);
    flashChart.setTitle(title);

    json.put("name", name);
    json.put("datas", datas);
    json.put("total", totalWorkflow);
    json.put("leafName", leafName);
    out.print(json);
%>
