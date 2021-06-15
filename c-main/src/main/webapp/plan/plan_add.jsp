<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String userName = ParamUtil.get(request, "userName");
String action = ParamUtil.get(request, "action");
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>增加日程</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/livevalidation_standalone.js"></script>
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

/**function SelectDateTime(objName) {
	var dt = showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no");
	if (dt!=null)
		o(objName).value = dt;
}*/
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        o(objName).value = dt;
}
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
</script>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

</head>
<body>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
  <tr class="stable">
    <td colspan="2" class="tabStyle_1_title">日程安排</td>
  </tr>
  <form action="plan_add_do.jsp?op=add" method="post" name="form1" id="form1" onSubmit="return checked()">
    <tr>
      <td align="center">标题</td>
      <td><input type="text" name="title"  id="title" size="50" /></td>
    </tr>
    <tr>
      <td align="center">开始时间 </td>
      <td>
      <input type="text" id="mydate" name="mydate" size="18" readonly="" value="<%=action.equals("addNotepaper")?DateUtil.format(new java.util.Date(), "yyyy-MM-dd"):""%>" />
			<input name="userName" value="<%=userName%>" type="hidden" />
			</td>
    </tr>
	<tr>
      <td align="center">结束时间 </td>
      <td>
      <input type="text" id="enddate" name="enddate" size="18" readonly="" />
		</td>
    </tr>
	<tr>
	  <td align="center">是否便笺</td>
	  <td><input type="checkbox" value="1" name="isNotepaper" <%=action.equals("addNotepaper")?"checked":""%> /></td>
    </tr>
    <%if (privilege.isUserPrivValid(request, "plan.share")) {%>
	<tr>
	  <td align="center">共享</td>
	  <td><input type="checkbox" value="1" name="shared" /></td>
    </tr>
    <%}%>
	<tr>
	  <td align="center">是否提醒</td>
	  <td><input type="checkbox" value="true" name="isRemind" checked="checked" />
        <select name="before">
          <option value="0">请选择</option>
          <option value="10" selected>十分钟</option>        
          <option value="20">二十分钟</option>
          <option value="30">三十分钟</option>
          <option value="45">四十五分钟</option>
          <option value="60">一小时</option>
          <option value="120">二小时</option>
          <option value="180">三小时</option>
          <option value="240">四小时</option>
          <option value="300">五小时</option>
          <option value="360">六小时</option>
          <option value="720">十二小时</option>
        </select>
之前</td>
    </tr>
<%
if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
%>
	<tr>
	  <td align="center">短信提醒</td>
	  <td>
        <input name="isToMobile" value="true" type="checkbox" checked="checked" /></td>
    </tr>
<%}%>
	<tr>
      <td align="center">内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;容</td>
      <td><textarea id="content" name="content" cols="50" rows="6"></textarea>
          <script>
			var title = new LiveValidation('title');
			title.add( Validate.Presence );			
			title.add(Validate.Length, { minimum: 1, maximum: 500 } );
			var person = new LiveValidation('content');
			person.add( Validate.Presence );			
			var mydate = new LiveValidation('mydate');
			mydate.add( Validate.Presence );			
		  </script>               
      
      </td>
    </tr>
    <tr>
      <td align="center" colspan="2"><input  name="add" type="submit" class="btn" value=" 确定 " />
        &nbsp;&nbsp;&nbsp;
        <input type="reset" class="btn" value=" 重置 " /></td>
    </tr>
  </form>
  <script>
  $(function(){
  	$('#mydate').datetimepicker({
  		lang:'ch',
  	  	value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',
  	  	step:5, 
  	  	format:'Y-m-d H:i:00'
  	});
	$('#enddate').datetimepicker({
		lang:'ch',
		value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',
		step:5, 
		format:'Y-m-d H:i:00'
	});
  })
function checked(){
	if(o("title").value.length <=30){
		//form1.submit();
		return true;
	}else{
		jAlert("标题长度不能超过30个字符","提示");
		return false;
	}
}
</script>
</table>
</body>
</html>