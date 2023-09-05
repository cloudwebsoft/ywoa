<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>取单位的孩子节点</title>
<LINK href="default.css" type=text/css rel=stylesheet>
<script>
function findObj(theObj, theDoc) {
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
</script>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.dept.DeptMgr"/>
<div id=content>
<%
String parentCode = ParamUtil.get(request, "parentCode");
if (parentCode.equals("")) {
	return;
}
DeptDb lf = new DeptDb();
lf = lf.getDeptDb(parentCode);

DeptView tv = new DeptView(request, lf);

String root_code = ParamUtil.get(request, "root_code");
request.setAttribute("root_code", root_code);

String op = ParamUtil.get(request, "op");
if (op.equals("funcCheckbox")) {
	String func = ParamUtil.get(request, "func");
	if (func.equals(""))
		func = "updateResults";
	String target = ParamUtil.get(request, "target");
	if (target.equals(""))
		target = "midFrame";
	
	boolean isOnlyUnitCheckable = ParamUtil.get(request, "isOnlyUnitCheckable").equals("true");	
	tv.ListUnitFuncWithCheckboxAjax(request, out, target, func, "", "", false, isOnlyUnitCheckable);
}
%>
</div>
</body>
<script>
window.parent.insertAdjacentHTML("<%=parentCode%>", document.getElementById("content").innerHTML, false);
// window.parent.<%=parentCode%>.insertAdjacentHTML("afterEnd", content.innerHTML);

<%
if (op.equals("funcCheckbox")) {
%>
window.parent.setDepts("<%=parentCode%>");
<%
}
%>
</script>
</html>
