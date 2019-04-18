<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="java.text.*"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="com.redmoon.oa.security.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="admin";
	if (!privilege.isUserPrivValid(request, priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	int id = ParamUtil.getInt(request, "id");

	String op = ParamUtil.get(request, "op");
	if(op.equals("edit")){
	  try {
			ServerIPMgr simgr = new ServerIPMgr();
			boolean re = simgr.save(request);
			if(re){
				out.print(StrUtil.Alert_Redirect("修改成功！", "ip_list.jsp"));
			}
		}catch (ErrMsgException e) {  
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>服务器IP修改</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
<%
	ServerIPDb stDb = new ServerIPDb();
	stDb = (ServerIPDb)stDb.getQObjectDb(id);
%>
</head>
<body>
<form name="form1" action="ip_edit.jsp?op=edit" method="post">
<input type="hidden" name="id" value="<%=stDb.getInt("id")%>">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">服务器IP</td>
      </tr>
  </tbody>
</table>
<TABLE width="73%" align="center" cellPadding=0 cellSpacing=0 class="tabStyle_1 percent60">
<thead>
<TR>
  <TD colspan="2" align=center>修改服务器</TD>
</TR>
</thead>
<TBODY>
<TR>
<TD width="19%" align=right>IP</TD>
<TD width="71%" align=left>
<input id="ip" name="ip" value="<%=stDb.getString("ip")%>" />
</TD>
</TR>
<tr>
<td align=right>描述</td>
<td align=left >
<textarea id="description" name="description" rows="5" cols="60"><%=stDb.getString("description")%></textarea>
<script>
var content = new LiveValidation('content');
content.add(Validate.Presence, { failureMessage:'请填写内容'} );
</script>
</td>
</tr>
<tr>
  <td colspan="2" align=center><input type="submit" value="确定" class="btn" />
    &nbsp;&nbsp;
    <input type="button" value="返回" class="btn" onclick="window.history.go(-1)" /></td>
  </tr>

</TBODY>
</TABLE>&nbsp;
</form>
  </body>
</html>
