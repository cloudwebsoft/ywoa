<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.message.MessageDb"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.redmoon.oa.sms.*"%>
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.redmoon.oa.shell.BSHShell" %>
<%@ page import="com.cloudweb.oa.vo.Result" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	int flow_id = ParamUtil.getInt(request, "flowId", -1);
	if (flow_id == -1) {
		out.print(StrUtil.makeErrMsg(SkinUtil.LoadString(request, "err_id")));
		return;
	}

	WorkflowMgr wfm = new WorkflowMgr();
	WorkflowDb wf = wfm.getWorkflowDb(flow_id);

	if (!wf.isLoaded()) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		String str = LocalUtil.LoadString(request,"res.flow.Flow","processNotExist");
		out.print(SkinUtil.makeErrMsg(request, str, true));
		return;
	}

	// 检查表单是否存在
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());
	FormDb fd = new FormDb();

	fd = fd.getFormDb(lf.getFormCode());
	if (!fd.isLoaded()) {
		String str = LocalUtil.LoadString(request,"res.flow.Flow","form");
		String str1 = LocalUtil.LoadString(request,"res.flow.Flow","noLongerExist");
		out.print(StrUtil.jAlert_Back(str + lf.getFormCode() + str1,"提示"));
		return;
	}

	// 置嵌套表需要用到的cwsId
	request.setAttribute("cwsId", "" + flow_id);
	// 置嵌套表需要用到的curOperate
	request.setAttribute("pageType", "flowShow");
	// 置NestSheetCtl需要用到的formCode
	request.setAttribute("formCode", lf.getFormCode());

	Directory dir = new Directory();
	Leaf ft = dir.getLeaf(wf.getTypeCode());
	boolean isFree = false;
	boolean isReactive = false;
	boolean isRecall = false;
	if (ft!=null) {
		isFree = ft.getType()!=Leaf.TYPE_LIST;
		WorkflowPredefineDb wfp = new WorkflowPredefineDb();
		wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());
		isReactive = wfp.isReactive();
		isRecall = wfp.isRecall();
	}

	boolean isFlowManager = false;
	LeafPriv lp = new LeafPriv(wf.getTypeCode());
	if (privilege.isUserPrivValid(request, "admin.flow")) {
		if (lp.canUserExamine(privilege.getUser(request))) {
			isFlowManager = true;
		}
	}

	// 判断是否拥有查看流程过程的权限
	if (!isFlowManager) {
		// 判断是否参与了流程
		if (!privilege.isUserPrivValid(request, "admin")) {
		%>
			<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid"), true));
			return;
		}
	}

com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
fdao = fdao.getFormDAO(flow_id, fd);

