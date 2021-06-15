<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.attendance.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<%
// 翻月
int showyear = ParamUtil.getInt(request, "showyear", -1);
int showmonth = ParamUtil.getInt(request, "showmonth", -1);
Calendar cal = Calendar.getInstance();
if (showyear==-1)
	showyear = cal.get(Calendar.YEAR);
if (showmonth==-1)
	showmonth = cal.get(Calendar.MONTH)+1;

int curyear = cal.get(Calendar.YEAR);	
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>排班列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css" />  
<script src="../js/bootstrap/js/bootstrap.min.js"></script>

<link href="../js/bootstrap-switch/bootstrap-switch.css" rel="stylesheet">
<script src="../js/bootstrap-switch/bootstrap-switch.js"></script>

<script src="../js/BootstrapMenu.min.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<style>
.holiday {
	background-color:#FBC4C4;	
}
.none {
	background-color:#AAF49F;
}
.normal {
	background-color:#eeeeee;
}
</style>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<div class="spacerH"></div>
<%
String deptCode = ParamUtil.get(request, "deptCode");

if (deptCode.equals("")) {
	out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
	return;
}
if (!Privilege.canUserAdminDept(request, deptCode)) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb();
dd = dd.getDeptDb(deptCode);
if (dd==null || !dd.isLoaded()) {
	out.print(StrUtil.Alert("部门" + deptCode + "不存在！"));
	return;
}

UserDb user = new UserDb();
	
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
%>
<table class="percent98" width="98%" align="center">
  <tr><td align="center" style="padding: 5px">
<select name="showyear" onchange="var y=this.options[this.selectedIndex].value; window.location.href='shift_schedule.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>&showyear=' + y;">
  <%for (int y=curyear-60; y<=curyear; y++) {%>
  <option value="<%=y%>"><%=y%></option>
  <%}%>
</select>
<script>
o("showyear").value = "<%=showyear%>";
</script>
<%
for (int i=1; i<=12; i++) {
	if (showmonth==i) {
		out.print("<a href='shift_schedule.jsp?showyear="+showyear+"&showmonth="+i+"&deptCode=" + StrUtil.UrlEncode(deptCode) + "'><font color=red>"+i+"月</font></a>&nbsp;");
	} else {
		out.print("<a href='shift_schedule.jsp?showyear="+showyear+"&showmonth="+i+"&deptCode=" + StrUtil.UrlEncode(deptCode) + "'>"+i+"月</a>&nbsp;");
	}
}
%>

<input type="checkbox" id="switcher" checked>
<script>
$.fn.bootstrapSwitch.defaults.onText = '开';
$.fn.bootstrapSwitch.defaults.offText = '关';
$.fn.bootstrapSwitch.defaults.size = 'small';
$.fn.bootstrapSwitch.defaults.handleWidth = '40';

$("#switcher").bootstrapSwitch();

// console.log($('#switcher').bootstrapSwitch('state', false));
</script>

<select id="shift" name="shift" class="selectpicker" data-style="btn-primary">
<!-- 取消恢复调整前的排班，以免逻辑过于复杂
<option value="" style="display:none">恢复调整前的排班</option>
-->
<%
ShiftScheduleMgr ssm = new ShiftScheduleMgr();
Iterator ir = ssm.listShift().iterator();
while (ir.hasNext()) {
	FormDAO fdao = (FormDAO)ir.next();
	%>
	<option value="<%=fdao.getId()%>" shiftColor="<%=fdao.getFieldValue("color") %>" style="background-color:<%=fdao.getFieldValue("color")%>"><%=fdao.getFieldValue("name")%></option>
	<%
}
%>
</select>
<input id="btnOk" type="button" class="btn btn-default" value="保存" />
<script>
/*
$('.selectpicker').selectpicker({
     style: 'btn-info',
     size: 4
});
*/
  
