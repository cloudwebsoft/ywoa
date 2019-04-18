<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<LINK href="../common.css" type="text/css" rel="stylesheet">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择模板-菜单</title>
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

function window_onload(depts) {
   try {
	   var depts = depts;	   
	   if (depts!="") {
		   var ary = depts.split(",");
		   var isFinded = true;
	   	   isFinded = false;
		   var len = document.all.tags('A').length;
		   for(var i=0; i<len; i++) {
		   		try {
					var aObj = document.all.tags('A')[i];
					var canSel = false;
					for (var j=0; j<ary.length; j++) {
					    if (aObj.outerHTML.split("=")[2].split("\"")[0] == ary[j]) {
						//if (aObj.outerHTML.indexOf("=" + ary[j])!=-1) {
							canSel = true;
							//alert(canSel);
							break;
						}
					}
					if (!canSel) {
						// aObj.innerHTML = "<font color='#888888'>" + aObj.innerText + "</font>";
						aObj.outerHTML = "<a onClick=><font color='#888888'>" + aObj.innerText + "</font></a>"; 
						// aObj.outerHTML.replace(/onClick/gi, "''");
					}
						
					isFinded = true;
				}
				catch (e) {}
		   }
	   }
	   else{
	   	   var len = document.all.tags('A').length;
		   for(var i=0; i<len; i++) {
		       var aObj = document.all.tags('A')[i];
			   aObj.outerHTML = "<a onClick=><font color='#888888'>" + aObj.innerText + "</font></a>";
		   }	  
	   }
   }
   catch (e) {}	
}
</script>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String operator = privilege.getUser(request);
UserDb ud = new UserDb();
ud = ud.getUserDb(operator);
String[] depts = ud.getAdminDepts();
String strDepts = "";

if (depts!=null) {
	int len = depts.length;
	for (int i=0; i<len; i++) {
	   strDepts += depts[i];
	   if(i < len - 1){
		   strDepts += ",";
	   }
	}
}
%>
<body onLoad="window_onload('<%=strDepts%>')" style="background-image:url()">
<table width="100%"  border="0">
  <tr>
    <td align="left">&nbsp;请选择部门</td>
  </tr>
</table>
<table width="100%"  border="0">
  <tr>
    <td width="5" align="left">&nbsp;</td>
  <td align="left">
<%
DeptMgr dm = new DeptMgr();
DeptDb dd = dm.getDeptDb(DeptDb.ROOTCODE);
DeptView tv = new DeptView(dd);
tv.ListSimple(request, out, "midFrame", "../archive/archive_user_main.jsp", "", "" );
%>
</td>
  </tr>
</table>
</body>
</html>
