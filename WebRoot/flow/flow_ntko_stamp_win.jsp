<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.stamp.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>图片印章密码</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style4 {
	color: #FFFFFF;
	font-weight: bold;
}
-->
</style>
</head>
<body>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 窗口不能做成模式对话框，因为form及location.href都会弹出新窗口
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // sign 来自于图片签名的调用

int stampId = StrUtil.toInt(ParamUtil.get(request, "stampId"));
StampDb stamp = new StampDb();
if (stampId!=-1) {
	stamp = stamp.getStampDb(stampId);
}
String filename = stamp.getImage();
String link = Global.getRealPath() + "upfile/" + stamp.linkBasePath + "/" + filename ; 
String link1 = stamp.getImageUrl(request);
UserDb user = new UserDb();
user = user.getUserDb(privilege.getUser(request));

if (op.equals("getstamp")) {
	String pwd = ParamUtil.get(request, "pwd");
	UserDb ud = new UserDb();
	ud = ud.getUserDb(privilege.getUser(request));
	if (ud.getPwdRaw().equals(pwd)) {
		StampLogDb sld = new StampLogDb();
		sld.create(new com.cloudwebsoft.framework.db.JdbcTemplate(), new Object[]{new Long(com.redmoon.oa.db.SequenceManager.nextID(com.redmoon.oa.db.SequenceManager.OA_STAMP_LOG)),user.getName(),new Integer(stampId),new java.util.Date(),StrUtil.getIp(request)});
	%>
		<script>
		<%if (action.equals("sign")) {%>
		window.opener.insertSignImg("<%=link1%>"); 
		<%}else{%>
		window.opener.AddPictureFromURL("<%=link1%>"); 
		<%}%>
		window.close();
		</script>
	<%
		return;
	}
	else {
		out.print(StrUtil.Alert("密码错误！"));
	}
}
%>
<table class="tabStyle_1 percent98" width="100%" cellPadding="0" cellSpacing="0">
  <thead>
    <tr>
      <td height="28" class="right-title">&nbsp;请输入密码</td>
    </tr>
  </thead>
  <tbody>
  <form name="form1" action="?op=getstamp&stampId=<%=stampId%>" method="post">
    <tr>
      <td height="40" align="center" bgcolor="#FFFFFF" class="head">
	  <input name="pwd" type="password">
	  &nbsp;&nbsp;
      <input class="btn" type="submit" value="签名">
      <input name="action" value="<%=action%>" type="hidden" />      
      </td></tr>
  </form>
</table>
</body>
</html>