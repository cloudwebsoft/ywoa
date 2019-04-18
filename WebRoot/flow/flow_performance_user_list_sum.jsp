<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String myname = ParamUtil.get(request, "userName");
if(myname.equals("")){
	myname = privilege.getUser(request);
}
if (!myname.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, myname))) {
		out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>流程绩效列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<script type="text/javascript" src="../inc/common.js"></script>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<%@ include file="flow_performance_list_menu_top.jsp"%>
<%
	String currentMenu = "menu2";
%>
<script>
o("<%=currentMenu%>").className="current";
</script>
<% 
	Calendar cal = Calendar.getInstance();
	Calendar cal2 = Calendar.getInstance();
	String rename = ParamUtil.get(request,"rename");
	int cury = cal.get(Calendar.YEAR);
	int y = ParamUtil.getInt(request, "showyear", cury);
%>
<table id="searchTable" border="0" align="center">
  <tr>
    <td align="center"> 
      <form name="formSearch" action="flow_performance_user_list_sum.jsp" method="get">
        &nbsp;&nbsp;年度
        <select id="showyear" name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='flow_performance_user_list_sum.jsp?deptCode=<%=deptCode%>&showyear=' + y;">
        <%for (int y2=cury-60; y2<=cury; y2++) {%>
        <option value="<%=y2%>" <%=(y2==y)?"selected":""%>><%=y2%></option>
        <%}%>
      </select>
	  姓名
        <input name="rename" value="<%=rename%>" style="width:100px;">
		<input name="op" value="search" type="hidden">
		<input name="deptCode" value="<%=deptCode%>" type="hidden">
        <input class="tSearch" type="submit" value="查询" />
        &nbsp;&nbsp;
	  </form>
    </td>
  </tr>
</table>
<table width="583" border="0" cellpadding="0" cellspacing="0" id="grid">
  <thead>
    <tr>
      <th width="58">姓名</th>
	  <th width="100">部门</th>
      <th width="100">1月</th>
      <th width="100">2月</th>
      <th width="100">3月</th>
      <th width="100">4月</th>
      <th width="100">5月</th>
      <th width="100">6月</th>
      <th width="100">7月</th>
      <th width="100">8月</th>
      <th width="100">9月</th>
      <th width="100">10月</th>
      <th width="100">11月</th>
      <th width="100">12月</th>
      <th width="100">合计</th>
    </tr>
  </thead>
  <tr class="highlight">
    <%
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
	if (op.equals("search")) {
		sql_user += " and u.realName like '%" + rename+"%'";
	}
	sql_user +="order by du.DEPT_CODE asc, du.orders asc";
	
	int curpage = ParamUtil.getInt(request, "CPages", 1);
	int pagesize = ParamUtil.getInt(request, "pageSize", 20);
	
	ListResult lr = jd.listResult(sql_user,curpage,pagesize);
	Iterator iterator = lr.getResult().iterator();
	int total = lr.getTotal();	
	Paginator paginator = new Paginator(request, total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
	
	String user_name ,realName;
   
	JdbcTemplate jt = new JdbcTemplate();
   
   	Vector v = lr.getResult();
	StringBuffer sb = new StringBuffer();
	Iterator ir = v.iterator();
	while (ir.hasNext()) {
		DeptUserDb pu = (DeptUserDb)ir.next();
		if (!pu.getUserName().equals(""))
			ud = ud.getUserDb(pu.getUserName());
            user_name  = ud.getName();
            realName = ud.getRealName(); 
	 %>
     <td align="center"><%=realName%></td>
	 <%
	   sb.append(realName);
	   sb.append(",");
	  %>
	 <td align="center">
	 <%	
	    dd = deptDb.getDeptDb(pu.getDeptCode());
		out.print(dd.getName()); 
     %>
     </td>
	 <%
		sb.append(dd.getName());
		sb.append(",");

		String sql = "select sum(performance) from flow_my_action where performance<>0 and (user_name=" + StrUtil.sqlstr(user_name) + " or proxy=" + StrUtil.sqlstr(user_name) + ") and is_checked=1 and checker=? and receive_date>=? and receive_date<=?";
		String sql2 = "select count(*) from flow_my_action where performance<>0 and (user_name=" + StrUtil.sqlstr(user_name) + " or proxy=" + StrUtil.sqlstr(user_name) + ") and is_checked=1 and checker=? and receive_date>=? and receive_date<=?";
		double yPerf = 0.0;
		double yCount = 0;
		double yAverage = 0;
		for (int i=0; i<=11; i++) {
		int d = DateUtil.getDayCount(y, i);
		cal.set(y,i,1,0,0,0);
		cal2.set(y,i,d,23,59,59);
		// System.out.println(getClass() + " cal=" + DateUtil.format(cal, "yyyy-MM-dd HH:mm:ss"));
		// System.out.println(getClass() + " cal=" + DateUtil.format(cal2, "yyyy-MM-dd HH:mm:ss"));
		ResultIterator ri = jt.executeQuery(sql, new Object[]{user_name, cal.getTime(), cal2.getTime()});
		double perf = 0.0;
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			perf = rr.getDouble(1);
		}
		yPerf += perf;
		int count = 0;
		ri = jt.executeQuery(sql2, new Object[]{user_name, cal.getTime(), cal2.getTime()});
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			count = rr.getInt(1);
		}
		yCount += count;
		double average = 0;
		if (count>0)
			average = perf/count;
	%>
    <td><%=count%></td>
	<%
	   sb.append(count);
	   sb.append(",");
	%>
  <%
}
if (yCount>0)
	yAverage = yPerf/yCount;
%>
<td><%=NumberUtil.round(yCount, 2)%></td>
	<%
	   sb.append(yCount);
       sb.append("|");
	%>
</tr>
<%}%>

<form name="excelForm" action="flow_performance_list_excel.jsp" method="post">
	<%
    String param = sb.toString();
    %>
   <input type="hidden" value="<%=param%>" name="param" >
</form>
</table>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_performance_user_sum.jsp?deptCode=<%=deptCode%>&pageSize=" + flex.attr('p').rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_performance_user_sum.jsp?deptCode=<%=deptCode%>&CPages=" + newp + "&pageSize=" + flex.attr('p').rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_performance_user_sum.jsp?deptCode=<%=deptCode%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}
function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
	    {name: '导出', bclass: 'export', onpress : action},
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	sortname: "iso",
	sortorder: "asc",
	*/
	url: false,
	usepager: true,
	checkbox : false,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	//title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	/*
	onRowDblclick: rowDbClick,
	onColSwitch: colSwitch,
	onColResize: colResize,
	onToggleCol: toggleCol,
	*/
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);
function action(com, grid) {
	if(com=="导出")	{
		doExpeort();
	}
}
function doExpeort() {
    excelForm.submit();
}
</script>
</html>
