<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="workplan";
if (!privilege.isUserPrivValid(request, priv)) {
	// out.println(fchar.makeErrMsg("对不起，您不具有发布工作计划的权限！"));
	// return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
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
	GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}

//-->
</script>
</head>
<body>
<%@ include file="workplan_inc_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
<form action="workplan_list.jsp?op=search" name="form1" method="post">
  <tbody>
    <tr>
      <td colspan="2" class="tabStyle_1_title">查询计划</td>
    </tr>
    <tr>
      <td width="15%" noWrap class="TableContent">计划名称：</td>
      <td width="85%" class="TableData"><input name="title" id="title" size="26" maxLength="80"></td>
    </tr>
    <tr>
      <td class="TableContent" noWrap>计划内容：</td>
      <td class="TableData"><input name="content" id="content" size="36" maxLength="200"></td>
    </tr>
    <tr>
      <td class="TableContent" noWrap>有效期：</td>
      <td class="TableData">开始日期：
           <input name="beginDate" id="beginDate" size="10">
           之后（为空表示不限制）<br>
        结束日期： 
        <input name="endDate" id="endDate" size="10">
        之前（为空表示不限制）</td>
    </tr>
    <tr>
      <td class="TableContent" noWrap>计划类型：</td>
      <td class="TableData">
	  <%
	  WorkPlanTypeDb wptd = new WorkPlanTypeDb();
	  String opts = "";
	  Iterator ir = wptd.list().iterator();
	  while (ir.hasNext()) {
	  	wptd = (WorkPlanTypeDb)ir.next();
	  	opts += "<option value='" + wptd.getId() + "'>" + wptd.getName() + "</option>";
	  }
	  %>
	  <select name="typeId" id="typeId">
	  <option value="all">全部</option>
	  <%=opts%>
      </select>	  </td>
    </tr>
    <tr class="TableControl" align="middle">
      <td colSpan="2" align="center" noWrap><input class="btn" name="submit" type="submit" value="提交">
        &nbsp;&nbsp;
          <input class="btn" name="button" type="reset" value="重填">
        &nbsp;&nbsp;</td>
    </tr>
  </tbody>
  </form>
</table>
<script>
$(function(){
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
    $('#endDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
})
</script>
</body>
</html>
