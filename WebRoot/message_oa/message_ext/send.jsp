<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.emailpop3.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String name = privilege.getUser(request);
String receiver = ParamUtil.get(request, "receiver");
String title = ParamUtil.get(request, "title");
String content = ParamUtil.get(request, "content");

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "receiver", receiver, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String qStr = request.getQueryString();
if (qStr!=null) {
	if (!cn.js.fan.security.AntiXSS.antiXSS(qStr).equals(qStr)) {
		com.redmoon.oa.LogUtil.log(name, StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF message_oa/message_ext/send.jsp");	
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
		return;
	}
}

int mailId = ParamUtil.getInt(request, "mailId", -1);
MailMsgDb mmd = null;
if (mailId!=-1) {
	try {
		MailMsgMgr mmm = new MailMsgMgr();		
		mmd = mmm.getMailMsgDb(request, mailId);
		title = mmd.getSubject();
		content = mmd.getContent();
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

if (!privilege.isUserPrivValid(request, "message")) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String netdiskFiles = ParamUtil.get(request, "netdiskFiles");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<title>撰写消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../../inc/common.js"></script>
<script src="../../js/jquery.js"></script>
<script type="text/javascript" src="../../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
<script src="../../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../../inc/upload.js"></script>
<script language=javascript>
<!--
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("../../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:220px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}

/**function SelectDateTime(objName) {
	var dt = showModalDialog("../../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
	if (dt!=null)
		findObj(objName).value = dt;
}*/
function SelectDateTime(objName) {
    var dt = openWin("../../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        findObj(objName).value = dt;
}
function form_onsubmit()
{
	errmsg = "";
	if (form.receiver.value=="")
		errmsg += "请填写接收者！\n"
	if (form.title.value=="")
		errmsg += "请填写标题！\n"
	if (form.content.value=="")
		errmsg += "请填写内容！\n"
	if (getRadioValue('send_now')=="no") {
		if(form.date.value=="" || form.time.value=="") {
			errmsg += "请选择定时发送日期！\n";
		}
		form.send_time.value = form.date.value;
	}
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}
	return true;
}
function getRadioValue(str) {
	var r = document.getElementsByName(str);
	for (var i=0;i<r.length;i++) {
 		if(r[i].checked) {
			return r[i].value;
		}
	}
}
function setVisibility() {
	if(getRadioValue('send_now')=="no") {
		document.getElementById("sendButton").disabled = "disabled";
	} else {
		document.getElementById("sendButton").disabled = "";
	}
}
function saveDraft() {
	form.action = "send_do.jsp?op=addDraft";
	if (!form_onsubmit())
		return;	
	form.submit();
}

function send() {
	o("sendButton").disabled = true;
	if (form_onsubmit()) {
		form.submit();
	}
	else
		o("sendButton").disabled = false;
}

function setPerson(deptCode, deptName, user, userRealName)
{
	form.receiver.value = user;
	form.userRealName.value = userRealName;
}

function getSelUserNames() {
	return form.receiver.value;
}

function getSelUserRealNames() {
	return form.userRealName.value;
}

function openWinUsers() {
	if (!isApple())
		showModalDialog('../../user_multi_sel.jsp',window.self,'dialogWidth:900px;dialogHeight:730px;status:no;help:no;')
	else
		openWin('../../user_multi_sel.jsp', 900, 730);
}

function openWinPersonGroup() {
	openWin('../../user/persongroup_user_multi_sel.jsp', 600, 480)
}
<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(name);
int messageToMaxUser = usd.getMessageToMaxUser();
%>

function getDept() {
	return "<%=usd.getMessageToDept()%>";
}

var messageToMaxUser = <%=messageToMaxUser%>;
function setUsers(users, userRealNames) {
	var ary = users.split(",");
	var len = ary.length;
	if (len>messageToMaxUser) {
		jAlert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！","提示");
		return;
	}
	form.receiver.value = users;
	form.userRealName.value = userRealNames;
}

function window_onload() {
<%
if (!netdiskFiles.equals("")) {
%>
setNetdiskFiles("<%=netdiskFiles%>");
<%
}
%>
}
//-->
</script>
</head>
<body onLoad="window_onload()">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">撰写消息
        <%
		if (privilege.isUserPrivValid(request, "message.group")) {
		%>
        [<a href="sendtogroup.jsp">群发</a>]
        <%}%></td>
    </tr>
  </tbody>
</table>
<table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td width="75"><div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><a href="listdraft.jsp"><img src="../images/m_draftbox.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><a href="listoutbox.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><a href="listrecycle.jsp"><img src="../images/m_recycle.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><img src="../images/newpm.gif" width="60" height="60" border="0"></div></td>
    <td width="75"><div align="center"> <img src="../images/m_delete.gif" width="60" height="60"></div></td>
  </tr>
</table>
<br />
<form action="send_do.jsp" method="post" enctype="multipart/form-data" name="form" onSubmit="return form_onsubmit()">
  <table class="tabStyle_1 percent80" align="center">
    <tr>
      <td class="tabStyle_1_title" colspan="2">新消息</td>
    </tr>
    <tr>
      <td width="120" align="right">接收者：</td>
      <td align="left"><input type="hidden" name="receiver" class="input1" value="<%=receiver%>">
        <%
			  String userRealName = "";
			  if (!receiver.equals("")) {
				String[] ary = StrUtil.split(receiver, ",");
				com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
				for (int i=0; i<ary.length; i++) {
					ud = ud.getUserDb(ary[i]);
					if (!ud.isLoaded())
						continue;
					if (userRealName.equals(""))
						userRealName = ud.getRealName();
					else
						userRealName += "," + ud.getRealName();
				}
			  }
			  %>
        <input type="text" readonly name="userRealName" class="input1" size="50" value="<%=userRealName%>">
        <input type="hidden" name="isDraft" value="false">
        <input type="hidden" name="action" value="<%=ParamUtil.get(request, "action")%>">
        <a href="javascript:;" onClick="openWinUsers()">选择用户</a>
        &nbsp;&nbsp;
        <a href="javascript:;" onClick="openWinPersonGroup()">我的用户组</a>
        </td>
    </tr>
    <tr>
      <td align="right">消息标题：</td>
      <td align="left"><input type="text" name="title" class="input1" size="50" value="<%=title%>"></td>
    </tr>
    <tr>
      <td align="right">消息内容：</td>
      <td align="left"><textarea name="content" cols="80" rows="16"><%=content%></textarea></td>
    </tr>
    <tr>
      <td align="right">附件：</td>
      <td align="left"><script>initUpload()</script></td>
    </tr>
<%if (mmd!=null) {%>    
    <tr>
      <td align="right">邮箱附件：</td>
      <td align="left">
<%
	java.util.Vector vAttach = mmd.getAttachments();
	java.util.Iterator attir = vAttach.iterator();
	while (attir.hasNext()) {
		Attachment att = (Attachment)attir.next();
%>
        <div id="mailFile<%=att.getId()%>"><input name="mailFiles" value="<%=att.getId()%>" type="hidden" /><%=att.getName()%>&nbsp;&nbsp;<a target="_self" href="javascript:;" onClick="o('mailFile<%=att.getId()%>').outerHTML=''" style="color:red; font-size:18px">×</a></div>
<%
	}
%>        
      </td>
    </tr>
<%}%>    
    <tr>
      <td align="right">网盘文件：</td>
      <td align="left">
	  <a href="javascript:;" onClick="openWin('../../netdisk/netdisk_frame.jsp?mode=select', 800, 600)">选择文件</a>
	  <div id="netdiskFilesDiv" style="line-height:1.5; margin-top:5px"></div>
	  </td>
    </tr>
    <tr>
      <td align="right">发送时间：</td>
      <td align="left">
      	<input type="radio" name="send_now" value="yes" id="send_now_0" checked="checked" onClick="setVisibility()" />
        <label for="send_now_0" onClick="setVisibility()">立即发送</label>
        <br />
        <input type="radio" name="send_now" value="no" id="send_now_1" onClick="setVisibility()" />
        <label for="send_now_1" onClick="setVisibility()">定时发送</label>
        <input name="date" id="date" readonly="readonly" />
        <!-- <img onClick="SelectDate('date','yyyy-mm-dd')" src="../../images/form/calendar.gif" align="absmiddle" />
        <input name="time" readonly="readonly" />
        <img onClick="SelectDateTime('time')" src="../../images/form/clock.gif" align="absmiddle" />-->
        <input name="send_time" type="hidden" />      </td>
    </tr>
<%
	if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
%>
    <tr>
      <td align="right">手机短信提醒：</td>
      <td align="left">
		<input type="radio" name="isToMobile" value="true" id="isToMobile_0" />
        <label for="isToMobile_0">是</label>
        <input type="radio" name="isToMobile" value="false" id="isToMobile_1" checked="checked" />
        <label for="isToMobile_1">否</label>      </td>
    </tr>
<%
	}
%>
    <tr>
      <td align="right">保存到发件箱：</td>
      <td align="left">
      	<input type="radio" name="isToOutBox" value="true" id="isToOutBox_0" checked="checked" />
     	<label for="isToOutBox_0">是</label>
        <input type="radio" name="isToOutBox" value="false" id="isToOutBox_1" />
        <label for="isToOutBox_1">否</label>      </td>
    </tr>
    <tr>
      <td align="right">是否需要回执：</td>
      <td align="left">
      	<input type="radio" name="receipt_state" value="1" id="receipt_state_0" />
        <label for="receipt_state_0">是</label>
        <input type="radio" name="receipt_state" value="0" id="receipt_state_1" checked="checked" />
        <label for="receipt_state_1">否</label>      </td>
    </tr>
    <tr>
      <td align="right">消息等级：</td>
      <td align="left">
      	<input type="radio" name="msg_level" value="0" id="msg_level_0" checked="checked" />
        <label for="msg_level_0">普通</label>
        <input type="radio" name="msg_level" value="1" id="msg_level_1" />
        <label for="msg_level_1">紧急</label>
	  </td>
    </tr>
    <tr>
      <td colspan="2" align="center">
        <input class="btn" type="button" id="sendButton" name="sendButton" value=" 发 送 " onClick="send()">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input class="btn" type="button" id="saveButton" name="saveButton" value="保存草稿" onClick="saveDraft()">  	  </td>
    </tr>
  </table>
</form>
</body>
<script>
$(function(){
	$('#date').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:1, format:'Y-m-d H:i:00'});
})
function setNetdiskFiles(ids) {
	getNetdiskFiles(ids);
}

function doGetNetdiskFiles(response){
	var rsp = response.responseText.trim();
	o("netdiskFilesDiv").innerHTML += rsp;
}

var errFunc = function(response) {
	// alert('Error ' + response.status + ' - ' + response.statusText);
	jAlert(response.responseText,"提示");
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
</script>
</html>
