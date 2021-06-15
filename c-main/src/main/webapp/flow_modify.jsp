<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="cn.js.fan.web.SkinUtil"%>
<%@ page import="com.cloudweb.oa.utils.ConstUtil"%>
<%@ page import="com.redmoon.oa.dept.DeptDb"%>
<%@ page import="com.redmoon.oa.dept.DeptMgr"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.message.MessageDb"%>
<%@ page import="com.redmoon.oa.oacalendar.OACalendarDb"%>
<%@ page import="com.redmoon.oa.person.PlanDb"%>
<%@ page import="com.redmoon.oa.person.UserDb"%>
<%@ page import="com.redmoon.oa.ui.LocalUtil"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Vector" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flow_id = ParamUtil.getInt(request, "flowId",-1);
WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flow_id);

if (wf.getTypeCode()==null) {
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
if (lf==null) {
	out.print(SkinUtil.makeErrMsg(request, "流程：" + wf.getTitle() + " 类型不存在！"));
	return;
}
FormDb fd = new FormDb();
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>查看/修改流程</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="js/layui/css/layui.css" media="all">
	<style type="text/css">
		@import url("util/jscalendar/calendar-win2k-2.css");

		.part-dept {
			color: blue;
		}

		#divTitle {
			margin-top: 10px;
			margin-bottom: 10px;
		}

		#btnEdit {
			width: 180px;
			height: 25px;
			line-height: 30px;
			letter-spacing: 2px;
			float: right;
			margin-right: 20px;
			margin-bottom: 10px;
			display: none;
		}

		#btnEdit a {
			background: #4e96f0;
			border-radius: 15px;
			color: #fff;
			display: block;
			width: 80px;
			float: left;
			margin-left: 10px;
			text-align: center;
		}

		#btnEdit a:hover {
			background: #6eB6f6;
			color: #fff
		}

		.icon-step {
			width: 16px;
			height: 16px;
			vertical-align: middle;
		}

		.main-content {
			padding: 5px 30px;
		}
	</style>
	<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
	<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
	<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
	<script src="inc/map.js"></script>
	<script src="inc/common.js"></script>
	<script src="js/jquery-1.9.1.min.js"></script>
	<script src="js/jquery-migrate-1.2.1.min.js"></script>
	<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script src="inc/flow_dispose.jsp"></script>
	<link rel="stylesheet" type="text/css" href="js/MyPaging/MyPaging.css">
	<script src="js/MyPaging/MyPaging.js"></script>
	<script type="text/javascript" src="js/goToTop/goToTop.js"></script>
	<link type="text/css" rel="stylesheet" href="js/goToTop/goToTop.css"/>

	<link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>

	<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>

	<script src="js/fixheadertable/jquery.fixheadertable.js"></script>
	<link rel="stylesheet" media="screen" href="js/fixheadertable/base.css"/>

	<link href="flowstyle.css" rel="stylesheet" type="text/css"/>

	<script src="inc/flow_js.jsp"></script>
	<script src="inc/ajax_getpage.jsp"></script>

	<script src="flow/form_js/form_js_<%=lf.getFormCode()%>.jsp?pageType=<%=ConstUtil.PAGE_TYPE_FLOW_SHOW%>&flowId=<%=flow_id %>&userName=<%=StrUtil.UrlEncode(privilege.getUser(request))%>"></script>
	<script src="js/layui/layui.js" charset="utf-8"></script>

	<script>
		function Operate() {
			jAlert('<lt:Label res="res.flow.Flow" key="notModifyState"/>', '提示');
		}

		function openWin(url, width, height) {
			var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width=" + width + ",height=" + height);
		}

		function getSelUserNames() {
			return o("nextActionUsers").value;
		}

		function getSelUserRealNames() {
			return o("userRealNames").value;
		}

		function openWinUsers() {
			openWin('user_multi_sel.jsp', 800, 600);
		}

		function openWinUserGroup() {
			openWin("user_usergroup_multi_sel.jsp", 520, 400);
		}

		function openWinUserRole() {
			openWin("user_role_multi_sel.jsp", 800, 600);
		}

		function setUsers(users, userRealNames) {
			if (users == "") {
				o("nextUsersDivs").innerHTML = "";
				o("nextActionUsers").value = "";
				o("userRealNames").value = "";
				return;
			}

			var uNameAry = users.split(",");
			var uRealNameAry = userRealNames.split(",");

			users = "";
			userRealNames = "";

			// 删除上次被选择，而本次未被选择的用户
			var userary = getSelUserNames().split(",");
			var len = userary.length;
			for (var k = 0; k < len; k++) {
				if (userary[k] == "")
					continue;
				var isFound = false;
				for (var i = 0; i < uNameAry.length; i++) {
					if (userary[k] == uNameAry[i]) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					document.getElementById("nextUsersDiv" + userary[k]).outerHTML = "";
				}
			}

			for (var i = 0; i < uNameAry.length; i++) {
				// 过滤掉已被选择的用户
				var len = userary.length;
				var isFound = false;
				for (var k = 0; k < len; k++) {
					if (userary[k] == uNameAry[i]) {
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					nextActionUserDiv.innerHTML += "<div id='nextUsersDiv" + uNameAry[i] + "' name='nextUsersDiv'>&nbsp;&nbsp;<input name='nextUsers' type='checkbox' checked value='" + uNameAry[i] + "'><span style='width:100px'>" + uRealNameAry[i] + "</span>&nbsp;&nbsp;到期时间：<input name='" + escape(uNameAry[i]).toUpperCase() + "_expireHour" + "' size=2 value=0>小时&nbsp;<a style='display:none' href='javascript:;' title='删除' style='font-size:16px;color:red' onclick=\"$('#nextUsersDiv" + uNameAry[i] + "').remove();\">×</a></div>";
				}
				if (users == "") {
					users = uNameAry[i];
					userRealNames = uRealNameAry[i];
				} else {
					users += "," + uNameAry[i];
					userRealNames += "," + uRealNameAry[i];
				}
			}

			o("nextActionUsers").value = users;
			o("userRealNames").value = userRealNames;
		}

		function deliver() {
			jConfirm('<lt:Label res="res.flow.Flow" key="submitForm"/>', '提示', function (r) {
				if (!r) {
					return;
				} else {
					$.ajax({
						type: "post",
						url: "flow/deliver.do",
						data: $("#deliverForm").serialize(),
						dataType: "html",
						beforeSend: function (XMLHttpRequest) {
							$('body').showLoading();
						},
						success: function (data, status) {
							data = $.parseJSON(data);
							if (data.ret == 1) {
								window.location.reload();
							} else {
								jAlert(data.msg, "提示");
							}
						},
						complete: function (XMLHttpRequest, status) {
							$('body').hideLoading();
						},
						error: function (XMLHttpRequest, textStatus) {
							// 请求出错处理
							jAlert(XMLHttpRequest.responseText, "提示");
						}
					});
				}
			})
		}

		$(function () {
			$(window).goToTop({
				showHeight: 1,//设置滚动高度时显示
				speed: 500 //返回顶部的速度以毫秒为单位
			});
		});
	</script>
</head>
<body>
<div id="bodyBox">
<%
fd = fd.getFormDb(lf.getFormCode());
if (!fd.isLoaded()) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","form");
	String str1 = LocalUtil.LoadString(request,"res.flow.Flow","noLongerExist");
	out.print(StrUtil.jAlert_Back(str + lf.getFormCode() + str1,"提示"));
	return;
}

com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
fdao = fdao.getFormDAO(flow_id, fd);

com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();

String myUserName = privilege.getUser(request);
String myRealName = um.getUserDb(myUserName).getRealName();

// 是来自于收文界面paper_received_list.jsp，置公文为已读状态
int paperId = ParamUtil.getInt(request, "paperId", -1);
if (paperId!=-1) {
	PaperDistributeDb pdd = new PaperDistributeDb();
	pdd = pdd.getPaperDistributeDb(paperId);

	if (pdd.getInt("is_readed")==0) {
		pdd.set("is_readed", new Integer(1));
		pdd.set("read_date", new java.util.Date());
		pdd.save();

		// 置日程为关闭状态
		PlanDb pd = new PlanDb();
		pd = pd.getPlanDb(myUserName, PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE, String.valueOf(paperId));
		if (pd!=null) {
			pd.setClosed(true);
			pd.save();
		}
	}
}

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + flow_id);
// 置嵌套表需要用到的curOperate
request.setAttribute("pageType", ConstUtil.PAGE_TYPE_FLOW_SHOW);
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

WorkflowRuler wr = new WorkflowRuler();

