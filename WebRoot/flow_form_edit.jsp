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
<%@ page import = "org.json.*"%>
<%
String op = ParamUtil.get(request, "op");
int flowId = ParamUtil.getInt(request, "flowId");

MyActionDb mad = new MyActionDb();
mad = mad.getLastMyActionDbOfFlow(flowId);
if (mad==null) {
	out.print(SkinUtil.makeErrMsg(request, "该待办流程记录不存！"));
	return;
}

long myActionId = mad.getId();

WorkflowMgr wfm = new WorkflowMgr();
WorkflowDb wf = wfm.getWorkflowDb(flowId);
Leaf lf = new Leaf();
lf = lf.getLeaf(wf.getTypeCode());

FormDb fd = new FormDb();
fd = fd.getFormDb(lf.getFormCode());

if ("save".equals(op)) {
	com.redmoon.oa.visual.FormDAOMgr fdaoMgr = new com.redmoon.oa.visual.FormDAOMgr(fd);
	JSONObject json = new JSONObject();
	try{
		boolean re = fdaoMgr.update(application, request);
		
		if (re) {
			json.put("ret", "1");
			String str = LocalUtil.LoadString(request,"res.common","info_op_success");
			json.put("msg", str);
			json.put("op", "editFormValue");
		}
		else {
			json.put("ret", "0");
			String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
			json.put("msg", str);
			json.put("op", "editFormValue");
		}
		out.print(json);
	}catch(Exception e){
		json.put("ret", "0");
        String str = LocalUtil.LoadString(request,"res.common","info_op_fail");
        json.put("msg", str + ":" + e.getMessage());
        json.put("op", "editFormValue");
        out.print(json);
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<style>
#checker ul {
text-align:left;
padding:2px 0px;
clear:both;
}
#checker ul li {
list-style-type: none;
float:left;
font-size:12px;
width:140px;
padding-right:6px;
padding-top:8px;
}
</style>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<script src="<%=request.getContextPath()%>/inc/livevalidation_standalone.js"></script>

<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String myname = privilege.getUser( request );

// 如果对流程没有管理权限，则抛出权限非法异常
/*
LeafPriv lp = new LeafPriv();
lp.setDirCode(lf.getParentCode());
if (!lp.canUserSee(privilege.getUser(request))) {
	out.print(SkinUtil.makeErrMsg(request, "流程权限非法！"));
	return;
}
*/

UserDb myUser = new UserDb();
myUser = myUser.getUserDb(myname);

WorkflowActionDb wa = new WorkflowActionDb();
int actionId = (int)mad.getActionId();
wa = wa.getWorkflowActionDb(actionId);
if ( wa==null || !wa.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "流程中的相应动作不存在！"));
	return;
}

// 置嵌套表需要用到的cwsId
request.setAttribute("cwsId", "" + flowId);
// 置嵌套表需要用到的pageType
request.setAttribute("pageType", "flow");
// 置NestFromCtl需要用到的workflowActionId
request.setAttribute("workflowActionId", "" + wa.getId());

String flag = wa.getFlag();

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String canUserSeeDesignerWhenDispose = cfg.get("canUserSeeDesignerWhenDispose");
String canUserModifyFlow = cfg.get("canUserModifyFlow");

String action = ParamUtil.get(request, "action");
%>
<title>编辑流程</title>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
<script src="js/datepicker/jquery.datetimepicker.js"></script>

<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  

<script src="js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.min.css" />
<script src="js/jquery.form.js"></script>
<script src="js/jquery.bgiframe.js"></script>

<link href="js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="inc/flow_dispose.jsp"></script>
<script src="inc/flow_js.jsp"></script>
<script src="inc/upload.js"></script>

<script src="<%=request.getContextPath()%>/inc/ajax_getpage.jsp"></script>

