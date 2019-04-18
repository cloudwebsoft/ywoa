<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划类型管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	PersonGroupTypeMgr wptm = new PersonGroupTypeMgr();
	boolean re = false;
	try {
		re = wptm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_type_list.jsp"));
	return;
}

if (op.equals("del")) {
	PersonGroupTypeMgr wptm = new PersonGroupTypeMgr();
	boolean re = false;
	try {
		re = wptm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！", "提示", "persongroup_type_list.jsp"));
	return;
}
%>
<body>
<%@ include file="persongroup_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60" width="80%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="2" class="tabStyle_1_title">用户组</td>
  </tr>
  <tr>
    <td colspan="2" align="center"><form id=form1 name="form1" action="?op=add" method=post>
        类型名称：
        <input name="name" maxlength="20">
		序号：
		<input name="orders" size="3" value="1" />
        &nbsp;
        <input class="btn" name="submit" type=submit value="添加">
    </form></td>
  </tr>
  <%
			  PersonGroupTypeDb pgtd = new PersonGroupTypeDb();
			  String sql = "select id from " + pgtd.getTableName() + " where user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + " order by orders";
			  Iterator ir = pgtd.list(sql).iterator();
			  while (ir.hasNext()) {
			  	pgtd = (PersonGroupTypeDb)ir.next();%>
  <tr>
    <td width="76%"><%=pgtd.getName()%></td>
    <td width="24%" align="center"><a href="persongroup_type_edit.jsp?id=<%=pgtd.getId()%>">编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" onclick="jConfirm('您确定要删除么？', '提示', function(r){if(!r){return;}else{location.href='?op=del&id=<%=pgtd.getId()%>';}})" >删除</a></td>
  </tr>
  <%}%>
</table>
</body>
</html>
