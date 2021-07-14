<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.message.MessageDb"%>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<% 
	String op = ParamUtil.get(request, "op");
	boolean isSuccess = false;
	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>消息回复</title>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery.form.js"></script>

<script>
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
	/**function SelectDateTime(objName) {
	var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
	if (dt!=null)
		findObj(objName).value = dt;
	}*/
	
	function SelectDateTime(objName) {
	    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
	}
	function sel(dt, objName) {
	    if (dt!=null && objName != "")
	        findObj(objName).value = dt.substring(0,5);
	}
	function openWin(url,width,height)
	{
	  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
	}
	
	function setVisibility() {
    
}
var uEditor;
function window_onload() {
	uEditor = UE.getEditor('myEditor',{
				initialContent : '',//初始化编辑器的内容  
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
				//默认的编辑区域高度
				initialFrameHeight:300,
				initialFrameWidth:756 
				///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
				//更多其他参数，请参考ueditor.config.js中的配置项
			});
}	

function form1_onsubmit()
	{
		errmsg = "";
		if (form1.title.value=="")
			errmsg += "请填写标题！\n"
		if(form1.title.value.length>200)
			errmsg += "不能大于200字符长度！\n"
		if (uEditor.getContentTxt()=="")
			errmsg += "请填写内容！\n"
		if ($("#send_now_1").attr("checked") == "checked")
        {
          if ($("input[name='date']").val() == "" || $("input[name='time']").val() == "")
          {
              errmsg += "请填写时间！\n"
          }
        }
		if (errmsg!="")
		{
			jAlert(errmsg,"提示");
			return false;
		}
		form1.send_time.value = form1.date.value;
		return true;
	}
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="window_onload()">
<div class="message_content">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
try {
		if(op.equals("myreply")){
			isSuccess = Msg.AddMsg(application, request);
			if(isSuccess){
				//json.put("ret","1");
				//json.put("msg","操作成功!");
				//out.print(json);
				//return;
				//alert("操作成功！");
				out.print(StrUtil.jAlert_Redirect("操作成功！","提示","message.jsp"));
			}else{
				//json.put("ret","0");
				//json.put("msg","操作失败!");
				//out.print(json);
				//return;
				//alert("操作失败！");
				out.print(StrUtil.jAlert_Redirect("操作失败！","提示","message.jsp"));
			}
		}
	}
	catch (ErrMsgException e) {
		out.println(SkinUtil.makeErrMsg(request, "消息发送失败："+e.getMessage()));
	}
