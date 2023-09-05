<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ErrMsgException,
				 cn.js.fan.util.ParamUtil,
				 cn.js.fan.util.StrUtil,
				 cn.js.fan.web.DBInfo,
				 cn.js.fan.web.SkinUtil,
				 com.cloudwebsoft.framework.base.QObjectMgr,
				 com.cloudwebsoft.framework.db.JdbcTemplate,
				 com.redmoon.oa.basic.SelectDb,
				 com.redmoon.oa.basic.SelectMgr,
				 com.redmoon.oa.basic.SelectOptionDb,
				 com.redmoon.oa.flow.FormDb,
				 com.redmoon.oa.flow.FormField"
%>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<%@ page import="com.redmoon.oa.kernel.JobUnitDb"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ page import="org.json.JSONArray"%>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
    String priv = "admin";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }

    String op = ParamUtil.get(request, "op");
    String formCode = ParamUtil.get(request, "formCode");

    int id = ParamUtil.getInt(request, "id");
    String primaryKey = "", foreignKey = "", table = "", dbSource = "";

    JobUnitDb ju = new JobUnitDb();
    ju = (JobUnitDb) ju.getQObjectDb(id);
    if ("edit".equals(op)) {
        QObjectMgr qom = new QObjectMgr();
        try {
            if (qom.save(request, ju, "scheduler_edit")) {
                out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "scheduler_edit_syn_data.jsp?id=" + id));
            } else {
                out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
            }
        } catch (ErrMsgException e) {
            out.print(StrUtil.Alert_Back(e.getMessage()));
        }
        return;
    }

    String job_data = ju.getString("job_data");
    String strDataMap = ju.getString("data_map");
    JSONObject dataMap = new JSONObject(strDataMap);
    try {
        dbSource = dataMap.getString("dbSource");
        primaryKey = dataMap.getString("primaryKey");
        foreignKey = dataMap.getString("foreignKey");
        if ("".equals(formCode)) {
            formCode = dataMap.getString("formCode");
        }
        table = dataMap.getString("table");
    } catch (JSONException e) {
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>调度-修改</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/select2/select2.js"></script>
<link href="../js/select2/select2.css" rel="stylesheet" />
    <script>
        function sel(dt) {
            if (dt != null)
                o("time").value = dt;
        }

        function trimOptionText(strValue) {
            // 注意option中有全角的空格，所以不直接用trim
            return strValue.replace(/^　*|\s*|\s*$/g, "");
        }
    </script>
</head>
<body>
<%@ include file="scheduler_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<form action="?op=edit" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<table width="94%" border="0" align="center" class="tabStyle_1 percent80">
    <tr>
      <td class="tabStyle_1_title" align="left"><strong>调度数据订阅</strong></td>
    </tr>
    <tr>
      <td align="left">
    表单
    	<select id="formCode" name="formCode">
        <option value="">请选择</option>        
        <%
		FormDb fd = new FormDb();
		Iterator ir = fd.list().iterator();
		while (ir.hasNext()) {
			fd = (FormDb)ir.next();
			%>
			<option value="<%=fd.getCode()%>"><%=fd.getName()%></option>
			<%
		}
		%>
        </select>
      &nbsp;&nbsp;数据源
        <select id="dbSource" name="dbSource">
        <option value="">请选择</option>
        <%
        cn.js.fan.web.Config cfg = new cn.js.fan.web.Config();
        ir = cfg.getDBInfos().iterator();
        while (ir.hasNext()) {
            DBInfo di = (DBInfo)ir.next();
            %>
            <option value="<%=di.name%>" <%=di.isDefault?"selected":""%>><%=di.name%></option>
            <%
        }
        %>
        </select>      
                       表
        <select id="tables" name="tables">
        </select>
        <script>
        $('#formCode').change(function() {
        	window.location.href = "scheduler_add_syn_data.jsp?formCode=" + $(this).val();
        });
        
		$('#dbSource').change(function() {
			if ($(this).val()=="")
				return;
			getTables($(this).val());
		});    
		
		function getTables(dbSource) {
			// 取所选数据源的表名
			var str = "op=getTables&dbSource=" + dbSource;
			var myAjax = new cwAjax.Request(
				"ide_left.jsp", 
				{ 
					method:"post", 
					parameters:str,
					onComplete:doGetTableOptions,
					onError:errFunc
				}
			);			
		}
		
		function getFields(table) {
			// 取所选数据源的表名
			var str = "op=getFields&table=" + table + "&dbSource=" + $('#dbSource').val();
			var myAjax = new cwAjax.Request(
				"scheduler_add_syn_data.jsp", 
				{ 
					method:"post", 
					parameters:str,
					onComplete:doGetFields,
					onError:errFunc
				}
			);		
		}
		
		$(function() {
			$('#formCode').val("<%=formCode%>");
			$('#dbSource').val("<%=dbSource%>");
			getTables($('#dbSource').val());
			$('#formCode').select2();
		});
		
		$('#tables').change(function() {
			if ($(this).val()=="")
				return;
			getFields($(this).val());
		});
		
		function doGetTableOptions(response) {
			var rsp = response.responseText.trim();
			$("#tables").empty();
			$("#tables").append(rsp);
			$('#tables').val("<%=table%>");
			
			getFields("<%=table%>");
		} 
		
		var fieldCount = 0;
		function doGetFields(response) {
			var rsp = response.responseText.trim();
			$("#primaryKey").empty();
			$("#primaryKey").append(rsp);
			
			$('#primaryKey').val("<%=primaryKey%>");
			
			var fieldMap = <%=dataMap.getJSONObject("fieldMap")%>;
			for (i=0; i<fieldCount; i++) {
				$('#field' + i).empty();
				$('#field' + i).append(rsp);
      			$("#field" + i).val(fieldMap[$('#fieldName' + i).val()]);
			}
		}		
		
		var errFunc = function(response) {
			window.status = response.responseText;
		}
        </script>
      主键
      <select id="primaryKey" name="primaryKey">
      </select>
      外键
      <input id="foreignKey" name="foreignKey" size="5" value="<%=foreignKey%>" />
      </td>
    </tr>    
    <tr>
      <td align="left">
        名称
    <input name="job_name" value="<%=ju.getString("job_name")%>" />
    &nbsp;<span id="spanKind">
       类型
	<input name="kind" value="0" type="radio" checked />按日期
	<input name="kind" value="1" type="radio" />按星期
	<input name="kind" value="2" type="radio" />按间隔
	<input name="kind" value="3" type="radio" />按cron表达式
    </span>    
    </td>
    </tr>
    <tr>
      <td align="left">
      <div id="div0" style="display:none">      
      开始时间
<%
String cron = ju.getString("cron");
String[] ary = cron.split(" ");
if (ary[0].length()==1)
	ary[0] = "0" + ary[0];
if (ary[1].length()==1)
	ary[1] = "0" + ary[1];
if (ary[2].length()==1)
	ary[2] = "0" + ary[2];

String interval = "", intervalType = "";
int p = ary[0].indexOf("/");
if (p!=-1) {
	interval = ary[0].substring(p+1);
	intervalType = "s";
}
p = ary[1].indexOf("/");
if (p!=-1) {
	interval = ary[1].substring(p+1);
	intervalType = "m";
}
p = ary[2].indexOf("/");
if (p!=-1) {
	interval = ary[2].substring(p+1);
	intervalType = "h";
}
p = ary[3].indexOf("/");
if (p!=-1) {
	interval = ary[3].substring(p+1);
	intervalType = "d";
}

String monthDay = StrUtil.getNullStr(ju.getString("month_day"));

String t = "";
if (intervalType.equals("")) {
	t = ary[2] + ":" + ary[1] + ":" + ary[0];
}
%>
        <input style="WIDTH: 60px" id="time" name="time" size="20" value="<%=t%>" />
        <span id="spanMon">
      	每月
        <input id="month_day" name="month_day" size="2" value="<%=monthDay%>" />
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

		<%
        String[] w = ary[5].split(",");
        for (int i=0; i<w.length; i++) {
        %>
        <script>
        setCheckboxChecked("weekDay", "<%=w[i]%>");
        </script>
        <%
        }
        %>
        </span>
        </div>
        
        <div id="divInterval" style="display:none">
        每隔
        <input id="interval" name="interval" size="3" value="<%=interval%>" />
        <input type="radio" id="intervalType" name="intervalType" value="d" />天
        <input type="radio" id="intervalType" name="intervalType" value="h" />时
        <input type="radio" id="intervalType" name="intervalType" value="m" />分
        <input type="radio" id="intervalType" name="intervalType" value="s" />秒
        <%
		if (!intervalType.equals("")) {
			%>
			<script>
			setRadioValue("intervalType", "<%=intervalType%>");
			</script>
			<%
		}
		%>
        </div>
        <div id="divCron" style="display:none">
        cron表达式
        <input id="mycron" name="mycron" value="<%=cron%>" />
        </div> 
      </td>
    </tr>
    <tr>
        <td align="center">
            <input name="submit" class="btn" type="submit" value="确定"/>
            <input name="job_class" type="hidden" value="com.redmoon.oa.job.SynThirdPartyDataJob"/>
            <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden"/>
            <input name="cron" type="hidden"/>
            <input name="data_map" type="hidden"/>
            <input name="job_data" type="hidden" value="<%=job_data%>"/>
        </td>
    </tr>
</table>

<table class="tabStyle_1 percent80" width="95%" align="center">
  	<tr>
        <td width="17%" class="tabStyle_1_title">列名</td>
        <td width="27%" class="tabStyle_1_title">字段名</td>
    </tr>
<%
if (!"".equals(formCode)) {
	JSONObject fieldMap = dataMap.getJSONObject("fieldMap");

	fd = new FormDb(formCode);
	ir = fd.getFields().iterator();
	int i = 0;
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		String field = "";
		if (fieldMap.has(ff.getName())) {
			field = fieldMap.getString(ff.getName());
		}
%>    
  	<tr>
  	  <td>
      <%=ff.getTitle() %>
      <input id="fieldName<%=i %>" name="fieldName<%=i %>" value="<%=ff.getName() %>" type="hidden" />
      </td>
  	  <td>
		<select id="field<%=i%>" name="field<%=i%>">
		</select>	 			
	  </td>
    </tr>
<%
		i++;
	}
	%>
	<script>
	fieldCount = <%=i%>;
	</script>
	<%
}
%>
</table>
<table class="percent80" width="95%" align="center">
<tr>
<td>
<b>主表基础数据清洗</b></td>
</tr>
</table>
<%
JSONArray aryClean = dataMap.getJSONArray("cleanMap");
SelectMgr sm = new SelectMgr();
MacroCtlMgr mm = new MacroCtlMgr();
ir = fd.getFields().iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu!=null && mu.getCode().equals("macro_flow_select")) {
			SelectDb sd = sm.getSelect(ff.getDefaultValueRaw());

			boolean isClean = false;
			JSONObject json = null;
			if (ary!=null) {
				for (int i=0; i<aryClean.length(); i++) {
					json = aryClean.getJSONObject(i);
					if (ff.getName().equals(json.get("fieldName"))) {
						isClean = true;
						break;
					}
				}
			}
%>
    <table class="percent80" width="95%" align="center">
    <tr>
    <td>
    <input type="checkbox" id="is_clean_<%=ff.getName()%>" name="is_clean_<%=ff.getName()%>" <%=isClean?"checked":""%> value="1" /><%=ff.getTitle()%>
          （勾选后才能清洗数据）
    </td>
    </tr>
    </table>
    <table class="tabStyle_1 percent80" width="95%" align="center">
        <tr>
            <td width="32%" class="tabStyle_1_title">名称</td>
            <td width="30%" class="tabStyle_1_title">值</td>
            <td width="38%" class="tabStyle_1_title">对应的名称</td>
        </tr>
<%
		Vector v = sd.getOptions(new JdbcTemplate());
		Iterator irBasic = v.iterator();
		while (irBasic.hasNext()) {
			SelectOptionDb sod = (SelectOptionDb) irBasic.next();
			if (!sod.isOpen())
				continue;
			String val = sod.getValue();
			String otherVal = "";
			if (isClean && json!=null) {
				Iterator keys = json.keys();
			  	while (keys.hasNext()) {
			  		String key = (String)keys.next();
			  		String toVal = json.getString(key);
			  		if (val.equals(toVal)) {
			  			otherVal = key;
			  			break;
			  		}
			  	}
			}
%>
			<tr>
			  <td><%=sod.getName()%></td>
			  <td><%=sod.getValue()%></td>
			  <td><input basicId="<%=ff.getName() %>" name="<%=ff.getName()%>_<%=StrUtil.escape(sod.getValue())%>" basicVal="<%=sod.getValue() %>" value="<%=otherVal%>" onfocus="this.select()" /></td>
			</tr>
<%
		}
%>
	</table>
<%
		}
	}
}
%>
<input id="id" name="id" value="<%=id%>" type="hidden"/>
</form>
</body>
<script>
function form1_onsubmit() {
	if (o("job_name").value=="") {
		alert("名称不能为空！");
		o("job_name").focus();
		return false;
	}
	
	if (o("formCode").value=="") {
		alert("请选择表单！");
		return false;
	}
	
	if (o("dbSource").value=="") {
		alert("请选择数据源！");
		return false;
	}
	
	if (o("primaryKey").value=="") {
		alert("主键不能为空！");
		o("primaryKey").focus();
		return false;
	}
	
	var t = o("time").value;
	var ary = t.split(":");
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = o("month_day").value;
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
		o("cron").value = cron;
	}
	else if (kind=="1") {
		if (weekDay=="") {
			alert("请选择星期几！");
			return false;
		}
		if (dayOfMonth=="")
			dayOfMonth = "?";
		var cron = ary[2] + " " + ary[1] + " " + ary[0] + " ? * " + weekDay;
		o("cron").value = cron;
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
		o("cron").value = cron;
	}
	else if (kind=="3") {
		if (o("mycron").value=="") {
			alert("请输入cron表达式！");
			o("mycron").focus();
			return false;
		}
		o("cron").value = o("mycron").value;
	}
	
	o("data_map").value = "{\"formCode\":\"" + o("formCode").value + "\", \"dbSource\":\"" + o("dbSource").value + "\", \"table\":\"" + o("tables").value + "\", \"primaryKey\":\"" + o("primaryKey").value + "\", \"foreignKey\":\"" + o("foreignKey").value + "\"}";
	var dataMap = $.parseJSON(o("data_map").value);
	var jsonMap = {};
	for (i=0; i<fieldCount; i++) {
		if ($('#field' + i).val()!="") {
			jsonMap[$('#fieldName' + i).val()] = $('#field' + i).val();
		}
	}
	
	dataMap["fieldMap"] = jsonMap;
	
	var cleanAry = [];
	<%
	ir = fd.getFields().iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
			if (mu!=null && mu.getCode().equals("macro_flow_select")) {
				%>
				var fName = "<%=ff.getName()%>";
				if ($('#is_clean_' + fName).is(':checked')) {
					var cleanJson = {};
					cleanJson["fieldName"] = fName;
					$("input[basicId=" + fName + "]").each(function() {
						cleanJson[$(this).val()] = $(this).attr("basicVal");
					});
					cleanAry.push(cleanJson);
				}
				<%
			}
		}
	}
	%>
	dataMap["cleanMap"] = cleanAry;	
	o("data_map").value = JSON.stringify(dataMap);
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
			
			$("#month_day").val("");			
		}
		else if ($(this).val()=="3") {
			$("#div0").hide();
			$("#divInterval").hide();
			$("#divCron").show();
		}
	});
	
	<%if (!monthDay.equals("")) {%>
		$("#div0").show();
		$("#spanMon").show();
		$("#spanWeek").hide();
		$("#divInterval").hide();
		$("#divCron").hide();
		
		setRadioValue("kind", "0");
	<%}else if (!w[0].equals("?")) {%>
		$("#div0").show();
		$("#spanMon").hide();
		$("#spanWeek").show();
		$("#divInterval").hide();
		$("#divCron").hide();
		
		setRadioValue("kind", "1");
	<%}else if (!interval.equals("")) {%>
		$("#div0").hide();
		$("#spanMon").hide();
		$("#spanWeek").hide();
		$("#divInterval").show();
		$("#divCron").hide();
		
		setRadioValue("kind", "2");
	<%}%>
});
</script>
</html>