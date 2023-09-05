<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="jofc2.model.axis.*" %>
<%@ page import="jofc2.model.elements.*" %>
<%@ page import="jofc2.model.*" %>
<%@page import="jofc2.OFCException" %>
<%@page import="org.json.JSONObject" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    if (!privilege.isUserPrivValid(request, "admin.flow")) {
		return;
	}

    String deptCode = ParamUtil.get(request, "typeCode");
    if ("".equals(deptCode)) {
        if (!privilege.isUserPrivValid(request, "admin")) {
			return;
		}
    }
    String leafName = "全部";
    JSONObject json = new JSONObject();
    if (!"".equals(deptCode)) {
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
        showmonth = ParamUtil.getInt(request, "showmonth", 0);
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
	JdbcTemplate jt = new JdbcTemplate();
	jt.setAutoClose(false);
	ArrayList al = new ArrayList();
	try {
		for (int i = 1; i <= days; i++) {
			showCal.set(showyear, showmonth - 1, i);

			String bdate = SQLFilter.getDateStr(DateUtil.format(showCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");
			Calendar nextCal = Calendar.getInstance();
			nextCal.set(showyear, showmonth - 1, i + 1);
			String edate = SQLFilter.getDateStr(DateUtil.format(nextCal, "yyyy-MM-dd 00:00:00"), "yyyy-MM-dd HH:mm:ss");

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

			DeptUserDb dud = new DeptUserDb();
			UserDb ud = new UserDb();

			String sql_user = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ")";
			int curpage = ParamUtil.getInt(request, "CPages", 1);
			int pagesize = ParamUtil.getInt(request, "pageSize", 20);

			ListResult lr = dud.listResult(sql_user, curpage, pagesize);
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
			Vector v = lr.getResult();
			Iterator ir = v.iterator();
			while (ir.hasNext()) {
				DeptUserDb pu = (DeptUserDb) ir.next();
				if (!"".equals(pu.getUserName())) {
					ud = ud.getUserDb(pu.getUserName());
					user_name = ud.getName();
					realName = ud.getRealName();
				}

				sql = "select sum(performance) from flow_my_action where (user_name=" + StrUtil.sqlstr(user_name) + " or proxy=" + StrUtil.sqlstr(user_name) + ") and is_checked=1 and checker=" + StrUtil.sqlstr(user_name) + " and receive_date>=" + bdate + " and receive_date<=" + edate + "";
				ResultIterator ri = jt.executeQuery(sql);
				double perf = 0.0;
				if (ri.hasNext()) {
					ResultRecord rr = ri.next();
					perf = rr.getDouble(1);
				}
				sum_perf += perf;
			}

			//actionV = mad.list(sql);
			totalWorkflow += sum_perf;
			al.add(sum_perf);
			if (max < sum_perf) {
				max = sum_perf;
			}

			BarChart.Bar bar = new BarChart.Bar(sum_perf, i + "月");       //条标题，显示在x轴上
			bar.setColour("0xff0000"); //颜色
			bar.setTooltip("#val# 分");            //鼠标移动上去后的提示

			chartFlow.addBars(bar);

			x.addLabels("" + i); //x轴的文字
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

    Text title = new Text(leafName + " " + showyear + "年" + showmonth + "月统计   共计：" + totalWorkflow);
    flashChart.setTitle(title);

    for (int i = 1; i <= al.size(); i++) {
        json.put("dayNum" + i, al.get(i - 1));
    }
    json.put("leafName", leafName);
    json.put("total", totalWorkflow);
    out.print(json);
%>
