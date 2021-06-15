<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.PlanDb"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int id = ParamUtil.getInt(request, "id");
PlanDb pdb = new PlanDb();
pdb = pdb.getPlanDb(id);
String userName = pdb.getUserName();

// System.out.println(getClass() + " " + userName + " " + privilege.getUser(request));
if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}	
String menuItem = ParamUtil.get(request, "menuItem");

boolean isShared = ParamUtil.getBoolean(request, "isShared", false);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>修改日程</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
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
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
  <tr class="stable">
    <td colspan="2" class="tabStyle_1_title">日程安排</td>
  </tr>
  <form action="plan_add_do.jsp?op=modify&id=<%=id%>&isShared=<%=isShared%>" method="post" name="form1" id="form1" onsubmit="">
    <tr>
      <td align="center">标题</td>
      <td><input name="title" size="50" value="<%=pdb.getTitle()%>"/></td>
    </tr>
    <tr>
      <td align="center">开始时间 </td>
	  <%
		String startDate = DateUtil.format(pdb.getMyDate(),"yyyy-MM-dd HH:mm:ss");
	  	//startDate = startDate.replace("-","/").substring(0,startDate.length()-3);
	  %>
      <td>
        <input id="mydate" name="mydate" size="18" readonly="" value="" />
        <script>
        o("mydate").value = "<%=startDate %>";
        </script>
    </tr>
	<tr>
      <td align="center">结束时间 </td>
	  <%
	  	String endDate = DateUtil.format(pdb.getEndDate(),"yyyy-MM-dd HH:mm:ss");
		if (endDate.equals("")){
			endDate = startDate;
		}
		//endDate = endDate.replace("-","/").substring(0,endDate.length()-3);
	  %>
      <td>
      <input id="enddate" name="enddate" size="18" readonly="" value="" />
		<script>
		o("enddate").value = "<%=endDate %>";
		</script>
    </tr>
	<tr>
	  <td align="center">是否完成</td>
	  <td><input type="checkbox" value="1" name="isClosed" <%=pdb.isClosed()?"checked":""%> />
      <%=pdb.isClosed()?"<img src='../images/task_complete.png' style='width:16px'>":"<img src='../images/task_ongoing.png' style='width:16px'"%></td>
    </tr>
	<tr>
	  <td align="center">是否便笺</td>
	  <td><input type="checkbox" value="1" name="isNotepaper" <%=pdb.isNotepaper()?"checked":""%> />
      <%if (pdb.isNotepaper()) {%><img src="../images/note.png" /><%}%>
      </td>
    </tr>
    <%if (privilege.isUserPrivValid(request, "plan.share")) {%>
	<tr>
	  <td align="center">共享</td>
	  <td><input type="checkbox" value="1" name="shared" <%=pdb.isShared()?"checked":""%> /></td>
    </tr>
    <%}%>    
	<tr>
	  <td align="center">是否提醒</td>
	  <td><input type="checkbox" value="true" name="isRemind" <%=pdb.isRemind()?"checked":""%> />
&nbsp;&nbsp;
<select name="before" id="before">
  <option value="0">请选择</option>
  <option value="10" selected>十分钟</option>
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
			  Date my_date = pdb.getMyDate();
		      Date remind_date = pdb.getRemindDate();		   
		%>
		<script>
		o("before").value = <%=DateUtil.datediffMinute(my_date,remind_date)%>
		</script>
		<input name="userName" value="<%=userName%>" type="hidden" /></td>
    </tr>
	<%
    if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {
    %>
	<tr>
	  <td align="center">短信提醒</td>
	  <td><input name="isToMobile" value="true" type="checkbox" checked="checked" /></td>
    </tr>
    <%}%>
    <tr>
      <td align="center">内&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;容</td>
      <td><textarea name="content" cols="50"  rows="8"><%=pdb.getContent()%></textarea>      </td>
    </tr>
    <tr>
      <td align="center" colspan="2"><input name="submit" type="submit" class="btn" value=" 确定 " />
        &nbsp;&nbsp;&nbsp;
        <input class="btn" name="button" type="button" onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='plan_del.jsp?id=<%=id%>'}})" value=" 删除 " />
        <!--
        &nbsp;&nbsp;&nbsp;
        <input class="btn" name="button" type="button" onclick="window.history.back()" value=" 返回 " />
        -->
        </td>
    </tr>
  </form>
</table>
</body>
<script language=javascript>
<!--
$(function(){
	$('#mydate').datetimepicker({
		lang:'ch',
		value:'',
		step:10, 
		format:'Y-m-d H:i:00'
	});
    $('#enddate').datetimepicker({
    	lang:'ch',
        value:'',
        step:10, 
        format:'Y-m-d H:i:00'
    });
})
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

	GetDate = showModalDialog("../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:220px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
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
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}


//-->
</script>
</html>