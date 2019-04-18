<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="java.util.Calendar"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
	String name = privilege.getUser(request);
	if (!privilege.isUserLogin(request)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	int id = ParamUtil.getInt(request, "id");
	MessageDb msg = new MessageDb();
	msg = (MessageDb)msg.getMessageDb(id);
	String sender = msg.getSender();
	if(!name.equals(sender)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String op = ParamUtil.get(request, "op");
	if(op.equals("edit")) {
		String receiversAll= ParamUtil.get(request, "receiver");
		String title = ParamUtil.get(request, "title");
		String content = ParamUtil.get(request, "content");
		int isSent = ParamUtil.get(request, "send_now").equals("yes") ? 1 :0;
		String sendTime = ParamUtil.get(request, "send_time");
		int receiptState = ParamUtil.getInt(request, "receipt_state");
		int msgLevel = ParamUtil.getInt(request, "msg_level");
		
		String action = ParamUtil.get(request, "action");
		System.out.println(getClass() + " action=" + action);
		if(action.equals("")) {
			msg.setReceiversAll(receiversAll);
			msg.setTitle(title);
			msg.setContent(content);
			msg.setIsSent(isSent);
			msg.setSendTime(sendTime);
			msg.setReceiptState(receiptState);
			msg.setMsgLevel(msgLevel);
			if(msg.save()) {
				out.println(StrUtil.Alert_Redirect("消息编辑成功！", "draft_edit.jsp?id=" + id));
			} else {
				out.println(StrUtil.Alert_Back("消息编辑失败！"));
			}
		} else {
			try {
				if(Msg.TransmitMsg(application, request)) {
					out.println(StrUtil.Alert_Redirect("消息发送成功！", "listdraft.jsp"));
				}
			}
			catch (ErrMsgException e) {
				out.println(StrUtil.Alert_Back("消息发送失败："+e.getMessage()));
			}
		}
	}
	String receiversAll = msg.getReceiversAll();
	String[] ary = receiversAll.split(",");
	String receiversAllName = "";
	UserMgr um = new UserMgr();
	for (int i=0; i<ary.length; i++) {
		UserDb user = um.getUserDb(ary[i]);
		if (user.isLoaded()) {
			if (receiversAllName.equals("")) {
				receiversAllName = user.getRealName();
			}else{
				receiversAllName += "," + user.getRealName();
			} 	
		}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>内部消息-编辑消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../../inc/common.js"></script>
<script type="text/javascript" src="../../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../../js/datepicker/jquery.datetimepicker.css"/>
<script src="../../js/datepicker/jquery.datetimepicker.js"></script>
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
function getRadioValue(str) {
	var r = document.getElementsByName(str);
	for (var i=0;i<r.length;i++) {
 		if(r[i].checked) {
			return r[i].value;
		}
	}
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
		form.send_time.value = form.date.value + " " + form.time.value;
	}
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
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

<%
	UserSetupDb usd = new UserSetupDb();
	usd = usd.getUserSetupDb(name);
	int messageToMaxUser = usd.getMessageToMaxUser();
%>

var messageToMaxUser = <%=messageToMaxUser%>;
function setUsers(users, userRealNames) {
	var ary = users.split(",");
	var len = ary.length;
	if (len>messageToMaxUser) {
		alert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！");
		return;
	}
	form.receiver.value = users;
	form.userRealName.value = userRealNames;
}
//-->
</script>
</head>

<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tr>
	<td class="tdStyle_1">草稿箱</td>
  </tr>
</table>
<table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td width="75"><div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><a href="listdraft.jsp"><img src="../images/m_draftbox.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><a href="listoutbox.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><a href="send.jsp"><img src="../images/newpm.gif" width="60" height="60" border="0" /></a></div></td>
    <td width="75"><div align="center"><img src="../images/m_delete.gif" name="imageField" width="60" height="60" border="0" id="imageField" /></div></td>
  </tr>
</table>
<br />
<form name="form" action="draft_edit.jsp?op=edit" method="post" onsubmit="return form_onsubmit()">
<table class="tabStyle_1 percent80" align="center">
  <tr>
	<td class="tabStyle_1_title" colspan="2">编辑消息</td>
  </tr>
  <tr>
    <td width="120" align="right">接收者：</td>
    <td>
    	<input type="hidden" name="receiver" value="<%=receiversAll%>">
        <input type="text" readonly name="userRealName" size="50" value="<%=receiversAllName%>">
		<input type="hidden" name="isDraft" value="false">
		<input type="hidden" name="id" value="<%=id%>">
        <a href="#" onClick="javascript:showModalDialog('../../user_multi_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">选择用户</a>
    </td>
  </tr>
  <tr>
    <td align="right">标&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
    <td><input type="text" name="title" value="<%=StrUtil.toHtml(msg.getTitle())%>" size=50></td>
  </tr>
  <tr>
    <td align="right">内&nbsp;&nbsp;&nbsp;&nbsp;容：</td>
    <td><textarea name="content" cols="80" rows="12"><%=msg.getContent()%></textarea></td>
  </tr>
  <tr>
    <td align="right">发送时间：</td>
    <td align="left">
<%
	int isSent = msg.getIsSent();
	String sendTime = msg.getSendTime();
	String[] array = StrUtil.split(sendTime.trim(), " ");
%>
      <input type="radio" name="send_now" value="yes" id="send_now_0" checked="checked" />
      <label for="send_now_0">立即发送</label>
      <br />
      <input type="radio" name="send_now" value="no" id="send_now_1" />
      <label for="send_now_1">定时发送</label>
      <input name="date" id="date" readonly="readonly" />
        <!-- <img onClick="SelectDate('date','yyyy-mm-dd')" src="../../images/form/calendar.gif" align="absmiddle" />
        <input name="time" readonly="readonly" />
        <img onClick="SelectDateTime('time')" src="../../images/form/clock.gif" align="absmiddle" />-->
        
    </td>
  </tr>
<%
	if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
%>
  <tr>
    <td align="right">手机短信提醒：</td>
    <td align="left">
      <input type="radio" name="isToMobile" value="true" id="isToMobile_0" checked="checked" />
      <label for="isToMobile_0">是</label>
      <input type="radio" name="isToMobile" value="false" id="isToMobile_1" />
      <label for="isToMobile_1">否</label>
    </td>
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
      <label for="isToOutBox_1">否</label>
    </td>
  </tr>
  <tr>
    <td align="right">是否需要回执：</td>
    <td align="left">
      <input type="radio" name="receipt_state" value="1" id="receipt_state_0" checked="checked" />
      <label for="receipt_state_0">是</label>
      <input type="radio" name="receipt_state" value="0" id="receipt_state_1" />
      <label for="receipt_state_1">否</label>
    </td>
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
    <td colspan="2" align="left"><%
			java.util.Iterator ir = msg.getAttachments().iterator();
			while (ir.hasNext()) {
				Attachment att = (Attachment)ir.next();
			%>
      <img src="../../images/attach2.gif" align="absmiddle" /> <a href="../getfile.jsp?attachId=<%=att.getId()%>&amp;msgId=<%=msg.getId()%>" target="_blank"><%=att.getName()%></a> <br />
      <%}%></td>
  </tr>
  <tr>
  	<td colspan="2" align="center">
	<input class="btn" type="submit" onclick="form.action.value=''" value=" 保存 " />
	&nbsp;&nbsp;
	<input class="btn" type="button" value=" 发送 " onclick="if (form_onsubmit) {form.action.value='send';form.submit();}" />
	<input type="hidden" name="action" />
	</td>
  </tr>
</table>
<br />
<script>
$(function(){
	$('#date').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:1, format:'Y-m-d H:i:00'});
})
setRadioValue("receipt_state", "<%=msg.getReCeiptState()%>");
setRadioValue("msg_level", "<%=msg.getMsgLevel()%>");
</script>
</form>
</body>
</html>