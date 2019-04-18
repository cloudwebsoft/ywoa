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
String strshowmonth = request.getParameter("showmonth");
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
<title>用户请假记录</title>
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

	window.location.href = "leave_list_user.jsp?orderBy=" + orderBy + "&sort=" + sort + "&op=<%=op%>&userName=<%=StrUtil.UrlEncode(userName)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>";
}
</script>
</head>
<body>
<%@ include file="../kaoqin_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<form id="formLeave" name="formLeave" action="leave_list_user.jsp" method="post">
  <table width="98%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td height="24" align="center">
	  <%if (strshowmonth==null) {%>
	  <a href="#" style="color:red" onclick="window.location.href='leave_list_user.jsp?op=search&userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + $('showyear').value;">全年</a>
	  <%}else{%>
	  <a href="#" onclick="window.location.href='leave_list_user.jsp?op=search&userName=<%=StrUtil.UrlEncode(userName)%>&showyear=' + $('showyear').value;">全年</a>
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
		out.print("<a href='leave_list_user.jsp?userName=" + StrUtil.UrlEncode(userName) + "&op=search&showyear="+showyear+"&showmonth="+i+"'><font color=red>"+i+"月</font></a>&nbsp;");
	else
		out.print("<a href='leave_list_user.jsp?userName=" + StrUtil.UrlEncode(userName) + "&op=search&showyear="+showyear+"&showmonth="+i+"'>"+i+"月</a>&nbsp;");
}
%></td>
    </tr>
  </table>
</form>
<%
// STATUS_FINISHED说明已经销假，流程已完毕
String sql = "select f.flowId from form_table_qjsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='qj' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
sql += " and f.applier=" + StrUtil.sqlstr(userName);
if (op.equals("search")) {
	if (!beginDate.equals("") && !endDate.equals("")) {
		// sql += " and ((f.qjkssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.qjkssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.qjjssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.qjjssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + ") and (f.qjkssj<=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.qjjssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + "))";
		sql += " and ((f.qjkssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.qjjssj<" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + ") or (f.qjkssj<" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + " and f.qjjssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd") + ") and (f.qjkssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + " and f.qjjssj>=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd") + "))";
	}
	else if (!beginDate.equals("")) {
		sql += " and f.qjkssj>=" + SQLFilter.getDateStr(beginDate, "yyyy-MM-dd");
	}
	else if (beginDate.equals("") && !endDate.equals("")) {
		sql += " and f.qjkssj<=" + SQLFilter.getDateStr(endDate, "yyyy-MM-dd");
	}
}
int result = ParamUtil.getInt(request, "result", -1);
if (result!=-1)
	sql += " and f.result='" + result + "'";
sql += " order by " + orderBy + " " + sort;

// out.print(sql);