<script tyle="text/javascript" language="javascript" src="spwhitepad/createShapes.js"></script>
<script language="javascript">
<!--
window.document.onkeydown = function() {
if(event.keyCode==13 && event.srcElement.type!='button' && event.srcElement.type!='submit' && event.srcElement.type!='reset' && event.srcElement.type!='textarea' && event.srcElement.type!='')
	event.keyCode=9;
}
-->
</script>
<script>
function setradio(myitem,v) {
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

var isXorRadiate = false;
var isCondSatisfied = true;
var hasCond = false;

var action = "<%=action%>";

// 编辑文件
function editdoc(doc_id, file_id) {
	redmoonoffice.AddField("doc_id", doc_id);
	redmoonoffice.AddField("file_id", file_id);
	redmoonoffice.Open("http://<%=request.getServerName()%>:<%=request.getServerPort()%>/<%=Global.virtualPath%>/flow_document_get.jsp?doc_id=" + doc_id + "&file_id=" + file_id);
}

// 审批文件，并作痕迹保留
function ReviseByUserColor(user, colorindex, doc_id, file_id) {
	redmoonoffice.AddField("doc_id", doc_id);
	redmoonoffice.AddField("file_id", file_id);
	<%if (wa.isStart==0) {%>
		<%if (cfg.get("isUseNTKO").equals("true")) {%>
		openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=1", 800, 600);
		<%}else{%>
		redmoonoffice.ReviseByUserColor("<%=Global.getFullRootPath(request)%>/flow_document_get.jsp?doc_id=" + doc_id + "&file_id=" + file_id, user, colorindex);
		<%}%>
	<%}else{%>
		<%if (cfg.get("isUseNTKO").equals("true")) {%>
		openWin("flow/flow_ntko_edit.jsp?file_id=" + file_id + "&flowId=<%=flowId%>&actionId=<%=actionId%>&doc_id=" + doc_id + "&isRevise=0", 800, 600);
		<%}else{%>
		editdoc(doc_id, file_id);
		<%}%>
	<%}%>
}

function uploaddoc(doc_id, file_id) {
	redmoonoffice.Clear();
	redmoonoffice.AddField("doc_id", doc_id);
	redmoonoffice.AddField("file_id", file_id);
	redmoonoffice.UploadDoc();
	// alert(redmoonoffice.ReturnMessage);
}

function OfficeOperate() {
	// alert(redmoonoffice.ReturnMessage);
}

function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
}

function saveArchive(flowId, actionId) {
	openWin("flow_doc_archive_save.jsp?op=saveFromFlow&flowId=" + flowId + "&actionId=" + actionId);
}


var curInternalName, toInternalname
isCondSatisfied = false;
var toActionName = "";

function checkDesignerInstalled() {
	var bCtlLoaded = false;
	try	{
		if (typeof(Designer.ModifyAction)=="undefined")
			bCtlLoaded = false;
		if (typeof(Designer.ModifyAction)=="unknown") {
			bCtlLoaded = true;
		}
	}
	catch (ex) {
	}
	if (!bCtlLoaded) {
		if (confirm("您还没有安装流程控件！请点击确定按钮下载安装！")) {
			window.open("activex/oa_client.EXE");
		}
	}	
}

function checkOfficeEditInstalled() {
	<%if (cfg.get("isUseNTKO").equals("true")) {%>
		return true;
	<%}%>	
	if (!isIE())
		return true;
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
		if (confirm("您还没有安装Office在线编辑控件！请点击确定按钮下载安装！")) {
			window.open("activex/oa_client.EXE");
		}
	}
}

function window_onload() {
	<%if (canUserSeeDesignerWhenDispose.equals("true")) {%>
	checkDesignerInstalled();
	<%}%>
	
	checkOfficeEditInstalled();
}
</script>
</head>
<body onLoad="window_onload()">
<%
String mode = "user";
if (canUserModifyFlow.equals("true"))
	mode = "user";
else
	mode = "view";
%>
<div id="bodyBox">
<table width="100%"  border="0" align="center" cellpadding="0" cellspacing="0" class="main">
  <tr>
    <td width="90%" align="left" class="tdStyle_1">&nbsp;<%=wf.getTitle()%> ( <%=lf.getName()%> ) 
    <%
		int doc_id = wf.getDocId();
		DocumentMgr dm = new DocumentMgr();
		Document doc = dm.getDocument(doc_id);
	%>
    </td>
    <td width="10%" align="left" class="tdStyle_1">
	<%if (canUserSeeDesignerWhenDispose.equals("true")) {%>
	<input class="btn" name="btnShowDesigner" type="button" onClick="ShowDesigner()" style="margin-right:10px" value="显示流程图" />
	<%}%>
	</td>
  </tr>