String myUserName = privilege.getUser(request);
boolean isStarted = wf.isStarted();
String op = ParamUtil.get(request, "op");
if ("deliver".equals(op)) {
	long myActionId = ParamUtil.getInt(request, "myActionId", -1);
	int actionId = ParamUtil.getInt(request, "actionId", -1);

	BSHShell shell = null;

	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb((int) actionId);

	WorkflowPredefineDb wpd = new WorkflowPredefineDb();
	wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
	WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
	String script = wpm.getActionFinishScript(wpd.getScripts(), wa.getInternalName());

	if (script != null && !"".equals(script.trim())) {
		WorkflowMgr wm = new WorkflowMgr();
		// shell = wm.runDeliverScript(request, privilege.getUser(request), wf, fdao, mad, script, true);
	}

	JSONObject json = new JSONObject();
	if (shell == null) {
		json.put("ret", 0);
		json.put("msg", "请检查脚本是否存在");
	} else {
		String errDesc = shell.getConsole().getLogDesc().trim();
		// json.put("msg", StrUtil.toHtml(errDesc));
		json.put("ret", 1);
		json.put("msg", errDesc);
	}
	out.print(json.toString());
	return;
}
else if ("delMyAction".equals(op)) {
	boolean re = false;
	JSONObject json = new JSONObject();
	try {
		long delMyActionId = ParamUtil.getLong(request, "delMyActionId");
		MyActionDb mad = new MyActionDb();
		mad = mad.getMyActionDb(delMyActionId);
		// 删除日程中的待办事宜
		PlanDb pd = new PlanDb();
		pd = pd.getPlanDb(mad.getUserName(), PlanDb.ACTION_TYPE_FLOW, String.valueOf(mad.getId()));
		if (pd!=null) {
			pd.del();
		}
		
		re = mad.del();
		if (re) {
			// 检查是否节点上是否有其它的办理记录，如果没有，则更改节点状态为未处理
			String sql = "select id from " + mad.getTableName() + " where action_id=" + mad.getActionId();
			if (mad.list(sql).size()==0) {
				WorkflowActionDb wa = new WorkflowActionDb();
				wa = wa.getWorkflowActionDb((int)mad.getActionId());
				wa.setStatus(WorkflowActionDb.STATE_NOTDO);
				wa.save();
			}

			wf.setIntervenor(myUserName);
			wf.setInterveneTime(new Date());
			wf.save();
		}
	}
	catch (ErrMsgException e) {
		e.printStackTrace();
		json.put("ret", 0);
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
	if (re) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		json.put("ret", 1);
		json.put("msg", str);
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		json.put("ret", 0);
		json.put("msg", str);
	}
	out.print(json);
	return;
}
else if ("changeStatus".equals(op)) {
	int status = ParamUtil.getInt(request, "status");
	MyActionDb mad = new MyActionDb();
	mad = mad.getLastMyActionDbOfFlow(wf.getId());
	long actionId = mad.getActionId();
	WorkflowActionDb lastAction = new WorkflowActionDb();
	lastAction = lastAction.getWorkflowActionDb((int)actionId);
	boolean re = false;
	try {
		int oldStatus = wf.getStatus();
		re = wf.changeStatus(request, status, lastAction);

		// 置所有未结束的mad为已处理状态
		if (status == WorkflowDb.STATUS_FINISHED && oldStatus != status) {
			Vector<MyActionDb> v = mad.getMyActionDbDoingOfFlow(flow_id);
			for (MyActionDb myActionDb : v) {
				lastAction.changeMyActionDb(myActionDb, ConstUtil.USER_SYSTEM);
			}
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
	if (re) {
		// 因为在changeStatus中修改了wf，所以此处需重新获取wf，否则flowstring得不到更新
		wf = wf.getWorkflowDb(flow_id);
		wf.setIntervenor(myUserName);
		wf.setInterveneTime(new Date());
		wf.save();
		json.put("ret", 1);
		json.put("msg", LocalUtil.LoadString(request, "res.common", "info_op_success"));
	} else {
		json.put("ret", 0);
		json.put("msg", LocalUtil.LoadString(request, "res.common", "info_op_fail"));
	}
	out.print(json.toString());
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>查看/修改流程</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
	<link type="text/css" rel="stylesheet" href="../js/goToTop/goToTop.css"/>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link rel="stylesheet" href="../js/bootstrap/css/bootstrap.min.css"/>
	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<script src="../inc/map.js"></script>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<script src="../inc/flow_dispose.jsp"></script>
	<script src="../js/goToTop/goToTop.js"></script>
	<script src="../js/jquery-showLoading/jquery.showLoading.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<script src="../js/layui/layui.js" charset="utf-8"></script>
	<script>
		var layer;
		layui.use('layer', function(){
			layer = layui.layer;
		});

		$(function() {
			$('#btnSubmit').click(function(e) {
				e.preventDefault();
				submitFlowForm();
			});
		});

		function submitFlowForm() {
			/*if ($('#status').val() == '<%=wf.getStatus()%>') {
				jAlert('<%=LocalUtil.LoadString(request, "res.flow.Flow", "statusNotChange")%>', '提示');
				return;
			}*/
			if ($('#status').val() == '<%=WorkflowDb.STATUS_FINISHED%>') {
				layer.open({
					type: 1
					, offset: 'auto' // 具体配置参考：http://www.layui.com/doc/modules/layer.html#offset
					, id: 'dlgFinishEvent' // 防止重复弹出
					, content: '<div style="padding: 20px 50px;">需要调用结束事件么</div>'
					, btn: ['是', '否', '取消']
					, btnAlign: 'c' //按钮居中
					, shade: 0 //不显示遮罩
					, yes: function (index, layero) {
						//按钮【按钮一】的回调
						$('#isCallEvent').val('true');
						// $('#flowForm').submit();
						$.ajax({
							type: "post",
							url: "flow_intervene.jsp",
							data: $('#flowForm').serialize(),
							success: function (data, status) {
								data = $.parseJSON(data);
								layer.msg(data.msg, {
									offset: '6px'
								});
							},
							error: function (XMLHttpRequest, textStatus) {
								alert(XMLHttpRequest.responseText);
							}
						})

						layer.close(index);
					}
					, btn2: function (index, layero) {
						//按钮【按钮二】的回调
						$('#isCallEvent').val('false');
						// $('#flowForm').submit();
						$.ajax({
							type: "post",
							url: "flow_intervene.jsp",
							data: $('#flowForm').serialize(),
							success: function (data, status) {
								data = $.parseJSON(data);
								layer.msg(data.msg, {
									offset: '6px'
								});
							},
							error: function (XMLHttpRequest, textStatus) {
								alert(XMLHttpRequest.responseText);
							}
						})

						layer.close(index);
					}
					, btn3: function (index, layero) {
						//按钮【按钮三】的回调
						// layer.close(index);
					}
					, cancel: function () {
						//右上角关闭回调
						//return false 开启该代码可禁止点击该按钮关闭
					}
				});
			}
			else {
				$.ajax({
					type: "post",
					url: "flow_intervene.jsp",
					data: $('#flowForm').serialize(),
					success: function (data, status) {
						data = $.parseJSON(data);
						layer.msg(data.msg, {
							offset: '6px'
						});
					},
					error: function (XMLHttpRequest, textStatus) {
						alert(XMLHttpRequest.responseText);
					}
				})
			}
		}
	</script>
</head>
<body>
<%
if ("submitTo".equals(op)) {
	long myActionId = ParamUtil.getLong(request, "myActionId");
	String internalName = ParamUtil.get(request, "internalName");
	String users = ParamUtil.get(request, "users");
	String userRealNames = ParamUtil.get(request, "userRealNames");
	WorkflowMgr workflowMgr = new WorkflowMgr();
	boolean re = workflowMgr.deliverTo(request, flow_id, myActionId, users, userRealNames, internalName);
	if (re) {
		wf = wf.getWorkflowDb(flow_id);
		wf.setIntervenor(myUserName);
		wf.setInterveneTime(new Date());
		wf.save();
	}
	String str = LocalUtil.LoadString(request, "res.common", "info_op_success");
	out.print(StrUtil.jAlert_Redirect(str, "提示", "flow_intervene.jsp?flowId=" + flow_id));
	return;
}
else if ("changeActionStatus".equals(op)) {
	int actionId = ParamUtil.getInt(request, "actionId");
	int status = ParamUtil.getInt(request, "status");
	WorkflowActionDb wa = new WorkflowActionDb();
	wa = wa.getWorkflowActionDb(actionId);
	wa.setStatus(status);
	boolean re = wa.save();
	if (re) {
		// 因为在wa.save()中修改了wf，所以此处需重新获取wf，否则flowstring得不到更新
		wf = wf.getWorkflowDb(flow_id);
		wf.setIntervenor(myUserName);
		wf.setInterveneTime(new Date());
		wf.save();

		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_intervene.jsp?flowId=" + flow_id));
		//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_intervene.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str,"提示"));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;
}

com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
	
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String canUserModifyFlow = cfg.get("canUserModifyFlow");
String mode = "user";
if ("true".equals(canUserModifyFlow)) {
	mode = "user";
} else {
	mode = "view";
}
	
String flowExpireUnit = cfg.get("flowExpireUnit");
boolean isHour = !"day".equals(flowExpireUnit);
if ("day".equals(flowExpireUnit)) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","day");
	flowExpireUnit = str;
}
else{
	String str = LocalUtil.LoadString(request,"res.flow.Flow","hour");
	flowExpireUnit = str;	
}
%>
<%@ include file="../flow_modify_inc_menu_top.jsp"%>
<script>
o("menu8").className="current"; 
</script>
<div class="spacerH"></div>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td align="center" >
		<%=WorkflowMgr.getLevelImg(request, wf)%><strong style="font-size:14px"><%=wf.getTitle()%></strong>
      </td>
    </tr>
  </tbody>
</table>
<table id="mainTable" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" style="padding-left:5px;">
	<table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td height="35" align="center" >
            <%
			int doc_id = wf.getDocId();
			DocumentMgr dm = new DocumentMgr();
			Document doc = dm.getDocument(doc_id);
			String intervenor = wf.getIntervenor();
			String intervenorRealName = "";
			Date interveneTime = null;
			if (!"".equals(intervenor)) {
				interveneTime = wf.getInterveneTime();
				intervenorRealName = um.getUserDb(intervenor).getRealName();
			}
			%>
            ID：<%=wf.getId()%>
            &nbsp;&nbsp;
            <lt:Label res="res.flow.Flow" key="organ"/>：<%=um.getUserDb(wf.getUserName()).getRealName()%>
            &nbsp;&nbsp;
            <lt:Label res="res.flow.Flow" key="state"/>：<%=wf.getStatusDesc()%>
            &nbsp;&nbsp;
			  <%
				  if (!"".equals(intervenor)) {
				  	out.print("干预：" + intervenorRealName + "&nbsp;" + DateUtil.format(interveneTime, "yyyy-MM-dd HH:mm:ss") + "&nbsp;&nbsp;");
				  }
			  %>
            <span id="projectName">
            <%if (wf.getProjectId()!=-1) {
                com.redmoon.oa.visual.FormDAO fdaoPrj = new com.redmoon.oa.visual.FormDAO();
                FormDb prjFd = new FormDb();
                prjFd = prjFd.getFormDb("project");
				fdaoPrj = fdaoPrj.getFormDAO((int)wf.getProjectId(), prjFd);
                %>
                <lt:Label res="res.flow.Flow" key="project"/>：<a href="javascript:;" onClick="addTab('<%=fdaoPrj.getFieldValue("name")%>', 'project/project_show.jsp?projectId=<%=wf.getProjectId()%>&formCode=project')"><%=fdaoPrj.getFieldValue("name")%></a>&nbsp;&nbsp;<a title="<lt:Label res='res.flow.Flow' key='disassociate'/>" href="javascript:;" onClick="unlinkProject()" style='font-size:16px; font-color:red'>×</a>
                <%
            }
            %>
            </span>
            </td>
        </tr>
        <tr>
          <td>
			  <table width="100%" border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center">
                <div id="formAllDiv">
					<form id="flowForm" name="flowForm" action="flow_intervene.jsp">
						<table width="100%" align="center" class="tabStyle_1 percent98">
							<tr>
								<td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="changeStatus"/></td>
							</tr>
							<tr>
								<td align="center">
									<input name="op" value="changeStatus" type="hidden"/>
									<input name="flowId" value="<%=flow_id%>" type="hidden"/>
									<select id="status" name="status">
										<option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%>
										</option>
										<option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%>
										</option>
										<option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%>
										</option>
										<option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%>
										</option>
										<option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%>
										</option>
										<option value="<%=WorkflowDb.STATUS_NONE%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NONE)%>
										</option>
										<option value="<%=WorkflowDb.STATUS_DELETED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DELETED)%>
										</option>
									</select>
									&nbsp;&nbsp;
									<input type="hidden" id="isCallEvent" name="isCallEvent" value="true"/>
									<input type="button" id="btnSubmit" class="btn btn-default"
										   value='<lt:Label res="res.flow.Flow" key="sure"/>'/>
									&nbsp;&nbsp;
									<input type="button" class="btn btn-default" title="编辑表单内容" value="编辑"
										   onclick="addTab('编辑：<%=wf.getTitle()%>', '<%=request.getContextPath()%>/visual/moduleEditPage.do?id=<%=fdao.getId()%>&code=<%=fd.getCode()%>')"/>
									<script>
										o("status").value = "<%=wf.getStatus()%>";
									</script>
								</td>
							</tr>
						</table>
					</form>
                <%
					String sql = "select id from flow_my_action where flow_id=" + flow_id + " order by receive_date asc";
					MyActionDb mad = new MyActionDb();
					Vector v = mad.list(sql);
				%>
                  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="percent98">
                    <tr>
                      <td height="30" align="left"><strong>&nbsp;<lt:Label res="res.flow.Flow" key="cprocess"/>：</strong></td>
                    </tr>
                  </table>
                  <table width="98%" align="center" class="tabStyle_1 percent98 mainTable">
                    <tbody>
                      <tr>
                        <td class="tabStyle_1_title" width="10%" align="center"><lt:Label res="res.flow.Flow" key="handler"/></td>
                        <td class="tabStyle_1_title" width="9%" align="center"><lt:Label res="res.flow.Flow" key="bearer"/></td>
                        <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="agent"/></td>
                        <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="task"/></td>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="reachState"/></td>
                        <td class="tabStyle_1_title" width="10%" align="center"><lt:Label res="res.flow.Flow" key="arrivalTime"/></td>
                        <td class="tabStyle_1_title" width="10%" align="center"><lt:Label res="res.flow.Flow" key="handleTime"/></td>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="processor"/></td>
						  <td class="tabStyle_1_title" width="9%" align="center"><lt:Label res="res.flow.Flow" key="processeStatus"/></td>
						  <td class="tabStyle_1_title" width="10%" align="center">IP/OS/Browser</td>
                        <td class="tabStyle_1_title" width="16%" align="center"><lt:Label res="res.flow.Flow" key="operate"/></td>
                      </tr>
                      <%
java.util.Iterator ir = v.iterator();
DeptMgr deptMgr = new DeptMgr();
OACalendarDb oad = new OACalendarDb();
int m=0;
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next();
	WorkflowDb wfd = new WorkflowDb();
	wfd = wfd.getWorkflowDb((int)mad.getFlowId());
	String userName = wfd.getUserName();
	String userRealName = "";
	if (userName!=null) {
		UserDb user = um.getUserDb(mad.getUserName());
		userRealName = user.getRealName();
	}
	WorkflowActionDb wad = new WorkflowActionDb();
	wad = wad.getWorkflowActionDb((int)mad.getActionId());
	m++;
	%>
                      <tr class="highlight" id="<%=m %>" privMyActionId="<%=mad.getPrivMyActionId()%>">
                        <td  ><%
						  String deptCodes = mad.getDeptCodes();
						  String[] depts = StrUtil.split(deptCodes, ",");
						  if (depts!=null) {
							String dts = "";
							int deptLen = depts.length;
							for (int n=0; n<deptLen; n++) {
								DeptDb dd = deptMgr.getDeptDb(depts[n]);
								if (dd!=null) {
									if (dts.equals(""))
										dts = dd.getName();
									else
										dts += "," + dd.getName();
								}
							}
							if (!dts.equals(""))
								out.print(dts + "：");
						  }

						  boolean isExpired = false;
						  java.util.Date chkDate = mad.getCheckDate();
						  if (chkDate==null)
							chkDate = new java.util.Date();
						  if (DateUtil.compare(chkDate, mad.getExpireDate())==1) {
							isExpired = true;
						  }
						  if (isExpired) {%>
							  <img src="../images/flow/expired.png" align="absmiddle" alt="<lt:Label res='res.flow.Flow' key='timeOut'/>" />
						  <%}%>
                      		<%=userRealName%>
                        </td>
                        <td><%
					  if (mad.getPrivMyActionId()!=-1) {
					  	MyActionDb mad2 = mad.getMyActionDb(mad.getPrivMyActionId());
					  	if (mad2.isLoaded()) {
							out.print(um.getUserDb(mad2.getUserName()).getRealName());
						}
					  }
					  else
					  	out.print("&nbsp;");
					  %>
                        </td>
                        <td>
                        <%
						if (!mad.getProxyUserName().equals("")) {
							out.print(um.getUserDb(mad.getProxyUserName()).getRealName());
						}
						%>
                        </td>
                        <td><%=StrUtil.getNullStr(wad.getTitle())%></td>
                        <td align="center">
						<%if (wad.getDateDelayed()!=null) {%>
                        	<lt:Label res="res.flow.Flow" key="delay"/>
                        <%}else{%>
							<%=WorkflowActionDb.getStatusName(mad.getActionStatus())%>
                            <%
							String reas = wad.getReason();
							if (reas!=null && !"".equals(reas.trim())) {%>
                            <BR />
                            (<%=reas%>)
                            <%}%>
                        <%}%>
                        </td>
                        <td align="center"><%=DateUtil.format(mad.getReceiveDate(), "yy-MM-dd HH:mm:ss")%> </td>
                        <td align="center"><%=DateUtil.format(mad.getCheckDate(), "yy-MM-dd HH:mm:ss")%></td>
                        <td align="center">
                          <%if (mad.isChecked()) {%>
                          <%if (mad.getChecker().equals(UserDb.SYSTEM)) {%>
                          <lt:Label res="res.flow.Flow" key="system"/>
                          <%}else{
								if (!mad.getChecker().equals("")) {
								%>
                          <%=um.getUserDb(mad.getChecker()).getRealName()%>
                          <%}
							}
						}%>
                        </td>
                        <td align="center" id="tdMad<%=mad.getId()%>" class="<%=MyActionDb.getCheckStatusClass(mad.getCheckStatus())%>">
                        <%
						if (false && mad.getChecker().equals(UserDb.SYSTEM)) {
							String str = LocalUtil.LoadString(request,"res.flow.Flow","skipOverTime");
							out.print(str);
						}else{
						%>
                          <%=mad.getCheckStatusName()%>
                        <%}
						if (mad.getCheckStatus()!=0 && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_TRANSFER && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
							if (mad.getResultValue()!=WorkflowActionDb.RESULT_VALUE_RETURN) {
								if (mad.getSubMyActionId()==MyActionDb.SUB_MYACTION_ID_NONE)
								  out.print("<BR>(" + WorkflowActionDb.getResultValueDesc(mad.getResultValue()) + ")");
							}
						}
						%>
                        </td>
						  <td>
							  <%
								  String ip = mad.getIp();
								  if ("".equals(ip)) {
								  	ip = "-";
								  }
								  String os = mad.getOs();
								  if ("".equals(os)) {
									  os = "-";
								  }
								  String browser = mad.getBrowser();
								  if ("".equals(browser)) {
									  browser = "-";
								  }
								  String clusterNo = mad.getClusterNo();
								  if ("".equals(clusterNo)) {
								  	clusterNo = "-";
								  }
							  %>
							  <%=ip%>/<%=os%>/<%=browser%>/<span title="集群编号"><%=clusterNo%></span>
						  </td>
                        <td align="center">
							<%
                            // 检查是否存在后续节点
                            boolean canRecall = false;
                            WorkflowActionDb wa = new WorkflowActionDb();
                            wa = wa.getWorkflowActionDb((int)mad.getActionId());
                            if (v.size()>1 && m==v.size()-1)
                                canRecall = true;
                            if (wa.getLinkToActions().size()==0)
                                canRecall = false;

                          if (isFree) {%>
                              <%if (canRecall) {%>
                              <a href='javascript:;' onClick="recallFree(<%=m %>,<%=flow_id%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="forcedWithdraw"/></a>
                              <%}
                          }else{
                              if (canRecall) {
                              %>
                              <a href='javascript:;' onClick="recall(<%=m %>,<%=flow_id%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="forcedWithdraw"/></a>
                              <%
                              }
                          }%>
							&nbsp;&nbsp;
							<a href="javascript:;" title="运行流转事件" onClick="runEventScript(<%=mad.getId()%>)">流转</a>
							<%
                          // 发起记录不能被删除
                          if (m!=1) {%>
                          &nbsp;&nbsp;
                          <a href="javascript:;" onClick="delMyAction('<%=mad.getId()%>', '<%=m%>')"><lt:Label res="res.flow.Flow" key="delete"/></a>
                          <%}%>
                          <%if (!isFree) {%>
                          &nbsp;&nbsp;
                          <a href="javascript:;" onClick="submitTo('<%=mad.getId()%>')"><lt:Label res="res.flow.Flow" key="forwarded"/></a>
                          <%}%>
                          &nbsp;&nbsp;
                          <a href="javascript:;" onClick="changeCheckStatus('<%=mad.getId()%>', <%=mad.getCheckStatus()%>, <%=mad.getActionId()%>)">状态</a>
                        </td>
                      </tr>
                      <%}%>
                    </tbody>
                  </table>
                  </div>
					<script>
						function runEventScript(myActionid) {
							layer.confirm('您确定要在节点上调用流转事件么', {icon: 3, title: '提示'}, function (index) {
								layer.close();
								$.ajax({
									type: "POST",
									url: "../flow/runDeliverScript",
									data: {
										"op": "deliver",
										"flowId": "<%=flow_id%>",
										"myActionId": myActionid
									},
									success: function (data, status) {
										console.log(data);
										if (data.code == "200") {
											if (data.data == '') {
												layer.msg('操作成功', {
													offset: '6px'
												});
											} else {
												layer.msg(data.data, {
													offset: '6px'
												});
											}
										} else {
											layer.msg(data.msg, {
												offset: '6px'
											});
										}
									},
									error:function(XMLHttpRequest, textStatus){
										alert(XMLHttpRequest.responseText);
									}
								})
							});
						}
					</script>
                <br>
                <%if (!isFree) {%>
                <table width="555" align="center" class="tabStyle_1 percent98 mainTable">
                <thead>
                    <tr>
                      <td width="216" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="node"/></td>
                      <td width="204" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="state"/></td>
                      <td width="119" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
                    </tr>
                </thead>
                <%
                    Iterator irwa = wf.getActions().iterator();
                    while (irwa.hasNext()) {
                        WorkflowActionDb wa = (WorkflowActionDb)irwa.next();
                        %>
                        <form id="formStauts<%=wa.getId()%>" action="flow_intervene.jsp">
                        <tr id="trAction<%=wa.getId()%>">
                            <td align="center">
                            <input name="inName" type="hidden" value="<%=wa.getInternalName()%>">
                            <%=wa.getJobName() + "：" + wa.getTitle()%></td>
                            <td align="center">
                            <select id="waStatus<%=wa.getId()%>" name="status">
                            <option value="<%=WorkflowActionDb.STATE_NOTDO%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_NOTDO)%></option>
                            <option value="<%=WorkflowActionDb.STATE_IGNORED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_IGNORED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_DOING%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_DOING)%></option>
                            <option value="<%=WorkflowActionDb.STATE_RETURN%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_RETURN)%></option>
                            <option value="<%=WorkflowActionDb.STATE_FINISHED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_FINISHED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_DISCARDED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_DISCARDED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_TRANSFERED%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_TRANSFERED)%></option>
                            <option value="<%=WorkflowActionDb.STATE_HANDOVER%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_HANDOVER)%></option>
                            <option value="<%=WorkflowActionDb.STATE_PLUS%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_PLUS)%></option>
                            <option value="<%=WorkflowActionDb.STATE_SUSPEND_OVER%>"><%=WorkflowActionDb.getStatusName(WorkflowActionDb.STATE_SUSPEND_OVER)%></option>
                            </select>

                            <input id="flowId" name="flowId" type="hidden" value="<%=flow_id%>" />
                            <input id="op" name="op" type="hidden" value="changeActionStatus" />
                            <input id="actionId" name="actionId" type="hidden" value="<%=wa.getId()%>" />

                            <script>
                            o("waStatus<%=wa.getId()%>").value = "<%=wa.getStatus()%>";
                            </script>

                          </td>
                          <td align="center">
                            <input type="submit" class="btn btn-default" value='<lt:Label res="res.flow.Flow" key="sure"/>' />
							  &nbsp;&nbsp;
							  <input type="button" class="btn btn-default" value="清除用户" title="仅限于代码调试用" onclick="clearActionUser(<%=wa.getId()%>)"/>
                          </td>
                        </tr>
                        </form>
                        <%
                    }
                %>
                </table>
                <%}%>
                </td>
              </tr>
            </table>
		  </td>
        </tr>
      </table></td>
  </tr>
	<tr><td>
		<form id="formEvent" action="flow_intervene.jsp">
			<table width="100%" align="center" class="tabStyle_1 percent98">
				<tr>
					<td class="tabStyle_1_title">调用事件</td>
				</tr>
				<tr>
					<td align="center">
						<select id="actionRunId" name="actionId">
							<option value="">请选择</option>
						<%
							Vector<WorkflowActionDb> va = wf.getActions();
							for (WorkflowActionDb wa : va) {
							%>
							<option value="<%=wa.getId()%>"><%=wa.getJobName() + ":" + wa.getTitle()%></option>
							<%
							}
						%>
						</select>
						<select id="event" name="event">
							<option value="deliver">流转事件</option>
						</select>
						<button class="btn btn-default" onclick="runEventScript()">确定</button>

					</td>
				</tr>
			</table>
		</form>
	</td></tr>
</table>
<div id="dlg" style="display:none">
<form id="formSubmitTo" action="flow_intervene.jsp" method="post">
	<%
    String flowString = wf.getFlowString();
    String options = "";
    Vector vt = null;
	if (isFree) {
		vt = wf.getActions();
	}
	else {
		vt = wf.getActionsFromString(flowString);
	}
    Iterator irwa = vt.iterator();
    while (irwa.hasNext()) {
        WorkflowActionDb wa = (WorkflowActionDb)irwa.next();
        // 过滤掉当前节点
        options += "<option value='" + wa.getInternalName() + "'>" + wa.getJobName() + "：" + wa.getTitle() + "</option>";
    }
    
    %>
    <lt:Label res="res.flow.Flow" key="node"/>：
    <select id="internalName" name="internalName">
    <option value=""><lt:Label res="res.flow.Flow" key="pleaseSelect"/></option>
    <%=options%>
    </select>
    <br />
    <lt:Label res="res.flow.Flow" key="user"/>：
    <input id="userRealNames" name="userRealNames" />
    <input class="btn" onClick="openWinUsers()" type="button" value='<lt:Label res="res.flow.Flow" key="choose"/>' name="button">
    
    <input id="users" name="users" type="hidden" />
    <input id="flowId" name="flowId" type="hidden" value="<%=flow_id%>" />
    <input id="myActionId" name="myActionId" type="hidden" />
    <input id="op" name="op" type="hidden" value="submitTo" />
</form>    
</div>

<div id="dlgCheckStatus" style="display:none">
	待办记录状态：<select id="checkStatus" name="checkStatus">
    <option value="<%=MyActionDb.CHECK_STATUS_NOT%>"><%=MyActionDb.getCheckStatusName(MyActionDb.CHECK_STATUS_NOT)%></option>
    <option value="<%=MyActionDb.CHECK_STATUS_CHECKED%>"><%=MyActionDb.getCheckStatusName(MyActionDb.CHECK_STATUS_CHECKED)%></option>
    </select>
</div>
</body>
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
	selUserNames = o("users").value;
	selUserRealNames = o("userRealNames").value;
	openWin('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>', 800, 600)
}

function setUsers(users, userRealNames) {
	o("users").value = users;
	o("userRealNames").value = userRealNames;
}

function delMyAction(delMyActionId, m) {
	layer.confirm('<lt:Label res="res.flow.Flow" key="isDelete"/>', {icon: 3, title: '提示'}, function (index) {
		layer.close();
		$.ajax({
			type:"get",
			url:"flow_intervene.jsp",
			data:{"op":"delMyAction","flowId":"<%=flow_id%>","delMyActionId":delMyActionId},
			success:function(data,status){
				data = $.parseJSON(data);
				layer.msg(data.msg, {
					offset: '6px'
				});
				if (data.ret=="1") {
					$("tr[id=" + m + "]").remove();
				}
			},
			error:function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		})
	});
}

function recall(id,flow_id,action_id){
	layer.confirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>', {icon: 3, title: '提示'}, function (index) {
		layer.close();
		$.ajax({
			type: "post",
			url:"../flow/recall.do",
			data:{"flow_id":flow_id,"action_id":action_id},
			success:function(data,status){
				data = $.parseJSON(data);
				layer.msg(data.msg, {
					offset: '6px'
				});
				if (data.ret=="1") {
					$("tr[privMyActionId=" + action_id + "]").remove();
				}
			},
			error:function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		})
	});
}

