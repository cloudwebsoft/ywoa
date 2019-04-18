<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.sql.*"%>
<%
String rootpath = request.getContextPath();
String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>编辑表单</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<div class="spacerH"></div>
<%
int id = ParamUtil.getInt(request, "id");
FormRemindDb frd = new FormRemindDb();
frd = (FormRemindDb)frd.getQObjectDb(new Integer(id));

if (op.equals("edit")) {
	boolean re = false;
	try {
		FormRemindMgr frm = new FormRemindMgr();
		re = frm.save(request, frd, "form_remind_edit");
		if (re) {
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "form_remind_edit.jsp?id=" + id));
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
<form action="form_remind_edit.jsp" id="form1" name="form1" method="post">
  <table width="98%"  border="0" cellpadding="5" cellspacing="0" class="tabStyle_1 percent80">
    <tr>
      <td colspan="2" class="tabStyle_1_title">编辑提醒</td>
    </tr>
    <tr>
      <td width="16%" >名称</td>
      <td width="84%" >
      <input id="name" name="name" value="<%=frd.getString("name")%>" />
      <input name="op" value="edit" type="hidden" />
      <input name="id" value="<%=id%>" type="hidden" />
      </td>
    </tr>
    <tr>
      <td >角色</td>
      <td >
      <%
		RoleMgr roleMgr = new RoleMgr();		  
		String[] roleAry = StrUtil.split(frd.getString("roles"), ",");
		int ulen = 0;
		if (roleAry!=null)
			ulen = roleAry.length;
		
		String roleCode, desc;
		String roleCodes = "";
		String descs = "";
		RoleDb rd = new RoleDb();
		for (int i=0; i<ulen-1; i++) {
			rd = rd.getRoleDb(roleAry[i]);
			roleCode = rd.getCode();
			desc = rd.getDesc();
			if (roleCodes.equals(""))
				roleCodes += roleCode;
			else
				roleCodes += "," + roleCode;
			if (descs.equals(""))
				descs += desc;
			else
				descs += "," + desc;
		}			  
	  %>
        <textarea name=roleDescs cols="40" rows="3"><%=descs%></textarea>
        <input name="roles" type=hidden value="<%=roleCodes%>" />      
      	<br />
        <input name="button2" class="btn" type="button" onClick="showModalDialog('../role_multi_sel.jsp?roleCodes=' + o('roles').value + '&unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择角色">
      </td>
    </tr>
    <tr>
      <td >用户</td>
      <td >
		<%
		  String users = "", userRealNames="";
		  String[] aryusers = StrUtil.split(frd.getString("users"), ",");
		  if (aryusers!=null) {
			int len = aryusers.length;
			for (int i=0; i<len; i++) {
				if (users.equals("")) {
					users = aryusers[i];
					UserDb ud = new UserDb();
					ud = ud.getUserDb(aryusers[i]);
					// System.out.print(getClass() + " aryusers=" + aryusers + " aryusers[" + i + "]=" + aryusers[i]);
					userRealNames = ud.getRealName();
				}
				else {
					users += "," + aryusers[i];
					UserDb ud = new UserDb();
					ud = ud.getUserDb(aryusers[i]);
					userRealNames += "," + ud.getRealName();
				}
			}
		  }		
		%>              
      <input name="users" id="users" type="hidden" value="<%=users%>">
      <textarea name="userRealNames" cols="60" rows="5" readonly="readonly" wrap="yes" id="userRealNames"><%=userRealNames%></textarea>
        <br />
        <input class="btn" title="添加收件人" onclick="openWinUsers()" type="button" value="添 加" name="button" />
        &nbsp;
        <input class="btn" title="清空收件人" onclick="o('users').value=''; o('userRealNames').value='';" type="button" value="清 空" name="button" /></td>
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
        <script>
		o("table_name").value = "<%=frd.getString("table_name")%>";
		</script>
      </td>
    </tr>
    <tr>
      <td >日期字段</td>
      <td >
      <select id="date_field" name="date_field">
      </select>
	  <script>
      o("table_name").value = "<%=frd.getString("table_name")%>";
      </script>
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
      <textarea id="filter" name="filter" style="width:98%; height:150px"><%=StrUtil.HtmlEncode(StrUtil.getNullStr(frd.getString("filter")))%></textarea>
      <br />
      (注：条件不能以where及and开头，可以直接输入条件，也可以使用脚本，脚本中必须返回ret，ret中是条件语句)      <input type="button" value="设计器" class="btn" onclick="ideWin = openWin('../admin/script_frame.jsp', screen.width, screen.height);" />
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
	  <script>
      o("kind").value = "<%=frd.getInt("kind")%>";
      </script>
      </td>
    </tr>
    <tr>
      <td >提前时间</td>
      <td >
      <input name="ahead_day" size="3" value="<%=frd.getInt("ahead_day")%>" />天
      <input name="ahead_hour" size="3" value="<%=frd.getInt("ahead_hour")%>" />小时
      <input name="ahead_minute" size="3" value="<%=frd.getInt("ahead_minute")%>" />分钟
      </td>
    </tr>
    <tr>
      <td >标题</td>
      <td ><input id="title" name="title" value="<%=frd.getString("title")%>" />可以用{$字段名}来引用表单中的字段
      <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" />
      </td>
    </tr>
    <tr>
      <td >内容</td>
      <td >
      <textarea name="content" style="width:500px; height:150px"><%=frd.getString("content")%></textarea>可以用{$字段名}来引用表单中的字段
      </td>
    </tr>
    <tr>
      <td >方式</td>
      <td >
	  	<%
        boolean isSmsUsed = SMSFactory.isUseSMS();
        %>
        <input name="is_msg" type="checkbox" value="1" <%=frd.getInt("is_msg")==1?"checked":""%> />
        消息
        &nbsp;&nbsp;
        <input name="is_email" type="checkbox" value="1" <%=frd.getInt("is_email")==1?"checked":""%> />
        邮件
        &nbsp;&nbsp; <span style="<%=isSmsUsed?"":"display:none" %>">
        <input name="is_sms" type="checkbox" value="1" <%=frd.getInt("is_sms")==1?"checked":""%> />
        短信 </span></td>
    </tr>
	<tr>
      <td colspan="2" align="center" ><input class="btn" type="submit" name="next" value="确定" /></td>
    </tr>
  </table>
</form>
<br />
</body>
<script>
function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
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
	ParamConfig pc = new ParamConfig(frd.getTable().getFormValidatorFile()); // "form_rule.xml");
	ParamChecker pck = new ParamChecker(request);
	out.print(pck.doGetCheckJS(pc.getFormRule("form_remind_edit")));
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
	var str = "op=getFields&tableName=<%=frd.getString("table_name")%>";
	var myAjax = new cwAjax.Request(
		"form_remind_add.jsp",
		{ 
			method:"post",
			parameters:str,
			onComplete:doGetFieldsOnload,
			onError:errFunc
		}
	);
});

var errFunc = function(response) {
	jAlert(response.responseText,"提示");
}

function doGetFieldsOnload(response) {
	var rsp = response.responseText.trim();
	$("#date_field").empty();
	$("#date_field").append(rsp);
	$("#date_field").val("<%=frd.getString("date_field")%>");
	
	$("#user_field").empty();
	$("#user_field").append(rsp);	
	$("#user_field").val("<%=frd.getString("user_field")%>");
}

function doGetFields(response) {
	var rsp = response.responseText.trim();
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

var ideWin;
var onMessage = function(e) {
	var d = e.data;
	var data = d.data;
	var type = d.type;
	if (type=="setScript") {
		setScript(data);
	}
	else if (type=="getScript") {
		var data={
		    "type":"openerScript",
		    "version":"<%=version%>",
		    "spVersion":"<%=spVersion%>",
		    "scene": "form.remind",			
		    "data":getScript()
	    }
		ideWin.mainScriptFrame.postMessage(data, '*');
	}
}

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
