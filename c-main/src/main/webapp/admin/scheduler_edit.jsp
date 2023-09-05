<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*,
				 java.text.*,
				 cn.js.fan.util.*,
				 com.redmoon.oa.flow.*,
				 cn.js.fan.cache.jcs.*,
				 cn.js.fan.web.*,
				 com.redmoon.oa.pvg.*,
				 com.redmoon.oa.kernel.*,
				 com.redmoon.oa.ui.*,
				 com.cloudwebsoft.framework.base.*"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
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
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm","266px","185px");
    
}
function sel(dt) {
    if (dt!=null)
        findObj("time").value = dt;
}
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function form1_onsubmit() {
	var t = form1.time.value;
	var ary = t.split(":");
	var weekDay = getCheckboxValue("weekDay");
	var dayOfMonth = form1.month_day.value;
	if (weekDay=="" && dayOfMonth=="") {
		alert("请填写每月几号或者星期几！");
		return false;
	}
	if (weekDay=="")
		weekDay = "?";	
	if (ary[2].indexOf("0")==0 && ary[2].length>1)
		ary[2] = ary[2].substring(1, ary[2].length);
	if (ary[1].indexOf("0")==0 && ary[1].length>1)
		ary[1] = ary[1].substring(1, ary[1].length);
	if (ary[0].indexOf("0")==0 && ary[0].length>1)
		ary[0] = ary[0].substring(1, ary[0].length);
	if (dayOfMonth=="")
		dayOfMonth = "?";
	var cron = ary[2] + " " + ary[1] + " " + ary[0] + " " + dayOfMonth + " * " + weekDay;
	form1.cron.value = cron;
	if (form1.flowCode.value=="not") {
		alert("请选择流程！");
		return false;
	}
	form1.data_map.value = form1.flowCode.value;
}

function trimOptionText(strValue) 
{
	// 注意option中有全角的空格，所以不直接用trim
	var r = strValue.replace(/^　*|\s*|\s*$/g,"");
	return r;
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

int id = ParamUtil.getInt(request, "id");

JobUnitDb ju = new JobUnitDb();
ju = (JobUnitDb)ju.getQObjectDb(new Integer(id));
if (op.equals("edit")) {
	QObjectMgr qom = new QObjectMgr();
	try {
	if (qom.save(request, ju, "scheduler_edit"))
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "scheduler_edit.jsp?id=" + id));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
	}
	return;
}
%>
<%@ include file="scheduler_inc_menu_top.jsp"%>
<div class="spacerH"></div>
<form action="?op=edit" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<table width="98%" border="0" class="tabStyle_1 percent80">
    <tr>
      <td align="left" class="tabStyle_1_title">调度流程</td>
    </tr>
    <tr>
      <td align="left">选择流程：
        <select name="flowCode" onchange="if(this.options[this.selectedIndex].value=='not'){alert(this.options[this.selectedIndex].text+' 不能被选择！'); return false;} form1.job_name.value=trimOptionText(this.options[this.selectedIndex].text) ">
            <%
					Leaf lf = new Leaf();
					lf = lf.getLeaf("root");
					DirectoryView dv = new DirectoryView(lf);
					dv.ShowDirectoryAsOptions(request, out, lf, 1);
				  %>
          </select>
          <script>
			form1.flowCode.value = "<%=ju.getString("data_map")%>";
			</script>
          <input name="job_class" type="hidden" value="com.redmoon.oa.job.WorkflowJob" />
          <input name="id" type="hidden" value="<%=id%>" />
        名称：
        <input name="job_name" value="<%=ju.getString("job_name")%>" />
        &nbsp;每月：
        <input name="month_day" size="2" value="<%=ju.getString("month_day")%>" />
        号
        <input name="user_name" value="<%=privilege.getUser(request)%>" type="hidden" /></td>
    </tr>
    <tr>
      <td align="left"> 开始时间：
<%
String cron = ju.getString("cron");
String[] ary = cron.split(" ");
if (ary[0].length()==1)
	ary[0] = "0" + ary[0];
if (ary[1].length()==1)
	ary[1] = "0" + ary[1];
if (ary[2].length()==1)
	ary[2] = "0" + ary[2];
String t = ary[2] + ":" + ary[1] + ":" + ary[0];
%>
        <input style="WIDTH: 50px" id="time" name="time" size="20" value="<%=t%>" />
        <!--&nbsp;<img style="CURSOR: hand" onclick="SelectDateTime('time')" src="../images/form/clock.gif" align="absmiddle" width="18" height="18" />-->
        <input name="weekDay" type="checkbox" value="1" />
        星期日
        <input name="weekDay" type="checkbox" value="2" />
        星期一
        <input name="weekDay" type="checkbox" value="3" />
        星期二
        <input name="weekDay" type="checkbox" value="4" />
        星期三
        <input name="weekDay" type="checkbox" value="5" />
        星期四
        <input name="weekDay" type="checkbox" value="6" />
        星期五
        <input name="weekDay" type="checkbox" value="7" />
        星期六
        
        <input name="cron" type="hidden" />
        <input name="data_map" type="hidden" />
        <%
String[] w = ary[5].split(",");
for (int i=0; i<w.length; i++) {
%>
        <script>
setCheckboxChecked("weekDay", "<%=w[i]%>");
</script>
        <%
}
%></td>
    </tr>
    <tr>
      <td align="center"><input name="submit" type="submit" value="确定" /></td>
    </tr>
</table>
</form>
</body>
<script>
$(function() {
	$('#time').datetimepicker({
     	lang:'ch',
     	datepicker:false,
     	format:'H:i:00',
     	step:1
	});
});
</script>
</html>
