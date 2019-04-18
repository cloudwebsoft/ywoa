<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.basic.TreeSelectDb"%>
<%@page import="com.redmoon.oa.basic.TreeSelectView"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.cms.dir" key="content"/></title>
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
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.basic.TreeSelectMgr"/>
<div id=content>
<%
String parentCode = ParamUtil.get(request, "parentCode");
if (parentCode.equals("")) {
	return;
}

TreeSelectDb lf = new TreeSelectDb();
lf = lf.getTreeSelectDb(parentCode);
if (lf==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
	return;
}

TreeSelectView tv = new TreeSelectView(lf);

String root_code = ParamUtil.get(request, "root_code");
request.setAttribute("root_code", root_code);

String op = ParamUtil.get(request, "op");

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
