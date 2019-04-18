<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.account.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = StrUtil.getNullString(request.getParameter("op"));
String roleCode = ParamUtil.get(request, "role_code").trim();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>管理角色中的用户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

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
	selUserNames = $('#form1 #users').val();
	selUserRealNames = $('#form1 #userRealNames').val();
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',800,600);
}

function setUsers(users, userRealNames) {
	$('#users').val(users);
	$('#userRealNames').val(userRealNames);
	if (users=="")
		return;
	form1.submit();
}
</script>
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
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要删除么？","提示",function(r){
		if(!r){return;}
		else{
			formRoleUser.action = "user_role_user.jsp";
			formRoleUser.op.value = "delBatch"; 
			formRoleUser.submit();
		}
	})
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

function sortUser() {
	window.location.href = "user_role_user.jsp?op=sortUser&role_code=<%=StrUtil.UrlEncode(roleCode)%>";
}

function moveTo(userName, realName) {
	$("#dlg").dialog({
		title: "移动" + realName + "的位置",
		modal: true,
		// bgiframe:true,
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				if ($("#targetUser").val()=="") {
					jAlert("请选择用户！","提示");
				}
				else {
					if (userName==$("#targetUser").val()) {
						jAlert("请选择别的用户！","提示");
						return;
					}
					$("#userName").val(userName);
					$("#frmMove").submit();
					$(this).dialog("close");
				}
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true,
		width:300					
		});
}

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
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'>
<img src='../images/loading.gif' />
</div>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String sql = "select name,realname,gender from users order by name";

String strWhat = "";
if (!roleCode.equals("")) {
	sql = "select u.name,u.realName,u.gender,r.orders from users u, user_of_role r where r.roleCode=" + StrUtil.sqlstr(roleCode) + " and r.userName=u.name order by r.orders asc";
	RoleDb rd = new RoleDb();
	rd = rd.getRoleDb(roleCode);
	strWhat = rd.getDesc();
}

