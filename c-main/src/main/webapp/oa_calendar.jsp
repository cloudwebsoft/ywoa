<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.Calendar"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="java.util.Date"%>
<%
	//String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
	//com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
	//if (!privilege.isUserPrivValid(request, "archive.user")) {
	//	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	//	return;
	//}
	
	com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
	if (!privilege.isUserLogin(request)) {
    	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	com.redmoon.oa.Config configs = new com.redmoon.oa.Config();
	String morningbegin = configs.get("morningbegin");
	String morningend = configs.get("morningend");
	String afternoonbegin = configs.get("afternoonbegin");
	String afternoonend = configs.get("afternoonend");
	String nightbegin = configs.get("nightbegin");
	String nightend = configs.get("nightend");
	int hour = 0;
	int min = 0;

	Calendar now = Calendar.getInstance();
	int year = ParamUtil.getInt(request, "year",now.get(Calendar.YEAR));
	OACalendarDb oacdb = new OACalendarDb();
	String op = ParamUtil.get(request, "op");
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>日历</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="js/datepicker/jquery.datetimepicker.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script type="text/javascript" src="../js/google_drag_2.js"></script>
<script src="js/jquery.bgiframe.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link href="<%=SkinMgr.getSkinPath(request)%>/main.css" rel="stylesheet" type="text/css" />
<link type="text/css" rel="stylesheet" href="js/hopscotch/css/hopscotch.css" />
<link type="text/css" rel="stylesheet" href="js/flexslider/flexslider.css" />
<script type="text/javascript" src="js/flexslider/jquery.flexslider.js"></script>

<style type="text/css">
body {
margin:0px;
padding:0px;
}
a {
cursor:hand;
}
.oaCalendarContent {
width:804px;
height:545px;
background-color:#ffffff;
color:#333333;
margin:0px;
padding:0px;
}
.oaCalendarHeader {
width:784px;
height:22px;
font-family:"宋体";
font-size:12px;
margin:6px 10px 0px 10px;
}
.oaCalendarContent a:link {
color:#333333;
text-decoration:none;
}
.oaCalendarContent a:visited {
color:#333333;
text-decoration:none;
}
.oaCalendarContent a:hover {
color:#333333;
text-decoration:none;
cursor:pointer;
}
.oaCalendarBody {
width:784px;
height:507px;
margin:15px 10px;
}
.oaCalendarBody ul {
width:784px;
height:507px;
padding:0px;
margin:0px;
list-style:none;
}
.oaCalendarBody li {
width:196px;
height:169px;
float:left;
text-align:center;
margin-bottom:5px;
}
.oaCalendarBody li td {
height:18px;
font-size:12px;
line-height:18px;
text-align:center;
color:#666666;
}
.holiday {
	background-color:#FBF5C7;
}
</style>
<script>
	function selectYear(selectValue) {
		window.location = "oa_calendar.jsp?year=" + selectValue;
	}
	
	//初始化点击事件
	function initModifydate(date,timeABegin,timeAEnd,timeBBegin,timeBEnd,timeCBegin,timeCEnd,dateType,weekDay) {
		$("#date_type_more").attr("showWeek",0);
		$(".curTimeDiv").show();
		$("#date_type_more").find("option[value='"+dateType+"']").attr("selected",true);
		$("#date_type_more").attr("disabled", true);  
		$("#oa_date").val(date);
		//工作日
		if(dateType == 0){ 
			$(".timeDiv").show();
			$("#work_time_begin_a").val(timeABegin);
			$("#work_time_end_a").val(timeAEnd);
			$("#work_time_begin_b").val(timeBBegin);
			$("#work_time_end_b").val(timeBEnd);
			$("#work_time_begin_c").val(timeCBegin);
			$("#work_time_end_c").val(timeCEnd);
		}else{
			$(".timeDiv").hide();
		}
		var year = $("#form4").attr("year");
		
		$("#modifyDates").dialog({
			title:"工作时间",
			modal: true,
			bgiframe:true,
			closeText : "关闭", 
			closeOnEscape: true,
			draggable: true,
			resizable:true
		});	
		
	}
</script>
</head>
<body>
<div class="oaCalendarContent" style="margin:0px auto">
	<div class="oaCalendarHeader">
		<div style="height:22px;float:left;line-height:22px; margin-left:8px">
		  <%
				if(year == 2008) {
			%>
			&lt;
			<%
				} else {
			%>
			<a href="?year=<%=year-1%>">&lt;</a>
			<%
				}
			%>
		</div>
		<div style="width:78px;height:22px;float:left;line-height:22px">
			&nbsp;
			<select id="selectYear" name="year" onchange="selectYear(this.options[this.options.selectedIndex].value)" style="font-size:12px;color:#666666">
			<%
				for(int i=2008;i<=2100;i++) {
					if(i == year) {
			%>
					<option selected="selected" value="<%=i%>"><%=i%></option>
			<%
					} else {
			%>
					<option value="<%=i%>"><%=i%></option>
			<%
					}
				}
			%>
			</select>
			&nbsp;
		</div>
		<div style="height:22px;float:left;line-height:22px">
			<%
				if(year == 2100) {
			%>
			&lt;
			<%
				} else {
			%>
			<a href="?year=<%=year+1%>">&gt;</a>
			<%
				}
			%>
			年
		</div>
	</div>
	<div class="oaCalendarBody">
		<ul>
		<%
			for(int i=0; i<12; i++) {
		%>
			<li><%=oacdb.renderMonthlyCalendar(year,i)%></li>
		<%
			}
		%>
		</ul>
	</div>
	<div id="modifyBackground" style="display:none"></div>
	  <div id="modifyDates" style="display:none">
						<input type="hidden" id="type" name="type" />
						<div class="dialog_margin curTimeDiv">
							<span>当前选择日期&nbsp;&nbsp;</span>
							<input  type="text"  id="oa_date" name="oa_date" size=20 readonly="readonly"/>
						</div>
						<div class="dialog_margin">
							<span>当前日期类型&nbsp;&nbsp;</span>
							 <select id="date_type_more" name="date_type">
								<option value="0">工作日</option>
								<option value="2">休息日</option>
							  </select>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>上午上班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_begin_a" name="work_time_begin_a" readonly="readonly"/>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>上午下班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_end_a" name="work_time_end_a" readonly="readonly"/>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>下午上班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_begin_b" name="work_time_begin_b" readonly="readonly"/>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>下午下班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_end_b" name="work_time_end_b" readonly="readonly"/>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>晚上上班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_begin_c" name="work_time_begin_c" readonly="readonly"/>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>晚上下班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_end_c" name="work_time_end_c" readonly="readonly"/>
						</div>
       </div>
</div>
</body>
</html>