</table>
	<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td width="100%" valign="top" class="main">
		<table width="100%"  border="0" cellspacing="0" cellpadding="0">
            <tr>
              <td width="100%" colspan="3" align="center">
			  <%if (canUserSeeDesignerWhenDispose.equals("true")) {
					String flowExpireUnit = cfg.get("flowExpireUnit");
					if (flowExpireUnit.equals("day"))
						flowExpireUnit = "天";
					else
						flowExpireUnit = "小时";			  
			  %>
			  <object id="Designer" classid="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" codebase="activex/cloudym.CAB#version=1,3,0,0" style="width:0px; height:0px; margin-bottom:10px">
                  <param name="Workflow" value="<%=wf.getFlowString()%>" />
                  <param name="Mode" value="<%=mode%>" />
                  <!--debug user initiate complete-->
                  <param name="CurrentUser" value="<%=privilege.getUser(request)%>" />
				  <param name="ExpireUnit" value="<%=flowExpireUnit%>" />
				  <%
                  com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();	  
                  %>
                  <param name="Organization" value="<%=license.getCompany()%>" />
                  <param name="Key" value="<%=license.getKey()%>" />                  
              </object>
			  <%}%>
			  </td>
            </tr>
        </table></td>
      </tr>
    </table>
      <table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
      <form id="flowForm" name="flowForm" action="flow_form_edit.jsp?op=save&flowId=<%=flowId%>" method="post" enctype="multipart/form-data">
        <tr>
          <td width="35%" align="center"><%
					Render rd = new Render(request, wf, doc);
					out.print(rd.rend(wa, true));
					%>
					<!--<textarea name="flowstring" style="display:none"><%=wf.getFlowString()%></textarea>-->
					<input name="returnBack" value="<%=wf.isReturnBack()?"true":"false"%>" type=hidden />
                    <input id="cwsWorkflowResult" name="cwsWorkflowResult" type="hidden" />
                    <br />
		<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr style="display:none">
            <td align="left">
			<script>initUpload();</script>
        	</td>
          </tr>
          <tr>
            <td>
            <%
			FormDAO fdao = new FormDAO();
			fdao = fdao.getFormDAO(flowId, fd);
			%>
            	<input type="hidden" name="id" value="<%=fdao.getId()%>" />
                <input type="hidden" name="flowId" value="<%=flowId%>" />
              <input type="hidden" name="actionId" value="<%=actionId%>" />
              <input type="hidden" name="myActionId" value="<%=myActionId%>" />
              <input type="hidden" name="XorNextActionInternalNames">
              <textarea name="formReportContent" style="display:none"></textarea></td>
          </tr>
        </table></td>
        </tr>
        <tr>
          <td height="30" align="center"><input type="hidden" name="op" value="editFormValue" />
          <input class="btn" type="submit" name="Submit" value=" 保 存 " onclick="SubmitResult()" />
			<%if (flag.length()>=5 && flag.substring(4, 5).equals("1")) {%>
			&nbsp;
			<input class="btn" type="button" value=" 存 档 " onClick="saveArchive(<%=wf.getId()%>, <%=wa.getId()%>)">
			<%}%>
            &nbsp;
            <input class="btn" name="button3" type="button" onClick="window.location.href='flow_modify.jsp?flowId=<%=wf.getId()%>'" value="查看过程">
            <!--<input name="button2" type="button" onclick="openWin('flow_modify3.jsp?flowId=<%=flowId%>', 640, 480)" value=" 修改流程 " />--></td>
        </tr>
      </form>
    </table>
        <br />
        <table class="tabStyle_1 percent80" style="display:none" width="80%"  border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td colspan="3" align="left" class="tabStyle_1_title">处理附件</td>
          </tr>
          <tr>
            <td colspan="3" align="center"><br />
			<object id="redmoonoffice" classid="CLSID:D01B1EDF-E803-46FB-B4DC-90F585BC7EEE" codebase="activex/cloudym.CAB#version=1,2,0,1" width="316" height="43" viewastext="viewastext">
                <param name="Encode" value="utf-8" />
                <param name="BackColor" value="0000ff00" />
                <param name="Server" value="<%=request.getServerName()%>" />
                <param name="Port" value="<%=request.getServerPort()%>" />
                <!--设置是否自动上传-->
                <param name="isAutoUpload" value="1" />
                <!--设置文件大小不超过1M-->
                <param name="MaxSize" value="<%=Global.FileSize%>" />
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
          
        <%
			  if (doc!=null) {
				  java.util.Vector attachments = doc.getAttachments(1);
				  java.util.Iterator ir = attachments.iterator();
				  while (ir.hasNext()) {
				  	Attachment am = (Attachment) ir.next(); %>
          <tr>
            <td width="5%" height="31" align="right"><img src="images/attach.gif" /></td>
            <td width="69%">&nbsp; <a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flowId%>" target="_blank"><%=am.getName()%></a><br />            </td>
            <td width="26%" align="center">
			<%
			String ext = StrUtil.getFileExt(am.getDiskName());
			if (ext.equals("doc") || ext.equals("xls") || ext.equals("docx") || ext.equals("xlsx")) {%>
				<%if (wa.isStart==1) {%>
                <input name="button" type="button" class="btn" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)" value="编 辑" />
                <%}else{%>
                <input name="button" type="button" class="btn" onClick="javascript:ReviseByUserColor('<%=myUser.getRealName()%>', <%=wa.getOfficeColorIndex()%>, <%=doc_id%>, <%=am.getId()%>)" value="审 批" />
                <%}%>
			<%}else{%>
				<a href="flow_getfile.jsp?attachId=<%=am.getId()%>&flowId=<%=flowId%>" target="_blank">查看</a>
			<%}%>
              &nbsp;&nbsp;
              <!--<input type=button class="btn" onClick="javascript:uploaddoc(<%=doc_id%>, <%=am.getId()%>)" value="  上 传  ">-->
              <%if (flag.length()>=6 && flag.substring(5, 6).equals("1")) {%>
			  &nbsp;
              <a href="#" onClick="if (confirm('您确定要删除吗？')) window.location.href='?op=delAttach&myActionId=<%=myActionId%>&flowId=<%=flowId%>&doc_id=<%=am.getDocId()%>&attach_id=<%=am.getId()%>&page_num=1'">删除</a>
		    <%}%>
            </td>
          </tr>
        <%}
	  }
	  %>
