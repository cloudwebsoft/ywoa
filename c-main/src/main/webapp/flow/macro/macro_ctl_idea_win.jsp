<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>意见</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="../inc/common.js"></script>
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
if (op.equals("idea")) {
	String idea = ParamUtil.get(request, "idea");
	
	try {
		com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "idea", idea, getClass().getName());
	}
	catch (ErrMsgException e) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
	
	UserDb ud = new UserDb();
	ud = ud.getUserDb(privilege.getUser(request));
	String str = "\r\n\r\n" + idea + "\r\n         " + ud.getRealName() + "   " + cn.js.fan.util.DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss") + "\r\n";
	%>
		<textarea id="idea" name="idea" style="display:none"><%=str%></textarea>
		<script>
		window.opener.setIntpuObjValue(idea.value);
		window.close();
		</script>
	<%
	return;
}
%>
<table class="tabStyle_1 percent98" width="100%" cellPadding="0" cellSpacing="0">
  <thead>
    <tr>
      <td height="28" class="right-title">&nbsp;请输入意见</td>
    </tr>
  </thead>
  <tbody>
  <form name="form1" action="?op=idea" method="post">
    <tr>
      <td height="20" align="center" class="head"><textarea id="idea" name="idea" cols="50" rows="8"></textarea>
&nbsp;&nbsp; <br>
<input class="btn" name="submit1" type="submit" value=" 确 定 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input class="btn" name="submit2" type="button" value=" 取 消 " onClick="window.close()"></td>
    </tr>
  </form>
</table>
</body>
</html>