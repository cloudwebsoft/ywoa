<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="userpop3setup" scope="page" class="com.redmoon.oa.emailpop3.UserPop3Setup"/>
<%
String rpath = request.getContextPath();
String op = ParamUtil.get(request, "op");
MailMsgMgr mmm = new MailMsgMgr();

int draftId = ParamUtil.getInt(request, "draftId", -1);
int isClickMenu = ParamUtil.getInt(request, "isClickMenu", -1);

//int id = ParamUtil.getInt(request, "id",-1);
int subMenu = ParamUtil.getInt(request, "subMenu",-1);




if (op.equals("modify")) {
	boolean re = false;
	try {
		re = mmm.modify(application, request);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "mail_edit.jsp?draftId=" + draftId+"&subMenu="+subMenu+"&isClickMenu="+isClickMenu));
		return;
	}
}
if (op.equals("delattach")) {
	boolean re = false;
	try {
		re = mmm.delAttachment(request);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	if (re) {
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "mail_edit.jsp?draftId=" + draftId+"&subMenu="+subMenu+"&isClickMenu="+isClickMenu));
		return;
	}
}
MailMsgDb mmd = null;
String cc = "";
String receiver = "";
try {
	if(draftId != -1){
		mmd = mmm.getMailMsgDb(request, draftId);
		
		
		receiver = mmd.getReceiver();
		cc = mmd.getCopyReceiver();
		String strToghter = "";
		String receiverArr[] = receiver.split(",");
		
		for(int i=0;i<receiverArr.length;i++){
			if((i+1) == receiverArr.length){
				strToghter += receiverArr[i];
			}else{
				strToghter += receiverArr[i]+",";
			}
		}
		
		
		
	}
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}


