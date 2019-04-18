<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.redmoon.forum.miniplugin.*,
				 org.jdom.*,
                 java.util.*"
%>
<title>Score Manage</title>
<%@ include file="../inc/nocache.jsp" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<LINK href="images/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
<body bgcolor="#FFFFFF">
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head>插件管理</TD>
    </TR>
  </TBODY>
</TABLE>
<table cellpadding="6" cellspacing="0" border="0" width="100%">
<tr>
<td width="1%" valign="top"></td>
<td width="99%" align="center" valign="top">
<%
int k = 0;
String code="", name = "";
MiniPluginMgr sm = new MiniPluginMgr();
sm.init();

Element root = sm.root;
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	code = ParamUtil.get(request, "code");
	String isPlugin = ParamUtil.get(request, "isPlugin");
	sm.set(code, "isPlugin", isPlugin);	

	sm.writemodify();
	sm.reload();
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_miniplugin.jsp"));
	return;
}
else if (op.equals("del")) {
	code = ParamUtil.get(request, "code");
	sm.delPluginUnit(code);
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_miniplugin.jsp"));
	return;
}
Vector v = sm.getAllPlugin();
if (v != null) {
Iterator ir = v.iterator();
while (ir.hasNext()) {
   	 MiniPluginUnit pu = (MiniPluginUnit) ir.next();
	 name = pu.getName(request);
%>
<table width="98%" border="0" align="center" cellpadding="3" cellspacing="1">
  <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_miniplugin.jsp?op=modify'>
  <tr>
    <td colspan="4" class="thead"><%=name%><input type="hidden" name="code" value="<%=pu.getCode()%>"/></td>
    </tr>
  <tr >
    <td width="15%" bgcolor="#F6F6F6">管理入口</td>
    <td width="35%" bgcolor="#F6F6F6"><input type="input" name="adminEntrance" value="<%=pu.getAdminEntrance()%>"></td>
    <td width="15%" bgcolor="#F6F6F6">是否启用</td>
    <td width="35%" bgcolor="#F6F6F6">
	<select name="isPlugin">
	<option value="true">是</option>
	<option value="false">否</option>
	</select>
	<script>
	form<%=k%>.isPlugin.value = "<%=pu.isPlugin()%>";
	</script>	</td>
  </tr>  
    
    <tr>
    <td colspan="4" bgcolor="#F6F6F6"><div align="center">
      <INPUT TYPE=submit name='edit' value='<lt:Label key="op_modify"/>'>
    &nbsp;
    <INPUT TYPE="button" value='<lt:Label key="op_del"/>' onClick="if (confirm('您确定要删除吗？')) window.location.href='config_miniplugin.jsp?op=del&code=<%=pu.getCode()%>'">
    </div></td>
    </tr>
</form>  
</table>
	<%	
	k++;	 
   }   
 } // end if
%></td>
</tr>
</table>	
</body>
</html>