boolean isFlowManager = false;
LeafPriv lp = new LeafPriv(wf.getTypeCode());
if (privilege.isUserPrivValid(request, "admin.flow")) {
	if (lp.canUserExamine(privilege.getUser(request))) {
		isFlowManager = true;
	}
}

boolean canUserView = lp.canUserQuery(privilege.getUser(request));

if (!canUserView) {
	canUserView = privilege.isUserPrivValid(request, "paper.receive");
}

boolean isPaperReceived = false;
if (!canUserView) {
	// 从流程控件或知会中访问
	String visitKey = ParamUtil.get(request, "visitKey");
	if (!"".equals(visitKey)) {
		String fId = String.valueOf(flow_id);
		com.redmoon.oa.sso.Config ssoconfig = new com.redmoon.oa.sso.Config();
		String desKey = ssoconfig.get("key");
		visitKey = cn.js.fan.security.ThreeDesUtil.decrypthexstr(desKey, visitKey);
		if (visitKey.equals(fId)) {
			canUserView = true;
			isPaperReceived = true;
		}
	}
}

// 判断是否拥有查看流程过程的权限
if (!isFlowManager && !canUserView) {
	// 判断是否参与了流程
	if (!wf.isUserAttended(privilege.getUser(request))) {
		%>
		<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<%
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid"), true));
		return;
	}
}

boolean isStarted = wf.isStarted();
String op = ParamUtil.get(request, "op");
String prompt = LocalUtil.LoadString(request,"res.flow.Flow","prompt");
if ("addAnnex".equals(op)) {
	boolean re = false;
	try {
		WorkflowAnnexMgr wam = new WorkflowAnnexMgr();
		request.setAttribute("flowId", flow_id);
		re = wam.create(application, request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), prompt));
	}
	if (re) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str, prompt, "flow_modify.jsp?flowId=" + flow_id));
		//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_modify.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str, prompt));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;
}
else if (op.equals("delAnnex")) {
	boolean re = false;
	long annexId = ParamUtil.getLong(request, "annexId");
	try {
		WorkflowAnnexDb wad = new WorkflowAnnexDb();
		wad = (WorkflowAnnexDb)wad.getQObjectDb(new Long(annexId));
		re = wad.del();
	}
	catch (Exception e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), prompt));
	}
	if (re) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str, prompt, "flow_modify.jsp?flowId=" + flow_id));
		//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_modify.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str, prompt));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;
}
else if ("setExpireDate".equals(op)) {
	if (!isFlowManager) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"), prompt));
		return;
	}

	long myActionId = ParamUtil.getLong(request, "myActionId");

	MyActionDb mad = new MyActionDb();
	mad = mad.getMyActionDb(myActionId);
	java.util.Date expireDate = DateUtil.parse(ParamUtil.get(request, "expireDate"), "yyyy-MM-dd HH:mm");
	mad.setExpireDate(expireDate);
	if (mad.save()) {
		String str = LocalUtil.LoadString(request,"res.common","info_op_success");
		out.print(StrUtil.jAlert_Redirect(str, prompt, "flow_modify.jsp?flowId=" + flow_id));
	}
	else {
		String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str, prompt));
		//out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	return;
}
else if ("recall".equals(op)) {
	String msg = ParamUtil.get(request, "msg");
	out.print(StrUtil.jAlert(msg, prompt));
}

com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
String canUserModifyFlow = cfg.get("canUserModifyFlow");
String mode = "user";
if ("true".equals(canUserModifyFlow)) {
	mode = "user";
} else {
	mode = "view";
}

String flowExpireUnit = cfg.get("flowExpireUnit");
boolean isHour = !"day".equals(flowExpireUnit);
if (flowExpireUnit.equals("day")){
	flowExpireUnit = LocalUtil.LoadString(request,"res.flow.Flow","day");
}
else{
	flowExpireUnit = LocalUtil.LoadString(request,"res.flow.Flow","hour");
}
%>
<%@ include file="flow_modify_inc_menu_top.jsp"%>
<script>
if (o("menu1")) {
	o("menu1").className="current";
}
</script>
<div class="spacerH"></div>
	<div class="main-content">
<%
// 如果是自由流程且是发起人
if (ft.getType()==Leaf.TYPE_FREE && myUserName.equals(wf.getUserName())) {
	WorkflowPredefineDb wfp = new WorkflowPredefineDb();
	wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

	boolean isRoleMemberOfFlow = false;
	String[][] rolePrivs = wfp.getRolePrivsOfFree();
	int privLen = rolePrivs.length;
	for (int i=0; i<privLen; i++) {
		if (rolePrivs[i][0].equals(RoleDb.CODE_MEMBER)) {
			isRoleMemberOfFlow = true;
			break;
		}
	}
%>
    <form id="deliverForm" method="post">
		<div style="margin:0; padding:0; margin-bottom:10px">
			&nbsp;&nbsp;<img src="images/user.png" width="16" height="16" align="absmiddle"/>&nbsp;<lt:Label res="res.flow.Flow" key="submitTo"/>&nbsp;→&nbsp;
			<%if (isRoleMemberOfFlow) {%>
			<a href="javascript:;" onClick="openWinUsers()"><lt:Label res="res.flow.Flow" key="selectUser"/></a>
			<!--
			&nbsp;&nbsp; <a href="javascript:;" onClick="openWinUserGroup()"><lt:Label res="res.flow.Flow" key="group"/></a>&nbsp;&nbsp; <a href="javascript:;" onClick="openWinPersonUserGroup()"><lt:Label res="res.flow.Flow" key="myUserGroup"/></a>&nbsp;&nbsp;
			<%}%>
			<a href="javascript:;" onClick="openWinUserRole()"><lt:Label res="res.flow.Flow" key="byRole"/></a>
			-->
			<textarea name="userRealNames" style="display:none" cols="38" rows="3" readonly wrap="yes" id="userRealNames"></textarea>
			<textarea name="nextActionUsers" style="display:none"></textarea>
			<%
				if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
			%>
			<input id="isToMobile" name="isToMobile" value="true" type="checkbox" checked="checked"/>
			<lt:Label res="res.flow.Flow" key="sms"/>
			<%}%>
			<input id="isUseMsg" name="isUseMsg" value="true" type="checkbox" checked="checked"/>
			<lt:Label res="res.flow.Flow" key="message"/>
			&nbsp;&nbsp;
			<lt:Label res="res.flow.Flow" key="rem"/>：<input id="cwsWorkflowResult" name="cwsWorkflowResult" size="50" style="border:1px solid #cccccc; color:#888888;width:220px;"/>
			<input type="button" class="btn" onClick="deliver()" value='<lt:Label res="res.flow.Flow" key="submit"/>'/>
		</div>
		<div id="nextActionUserDiv">
		</div>
		<input name="flowId" type="hidden" value="<%=flow_id%>"/>
    </form>
