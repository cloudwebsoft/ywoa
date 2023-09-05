<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.security.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="admin";
	if (!privilege.isUserPrivValid(request, priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String op = ParamUtil.get(request, "op");
	
	if(op.equals("add")){
		try{
			ServerIPMgr sim = new ServerIPMgr();
			boolean re  = sim.create(request);
			if(re) {
				out.print(StrUtil.Alert_Redirect("添加成功！", "ip_list.jsp"));
			} else {
				out.print(StrUtil.Alert_Redirect("添加失败！", "ip_add.jsp"));
			}
		}catch (ErrMsgException e) {
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		return;
	}
	
%>
<!DOCTYPE html>
<html>
  <head>
    <title>服务器IP添加</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
  </head>
  
  <body>
    <form name="form1" action="ip_add.jsp?op=add" method="post" >
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">服务器IP</td>
      </tr>
  </tbody>
</table>
<TABLE width="71%" align="center" cellPadding=0 cellSpacing=0 class="tabStyle_1 percent60">
<thead>
<TR>
  <TD colspan="2" align=center>添加服务器</TD>
  </TR>
</thead>
<TBODY>
<TR>
<TD width="19%" align=right>IP</TD>
<TD width="81%" align=left>
<input id="ip" name="ip" />
</TD>
</TR>
<tr>
<td align=right>描述</td>
<td align=left ><textarea id="description" name="description" rows="5" cols="60"></textarea>
<script>
var ip = new LiveValidation('ip');
ip.add(Validate.Presence, { failureMessage:'请填写IP'} );
var content = new LiveValidation('content');
content.add(Validate.Presence, { failureMessage:'请填写描述'} );
</script>
</td>
</tr>
<tr>
  <td colspan="2" align=center>
  <input type="submit" value="确定" class="btn" />&nbsp;&nbsp;
  <input type="button" value="返回" class="btn" onclick="window.history.back()" />
  </td>
  </tr>
</TBODY>
</TABLE>&nbsp;
</form>
  </body>
</html>
