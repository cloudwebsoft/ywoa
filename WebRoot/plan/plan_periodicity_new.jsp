<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.io.File"%>
<%@ page import = "java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="plan" scope="page" class="com.redmoon.oa.person.PlanMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = ParamUtil.get(request, "userName");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>日程安排</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>

</HEAD>
<BODY>
<%@ include file="plan_inc_menu_top.jsp"%>
<script>
o("menu2").className="current"; 
</script>
<div class="spacerH"></div>
<script language="javascript" type="text/javascript">
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
        findObj(objName).value = dt;
}*/
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        findObj(objName).value = dt;
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
</script>
<form name="form1" action="plan_periodicity_do.jsp?op=add">
<table width="60%" align="center" class="tabStyle_1 percent60">
  <tr>
    <td colspan="2" class="tabStyle_1_title">添加周期性事务</td>
    </tr>
  <tr>
    <td>标题</td><td><input type="text" name="title" size="50" /></td>
  </tr>
  <tr>
    <td>开始时间</td>
	<%
		Date d = new Date();
	%>
	<td>
      <input type="text" id="startdate" name="startdate" size="20" readonly="" value="<%=DateUtil.format(d,"yyyy-MM-dd")%>"/>
		</td>     
  </tr>
  <tr>
    <td>结束时间</td>
	<td>
    <input type="text" id="enddate" name="enddate" size="20" readonly="" />
    </td>
  </tr>
  <tr>
    <td>提醒类型</td>
	<td><select id="type" name="type" onchange="typeChange()">
	    <option value="1" selected="selected">按日提醒</option>
		<option value="2">按周提醒</option>
		<option value="3">按月提醒</option>
		<option value="4">按年提醒</option>
	    </select>
		<script>
		document.getElementById('type').options.selectedIndex = 0;
		</script>
		<input name="userName" value="<%=userName%>" type="hidden" /></td>
  </tr>
  <tr>
    <td>提醒时间</td>
	<td>
	<%
		Calendar current=Calendar.getInstance();
		int currentYear = current.get(Calendar.YEAR);
		int currentMonth = current.get(Calendar.MONTH)+1;   
		int currentDay = current.get(Calendar.DATE);
		int currentDate = current.get(Calendar.DAY_OF_WEEK)-1;
		if(currentDate == 0){
			currentDate = 7;
		}
		int dayCount = DateUtil.getDayCount(currentYear,currentMonth-1);
	%>
	<select id="month" name="month" style="display:none">
		<%for(int i=1;i<=12;i++){%>
			<option value="<%=i%>"><%=i%>月</option>
		<%}%>
		<script>
		document.getElementById('month').options.selectedIndex = <%=currentMonth-1%>;
		</script>
	</select>
	<select id="day" name="day" style="display:none">
		<%for(int i=1;i<=dayCount;i++){%>
			<option value="<%=i%>"><%=i%>日</option>
		<%}%>
		<script>
			document.getElementById('day').options.selectedIndex = <%=currentDay-1%>;
		</script>
	</select>
	<%
		String[] weekDate = {"星期一","星期二","星期三","星期四","星期五","星期六","星期日"}; 
	%>
	<select id="week" name="week" style="display:none">
		<%for(int i=0;i<7;i++){%>
			<option value="<%=i%>"><%=weekDate[i]%></option>
		<%}
		%>
		<script>
			document.getElementById('week').options.selectedIndex = <%=currentDate-1%>;
		</script>
	</select>
	<input name="reminddate" size="10" readonly="" style="display:none" />
    <img style="CURSOR: hand; display:none" onclick="SelectDate('reminddate','yyyy-mm-dd')" src="../images/form/calendar.gif" align="absmiddle" width="26" height="26" />
    <input type="text" id="remindtime" name="remindtime" size="8" value="<%=DateUtil.format(d,"HH:mm:00")%>" />
	</td>
  </tr>
  <tr>
    <td>事务内容</td>
	<td><textarea name="content" cols="55" rows="7" ></textarea></td>
  </tr>
  <tr>
    <td colspan="2" style="text-align:center"><input class="btn" type="submit" value="确定" />&nbsp;&nbsp; <input class="btn" type="button" onclick="window.location.href='plan_periodicity.jsp'" value="返回" /></td>
  </tr>
</table>
</form>
<script>
$(function(){
	$('#startdate').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00',lang:'ch'});
	$('#enddate').datetimepicker({value:'<%=DateUtil.format(new java.util.Date(),"yyyy-MM-dd HH:mm:ss") %>',step:10, format:'Y-m-d H:i:00',lang:'ch'});
    $('#remindtime').datetimepicker({
		datepicker:false,
		format:'H:i:00',
		step:1
	});
})
function typeChange(){
	var type = document.getElementById('type').value;
	if(type == 1){
		document.getElementById('month').style.display = "none";
		document.getElementById('week').style.display = "none";
		document.getElementById('day').style.display = "none";
	}else if(type == 2){
		document.getElementById('month').style.display = "none";
		document.getElementById('week').style.display = "";
		document.getElementById('day').style.display = "none";
	}else if(type == 3){
		document.getElementById('month').style.display = "none";
		document.getElementById('week').style.display = "none";
		document.getElementById('day').style.display = "";
	}else{
		document.getElementById('month').style.display = "";
		document.getElementById('week').style.display = "none";
		document.getElementById('day').style.display = "";
	}
}
</script>
</BODY>
</HTML>

