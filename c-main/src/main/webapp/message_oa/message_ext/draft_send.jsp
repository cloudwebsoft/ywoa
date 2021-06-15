<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String name = privilege.getUser(request);
int id = ParamUtil.getInt(request, "id");
MessageDb msg = new MessageDb();
msg = (MessageDb)msg.getMessageDb(id);
String receiversAll = msg.getReceiversAll();
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

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>转发消息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script language=javascript>
<!--
function form1_onsubmit()
{
	errmsg = "";
	if (form1.receiver.value=="")
		errmsg += "请填写接收者！\n"
	if (form1.title.value=="")
		errmsg += "请填写标题！\n"
	if (form1.content.value=="")
		errmsg += "请填写内容！\n"
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
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
		alert("对不起，您一次最多只能发往" + messageToMaxUser + "个用户！");
		return;
	}
	form1.receiver.value = users;
	form1.userRealName.value = userRealNames;
}
//-->
</script>
</head>

<body text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%
if (!privilege.isUserLogin(request)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}%>
<table width="100%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black" height="260">
  <form action="transmit_do.jsp" method="post" name="form1" onSubmit="return form1_onsubmit()">
  <tr> 
    <td height="23" class="tdStyle_1">
        转发消息
	</td>
  </tr>
  <tr> 
    <td height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="listdraft.jsp"><img src="../images/m_draftbox.gif" width="60" height="60" border="0"></a></div>
            </td>
            <td width="75"> 
    		  <div align="center"><a href="listoutbox.jsp"><img src="../images/m_outbox.gif" width="60" height="60" border="0"></a></div>
            </td>            
            <td width="75"> 
              <div align="center"><a href="send.jsp"><img src="../images/newpm.gif" width="60" height="60" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"> <img src="../images/m_delete.gif" width="60" height="60"></div>
            </td>
          </tr>
        </table>
    </td>
  </tr>
  <tr> 
      <td height="420" valign="top">
	  <table width="487" border="0" cellspacing="0" cellpadding="3" align="center" class="tabStyle_1 percent80">
          <tr>
            <td height="27" colspan="2" class="tabStyle_1_title">消息</td>
          </tr>
          <tr> 
            <td width="84" height="27"> 
            接收者：</td>
            <td width="388" height="27">   
              <input type="hidden" name="receiver" value="<%=receiversAll%>">
              <input type="text" readonly name="userRealName" size="50" value="<%=receiversAllName%>">
			  <input type="hidden" name="isDraft" value="false">
			  <input type="hidden" name="id" value="<%=id%>">
		    <a href="#" onClick="javascript:showModalDialog('../../user_multi_sel.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">选择用户</a></td>
          </tr>
          <tr> 
            <td width="84" height="26"> 
            消息标题：           </td>
            <td width="388" height="26">
            <input type="text" name="title" size="50" value="<%=StrUtil.toHtml(msg.getTitle())%>"></td>
          </tr>
          <tr> 
            <td width="84" height="26"> 
            消息内容：			</td>
            <td width="388" height="26"> 
            <textarea name="content" cols="50" rows="12"><%=msg.getContent()%></textarea>			</td>
          </tr>
<%		  
if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
%>
          <tr>
            <td height="26" align="left">提醒</td>
            <td height="26" align="left"><%
%>
<input name="isToMobile" value="true" type="checkbox" checked="checked" />
短信
</td>
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
              <img src="../../images/attach2.gif" align="absmiddle" /> <a href="../getfile.jsp?attachId=<%=att.getId()%>&msgId=<%=msg.getId()%>" target="_blank"><%=att.getName()%></a> <BR>
              <%}%></td>
          </tr>
<%}%>		  
          <tr> 
            <td height="26" colspan="2" align="center"> 
			<% if(msg.getBox()==MessageDb.OUTBOX){ %>
				<input class="btn" type="submit" name="Submit" value="重新发送">
			<%}else{%>
                <input class="btn" type="submit" name="Submit" value="发送">
			<%}%>	
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                &nbsp;&nbsp;&nbsp;&nbsp;    
				<% if(msg.getBox()!=MessageDb.OUTBOX){ %>    
				      <input name="isToOutBox" value="true" type="checkbox" checked> 存至发件箱
				<%}%></td>
          </tr>
        </table>
      </td>
  </tr>
  <tr> 
    <td bgcolor="#CEE7FF" height="6"></td>
  </tr>
  </form>
</table>
</body>
</html>
