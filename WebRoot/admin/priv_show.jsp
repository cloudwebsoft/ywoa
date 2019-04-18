<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理权限-查看</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script>
function setPerson(userName, userRealName){
	formUser.userName.value = userName;
	formUser.userRealName.value = userRealName;
}

function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="privmgr" scope="page" class="com.redmoon.oa.pvg.PrivMgr"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
String priv = ParamUtil.get(request, "priv");
PrivDb pd = privmgr.getPriv(priv);
if (pd==null || !pd.isLoaded()) {
	out.print(StrUtil.Alert_Back("权限不存在！"));
	return;
}

if (op.equals("del")) {
	String type = ParamUtil.get(request, "type");
	if (type.equals("user")) {
		String userName = ParamUtil.get(request, "userName");

		String sql = "delete from user_priv where username=" +
                         StrUtil.sqlstr(userName) + " and priv=" + StrUtil.sqlstr(priv);
		JdbcTemplate jt = new JdbcTemplate();
		boolean re = jt.executeUpdate(sql)==1;
		if (re) {
			UserDb user = new UserDb();
			user = user.getUserDb(userName);
			if (!user.isLoaded()) {
				out.print(StrUtil.Alert_Back("用户 " + userName + " 不存在！"));
				return;
			}
		    UserCache uc = new UserCache(user);
            uc.refreshPrivs(userName);		
			out.print(StrUtil.Alert_Redirect("操作成功！", "priv_show.jsp?priv=" + StrUtil.UrlEncode(priv)));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
		return;
	}
	else if (type.equals("group")) {
		String code = ParamUtil.get(request, "code");
		UserGroupPrivDb ugpd = new UserGroupPrivDb();
		ugpd = ugpd.getUserGroupPrivDb(code, priv);
		if (ugpd.del()) {
			out.print(StrUtil.Alert_Redirect("操作成功！", "priv_show.jsp?priv=" + StrUtil.UrlEncode(priv)));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
		return;	
	}
	else {
		String code = ParamUtil.get(request, "code");
		RolePrivDb rpd = new RolePrivDb();
		rpd = rpd.getRolePrivDb(code, priv);
		if (rpd.del()) {
			out.print(StrUtil.Alert_Redirect("操作成功！", "priv_show.jsp?priv=" + StrUtil.UrlEncode(priv)));
		}
		else {
			out.print(StrUtil.Alert_Back("操作失败！"));
		}
		return;	
	}
}
else if (op.equals("addGroupPriv")) {
	String groupCode = ParamUtil.get(request, "groupCode");
	String sql = "select groupCode from user_group_priv where groupCode=" + StrUtil.sqlstr(groupCode) + " and priv=" + StrUtil.sqlstr(priv);	
	JdbcTemplate jt = new JdbcTemplate();
	// 检查是否已存在
	ResultIterator ri = jt.executeQuery(sql);
	if (ri.hasNext()) {
		out.print(StrUtil.Alert_Back("该用户组已被添加！"));
		return;
	}
	sql = "insert into user_group_priv (groupCode, priv) values (" + StrUtil.sqlstr(groupCode) + "," + StrUtil.sqlstr(priv) + ")";	
	boolean re = jt.executeUpdate(sql)==1;
	if (re) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "priv_show.jsp?priv=" + StrUtil.UrlEncode(priv)));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;	
}
else if (op.equals("addUserPriv")) {
	String userName = ParamUtil.get(request, "userName");
	UserDb user = new UserDb();
	user = user.getUserDb(userName);
	if (!user.isLoaded()) {
		out.print(StrUtil.Alert_Back("用户 " + userName + " 不存在！"));
		return;
	}
	
	String sql = "select username from user_priv where username=" + StrUtil.sqlstr(userName) + " and priv=" + StrUtil.sqlstr(priv);	
	JdbcTemplate jt = new JdbcTemplate();
	// 检查是否已存在
	ResultIterator ri = jt.executeQuery(sql);
	if (ri.hasNext()) {
		out.print(StrUtil.Alert_Back("该用户已被添加！"));
		return;
	}
	sql = "insert into user_priv (username, priv) values (" + StrUtil.sqlstr(userName) + "," + StrUtil.sqlstr(priv) + ")";	
	boolean re = jt.executeUpdate(sql)==1;
	if (re) {
		UserCache uc = new UserCache(user);
		uc.refreshPrivs(userName);
		out.print(StrUtil.Alert_Redirect("操作成功！", "priv_show.jsp?priv=" + StrUtil.UrlEncode(priv)));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;	
}
else if (op.equals("addRolePriv")) {
	String roleCode = ParamUtil.get(request, "roleCode");
	String sql = "select roleCode from user_role_priv where roleCode=" + StrUtil.sqlstr(roleCode) + " and priv=" + StrUtil.sqlstr(priv);	
	JdbcTemplate jt = new JdbcTemplate();
	// 检查是否已存在
	ResultIterator ri = jt.executeQuery(sql);
	if (ri.hasNext()) {
		out.print(StrUtil.Alert_Back("该角色已被添加！"));
		return;
	}
	sql = "insert into user_role_priv (roleCode, priv) values (" + StrUtil.sqlstr(roleCode) + "," + StrUtil.sqlstr(priv) + ")";	
	boolean re = jt.executeUpdate(sql)==1;
	if (re) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "priv_show.jsp?priv=" + StrUtil.UrlEncode(priv)));
	}
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
	}
	return;	
}
%>
<%@ include file="priv_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<table cellSpacing="0" cellPadding="0" width="100%" class="tabTitle">
  <tbody>
    <tr>
      <td align="center" class="head">权限 - <%=pd.getDesc()%>(<%=priv%>)</td>
    </tr>
  </tbody>
