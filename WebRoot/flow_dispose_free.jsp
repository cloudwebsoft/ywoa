<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.task.TaskDb"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="renderer" content="ie-stand" />
<title>处理流程</title>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/Toolbar_flow.css" />

<style>
.userRealName {
display:-moz-inline-box;
display:inline-block;
width: 80px;
}
.ui-icon{width:30px}
</style>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>

<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />            

<script type="text/javascript" src="js/activebar2.js"></script>

<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />

<script src="js/jquery.form.js"></script>
<script src="js/jquery.bgiframe.js"></script>

<script src="inc/livevalidation_standalone.js"></script>

<script src="js/tabpanel/Toolbar.js" type="text/javascript"></script>

<script type="text/javascript" src="js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="js/goToTop/goToTop.css" />

<link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>

<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
<script src="js/datepicker/jquery.datetimepicker.js"></script>

<script src="js/fixheadertable/jquery.fixheadertable.js"></script>
<link rel="stylesheet" media="screen" href="js/fixheadertable/base.css" /> 

<script src="inc/map.js"></script>
<script src="inc/upload.js"></script>
<script src="inc/flow_dispose.jsp"></script>
<script src="inc/flow_js.jsp"></script>
<script tyle="text/javascript" language="javascript" src="spwhitepad/createShapes.js"></script>
<script src="inc/ajax_getpage.jsp"></script>

<script src="js/jquery.toaster.flow.js"></script>

<script type="text/javascript" src="js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
<link type="text/css" rel="stylesheet" href="js/appendGrid/jquery.appendGrid-1.5.1.css" />

<link href="flowstyle.css" rel="stylesheet" type="text/css" />

<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String myname = privilege.getUser( request );

UserMgr um = new UserMgr();
UserDb myUser = um.getUserDb(myname);
String myRealName = myUser.getRealName();

long myActionId = ParamUtil.getLong(request, "myActionId");
MyActionDb mad = new MyActionDb();
mad = mad.getMyActionDb((long)myActionId);

if (!mad.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "由于上一节点重新激活或撤回，待办记录已不存在！"));
	return;
}
else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_TRANSFER) {
	out.print(SkinUtil.makeInfo(request, "流程已指派，不需要再处理！", true));
	return;
}
/*
// 有可能是重激活
else if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_CHECKED) {
	out.print(SkinUtil.makeInfo(request, "流程已处理！", true));
	return;
}
*/

if (!mad.getUserName().equals(myname) && !mad.getProxyUserName().equals(myname)) {
	// 权限检查
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

WorkflowActionDb wa = new WorkflowActionDb();
int actionId = (int)mad.getActionId();
wa = wa.getWorkflowActionDb(actionId);
if ( wa==null ) {
	out.print(SkinUtil.makeErrMsg(request, "流程中的相应动作不存在！"));
	return;
}

if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_TRANSFER) {
	out.print(SkinUtil.makeInfo(request, "流程已指派，不需要再处理！", true));
	return;
}

int flowId = wa.getFlowId();

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + flowId);
request.setAttribute("pageType", "flow");
request.setAttribute("workflowActionId", "" + wa.getId());
// 置macro_js_ntko.jsp中需要用到的myActionId
request.setAttribute("myActionId", "" + myActionId);

WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);

if (wf.getStatus()==WorkflowDb.STATUS_DELETED) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","flowDeleted");
	out.print(SkinUtil.makeErrMsg(request, str));
	return;
}

Leaf lf = new Leaf();
lf = lf.getLeaf(wf.getTypeCode());

FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());

// 置NestSheetCtl需要用到的formCode
request.setAttribute("formCode", lf.getFormCode());
%>
<script src="flow/form_js/form_js_<%=lf.getFormCode()%>.js"></script>
<%

// 锁定流程
wfm.lock(wf, myname);

// 如果是未读状态
if (!mad.isReaded()) {
	mad.setReaded(true);
	mad.setReadDate(new java.util.Date());
	mad.save();
}

WorkflowPredefineDb wfp = new WorkflowPredefineDb();
wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

boolean isFlowManager = false;
if (wfp.canUserDo(myUser, wf.getTypeCode(), "del")) {
	isFlowManager = true;
}

if (!wfp.isReactive()) {
	if (wa.getStatus()!=wa.STATE_DOING && wa.getStatus()!=wa.STATE_RETURN) {
		// mad.del();
        mad.setCheckStatus(MyActionDb.CHECK_STATUS_CHECKED);
        mad.setCheckDate(new java.util.Date());
        mad.save();		
		out.print(SkinUtil.makeErrMsg(request, "流程中动作节点不处在正处理或被打回状态，可能已经被处理过了！"));
		return;
	}
}

String op = ParamUtil.get(request, "op");

if (op.equals("discard")) {
	boolean re = false;
	try {
		re = wfm.discard(request, flowId);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow/flow_list.jsp?displayMode=1"));
	return;
}

if (op.equals("delAttach")) {
	int doc_id = ParamUtil.getInt(request, "doc_id");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	int page_num = ParamUtil.getInt(request, "page_num");
	Document doc = new Document();
	doc = doc.getDocument(doc_id);
	DocContent dc = doc.getDocContent(page_num);
	boolean re = dc.delAttachment(attach_id);
	if (re) {
		out.print(StrUtil.Alert_Redirect("删除成功！", "flow_dispose_free.jsp?myActionId=" + myActionId));
	}
	else
		out.print(StrUtil.Alert_Back("删除失败！"));
	return;
}

String action = ParamUtil.get(request, "action");

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String flowExpireUnit = cfg.get("flowExpireUnit");
boolean isHour = !flowExpireUnit.equals("day");	
if (flowExpireUnit.equals("day"))
	flowExpireUnit = "天";
else
	flowExpireUnit = "小时";	
%>
<script>
function OfficeOperate() {
	// alert(redmoonoffice.ReturnMessage);
}

function setradio(myitem,v)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
				 	radioboxs[i].checked = true;
              }
          }
     }
}

var action = "<%=action%>";
function SubmitResult() {
	<%
	if (mad.getActionStatus()!=WorkflowActionDb.STATE_PLUS) {
	%>
	if (flowForm.nextActionUsers.value=="") {
		if (!confirm("您还没有选择下一步处理流程的用户，您确定办理完毕了么?"))
			return false;
	}
	<%}%>
	
	flowForm.op.value='finish';
	
	var re = true;
	try {
		// 在嵌套表格页面中，定义了onsubmit方法
		re = flowForm.onsubmit();
	}
	catch (e) {}
	if (re) {
		// 去除掉未打勾的nextUsersDiv数据
		$("div[name='nextUsersDiv']").each(function() {
		   var objDiv = $(this);
		   var objs = $(this).find('input');
		   objs.each(function () {
			   var obj = $(this);
			   if (obj.attr('name')=='nextUsers') {
				if (obj.attr("checked")!="checked") {
					objDiv.html("");
					objDiv.hide();
				}
			   }
		   });
		});
		
		/*
		flowForm.submit();
		toolbar.setDisabled(0, true);		
		toolbar.setDisabled(1, true);		
		toolbar.setDisabled(2, true);		
		toolbar.setDisabled(3, true);	
		toolbar.setDisabled(4, true);	
		toolbar.setDisabled(5, true);	
		toolbar.setDisabled(6, true);	
		toolbar.setDisabled(7, true);	
		toolbar.setDisabled(8, true);	
		toolbar.setDisabled(9, true);	
		toolbar.setDisabled(10, true);	
		$('#flowForm').showLoading();
		*/
		$('#bodyBox').showLoading();
		$('#flowForm').submit();
	}
}

// 编辑文件
function editdoc(doc_id, file_id) {
	redmoonoffice.AddField("doc_id", doc_id);
	redmoonoffice.AddField("file_id", file_id);
	redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/flow_document_get.jsp?doc_id=" + doc_id + "&file_id=" + file_id);
}

//审批文件，并作痕迹保留
function ReviseByUserColor(user, colorindex, doc_id, file_id) {
	if (o("redmoonoffice")) {
		redmoonoffice.AddField("doc_id", doc_id);
		redmoonoffice.AddField("file_id", file_id);
	}
	
	<%if (wa.isStart==0) {%>
		<%if (cfg.get("isUseNTKO").equals("true")) {%>
		var isRevise=1;
		confirmLock(file_id, user, doc_id, isRevise);
		//openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=1", 800, 600);
	<%}else{%>
		redmoonoffice.ReviseByUserColor("<%=Global.getFullRootPath(request)%>/flow_document_get.jsp?doc_id=" + doc_id + "&file_id=" + file_id, user, colorindex);
		<%}%>
	<%}else{%>
		<%if (cfg.get("isUseNTKO").equals("true")) {%>
		var isRevise=0;
		confirmLock(file_id, user, doc_id, isRevise);
		//openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=0", 800, 600);
		<%}else{%>
		editdoc(doc_id, file_id);
		<%}%>
	<%}%>
}

function confirmLock(file_id, userName, doc_id, isRevise) {
	openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=" + isRevise, 1024, 768);
/*
	var islocked = false;
	var content = "";

	$.ajax({
		async: false,
		type: "post",
		url: "flow_dispose_do.jsp",
		data : {
			myop: "getLocker",
			fileId: file_id,
			userName: userName
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			
		},
		success: function(data, status){
			data = $.parseJSON(data);
			content = data.content;
			islocked = data.islocked;
			console.log(data);
		},
		complete: function(XMLHttpRequest, status){
			
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText, '提示');
		}
	});		
	
	jConfirm(content, '提示', function(r) {
		if (r) {
				$.ajax({
					async: false,
					type: "post",
					url: "flow_dispose_do.jsp",
					data : {
						myop: "confirmLock",
						fileId: file_id,
						userName: userName
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){

					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, '提示');
						}
						else {
							openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=" + isRevise, 1024, 768);
						}
					},
					complete: function (XMLHttpRequest, status) {

					},
					error: function(XMLHttpRequest, textStatus){
						jAlert(XMLHttpRequest.responseText, '提示');
					}
				});
		} else {
			if(!islocked){
				openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=" + isRevise, 1024, 768);
			}
		}
	});*/
}


