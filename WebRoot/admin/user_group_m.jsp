<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理用户组</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
</head>
<body>
<jsp:useBean id="usergroupmgr" scope="page" class="com.redmoon.oa.pvg.UserGroupMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

boolean isAdmin = privilege.isUserPrivValid(request, "admin");
String curUnitCode = ParamUtil.get(request, "curUnitCode");

String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("add")) {
	try {
		if (usergroupmgr.add(request))
			out.print(StrUtil.Alert_Redirect("添加成功！", "user_group_m.jsp"));
		else
			out.print(StrUtil.Alert_Back("添加失败！请检查是否有重复项！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		e.printStackTrace();
	}
	return;
}
else if (op.equals("del")) {
	if (usergroupmgr.del(request))
		out.print(StrUtil.Alert_Redirect("删除成功！", "user_group_m.jsp"));
	else
		out.print(StrUtil.Alert_Back("删除失败！"));
	return;
}
%>
<%@ include file="user_group_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
String code;
String desc;
UserGroupDb ugroup = new UserGroupDb();
Vector result;

if (curUnitCode.equals("")) {
	if (isAdmin) {
		result = ugroup.list();
	}
	else {
		String unitCode;
		if (isAdmin) {
			unitCode = DeptDb.ROOTCODE;
		}
		else {
			unitCode = privilege.getUserUnitCode(request);
		}	
		result = ugroup.getUserGroupsOfUnit(unitCode);
	}
}
else {
	result = ugroup.getUserGroupsOfUnit(curUnitCode);
}

Iterator ir = result.iterator();
%>
<table id="mainTable" cellSpacing="0" cellPadding="3" width="95%" align="center" class="tabStyle_1 percent98">
  <tbody>
    <tr>
      <td style="display:none" width="13%" align="center" class="tabStyle_1_title">编码</td>
      <td width="18%" align="center" noWrap class="tabStyle_1_title">名称</td>
      <td width="18%" align="center" noWrap class="tabStyle_1_title">部门</td>
      <td width="8%" align="center" noWrap class="tabStyle_1_title">是否部门</td>
      <td class="tabStyle_1_title" nowrap="nowrap" width="20%">单位</td>
      <td width="10%" align="center" noWrap class="tabStyle_1_title">系统</td>
      <td width="13%" align="center" noWrap class="tabStyle_1_title">操作</td>
    </tr>
<%
DeptMgr dm = new DeptMgr();
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
	code = ug.getCode();
	desc = ug.getDesc();
	%>
    <tr class="highlight">
      <td style="display:none" style="PADDING-LEFT: 10px">&nbsp;&nbsp;<%=code%></td>
      <td><%=desc%></td>
      <td><%
	  if (ug.isDept()) {
	  	DeptDb dd = dm.getDeptDb(ug.getDeptCode());
		if (dd==null)
			out.print("部门不存在！");
		else
			out.print(dd.getName());
	  }
	  %>      </td>
      <td align="center"><%=ug.isDept()?"<font color=red>是</font>":"否"%> </td>
      <td><%=dm.getDeptDb(ug.getUnitCode()).getName()%> </td>
      <td align="center"><%=ug.isSystem()?"是":"否"%></td>
      <td align="center">
	  <a href="javascript:;" onclick="addTab('<%=desc%>', '<%=request.getContextPath()%>/admin/user_group_op.jsp?op=edit&code=<%=StrUtil.UrlEncode(code)%>')">[ 编辑 ]</a>
	  <%if (false && !ug.getCode().equals(ug.ADMINISTRATORS)) {%>	  
	  [ <a href="user_group_priv.jsp?group_code=<%=StrUtil.UrlEncode(code)%>&desc=<%=StrUtil.UrlEncode(desc)%>">权限</a> ] 
	  <%}%>
	  <%if (false && !ug.getCode().equals(ug.EVERYONE)) {%>
	  [ <a href="user_group_user.jsp?group_code=<%=StrUtil.UrlEncode(code)%>">用户</a> ]
	  <%}%>
	  <%if (!ug.isSystem()) {%>
	[ <a onClick="if (!confirm('您确定要删除吗？确定后将删除所有的与之相关的权限！')) return false" href="user_group_m.jsp?op=del&code=<%=StrUtil.UrlEncode(code)%>">删除</a> ]
	<%}%>	</td>
    </tr>
<%}%>
  </tbody>
</table>
</body>
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>