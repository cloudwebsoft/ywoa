<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE>请等待</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
window.onload = function() {
	var t = 1000;
	try {
		t = dialogArguments.getWaitTime();
	}
	catch(e){}
	window.setTimeout("window.close()", t);
}
</script>
<style type="text/css">
<!--
body {
	background-color: #eeeeee;
}
-->
</style></HEAD>
<BODY>
<TABLE cellSpacing=0 cellPadding=0 width=100% border=0>
  <TBODY>
    <TR>
      <TD height="50" colSpan=2 align="center" vAlign=middle class=leftBorder>请等待，正在退出...</TD>
    </TR>
  </TBODY>
</TABLE>
</BODY>
</HTML>
