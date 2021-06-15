<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>查询短信</title>
<link rel="stylesheet" type="text/css" href="../common.css">
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
	GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}
</script>
</head>
<body class="bodycolor">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
%>
<br>
<table width="80%"  border="0" align="center" cellpadding="0" cellspacing="0" bordercolor="#000000" bgcolor="#FFFFFF" class="tableframe">
<form action="send_sms_list.jsp?op=search" method="post">
  <tr>
    <td height="24" colspan="2" class="right-title">&nbsp;&nbsp;查询短信</td>
    </tr>
  <tr>
    <td height="24">&nbsp;用户名</td>
    <td width="85%"><input type="text" name="userName" size="20" maxlength="25" class="BigInput"></td>
  </tr>
  <tr>
    <td height="24">&nbsp;手机号码</td>
    <td><input type="text" name="sendMobile" size="20" maxlength="25" class="BigInput"></td>
  </tr>
  <tr>
    <td height="24">&nbsp;发送内容</td>
    <td><input type="text" name="msgText" size="20" maxlength="25" class="BigInput"></td>
  </tr>
  <tr>
    <td height="24">&nbsp;发送时间</td>
    <td><input maxLength="10" size="20" name="fromSendTime"> 
        <img style="CURSOR: hand" onClick="SelectDate('fromSendTime', 'yyyy-MM-dd')" src="../images/form/calendar.gif" align="absMiddle" border="0" width="26" height="26">&nbsp;至 
        <input maxLength="10" size="20" name="toSendTime"><img style="CURSOR: hand" onClick="SelectDate('toSendTime', 'yyyy-MM-dd')" src="../images/form/calendar.gif" align="absMiddle" border="0" width="26" height="26"> 日期格式形如 1999-1-2	</td>
  </tr>
  <tr>
    <td height="24">&nbsp;</td>
    <td height="30"><input class="btn" type="submit" name="Submit" value=" 查 询 "></td>
  </tr>
</form>
</table>
</body>
</html>
