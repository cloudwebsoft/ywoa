<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>转发消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery.form.js"></script>
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
<%
String op = ParamUtil.get(request, "op");
boolean isSuccess = false;
try {
	if (op.equals("sendDraft")) {
		isSuccess = Msg.TransmitMsg(application, request);
		if(isSuccess){
			out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "listoutbox.jsp"));	
			return;
		}else{
			out.print(StrUtil.jAlert_Back("操作失败！", "提示"));	
			return;
		}
	}
}
catch (ErrMsgException e) {
	out.print(StrUtil.jAlert_Back("消息转发失败：" + com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage()), "提示"));	
	return;
}

String name = privilege.getUser(request);
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(name);
int messageToMaxUser = usd.getMessageToMaxUser();
%>

</head>

<body text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="window_onload()">
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
int id = ParamUtil.getInt(request, "id");
MessageDb msg = new MessageDb();
msg = (MessageDb)msg.getMessageDb(id);

if (op.equals("return")) {
	isSuccess = msg.doReturn(name, id);
	if(isSuccess){
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "draft_send.jsp?id=" + id));	
		return;
	}else{
		out.print(StrUtil.jAlert_Redirect("操作失败！", "提示", "draft_send.jsp?id=" + id));	
		return;
	}
}

boolean canDoReturn = msg.canDoReturn(name, id);

