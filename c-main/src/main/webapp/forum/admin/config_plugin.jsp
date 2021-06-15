<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.redmoon.forum.*,
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
PluginMgr sm = new PluginMgr();
sm.init();

Element root = sm.root;
String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	code = ParamUtil.get(request, "code");
	String classUnit = ParamUtil.get(request, "classUnit");
	String isShowName = ParamUtil.get(request, "isShowName");
	sm.set(code, "classUnit", classUnit);	
	sm.set(code, "isShowName", isShowName);	

	sm.writemodify();
	sm.reload();
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_plugin.jsp"));
	return;
}
else if (op.equals("del")) {
	code = ParamUtil.get(request, "code");
	sm.delPluginUnit(code);
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_plugin.jsp"));
	return;
}
Vector v = sm.getAllPlugin();
if (v != null) {
Iterator ir = v.iterator();
while (ir.hasNext()) {
   	 PluginUnit pu = (PluginUnit) ir.next();
	 name = pu.getName(request);
%>
<table width="98%" border="0" align="center" cellpadding="3" cellspacing="1">
  <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_plugin.jsp?op=modify'>
  <tr>
    <td colspan="4" class="thead"><%=name%><input type="hidden" name="code" value="<%=pu.getCode()%>"/></td>
    </tr>
  <tr >
    <td width="15%" bgcolor="#F6F6F6">类</td>
    <td width="35%" bgcolor="#F6F6F6"><input type="input" name="classUnit" value="<%=pu.getClassUnit()%>"></td>
    <td width="15%" bgcolor="#F6F6F6">是否显示名称</td>
    <td width="35%" bgcolor="#F6F6F6">
	<select name="isShowName">
	<option value="true">是</option>
	<option value="false">否</option>
	</select>
	<script>
	form<%=k%>.isShowName.value = "<%=pu.isShowName()%>";
	</script>	</td>
  </tr>  
    <tr>
      <td bgcolor="#F6F6F6">管理入口</td>
      <td bgcolor="#F6F6F6"><input type="input" name="adminEntrance" value="<%=pu.getAdminEntrance()%>"></td>
      <td bgcolor="#F6F6F6">类型</td>
      <td bgcolor="#F6F6F6">
	  <%
	  if (pu.getType().equals("board"))
	  	out.print("版块");
	  else if (pu.getType().equals("topic"))
	  	out.print("贴子");
	  else if (pu.getType().equals("forum"))
	  	out.print("全局型");
	  %>	  </td>
    </tr>
    <tr>
    <td colspan="4" bgcolor="#F6F6F6"><div align="center">
      <INPUT TYPE=submit name='edit' value='<lt:Label key="op_modify"/>'>
    &nbsp;
    <INPUT TYPE="button" value='<lt:Label key="op_del"/>' onClick="if (confirm('您确定要删除吗？')) window.location.href='config_plugin.jsp?op=del&code=<%=pu.getCode()%>'">
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
