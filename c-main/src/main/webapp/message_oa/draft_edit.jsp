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
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>内部消息-编辑消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery.form.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<%
	if(op.equals("edit")) {
		String receiversAll= ParamUtil.get(request, "receiver");
		String title = ParamUtil.get(request, "title");
		String content = ParamUtil.get(request, "content");
		int isSent = ParamUtil.get(request, "send_now").equals("yes") ? 1 :0;
		String sendTime = ParamUtil.get(request, "send_time");
		int receiptState = ParamUtil.getInt(request, "receipt_state");
		int msgLevel = ParamUtil.getInt(request, "msg_level");
		String receiverscs1 = ParamUtil.get(request, "receiver1");
		String receiversms1 = ParamUtil.get(request, "receiver2");
		String receiversjs1 = ParamUtil.get(request, "receiver");
		
		String action = ParamUtil.get(request, "action");
		// System.out.println(getClass() + " action=" + action);
		if(action.equals("")) {
			if(!"".equals(receiverscs1)){
				receiversAll += "," + receiverscs1;
			}
			if(!"".equals(receiversms1)){
				receiversAll += "," + receiversms1;
			}
			msg.setReceiversAll(receiversAll);
			msg.setTitle(title);
			msg.setContent(content);
			msg.setIsSent(isSent);
			msg.setSendTime(sendTime);
			msg.setReceiptState(receiptState);
			msg.setMsgLevel(msgLevel);
			msg.setReceiverscs(receiverscs1);
			msg.setReceiversms(receiversms1);
			msg.setReceiversjs(receiversjs1);
			if(msg.save()) {
				out.println(StrUtil.jAlert_Redirect("消息编辑成功！","提示", "draft_edit.jsp?id=" + id));
			} else {
				out.println(StrUtil.jAlert_Back("消息编辑失败！","提示"));
			}
		} else {
			try {
				if(Msg.TransmitMsg(application, request)) {
					out.println(StrUtil.jAlert_Redirect("消息发送成功！","提示", "listdraft.jsp"));
				}
			}
			catch (ErrMsgException e) {
				out.println(StrUtil.jAlert_Back("消息发送失败："+e.getMessage(),"提示"));
			}
		}
	}
	String receiversAll = msg.getReceiversAll();
	String receiverscs = msg.getReceiverscs();
	String receiversms = msg.getReceiversms();
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
	
	String receiversjs = msg.getReceiversjs();
	String realNamejs = "";
	
	if (receiversjs == null || receiversjs.equals("")) {
		receiversjs = receiversAll;
	}
	
	if(!"".equals(receiversjs)){
		String[] recjsArr = receiversjs.split(",");
		UserDb u1 = new UserDb();
		for(int j=0;j<recjsArr.length;j++){
    		u1 = u1.getUserDb(recjsArr[j]);
    		if("".equals(realNamejs)){
    			realNamejs = u1.getRealName();
    		}else{
    			realNamejs += "," + u1.getRealName();
    		}
		}
	}
	
	String realNamecs = "";
	String trcStyle = "";
	String trmStyle = "";
	boolean isStylecs = false;
	if (receiverscs == null || receiverscs.equals("")) {
		trcStyle = "style='display:none'";
	}else{
		isStylecs = true;
		String[] recArry = receiverscs.split(",");
    	for(int t = 0;t<recArry.length;t++){
    		UserDb udb1 = new UserDb();
    		udb1 = udb1.getUserDb(recArry[t]);
    		if("".equals(realNamecs)){
    			realNamecs = udb1.getRealName();
    		}else{
    			realNamecs += "," + udb1.getRealName();
    		}
    	}
	}
	
	String realNamems = "";
	boolean isStylems = false;
	if (receiversms == null || receiversms.equals("")) {
		trmStyle = "style='display:none'";
	}else{
		isStylems = true;
		String[] remArry = receiversms.split(",");
    	for(int m = 0;m<remArry.length;m++){
    		UserDb udb2 = new UserDb();
    		udb2 = udb2.getUserDb(remArry[m]);
    		if("".equals(realNamems)){
    			realNamems = udb2.getRealName();
    		}else{
    			realNamems += "," + udb2.getRealName();
    		}
    	}
	}
	
	String csaddStyle = "";
	String csdelStyle = "";
	String msaddStyle = "";
	String msdelStyle = "";
	if(!isStylecs){
		csdelStyle = "style='display:none'";
	}else{
		csaddStyle = "style='display:none'";
	}
	if(!isStylems){
		msdelStyle = "style='display:none'";
	}else{
		msaddStyle = "style='display:none'";
	}
 %>
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

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:220px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}

