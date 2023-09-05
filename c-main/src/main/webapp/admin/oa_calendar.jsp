<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.Calendar"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="java.util.Date"%>
<%
	String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
	com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
	if (!privilege.isUserPrivValid(request, "archive.user")) {
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
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>OA日历</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>

<script type="text/javascript" src="../js/google_drag_2.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link href="<%=SkinMgr.getSkinPath(request)%>/main.css" rel="stylesheet" type="text/css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<link type="text/css" rel="stylesheet" href="../js/flexslider/flexslider.css" />
<script type="text/javascript" src="../js/flexslider/jquery.flexslider.js"></script>

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
.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
}
.holiday {
	background-color:#FBF5C7;
}
</style>
<script>
	function selectYear(selectValue) {
		window.location = "oa_calendar.jsp?year=" + selectValue;
	}
	function disableModifyDatesTime(dateType) {
		var showWeek = $("#date_type_more").attr("showWeek");
		if(dateType == 2) {
			$(".timeDiv").hide();
			$(".weekDayDiv").hide();
		}else{
			$(".timeDiv").show();
			if(showWeek == 0){
				$(".weekDayDiv").hide();
			}else{
				$(".weekDayDiv").show();
			}
			
		}
	}
	
	// 初始化点击事件
	function initModifydate(date,timeABegin,timeAEnd,timeBBegin,timeBEnd,timeCBegin,timeCEnd,dateType,weekDay) {
		$("#date_type_more").attr("showWeek",0);
		$(".curTimeDiv").show();
		$("#date_type_more").removeAttr("disabled");  
		$("#date_type_more").find("option[value='"+dateType+"']").attr("selected",true);
		$("#oa_date").val(date);
		$(".endTimeDiv").hide();
		$(".startTimeDiv").hide();
		//工作日
		$(".weekDayDiv").hide();
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
		title:"设置时间",
		modal: true,
		bgiframe:true,
		closeText : "关闭", 
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				document.getElementById('form4').action = "oa_calendar.jsp?op=modifyOne&year="+year;
				document.getElementById('form4').submit();
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
		});
	}
	//批量修改日期类型（1） -批量修改工作时间（2）
	function initModifydates(mode) {
	    flag = false;
		$(".curTimeDiv").hide();
		$(".timeDiv").show();
		$(".endTimeDiv").show();
		$(".startTimeDiv").show();
		//工作日
		$(".weekDayDiv").show();
		$("#date_type_more").attr("showWeek",1);
		if(mode == 1){
			$("#date_type_more").removeAttr("disabled");  
		}else{
			$("#date_type_more").attr("disabled","disabled");  
			$(".weekDayDiv").hide();
		}
		var year = $("#form4").attr("year");
		$("#type").val(mode);
		$("#work_time_begin_a").val('<%=morningbegin%>');
		$("#work_time_end_a").val('<%=morningend%>');
		$("#work_time_begin_b").val('<%=afternoonbegin%>');
		$("#work_time_end_b").val('<%=afternoonend%>');
		$("#work_time_begin_c").val('<%=nightbegin%>');
		$("#work_time_end_c").val('<%=nightend%>');
		$("#modifyDates").dialog({
		title:"设置时间",
		modal: true,
		bgiframe:true,
		closeText : "关闭", 
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				var contains = "";
			   jQuery("[name='containRestDays']").each(function(){     
	           if(jQuery(this).attr("checked"))     
			   {     
			      contains = contains + jQuery(this).val() + ",";
			   }     
	    	    });
				$("#contains").val(contains);
				document.getElementById('form4').action = "oa_calendar.jsp?op=modifyMore&year="+year;
				document.getElementById('form4').submit();
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
		});	
	
	}
	function init(){
		jConfirm('您确定要初始化么？','提示',function(r){ 
			 if(!r){
			 	return;
			 }else{
			 	$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			 	window.location.href='oa_calendar.jsp?op=init&year=<%=year%>';
			 }
		});
	}
	
</script>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
	boolean re = false;
	if (op.equals("init")) {
		re = oacdb.initCalendar(year);
		if (re) {
			out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "oa_calendar.jsp?year=" + year));
			return;
		} else {
			out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"), "提示"));
			return;
		}
	}
	if (op.equals("modifyOne")) {
		OACalendarMgr qom = new OACalendarMgr();
		OACalendarDb oaCalendarDb = new OACalendarDb();
		Date date = DateUtil.parse(ParamUtil.get(request, "oa_date"), "yyyy-MM-dd");
		oaCalendarDb = (OACalendarDb) oaCalendarDb.getQObjectDb(date);
		if (oaCalendarDb != null) {
			oaCalendarDb.del();
		}
		try {
			re = qom.create(request, new OACalendarDb(), "oa_calendar_create");
			if (re) {
				out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "oa_calendar.jsp?year=" + year));
				return;
			} else {
				out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"), "提示"));
				return;
			}
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
			return;
		}
	}
	if (op.equals("modifyMore")) {
		try {
			re = oacdb.modifyDates(request);
			if (re) {
				out.print(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "提示", "oa_calendar.jsp?year=" + year));
				return;
			} else {
				out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "info_op_fail"), "提示"));
				return;
			}
		} catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
			return;
		}
	}