</table>
<br />
<%if (flag.length()>=5 && flag.substring(4, 5).equals("2")) {%>			  
  <iframe id="hidFrame" src="flow_doc_archive_content.jsp?flowId=<%=flowId%>&actionId=<%=actionId%>" width=0 height=0></iframe>
<%}%>
</div>
</body>
<script>
$(function() {
	SetNewDate();
});

function ShowDesigner() {
	if ($('#Designer').width()==0) {
		$('#Designer').width(745);
		$('#Designer').height(515);
		btnShowDesigner.value = "隐藏流程图";
	}
	else {
		$('#Designer').width(0);
		$('#Designer').height(0);
		btnShowDesigner.value = "显示流程图";
	}
}

function showError(pRequest, pStatus, pErrorText) {
	alert('pStatus='+pStatus+'\r\n\r\n'+'pErrorText='+pErrorText);
}

//ajaxSubmit存在bug，当提交后，遇到提示”请先套红“，再提交时表单会被提交两次，出现两个附件，独立使用$(this).ajaxSubmit(options)问题依然存在，且仅当提交文件时才存在
//经检测jquery.form.js升级到新版(3.48.0)也同样存在此问题，且每次带文件提交时，jquery.form.js中存储的数据是被清空了的，因而不是数据bug问题，而是事件问题
var lastSubmitTime = new Date().getTime();
$('#flowForm').submit(function() {
	// 通过判断时间，禁多次重复提交
	var curSubmitTime = new Date().getTime();
	// 在0.5秒内的点击视为连续提交两次，实际当出现重复提交时，测试时间差为0
	if (curSubmitTime - lastSubmitTime < 500) {
		lastSubmitTime = curSubmitTime;
		// alert(curSubmitTime - lastSubmitTime);
		$('#bodyBox').hideLoading();		
		
		return false;
	}
	else {
		lastSubmitTime = curSubmitTime;
	}
	
	var dataType = "";
	if(isIE10 || isIE11){
	   dataType = 'json';
	}else{
	   dataType = 'text/html';
	}
	var options = {
	    dataType:  dataType, // 'xml', 'script', or 'json' (expected server response type)  表单为multipart/form-data即上传文件时，json无法解析
		success:       showResponse,  // post-submit callback 
		error: 		 showError
		  
	};
	$(this).ajaxSubmit(options);
	return false;
});

// 用于massValidate检查表单内容
var lv_cwsWorkflowResult = new LiveValidation('cwsWorkflowResult');

function SubmitResult() {
	if (!LiveValidation.massValidate(lv_cwsWorkflowResult.formObj.fields)) {
		jAlert("请检查表单中的内容填写是否正常！", "提示");
		return;
	}
	
	$('#bodyBox').showLoading();				
	$("#flowForm").submit();
}

function showResponse(data)  {
	/*
	data = data.replace("<HEAD></HEAD>", "");
	data = data.replace("<BODY>", "");
	data = data.replace("</BODY>", "");
	*/
	
	// 过滤掉其它字符，只保留JSON字符串
	if (!isIE10 && !isIE11){
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
	}
	
	
	if (data==null)
		return;
	data.msg = data.msg.replace(/\\r/ig, "<BR>");
	var op = data.op;
	if (op=="editFormValue") {
		jAlert_Redirect(data.msg, "提示", "flow_form_edit.jsp?flowId=<%=wf.getId()%>");
		return;
	}

	return;
}  
</script>
</html>