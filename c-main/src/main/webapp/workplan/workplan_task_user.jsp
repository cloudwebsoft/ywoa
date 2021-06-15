<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
if(userName.equals("")){
	userName = privilege.getUser(request);
}
if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "orders";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "asc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划任务用户</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>

</head>
<body>
<%@ include file="workplan_show_inc_menu_top.jsp"%>
<script>
o("menu6").className="current";
</script>
<%
		String sql;
		String myname = privilege.getUser(request);
		String querystr = "";
		long taskId = ParamUtil.getLong(request, "taskId");
		WorkPlanTaskDb wptd = new WorkPlanTaskDb();
		wptd = (WorkPlanTaskDb)wptd.getQObjectDb(new Long(taskId));
		
		sql = "select id from work_plan_task_user where task_id=" + taskId;
		sql += " order by " + orderBy + " " + sort;

		String urlStr = "op=" + op + "&taskId=" + taskId;
		
		querystr = urlStr + "&orderBy=" + orderBy + "&sort=" + sort;
		
		int pagesize = ParamUtil.getInt(request, "pageSize", 100);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		WorkPlanTaskUserDb wptud = new WorkPlanTaskUserDb();
		
		ListResult lr = wptud.listResult(sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
		Iterator ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<table id="grid">
  <thead>
        <tr>
          <th width="36" style="cursor:pointer" abbr="ID">ID</th>          
          <th width="186" style="cursor:pointer" abbr="user_name">参与者</th>
          <th width="136" style="cursor:pointer" abbr="duration">使用率</th>
          <th width="116" style="cursor:pointer">工作日</th>
        </tr>
   </thead>
   <tbody>
      <%	
	  	int i = 0;
		WorkPlanDb wpd = new WorkPlanDb();
		wpd = wpd.getWorkPlanDb(wptd.getInt("work_plan_id"));
		UserMgr um = new UserMgr(); 
		com.redmoon.oa.workplan.Privilege workplanPvg = new com.redmoon.oa.workplan.Privilege();
		while (ir!=null && ir.hasNext()) {
			wptud = (WorkPlanTaskUserDb)ir.next();
			i++;
		%>
        <tr>
          <td align="center"><%=wptud.getLong("id")%></td>
          <td>
		  <%
			UserDb user = um.getUserDb(wptud.getString("user_name"));
		  %>
		  <a href="javascript:;" onclick="addTab('消息', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a>
          </td>
          <td align="center"><%=wptud.getInt("percent")%>&nbsp;%</td>
          <td align="center"><%=NumberUtil.round(wptud.getDouble("duration"), 1)%></td>
        </tr>
      <%
		}
%>
	</tbody>
</table>

<div id="taskDlg" style="display:none">
<form id="formAdd">
<table>
<tr>
  <td width="70">任务</td>
  <td width="290"><%=wptd.getString("name")%></td>
</tr>
<tr>
  <td width="70">工期</td>
  <td width="290"><%=wptd.getInt("duration")%>天</td>
</tr>
<tr>
  <td>参与者</td>
  <td>
  	<span id="select_user">
    <select id="user_name" name="user_name">
      <%
String[] principalAry = wpd.getPrincipals();
int len = principalAry==null?0:principalAry.length;
for (i=0; i<len; i++) {
  if (principalAry[i].equals(""))
      continue;
  UserDb user = um.getUserDb(principalAry[i]);
  %>
      <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
      <%
}

String[] userAry = wpd.getUsers();
len = userAry==null?0:userAry.length;
for (i=0; i<len; i++) {
  if (userAry[i].equals(""))
      continue;
  // 过滤掉负责人
  boolean isFound = false;
  for (int j=0; j<principalAry.length; j++) {
      if (principalAry[j].equals(userAry[i])) {
          isFound = true;
          break;
      }
  }
  if (isFound)
      continue;
  UserDb user = um.getUserDb(userAry[i]);
  %>
      <option value="<%=user.getName()%>"><%=user.getRealName()%></option>
      <%
}
%>
      </select></span>
      <span id="real_user" style="display:none"></span>
    <input id="taskId" name="taskId" value="<%=taskId%>" type="hidden" />
    </td>
</tr>
<tr>
  <td>按</td>
  <td>
  <input id="mode" name="mode" value="byPercent" type="radio" onclick="$('#trDuration').hide();$('#trPercent').show();" checked />使用率
  &nbsp;&nbsp;
  <input id="mode" name="mode" value="byDuration" type="radio" onclick="$('#trDuration').show();$('#trPercent').hide();" />工作日
  </td>
</tr>
<tr id="trPercent">
  <td>使用率</td>
  <td><input type="text" name="percent" id="percent" value="100" size="3" class="formElements" />
    &nbsp;% </td>
</tr>
<tr id="trDuration" style="display:none">
  <td>工作日</td>
  <td><input type="text" name="duration" id="duration" value="1" size="3" class="formElements" />天</td>
</tr>
</table>
</form>
</div>

<table id="searchTable"><tr><td valign="bottom">
&nbsp;&nbsp;任务：<%=wptd.getString("name")%>， 从&nbsp;<%=DateUtil.format(wptd.getDate("start_date"), "yyyy-MM-dd")%>&nbsp;至&nbsp;<%=DateUtil.format(wptd.getDate("end_date"), "yyyy-MM-dd")%>&nbsp;&nbsp;工期：<%=wptd.getInt("duration")%>天
</td></tr></table>
</body>
<script type="text/javascript">
function doOnToolbarInited() {
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "workplan_task_user.jsp?<%=urlStr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "workplan_task_user.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "workplan_task_user.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '添加', bclass: 'add', onpress : action},
		{name: '编辑', bclass: 'edit', onpress : action},
		{name: '删除', bclass: 'delete', onpress : action},
		{separator: true},
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}		
	],
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
	url: false,
	usepager: true,
	checkbox : false,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	//title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	/*
	onRowDblclick: rowDbClick,
	onColSwitch: colSwitch,
	onColResize: colResize,
	onToggleCol: toggleCol,
	*/
	onToolbarInited: doOnToolbarInited,
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
	if (com=='添加')	{
		$('#select_user').show();
		$('#real_user').hide();
		$("#taskDlg").dialog({
			title:"添加参与者",
			modal: true,
			bgiframe: true,
			width: 420,
			height: 200,
			// bgiframe:true,
			buttons: {
				"取消": function() {
					$(this).dialog("close");
				},
				"确定": function() {
					// f_name为doCheckJS自动生成的变量
					if (!LiveValidation.massValidate(f_user_name.formObj.fields))
						return false;
					$.ajax({
						type: "post",
						url: "workplan_do.jsp",
						data: {
							op: "addTaskUser",
							user_name: o("user_name").value,
							percent: o("percent").value,
							duration: o("duration").value,
							task_id: <%=taskId%>,
							mode: getRadioValue("mode")
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							$('#grid').showLoading();
						},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="0") {
								jAlert(data.msg, "提示");
							}
							else {
								jAlert(data.msg, "提示");
								window.location.reload();
							}
						},
						complete: function(XMLHttpRequest, status){
							$('#grid').hideLoading();				
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
	else if (com=="删除") {
		if ($('tr', $('#grid')).length>0) {
			if ($('.trSelected', $('#grid')).length == 0) {
				jAlert("请选择任务！", "提示");
				return;
			}
		}
		
		var taskUserId = "";
		$('.trSelected td:nth-child(1) div', $('#grid')).each(function(i){
			taskUserId = $(this).text();
		});
		
		jConfirm('您确定要删除么？', '提示', function(r) {
			if (!r){return;} 
			else{
				$.ajax({
					type: "post",
					url: "workplan_do.jsp",
					data: {
						op: "delTaskUser",
						id: taskUserId
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#grid').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
							jAlert(data.msg, "提示");
							window.location.reload();
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#grid').hideLoading();				
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});
			}
		});
	}
	else if (com=="编辑") {
		if ($('tr', $('#grid')).length>0) {
			if ($('.trSelected', $('#grid')).length == 0) {
				jAlert("请选择记录！", "提示");
				return;
			}
		}
		
		var taskUserId = "";
		$('.trSelected td:nth-child(1) div', $('#grid')).each(function(i){
			taskUserId = $(this).text();
		});

		$.ajax({
			type: "post",
			url: "workplan_do.jsp",
			data: {
				op: "getTaskUser",
				id: taskUserId
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#grid').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="0") {
					jAlert(data.msg, "提示");
				}
				else {
					o("percent").value = data.percent;
					o("duration").value = data.duration;
					o("user_name").value = data.user_name;

					var taskUserName = "";
					$('.trSelected td:nth-child(2) div', $('#grid')).each(function(i){
						taskUserName = $(this).text().trim();
					});

					$('#select_user').hide();
					$('#real_user').show();
					$('#real_user').html(taskUserName);
										
					$("#taskDlg").dialog({
						title:"编辑任务用户",
						modal: true,
						bgiframe: true,
						width: 420,
						height: 200,
						// bgiframe:true,
						buttons: {
							"取消": function() {
								$(this).dialog("close");
							},
							"确定": function() {
								// f_name为doCheckJS自动生成的变量
								if (!LiveValidation.massValidate(f_user_name.formObj.fields))
									return false;
									
								$.ajax({
									type: "post",
									url: "workplan_do.jsp",
									data: {
										op: "editTaskUser",
										user_name: o("user_name").value,
										percent: o("percent").value,
										duration: o("duration").value,
										mode: getRadioValue("mode"),
										id: taskUserId
									},
									dataType: "html",
									beforeSend: function(XMLHttpRequest){
										$('#grid').showLoading();
									},
									success: function(data, status){
										data = $.parseJSON(data);
										if (data.ret=="0") {
											jAlert(data.msg, "提示");
										}
										else {
											jAlert(data.msg, "提示", function() {
												window.location.reload();
											});
										}
									},
									complete: function(XMLHttpRequest, status){
										$('#grid').hideLoading();				
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
			},
			complete: function(XMLHttpRequest, status){
				$('#grid').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	}

}

<%
	// WorkPlanTaskDb wptud = new WorkPlanTaskDb();
	ParamConfig pc = new ParamConfig(wptud.getTable().getFormValidatorFile()); // "form_rule.xml");
	ParamChecker pck = new ParamChecker(request);
	out.print(pck.doGetCheckJS(pc.getFormRule("workplan_task_user_create")));
%>
f_duration.add( Validate.Numericality, { maximum: <%=wptd.getInt("duration")%> } );

</script>
</html>