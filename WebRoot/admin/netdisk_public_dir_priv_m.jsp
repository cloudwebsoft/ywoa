<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理公共共享目录权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.netdisk.PublicLeafPriv"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
//	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
//	return;
//}
String op = ParamUtil.get(request, "op");
String dirCode = ParamUtil.get(request, "dirCode");

leafPriv.setDirCode(dirCode);
if (!(leafPriv.canUserManage(privilege.getUser(request)))) {
	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID + " 用户需对该节点拥有管理的权限！"));
	return;
}

PublicLeaf leaf = new PublicLeaf();
leaf = leaf.getLeaf(dirCode);

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.Alert_Back("名称不能为空！"));
		return;
	}
	int type = ParamUtil.getInt(request, "type");
	if (type==PublicLeafPriv.TYPE_USER) {
		UserDb user = new UserDb();
		user = user.getUserDb(name);
		if (!user.isLoaded()) {
			out.print(StrUtil.Alert_Back("该用户不存在！"));
			return;
		}
	}
	try {
		if (leafPriv.add(name, type))
			out.print(StrUtil.Alert_Redirect("添加成功！", "netdisk_public_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		String leafCode = ParamUtil.get(request, "dirCode");
		PublicLeafPriv lp = new PublicLeafPriv(leafCode);
		lp.setRoles(leafCode, roleCodes);
		out.print(StrUtil.Alert_Redirect("操作成功！", "netdisk_public_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	}
	catch (Exception e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	int see = 0, append=0, del=0, modify=0, examine=0;
	String strsee = ParamUtil.get(request, "see");
	if (StrUtil.isNumeric(strsee)) {
		see = Integer.parseInt(strsee);
	}
	String strappend = ParamUtil.get(request, "append");
	if (StrUtil.isNumeric(strappend)) {
		append = Integer.parseInt(strappend);
	}
	String strmodify = ParamUtil.get(request, "modify");
	if (StrUtil.isNumeric(strmodify)) {
		modify = Integer.parseInt(strmodify);
	}
	String strdel = ParamUtil.get(request, "del");
	if (StrUtil.isNumeric(strdel)) {
		del = Integer.parseInt(strdel);
	}
	String strexamine = ParamUtil.get(request, "examine");
	if (StrUtil.isNumeric(strexamine)) {
		examine = Integer.parseInt(strexamine);
	}
	
	if (examine==1) {
		see = 1;
		append = 1;
		modify = 1;
		del = 1;
	}
	else if (modify==1 || del==1) {
		see = 1;
	}
	
	leafPriv.setId(id);
	leafPriv.setAppend(append);
	leafPriv.setModify(modify);
	leafPriv.setDel(del);
	leafPriv.setSee(see);
	leafPriv.setExamine(examine);
	
	if (leafPriv.save())
		out.print(StrUtil.Alert_Redirect("操作成功！", "netdisk_public_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	PublicLeafPriv lp = new PublicLeafPriv();
	lp = lp.getPublicLeafPriv(id);
	if (lp.del())
		out.print(StrUtil.Alert_Redirect("操作成功！", "netdisk_public_dir_priv_m.jsp?dirCode=" + StrUtil.UrlEncode(dirCode)));
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">管理 <%=leaf.getName()%> 权限&nbsp;&nbsp;|&nbsp;&nbsp;<a href="../netdisk/netdisk_public_share_frame.jsp">浏览视图</a></td>
    </tr>
  </tbody>
</table>
<%
Vector result = leafPriv.list();
Iterator ir = result.iterator();
%>
<br>
<br>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" style="PADDING-LEFT: 10px" noWrap width="25%">用户</td>
      <td class="tabStyle_1_title" noWrap width="22%">类型</td>
      <td class="tabStyle_1_title" noWrap width="32%">权限</td>
      <td width="21%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
while (ir.hasNext()) {
 	PublicLeafPriv lp = (PublicLeafPriv)ir.next();
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
    <tr class="row" style="BACKGROUND-COLOR: #ffffff">
      <td style="PADDING-LEFT: 10px">&nbsp;&nbsp;
	  <%
	  if (lp.getType()==lp.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		out.print(ud.getRealName());
	  }else if (lp.getType()==lp.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(lp.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (lp.getType()==lp.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(lp.getName());
	  	out.print(ug.getDesc());
	  }
	  %>
	  <input type=hidden name="id" value="<%=lp.getId()%>">
      <input type=hidden name="dirCode" value="<%=lp.getDirCode()%>"></td>
      <td><%=lp.getTypeDesc()%><!--<input name=examine type=checkbox <%=lp.getExamine()==1?"checked":""%> value="1">
	  审核--></td>
      <td align="center">
	  	<input name=see type=checkbox <%=lp.getSee()==1?"checked":""%> value="1" />
        浏览&nbsp;
        <input name=append type=checkbox <%=lp.getAppend()==1?"checked":""%> value="1" />
		添加&nbsp;
		<input name=del type=checkbox <%=lp.getDel()==1?"checked":""%> value="1" />
		删除&nbsp;
		<input name=modify type=checkbox <%=lp.getModify()==1?"checked":""%> value="1" />
		修改&nbsp;
		<input name=examine type=checkbox <%=lp.getExamine()==1?"checked":""%> value="1" />
		管理	  </td>
      <td align="center">
	  	<input type=submit value="修改" class="btn" />
	  &nbsp;<input type=button class="btn" onClick="window.location.href='netdisk_public_dir_priv_m.jsp?op=del&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&id=<%=lp.getId()%>'" value=删除> </td>
    </tr></form>
<%}%>
  </tbody>
</table>
<DIV style="WIDTH: 95%" align=right>
  <INPUT onclick="javascript:location.href='netdisk_public_dir_priv_add.jsp?dirCode=<%=StrUtil.UrlEncode(leafPriv.getDirCode())%>';" type="button" value="添加" class="btn" />
</DIV>
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