<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.task.TaskDb"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import="java.util.regex.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<%@ page import="org.json.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><lt:Label res="res.flow.Flow" key="suggestion"/></title>
<meta http-equiv="X-UA-Compatible" content="IE=8">
<meta name="renderer" content="ie-stand">
<link href="flow_dispose_light.css" rel="stylesheet" type="text/css" />
<link href="flowstyle.css" rel="stylesheet" type="text/css" />
<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />
<link rel="stylesheet" href="js/jstree/themes/default/style.css" />
<link href="images/dateline/style.css" rel="stylesheet" type="text/css" />
<style>

.uploadFileFlow{
    border: medium none;
    cursor: pointer;
    font-size: 30px;
    height: 45px;
    margin: 0px 0 0 -20px;
    opacity: 0;
	filter:alpha(opacity=0);
    padding: 0;
    text-align: left;
    width: 181px;
    float:left;
}

.userRealName {
	display: -moz-inline-box;
	display: inline-block;
	width: 150px;
}
</style>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>

<script type="text/javascript" src="js/jquery.qqFace.js"></script>
<script src="js/jquery.toaster.js"></script>
<script type="text/javascript" src="js/appendGrid/jquery.appendGrid-1.5.1.js"></script>
<link type="text/css" rel="stylesheet" href="js/appendGrid/jquery.appendGrid-1.5.1.css" />
<link type="text/css" rel="stylesheet" href="skin/common/macro_detaillist_ctl.css" />
<script>
//校验@对象系统是否存在
var hasError = false;
// 为了解决其它jquery插件的兼容性
(function($) {
    if (!$.browser && $.fn.jquery != "1.3.2") {
        $.extend({
            browser: {}
        });
        $.browser.init = function() {
            var a = {};
            try {
                navigator.vendor ?
                    /Chrome/.test(navigator.userAgent) ?
                    (a.browser = "Chrome", a.version = parseFloat(navigator.userAgent.split("Chrome/")[1].split("Safari")[0])) : /Safari/.test(navigator.userAgent) ? (a.browser = "Safari", a.version = parseFloat(navigator.userAgent.split("Version/")[1].split("Safari")[0])) : /Opera/.test(navigator.userAgent) && (a.Opera = "Safari", a.version = parseFloat(navigator.userAgent.split("Version/")[1])) : /Firefox/.test(navigator.userAgent) ? (a.browser = "mozilla",
                        a.version = parseFloat(navigator.userAgent.split("Firefox/")[1])) : (a.browser = "MSIE", /MSIE/.test(navigator.userAgent) ? a.version = parseFloat(navigator.userAgent.split("MSIE")[1]) : a.version = "edge")
            } catch (e) { a = e; }
            $.browser[a.browser.toLowerCase()] = a.browser.toLowerCase();
            $.browser.browser = a.browser;
            $.browser.version = a.version;
            $.browser.chrome = $.browser.browser.toLowerCase() == 'chrome';
            $.browser.safari = $.browser.browser.toLowerCase() == 'safari';
            $.browser.opera = $.browser.browser.toLowerCase() == 'opera';
            $.browser.msie = $.browser.browser.toLowerCase() == 'msie';
            $.browser.mozilla = $.browser.browser.toLowerCase() == 'mozilla';
        };
        $.browser.init();
    }
})(jQuery);

function deliver() {
	if(flowForm.nextActionUsers.value == "") {
		if (CKEDITOR.instances.cwsWorkflowResult.document.getBody().getText() == "") {
			alert('<lt:Label res="res.flow.Flow" key="enterContent"/>');
			return ;
		}
	}
	
	if (hasError) {
		// alert('<lt:Label res="res.flow.Flow" key="alertMessage"/>');
		// return ;
	}

	if (!confirm('<lt:Label res="res.flow.Flow" key="submitForm"/>'))
		return false;
	
	o('flowForm').submit();
}
</script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet"
			type="text/css" media="screen" />
<script type="text/javascript" src="js/activebar2.js"></script>
<!--<script src="js/jquery-ui/jquery-ui.js"></script>-->
<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="js/jquery.form.js"></script>
<script src="js/jquery.bgiframe.js"></script>
<script src="inc/livevalidation_standalone.js"></script>
<script type="text/javascript" src="js/goToTop/goToTop.js"></script>
<link type="text/css" rel="stylesheet" href="js/goToTop/goToTop.css" />
<link href="js/jquery-showLoading/showLoading.css" rel="stylesheet"
			media="screen" />
<script type="text/javascript"
			src="js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="js/jstree/jstree.js"></script>
<style type="text/css">
@import  url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css");
</style>
<script type="text/javascript"
			src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript"
			src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript"
			src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
<script src="inc/map.js"></script>
<script src="inc/upload.js"></script>
<script src="inc/flow_dispose.jsp"></script>
<script src="inc/flow_js.jsp"></script>
<script tyle="text/javascript" language="javascript"
			src="spwhitepad/createShapes.js"></script>
<script src="inc/ajax_getpage.jsp"></script>
<link href="js/atwho/jquery.atwho.css" rel="stylesheet">
<script src="js/atwho/jquery.atwho.js"></script>
<script src="js/jquery.caret.js"></script>
<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>

<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
<%
	boolean isTreeStyle = ParamUtil.getBoolean(request, "isTreeStyle", false);

	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(
				request, "pvg_invalid")));
		return;
	}

	String myname = privilege.getUser(request);

	UserMgr um = new UserMgr();
	UserDb myUser = um.getUserDb(myname);

	/*
	if (!mad.getUserName().equals(myname)
			&& !mad.getProxyUserName().equals(myname)) {
		// 权限检查
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request,
				"pvg_invalid")));
		return;
	}
	*/

	int flowId = ParamUtil.getInt(request, "flowId", -1);

	// 置嵌套表需要用到的cwsId
	request.setAttribute("cwsId", "" + flowId);
	request.setAttribute("pageType", "flow");

	WorkflowMgr wfm = new WorkflowMgr();
	WorkflowDb wf = wfm.getWorkflowDb(flowId);
	int flowStatus = wf.getStatus();//判断该流程是否已经结束
	Leaf lf = new Leaf();
	lf = lf.getLeaf(wf.getTypeCode());

	// 置NestSheetCtl需要用到的formCode
	request.setAttribute("formCode", lf.getFormCode());
