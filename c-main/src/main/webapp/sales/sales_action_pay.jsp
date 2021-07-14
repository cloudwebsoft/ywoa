<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.sale.Chart"%>
<%@page import="net.sf.json.JSONArray"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String op = ParamUtil.get(request, "op");
if (!privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String preDate = ParamUtil.get(request, "preDate");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "preDate", preDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	if (preDate.equals("*")) {
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	}
	else {
		strBeginDate = "";
		strEndDate = "";
	}
}
String depts = ParamUtil.get(request, "depts");
try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "preDate", preDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "depts", depts, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (depts.equals(DeptDb.ROOTCODE)) {
	depts = "";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>

<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/highcharts/highcharts.js" ></script>
<script type="text/javascript" src="../js/highcharts/highcharts-3d.js" ></script>
<script type="text/javascript" src="../js/my_highcharts.js" ></script>
<script>
function getDepts() {
	return o("depts").value;
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')
	if (ret==null)
		return;
	o("spanDeptNames").innerText = "";
	o("depts").value = "";
	for (var i=0; i<ret.length; i++) {
		if (o("spanDeptNames").innerText=="") {
			o("depts").value += ret[i][0];
			o("spanDeptNames").innerText += ret[i][1];
		}
		else {
			o("depts").value += "," + ret[i][0];
			o("spanDeptNames").innerText += "," + ret[i][1];
		}
	}
	if (o("depts").value.indexOf("<%=DeptDb.ROOTCODE%>")!=-1) {
		o("depts").value = "<%=DeptDb.ROOTCODE%>";
		o("spanDeptNames").innerText = "全部";
	}
	window.location.href = "sales_action_pay.jsp?preDate=<%=preDate%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>&depts=" + o("depts").value;
}
</script>

</head>
<body>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

request.setAttribute("isShowVisitTag", "true");
%>
<%@ include file="sales_action_inc_menu_top.jsp"%>
<script>
$("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<%
String[] deptAry = StrUtil.split(depts, ",");
String ds = "";
if (deptAry!=null) {
	for (int i=0; i<deptAry.length; i++) {
		if (ds.equals(""))
			ds = StrUtil.sqlstr(deptAry[i]);
		else
			ds += "," + StrUtil.sqlstr(deptAry[i]);
	}
}

String unitCode = privilege.getUserUnitCode(request);
String sql = "select s.name,sum(cost_sum) from form_table_day_lxr d,oa_select_option s where d.cost_type = s.value and d.unit_code=" + StrUtil.sqlstr(unitCode) + " and s.code = 'cost_type'";

if (!ds.equals("")) {
	sql = "select s.name,sum(cost_sum) from form_table_day_lxr d, dept_user du ,oa_select_option s where d.cws_creator=du.user_name and d.cost_type = s.value  and  s.code = 'cost_type'  and du.dept_code in (" + ds + ")";
}

if (beginDate!=null) {
	sql += " and d.visit_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
}
if (endDate!=null) {
	sql += " and d.visit_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
}
sql += "group by s.name";
Chart chart = new Chart();
JSONArray pieDate = chart.pieData(sql);

%>
<script>
var pie_params = {data:<%=pieDate%>,seriesName:'百分比',title:'行动成本',tooltip:'{series.name}: <b>{point.percentage:.1f}%</b>',plotOptions:'{point.name}：{point.y}'};
$(function(){
	pieCharts('#pieContainer',pie_params);
})



</script>
<form action="sales_action_pay.jsp" method="get">
<table class="percent98" align="center">
    <tr>
      <td align="center">
部门：<span id="spanDeptNames">
	<%
	String[] arydepts = StrUtil.split(depts, ",");  	  
	int len = 0;
	String deptNames = "";
	if (arydepts!=null) {
	  len = arydepts.length;
	  DeptDb dd = new DeptDb();
	  for (int i=0; i<len; i++) {
		  dd = dd.getDeptDb(arydepts[i]);
		  if (deptNames.equals("")) {
			  deptNames = dd.getName();
		  }
		  else {
			  deptNames += "," + dd.getName();
		  }
	  }
	}
	%>    
    <%=deptNames%>
    </span>
    &nbsp;&nbsp;<a href="javascript:;" onclick="openWinDepts()">选择部门</a>
    <input id="depts" name="depts" type="hidden" value="<%=depts%>" />      
<select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
<option selected="selected" value="">不限</option>
<%
java.util.Date[] ary = DateUtil.getDateSectOfToday();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今天</option>
<%
ary = DateUtil.getDateSectOfYestoday();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">昨天</option>
<%
ary = DateUtil.getDateSectOfCurWeek();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本周</option>
<%
ary = DateUtil.getDateSectOfLastWeek();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上周</option>
<%
ary = DateUtil.getDateSectOfCurMonth();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本月</option>
<%
ary = DateUtil.getDateSectOfLastMonth();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上月</option>
<%
ary = DateUtil.getDateSectOfQuarter();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本季度</option>
<%
ary = DateUtil.getDateSectOfCurYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今年</option>
<%
ary = DateUtil.getDateSectOfLastYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">去年</option>
<%
ary = DateUtil.getDateSectOfLastLastYear();
%>
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">前年</option>
<option value="*">自定义</option>
</select>
<script>
o("preDate").value = "<%=preDate%>";
</script>
<span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>">
从
<input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>		
至
<input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>
</span>
<input type="submit" value="确定" class="btn"/>
		</td>
    </tr>
  </table>
  </form>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="28">
      <div id="pieContainer"></div>
    </td>
  </tr>
</table>
<br />
</body>
</html>
