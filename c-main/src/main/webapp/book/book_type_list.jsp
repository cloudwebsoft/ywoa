<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.book.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="book.all";
if (!privilege.isUserPrivValid(request, priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>添加图书类别</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language="JavaScript" type="text/JavaScript">
<!--
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=150,left=220,width="+width+",height="+height);
}
//-->
</script>
<script src="../inc/common.js"></script>
</head>
<body>
<%@ include file="book_inc_menu_top.jsp"%>
<script>
$("menu4").className="current";
</script>
<%
if (op.equals("add")) {
	BookTypeMgr btm = new BookTypeMgr();
	boolean re = false;
	try {
		  re = btm.create(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert("操作成功！","提示"));
}

if (op.equals("del")) {
	BookTypeMgr btm = new BookTypeMgr();
	boolean re = false;
	try {
		re = btm.del(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert("操作成功！","提示"));
}
 %>
<div class="spacerH"></div>
<table align="center" class="tabStyle_1 percent60">
	<form id=form1 name="form1" action="?op=add" method=post>
	<tr>
		<td colspan="3" class="tabStyle_1_title">图书类别管理</td>
	</tr>
<%
	BookTypeDb btd = new BookTypeDb();
	String sql = "select id from book_type";
	Iterator ir = btd.list(sql).iterator();
	while (ir.hasNext()) {
		btd = (BookTypeDb)ir.next();
%>
	<tr>
		<td><%=btd.getName()%></td>
		<td><a href="book_type_edit.jsp?id=<%=btd.getId()%>">编辑</a></td>
		<td><a href="#" onClick="jConfirm('您确定要删除<%=btd.getName()%>吗？','提示',function(r){ if(!r){return;}else{ window.location.href='?op=del&id=<%=btd.getId()%>'}}) ">删除</a></td>
	</tr>
<%}%>

    <tr>
		<td colspan="3" align="center">图书类别名称：
        	<input name="name" width="200">
        	&nbsp;&nbsp;&nbsp;&nbsp;
        	<input name="submit" type=submit class="btn" value="添  加"></td>
    </tr>
	</form>
</table>
</body>
</html>
