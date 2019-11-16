<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.sms.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.sms.Config"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String name = privilege.getUser(request);
String receiver = ParamUtil.get(request, "receiver");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "receiver", receiver, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>撰写消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ include file="../inc/nocache.jsp"%>
<%
if(op.equals("send")) {
	String content = ParamUtil.get(request, "content");
	String messageContent = content ;
    IMsgUtil imu = SMSFactory.getMsgUtil();
	String[] receivers = StrUtil.split(receiver, ",");
	String isTimeSend = ParamUtil.get(request,"isTimeSend");
	boolean timing = "1".equals(isTimeSend)?true:false;
	String timeSendDate = ParamUtil.get(request,"timeSend_Date");
	//String timeSendTime = ParamUtil.get(request,"timeSend");
	//timeSendDate = timeSendDate.replace("/","-")+":00";
	java.util.Date timeSend = DateUtil.parse(timeSendDate,"yyyy-MM-dd hh:mm:ss");
	long batch = SMSSendRecordDb.getBatchCanUse();
	com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
	int length = receivers.length;//发送短信条数
	Config cfg = new Config();
	int remain = cfg.canSendSMS(length,messageContent.length());
	int count = cfg.getDivNumber(messageContent.length());
	int realSendUserCount = 0;
	int i = 0;
	for(i=0; i<length && remain>0; i++) {
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
	if(i < length){
		if(cfg.getBoundary()==Config.SMS_BOUNDARY_YEAR){
			out.print(StrUtil.jAlert_Redirect(StrUtil.format(cfg.getProperty("sms.alertYearExceed"), new Object[]{cfg.getIsUsedProperty("yearTotal"), "" + realSendCount}),"提示", "sms_send_message.jsp"));
		}else if(cfg.getBoundary()==Config.SMS_BOUNDARY_MONTH){
			out.print(StrUtil.jAlert_Redirect(StrUtil.format(cfg.getProperty("sms.alertMonthExceed"), new Object[]{cfg.getIsUsedProperty("monthTotal"), "" + realSendCount}),"提示", "sms_send_message.jsp"));
		}else{
			out.print(StrUtil.jAlert_Redirect("发送完毕，本次共发送短信"+realSendCount+"条。","提示","sms_send_message.jsp"));
		}
	}else{
		out.print(StrUtil.jAlert_Redirect("发送完毕，本次共发送短信"+realSendCount+"条。","提示","sms_send_message.jsp"));
	}
	return;
}
 %>
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
	form1.submit();
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
	return form1.userRealName.value;
}

function openWinUsers() {
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:900px;dialogHeight:730px;status:no;help:no;')
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
	form1.receiver.value = users;
	form1.userRealName.value = userRealNames;
}

function setObj(th){
	document.getElementById('content').innerHTML =th;
	countChar("content","counter");
}
//-->
</script>
<script src="../inc/common.js"></script>
<script src="../inc/flow_dispose_js.jsp"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
</head>
<body>
<%@ include file="sms_user_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
/**function SelectDateTime(objName) {
		var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
		if (dt!=null)
			$(objName).value = dt.substring(0, 5);
	}*/
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        $(objName).value = dt.substring(0, 5);
}
</script>
<%
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}%>
<div class="spacerH"></div>
<form action="sms_send_message.jsp?op=send" name="form1" method="post">
  <table width="416" border="0" cellspacing="0" cellpadding="3" align="center" class="tabStyle_1 percent60">
          <thead>
            <tr>
            <td class="tabStyle_1_title" height="27" colspan="2">发送短信</td>
          </tr>
          </thead>
          <tr> 
            <td width="86" height="27"> 
              接 收 者：
            </td>
            <td width="318" height="27">
              <input type="hidden" name="receiver" class="input1" value="<%=receiver%>">
			  <%
			  String userRealName = "";
			  if (!receiver.equals("")) {
				String[] ary = StrUtil.split(receiver, ",");
				com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
				for (int i=0; i<ary.length; i++) {
					ud = ud.getUserDb(ary[i]);
					if (userRealName.equals(""))
						userRealName = ud.getRealName();
					else
						userRealName += "," + ud.getRealName();
				}
			  }
			  %>
              <input type="text" readonly name="userRealName" size="30" value="<%=userRealName%>">
			  <a href="#" onClick="openWinUsers()">选择用户</a>
			</td>
          </tr>
          <tr> 
            <td width="86" height="26"> 
              消息内容：</td>
            <td width="318" height="26"> 
              <textarea name="content" id="content" style="width:98%; height:100px" onkeydown='countChar("content","counter");' onkeyup='countChar("content","counter");'></textarea>            </td>
          </tr>
           <tr> 
            <td width="86" height="26"> 
            短信字数：</td>
            <td width="318" height="26" >已经输入<span id="counter" style="color:#FF0000">0</span>字&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" onclick="openWin('sms_template_sel.jsp', 640, 480)">短信模版</a></td>
          </tr>
		  <tr>
          	<td width="86" height="26"> 
            定时发送：</td>
            <td width="318" height="26"> 
            <input type="checkbox" name="isTimeSend" value="1" />
            </td>
          </tr>
          <tr>
          	<td width="86" height="26"> 
            发送时间：</td>
            <td width="318" height="26"> 
            <input id="timeSend_Date" name="timeSend_Date" size="18" />
        </td>
          </tr>
          <tr>
            <td colspan="2" height="26" align="center"><span style="padding-left:14px;"><span style="color:#FF0000">注：短信字数超过70字，将自动转为2条短信！</span></span></td>
          </tr>
          <tr> 
            <td colspan="2" height="26" align="center"> 
                <input type="button" name="Submit" value="发送短信" class="btn" onClick="form1_onsubmit()" ></td>
          </tr>
        </table></form>
</body>
<script language="javascript"> 
$(function(){
	$('#timeSend_Date').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00'});
})
function countChar(textareaName,spanName){ 
document.getElementById(spanName).innerHTML = document.getElementById(textareaName).value.length;
} 
</script> 
</html>
