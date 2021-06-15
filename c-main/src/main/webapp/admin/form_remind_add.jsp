<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.sql.*"%>
<%
String rootpath = request.getContextPath();
String op = ParamUtil.get(request, "op");
String code = ParamUtil.get(request, "code");
if (op.equals("getFields")) {
	String tableName = ParamUtil.get(request, "tableName");
	
	String sql = "select * from " + tableName;
	String dbSource = Global.getDefaultDB();
	com.cloudwebsoft.framework.db.Connection conn = new com.cloudwebsoft.framework.db.Connection(dbSource);
	try {
		conn.setMaxRows(1); //尽量减少内存的使用
		ResultSet rs = conn.executeQuery(sql);
		ResultSetMetaData rm = rs.getMetaData();
		int colCount = rm.getColumnCount();
		
		String fieldName = "";
		FormDb fd = new FormDb();
		
		if (tableName.toUpperCase().startsWith("FORM_TABLE_")) {
			String formCode = FormDb.getCodeByTableName(tableName);
			fd = fd.getFormDb(formCode);
		}
		%>
		<option value="">请选择</option>
		<%
		for (int i = 1; i <= colCount; i++) {
			//System.out.println(rm.getColumnName(i));
			fieldName = rm.getColumnName(i);
			if (fd.isLoaded()) {
				FormField ff = fd.getFormField(fieldName);
				if (ff!=null)
					fieldName = ff.getTitle();
			}
			%>
			<option value="<%=rm.getColumnName(i)%>"><%=fieldName%></option>
			<%
		}
	}
	finally {
		conn.close();
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>编辑表单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<style>
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
</style>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<div class="spacerH"></div>
<%
if (op.equals("add")) {
	FormRemindDb frd = new FormRemindDb();
	boolean re = false;
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		FormRemindMgr frm = new FormRemindMgr();
		
		re = frm.create(request, frd, "form_remind_create");
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_remind_list.jsp?code=" + code));
		}
		else {
			out.print(StrUtil.jAlert_Back("操作失败！","提示"));
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	return;
}
%>
<form action="form_remind_add.jsp" id="form1" name="form1" method="post">
  <table width="98%"  border="0" cellpadding="5" cellspacing="0" class="tabStyle_1 percent80">
    <tr>
      <td colspan="2" class="tabStyle_1_title">添加提醒</td>
    </tr>
    <tr>
      <td width="16%" >名称</td>
      <td width="84%" >
      <input id="name" name="name" />
      <input name="op" value="add" type="hidden" />
      <input name="code" value="<%=code%>" type="hidden" />
      </td>
    </tr>
    <tr>
      <td >角色</td>
      <td >
        <textarea name=roleDescs cols="40" rows="3"></textarea>
        <input name="roles" type=hidden />      
      	<br />
        <input name="button2" class="btn" type="button" onClick="showModalDialog('../role_multi_sel.jsp?roleCodes=' + o('roles').value + '&unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择角色">
      </td>
    </tr>
    <tr>
      <td >用户</td>
      <td >
      <input name="users" id="users" type="hidden">
      <textarea name="userRealNames" cols="60" rows="5" readonly="readonly" wrap="yes" id="userRealNames"></textarea>
        <br />
        <input class="btn" title="添加收件人" onClick="openWinUsers()" type="button" value="添 加" name="button" />
&nbsp;
<input class="btn" title="清空收件人" onClick="o('users').value=''; o('userRealNames').value='';" type="button" value="清 空" name="button" /></td>
    </tr>
    <tr>
      <td >表名</td>
      <td >
      <select id="table_name" name="table_name">
      <option value="">请选择</option>
<%
	JdbcTemplate jt = new JdbcTemplate();
	java.sql.Connection conn = jt.getConnection().getCon();
	DatabaseMetaData metaData = conn.getMetaData();
	FormDb fd = new FormDb();

	if (Global.db.equals(Global.DB_MYSQL)) {
		ResultSet rs = metaData.getTables(conn.getCatalog(), "root", null, new String[]{"TABLE"});
		
		while(rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			if (!tableName.toUpperCase().endsWith("_LOG")) {
				%>
				<option value="<%=tableName%>">
				<%=tableName%>
                <%
				if (tableName.toUpperCase().startsWith("FORM_TABLE_")) {
					String formCode = FormDb.getCodeByTableName(tableName);
					fd = fd.getFormDb(formCode);
					if (fd.isLoaded()) {
						out.print("(" + fd.getName() + ")");
					}
				}%>
                </option>
				<%
			}
		}
	}
	else if (Global.db.equals(Global.DB_ORACLE)) {
		ResultSet rs = metaData.getTables(conn.getCatalog(), "SCOTT", null, new String[]{"TABLE"});
		while(rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			if (!tableName.toUpperCase().endsWith("_LOG")) {
				%>
				<option value="<%=tableName%>"><%=tableName%></option>
				<%
			}
		}
	}
	else {
		ResultSet rs = metaData.getTables(conn.getCatalog(), "SCOTT", null, new String[]{"TABLE"});
		while(rs.next()) {
			String tableName = rs.getString("TABLE_NAME");
			if (!tableName.toUpperCase().endsWith("_LOG")) {
				%>
				<option value="<%=tableName%>"><%=tableName%></option>
				<%
			}
		}
	}
%>      
		</select>
      </td>
    </tr>
    <tr>
      <td >日期字段</td>
      <td >
      <select id="date_field" name="date_field">
      </select>
      </td>
    </tr>
    <tr>
      <td >表单中的用户</td>
      <td >
      <select id="user_field" name="user_field">
      </select>
      </td>
    </tr>
    <tr>
      <td >条件</td>
      <td >
        <textarea id="filter" name="filter" style="width:98%; height:150px"></textarea>
        <br />
        (注：条件不能以where及and开头，可以直接输入条件，也可以使用脚本，脚本中必须返回ret，ret中是条件语句)
        <input type="button" value="设计器" class="btn" onclick="openIdeWin()" />
      </td>      
      </td>
    </tr>    
    <tr>
      <td >提醒方式</td>
      <td >
      <select id="kind" name="kind">
      <option value="<%=FormRemindDb.KIND_EXPIRE%>">到期提醒</option>
      <option value="<%=FormRemindDb.KIND_EVERY_YEAR%>">周年提醒</option>
      </select>
      </td>
    </tr>
    <tr>
      <td >提前时间</td>
      <td >
      <input name="ahead_day" size="3" />天
      <input name="ahead_hour" size="3" />小时
      <input name="ahead_minute" size="3" />分钟
      </td>
    </tr>
    <tr>
      <td >标题</td>
      <td ><input id="title" name="title" />可以用{$字段名}来引用表单中的字段
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
      </td>
    </tr>
    <tr>
      <td >内容</td>
      <td >
      <textarea name="content" style="width:500px; height:150px"></textarea>可以用{$字段名}来引用表单中的字段
      </td>
    </tr>
    <tr>
      <td >方式</td>
      <td >
		  <%
          boolean isSmsUsed = SMSFactory.isUseSMS();
          %>
          <input name="is_msg" type="checkbox" value="1" checked /> 消息
          &nbsp;&nbsp;
          <input name="is_email" type="checkbox" value="1" checked /> 邮件
          &nbsp;&nbsp;
          <span style="<%=isSmsUsed?"":"display:none" %>">
          <input name="is_sms" type="checkbox" value="1" checked /> 短信
          </span>      
      </td>
    </tr>
	<tr>
      <td colspan="2" align="center" >
      <input class="btn" type="submit" name="next" value="确定" />
      &nbsp;&nbsp;
      <input class="btn" type="button" value="返回" onClick="window.history.back()" />
      </td>
    </tr>
  </table>
</form>
<br />
</body>
<script>
function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
}

function getSelUserNames() {
	return o("users").value;
}

function getSelUserRealNames() {
	return o("userRealNames").value;
}

function setUsers(users, userRealNames) {
	o("users").value = users;
	o("userRealNames").value = userRealNames;
}


function setRoles(roles, descs) {
	o("roles").value = roles;
	o("roleDescs").value = descs
}

<%
	FormRemindDb frd = new FormRemindDb();

	ParamConfig pc = new ParamConfig(frd.getTable().getFormValidatorFile()); // "form_rule.xml");
	ParamChecker pck = new ParamChecker(request);
	out.print(pck.doGetCheckJS(pc.getFormRule("form_remind_create")));
%>

$('#table_name').change(function() {
	if ($(this).val()=="")
		return;
	// 取所选数据源的表名
	var str = "op=getFields&tableName=" + $(this).val();
	var myAjax = new cwAjax.Request(
		"form_remind_add.jsp",
		{ 
			method:"post",
			parameters:str,
			onComplete:doGetFields,
			onError:errFunc
		}
	);
});

$(function() {
	<%if (!code.equals("")) {%>
	$('#table_name').val("<%=FormDb.getTableName(code)%>");
	$('#table_name').trigger('change');
	<%}%>
});

var errFunc = function(response) {
	jAlert(response.responseText,"提示");
	// window.status = response.responseText;
}

function doGetFields(response) {
	var rsp = response.responseText.trim();
	// alert(rsp);
	// $("#fieldsDiv").html(rsp);
	
	$("#date_field").empty();
	$("#date_field").append(rsp);
	
	$("#user_field").empty();
	$("#user_field").append(rsp);
}

function getScript() {
	return $('#filter').val();
}

function setScript(script) {
	$('#filter').val(script);
}

<%
    com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
    com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
    String version = StrUtil.getNullStr(oaCfg.get("version"));
    String spVersion = StrUtil.getNullStr(spCfg.get("version"));
%>
var ideUrl = "script_frame.jsp";
var ideWin;
var cwsToken = "";

function openIdeWin() {
    ideWin = openWinMax(ideUrl);
}

var onMessage = function(e) {
    var d = e.data;
    var data = d.data;
    var type = d.type;
    if (type == "setScript") {
        setScript(data);
        if (d.cwsToken!=null) {
            cwsToken = d.cwsToken;
            ideUrl = "script_frame.jsp?cwsToken=" + cwsToken;
        }
    } else if (type == "getScript") {
        var data = {
            "type": "openerScript",
            "version": "<%=version%>",
            "spVersion": "<%=spVersion%>",
            "scene": "form.remind",
            "data": getScript()
        };
        ideWin.leftFrame.postMessage(data, '*');
    } else if (type == "setCwsToken") {
        cwsToken = d.cwsToken;
        ideUrl = "script_frame.jsp?cwsToken=" + cwsToken;
    }
};

$(function() {
     if (window.addEventListener) { // all browsers except IE before version 9
         window.addEventListener("message", onMessage, false);
     } else {
         if (window.attachEvent) { // IE before version 9
             window.attachEvent("onmessage", onMessage);
         }
     }
});
</script>
</html>
