<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.flow.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.job.*,
				 com.redmoon.oa.ui.*,
				 com.cloudwebsoft.framework.base.*"
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>调度-添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script>
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm","266px","185px");
    
}
function sel(dt) {
    if (dt!=null)
        o("time").value = dt;
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function trimOptionText(strValue) 
{
	// 注意option中有全角的空格，所以不直接用trim
	var r = strValue.replace(/^　*|\s*|\s*$/g,"");
	return r;
}
</script>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");

    if ("add".equals(op)) {
        QObjectMgr qom = new QObjectMgr();
        JobUnitDb ju = new JobUnitDb();
        try {
            if (qom.create(request, ju, "scheduler_add")) {
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "scheduler_list.jsp"));
            } else {
                out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert_Back(e.getMessage()));
        }
        return;
    }
%>
</head>
<body>
<%@ include file="scheduler_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<form action="?op=add" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<table width="94%" border="0" align="center" class="tabStyle_1 percent80">
    <tr>
      <td class="tabStyle_1_title" align="left"><strong>调度流程</strong></td>
    </tr>
    <tr>
      <td align="left">
        名称：
        <input name="job_name" />
    &nbsp;<span id="spanKind">
    类型：
	<input id="kind" name="kind" value="0" type="radio" checked />按日期
	<input id="kind" name="kind" value="1" type="radio" />按星期
	<input id="kind" name="kind" value="2" type="radio" />按间隔
	<input id="kind" name="kind" value="3" type="radio" />按cron表达式
    </span>    
    </td>
    </tr>
    <tr>
      <td align="left">
      <div id="div0">
      	开始时间：
        <input style="WIDTH: 70px" value="12:00:00" id="time" name="time" size="20" readonly="readonly" />
        <!-- &nbsp;<img style="CURSOR: hand" onclick="SelectDateTime('time')" src="../images/form/clock.gif" align="absmiddle" width="18" height="18" /> -->
        <span id="spanMon">
        每月
        <input id="month_day" name="month_day" size="2" />
        号
        </span>
        <span id="spanWeek" style="display:none">
        <input name="weekDay" type="checkbox" value="1" />
        星期日
        <input name="weekDay" type="checkbox" value="2" />
        星期一
        <input name="weekDay" type="checkbox" value="3" />
        星期二
        <input name="weekDay" type="checkbox" value="4" />
        星期三
        <input name="weekDay" type="checkbox" value="5" />
        星期四
        <input name="weekDay" type="checkbox" value="6" />
        星期五
        <input name="weekDay" type="checkbox" value="7" />
        星期六
        </span>
      </div>
          <div id="divInterval" style="display:none">
          每隔
          <input id="interval" name="interval" size="3" />
          <input type="radio" id="intervalType" name="intervalType" value="d" />天
          <input type="radio" id="intervalType" name="intervalType" value="h" />时
          <input type="radio" id="intervalType" name="intervalType" value="m" />分
          <input type="radio" id="intervalType" name="intervalType" value="s" />秒
          </div>
          <div id="divCron" style="display:none">
          cron表达式
          <input id="mycron" name="mycron" />
          </div>        
      </td>
    </tr>
    <tr>
      <td align="left">
    选择流程：
        <select name="flowCode" style="min-width: 150px" onchange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); return false;} form1.job_name.value=trimOptionText(this.options[this.selectedIndex].text) ">
            <%
            Leaf lf = new Leaf();
            lf = lf.getLeaf(Leaf.CODE_ROOT);
            DirectoryView dv = new DirectoryView(lf);
            //dv.ShowDirectoryAsOptions(request, out, lf, 1);
            dv.ShowDirectoryAsOptionsForSchedule(request, out, lf, 1);
          %>
        </select>
          <span style="color:red">*(只能选择能确认发起人或者发起角色的流程)</span>
      </td>
    </tr>
    <tr>
      <td align="center"><input name="submit" class="btn" type="submit" value="确定" />
      <input name="job_class" type="hidden" value="com.redmoon.oa.job.WorkflowJob" />
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
      <input name="cron" type="hidden" />
      <span class="p14">
      <input name="data_map" type="hidden" />
      </span>
      </td>
    </tr>
</table>
</form>
</body>
<script>
function form1_onsubmit() {
	if (o("job_name").value=="") {
		alert("名称不能为空！");
		o("job_name").focus();
		return false;
	}
	
	if (o("flowCode").value=="not") {
		alert("请选择流程类型！");
		return false;
	}

	form1.data_map.value = o("flowCode").value;
	var t = form1.time.value;
	var ary = t.split(":");
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = form1.month_day.value;
	var kind = getRadioValue("kind");
	if (kind=="0") {
		if (dayOfMonth=="") {
			alert("请填写每月几号！");
			o("month_day").focus();
			return false;
		}
		if (dayOfMonth!="" && (parseInt(dayOfMonth)>31 || parseInt(dayOfMonth)<0)) {
			alert("每月天数不能小于0，且不能大于31");
			return false;
		}
		if (dayOfMonth=="")
			dayOfMonth = "?";
		var cron = ary[2] + " " + ary[1] + " " + ary[0] + " " + dayOfMonth + " * ?";
		form1.cron.value = cron;
	}
	else if (kind=="1") {
		if (weekDay=="") {
			alert("请选择星期几！");
			return false;
		}
		if (dayOfMonth=="")
			dayOfMonth = "?";
		var cron = ary[2] + " " + ary[1] + " " + ary[0] + " ? * " + weekDay;
		form1.cron.value = cron;
	}
	else if (kind=="2") {
		var type = getRadioValue("intervalType");
		if (type=="") {
			alert("请选择天、时、分或秒！");
			return false;
		}
		var val = o("interval").value;
		if (val=="") {
			alert("请输入时间间隔！");
			o("interval").focus();
			return false;
		}
		var cron = "";
		if (type=="s") {
			cron = "0/" + val + " * * * * ?";
		}
		else if (type=="m") {
			cron = "* 0/" + val + " * * * ?";
		}
		else if (type=="h") {
			cron = "* * 0/" + val + " * * ?";
		}
		else if (type=="d") {
			cron = "* * * 0/" + val + " * ?";
		}
		form1.cron.value = cron;
	}
	else if (kind=="3") {
		if (o("mycron").value=="") {
			alert("请输入cron表达式！");
			o("mycron").focus();
			return false;
		}
		o("cron").value = o("mycron").value;
	}
}

$(function() {
	$('#time').datetimepicker({
     	lang:'ch',
     	datepicker:false,
     	format:'H:i:00',
     	step:1
	});
	$("#spanKind :radio").click(function() {
		if ($(this).val()=="0") {
			$("#div0").show();
			$("#divInterval").hide();
			$("#divCron").hide();
			
			$("#spanMon").show();
			$("#spanWeek").hide();
			$("input[name='weekDay']").each(function() {
				$(this).attr("checked", false);
			});
		}
		if ($(this).val()=="1") {
			$("#div0").show();
			$("#divInterval").hide();
			$("#divCron").hide();
			
			$("#spanMon").hide();
			$("#spanWeek").show();
			
			$("#month_day").val("");
		}		
		else if ($(this).val()=="2") {
			$("#div0").hide();
			$("#divInterval").show();
			$("#divCron").hide();
		}
		else if ($(this).val()=="3") {
			$("#div0").hide();
			$("#divInterval").hide();
			$("#divCron").show();
		}
	});
});
</script>
</html>