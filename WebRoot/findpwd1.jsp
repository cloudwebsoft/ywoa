<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.findpwd" key="findpwd"/> - <%=Global.AppName%>1</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<form name=form1 action="findpwd2.jsp" method="post" onSubmit="return form1_onsubmit()">
<table width="46%" class="tableCommon60" border="1" align="center" cellpadding="1" cellspacing="0">
  <thead>
  <tr>
    <td height="26" align="center" class="text_title"><lt:Label res="res.label.findpwd" key="user_service"/> - <lt:Label res="res.label.findpwd" key="findpwd"/></td>
  </tr>
  </thead>
  <tr>
    <td><lt:Label res="res.label.findpwd" key="input_user_name"/>&nbsp;
      <input name="name" class="singleboarder" style="width:120" />
  &nbsp;<a href="regist.jsp">
  <lt:Label res="res.label.findpwd" key="ask_not_regist"/>
  </a>
  <input name="submit" type=submit value="<lt:Label res="res.label.findpwd" key="next"/>" /></td>
  </tr>
</table></form>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<script language="javascript">
<!--
function form1_onsubmit()
{
	errmsg = "";
	if (form1.name.value=="")
		errmsg += '<lt:Label res="res.label.findpwd" key="need_user_name" />\n'
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</html>