String receiversAll = msg.getReceiversAll();
String receiverscs = msg.getReceiverscs();
String receiversms = msg.getReceiversms();
String[] ary = receiversAll.split(",");
String receiversAllName = "";
int len = ary.length;
UserMgr um = new UserMgr();
for (int i=0; i<len; i++) {
	// 检查用户是否存在
	UserDb user = um.getUserDb(ary[i]);
	if (user.isLoaded()) {
		if (receiversAllName.equals("")){
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
<div class="message_content">
<%
if (!privilege.isUserLogin(request)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tr>
	<td class="message_tdStyle_1">编辑消息</td>
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
  <form action="draft_send.jsp?op=sendDraft" method="post" name="form1" id="form1" >
 	<table class="tabStyle_1 percent100" align="center">
 		 <tr>
			<td class="tabStyle_1_title" colspan="2">消息</td>
  		</tr>
          <tr> 
            <td width="120" align="right">收件人：</td>
            <td>   
              <input type="hidden" name="receiver" value="<%=receiversjs%>">
              <input type="text" readonly name="userRealName" size="110" value="<%=realNamejs%>">
			  <input type="hidden" name="isDraft" value="false">
			  <input type="hidden" name="id" value="<%=id%>"><p>
			  <img src="../skin/bluethink/images/message/message_icon_uesr.png"  style="height:15px;width:15px;margin-top:5px;"/>
		    <a href="#" onClick="openWinUsers(1)">选择用户</a>&nbsp;&nbsp;
            <span style="display:none">
	        <img src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(1)">我的用户组</a>&nbsp;&nbsp;</span>|&nbsp;&nbsp;
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
			        <input type="text" readonly name="userRealName1" size="110" value="<%=realNamecs %>"><p>
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
			        <input type="text" readonly name="userRealName2" size="110" value="<%=realNamems %>"><p>
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
            <td align="right">消息标题：</td>
            <td><input type="text" name="title" size="110" value="<%=StrUtil.toHtml(msg.getTitle())%>"/>
            </td>
          </tr>
          
          <tr> 
            <td align="right">消息内容：</td>
             <td style="width:90%;"> 
             <div id="myEditor" style="height:200px"></div>  
            <!-- <textarea name="content" cols="50" rows="12"><%=msg.getContent()%></textarea>	 -->
            </td>
          </tr>
		<%		  
		if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
		%>
          <tr>
            <td align="right">提醒</td>
            <td><input name="isToMobile" value="true" type="checkbox" checked="checked" />短信</td>
          </tr>
		<%}%>
		<%
		if (msg.getAttachments().size()>0) {
		%>
		  <tr>
            <td height="26" colspan="2" align="left"><%
			java.util.Iterator ir = msg.getAttachments().iterator();
			while (ir.hasNext()) {
				Attachment att = (Attachment)ir.next();
			%>
              <img src="../images/attach2.gif" align="absmiddle" /> <a href="getfile.jsp?attachId=<%=att.getId()%>&msgId=<%=msg.getId()%>" target="_blank"><%=att.getName()%></a> <BR>
              <%}%></td>
          </tr>
<%}%>		  
          <tr> 
            <td height="26" colspan="2" align="center"> 
			<% if(msg.getBox()==MessageDb.OUTBOX){ %>
				<input class="blue_btn_90" type="submit" name="Submit" value="重新发送">
				<input name="isToOutBox" value="true" type="checkbox" checked style="display:none">
			<%}else{%>
                <input class="btn" type="submit" name="Submit" value="发送">
			<%}%>	
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                &nbsp;&nbsp;&nbsp;&nbsp;    
				<% if(msg.getBox()!=MessageDb.OUTBOX){ %>    
				      <input name="isToOutBox" value="true" type="checkbox" checked> 存至发件箱
				<%}%>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                &nbsp;&nbsp;&nbsp;&nbsp;   
				<%if (canDoReturn) {%>
					<input class="blue_btn_90" type="button" name="Submit" value="撤回" onclick="dosubmit()">
				<%}%>
				</td>
          </tr>
        </table>
  </form>
  </div>
</body>
<script>
var type;
function setPerson(deptCode, deptName, user, userRealName)
{
	if (type == 1) {
		form1.receiver.value = user;
		form1.userRealName.value = userRealName;
	} else if (type == 2) {
		form1.receiver1.value = users;
		form1.userRealName1.value = userRealNames;
	} else if (type == 3) {
		form1.receiver2.value = users;
		form1.userRealName2.value = userRealNames;
	}
}

function getSelUserNames() {
	if (type == 1) {
		return form1.receiver.value;
	} else if (type == 2) {
		return form1.receiver1.value;
	} else if (type == 3) {
		return form1.receiver2.value;
	}
}

function getSelUserRealNames() {
	if (type == 1) {
		return form1.userRealName.value;
	} else if (type == 2) {
		return form1.userRealName1.value;
	} else if (type == 3) {
		return form1.userRealName2.value;
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

var messageToMaxUser = <%=messageToMaxUser%>;
function setUsers(users, userRealNames) {
	var ary = users.split(",");
	var len = ary.length;
	if (len>messageToMaxUser) {
		jAlert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！","提示");
		return;
	}
	if (type == 1) {
		form1.receiver.value = users;
		form1.userRealName.value = userRealNames;
	} else if (type == 2) {
		form1.receiver1.value = users;
		form1.userRealName1.value = userRealNames;
	} else if (type == 3) {
		form1.receiver2.value = users;
		form1.userRealName2.value = userRealNames;
	}
}
//-->
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,top="+(screen.height-600)/2+",left=" +(screen.width-800)/2 + ",fullscreen=3,width=792,height=550 ");
}

function openChrom(){
	if (navigator.userAgent.toLowerCase().match(/chrome/)!=null){
		openWin('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;');
	}else{
		showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;');
	}
}

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

function form1_onsubmit()
{
	errmsg = "";
	if (form1.receiver.value=="")
		errmsg += "请填写接收者！\n"
	if (form1.title.value=="")
		errmsg += "请填写标题！\n"
	if(form1.title.value.length>200)
		errmsg += "不能大于200字符长度！\n"
	if (uEditor.getContentTxt()=="")
		errmsg += "请填写内容！\n"
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
}

//$(document).ready(function(){ 
//	var options = { 
//		success:showResponse,  // post-submit callback 
//		beforeSubmit:    form1_onsubmit
//		}; 
//		$('#form1').submit(function() { 
//		   $(this).ajaxSubmit(options); 
//		   return false; 
//		});
//	});

function showResponse(data)  {
	data = $.parseJSON(data);
	alert(data.msg);
	$(".loading").css({"display":"none"});
	$(".treeBackground").css({"display":"none"});
	$(".treeBackground").removeClass("SD_overlayBG2");
	if (data.ret == "1") {
		window.location.href = "message.jsp";
		parent.leftFrame.location.href="left_menu.jsp";
	}
}

function dosubmit() {
	jConfirm("邮件撤回后对方将无法读取您的这封邮件！\n您确定要撤回邮件么？","提示",function(r){
		if (!r) {
			return
		} else{
			form1.action = "draft_send.jsp?op=return";
			form1.submit();
		}
	});
}

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
</html>
