<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>脚本</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style type="text/css" media="screen">
html { 
height:100%; 
_height:100%; //兼容IE6 
}

body { 
height:100%; 
_height:100%; //兼容IE6 
} 
</style>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
</head>
<body style="padding:0px; margin:0px">
<%@ include file="../admin/form_edit_inc_menu_top.jsp"%>
<script>
o("menu7").className="current"; 
</script>
<%
String code = ParamUtil.get(request, "code");
%>
<div class="spacerH" style="margin:0px; padding:0px"></div>
<iframe src="module_scripts.jsp?code=<%=code%>" style="padding:0px; margin:0px; width:100%; height:100%; border:0"></iframe>
</body>
</html>