function SelectDateTime(objName) {
	var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
	if (dt!=null)
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

var type;
function setPerson(deptCode, deptName, user, userRealName)
{
	if (type == 1) {
		form.receiver.value = user;
		form.userRealName.value = userRealName;
	} else if (type == 2) {
		form.receiver1.value = users;
		form.userRealName1.value = userRealNames;
	} else if (type == 3) {
		form.receiver2.value = users;
		form.userRealName2.value = userRealNames;
	}
}

function getSelUserNames() {
	if (type == 1) {
		return form.receiver.value;
	} else if (type == 2) {
		return form.receiver1.value;
	} else if (type == 3) {
		return form.receiver2.value;
	}
}

function getSelUserRealNames() {
	if (type == 1) {
		return form.userRealName.value;
	} else if (type == 2) {
		return form.userRealName1.value;
	} else if (type == 3) {
		return form.userRealName2.value;
	}
}

function openWinUsers(i) {
	type = i;
	openWin('../user_multi_sel.jsp', 800, 600);	
}

function openWinPersonGroup(i) {
	type = i;
	openWin('../user/persongroup_user_multi_sel.jsp', 800, 600)
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
		jAlert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！","提示");
		return;
	}
	if (type == 1) {
		form.receiver.value = users;
		form.userRealName.value = userRealNames;
	} else if (type == 2) {
		form.receiver1.value = users;
		form.userRealName1.value = userRealNames;
	} else if (type == 3) {
		form.receiver2.value = users;
		form.userRealName2.value = userRealNames;
	}
}
//-->

var uEditor;
function window_onload() {
	uEditor = UE.getEditor('myEditor',{
				initialContent : '<%=msg.getContent()%>',//初始化编辑器的内容  
				//allowDivTransToP: false,//阻止转换div 为p
				toolleipi:true,//是否显示，设计器的 toolbars
				textarea: 'content',
				enableAutoSave: false,  
				//选择自己需要的工具按钮名称,此处仅选择如下五个
				toolbars:[[
				'fullscreen','undo', 'redo', '|',
           'bold', 'italic', 'underline', 'superscript', 'subscript', 'pasteplain', '|', 'forecolor',
           'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
           'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
           'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify'
				]],
				//focus时自动清空初始化时的内容
				//autoClearinitialContent:true,
				//关闭字数统计
				wordCount:false,
				//关闭elementPath
				elementPathEnabled:false,
				//默认的编辑区域高度
				initialFrameHeight:300
				///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
				//更多其他参数，请参考ueditor.config.js中的配置项
			});
}

function form_onsubmit()
{
	errmsg = "";
	if (form.receiver.value=="")
		errmsg += "请填写收件人！\n"
	if (form.title.value=="")
		errmsg += "请填写标题！\n"
	if (uEditor.getContentTxt()=="")
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
	}else{
		return true;
	}
}
</script>
</head>

<body onLoad="window_onload()">
<div class="message_content">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tr>
	<td class="message_tdStyle_1">草稿箱</td>
  </tr>
</table>
<table class="tabStyle_1 percent100" align="center">
	<tr>
      <td class="message_btnbox" colspan="2">
	      	<img src="../skin/bluethink/images/message/message_back.png" onclick="location.href='javascript:history.back()'"/>
	      	<img src="../skin/bluethink/images/message/message_furbish.png" onclick="window.location.reload()"/>
      	</td>
    </tr>
</table>

<form name="form" action="draft_edit.jsp?op=edit" method="post" onsubmit="return form_onsubmit()">
<table class="tabStyle_1 percent100" align="center">
  <tr>
	<td class="tabStyle_1_title" colspan="2">编辑消息</td>
  </tr>
  <tr>
    <td width="120" align="right">接收人：</td>
    <td>
    	<input type="hidden" name="receiver" value="<%=receiversjs%>">
        <input type="text" readonly name="userRealName" size="110" value="<%=realNamejs%>">
		<input type="hidden" name="isDraft" value="false">
		<input type="hidden" name="id" value="<%=id%>">
		<p>
		<img src="../skin/bluethink/images/message/message_icon_uesr.png"  style="height:15px;width:15px;margin-top:7px"/>
        <a href="#" onClick="openWinUsers(1)">选择用户</a>&nbsp;&nbsp;
        <span style="display:none">
        <img
							src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(1)">我的用户组</a>
        &nbsp;&nbsp;</span>|&nbsp;&nbsp;
        <span id="spca" <%=csaddStyle %>><a href="javascript:;" onClick="addcs()" style="text-decoration:underline">添加抄送</a></span>
	<span id="spcs"  <%=csdelStyle %>><a href="javascript:;" onClick="deletecs()" style="text-decoration:underline">删除抄送</a></span>
	&nbsp;-&nbsp;
		<span id="spma"  <%=msaddStyle %>><a href="javascript:;" onClick="addms()" style="text-decoration:underline">添加密送</a></span>
		<span id="spms"  <%=msdelStyle %>><a href="javascript:;" onClick="deletems()" style="text-decoration:underline">删除密送</a></span>
    </td>
  </tr>
  
  <tr id="trc" <%=trcStyle %>>
    <td width="120" align="right">抄送：</td>
    <td>
    <input type="hidden" name="receiver1" value="<%=receiverscs %>">
        <input type="text" readonly name="userRealName1" size="110" value="<%=realNamecs %>">
        <p>
		<img src="../skin/bluethink/images/message/message_icon_uesr.png"  style="height:15px;width:15px;margin-top:7px"/>
        <a href="#" onClick="openWinUsers(2)">选择用户</a>&nbsp;&nbsp;
        <span style="display:none">
        <img
							src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(2)">我的用户组</a></span>
    </td>
  </tr>
  
  <tr id="trm" <%=trmStyle %>>
    <td width="120" align="right">密送：</td>
    <td>
    <input type="hidden" name="receiver2" value="<%=receiversms %>">
        <input type="text" readonly name="userRealName2" size="110" value="<%=realNamems %>">
        <p>
		<img src="../skin/bluethink/images/message/message_icon_uesr.png"  style="height:15px;width:15px;margin-top:7px"/>
        <a href="#" onClick="openWinUsers(3)">选择用户</a>&nbsp;&nbsp;
        <span style="display:none">
        <img
							src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(3)">我的用户组</a></span>
    </td>
  </tr>
  
  <tr>
    <td align="right">标&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
    <td><input type="text" name="title" value="<%=StrUtil.toHtml(msg.getTitle())%>" size=50></td>
  </tr>
  <tr>
    <td align="right">内&nbsp;&nbsp;&nbsp;&nbsp;容：</td>
    <td style="width:90%;">
    	<div id="myEditor" style="height:200px"></div>  
		  
    <!-- <textarea name="content" cols="80" rows="12"><%=msg.getContent()%></textarea> -->
    </td>
  </tr>
  <tr>
    <td align="right">发送时间：</td>
    <td align="left">
