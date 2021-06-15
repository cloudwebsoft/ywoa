<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import ="com.redmoon.forum.ImageUtil"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.io.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="default.css" rel="stylesheet" type="text/css">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ include file="../../inc/nocache.jsp" %>
<title><lt:Label res="res.label.forum.admin.ad_list" key="check_referer"/></title>
<%
String op = ParamUtil.get(request, "op");
if(op.equals("modfiy")) {
    ImageUtil iu = new ImageUtil();
    try {
	  iu .modify(application, request, "err_pvg.gif");
	  out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "check_referer.jsp")); 
	} catch(ErrMsgException e) {
	  out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
</head>

<body>

<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.ad_list" key="check_referer"/></td>
  </tr>
</table>
<br>
 <table width="92%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999">
  <tr class="thead">
    <td><lt:Label res="res.label.forum.admin.ad_list" key="check_referer_modfiy"/></td>
   </tr> 
<form name="form_upload" enctype="MULTIPART/FORM-DATA" action="?op=modfiy" method="post">
  <tr>
    <td bgcolor="#FFF7FF"><br>&nbsp;&nbsp;<img src="../../images/err_pvg.gif" border="0"/>&nbsp; <br>
        <br>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input name=filename type=file id="filename"> 
      <input type=submit value=<lt:Label key="ok"/>/>
      &nbsp;&nbsp;&nbsp;<lt:Label res="res.label.forum.admin.ad_list" key="water_notice"/>
      <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<lt:Label res="res.label.forum.admin.ad_list" key="pic_src"/><%out.print(Global.getRealPath() + "images/err_prv.gif");%></p>
     </td>
    </tr>
</form> 
</table>
</body>
</html>
