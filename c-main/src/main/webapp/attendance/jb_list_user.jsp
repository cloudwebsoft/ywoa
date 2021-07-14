<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ include file="../inc/nocache.jsp"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
// 防XSS
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (userName.equals("")) {
	userName = privilege.getUser(request);
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "fl.mydate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String op = "search"; // ParamUtil.get(request, "op");

Calendar cal = Calendar.getInstance();
int curmonth = cal.get(Calendar.MONTH);
int curyear = cal.get(Calendar.YEAR);

int showyear,showmonth;
String strshowyear = request.getParameter("showyear");
if (strshowyear!=null && strshowyear.equals(""))
	strshowyear = null;
String strshowmonth = request.getParameter("showmonth");
if (strshowmonth!=null && strshowmonth.equals(""))
	strshowmonth = null;
if (strshowyear!=null)
	showyear = Integer.parseInt(strshowyear);
else
	showyear = cal.get(Calendar.YEAR);
if (strshowmonth!=null)
	showmonth = Integer.parseInt(strshowmonth);
else
	showmonth = 1;
	
int dayCount = DateUtil.getDayCount(showyear, showmonth-1);

String beginDate = showyear + "-" + showmonth + "-1";
String endDate = showyear + "-" + showmonth + "-" + dayCount;

// 查看全年
if (strshowmonth==null) {
	beginDate = showyear + "-1-1";
	endDate = showyear + "-12-31";
}

java.util.Date bDate = DateUtil.parse(beginDate, "yyyy-MM-dd");
java.util.Date eDate = DateUtil.parse(endDate, "yyyy-MM-dd");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>用户加班记录</title>
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../inc/common.js"></script>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";

	window.location.href = "jb_list_user.jsp?orderBy=" + orderBy + "&sort=" + sort + "&op=<%=op%>&userName=<%=StrUtil.UrlEncode(userName)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>&showyear=<%=strshowyear!=null?strshowyear:""%>&showmonth=<%=strshowmonth!=null?strshowmonth:""%>";
}
</script>
</head>
<body>
<%@ include file="../kaoqin_inc_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<div class="spacerH"></div>
<form id="formLeave" name="formLeave" action="jb_list_user.jsp" method="post">
  <table width="98%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td height="24" align="center">
	  <%if (strshowmonth==null) {%>
	  <a href="#" style="color:red" onclick="window.location.href='jb_list_user.jsp?op=search&userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + $('showyear').value;">全年</a>
	  <%}else{%>
	  <a href="#" onclick="window.location.href='jb_list_user.jsp?op=search&userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + $('showyear').value;">全年</a>
	  <%}%>
	  <select id="showyear" name="showyear" onchange="var y = this.options[this.selectedIndex].value; window.location.href='?op=search&userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + y;">
        <%for (int y=curyear-60; y<=curyear; y++) {%>
        <option value="<%=y%>"><%=y%></option>
        <%}%>
      </select>
      <script>
		$("showyear").value = "<%=showyear%>";
	  </script>
	  <input name="userName" value="<%=userName%>" type="hidden" />
<%
for (int i=1; i<=12; i++) {
	if (showmonth==i && strshowmonth!=null)
		out.print("<a href='jb_list_user.jsp?userName=" + StrUtil.UrlEncode(userName) + "&op=search&showyear="+showyear+"&showmonth="+i+"'><font color=red>"+i+"月</font></a>&nbsp;");
	else
		out.print("<a href='jb_list_user.jsp?userName=" + StrUtil.UrlEncode(userName) + "&op=search&showyear="+showyear+"&showmonth="+i+"'>"+i+"月</a>&nbsp;");
}
%></td>
    </tr>
  </table>
</form>
<%
// STATUS_FINISHED说明流程已完毕
String sql = "select f.flowId from form_table_jbsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='jbsq' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
sql += " and f.applier=" + StrUtil.sqlstr(userName);
if (op.equals("search")) {
	if (!beginDate.equals("") && !endDate.equals("")) {
		sql += " and ((f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.jssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.jssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + ") and (f.kssj<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.jssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + "))";
	}
	else if (!beginDate.equals("")) {
		sql += " and f.kssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
	}
	else if (beginDate.equals("") && !endDate.equals("")) {
		sql += " and f.kssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
	}
}
int result = ParamUtil.getInt(request, "result", -1);
if (result!=-1)
	sql += " and f.result='" + result + "'";
sql += " order by " + orderBy + " " + sort;

// out.print(sql);

FormDb fd = new FormDb();
fd = fd.getFormDb("jbsqd");

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = 10;
int curpage = Integer.parseInt(strcurpage);