<%
}

		Leaf lfParent = new Leaf();
		lfParent = lfParent.getLeaf(lf.getParentCode());

		int formViewId = ParamUtil.getInt(request, "formViewId", -1);
		FormViewDb formViewDb = new FormViewDb();
		Vector vView = formViewDb.getViews(lf.getFormCode());
	%>
	<div style="margin-left: 20px">
		<%
			if (vView.size()>0) {
		%>
		视图
		<select id="formView" title="切换视图">
			<option value="-1">默认</option>
			<%
				Iterator irView = vView.iterator();
				while (irView.hasNext()) {
					formViewDb = (FormViewDb)irView.next();
			%>
			<option value="<%=formViewDb.getLong("id")%>"><%=formViewDb.getString("name")%></option>
			<%
				}
			%>
		</select>
		<script>
			$(function() {
				$('#formView').val('<%=formViewId%>');
				$('#formView').change(function() {
					if ($(this).val()!="") {
						window.location.href = "flow_modify.jsp?flowId=<%=flow_id%>&formViewId=" + $(this).val();
					}
				})
			})
		</script>
		<%
			}
		%>
		<span id="btnEdit"></span>
	</div>
	<table cellSpacing="0" cellPadding="0" width="100%">
		<tbody>
		<tr>
			<td align="center" id="tableTitle" title="<%=lfParent.getName()%>：<%=lf.getName()%>">
				<%
					WorkflowPredefineDb wpd = new WorkflowPredefineDb();
					wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
				%>
				<%=WorkflowMgr.getLevelImg(request, wf)%>
				<strong style="font-size:18px">
					<%if (wpd.isLight()) {%>
					<%=MyActionMgr.renderTitle(request, wf)%>
					<%} else {%>
					<%=wf.getTitle()%>
					<%}%>
				</strong>
			</td>
		</tr>
		</tbody>
	</table>
        <%
		if (lf.getQueryId()!=Leaf.QUERY_NONE) {
			// 判断权限，管理员能看见查询，其它人员根据角色进行判断
			String[] roles = StrUtil.split(lf.getQueryRole(), ",");
			boolean canSeeQuery = false;
			if (!privilege.isUserPrivValid(request, "admin")) {
				if (roles!=null) {
					UserDb user = new UserDb();
					user = user.getUserDb(privilege.getUser(request));
					for (int i=0; i<roles.length; i++) {
						if (user.isUserOfRole(roles[i])) {
							canSeeQuery = true;
							break;
						}
					}
				}
				else {
					canSeeQuery = true;
				}
			}
			else {
				canSeeQuery = true;
			}
			FormQueryDb aqd = new FormQueryDb();
			aqd = aqd.getFormQueryDb((int)lf.getQueryId());
			if (canSeeQuery && aqd.isLoaded()) {
		%>
                <table style="width:100%" align="center"><tr><td align="center">
				<div id="formQueryBox"></div>
                </td></tr></table>
                <%
                String colratio = "";
                String colP = aqd.getColProps();
                if (colP == null || colP.equals("")) {
                	colP = "[]";
                }
				int tableWidth = 0;
                JSONArray jsonArray = new JSONArray(colP);
                for (int i=0; i<jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
					if (((Boolean)json.get("hide")).booleanValue()) {
						continue;
					}
                    String name = (String)json.get("name");
                    if (name.equalsIgnoreCase("cws_op")) {
						continue;
					}
					tableWidth += ((Integer)json.get("width")).intValue();
                    if (colratio.equals("")) {
						colratio = "" + ((Integer)json.get("width")).intValue();
					} else {
						colratio += "," + ((Integer)json.get("width")).intValue();
					}
                }

				// System.out.println(getClass() + " colratio=" + colratio);

                String queryAjaxUrl;
                if (aqd.isScript()) {
                    queryAjaxUrl = "flow/form_query_list_script_embed_ajax.jsp";
                }
                else {
                    queryAjaxUrl = "flow/form_query_list_embed_ajax.jsp";
                }

				JSONObject json = new JSONObject(lf.getQueryCondMap());
				Iterator irJson = json.keys();
                %>
                <script>
				function onQueryRelateFieldChange() {
                    $.ajax({
                        type: "post",
                        url: "<%=queryAjaxUrl%>",
                        data: {
                            id: "<%=lf.getQueryId()%>",
							<%
							while (irJson.hasNext()) {
								String qField = (String) irJson.next();
							%>
							<%=qField%> : "<%=fdao.getFieldValue(qField)%>",
							<%}%>
							flowTypeCode: "<%=lf.getCode()%>"
                        },
                        dataType: "html",
                        beforeSend: function(XMLHttpRequest){
                            $('#bodyBox').showLoading();
                        },
                        success: function(data, status){
                        	// 如果存在queryBox（内置于表单中）
							if (o("queryBox")) {
								o("queryBox").innerHTML = data;
							}
							else {
                            	o("formQueryBox").innerHTML = data;
							}
                            // var w = $(document).width() * 0.98;

                            $('#formQueryTable').fixheadertable({
                                caption : '<%=aqd.getQueryName()%>',
                                colratio    : [<%=colratio%>],
                                // height      : 150,
                                width       : <%=tableWidth+2%>,
                                zebra       : true,
                                // sortable    : true,
                                sortedColId : 1,
                                resizeCol   : true,
                                pager       : true,
                                rowsPerPage : 10,
                                // sortType    : ['integer', 'string', 'string', 'string', 'string', 'date'],
                                dateFormat  : 'm/d/Y'
                            });
                        },
                        complete: function(XMLHttpRequest, status){
                            $('#bodyBox').hideLoading();
                        },
                        error: function(XMLHttpRequest, textStatus){
							$('#bodyBox').hideLoading();
                            // 请求出错处理
                            jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
                        }
                    });
				}

                $(function() {
					onQueryRelateFieldChange();
                });
                </script>
            <%}%>
        <%}%>
<table id="mainTable" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" style="padding-left:5px;">
	<table width="100%"  border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td height="35" align="center">
          	<div id="divTitle">
            <%
			int doc_id = wf.getDocId();
			DocumentMgr dm = new DocumentMgr();
			Document doc = dm.getDocument(doc_id);
			%>
            <lt:Label res="res.flow.Flow" key="organ"/>：<%=um.getUserDb(wf.getUserName()).getRealName()%>&nbsp;&nbsp;
			<lt:Label res="res.flow.Flow" key="state"/>：<%=wf.getStatusDesc()%>&nbsp;&nbsp;
				ID：<%=wf.getId()%>
				<%
					if (isFlowManager &&  wf.getStatus()==WorkflowDb.STATUS_DELETED) {
						if (!"".equals(StrUtil.getNullStr(wf.getDelUser()))) {
							out.print("&nbsp;&nbsp;删除者：" + um.getUserDb(wf.getDelUser()).getRealName());
							out.print("&nbsp;&nbsp;删除时间：" + DateUtil.format(wf.getDelDate(), "yyyy-MM-dd HH:mm:ss"));
						}
					}
				%>
            <span id="projectName">
            <%if (wf.getProjectId()!=-1) {
                com.redmoon.oa.visual.FormDAO fdaoPrj = new com.redmoon.oa.visual.FormDAO();
                FormDb prjFd = new FormDb();
                prjFd = prjFd.getFormDb("project");
				fdaoPrj = fdaoPrj.getFormDAO((int)wf.getProjectId(), prjFd);
                %>
                &nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="project"/>：<a href="javascript:;" onClick="addTab('<%=fdaoPrj.getFieldValue("name")%>', 'project/project_show.jsp?projectId=<%=wf.getProjectId()%>&formCode=project')"><%=fdaoPrj.getFieldValue("name")%></a>&nbsp;&nbsp;<a title="<lt:Label res='res.flow.Flow' key='disassociate'/>" href="javascript:;" onClick="unlinkProject()" style='font-size:16px; font-color:red'>×</a>
                <%
            }
            %>
            </span>
            </div>
            </td>
        </tr>
        <tr>
          <td><table width="100%" border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center">
                <div id="formAllDiv">
                <form id="flowForm" name="flowForm" action="">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td>
					<%
					Render rd = new Render(request, wf, doc);
					if (formViewId!=-1) {
						out.print(rd.reportForView(formViewId, true));
					}
					else {
						out.print(rd.report());
					}
					%>
                    	</td>
                      </tr>
                  </table>
                  </form>
          <%
		  java.util.Vector attachments = null;
		  if (doc!=null) {
			  attachments = doc.getAttachments(1);
		  }
		  WorkflowAnnexAttachment wfaa = new WorkflowAnnexAttachment();
		  java.util.Vector annexAtt = wfaa.getAllAttachments(flow_id);
		  if ((attachments!=null && attachments.size()>0) || (annexAtt != null && annexAtt.size() > 0)) {
		  %>
          <table width="100%" border="0" cellspacing="0" cellpadding="0" class="percent98">
            <tr>
              <td height="30" align="left"><strong>&nbsp;<lt:Label res="res.flow.Flow" key="accessory"/>：</strong></td>
            </tr>
          </table>
          <div id="attDiv">
          <table id="attTable" class="tabStyle_1 percent98" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td height="31" align="right" class="tabStyle_1_title">&nbsp;</td>
            <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="fileName"/></td>
            <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="creator"/></td>
            <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="time"/></td>
            <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="size"/></td>
            <td align="center" class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="operate"/></td>
            </tr>
        	<%
			  java.util.Iterator ir = attachments.iterator();
			  String creatorRealName = "";
			  while (ir.hasNext()) {
				Attachment am = (Attachment) ir.next();

				if (am.getFieldName()!=null && !"".equals(am.getFieldName())) {
					// IOS上传fieldName为upload
					if (!am.getFieldName().startsWith("att") && !am.getFieldName().startsWith("upload")) {
						// 不再跳过，因为ntko office在线编辑宏控件对应的为表单域的编码
						// continue;
					}
				}

				UserDb creator = um.getUserDb(am.getCreator());
				if (creator.isLoaded()) {
					creatorRealName = creator.getRealName();
				}
			%>
          <tr id="trAtt<%=am.getId()%>">
            <td width="2%" height="31" align="center"><img src="images/attach.gif" /></td>
            <td width="51%" align="left">
			&nbsp;<span id="spanAttLink<%=am.getId()%>"><a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flow_id%>" target="_blank"><span id="spanAttName<%=am.getId()%>"><%=am.getName()%></span></a></span>
			</td>
            <td width="10%"><%=creatorRealName%></td>
            <td width="15%" align="center"><%=DateUtil.format(am.getCreateDate(), "yyyy-MM-dd HH:mm")%></td>
            <td width="11%" align="center"><%=NumberUtil.round((double)am.getSize()/1024000, 2)%>M</td>
            <td width="11%" align="center">
				<%
					if (privilege.isUserPrivValid(request, "admin") || isFlowManager) {
				%>
				<a href="javascript:;" onClick="delAtt('<%=doc_id%>', '<%=am.getId()%>')"><lt:Label res="res.flow.Flow" key="delete"/></a>
				&nbsp;&nbsp;
				<%
					}
				%>
            <a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flow_id%>" target="_blank"><lt:Label res="res.flow.Flow" key="download"/>
            </span></a>
            &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=am.getName()%> 日志', 'flow/att_log_list.jsp?flowId=<%=flow_id%>&attId=<%=am.getId()%>')">日志</a>
            <%
			if ("true".equals(cfg.get("canConvertToPDF")) && (StrUtil.getFileExt(am.getName()).equals("doc") || StrUtil.getFileExt(am.getName()).equals("docx"))) {
				%>
				&nbsp;&nbsp;<a href="flow_getfile.jsp?op=toPDF&flowId=<%=flow_id%>&attachId=<%=am.getId()%>" target="_blank"><lt:Label res="res.flow.Flow" key="downloadPDF"/></a>
				<%
			}

            if (cfg.getBooleanProperty("canPdfFilePreview") || cfg.getBooleanProperty("canOfficeFilePreview")) {
                String s = Global.getRealPath() + am.getVisualPath() + "/" + am.getDiskName();
                String htmlfile = s.substring(0, s.lastIndexOf(".")) + ".html";
                java.io.File fileExist = new java.io.File(htmlfile);
                if (fileExist.exists()) {
                    %>
                    &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=am.getName()%>', '<%=request.getContextPath()%>/<%=am.getVisualPath()%>/<%=am.getDiskName().substring(0, am.getDiskName().lastIndexOf(".")) + ".html"%>')">预览</a>
                    <%
                }
            }
          %>
            </td>
          </tr>
        <%}%>
        <%
        java.util.Iterator annexIr = annexAtt.iterator();
        while (annexIr.hasNext()) {
        	WorkflowAnnexAttachment wfaatt = (WorkflowAnnexAttachment) annexIr.next();
        	long annexId = wfaatt.getAnnexId();
        	WorkflowAnnexDb wfadb = new WorkflowAnnexDb();
        	wfadb = (WorkflowAnnexDb) wfadb.getQObjectDb(annexId);
        	String annexUser = wfadb.getString("user_name");
        	UserDb annexUserDb = new UserDb(annexUser);
        	%>
            <tr>
              <td width="2%" height="31" align="center"><img src="images/attach.gif" /></td>
              <td width="51%" align="left">
  			&nbsp;<a href="<%=wfaatt.getAttachmentUrl(request)%>" target="_blank"><%=wfaatt.getName()%></a>
  			</td>
              <td width="10%"><%=annexUserDb.getRealName()%>  (附言)</td>
              <td width="15%" align="center"><%=DateUtil.format(wfadb.getDate("add_date"), "yyyy-MM-dd HH:mm")%></td>
              <td width="11%" align="center"><%=NumberUtil.round((double)wfaatt.getSize()/1024000, 2)%>M</td>
              <td width="11%" align="center">
              <a href="<%=wfaatt.getAttachmentUrl(request)%>" target=_blank><lt:Label res="res.flow.Flow" key="download"/></a>
              </td>
              </tr>
        <%
        }
        %>
		</table>
        </div>
        <%}%>
              <%
