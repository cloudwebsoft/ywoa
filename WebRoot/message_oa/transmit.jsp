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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
<title>转发消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery.form.js"></script>
<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
<%
String name = privilege.getUser(request);
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int id = ParamUtil.getInt(request, "id");
MessageDb msg = new MessageDb();
msg = (MessageDb)msg.getMessageDb(id);
String op = ParamUtil.get(request, "op");
boolean isSuccess = false;
//JSONObject json = new JSONObject();
if (op.equals("saveTransmit")) {
	try {
		isSuccess = Msg.TransmitMsg(application, request);
		if(isSuccess){
			//json.put("ret","1");
			//json.put("msg","操作成功!");
			//out.print(json);
			out.print("<script>parent.leftFrame.location='left_menu.jsp';</script>");
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "message.jsp"));
		}else{
			//json.put("ret","0");
			//json.put("msg","操作失败!");
			//out.print(json);
			out.print(StrUtil.jAlert("操作失败！","提示"));
		}
	} catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back("消息转发失败：" + com.cloudwebsoft.framework.security.AntiXSS.clean(e.getMessage())));	
	}
	return;
}
%>
<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(name);
int messageToMaxUser = usd.getMessageToMaxUser();
%>

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
		errmsg += "请填写收件人！\n"
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
	}else{
		return true;
	}
}
</script>
</head>

<body text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="window_onload()">
<div class="message_content">
<%
if (!privilege.isUserLogin(request)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tr>
	<td class="message_tdStyle_1">转发消息</td>
  </tr>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
	<tr>
      <td class="message_btnbox" colspan="2">
	      	<img src="../skin/bluethink/images/message/message_back.png" onclick="location.href='javascript:history.back()'" />
	      	<img src="../skin/bluethink/images/message/message_furbish.png" onclick="window.location.reload()" />
      	</td>
    </tr>
</table>

  <form action="?op=saveTransmit&id=<%=id%>" method="post" name="form1" id="form1" enctype="multipart/form-data">
 	<table class="tabStyle_1 percent100" width="100%" border="0" cellspacing="0" cellpadding="3" align="center">
          <tr>
            <td class="tabStyle_1_title" colspan="2">转发</td>
          </tr>
          <tr> 
            <td width="120" align="right"> 
            收件人：</td>
            <td>
              <input type="hidden" name="receiver" id="receiver" value=""/>
              <input type="text" readonly name="userRealName" size="110" value=""/>
			  <input type="hidden" name="isDraft" value="false">
			  <input type="hidden" name="id" id="id" value="<%=id%>">
			  <p>
						<img src="../skin/bluethink/images/message/message_icon_uesr.png" style="cursor: pointer;height:15px;width:15px;margin-top:7px"/>
		    <a href="#" onClick="openWinUsers(1)">选择用户</a>&nbsp;&nbsp;
		    <img
							src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(1)">我的用户组</a>&nbsp;&nbsp;|&nbsp;&nbsp;
        <span id="spca"><a href="javascript:;" onClick="addcs()" style="text-decoration:underline">添加抄送</a></span>
	<span id="spcs" style="display:none"><a href="javascript:;" onClick="deletecs()" style="text-decoration:underline">删除抄送</a></span>
	&nbsp;-&nbsp;
		<span id="spma"><a href="javascript:;" onClick="addms()" style="text-decoration:underline">添加密送</a></span>
		<span id="spms" style="display:none"><a href="javascript:;" onClick="deletems()" style="text-decoration:underline">删除密送</a></span>
		    </td>
          </tr>
          <tr id="trc" style="display:none">
    <td width="120" align="right">抄送：</td>
    <td>
    <input type="hidden" name="receiver1">
        <input type="text" readonly name="userRealName1" size="110">
        <p>
		<img src="../skin/bluethink/images/message/message_icon_uesr.png"  style="height:15px;width:15px;margin-top:7px"/>
        <a href="#" onClick="openWinUsers(2)">选择用户</a>&nbsp;&nbsp;
        <img
							src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(2)">我的用户组</a>
    </td>
  </tr>
  
  <tr id="trm" style="display:none">
    <td width="120" align="right">密送：</td>
    <td>
    <input type="hidden" name="receiver2">
        <input type="text" readonly name="userRealName2" size="110">
        <p>
		<img src="../skin/bluethink/images/message/message_icon_uesr.png"  style="height:15px;width:15px;margin-top:7px"/>
        <a href="#" onClick="openWinUsers(3)">选择用户</a>&nbsp;&nbsp;
        <img
							src="../skin/bluethink/images/message/message_icon_usergroup.png"
							style="cursor: pointer; height: 15px; width: 15px; " />
						<a href="javascript:;" onClick="openWinPersonGroup(3)">我的用户组</a>
    </td>
  </tr>
          <tr> 
            <td align="right"> 
            消息标题：           </td>
            <td >
            <input type="text" name="title" size="110" value="<%=msg.getTitle()%>"/></td>
          </tr>
          <tr> 
            <td align="right" > 
            消息内容：			</td>
            <td style="width:90%;"> 
			<div id="myEditor" ></div>
           <!--  <textarea name="content" cols="80" rows="16"><%=msg.getContent()%></textarea> -->			
          </td>
          </tr>
          <tr>
            <td align="right">保存至发件箱</td>
            <td><input name="isToOutBox" value="true" type="radio" checked="checked" />是
<input name="isToOutBox" value="false" type="radio" />否</td>
          </tr>
		<%
		if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
		%>		  
          <tr>
            <td align="right">手机短信提醒</td>
            <td><input name="isToMobile" value="true" type="radio" checked="checked" />是
			<input name="isToMobile" value="false" type="radio" />否
			</td>
          </tr>
		  <%}%>
		  <%
		  java.util.Vector v = msg.getAttachments();
		  if (v.size()>0) {
		  %>
          <tr>
            <td height="26" colspan="2" align="left">
			<%
			java.util.Iterator ir = v.iterator();
			while (ir.hasNext()) {
				Attachment att = (Attachment)ir.next();
			%>
              <img src="../images/attach.gif" align="absmiddle" /> <a href="getfile.jsp?attachId=<%=att.getId()%>&msgId=<%=msg.getId()%>" target="_blank"><%=att.getName()%></a> <BR>
              <%}%></td>
          </tr>
		  <%}%>
          <tr> 
            <td height="26" colspan="2" align="center"> 
                <input type="submit" name="Submit" value=" 转发 " class="blue_btn_90">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                <input type="reset" name="Submit2" value=" 重写 " class="grey_btn_90">
            &nbsp;&nbsp;&nbsp;&nbsp;</td>
          </tr>
 
</table>
</form>
</div>
</body>
<script>
//$(document).ready(function(){ 
	//var options = { 
		//success:showResponse,  // post-submit callback 
		//beforeSubmit:    form1_onsubmit,
		//url:"transmit.jsp?op=saveTransmit&id=<%=id%>"
		//}; 
		//$('#form1').submit(function() { 
		//	$(this).ajaxSubmit(options); 
		//	return false; 
		//});
		
	//});
function showResponse(data)  {
	try {
		data = $.parseJSON(data);
		if(data.ret == "1"){
			alert(data.msg);
			window.location.href = "message.jsp";
			parent.leftFrame.location.href="left_menu.jsp";
		}
	} catch (e) {
		alert(data);
	}
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