%>
<script src="flow/form_js/form_js_<%=lf.getFormCode()%>.js"></script>
<%
	// 锁定流程
	wfm.lock(wf, myname);

	WorkflowPredefineDb wfp = new WorkflowPredefineDb();
	wfp = wfp.getPredefineFlowOfFree(wf.getTypeCode());

	String op = ParamUtil.get(request, "op");

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

// 编辑文件
function editdoc(doc_id, file_id) {
	redmoonoffice.AddField("doc_id", doc_id);
	redmoonoffice.AddField("file_id", file_id);
	redmoonoffice.Open("<%=Global.getFullRootPath(request)%>/flow_document_get.jsp?doc_id=" + doc_id + "&file_id=" + file_id);
}

// 审批文件，并作痕迹保留
function ReviseByUserColor(user, colorindex, doc_id, file_id) {
	if (o("redmoonoffice")) {
		redmoonoffice.AddField("doc_id", doc_id);
		redmoonoffice.AddField("file_id", file_id);
	}

	<%if (cfg.get("isUseNTKO").equals("true")) {%>
	openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&doc_id=" + doc_id + "&isRevise=0", 800, 600);
	<%}else{%>
	editdoc(doc_id, file_id);
	<%}%>
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
		$('<div></div>').html('<lt:Label res="res.flow.Flow" key="install"/>').activebar({
			'icon': 'images/alert.gif',
			'highlight': '#FBFBB3',
			'url': 'activex/oa_client.EXE',
			'button': 'images/bar_close.gif'
		});
	}	
}

function window_onload() {	
	checkOfficeEditInstalled();
}

function linkProject() {
	openWin("<%=request.getContextPath()%>/project/project_list_sel.jsp?action=linkProject", 800, 600);
}

function unlinkProject() {
	jConfirm('<lt:Label res="res.flow.Flow" key="cancelAssociation"/>', '<lt:Label res="res.flow.Flow" key="prompt"/>', function(r) {	
		if (!r)
			{return false;}
		else{
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
						jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
						o("projectName").innerHTML = "";
					}
					else {
						jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
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
	});
}

function doLinkProject(prjId, prjName) {
	$.ajax({
		type: "post",
		url: "flow_dispose_do.jsp",
		data: {
			myop: "linkProject",
			projectId: prjId,
			flowId: <%=wf.getId()%>
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			// $('#bodyBox').showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			if (data.ret=="1") {
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
				o("projectName").innerHTML = "<lt:Label res='res.flow.Flow' key='project'/>：<a href=\"javascript:;\" onclick=\"addTab('" + prjName + "', 'project/project_show.jsp?projectId=" + prjId + "&formCode=project')\">" + prjName + "</a>&nbsp;&nbsp;<a title=\"取消关联\" href=\"javascript:;\" onclick=\"unlinkProject()\" style='font-size:16px; font-color:red'>×</a>";				
				insertProjectRelated(prjId);				
			}
			else {
				jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
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

function getSelUserNames() {
	return "";
	//return o("nextActionUsers").value;
}

function getSelUserRealNames() {
	return "";
	//return o("userRealNames").value;
}

function openWinUsers() {
	openWin('user_multi_sel.jsp','600px','480px');
}

function insertProjectRelated(projectId) {
	var str = projectId;
	
	insertCkeditorText("#"+str+"#");
	/*	
	var cont = o("cwsWorkflowResult");
	var oRange = o("cwsWorkflowResult").createTextRange();
    cont.focus();
	var contlen = cont.value.length;

    if(typeof document.selection != "undefined") {
        document.selection.createRange().text = " #" + str + "# ";
    }
    else {
        cont.value = cont.value.substr(0, cont.selectionStart)+"#"+str+"#"+cont.value.substring(cont.selectionStart,contlen);
    }

	if (oRange.findText(str)!=false) {
	   oRange.select();
	}
	*/
}

function setUsers(users, userRealNames, isOnInput) {
	if (users=="") {
		o("nextActionUsers").value = "";
		o("userRealNames").value = "";
		return;
	}
	var uNameAry = users.split(",");
	var uRealNameAry = userRealNames.split(",");
	var atUsers = "";
	users = "";
	userRealNames = "";
	var userary = flowForm.nextActionUsers.value.split(",");
	//var userary = getSelUserNames().split(",");
	// console.log(uNameAry.length);

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
			nextActionUserDiv.innerHTML += "<div id='nextUsersDiv" + uNameAry[i] + "' userName='" + uNameAry[i] + "' name='nextUsersDiv'><!--顺序：--><input name='orders' size='3' value='1' style='width:20px' type='hidden' />&nbsp;<input name='nextUsers' checked type='checkbox' value='" + uNameAry[i] + "' style='display:none'><span class='userRealName'>" + uRealNameAry[i] + "</span>&nbsp;&nbsp;到期时间：<input name='expireHours' size=2 value=0>小时<span style='display:none'><input name='toMes' type='checkbox' value=1>处理完交办给我</span>&nbsp;&nbsp;<span style='display:none'>&nbsp;&nbsp;<a href='javascript:;' onclick='up(this)' title='上移'>↑</a>&nbsp;&nbsp;<a href='javascript:;' onclick='down(this)' title='下移'>↓</a>&nbsp;&nbsp;</span><a href='javascript:;' title='删除' onclick='$(this).parent().remove();$(\".flowright\").height($(\".flowleft\").height());' style='font-size:16px; font-color:red'>×</</div>";
			
		}
		if (atUsers=="") {
			atUsers = "@" + uNameAry[i] + " ";
			users = uNameAry[i];
			userRealNames = uRealNameAry[i];
		}
		else {
			atUsers += "@" + uNameAry[i] + " ";
			users += "," + uNameAry[i];
			userRealNames += "," + uRealNameAry[i];
		}
	}
	$(".flowright").height($(".flowleft").height());//实时调整flowright的高度			
	// 判断原来显示的用户及到期时间是否还存在于@用户中，如果不存在，则删除
	$("[id^=nextUsersDiv]").each(function() {
	   var isFound = false;
	   for (var i=0; i<uNameAry.length; i++) {
		   if ($(this).attr("userName")==uNameAry[i]) {
			  isFound = true;
			  break;
		   }
	   }
	   if (!isFound) {
	   	$(this).remove();
	   }
	});		
		
	o("nextActionUsers").value = users;
	o("userRealNames").value = userRealNames;
	
	o('flowForm').submit();
	return;
	
	/*******************************/
	
	// 如果是在input事件中检测
	if (isOnInput)
		return;
	
	insertCkeditorText(atUsers);

	/*
	// 在备注框中插入@用户
	var cont = o("cwsWorkflowResult");
	var oRange = o("cwsWorkflowResult").createTextRange();
    cont.focus();
	var contlen = cont.value.length;

    if(typeof document.selection != "undefined") {
        document.selection.createRange().text = atUsers;
    }
    else {
        cont.value = cont.value.substr(0, cont.selectionStart) + atUsers + cont.value.substring(cont.selectionStart,contlen);
    }

	if (oRange.findText(atUsers)!=false) {
	   oRange.select();
	}	
	*/
}

function atUser(userName) {
	insertCkeditorText(" @" + userName + " ");	
}

function insertCkeditorText(str) {
    var oEditor = CKEDITOR.instances.cwsWorkflowResult;
	if (!oEditor)
		return;
    if ( oEditor.mode == 'wysiwyg' ) {
        oEditor.insertText( str );
    }
    else
        alert( '<lt:Label res="res.flow.Flow" key="wysiwyg"/>' );
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
});

function atWorkmate() {
	openWinUsers();
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
                initTree();
        };
 
        addEvent('propertychange', change);
        addEvent('input', change);
        addEvent('focus', change);
        change();
        
};
</script>
<style>
.mainBox {
	width: 90%;
	margin: 0 auto;
}
.rightBox {
	width: 300px;
	float: right;
}
.leftBox {
	margin-right: 310px;
}
#taskListBox {
	text-align: left;
}

