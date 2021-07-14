<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/><%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String uName = privilege.getUser(request);

String op = ParamUtil.get(request, "op");
boolean isSuccess = false;
JSONObject json = new JSONObject();
try {
	if(op.equals("reply")){
		isSuccess = Msg.AddMsg(application, request);
		if(isSuccess){
			json.put("ret","1");
			json.put("msg","操作成功!");
			out.print(json);
			return;
		}else{
			json.put("ret","0");
			json.put("msg","操作失败!");
			out.print(json);
			return;
		}
	}
	
}
catch (ErrMsgException e) {
	out.println(SkinUtil.makeErrMsg(request, "消息发送失败："+e.getMessage()));
}

int id = ParamUtil.getInt(request, "id");
MessageDb md = Msg.getMessageDb(id);
if (md==null || !md.isLoaded()) {
	out.print(StrUtil.Alert_Redirect("该消息已不存在！", "message.jsp"));
	return;
}
String title,content,rq,receiver,sender;
int type;
boolean isreaded;
boolean isdustbin;
boolean isSenderDustbin;
id = md.getId();
title = md.getTitle();
content = md.getContent();
type = md.getType();
rq = md.getRq();
receiver = md.getReceiver();
sender = md.getSender();
isdustbin = md.isDustbin();
isSenderDustbin = md.isSenderDustbin();
String receiverscs = md.getReceiverscs();
String receiversms = md.getReceiversms();
String receiversjs = md.getReceiversjs();

UserMgr um = new UserMgr();
String senderName = sender;
if (!sender.equals(MessageDb.SENDER_SYSTEM)) {
	senderName = um.getUserDb(sender).getRealName();
}

isreaded = md.isReaded();
int msgLevel = md.getMsgLevel();
int receipt = md.getReCeiptState();

if (md.getBox()==MessageDb.INBOX && receiver.equals(privilege.getUser(request))) {
	md.setReaded(true);
	md.save();
}


%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
<title>查看邮件</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
<script src="../inc/common.js"></script>
<script src="../inc/upload.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script src="../js/jquery.form.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>
<script>
var uEditor;
function loadMenu(){
	if (parent.leftFrame != null){
		parent.leftFrame.location.href="left_menu.jsp";
	}
	
	uEditor = UE.getEditor('myEditor',{
		initialContent : '<span style="color:gray;font-size:14px;">快速回复</span>',//初始化编辑器的内容  
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
		initialFrameHeight:150
		///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
		//更多其他参数，请参考ueditor.config.js中的配置项
	});
	
	 uEditor.addListener('focus', function(){
     	var content = uEditor.getContentTxt();
     	if(content=="快速回复"){
     		 uEditor.setContent("");
     	}
     }); 
     uEditor.addListener('blur', function(){
     	var content = uEditor.getContent();
     	if(content==""){
     		 uEditor.setContent("<span style='color:gray;font-size:14px;'>快速回复</span>");
     	}
     }); 
	
}
function chkReceipt() {
	jConfirm('消息发送者需要回执，现在就发送回执么？','提示',function(r){
		if(!r){return;}
		else{
			window.location.href = 'do_receipt.jsp?id=<%=id%>';
		}
	})
}

function form_onsubmit()
{
	errmsg = "";
	if (uEditor.getContentTxt()=="" || uEditor.getContentTxt() == '快速回复')
		errmsg += "请填写内容！\n"
	
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}else{
		return true;
	}
	
}


function showMore(id){
	var isShow = o("isShow"+id).value;
	var divArr = document.getElementsByName("div"+id);
	if(isShow == 'true'){
		for(var i=1;i<divArr.length;i++){
			divArr[i].style.display = "none";
		}
		o("isShow"+id).value = false;
		$('html').animate({scrollTop: document.documentElement.scrollTop-100}, "normal");
	}else{
		for(var i=0;i<divArr.length;i++){
			divArr[i].style.display = "inline";
		}
		o("isShow"+id).value = true;
		$('html').animate({scrollTop: document.documentElement.scrollTop+100}, "fast");
	}
}
function isChat(id){
	window.location.href = "showmsg.jsp?id="+id;
	<%
		String userName = privilege.getUser(request);
		UserSetupDb userSetupDb = new UserSetupDb();
		userSetupDb = userSetupDb.getUserSetupDb(userName);
		userSetupDb.setMsgChat(false);
		userSetupDb.save();
	%>
}
</script>
</head>
<body onload="loadMenu()">
<div class="message_content">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="message_tdStyle_1">查看邮件</td>
    </tr>
  </tbody>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" align="center">
	<tr>
      <td colspan="2" class="message_btnbox">
	      	<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_back.png" onclick="javascript:history.go(-1)" />
	      	<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_furbish.png" onclick="window.location.reload()" />
	      	<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_revert.png" onclick="parent.rightFrame.location.href='myreply.jsp?id=<%=id%>'"/>
	      	<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_close.png"   onClick="doDel(<%=id%>,<%=isdustbin %>)"/>
	      	<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_transmit.png"  onClick="window.location.href='transmit.jsp?id=<%=id%>'"/>
	      	<img src="<%=SkinMgr.getSkinPath(request)%>/images/message/message_mode-1.png" id="img"  onClick="isChat(<%=id %>)"/>
      	</td>
    </tr>
