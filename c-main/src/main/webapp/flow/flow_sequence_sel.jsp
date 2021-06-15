<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>序列号</title>
<link href="../common.css" rel="stylesheet" type="text/css">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
%>
<table width="100%" height="68" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td height="28" class="tabStyle_1_title">&nbsp;工作流序列号种类选择</td>
    </tr>
    <tr>
      <td height="42" align="center" bgcolor="#FFFFFF" class="head"><%
WorkflowSequenceDb wsd = new WorkflowSequenceDb();
java.util.Iterator ir = wsd.list().iterator();
String opts = "";
while (ir.hasNext()) {
	wsd = (WorkflowSequenceDb)ir.next();
	opts += "<option value='" + wsd.getId() + "'>" + wsd.getName() + "</option>";
}
%>
<select name="sel" style="width:200px">
<%=opts%>
</select>
&nbsp;&nbsp;<input type="button" class="btn" value="确定" onClick="doSel()"></td>
    </tr>
  </tbody>
</table>
</body>
<script language="javascript">
<!--
function doSel() {
	window.opener.setSequence(sel.options[sel.selectedIndex].value, sel.options[sel.selectedIndex].text);
	window.close();
}
//-->
</script>
</html>