<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理流程权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
<%
String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
boolean isNav = tabIdOpener.equals("")?true:false;
if (isNav) {
%>
<%@ include file="flow_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<script>
o("menu6").className="current";
</script>
<%}%>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
Leaf leaf = new Leaf();
leaf = leaf.getLeaf(dirCode);
%>
<form name="formRole" method="post" action="flow_dir_priv_m.jsp?op=setrole">
<table class="tabStyle_1 percent80" cellspacing="0" cellpadding="3" width="50%" align="center">
    <tbody>
      <tr>
        <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
      </tr>
<%
RoleMgr roleMgr = new RoleMgr();		
LeafPriv lp = new LeafPriv();
Vector vrole = lp.getRolesOfLeafPriv(leaf.getCode());

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
        <td align="center"><textarea name="roleDescs" cols="60" rows="3"><%=descs%></textarea>
            <input name="roleCodes" value="<%=roleCodes%>" type=hidden />
            <input name="dirCode" value="<%=dirCode%>" type=hidden />
            <input name="tabIdOpener" value="<%=tabIdOpener%>" type="hidden" />
            </td>
      </tr>
      <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px"><input type="button" class="btn" onclick="openWin('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>&unitCode=<%=unitCode%>', 800, 600)" value="选择" />
          &nbsp;&nbsp;&nbsp;&nbsp;
          <input type="submit" class="btn" value="确定" /></td>
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
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="50%" align="center">
  <tbody>
    <tr>
      <td style="display:none" class="tabStyle_1_title" noWrap width="26%">用户组名称</td>
      <td class="tabStyle_1_title" noWrap width="40%">描述</td>
      <td width="34%" noWrap class="tabStyle_1_title">操作</td>
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
	  <a href="flow_dir_priv_m.jsp?op=add&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=LeafPriv.TYPE_USERGROUP%>&tabIdOpener=<%=tabIdOpener%>">[ 添加 ]</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent80" width="453"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="453" class="tabStyle_1_title">添加用户</td>
  </tr>
  <form name="form1" action="flow_dir_priv_m.jsp?op=add" method=post>
  <tr>
    <td height="25" align="center">
	用户名：
	  <input name="userRealName" value="" readonly>
	  <input name="name" value="" type="hidden"><input type=hidden name=type value=1>
	  <input type=hidden name=dirCode value="<%=leaf.getCode()%>">
      <input name="tabIdOpener" value="<%=tabIdOpener%>" type="hidden" />
	  <input class="btn" type="button" onclick="openWinUsers()" value="选择" />
	  &nbsp;
	<INPUT class="btn" type=submit value="确定" />
	&nbsp;</td>
  </tr></form>
</table>
<div style="text-align:center"><input type="button" class="btn" value="返回" onclick="window.history.back()" /></div>
<br>
</body>
</html>