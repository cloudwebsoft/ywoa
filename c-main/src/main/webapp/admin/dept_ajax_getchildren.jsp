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
<title>取子节点</title>
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
if (lf==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
	return;
}

DeptView tv = new DeptView(request, lf);

String root_code = ParamUtil.get(request, "root_code");
request.setAttribute("root_code", root_code);

String op = ParamUtil.get(request, "op");
if (op.equals("simple")) {
	String actionUrl = ParamUtil.get(request, "actionUrl");
	if (actionUrl.equals(""))
		actionUrl = request.getContextPath() + "/admin/dept_user.jsp";
	tv.ListSimpleAjax(request, out, "midFrame", actionUrl, "", "", false);
}
else if (op.equals("func")) {
	String func = ParamUtil.get(request, "func");
	if (func.equals(""))
		func = "updateResults";
	String target = ParamUtil.get(request, "target");
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "target", target, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	tv.ListFuncAjaxStyle(request, out, target, func, "", "", false);
}
else if (op.equals("funcCheckbox")) {
	String func = ParamUtil.get(request, "func");
	if (func.equals(""))
		func = "updateResults";
	String target = ParamUtil.get(request, "target");
	if (target.equals(""))
		target = "midFrame";
	
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "target", target, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	boolean isOnlyUnitCheckable = ParamUtil.get(request, "isOnlyUnitCheckable").equals("true");	
	tv.ListFuncWithCheckboxAjax(request, out, target, func, "", "", false, isOnlyUnitCheckable);
}
else if (op.equals("singleSel")) {
	tv.SelectSingleAjax(out, "selectNode", "", "", false );
}
else
	tv.listAjax(request, out, false);
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
