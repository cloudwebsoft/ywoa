<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>档案查询设计器</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
html,body{height:100%}
</style>
</head>
<body onload="initIframe()">
<%
int id = ParamUtil.getInt(request, "id", -1);
%>
<table id="tbl" width="100%" border="0" align="center" cellpadding="0" cellspacing="0" style="border:0px solid #cccccc; background-color:red; height:100%"><tr><td align="center" valign="top">
  <iframe id="ifrm" style="border:0px" frameborder="0" align="center" src="designer.jsp?id=<%=id%>" width="100%" height="100%"></iframe>
</td></tr></table>
</body>
<script>
function initIframe(){
	var iframes = document.getElementsByName("ifrm");
	for (i=0; i<iframes.length; i++) {
		try{
		var cw = document.getElementById("tbl").clientWidth;
		var sw = document.getElementById("tbl").scrollWidth;
		var w = Math.max(cw, sw);
		
		//window.status = document.documentElement.scrollWidth ;
		
		iframes[i].width = w - 2; // IE6的bug，宽度包含了边框
		iframes[i].height = document.getElementById("tbl").clientHeight - 70;
			
		}catch (ex){}
	}
}
</script>
</html>