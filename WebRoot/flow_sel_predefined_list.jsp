<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="common.css">
<title>预定义流程列表</title>
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
	GetDate = showModalDialog("util/calendar/calendar.htm", PostAtt ,"dialogWidth:286px;dialogHeight:221px;status:no;help:no;");
}

function SetDate()
{ 
	findObj(ObjName).value = GetDate; 
}

function sel(id) {
	var url = window.parent.opener.location.href;
	var p = url.indexOf("?");
	url = url.substring(0, p);
	var flowId = window.parent.opener.getFlowId();
	window.parent.opener.location.href = url + "?op=loadPredefinedFlow&preId=" + id + "&flowId=" + flowId;
	window.parent.close();
}

function openWin(url,width,height)
{
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
</script>
<style type="text/css">
<!--
.STYLE3 {color: #FFFFFF}
-->
</style>
</head>
<body>
<%
String dirCode = ParamUtil.get(request, "dirCode");
if (dirCode.equals("")) {
	out.print(StrUtil.p_center("请选择流程类型！"));
	return;
}
Leaf lf = new Leaf();
lf = lf.getLeaf(dirCode);
if (lf==null || !lf.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "节点不存在！"));
	return;
}

WorkflowPredefineDb fd = new WorkflowPredefineDb();
%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = PrivDb.PRIV_ADMIN;
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
		
String op = ParamUtil.get(request, "op");
if (op.equals("del")) {
	WorkflowPredefineMgr ftm = new WorkflowPredefineMgr();
	boolean re = false;
	try {
		re = ftm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert("删除成功！"));
	}
	else {
		out.print(StrUtil.Alert("删除失败！"));
	}
}		
%>
<br />
<table width="100%" cellpadding="0" cellspacing="1">
  <tr>
    <td width="55%" height="25" align="center" bgcolor="#5286BD" ><span class="STYLE3">名称</span></td>
    <td width="29%" align="center" bgcolor="#5286BD" class="STYLE3" >类别</td>
    <td width="16%" height="25" align="center" bgcolor="#5286BD" ><span class="STYLE3">操作</span></td>
  </tr>
</table>
<%
		WorkflowPredefineDb ftd = new WorkflowPredefineDb();
		String sql = "select id from flow_predefined where typeCode=" + StrUtil.sqlstr(dirCode);
		Iterator ir = ftd.list(sql).iterator();
		while (ir.hasNext()) {
			ftd = (WorkflowPredefineDb) ir.next();
			lf = lf.getLeaf(ftd.getTypeCode());
%>
<table width="100%"  border="0" cellpadding="5" cellspacing="1" class="p14">
  <tr>
    <td width="55%" bgcolor="#EAEAEA" ><a href="javascript:openWin('flow_sel_predefined_preview.jsp?id=<%=ftd.getId()%>', 640, 365)"><%=ftd.getTitle()%></a></td>
    <td width="29%" bgcolor="#EAEAEA" ><%=lf.getName()%></td>
    <td width="16%" align="center" bgcolor="#EAEAEA" ><input type="button" value="选择" onclick="sel('<%=ftd.getId()%>')"></td>
  </tr>
</table>
<%}%>
<br />
<br />
</body>
</html>
