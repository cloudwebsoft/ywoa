<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.exam.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理文件柜权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery.bgiframe.js"></script>
<script>
var selUserNames = "";
var selUserRealNames = "";

function getSelUserNames() {
	return selUserNames;
}

function getSelUserRealNames() {
	return selUserRealNames;
}

function setUsers(user, userRealName) {
	form1.name.value = user;
	form1.userRealName.value = userRealName;
}

function setPerson(deptCode, deptName, user, userRealName)
{
	form1.name.value = user;
	form1.userRealName.value = userRealName;
}

function setRoles(roles, descs) {
	formRole.roleCodes.value = roles;
	formRole.roleDescs.value = descs
}

function openWinUsers() {
	selUserNames = form1.name.value;
	selUserRealNames = form1.userRealName.value;
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;');
}

</script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String majorCode = ParamUtil.get(request, "majorCode");
TreeSelectDb tsd = new TreeSelectDb();
tsd =tsd.getTreeSelectDb(majorCode);
//if(!tsd.getParentCode().equals(MajorView.ROOT_CODE)){
	//out.print(StrUtil.jAlert_Back("请在上级节点添加权限","提示"));
	//return;
//}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">设置目录 <a href="dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(majorCode)%>"><%=tsd.getName()%></a> 权限</td>
    </tr>
  </tbody>
</table>
<br/>
<form id="formRole" name="formRole" method="post" action="">
	<table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" width="50%" align="center">
	    <tbody>
	      <tr>
	        <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
	      </tr>
	      <%
		RoleMgr roleMgr = new RoleMgr();		
		MajorPriv mp = new MajorPriv();
		Vector vrole = mp.getRolesOfLeafPriv(majorCode);
		String roleCode;
		String roleCodes = "";
		String descs = "";
		Iterator irrole = vrole.iterator();
		while (irrole.hasNext()) {
			RoleDb rd = (RoleDb)irrole.next();
			roleCode = rd.getCode();
			if (roleCodes.equals(""))
				roleCodes += roleCode;
			else
				roleCodes += "," + roleCode;
			if (descs.equals(""))
				descs += rd.getDesc();
			else
				descs += "," + rd.getDesc();
		}
	%>
	      <tr class="row" style="BACKGROUND-COLOR: #ffffff">
	        <td align="center"><textarea name=roleDescs cols="45" rows="3" style="width:100%"><%=descs%></textarea>
	            <input name="roleCodes" value="<%=roleCodes%>" type="hidden"/>
	            <input name="majorCode" value="<%=majorCode%>" type="hidden"/></td>
	      </tr>
	      <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
	        <td style="PADDING-LEFT: 10px"><input type="button" class="btn" onClick="showModalDialog('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择"/>
	          &nbsp;&nbsp;&nbsp;&nbsp;
	          <input type="button" class="btn" onclick="setRole()" value="确定"/>
	          &nbsp;&nbsp;&nbsp;&nbsp;
	          <input type="button" class="btn" value="返回" onclick="history.back()"/>
	          </td>
	      </tr>
	    </tbody>
	</table>
</form>
<br/>
<form id="form1" name="form1" action="" method=post>
	<table class="tabStyle_1 percent60" width="492"  border="0" align="center" cellpadding="0" cellspacing="0">
	  <tr>
	    <td class="tabStyle_1_title">添加用户</td>
	  </tr>
	  <tr>
	    <td height="25" align="center">
		用户名：
		  <textarea name="userRealName" style="width:100%;height:100px" readonly></textarea>
		  <input name="name" value="" type="hidden" />
		  <input type=hidden name=type value=0 />
		  <input type=hidden name="majorCode" value="<%=majorCode%>"/>
		  &nbsp;&nbsp;<input class="btn" onClick="openWinUsers()" value="选择" type="button" />
		  &nbsp;&nbsp;
	<input class="btn" type="button" align="middle" onclick="addUser()" value="确定" /></td>
	  </tr>
	</table>
</form>
<br/>

<script >
function ajaxPost(path,parameter,func){
	$.ajax({
		type: "post",
		url: path,
		data: parameter,
		dataType: "html",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",			
		success: function(data, status){
			func(data);
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});
}
function addUser(){
	var formData = $("#form1").serialize();
	var majorCode = "<%=majorCode%>";
	ajaxPost('../majorPriv/majorUserAdd.do',formData,function(data){
		data = $.parseJSON(data);
		if(data.ret=="1"){
			jAlert_Redirect(data.msg,"提示","major_priv_manage.jsp?majorCode="+majorCode);
		}else if(data.ret=="0"){
			jAlert(data.msg,"提示");
		}
	});
}
function setRole(){
	var formData = $("#formRole").serialize();
	var majorCode = "<%=majorCode%>";
	ajaxPost('../majorPriv/majorRoleAdd.do',formData,function(data){
		data = $.parseJSON(data);
		if(data.ret=="1"){
			jAlert_Redirect(data.msg,"提示","major_priv_manage.jsp?majorCode="+majorCode);
		}else if(data.ret=="0"){
			jAlert(data.msg,"提示");
		}
	});
}
</script>
</body>
</html>