function uploaddoc(doc_id, file_id) {
	redmoonoffice.Clear();
	redmoonoffice.AddField("doc_id", doc_id);
	redmoonoffice.AddField("file_id", file_id);
	redmoonoffice.UploadDoc();
	// alert(redmoonoffice.ReturnMessage);
}

function openWin(url,width,height){
if (width>window.screen.width)
	width = window.screen.width;
if (height>window.screen.height)
	height = window.screen.height;
var l = (window.screen.width - width) / 2; 
var t = (window.screen.height - height) / 2;
var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=" + t + ",left=" + l + ",width="+width+",height="+height);
}

function saveArchive(flowId, actionId) {
	openWin("flow_doc_archive_save.jsp?op=saveFromFlow&flowId=" + flowId + "&actionId=" + actionId, 800, 600);
}

var curInternalName, toInternalname

function checkOfficeEditInstalled() {
	<%if (cfg.get("isUseNTKO").equals("true")) {%>
		return true;
	<%}%>	
	var bCtlLoaded = false;
	try	{
		if (typeof(redmoonoffice.AddField)=="undefined")
			bCtlLoaded = false;
		if (typeof(redmoonoffice.AddField)=="unknown") {
			bCtlLoaded = true;
		}
	}
	catch (ex) {
	}
	if (!bCtlLoaded) {
		$('<div></div>').html('您还没有安装客户端控件，请点击确定此处下载安装！').activebar({
			'icon': 'images/alert.gif',
			'highlight': '#FBFBB3',
			'url': 'activex/oa_client.EXE',
			'button': 'images/bar_close.gif'
		});
	}	
}

function window_onload() {
	switchProcessList();
	
	checkOfficeEditInstalled();
}

function linkProject() {
	openWin("<%=request.getContextPath()%>/project/project_list_sel.jsp?action=linkProject", 800, 600);
}