FormDb fd = new FormDb();
fd = fd.getFormDb("qjsqd");

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
int total = lr.getTotal();
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
    <td width="38%" height="30" align="left"><input class="btn" value="我要请假" type="button" onclick="addTab('请假', '<%=request.getContextPath()%>/flow_initiate1.jsp?op=qj')" /></td>
    <td width="62%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<table width="98%" align="center" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td width="8%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.applier')">请假人
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
      <td width="9%" class="tabStyle_1_title"style="cursor:pointer" onclick="doSort('f.jqlb')">假期类别</td>
      <td width="6%" class="tabStyle_1_title"style="cursor:pointer" onclick="doSort('f.ts')">天数</td>
      <td width="10%" class="tabStyle_1_title"style="cursor:pointer" onClick="doSort('f.qjkssj')">开始日期
      <%if (orderBy.equals("f.qjkssj")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="9%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.qjjssj')">结束日期
      <%if (orderBy.equals("f.qjjssj")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="10%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.xjrq')">销假日期
      <%if (orderBy.equals("f.xjrq")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>      </td>
      <td width="10%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('fl.mydate')">请假时间
      <%if (orderBy.equals("fl.mydate")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px>");
		}%>	  </td>
      <td width="8%" class="tabStyle_1_title" style="cursor:pointer" onClick="doSort('f.result')">审批结果
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
    </tr>
    <%
Leaf ft = new Leaf();	
UserMgr um = new UserMgr();
FormDAO fdao = new FormDAO();
DeptMgr dm = new DeptMgr();

int nj=0, bj = 0, sj = 0, qt = 0; // 年假、病假、事假、其它
java.util.Date qjbDate = null; // 本月请假于本月的实际开始日期
java.util.Date qjeDate = null; // 本月请假于本月的实际结束日期
com.redmoon.oa.oacalendar.OACalendarDb oacal = new com.redmoon.oa.oacalendar.OACalendarDb();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next();
	fdao = fdao.getFormDAO(wfd.getId(), fd);
	String strBeginDate = fdao.getFieldValue("qjkssj");
	String strEndDate = fdao.getFieldValue("qjjssj");
	String xjrq = fdao.getFieldValue("xjrq");
	DeptDb dd = dm.getDeptDb(fdao.getFieldValue("dept"));
	String deptName = "";
	if (dd!=null)
		deptName = dd.getName();
		
	java.util.Date ksDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	java.util.Date jsDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	java.util.Date xjDate = DateUtil.parse(xjrq, "yyyy-MM-dd");
	boolean isExpire = false;
	if (DateUtil.compare(xjDate, jsDate)==1) {
		isExpire = true;
	}
	
	// 同意请假
	double qjDays = 0;
	if (fdao.getFieldValue("result").equals("1")) {
		if (strshowmonth!=null) {
			qjbDate = ksDate;
			qjeDate = jsDate;
			
			// 如果请假开始时间早于本月第一天
			if (DateUtil.compare(ksDate, bDate)==2) {
				qjbDate = bDate;
			}
			// 如果请假结束日期晚于本月最后一天
			if (DateUtil.compare(jsDate, eDate)==1) {
				qjeDate = eDate;
			}
			// 如果销假日期晚于本月最后一天
			if (DateUtil.compare(xjDate, eDate)==1) {
				qjeDate = eDate;
			}		
			// 去除本月请假区间中的节假日
			qjDays = oacal.getWorkDayCount(DateUtil.addDate(qjbDate, -1), qjeDate);
			// System.out.println(getClass() + " qjDays=" + qjDays + " " + DateUtil.format(qjbDate, "yyyy-MM-dd") + " " + DateUtil.format(qjeDate, "yyyy-MM-dd"));
		}
		else {
			qjbDate = ksDate;
			qjeDate = jsDate;
			// 如果销假日期晚于请假结束日期
			if (DateUtil.compare(xjDate, jsDate)==1) {
				qjeDate = xjDate;
			}
			// 去除请假区间中的节假日
			qjDays = oacal.getWorkDayCount(DateUtil.addDate(qjbDate, -1), qjeDate);
		}
		
		// 取得表单中的请假天数
		double qjDayCount = StrUtil.toDouble(fdao.getFieldValue("ts"), 0.0);
		// 如果表单中的请假天数小于计算出的天数（去除节假日后），则以表单中的为准
		if (qjDayCount < qjDays)
			qjDays = qjDayCount;
	}
	%>
    <tr class="highlight">
      <td align="center"><%=um.getUserDb(fdao.getFieldValue("applier")).getRealName()%></td>
      <td align="center"><%=deptName%></td>
      <td align="center">
	  <%
	  String jqlb = fdao.getFieldValue("jqlb");
	  if (jqlb.equals("年假"))
	  	nj += qjDays;
	  else if (jqlb.equals("病假"))
	  	bj += qjDays;
	  else if (jqlb.equals("事假"))
	  	sj += qjDays;
	  else
	  	qt += qjDays;
	  %>
	  <%=jqlb%>
	  </td>
      <td align="center"><%=qjDays%></td>
      <td align="center"><%=strBeginDate%></td>
      <td align="center"><%=strEndDate%></td>
      <td align="center">
	  <%if (isExpire) {%>
	  <span style="color:red"><%=xjrq%></span>
	  <%}else{%>
	  <%=xjrq%>
	  <%}%>
	  </td>
      <td align="center"><%=DateUtil.format(wfd.getMydate(), "yy-MM-dd HH:mm")%> </td>
      <td align="center"><%=fdao.getFieldValue("result").equals("1")?"通过":"不通过"%></td>
      <td align="center"><%=wfd.getStatusDesc()%></td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="48%" align="left">
    <%
	sql = "select sum(ts) from form_table_qjsqd f, flow fl where f.flowId=fl.id and f.flowTypeCode='qj' and (fl.status=" + WorkflowDb.STATUS_STARTED + " or fl.status=" + WorkflowDb.STATUS_FINISHED + ")";
	sql += " and f.qjkssj>=" + SQLFilter.getDateStr(showyear + "-01-01", "yyyy-MM-dd") + " and f.qjjssj<" + SQLFilter.getDateStr((showyear+1) + "-01-01", "yyyy-MM-dd");;
	sql += " and f.applier=" + StrUtil.sqlstr(userName);
	double allCount = 0.0;
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	if (ri.hasNext()) {
		ResultRecord rr = (ResultRecord)ri.next();
		allCount += rr.getDouble(1);
	}
	%>
    年假：<%=nj%>天&nbsp;&nbsp;病假：<%=bj%>天&nbsp;&nbsp;事假：<%=sj%>天&nbsp;&nbsp;其它：<%=qt%>天&nbsp;&nbsp;全年：<%=allCount%>天</td>
    <td width="52%" align="right"><%
	String querystr = "op="+op + "&orderBy=" + orderBy + "&sort=" + sort + "&op=" + op + "&userName=" + StrUtil.UrlEncode(userName) + "&beginDate=" + beginDate + "&endDate=" + endDate;
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
  </tr>
</table>
</body>
</html>