WorkflowDb wf = new WorkflowDb();
ListResult lr = wf.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
  <tr>
    <td width="38%" height="30" align="left"><input class="btn" value="加班申请" type="button" onclick="window.location.href='../flow_initiate1.jsp?op=jbsq'" /></td>
    <td width="62%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="98%" align="center" class="tabStyle_1 percent98">
 <thead>
    <tr>
      <td width="8%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.applier')">申请人
      <%if (orderBy.equals("f.applier")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="10%" class="tabStyle_1_title"style="cursor:pointer" onClick="doSort('f.dept')">部门
      <%if (orderBy.equals("f.dept")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");

		}%>      </td>
      <td width="8%" class="tabStyle_1_title"style="cursor:pointer" onclick="doSort('f.jblb')">加班类别
		<%if (orderBy.equals("f.jblb")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");

		}%>      
      </td>
      <td width="5%" class="tabStyle_1_title"style="cursor:pointer" onClick="doSort('f.day_count')">小时</td>
      <td width="13%" class="tabStyle_1_title"style="cursor:pointer" onClick="doSort('f.kssj')">开始时间
        <%if (orderBy.equals("f.kssj")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="13%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.jssj')">结束时间
        <%if (orderBy.equals("f.jssj")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="10%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('fl.mydate')">申请时间
      <%if (orderBy.equals("fl.mydate")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="7%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.result')">审批结果
      <%if (orderBy.equals("f.result")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>      </td>
      <td width="9%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('fl.status')">流程状态
      <%if (orderBy.equals("fl.status")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>      </td>
      <td width="17%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.checker')">审批者
      <%if (orderBy.equals("f.checker")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>      </td>
    </tr>
    </thead>
    <%
Leaf ft = new Leaf();	
UserMgr um = new UserMgr();
FormDAO fdao = new FormDAO();
DeptMgr dm = new DeptMgr();

double ps = 0, xxr = 0, jjr = 0; // 平时、休息日、节假日
java.util.Date qjbDate = null; // 本月加班于本月的实际开始日期
java.util.Date qjeDate = null; // 本月加班于本月的实际结束日期
com.redmoon.oa.oacalendar.OACalendarDb oacal = new com.redmoon.oa.oacalendar.OACalendarDb();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next();
	fdao = fdao.getFormDAO(wfd.getId(), fd);
	String strBeginDate = fdao.getFieldValue("kssj");
	String strEndDate = fdao.getFieldValue("jssj");
	String xjrq = fdao.getFieldValue("xjrq");
	DeptDb dd = dm.getDeptDb(fdao.getFieldValue("dept"));
	String deptName = "";
	if (dd!=null)
		deptName = dd.getName();
	String checker = fdao.getFieldValue("checker");
	if (!checker.equals(""))
		checker = um.getUserDb(checker).getRealName();
	
	// 同意加班
	double jbDays = 0;
	if (fdao.getFieldValue("result").equals("1")) {
		jbDays = StrUtil.toDouble(fdao.getFieldValue("day_count"));
	}
	%>
    <tr class="highlight">
      <td align="center"><%=um.getUserDb(fdao.getFieldValue("applier")).getRealName()%></td>
      <td align="center"><%=deptName%></td>
      <td align="center">
	  <%
	  String jqlb = fdao.getFieldValue("jblb");
	  if (jqlb.equals("平时"))
	  	ps += jbDays;
	  else if (jqlb.equals("休息日"))
	  	xxr += jbDays;
	  else
	  	jjr += jbDays;
	  %>
	  <%=jqlb%>
	  </td>
      <td align="center"><%=fdao.getFieldValue("day_count")%></td>
      <td align="center"><%=strBeginDate%></td>
      <td align="center"><%=strEndDate%></td>
      <td align="center"><%=DateUtil.format(wfd.getMydate(), "yy-MM-dd HH:mm")%> </td>
      <td align="center"><%=fdao.getFieldValue("result").equals("1")?"通过":"不通过"%></td>
      <td align="center"><%=wfd.getStatusDesc()%></td>
      <td align="center"><%=checker%></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="40%" align="left">
    <%
	sql = "select sum(day_count) from form_table_jbsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='jbsq' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
	sql += " and f.kssj>=" + SQLFilter.getDateStr(showyear + "-01-01", "yyyy-MM-dd") + " and f.jssj<" + SQLFilter.getDateStr((showyear+1) + "-01-01", "yyyy-MM-dd");;
	sql += " and f.applier=" + StrUtil.sqlstr(userName);

	double allCount = 0.0;
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		allCount += rr.getDouble(1);
	}
	%>
    平时：<%=ps%>&nbsp;&nbsp;休息日：<%=xxr%>&nbsp;&nbsp;节假日：<%=jjr%>&nbsp;&nbsp;全年：<%=allCount%></td>
    <td width="60%" align="right"><%
	String querystr = "op="+op + "&orderBy=" + orderBy + "&sort=" + sort + "&op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&beginDate=" + beginDate + "&endDate=" + endDate;
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table>
</body>
</html>