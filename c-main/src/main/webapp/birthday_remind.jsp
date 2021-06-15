<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="java.util.*"%>
<LINK href="common.css" type=text/css rel=stylesheet>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin"))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String nowMonth = ""+DateUtil.format(new java.util.Date() , "MM");
String nowDate = ""+DateUtil.format(new java.util.Date() , "dd");

UserDb userdb = new UserDb();
String sql ="select distinct name from users where month(birthday) = " + StrUtil.sqlstr(nowMonth) + " and dayofmonth(birthday) = " + StrUtil.sqlstr(nowDate) + " and isValid=1 order by name ";
Iterator ir = userdb.list(sql).iterator();		
%>
<html>
<head>
<title>生日提醒</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<%@ include file="inc/nocache.jsp"%>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="600" border="0" cellspacing="1" cellpadding="3" align="center" height="300" bgcolor="#FFFFFF" class="tableframe_gray">
  <tr> 
    <td width="600" height="23" class="right-title">
        <div align="center"><b>查看生日 </b></div></td>
  </tr>
  <tr> 
    <td valign="top">
	<%
		while (ir.hasNext()) {
		UserDb user = (UserDb)ir.next();
		   out.println(user.getRealName() + "[" + user.getName() + "]<br />");
		}
	%>
	</td>
  </tr>
</table>
</body>
</html>
