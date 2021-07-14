<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.door" key="login"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="forum/<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<script src="inc/common.js"></script>
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<br />
<%
String privurl = "";
String targeturl = StrUtil.getNullString(request.getParameter("targeturl"));
if (!targeturl.equals(""))
	privurl = targeturl;
else
	privurl = StrUtil.getNullString(request.getParameter("privurl"));
privurl = StrUtil.toHtml(privurl);
%>
<form action="login.jsp" method="post" name="form1" id="form1" onsubmit="return form1_onsubmit()">
<table border="0" align="center" class="tableCommon60">
    <thead>
	<tr>
      <td height="22" colspan="2" align="left"><lt:Label res="res.label.door" key="user_login"/>
    </td>
    </tr>
	</thead>
    <tr>
      <td width="23%" height="22" align="left"><lt:Label res="res.label.door" key="user_name"/></td>
      <td width="77%" height="22"><input name="name" style="width:120" /></td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.door" key="pwd"/></td>
      <td height="22"><input name="pwd" type="password" style="width:120" />
          <input name="privurl" type="hidden" value="<%=privurl%>" />
      </td>
    </tr>
    <%
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        if (cfg.getBooleanProperty("forum.loginUseValidateCode")) {
		%>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.door" key="validate_code"/></td>
      <td height="22"><input name="validateCode" type="text" size="4" />
          <img src='validatecode.jsp' border="0" align="absmiddle" style="cursor:hand" onclick="this.src='validatecode.jsp'" alt="<lt:Label res="res.label.forum.index" key="refresh_validatecode"/>" /></td>
    </tr>
    <%}%>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.door" key="save"/></td>
      <td height="22"><select id="loginSaveDate" name="loginSaveDate">
        <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_NONE%>" selected="selected">
          <lt:Label res="res.label.door" key="not_save"/>
          </option>
        <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_DAY%>">
          <lt:Label res="res.label.door" key="save_one_day"/>
          </option>
        <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_MONTH%>">
          <lt:Label res="res.label.door" key="save_one_month"/>
          </option>
        <option value="<%=com.redmoon.forum.Privilege.LOGIN_SAVE_YEAR%>">
          <lt:Label res="res.label.door" key="save_one_year"/>
          </option>
      </select>
      <script>
	  o("loginSaveDate").value = "<%=cfg.getProperty("forum.loginSaveDate")%>";
	  </script>
      </td>
    </tr>
    <tr>
      <td height="22" align="left"><lt:Label res="res.label.forum.index" key="login_hide"/></td>
      <td height="22"><select name="covered">
        <option value="0" selected="selected" type='checkbox' checked="checked">
          <lt:Label res="res.label.forum.index" key="login_not_hide"/>
          </option>
        <option value="1">
          <lt:Label res="res.label.forum.index" key="login_hide"/>
          </option>
      </select></td>
    </tr>
    <tr align="center">
      <td height="35" colspan="3"><input name="submit" type="submit" value="<lt:Label res="res.label.forum.index" key="commit"/>" />
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input name="button" type="button" onclick="window.location.href='regist.jsp'" value="<lt:Label res="res.label.door" key="regist"/>" /></td>
    </tr>
</table>
</form>
<p>&nbsp;</p>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<script language="javascript">
<!--
function form1_onsubmit(){
	errmsg = "";
	if (form1.name.value=="")
		errmsg += '<lt:Label res="res.label.door" key="input_name"/>\n'
	if (form1.pwd.value=="")
		errmsg += '<lt:Label res="res.label.door" key="input_pwd"/>\n'
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</html>