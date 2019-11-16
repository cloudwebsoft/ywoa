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
<%--
bootstrap.min.css因下面的设置，会导致超链接在预览时显示为(超链接)
  a[href]:after {
    content: " (" attr(href) ")";
  }
<link href="lte/css/bootstrap.min.css?v=3.3.6" rel="stylesheet">
--%>
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
	
	<%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	// 适配纸张大小，但注意会影响打印预览比例，有的单位可能会要求缩放大小，打印在一张纸上
	if (cfg.getInt("pageSizeForced")>0) {
	%>
    $('.tabStyle_8').width(<%=cfg.getInt("pageSizeForced")%>).css({'margin':'0 auto'});
	$('#content').find('.percent98').width(<%=cfg.getInt("pageSizeForced")%>).css({'margin':'0 auto'});
    <%
    }

	if (ParamUtil.getBoolean(request, "print", false)) {%>
	window.print();
	<%}%>
}
</script>
</head>
<body onload="onload()">
<div id="content"></div>
</body>
</html>