</table>
<%
String sql = "select username from user_priv where priv=" + StrUtil.sqlstr(priv) + " order by username asc";
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
ResultRecord rr = null;
%>
<br>
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="19%">用户名</td>
      <td class="tabStyle_1_title" noWrap width="44%">部门</td>
      <td width="16%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
UserDb user;
UserMgr um = new UserMgr();
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next();
	String userName = rr.getString(1);
	user = um.getUserDb(userName);
	%>
    <tr class="row">
      <td style="PADDING-LEFT: 10px"><%=user.getRealName()%></td>
      <td><%
			DeptMgr dm = new DeptMgr();		
			DeptUserDb du = new DeptUserDb();
			Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
			int k = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span class=STYLE1>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				}
				else
					deptName = dd.getName() + "&nbsp;&nbsp;";
				if (k==0) {
					out.print("<a href='#' onClick=\"openWin('dept_user.jsp?deptCode=" + StrUtil.UrlEncode(dd.getCode()) + "', 620, 420)\">" + deptName + "</a>");
				}
				else {
					out.print("，&nbsp;" + "<a href='#' onClick=\"openWin('dept_user.jsp?deptCode=" + StrUtil.UrlEncode(dd.getCode()) + "', 620, 420)\">" + deptName + "</a>");
				}
				k++;
			} 
			%></td>
      <td align="center">
	  <a href="priv_op.jsp?op=edit&priv=<%=StrUtil.UrlEncode(priv)%>"></a>
	   [ <a onClick="if (!confirm('您确定要删除吗？')) return false" href="priv_show.jsp?op=del&type=user&userName=<%=StrUtil.UrlEncode(userName)%>&priv=<%=StrUtil.UrlEncode(priv)%>">删除</a> ]      </td>
    </tr>
<%}%>
    <tr class="row">
      <td colspan="3">
        <form name="formUser" method="post" action="priv_show.jsp">
			<input name="op" value="addUserPriv" type=hidden>
			<input name="priv" value="<%=priv%>" type=hidden>
              <input name="userName" value="" size="16" type="hidden">
                <input name="userRealName"  id="userRealName" value="" size="18" readonly>
                <a href="#" onClick="openWin('user_sel.jsp', 800, 600)">选择</a>&nbsp;&nbsp;
                <input name="submit2" class="btn" type="submit" value="添加">
        </form>				
	  </td>
    </tr>
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="19%">用户组</td>
      <td class="tabStyle_1_title" noWrap width="44%">描述</td>
      <td width="16%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
