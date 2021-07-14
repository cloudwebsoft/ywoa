<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../../inc/nocache.jsp"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/><%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
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
id = md.getId();
title = md.getTitle();
content = md.getContent();
type = md.getType();

if (type != MessageDb.TYPE_SYSTEM) {
	response.sendRedirect("../showmsg.jsp?id=" + id);
	return;
}

rq = md.getRq();
receiver = md.getReceiver();
sender = md.getSender();

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
<title>查看消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script tyle="text/javascript" language="javascript" src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
<script src="../../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
<script src="../../js/jquery.bgiframe.js"></script>

<script>
function chkReceipt() {
	if(confirm('消息发送者需要回执，现在就发送回执么？')) {
		window.location.href = 'do_receipt.jsp?id=<%=id%>';
	}
}
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">查看消息</td>
    </tr>
  </tbody>
</table>
<form name="form1" method="post" action="myreply.jsp?id=<%=id%>">
  <table class="tabStyle_1 percent98" align="center" style="margin-top:10px">
    <tr>
      <td class="tabStyle_1_title"><%=title%>&nbsp;&nbsp;&nbsp;&nbsp;<%=senderName%>&nbsp;&nbsp;&nbsp;&nbsp;<%=rq%>
              <input type="hidden" name="title" value="<%="RE:"+title%>">
              <input name="receiver" value="<%=sender%>" type="hidden"></td>
          </tr>
          <tr>
            <td>
			<div class="msgContent">
			<%
			String body = content; // StrUtil.toHtml(content);
			%>
              <%=StrUtil.ubb(request, MessageDb.toHtml(content), true)%>
              <br>
            <%
			String actionLink = md.renderAction(request);
			if (!actionLink.equals("")) {
				out.print("<BR><font style='font-family:宋体;'>>></font>&nbsp;" + actionLink);
			}
			%>
            <div style="margin-top:10px">
			<%
			Iterator ir = md.getAttachments().iterator();
			while (ir.hasNext()) {
				Attachment att = (Attachment)ir.next();
			%>
              <img src="../../images/attach2.gif" align="absmiddle" /> <a href="../getfile.jsp?attachId=<%=att.getId()%>&msgId=<%=md.getId()%>" target="_blank"><%=att.getName()%></a>
			  &nbsp;&nbsp;<a href="javascript:;" onclick="saveToNetdisk(<%=att.getId()%>)">存至网盘</a>              
              <BR>
              <%}%>
			</div>
            </div>
            </td>
          </tr>
          <tr>
            <td align="center">
                <%if (md.getBox() == MessageDb.DRAFT) {%>
                <input class="btn" type="button" name="Button" value=" 发送 " onClick="window.location.href='send_do.jsp?op=sendDraft&id=<%=md.getId()%>'">
                <%}else{
			  	if (type != MessageDb.TYPE_SYSTEM && !md.getSender().equals(privilege.getUser(request))) {
			  %>
                <input class="btn" type="submit" name="Button" value=" 回复 ">
              <%}%>
                &nbsp;&nbsp;
                <!-- <input class="btn" name="button" type="button" onClick="window.location.href='transmit.jsp?id=<%=id%>'" value=" 转发 "> -->
                <%}%></td>
          </tr>
        </table>
  </form>
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
function saveToNetdisk(attId) {
	alert("here");
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
</script>
</html>