String sql = "select id from flow_my_action where flow_id=" + flow_id + " order by receive_date asc";
MyActionDb mad = new MyActionDb();
Vector v = mad.list(sql);
%>
                  <table align="center" width="98%" border="0" cellspacing="0" cellpadding="0" class="percent98">
                    <tr>
                      <td height="30" align="left"><strong>&nbsp;<lt:Label res="res.flow.Flow" key="cprocess"/>：</strong></td>
                    </tr>
                  </table>
                  <table align="center" class="tabStyle_1 percent98 mainTable">
					  <thead>
                      <tr>
                        <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="handler"/></td>
                        <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="bearer"/></td>
                        <td class="tabStyle_1_title" width="5%" align="center"><lt:Label res="res.flow.Flow" key="agent"/></td>
                        <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="task"/></td>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="reachState"/></td>
                        <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="signTime"/></td>
                        <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="handleTime"/></td>
						  <%
							  if (cfg.getBooleanProperty("flowPerformanceDisplay")) {
						  %>
                        <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="expirationDate"/></td>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="remainTime"/></td>
                        <td class="tabStyle_1_title" width="4%" align="center"><lt:Label res="res.flow.Flow" key="timeSpent"/><br />
                        (<%=flowExpireUnit%>)</td>
                        <td class="tabStyle_1_title" width="3%" align="center"><lt:Label res="res.flow.Flow" key="achievements"/></td>
						  <%
							  }
						  %>
                        <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="processor"/></td>
                        <td class="tabStyle_1_title" width="6%" align="center"><lt:Label res="res.flow.Flow" key="processeStatus"/></td>
						  <%
							  if (wpd.isReactive()) {
						  %>
						  <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="alterTime"/></td>
						  <%
							  }
						  %>
                        <td class="tabStyle_1_title" width="7%" align="center"><lt:Label res="res.flow.Flow" key="rem"/></td>
                        <td class="tabStyle_1_title" width="8%" align="center"><lt:Label res="res.flow.Flow" key="operate"/></td>
                      </tr>
					  </thead>
					  <tbody>
					<%
					WorkflowDb wfd = new WorkflowDb();
					wfd = wfd.getWorkflowDb((int)mad.getFlowId());
					WorkflowActionDb wa = new WorkflowActionDb();

					java.util.Iterator ir = v.iterator();
                    DeptMgr deptMgr = new DeptMgr();
                    OACalendarDb oad = new OACalendarDb();
                    int m=0;
                    while (ir.hasNext()) {
                        mad = (MyActionDb)ir.next();

                        String userName = mad.getUserName();
                        String userRealName = "";
                        if (userName!=null) {
                            UserDb user = um.getUserDb(mad.getUserName());
                            userRealName = user.getRealName();
                        }
                        wa = wa.getWorkflowActionDb((int)mad.getActionId());
                        m++;
                        %>
                      <tr class="highlight">
                        <td>
							<%
								if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_NOT) {
									%>
									<img src="images/flow/doing.png" class="icon-step"/>
									<%
								}
								else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_CHECKED) {
									%>
									<img src="images/flow/done.png" class="icon-step"/>
									<%
								}
								else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_WAITING_TO_DO) {
									%>
									<img src="images/flow/waiting.png" class="icon-step"/>
									<%
								}
								else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_PASS || mad.getCheckStatus()== MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
									%>
									<img src="images/flow/pass.png" class="icon-step"/>
									<%
								}
								else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_SUSPEND) {
									%>
									<img src="images/flow/suspend.png" class="icon-step"/>
									<%
								}
								else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_RETURN) {
									%>
									<img src="images/flow/returned.png" class="icon-step"/>
									<%
								}
								else {
									%>
									<img src="images/flow/transfer.png" class="icon-step"/>
									<%
								}
							%>
						<%
						String partDept = ""; // 所选择的兼职部门
						if (!"".equals(mad.getPartDept())) {
							partDept = mad.getPartDept();
						}
						  String deptCodes = mad.getDeptCodes();
						  String[] depts = StrUtil.split(deptCodes, ",");
						  if (depts!=null) {
							String dts = "";
							int deptLen = depts.length;
							for (int n=0; n<deptLen; n++) {
								DeptDb dd = deptMgr.getDeptDb(depts[n]);
								if (dd!=null) {
									String deptName = dd.getName();
									if (dd.getCode().equals(partDept)) {
										deptName = "<span class='part-dept' title='处理时选择的部门'>" + deptName + "</span>";
									}
									if (dts.equals("")) {
										dts = deptName;
									} else {
										dts += "," + deptName;
									}
								}
							}
							if (!dts.equals("")) {
								out.print(dts + "：");
							}
						  }

						  boolean isExpired = false;
						  java.util.Date chkDate = mad.getCheckDate();
						  if (chkDate==null) {
							  chkDate = new Date();
						  }
						  if (DateUtil.compare(chkDate, mad.getExpireDate())==1) {
							isExpired = true;
						  }
						  if (isExpired) {%>
							  <img src="images/flow/expired.png" align="absmiddle" alt="<lt:Label res='res.flow.Flow' key='timeOut'/>" />
						  <%}%>
                      	  <%=userRealName%>
						<%
                        if (!mad.isReaded()) {
                        %>
                        &nbsp;
                        <img src="images/icon_new.gif" />
                        <%
                        }
                        %>
                        </td>
                        <td><%
					  if (mad.getPrivMyActionId()!=-1) {
					  	MyActionDb mad2 = mad.getMyActionDb(mad.getPrivMyActionId());
						out.print(um.getUserDb(mad2.getUserName()).getRealName());
					  }
					  else {
						  out.print("&nbsp;");
					  }
					  %>
                        </td>
                        <td>
                        <%
						if (!mad.getProxyUserName().equals("")) {
							out.print(um.getUserDb(mad.getProxyUserName()).getRealName());
						}
						%>
                        </td>
                        <td><%=StrUtil.getNullStr(wa.getTitle())%></td>
                        <td align="center">
						<%if (wa.getDateDelayed()!=null) {%>
                        	<lt:Label res="res.flow.Flow" key="delay"/>
                        <%}else{%>
                            <%=DateUtil.format(mad.getReceiveDate(), "MM-dd HH:mm")%>
                            <br/>
							<%=WorkflowActionDb.getStatusName(mad.getActionStatus())%>
                            <%
							String reas = wa.getReason();
							if (reas!=null && !"".equals(reas.trim())) {%>
                            <BR />
                            (<%=reas%>)
                            <%}%>
                        <%}%>
                        </td>
                        <td align="center"><%=DateUtil.format(mad.getReadDate(), "MM-dd HH:mm")%> </td>
                        <td align="center"><%=DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm")%></td>
                          <%
                              if (cfg.getBooleanProperty("flowPerformanceDisplay")) {
                          %>
                        <td align="center">
						<%
						// @task:如果存在嵌套表，jscalendar会引起IE崩溃
						if (isFlowManager && !mad.isChecked()) {%>
							<a href="javascript:;" title="<lt:Label res='res.flow.Flow' key='clickChangeTime'/>" onClick="changeExpireDate('<%=mad.getId()%>', '<%=DateUtil.format(mad.getExpireDate(), "MM-dd HH:mm")%>')"><%=DateUtil.format(mad.getExpireDate(), "MM-dd HH:mm")%></a>
						<%}else{%>
                        	<span><%=DateUtil.format(mad.getExpireDate(), "MM-dd HH:mm")%></span>
                        <%}%>
                        </td>
                        <td align="center">
                        <%
						String remainDateStr = "";
						if (mad.getExpireDate()!=null && DateUtil.compare(new java.util.Date(), mad.getExpireDate())==2) {
							int[] ary = DateUtil.dateDiffDHMS(mad.getExpireDate(), new java.util.Date());
							String str_day = LocalUtil.LoadString(request,"res.flow.Flow","day");
							String str_hour = LocalUtil.LoadString(request,"res.flow.Flow","h_hour");
							String str_minute = LocalUtil.LoadString(request,"res.flow.Flow","minute");
							remainDateStr = ary[0] + " "+str_day + ary[1] + " "+str_hour + ary[2] + " "+str_minute;
							out.print(remainDateStr);
						}%>
                        </td>
                        <td align="center"><%
					  if (isHour) {
						double d = oad.getWorkHourCount(mad.getReceiveDate(), mad.getCheckDate());
						out.print(NumberUtil.round(d, 1));
					  }
					  else {
						int d = oad.getWorkDayCountFromDb(mad.getReceiveDate(), mad.getCheckDate());
						out.print(d);
					  }
					  %>
                      	</td>
                        <td align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%> </td>
                          <%
                              }
                          %>
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
                        <td align="center" class="<%=MyActionDb.getCheckStatusClass(mad.getCheckStatus())%>">
						<%
						if (mad.getChecker().equals(UserDb.SYSTEM)) {
							out.print(LocalUtil.LoadString(request,"res.flow.Flow","systemAccessed"));
						}else{
						%>
						<%=mad.getCheckStatusName()%>
						<%}
						if (mad.getCheckStatus()!=0 && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_TRANSFER && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
							  if (mad.getResultValue()!=WorkflowActionDb.RESULT_VALUE_RETURN) {
								  if (mad.getSubMyActionId()==MyActionDb.SUB_MYACTION_ID_NONE) {
									  out.print("<BR>(" + WorkflowActionDb.getResultValueDesc(mad.getResultValue()) + ")");
								  }
							  }
						}
						%>
                        </td>
						  <%
							  if (wpd.isReactive()) {
						  %>
						  <td align="center"><%=DateUtil.format(mad.getAlterTime(), "MM-dd HH:mm")%></td>
						  <%
							  }
						  %>
                        <td align="center">
							<%=MyActionMgr.renderResult(request, mad)%>
                        <%
						if (mad.getSubMyActionId()!=MyActionDb.SUB_MYACTION_ID_NONE) {
							MyActionDb submad = new MyActionDb();
							submad = submad.getMyActionDb(mad.getSubMyActionId());
							String str1 = LocalUtil.LoadString(request,"res.flow.Flow","subprocess");
							out.print("&nbsp;<a href=\"javascript:;\" onclick=\"addTab('" + str1 + "', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + submad.getFlowId() + "')\"><font color='red'>" + str1 + "</font></a>");
						}
						%>
                        </td>
                        <td align="center"><%
                        boolean showSep = false;
					  if (isFree) {%>
                        <%
						if ((myUserName.equals(mad.getUserName()) || myUserName.equals(mad.getProxyUserName())) && !mad.isChecked()) {
							showSep = true;
							if (wpd.isLight()) {
							%>
                          		<a href="flow_dispose_light.jsp?myActionId=<%=mad.getId()%>"><lt:Label res="res.flow.Flow" key="handle"/></a>
							<%
							}
							else {
							%>
                          		<a href="flow_dispose_free.jsp?myActionId=<%=mad.getId()%>"><lt:Label res="res.flow.Flow" key="handle"/></a>
                        <%	}
						}
						  if (isReactive && (wf.getStatus()==WorkflowDb.STATUS_FINISHED || wf.getStatus()==WorkflowDb.STATUS_DISCARDED) && (mad.getUserName().equals(myUserName) || mad.getProxyUserName().equals(myUserName)) && mad.isChecked() && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND_OVER && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {%>
                          <%=showSep ? "&nbsp;&nbsp;" : "" %><a title='<lt:Label res="res.flow.Flow" key="reActivated"/>' href='flow_dispose_free.jsp?myActionId=<%=mad.getId()%>'><lt:Label res="res.flow.Flow" key="reactivation"/></a>
                          <%
                          showSep = true;
						  }%>
                          <%if (!isPaperReceived && isRecall && mad.canRecall(myUserName)) {%>
                          <%=showSep ? "&nbsp;&nbsp;" : "" %><a href='javascript:;' onClick="recallFree(<%=wfd.getId()%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="withdraw"/></a>
                          <%
                          showSep = true;
                          }
					  }else{
							  boolean canHandle = true;
							  // 如果是激活状态，但是流程已被放弃，则不显示“处理”按钮
							  if (wa.getStatus() == WorkflowActionDb.STATE_DOING || wa.getStatus() == WorkflowActionDb.STATE_RETURN) {
								  if (wf.getStatus() == WorkflowDb.STATUS_DISCARDED) {
									  canHandle = false;
								  }
							  }
							  
							  if (canHandle) {
								if (mad.getChecker().equals(UserDb.SYSTEM)) {
									;
								}
								else {
									if ((myUserName.equals(mad.getUserName()) || myUserName.equals(mad.getProxyUserName())) && !mad.isChecked() && mad.getCheckStatus() != MyActionDb.CHECK_STATUS_WAITING_TO_DO) {
								  %>
									<a href="flow_dispose.jsp?myActionId=<%=mad.getId()%>"><lt:Label res="res.flow.Flow" key="handle"/></a>
							<script>
								$(function () {
									$("#btnEdit").show();
									$("#btnEdit").append('<a href="flow_dispose.jsp?myActionId=<%=mad.getId()%>">处理</a>');
								});
							</script>
									<%
											showSep = true;
										}
										if (isReactive && (wf.getStatus()==WorkflowDb.STATUS_FINISHED || wf.getStatus()==WorkflowDb.STATUS_DISCARDED) && (mad.getUserName().equals(myUserName) || mad.getProxyUserName().equals(myUserName)) && mad.isChecked() && mad.getCheckStatus() != MyActionDb.CHECK_STATUS_SUSPEND_OVER && mad.getCheckStatus() != MyActionDb.CHECK_STATUS_PASS_BY_RETURN) {
									%>
										<%=showSep ? "&nbsp;&nbsp;" : "" %><a title='<lt:Label res="res.flow.Flow" key="reActivated"/>' href='flow_dispose.jsp?myActionId=<%=mad.getId()%>'>
										<lt:Label res="res.flow.Flow" key="reactivation"/></a>
									<%
											showSep = true;
										}
									%>
									<%
										if (!isPaperReceived && isRecall && mad.canRecall(myUserName)) {
									%>
									  <%=showSep ? "&nbsp;&nbsp;" : "" %><a href='javascript:;' onClick="recall(<%=flow_id%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="withdraw"/></a>
									<script>
										$(function () {
											$("#btnEdit").show();
											$("#btnEdit").append('<a href="javascript:;" onclick="recall(<%=flow_id%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="withdraw"/></a>');
										});
									</script>
									<%
										showSep = true;
									}
								}
							  }
							}
						%>
                      <%
						  if (!isPaperReceived && !mad.isChecked() && !privilege.getUser(request).equals(mad.getUserName()) && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_PASS && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_WAITING_TO_DO) {
						String action = "action=" + MessageDb.ACTION_FLOW_DISPOSE + "|myActionId=" + mad.getId();
					  %>
                  		<%=showSep ? "&nbsp;&nbsp;" : "" %><a href="javascript:;" onClick="addTab('<lt:Label res="res.flow.Flow" key="reminders"/>', 'message_oa/message_frame.jsp?op=send&receiver=<%=StrUtil.UrlEncode(mad.getUserName())%>&title=<%=StrUtil.UrlEncode(LocalUtil.LoadString(request,"res.flow.Flow","possible")+"：" + wf.getTitle())%>&action=<%=StrUtil.UrlEncode(action)%>', 320, 262)"><lt:Label res="res.flow.Flow" key="reminders"/></a>
                      <%}%>
                      </td>
                      </tr>
                      <%}%>
                    </tbody>
                  </table>
                  </div>
                  <table width="98%" class="percent98"><tr><td align="left">
				  <%
				  WorkflowActionDb actionDelayed = new WorkflowActionDb();
				  Vector vdelayed = actionDelayed.getActionsDelayedOfFlow(flow_id);
				  Iterator irdelayed = vdelayed.iterator();
				  if (vdelayed.size()>0) {
					  %>
					  <div>
                      <img src="images/job.png" align="absmiddle" />&nbsp;<lt:Label res="res.flow.Flow" key="delayAction"/>：
					  <%
					  while (irdelayed.hasNext()) {
						actionDelayed = (WorkflowActionDb)irdelayed.next();
						%>
						<%=actionDelayed.getTitle()%>&nbsp;-&nbsp;<%=actionDelayed.getJobName()%>
						<%
					  }
					  %>
					  ，<lt:Label res="res.flow.Flow" key="stime"/>：<%=DateUtil.format(actionDelayed.getDateDelayed(), "yyyy-MM-dd HH:mm")%>
					  </div>
					  <%
				  }
				  %>
				  <%=wf.getRemark()%>
                  </td></tr></table>
					<table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px">
						<tr>
							<td align="center"><%
								if (privilege.isUserPrivValid(request, "admin") || wr.canMonitor(request, wf)) {
									if (wr.canUserDelFlow(request, wf)) {%>
								<!--&nbsp;&nbsp;&nbsp;&nbsp;
								<input type=button class="button1" onclick="window.location.href='flow_del.jsp?flow_id=<%=flow_id%>'" value=" 删 除 " />
								-->
								<%}%>
								<!--&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" class="button1" onclick="window.location.href='flow_monitor.jsp?flowId=<%=flow_id%>'" value="监控人员"/>
								-->
								<%}%>
								<%if (wr.canUserStartFlow(request, wf)) {%>
								<!--&nbsp;&nbsp;&nbsp;&nbsp;
								<input name="button" type=button class="button1" onclick="window.location.href='flow_dispose.jsp?flowId=<%=flow_id%>'" value=" 办 理 " />-->
								<%}%>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" class="btn" onclick="showFormReport()" value='<lt:Label res="res.flow.Flow" key="printForm"/>'/>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" class="btn" onclick="showFormReportAll()" value='<lt:Label res="res.flow.Flow" key="printAllForms"/>'/>
								<%
									if (cfg.getBooleanProperty("flowIsBtnExportWordShow")) {
								%>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" class="btn" title="" onclick="exportToWord()" value="<lt:Label res="res.flow.Flow" key="exportWord"/>"/>
								<%
									}
									boolean isProjectUsed = cfg.get("isProjectUsed").equals("true");
									if (isProjectUsed && isFlowManager && com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" class="btn" onClick="linkProject()" value='<lt:Label res="res.flow.Flow" key="connProject"/>'/>
								<%}%>
							</td>
						</tr>
					</table>
                  <br>
                  <%
					String replyDis = wf.isStarted() ? "" : "display:none";
					if (!wpd.isReply()) {
						replyDis = "display:none";
					}
				  %>
                  <div style="width:98%;background-color:#efefef;padding-top:10px;padding-bottom:10px;;<%=replyDis%>">
                  <div style="width:98%;background-color:white;padding-top:10px;">
                  <table class="" width="95%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="left" style="text-align:left"><strong>&nbsp;附言：</strong>
                      <%if (fd.isProgress()) {
					  %>
				      总进度<span id="totalProgress"><%=fdao.getCwsProgress()%></span>%）
                      <%}%>
                      </td>
                      <td style="text-align:right">
                      	<input id="showDiv" style="display:none" class="mybtn2" type="button" value="展开" onClick="show()"/>
                      	<input id="notShowDiv" class="mybtn2" type="button" value="收起" onClick="notshow()"/>
                      	<input class="mybtn2" type="button" value="回复" onClick="addMyReply('<%=0%>')"/>
 					  </td>
                    </tr>
                    <tr>
                     	<td align="left" colspan="2">
                    		 <div id="myReplyTextarea<%=0%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
		                		<form id="flowForm<%=0%>" name="flowForm<%=0%>" method="post">
									<textarea name="myReplyTextareaContent" id="get<%=0%>" class="myTextarea"></textarea>
									<span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onClick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" id="isSecret<%=0%>" name="isSecret<%=0%>" value="0"/></span>
                                    <%if (fd.isProgress()) {%>
                                    &nbsp;&nbsp;进度&nbsp;<input id="cwsProgress" name="cwsProgress" style="width:30px; height:22px;" value="0" readonly />
                                    <div id="slider" style="margin-left:10px;width:200px; display:inline-block; *display:inline;*zoom:1;"></div>
                                    <%}%>

                                    <script>
									$(function() {
										$( "#slider" ).slider({
										  value:0,
										  min: 0,
										  max: 100,
										  step: 5,
										  slide: function( event, ui ) {
											$( "#cwsProgress" ).val( ui.value );
										  }
										});
									});
									</script>
                            		<input type="hidden" id="myActionId<%=0%>" name="myActionId<%=0%>" value=""/>
									<input type="hidden" id="discussId<%=0%>" name="discussId<%=0%>" value="<%=0 %>"/>
									<input type="hidden" id="flow_id<%=0%>" name="flow_id<%=0%>" value="<%=flow_id %>"/>
									<input type="hidden" id="action_id<%=0%>" name="action_id<%=0%>" value="<%=0 %>"/>
									<input type="hidden" id="user_name<%=0%>" name="user_name<%=0%>" value="<%=myUserName%>"/>
									<input type="hidden" id="userRealName<%=0%>" name="userRealName<%=0%>" value="<%=myRealName%>"/>
									<input type="hidden" id="reply_name<%=0%>" name="reply_name<%=0%>" value="<%=myUserName%>"/>
									<input type="hidden" id="parent_id<%=0%>" name="parent_id<%=0%>" value="<%=-1%>"/>
									<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onClick="submitPostscript('<%=0%>','<%=0%>')"/>
								</form>
					 		</div>
					 	 </td>
					 </tr>
                    <tr >
                     	<td colspan="2">
                     	    <hr class="hrLine"/>
                        </td>
                    </tr>
                 </table>
                 <div id="divShow" style="width:98%;">
                 <table id="tablehead" class="" width="95%" border="0" cellspacing="3" cellpadding="0">
                 </table>
                  <%
		WorkflowAnnexDb wad = new WorkflowAnnexDb();
		Vector<WorkflowAnnexDb> vec1 = wad.listRoot(flow_id, myUserName);

        Iterator<WorkflowAnnexDb> ir1 = vec1.iterator();

		while (ir1.hasNext()) {
			wad = ir1.next();
			int id = (int)wad.getLong("id");
			int n=1;
		%>
		<table id="replaytable<%=id%>" class="" width="95%" border="0" >
                    <tr id="trReply<%=id%>">
                      <td width="50" style="text-align:left;" class="nameColor">
                      	  <%=um.getUserDb(wad.getString("user_name")).getRealName()%>&nbsp;:
                      </td>
                      <td width="60%" style="text-align:left;word-break:break-all">
                          <%
						  if (fd.isProgress()) {
						  	%>
							<div>进度：<%=wad.getInt("progress")%>%</div>
							<%
						  }
						  %>
                      	  <%=wad.getString("content")%>
                      	  <%if (isFlowManager) {%>
                      		  <a href="javascript:;" onClick="delAnnex('<%=wad.getLong("id")%>')">[<lt:Label res="res.flow.Flow" key="delete"/>]</a>
                      	  <%}%>
                      </td>
                      <td width="" style="text-align:right;">
                      	  <%=DateUtil.format(wad.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%> &nbsp;&nbsp;
                      	  <a align="right" class="comment" href="javascript:;" onClick="addMyReply('<%=id%>')"><img title="<lt:Label res="res.flow.Flow" key="replyTo"/>" src="images/dateline/replyto.png"/></a>
                      </td>
                     </tr>
                     <tr>
                     	 <td align="left" colspan="3">
                    		 <div id="myReplyTextarea<%=id%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
		                		<form id="flowForm<%=id%>" name="flowForm<%=id%>" method="post">
									<textarea name="myReplyTextareaContent" id="get<%=id%>" class="myTextarea"></textarea>
									<span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onClick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" id="isSecret<%=id%>" name="isSecret<%=id%>" value="0"/></span>
                            		<input type="hidden" id="myActionId<%=id%>" name="myActionId<%=id%>" value=""/>
									<input type="hidden" id="discussId<%=id%>" name="discussId<%=id%>" value="<%=id %>"/>
									<input type="hidden" id="flow_id<%=id%>" name="flow_id<%=id%>" value="<%=wad.getString("flow_id") %>"/>
									<input type="hidden" id="action_id<%=id%>" name="action_id<%=id%>" value="<%=wad.getString("action_id") %>"/>
									<input type="hidden" id="user_name<%=id%>" name="user_name<%=id%>" value="<%=myUserName%>"/>
									<input type="hidden" id="userRealName<%=id%>" name="userRealName<%=id%>" value="<%=myRealName%>"/>
									<input type="hidden" id="reply_name<%=id%>" name="reply_name<%=id%>" value="<%=wad.getString("user_name")%>"/>
									<input type="hidden" id="parent_id<%=id%>" name="parent_id<%=id%>" value="<%=id%>"/>
									<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onClick="submitPostscript('<%=id%>','<%=id%>')"/>
								</form>
					 		</div>
					 	 </td>
					 </tr>
                    <%
			       	    WorkflowAnnexDb wad2 = new WorkflowAnnexDb();
					    Vector<WorkflowAnnexDb> vec2 = wad2.listChildren(wad.getInt("id"), myUserName);
					    Iterator<WorkflowAnnexDb> ir2 = vec2.iterator();

					    while (ir2.hasNext()) {
						    wad2 = ir2.next();
							int id2 = (int)wad2.getLong("id");
					%>
					<tr id="trReply<%=id2%>" pId="<%=id%>">
                        <td width="180" style="text-align:left;" class="nameColor">
					  	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=um.getUserDb(wad2.getString("user_name")).getRealName()%>&nbsp;回复&nbsp;<%=um.getUserDb(wad2.getString("reply_name")).getRealName()%>&nbsp;:
					  	</td>
					  	<td style="text-align:left;">
					  		<%=wad2.getString("content")%>
					  		<%if (isFlowManager) {%>
                      		  <a href="javascript:;" onClick="delAnnex('<%=wad2.getLong("id")%>')">[<lt:Label res="res.flow.Flow" key="delete"/>]</a>
                      	   <%}%>
					  	</td>
					  	<td style="text-align:right;">
                      	  <%=DateUtil.format(wad2.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%> &nbsp;&nbsp;
                      	  <a align="right" class="comment" href="javascript:;" onClick="addMyReply('<%=id2%>')"><img title="<lt:Label res="res.flow.Flow" key="replyTo"/>" src="images/dateline/replyto.png"/></a>
                      	</td>
                    </tr>
                    <tr>
                     	 <td align="left" colspan="3">
                    		 <div id="myReplyTextarea<%=id2%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
		                		<form id="flowForm<%=id2%>" name="flowForm<%=id2%>" method="post">
									<textarea name="myReplyTextareaContent" id="get<%=id2%>" class="myTextarea"></textarea>
									<span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onClick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" id="isSecret<%=id2%>" name="isSecret<%=id2%>" value="0"/></span>
									<input type="hidden" id="myActionId<%=id2%>" name="myActionId<%=id2%>" value=""/>
									<input type="hidden" id="discussId<%=id2%>" name="discussId<%=id2%>" value="<%=id2 %>"/>
									<input type="hidden" id="flow_id<%=id2%>" name="flow_id<%=id2%>" value="<%=wad2.getString("flow_id") %>"/>
									<input type="hidden" id="action_id<%=id2%>" name="action_id<%=id2%>" value="<%=wad2.getString("action_id") %>"/>
									<input type="hidden" id="user_name<%=id2%>" name="user_name<%=id2%>" value="<%=myUserName%>"/>
									<input type="hidden" id="userRealName<%=id2%>" name="userRealName<%=id2%>" value="<%=myRealName%>"/>
									<input type="hidden" id="reply_name<%=id2%>" name="reply_name<%=id2%>" value="<%=wad2.getString("user_name")%>"/>
									<input type="hidden" id="parent_id<%=id2%>" name="parent_id<%=id2%>" value="<%=id%>"/>
									<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onClick="submitPostscript('<%=id2%>','<%=id%>')"/>
								</form>
					 		 </div>
					 	  </td>
					 </tr>
					<%
					  	}
					%>
					<tr id="trline<%=id%>" >
                     	<td colspan="3">
                     	     <hr class="hrLine"/>
                        </td>
                    </tr>
	     </table>
                  <%
                  n++;
				  }
				  %>
                  </div></div></div><br /><br /><br /><br /><br /></td>
              </tr>
            </table></td>
        </tr>
      </table></td>
  </tr>
</table>
	<form name="formWord" target="_blank" action="visual/exportWord.do" method="post">
		<input name="formViewId" value="<%=formViewId%>" type="hidden"/>
		<input name="code" value="<%=lf.getFormCode()%>" type="hidden"/>
		<input name="ids" value="<%=fdao.getId()%>" type="hidden"/>
	</form>
</div>
</div>
</body>
<script>
var isAll = false;
function getPrintContent() {
	if (!isAll) {
		if (o("attDiv")) {
			return formDiv.innerHTML + $("#attDiv").html();
		}
		else {
			return formDiv.innerHTML;
		}
	}
	else {
		var str = "<div style='text-align:center;margin-top:10px'>" + $('#tableTitle').html() + "</div><div style='text-align:center;margin-top:10px'>" + $('#divTitle').html() + "</div>" + formAllDiv.innerHTML;
		return str;
	}
}

function showFormReportAll() {
	isAll = true;
	var preWin=window.open('print_preview.jsp?print=true','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
}

function showFormReport() {
	isAll = false;
	// 下列方式有时会导致IE崩溃
	var preWin=window.open('print_preview.jsp?print=true','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
}

function exportToWord() {
	formWord.submit();
}

var curMyActionId;
function changeExpireDate(myActionId, expireDate) {
	curMyActionId = myActionId;
	showModalDialog("flow/flow_modify_expire_date.jsp?dt=" + expireDate, window.self ,"dialogWidth:480px;dialogHeight:320px;status:no;help:no;");
}

function setExpireDate(expireDate) {
	window.location.href='<%=request.getContextPath()%>/flow_modify.jsp?op=setExpireDate&myActionId=' + curMyActionId + '&flowId=<%=flow_id%>&expireDate=' + expireDate;
}

function linkProject() {
	openWin("<%=request.getContextPath()%>/project/project_list_sel.jsp?action=linkProject", 800, 600);
}

function unlinkProject() {
	jConfirm('<lt:Label res="res.flow.Flow" key="cancelAssociation"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function(r) {
		if (!r)
			return false;
		$.ajax({
			type: "post",
			url: "flow/unlinkProject.do",
			data: {
				flowId: <%=wf.getId()%>
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#mainTable').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="1") {
					jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
					o("projectName").innerHTML = "";
				}
				else {
					jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
				}
			},
			complete: function(XMLHttpRequest, status){
				$('#mainTable').hideLoading();
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});
	});
}

function doLinkProject(prjId, prjName) {
	$.ajax({
		type: "post",
		url: "flow/linkProject.do",
		data: {
			projectId: prjId,
			flowId: <%=wf.getId()%>
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#mainTable').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="1") {
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
				o("projectName").innerHTML = "<lt:Label res='res.flow.Flow' key='project'/>：<a href=\"javascript:;\" onclick=\"addTab('" + prjName + "', 'project/project_show.jsp?projectId=" + prjId + "&formCode=project')\">" + prjName + "</a>&nbsp;&nbsp;<a title=\"取消关联\" href=\"javascript:;\" onclick=\"unlinkProject()\" style='font-size:16px; font-color:red'>×</a>";
			}
			else {
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
		},
		complete: function(XMLHttpRequest, status){
			$('#mainTable').hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});
}


function addMyReply(id) {
	if($("#myReplyTextarea"+id).is(":hidden")){
		$("#myReplyTextarea"+id).show();
		$("#get"+id).focus();
		autoTextarea($("#get"+id).get(0));
	}else{
		$("#myReplyTextarea"+id).hide();
	}
}

/**
 * 文本框根据输入内容自适应高度
 * @param                {HTMLElement}        输入框元素
 * @param                {Number}                设置光标与输入框保持的距离(默认0)
 * @param                {Number}                设置最大高度(可选)
 */
var autoTextarea = function (elem, extra, maxHeight) {
        extra = extra || 0;
        var isFirefox = !!document.getBoxObjectFor || 'mozInnerScreenX' in window,
        isOpera = !!window.opera && !!window.opera.toString().indexOf('Opera'),
                addEvent = function (type, callback) {
                        elem.addEventListener ?
                                elem.addEventListener(type, callback, false) :
                                elem.attachEvent('on' + type, callback);
                },
                getStyle = elem.currentStyle ? function (name) {
                        var val = elem.currentStyle[name];

                        if (name === 'height' && val.search(/px/i) !== 1) {
                                var rect = elem.getBoundingClientRect();
                                return rect.bottom - rect.top -
                                        parseFloat(getStyle('paddingTop')) -
                                        parseFloat(getStyle('paddingBottom')) + 'px';
                        };

                        return val;
                } : function (name) {
                                return getComputedStyle(elem, null)[name];
                },
                minHeight = parseFloat(getStyle('height'));

        elem.style.resize = 'none';

        var change = function () {
                var scrollTop, height,
                        padding = 0,
                        style = elem.style;

                if (elem._length === elem.value.length) return;
                elem._length = elem.value.length;

                if (!isFirefox && !isOpera) {
                        padding = parseInt(getStyle('paddingTop')) + parseInt(getStyle('paddingBottom'));
                };
                //scrollTop = document.body.scrollTop || document.documentElement.scrollTop;

                elem.style.height = minHeight + 'px';
                if (elem.scrollHeight > minHeight) {
                        if (maxHeight && elem.scrollHeight > maxHeight) {
                                height = maxHeight - padding;
                                style.overflowY = 'auto';

                        } else {
                                height = elem.scrollHeight - padding;
                                style.overflowY = 'hidden';

                        };
                        style.height = height + extra + 'px';
                        //scrollTop += parseInt(style.height) - elem.currHeight;
                        //document.body.scrollTop = scrollTop;
                        //document.documentElement.scrollTop = scrollTop;
                        elem.currHeight = parseInt(style.height);
                };
        };

        addEvent('propertychange', change);
        addEvent('input', change);
        addEvent('focus', change);
        change();

};

function submitPostscript(textareaId, rootId){
	var textareaContent = $("#get"+textareaId).val();//“评论”文本框的内容
	var flow_id = $("#flow_id"+textareaId).val();
	var action_id = $("#action_id"+textareaId).val();
	var myActionId = $("#myActionId"+textareaId).val();
	var discussId = $("#discussId"+textareaId).val();
	var userRealName = $("#userRealName"+textareaId).val();
	var user_name = $("#user_name"+textareaId).val();
	var reply_name = $("#reply_name"+textareaId).val();
	var flow_name = $("#flow_name"+textareaId).val();
	var parent_id = $("#parent_id"+textareaId).val();
	var is_secret = $("#isSecret"+textareaId).val();

	if(textareaContent == ""){
		alert("<lt:Label res='res.flow.Flow' key='reviewContent'/>");
	}else{
		$.ajax({
			type: "post",
			url: "flow/addReplyDispose.do",
			data : {
				content: textareaContent,
				flow_id: flow_id,
				action_id: action_id,
				myActionId: myActionId,
				discussId: discussId,
				userRealName: userRealName,
				user_name: user_name,
				reply_name: reply_name,
				flow_name: flow_name,
				parent_id: parent_id,
				cwsProgress: $('#cwsProgress').val(),
				isSecret: is_secret
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#bodyBox').showLoading();
				//$('#loading-indicator-bodyBox-overlay').height($('#flowbodybg').height());
				//$('#loading-indicator-bodyBox').css({'bottom':'350px','top':''});
			},
			complete: function(XMLHttpRequest, status){
				$('#bodyBox').hideLoading();
				//$("#get"+textareaId).height("48px");
				//$('#bodyBox').hideLoading();
			},
			success: function(data, status){
				var re = $.parseJSON(data);
				if (re.ret=="1") {
					if(rootId==0){
						$("#tablehead").append(re.result);
                        <%if (fd.isProgress()) {%>
						$("#tablehead").find("td:eq(1)").prepend("<div>进度：" + $('#cwsProgress').val() + "%</div>");
						<%}%>
						$("#get"+textareaId).val("");
						$("#myReplyTextarea"+textareaId).hide();
						$("#divShow").show();
					}else{
						$("#trline" + rootId).before(re.result);
						$("#get"+textareaId).val("");
						$("#myReplyTextarea"+textareaId).hide();
						$("#divShow").show();
					}
				}
			},
			error: function(){
				jAlert('<lt:Label res="res.flow.Flow" key="replyWrong"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
		});

	}
}

function show() {
	$("#notShowDiv").show();
	$("#showDiv").hide();
	$("#divShow").show();
}

function notshow() {
	$("#notShowDiv").hide();
	$("#showDiv").show();
	$("#divShow").hide();
}

function recall(flow_id, action_id){
	jConfirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"post",
				url:"flow/recall.do",
				data: {
					"flow_id": flow_id,
					"action_id": action_id
				},
		 		success:function(data,status){
		 			data = $.parseJSON(data);
		 			if (data.ret=="1") {
						jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', function() {
							window.location.href = 'flow_dispose.jsp?myActionId=' + action_id;
						});
					}
		 			else {
		 				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
					}
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}

function recallFree(flow_id,action_id){
	jConfirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"post",
				url:"flow/recall.do",
				data:{"action":"recall","flow_id":flow_id,"action_id":action_id},
		 		success:function(data,status){
		 			data = $.parseJSON(data);
		 			location.href = 'flow_modify.jsp?flowId=' + flow_id + "&op=recall&msg=" + data.msg;
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			//alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}

function chooseHideComment(obj){
	var myImg = $(obj).children("img");
	var myInput = $(obj).children("input");
	if(myImg.attr("src").indexOf("checkbox_sel") != -1){//现在是“显示”状态
		myImg.attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png");
		myInput.val("0");
	}else{//现在是“隐藏”状态
		myImg.attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_sel.png");
		myInput.val("1");
	}
}

function delAnnex(annexId) {
	jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>','提示',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type: "post",
				url: "flow/delAnnex.do",
				data : {
					annexId: annexId
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					$('#bodyBox').showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					jAlert(data.msg,"提示");
				},
				complete: function(XMLHttpRequest, status){
					$('#bodyBox').hideLoading();
					$('#trReply' + annexId).hide();
					$('#trline' + annexId).hide();
					$("tr[pId='" + annexId + "']").hide();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					jAlert(XMLHttpRequest.responseText,"提示");
				}
			});
		}
	});
}


function delAtt(docId, attId) {
	jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){return;}
		else{
			$.getJSON('flow_dispose_ajax_att.jsp',
					{
						"op":"delAttach",
						"flowId":<%=flow_id%>,
						"doc_id":docId,
						"attach_id":attId
					},
					function(data) {
						if (data.re=="true") {
							jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
							$('#trAtt' + attId).remove();
						}
						else {
							jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
						}
						
					});
		}
	})
}

<%
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(lf.getFormCode());
	if (msd.getPageStyle()==ConstUtil.PAGE_STYLE_LIGHT) {
%>
// 不能放在$(function() 中，原来的tabStyle_8风格会闪现
$('#flowForm').find('.tabStyle_8').addClass('layui-table').removeClass('tabStyle_8');
$('.tabStyle_1 ').addClass('layui-table').removeClass('tabStyle_1').find('.tabStyle_1_title').removeClass('tabStyle_1_title');
<%
    }
	else {
%>
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
<%
	}
%>
</script>
</html>