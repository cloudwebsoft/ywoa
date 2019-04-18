<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String name = privilege.getUser(request);
String receiver = ParamUtil.get(request, "receiver");
%>
<html>
<head>
<title>撰写群发消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ include file="../inc/nocache.jsp"%>
<LINK href="../common.css" type=text/css rel=stylesheet>
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
	form1.action="send_do.jsp?op=addDraft";
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
	showModalDialog('../user_multi_sel.jsp',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUserGroup() {
	openWin("../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../user_role_multi_sel.jsp", 520, 400);
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
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%
if (!privilege.isUserPrivValid(request, "message.group")) {	
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black" height="260">
  <form action="send_do.jsp" method="post" enctype="multipart/form-data" name="form1" onSubmit="return form1_onsubmit()">
  <tr> 
    <td bgcolor="#CEE7FF" height="23">
        <div align="center"> <b>撰 写 群 发 消 息</b></div>
    </td>
  </tr>
  <tr> 
    <td bgcolor="#FFFFFF" height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><a href="message.jsp?page=1"><img src="images/inboxpm.gif" width="60" height="60" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="listdraft.jsp"><img src="images/m_draftbox.gif" width="60" height="60" border="0"></a></div>
            </td>
            <td width="75"> 
    		  <div align="center"><a href="listoutbox.jsp"><img src="images/m_outbox.gif" width="60" height="60" border="0"></a></div>
            </td>                   
            <td width="75"> 
              <div align="center"><img src="images/newpm.gif" width="60" height="60" border="0"></div>
            </td>
            <td width="75"> 
              <div align="center"> <img src="images/m_delete.gif" width="60" height="60"></div>
            </td>
          </tr>
        </table>
    </td>
  </tr>
  <tr> 
      <td bgcolor="#FFFFFF" height="152" valign="top">
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
          <tr> 
            <td></td>
          </tr>
        </table>
        <table width="300" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr> 
            <td width="68" height="27" valign="top"> 
            <div align="center">接 收 者：</div>            </td>
            <td width="217" height="27">
              <textarea name="userRealNames" cols="28" rows="2" readOnly wrap="yes" id="userRealNames"></textarea>
              <input type=hidden name="receiver">
              <input type="hidden" name="isDraft" value="false">
			  <input class="btn" type="button" onClick="openWinUsers()" value="选择用户">
		    <input class="btn" type=button onClick="openWinUserGroup()" value="按用户组"> <input class="btn" name="button" type=button onClick="openWinUserRole()" value="按角色"></td></tr>
          <tr> 
            <td width="68" height="26"> 
              <div align="center">消息标题：</div>            </td>
            <td width="217" height="26">
              <input type="text" name="title" class="input1" size="30" maxlength="30">            </td>
          </tr>
          <tr> 
            <td width="68" height="26"> 
              <div align="center">消息内容：</div>            </td>
            <td width="217" height="26"> 
              <textarea name="content" cols="28" rows="3"></textarea>            </td>
          </tr>
          <tr>
            <td height="26" align="center">附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：</td>
            <td height="26"><input type="file" name="filename"></td>
          </tr>
          <tr> 
            <td colspan="2" height="26"> 
              <div align="center">
                <input type="submit" name="Submit" value="发送" class="btn">
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                <input type="reset" name="Submit2" value="重写" class="btn">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="button" name="Submit3" value="存草稿" class="btn" onClick="saveDraft()">
&nbsp;
<input name="isToOutBox" value="true" type="checkbox" checked>存至发件箱
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS() && privilege.isUserPrivValid(request, "sms")) {
%>
&nbsp;
<input name="isToMobile" value="true" type="checkbox" checked>
短信
<%}%>
              </div>            </td>
          </tr>
        </table>
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
          <tr> 
            <td></td>
          </tr>
        </table>
      </td>
  </tr>
  <tr> 
    <td bgcolor="#CEE7FF" height="6"></td>
  </tr></form>
</table>
</body>
</html>
