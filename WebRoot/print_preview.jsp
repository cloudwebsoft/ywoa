<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>打印预览</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" media="screen" href="js/fixheadertable/base.css">
<link href="lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="lte/js/bootstrap.min.js?v=3.3.6"></script>
<style>
#formQueryTable {
border-collapse:collapse;
border:1px solid #000000;
}
#formQueryTable td {
border:1px solid #000000;
}
</style>
<script>
function onload() {
	document.getElementById("content").innerHTML = window.opener.getPrintContent();
	
	// 将嵌套表的操作列隐藏
	$('.tdOperate').hide();
	
	$('#content > table').width(1000);
	
	<%if (ParamUtil.getBoolean(request, "print", false)) {%>
	window.print();
	<%}%>
}
</script>
</head>
<body onload="onload()">
<div id="content"></div>
</body>
</html>