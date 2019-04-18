<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
PersonGroupTypeDb pgtd = new PersonGroupTypeDb();
Vector typeV = pgtd.listOfUser(privilege.getUser(request));
if (typeV.size()==0) {
	out.print(StrUtil.Alert_Back("您还没有创建自己的用户组！"));
	%>
	<script>
	window.close();
	</script>
	<%
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>按用户组选择人员</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<script>
var allUserOfGroup="";
var allUserRealNameOfGroup = "";

function setUsers() {
  // window.returnValue = users.innerText;
  if (window.opener) {
	  window.opener.setUsers(users.innerHTML, userRealNames.innerHTML);
  }
  else {
	  dialogArguments.setUsers(users.innerHTML, userRealNames.innerHTML);
  }
  window.close();
}

function initUsers() {
  if (window.opener) {
	  users.innerHTML = window.opener.getSelUserNames();
	  userRealNames.innerHTML = window.opener.getSelUserRealNames();
  }
  else {
	  users.innerHTML = dialogArguments.getSelUserNames();
	  userRealNames.innerHTML = dialogArguments.getSelUserRealNames();
  }
}

function selPerson(deptCode, deptName, userName, userRealName) {
  // 检查用户是否已被选择
  if (users.innerHTML.indexOf(userName)!=-1) {
	  alert("用户" + userRealName + "已被选择！");
	  return;
  }
  if (users.innerHTML=="") {
	  users.innerHTML += userName
	  userRealNames.innerHTML += userRealName;
  }
  else {
	  users.innerHTML += "," + userName;
	  userRealNames.innerHTML += "," + userRealName;
  }
}

function cancelSelPerson(deptCode, deptName, userName) {
  // 检查用户是否已被选择
  var strUsers = users.innerHTML;
  if (strUsers=="")
	  return;
  if (strUsers.indexOf(userName)==-1) {
	  return;
  }
  
  var strUserRealNames = userRealNames.innerHTML;
  var ary = strUsers.split(",");
  var aryRealName = strUserRealNames.split(",");
  var len = ary.length;
  var ary1 = new Array();
  var aryRealName1 = new Array();
  var k = 0;
  for (i=0; i<len; i++) {
	  if (ary[i]!=userName) {
		  ary1[k] = ary[i];
		  aryRealName1[k] = aryRealName[i];
		  k++;
	  }
  }
  var str = "";
  var str1 = "";
  for (i=0; i<k; i++) {
	  if (str=="") {
		  str = ary1[i];
		  str1 = aryRealName1[i];
	  }
	  else {
		  str += "," + ary1[i];
		  str1 += "," + aryRealName1[i];
	  }
  }
  users.innerHTML = str;
  userRealNames.innerHTML = str1;
}

function selAllUserOfGroup() {
  if (allUserOfGroup=="")
	  return;
  var allusers = users.innerHTML;
  var allUserRealNames = userRealNames.innerHTML;
  if (allusers=="") {
	  allusers += allUserOfGroup;
	  allUserRealNames += allUserRealNameOfGroup;
  }
  else {
	  allusers += "," + allUserOfGroup;
	  allUserRealNames += "," + allUserRealNameOfGroup;
  }
  //alert(allusers);
   //alert(allUserRealNames);
  var r = clearRepleatUser(allusers, allUserRealNames);
  
  users.innerHTML = r[0];
  userRealNames.innerHTML = r[1];
}
 
function clearRepleatUser(strUsers, strUserRealNames) {
  var ary = strUsers.split(",");
  var aryRealName = strUserRealNames.split(",");
  
  var len = ary.length;
  // 创建二维数组
  var ary1 = new Array();
  for (i=0; i<len; i++) {
	  ary1[i] = new Array(2);
	  ary1[i][0] = ary[i];
	  ary1[i][1] = 0; // 1 表示重复
  }
  
  // 标记重复的用户
  for (i=0; i<len; i++) {
	  var user = ary[i];
	  for (j=i+1; j<len; j++) {
		  if (ary1[j][1]==1)
			  continue;
		  if (ary[j]==user)
			  ary1[j][1] = 1;
	  }
  }

  // 重组为字符串
  var str = "";
  var str1 = "";
  for (i=0; i<len; i++) {
	  if (ary1[i][1]==0) {
		  u = ary1[i][0];
		  if (str=="") {
			  str = u;
			  str1 = aryRealName[i];
		  }
		  else {
			  str += "," + u;
			  str1 += "," + aryRealName[i];
		  }
	  }
  }
  var retary = new Array();
  retary[0] = str;
  retary[1] = str1;
  return retary;
  
}
</script>
</HEAD>
<BODY onLoad="initUsers()">
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="460" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
<thead>
  <tr> 
    <td class="tabStyle_1_title" height="24" colspan="3" align="center"><span>用户组</span></td>
  </tr>
</thead>
  <tr> 
    <td height="87" colspan="2" valign="top" width="35%">
<%
int groupId = ParamUtil.getInt(request, "groupId", -1);
if (groupId == -1) {
	groupId = ((PersonGroupTypeDb)typeV.elementAt(0)).getId();
}
Iterator ir = typeV.iterator();
%>
      <br>
      <table width="95%" border="0" align="center" cellspacing="0" class="tabStyle_1_sub">
        <tbody>
<%
while (ir.hasNext()) {
 	pgtd = (PersonGroupTypeDb)ir.next();
%>
          <tr>
            <td>
              <a href="javascript:;" onclick="location.href='persongroup_user_multi_sel.jsp?groupId=<%=pgtd.getId()%>'" menu="true"><%=pgtd.getName()%></a>
            </td>
          </tr>
  <%}%>
        </tbody>
      </table>	</td>
    <td align="center" valign="top">
	<div id="resultTable">
	  <table width="100%" border="0" cellpadding="4" cellspacing="0" class="tabStyle_1_subTab">
        <tr>
          <td width="91" align="left" class="tabStyle_1_subTab_title">职员</td>
          <td width="74" align="left" class="tabStyle_1_subTab_title">&nbsp;</td>
        </tr>
      <tbody id="postsbody">
	  <%

	PersonGroupUserDb pgud = new PersonGroupUserDb();
	String sql = "select id from " + pgud.getTable().getName() + " where group_id=? order by orders";
	ir = pgud.list(sql, new Object[]{new Integer(groupId)}).iterator();
	UserDb ud = new UserDb();
	  while (ir.hasNext()) {
	  	pgud = (PersonGroupUserDb)ir.next();
		ud = ud.getUserDb(pgud.getString("user_name"));
	  %>
	  <script>
	  if (allUserOfGroup=="") {
	  	allUserOfGroup = "<%=StrUtil.toHtml(ud.getName())%>";
		allUserRealNameOfGroup = "<%=StrUtil.toHtml(ud.getRealName())%>";
	  }
	  else {
	  	allUserOfGroup += "," + "<%=ud.getName()%>";
		allUserRealNameOfGroup += "," + "<%=ud.getRealName()%>";
	  }
	  </script>
	  <tr>
	    <td><a onClick="selPerson('', '', '<%=StrUtil.toHtml(ud.getName())%>', '<%=StrUtil.toHtml(ud.getRealName())%>')" href="#"><%=ud.getRealName()%></a></td>
	    <td>[<a onClick="cancelSelPerson('', '', '<%=StrUtil.toHtml(ud.getName())%>')" href="#">取消选择</a>]</td>
	  </tr>
	  <%}%>
      </tbody>
    </table>
	</div><div><input class="btn" type="button" value="选择该用户组所有用户" onClick="selAllUserOfGroup()"></div></td>
  </tr>
  <tr align="center">
    <td height="63">已选人员</td>
    <td height="63" colspan="2" align="left">
	  <div id="users" name="users" style="display:none"></div>
	  <div id="userRealNames" name="userRealNames"></div>
	</td>
  </tr>
  <tr align="center">
    <td height="28" colspan="3">
<input class="btn" type="button" name="okbtn" value="确定" onClick="setUsers()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
      <input class="btn" type="button" name="cancelbtn" value="取消" onClick="window.close()">    </td>
  </tr>
</table>
</BODY></HTML>
