<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.netdisk.Leaf"%>
<%@page import="org.json.JSONObject"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Iterator"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="userpop3setup" scope="page" class="com.redmoon.oa.emailpop3.UserPop3Setup"/>
<jsp:useBean id="sendmail" scope="page" class="com.redmoon.oa.emailpop3.SendMail" />
<%
String op = ParamUtil.get(request, "op");
String from = ParamUtil.get(request, "from");
MailMsgMgr mmm = new MailMsgMgr();
int id = ParamUtil.getInt(request, "id", -1);
int subMenu = ParamUtil.getInt(request, "subMenu", -1);
int subMenuButton = ParamUtil.getInt(request, "subMenuButton", -1);
int box = ParamUtil.getInt(request, "box", -1);
int isDelete = ParamUtil.getInt(request, "isDelete", -1);
String emailAddr = ParamUtil.get(request,"emailAddr");
if (id==-1) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "err_id")));
	return;	
}

EmailPop3Db eamilPop3Db = new EmailPop3Db();
eamilPop3Db = eamilPop3Db.getEmailPop3Db(privilege.getUser(request),emailAddr);


MailMsgDb mmd = null;
int isReaded = 0;
int receiptState = 0;
int msgType = 0;
try {
	mmd = mmm.getMailMsgDb(request, id);
	isReaded = mmd.isReaded()?1:0;
	receiptState = mmd.getReceiptState();
	msgType = mmd.getType();
	if (!mmd.isReaded()) {
		mmd.setReaded(true);
		mmd.save();
	}
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
if(op.equals("receiptOptions")){
	sendmail.getMailInfo(request);
	boolean re = sendmail.send();
	JSONObject json = new JSONObject();
	if(re){
		json.put("ret", 1);
		json.put("msg", "回执成功！");	
		out.print(json.toString());
		return;
	}else{
		json.put("ret", 0);
		json.put("msg", "回执失败！");	
		out.print(json.toString());
		return;
	}
}
if(op.equals("del")){
	if(box == MailMsgDb.TYPE_DUSTBIN){
		mmm.del(id,false);
	}else{
		mmm.del(id,true);
	}
	SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String sql = "";
	if(box == MailMsgDb.TYPE_INBOX){
		sql = "select id from email where sender = '"+mmd.getSender()+"' and mydate <='"+sd.format(mmd.getMyDate())+"' and msg_type = "+ MailMsgDb.TYPE_INBOX+" order by mydate desc";
	}else if(box == MailMsgDb.TYPE_SENDED){
	 	sql = "select id from email where sender = '"+mmd.getSender()+"' and mydate <='"+sd.format(mmd.getMyDate())+"' and msg_type = "+ MailMsgDb.TYPE_SENDED+" order by mydate desc";
	}else if(box == MailMsgDb.TYPE_DUSTBIN){
		sql = "select id from email where sender = '"+mmd.getSender()+"' and mydate <='"+sd.format(mmd.getMyDate())+"' and msg_type = "+ MailMsgDb.TYPE_DUSTBIN+" order by mydate desc";
	}
	MailMsgDb mailMsgDb = null;
	Iterator ir = mmd.list(sql).iterator();
	while(ir.hasNext()){
		mailMsgDb = new MailMsgDb();
		mailMsgDb = (MailMsgDb)ir.next();
		break;
	}
	if(mailMsgDb != null){
		response.sendRedirect("mail_show.jsp?id=" + mailMsgDb.getId() + "&emailAddr=" + mailMsgDb.getEmailAddr()+"&subMenu="+subMenu+"&subMenuButton="+subMenuButton+"&isDelete=1&box="+box);
	}else{
		EmailPop3Db epd = new EmailPop3Db();
		epd = epd.getEmailPop3Db(privilege.getUser(request), mmd.getEmailAddr());
		if(box == MailMsgDb.TYPE_INBOX){
			response.sendRedirect("in_box.jsp?id=" + epd.getId() + "&box=" + MailMsgDb.TYPE_INBOX + "&sender=" + mmm.getMailMsgDb().getSender()+"&subMenu="+subMenu+"&subMenuButton="+subMenuButton);
		}else if(box == MailMsgDb.TYPE_SENDED){
			response.sendRedirect("list_box.jsp?id=" + epd.getId() + "&box=" + MailMsgDb.TYPE_SENDED + "&sender=" + mmm.getMailMsgDb().getSender()+"&subMenu="+subMenu+"&subMenuButton="+subMenuButton);
		}else if(box == MailMsgDb.TYPE_DUSTBIN){
			response.sendRedirect("list_delete.jsp?id=" + epd.getId() + "&box=" + MailMsgDb.TYPE_DUSTBIN + "&sender=" + mmm.getMailMsgDb().getSender()+"&subMenu="+subMenu+"&subMenuButton="+subMenuButton);
		}
		
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>查看邮件</title>
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery.toaster.email.js"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link href="../skin/outside_mail.css" type="text/css" rel="stylesheet" />
<script>

function window_load(){
	parent.leftFrame.location.href="left_menu.jsp?popId=<%=eamilPop3Db.getId()%>&subMenu=<%=subMenu%>&subMenuButton=<%=subMenuButton%>";

	if("<%=isDelete%>" == "1"){
		$.toaster({priority : 'info', message : "删除成功" });
	}
}
function form1_onsubmit() {
}
function saveDrafe() {
	form1.action = "pop3_draft_save.jsp";
	form1.content.value = getHtml();
	if (form1.content.value.length>3000) {
	}
	form1.submit();
}
function chkReceipt(){
	$("#receiptDiv").dialog({
		title: "",
		modal: true,
		buttons: {
			"不发送": function() {
				$(this).dialog("close");
			},
			"发送": function() {
				$.ajax({
					type: "post",
					url: "mail_show.jsp",
					data: {
						op: "receiptOptions",
						id:<%=id%>,
						emailAddr:'<%=emailAddr%>',
						from: '<%=emailAddr%>',
						to:'<%=mmd.getSender()%>',
						sendTime:'<%=mmd.getMyDate()%>',
						subject:'<%=mmd.getSubject()%>'
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest){
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg,"提示");
						}
						else {
							jAlert(data.msg,"提示");
						}
					},
					complete: function(XMLHttpRequest, status){
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

function goBack(){
	if(<%=box%> == <%=MailMsgDb.TYPE_INBOX%>){
		window.location.href = "in_box.jsp?id=<%=eamilPop3Db.getId()%>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton%>";
	}
	if(<%=box%> == <%=MailMsgDb.TYPE_SENDED%>){
		window.location.href = "list_box.jsp?id=<%=eamilPop3Db.getId()%>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton%>";
	}
	if(<%=box%> == <%=MailMsgDb.TYPE_DUSTBIN%>){
		window.location.href = "list_delete.jsp?id=<%=eamilPop3Db.getId()%>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton%>";
	}
	if(<%=box%> == <%=MailMsgDb.TYPE_DRAFT%>){
		window.location.href = "list_draft.jsp?id=<%=eamilPop3Db.getId()%>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton%>";
	}
}

</script>

</head>
<body onload="window_load()">
<div class="inbox-wrap" >
	<div class="inbox-right">
		<div class="inbox-toolbar">
		<%
		if(!from.equals("desktop")){%>
			<div class="inbox-right-btnbox" onclick="goBack()"><img src="images/inbox-back.png" width="20" height="20"/>返回</div>
		<%
		}
		%>
		  <%if(mmd.getType() == MailMsgDb.TYPE_SENDED){ %>
		  <div class="inbox-right-btnbox" onclick="window.location.href='mail_edit.jsp?isClickMenu=1&draftId=<%=mmd.getId()%>&emailAddr=<%=emailAddr%>&subMenu=<%=subMenu %>'">再次编辑发送</div>
		  <%}else{ %>
		  <div class="inbox-right-btnbox" onclick="window.location.href='email_reply.jsp?to=<%=mmd.getSender()%>&emailId=<%=eamilPop3Db.getId() %>&email=<%=emailAddr%>&subject=<%=StrUtil.UrlEncode("回复: " + mmd.getSubject())%>&id=<%=id %>&subMenu=<%=subMenu %>'"><img src="images/inbox-revert.png" width="20" height="20"/>回复</div>
		  <div class="inbox-right-btnbox" onclick="window.location.href='email_reply_all.jsp?id=<%=id%>&email=<%=emailAddr%>&emailId=<%=eamilPop3Db.getId() %>&subject=<%=StrUtil.UrlEncode("回复: " + mmd.getSubject())%>&subMenu=<%=subMenu %>'"><img src="images/inbox-revertall.png" width="20" height="20"/>回复全部</div>
		  <%} %>
		  <%
			if(!from.equals("desktop")){%>
		  <div class="inbox-right-btnbox" onclick="window.location.href='mail_show.jsp?op=del&id=<%=id %>&emailAddr=<%=emailAddr %>&subMenu=<%=subMenu %>&subMenuButton=<%=subMenuButton %>&box=<%=box %>'"><img src="images/inbox-dustbin.png" width="20" height="20"/>删除</div>
		  <%
			}
		%>
		  <div class="inbox-right-btnbox" onclick="window.location.href='email_forward.jsp?id=<%=id%>&subMenu=<%=subMenu %>&emailId=<%=eamilPop3Db.getId() %>'"><img src="images/inbox-repeat.png" width="20" height="20"/>转发</div>
		  <div class="inbox-right-btnbox" onclick="addTab('工作交办','flow_initiate1.jsp?op=at&emailId=<%=id %>')"><img src="images/inbox-repeat@.png" width="20" height="20"/>工作交办</div>
		  <div class="inbox-right-btnbox" onclick="transfer(<%=id %>)"><img src="images/inbox-repeat@.png" width="20" height="20"/>转内部邮箱</div>
		</div>
	</div>
</div>

  <div id="sendBox" style="overflow:auto;height:470px;">
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="in_box_table">
      <tr class="in_box_bg">
      	<td colspan="2">
      		<div class="mailShowDiv">
      			发&nbsp;&nbsp;件&nbsp;人：<%=mmd.getSender()%><br/>
      			收&nbsp;&nbsp;件&nbsp;人：<%=StrUtil.toHtml(mmd.getReceiver())%><br/>
      		
      			<% 
			      	if(mmd.getCopyReceiver() != null && !mmd.getCopyReceiver().equals("")){
			      		String copyReceiver = mmd.getCopyReceiver();
			    %>
			          抄&nbsp;&nbsp;送&nbsp;人:<%=StrUtil.toHtml(copyReceiver)%><br/>
			    
			    <%
      			 }
      		    %>
      			<%
      				if(mmd.getType() == 3 || mmd.getType() == 0 || mmd.getType() == 1){
      					if(!mmd.getBlindReceiver().equals("")){ 
      			%>
      			 密&nbsp;&nbsp;送&nbsp;人：<%=StrUtil.toHtml(mmd.getBlindReceiver())%><br/>
      			 
      			<%	
      				} 
      			   }
      			%>
      			发送日期：<%=DateUtil.format(mmd.getMyDate(), "yyyy-MM-dd HH:mm:ss")%><br/>
      			主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;题：<%=mmd.getSubject()%>
      		</div>
      	</td>
      </tr>
     
     
      <%
      if(mmd.getType() != 3 && mmd.getType() != 0){
      	if(mmd.getReceiver().indexOf(mmd.getEmailAddr()) == -1  && mmd.getCopyReceiver().equals("") && mmd.getBlindReceiver().equals("")){ %>
      	<tr>
	        <td colspan="2" style="padding-left:40px;background-color:#FFF1B4">提   示：你不在收件人里，可能这封邮件是密送给你的。</td>
	     </tr>
      <%	} 
      	}%>
      
      <tr>
        <td colspan="2">
		<div style="padding:5px">
		<%
	if (!mmd.isHtml()) {
%>
          <%=StrUtil.toHtml(mmd.getContent())%>
          <%
	} else {
%>
          <%=mmd.getContent()%>
          <%
	}
%>        </div></td>
      </tr>
      <tr>
        <td colspan="2" style="padding-top:5px">
<%
	java.util.Vector vAttach = mmd.getAttachments();
	if (vAttach.size()>0) {
%>
          附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：
          <%
	}
	%>
    <ul class="attachments" style="list-style:none; margin-top:5px; padding:0px">
	<%
	java.util.Iterator attir = vAttach.iterator();
	boolean isImage = false;
	while (attir.hasNext()) {
		Attachment att = (Attachment)attir.next();
		String name = att.getName();
		if(!name.equals("")){
			String[] nameArr = name.split("\\.");
      		int len = nameArr.length-1;
      		if (StrUtil.isImage(StrUtil.getFileExt(att.getDiskName()))) {
				isImage = true;
			}
      		
      		if(isImage){
		
	%>
			<li>
            	<img src="<%=SkinMgr.getSkinPath(request)%>/images/attach.gif" width="17" height="17" />&nbsp;<a target="_blank" href="email_getfile.jsp?id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a href="?op=delattach&amp;id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"></a>
				<a target=_blank href="show_image.jsp?id=<%=mmd.getId()%>&attachId=<%=att.getId()%>">预览</a>
            </li>
	<%}else {
		String fileExt = StrUtil.getFileExt(att.getDiskName());
		if(fileExt.equals("doc") || fileExt.equals("docx") || fileExt.equals("xls") || fileExt.equals("xlsx")){
	%>
            <li>
            <img src="<%=SkinMgr.getSkinPath(request)%>/images/attach.gif" width="17" height="17" />&nbsp;<a target="_blank" href="email_getfile.jsp?id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a href="?op=delattach&amp;id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"></a>
			<a target=_blank href="email_ntko_show.jsp?pageNum=1&id=<%=mmd.getId()%>&attachId=<%=att.getId()%>">预览</a>
            </li>
    <%}else{ %>
    		 <li>
            <img src="<%=SkinMgr.getSkinPath(request)%>/images/attach.gif" width="17" height="17" />&nbsp;<a target="_blank" href="email_getfile.jsp?id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"><%=att.getName()%></a>&nbsp;&nbsp;&nbsp;<a href="?op=delattach&amp;id=<%=mmd.getId()%>&amp;attachId=<%=att.getId()%>"></a>
            </li>
    		
	<%
		}
		}
		}
	}
	%>        
          </ul>
		</td>
      </tr>
    </table>
 
  </div>


<div id="dlg" style="display:none">
网盘目录&nbsp;<select id="dirCode" name="dirCode">
<%
Leaf rootLeaf = new Leaf();
rootLeaf = rootLeaf.getLeaf(privilege.getUser(request));
if (rootLeaf!=null) {
	com.redmoon.oa.netdisk.DirectoryView pdv = new com.redmoon.oa.netdisk.DirectoryView(rootLeaf);
	pdv.ShowDirectoryAsOptionsWithCode(out, rootLeaf, rootLeaf.getLayer());
}
%>
</select>

</div>
<div id="receiptDiv" style="display:none">
	消息发送者需要回执，现在就发送回执么？
</div>
<script>
<%
if(isReaded == 0 && receiptState == 1 && msgType == 1) {
%>
	chkReceipt();
<%
}
%>
</script>
</body>
<script>
function saveToNetdisk(attId) {
	$("#dlg").dialog({
		title: "请选择目录",
		modal: true,
		buttons: {
			"取消": function() {
				$(this).dialog("close");
			},
			"确定": function() {
				$.ajax({
					type: "post",
					url: "mail_do.jsp",
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

function transfer(mailId) {
	addTab('转发内部邮件', '<%=request.getContextPath()%>/message_oa/send.jsp?mailId=' + mailId+'&isShowBack=1');
}

$(function(){
	top.mainFrame.reloadTab("桌面");
})
</script>
</html>
