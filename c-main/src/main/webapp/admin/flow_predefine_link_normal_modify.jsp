<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<HTML><HEAD><TITLE>流程连接属性</TITLE>
<link href="../common.css" rel="stylesheet" type="text/css">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<script src="../inc/common.js"></script>
<script language="JavaScript">
function openWin(url,width,height)
{
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function ModifyLink() {
	if (expireHour.value.trim()!="") {
		if (!isNumeric(expireHour.value.trim())) {
			alert("到期时间必须为大于1的数字！");
			return;
		}
		if (expireHour.value<0) {
			alert("到期时间必须为大于或等于0的数字！");
			return;
		}
	}
	window.opener.SetSelectedLinkProperty("expireHour", expireHour.value);
	window.opener.SetSelectedLinkProperty("expireAction", expireAction.value);
	window.close();
}

function window_onload() {
	expireHour.value = window.opener.GetSelectedLinkProperty("expireHour");
	expireAction.value = window.opener.GetSelectedLinkProperty("expireAction");
}
</script>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</HEAD>
<BODY leftMargin=4 topMargin=8 rightMargin=0 class=menubar onLoad="window_onload()">
<table width="100%"  border="0" align="center" cellpadding="2" cellspacing="1" bgcolor="#CCCCCC">
  <tr>
    <td height="23" colspan="2" class="right-title">&nbsp;&nbsp;流程连接属性</td>
  </tr>
  
  <tr>
    <td width="90" height="22" align="center" bgcolor="#FFFFFF">到期</td>
    <td height="22" bgcolor="#FFFFFF"><input type="text" name="expireHour" style="width: 60px" value="0">
      <%
	  	Config cfg = new Config();
		String flowExpireUnit = cfg.get("flowExpireUnit");
		if (flowExpireUnit.equals("day")) {
			out.print("天");
		} else {
			out.print("小时");
		}
	  %>      
	  (下一节点人员处理的到期时间，0表示不限时)&nbsp;&nbsp;超期则
	  <select name="expireAction">
	  <option value="">等待</option>
	  <option value="next">交办至后续节点</option>
	  </select>
	  </td>
  </tr>
  
  <tr align="center">
    <td height="28" colspan="2" bgcolor="#FFFFFF"><input name="okbtn" type="button" class="button1" onClick="ModifyLink()" value=" 确 定 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name="cancelbtn" type="button" class="button1" onClick="window.close()" value=" 取 消 "></td>
  </tr>
</table>
</BODY></HTML>
