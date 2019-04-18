<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
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

// 检查myname用户是否存在，以防注入，因为有可能是向原始参数/cookie 值附加以下字符串：\'%20having%201=1--
UserDb user = new UserDb();
user = user.getUserDb(myname);
if (!user.isLoaded()) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ flow/flow_user_performance.jsp myname=" + myname);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>流程绩效列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>
<div class="tabs1Box">
<%@ include file="../flow_inc_menu_top.jsp"%>
</div>
<script>
o("menu4").className="current"; 
</script>
<%
JdbcTemplate jt = new JdbcTemplate();
Calendar cal = Calendar.getInstance();
Calendar cal2 = Calendar.getInstance();
int cury = cal.get(Calendar.YEAR);
int y = ParamUtil.getInt(request, "showyear", cury);
%>
<table id="searchTable" border="0" align="center">
    <tr>
      <td align="center">
        <select id="showyear" name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='flow_user_performance.jsp?showyear=' + y;">
        <%for (int y2=cury-60; y2<=cury; y2++) {%>
        <option value="<%=y2%>"><%=y2%></option>
        <%}%>
      	</select>
        <lt:Label res="res.flow.Flow" key="year" />
	  <script>
	  $(document).ready(function() {
	  o("showyear").value = "<%=y%>";
	  });
	  </script>
	  </td>
    </tr>
</table>
<table width="583" border="0" cellpadding="0" cellspacing="0" id="grid">
<thead>
  <tr>
    <th width="58"><lt:Label res="res.flow.Flow" key="month"/></th>
    <th width="209"><lt:Label res="res.flow.Flow" key="totalPerformance"/></th>
    <th width="164"><lt:Label res="res.flow.Flow" key="processTimes"/></th>
    <th width="152"><lt:Label res="res.flow.Flow" key="averageMonth"/></th>
  </tr>
</thead>
<%
String sql = "select sum(performance) from flow_my_action where (user_name=" + StrUtil.sqlstr(myname) + " or proxy=" + StrUtil.sqlstr(myname) + ") and is_checked=1 and checker=? and receive_date>=? and receive_date<=?";
String sql2 = "select count(*) from flow_my_action where (user_name=" + StrUtil.sqlstr(myname) + " or proxy=" + StrUtil.sqlstr(myname) + ") and is_checked=1 and checker=? and receive_date>=? and receive_date<=?";
double yPerf = 0.0;
double yCount = 0;
double yAverage = 0;
for (int i=0; i<=11; i++) {
	int d = DateUtil.getDayCount(y, i);
	cal.set(y,i,1,0,0,0);
	cal2.set(y,i,d,23,59,59);
	// System.out.println(getClass() + " cal=" + DateUtil.format(cal, "yyyy-MM-dd HH:mm:ss"));
	// System.out.println(getClass() + " cal=" + DateUtil.format(cal2, "yyyy-MM-dd HH:mm:ss"));
	ResultIterator ri = jt.executeQuery(sql, new Object[]{myname, cal.getTime(), cal2.getTime()});
	double perf = 0.0;
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		perf = rr.getDouble(1);
	}
	yPerf += perf;
	int count = 0;
	ri = jt.executeQuery(sql2, new Object[]{myname, cal.getTime(), cal2.getTime()});
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		count = rr.getInt(1);
	}
	yCount += count;
	double average = 0;
	if (count>0)
		average = perf/count;
	%>
	<tr class="highlight">
    <td align="center"><%=i+1%></td>
    <td><%=NumberUtil.round(perf, 2)%></td>
    <td><%=count%></td>
    <td><%=NumberUtil.round(average, 2)%></td>
  </tr>
	<%
}
if (yCount>0)
	yAverage = yPerf/yCount;
%>
	<tr class="highlight">
	  <td align="center"><strong>合计</strong></td>
	  <td><%=NumberUtil.round(yPerf, 2)%></td>
	  <td><%=NumberUtil.round(yCount, 2)%></td>
	  <td><%=NumberUtil.round(yAverage, 2)%></td>
  </tr>
</table>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
}

function changePage(newp) {
}

function rpChange(pageSize) {
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
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
	usepager: false,
	checkbox : false,
	page: 1,
	total: 13,
	useRp: true,
	rp: 20,
	
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
}
</script>
</html>