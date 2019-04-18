<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

PersonGroupTypeDb pgtd = new PersonGroupTypeDb();
Vector typeV = pgtd.listOfUser(privilege.getUser(request));
if (typeV.size()==0) {
	response.sendRedirect("persongroup_type_list.jsp");
	return;
}

int groupId = ParamUtil.getInt(request, "groupId", -1);
if (groupId == -1) {
	groupId = ((PersonGroupTypeDb)typeV.elementAt(0)).getId();
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划类型管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script>
function form_onsubmit() {
	jConfirm("您确定要删除么？", "提示",function(r){
		if(!r){return;}
		else{
			toolbar.setDisabled(1, true);				  
			$('#bodyBox').showLoading();				
			SubmitResult();
		}
	})
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请选择记录！", "提示");
		return false;
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

function openWinUsers() {
	showModalDialog('../user_multi_sel.jsp', window.self, 'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')
	if (form2.users.value!="")
		form2.submit();
}

function setUsers(users, userRealNames) {
	form2.users.value = users;
	form2.userRealNames.value = userRealNames;
}

function getSelUserNames() {
	return form2.users.value;
}

function getSelUserRealNames() {
	return form2.userRealNames.value;
}

</script>
</head>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("sel")) {
	String userNames = ParamUtil.get(request, "users");
	String[] ary = StrUtil.split(userNames, ",");
	if (ary==null) {
		out.print(StrUtil.jAlert_Back("请选择人员！", "提示"));
		return;
	}
	boolean re = false;
	boolean isErr = false;
	for (int i=0; i<ary.length; i++) {
		try {
			PersonGroupUserDb pgud = new PersonGroupUserDb();
			re = pgud.create(new JdbcTemplate(), new Object[]{new Integer(groupId), ary[i], new Integer(0)});
		}
		catch (ResKeyException e) {
			isErr = true;
		}
	}
	if (isErr) {
		out.print(StrUtil.jAlert_Redirect("请检查是否已存在相同记录！", "提示", "persongroup_user_list.jsp?groupId=" + groupId));
	}
	else if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_user_list.jsp?groupId=" + groupId));
	return;
}
else if (op.equals("del")) {
	PersonGroupUserDb pgud = new PersonGroupUserDb();
	
	String[] ary = ParamUtil.getParameters(request, "ids"); // StrUtil.split(ids, ",");
	if (ary==null) {
		out.print("请选择记录！");
		return;
	}
	
	boolean re = false;
	for (int i=0; i<ary.length; i++) {
		pgud = (PersonGroupUserDb)pgud.getQObjectDb(new Integer(StrUtil.toInt(ary[i])));
		re = pgud.del();
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_user_list.jsp?groupId=" + groupId));
	return;
}
%>
<body>
<%@ include file="persongroup_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form action="persongroup_user_list.jsp?op=sel" method="post" id="form2" name="form2" onSubmit="return form_onsubmit()">
<table width="97%" align="center">
  <tr><td align="center">
<select id="groupId" name="groupId" onchange="window.location.href='persongroup_user_list.jsp?groupId=' + this.value">
<%
Iterator ir = typeV.iterator();
while (ir.hasNext()) {
	pgtd = (PersonGroupTypeDb)ir.next();
%>
<option value="<%=pgtd.getId()%>"><%=pgtd.getName()%></option>
<%}%>
</select>
<script>
o("groupId").value = "<%=groupId%>";
</script>
&nbsp;&nbsp;
<input type="button" class="btn" onclick="openWinUsers()" value="添加用户" />
<input type="hidden" id="users" name="users" />
<input type="hidden" id="userRealNames" name="userRealNames" />
</td></tr></table>
</form>
<form action="persongroup_user_list.jsp?op=del" method="post" id="form1" name="form1" onSubmit="return form_onsubmit()">
<table id="grid" cellSpacing="0" cellPadding="0" width="1028">
	<thead>
  <tr>
    <th width="300" style="cursor:pointer">部门</th>
    <th width="300" style="cursor:pointer">用户</th>
    <th width="300" style="cursor:pointer">手机</th>
    <th width="300" style="cursor:pointer">操作</th>
  </tr>
  </thead>
  <tbody>
  <%
  UserMgr um = new UserMgr();
  DeptMgr dm = new DeptMgr();		
  DeptUserDb du = new DeptUserDb();

  PersonGroupUserDb pgud = new PersonGroupUserDb();
  String sql = "select id from " + pgud.getTable().getName() + " where group_id=? order by orders";
  ir = pgud.list(sql, new Object[]{groupId}).iterator();
  while (ir.hasNext()) {
	pgud = (PersonGroupUserDb)ir.next();%>
  <tr>
    <td>
<%
	UserDb user = um.getUserDb(pgud.getString("user_name"));
	Iterator ir2 = du.getDeptsOfUser(pgud.getString("user_name")).iterator();
	int k = 0;
	while (ir2.hasNext()) {
		DeptDb dd = (DeptDb)ir2.next();
		String deptName = "";
		if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {					
			deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
		}
		else
			deptName = dd.getName();
		if (k==0) {
			out.print(deptName);
		}
		else {
			out.print("，&nbsp;" + deptName);
		}
		k++;
	} 
	%>    
    </td>
    <td align="center">
    <%if (user.isLoaded()) {%>
    <a href="javascript:;" onclick="addTab('<%=user.getRealName() %>', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a>
    <%}else{%>
    用户不存在
	<%}%>
    </td>
    <td align="center"><%=user.getMobile()%></td>
    <td align="center"><a href="javascript:;" onclick="jConfirm('您确定要删除么？','提示',function(r){if(!r){return;} else {location.href='?op=del&ids=<%=pgud.getInt("id")%>'}})">删除</a>
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="addTab('短消息', '<%=request.getContextPath()%>/message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(pgud.getString("user_name"))%>')">短消息</a>
	<%
    if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms") && !StrUtil.getNullStr(user.getMobile()).equals("")) {
    %>    
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="addTab('短信', '<%=request.getContextPath()%>/message_oa/sms_send_message.jsp?receiver=<%=StrUtil.UrlEncode(user.getName())%>')">短信</a>
    <%}%>
    </td>
  </tr>
  <%}%>
</tbody>
</table>
</form>
</body>
<script>
function sendMessage() {
  var ids = "";
  var checkboxboxs = document.getElementsByName("ids");
  if (checkboxboxs!=null)
  {
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  if (checkboxboxs[i].checked) {
		  	if (ids=="")
				ids = checkboxboxs[i].getAttribute("userName");
			else
				ids += "," + checkboxboxs[i].getAttribute("userName");
		  }
	  }
	  addTab("短消息", "<%=request.getContextPath()%>/message_oa/message_frame.jsp?op=send&receiver=" + ids);
  }	
}

