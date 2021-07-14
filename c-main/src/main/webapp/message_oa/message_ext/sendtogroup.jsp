<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
if (!privilege.isUserPrivValid(request, "message.group")) {	
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String name = privilege.getUser(request);
String receiver = ParamUtil.get(request, "receiver");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>撰写群发消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<script src="../../inc/common.js"></script>
<script src="../../inc/upload.js"></script>
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

function saveDraft() {
	form1.isDraft.value = "true";
	form1_onsubmit();
	form1.submit();
}

<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(name);
%>	
function getDept() {
	return "<%=usd.getMessageToDept()%>";
}

function getValidUserGroup() {
	return "<%=usd.getMessageToUserGroup()%>";
}

function getValidUserRole() {
	return "<%=usd.getMessageToUserRole()%>";
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
	return form1.userRealNames.value;
}

function openWinUsers() {
	showModalDialog('../../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUserGroup() {
	openWin("../../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../../user_role_multi_sel.jsp", 520, 400);
}

function openWinPersonUserGroup() {
	openWin("../../user/persongroup_user_multi_sel.jsp", 520, 400);
}

<%
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
	form1.userRealNames.value = userRealNames;
}
//-->
</script>
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">撰写群发消息</td>
    </tr>
  </tbody>
</table>
<table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
  <tr>
    <td width="75"><div align="center"><a href="message.jsp?page=1"><img src="../images/inboxpm.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><a href="listdraft.jsp"><img src="../images/m_draftbox.gif" width="60" height="60" border="0"></a></div></td>
    <td width="75"><div align="center"><img src="../images/newpm.gif" width="60" height="60" border="0"></div></td>
    <td width="75"><div align="center"> <img src="../images/m_delete.gif" width="60" height="60"></div></td>
  </tr>
</table>
<form action="send_do.jsp" method="post" enctype="multipart/form-data" name="form1" onSubmit="return form1_onsubmit()">
<table class="tabStyle_1 percent80" style="margin-top:10px" align="center">
<tr><td class="tabStyle_1_title" colspan="2">撰写群发消息</td></tr>
    <tr>
      <td width="19%" align="center">接 收 者：</td>
      <td width="81%"><textarea name="userRealNames" cols="60" rows="4" readOnly wrap="yes" id="userRealNames" style="margin-bottom:5px"></textarea>
        <input type=hidden name="receiver">
        <input type="hidden" name="isDraft" value="false">
        <br />
        <input class="btn" type="button" onClick="openWinUsers()" value="选择用户">
        &nbsp;
        <input class="btn" type=button onClick="openWinUserGroup()" value="按用户组">
        &nbsp;
        <input class="btn" name="button" type=button onClick="openWinUserRole()" value="按角色">
        &nbsp;
        <input class="btn" name="button" type=button onClick="openWinPersonUserGroup()" value="我的用户组">
      </td>
    </tr>
    <tr>
      <td align="center">消息标题：</td>
      <td><input type="text" name="title" class="input1" size="50"></td>
    </tr>
    <tr>
      <td align="center">消息内容：</td>
      <td><textarea name="content" cols="60" rows="16"></textarea></td>
    </tr>
    <tr>
      <td align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
      <td>
		<script>initUpload()</script>	  
	  </td>
    </tr>
    <tr>
      <td align="center" colspan="2"><input class="btn" type="submit" name="Submit" value="发送">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input class="btn" type="reset" name="Submit2" value="重写">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input class="btn" type="button" name="Submit3" value="存草稿" onClick="saveDraft()">
        &nbsp;
        <%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
%>
        &nbsp;
        <input name="isToMobile" value="true" type="checkbox" checked>
        短信
        <%}%>
      </td>
    </tr>
  </table>
</form>
</body>
</html>