if (!privilege.isUserLogin(request)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="message_tdStyle_1">回复消息</td>
    </tr>
  </tbody>
</table>
<%
int id = ParamUtil.getInt(request, "id");
MessageDb md = Msg.getMessageDb(id);
if (md==null || !md.isLoaded()) {
	out.print(StrUtil.Alert_Redirect("该消息已不存在！", "message.jsp"));
	return;
}
String title = md.getTitle();
String receiver = md.getSender();

try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "title", title, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "receiver", receiver, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
  
%>
<table class="tabStyle_1 percent98" align="center" style="margin-top:10px">
	<tr>
      <td class="message_btnbox" colspan="2" >
	      	<img src="../skin/bluethink/images/message/message_back.png" onclick="location.href='javascript:history.back()'"/>
	      	<img src="../skin/bluethink/images/message/message_furbish.png" onclick="window.location.reload()"/>
      	</td>
    </tr>
</table>
<form name="form1" method="post" action="myreply.jsp?op=myreply&id=<%=id %>" id="form1" enctype="multipart/form-data">
<table class="tabStyle_1 percent100" width="100%" border="0" cellspacing="0" cellpadding="3" align="center">
  <tr>
    <td class="tabStyle_1_title" height="26" colspan="2" align="center">回复消息</td>
  </tr>
  <tr>
    <td height="26" align="center">接 收 者：</td>
    <td height="26"><%
				com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
			  	ud = ud.getUserDb(receiver);
				String userRealName = ud.getRealName();
			    %>
        <input type="hidden" name="receiver" value="<%=receiver %>" />
        <input type="hidden" name="isDraft" value="false" />
		<%=userRealName%>
        <input type="hidden" name="userRealName" class="input1" size="20" maxlength="20" value="<%=userRealName %>" />    </td>
  </tr>
  <tr>
    <td height="26" align="center">消息标题：</td>
    <td height="26"><input type="text" name="title" id="title" class="input1" size="110" value="<%=title%>" />
    </td>
  </tr>
  <tr>
    <td height="26" align="center">消息内容：</td>
    <td height="26" style="width:90%;">
		   <div id="myEditor" style="height:200px"></div>  
    <!-- <textarea name="content" cols="50" rows="16"></textarea> -->
     </td>
  </tr>
  <tr>
    <td height="26" align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
    <td height="26"><script>initUpload()</script></td>
  </tr>
  <tr>
    <td align="right">发送时间：</td>
    <td align="left"><input type="radio" name="send_now" value="yes" id="send_now_0" checked="checked" onclick="setVisibility()" />
        <label for="send_now_0" onclick="setVisibility()">立即发送</label>
        <br />
        <input type="radio" name="send_now" value="no" id="send_now_1" onclick="setVisibility()" />
        <label for="send_now_1" onclick="setVisibility()">定时发送</label>
        <input name="date" id="date" readonly="readonly" />
        <input name="send_time" type="hidden" />    </td>
  </tr>
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>  
  <tr>
    <td align="right">手机短信提醒：</td>
    <td align="left"><input type="radio" name="isToMobile" value="true" id="isToMobile_0" checked="checked" />
        <label for="isToMobile_0">是</label>
        <input type="radio" name="isToMobile" value="false" id="isToMobile_1" />
        <label for="isToMobile_1">否</label>    </td>
  </tr>
<%}%>  
  <tr>
    <td align="right">保存到发件箱：</td>
    <td align="left"><input type="radio" name="isToOutBox" value="true" id="isToOutBox_0" checked="checked" />
        <label for="isToOutBox_0">是</label>
        <input type="radio" name="isToOutBox" value="false" id="isToOutBox_1" />
        <label for="isToOutBox_1">否</label>    </td>
  </tr>
  <tr>
    <td align="right">是否需要回执：</td>
    <td align="left"><input type="radio" name="receipt_state" value="1" id="receipt_state_0" checked="checked" />
        <label for="receipt_state_0">是</label>
        <input type="radio" name="receipt_state" value="0" id="receipt_state_1" />
        <label for="receipt_state_1">否</label>    </td>
  </tr>
  <tr>
    <td align="right">消息等级：</td>
    <td align="left"><input type="radio" name="msg_level" value="0" id="msg_level_0" checked="checked" />
        <label for="msg_level_0">普通</label>
        <input type="radio" name="msg_level" value="1" id="msg_level_1" />
        <label for="msg_level_1">紧急</label>    </td>
  </tr>
  <tr>
    <td align="right">&nbsp;</td>
    <td align="left">&nbsp;</td>
  </tr>
  
  
  <tr>
    <td height="26" colspan="2" align="center">
      <input type="submit" name="Submit" value="确定" class="blue_btn_90" onclick="return form1_onsubmit()"/>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="reset" name="Submit2" value="重写" class="grey_btn_90" /></td>
  </tr>
</table>
<br />
</form>
</div>
</body>
<script>
$(document).ready(function(){
	$('#date').datetimepicker({value:'',step:1, format:'Y-m-d H:i:00'});
	 
//	var options = { 
//		success:showResponse,  // post-submit callback 
//		beforeSubmit:    form1_onsubmit
//		}; 
//		$('#form1').submit(function() { 
//		   $(this).ajaxSubmit(options); 
//		   return false; 
//		});
		
//	});
//function showResponse(data)  {
//	data = $.parseJSON(data);
//	if(data.ret == "1"){
//		alert(data.msg);
//		window.location.href = "message.jsp";
//	}
});
</script>
</html>