<%
	int isSent = msg.getIsSent();
	String sendTime = msg.getSendTime();
	//判断发送时间是否在当前时间之后，若是则为定时发送
	boolean flag = false;
	if (sendTime != null && !sendTime.equals(""))
	{
		java.util.Date sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd hh:mm:ss");
		if (sendDate == null) {
			sendDate = DateUtil.parse(sendTime, "yyyy-MM-dd hh:mm");
			sendTime += ":00";
		}
		java.util.Date now = DateUtil.parse(DateUtil.format(new java.util.Date(),"yyyy-MM-dd hh:mm:ss"), "yyyy-MM-dd hh:mm:ss") ;
		
		if (sendDate.after(now))
		{
			flag = true;
		}
	}
	if (isSent==0)
	{
%>
      <input type="radio" name="send_now" value="yes" id="send_now_0"  />
      <label for="send_now_0">立即发送</label>
      <br />
      <input type="radio" name="send_now" value="no" id="send_now_1" checked="checked"/>
      <%}else{ %>
         <input type="radio" name="send_now" value="yes" id="send_now_0" checked="checked" />
	      <label for="send_now_0">立即发送</label>
	      <br />
	      <input type="radio" name="send_now" value="no" id="send_now_1" />
      
      <%} %>
      <label for="send_now_1">定时发送</label>
      <%if(isSent==0){ %>
      	<input name="date" readonly="readonly" id="date" value="<%=sendTime%>" />
      <%}else{ %>
      	<input name="date" readonly="readonly" id="date" value="" />
      <%} %>
      <input name="send_time" type="hidden" value="<%=sendTime%>" />
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
      <img src="../images/attach2.gif" align="absmiddle" /> <a href="../getfile.jsp?attachId=<%=att.getId()%>&amp;msgId=<%=msg.getId()%>" target="_blank"><%=att.getName()%></a> <br />
      <%}%></td>
  </tr>
  <tr>
  	<td colspan="2" align="center">
	<input class="grey_btn_90" type="submit" onclick="form.action.value=''" value=" 保存 " />
	&nbsp;&nbsp;
	<input class="blue_btn_90" type="button" value=" 发送 " onclick="if (form_onsubmit()) {form.action.value='send';form.submit();}" />
	<input type="hidden" name="action" />
	</td>
  </tr>
</table>
<br />
<script>
$(function(){
	$('#date').datetimepicker({value:'',step:1, format:'Y-m-d H:i:00'});
})
setRadioValue("receipt_state", "<%=msg.getReCeiptState()%>");
setRadioValue("msg_level", "<%=msg.getMsgLevel()%>");
</script>

<script>
function addcs(){
	o("trc").style.display = "";
	o("spca").style.display = "none";
	o("spcs").style.display = "";
}
function deletecs(){
	o("receiver1").value = "";
	o("userRealName1").value = "";
	o("trc").style.display = "none";
	o("spca").style.display = "";
	o("spcs").style.display = "none";
}

function addms(){
	o("trm").style.display = "";
	o("spma").style.display = "none";
	o("spms").style.display = "";
}
function deletems(){
	o("receiver2").value = "";
	o("userRealName2").value = "";
	o("trm").style.display = "none";
	o("spma").style.display = "";
	o("spms").style.display = "none";
}

</script>

</form>
</div>
</body>
</html>