var data = "";
$(function() {
	$('#btnOk').click(function() {
		data = "";
		$("#mainTable tr").each(function(n){
			// 跳过表头
			if (n==0) {
				return;
			}
			var arrTd = $(this).children();
			var userName = "";
			var userData = "";
			for (var k = 0; k < arrTd.length; k++) {
				var td = arrTd.eq(k);
				if (k==0) {
					// 取第一列中的用户
					userName = td.attr("userName");
					continue;
				}
				
				if (td.text()=="*") {
					td.text("O");
					td.addClass('tdAdjust');
				}
				
				var shift = td.attr("shift");
				// 如果与原始的一致，说明没有调整过排班
				if (shift==td.attr("origShift")) {
					continue;
				}
				if (true || shift!="") {
					var day = td.attr("day");
					if (userData=="") {
						userData = "<%=showyear%>-<%=showmonth%>-" + day + "," + shift;
					}
					else {
						userData += "#<%=showyear%>-<%=showmonth%>-" + day + "," + shift;				
					}
				}
				
				if (data=="") {
					data = userName + ":" + userData;
				}
				else {
					data += ";" + userName + ":" + userData;				
				}
			}
		});		
		
		$.ajax({
			type: "post",
			url: "saveShiftAdjust.do",
			contentType:"application/x-www-form-urlencoded; charset=iso8859-1", 			
			data: {
				data: data
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#mainTable').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="0") {
					jAlert(data.msg, "提示");
				}
				else {
					jAlert(data.msg, "提示");
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#mainTable').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});			
	});
});
</script>
</td></tr></table>
<form action="" method="post" name="form1" id="form1">
  <table id="mainTable" class="tabStyle_1" border="0" align="center" cellpadding="0" cellspacing="0">
  <thead>
    <tr>
      <td style="width:100px;font-weight:normal" nowrap height="24" align="center">&nbsp;&nbsp;&nbsp;姓名&nbsp;&nbsp;&nbsp;</td>
      <%
	  int dayCount = DateUtil.getDayCount(showyear, showmonth-1);
	  int allNoCount = 0;
	  for (int i=1; i<=dayCount; i++) {
		java.util.Date day = DateUtil.getDate(showyear, showmonth-1, i);
		String weekDay = DateUtil.getDayOfWeek(day);
	  %>
      <td style="width:40px;font-weight:normal" align="center">
		<%=i%>
		<br/>
		<%=weekDay%>
	  </td>
      <%}%>
      </tr>
  </thead>
<%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";

	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";
	java.util.Date start = DateUtil.getDate(showyear, showmonth-1, 1);
	OACalendarDb oacal = new OACalendarDb();
	ir = du.listBySQL(sql).iterator();
	int row = 0;
	while (ir.hasNext()) {
		DeptUserDb pu = (DeptUserDb)ir.next();
		if (!pu.getUserName().equals(""))
			user = user.getUserDb(pu.getUserName());
		else
			continue;
		int noCount = 0;
		row ++;
%>
    <tr>
      <td height="22" align="center" userName="<%=user.getName() %>">
	  <%=user.getRealName()%>
      </td>
      <%
	  for (int i=0; i<=dayCount-1; i++) {
		  // 判断是否为非工作日
		  java.util.Date d = DateUtil.addDate(start, i);
		  oacal = (OACalendarDb) oacal.getQObjectDb(d);
		  String cls = "";
		  if (oacal!=null && oacal.getInt("date_type") != OACalendarDb.DATE_TYPE_WORK) {
			cls = "class='holiday'";
		  }
		  else {
			cls = "class='normal'";	
		  }
		  
		  if (oacal==null) {
		  	oacal = new OACalendarDb();
		  }

		String style = "";
		boolean isAdjust = false;
	  	Object[] objAry = ShiftScheduleMgr.getShiftDAO(pu.getUserName(), d);
		FormDAO fdao = null;
		if (objAry!=null) {
			fdao = (FormDAO)objAry[0];
			isAdjust = (Boolean)objAry[1];
		}
		String shiftName = "";
		String origBgColor = ""; // 记录的背景色，如果是调整过的，则为调整后班次的背景色，如果未调整，则为进入页面时初始背景色
		String normalBgColor = ""; // 记录的正常排班背景色，如果有排班则为排班颜色，如果没有排班，则为空
		String normalName = "";
		long origShift = -1;
		long normalShift = -1;
	  	if (fdao!=null) {
			origBgColor = fdao.getFieldValue("color");
			style = "background-color:" + fdao.getFieldValue("color");
			origShift = fdao.getId();
	  		shiftName = fdao.getFieldValue("name");
	  	}
	  	else {
	  		shiftName = "默认排班";
	  	}
		
		FormDAO fdaoNormal = ShiftScheduleMgr.getShiftNormal(pu.getUserName(), d);
		if (fdaoNormal!=null) {
			normalBgColor = fdaoNormal.getFieldValue("color");	
			normalName = fdaoNormal.getFieldValue("name");
			normalShift = fdaoNormal.getId();
		}
	  %>
      <td id="td<%=row%>_<%=i%>" normalShift="<%=normalShift%>" origName="<%=shiftName%>" class="<%=isAdjust?"tdAdjust":""%>" normalName="<%=normalName%>" align="center" <%=cls%> style="<%=style%>" title="<%=shiftName%>" day="<%=i+1 %>" origShift="<%=origShift%>" normalBgColor="<%=normalBgColor%>" origBgColor="<%=origBgColor%>" shift="<%=origShift%>" >
	  <%if (isAdjust) {%>
      O
      <%}%>
      <script>
		var isLeftBtnDown = false; // 定义全局变量，表示鼠标是否按下
		var body = $("body");
		$("body").mousedown(function(e){
			if (e.which==3) // 右键
				return;
			isLeftBtnDown = true;
		});
		$("body").mouseup(function(){
			isLeftBtnDown = false;
		});
      
		$(function(){
		  	$("#td<%=row%>_<%=i%>").click(function (e){
				if (e.which==3) // 右键
					return;
		  		var isOn = $('#switcher').bootstrapSwitch('state');
				if (isOn) {
					var shiftSel = $('#shift').val();
					if (shiftSel=="") {
						// 恢复为原始值					
						$(this).css("background-color", $(this).attr('origBgColor'));
						$(this).css("shift", $(this).attr('origShift'));
						$(this).attr('title', $(this).attr('origName'));						
						if ($(this).text()=="*") {
							$(this).html('');
						}
					}
					else {
						if (shiftSel != $(this).attr('shift')) {
							var clr = $("#shift").find("option:selected").attr("shiftColor");
							$(this).css("background-color", clr);
							$(this).attr('title', $("#shift").find("option:selected").text());
							if (shiftSel==$(this).attr('normalShift')) {
								// 如果所选班次与正常排班一致，则恢复为正常排班的班次
								$(this).attr("shift", ""); // 标记为没有调整的班次	
								$(this).html('');								
							}
							else {
								$(this).attr("shift", shiftSel);	
								$(this).html('*');	
							}				
						}		
					}
				}
		  	});
	
			$("#td<%=row%>_<%=i%>").mousemove(function(e){
				if (isLeftBtnDown) {
			  		var isOn = $('#switcher').bootstrapSwitch('state');
					if (isOn) {
						var shiftSel = $('#shift').val();
						if (shiftSel=="") {
							// 恢复为初始值
							$(this).css("background-color", $(this).attr('origBgColor'));
							$(this).css("shift", $(this).attr('origShift'));
							if ($(this).text()=="*") {
								$(this).html('');
							}
							$(this).attr('title', $(this).attr('origName'));
						}
						else {
							if (shiftSel != $(this).attr('shift')) {						
								var clr = $("#shift").find("option:selected").attr("shiftColor");
								$(this).css("background-color", clr);	
								$(this).attr('title', $("#shift").find("option:selected").text());
								if (shiftSel==$(this).attr('normalShift')) {
									// 如果所选班次与正常排班一致，则恢复为正常排班的班次
									$(this).attr("shift", ""); // 标记为没有调整的班次	
									$(this).html('');								
								}
								else {								
									$(this).attr("shift", $('#shift').val());
									$(this).html('*');
								}
							}
						}
					}
			  	}
			});	
		});
	  </script>
      </td>
      <%}%>
    </tr>
    <%
		allNoCount += noCount;
    }
%>
  </table>
</form>
<div style="text-align:center">(点击或拖动鼠标可以排班，O&nbsp;表示是已被调整的排班，*&nbsp;表示正被调整还没有保存)</div>
</body>
<script>
var addBtpMenuEvent = function(){
        var menu = new BootstrapMenu('.tdAdjust', {
            //fetchElementData获取元数据
            fetchElementData:function($rowElem){
                var data = $rowElem;   
                return data;    //return的目的是给下面的onClick传递参数
            },
            actions: [{
                  	name: '删除调整',
                  	width:300,
                  	iconClass: 'fa-trash',
					onClick: function (obj) {
						obj.attr("shift", "");
						obj.html('');
						obj.css("background-color", obj.attr('normalBgColor'));
						obj.attr("title", obj.attr('normalName'));
						obj.removeClass('tdAdjust');
					}
                }]
            });
    };

$(function(){
    addBtpMenuEvent();
});
</script>
</html>
