<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.search.Indexer"%>
<%@ page import="org.apache.lucene.search.*,org.apache.lucene.document.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title></title>
<link href="default.css" rel="stylesheet" type="text/css">
<LINK href="../../common.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<!--
<script src="../../inc/calendar.js"></script>
-->
<script src="../../inc/common.js"></script>
<script src="../../js/jquery-1.9.1.min.js"></script>
<script src="../../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.css"/>
<script src="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.js"></script>
<style type="text/css">
<!--
.style1 {
	color: #FFFFFF;
	font-weight: bold;
}
.style2 {color: #FFFFFF}
-->
</style>
</head>
<body bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="cfg" scope="page" class="cn.js.fan.web.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if(op.trim().equals("create")){
    String sFromDate = ParamUtil.get(request, "fromDate");
	String sToDate = ParamUtil.get(request, "toDate");
	String sFromTime = ParamUtil.get(request, "fromTime");
	String sToTime = ParamUtil.get(request, "toTime");
	
	MsgDb md = new MsgDb();
	Indexer indexer = new Indexer();
	long lBeginDate = 0;
	long lEndDate = 0;
	java.util.Date beginDate = null;
	java.util.Date endDate = null;
	
	if(!sFromDate.equals("") && !sToDate.equals("")){
	    if(!sFromTime.equals("")){
			beginDate = DateUtil.parse(sFromDate + " " + sFromTime, "yyyy-MM-dd HH:mm:ss");
		}else{
			beginDate = DateUtil.parse(sFromDate, "yyyy-MM-dd");
		}
		if(!sToTime.equals("")){
			endDate = DateUtil.parse(sToDate + " " + sToTime, "yyyy-MM-dd HH:mm:ss");
		}else{
			endDate = DateUtil.parse(sToDate, "yyyy-MM-dd");
		}
		lBeginDate = beginDate.getTime();
		lEndDate = endDate.getTime();
	}
	if(!sFromDate.equals("") && sToDate.equals("")){
	    if(!sFromTime.equals("")){
			beginDate = DateUtil.parse(sFromDate + " " + sFromTime, "yyyy-MM-dd HH:mm:ss");
		}else{
			beginDate = DateUtil.parse(sFromDate, "yyyy-MM-dd");
		}
		lBeginDate = beginDate.getTime();
		lEndDate = System.currentTimeMillis();
	}
	if(sFromDate.equals("") && !sToDate.equals("")) {
		if(!sToTime.equals("")){
			endDate = DateUtil.parse(sToDate + " " + sToTime, "yyyy-MM-dd HH:mm:ss");
		}else{
			endDate = DateUtil.parse(sToDate, "yyyy-MM-dd");
		}
		lEndDate = endDate.getTime();
	}
	if (indexer.index(md.list(lBeginDate, lEndDate), true))
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"full_text_search.jsp"));
	else
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_fail"),"full_text_search.jsp"));
}

if (op.equals("regenerate")) {
	Indexer indexer = new Indexer();
	MsgDb md = new MsgDb();	
	if (indexer.index(md.list(0, 0), false))
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"full_text_search.jsp"));
	else
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_fail"),"full_text_search.jsp"));
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head"><lt:Label res="res.label.forum.admin.full_text_search" key="create_index"/></td>
    </tr>
  </tbody>
</table>
<br>
<TABLE style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" cellSpacing=0 cellPadding=3 width="95%" align=center>
  <form name="form1" method="post" action="?op=create">
  <TBODY>
    <TR>
      <TD colspan="2" noWrap class=thead style="PADDING-LEFT: 10px">&nbsp;</TD>
    </TR>
    <TR>
      <TD width="32%" align="right"><lt:Label res="res.label.forum.admin.full_text_search" key="begin_date"/></TD>
      <TD width="68%">
		<input name="fromDate" id="fromDate" size=10>
		&nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" > <input style="WIDTH: 80px" name="fromTime" size="30"> 
		<img style="CURSOR: hand" onClick="SelectDateTime(form1.fromTime)" src="../images/clock.gif" align="absMiddle" width="18" height="18">	  </TD>
      </TR>
    <TR>
      <TD align="right"><lt:Label res="res.label.forum.admin.full_text_search" key="end_date"/></TD>
      <TD><input name="toDate" id="toDate" size=10>
        &nbsp;<img src="../../util/calendar/calendar.gif" align=absMiddle style="cursor:hand" >
        <input style="WIDTH: 80px" name="toTime" size="30">        <img style="CURSOR: hand" onClick="SelectDateTime(form1.toTime)" src="../images/clock.gif" align="absMiddle" width="18" height="18"> </TD>
      </TR>
    <TR>
      <TD align="right">&nbsp;</TD>
      <TD><input name="submit" type=submit value="<lt:Label res="res.label.forum.admin.full_text_search" key="increment_create"/>">        &nbsp;&nbsp;&nbsp;
        <input name="submit2" type=button value="<lt:Label res="res.label.forum.admin.full_text_search" key="all_create"/>" onClick="window.location.href='?op=regenerate'">	   </TD>
      </TR>
    <TR>
      <TD colspan="2" align="center"><lt:Label res="res.label.forum.admin.full_text_search" key="description"/></TD>
      </TR>	
    <TR>
      <TD colspan="2" align=right class=tfoot></TD>
    </TR>
  </TBODY>
  </form>
</TABLE>
</body>
<script>
$(function(){
	$('#fromDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d H:i',
    	step:1
    });
    $('#toDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d H:i',
    	step:1
    });
});

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
function SelectDate(ObjName,FormatDate) {
	var PostAtt = new Array;
	PostAtt[0]= FormatDate;
	PostAtt[1]= findObj(ObjName);

	GetDate = showModalDialog("../../util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate() { 
	findObj(ObjName).value = GetDate; 
} 

function SelectDateTime(obj) {
	var dt = showModalDialog("../../util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no;");
	if (dt!=null)
		obj.value = dt;
}
</script>
</html>