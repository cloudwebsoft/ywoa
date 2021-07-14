<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<HTML><HEAD><TITLE>选择用户</TITLE>
<link rel="stylesheet" href="common.css">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style1 {
	font-size: 12pt;
	font-weight: bold;
}
-->
</style>
<script>
function selPerson(userName, userRealName) {
	var title = window.opener.getActionTitle();
	var OfficeColorIndex = window.opener.getActionColorIndex();
	var jobCode = window.opener.getActionJobCode();
	var jobName = window.opener.getActionJobName();
	var proxyJobCode = window.opener.getActionProxyJobCode();
	var proxyJobName = window.opener.getActionProxyJobName();
	var proxyUserName = window.opener.getActionProxyUserName();
	var proxyUserRealName = window.opener.getActionProxyUserRealName();
	var fieldWrite = window.opener.getActionFieldWrite();
	var checkState = window.opener.getActionCheckState();
	var dept = window.opener.getActionDept();
	var nodeMode = <%=WorkflowActionDb.NODE_MODE_ROLE_SELECTED%>;
	window.opener.ModifyAction(userName, title, OfficeColorIndex, userRealName, jobCode, jobName, proxyJobCode, proxyJobName, proxyUserName, proxyUserRealName, fieldWrite, checkState, nodeMode);
	window.close();
}
</script>
</HEAD>
<BODY bgColor=#FBFAF0 leftMargin=4 topMargin=8 rightMargin=0 class=menubar>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String roleCodes = ParamUtil.get(request, "roleCodes");
String[] ary = StrUtil.split(roleCodes, ",");
int len = 0;
if (ary==null) {
	return;
}
len = ary.length;
%>
<table width="100%" height="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
  <tr> 
    <td height="24" colspan="4" align="center" class="right-title"><span>用户角色</span></td>
  </tr>
  <tr> 
    <td width="3%">&nbsp;</td>
    <td colspan="2" valign="top">
<%
String showCode = ParamUtil.get(request, "showCode");
String code;
String desc;
RoleDb urole = new RoleDb();
Iterator userir = null;
if (!showCode.equals("")) {
	urole = urole.getRoleDb(showCode);
	userir = urole.getAllUserOfRole().iterator();
}
else
	userir = (new Vector()).iterator();
%>
      <br>
      <table width="95%" align="center">
        <tbody>
<%
for (int i=0; i<len; i++) {
 	RoleDb ug = urole.getRoleDb(ary[i]);
	code = ug.getCode();
	desc = ug.getDesc();
%>
          <tr class="row" style="BACKGROUND-COLOR: #ffffff">
            <td width="31%">
			  <a href="?showCode=<%=StrUtil.UrlEncode(code)%>&roleCodes=<%=StrUtil.UrlEncode(roleCodes)%>" menu="true"><%=desc%></a>			</td>
          </tr>
<%}%>
        </tbody>
      </table>	</td>
    <td width="63%" align="center" valign="top" bgcolor="#F3F3F3">
	<div id="resultTable">
	  <table width="100%" border="0" cellpadding="4" cellspacing="0">
      <thead>
        <tr>
          <th width="91" align="left" bgcolor="#B4D3F1">职员</th>
          </tr>
      </thead>
      <tbody id="postsbody">
	  <%
	  while (userir.hasNext()) {
	  	UserDb ud = (UserDb)userir.next();
	  %>
	  <tr>
	    <td><a onClick="selPerson('<%=ud.getName()%>', '<%=ud.getRealName()%>')" href="javascript:;"><%=ud.getRealName()%></a></td>
	    </tr>
	  <%}%>
      </tbody>
    </table>
	</div><table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="30" align="center">&nbsp;</td>
  </tr>
</table>	</td>
  </tr>
</table>
</BODY></HTML>