.myTextarea{ 
    display: block;
    margin:8px auto;
    overflow: hidden; 
    width: 100%; 
    font-size: 14px;
    line-height: 24px;
    text-indent:1em;
    height:48px;
    border: solid 1px #ffa200;
    -moz-border-radius: 5px;
	-webkit-border-radius: 5px;
	border-radius: 5px;
	behavior: url(skin/commom/ie-css3.htc);
}

#upfilePanelShow{
	width:60%;
	-moz-border-radius: 5px;
	-webkit-border-radius: 5px;
	border-radius: 5px;
	position: relative;
	padding-left: 10px;
	behavior: url(skin/commom/ie-css3.htc);
	background-color: #dbe2f5;
	line-height:25px;
}
</style>
</head>

<body onLoad="window_onload()" class="flowbodybg" id="flowbodybg">
<!--@流程开始-->
<div id="bodyBox" class="flowbgfram">
  <div class="flowwhitebg" id="flowwhitebg">
    <div class="flowleft" >
      <form id="flowForm" name="flowForm" action="flow_dispose_free_do.jsp?action=deliver&flowId=<%=flowId%>" method="post">
        <%
        if (!wf.isStarted()) {
        %>
        <input id="cwsWorkflowTitle" name="cwsWorkflowTitle"
								type="hidden" value="<%=StrUtil.HtmlEncode(wf.getTitle())%>"
								style="border: 1px solid #cccccc; color: #888888" size="40" />
        &nbsp;
        <%}%>
<%
UserDb user = new UserDb();

String smschk = "";
String newschk = "";

boolean flowAutoMsgRemind = cfg.getBooleanProperty("flowAutoMsgRemind");

if (!flowAutoMsgRemind){
	newschk = "";
}else{
	newschk = "checked";
}

String myUserName = privilege.getUser(request);

