<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

if (userName.equals(""))
	userName = privilege.getUser(request);

if (userName.equals(UserDb.SYSTEM)) {
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("add")) {
	String groupName = ParamUtil.get(request, "groupName");
	String icon = ParamUtil.get(request, "icon");
	String depts = ParamUtil.get(request, "depts");
	String roles = ParamUtil.get(request, "roles");
	int isFixed = ParamUtil.getInt(request, "isFixed", 1);
	JSONObject json = new JSONObject();
	if (groupName.equals("")) {
		json.put("ret", "0");
		json.put("msg", "请输入门户名称");
		out.print(json);
		return;
	}
	
	PortalDb smgd = new PortalDb();
	boolean re = smgd.create(new JdbcTemplate(), new Object[]{userName, groupName, new Integer(smgd.getNextOrders(userName)), new Integer(PortalDb.SYSTEM_ID_NONE),icon,isFixed,depts,roles});
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);		
		return;
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);		
		return;
	}
}
else if (op.equals("edit")) {
	String groupName = ParamUtil.get(request, "groupName");
	String icon = ParamUtil.get(request, "icon");
	String depts = ParamUtil.get(request, "depts");
	String roles = ParamUtil.get(request, "roles");	
	int isFixed = ParamUtil.getInt(request, "isFixed", 1);
	
	JSONObject json = new JSONObject();
	if (groupName.equals("")) {
		json.put("ret", "0");
		json.put("msg", "请输入门户名称");
		out.print(json);		
		return;
	}
	
	long groupId = ParamUtil.getLong(request, "groupId", -1);
	if (groupId==-1) {
		json.put("ret", "0");
		json.put("msg", "标识不能为空！");
		out.print(json);		
		return;
	}
	
	int orders = ParamUtil.getInt(request, "orders");
	
	PortalDb smd = new PortalDb();
	smd = (PortalDb)smd.getQObjectDb(new Long(groupId));
	boolean re = smd.save(new JdbcTemplate(), new Object[]{groupName, new Integer(orders), icon, isFixed, depts, roles, new Long(groupId)});
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);		
		return;
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);		
		return;
	}
}
else if (op.equals("sort")) {
	JSONObject json = new JSONObject();	
	String strIds = ParamUtil.get(request, "ids");
	if (strIds.equals("")) {
		json.put("ret", "0");
		json.put("msg", "标识不能为空！");
		out.print(json);		
		return;
	}
	
	String[] ids = StrUtil.split(strIds, ",");
	PortalDb smd = new PortalDb();
	for (int i=0; i<ids.length; i++) {
		smd = (PortalDb)smd.getQObjectDb(new Long(StrUtil.toLong(ids[i])));
		smd.set("orders", new Integer(i+1));
		smd.save();
	}
	json.put("ret", "1");
	json.put("msg", "操作成功！");
	out.print(json);		
	return;
}
else if (op.equals("del")) {
	JSONObject json = new JSONObject();
	
	long groupId = ParamUtil.getLong(request, "groupId", -1);
	if (groupId==-1) {
		json.put("ret", "0");
		json.put("msg", "标识不能为空！");
		out.print(json);		
		return;
	}
	
	PortalDb smd = new PortalDb();
	smd = (PortalDb)smd.getQObjectDb(new Long(groupId));
	boolean re = smd.del();
	if (re) {
		json.put("ret", "1");
		json.put("msg", "操作成功！");
		out.print(json);		
		return;
	}
	else {
		json.put("ret", "0");
		json.put("msg", "操作失败！");
		out.print(json);
		return;
	}
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>门户管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_slidemenu.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/tabpanel/Toolbar.js" type="text/javascript"></script>
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>

<script>
var curIndex = -1;

$(function() {	
	$( "#sortable" )
	  .sortable({ 
	  	  handle: ".handle", // 注释掉使可以拖动，否则限制仅具有class=handle的元素上才能拖动，不能注释，否则会使得无法选择
		  stop: function() {
			  	var ids = "";
			  	var orders = "1";
				$("#sortable li").each(function () {
					if (ids=="") {
						ids = $(this).attr("groupId");
						orders = $(this).attr("orders");
					}
					else
						ids += "," + $(this).attr("groupId");
				});
				if (orders!="1") {
					jAlert("默认桌面不允许被拖动", "提示");
					$(this).sortable("cancel");
					return;
				}

				$.ajax({
					type: "post",
					url: "portal.jsp",
					data: {
						op: "sort",
						ids: ids
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#sortable').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
							// jAlert_Redirect(data.msg, "提示", "portal.jsp");
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#sortable').hideLoading();				
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});

			}
	  	})
	  	.selectable({
		  stop: function() {
				$( ".ui-selected", this ).each(function() {
					curIndex = $( "#sortable li" ).index( this );
				});
			}
		})
		.find( "li" )
		  .addClass( "ui-corner-all" );
	
	// 实现单选
	$("#sortable").selectable({
		  selected: function(event, ui) {
				$(ui.selected).siblings().removeClass("ui-selected");
		  }
	});
});
</script>
</head>
<body>
<div id="toolbar" style="height:25px; clear:both; margin-bottom:20px"></div>
<div style="margin:10px">
<ul id="sortable">
<%
if (op.equals("restorePortal")) {
	PortalDb pd = new PortalDb();
	pd.init(userName);
	out.print(StrUtil.jAlert_Redirect("操作成功!","提示", "portal.jsp"));
	return;
}
PortalDb smgd = new PortalDb();
String sql = "select id from " + smgd.getTable().getName() + " where user_name=? order by orders";
Iterator ir = smgd.list(sql, new Object[]{userName}).iterator();
while (ir.hasNext()) {
	smgd = (PortalDb)ir.next();
	String depts = StrUtil.getNullStr(smgd.getString("depts"));
	String deptNames = "";
	if (!"".equals(depts)) {
		String[] ary = StrUtil.split(depts, ",");
		if (ary!=null) {
			DeptDb dd = new DeptDb();
			for (int i=0; i<ary.length; i++) {
				if ("".equals(deptNames)) {
					deptNames = dd.getDeptDb(ary[i]).getName();
				}
				else {
					deptNames += "," + dd.getDeptDb(ary[i]).getName();
				}
			}
		}
	}
	String roles = StrUtil.getNullStr(smgd.getString("roles"));
	String roleNames = "";
	if (!"".equals(roles)) {
		String[] ary = StrUtil.split(roles, ",");
		if (ary!=null) {
			RoleDb rd = new RoleDb();
			for (int i=0; i<ary.length; i++) {
				if ("".equals(roleNames)) {
					roleNames = rd.getRoleDb(ary[i]).getDesc();
				}
				else {
					roleNames += "," + rd.getRoleDb(ary[i]).getDesc();
				}
			}
		}
	}	
	int isFixed = smgd.getInt("is_fixed");
	int orders = smgd.getInt("orders");
	%>
	<li class='portalLiBg' title="按下图标可拖动，双击图标可编辑门户" groupId="<%=smgd.getLong("id")%>" groupName="<%=smgd.getString("name")%>" icon="<%=smgd.getString("icon")%>" isFixed="<%=isFixed %>" depts="<%=depts%>" deptNames="<%=deptNames %>" roles="<%=roles %>" roleDescs="<%=roleNames%>" orders="<%=orders%>">
	<img src="<%=SkinMgr.getSkinPath(request)%>/icons/<%=smgd.get("icon") %>" width='24' height='24' class='portalIcon handle'/>
	<span><%=smgd.getString("name")%></span>
	</li>
	<%
}
%>
</ul>
</div>

<div id="dlg" style="display:none">
<form id="form1">
名称&nbsp;&nbsp;&nbsp;&nbsp;<input id="groupName" name="groupName" onKeyDown="onGroupNamePresskey()" /><br/>
图标&nbsp;&nbsp;&nbsp;&nbsp;<input name="icon" value="" id="icon"/>&nbsp;<input name="button" class="btn" type="button" onclick="openWin('<%=request.getContextPath()%>/admin/menu_icon_sel.jsp', 800, 600)" value="选择" /><br/>
<div id="iconDiv" class="dialog"><img src="" id="iconImg" width='24' height='24' class='imgbg'/></div>
固定&nbsp;&nbsp;&nbsp;&nbsp;<input id="isFixed" name="isFixed" checked="checked" value="1" type="checkbox" />&nbsp;不允许用户自定义<br/>

部门&nbsp;&nbsp;&nbsp;&nbsp;<textarea name="deptNames" cols="45" rows="3" readOnly wrap="yes" id="deptNames"></textarea>
<input class="btn" title="添加" onclick="openWinDepts(form1)" type="button" value="选择" />   
<input type="hidden" id="depts" name="depts" value=""><br/>
角色&nbsp;&nbsp;&nbsp;&nbsp;<textarea id="roleDescs" name=roleDescs cols="45" rows="3"></textarea>
<input id="roles" name="roles" type=hidden />
<input class="btn" type="button" onClick="openWinUserRoles(form1)" value="选择" />
<input id="groupId" name="groupId" type="hidden" />
<input id="orders" name="orders" type="hidden" />
</form>
</div>

</body>
<script>
function selIcon(icon) {
	$("#iconImg").show();
	o("icon").value = icon;
	$("#iconImg").attr("src","<%=SkinMgr.getSkinPath(request)%>/icons/" + icon);
}
function onGroupNamePresskey() {
	if (window.event.keyCode==13) {
		window.event.keyCode = 9;
	}
}

var groupNameCtl = new LiveValidation('groupName');
groupNameCtl.add(Validate.Presence, { failureMessage:'请填写名称！'} );
groupNameCtl.add(Validate.Length, { maximum: 6} );

var toolbar;

toolbar = new Toolbar({
  renderTo : 'toolbar',
  //border: 'top',
  items : [
  {
	type : 'button',
	text : '添加',
	title: '添加',
	bodyStyle : 'add',
	useable : 'T',
	handler : function(){
		$("#iconImg").hide();
		$("#icon").val("");
		$("#groupName").val("");
		$("#dlg").dialog({
			title:"请输入名称",
			modal: true,
			width: 600,
			height: 590,
			// bgiframe:true,
			buttons: {
				"取消": function() {
					$(this).dialog("close");
				},
				"确定": function() {
					if (!LiveValidation.massValidate(groupNameCtl.formObj.fields))
						return false;
					$.ajax({
						type: "post",
						url: "portal.jsp",
						contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
						data: {
							op: "add",
							userName: "<%=userName%>",
							icon:o("icon").value,
							groupName: o("groupName").value,
							isFixed: o("isFixed").checked?1:0,
							depts: o("depts").value,
							roles: o("roles").value
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							$('#dlg').showLoading();
						},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="0") {
								jAlert(data.msg, "提示");
							}
							else {
								jAlert_Redirect(data.msg, "提示", "portal.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
							}
						},
						complete: function(XMLHttpRequest, status){
							$('#dlg').hideLoading();				
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});					
					
					$(this).dialog("close");
				}
			},
			closeOnEscape: true,
			draggable: true,
			resizable:true
			});
	}
  }
  ,'-',{
	type : 'button',
	text : '修改',
	title: '修改',
	bodyStyle : 'edit',
	useable : 'T',
	handler : function(){
		if (curIndex==-1) {
			jAlert("请选择门户！", "提示");
			return false;
		}
		$("#groupName").val($("#sortable").children().eq(curIndex).attr("groupName"));
		$("#groupId").val($("#sortable").children().eq(curIndex).attr("groupId"));
		$("#orders").val($("#sortable").children().eq(curIndex).attr("orders"));
		$("#icon").val($("#sortable").children().eq(curIndex).attr("icon"));
		o("isFixed").checked = ($("#sortable").children().eq(curIndex).attr("isFixed")==1)?"checked":"";
		$("#depts").val($("#sortable").children().eq(curIndex).attr("depts"));
		$("#deptNames").val($("#sortable").children().eq(curIndex).attr("deptNames"));
		$("#roles").val($("#sortable").children().eq(curIndex).attr("roles"));
		$("#roleDescs").val($("#sortable").children().eq(curIndex).attr("roleDescs"));
		$("#iconImg").show();
		$("#iconImg").attr("src","<%=SkinMgr.getSkinPath(request)%>/icons/" + $("#icon").val());
		$("#dlg").dialog({
			title: "请输入组名称",
			modal: true,
			width: 600,
			height: 590,
			// bgiframe:true,
			buttons: {
				"取消": function() {
					$(this).dialog("close");
				},
				"确定": function() {
					if (!LiveValidation.massValidate(groupNameCtl.formObj.fields))
						return false;						
					if (o("groupName").value=="") {
						jAlert("请输入门户名称", "提示");
						return false;
					}
					
					$.ajax({
						type: "post",
						url: "portal.jsp",
						contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
						data: {
							op: "edit",
							groupId: o("groupId").value,
							groupName: o("groupName").value,
							icon:o("icon").value,
							depts:o("depts").value,
							isFixed: o("isFixed").checked?1:0,
							roles:o("roles").value,
							orders: o("orders").value
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							$('#dlg').showLoading();
						},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="0") {
								jAlert(data.msg, "提示");
							}
							else {
								jAlert_Redirect(data.msg, "提示", "portal.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
							}
						},
						complete: function(XMLHttpRequest, status){
							$('#dlg').hideLoading();				
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});					
					
					$(this).dialog("close");
				}
			},
			closeOnEscape: true,
			draggable: true,
			resizable:true
			});		
	}
  } ,'-',{
	type : 'button',
	text : '删除',
	title: '删除',
	bodyStyle : 'del',
	useable : 'T',
	handler : function(){
		if (curIndex==-1) {
			jAlert("请选择门户！", "提示");
			return false;
		}
		// if (!confirm("您确定要删除么？"))
		// 	return false;
		
		jConfirm('您确定要删除么？', '提示', function(r) {
			if (r) {
				$.ajax({
					type: "post",
					url: "portal.jsp",
					data: {
						op: "del",
						groupId: $("#sortable").children().eq(curIndex).attr("groupId")
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#dlg').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
							jAlert_Redirect(data.msg, "提示", "portal.jsp?userName=<%=StrUtil.UrlEncode(userName)%>");
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#dlg').hideLoading();				
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});			
			}
		});
	}
  }
  <%if (privilege.isUserPrivValid(request, "admin")) {%>
   ,'-',{
	type : 'button',
	text : '图片轮播',
	title: '图片轮播',
	bodyStyle : 'flashImage',
	useable : 'T',
	handler : function(){
		addTab('图片轮播', '<%=request.getContextPath()%>/cms/flash_image_list.jsp');
	}

  }
  <%}%>
  <%if (!userName.equals(UserDb.SYSTEM)) {%>
   ,'-',{
   type : 'button',
	text : '恢复默认',
	title: '恢复默认',
	bodyStyle : 'recover',
	useable : 'T',
	handler : function(){
		jConfirm('您确定要恢复默认门户么？','提示',function(r){
			if(!r){return;}
			else{
				window.location.href='portal.jsp?op=restorePortal';
			}
		})
	}
  }
  <%}%>
  ]
});

toolbar.render();

$("#sortable li").dblclick(function(){
	addTab($(this).attr("groupName"), "user/desktop_setup.jsp?portalId=" + $(this).attr("groupid"));
});

var curForm;
function getDepts() {
	return o("depts").value;
}

function getUserRoles() {
	return curForm.roles.value;
}

function openWinDepts(formDept) {
	curForm = formDept;
	var ret = showModalDialog('../dept_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:500px;dialogHeight:480px;status:no;help:no;')
	if (ret==null)
		return;
	formDept.deptNames.value = "";
	formDept.depts.value = "";

	for (var i=0; i<ret.length; i++) {
		if (formDept.deptNames.value=="") {
			formDept.depts.value += ret[i][0];
			formDept.deptNames.value += ret[i][1];
		}
		else {
			formDept.depts.value += "," + ret[i][0];
			formDept.deptNames.value += "," + ret[i][1];
		}
	}
}

function openWinUserRoles(formDept) {
	curForm = formDept;
	var ret = showModalDialog('../userrole_multi_sel.jsp',window.self,'dialogWidth:500px;dialogHeight:480px;status:no;help:no;')
	if (ret==null)
		return;
	formDept.roles.value = "";
	formDept.roleDescs.value = "";
	for (var i=0; i<ret.length; i++) {
		if (formDept.roleDescs.value=="") {
			formDept.roles.value += ret[i][0];
			formDept.roleDescs.value += ret[i][1];
		}
		else {
			formDept.roles.value += "," + ret[i][0];
			formDept.roleDescs.value += "," + ret[i][1];
		}
	}
}

function setRoles(roles, descs) {
	form1.roles.value = roles;
	form1.roleDescs.value = descs
}
</script>
</html>