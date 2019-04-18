<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
WorkflowPredefineMgr wpm = new WorkflowPredefineMgr();
WorkflowPredefineDb wpd = null;
int id = ParamUtil.getInt(request, "id");
wpd = wpm.getWorkflowPredefineDb(request, id);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>预览预定义流程</title>
<link href="common.css" rel="stylesheet" type="text/css">
<%@ include file="inc/nocache.jsp"%>
<style type="text/css">
<!--
.style1 {color: #FFFFFF}
-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv))
{
	out.println(fchar.makeErrMsg("对不起，您没有权限！"));
	return;
}
%>
<table height="89" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="23" class="right-title">&nbsp;&nbsp;预览流程&nbsp;-&nbsp;<%=wpd.getTitle()%></td>
  </tr>
  <tr> 
    <td valign="top">
	<table width="100%"  border="0" cellspacing="0" cellpadding="0">
      
      <tr>
        <td align="center">
	<OBJECT ID="Designer" CLASSID="CLSID:ADF8C3A0-8709-4EC6-A783-DD7BDFC299D7" codebase="activex/cloudym.CAB#version=1,2,0,2" width=740 height=338>
    <param name="Workflow" value="<%=wpd.getFlowString()%>">
	<param name="Mode" value="user"><!--debug user initiate complete-->
	</OBJECT></td>
      </tr>
    </table></td>
  </tr>
</table>
</body>
<script>
function Operate()
{
}
</script>
</html>