// 如果本人且是发起人
if (myUserName.equals(wf.getUserName())) {
%>  
        <div class="leftbox-1">
          <div class="box" style="margin-bottom:5px;">
          	<!-- <span class="span1"><lt:Label res="res.flow.Flow" key="publicationTasks"/></span> -->
             <span id="projectName"> <%
 	if (wf.getProjectId() != -1) {
 		com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
 		FormDb prjFd = new FormDb();
 		prjFd = prjFd.getFormDb("project");
 		fdao = fdao.getFormDAO((int) wf.getProjectId(), prjFd);
 %> <lt:Label res="res.flow.Flow" key="project"/>：<a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("name")%>', 'project/project_show.jsp?projectId=<%=wf.getProjectId()%>&formCode=project')"><%=fdao.getFieldValue("name")%></a>
    &nbsp;&nbsp;<a title="<lt:Label res='res.flow.Flow' key='disassociate'/>" href="javascript:;" onclick="unlinkProject()" style='font-size:16px; font-color:red'>×</a>
				<%
					}
				 %> 
              </span>
            <div class="span2">
            <!--
              <input name="" type="checkbox" value="" />
              -->
			<%
              if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
                  boolean flowAutoSMSRemind = cfg
                          .getBooleanProperty("flowAutoSMSRemind");
                  smschk = "checked=\"checked\"";
                  if (!flowAutoSMSRemind)
                	  smschk = "";
            %>
            <input id="isToMobile" name="isToMobile" value="true"
                type="checkbox" <%=smschk%> />
            <img src="images/@flowico_7.png" width="23" height="23" /> <lt:Label res="res.flow.Flow" key="sms"/>
			<%
              }
            %>
            </div>
              <input id="isUseMsg" name="isUseMsg" value="true" style="display:none"
                  type="checkbox" <%=newschk%> />
              <!-- <div class="span3">
              <img src="images/@flowico_8.png" width="23" height="23" /> <lt:Label res="res.flow.Flow" key="news"/></div> -->
              <span id="spanLoad2"></span>
            </div>
          <%
         	 boolean isProjectUsed = cfg.get("isProjectUsed").equals("true");
          %>
          
          <!--功能图标-->
          <div class="icobox">
          <span onclick="atWorkmate()"><img src="images/@flowico_1.png" width="23" height="23" /> <lt:Label res="res.flow.Flow" key="colleague"/></span>
          <span class="emotion"><img src="images/@flowico_2.png" width="23" height="23" /> <lt:Label res="res.flow.Flow" key="phiz"/></span>
          <span onclick="linkProject()" style='<%=isProjectUsed?"":"display:none"%>'><img src="images/@flowico_3.png" width="23" height="23" /> <lt:Label res="res.flow.Flow" key="project"/></span>  
          <span onmouseover="tipPhrase('phraseBox', this)"><img src="images/@flowico_13.png" width="23" height="23" /> <lt:Label res="res.flow.Flow" key="commonUse"/></span>             
          </div>
          <!--文本输入框-->
          <div class="textbox">
            <!-- <textarea id="cwsWorkflowResult" name="cwsWorkflowResult" cols="" rows="6"></textarea>-->
            <textarea id="cwsWorkflowResult" name="cwsWorkflowResult"></textarea>
            <input type="hidden" id="hiddenObj" name="hiddenObj" />
            
		<script>
		<%
		String sql = "select name from users where isvalid=1";
		Iterator ir = user.list(sql).iterator();
		StringBuffer userNames = new StringBuffer();
		StringBuffer checkNames = new StringBuffer();	
		StringBuffer realNames = new StringBuffer();
		while (ir.hasNext()) {
			user = (UserDb)ir.next();
			if (user.getName().equals("system")) {
				continue;
			}			
			if (userNames.length()==0) {
				userNames.append("{name:'" + user.getName() + "', realName:'" + user.getRealName() + "'}");
				checkNames.append(user.getName());		
				realNames.append(user.getRealName());
			}
			else {
				userNames.append(",{name:'" + user.getName() + "', realName:'" + user.getRealName() + "'}");
				checkNames.append(","+user.getName());	
				realNames.append("," + user.getRealName());
			}
		}
		
		%>
		var issues = [<%=userNames%>];
		var names = "<%=checkNames%>";
		var namesArr = names.split(",");
		var realnamesArr = "<%=realNames%>".split(",");
		
		var at_config = {
			at: "@",
			'limit': 10,
			tpl: '<li data-value="@${name}">${name} <small>(${realName})</small></li>',
			data: issues
		}		
		
		/*CKEDITOR.config.removePlugins = 'elementspath';
		CKEDITOR.config.resize_enabled = false;
		
		var editor = CKEDITOR.replace('cwsWorkflowResult',
		{
			// skin : 'kama',
			toolbar : 'Simple',
			height: '160px'
		});
		editor.enableEnter = true; //Use this as a flag
		editor.on('instanceReady', function(event) {
			// Make sure the textarea's `contentEditable` property is set to `true`
			this.document.getBody().$.contentEditable = true;
			$(this.document.getBody().$)
			.atwho('setIframe', this.window.getFrame().$)
			.atwho(at_config);
			
			// 获取焦点
			this.focus();
			
			// 将光标移至末尾			
			var range = editor.createRange();
			range.moveToElementEditEnd( range.root );
			editor.getSelection().selectRanges( [ range ] );			
			
			// Prevent adding a new line when pressing ENTER			
			$(this.document.getBody().$).on('shown.atwho', function(event){
				editor.enableEnter = false;
			});
			$(this.document.getBody().$).on('hidden.atwho', function(event){
				setTimeout(function(){
					editor.enableEnter = true;
				},100); //Give it a small time so that the ENTER key only affects the popup and not the editor
				
				var html = editor.document.getBody().getHtml();
				parseAtWho(html);				
			});
			event.editor.on( 'key', function( event ) {
				if ( event.data.keyCode == 13 && !editor.enableEnter ) {
					event.cancel();
				}
			});				
			// 当输入时
			event.editor.on('change', function () {
				if (!event.editor.unsaved && event.editor.checkDirty()) {					
					var val = this.document.getBody().getHtml();
					parseAtWho(val);
				}
			});		
			
			var html = this.document.getBody().getHtml();	
			parseAtWho(html);
		});		
			
		function parseAtWho(val) {
			// var reg = /@(.*?)[ |　]/gi;
			var reg = /@(.*?)[&|<| |　]/gi; // 可能会出现以下情况@test &nbsp; @test</span>
			var users = "";
			var rnames = "";
			hasError = false;
			while(r=reg.exec(val)){
				if (r[1]=="")
					continue;
				var flag = false;
				var realName = "";
				var n = -1;
				for(var j=0;j<namesArr.length;j++)
				{
					if(namesArr[j] == r[1]){
						flag = true;
						realName = realnamesArr[j];
						n = j;
						break;
					}
				}
				if (!flag)
				{
					hasError = true;
					break;
				}
				if (users=="") {
					users = r[1];
					rnames = realnamesArr[n];
				}
				else {
					if (("," + users + ",").indexOf("," + r[1] + ",")==-1) {
						users += "," + r[1];
						rnames += "," + realnamesArr[n];					
					}
				}
			}
			setUsers(users, rnames, true);
		}*/
				
		/*
		$(function() {
			$('#cwsWorkflowResult').atwho({
				at: "@",
				'limit': 10,
				tpl: '<li data-value="@${name}">${name} <small>(${realName})</small></li>',
				data: issues
			})
		});
		
		$("#cwsWorkflowResult").on('input',function(e){  
			var reg = /@(.*?)[ |　]/gi;
			var val = $(this).val();
			var users = "";
			hasError = false;
			while(r=reg.exec(val)){
				// console.log(r.index+':'+r[0]);
				var flag = false;
				for(var j=0;j<namesArr.length;j++)
				{
					if(namesArr[j] == r[1]){
						flag = true;
						break;
					}
				}
				if (!flag)
				{
					hasError = true;
					break;
				}
				if (users=="") {
					users = r[1];
				}
				else {
					users += "," + r[1];
				}
			}
			setUsers(users, users, true);

		});		
		*/
		
		$(function() {
			$('.emotion').qqFace({ 
					assign:'cwsWorkflowResult', //给输入框赋值 
					path:'forum/images/emot/'    //表情图片存放的路径 
				}); 
		});
		</script>       
  			<%@ include file="inc/tip_phrase.jsp"%>
          </div>
          <div class="button" onclick="deliver();"><a href="javascript:;" ><lt:Label res="res.flow.Flow" key="submit"/></a></div>
          <div class="button" onclick="deliver();"><a href="javascript:;" ><lt:Label res="res.flow.Flow" key="submit"/></a></div>
          <div id="nextActionUserDiv" class="nextActionUserDiv"></div>          
        </div>
      
      <div>
		<script>initUpload();</script>
		<input type="hidden" name="flowId" value="<%=flowId%>" />
        <input type="hidden" name="isFlowModified" value="0" />
        <textarea name="formReportContent" style="display: none"></textarea>
												
      </div>
      
