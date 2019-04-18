<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.redmoon.oa.util.TwoDimensionCode"%>
<%@page import="com.redmoon.oa.Config"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>系统变量</title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}
</script>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin";
if (!privilege.isUserPrivValid(request, priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="config_m_inc_menu_top.jsp"%>
<script>
o("menu5").className="current";
</script>
<div class="spacerH"></div>
<%
XMLConfig cfg = new XMLConfig("config_cws.xml", false, "utf-8");
Config oaconfig = new Config();
String op = ParamUtil.get(request, "op");

if (op.equals("setup")) {
	String v = ParamUtil.get(request, "flowNotifyByEmail");
	if (v == null || v.equals("")) {
		v = "false";
	}
	oaconfig.put("flowNotifyByEmail", v);
	oaconfig.writemodify();
	
	Enumeration e = request.getParameterNames();
	while (e.hasMoreElements()) {
		String fieldName = (String)e.nextElement();
		if (fieldName.startsWith("Application")) {
			String value = ParamUtil.get(request, fieldName);
			if (fieldName.equals("Application.smtpSSL") && value.equals("")) {
				value = "false";
			}
			if (fieldName.equals("Application.smtpCharset") && value.equals("other")) {
				value = ParamUtil.get(request, "otherCharset");
			}
			cfg.set(fieldName, value);
		}
	}
	cfg.writemodify();
	Global.init();
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "config_email_m.jsp"));
	return;
}
%>
<form action="?op=setup" method="post" name="form1" id="form1">
<table class="tabStyle_1 percent80" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" height="24" colspan="2" align="center">流程交办时用于提醒的Email配置</td>
    </tr>
    <tr>
      <td width="36%" height="24" align="right">是否启用：</td>
      <td width="64%" align="left">
	      <input type="checkbox" name="flowNotifyByEmail" value="true" <%=oaconfig.getBooleanProperty("flowNotifyByEmail") ? "checked" : "" %>/>
    </tr>
    <tr>
      <td height="24" align="right">邮箱服务器地址：</td>
      <td align="left"><input name="Application.smtpServer" value="<%=Global.getSmtpServer()%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">邮箱服务器端口：</td>
      <td align="left"><input type="text" name="Application.smtpPort" value="<%=Global.getSmtpPort()%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">邮箱登录账号：</td>
      <td align="left"><input type="text" name="Application.smtpUser" value="<%=Global.getSmtpUser()%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">邮箱登录密码：</td>
      <td align="left"><input type="text" name="Application.smtpPwd" value="<%=Global.getSmtpPwd()%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">邮箱地址：</td>
      <td align="left"><input type="text" name="Application.email" value="<%=Global.getEmail()%>"/>
        ( 例如：123456@qq.com )</td>
    </tr>
    <tr>
      <td height="24" align="right">是否采用SSL安全套接字连接：</td>
      <td align="left"><input type="checkbox" name="Application.smtpSSL" value="true" <%=Global.isSmtpSSL() ? "checked" : ""%>"/></td>
    </tr>
    <tr>
      <td height="24" align="right">邮箱服务器编码：</td>
      <td align="left">
      	<select id="smtpCharset" name="Application.smtpCharset" onchange="showOther(this)">
      		<option value="">默认</option>
      		<option value="UTF-8">UTF-8</option>
      		<option value="gb2312">gb2312</option>
      		<option value="other">其他</option>
      	</select>
      	<input type="text" id="otherCharset" name="otherCharset" value="<%=Global.getSmtpCharset() %>" style="display:none" />
      </td>
    </tr>
    <tr>
      <td height="24" colspan="2" align="center"><input name="button" type="button" onclick="form1.submit()" value=" 确 定 "  class="btn"/></td>
    </tr>
</table>
  </form>
</body>
<script>
$(function() {
	<%if (Global.getSmtpCharset().equals("") || Global.getSmtpCharset().equals("UTF-8") || Global.getSmtpCharset().equals("gb2312")) {%>
		$("#smtpCharset").find("option[value='<%=Global.getSmtpCharset()%>']").attr("selected", true);
		$('#otherCharset').hide();
	<%} else {%>
		$("#smtpCharset").find("option[value='other']").attr("selected", true);
		$('#otherCharset').show();
	<%}%>
});

function showOther(obj) {
	var temp = obj.options[obj.selectedIndex].value;
	if (temp == 'other') {
		$('#otherCharset').show();
	} else {
		$('#otherCharset').hide();
	}
}
</script>
</html>                            
  