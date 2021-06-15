<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.security.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int serverIPId = ParamUtil.getInt(request, "serverIPId");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
String sort = ParamUtil.get(request, "sort");
String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理服务器访问权限</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "ip_priv_list.jsp?serverIPId=<%=serverIPId%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<jsp:useBean id="ServerIPPriv" scope="page" class="com.redmoon.oa.security.ServerIPPriv"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	out.print(StrUtil.Alert_Back(Privilege.MSG_INVALID));
	return;
}

ServerIPDb sid = new ServerIPDb();
sid = (ServerIPDb)sid.getQObjectDb(new Integer(serverIPId));

if (op.equals("add")) {
	String name = ParamUtil.get(request, "name");
	if (name.equals("")) {
		out.print(StrUtil.Alert_Back("名称不能为空！"));
		return;
	}
	int type = ParamUtil.getInt(request, "type");
	if (type == ServerIPPriv.TYPE_USER) {
		UserDb user = new UserDb();
		user = user.getUserDb(name);
		if (!user.isLoaded()) {
			out.print(StrUtil.Alert_Back("该用户不存在！"));
			return;
		}
	}
	try {
		ServerIPPriv sip = new ServerIPPriv();
		if (sip.add(name, type, serverIPId))
			out.print(StrUtil.Alert_Redirect("操作成功！", "ip_priv_list.jsp?serverIPId=" + serverIPId));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("setrole")) {
	try {
		String roleCodes = ParamUtil.get(request, "roleCodes");
		ServerIPPriv lp = new ServerIPPriv();
		lp.setRoles(roleCodes, serverIPId);
		out.print(StrUtil.Alert_Redirect("操作成功！", "ip_priv_list.jsp?serverIPId=" + serverIPId));
	}
	catch (Exception e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
else if (op.equals("modify")) {
	int id = ParamUtil.getInt(request, "id");
	int login = ParamUtil.getInt(request, "login", 0);
	ServerIPPriv sip = new ServerIPPriv();
	sip = sip.getServerIPPriv(id);
	sip.setId(id);
	sip.setLogin(login);
	if (sip.save()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "ip_priv_list.jsp?serverIPId=" + sip.getServerIPId()));
	}
	else
		out.print(StrUtil.Alert_Back("修改失败！"));
	return;
}
else if (op.equals("del")) {
	int id = ParamUtil.getInt(request, "id");
	ServerIPPriv sip = new ServerIPPriv();
	sip = sip.getServerIPPriv(id);
	if (sip.del())
		out.print(StrUtil.Alert_Redirect("操作成功！", "ip_priv_list.jsp?serverIPId=" + sip.getServerIPId()));
	else
		out.print(StrUtil.Alert_Back("删除失败！"));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">管理 <%=sid.getString("description")%> 权限</td>
    </tr>
  </tbody>
</table>
<%
ServerIPPriv sip = new ServerIPPriv();
String sql = "select id from oa_server_ip_priv where server_ip_id=" + serverIPId + " order by " + orderBy + " " + sort;
Vector result = sip.list(sql);
Iterator ir = result.iterator();
%>
<br>
<table class="percent98" width="80%" align="center">
  <tr>
    <td align="right"><input class="btn" name="button" type="button" onclick="window.location.href='ip_priv_add.jsp?serverIPId=<%=serverIPId%>';" value="添加权限" width=80 height=20 />
  </td>
  </tr>
</table>
<table class="tabStyle_1 percent98" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" width="16%" style="cursor:hand" onclick="doSort('name')">用户
        <%if (orderBy.equals("name")) {
			if (sort.equals("asc")) 
				out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
			else
				out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}%>	  
	  </td>
      <td class="tabStyle_1_title" width="13%" style="cursor:hand" onclick="doSort('name')">类型</td>
      <td class="tabStyle_1_title" width="49%">权限</td>
      <td width="22%" class="tabStyle_1_title">操作</td>
    </tr>
<%
int i = 0;
while (ir.hasNext()) {
 	ServerIPPriv lp = (ServerIPPriv)ir.next();
	i++;
	%>
  <form id="form<%=i%>" name="form<%=i%>" action="?op=modify" method=post>
    <tr class="highlight">
      <td>
      <%
	  if (lp.getType()==ServerIPPriv.TYPE_USER) {
	  	UserDb ud = new UserDb();
		ud = ud.getUserDb(lp.getName());
		out.print(ud.getRealName());
	  }else if (lp.getType()==ServerIPPriv.TYPE_ROLE) {
	    RoleDb rd = new RoleDb();
		rd = rd.getRoleDb(lp.getName());
	  	out.print(rd.getDesc());
	  }
	  else if (lp.getType()==ServerIPPriv.TYPE_USERGROUP) {
	  	UserGroupDb ug = new UserGroupDb();
		ug = ug.getUserGroupDb(lp.getName());
	  	out.print(ug.getDesc());
	  }
	  %>
	  <input type=hidden name="id" value="<%=lp.getId()%>"/>
      <input type=hidden name="serverIPId" value="<%=serverIPId%>"/>
      <td><%=lp.getTypeDesc()%>     
      <td>
      <input name="login" type=checkbox <%=lp.getLogin()==1?"checked":""%> value="1"/>登录</td>
      <td align="center">
	  <input class="btn" type=submit value="修改"/>
	  &nbsp;
	  <input class="btn" type=button onclick="if (confirm('您确定要删除吗?')) window.location.href='ip_priv_list.jsp?op=del&serverIPId=<%=serverIPId%>&id=<%=lp.getId()%>'" value="删除"/></td>
    </tr></form>
<%}%>
</table>
<br>
</body>
</html>