<%}%>
<%
	int doc_id = wf.getDocId();
	DocumentMgr dm = new DocumentMgr();
	Document doc = dm.getDocument(doc_id);
	user = user.getUserDb(privilege.getUser(request));
%>      
      
      <!--<div class="leftbox-2"><a href="#">请点击此处添加文件</a></div>-->
        <textarea name="userRealNames" cols="38" rows="3" readOnly
            wrap="yes" id="userRealNames" style="display: none"></textarea>
        <input type="hidden" id="nextActionUsers" name="nextActionUsers">
        <div id="dlgReturn" style="display: none">
        </div>
        <input type="hidden" name="op" value="saveformvalue" />
        <span id="spanLoad"></span>
		<div id="netdiskFilesDiv" class="percent98" style="line-height: 1.5; text-align: left"></div>
        <input name="returnBack" value="<%=wf.isReturnBack() ? "true" : "false"%>" type=hidden>
      </form>
    <%
    com.redmoon.forum.person.UserDb ud = new com.redmoon.forum.person.UserDb();
    
    MyActionDb rootMyAction = null;
    MyActionDb mad = new MyActionDb();
	String processListSql = "select id from flow_my_action where flow_id="
			+ flowId
			+ " order by receive_date asc";
	
	Vector vProcess = mad.list(processListSql);
	Iterator ir = vProcess.iterator();
	if (ir.hasNext()) {
		  rootMyAction = (MyActionDb) ir.next();
	}
	String flow_name = "";
	if (rootMyAction!=null) {
		user = um.getUserDb(rootMyAction.getUserName());
	    ud = ud.getUser(user.getName());
	    flow_name = user.getName();
	}%>
  	  </div>
     <div class="flowright" id="flowright">
      	<div class="task" >
      		<div class="enclosure">
      			<img src="images/@flowico_9.png" width="23" height="23" />
      			<lt:Label res="res.flow.Flow" key="attachmentColumn"/>
      		</div>
     		<div id="attDiv" style="clear:both;"></div>      
        </div>
	</div>
  </div>
<div style="clear:both"></div>
<div class="flowmodeswitch">
<%if (myname.equals(wf.getUserName()) && flowStatus !=0) {%>
<div class="flowmodeswitchAtBtn">
<span onclick="atWorkmate()" title="<lt:Label res="res.flow.Flow" key="supply"/>"><img src="images/@flowico_1.png" width="23" height="23" /><lt:Label res="res.flow.Flow" key="colleague"/></span>
</div>
<%}%>
<div class="flowmodeswitchbtn">
	<%if (isTreeStyle) {%>
	<p href="javascript:;" style="color:white;" onclick="window.location.href='flow_dispose_light_show.jsp?flowId=<%=rootMyAction.getFlowId()%>&isTreeStyle=false'"><lt:Label res="res.flow.Flow" key="flatmode"/></p>
	<%}else{%>
	<p href="javascript:;" style="color:white;" onclick="window.location.href='flow_dispose_light_show.jsp?flowId=<%=rootMyAction.getFlowId()%>&isTreeStyle=true'"><lt:Label res="res.flow.Flow" key="treemode"/></p>
	<%}%>