if (op.equals("add")) {
	String userNames = ParamUtil.get(request, "users");
	String[] users = StrUtil.split(userNames, ",");
	if (users==null) {
		out.print(StrUtil.jAlert_Back("请选择用户！","提示"));
		return;
	} else {
		RoleDb ugd = new RoleDb();
		ugd.addUsers(roleCode, users);
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode)));
		return;
	}
}
else if (op.equals("modify")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	int orders = ParamUtil.getInt(request, "orders");
	String userName = ParamUtil.get(request, "userName");
	sql = "update user_of_role set orders=? where userName=? and roleCode=?";
	JdbcTemplate jt = new JdbcTemplate();
	boolean re = jt.executeUpdate(sql, new Object[]{new Integer(orders), userName, roleCode})==1;
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode)));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("del")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	String userName = ParamUtil.get(request, "userName");
	RoleDb rd = new RoleDb();
	boolean re = rd.delUser(roleCode, userName);
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode)));
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("delBatch")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	String[] ids = ParamUtil.getParameters(request, "ids");
	if (ids==null) {
		out.print(StrUtil.jAlert_Back("请选择记录！","提示"));
		return;
	}
	RoleDb rd = new RoleDb();
	for (int i=0; i<ids.length; i++) {
		String[] ary = StrUtil.split(ids[i], "\\|");
		String rCode = ary[0];
		String uName = ary[1];
		rd.delUser(rCode, uName);
		UserDb ud = new UserDb(uName);
		RoleDb[] roles = ud.getRoles();
		int order = 0;
		for (RoleDb role : roles) {
			if (role.getOrders() > order) {
				order = role.getOrders();
			}
		}
		ud.setDuty(order + "");
		ud.save();
	}
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode)));
	return;
}
else if (op.equals("sortUser")) {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	RoleUser ru = new RoleUser();
	Iterator ir = ru.list(roleCode).iterator();
	int k = 1;
	while (ir.hasNext()) {
		ru = (RoleUser)ir.next();
		ru.setOrders(k);
		ru.save();
		k++;
	}
	%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode)));
	return;
}
else if (op.equals("move")) {
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		String direction = ParamUtil.get(request, "direction");
		String userName = ParamUtil.get(request, "userName");
		RoleUser ru = new RoleUser(userName, roleCode);
		ru.load();
		boolean re = ru.move(direction);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	response.sendRedirect("user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode));
	return;
}
else if (op.equals("moveTo")) {
	try {%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
		String userName = ParamUtil.get(request, "userName");
		String targetUser = ParamUtil.get(request, "targetUser");
		int pos = ParamUtil.getInt(request, "pos");
		RoleUser ru = new RoleUser(userName, roleCode);
		ru.load();
		boolean re = ru.moveTo(targetUser, pos);
		%>
	<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	response.sendRedirect("user_role_user.jsp?role_code=" + StrUtil.UrlEncode(roleCode));
	return;
}
%>
<%@ include file="user_role_op_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="tabTitle" width="100%" align="center" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td align="center">属于角色&nbsp;<%=strWhat%>&nbsp;的用户</td>
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
<form id="formRoleUser" name="formRoleUser" action="user_role_user.jsp" method="post">
<table width="253" align="center" class="percent80">
    <tr>
      <td colspan="7" align="left">
	  <input class="btn" title="选择并增加人员" onclick="openWinUsers()" type="button" value="增加" />  
      &nbsp;&nbsp;    
      <input class="btn" type="button" value="删除" onclick="delBatch()" />
      &nbsp;&nbsp;<input id="btnSort" class="btn" type="button" value="排序" onclick="sortUser()" style="display:none" />
      </td>
    </tr>
</table>
<input type="hidden" name="op" value="delBatch" />
<input type="hidden" name="role_code" value="<%=roleCode%>" />
<table id="mainTable" width="98%" align="center" cellPadding="3" cellSpacing="0" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td width="3%" align="center" noWrap class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
      <td width="5%" align="center" noWrap class="tabStyle_1_title" style="display:none">序号</td>
      <td width="15%" align="center" noWrap class="tabStyle_1_title">用户名</td>
      <td width="17%" align="center" noWrap class="tabStyle_1_title">真实姓名</td>
	  <%
	  com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();		
      boolean isUseAccount = oacfg.getBooleanProperty("isUseAccount");
      if (isUseAccount) {
      %>            
      <td width="12%" class="tabStyle_1_title">工号</td>		    
      <%} %>      
      <td width="7%" align="center" noWrap class="tabStyle_1_title">性别</td>
      <td width="24%" align="center" class="tabStyle_1_title">所属部门</td>
      <td width="17%" align="center" noWrap class="tabStyle_1_title">操作</td>
    </tr>
    <%
String userNames = "";
String userRealNames = "";
DeptMgr dm = new DeptMgr();		
int m=1;
boolean isNeedSort = false;
String opts = "";
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
	
	opts += "<option value='" + name + "'>" + realname + "</option>";

	genderdesc = rr.getInt(3)==0?"男":"女";
	int orders = rr.getInt(4);
	
	if (!isNeedSort && m!=orders)
		isNeedSort = true;
	m++;
%>
    <tr>
      <td align="center"><input type="checkbox" name="ids" value="<%=roleCode + "|" + name%>" /></td>
      <td align="center" style="display:none">
	  <input id="orders<%=m%>" name="orders" value="<%=orders%>" type="hidden" style="width:30px" size="2" />
      <%=orders%>
      </td>
      <td>
      <!-- <a href="javascript:;" onclick="addTab('<%=name%>', '<%=request.getContextPath()%>/admin/user_op.jsp?op=edit&name=<%=StrUtil.UrlEncode(name)%>')"><%=name%></a> -->
      <a href="javascript:;" onclick="addTab('<%=realname%>', '<%=request.getContextPath()%>/admin/organize/user_edit.jsp?name=<%=StrUtil.UrlEncode(name)%>')"><%=name%></a>
      </td>
      <td><%=realname%></td>      
	  <%if (isUseAccount) {
      AccountDb ad = new AccountDb();
      ad = ad.getUserAccount(name);
      String aName = "";
      if (ad!=null) {
          aName = ad.getName();
      }
      %>           
      <td><%=aName %></td>    
      <%} %> 
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
      <!--
      <a href="javascript:;" onclick="if (confirm('您确定要修改<%=realname%>的序号么？')) window.location.href='user_role_user.jsp?op=modify&userName=<%=StrUtil.UrlEncode(name)%>&role_code=<%=StrUtil.UrlEncode(roleCode)%>&orders=' + o('orders<%=m%>').value">编辑</a>
      &nbsp;&nbsp;
      <a onclick="return confirm('您确定要删除么？')" href="user_role_user.jsp?op=del&role_code=<%=StrUtil.UrlEncode(roleCode)%>&userName=<%=StrUtil.UrlEncode(name)%>">删除</a>
      &nbsp;&nbsp;
      -->
      <a href="user_role_user.jsp?op=move&direction=up&userName=<%=StrUtil.UrlEncode(name)%>&role_code=<%=StrUtil.UrlEncode(roleCode)%>">上移</a>
      &nbsp;&nbsp;
      <a href="user_role_user.jsp?op=move&direction=down&userName=<%=StrUtil.UrlEncode(name)%>&role_code=<%=StrUtil.UrlEncode(roleCode)%>">下移</a>
      &nbsp;&nbsp;
      <a href="javascript:;" onclick="moveTo('<%=name%>', '<%=realname%>')">移至</a>
      </td>
    </tr>
<%}%>
  </tbody>
</table>
</form>
<div style="display:none" id="dlg">
<form id="frmMove" action="user_role_user.jsp" method="post">
移至用户
<select id="targetUser" name="targetUser">
<option value="">请选择</option>
<%=opts%>
</select>
<input id="pos" name="pos" checked value="0" type="radio" />之前
<input id="pos" name="pos" value="1" type="radio" />之后
<input name="role_code" type="hidden" value="<%=roleCode%>" />
<input name="op" type="hidden" value="moveTo" />
<input id="userName" name="userName" type="hidden" value="" />
</form>
</div>
<br />
<%
UserGroupDb ugd = new UserGroupDb();
Iterator ir = ugd.getGroupsOfRole(roleCode).iterator();
while (ir.hasNext()) {
	ugd = (UserGroupDb)ir.next();
%>
<table align="center" class="percent80"><tr><td>用户组：<a href="user_group_user.jsp?group_code=<%=ugd.getCode()%>"><%=ugd.getDesc()%></a></td></tr></table>
<table width="98%" align="center" cellpadding="3" cellspacing="0" class="tabStyle_1 percent80">
  <tbody>
    <tr>
      <td width="5%" align="center" nowrap="nowrap" class="tabStyle_1_title">&nbsp;</td>
      <td width="11%" align="center" nowrap="nowrap" class="tabStyle_1_title">序号</td>
      <td width="14%" align="center" nowrap="nowrap" class="tabStyle_1_title">用户名</td>
      <td width="16%" align="center" nowrap="nowrap" class="tabStyle_1_title">真实姓名</td>
      <td width="8%" align="center" nowrap="nowrap" class="tabStyle_1_title">性别</td>
      <td width="29%" align="center" class="tabStyle_1_title">所属部门</td>
      <td width="17%" align="center" nowrap="nowrap" class="tabStyle_1_title">操作</td>
    </tr>
<%
Iterator irUser = ugd.getAllUserOfGroup().iterator();
int k=0;
while (irUser.hasNext()) {
 	UserDb user = (UserDb)irUser.next();
	name = user.getName();
	realname = user.getRealName();

	genderdesc = user.getGender()==0?"男":"女";
	k++;
%>
    <tr>
      <td align="center">&nbsp;</td>
      <td align="center"><%=k%></td>
      <td><a href="user_op.jsp?op=edit&amp;name=<%=StrUtil.UrlEncode(name)%>"><%=name%></a></td>
      <td><%=realname%></td>
      <td align="center"><%=genderdesc%></td>
      <td align="left"><%
			DeptUserDb du = new DeptUserDb();
			Iterator ir2 = du.getDeptsOfUser(name).iterator();
			int n = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				}
				else
					deptName = dd.getName() + "&nbsp;&nbsp;";
				if (n==0) {
					out.print(deptName);
				}
				else {
					out.print("，" + deptName);
				}
				n++;
			} 
			%></td>
      <td align="center"><a href="user_op.jsp?op=edit&amp;name=<%=StrUtil.UrlEncode(name)%>">编辑</a></td>
    </tr>
    <%}%>
  </tbody>
</table>
<%}%>
<form id="form1" name="form1" style="display:none" action="user_role_user.jsp?op=add" method="post">
<table class="tabStyle_1 percent80" width="80%" border="0" align="center">
  <tr>
    <td align="center" class="tabStyle_1_title">添加角色中的用户</td>
  </tr>
  <tr>
    <td align="center">
    <input name="users" id="users" type="hidden" value="" />
	<input name="role_code" type="hidden" value="<%=roleCode%>" />
    <textarea name="userRealNames" cols="50" rows="5" readonly wrap="yes" id="userRealNames" />
    <input class="btn" type="submit" value="确定" />
    </td>
  </tr>
</table>
</form>

<script>
$(function() {
<%if (isNeedSort) {%>
$('#btnSort').show();
<%}%>
});
</script>
</body>
</html>