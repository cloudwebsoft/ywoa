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
<%@page import="com.redmoon.oa.sms.SMSTemplateDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<%
	String priv="admin";
	if (!privilege.isUserPrivValid(request,priv)) {
	    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>短信模板修改</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
	<script src="../inc/livevalidation_standalone.js"></script>
<script>		
function setObj(th,id){
	document.getElementById('master').value =th;
	document.getElementById('master_id').value =id;
}
</script>
<%
	long id = ParamUtil.getLong(request,"id");
	SMSTemplateDb stDb = new SMSTemplateDb();
	stDb = stDb.getMSTemplateDb(id);
%>
</head>
<body>
<form name="form1" action="sms_template_add_do.jsp?op=edit" method="post">
<input type="hidden" name = id  value="<%=stDb.getInt("id")%>">
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">短信模版</td>
      </tr>
  </tbody>
</table>
<TABLE width="73%" align="center" cellPadding=0 cellSpacing=0 class="tabStyle_1 percent60">
<thead>
<TR>
  <TD colspan="2" align=center>模板</TD>
</TR>
</thead>
<TBODY>
<TR>
<TD width="19%" align=right>短信类型：</TD>
<TD width="71%" align=left>&nbsp;<%
out.print(BasicSelectCtl.convertToHTMLCtl(request, "sms_type", "sms_type"));
String type = stDb.getString("type");
%>
<script>
	form1.sms_type.value="<%=type%>";
</script>
</TD>
</TR>
<tr>
<td align=right>短信内容：</td>
<td align=left >
<textarea id="content" name="content" rows="5" cols="60"><%=stDb.getString("content")%></textarea>
<script>
var content = new LiveValidation('content');
content.add(Validate.Presence, { failureMessage:'请填写内容'} );
</script>
</td>
</tr>
<tr>
  <td colspan="2" align=center><input type="submit" value="修改" class="btn" /></td>
  </tr>

</TBODY>
</TABLE>&nbsp;
</form>
  </body>
</html>