</div>  
</div> 
<div class="content" id="content">
	  <div class="wrapper">
	    <div class="light"><i></i></div>
	    <div class="main">
	      <h1 class="title"><lt:Label res="res.flow.Flow" key="flowProcessShow"/></h1>
	      <%
      		String cls = "";
			int k = 0;
			if (!isTreeStyle) {
		          user = um.getUserDb(rootMyAction.getUserName());
		          ud = ud.getUser(user.getName());
			%>
		      <div class="year" id="year<%=rootMyAction.getId()%>">
		        <h2><a href="javascript:;" title="<lt:Label res="res.flow.Flow" key="sign"/>
				<%=DateUtil.format(rootMyAction.getReceiveDate(), "MM-dd HH:mm")%>">
				<%if (!isTreeStyle) {%><%=DateUtil.format(rootMyAction.getReceiveDate(), "MM-dd")%><%}else{%><%=user.getRealName()%><%}%>
		        <i></i></a></h2>
		        <div class="list">
		          <ul style="width:900px;">
	      	<%}%> 
		  	<%
		  	WorkflowAnnexDb wad = new WorkflowAnnexDb();
		  	ir = vProcess.iterator();
		  	String myRealName = "";
	      	while (ir.hasNext()) {
          		MyActionDb pmad = (MyActionDb) ir.next();
         		user = um.getUserDb(pmad.getUserName());
          		ud = ud.getUser(user.getName());
          		k++;
		  		String highlight = "";
		  		if (mad.getId()==pmad.getId()) {
			  		highlight = "highlight";
			 	}
		  		
		  		if(user.getName().trim().equals(myname)){
					  myRealName = user.getRealName();
				}
		  		
		  		boolean defautHeight = false;;
			    if(StrUtil.getAbstract(request,MyActionMgr.renderResult(request, pmad),500).length()>200){
					defautHeight = true;
			    }
			    
			    String imgSrc = "";
			    if (StrUtil.getNullStr(user.getPhoto()).equals("")) {
			    	imgSrc = "forum/images/face/";
			    	if (StrUtil.getNullStr(user.getPicture()).equals("")) {
			    		if (StrUtil.getNullStr(ud.getMyface()).equals("")) {
			    			if (ud.getRealPic().toLowerCase().endsWith(".gif") || 
			    					ud.getRealPic().toLowerCase().endsWith(".png") ||
			    					ud.getRealPic().toLowerCase().endsWith(".jpg") ||
			    					ud.getRealPic().toLowerCase().endsWith(".bmp")) {
			    				imgSrc = imgSrc + ud.getRealPic();
			    			} else {
			    				imgSrc = imgSrc + "face.gif";
			    			}
			    		} else {
			    			imgSrc = ud.getMyfaceUrl(request);
			    		}
			    	} else {
			    		if (user.getPicture().toLowerCase().endsWith(".gif") || 
			    				user.getPicture().toLowerCase().endsWith(".png") ||
			    				user.getPicture().toLowerCase().endsWith(".jpg") ||
			    				user.getPicture().toLowerCase().endsWith(".bmp")) {
				    		imgSrc = imgSrc + user.getPicture();
		    			} else {
		    				imgSrc = imgSrc + "face.gif";
		    			}
			    	}
			    } else {
			    	imgSrc = user.getPhoto();
			    }
			%>
	      	<%if ( isTreeStyle && (pmad.getPrivMyActionId()==-1 || pmad.getPrivMyActionId()==rootMyAction.getId()) ) {%>
		      <div class="year" id="year<%=pmad.getId()%>">
		        <h2><a href="javascript:;" title="<lt:Label res="res.flow.Flow" key="sign"/>
				<%=DateUtil.format(pmad.getReceiveDate(), "MM-dd HH:mm")%>">
				<%if (!isTreeStyle) {%><%=DateUtil.format(pmad.getReceiveDate(), "MM-dd")%><%}else{%><%=user.getRealName()%><%}%>
		        <i></i></a></h2>
		        <div class="list">
		        	<input type="hidden" name="replyToName" value="<%=pmad.getUserName() %>" />
		          <ul >
		      <%}%> 
		            <li id="detail<%=pmad.getId()%>" class="cls <%=highlight%>" >
		              <p class="date"><%=DateUtil.format(pmad.getReceiveDate(), "MM-dd HH:mm")%></p>
		              <p id="intro<%=pmad.getId()%>" class="intro">
						<a href="javascript:;" onclick="addTab('<%=user.getRealName()%>', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><img src="<%=imgSrc %>" width="44" height="44" /></a>
						<a href="javascript:;" title="@<%=user.getName()%>" onclick="insertCkeditorText(' @<%=user.getName()%> ')"><%=user.getRealName()%></a>
						<span title="<lt:Label res="res.flow.Flow" key="handleTime"/>"><%=DateUtil.format(pmad.getCheckDate(), "MM-dd HH:mm")%></span>
				  		<%
						//if (mad.getId()==pmad.getId()) {
						%>
						<!-- &nbsp;&nbsp;<a href="#editorTag" onclick="editor.focus();"><lt:Label res="res.flow.Flow" key="suggestion"/></a> -->
						<%
						//}
						String myActionResult = MyActionMgr.renderResult(request, pmad);
				  		// if((pmad.getCheckStatus()!=MyActionDb.CHECK_STATUS_NOT || !pmad.isReaded()) && flowStatus != 2){
						%>
						<a class="comment" href="javascript:;" onclick="addMyReply('<%=pmad.getId()%>')"><img style="margin-top:9px;" title="<lt:Label res="res.flow.Flow" key="replyTo"/>" src="images/dateline/replyto.png"/></a>
              			<%// } %>
		              </p>
		              <p class="version">&nbsp;</p>
		              <div id="remark<%=pmad.getId()%>" class="more">
		              	<div class="suggestionStyle" >
		                <%if (pmad.getCheckStatus()!=MyActionDb.CHECK_STATUS_NOT) {%>
						<%=myActionResult.trim().equals("") ? "&nbsp;" : myActionResult%>
		                <%}%>
		                </div>
		                <script>
		                	<%if(pmad.getCheckStatus()!=MyActionDb.CHECK_STATUS_NOT){
		                	if(defautHeight){%>
		                		var allP = $("#remark"+"<%=pmad.getId()%>").children("div").first().children("p");
		                		var suggestionP = allP.first();
		                		//$("#remark"+"<%=pmad.getId()%>").children("div").first().height("110px");
		                		//在suggestionP的后面加入下拉的图标
		                		var imgNode = $('<img src="images/dateline/suggestion_up.png" onclick="allSuggestions(this)" style="cursor:pointer;margin-top:2px;margin-left:4px;display:block;" title="<lt:Label res='res.flow.Flow' key='expansion'/>"/>');
		                		suggestionP.after(imgNode);
		                		suggestionP.css("width","96%");
		                		suggestionP.css("text-overflow","ellipsis");
		                		suggestionP.css("float","left");
		                		//suggestionP.css("overflow","hidden");
		                		allP.each(function(){
		                			$(this).css("width","96%");
		                		});
		                	<%}}else{%>
                				var newP = "";
		                		<%if(pmad.isReaded()){
		                			if(pmad.getUserName().equals(myname)){%>
		                				newP = $('<p style="color:#EC6A13;"><img style="padding-top:1px;" src="images/dateline/waiting_handle2.png"/>&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="signed"/>&nbsp;:&nbsp;<%=DateUtil.format(pmad.getReadDate(), "MM-dd HH:mm")%></p>');
		                			<%}else{%>
		                				newP = $('<p ><img style="padding-top:1px;" src="images/dateline/waiting_handle2.png"/>&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="signed"/>&nbsp;:&nbsp;<%=DateUtil.format(pmad.getReadDate(), "MM-dd HH:mm")%></p>');
		                				<%}
		                		}else{
		                			if(pmad.getUserName().equals(myname)){%>							
		                				newP = $('<p style="color:#EC6A13;"><img style="padding-top:1px;" src="images/dateline/waiting_handle.png"/>&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="waitingSign"/></p>');
		                			<%}else{%>
		                				newP = $('<p ><img style="padding-top:1px;" src="images/dateline/waiting_handle.png"/>&nbsp;&nbsp;<lt:Label res="res.flow.Flow" key="waitingSign"/></p>');
		                			<%}
		                		}%>
		                		$("#remark"+"<%=pmad.getId()%>").children("div").first().append(newP);
		                	<%}%>		                	
		                </script>
		                <p class="commentup" style="display:none;"></p>
		                <div class="replyDiv" id="replyDiv<%=pmad.getId()%>" style="display:none;">
		                <%
	                	String sqlReply = "select id from flow_annex where flow_id=? and action_id=? order by add_date asc";
						Vector vwad = wad.list(sqlReply, new Object[]{new Long(pmad.getFlowId()), new Long(pmad.getActionId())});					
						// System.out.println(getClass() + " " + vwad.size());               		
						Iterator ir1 = vwad.iterator();
	             		String user_name = "";
	             		String reply_name = "";
	             		boolean isSecret = false;
	             		UserDb send_user = null;
						while(ir1.hasNext()) {
							wad = (WorkflowAnnexDb)ir1.next();
							user_name = wad.getString("user_name");
							reply_name = wad.getString("reply_name");
							send_user = um.getUserDb(user_name);
							isSecret = wad.getBoolean("is_secret");
							if(isSecret && (!myname.equals(user_name) && !myname.equals(reply_name))) {
							}else{
								String content = wad.getString("content");
								content=content.replace("\n","<br/>");
								%>
									<script>
										$("#replyDiv"+"<%=pmad.getId()%>").show();
										$("#replyDiv"+"<%=pmad.getId()%>").prev(".commentup").show();
									</script>
	                                <p style="margin-top:10px;"><%=send_user.getRealName() %>&nbsp;&nbsp;&nbsp;&nbsp;<%=DateUtil.format(wad.getDate("add_date"), "yyyy-MM-dd HH:mm:ss")%></p>
	                                <p style="padding-left:10px">&nbsp;&nbsp;&nbsp;&nbsp;<%=content%></p>
	                        	<%
							}
						}
						%>
					</div>
						<div id="myReplyTextarea<%=pmad.getId()%>" style="display:none; clear:both;position:relative;">
		                	<form id="flowForm<%=pmad.getId()%>" name="flowForm<%=pmad.getId()%>" action="flow_dispose_free_do.jsp?action=addReply" method="post">
								<textarea name="content" id="get<%=pmad.getId()%>" class="myTextarea"></textarea>
								<span title="<lt:Label res='res.flow.Flow' key='othersHidden'/>" style="cursor:pointer;" onclick="chooseHideComment(this);"><img src="<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png" />&nbsp;<lt:Label res='res.flow.Flow' key='needHidden'/><input type="hidden" name="isSecret" value="0"/></span>
                            	<input type="hidden" name="myActionId" value=""/>
								<input type="hidden" name="discussId" value="<%=pmad.getId() %>"/>
								<input type="hidden" name="flow_id" value="<%=pmad.getFlowId() %>"/>
								<input type="hidden" name="action_id" value="<%=pmad.getActionId() %>"/>
								<input type="hidden" name="userRealName" value="<%=myUser.getRealName()%>"/>
								<input type="hidden" name="user_name" value="<%=myname%>"/>
								<input type="hidden" name="reply_name" value="<%=user.getName()%>"/>
								<input type="hidden" name="flow_name" value="<%=flow_name%>"/>
								<input class="mybtn" type="button" value="<lt:Label res='res.flow.Flow' key='sure'/>" onclick="submitPostscript('<%=pmad.getId()%>')"/>
							</form>
						</div>
		              </div>
		            </li>
				<%if ( isTreeStyle && (pmad.getPrivMyActionId()==-1 || pmad.getPrivMyActionId()==rootMyAction.getId()) ) {%>
			          </ul>
			        </div>
			      </div>
			      <%}%>
		     	 <%}%>
		       <%if (!isTreeStyle) {%>
	          </ul>
	        </div>
	      </div>          
	      <%}%>
	    </div>
	   </div>
	</div>
	
