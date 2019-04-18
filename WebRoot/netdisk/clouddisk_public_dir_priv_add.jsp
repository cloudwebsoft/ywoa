<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理文件柜权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script>
function setPerson(deptCode, deptName, user, userRealName)
{
	form1.name.value = user;
	form1.userRealName.value = userRealName;
}

function setRoles(roles, descs) {
	formRole.roleCodes.value = roles;
	formRole.roleDescs.value = descs
}
</script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
PublicLeaf leaf = new PublicLeaf();
leaf = leaf.getLeaf(dirCode);
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">设置目录 <a href="netdisk_public_dir_priv_m.jsp?dirCode=<%=StrUtil.UrlEncode(dirCode)%>"><%=leaf.getName()%></a> 权限</td>
    </tr>
  </tbody>
</table>
<br>
<form name="formRole" method="post" action="netdisk_public_dir_priv_m.jsp?op=setrole">
<table class="tabStyle_1 percent80" cellspacing="0" cellpadding="3" width="50%" align="center">
    <tbody>
      <tr>
        <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
      </tr>
      <%
RoleMgr roleMgr = new RoleMgr();		
PublicLeafPriv lp = new PublicLeafPriv();
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
        <td align="center"><textarea name=roleDescs cols="60" rows="3"><%=descs%></textarea>
            <input name="roleCodes" value="<%=roleCodes%>" type=hidden>
            <input name="dirCode" value="<%=dirCode%>" type=hidden></td>
      </tr>
      <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px"><input name="button2" type="button" class="btn" onClick="showModalDialog('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择角色">
          &nbsp;&nbsp;&nbsp;&nbsp;
          <input name="Submit3" type="submit" class="btn" value=" 提 交 "></td>
      </tr>
    </tbody>
</table>
</form>
<%
String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
%>
<br>
<br>
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="50%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" style="PADDING-LEFT: 10px" noWrap width="26%">用户组名称</td>
      <td class="tabStyle_1_title" noWrap width="40%">描述</td>
      <td width="34%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td><%=code%></td>
      <td><%=desc%></td>
      <td>
	  <a href="netdisk_public_dir_priv_m.jsp?op=add&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=LeafPriv.TYPE_USERGROUP%>">[ 添加 ]</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<br>
<form name="form1" action="netdisk_public_dir_priv_m.jsp?op=add" method=post>
<table class="tabStyle_1 percent80" width="395"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="329" class="tabStyle_1_title">添加用户</td>
  </tr>
  <tr>
    <td height="25" align="center">
	用户名：
	  <input name="userRealName" value="" readonly>
	  <input name="name" value="" type="hidden"><input type=hidden name=type value=1>
	  <input type=hidden name=dirCode value="<%=leaf.getCode()%>">
	&nbsp;<a href="#" onClick="javascript:showModalDialog('../user_sel.jsp',window.self,'dialogWidth:480px;dialogHeight:320px;status:no;help:no;')">选择用户</a>
	  &nbsp;
	<INPUT type="submit" class="btn" value="添加">
	</td>
  </tr>
</table></form>
</body>
</html>