function unlinkProject() {
	jConfirm('您确定要取消关联么？', '提示', function(r) {	
		if (!r)
			return false;
		$.ajax({
			type: "post",
			url: "flow_dispose_do.jsp",
			data: {
				myop: "unlinkProject",
				flowId: <%=wf.getId()%>
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				// $('#bodyBox').showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				if (data.ret=="1") {
					jAlert(data.msg, "提示");
					o("projectName").innerHTML = "";
				}
				else {
					jAlert(data.msg, "提示");
				}
			},
			complete: function(XMLHttpRequest, status){
				// $('#bodyBox').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
	});
}

function doLinkProject(prjId, prjName) {
	$.ajax({
		type: "post",
		url: "flow_dispose_do.jsp",
		data: {
			myop: "linkProject",
			projectId: prjId,
			myActionId: "<%=myActionId%>",
			flowId: <%=wf.getId()%>
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			// $('#bodyBox').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="1") {
				jAlert(data.msg, "提示");
				o("projectName").innerHTML = "项目：<a href=\"javascript:;\" onclick=\"addTab('" + prjName + "', 'project/project_show.jsp?projectId=" + prjId + "&formCode=project')\">" + prjName + "</a>&nbsp;&nbsp;<a title=\"取消关联\" href=\"javascript:;\" onclick=\"unlinkProject()\" style='font-size:16px; font-color:red'>×</a>";				
			}
			else {
				jAlert(data.msg, "提示");
			}
		},
		complete: function(XMLHttpRequest, status){
			// $('#bodyBox').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}

function setPerson(deptCode, deptName, user, userRealName) {
	flowForm.nextActionUsers.value = user;
	flowForm.userRealName.value = userRealName;
}

var isOpenWinUsersForPlus = false;
function openWinUsers(paramIsOpenWinUsersForPlus) {
	// showModalDialog('user_multi_sel.jsp?from=flow',window.self,'dialogWidth:800px;dialogHeight:650px;status:no;help:no;')
	openWin('user_multi_sel.jsp?from=flow', 800, 600);
	if (paramIsOpenWinUsersForPlus!=null) {
		isOpenWinUsersForPlus = paramIsOpenWinUsersForPlus;
	}
	else {
		isOpenWinUsersForPlus = false;
	}
}

function getSelUserNames() {
	if (!isOpenWinUsersForPlus) {
		return flowForm.nextActionUsers.value;
	}
	else {
		return o("plusUsers").value;		
	}
}

function getSelUserRealNames() {
	if (!isOpenWinUsersForPlus) {
		return flowForm.userRealNames.value;
	}
	else {
		return o("plusUserRealNames").value;		
	}
}

function openWinUserGroup() {
	openWin("user_usergroup_multi_sel.jsp", 520, 400);
}
var count = 0;
function openWinUserRole() {
	openWin("user_role_multi_sel.jsp", 800, 600);
}
var selUsers = "";
var isAddPlusing = false; // 是否正在进行加签操作
function setUsers(users, userRealNames, isSeries) {
	if (isAddPlusing) {
		o("plusUsers").value = users;
		o("plusUserRealNames").value = userRealNames;
		return;
	}
	if (users=="") {
		nextUsersDivs.innerHTML = "";
		flowForm.nextActionUsers.value = "";
		flowForm.userRealNames.value = "";
		return;
	}
	
	var uNameAry = users.split(",");
	var uRealNameAry = userRealNames.split(",");

	users = "";
	userRealNames = "";
	
	// 删除上次被选择，而本次未被选择的用户
	var userary = getSelUserNames().split(",");
	var len = userary.length;
	for (var k=0; k<len; k++) {
		if (userary[k]=="")
			continue;
		var isFound = false;
		for (var i=0; i<uNameAry.length; i++) {
			if (userary[k]==uNameAry[i]) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			// document.getElementById("nextUsersDiv" + userary[k]).outerHTML = "";
			$("#"+"nextUsersDiv" + userary[k]).prop("outerHTML", "")
		}
	}
	/*
	for (var i=0; i<uNameAry.length; i++) {
		var len = userary.length;
		var nextUsersDivs = document.getElementsByName("nextUsersDiv");
		for (i=0; i<nextUsersDivs.length; i++) {
			nextUsersDivs[i].outerHTML = "";
		}
	}
	*/

	for (var i=0; i<uNameAry.length; i++) {
		// 过滤掉已被选择的用户
		var len = userary.length;
		var isFound = false;
		for (var k=0; k<len; k++) {
			if (userary[k]==uNameAry[i]) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			
			// 用户名前面的checkbox不显示，因为后面有删除键，保留的意义似乎不大
			// toMes如果不选，在服务器端不太好匹配，且前台操作复杂，易引起混淆，所以在此也去掉
			if(isSeries)
				nextActionUserDiv.innerHTML += "<div id='nextUsersDiv" + uNameAry[i] + "' name='nextUsersDiv'>顺序：<input name='orders' size='3' value='"+(i+1)+"' style='width:20px'>&nbsp;<input name='nextUsers' checked type='checkbox' value='" + uNameAry[i] + "' style='display:none'><span class='userRealName'>" + uRealNameAry[i] + "</span>&nbsp;&nbsp;到期时间：<input name='expireHours' size=2 value=0>小时<span style='display:none'><input name='toMes' type='checkbox' value=1>处理完交办给我</span>&nbsp;&nbsp;&nbsp;&nbsp;<a href='javascript:;' onclick='up(this)' title='上移'>↑</a>&nbsp;&nbsp;<a href='javascript:;' onclick='down(this)' title='下移'>↓</a>&nbsp;&nbsp;<a href='javascript:;' title='删除' onclick='$(this).parent().remove()' style='font-size:16px; font-color:red'>×</</div>";
			else
				nextActionUserDiv.innerHTML += "<div id='nextUsersDiv" + uNameAry[i] + "' name='nextUsersDiv'>顺序：<input name='orders' size='3' value='1' style='width:20px'>&nbsp;<input name='nextUsers' checked type='checkbox' value='" + uNameAry[i] + "' style='display:none'><span class='userRealName'>" + uRealNameAry[i] + "</span>&nbsp;&nbsp;到期时间：<input name='expireHours' size=2 value=0>小时<span style='display:none'><input name='toMes' type='checkbox' value=1>处理完交办给我</span>&nbsp;&nbsp;&nbsp;&nbsp;<a href='javascript:;' onclick='up(this)' title='上移'>↑</a>&nbsp;&nbsp;<a href='javascript:;' onclick='down(this)' title='下移'>↓</a>&nbsp;&nbsp;<a href='javascript:;' title='删除' onclick='$(this).parent().remove()' style='font-size:16px; font-color:red'>×</</div>";
			//count ++;
			selUsers += uRealNameAry[i] + ",";
		}
		//alert(selUsers)
		if (users=="") {
			users = uNameAry[i];
			userRealNames = uRealNameAry[i];
		}
		else {
			users += "," + uNameAry[i];
			userRealNames += "," + uRealNameAry[i];
		}
	}
	
	flowForm.nextActionUsers.value = users;
	flowForm.userRealNames.value = userRealNames;
	flowForm.selUser.value = selUsers.substring(0,selUsers.length - 1);
}

function exchangePos(elem1, elem2){
	if(elem1.length === 0 && elem2.length === 0){
		return;
	}
	var next = elem2.next();
	var parent = elem2.parent();
	elem1.after(elem2);
	if(next.length === 0){
		parent.append(elem1);
	}else{
		next.before(elem1);
	}
}

function up(obj) {
	var p = $(obj).parent();
	var pp = p.parent();
	pp.children().each(function(k) {
		if ($(this)[0]==p[0]) {
			if (k==0)
				return;
			exchangePos(pp.children().eq(k-1), pp.children().eq(k));
			return;
		}
	});
}

function down(obj) {
	var p = $(obj).parent();
	var pp = p.parent();
	pp.children().each(function(k) {
		if ($(this)[0]==p[0]) {
			exchangePos(pp.children().eq(k), pp.children().eq(k+1));
			return;
		}
	});
}

function getValidUserRole() {
<%
	boolean isRoleMemberOfFlow = false;
	String rolesOfFlow = "";
	String[][] rolePrivs = wfp.getRolePrivsOfFree();
	int privLen = rolePrivs.length;
	for (int i=0; i<privLen; i++) {
		if (rolePrivs[i][0].equals(RoleDb.CODE_MEMBER))
			isRoleMemberOfFlow = true;
		if (rolesOfFlow.equals("")) {
			rolesOfFlow = rolePrivs[i][0];
		}
		else {
			rolesOfFlow += "," + rolePrivs[i][0];
		}
	}
%>
	return "<%=rolesOfFlow%>";
}

$(function (){
	$(window).goToTop({
		showHeight : 1,//设置滚动高度时显示
		speed : 500 //返回顶部的速度以毫秒为单位
	});
	setActiveTabTitle("<%=wf.getTitle().replaceAll("\r\n", "").trim().length() >= 8 ? wf.getTitle().replaceAll("\r\n", "").trim().substring(0, 8) : wf.getTitle().replaceAll("\r\n", "").trim()%>");
	SetNewDate();
});
</script>
</head>
<body onLoad="window_onload()">
<div id="bodyBox">
<div id="toolbar" style="height:25px; clear:both"></div>

<%@ include file="inc/tip_phrase.jsp"%>

<form id="flowForm" name="flowForm" action="flow_dispose_free_do.jsp" method="post" enctype="multipart/form-data">
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="main" style="background-color:#dfe8f2; border-bottom:1px solid #bfcad6;  padding-top:5px">
  <tr>
    <td width="56%" align="left" height="36px">
    <div style="margin-top: 10px">
	<%if (mad.getActionStatus()==WorkflowActionDb.STATE_PLUS) {%>
    	&nbsp;&nbsp;加签处理
    <%}else{%>
        &nbsp;<img src="images/man.gif" width="16" height="16" align="absmiddle" />&nbsp;提交给&nbsp;→&nbsp;
		<%if (true || isRoleMemberOfFlow) {%>
        <input type="hidden" name="selUser" />
        <a href="javascript:;" onClick="openWinUsers()">选择用户</a>&nbsp;&nbsp;
        <!--
        <a href="javascript:;" onClick="openWinUserGroup()">按用户组</a>&nbsp;&nbsp;
        <a href="javascript:;" onclick="openWinPersonUserGroup()">我的用户组</a>&nbsp;&nbsp;
        -->
        <%}%>
        <!--
        <a href="javascript:;" onClick="openWinUserRole()">按角色</a>
        -->
        <span title="如果打勾，则所选人员顺序处理，如果不打勾，则所选人员同时处理" style="display:none"><input type="checkbox" name="isOrder" checked value="1" />所选人员顺序处理</span>
    <%}%>
    </div>
    </td>
    <td width="44%" align="right" style="font-size:12px; font-weight:normal">
    <span id="projectName">
    <%if (wf.getProjectId()!=-1) {
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
        FormDb prjFd = new FormDb();
        prjFd = prjFd.getFormDb("project");
        fdao = fdao.getFormDAO((int)wf.getProjectId(), prjFd);
        %>
        项目：<a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("name")%>', 'project/project_show.jsp?projectId=<%=wf.getProjectId()%>&formCode=project')"><%=fdao.getFieldValue("name")%></a>
        &nbsp;&nbsp;<a title="取消关联" href="javascript:;" onclick="unlinkProject()" style='font-size:16px; font-color:red'>×</a>
        <%
    }
    %>
    </span>	
	<%
      if (mad.getExpireDate()!=null) {%>
      <img src="images/clock.png" align="absmiddle" />&nbsp;&nbsp;到期时间：<%=DateUtil.format(mad.getExpireDate(), "yyyy-MM-dd HH:mm")%>
      <%}%>
	  <%
      if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
          boolean flowAutoSMSRemind = cfg.getBooleanProperty("flowAutoSMSRemind");
          String chk = "checked=\"checked\"";
          if (!flowAutoSMSRemind)
              chk = "";
      %>
      <input id="isToMobile" name="isToMobile" value="true" type="checkbox" <%=chk%> />
      短信
      <%}%>
      <%
      boolean flowAutoMsgRemind = cfg.getBooleanProperty("flowAutoMsgRemind");
      String chk = "checked=\"checked\"";
      if (!flowAutoMsgRemind)
          chk = "";
      %>
      <input id="isUseMsg" name="isUseMsg" value="true" type="checkbox" <%=chk%> />
      消息提醒&nbsp;&nbsp; <span id="spanLoad2"></span></td>
  </tr>
  <tr>
    <td colspan="2" align="left">
<table width="100%" border="0" align="center" cellpadding="2" cellspacing="0" style="display:<%=mad.getActionStatus()==WorkflowActionDb.STATE_PLUS?"none":""%>">
                  <tr>
                    <td align="left" valign="top">
  <div id="nextActionUserDiv">
<%					
Vector vto = wa.getLinkToActions();
Iterator irto = vto.iterator();
String userRealNames="", nextActionUsers="";
int count = 0;
while (irto.hasNext()) {
	WorkflowActionDb toAction = (WorkflowActionDb)irto.next();
	if (nextActionUsers.equals("")) {
		nextActionUsers = toAction.getUserName();
		userRealNames = um.getUserDb(toAction.getUserName()).getRealName();
	}
	else {
		nextActionUsers += "," + toAction.getUserName();
		userRealNames += "," + um.getUserDb(toAction.getUserName()).getRealName();
	}
	count ++;
	WorkflowLinkDb wld = new WorkflowLinkDb();
	wld = wld.getWorkflowLinkDbForward(wa, toAction);
%>
  <div id="already<%=count%>"><input type="checkbox" checked disabled /><input name='nextUsers' type='hidden' value='<%=toAction.getUserName()%>'><span class="userRealName">
  <%
  String realNames = "";
  String userNames = toAction.getUserName().replaceAll("，", ",");
  String[] userAry = StrUtil.split(userNames, ",");
  if (userAry!=null) {
	  for (int k=0; k<userAry.length; k++) {
	  	if ("".equals(realNames))
			realNames = um.getUserDb(userAry[k]).getRealName();
		else
			realNames += "，" + um.getUserDb(userAry[k]).getRealName();
	  }
  }
  out.print(realNames);
  %>
  </span>&nbsp;&nbsp;到期时间：<input name='<%=StrUtil.escape(toAction.getUserName()).toUpperCase()%>_expireHour' size=2 value="<%=wld.getExpireHour()%>">小时</div>
  <%}%>
  </div>
  <textarea name="userRealNames" cols="38" rows="3" readOnly wrap="yes" id="userRealNames" style="display:none"><%=userRealNames%></textarea>
  <input type=hidden name="nextActionUsers" value="<%=nextActionUsers%>">
<div id="dlgReturn" style="display:none">
<%
wf = wfm.getWorkflowDb(flowId);
Vector returnv = wa.getLinkReturnActions();
if (returnv.size()>0) {
%>
&nbsp;返回至：
<%}
Iterator returnir = returnv.iterator();
while (returnir.hasNext()) {
	WorkflowActionDb returnwa = (WorkflowActionDb)returnir.next();
	if (returnwa.getStatus()!=WorkflowActionDb.STATE_IGNORED) {
%>
    <input type="checkbox" name="returnId" value="<%=returnwa.getId()%>" checked="checked" />
    <%=um.getUserDb(returnwa.getUserName()).getRealName()%>
  <%}
}
%>
</div>
          <input type="hidden" name="op" value="saveformvalue" />
            <span id="spanLoad"></span>
            </td>
                  </tr>
                  </table>    
    </td>
    </tr>
</table>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td align="center" style="background-color:#f4f4f4;border:1px solid #dcdcdc">
<div style="text-align:left; padding:0px 5px; margin-bottom:10px; color:#888888;margin-top:5px">
<%if (!wf.isStarted()) {%>
            <input name="cwsWorkflowLevel" type="radio" value="<%=WorkflowDb.LEVEL_NORMAL%>" checked /><img src="images/general.png" align="absmiddle" />普通
            <input name="cwsWorkflowLevel" type="radio" value="<%=WorkflowDb.LEVEL_IMPORTANT%>" /><img src="images/important.png" align="absmiddle" />&nbsp;重要
            <input name="cwsWorkflowLevel" type="radio" value="<%=WorkflowDb.LEVEL_URGENT%>" /><img src="images/urgent.png" align="absmiddle" />&nbsp;紧急
            <script>
			setRadioValue("cwsWorkflowLevel", "<%=wf.getLevel()%>");
			</script>
            <%}else{%>
			<%=WorkflowMgr.getLevelImg(request, wf)%>
<%}%>
              标题：
              <%if (wf.isStarted()) {%>
				<%=wf.getTitle()%>&nbsp;&nbsp;
              <%}else{%>
              	<input id="cwsWorkflowTitle" name="cwsWorkflowTitle" value="<%=StrUtil.HtmlEncode(wf.getTitle())%>" style="border:1px solid #cccccc; color:#888888" size="40" />&nbsp;
			  <%}%>
              <!--<%=lf.getName()%>&nbsp;-&nbsp;-->
              
              发起人：
              <%
              String starterName = wf.getUserName();
              String starterRealName = "";
              if (starterName!=null) {
                  UserDb starter = um.getUserDb(wf.getUserName());
                  starterRealName = starter.getRealName();
              }
              out.print(starterRealName);
              %>
	          <%if (wf.isStarted()) {%>
              &nbsp;发起日期： <%=DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm")%>
<%}%>
			  &nbsp;&nbsp;我的备注：
			<input name="cwsWorkflowResult" size="30" style="border:1px solid #cccccc; color:#888888" value="<%=StrUtil.HtmlEncode(mad.getResult())%>" />
      </div>
      
        <table id="processListTab" width="98%" class="tabStyle_1 percent98" style="display:none">
                    <tbody>
                      <tr>
                        <td class="tabStyle_1_title" width="13%" align="center">处理人</td>
                        <td class="tabStyle_1_title" width="12%" align="center">转交人</td>
                        <td class="tabStyle_1_title" width="14%" align="center">开始时间</td>
                        <td class="tabStyle_1_title" width="14%" align="center">处理时间</td>
                        <td class="tabStyle_1_title" width="10%" align="center">用时
                        (<%=flowExpireUnit%>)</td>
                        <td class="tabStyle_1_title" width="9%" align="center">绩效</td>
                        <td class="tabStyle_1_title" width="11%" align="center">处理</td>
                        <td class="tabStyle_1_title" width="17%" align="center">备注</td>
                      </tr>
                      <%
String processListSql = "select id from flow_my_action where flow_id=" + mad.getFlowId() + " order by receive_date asc";
Vector vProcess = mad.list(processListSql);					  
Iterator ir = vProcess.iterator();
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
                      <tr class="highlight">
                        <td><%
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
                          <img src="images/expired.gif" align="absmiddle" alt="超时" />
                      <%}%>
                      <%=userRealName%>
                      </td>
                      <td>
					  <%
					  if (mad.getPrivMyActionId()!=-1) {
					  	MyActionDb mad2 = mad.getMyActionDb(mad.getPrivMyActionId());
						out.print(um.getUserDb(mad2.getUserName()).getRealName());
					  }
					  else
					  	out.print("&nbsp;");
					  %>                        </td>
                        <td align="center"><%=DateUtil.format(mad.getReceiveDate(), "yy-MM-dd HH:mm")%> </td>
                        <td align="center"><%=DateUtil.format(mad.getCheckDate(), "yy-MM-dd HH:mm")%> </td>
                        <td align="center"><%
					  if (isHour) {
						double d = oad.getWorkHourCount(mad.getReceiveDate(), mad.getCheckDate());
						out.print(NumberUtil.round(d, 1));
					  }
					  else {
						int d = oad.getWorkDayCountFromDb(mad.getReceiveDate(), mad.getCheckDate());
						out.print(d);
					  }
					  %>                        </td>
                        <td align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%> </td>
                        <td align="center">
						<%
						if (mad.getChecker().equals(UserDb.SYSTEM)) {
							out.print("超时跳过");
						}else{						
						%>
						<%=mad.getCheckStatusName()%>
						<%}
						if (mad.getCheckStatus()!=0 && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_TRANSFER && mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {
							if (mad.getResultValue() == WorkflowActionDb.RESULT_VALUE_DISAGGREE) {
								out.print("(" +
										WorkflowActionDb.getResultValueDesc(WorkflowActionDb.
										RESULT_VALUE_DISAGGREE) + ")");
							} else {
								if (mad.getResultValue()!=WorkflowActionDb.RESULT_VALUE_RETURN) {
									out.print("(" +
											WorkflowActionDb.getResultValueDesc(WorkflowActionDb.
											RESULT_VALUE_AGGREE) + ")");
								}
							}
						}
						%>
                        </td>
                        <td align="center"><%=mad.getResult()%></td>
                      </tr>
                      <%}%>
                    </tbody>
                  </table> 
                   <span style="cursor:pointer" onclick="switchProcessList()">
                    <img id="imgSwitchProcess" src="images/hide.png"  alt="<lt:Label res='res.flow.Flow' key='displayProcess'/>"  /><span id="spanSwitchProcess" style="font-size: 12px;color:#606060;">&nbsp;&nbsp;<lt:Label res='res.flow.Flow' key='expansion'/></span>
                   </span> 
                  </td>
                  </tr>
                  </table> 
                   
                      
	<%
	int doc_id = wf.getDocId();
	DocumentMgr dm = new DocumentMgr();
	Document doc = dm.getDocument(doc_id);
	UserDb user = new UserDb();
	user = user.getUserDb(privilege.getUser(request));
	%>
      <table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td width="35%" align="center">
          <table class="percent98" style="padding-top:0px; margin-bottom:10px" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td align="left" style="padding-top:5px;">
            <%if (fd.isHasAttachment()) {%>            
            <script>initUpload();</script>
            <%}%>
        	<input type="hidden" name="flowId" value="<%=flowId%>" />
            <input type="hidden" name="actionId" value="<%=actionId%>" />
            <input type="hidden" name="myActionId" value="<%=myActionId%>" />
            <input type="hidden" name="isFlowModified" value="0"/>
			<textarea name="formReportContent" style="display:none"></textarea>
            </td>
            </tr>
        </table>
       
		<div id="attDiv">
		<%
		if (doc!=null) {
			java.util.Vector attachments = doc.getAttachments(1);
			if (attachments.size()>0) {
		%>
        <table class="tabStyle_1 percent98" width="98%" style="margin-top:6px" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td height="31" align="right" class="tabStyle_1_title">&nbsp;</td>
            <td class="tabStyle_1_title">文件名</td>
            <td align="center" class="tabStyle_1_title">创建者</td>
            <td align="center" class="tabStyle_1_title">时间</td>
            <td align="center" class="tabStyle_1_title">大小</td>
            <td align="center" class="tabStyle_1_title">操作</td>
          </tr>
        <%
				  ir = attachments.iterator();
				  String creatorRealName = "";				  
				  while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next();
					UserDb creator = um.getUserDb(am.getCreator());
					if (creator.isLoaded()) {
						creatorRealName = creator.getRealName();
					}					
					%>
          <tr>
            <td width="2%" height="31" align="center"><img src="images/attach.gif" /></td>
            <td width="46%" align="left" onmousemove="spanRename<%=am.getId()%>.style.display=''" onmouseout="spanRename<%=am.getId()%>.style.display='none'">
			
			&nbsp;<span id="spanAttLink<%=am.getId()%>"><a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flowId%>" target="_blank"><span id="spanAttName<%=am.getId()%>"><%=am.getName()%></span></a></span>
			<span id="spanAttNameInput<%=am.getId()%>" style="display:none"><input id="attName<%=am.getId()%>" value="<%=StrUtil.HtmlEncode(am.getName())%>" /><input class="btn" type="button" value="确定" onclick="renameAtt('<%=am.getId()%>')"></span>
			&nbsp;&nbsp;<span id="spanRename<%=am.getId()%>" style="display:none;color:#aaaaaa;cursor:pointer" onclick="changeName('<%=am.getId()%>')">改名</span>
			
			</td>
            <td width="11%" align="center"><%=creatorRealName%></td>
            <td width="13%" align="center"><%=DateUtil.format(am.getCreateDate(), "yyyy-MM-dd HH:mm")%></td>
            <td width="8%" align="center"><%=NumberUtil.round((double)am.getSize()/1024000, 2)%>M</td>
            <td width="20%" align="center">
			<%
			String ext = StrUtil.getFileExt(am.getDiskName());
			if (ext.equals("doc") || ext.equals("xls") || ext.equals("docx") || ext.equals("xlsx") || ext.equals("wps")) {%>
				<%if (wa.isStart==1) {%>
                <a href="javascript:;" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)">编辑</a>
                <%}else{%>
                <a href="javascript:;" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)">审批</a>
                <%}%>
			<%}else{%>
				<a href="javascript:;" onClick="javascript:window.open('flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flowId%>')">查看</a>
			<%}%>
              &nbsp;&nbsp;
              <!--<input type=button" onClick="javascript:uploaddoc(<%=doc_id%>, <%=am.getId()%>)" value="  上 传  ">-->
			<%if (wfp.canUserDo(user, wf, "delAttach")) {%>
			  &nbsp;
              <a href="javascript:;" onClick="if (confirm('您确定要删除吗？')) window.location.href='?op=delAttach&myActionId=<%=myActionId%>&flowId=<%=flowId%>&doc_id=<%=am.getDocId()%>&attach_id=<%=am.getId()%>&page_num=1'">删除</a>
		    <%}%></td>
          </tr>
        <%}%>
		</table>
		<%}
	  }%>
	  </div>
	  
        <%
        // out.print("lf.getQueryId=" + lf.getQueryId() +"<BR>");
		if (lf.getQueryId()!=Leaf.QUERY_NONE) {
			// 判断权限，管理员能看见查询，其它人员根据角色进行判断
			String[] roles = StrUtil.split(lf.getQueryRole(), ",");
			boolean canSeeQuery = false;
			if (!privilege.isUserPrivValid(request, "admin")) {
				if (roles!=null) {
					UserDb userDb = new UserDb();
					userDb = userDb.getUserDb(privilege.getUser(request));
					for (int i=0; i<roles.length; i++) {
						if (userDb.isUserOfRole(roles[i])) {
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
                <div id="formQueryBox"></div>
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
					if (((Boolean)json.get("hide")).booleanValue())
						continue;					
                    String name = (String)json.get("name");
                    if (name.equalsIgnoreCase("cws_op"))
                        continue;
					tableWidth += ((Integer)json.get("width")).intValue();
                    if (colratio.equals(""))
                        colratio = "" + ((Integer)json.get("width")).intValue();
                    else
                        colratio += "," + ((Integer)json.get("width")).intValue();			
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
						contentType:"application/x-www-form-urlencoded; charset=iso8859-1",                        
                        url: "<%=queryAjaxUrl%>",
                        data: {
                            id: "<%=lf.getQueryId()%>",
							<%
							while (irJson.hasNext()) {
								String qField = (String) irJson.next();
							%>
							<%=qField%> : o("<%=qField%>").value,
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
                            jAlert("查询错误！",'<lt:Label res="res.flow.Flow" key="prompt"/>');
                        }
                    });		
				}
				
                $(function() {
					onQueryRelateFieldChange();
                });
				
				<%
				irJson = json.keys();
				while (irJson.hasNext()) {
					// 主表单中的字段
					String qField = (String) irJson.next();
					%>
					$(function() {
						if (isIE11) {
							var oldValue_<%=qField%> = o("<%=qField%>").value;
							
							setInterval(function(){
											if (oldValue_<%=qField%> != o("<%=qField%>").value) {
												onQueryRelateFieldChange();
												oldValue_<%=qField%> = o("<%=qField%>").value;
											}
										},500);
						}
						else {
							$('input[name=<%=qField%>]').bind('input propertychange', function() {
								onQueryRelateFieldChange();
							});
						}
					  
					});	
					<%
				}
				%>
				
				
                </script>
            <%}%>
        <%}%>	  
	  
      <div id="netdiskFilesDiv" class="percent98" style="line-height:1.5; text-align:left"></div>      
      <div style="clear:both">       
		<%
		Render rd = new Render(request, wf, doc);
		out.print(rd.rendFree(wa));
		%>
		
		<br>
        <div id="div1111" style="width:98%;background-color:#efefef;padding-top:10px;padding-bottom:10px;margin-bottom:50px;<%=wf.isStarted() ? "" : "display:none" %>">
            <div id="div2222" style="width:98%;background-color:white;padding-top:10px;">
                  <table class="" width="95%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="left" style="text-align:left"><strong>&nbsp;附言：</strong></td>
                      <td style="text-align:right">
                      	<input id="showDiv" style="display:none" class="mybtn2" type="button" value="展开" onclick="show()"/>
                      	<input id="notShowDiv" class="mybtn2" type="button" value="收起" onclick="notshow()"/>
                      	<input class="mybtn2" type="button" value="回复" onclick="addMyReply('<%=0%>')"/>
 					  </td>
                    </tr>
                    <tr>
                     	<td align="left" colspan="2">
                    		 <div id="myReplyTextarea<%=0%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
									<textarea name="myReplyTextareaContent" id="get<%=0%>" class="myTextarea"></textarea>
									<span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" id="isSecret<%=0%>" name="isSecret<%=0%>" value="0"/></span>
                                    <%if (fd.isProgress()) {%>
                                    &nbsp;&nbsp;进度&nbsp;<input id="cwsProgress" name="cwsProgress" style="width:30px; height:22px;" value="0" readonly="readonly" />
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
									<input type="hidden" id="discussId<%=0%>"  name="discussId<%=0%>" value="<%=0 %>"/>
									<input type="hidden" id="flow_id<%=0%>"  name="flow_id<%=0%>" value="<%=flowId %>"/>
									<input type="hidden" id="action_id<%=0%>" name="action_id<%=0%>" value="<%=0 %>"/>
									<input type="hidden" id="user_name<%=0%>" name="user_name<%=0%>" value="<%=myname%>"/>
									<input type="hidden" id="userRealName<%=0%>" name="userRealName<%=0%>" value="<%=myRealName%>"/>
									<input type="hidden" id="reply_name<%=0%>" name="reply_name<%=0%>" value="<%=myname%>"/>
									<input type="hidden" id="parent_id<%=0%>" name="parent_id<%=0%>" value="<%=-1%>"/>
									<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=0%>','<%=0%>')"/>
					 		</div>
					 	 </td>
					 </tr>
                    <tr>
                     	<td colspan="2">
                     	    <hr class="hrLine"/>
                        </td>
                    </tr> 
                  </table>
                 <div id="divShow" style="width:98%;">
                 <table id="tablehead" class="" width="95%" border="0" cellspacing="3" cellpadding="0">
                 </table>
                  <%		
		int total = 0;
		int pagesize = 20;
		int curpage = ParamUtil.getInt(request, "CPages", 1);
		WorkflowAnnexDb wad = new WorkflowAnnexDb();
		Vector<WorkflowAnnexDb> vec1 = wad.listRoot(flowId, myname);

        Iterator<WorkflowAnnexDb> ir1 = vec1.iterator();

		while (ir1.hasNext()) {
			wad = ir1.next();
			int id = (int)wad.getLong("id");
			int n=1;
		%>                
		<table id="replaytable<%=id%>" class="" width="95%" border="0" cellspacing="3" cellpadding="0">
                    <tr>
                      <td width="50" style="text-align:left;" class="nameColor">
                      	  <%=um.getUserDb(wad.getString("user_name")).getRealName()%>&nbsp;:
                      </td>
                       <td width="70%" style="text-align:left;word-break:break-all">
                          <%
						  if (fd.isProgress()) {
						  	%>
							<div>进度：<%=wad.getInt("progress")%>%</div>
							<%
						  }
						  %>
                      	  <%=wad.getString("content")%>
                      	  <%if (isFlowManager) {%>
                      		  <a href="#" onClick="jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>','提示',function(r){if(!r){return;}else{window.location.href='flow_modify.jsp?op=delAnnex&annexId=<%=wad.getLong("id")%>&flowId=<%=flowId%>'}}) ">[<lt:Label res="res.flow.Flow" key="delete"/>]</a>
                      	  <%}%>
                      </td>
                      <td width="" style="text-align:right;">
                      	  <%=DateUtil.format(wad.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%> &nbsp;&nbsp;
                      	  <a align="right" class="comment" href="javascript:;" onclick="addMyReply('<%=id%>')"><img title='<lt:Label res="res.flow.Flow" key="replyTo"/>' src="images/dateline/replyto.png"/></a>
                      </td>
                     </tr>
                     <tr>
                     	 <td align="left" colspan="3">
                    		 <div id="myReplyTextarea<%=id%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
									<textarea name="myReplyTextareaContent" id="get<%=id%>" class="myTextarea"></textarea>
									<span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" id="isSecret<%=id%>" name="isSecret<%=id%>" value="0"/></span>
                            		<input type="hidden" id="myActionId<%=id%>" name="myActionId<%=id%>" value=""/>
									<input type="hidden" id="discussId<%=id%>" name="discussId<%=id%>" value="<%=id %>"/>
									<input type="hidden" id="flow_id<%=id%>" name="flow_id<%=id%>" value="<%=wad.getString("flow_id") %>"/>
									<input type="hidden" id="action_id<%=id%>" name="action_id<%=id%>" value="<%=wad.getString("action_id") %>"/>
									<input type="hidden" id="user_name<%=id%>" name="user_name<%=id%>" value="<%=myname%>"/>
									<input type="hidden" id="userRealName<%=id%>" name="userRealName<%=id%>" value="<%=myRealName%>"/>
									<input type="hidden" id="reply_name<%=id%>" name="reply_name<%=id%>" value="<%=wad.getString("user_name")%>"/>
									<input type="hidden" id="parent_id<%=id%>" name="parent_id<%=id%>" value="<%=id%>"/>
									<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=id%>','<%=id%>')"/>
					 		</div>
					 	 </td>
					 </tr>
                    <%
			       	    WorkflowAnnexDb wad2 = new WorkflowAnnexDb();
					    Vector<WorkflowAnnexDb> vec2 = wad2.listChildren(wad.getInt("id"), myname);
					    Iterator<WorkflowAnnexDb> ir2 = vec2.iterator();
					    
					    while (ir2.hasNext()) {
						    wad2 = ir2.next();
							int id2 = (int)wad2.getLong("id");
					%> 
					<tr>
                        <td width="180" style="text-align:left;" class="nameColor">
					  	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=um.getUserDb(wad2.getString("user_name")).getRealName()%>&nbsp;回复&nbsp;<%=um.getUserDb(wad2.getString("reply_name")).getRealName()%>&nbsp;:
					  	</td>
					  	<td style="text-align:left;">
					  	   <%=wad2.getString("content")%>
					  	   <%if (isFlowManager) {%>
                      		  <a href="javascript:;" onclick="jConfirm('<lt:Label res="res.flow.Flow" key="isDelete"/>','提示',function(r){if(!r){return;}else{window.location.href='flow_modify.jsp?op=delAnnex&annexId=<%=wad2.getLong("id")%>&flowId=<%=flowId%>'}}) ">[<lt:Label res="res.flow.Flow" key="delete"/>]</a>
                      	   <%}%>
					  	</td>
					  	<td style="text-align:right;">
                      	  <%=DateUtil.format(wad2.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%> &nbsp;&nbsp;
                      	  <a align="right" class="comment" href="javascript:;" onclick="addMyReply('<%=id2%>')"><img title='<lt:Label res="res.flow.Flow" key="replyTo"/>' src="images/dateline/replyto.png"/></a>
                      	</td>
                    </tr>
                    <tr>
                     	 <td align="left" colspan="3">
                    		 <div id="myReplyTextarea<%=id2%>" style="display:none; clear:both;position:relative;margin-bottom:40px">
									<textarea name="myReplyTextareaContent" id="get<%=id2%>" class="myTextarea"></textarea>
									<span align="left" title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" id="isSecret<%=id2%>" name="isSecret<%=id2%>" value="0"/></span>
                            		<input type="hidden" id="myActionId<%=id2%>" name="myActionId<%=id2%>" value=""/>
									<input type="hidden" id="discussId<%=id2%>" name="discussId<%=id2%>" value="<%=id2 %>"/>
									<input type="hidden" id="flow_id<%=id2%>" name="flow_id<%=id2%>" value="<%=wad2.getString("flow_id") %>"/>
									<input type="hidden" id="action_id<%=id2%>" name="action_id<%=id2%>" value="<%=wad2.getString("action_id") %>"/>
									<input type="hidden" id="user_name<%=id2%>" name="user_name<%=id2%>" value="<%=myname%>"/>
									<input type="hidden" id="userRealName<%=id2%>" name="userRealName<%=id2%>" value="<%=myRealName%>"/>
									<input type="hidden" id="reply_name<%=id2%>" name="reply_name<%=id2%>" value="<%=wad2.getString("user_name")%>"/>
									<input type="hidden" id="parent_id<%=id2%>" name="parent_id<%=id2%>" value="<%=id%>"/>
									<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=id2%>','<%=id%>')"/>
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
                  </div></div></div>
		
		<input name="returnBack" value="<%=wf.isReturnBack()?"true":"false"%>" type=hidden>
     </div>
          </td>
        </tr>
  </table>
</form>
<%if (!cfg.get("isUseNTKO").equals("true")) {%>
        <br />
        <table class="tabStyle_1 percent80" width="75%"  border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td colspan="4" align="left" class="tabStyle_1_title">&nbsp;处理附件</td>
          </tr>
          <tr>
            <td colspan="4" align="center"><object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                <param name="Encode" value="utf-8" />
                <param name="BackColor" value="0000ff00" />
                <param name="Server" value="<%=Global.server%>" />
                <param name="Port" value="<%=Global.port%>" />
                <!--设置是否自动上传-->
                <param name="isAutoUpload" value="1" />
                <!--设置文件大小不超过1M-->
                <param name="MaxSize" value="<%=Global.MaxSize%>" />
                <!--设置自动上传前出现提示对话框-->
                <param name="isConfirmUpload" value="1" />
                <!--设置IE状态栏是否显示信息-->
                <param name="isShowStatus" value="0" />
                <param name="PostScript" value="<%=Global.virtualPath%>/flow_document_check.jsp" />
				  <%
                  com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
                  %>
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />                
              </object>
            <!--<input name="remsg" type="button" onclick='alert(redmoonoffice.ReturnMessage)' value="查看上传后的返回信息" />-->            </td>
          </tr>
		  </table>
<%}%>          
			<%if (wfp.canUserDo(user, wf, "archive")) {%>
			  <iframe id="hidFrame" src="flow_doc_archive_content.jsp?flowId=<%=flowId%>&actionId=<%=actionId%>" width=0 height=0></iframe>
			<%}%>
      <br />
<table width=98% border="0" align="center" cellpadding="0" cellspacing="0">
<form name=form100 action="?op=modifyFlow" method=post>
<tr><td>
<input name="flowId" type="hidden" value="<%=flowId%>">
<input name="myActionId" type="hidden" value="<%=myActionId%>">
</td></tr>
</form>
</table>
<br />
<div id="plusDlg" style="display:none">
<table width="80%" class="tabStyle_1 percent80">
<tr>
  <td width="23%"><strong>
  加签类型</strong></td>
  <td width="77%">
  <input id="plusType" name="plusType" type="radio" checked value="<%=WorkflowActionDb.PLUS_TYPE_BEFORE%>" />
  前加签
  <input id="plusType" name="plusType" type="radio" value="<%=WorkflowActionDb.PLUS_TYPE_AFTER%>" />
  后加签
  <input id="plusType" name="plusType" type="radio" value="<%=WorkflowActionDb.PLUS_TYPE_CONCURRENT%>" />
  并签</td>
</tr>
<tr id="plusModeTr">
  <td><strong>审批方式</strong></td>
  <td>
  <input id="plusMode" name="plusMode" type="radio" value="<%=WorkflowActionDb.PLUS_MODE_ORDER%>" />
  顺序审批<br />
  <input id="plusMode" name="plusMode" type="radio" value="<%=WorkflowActionDb.PLUS_MODE_ONE%>" />
  有人审批完成即向下流转<br />
  <input id="plusMode" name="plusMode" type="radio" value="<%=WorkflowActionDb.PLUS_MODE_ALL%>" checked />
  全部审批
  </td>
</tr>
<tr>
  <td><strong>选择人员</strong></td>
  <td>
    <input name="plusUsers" id="plusUsers" type="hidden" value="">	
    <input name="plusUserRealNames" readonly wrap="yes" id="plusUserRealNames" />
    <input class="btn" title="选择用户" onClick="openWinUsers(true)" type="button" value="选择">  
  </td>
</tr>
</table>
</div>

</div>

<div id="dlg" style="display:none"></div>
</body>
<script>
function openWinPersonUserGroup() {
	openWin("user/persongroup_user_multi_sel.jsp", 520, 400);
}

var errFunc = function(response) {
    alert('Error ' + response.status + ' - ' + response.statusText);
	alert(response.responseText);
}

function doRenameAtt(response) {
	var items = response.responseXML.getElementsByTagName("item");
	if (items.length==0){
		alert(response.responseText);
		return;
	}
	for (var i=0; i<items.length; i++){
		var item = items[i];
		var attId = item.getElementsByTagName("attId")[0].firstChild.data;
		var result = item.getElementsByTagName("result")[0].firstChild.data;
		var newName = item.getElementsByTagName("newName")[0].firstChild.data;
		
		alert(result);
		
		if (result=="操作成功！") {
			o("spanRename" + attId).style.display = "";
			o("spanAttNameInput" + attId).style.display = "none";
			o("spanAttName" + attId).innerHTML = newName;
			o("spanAttLink" + attId).style.display = "";
		}
	}
}

function renameAtt(attId) {
	if (o("spanAttName" + attId).innerHTML==o("attName"+attId).value) {
		o("spanRename" + attId).style.display = "";
		o("spanAttNameInput" + attId).style.display = "none";
		o("spanAttLink" + attId).style.display = "";
		return;
	}
	var str = "myop=renameAtt&attId=" + attId + "&newName=" + o("attName" + attId).value;
	var myAjax = new cwAjax.Request(
		"flow_dispose_do.jsp",
		{
			method:"post",
			parameters:str,
			onComplete:doRenameAtt,
			onError:errFunc
		}
	);	
}

function changeName(attId) {
	o("spanRename" + attId).style.display = "none";
	o("spanAttNameInput" + attId).style.display = "";
	o("spanAttLink" + attId).style.display = "none";
}

function writeDoc() {
	openWin("flow/flow_ntko_write_doc.jsp?flowId=<%=flowId%>", 800, 600);
}

function setPerson(deptCode, deptName, user, userRealName) {
	/*
	form1.userRealName.value = userRealName;
	form1.userName.value = user;
	*/
	
	jConfirm('您确定要指派给' + userRealName + '么？', '提示', function(r) {
    	if (r) {
			o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";
			
			$.ajax({
				type: "post",
				url: "flow/flow_dispose_ajax.jsp",
				data : {
					op: "transfer",
					toUserName: user,
					isUseMsg: o("isUseMsg").checked,
					isToMobile: o("isToMobile")?o("isToMobile").checked:"false",
					myActionId: "<%=myActionId%>"
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					//ShowLoading();
				},
				success: function(data, status){
					o("spanLoad").innerHTML = "";
					data = $.parseJSON(data);
					if (data.ret=="0") {
						jAlert(data.msg, "提示");
					}
					else {
						jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
					}
				},
				complete: function(XMLHttpRequest, status){
					//HideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});			
		}
	});
}

function refreshAttachments() {
	ajaxpage("<%=Global.getFullRootPath(request)%>/flow_dispose_ajax_att.jsp?myActionId=<%=myActionId%>&flowId=<%=flowId%>", "attDiv");
	o("netdiskFilesDiv").innerHTML = "";
}

function transfer() {
	// showModalDialog('user_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;');
	openWin('user_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>', 800, 600);
}

function suspend() {
	jConfirm('您确定要挂起么？', '提示', function(r) {
    	if (r) {
			o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";
			
			$.ajax({
				type: "post",
				url: "flow/flow_dispose_ajax.jsp",
				data : {
					op: "suspend",
					myActionId: "<%=myActionId%>"
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					//ShowLoading();
				},
				success: function(data, status){
					o("spanLoad").innerHTML = "";
					data = $.parseJSON(data);
					if (data.ret=="0") {
						jAlert(data.msg, "提示");
					}
					else {
						jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
					}
				},
				complete: function(XMLHttpRequest, status){
					//HideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});			
		}
	});
}

function resume() {
	jConfirm('您确定要恢复么？', '提示', function(r) {
    	if (r) {
			o("spanLoad").innerHTML = "<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' />";
			
			$.ajax({
				type: "post",
				url: "flow/flow_dispose_ajax.jsp",
				data : {
					op: "resume",
					myActionId: "<%=myActionId%>"
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					//ShowLoading();
				},
				success: function(data, status){
					o("spanLoad").innerHTML = "";
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
					//HideLoading();
				},
				error: function(XMLHttpRequest, textStatus){
					// 请求出错处理
					alert(XMLHttpRequest.responseText);
				}
			});			
		}
	});
}

var lv_title = new LiveValidation('cwsWorkflowTitle');
lv_title.add(Validate.Presence, { failureMessage:'<lt:Label res="res.flow.Flow" key="writeTitle"/>'} );

// 用于massValidate检查表单内容
var lv_cwsWorkflowResult = new LiveValidation('cwsWorkflowResult');

var toolbar;

toolbar = new Toolbar({
  renderTo : 'toolbar',
  //border: 'top',
  items : [
  {
	type : 'button',
	text : '保存草稿',
	title: '不提交，仅保存草稿',
	bodyStyle : 'save',
	useable : 'T',
	handler : function(){
		  /*
		  if (!LiveValidation.massValidate(lv_cwsWorkflowResult.formObj.fields)) {
			  jAlert("请检查表单中的内容填写是否正常！", "提示");
			  return;
		  }
		  */	
		
		  o('op').value = "saveformvalue";
		  if (o('flowForm').onsubmit) {
			  // if (o('flowForm').onsubmit()) {
				  // o('flowForm').submit();
				  
				  $('#bodyBox').showLoading();
				  $('#flowForm').submit();
			  // }
		  }
	}
  }
  <%if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>  
  ,'-',{
		  type: 'button',
		  text: '提交',
		  title: '提交至下一节点处理',
		  bodyStyle: 'commit',
		  useable: 'T',
		  handler: function () {
			  if (!LiveValidation.massValidate(lv_cwsWorkflowResult.formObj.fields)) {
				  jAlert("请检查表单中的内容填写是否正常！", "提示");
				  return;
			  }

			  if (o("cwsWorkflowTitle") && o("cwsWorkflowTitle").value == "<%=lf.getName()%>") {
				  jConfirm("流程标题为默认标题，您确定不修改就提交么？", "提示", function (r) {
					  if (r) {
						  SubmitResult();
					  }
				  });
				  return;
			  }

			  jConfirm("您确定要提交么？", "提示", function (r) {
				  if (r) {
					  SubmitResult();
				  }
			  });
		  }
  }
  <%if (mad.getActionStatus()!=WorkflowActionDb.STATE_PLUS && returnv.size()>0) {%>
  ,'-',{
	type : 'button',
	text : '返回',
	title: '返回给之前的节点处理人员',	  
	bodyStyle : 'return',
	useable : 'T',
	handler : function(){
	  returnFlow();
	  
	  /*
	  if (confirm('您确定要返回么？')) {
		  flowForm.op.value='return';
		  
		  if (o('flowForm').onsubmit) {
			  if (o('flowForm').onsubmit()) {
				  flowForm.submit();
			  }
		  }
	  }
	  */
	}
  }
  <%}%>	
  <%}%>
  <%if (wf.isStarted()) {%>
  ,'-',{
	type : 'button',
	text : '过程',
	title: '查看流程的流转过程',	  
	bodyStyle : 'process',
	useable : 'T',
	handler : function(){
	  window.location.href='flow_modify.jsp?flowId=<%=wf.getId()%>';
	}	  
  }
  <%}%>
  <%
  FlowConfig conf = new FlowConfig();	  
  if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>    
	<%
	// if (mad.getId()!=mad.getFirstMyActionDbOfFlow(flowId).getId() && wa.getPlus().equals("")) {
	if (conf.getIsDisplay("FLOW_BUTTON_PLUS") && wa.getPlus().equals("")) {
	%>
	,'-',{
      type : 'button',
      text : '加签',
	  title: '加签',
      bodyStyle : 'plus',
      useable : 'T',
      handler : function() {
	 	  addPlus();
	  }
    }	
	<%}%> 
	<%
	if (conf.getIsDisplay("FLOW_BUTTON_DOC")){%>
  	,'-',{
		type : 'button',
		text : '拟文',
		title: '拟订Word文档',	  
		bodyStyle : 'doc',
		useable : 'T',
		handler: function(){
		  writeDoc();
		}
  	}
  <%}%>
<%if (wf.isStarted() && wfp.canUserDo(user, wf, "stop")) {%>
  ,'-',{
	type : 'button',
	text : '拒绝',
	title: '拒绝同时结束流程',	  
	bodyStyle : 'disagree',
	useable : 'T',
	handler : function(){
	  if (confirm('您确定要结束流程么？')) {
		  if (o('flowForm').onsubmit) {
			  if (o('flowForm').onsubmit()) {
				  flowForm.op.value='manualFinish';
				  // flowForm.submit();
				  $("#flowForm").submit();
			  }
		  }
	  }
	}
  }
<%}%>
<%}%>
<%
if (wf.isStarted()) {
if (mad.getCheckStatus()==MyActionDb.CHECK_STATUS_SUSPEND) {%>
  ,'-',{
	type : 'button',
	text : '恢复',
	title: '恢复处理流程',	  
	bodyStyle : 'suspend',
	useable : 'T',
	handler : function(){
	  resume();
	}	  
  }
<%}else{%>
  ,'-',{
	type : 'button',
	text : '挂起',
	title: '将流程挂起，暂不处理',	  
	bodyStyle : 'suspend',
	useable : 'T',
	handler : function(){
	   suspend();
	}	  
  }
<%}
}%>
<%
if (conf.getIsDisplay("FLOW_BUTTON_TRANSFER")) {
	if (mad.getCheckStatus()!=MyActionDb.CHECK_STATUS_SUSPEND) {%>
	<%if (wf.isStarted()) {%>
	  ,'-',{
		type : 'button',
		text : '指派',
		title: '指派给其他人处理',	  
		bodyStyle : 'transfer',
		useable : 'T',
		handler : function(){
			transfer();
		}	  
	  }
	<%
	  }
	}
}
%>
/*
<%if (wfp.canUserDo(user, wf, "discard")) {%>
  ,'-',{
	type : 'button',
	text : '放弃',
	title: '放弃流程，流程不会被删除，也不能再被处理',	  
	bodyStyle : 'discard',
	useable : 'T',
	handler : function(){
		<%if (wfp.canUserDo(user, wf, "discard")) {%>
		if (confirm('您确定要放弃流程么？'))
			window.location.href='?op=discard&myActionId=<%=myActionId%>';
		<%}%>
	}
  }
<%}%>
*/
<%if (wfp.canUserDo(user, wf, "del")) {%>
  ,'-',{
	type : 'button',
	text : '删除',
	title: '删除流程',	  
	bodyStyle : 'del',
	useable : 'T',
	handler : function(){
	  if (confirm('您确定要删除流程么？'))
		  window.location.href='flow_del.jsp?flow_id=<%=flowId%>';
	}
  }
<%}%>
<%if (wfp.canUserDo(user, wf, "archive")) {%>
  ,'-',{
	type : 'button',
	text : '存档',
	title: '将表单存入文件柜',	  
	bodyStyle : 'archive',
	useable : 'T',
	handler : function(){		  
	  saveArchive(<%=wf.getId()%>, <%=wa.getId()%>)
	}
  }
<%}%>

	<%
	if (conf.getIsDisplay("FLOW_BUTTON_LINKPROJECT")){	
		if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform()) {%>
		,'-',{
		  type : 'button',
		  text : '关联项目',
		  title: '关联项目',
		  bodyStyle : 'linkProject',
		  useable : 'T',
		  handler : function(){
			  linkProject();
		  }
		}
	<%}
	}%>
	<%
	if (conf.getIsDisplay("FLOW_BUTTON_NETDISKFILE")){%>	
	,'-',{
      type : 'button',
      text : '网盘附件',
	  title: '网盘附件',
      bodyStyle : 'netdiskfile',
      useable : 'T',
      handler : function(){
		  selectNetdiskFile();
      }
    }	
    <%}%>
	<%
	if (conf.getIsDisplay("FLOW_BUTTON_ATTENTION")){%>
	,'-',
		<%
		WorkflowFavoriteDb wffd = new WorkflowFavoriteDb();
		if (!wffd.isExist(myname, (long) flowId)){%>
		{
	      type : 'button',
	      text : '<%=conf.getBtnName("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnName("FLOW_BUTTON_ATTENTION")%>',
		  title: '<%=conf.getBtnTitle("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnTitle("FLOW_BUTTON_ATTENTION")%>',
	      bodyStyle : 'attention',
	      useable : 'T',
	      handler : function(){
			  setAttention(true);
	      }
	    }
		<%} else {%>
		{
		  type : 'button',
		  text : '<%=conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION")%>',
		  title: '<%=conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION")%>',
		  bodyStyle : 'cancelAttention',
		  useable : 'T',
		  handler : function(){
			  setAttention(false);
		  }
		}
		<%}%>
		<%}%>
  ]
});

toolbar.render();

$('#flowForm').submit(function() {
	var options = {
		success:    showResponse,  // post-submit callback 
		error: 		showError,
		dataType:   'text' // 'text/html'   // 'xml', 'script', or 'json' (expected server response type)  表单为multipart/form-data即上传文件时，json无法解析
	};
	$(this).ajaxSubmit(options);
	return false;
});

// 如果不注释掉，则当直接在桌面点击时，无法载入
// $(document).ready(function() {
  refreshAttachments();
// });

function selectNetdiskFile() {
	openWin('netdisk/netdisk_frame.jsp?mode=select', 800, 600);
}

function switchProcessList() {
	/**if (o("imgSwitchProcess").src.indexOf("show.gif")!=-1) {
		$("#processListTab").show();
		o("imgSwitchProcess").src = "images/hide.gif";
		o("imgSwitchProcess").alt = "隐藏流程处理过程";
	}
	else {
		$("#processListTab").hide();
		o("imgSwitchProcess").src = "images/show.gif";
		o("imgSwitchProcess").alt = "显示流程处理过程";
	}*/
	if (o("imgSwitchProcess") == null){
        return;
    }
    if (o("imgSwitchProcess").src.indexOf("show.png")!=-1) {
        $("#processListTab").show();
        o("imgSwitchProcess").src = "images/hide.png";
        $("#spanSwitchProcess").html('&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="collapse"/>');
        o("imgSwitchProcess").alt = '<lt:Label res="res.flow.Flow" key="flowProcess"/>';
        o("imgSwitchProcess").title = '<lt:Label res="res.flow.Flow" key="flowProcess"/>';
    }
    else {
        $("#processListTab").hide();
        o("imgSwitchProcess").src = "images/show.png";
        $("#spanSwitchProcess").html('&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="expansion"/>');
        o("imgSwitchProcess").alt = '<lt:Label res="res.flow.Flow" key="displayProcess"/>';
        o("imgSwitchProcess").title = '<lt:Label res="res.flow.Flow" key="displayProcess"/>';
    }
}

function addPlus() {
	isAddPlusing = true;
	$("#plusDlg").dialog({
		title:"请选择加签人员",
		modal: true,
		width: 500,
		height: 220,
		// bgiframe:true,
		buttons: {
			"取消": function() {
				isAddPlusing = false;
				$(this).dialog("close");
			},
			"确定": function() {
				isAddPlusing = false;
				if ($("#plusUsers").val()=="") {
					jAlert("请选择加签人员！", "提示");
					return;
				}
				
				$.ajax({
					type: "post",
					url: "flow/flow_dispose_ajax.jsp",
					data : {
						op: "plus",
						myActionId: "<%=myActionId%>",
						users: o("plusUsers").value,
						type: $("input[name='plusType']:checked").val(),
						mode: $("input[name='plusMode']:checked").val(),
						actionId: "<%=actionId%>"
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#flowForm').showLoading();				
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
							if (data.type=="<%=WorkflowActionDb.PLUS_TYPE_BEFORE%>") {
								jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
							}
							else {
								jAlert(data.msg, "提示");
							}
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#flowForm').hideLoading();				
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

function setNetdiskFiles(ids) {
	getNetdiskFiles(ids);
}

function doGetNetdiskFiles(response){
	var rsp = response.responseText.trim();
	o("netdiskFilesDiv").innerHTML += rsp;
}

function getNetdiskFiles(ids) {
	var str = "ids=" + ids;
	var myAjax = new cwAjax.Request( 
		"<%=cn.js.fan.web.Global.getFullRootPath(request)%>/netdisk/ajax_getfile.jsp", 
		{ 
			method:"post",
			parameters:str,
			onComplete:doGetNetdiskFiles,
			onError:errFunc
		}
	);
}

function returnFlow() {
		$.ajax({
			type: "post",
			url: "flow_dispose_ajax_return.jsp",
			data : {
				myActionId: "<%=myActionId%>",
				actionId: "<%=actionId%>",
				flowId: "<%=flowId%>"
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#bodyBox').showLoading();				
			},
			success: function(data, status){
				o("spanLoad").innerHTML = "";
				$("#dlg").html("" + data.trim());
				$("#dlg").dialog({title:"请选择需返回的用户", modal: true,
									buttons: {
										"取消":function() {
											$(this).dialog("close");
										},
										"确定": function() {
											// 必须要用clone，否则checked属性在IE9、chrome中会丢失
											// o("dlgReturn").innerHTML = $("#dlg").html();
											
											if (true || confirm('您确定要返回么？')) {
												// 因为radio是成组的，所以不能直接用$("#dlgReturn").html($("#dlg").clone())
												// 某一组radio只会有一个被选中，所以这里可能会导致第一次选中返回时报请选择用户，而刷新以后，再选择用户返回就可以了
												var tmp = $("#dlg").clone().html();
												$("#dlgReturn").html(tmp);
												
												if (isIE11) {
													$("#dlg").find("input").each(function(){
														var obj = $(this);
														$("input:radio", o("dlgReturn")).each(function(){
															if (obj.val()==this.value) {
																if (obj.attr("checked")=="checked") {
																	this.setAttribute("checked", "checked");
																}
															}
														});
													});
												}												
												
												$("#dlg").html();
												// alert($("#dlgReturn").html());
												
												flowForm.op.value='return';

												if (o('flowForm').onsubmit) {
													if (o('flowForm').onsubmit()) {
														$('#flowForm').submit();
													}
												}
												else
													$('#flowForm').submit();
											}
											$(this).dialog("close");
											
											$('#bodyBox').showLoading();				
										}
									},
									closeOnEscape: true,
									draggable: true,
									resizable:true,
									width:500
								});
			},
			complete: function(XMLHttpRequest, status){
				$('#bodyBox').hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});
}

function showResponse(data)  {	
	// 过滤掉其它字符，只保留JSON字符串
	var m = data.match(/\{.*?\}/gi);
	if (m!=null) {
		if (m.length==1) {
			data = m[0];
		}
	}
	
	$('#bodyBox').hideLoading();		
		
	try {
		data = jQuery.parseJSON(data);
	}
	catch(e) {
		alert(data);
		toolbar.setDisabled(1, false);
		return;
	}
	
	if (data==null)
		return;
	
	data.msg = data.msg.replace(/\\r/ig, "<BR>");
	
	if (data.ret=="0") {
		toolbar.setDisabled(0, false);
		jAlert(data.msg, "提示");
		return;
	}
	
	var op = data.op;
	if (op=="saveformvalue") {
		toolbar.setDisabled(0, false);
		jAlert(data.msg, "提示");
		// jAlert_Redirect(data.msg, "提示", "flow_dispose.jsp?myActionId=<%=myActionId%>");
		refreshAttachments();
		delAllUploadFile();
		return;
	}
	else if (op=="manualFinish") {
		jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
	}
	/*
	else if (op=="AutoSaveArchiveNodeCommit") {
		var nextMyActionId = data.nextMyActionId;
		if (nextMyActionId!="") {
			// 用户有可能不点确定按钮就直接关闭IE，导致未能自动存档
			// jAlert_Redirect(data.msg, "提示", "flow_doc_archive_save_auto.jsp?actionId=<%=actionId%>&myActionId=" + nextMyActionId);
			window.location.href = "flow_doc_archive_save_auto.jsp?actionId=<%=actionId%>&myActionId=" + nextMyActionId;
		}
		else {
			// jAlert_Redirect(data.msg, "提示", "flow_doc_archive_save_auto.jsp?actionId=<%=actionId%>");
			window.location.href = "flow_doc_archive_save_auto.jsp?actionId=<%=actionId%>";
		}			
	}
	*/
	else if (op=="finish") {
		var nextMyActionId = data.nextMyActionId;
		if (nextMyActionId!="")
			jAlert_Redirect(data.msg, "提示", "flow_dispose_free.jsp?myActionId=" + nextMyActionId);
		else
			jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
	}
	else if (op=="return") {
		jAlert_Redirect(data.msg, "提示", "flow/flow_list.jsp?displayMode=1");
	}
	
	return;
}

function showError(pRequest, pStatus, pErrorText) {
	alert('pStatus='+pStatus+'\r\n\r\n'+'pErrorText='+pErrorText);
}

function delAtt(docId, attId) {
	if (confirm('您确定要删除吗？')) {
		$.getJSON('flow_dispose_ajax_att.jsp', 
			{
			"op":"delAttach",
			"myActionId":<%=myActionId%>,
			"flowId":<%=flowId%>,
			"doc_id":docId,
			"attach_id":attId
			},
			function(data) {
				if (data.re=="true") {
					jAlert(data.msg, "提示");
					$('#trAtt' + attId).remove();
				}
				else {
					jAlert(data.msg, "提示");
				}
				
			});		
	}
}

function setAttention(isAttention) {
	$.ajax({
		type: "post",
		url: "flow/flow_do.jsp",
		data : {
			op: isAttention ? "favorite" : "unfavorite",
			flowId: "<%=flowId%>"
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$('#bodyBox').showLoading();				
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="0") {
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
			}
			else {
				//jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
				$.toaster({priority : 'info', message : data.msg });
				if (isAttention) {
					$('.attention').html('<%=conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnName("FLOW_BUTTON_CANCEL_ATTENTION")%>');
					$('.attention').attr('title', '<%=conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","cancelAttention"):conf.getBtnTitle("FLOW_BUTTON_CANCEL_ATTENTION")%>');
					$('.attention').attr('class', 'cancelAttention');
					$('.cancelAttention').unbind().bind('click', function(){
						setAttention(false);
					});
				} else {
					$('.cancelAttention').html('<%=conf.getBtnName("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnName("FLOW_BUTTON_ATTENTION")%>');
					$('.cancelAttention').attr('title', '<%=conf.getBtnTitle("FLOW_BUTTON_ATTENTION").startsWith("#")?LocalUtil.LoadString(request,"res.flow.Flow","attention"):conf.getBtnTitle("FLOW_BUTTON_ATTENTION")%>');
					$('.cancelAttention').attr('class', 'attention');
					$('.attention').unbind().bind('click', function(){
						setAttention(true);
					});
				}
			}
		},
		complete: function(XMLHttpRequest, status){
			$('#bodyBox').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
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
                //initTree();
        };
 
        addEvent('propertychange', change);
        addEvent('input', change);
        addEvent('focus', change);
        change();
        
};

function submitPostscript(textareaId, rootId){
	var textareaContent = $("#get"+textareaId).val();//“评论”文本框的内
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
			type: "get",
			url: "flow_dispose_free_do.jsp?action=addReplyDispose",
			//data : $("#flowForm"+textareaId).serialize(),
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
				data = data.substring(data.indexOf("{\"ret\""));
				//alert(data);
				var re = $.parseJSON(data);
				if (re.ret=="1") {
					if(rootId==0){
						$("#tablehead").append(re.result);
						$("#tablehead").find("td:eq(1)").prepend("<div>进度：" + $('#cwsProgress').val() + "%</div>");						
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
				alert('<lt:Label res="res.flow.Flow" key="replyWrong"/>');
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

</script>
</html>