</div>
</body>
<script>
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

function addMyReply(id) {
	if($("#myReplyTextarea"+id).is(":hidden")){
		$("#myReplyTextarea"+id).show();
		$("#get"+id).focus();
		autoTextarea($("#get"+id).get(0));
	}else{
		$("#myReplyTextarea"+id).hide();
	}
	initTree();
}

function submitPostscript(textareaId){
	var textareaContent = $("#get"+textareaId).val();//“评论”文本框的内容
	if(textareaContent == ""){
		alert("<lt:Label res='res.flow.Flow' key='reviewContent'/>");
	}else{
		$.ajax({
			type: "post",
			url: "flow_dispose_free_do.jsp?action=addReply",
			data : $("#flowForm"+textareaId).serialize(),
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$('#bodyBox').showLoading();
				$('#loading-indicator-bodyBox-overlay').height($('#flowbodybg').height());
				$('#loading-indicator-bodyBox').css({'bottom':'350px','top':''});
			},
			complete: function(XMLHttpRequest, status){
				$("#get"+textareaId).height("48px");
				$('#bodyBox').hideLoading();	
			},
			success: function(data, status){
				var re = $.parseJSON(data);
				if (re.ret=="1") {
					$("#replyDiv"+textareaId).show();
					$("#replyDiv"+textareaId).prev(".commentup").show();
					var node1 = "<p style='margin-top:10px;'><%=myRealName%>"+"&nbsp;&nbsp;&nbsp;&nbsp;"+re.myDate+"</p>";
					var reg= new RegExp("\n","g");
					textareaContent=textareaContent.replace(reg,"<br/>");
					var node2 = "<p style='padding-left:10px;'>&nbsp;&nbsp;&nbsp;&nbsp;"+textareaContent+"</p>";
					var p1 = $(node1);
					var p2 = $(node2);
					$("#replyDiv"+textareaId).append(p1);
					$("#replyDiv"+textareaId).append(p2);
					$("#get"+textareaId).val("");
					var myImg = $("#flowForm"+textareaId).children("span").children("img");
					var myInput = $("#flowForm"+textareaId).children("span").children("input");
					if(myImg.attr("src").indexOf("checkbox_sel") != -1){//现在是“显示”状态
						myImg.attr("src","<%=SkinMgr.getSkinPath(request)%>/images/admin/functionManage/checkbox_not.png");
						myInput.val("0");
					}
					$("#myReplyTextarea"+textareaId).hide();
					initTree();
				}	
			},
			error: function(){
				alert('<lt:Label res="res.flow.Flow" key="replyWrong"/>');
			}
		});
		
	}
}
function allSuggestions(obj){
	var nextP = $(obj).prev("p");
	
	if(nextP.css("overflow") == "hidden"){
		nextP.css("word-break","break-all");
		nextP.css("overflow","");
		obj.src = "images/dateline/suggestion_up.png";
		obj.title = "<lt:Label res='res.flow.Flow' key='collapse'/>";
		$(obj).parent(".suggestionStyle").height("");
	}else{
		nextP.css("word-break","");
		nextP.css("overflow","hidden");
		obj.src = "images/dateline/suggestion_down.png";
		obj.title = "<lt:Label res='res.flow.Flow' key='expansion'/>";
		$(obj).parent(".suggestionStyle").height("48px");
	}
	initTree();
}
</script>

