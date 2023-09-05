<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理流程权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
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

function setPerson(deptCode, deptName, user, userRealName) {
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
	openWin('../user_multi_sel.jsp', 800, 600);
}
</script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int kindId = ParamUtil.getInt(request, "kindId");
String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
SelectKindDb leaf = new SelectKindDb();
leaf = leaf.getSelectKindDb(kindId);
%>
<form name="formRole" method="post" action="basic_select_kind_priv_m.jsp?op=setrole">
<table class="tabStyle_1 percent80" cellspacing="0" cellpadding="3" width="50%" align="center">
    <tbody>
      <tr>
        <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
      </tr>
<%
RoleMgr roleMgr = new RoleMgr();		
SelectKindPriv lp = new SelectKindPriv();
Vector vrole = lp.getRolesOfSelectKindPriv(kindId);

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

String unitCode = privilege.getUserUnitCode(request);
%>
      <tr class="row" style="BACKGROUND-COLOR: #ffffff">
        <td align="center"><textarea name=roleDescs cols="60" rows="3"><%=descs%></textarea>
            <input name="roleCodes" value="<%=roleCodes%>" type=hidden />
            <input name="kindId" value="<%=kindId%>" type=hidden />
		    <input type=hidden name="tabIdOpener" value="<%=tabIdOpener%>" />        
        </td>
      </tr>
      <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px">
            <input type="button" class="btn btn-defualt" onclick="openWin('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>&unitCode=<%=unitCode%>', 800, 600)" value="选择" />
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input type="submit" class="btn btn-defualt" value="确定" />
            &nbsp;&nbsp;&nbsp;&nbsp;
            <input type="button" class="btn btn-defualt" value="返回" onclick="window.history.back()" />
        </td>
      </tr>
    </tbody>
</table>
</form>
<%
String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.getUserGroupsOfUnit(unitCode);
Iterator ir = result.iterator();
%>
<br>
<table class="tabStyle_1 percent80" cellspacing="0" cellpadding="3" width="50%" align="center">
  <tbody>
    <tr>
      <td style="display:none" class="tabStyle_1_title" nowrap width="26%">用户组名称</td>
      <td class="tabStyle_1_title" nowrap width="40%">描述</td>
      <td width="34%" nowrap class="tabStyle_1_title">操作</td>
    </tr>
<%
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="highlight">
      <td style="display:none"><%=code%></td>
      <td><%=desc%></td>
      <td style="text-align:center">
	  <a href="basic_select_kind_priv_m.jsp?op=add&kindId=<%=kindId %>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=SelectKindPriv.TYPE_USERGROUP%>&tabIdOpener=<%=tabIdOpener%>">[ 添加 ]</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<br />
<form name="form1" action="basic_select_kind_priv_m.jsp?op=add" method="post">
<table class="tabStyle_1 percent80" width="453"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="453" class="tabStyle_1_title">添加用户</td>
  </tr>
  <tr>
    <td height="25" align="center">
	用户名：
	  <input name="userRealName" value="" readonly />
	  <input name="name" value="" type="hidden" />
	  <input type=hidden name=type value=1 />
	  <input type=hidden name="kindId" value="<%=kindId%>" />
	  <input type=hidden name="tabIdOpener" value="<%=tabIdOpener%>" />
	  <input class="btn" type="button" onclick="openWinUsers()" value="选择" />
	  &nbsp;
	  <input class="btn" type=submit value="确定" />
	  &nbsp;</td>
  </tr>
</table>
</form>
<br>
</body>
</html>