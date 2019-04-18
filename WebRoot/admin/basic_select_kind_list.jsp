<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="admin";
if (!privilege.isUserPrivValid(request, priv)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>基础数据类型管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%@ include file="basic_select_inc_menu_top.jsp"%>
<%
if (op.equals("add")) {
	SelectKindMgr wptm = new SelectKindMgr();
	boolean re = false;
	try {
		re = wptm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "basic_select_kind_list.jsp"));
	return;
}

if (op.equals("del")) {
	SelectKindMgr wptm = new SelectKindMgr();
	boolean re = false;
	try {
		re = wptm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "basic_select_kind_list.jsp"));
	return;
}
%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="3" class="tabStyle_1_title">类型</td>
  </tr>
  <tr>
    <td colspan="3" align="center">
    	<form id=form1 name="form1" action="?op=add" method=post>
		序号 
		<input name="orders" size="3" />
        名称 
        <input name="name" maxlength="20" /> 
        &nbsp;
        <input class="btn" name="submit" type=submit value="添加" />
        &nbsp;&nbsp;
    </form>
    </td>
  </tr>
  <%
	SelectKindDb wptd = new SelectKindDb();
	Iterator ir = wptd.list().iterator();
	while (ir.hasNext()) {
	  wptd = (SelectKindDb)ir.next();
  %>
  <tr>
    <td width="7%" align="center">
    <%=wptd.getOrders()%>
    </td>
    <td width="70%"><a href="basic_select_list.jsp?kind=<%=wptd.getId()%>"><%=wptd.getName()%></a></td>
    <td width="23%" align="center">
    <a href="basic_select_kind_edit.jsp?id=<%=wptd.getId()%>">编辑</a>
    &nbsp;&nbsp;
    <a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='?op=del&id=<%=wptd.getId()%>'}})" >删除</a>
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="addTab('<%=wptd.getName()%>权限', '<%=request.getContextPath()%>/admin/basic_select_kind_priv_m.jsp?kindId=<%=wptd.getId()%>')">权限</a>
    </td>
  </tr>
  <%}%>
</table>
</body>
</html>
