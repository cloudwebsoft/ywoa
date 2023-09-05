<%@ page contentType="text/html;charset=utf-8"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script>
function setUsers(users, userRealNames) {
	addform.receiver.value = users;
	addform.userRealNames.value = userRealNames;
}

function getSelUserNames() {
	return addform.receiver.value;
}

function getSelUserRealNames() {
	return addform.userRealNames.value;
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUsers() {
  openWin('user_multi_sel.jsp', 800, 600);
}

function openWinUserGroup() {
	openWin("user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("user_role_multi_sel.jsp", 520, 400);
}
</script>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
  <tr>
    <td width="83" height="27" rowspan="3" valign="top">短消息<br />
    接收者：<br />
      <%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
<input name="isToMobile" value="true" type="checkbox" checked />
短信
<%}%><br /></td>
    <td width="315" height="27" rowspan="3"><textarea name="userRealNames" cols="50" rows="8" readOnly wrap="yes" id="userRealNames"></textarea>
      <input type=hidden name="receiver">
        <input type="hidden" name="isDraft" value="false"></td>
    <td width="487"><input name="button2" type="button" onclick="openWinUsers()" value="选择用户" /></td>
  </tr>
  <tr>
    <td><input name="button3" type=button onclick="openWinUserGroup()" value="按用户组" /></td>
  </tr>
  <tr>
    <td><input name="button" type=button onclick="openWinUserRole()" value=" 按角色 " /></td>
  </tr>
</table>