<%if (wf.isStarted()) {%>
    <script>
	$(function() {
		<%if (isTreeStyle) {
			// 排序
			JSONArray treeAry = MyActionDb.getTaskTreeSorted(wf.getId());
			for (int i=1; i<treeAry.length(); i++) {
				String thisId = treeAry.getJSONObject(i).getString("id");
				String parentId = treeAry.getJSONObject(i).getString("parent");
				int layer = treeAry.getJSONObject(i).getInt("layer");
				%>
				// $('#year<%=parentId%>').after($('#year<%=thisId%>')[0]);
				if (!o('year<%=thisId%>')) {
					$('#detail<%=parentId%>').after($('#detail<%=thisId%>')[0]);
				}
				
				$("#intro<%=thisId%>").css("paddingLeft", <%=layer*30%>);
				$("#remark<%=thisId%>").css("paddingLeft", <%=layer*30%>);
				<%
			}
		}%>
		initTree();
	});
	
	</script>
	<%}%>

	<script>
$(function (){
	$(window).goToTop({
		showHeight : 1,//设置滚动高度时显示
		speed : 500 //返回顶部的速度以毫秒为单位
	});
	
	// 如果本人不是发起人，需要修改我的流程的样式
	$(".flowleft").hide();
	$("#flowwhitebg").removeClass("flowwhitebg");
	$("#flowwhitebg").addClass("flowwhitebg1");
	$("#flowright").removeClass("flowright");
	$("#flowright").addClass("flowright1");
});

function initTree() {
		$(".main .year .list").each(function (e, target) {
			var $target = $(target),
				$ul = $target.find("ul");
			$target.height($ul.outerHeight()), $ul.css("position", "absolute");
		}); 
		$(".main .year>h2>a").click(function (e) {
			e.preventDefault();
			$(this).parents(".year").toggleClass("close");
		});
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
		
		if (result=='<lt:Label res="res.common" key="info_op_success"/>') {
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
		
		if (result=='<lt:Label res="res.common" key="info_op_success"/>') {
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

function refreshAttachments() {
	ajaxpage("<%=Global.getFullRootPath(request)%>/flow_dispose_light_ajax_att.jsp?flowId=<%=flowId%>", "attDiv");
	o("netdiskFilesDiv").innerHTML = "";
}

// 用于massValidate检查表单内容
var lv_cwsWorkflowResult = new LiveValidation('hiddenObj');

function saveDraft() {
	/*
	if (!LiveValidation.massValidate(lv_cwsWorkflowResult.formObj.fields)) {
		jAlert("请检查表单中的内容填写是否正常！", "提示");
		return;
	}		
	*/
	var fields = lv_cwsWorkflowResult.formObj.fields;
	// 取消验证
	LiveValidation.cancelValidate(lv_cwsWorkflowResult.formObj.fields);
		
	o('op').value = "saveformvalue";
	if (o('flowForm').onsubmit) {
		if (o('flowForm').onsubmit()) {		
		
			// o('deliverForm').submit();
			
			var html = CKEDITOR.instances.cwsWorkflowResult.document.getBody().getHtml();	
			o("cwsWorkflowResult").value = html;
						
			$('#bodyBox').showLoading();
			$('#flowForm').submit();
		}
	}
	
	// 恢复验证
	LiveValidation.restoreValidate(fields);	
}

// 如果不注释掉，则当直接在桌面点击时，无法载入
// $(document).ready(function() {
  refreshAttachments();
// });

function selectNetdiskFile() {
	openWin('netdisk/netdisk_frame.jsp?mode=select', 800, 600);
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
		return;
	}
	
	if (data==null)
		return;
	
	data.msg = data.msg.replace(/\\r/ig, "<BR>");
	
	if (data.ret=="0") {
		jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
		return;
	}
	
	var op = data.op;
	if (op=="saveformvalue") {
		jAlert(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>');
		refreshAttachments();
		delAllUploadFile();
		return;
	}
	else if (op=="manualFinish") {
		jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
	}
	else if (op=="finish") {
		var nextMyActionId = data.nextMyActionId;
		if (nextMyActionId!="") {			
			jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow_dispose_light.jsp?myActionId=" + nextMyActionId);
		}
		else
			jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
	}
	else if (op=="return") {
		jAlert_Redirect(data.msg, '<lt:Label res="res.flow.Flow" key="prompt"/>', "flow/flow_list.jsp?displayMode=1");
	}
	
	return;
}

function showError(pRequest, pStatus, pErrorText) {
	$('#bodyBox').hideLoading();
	alert(pRequest);
	alert('pStatus='+pStatus+'\r\n\r\n'+'pErrorText='+pErrorText);
}


function showTab(obj)
{
   if (1==obj)
   {
       $("#details_ID").hide()
       //$("#detailsboxID").show();
      
      $("#selectID").removeClass("normol");
      $("#selectID").addClass("select");
      
      $("#normolID").removeClass("select");
      $("#normolID").addClass("normol");
   }
   else if(2==obj)
   {
      
      $("#details_ID").show();
      //$("#detailsboxID").hide()
      
      $("#normolID").removeClass("normol");
      $("#normolID").addClass("select");
      
      $("#selectID").removeClass("select");
      $("#selectID").addClass("normol");
      
   }
}
 
function delAtt(docId, attId) {
	if (confirm('<lt:Label res="res.flow.Flow" key="isDelete"/>')) {
		$.getJSON('flow_dispose_ajax_att.jsp', 
			{
			"op":"delAttach",
			"flowId":"<%=flowId%>",
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
}

</script>
</html>
