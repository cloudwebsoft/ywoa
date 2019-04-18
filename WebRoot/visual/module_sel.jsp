<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>模块选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
// 当从模式对话框打开本窗口时，因为分属于不同的IE进程，SESSION会丢失，可以用cookie中置sessionId来解决这个问题
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	// out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	// return;
}
%>
<table class="tabStyle_1" style="padding:0px; margin:0px;" width="100%" cellPadding="0" cellSpacing="0">
  <tbody>
    <tr>
      <td height="28" class="tabStyle_1_title">&nbsp;请选择：</td>
    </tr>
    <tr>
      <td height="42" align="center"><%
ModuleSetupDb msd = new ModuleSetupDb();
// String sql = "select code from visual_module_setup where is_use=1 order by code";
String sql = "select code from visual_module_setup order by code";
Iterator mir = msd.list(sql).iterator();
String opts = "";
FormDb fd = new FormDb();
while (mir.hasNext()) {
	msd = (ModuleSetupDb)mir.next();
	fd = fd.getFormDb(msd.getString("code"));
	if (fd.isLoaded())
		opts += "<option value='" + msd.getString("code") + "'>" + fd.getName() + "</option>";
}
%>
<select name="sel" style="width:200px">
<%=opts%>
</select>
&nbsp;&nbsp;<input type="button" value="确定" onClick="doSel()"></td>
    </tr>
  </tbody>
</table>
</body>
<script language="javascript">
<!--
function doSel() {
	window.opener.setSequence(sel.options[sel.selectedIndex].value, sel.options[sel.selectedIndex].text);
	window.close();
}
//-->
</script>
</html>