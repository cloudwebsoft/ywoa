<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.sms.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String name = privilege.getUser(request);
String receiver = ParamUtil.get(request, "receiver");

String op = ParamUtil.get(request, "op");
if(op.equals("send")) {
	String content = ParamUtil.get(request, "content");
	String messageContent = content;
    IMsgUtil imu = SMSFactory.getMsgUtil();
	String[] receivers = StrUtil.split(receiver, ",");
	String isTimeSend = ParamUtil.get(request,"isTimeSend");
	boolean timing = "1".equals(isTimeSend)?true:false;
	String timeSendDate = ParamUtil.get(request,"timeSend_Date");
	//timeSendDate = timeSendDate.replace("/","-")+":00";
	//String timeSendTime = ParamUtil.get(request,"timeSend"); 
	//if(timeSendTime == null){
		//timeSendTime = "00:00";
	//}
	java.util.Date timeSend = DateUtil.parse(timeSendDate,"yyyy-MM-dd hh:mm:ss");
	long batch = SMSSendRecordDb.getBatchCanUse();
	com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
	int length = receivers.length;//发送短信条数
	Config cfg = new Config();
	int remain = cfg.canSendSMS(length,messageContent.length());
	int count = cfg.getDivNumber(messageContent.length());
	int i=0;
	int realSendUserCount = 0;
	for(; i<length&&remain>0; i++) {
		ud = ud.getUserDb(receivers[i]);
		if(ud == null) {
			continue;
		}
		try {
			if (imu.send(ud,messageContent,name,timing,timeSend,batch)) {
				realSendUserCount ++;				
				remain --;
			}
		} catch(ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
	}
	int realSendCount = realSendUserCount * count;	
	if(i<length){
		if(cfg.getBoundary()==Config.SMS_BOUNDARY_YEAR){
			out.print(StrUtil.jAlert_Redirect(StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}),"提示", "sms_send_message_to_group.jsp"));
		}else if(cfg.getBoundary()==Config.SMS_BOUNDARY_MONTH){
			out.print(StrUtil.jAlert_Redirect(StrUtil.format(cfg.getProperty("sms.alertMonthExceed"), new Object[]{cfg.getIsUsedProperty("monthTotal"), "" + realSendCount}),"提示", "sms_send_message_to_group.jsp"));
		}else{
			out.print(StrUtil.jAlert_Redirect("发送完毕，本次共发送短信"+realSendCount+"条。","提示","sms_send_message_to_group.jsp"));
		}
	}else{
		out.print(StrUtil.jAlert_Redirect("发送完毕，本次共发送短信"+realSendCount+"条。","提示","sms_send_message_to_group.jsp"));
	}
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>撰写群发消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script language=javascript>
<!--
function form1_onsubmit()
{
	errmsg = "";
	if (form1.receiver.value=="")
		errmsg += "请填写接收者！\n"
	if (form1.content.value=="")
		errmsg += "请填写内容！\n"
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}
}

<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(name);
%>	
function getDept() {
	return "<%=usd.getMessageToDept()%>";
}

function getValidUserGroup() {
	return "<%=usd.getMessageToUserGroup()%>";
}

function getValidUserRole() {
	return "<%=usd.getMessageToUserRole()%>";
}

function setPerson(deptCode, deptName, user, userRealName)
{
	form1.receiver.value = user;
	form1.userRealName.value = userRealName;
}

function getSelUserNames() {
	return form1.receiver.value;
}

function getSelUserRealNames() {
	return form1.userRealNames.value;
}

function openWinUsers() {
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:900px;dialogHeight:730px;status:no;help:no;')
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUserGroup() {
	openWin("../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../user_role_multi_sel.jsp", 520, 400);
}

function openWinTemplateSel(){
	openWin("sms_template_sel.jsp", 520, 400);
}

<%
int messageToMaxUser = usd.getMessageToMaxUser();
%>

var messageToMaxUser = <%=messageToMaxUser%>;

function setUsers(users, userRealNames) {
	var ary = users.split(",");
	var len = ary.length;
	if (len>messageToMaxUser) {
		jAlert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！","提示");
		return;
	}

	form1.receiver.value = users;
	form1.userRealNames.value = userRealNames;
}

function setObj(th){
	document.getElementById('content').innerHTML =th;
	countChar("content","counter");
}

function countChar(textareaName,spanName){
	document.getElementById(spanName).innerHTML = document.getElementById(textareaName).value.length;
} 
//-->
</script>
</head>
<body>
<%@ include file="sms_user_inc_menu_top.jsp"%>
<script src="../inc/flow_dispose_js.jsp"></script>
<script>
o("menu2").className="current";
/**function SelectDateTime(objName) {
		var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
		if (dt!=null)
			$(objName).value = dt.substring(0, 5);
	}*/
$(function(){
	$('#timeSend_Date').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00'});
})
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        $(objName).value = dt.substring(0, 5);
}
</script>
<%
if (!privilege.isUserPrivValid(request, "sms")) {	
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<div class="spacerH"></div>
<form action="sms_send_message_to_group.jsp?op=send" method="post" name="form1" onSubmit="return form1_onsubmit()">
<table width="447" border="0" cellspacing="0" cellpadding="3" align="center" class="tabStyle_1 percent60">
<thead>
  <tr>
    <td class="tabStyle_1_title" height="27" colspan="2" align="center">短信群发</td>
    </tr>
  </thead>
  <tr> 
    <td width="106" height="27" align="center"> 
        接 收 者：
    </td>
    <td width="329" height="27">
      <textarea name="userRealNames" cols="28" rows="5" style="width:98%" readOnly wrap="yes" id="userRealNames"></textarea>
      <input type=hidden name="receiver">
      <input class="btn" type="button" onClick="openWinUsers()" value="选择用户">
      <input class="btn" type=button onClick="openWinUserGroup()" value="按用户组">
      <input class="btn" name="button" type=button onClick="openWinUserRole()" value="按角色"></td>
  </tr>
  <tr> 
    <td width="106" height="26"> 
      <div align="center">消息内容：</div>
    </td>
    <td width="329" height="26"> 
      <textarea name="content" style="width:98%; height:100px" cols="28" rows="6" id="content"  onkeydown='countChar("content","counter");' onkeyup='countChar("content","counter");'></textarea>
    </td>
  </tr>
  <tr> 
    <td width="106" height="26"> 
      <div align="center">短信字数：</div>            </td>
    <td width="329" height="26" >已经输入<span id="counter" style="color:#FF0000">0</span>字 
      &nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" onclick="openWin('sms_template_sel.jsp', 640, 480)">短信模版</a></td>
  </tr>
  <tr>
    <td width="106" height="26"> 
      <div align="center">定时发送：</div>            </td>
    <td width="329" height="26"> 
      <input type="checkbox" name="isTimeSend" value="1" />
    </tr>
  <tr>
    <td width="106" height="26"> 
    <div align="center">发送时间：</div>            </td>
    <td width="329" height="26"> 
    <input name="timeSend_Date" id="timeSend_Date"  size="18" readonly="">
        </td>
  </tr>
  <tr>
    <td colspan="2" height="26" align="center"><span style="padding-left:14px;"><span style="color:#FF0000">注：短信字数超过70字，自动转为2条短信！</span></span></td>
  </tr>
  <tr> 
    <td colspan="2" height="26" align="center"> 
        <input type="submit" name="Submit" value="发送消息" class="btn">
    </td>
  </tr>
</table>
</form>
</body>
</html>
