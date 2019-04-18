<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if ("check".equals(op)) {
	String name = ParamUtil.get(request, "name");
	JSONObject json = new JSONObject();
	AccountDb ad = new AccountDb();	
	if (ad.isExist(name)) {
		json.put("ret", "1");
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("msg", str);
	}
	else {
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("msg", str);
	}	
	out.print(json);
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>添加工号</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  
<%
if (op.equals("add")) {
	AccountMgr am = new AccountMgr();
	boolean re = false;
	try {
		  re = am.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert_Redirect("操作成功！", "account_add.jsp"));
}
%>
<script>
function setPerson(deptCode, deptName, user, userRealName){
	form1.person.value = userRealName;
	form1.userName.value = user;
}
</script>
<style type="text/css">
<!--
.STYLE5 {color: #FF0000}
-->
</style>
</head>
<body>
<%@ include file="account_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<form name="form1" action="?op=add" method="post">
<TABLE align="center" class="tabStyle_1 percent80">
    <TBODY>
      <TR>
        <TD colspan="2" align="left" class="tabStyle_1_title">添加工号</TD>
      </TR>
      <TR>
        <TD height="26" align="right">工号：</TD>
        <td><input name="name" id="accName" maxlength="255" onchange="checkName()" />
        <span id="spanNote"></span>
        <span class="STYLE5"> *</span> </TD>
      </TR>
      <TR>
        <TD height="26" align="right">姓名：</TD>
        <td><input name="person" type="text" id="person" size="20" readonly>&nbsp;<a href="#" onClick="javascript:showModalDialog('../user_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">选择用户</a><input type="hidden" name="userName"></td>
      </TR>
      <TR>
        <TD height="30" colspan="2" align="center"><input name="button" type="submit" class="btn"  value="确定 ">
          &nbsp;&nbsp;&nbsp;&nbsp;
        <input name="button2" type="button" class="btn"  value="返回" onClick="window.location.href='account_list.jsp'"></TD>
      </TR>
    </TBODY>
</TABLE>
</form>
<script>
function checkName() {
	$.ajax({
		type: "post",
		url: "account_add.jsp",
		data: {
		    op: "check",
        	name: $('#accName').val()
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		complete: function(XMLHttpRequest, status){
		},
		success: function(data, status){
			var re = $.parseJSON(data);
			if (re.ret=="1") {
				$('#spanNote').html("<span style='color:red'>工号已存在</span>");				
			}
			else {
				$('#spanNote').html("");			
			}
		},
		error: function() {
			jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		}
	});	
}
</script>
</body>
</html>