function sendSMS() {
  var ids = "";
  var checkboxboxs = document.getElementsByName("ids");
  if (checkboxboxs!=null)
  {
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  if (checkboxboxs[i].checked) {
		  	if (ids=="")
				ids = checkboxboxs[i].getAttribute("userName");
			else
				ids += "," + checkboxboxs[i].getAttribute("userName");
		  }
	  }
	  addTab("短消息", "<%=request.getContextPath()%>/message_oa/sms_send_message.jsp?receiver=" + ids);
  }	
}

$(document).ready(function() {
	flex = $("#grid").flexigrid
	(
		{
		<%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
        buttons : [{name: '短信', bclass: 'SMS', onpress : action}],
    <%}%>
		/*
		searchitems : [
			{display: 'ISO', name : 'iso'},
			{display: 'Name', name : 'name', isdefault: true}
			],
		sortname: "iso",
		sortorder: "asc",
		*/
		url: false,
		usepager: false,
		<%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
		checkbox: true,
		<%}else{%>
		checkbox: false,
		<%}%>
		
		useRp: false,
		
		// title: "通知",
		singleSelect: true,
		resizable: false,
		showTableToggleBtn: true,
		showToggleBtn: true,
		
		onReload: onReload,
		/*
		onRowDblclick: rowDbClick,
		onColSwitch: colSwitch,
		onColResize: colResize,
		onToggleCol: toggleCol,
		*/
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);	
});

function onReload() {
	window.location.reload();
}
function action(com, grid) {
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择记录!','提示');
			return;
		}
		
		if (selectedCount > 0)
		{
				$(".cth input[type='checkbox'][value!='on'][checked=true]", grid.bDiv).each(function(i) {
						if (ids=="")
							ids = $(this).val();
						else
							ids += "," + $(this).val();
				});
		}	
		addTab("短消息", "<%=request.getContextPath()%>/message_oa/sms_send_message.jsp?receiver=" + ids);

}
</script>
</html>
