<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.Privilege"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>查询授权</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language="JavaScript">
function findObj(theObj, theDoc) {
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

var selUserNames = "";
var selUserRealNames = "";
function getSelUserNames() {
	return selUserNames;
}
function setRoles(str,strText){
	formRole.roleDescs.value = strText;
	formRole.users.value = str;
}
function getSelUserRealNames() {
	return selUserRealNames;
}

var doWhat = "";

function openWinUsers() {
	doWhat = "users";
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	window.open("../user_multi_sel.jsp","_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width=640,height=480");
	
}

function setUsers(users, userRealNames) {
	if (doWhat=="users") {
		form1.users.value = users;
		form1.userRealNames.value = userRealNames;
	}
	if (doWhat=="principal") {
		form1.principal.value = users;
		form1.principalRealNames.value = userRealNames;
	}
}

function openWinPrincipal() {
	doWhat = "principal";
	selUserNames = form1.principal.value;
	selUserRealNames = form1.principalRealNames.value;
	window.open("../user_multi_sel.jsp","_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width=500,height=400");
}

function openRoleWin(roleCodes,unitCode){
	window.open("../role_multi_sel.jsp?roleCodes="+roleCodes+"&unitCode="+unitCode,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width=526,height=435");
}

function openWinDepts(formDept) {
	window.open('../dept_multi_sel.jsp?openType=open','_blank','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width=500,height=480')
}

function getDepts() {
	return formAdminDept.users.value;
}

function setDepts(formDept){
	var strdepts = "";
	var	strDeptNames = "";
	for (var i=0; i<formDept.length; i++) {
		strdepts += formDept[i][0] + "," ;
		strDeptNames += formDept[i][1] + "," ;
	}
	strdepts = strdepts.substring(0,strdepts.length-1); 
	strDeptNames = strDeptNames.substring(0,strDeptNames.length-1); 
	formAdminDept.users.value = strdepts;
	document.getElementById("deptNames").innerText = strDeptNames;
}
</script>
</HEAD>
<BODY>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
	String userRealNames = "",users = "";
	int id = ParamUtil.getInt(request, "id");
	
	FormQueryDb aqd = new FormQueryDb();			
	aqd = aqd.getFormQueryDb(id);
		
	String sql = FormSQLBuilder.getQueryPrivilege(id, FormQueryPrivilegeDb.TYPE_USER);
	FormQueryPrivilegeDb apd = new FormQueryPrivilegeDb();
	Vector vt = apd.list(sql);
	Iterator ir = null;
	ir = vt.iterator();
	while (ir != null && ir.hasNext()) {
		apd = (FormQueryPrivilegeDb) ir.next();
		if (users.equals("")) {
			users = apd.getUserName();
			UserDb ud = new UserDb();
			ud = ud.getUserDb(apd.getUserName());
			userRealNames = ud.getRealName();
		}
		else {
			users += "," + apd.getUserName();
			UserDb ud = new UserDb();
			ud = ud.getUserDb(apd.getUserName());
			userRealNames += "," + ud.getRealName();
		}
	}
  
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" valign="middle" class="tdStyle_1">查询授权&nbsp;-&nbsp;<%=aqd.getQueryName()%></td>
  </tr>
</table>
<form action="form_query_do.jsp?op=modifyQueryPriv" name="form1" method="post">
<table width="426" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
  <tr>
    <td align="center" class="tabStyle_1_title">授权给用户</td>
    </tr>
  <tr>
    <td align="center">
      <input name="users" id="users" type="hidden" value="<%=users%>">
      <textarea name="userRealNames" cols="45" rows="3" readOnly wrap="yes" id="userRealNames"><%=userRealNames%></textarea></td>
    </tr>
  <tr>
    <td align="center"><input class="btn" type="submit" value="确 定">
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input type="hidden" name="id" value="<%=id%>">
      <input type="hidden" name="type" value="<%=FormQueryPrivilegeDb.TYPE_USER%>">
      <input class="btn" onclick="openWinUsers()" type="button" value="选择用户" name="button2" />
      &nbsp;&nbsp;&nbsp;&nbsp;
      <input class="btn" onclick="form1.users.value='';form1.userRealNames.value=''" type="button" value="清 空" name="button" />
      </td>
    </tr>
</table>
</form>
<form action="form_query_do.jsp?op=modifyQueryPriv" method="post" name="formRole" id="formRole">
<table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" align="center">
    <tbody>
      <tr>
        <td align="left" nowrap="nowrap" class="tabStyle_1_title">授权给角色</td>
      </tr>
      <%
RoleMgr roleMgr = new RoleMgr();  
String descs = "";
String roleCodes = "";

sql = FormSQLBuilder.getQueryPrivilege(id, FormQueryPrivilegeDb.TYPE_ROLE);
ir = apd.list(sql).iterator();
while (ir.hasNext()) {
	apd = (FormQueryPrivilegeDb) ir.next();
	RoleDb rd = roleMgr.getRoleDb(apd.getUserName());
	if (descs.equals("")) {
		roleCodes = rd.getCode();
		descs = rd.getDesc();
	}
	else {
		roleCodes += "," + rd.getCode();
		descs += "," + rd.getDesc();
	}
}	
%>
      <tr>
        <td align="center"><textarea name="roleDescs" cols="45" rows="3"><%=descs%></textarea>
            <input name="users" value="<%=roleCodes%>" type="hidden" /></td>
      </tr>
      <tr align="center">
        <td><input name="Submit3" type="submit" class="btn" value="确 定" />
        <input type="hidden" name="id" value="<%=id%>" />
        <input type="hidden" name="type" value="<%=FormQueryPrivilegeDb.TYPE_ROLE%>" />
          &nbsp;
          <input name="button22" type="button" class="btn" onclick="openRoleWin('<%=roleCodes%>','<%=privilege.getUserUnitCode(request)%>')" value="选择角色" />
&nbsp;&nbsp;&nbsp;&nbsp;        </td>
      </tr>
    </tbody>
</table>
</form>
<%
String strdepts = "", strDeptNames = "";
com.redmoon.oa.dept.DeptDb ddb = new com.redmoon.oa.dept.DeptDb();	

sql = FormSQLBuilder.getQueryPrivilege(id, FormQueryPrivilegeDb.TYPE_DEPT);
ir = apd.list(sql).iterator();
while (ir.hasNext()) {
	apd = (FormQueryPrivilegeDb) ir.next();
	
	if (strdepts.equals("")) {
		strdepts = ddb.getDeptDb(apd.getUserName()).getCode();
		strDeptNames = ddb.getDeptDb(apd.getUserName()).getName();
	}
	else {
		strdepts += "," + ddb.getDeptDb(apd.getUserName()).getCode();
		strDeptNames += "," + ddb.getDeptDb(apd.getUserName()).getName();
	}
}
%>
<form action="form_query_do.jsp?op=modifyQueryPriv" method="post" name="formAdminDept" id="formAdminDept">
<table class="tabStyle_1 percent60" border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td align="center" class="tabStyle_1_title">授权给部门</td>
    </tr>
    <tr>
      <td align="center">
        <input type="hidden" name="users" value="<%=strdepts%>" />
        <input type="hidden" name="id" value="<%=id%>" />
        <input type="hidden" name="type" value="<%=FormQueryPrivilegeDb.TYPE_DEPT%>" />
        <textarea name="textarea" cols="45" rows="3" readonly wrap="yes" id="deptNames"><%=strDeptNames%></textarea>      </td>
    </tr>
    <tr>
      <td align="center"><input class="btn" name="submit2" type="submit" value="确 定" />
        &nbsp;&nbsp;
        &nbsp;
        <input class="btn" title="添加部门" onclick="openWinDepts(formAdminDept)" type="button" value="选择部门" name="button3" /></td></tr>
</table>
</form>
<br />
</BODY>
</HTML>
