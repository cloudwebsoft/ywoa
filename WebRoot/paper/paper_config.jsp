<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>备份</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

PaperConfig pc = PaperConfig.getInstance();
String op = ParamUtil.get(request, "op");
if (op.equals("saveSWRole")) {
	String swRoles = ParamUtil.get(request, "swRoles");
	pc.setProperty("swRoles", swRoles);
	pc.reload();
	out.print(StrUtil.Alert_Redirect("操作成功！", "paper_config.jsp"));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">公文配置</td>
    </tr>
  </tbody>
</table>
<form id="frm" name="frm" action="paper_config.jsp" method="post">
<table width="532" border="0" align="center" class="tabStyle_1 percent60">
  <tr>
    <td class="tabStyle_1_title">收文角色配置</td>
  </tr>
  <tr>
    <td align="center">
    <%
	String swRoles = pc.getProperty("swRoles");
	String[] ary = StrUtil.split(swRoles, ",");
	int len = 0;
	if (ary!=null)
		len = ary.length;
	RoleDb rd = new RoleDb();
	String swRoleDescs = "";
	for (int i=0; i<len; i++) {
		rd = rd.getRoleDb(ary[i]);
		if (!rd.isLoaded()) {
			continue;
		}
		if (swRoleDescs.equals(""))
			swRoleDescs = rd.getDesc();
		else
			swRoleDescs += "," + rd.getDesc();
	}
	%>
    <textarea id="swRolesDescs" name="swRolesDescs" cols="50" rows="10"><%=swRoleDescs%></textarea>
    <input id="swRoles" name="swRoles" type="hidden" value="<%=swRoles%>" />
    
    <br />
    <span style="line-height:1.5"><a href="javascript:;" onclick="openWinUserRoles()">选择角色</a></span></td>
  </tr>
  <tr>
    <td align="center">
    <input value="确定" type="submit" />
    <input name="op" value="saveSWRole" type="hidden" />
    </td>
  </tr>
</table>
</form>
</body>
<script>
function openWinUserRoles() {
	showModalDialog('../role_multi_sel.jsp?roleCodes=' + o("swRoles").value, window.self, 'dialogWidth:526px;dialogHeight:435px;status:no;help:no;');
	return;
}

function setRoles(roleCodes, roleDescs) {
	o("swRoles").value = roleCodes;
	o("swRolesDescs").value = roleDescs;
}
</script>
</html>