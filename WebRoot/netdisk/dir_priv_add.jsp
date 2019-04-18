<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String dirCode = ParamUtil.get(request, "dirCode");
Leaf leaf = new Leaf();
leaf = leaf.getLeaf(dirCode);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理共享</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script>
function setPerson(deptCode, deptName, userName, userRealName)
{
	form1.name.value = userName;
	form1.userRealName.value = userRealName;
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">设置<%=leaf.getName()%>共享</td>
    </tr>
  </tbody>
</table>
<%
String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
%>
<table class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td class="tabStyle_1_title">用户组名称</td>
      <td class="tabStyle_1_title">描述</td>
      <td class="tabStyle_1_title">操作</td>
    </tr>
<%
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="highlight">
      <td>&nbsp;<%=code%></td>
      <td><%=desc%></td>
      <td align="center">
	  <a href="dir_priv_m.jsp?op=add&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=LeafPriv.TYPE_USERGROUP%>">[ 添加 ]</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<br>
<br>
<table class="tabStyle_1 percent60">
  <tr>
    <td class="tabStyle_1_title">添加用户</td>
  </tr>
  <form name="form1" action="dir_priv_m.jsp?op=add" method=post>
  <tr>
    <td>
	用户名：
	  
	  <input name="userRealName" value="" readonly>
	  <input type=hidden name=type value=1>
	  <input type=hidden name=dirCode value="<%=leaf.getCode()%>">
	  <input name="name" value="" type=hidden>
	  &nbsp;
	<INPUT type=image 
onclick="javascript:location.href='user_group_op.jsp';" src="../admin/../admin/images/btn_add.gif" align="middle" width=80 
height=20>
	<span class="p14">&nbsp;<a href="#" onClick="javascript:showModalDialog('../user_sel.jsp',window.self,'dialogWidth:500px;dialogHeight:320px;status:no;help:no;')">选择用户</a></span></td>
  </tr></form>
</table>
</body>
<script language="javascript">
<!--
function form1_onsubmit()
{
	errmsg = "";
	if (form1.pwd.value!=form1.pwd_confirm.value)
		errmsg += "密码与确认密码不致，请检查！\n"
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</html>