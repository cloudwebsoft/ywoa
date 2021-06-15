<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>增加日程</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language=javascript>
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

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var GetDate=""; 
function SelectDate(ObjName,FormatDate){
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:220px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}

/**function SelectDateTime(objName) {
	var dt = showModalDialog("util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
	if (dt!=null)
		findObj(objName).value = dt;
}*/
function SelectDateTime(objName) {
    var dt = openWin("util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        findObj(objName).value = dt;
}
</script>
<script src="inc/common.js"></script>
</head>
<body>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
$("menu3").className="current";
</script>
<table class="tabStyle_1 percent60">
  <tr class="stable">
    <td colspan="2" class="tabStyle_1_title">增加日程</td>
  </tr>
  <form action="plan_add_do.jsp" method="post" name="form1" id="form1" onsubmit="">
    <tr>
      <td>标&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;题</td>
      <td><input name="title" class="btn" size="50" /></td>
    </tr>
    <tr>
      <td>待办日期 </td>
      <td><input name="mydate" size="10" readonly="" />
                <img style="CURSOR: hand" onclick="SelectDate('mydate','yyyy-mm-dd')" src="images/form/calendar.gif" align="absmiddle" width="26" height="26" />
                <input style="WIDTH: 50px" value="12:00:00" name="time" size="20" />
            &nbsp;<img style="CURSOR: hand" onclick="SelectDateTime('time')" src="images/form/clock.gif" align="absmiddle" width="18" height="18" /></td>
    </tr>
    <tr>
      <td>是否提醒</td>
      <td><input type="checkbox" name="isRemind" value="1" checked="checked" />
        &nbsp;&nbsp;
        <select name="before">
          <option value="10">十分钟</option>
          <option value="20">二十分钟</option>
          <option value="30">三十分钟</option>
          <option value="45">四十五分钟</option>
          <option value="60">一小时</option>
          <option value="120">二小时</option>
          <option value="180">三小时</option>
          <option value="360">六小时</option>
          <option value="720">十二小时</option>
        </select>
        之前
        <%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
        <input name="isToMobile" value="true" type="checkbox" checked="checked" />
        短信提醒
        <%}%></td>
    </tr>
    <tr>
      <td>内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;容</td>
      <td><textarea name="content" cols="50" class="btn" rows="12"></textarea>
      </td>
    </tr>
    <tr>
      <td align="center" colspan="2"><input name="submit" type="submit" class="btn" value=" 发 送 " />
        &nbsp;&nbsp;&nbsp;
        <input name="reset" type="reset" class="btn" value=" 取 消 " /></td>
    </tr>
  </form>
</table>
</body>
</html>