<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>打印预览</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
function onload() {
	document.getElementById("content").innerHTML = window.opener.getPrintContent();
	window.print();
}
</script>
</head>
<body onload="onload()">
<div id="content"></div>
</body>
</html>