int isReaded = 0;
int receiptState = 0;
MailMsgDb mailMsgDb = null;
try {
	if(draftId != -1){
		mailMsgDb = mmm.getMailMsgDb(request, draftId);
		isReaded = mailMsgDb.isReaded()?1:0;
		receiptState = mailMsgDb.getReceiptState();
		if (!mailMsgDb.isReaded()) {
			mailMsgDb.setReaded(true);
			mailMsgDb.save();
		}
	}
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

Calendar calendar = Calendar.getInstance();
int year = calendar.get(Calendar.YEAR);
int month = calendar.get(Calendar.MONTH)+1;
int day = calendar.get(Calendar.DAY_OF_MONTH);
%>
<% 



//保存草稿的时候不会跳转页面，获取当前所填的数据


String bcc = "";
String to = "";
String subject = "";
String content = "";
String email = "";
if(draftId != -1){
	mmm = new MailMsgMgr();
	try {
		mmd = mmm.getMailMsgDb(request, draftId);
		subject = mmd.getSubject();
		content = mmd.getContent();
		to = mmd.getReceiver();
		email = mmd.getEmailAddr();
		cc = mmd.getCopyReceiver();
		bcc = mmd.getBlindReceiver();
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" ></meta>
<title>编辑邮件</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link href="mail.css" type="text/css" rel="stylesheet" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.toaster.email.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<script src="../inc/upload.js"></script>
<!-- <script type="text/javascript" src="../inc/livevalidation_standalone.js"></script> -->
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />

<script>
function form1_onsubmit() {
	var timeSend = document.getElementById("timeSend");
	if(timeSend.checked == true){
		var sendDate = $("#sendDate").val();
		form1.send_time.value = sendDate;
	}
	var emergent = document.getElementById("emergent");
	var replySinge = document.getElementById("replySinge");
	if(emergent.checked == true){
		form1.msg_level.value = "1";
	}
	if(replySinge.checked == true){
		form1.receipt_state.value = "1";
	}
	return true;
}


function showDiv(){
	var times = $("#times").val();
	if(times == "0"){
		$("#hiddenDiv").css("display","block");
		$("#times").val("1");
		$("#selectMore").text("隐藏选项");
	}
	if(times == "1"){
		$("#hiddenDiv").css("display","none");
		$("#times").val("0");
		$("#selectMore").text("更多选项");
	}
}

function isSelected(){
	var timeSend = document.getElementById("timeSend");
	if(timeSend.checked == true){
		$("#dateSelect").css("display","block");
	}else{
		$("#dateSelect").css("display","none");
	}
}
var uEditor;
function window_load(){
	
	//document.getElementById("to").focus();
	$("#divTo").focus();
	$("#common").attr("lt","divTo");
	
	if("<%=mmd.getSendTime()%>" == "null"){
		var nowDate = new Date();
		var nextday_milliseconds=nowDate.getTime()+1000*60*60*24; 
		var nextDate = new Date();
		nextDate.setTime(nextday_milliseconds);

		var strMonth = nextDate.getMonth()+1;
		var strDate = nextDate.getDate();
		if(strDate<10){
			strDate = "0"+strDate;
		}
		if(strMonth <10){
			strMonth = "0"+strMonth;
		}
		
		$("#sendDate").val(nextDate.getFullYear()+"-"+strMonth+"-"+strDate+" "+"00:00");
		changeDay();
	}
	
	if('<%=mmd.getSendTime()%>' != 'null'){
		$("#hiddenDiv").css("display","block");
		$("#dateSelect").css("display","block");
		$("#timeSend").attr("checked","checked");
		$("#times").val("1");
		$("#selectMore").text("隐藏选项");
		changeDay();
	}

	if('<%=mmd.getReceiptState()%>' == '1'){
		$("#hiddenDiv").css("display","block");
		//$("#dateSelect").css("display","block");
		$("#replySinge").attr("checked","checked");
		$("#times").val("1");
		$("#selectMore").text("隐藏选项");
	}
	if('<%=mmd.getMsgLevel()%>' == '1'){
		$("#hiddenDiv").css("display","block");
		//$("#dateSelect").css("display","block");
		$("#emergent").attr("checked","checked");
		$("#times").val("1");
		$("#selectMore").text("隐藏选项");
	}
	

	if("<%=cc%>" != ""){
		$("#copyReceiverTR").css("display","");
		$("#copyReceiverTimes").val("1");
		$("#copyReceiverText").html("<img src=\"images/inbox-cc.png\" width=\"15\" height=\"15\"/>取消抄送");
	}

	uEditor = UE.getEditor('myEditor',{
		initialContent : o("hidContent").value,//初始化编辑器的内容  
		//allowDivTransToP: false,//阻止转换div 为p
		toolleipi:true,//是否显示，设计器的 toolbars
		textarea: 'content',
		enableAutoSave: false,  
		//选择自己需要的工具按钮名称,此处仅选择如下五个
		toolbars:[[
		'fullscreen','undo', 'redo', '|',
		           'bold', 'italic', 'underline','|','forecolor',
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
		autoFloatEnabled:false,
		//默认的编辑区域高度
		initialFrameHeight:300
		///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
		//更多其他参数，请参考ueditor.config.js中的配置项
	});
	
	 if("<%=isClickMenu%>" == "-1"){
			$.toaster({priority : 'info', message : '已经保存到草稿' });
		}
}
function changeDay(){
	var sendDate = $("#sendDate").val();
	if(sendDate != ""){
		
		var dateArr = sendDate.split(" ");
		var year = dateArr[0].split("-")[0];
		var month = dateArr[0].split("-")[1];
		var day = dateArr[0].split("-")[2];

		var hour = dateArr[1].split(":")[0];
		var minute = dateArr[1].split(":")[1];

		var date = new Date();
		date.setFullYear(year);
		date.setMonth(month-1);
		date.setDate(day);

		var result = date.getDay();//0-6 
		var week;
		if(result == 0){
			week = "周日";
		}else if(result == 1){
			week = "周一";
		}else if(result == 2){
			week = "周二";
		}else if(result == 3){
			week = "周三";
		}else if(result == 4){
			week = "周四";
		}else if(result == 5){
			week = "周五";
		}else if(result == 6){
			week = "周六";
		}
		var am;
		if(hour >=0 && hour<=5){
			am = "凌晨";
		}else if(hour >5 && hour<=11){
			am = "早上";
		}else if(hour == 12){
			am = "中午";
		}else if(hour >=13 && hour<18){
			am = "下午";
		}else if(hour>=18 && hour<=23){
			am = "晚上";
		}
		if(year == <%=year%>){
			if(month == <%=month%>){
				$("#timePS").html("本邮件将在本月"+day+"日"+week+am+hour+":"+minute+" 发送到对方邮箱");
			}else{
				$("#timePS").html("本邮件将在 "+month+"月"+day+"日"+week+am+hour+":"+minute+" 发送到对方邮箱");
			}
		}else{
			$("#timePS").html("本邮件将在"+year+"年"+month+"月"+day+"日"+week+am+hour+":"+minute+" 发送到对方邮箱");
		}
	}
}

function mailSend(){
	var divTo = $("#divTo").text();
	var divCc = $("#divCc").text();
	var divBcc = $("#divBcc").text();

	$("#to").val(divTo);
	$("#cc").val(divCc);
	$("#bcc").val(divBcc);
	var timeSend = document.getElementById("timeSend");
	if(form1_onsubmit()){
		if(timeSend.checked == true && form1.send_time.value != ""){
			form1.action = "pop3_sendTime_save.jsp?draftId=<%=draftId%>&sendTime=1&subMenu=<%=subMenu%>";
		}
		form1.submit();
	}
	$('#bodyBox').showLoading();
}
function saveDrafe(){
	var divTo = $("#divTo").text();
	var divCc = $("#divCc").text();
	var divBcc = $("#divBcc").text();

	$("#to").val(divTo);
	$("#cc").val(divCc);
	$("#bcc").val(divBcc);
	form1.action = "pop3_draft_save.jsp?draftId=<%=draftId%>&type=1";
	if(form1_onsubmit()){
		form1.submit();
	}
}
function chkIsLeave(){
	$("#isLeaveSendMail").dialog({
		title: "",
		modal: true,
		buttons: {
			//"取消": function() {
			//	$(this).dialog("close");
			//},
			"不存草稿": function() {
				history.go(-1);
			},
			"保存草稿": function() {
				leaveSaveDrafe();
				$(this).dialog("close");						
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true,
		width:500,
		height:120
		});
}
function leaveSaveDrafe() {
	var divTo = $("#divTo").text();
	var divCc = $("#divCc").text();
	var divBcc = $("#divBcc").text();

	$("#to").val(divTo);
	$("#cc").val(divCc);
	$("#bcc").val(divBcc);
	form1.action = "pop3_draft_leave_save.jsp?draftId=<%=draftId%>&subMenu=<%=subMenu%>";
	form1.submit();
	
}

function showCopyReceiver(){
	var times = $("#copyReceiverTimes").val();
	if(times == "0"){
		//document.getElementById("cc").focus();
		$("#divCc").focus();
		$("#common").attr("lt","divCc");
		$("#copyReceiverTR").css("display","");
		$("#copyReceiverTimes").val("1");
		$("#copyReceiverText").html("<img src=\"images/inbox-cc.png\" width=\"15\" height=\"15\"/>取消抄送");
		$("#divCc").text("");
	}
	if(times == "1"){
		$("#common").attr("lt","");
		$("#copyReceiverTR").css("display","none");
		$("#copyReceiverTimes").val("0");
		$("#copyReceiverText").html("<img src=\"images/inbox-cc.png\" width=\"15\" height=\"15\"/>抄送");
	}
}
function showBlindReceiver(){
	var times = $("#blindReceiverTimes").val();
	if(times == "0"){
		//document.getElementById("bcc").focus();
		$("#divBcc").focus();
		$("#common").attr("lt","divBcc");
		$("#blindReceiverTR").css("display","");
		$("#blindReceiverTimes").val("1");
		$("#blindReceiverText").html("<img src=\"images/inbox-bcc.png\" width=\"15\" height=\"15\"/>取消密送");
		$("#divBcc").text("");
	}
	if(times == "1"){
		$("#common").attr("lt","");
		$("#blindReceiverTR").css("display","none");
		$("#blindReceiverTimes").val("0");
		$("#blindReceiverText").html("<img src=\"images/inbox-bcc.png\" width=\"15\" height=\"15\"/>密送");
	}
}
function selectEmailAddr(myValue) { 
	var setVal = $("#common").attr("lt");
	var getVal = $("#"+setVal).text();
	var re = false;
	if(getVal == ""){
		$("#"+setVal).text(myValue);
	}else{
		var valArr = getVal.split(",");
		for(var i=0;i<valArr.length;i++){
			if(valArr[i] == myValue){
				re = true;
				return;
			}
		}
		if(!re){
			$("#"+setVal).text(getVal+","+myValue);
		}
	}
	
} 

function setValue(obj){
	$("#common").attr("lt",obj);
}
</script>
</head>
<body onload="window_load()">
<div id="bodyBox" style="height:100%;">
<div class="inbox-wrap">
   <div class="inbox-right">
	 <div class="inbox-toolbar">
       <div class="inbox-toolbar-send" onclick="mailSend()"><img src="images/inbox-send.png" width="20" height="20"/>发送</div>
       <div class="inbox-right-btnbox" onclick="saveDrafe()"><img src="images/inbox-save.png" width="20" height="20"/>存草稿</div>
       <div class="inbox-right-btnbox" onclick="chkIsLeave()"><img src="images/inbox-cancel.png" width="20" height="20"/>取消</div>
       <div class="inbox-right-btnbox" style="float:right;padding-top:8px;" id="blindReceiverText" onclick="showBlindReceiver()"><img src="images/inbox-bcc.png" width="15" height="15"/>密送</div>
       <div class="inbox-right-btnbox" style="float:right;padding-top:8px;" id="copyReceiverText"  onclick="showCopyReceiver()"><img src="images/inbox-cc.png" width="15" height="15"/>抄送</div>
     </div>
	</div>
</div>
<div id="rightMain" >
<div id="sendBox" style="overflow:auto;height:80%">
	<div  id="common" lt="" class="commonUser">
	    	<li class="text_li">
	    		常用联系人
	    	</li>
	    	<div class="commonLine"></div>
	    	<%
	    		EmailAddrDb emailAddrDb = new EmailAddrDb();
	    		String sql = "select id,email_addr from email_recently_addr where user_name = '"+privilege.getUser(request)+"' order by send_date desc limit 10";
	    		JdbcTemplate jt = new JdbcTemplate();
	    		ResultIterator ir = jt.executeQuery(sql);
	    		ResultRecord rr = null;
	    		while(ir.hasNext()){
	    			rr = (ResultRecord)ir.next(); 
	    			String emailAddr = rr.getString("email_addr");
	    			
	    			String newEmail = "";
	        		String subStartEmail = "";
	        		if(emailAddr.length()>=16){
	        			subStartEmail = emailAddr.substring(0,15);
	        			newEmail  = subStartEmail +"...";
	        		}else{
	        			newEmail = emailAddr;
	        		}
	    			
	    	%>
	    	<li title="<%=emailAddr %>">
	    		<a href="#" onclick="selectEmailAddr('<%=emailAddr %>')"><%=StrUtil.toHtml(newEmail) %></a>
	    	</li>
	    	<%} %>
	    </div>
	    <div class="mainClass">
			<form name="form1" enctype="multipart/form-data"  action="pop3_sendmail_do.jsp?id=<%=draftId %>&subMenu=<%=subMenu %>" method="post" onsubmit="return form1_onsubmit()">
			<table  width="100%" border="0" cellpadding="0" cellspacing="0" >
			    <tr style="display:none;">
			      <td>发件人：</td>
			      <td>
					  <input type="text" name="email" id="email" value="<%=mmd.getSender() %>" size="37"/>
			          <input name="username" type="hidden" value="<%=privilege.getUser(request)%>" />
			          <input name="id" type="hidden" value="<%=mmd.getId() %>" />
			     	  <div style="display:none">
							<input type="checkbox" value="true" name="isSaveToSendBox" checked />存至发件箱          
						</div>     
			     </td>
			    </tr>
			    <tr class="lineTr">
			      <td colspan="2">
			      	<div style="float:left;">收件人：</div><div contentEditable="true" id="divTo" onclick="setValue('divTo')" style="width:650px;outline:none;"><%=StrUtil.toHtml(mmd.getReceiver())%></div>
			      	<input class="p1" size="100" name="to" id="to" value="" type="hidden"/>
			      	<input type="hidden" name="copyReceiverTimes" id="copyReceiverTimes" value="0"/>
			      </td>
			    </tr>
			    <tr id="copyReceiverTR" style="display:none" class="lineTr">
			      <td colspan="2">
			      	<div style="float:left;">抄送人：</div><div contentEditable="true" id="divCc" onclick="setValue('divCc')" style="width:650px;outline:none;"><%=StrUtil.toHtml(cc)%></div>
			      	<input id="cc" class="p1" size="100"  name="cc" value="" type="hidden"/>
			      	<input type="hidden" name="blindReceiverTimes" id="blindReceiverTimes" value="0"/>
			      </td>
			    </tr>
			    <tr id="blindReceiverTR" style="display:none" class="lineTr">
			      <td colspan="2">
			      	<div style="float:left;">密送人：</div><div contentEditable="true" id="divBcc" onclick="setValue('divBcc')" style="width:650px;outline:none;"><%=StrUtil.toHtml(mmd.getBlindReceiver())%></div>
			      	<input id="bcc" class="p1" size="100"  name="bcc" value="" type="hidden"/>
			      </td>
			    </tr>
			    <tr class="lineSubject">
			      <td colspan="2">主　题：<input class="p1" size="100" name="subject" value="<%=mmd.getSubject()%>"/></td>
			    </tr>
			    <tr>
			      <td colspan="2">
					<div style="float:left"><script>initUpload()</script></div> 
					 <div style="padding-top:5px;">
			         	<img src="images/inbox-disk.png" alt="" />	 
				  		<a href="javascript:;" onclick="openWin('../netdisk/clouddisk_list.jsp?mode=select', 800, 600)">选择网盘文件</a>
				     </div>
					 <div id="netdiskFilesDiv" style="line-height:1.5"></div>
					 <ul class="attachments" style="list-style:none; margin-top:10px">
					<%
					  java.util.Iterator attir = mmd.getAttachments().iterator();
					  while (attir.hasNext()) {
						Attachment att = (Attachment)attir.next();
				  	%>
			          <li>
			          <input name="attachmentFiles" value="<%=att.getId()%>" type="hidden" />
			          <img src="../images/attach.gif" width="17" height="17" />&nbsp;<a target="_blank" href="email_getfile.jsp?id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a onclick="return confirm('您确定要删除么？')" href="?op=delattach&amp;id=<%=mmd.getId() %>&amp;draftId=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>">删除</a>
			          </li>
			        <%}%>
			        </ul>
			       
				  </td>
			    </tr>
			    
			    <tr>
			      <td colspan="2"><textarea class="p1" id="hidContent" name="hidContent" rows="20" wrap="physical" cols="65" style="display:none"><%=mmd.getContent()==null ?"":mmd.getContent()%></textarea>
			         
					<div id="myEditor" style="height:200px;"></div>
					<div id="hiddenDiv" style="display:none;padding-top:5px;">
			        	<input type="checkbox" name="emergent" id="emergent" value="1"/> 紧急
			        	<input type="checkbox" name="replySinge" id="replySinge" value="1"/> 回执
			        	<input type="checkbox" name="timeSend" id="timeSend" value="1" onclick="isSelected()"/> 定时发送
			        	<div id="dateSelect" class="dateDivClass" style="display:none">
			        		<div style="padding-top:10px;padding-bottom:10px;padding-left:10px;">
				        		<%
			        							String newDate = "";
			        							if(mmd.getSendTime() == null){
			        								newDate = "";
			        							}else{
			        								newDate = mmd.getSendTime().toString().split(":")[0]+":"+mmd.getSendTime().toString().split(":")[1];
			        							}
			        						%>
				        		发送时间：<input id="sendDate" name="sendDate" value="<%=newDate%>" size=20 onblur="changeDay()"/>&nbsp;
								        <input name="send_time" type="hidden" />
					        			<input name="receipt_state" type="hidden" />
					        			<input name="msg_level" type="hidden" />
							    </div>
							    <div style="padding-top:10px;padding-bottom:10px;padding-left:10px;" id="timePS">
							    	 本邮件将在 明天凌晨0 : 00 发送到对方邮箱
							    </div>
			        	</div>
			        </div>		
				</td>
			    </tr>
			   	<tr>
			     	<td colspan="2" align="right">
			     		发件人：<%=mmd.getSender() %>
			     		<a href="#" onclick="showDiv()" id="selectMore">更多选项</a>
			     		<input type="hidden" name="times" id="times" value="0"/>
			     	</td>
			     </tr>
			    
			</table>
			</form>
</div>
</div>
<div class="inbox-wrap">
   <div class="inbox-right">
	 <div class="inbox-toolbar">
       <div class="inbox-toolbar-send" onclick="mailSend()"><img src="images/inbox-send.png" width="20" height="20"/>发送</div>
       <div class="inbox-right-btnbox" onclick="saveDrafe()"><img src="images/inbox-save.png" width="20" height="20"/>存草稿</div>
     </div>
	</div>
</div>
</div>

<div id="isLeaveSendMail" style="display:none;text-align:center;padding-top:17px;">
	您正在写信中,是否离开写信页面？
</div>
</div>
</body>
<script>
$(function(){
	 $('#sendDate').datetimepicker({
     	lang:'ch',
     	timepicker:true,
     	format:'Y-m-d H:i',
     	formatDate:'Y/m/d H:i',
     	step:1
     });
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