</table>
  <form name="form1" method="post" action="myreply.jsp?id=<%=id%>">
  <table class="message_table_Style_1 percent98" align="center" style="margin-top:10px">
  	<input type="hidden" name="title" value="<%="RE:"+title%>">
    <input name="receiver" value="<%=sender%>" type="hidden">

	    <tr class="message_tableStyle_1_tr">
	    	<td width="50">主题:</td>
	    	<%
	    		String title1 = title;
	    		if(title.length()>30){
		  	 		title = title.substring(0,30)+"......";
		  		}
	    	%>
	    	<td title="<%=title1 %>"><%=title %></td>
	    </tr>
	    <tr class="message_tableStyle_1_tr">
	    	<td>发件人:</td>
	    	<td><%=senderName %></td>
	    </tr>
	    <tr class="message_tableStyle_1_tr">
	    	<td>收件人:</td>
	    	<%
	    		String realNamejs = "";
	    		if(receiversjs != null && !"".equals(receiversjs)){
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
	    	%>
	    	<td><%=realNamejs %></td>
	    </tr>
	    <%if(receiverscs != null && !"".equals(receiverscs)){ 
	    	String[] recArry = receiverscs.split(",");
	    	String realNameAll = "";
	    	for(int t = 0;t<recArry.length;t++){
	    		UserDb udb1 = new UserDb();
	    		udb1 = udb1.getUserDb(recArry[t]);
	    		if("".equals(realNameAll)){
	    			realNameAll = udb1.getRealName();
	    		}else{
	    			realNameAll += "," + udb1.getRealName();
	    		}
	    	}
	    %>
	    
	    <tr class="message_tableStyle_1_tr">
	    	<td>抄送:</td>
	    	<td><%=realNameAll %></td>
	    </tr> 
	    <%} %> 
	    <%if(sender.equals(uName) && receiversms != null && !"".equals(receiversms)){ 
	    	String[] recArry = receiversms.split(",");
	    	String realNameAll2 = "";
	    	for(int t = 0;t<recArry.length;t++){
	    		UserDb udb1 = new UserDb();
	    		udb1 = udb1.getUserDb(recArry[t]);
	    		if("".equals(realNameAll2)){
	    			realNameAll2 = udb1.getRealName();
	    		}else{
	    			realNameAll2 += "," + udb1.getRealName();
	    		}
	    	}
	    %>
	    
	    <tr class="message_tableStyle_1_tr">
	    	<td>密送:</td>
	    	<td><%=realNameAll2 %></td>
	    </tr> 
	    <%} %> 
	    <tr style="border-bottom:1px solid #ACDCDC" class="message_tableStyle_1_tr">
	    	<td >时间:</td>
	    	<td><%=rq %></td>
	    </tr>
	    <%if(!sender.equals(uName) && receiversms != null && receiversms.contains(uName) && (receiversjs == null || !receiversjs.contains(uName)) && (receiverscs == null || !receiverscs.contains(uName))){ %>
	    	<tr style="background:#FFF1B4"><td>提示：</td><td>你是这封邮件的密送人，所以不会显示在收件人中。</td></tr>  
	    <%} %>
		<tr>
			<td colspan="2" style="word-break:break-all;height:150px;padding-top:10px;" valign="top">
				<%
				//String body = content; // StrUtil.toHtml(content);
				%>
	              <%=StrUtil.ubb(request, MessageDb.toHtml(content), true)%>
			</td>
		</tr>
          <tr>
            <td colspan="2" >
			<div>
            <%
			String actionLink = md.renderAction(request);
			if (!actionLink.equals("")) {
				out.print("<BR><font style='font-family:宋体;'>>></font>&nbsp;" + actionLink);
			}
			%>
            <div>
			<%
			Iterator ir = md.getAttachments().iterator();
			while (ir.hasNext()) {
				Attachment att = (Attachment)ir.next();
			%>
              <img src="../images/attach2.gif" align="absmiddle" /> <a href="getfile.jsp?attachId=<%=att.getId()%>&msgId=<%=md.getId()%>" target="_blank"><%=att.getName()%></a>
			  &nbsp;&nbsp;
			  <!-- <a href="javascript:;" onclick="saveToNetdisk(<%=att.getId()%>)">存至网盘</a>  -->           
              <%}%>
			</div>
            </div>
            </td>
          </tr>
          <!-- <tr>
            <td align="center" colspan="2">
                <%if (md.getBox() == MessageDb.DRAFT) {%>
                <input class="btn" type="button" name="Button" value=" 发送 " onClick="window.location.href='send_do.jsp?op=sendDraft&id=<%=md.getId()%>'">
                <%}else{
			  	if (type != MessageDb.TYPE_SYSTEM && !md.getSender().equals(privilege.getUser(request))) {
			  %>
                <input class="btn" type="submit" name="Button" value=" 回复 ">
                <%}%>
                &nbsp;&nbsp;
                <input class="btn" name="button" type="button" onClick="window.location.href='transmit.jsp?id=<%=id%>'" value=" 转发 ">
                <%}%></td>
          </tr> -->
        </table>
  </form>
  <%if(isdustbin == false && isSenderDustbin == false){ %>
  <form action="" method="post" enctype="multipart/form-data" name="form2" id="form2">
  	<table width="100%">
  		<input type="hidden" name="title" id="replyTitle" value="<%=title1%>"/>
  		<input type="hidden" name="id" id="replyId" value="<%=id%>"/>
    	<%
				com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
			  	ud = ud.getUserDb(receiver);
				String userRealName = ud.getRealName();
			    %>
        <input type="hidden" name="receiver" value="<%=sender%>" />
        <input type="hidden" name="isDraft" value="false" />
        <input type="hidden" name="userRealName" class="input1" size="20" maxlength="20" value="<%=userRealName %>" />
  	
  		<tr>
  			<td colspan="2" class="showMsg_Table_td" style="width:95%;">
  				<div id="myEditor" style="height:100px;"></div>  
  				<!--  <textarea cols="50" rows="4" style="width:850px;" name="content" id="content" onfocus="mouseFocus()" onblur="mouseBlur()" >快速回复</textarea>-->
  			</td>
  		</tr>
  		<tr>
		    <td colspan="2" class="showMsg_Table_td"><script>initUpload()</script></td>
		 </tr>
  		<tr class="message_style_tr">
  			<td colspan="2" class="showMsg_Table_td" align="center">
  				<input name="button" type="submit" value="确定"  style="margin-top:10px" class="blue_btn_90"/>
  			</td>
  		</tr>
  	</table>
  </form>
  </div>
  <%} %>
<script>
<%
	if(receipt == 1) {
%>
	chkReceipt();
<%
	}
%>
</script>
</body>
<script>
 $(document).ready(function(){ 
 	<%if(isdustbin == false && isSenderDustbin == false){ %>
 		//document.getElementById("content").style.color="gray";
 	<%}%>
	var options = { 
		success:showResponse,  // post-submit callback 
		beforeSubmit:    form_onsubmit,
		url:"showmsg.jsp?op=reply"
		}; 
		$('#form2').submit(function() { 
		   $(this).ajaxSubmit(options); 
		   return false; 
		});
	});
function showResponse(data){
	data = $.parseJSON(data);
	if(data.ret == "1"){
		alert(data.msg);
		window.location.href="showmsg.jsp?id="+$("#replyId").val()+"&title="+$("#replyTitle").val();
		//window.location.href = "message.jsp";
		//parent.leftFrame.location.href="left_menu.jsp";
	}
}
 function mouseFocus(){
 	var cont = document.getElementById("content").value;
 	if(cont=="快速回复"){
 		document.getElementById("content").style.color="";
 		document.getElementById("content").value="";
 	}
 }
 function mouseBlur(){
 	var cont = document.getElementById("content").value;
 	if(cont==""){
 		document.getElementById("content").value="快速回复";
 		document.getElementById("content").style.color="gray";
 	}
 }
	


function saveToNetdisk(attId) {
	$("#dlg").dialog({
		title: "请选择目录",
		modal: true,
		// bgiframe:true,
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				$.ajax({
					type: "post",
					url: "../mail/mail_do.jsp",
					data: {
						op: "saveToNetdisk",
						dirCode: o("dirCode").value,
						mailId: <%=id%>,
						attachmentId: attId
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						$('#rightMain').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						else {
							jAlert(data.msg, "提示");
						}
					},
					complete: function(XMLHttpRequest, status){
						$('#rightMain').hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						alert(XMLHttpRequest.responseText);
					}
				});	
				$(this).dialog("close");						
			}
		},
		closeOnEscape: true,
		draggable: true,
		resizable:true,
		width:300,
		height:100
		});
			
}
///信息删除
function doDel(id,isdustbin){
	if(isdustbin == false){//收件箱详细信息删除
		jConfirm('您确定要彻底删除么？','提示',function(r){
			if(!r){return;}
			else{
				$.ajax({
					type: "post",
					url: "message.jsp",
					data : {
						op: "del",
						ids: id
			        },
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						//ShowLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						jAlert(data.msg,"提示");
						if(data.ret == "1"){
							window.location.href = "message.jsp?"+data.url;
						}
					},
					complete: function(XMLHttpRequest, status){
						//HideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});		
			}
		})
			//window.location.href='message.jsp?op=del&ids='+id;
	}else{//垃圾信箱详细信息删除
		jConfirm('您确定要彻底删除么？','提示',function(r){
			if(!r){return;}
			else{
				$.ajax({
					type: "post",
					url: "listrecycle.jsp",
					data : {
						op: "del",
						ids: id
			        },
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
						//ShowLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						jAlert(data.msg,"提示");
						if(data.ret == "1"){
							window.location.href = "listrecycle.jsp?"+data.url;
						}
					},
					complete: function(XMLHttpRequest, status){
						//HideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});
			}
		})
			//window.location.href='listrecycle.jsp?op=del&ids='+id;
	}
}
</script>
</html>