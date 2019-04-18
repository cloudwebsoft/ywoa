<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.task.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>任务查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("util/jscalendar/calendar-win2k-2.css"); </style>
<script language="JavaScript" type="text/JavaScript">
<!--
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
	GetDate = showModalDialog("util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate(){ 
	findObj(ObjName).value = GetDate; 
}
//-->
</script>
<script src="inc/common.js"></script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="task_inc_menu_top.jsp"%>
<script>
$("menu7").className="current";
</script>
<div class="spacerH"></div>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<form action="task.jsp?op=search" name="form1" method="post">
<table class="tabStyle_1 percent80" cellSpacing="0" cellPadding="2" width="600" align="center" border="0">
  <tbody>
    <tr>
      <td colspan="2" noWrap class="tabStyle_1_title">&nbsp;任务查询</td>
    </tr>
    <tr>
      <td width="10%" noWrap>任务名称：</td>
      <td width="90%" class="TableData"><input name="title" id="title" size="26" maxLength="80"></td>
    </tr>
    <tr>
      <td noWrap>任务内容：</td>
      <td class="TableData"><input name="content" id="content" size="36" maxLength="200"></td>
    </tr>
    <tr>
      <td noWrap>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态：</td>
      <td class="TableData">
	  <select name="status">
	  <option value="">全部</option>
	  <%for (int i=0; i<=9; i++) {%>
	  <option value="<%=i%>"><%=TaskDb.getTaskStatusDesc(i)%></option>
	  <%}%>
	  </select>
	  </td>
    </tr>
    <tr>
      <td noWrap>有&nbsp;&nbsp;效&nbsp;&nbsp;期：</td>
      <td class="TableData">开始日期&nbsp;&nbsp;
        <input type="text" id="beginDate" name="beginDate" size="10">
        <script type="text/javascript">
    Calendar.setup({
        inputField     :    "beginDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
            </script>
结束日期&nbsp;&nbsp;
<input type="text" id="endDate" name="endDate" size="10">
<script type="text/javascript">
    Calendar.setup({
        inputField     :    "endDate",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        showsTime      :    false,            // will display a time selector
        singleClick    :    false,           // double-click mode
        align          :    "Tl",           // alignment (defaults to "Bl")		
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
    });
</script>（为空表示不限制）</td>
    </tr>
    
    
    <tr class="TableControl" align="middle">
      <td colSpan="2" align="center" noWrap><input name="submit" type="submit" value="提交" class="btn">
        &nbsp;&nbsp;
          <input name="button" type="reset" value="重填" class="btn">
        &nbsp;&nbsp;</td>
    </tr>
  </tbody>
</table>
</form>
</body>
</html>
