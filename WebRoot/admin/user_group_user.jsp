<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理用户组的用户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
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

function openWinUsers() {
	selUserNames = form1.users.value;
	selUserRealNames = form1.userRealNames.value;
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',600,480);
	// showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
}

function setUsers(users, userRealNames) {
	form1.users.value = users;
	form1.userRealNames.value = userRealNames;
	if (users=="")
		return;
	form1.submit();
}
</script>
</head>
<body>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
String groupCode = ParamUtil.get(request, "group_code").trim();
String sql = "select u.name,u.realName,u.gender from users u,user_of_group g where g.group_code=" + StrUtil.sqlstr(groupCode) + " and g.user_name=u.name";

String strWhat = "";
if (!groupCode.equals("")) {
	UserGroupDb ugd = new UserGroupDb();
	ugd = ugd.getUserGroupDb(groupCode);
	if (ugd.isDept()) {
		response.sendRedirect("user_group_dept_user.jsp?group_code=" + StrUtil.UrlEncode(groupCode));
		return;
	}
	strWhat = ugd.getDesc();
}

if (op.equals("add")) {
	String userNames = ParamUtil.get(request, "users");
	String[] users = StrUtil.split(userNames, ",");
	if (users==null) {
		out.print(StrUtil.Alert_Back("请选择用户！"));
		return;
	}
	else {
		UserGroupDb ugd = new UserGroupDb();
		ugd.addUsers(groupCode, users);
		out.print(StrUtil.Alert_Redirect("操作成功！", "user_group_user.jsp?group_code=" + StrUtil.UrlEncode(groupCode)));
		return;
	}
}
else if (op.equals("del")) {
	String userName = ParamUtil.get(request, "userName");
	UserGroupDb ugd = new UserGroupDb();
	boolean re = ugd.delUser(groupCode, userName);
	if (re)
		out.print(StrUtil.Alert_Redirect("操作成功！", "user_group_user.jsp?group_code=" + StrUtil.UrlEncode(groupCode)));
	else {
		out.print(StrUtil.Alert_Back("操作失败！"));
		return;
	}
}
else if (op.equals("delBatch")) {
	String[] ids = ParamUtil.getParameters(request, "ids");
	if (ids==null) {
		out.print(StrUtil.Alert_Back("请选择记录！"));
		return;
	}
	UserGroupDb ugd = new UserGroupDb();
	for (int i=0; i<ids.length; i++) {
		String[] ary = StrUtil.split(ids[i], "\\|");
		String gCode = ary[0];
		String uName = ary[1];
		ugd.delUser(gCode, uName);
	}
	out.print(StrUtil.Alert_Redirect("操作成功！", "user_group_user.jsp?group_code=" + StrUtil.UrlEncode(groupCode)));
	return;
}
%>
<%@ include file="user_group_op_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="tabTitle" cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td align="center" class="head">属于用户组&nbsp;<%=strWhat%>&nbsp;的用户</td>
    </tr>
  </tbody>
</table>
<%
RMConn rmconn = new RMConn(Global.getDefaultDB());
ResultIterator ri = rmconn.executeQuery(sql);
ResultRecord rr = null;
String name;
String realname;
String genderdesc;
%>
<br>
<form id="formGroupUser" name="formGroupUser" action="user_group_user.jsp" method="post">
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <tbody>
    <tr>
      <td width="5%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td width="13%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px">用户名</td>
      <td width="18%" align="center" noWrap class="tabStyle_1_title" style="PADDING-LEFT: 10px">真实姓名</td>
      <td width="10%" align="center" noWrap class="tabStyle_1_title">性别</td>
      <td width="38%" align="center" class="tabStyle_1_title">所属部门</td>
      <td width="16%" align="center" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
String userNames = "";
String userRealNames = "";
DeptMgr dm = new DeptMgr();		
while (ri.hasNext()) {
 	rr = (ResultRecord)ri.next();
	name = rr.getString(1);
	realname = rr.getString(2);
	if (userNames.equals("")) {
		userNames = name;
		userRealNames = realname;
	}
	else {
		userNames += "," + name;
		userRealNames += "," + realname;
	}

	genderdesc = rr.getInt(3)==0?"男":"女";
%>
    <tr>
      <td align="center" style="PADDING-LEFT: 10px"><input type="checkbox" name="ids" value="<%=groupCode + "|" + name%>" /></td>
      <td style="PADDING-LEFT: 10px">&nbsp;&nbsp;<%=name%></td>
      <td style="PADDING-LEFT: 10px"><%=realname%></td>
      <td align="center"><%=genderdesc%></td>
      <td align="left"><%
			DeptUserDb du = new DeptUserDb();
			Iterator ir2 = du.getDeptsOfUser(name).iterator();
			int k = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				}
				else
					deptName = dd.getName() + "&nbsp;&nbsp;";
				if (k==0) {
					out.print(deptName);
				}
				else {
					out.print("，" + deptName);
				}
				k++;
			} 
			%></td>
      <td align="center">
      <a href="user_op.jsp?op=edit&name=<%=StrUtil.UrlEncode(name)%>">管理</a>
      <!--<a href="javascript:;" onclick="if (confirm('您确定要删除么？')) window.location.href='user_group_user.jsp?op=del&group_code=<%=StrUtil.UrlEncode(groupCode)%>&userName=<%=StrUtil.UrlEncode(name)%>'">删除</a>-->
      </td>
    </tr>
<%}%>
  </tbody>
</table>
<table width="253" align="center" class="percent80">
  <tr>
    <td colspan="7" align="left">
      <input class="btn" title="选择人员" onclick="openWinUsers()" type="button" value="选择" />
	  &nbsp;&nbsp;    
      <input class="btn" type="button" value="删除" onclick="delBatch()" />
      <input type="hidden" name="op" value="delBatch" />
      <input type="hidden" name="group_code" value="<%=groupCode%>" /></td>
  </tr>
</table>
</form>
<form name="form1" style="display:none" action="user_group_user.jsp?op=add" method="post">
<table class="tabStyle_1 percent80" width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="center" class="tabStyle_1_title">添加用户组中的用户</td>
  </tr>
  <tr>
    <td align="center"><input name="users" id="users" type="hidden" value="">
	<input name="group_code" type="hidden" value="<%=groupCode%>">
      <textarea name="userRealNames" cols="50" rows="5" readonly wrap="yes" id="userRealNames"></textarea></td>
  </tr>
  <tr>
    <td align="center"><span class="TableData">
  &nbsp;&nbsp;
  <input class="btn" title="清空收件人" onclick="form1.users.value='';form1.userRealNames.value=''" type="button" value="清空" name="button" />
      </span>
      &nbsp;&nbsp;
      <input class="btn" type="submit" value="确定"></td></tr>
</table>
</form>
</body>
<script>
function delBatch(){
    var checkedboxs = 0;
	var checkboxboxs = document.getElementsByName("ids");
	
	if (checkboxboxs!=null)	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked){
			   checkedboxs = 1;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].checked){
			   checkedboxs = 1;
			}
		}
	}

	if (checkedboxs==0){
	    alert("请先选择记录！");
		return;
	}
	if (confirm("您确定要删除么？")) {
		formGroupUser.action = "user_group_user.jsp";
		formGroupUser.op.value = "delBatch";
		formGroupUser.submit();
	}
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}
</script>
</html>