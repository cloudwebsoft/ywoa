<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="strutil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
var shen = "*,280,0";
var suo = "*,0,0";
function shensuo() {
	/*
	if (allFrame.rows==suo)
		allFrame.rows = shen;
	else
		allFrame.rows = suo;
	*/
	var rows = document.getElementById("allFrame").getAttribute('rows');
	if (rows==suo)
		document.getElementById("allFrame").setAttribute('rows',shen);	
	else
		document.getElementById("allFrame").setAttribute('rows',suo);	
}

function shrink(){
	document.getElementById("allFrame").setAttribute('rows',suo);	
}
</script>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String root_code = ParamUtil.get(request, "root_code");
%>
<frameset id="mainFrame" rows="" cols="30%,*">
	<frameset id="allFrame" rows="*,0,0" cols="*">
	  <frame src="dept_top_ajax.jsp?root_code=<%=StrUtil.UrlEncode(root_code)%>" name="dirmainFrame">
	  <frame src="dept_bottom.jsp" name="dirbottomFrame">
	</frameset>
  	<frame src="dept_do.jsp" name="dirhidFrame">  
</frameset>
<noframes><body>
</body></noframes>
</html>
