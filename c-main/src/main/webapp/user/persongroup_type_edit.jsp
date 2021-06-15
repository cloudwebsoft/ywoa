<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划类型管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="persongroup_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	PersonGroupTypeMgr wptm = new PersonGroupTypeMgr();
	boolean re = false;
	try {
		re = wptm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(), "提示"));
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_type_edit.jsp?id=" + id));
		return;
	}
}

PersonGroupTypeDb wptd = new PersonGroupTypeDb();
wptd = wptd.getPersonGroupTypeDb(id);
%>
<table width="494" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent60">
  <tr> 
    <td height="23" class="tabStyle_1_title">用户组</td>
  </tr>
  <tr> 
    <td align="center">
	  <form action="?op=modify" method=post> 
		名称：
	    <input name="name" value="<%=wptd.getName()%>" maxlength="30">
		  <input name="id" value="<%=id%>" type=hidden>
		  序号：<input name="orders" value="<%=wptd.getOrders()%>" size="3">
		  &nbsp;
		  <input class="btn" name="submit" type=submit value="确定">
	  </form>
	</td>
  </tr>
</table>
<br>
<br>
</body>
</html>