UserGroupDb ug;
UserGroupMgr ugm = new UserGroupMgr();
sql = "select groupcode from user_group_priv where priv=" + StrUtil.sqlstr(priv) + " order by groupcode asc";
ri = jt.executeQuery(sql);
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next();
	String groupCode = rr.getString(1);
	ug = ugm.getUserGroupDb(groupCode);
	%>
    <tr>
      <td><a href="javascript:;" onclick="addTab('用户组中的用户', '<%=request.getContextPath()%>/admin/user_group_user.jsp?group_code=<%=StrUtil.UrlEncode(ug.getCode())%>')"><%=ug.getCode()%></a></td>
      <td><%=ug.getDesc()%></td>
      <td align="center"><a href="priv_op.jsp?op=edit&priv=<%=StrUtil.UrlEncode(priv)%>"></a> [ <a onClick="if (!confirm('您确定要删除吗？')) return false" href="priv_show.jsp?op=del&type=group&priv=<%=StrUtil.UrlEncode(priv)%>&code=<%=StrUtil.UrlEncode(ug.getCode())%>">删除</a> ] </td>
    </tr>
    <%}%>
    <tr>
      <td colspan="3">
<form name="formGroup" method="post" action="priv_show.jsp">
<input type="hidden" name="priv" value="<%=priv%>">
<input type="hidden" name="op" value="addGroupPriv">
<select name="groupCode">	  
<%
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
while (ir.hasNext()) {
	ugroup = (UserGroupDb)ir.next();
%>
<option value="<%=ugroup.getCode()%>"><%=ugroup.getDesc()%></option>
<%
}
%>
</select>
&nbsp;
<input value="添加"  class="btn" type="submit">
</form>
</td>
    </tr>	
  </tbody>
</table>
<br>
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td class="tabStyle_1_title" noWrap width="19%">角色</td>
      <td class="tabStyle_1_title" noWrap width="44%">描述</td>
      <td width="16%" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
RoleDb rd;
RoleMgr rm = new RoleMgr();
sql = "select rolecode from user_role_priv where priv=" + StrUtil.sqlstr(priv) + " order by rolecode asc";
ri = jt.executeQuery(sql);
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next();
	String roleCode = rr.getString(1);
	rd = rm.getRoleDb(roleCode);
	%>
    <tr>
      <td><a href="javascript:;" onclick="addTab('角色中的用户', '<%=request.getContextPath()%>/admin/user_role_user.jsp?role_code=<%=StrUtil.UrlEncode(rd.getCode())%>')"><%=rd.getCode()%></a></td>
      <td><%=rd.getDesc()%></td>
      <td align="center"><a href="priv_op.jsp?op=edit&priv=<%=StrUtil.UrlEncode(priv)%>"></a> [ <a onClick="if (!confirm('您确定要删除吗？')) return false" href="priv_show.jsp?op=del&type=role&priv=<%=StrUtil.UrlEncode(priv)%>&code=<%=StrUtil.UrlEncode(rd.getCode())%>">删除</a> ] </td>
    </tr>
    <%}%>
    <tr class="row">
      <td colspan="3"><form name="formRole" method="post" action="priv_show.jsp">
        <input type="hidden" name="priv" value="<%=priv%>">
        <input type="hidden" name="op" value="addRolePriv">
        <select name="roleCode">
          <%
RoleDb role = new RoleDb();		  
result = role.list();
ir = result.iterator();
while (ir.hasNext()) {
	rd = (RoleDb)ir.next();
%>
          <option value="<%=rd.getCode()%>"><%=rd.getDesc()%></option>
          <%
}
%>
        </select>
  &nbsp;
  <input name="submit" class="btn" type="submit" value="添加">
      </form></td>
    </tr>
  </tbody>
</table>
<br>
<br>
</body>
<script>
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
}
</script>
</html>