function recallFree(id,flow_id,action_id){
	layer.confirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>', {icon: 3, title: '提示'}, function (index) {
		layer.close();
		$.ajax({
			type:"get",
			url:"../flow/recall.do",
			data:{"flow_id":flow_id,"action_id":action_id},
			success:function(data,status){
				$("#"+id).remove();
				data = $.parseJSON(data);

				layer.msg(data.msg, {
					offset: '6px'
				});
			},
			error:function(XMLHttpRequest, textStatus){
				//alert(XMLHttpRequest.responseText);
			}
		})
	});
}

function submitTo(myActionId) {
	$("#dlg").dialog({
		title:'<lt:Label res="res.flow.Flow" key="selectNodeUser"/>',
		modal: true,
		width: 350,
		height: 160,
		// bgiframe:true,
		buttons: {
			'<lt:Label res="res.flow.Flow" key="cancel"/>': function() {
				$(this).dialog("close");
			},
			'<lt:Label res="res.flow.Flow" key="sure"/>': function() {
				if (o("users").value=="") {
					layer.msg('<lt:Label res="res.flow.Flow" key="selectUser"/>', {
						offset: '6px'
					});
					return;
				}
				if (o("internalName").value=="") {
					layer.msg('<lt:Label res="res.flow.Flow" key="selectNode"/>', {
						offset: '6px'
					});
					return;
				}
				
				$("#myActionId").val(myActionId);
				$("#formSubmitTo").submit();
				
				$(this).dialog("close");
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true
		});	
}

function changeCheckStatus(myActionId, curStatus, actionId) {
	$('#checkStatus').val(curStatus);
	$("#dlgCheckStatus").dialog({
		title:'<lt:Label res="res.flow.Flow" key="selChangeStatus"/>',
		modal: true,
		width: 100,
		height: 30,
			// bgiframe:true,
			buttons : {
				'<lt:Label res="res.flow.Flow" key="cancel"/>' : function() {
					$(this).dialog("close");
				},
				'<lt:Label res="res.flow.Flow" key="sure"/>' : function() {
					// consoleLog($('#checkStatus').val());
					$.ajax({
						type : "post",
						url : "../flow/setMyActionStatus.do",
						data : {
							"myActionId" : myActionId,
							"checkStatus" : $('#checkStatus').val()
						},
						success : function(data, status) {
							data = $.parseJSON(data);
							layer.msg(data.msg, {
								offset: '6px'
							});
							if (data.ret == "1") {
								$("#tdMad" + myActionId).html($("#checkStatus").find("option:selected").text());
								var actionStatus = data.actionStatus;
								if (actionStatus!=-1) {
									$('#waStatus'+ actionId).val(actionStatus);
								}
								
								// 如果新状态为“未处理”，则如果流程状态为“已结束”，需改为“处理中”
								if ($('#checkStatus').val()=='<%=MyActionDb.CHECK_STATUS_NOT%>') {
									if ($('#status').val()=='<%=WorkflowDb.STATUS_FINISHED%>') {
										$('#status').val('<%=WorkflowDb.STATUS_STARTED%>');
									}
								}
							}
						},
						error : function(XMLHttpRequest, textStatus) {
							alert(XMLHttpRequest.responseText);
						}
					})
					$(this).dialog("close");
				}
			},
			closeOnEscape: true,
		draggable: true,
		resizable:true
		});		
}

function clearActionUser(actionId) {
	layer.confirm('您确定要清除用户么', {icon: 3, title: '提示'}, function (index) {
		layer.close();
		$.ajax({
			type:"post",
			url:"../public/flow/clearActionUser.do",
			data:{"actionId":actionId},
			success:function(data,status){
				data = $.parseJSON(data);
				layer.msg(data.msg, {
					offset: '6px'
				});
			},
			error:function(XMLHttpRequest, textStatus){
				//alert(XMLHttpRequest.responseText);
			}
		})
	});
}

$(document).ready( function() {
	$(".mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});
	
	$(".mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });
	});
});
</script>
</html>