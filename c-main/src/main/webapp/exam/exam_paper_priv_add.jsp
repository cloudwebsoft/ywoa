<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.exam.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理试卷权限</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script>
function setPerson(deptCode, deptName, user, userRealName) {
	form1.name.value = user;
	form1.userRealName.value = userRealName;
}

function setRoles(roles, descs) {
	formRole.roleCodes.value = roles;
	formRole.roleDescs.value = descs
}
function setUsers(users, userRealNames) {
	o("userRealName").value = users;
	o("userRealName").value = userRealNames;
	o("name").value = users;
}
</script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int paperId = ParamUtil.getInt(request, "paperId");
PaperDb pd = new PaperDb();
pd = pd.getPaperDb(paperId);
String title = pd.getTitle();
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">设置 <a href="exam_paper_priv_m.jsp?paperId=<%=paperId %>"><%=title%></a> 权限</td>
    </tr>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent60" cellspacing="0" cellpadding="3" width="50%" align="center">
  <form name="formRole" method="post" action="exam_paper_priv_m.jsp?op=setrole">
    <tbody>
      <tr>
        <td width="88%" align="left" nowrap class="tabStyle_1_title">角色</td>
      </tr>
      <%
RoleMgr roleMgr = new RoleMgr();		
PaperPriv lp = new PaperPriv();
Vector vrole = lp.getRolesOfPaperPriv(paperId);

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
        <td align="center"><textarea name=roleDescs cols="45" rows="3"><%=descs%></textarea>
            <input name="roleCodes" value="<%=roleCodes%>" type=hidden>
            <input name="paperId" value="<%=paperId%>" type=hidden></td>
            <input name="title" value="<%=title%>" type=hidden></td>
      </tr>
      <tr align="center" class="row" style="BACKGROUND-COLOR: #ffffff">
        <td style="PADDING-LEFT: 10px"><input name="button2" type="button" class="btn" onClick="showModalDialog('../role_multi_sel.jsp?roleCodes=<%=roleCodes%>',window.self,'dialogWidth:526px;dialogHeight:435px;status:no;help:no;')" value="选择角色">
          &nbsp;&nbsp;&nbsp;&nbsp;
          <input name="Submit3" type="submit" class="btn" value="确定"></td>
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
	  <a href="exam_paper_priv_m.jsp?op=add&title=<%=title %>&paperId=<%=paperId %>&name=<%=StrUtil.UrlEncode(code)%>&type=<%=PaperPriv.TYPE_USERGROUP%>">[ 添加 ]</a></td>
    </tr>
<%}%>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent60" width="492"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tabStyle_1_title" >添加用户</td>
  </tr>
  <form name="form1" action="exam_paper_priv_m.jsp?op=add" method=post>
  <tr class="row" style="BACKGROUND-COLOR: #ffffff">
	<td align="center">
	  <textarea cols="45" name="userRealName" value="" readonly ></textarea>
	  <input name="name" value="" type="hidden">
	  <input type=hidden name="type" value=1>
	  <input type="hidden" name="paperId" value="<%=paperId %>">
	  <input type="hidden" name="title" value="<%=title %>">
	  </td>
  </tr>
  <tr height="25" align="center">
	  <td><input class="btn"  onClick="javascript:showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')" value="选择用户" type="button" />
	  &nbsp;&nbsp;
		<input class="btn" type="submit" align="middle" value="确定" /></td>
  </tr>
</form>
</table>
<br>
</body>
</html>