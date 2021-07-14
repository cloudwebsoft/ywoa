<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="leafPriv" scope="page" class="com.redmoon.oa.netdisk.LeafPriv"/>
<jsp:useBean id="leafPrivMgr" scope="page" class="com.redmoon.oa.netdisk.LeafPrivMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
//if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
//	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID));
//	return;
//}
String op = ParamUtil.get(request, "op");
String dirCode = ParamUtil.get(request, "dirCode");

leafPriv.setDirCode(dirCode);
if (!(leafPriv.canUserDel(privilege.getUser(request)) || leafPriv.canUserExamine(privilege.getUser(request)))) {
	out.print(StrUtil.Alert_Back(privilege.MSG_INVALID + " 用户需对该节点拥有删除和审核的权限！"));
	return;
}

Leaf leaf = new Leaf();
leaf = leaf.getLeaf(dirCode);

if (!privilege.getUser(request).equals(leaf.getRootCode())) {
	out.print(StrUtil.Alert(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	int type = ParamUtil.getInt(request, "type");
	if (type==LeafPriv.TYPE_USER) {
		UserDb user = new UserDb();
		user = user.getUserDb(name);
		if (!user.isLoaded()) {
			out.print(StrUtil.Alert_Back("该用户不存在！"));
			return;
		}
	}
	try {
		if (leafPrivMgr.add(request, name, type, dirCode)) {
			out.print(StrUtil.Alert("添加成功！"));
		%>
			<script>
			window.parent.leftFileFrame.location.reload();
			</script>
		<%
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
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
	
	leafPriv.setId(id);
	leafPriv.setAppend(append);
	leafPriv.setModify(modify);
	leafPriv.setDel(del);
	leafPriv.setSee(see);
	leafPriv.setExamine(examine);
	if (leafPrivMgr.save(request, leafPriv))
		out.print(StrUtil.Alert("修改成功！"));
	else
		out.print(StrUtil.Alert("修改失败！"));
}

if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	LeafPriv lp = new LeafPriv();
	lp = lp.getLeafPriv(id);
	if (!lp.isLoaded()) {
		out.print(StrUtil.Alert("该共享已不存在！"));
	}
	else {
		if (leafPrivMgr.del(request,lp))
			out.print(StrUtil.Alert("删除成功！"));
		else
			out.print(StrUtil.Alert("删除失败！"));
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理个人目录权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><%=leaf.getName()%>共享</td>
    </tr>
  </tbody>
</table>
<%
Vector result = leafPriv.list();
Iterator ir = result.iterator();
%>
<table class="percent98" width="98%">
  <tr>
    <td align="right"><input class="btn" type="button" value="添加" onClick="window.location.href='dir_priv_add.jsp?dirCode=<%=StrUtil.UrlEncode(leafPriv.getDirCode())%>';" />
    </td>
  </tr>
</table>
<table class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td class="tabStyle_1_title">用户</td>
      <td class="tabStyle_1_title">类型</td>
      <td class="tabStyle_1_title">权限</td>
      <td class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
while (ir.hasNext()) {
 	LeafPriv lp = (LeafPriv)ir.next();
	String realName = "";
	if (lp.getType()==lp.TYPE_USER) {
		UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		realName = ud.getRealName();
	}
	else {
		UserGroupDb ugd = new UserGroupDb();
		ugd = ugd.getUserGroupDb(lp.getName());
		realName = ugd.getDesc();
	}
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
    <tr class="highlight">
      <td>&nbsp;&nbsp;<%=realName%>
	  <input type=hidden name="id" value="<%=lp.getId()%>">
      <input type=hidden name="dirCode" value="<%=lp.getDirCode()%>">
</td>
      <td><%=lp.getTypeDesc()%></td>
      <td>
	  <input name=see type=checkbox <%=lp.getSee()==1?"checked":""%> value="1">浏览&nbsp;
	  <input name=append type=checkbox <%=lp.getAppend()==1?"checked":""%> value="1"> 
	  添加 &nbsp;
	  <input name=del type=checkbox <%=lp.getDel()==1?"checked":""%> value="1">
	  删除&nbsp;
	  <input name=modify type=checkbox <%=lp.getModify()==1?"checked":""%> value="1"> 
	  修改 
	  <input name=examine type=hidden value="1"></td>
      <td align="center">
	  <input class="btn" type=submit value="修改">
&nbsp;&nbsp;<input class="btn" type=button onClick="window.location.href='dir_priv_m.jsp?op=del&dirCode=<%=StrUtil.UrlEncode(leaf.getCode())%>&id=<%=lp.getId()%>'" value=删除> </td>
    </tr></form>
<%}%>
  </tbody>
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