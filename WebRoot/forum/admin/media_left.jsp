<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.module.cms.*" %>
<%@ page import="com.redmoon.forum.media.*" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>管理</title>
<LINK href="default.css" type=text/css rel=stylesheet>
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

function ShowChild(imgobj, name)
{
	var tableobj = findObj("childof"+name);
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus-1-1.gif")!=-1)
			imgobj.src = "images/i_plus2-2.gif";
		if (imgobj.src.indexOf("i_plus-1.gif")!=-1)
			imgobj.src = "images/i_plus2-1.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_puls-root.gif")!=-1)
			imgobj.src = "images/i_puls-root-1.gif";
		if (imgobj.src.indexOf("i_plus2-2.gif")!=-1)
			imgobj.src = "images/i_plus-1-1.gif";
		if (imgobj.src.indexOf("i_plus2-1.gif")!=-1)
			imgobj.src = "images/i_plus-1.gif";
	}
}
</script>
<style type="text/css">
<!--
body {
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style></head>
<body style="overflow:auto">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String action = ParamUtil.get(request, "action");
com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "action", action, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if (!privilege.isMasterLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="dm" scope="page" class="com.redmoon.forum.media.MediaDirMgr"/>
<%
String root_code = ParamUtil.get(request, "root_code");
if (root_code.equals(""))
{
	root_code = "root";
}
%>
<Script>
var root_code = "<%=root_code%>";
// 使框架的bottom能得到此root_code
function getRootCode() {
	return root_code;
}
</Script>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("AddChild")) {
	boolean re = false;
	try {
		re = dm.AddChild(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re) {
		out.print(StrUtil.Alert("操作成功！"));
	}
}
if (op.equals("del")) {
	String delcode = ParamUtil.get(request, "code");
	try {
		dm.del(delcode);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
if (op.equals("modify")) {
	boolean re = false;
	try {
		re = dm.update(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
	if (re)
		out.print(StrUtil.Alert("修改完成"));
}
if (op.equals("move")) {
	try {
		dm.move(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}

MediaDirDb leaf = dm.getMediaDirDb(root_code);
if (op.equals("repair")) {
	dm.repairTree(leaf);
	leaf = dm.getMediaDirDb(root_code);
}

String root_name = leaf.getName();
int root_layer = leaf.getLayer();
String root_description = leaf.getDescription();
boolean isHome = false;
%>
<TABLE width="100%" height="100%" align=center cellPadding=0 
cellSpacing=0>
  <TBODY>
    <TR>
      <TD height="10" valign="top" bgcolor="#FFFBFF"></TD>
    </TR>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF"><%
MediaDirView dv = new MediaDirView(leaf);
dv.ListSimple(out, "mainFileFrame", "media_list.jsp?action=" + action + "&dirCode=", "", "" );
%></TD>
    </TR>
  </TBODY>
</TABLE>
</body>
</html>
