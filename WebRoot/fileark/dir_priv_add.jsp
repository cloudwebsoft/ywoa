<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理文件柜权限</title>
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
	o("name").value = user;
	o("userRealName").value = userRealName;
}

function setPerson(deptCode, deptName, user, userRealName)
{
	o("name").value = user;
	o("userRealName").value = userRealName;
}

function setRoles(roles, descs) {
	o("roleCodes").value = roles;
	o("roleDescs").value = descs;
}

function openWinUsers() {
	selUserNames = o("name").value;
	selUserRealNames = o("userRealName").value;
	openWin('../user_multi_sel.jsp', 800, 600);
}
</script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
Leaf leaf = new Leaf();
leaf = leaf.getLeaf(dirCode);
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">设置目录 <a href="dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(dirCode)%>"><%=leaf.getName()%></a> 权限</td>
    </tr>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" width="50%" align="center">
  <form name="formRole" method="post" action="dir_priv_m.jsp?op=setrole">
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
%>
      <tr class="row" style="BACKGROUND-COLOR: #ffffff">
        <td align="center"><textarea name=roleDescs cols="45" rows="3" style="width:100%"><%=descs%></textarea>
            <input name="roleCodes" value="<%=roleCodes%>" type=hidden>
            <input name="dirCode" value="<%=dirCode%>" type=hidden></td>
      </tr>
      <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px"><input type="button" class="btn" onClick="openWin('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>', 526, 435)" value="选择">
          &nbsp;&nbsp;&nbsp;&nbsp;
          <input type="submit" class="btn" value="确定">
          &nbsp;&nbsp;&nbsp;&nbsp;
          <input type="button" class="btn" value="返回" onclick="history.back()">
          </td>
      </tr>
    </tbody>
  </form>
</table>
<%
String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
%>
<br>
<br>
<table class="tabStyle_1 percent60" cellSpacing="0" cellPadding="3" width="50%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="26%">用户组名称</td>
      <td class="tabStyle_1_title" noWrap width="53%">描述</td>
      <td width="21%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="highlight">
      <td><%=code%></td>
      <td><%=desc%></td>
      <td align="center">
	  <a href="dir_priv_m.jsp?op=add&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=LeafPriv.TYPE_USERGROUP%>">添加</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent60" width="492"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tabStyle_1_title">添加用户</td>
  </tr>
  <form name="form1" action="dir_priv_m.jsp?op=add" method=post>
  <tr>
    <td height="25" align="center">
	用户名：
	  <textarea name="userRealName" style="width:100%;height:100px" readonly></textarea>
	  <input name="name" value="" type="hidden"><input type=hidden name=type value=1>
	  <input type=hidden name=dirCode value="<%=leaf.getCode()%>">
	  &nbsp;&nbsp;<input class="btn" onClick="openWinUsers()" value="选择" type="button" />
	  &nbsp;&nbsp;
<input class="btn" type="submit" align="middle" value="确定" /></td>
  </tr></form>
</table>
<br>
</body>
</html>