%>
<div class="bg"></div>
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
		<div style="height:22px;float:left;line-height:22px;margin-left:30px;">
		<%
			OACalendarDb oACalendarDb = new OACalendarDb();
			// if(!oACalendarDb.isYearInitialized(year)) {
		%>
			<a href="javascript:;" onclick="init()">初始化</a>
		    <%
			// }
			
		%>
		</div>
		<div style="height:22px;float:left;line-height:22px;margin-left:30px;" id="hopscotch">
			<a href="javascript:;" onclick="initModifydates(1)">批量修改日期类型</a>
			&nbsp;&nbsp;
            <a href="javascript:;" onclick="initModifydates(2)">批量修改工作时间</a>
		</div>
		<%
        	if("introduction".equals(flag)){
        		%>
        		<script>
	        		jQuery(document).ready(function(){
				    	var tour = {
							id : "hopscotch",
							steps : [ {
								title : "提示",
								content : "此处可以修改日期类型、调整工作时间",
								target : "hopscotch",
								placement : "right",
								showNextButton : false,
								width : "120px",
								xOffset : 10,
								yOffset : -5,
								arrowOffset : -1
							}]
						};
						hopscotch.startTour(tour);
					});
				</script>
        		<%
        	}
		 %>
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
	  	<form id="form4" year=<%=year%> action="" method="post" name="memberform">
						<input type="hidden" id="type" name="type" />
						<div class="dialog_margin curTimeDiv">
							<span>当前选择时间&nbsp;&nbsp;</span>
							<input  type="text" id="oa_date" name="oa_date" size=20 readonly="readonly"/>
						</div>
						<div class="dialog_margin startTimeDiv">
							<span>选择开始时间&nbsp;&nbsp;</span>
							<input  type="text"  id="modifyBeginDate" name="modifyBeginDate" size=20 />
						</div>
                     	<div class="dialog_margin endTimeDiv">
							<span>选择结束时间&nbsp;&nbsp;</span>
							<input type="text" id="modifyEndDate"  name="modifyEndDate" readonly="true" />
						</div>
						<div class="dialog_margin">
							<span>选择日期类型&nbsp;&nbsp;</span>
							 <select id="date_type_more" name="date_type" onchange="disableModifyDatesTime(this.options[this.options.selectedIndex].value)">
								<option value="0">工作日</option>
								<option value="2">休息日</option>
							  </select>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>上午上班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_begin_a" name="work_time_begin_a"/>
						</div>
						<div class="dialog_margin timeDiv" >
							<span>上午下班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_end_a" name="work_time_end_a" />
						</div>
						<div class="dialog_margin timeDiv" >
							<span>下午上班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_begin_b" name="work_time_begin_b" />
						</div>
						<div class="dialog_margin timeDiv" >
							<span>下午下班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_end_b" name="work_time_end_b" />
						</div>
						<div class="dialog_margin timeDiv" >
							<span>晚上上班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_begin_c" name="work_time_begin_c" />
						</div>
						<div class="dialog_margin timeDiv" >
							<span>晚上下班时间&nbsp;&nbsp;</span>
							<input type="text" class="timepicker" id="work_time_end_c" name="work_time_end_c" />
						</div>
						<div class="dialog_margin weekDayDiv">
							<input type="checkbox"  name="containRestDays"  id="containSat" value="containSat"> <span>含周六</span>
							<input type="checkbox" name="containRestDays" id="containSun" value="containSun" > 含周日
							<input type="hidden" id="contains" name="contains">
						</div>
						
       </form>
                      
       </div>
	
	
</div>
</body>
<script>
var flag = false;
$(function(){
	var myDate = new Date();
	var startDate = myDate.getFullYear()+"/01/01";
	var endDate = myDate.getFullYear()+"/12/31";
	
	$('.timepicker').datetimepicker({
		datepicker:false,
		format:'H:i',
		step:5
	});
	$('#modifyBeginDate').datetimepicker({
      	lang:'ch',
      	timepicker:false,
      	format:'Y-m-d',
      	minDate:startDate,
    	maxDate:endDate,
      	onShow:function() {
      		if (!flag) {
      			flag = true;
      			return false;
      		}
      	}
     });
     
     $('#modifyEndDate').datetimepicker({
    	lang:'ch',
    	minDate:startDate,
    	maxDate:endDate,
    	timepicker:false,
    	format:'Y-m-d'
     });
    var now = new Date();
	var nowday = now.getFullYear()+"-"+((now.getMonth()+1)<10?"0":"")+(now.getMonth()+1)+"-"+(now.getDate()<10?"0":"")+now.getDate();
	document.getElementById('modifyBeginDate').value = nowday;
	document.getElementById('modifyEndDate').value = nowday;
    
})
</script>
</html>
